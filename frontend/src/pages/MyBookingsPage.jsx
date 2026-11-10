import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { QRCodeSVG, QRCodeCanvas } from 'qrcode.react';
import { jsPDF } from 'jspdf';
import { bookingService } from '../services/bookingService';
import { refundService } from '../services/refundService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { PaymentModal } from '../components/PaymentModal';
import {
  Ticket,
  Calendar,
  Clock,
  MapPin,
  Film,
  Armchair,
  ArrowRight,
  ShieldCheck,
  X,
  AlertCircle,
  RefreshCw,
  QrCode,
  Ban,
  CreditCard,
  CheckCircle,
  CheckCircle2,
  Printer,
  Download,
  Sparkles,
  Info,
  RotateCcw,
  Receipt,
  FileText,
  AlertTriangle,
  ChevronRight
} from 'lucide-react';

export function MyBookingsPage() {
  const [activeTab, setActiveTab] = useState('bookings'); // 'bookings' | 'refunds'
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

  // Refund history state
  const [refunds, setRefunds] = useState([]);
  const [refundsLoading, setRefundsLoading] = useState(false);
  const [refundsError, setRefundsError] = useState(null);

  // Cancellation modal state
  const [cancellingBooking, setCancellingBooking] = useState(null);
  const [cancellationPreview, setCancellationPreview] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [simulateAutoProcess, setSimulateAutoProcess] = useState(true);
  const [submittingCancel, setSubmittingCancel] = useState(false);
  const [cancelResult, setCancelResult] = useState(null);

  // Policy modal state
  const [showPolicyModal, setShowPolicyModal] = useState(false);
  const [policies, setPolicies] = useState([]);
  const [policiesLoading, setPoliciesLoading] = useState(false);

  // Ticket viewing modal state
  const [selectedBookingForTickets, setSelectedBookingForTickets] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [ticketsLoading, setTicketsLoading] = useState(false);
  const [ticketsError, setTicketsError] = useState(null);

  // Resume payment modal state
  const [pendingPaymentBooking, setPendingPaymentBooking] = useState(null);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await bookingService.getMyBookings();
      setBookings(data);
    } catch (err) {
      setError(err.message || 'Unable to retrieve your bookings.');
    } finally {
      setLoading(false);
    }
  };

  const fetchRefunds = async () => {
    try {
      setRefundsLoading(true);
      setRefundsError(null);
      const data = await refundService.getMyRefunds();
      setRefunds(data || []);
    } catch (err) {
      setRefundsError(err.message || 'Unable to retrieve refund history.');
    } finally {
      setRefundsLoading(false);
    }
  };

  const fetchPolicies = async () => {
    try {
      setPoliciesLoading(true);
      const data = await refundService.getPublicPolicies();
      setPolicies(data || []);
    } catch (_) {
    } finally {
      setPoliciesLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
    fetchRefunds();
    fetchPolicies();
  }, []);

  const handleOpenCancelModal = async (booking) => {
    if (booking.status === 'PENDING_PAYMENT') {
      if (!window.confirm(`Are you sure you want to cancel pending reservation ${booking.bookingReference}? Held seats will be released immediately.`)) return;
      try {
        setActionLoading(booking.id);
        await bookingService.cancelBooking(booking.id);
        await fetchBookings();
      } catch (err) {
        alert(`Cancellation failed: ${err.message}`);
      } finally {
        setActionLoading(null);
      }
      return;
    }

    setCancellingBooking(booking);
    setCancellationPreview(null);
    setPreviewError(null);
    setCancelReason('');
    setCancelResult(null);
    setPreviewLoading(true);

    try {
      const preview = await refundService.getCancellationPreview(booking.id);
      setCancellationPreview(preview);
    } catch (err) {
      setPreviewError(err.message || 'Unable to calculate cancellation preview.');
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleConfirmCancellation = async () => {
    if (!cancellingBooking) return;
    try {
      setSubmittingCancel(true);
      const result = await refundService.requestCancellation(cancellingBooking.id, {
        reason: cancelReason,
        simulateAutoProcess
      });
      setCancelResult(result);
      await fetchBookings();
      fetchRefunds();
    } catch (err) {
      alert(`Cancellation failed: ${err.message}`);
    } finally {
      setSubmittingCancel(false);
    }
  };

  const handleViewTickets = async (booking) => {
    try {
      setSelectedBookingForTickets(booking);
      setTicketsLoading(true);
      setTicketsError(null);
      const data = await bookingService.getBookingTickets(booking.id);
      setTickets(data);
    } catch (err) {
      setTicketsError(err.message || 'Failed to fetch tickets for this reservation.');
    } finally {
      setTicketsLoading(false);
    }
  };

  const handleDownloadPDF = (singleTicket = null) => {
    try {
      const doc = new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'a4'
      });

      const ticketsToPrint = singleTicket ? [singleTicket] : tickets;
      const booking = selectedBookingForTickets;

      ticketsToPrint.forEach((t, index) => {
        if (index > 0) {
          doc.addPage();
        }

        const movieTitle = t.movieTitle || booking?.movieTitle || 'Movie Ticket';
        const cinemaName = t.cinemaName || booking?.cinemaName || 'CineSmart Central';
        const screenName = t.screenName || (booking?.screenName ? booking.screenName : `Screen ${t.screenNumber || 1}`);
        const sTime = t.showStartTime ? new Date(t.showStartTime) : (booking?.showStartTime ? new Date(booking.showStartTime) : null);
        const timeStr = sTime ? sTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'TBD';
        const dateStr = sTime ? sTime.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : 'TBD';
        const seatLabel = t.seatLabel || 'General';
        const seatTier = t.seatTier || 'STANDARD';
        const rawPrice = t.seatPrice != null ? t.seatPrice : (t.price != null ? t.price : (booking?.totalAmount ? (booking.totalAmount / (tickets.length || 1)) : 200));
        const priceStr = `INR ${Number(rawPrice || 0).toFixed(2)}`;
        const ticketCode = t.ticketCode || 'TKT-CINESMART';
        const tokenHash = t.qrCodeHash || t.qrVerificationHash || 'SECURE-SHA256-TOKEN';

        // Outer Dark Container
        doc.setFillColor(15, 23, 42); // slate-900
        doc.roundedRect(15, 20, 180, 110, 4, 4, 'F');

        // Header Top Bar - CineSmart Red
        doc.setFillColor(229, 9, 20);
        doc.roundedRect(15, 20, 180, 14, 4, 4, 'F');
        doc.rect(15, 26, 180, 8, 'F');

        doc.setTextColor(255, 255, 255);
        doc.setFontSize(11);
        doc.setFont('helvetica', 'bold');
        doc.text('CINESMART DIGITAL CINEMA PASS', 22, 29);

        doc.setFontSize(9);
        doc.setFont('helvetica', 'normal');
        doc.text('OFFICIAL E-TICKET', 155, 29);

        // Movie Title
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(16);
        doc.setFont('helvetica', 'bold');
        doc.text(movieTitle, 22, 45);

        // Cinema & Screen
        doc.setTextColor(203, 213, 225); // slate-300
        doc.setFontSize(10);
        doc.setFont('helvetica', 'normal');
        doc.text(`${cinemaName}  |  ${screenName}`, 22, 53);

        // Date & Showtime
        doc.setTextColor(6, 182, 212); // cyan-500
        doc.setFontSize(10);
        doc.setFont('helvetica', 'bold');
        doc.text(`${dateStr}  at  ${timeStr}`, 22, 60);

        // Details Grid Box
        doc.setFillColor(30, 41, 59); // slate-800
        doc.roundedRect(22, 66, 110, 38, 2, 2, 'F');

        // Seat & Tier
        doc.setTextColor(148, 163, 184); // slate-400
        doc.setFontSize(8);
        doc.setFont('helvetica', 'normal');
        doc.text('SEAT & TIER', 26, 73);
        doc.setTextColor(245, 158, 11); // amber-500
        doc.setFontSize(11);
        doc.setFont('helvetica', 'bold');
        doc.text(`${seatLabel} (${seatTier})`, 26, 79);

        // Ticket Code
        doc.setTextColor(148, 163, 184);
        doc.setFontSize(8);
        doc.setFont('helvetica', 'normal');
        doc.text('TICKET CODE', 26, 88);
        doc.setTextColor(6, 182, 212);
        doc.setFontSize(10);
        doc.setFont('courier', 'bold');
        doc.text(ticketCode, 26, 94);

        // Price
        doc.setTextColor(148, 163, 184);
        doc.setFontSize(8);
        doc.setFont('helvetica', 'normal');
        doc.text('PRICE', 80, 73);
        doc.setTextColor(16, 185, 129); // emerald-500
        doc.setFontSize(11);
        doc.setFont('helvetica', 'bold');
        doc.text(priceStr, 80, 79);

        // Booking Reference
        doc.setTextColor(148, 163, 184);
        doc.setFontSize(8);
        doc.setFont('helvetica', 'normal');
        doc.text('BOOKING REF', 80, 88);
        doc.setTextColor(255, 255, 255);
        doc.setFontSize(9);
        doc.setFont('helvetica', 'bold');
        doc.text(booking?.bookingReference || 'CONFIRMED', 80, 94);

        // QR Code Box on right
        doc.setFillColor(255, 255, 255);
        doc.roundedRect(138, 43, 49, 49, 2, 2, 'F');

        // Capture QR Code from hidden canvas
        const canvas = document.getElementById(`qr-canvas-${t.ticketCode}`);
        if (canvas) {
          try {
            const qrImgData = canvas.toDataURL('image/png');
            doc.addImage(qrImgData, 'PNG', 140.5, 45.5, 44, 44);
          } catch (_) {}
        }

        doc.setTextColor(100, 116, 139);
        doc.setFontSize(7);
        doc.setFont('helvetica', 'bold');
        doc.text('SCAN FOR ENTRY', 148, 97);

        // Footer Token Strip
        doc.setFillColor(10, 14, 23);
        doc.rect(15, 116, 180, 14, 'F');

        doc.setTextColor(148, 163, 184);
        doc.setFontSize(7);
        doc.setFont('courier', 'normal');
        doc.text(`SECURITY TOKEN: ${tokenHash.substring(0, 42)}...`, 22, 124);
        doc.text(`STATUS: ${t.status || 'ACTIVE'}`, 155, 124);

        // Instructions note
        doc.setTextColor(100, 116, 139);
        doc.setFontSize(8);
        doc.setFont('helvetica', 'italic');
        doc.text('Please present this digital pass or printed copy at cinema turnstile for QR entry verification.', 22, 140);
      });

      const filename = singleTicket
        ? `CineSmart-Ticket-${singleTicket.ticketCode}.pdf`
        : `CineSmart-Tickets-${booking?.bookingReference || 'passes'}.pdf`;

      doc.save(filename);
    } catch (err) {
      console.error('Failed to generate PDF directly:', err);
      // Fallback to browser print if client-side pdf encounters any issue
      window.print();
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="badge badge-green">Confirmed</span>;
      case 'PENDING_PAYMENT':
        return <span className="badge badge-gold">Pending Payment</span>;
      case 'PAYMENT_FAILED':
        return <span className="badge badge-red">Payment Failed</span>;
      case 'CANCELLED':
        return <span className="badge badge-red">Cancelled</span>;
      case 'REFUNDED':
        return <span className="badge badge-cyan">Refunded</span>;
      default:
        return <span className="badge">{status}</span>;
    }
  };

  const getRefundStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return (
          <span className="badge badge-green" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            <CheckCircle2 size={12} />
            <span>Refund Completed</span>
          </span>
        );
      case 'REQUESTED':
        return (
          <span className="badge badge-gold" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            <Clock size={12} />
            <span>Under Review</span>
          </span>
        );
      case 'APPROVED':
        return (
          <span className="badge badge-cyan" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            <ShieldCheck size={12} />
            <span>Approved</span>
          </span>
        );
      case 'PROCESSING':
        return (
          <span
            className="badge"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.3rem',
              background: 'rgba(168, 85, 247, 0.15)',
              color: '#c084fc',
              borderColor: 'rgba(168, 85, 247, 0.3)'
            }}
          >
            <RefreshCw size={12} className="spin" />
            <span>Processing</span>
          </span>
        );
      case 'REJECTED':
        return (
          <span className="badge badge-red" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            <Ban size={12} />
            <span>Rejected</span>
          </span>
        );
      case 'FAILED':
        return (
          <span className="badge badge-red" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
            <AlertCircle size={12} />
            <span>Failed</span>
          </span>
        );
      default:
        return <span className="badge">{status}</span>;
    }
  };

  if (loading) {
    return <LoadingSpinner message="Retrieving your reservation history..." />;
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Header with Title & Policy Link */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
            <Ticket size={28} color="var(--accent-red)" />
            <h1 style={{ fontSize: '2.2rem', fontWeight: 800 }}>My Bookings & Refunds</h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1rem', margin: 0 }}>
            Review your digital passes, manage reservations, and track refund requests in real-time.
          </p>
        </div>

        <button
          onClick={() => setShowPolicyModal(true)}
          className="btn-secondary"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', padding: '0.5rem 0.95rem', fontSize: '0.85rem' }}
        >
          <Info size={15} color="var(--accent-cyan)" />
          <span>Cancellation Policies</span>
        </button>
      </div>

      {/* Tab Switcher */}
      <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--bg-glass)', padding: '0.35rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', marginBottom: '1.75rem' }}>
        <button
          onClick={() => setActiveTab('bookings')}
          className={activeTab === 'bookings' ? 'btn-primary' : 'btn-secondary'}
          style={{ padding: '0.55rem 1.15rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.45rem' }}
        >
          <Ticket size={16} />
          <span>My Reservations ({bookings.length})</span>
        </button>
        <button
          onClick={() => setActiveTab('refunds')}
          className={activeTab === 'refunds' ? 'btn-primary' : 'btn-secondary'}
          style={{ padding: '0.55rem 1.15rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.45rem' }}
        >
          <RotateCcw size={16} />
          <span>Refund Tracking ({refunds.length})</span>
        </button>
      </div>

      {/* TAB 1: RESERVATIONS */}
      {activeTab === 'bookings' && (
        <>
          {error ? (
            <ErrorMessage message={error} onRetry={fetchBookings} />
          ) : bookings.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3.5rem 2rem', textAlign: 'center' }}>
              <Film size={48} color="var(--text-muted)" style={{ margin: '0 auto 1.25rem' }} />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 700, marginBottom: '0.5rem' }}>No Bookings Yet</h2>
              <p style={{ color: 'var(--text-secondary)', maxWidth: '440px', margin: '0 auto 2rem' }}>
                You haven't reserved any tickets yet. Explore now showing movies and pick your preferred seats!
              </p>
              <Link to="/movies" className="btn-primary" style={{ display: 'inline-flex', padding: '0.8rem 1.75rem' }}>
                <span>Explore Movies</span>
                <ArrowRight size={16} />
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {bookings.map((booking) => {
                const showTime = booking.showStartTime ? new Date(booking.showStartTime) : null;
                const timeFormatted = showTime ? showTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'TBD';
                const dateFormatted = showTime ? showTime.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : 'TBD';
                const isConfirmed = booking.status === 'CONFIRMED';
                const isPending = booking.status === 'PENDING_PAYMENT';
                const isCancellable = isConfirmed || isPending;

                return (
                  <div
                    key={booking.id}
                    className="glass-panel"
                    style={{
                      padding: '1.75rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '1.25rem',
                      borderLeft: isConfirmed
                        ? '4px solid var(--accent-green)'
                        : isPending
                        ? '4px solid var(--accent-gold)'
                        : booking.status === 'REFUNDED'
                        ? '4px solid var(--accent-cyan)'
                        : '4px solid var(--accent-red)'
                    }}
                  >
                    {/* Header with Reference & Status */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', display: 'block' }}>
                          Booking Reference
                        </span>
                        <span style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--accent-gold)' }}>
                          {booking.bookingReference}
                        </span>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        {getStatusBadge(booking.status)}
                      </div>
                    </div>

                    {/* Details Grid */}
                    <div style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                      gap: '1.25rem',
                      padding: '1.25rem',
                      background: 'var(--bg-glass)',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--border-subtle)'
                    }}>
                      <div>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
                          Movie
                        </span>
                        <div style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                          {booking.movieTitle}
                        </div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
                          Theater & Screen
                        </span>
                        <div style={{ fontSize: '0.95rem', fontWeight: 600 }}>
                          {booking.cinemaName} &bull; {booking.screenName}
                        </div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
                          Showtime
                        </span>
                        <div style={{ fontSize: '0.95rem', fontWeight: 600 }}>
                          {dateFormatted} at {timeFormatted}
                        </div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.25rem' }}>
                          {isConfirmed ? 'Total Paid' : 'Total Amount'}
                        </span>
                        <div style={{ fontSize: '1.15rem', fontWeight: 800, color: isConfirmed ? 'var(--accent-green)' : 'var(--text-primary)' }}>
                          ₹{Number(booking.totalAmount).toFixed(2)}
                        </div>
                      </div>
                    </div>

                    {/* Seats List */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        <Armchair size={15} />
                        <span>Seats:</span>
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                        {booking.seats?.map((seat) => (
                          <span
                            key={seat.id}
                            style={{
                              padding: '0.25rem 0.6rem',
                              borderRadius: 'var(--radius-sm)',
                              background: 'rgba(255, 255, 255, 0.06)',
                              border: '1px solid var(--border-subtle)',
                              fontSize: '0.85rem',
                              fontWeight: 700,
                              color: 'var(--text-primary)'
                            }}
                          >
                            {seat.rowIdentifier}{seat.columnNumber} ({seat.seatTier})
                          </span>
                        ))}
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div style={{
                      display: 'flex',
                      justifyContent: 'flex-end',
                      alignItems: 'center',
                      gap: '0.75rem',
                      borderTop: '1px solid var(--border-subtle)',
                      paddingTop: '1rem',
                      flexWrap: 'wrap'
                    }}>
                      {isConfirmed && (
                        <button
                          onClick={() => handleViewTickets(booking)}
                          className="btn-primary"
                          style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
                        >
                          <Ticket size={15} />
                          <span>View Tickets</span>
                        </button>
                      )}

                      {booking.status === 'REFUNDED' && (
                        <button
                          onClick={() => setActiveTab('refunds')}
                          className="btn-secondary"
                          style={{ padding: '0.5rem 1rem', fontSize: '0.85rem', color: 'var(--accent-cyan)', borderColor: 'rgba(6, 182, 212, 0.4)' }}
                        >
                          <RotateCcw size={15} />
                          <span>Track Refund</span>
                        </button>
                      )}

                      {isPending && (
                        <button
                          onClick={() => setPendingPaymentBooking(booking)}
                          className="btn-primary"
                          style={{ padding: '0.5rem 1rem', fontSize: '0.85rem', background: 'var(--accent-gold)' }}
                        >
                          <CreditCard size={15} />
                          <span>Complete Payment</span>
                        </button>
                      )}

                      {isCancellable && (
                        <button
                          onClick={() => handleOpenCancelModal(booking)}
                          disabled={actionLoading === booking.id}
                          className="btn-secondary"
                          style={{
                            padding: '0.5rem 1rem',
                            fontSize: '0.85rem',
                            borderColor: 'rgba(239, 68, 68, 0.4)',
                            color: 'var(--accent-red)'
                          }}
                        >
                          <Ban size={15} />
                          <span>{actionLoading === booking.id ? 'Processing...' : 'Cancel Booking'}</span>
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </>
      )}

      {/* TAB 2: REFUND TRACKING & HISTORY */}
      {activeTab === 'refunds' && (
        <div>
          {refundsLoading ? (
            <LoadingSpinner message="Loading your refund history..." />
          ) : refundsError ? (
            <ErrorMessage message={refundsError} onRetry={fetchRefunds} />
          ) : refunds.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3.5rem 2rem', textAlign: 'center' }}>
              <RotateCcw size={48} color="var(--text-muted)" style={{ margin: '0 auto 1.25rem', opacity: 0.5 }} />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 700, marginBottom: '0.5rem' }}>No Refund Records</h2>
              <p style={{ color: 'var(--text-secondary)', maxWidth: '440px', margin: '0 auto' }}>
                You have not requested any cancellations or refunds yet. All future cancellations will be tracked here.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {refunds.map((ref) => {
                const reqDate = ref.requestedAt ? new Date(ref.requestedAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : 'N/A';
                const procDate = ref.processedAt ? new Date(ref.processedAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : null;

                return (
                  <div
                    key={ref.id}
                    className="glass-panel"
                    style={{
                      padding: '1.5rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '1rem',
                      borderLeft: ref.status === 'COMPLETED'
                        ? '4px solid var(--accent-green)'
                        : ref.status === 'REJECTED'
                        ? '4px solid var(--accent-red)'
                        : '4px solid var(--accent-gold)'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.75rem' }}>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.08em', display: 'block' }}>
                          Refund Reference
                        </span>
                        <span style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--accent-cyan)' }}>
                          {ref.refundReference}
                        </span>
                      </div>
                      <div>
                        {getRefundStatusBadge(ref.status)}
                      </div>
                    </div>

                    <div style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
                      gap: '1rem',
                      padding: '1rem',
                      background: 'var(--bg-glass)',
                      borderRadius: 'var(--radius-md)',
                      border: '1px solid var(--border-subtle)'
                    }}>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Movie & Booking</span>
                        <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{ref.movieTitle || 'Movie'}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Booking: {ref.bookingReference}</div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Original Paid</span>
                        <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>₹{Number(ref.originalAmount || 0).toFixed(2)}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--accent-red)' }}>Fee: -₹{Number(ref.cancellationFee || 0).toFixed(2)}</div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Refund Amount</span>
                        <div style={{ fontWeight: 800, fontSize: '1.1rem', color: 'var(--accent-green)' }}>
                          ₹{Number(ref.refundAmount || 0).toFixed(2)}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Tier: {ref.refundPercentage}% Refund</div>
                      </div>

                      <div>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Timeline</span>
                        <div style={{ fontSize: '0.82rem' }}>Requested: {reqDate}</div>
                        {procDate && <div style={{ fontSize: '0.82rem', color: 'var(--accent-green)' }}>Processed: {procDate}</div>}
                      </div>
                    </div>

                    {ref.gatewayRefundId && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.82rem', color: 'var(--text-muted)', background: 'rgba(255, 255, 255, 0.02)', padding: '0.5rem 0.75rem', borderRadius: 'var(--radius-sm)' }}>
                        <ShieldCheck size={14} color="var(--accent-green)" />
                        <span>Simulated Gateway Ref: <strong style={{ color: 'var(--text-primary)', fontFamily: 'monospace' }}>{ref.gatewayRefundId}</strong></span>
                      </div>
                    )}

                    {ref.rejectionReason && (
                      <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', padding: '0.65rem 0.85rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', color: 'var(--accent-red)' }}>
                        <strong>Rejection Reason:</strong> {ref.rejectionReason}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* Ticket Viewer Modal */}
      {selectedBookingForTickets && (
        <div
          className="modal-backdrop"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.85)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1.5rem'
          }}
        >
          <div
            className="glass-panel print-only-container"
            style={{
              maxWidth: '680px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '2rem',
              position: 'relative',
              borderRadius: 'var(--radius-lg)'
            }}
          >
            <div className="no-print" style={{ position: 'absolute', top: '1.25rem', right: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <button
                onClick={() => handleDownloadPDF()}
                className="btn-primary"
                style={{ padding: '0.45rem 0.95rem', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 700 }}
                title="Download Tickets as PDF"
              >
                <Download size={15} />
                <span>Download PDF</span>
              </button>
              <button
                onClick={() => window.print()}
                className="btn-secondary"
                style={{ padding: '0.45rem 0.75rem', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
                title="Print"
              >
                <Printer size={15} />
              </button>
              <button
                onClick={() => setSelectedBookingForTickets(null)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--text-muted)',
                  cursor: 'pointer',
                  padding: '0.25rem'
                }}
                title="Close"
              >
                <X size={22} />
              </button>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
              <Ticket size={26} color="var(--accent-red)" />
              <h2 style={{ fontSize: '1.5rem', fontWeight: 800 }}>Digital Cinema E-Tickets</h2>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.75rem' }}>
              Booking: <strong style={{ color: 'var(--accent-gold)' }}>{selectedBookingForTickets.bookingReference}</strong> &bull; {selectedBookingForTickets.movieTitle}
            </p>

            {ticketsLoading ? (
              <LoadingSpinner message="Loading authenticated digital tickets & cryptographic QR passes..." />
            ) : ticketsError ? (
              <ErrorMessage message={ticketsError} />
            ) : tickets.length === 0 ? (
              <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem 0' }}>No tickets found for this reservation.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {tickets.map((t) => {
                  const sTime = t.showStartTime ? new Date(t.showStartTime) : (selectedBookingForTickets.showStartTime ? new Date(selectedBookingForTickets.showStartTime) : null);
                  const timeStr = sTime ? sTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'TBD';
                  const dateStr = sTime ? sTime.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : 'TBD';
                  const poster = t.posterUrl || selectedBookingForTickets.posterUrl;
                  const isUsed = t.status === 'USED';
                  const isCancelled = t.status === 'CANCELLED' || t.status === 'VOIDED';

                  const qrPayload = JSON.stringify({
                    ticketCode: t.ticketCode,
                    hash: t.qrCodeHash
                  });

                  return (
                    <div
                      key={t.id || t.ticketCode}
                      className="ticket-print-card"
                      style={{
                        background: 'linear-gradient(135deg, rgba(18, 24, 38, 0.95), rgba(10, 14, 23, 0.98))',
                        border: '1px solid var(--border-subtle)',
                        borderRadius: 'var(--radius-md)',
                        overflow: 'hidden',
                        boxShadow: 'var(--shadow-md)',
                        position: 'relative'
                      }}
                    >
                      {/* Ticket Header Bar */}
                      <div
                        style={{
                          background: 'rgba(255, 255, 255, 0.04)',
                          padding: '0.75rem 1.25rem',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          borderBottom: '1px solid var(--border-subtle)'
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <Film size={16} color="var(--accent-red)" />
                          <span style={{ fontSize: '0.8rem', fontWeight: 800, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
                            CineSmart E-Pass
                          </span>
                        </div>
                        <div>
                          {isUsed ? (
                            <span className="badge badge-gold" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.72rem' }}>
                              <CheckCircle2 size={12} />
                              Admitted
                            </span>
                          ) : isCancelled ? (
                            <span className="badge badge-red" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.72rem' }}>
                              <Ban size={12} />
                              {t.status}
                            </span>
                          ) : (
                            <span className="badge badge-emerald" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.72rem' }}>
                              <ShieldCheck size={12} />
                              Active Pass
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Ticket Main Content */}
                      <div
                        style={{
                          padding: '1.25rem',
                          display: 'grid',
                          gridTemplateColumns: '1fr auto',
                          gap: '1.25rem',
                          alignItems: 'center'
                        }}
                      >
                        {/* Left Details */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                          <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                            {poster ? (
                              <img
                                src={poster}
                                alt={t.movieTitle || selectedBookingForTickets.movieTitle}
                                style={{
                                  width: '65px',
                                  height: '92px',
                                  objectFit: 'cover',
                                  borderRadius: 'var(--radius-sm)',
                                  border: '1px solid var(--border-subtle)',
                                  flexShrink: 0
                                }}
                              />
                            ) : (
                              <div
                                style={{
                                  width: '65px',
                                  height: '92px',
                                  borderRadius: 'var(--radius-sm)',
                                  background: 'rgba(255, 255, 255, 0.05)',
                                  display: 'flex',
                                  alignItems: 'center',
                                  justifyContent: 'center',
                                  flexShrink: 0
                                }}
                              >
                                <Film size={28} color="var(--accent-red)" />
                              </div>
                            )}

                            <div>
                              <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '0.35rem' }}>
                                {t.movieTitle || selectedBookingForTickets.movieTitle}
                              </h3>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', color: 'var(--text-secondary)', marginBottom: '0.25rem' }}>
                                <MapPin size={13} color="var(--accent-red)" style={{ flexShrink: 0 }} />
                                <span>{t.cinemaName || selectedBookingForTickets.cinemaName} &bull; {t.screenName || `Screen ${t.screenNumber || 1}`}</span>
                              </div>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                                <Calendar size={13} color="var(--accent-cyan)" style={{ flexShrink: 0 }} />
                                <span>{dateStr} &bull; {timeStr}</span>
                              </div>
                            </div>
                          </div>

                          {/* Seat & Price Strip */}
                          <div
                            style={{
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'space-between',
                              background: 'rgba(255, 255, 255, 0.03)',
                              border: '1px solid var(--border-subtle)',
                              borderRadius: 'var(--radius-sm)',
                              padding: '0.5rem 0.85rem',
                              flexWrap: 'wrap',
                              gap: '0.5rem'
                            }}
                          >
                            <div>
                              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'block' }}>
                                Seat & Tier
                              </span>
                              <span style={{ fontSize: '1rem', fontWeight: 800, color: 'var(--accent-gold)' }}>
                                {t.seatLabel} <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)' }}>({t.seatTier})</span>
                              </span>
                            </div>

                            <div>
                              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'block' }}>
                                Ticket Code
                              </span>
                              <span style={{ fontSize: '0.85rem', fontWeight: 800, fontFamily: 'monospace', color: 'var(--accent-cyan)' }}>
                                {t.ticketCode}
                              </span>
                            </div>

                            <div>
                              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'block' }}>
                                Price
                              </span>
                              <span style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--accent-green)' }}>
                                ₹{Number(t.seatPrice != null ? t.seatPrice : (t.price != null ? t.price : (selectedBookingForTickets?.totalAmount ? (selectedBookingForTickets.totalAmount / (tickets.length || 1)) : 200))).toFixed(2)}
                              </span>
                            </div>

                            <div className="no-print">
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDownloadPDF(t);
                                }}
                                className="btn-secondary"
                                style={{
                                  padding: '0.35rem 0.65rem',
                                  fontSize: '0.75rem',
                                  display: 'inline-flex',
                                  alignItems: 'center',
                                  gap: '0.35rem',
                                  borderRadius: 'var(--radius-sm)'
                                }}
                                title="Download this individual ticket as PDF"
                              >
                                <Download size={13} />
                                <span>Download PDF</span>
                              </button>
                            </div>
                          </div>
                        </div>

                        {/* Right QR Box */}
                        <div
                          style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            background: '#ffffff',
                            padding: '0.6rem',
                            borderRadius: 'var(--radius-sm)',
                            textAlign: 'center'
                          }}
                        >
                          <QRCodeSVG
                            value={qrPayload}
                            size={120}
                            level="H"
                            includeMargin={false}
                            bgColor="#ffffff"
                            fgColor="#0f172a"
                          />
                          {/* Hidden canvas used to extract QR as image for direct PDF export */}
                          <QRCodeCanvas
                            id={`qr-canvas-${t.ticketCode}`}
                            value={qrPayload}
                            size={160}
                            style={{ display: 'none' }}
                          />
                          <span style={{ fontSize: '0.62rem', color: '#64748b', fontWeight: 700, marginTop: '0.35rem', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                            Scan for Entry
                          </span>
                        </div>
                      </div>

                      {/* Footer Hash Bar */}
                      <div
                        style={{
                          background: 'rgba(0, 0, 0, 0.4)',
                          padding: '0.4rem 1.25rem',
                          borderTop: '1px solid var(--border-subtle)',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          fontSize: '0.68rem',
                          color: 'var(--text-muted)',
                          fontFamily: 'monospace'
                        }}
                      >
                        <span>TOKEN: {t.qrCodeHash ? t.qrCodeHash.substring(0, 24) + '...' : 'SECURE-SHA256-TOKEN'}</span>
                        {t.validatedAt && (
                          <span style={{ color: 'var(--accent-gold)' }}>
                            Validated: {new Date(t.validatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            <div className="no-print" style={{ marginTop: '1.75rem', display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
              <button
                onClick={() => handleDownloadPDF()}
                className="btn-primary"
                style={{ padding: '0.65rem 1.4rem', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700 }}
              >
                <Download size={18} />
                <span>Download All Tickets (PDF)</span>
              </button>
              <button
                onClick={() => window.print()}
                className="btn-secondary"
                style={{ padding: '0.65rem 1rem', fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}
                title="Print Tickets"
              >
                <Printer size={16} />
                <span>Print</span>
              </button>
              <button
                onClick={() => setSelectedBookingForTickets(null)}
                className="btn-secondary"
                style={{ padding: '0.65rem 1.25rem', fontSize: '0.9rem' }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Resume Pending Payment Modal */}
      {pendingPaymentBooking && (
        <PaymentModal
          booking={pendingPaymentBooking}
          onClose={() => {
            setPendingPaymentBooking(null);
            fetchBookings();
          }}
          onBookingCompleted={() => {
            setPendingPaymentBooking(null);
            fetchBookings();
          }}
        />
      )}

      {/* Cancellation & Refund Modal */}
      {cancellingBooking && (
        <div
          className="modal-backdrop"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.85)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1.5rem'
          }}
        >
          <div
            className="glass-panel"
            style={{
              maxWidth: '560px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '2rem',
              position: 'relative',
              borderRadius: 'var(--radius-lg)'
            }}
          >
            <button
              onClick={() => {
                setCancellingBooking(null);
                setCancelResult(null);
              }}
              style={{
                position: 'absolute',
                top: '1.25rem',
                right: '1.25rem',
                background: 'none',
                border: 'none',
                color: 'var(--text-muted)',
                cursor: 'pointer'
              }}
            >
              <X size={20} />
            </button>

            {cancelResult ? (
              <div style={{ textAlign: 'center', padding: '1rem 0' }}>
                <div style={{ width: '56px', height: '56px', borderRadius: '50%', background: 'rgba(34, 197, 94, 0.15)', border: '2px solid var(--accent-green)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.25rem' }}>
                  <CheckCircle size={32} color="var(--accent-green)" />
                </div>
                <h3 style={{ fontSize: '1.4rem', fontWeight: 800, marginBottom: '0.5rem' }}>
                  Booking Cancelled
                </h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginBottom: '1.5rem' }}>
                  Your reservation seats have been safely released back to availability.
                </p>

                <div style={{ background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)', padding: '1.25rem', border: '1px solid var(--border-subtle)', textAlign: 'left', marginBottom: '1.75rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Refund Reference:</span>
                    <strong style={{ color: 'var(--accent-cyan)', fontFamily: 'monospace' }}>{cancelResult.refundReference}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Refund Amount:</span>
                    <strong style={{ color: 'var(--accent-green)', fontSize: '1.1rem' }}>₹{Number(cancelResult.refundAmount || 0).toFixed(2)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Cancellation Charge:</span>
                    <span style={{ color: 'var(--accent-red)' }}>-₹{Number(cancelResult.cancellationFee || 0).toFixed(2)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Status:</span>
                    {getRefundStatusBadge(cancelResult.status)}
                  </div>
                  {cancelResult.gatewayRefundId && (
                    <div style={{ marginTop: '0.75rem', paddingTop: '0.75rem', borderTop: '1px solid var(--border-subtle)', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      Simulated Gateway Refund ID: <strong style={{ color: 'var(--text-primary)' }}>{cancelResult.gatewayRefundId}</strong>
                    </div>
                  )}
                </div>

                <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
                  <button
                    onClick={() => {
                      setCancellingBooking(null);
                      setCancelResult(null);
                      setActiveTab('refunds');
                    }}
                    className="btn-primary"
                    style={{ padding: '0.65rem 1.25rem' }}
                  >
                    View in Refund History
                  </button>
                  <button
                    onClick={() => {
                      setCancellingBooking(null);
                      setCancelResult(null);
                    }}
                    className="btn-secondary"
                    style={{ padding: '0.65rem 1.25rem' }}
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : previewLoading ? (
              <div style={{ padding: '3rem 1rem', textAlign: 'center' }}>
                <LoadingSpinner message="Calculating cancellation refund & policy rules..." />
              </div>
            ) : previewError ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', color: 'var(--accent-red)', marginBottom: '1rem' }}>
                  <AlertCircle size={24} />
                  <h3 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Cancellation Unavailable</h3>
                </div>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>{previewError}</p>
                <button onClick={() => setCancellingBooking(null)} className="btn-secondary" style={{ width: '100%' }}>
                  Close
                </button>
              </div>
            ) : cancellationPreview && !cancellationPreview.eligible ? (
              <div>
                <div style={{ width: '50px', height: '50px', borderRadius: '50%', background: 'rgba(239, 68, 68, 0.15)', border: '2px solid var(--accent-red)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1.25rem' }}>
                  <Ban size={28} color="var(--accent-red)" />
                </div>
                <h3 style={{ fontSize: '1.3rem', fontWeight: 800, textAlign: 'center', marginBottom: '0.5rem' }}>
                  Cancellation Not Allowed
                </h3>
                <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', padding: '1rem', borderRadius: 'var(--radius-md)', color: 'var(--text-primary)', fontSize: '0.9rem', marginBottom: '1.5rem', textAlign: 'center' }}>
                  {cancellationPreview.ineligibilityReason}
                </div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textAlign: 'center', marginBottom: '1.5rem' }}>
                  Under CineSmart policy, bookings with used tickets, past shows, or existing cancellations cannot be refunded.
                </p>
                <button onClick={() => setCancellingBooking(null)} className="btn-secondary" style={{ width: '100%' }}>
                  Close
                </button>
              </div>
            ) : cancellationPreview ? (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                  <RotateCcw size={22} color="var(--accent-red)" />
                  <h3 style={{ fontSize: '1.3rem', fontWeight: 800 }}>Confirm Booking Cancellation</h3>
                </div>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                  Booking {cancellationPreview.bookingReference} &bull; {cancellationPreview.movieTitle}
                </p>

                <div style={{ background: 'rgba(6, 182, 212, 0.08)', border: '1px solid rgba(6, 182, 212, 0.3)', borderRadius: 'var(--radius-md)', padding: '0.85rem 1rem', marginBottom: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                    <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--accent-cyan)' }}>
                      {cancellationPreview.policyName}
                    </span>
                    <span className="badge badge-green" style={{ fontSize: '0.75rem' }}>
                      {cancellationPreview.refundPercentage}% Refund Eligible
                    </span>
                  </div>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: 0 }}>
                    {cancellationPreview.policyDescription} ({cancellationPreview.hoursRemaining} hours before showtime)
                  </p>
                </div>

                <div style={{ background: 'var(--bg-glass)', borderRadius: 'var(--radius-md)', padding: '1rem', border: '1px solid var(--border-subtle)', marginBottom: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.45rem', fontSize: '0.9rem' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Original Amount Paid:</span>
                    <span>₹{Number(cancellationPreview.paidAmount).toFixed(2)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.45rem', fontSize: '0.9rem' }}>
                    <span style={{ color: 'var(--text-muted)' }}>Cancellation Charges ({100 - Number(cancellationPreview.refundPercentage)}%):</span>
                    <span style={{ color: 'var(--accent-red)' }}>-₹{Number(cancellationPreview.cancellationFee).toFixed(2)}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: '0.65rem', borderTop: '1px solid var(--border-subtle)', fontSize: '1.05rem', fontWeight: 800 }}>
                    <span style={{ color: 'var(--text-primary)' }}>Estimated Refund:</span>
                    <span style={{ color: 'var(--accent-green)' }}>₹{Number(cancellationPreview.estimatedRefundAmount).toFixed(2)}</span>
                  </div>
                </div>

                <div style={{ background: 'rgba(255, 255, 255, 0.03)', border: '1px solid var(--border-subtle)', borderRadius: 'var(--radius-md)', padding: '0.75rem 1rem', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                  <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>Policy Reversals on Cancellation:</div>
                  <div>&bull; Reserved seats will be immediately released to availability.</div>
                  <div>&bull; Digital cinema passes will be voided for turnstile entry.</div>
                  {cancellationPreview.loyaltyPointsToClawback > 0 && (
                    <div style={{ color: 'var(--accent-gold)' }}>&bull; {cancellationPreview.loyaltyPointsToClawback} earned loyalty points will be reversed.</div>
                  )}
                  {cancellationPreview.loyaltyPointsToRestore > 0 && (
                    <div style={{ color: 'var(--accent-green)' }}>&bull; {cancellationPreview.loyaltyPointsToRestore} redeemed loyalty points will be credited back to your account.</div>
                  )}
                  {cancellationPreview.couponRestored && (
                    <div>&bull; Applied coupon discount will be freed for your future bookings.</div>
                  )}
                </div>

                <div style={{ marginBottom: '1.25rem' }}>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.4rem' }}>
                    Reason for Cancellation (Optional)
                  </label>
                  <textarea
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                    placeholder="e.g., Schedule conflict, travel change..."
                    rows={2}
                    className="input-field"
                    style={{ width: '100%', resize: 'none', fontSize: '0.85rem' }}
                  />
                </div>

                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)', cursor: 'pointer', marginBottom: '1.5rem' }}>
                  <input
                    type="checkbox"
                    checked={simulateAutoProcess}
                    onChange={(e) => setSimulateAutoProcess(e.target.checked)}
                  />
                  <span>Process instant simulated refund to original payment method</span>
                </label>

                <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
                  <button
                    onClick={() => setCancellingBooking(null)}
                    disabled={submittingCancel}
                    className="btn-secondary"
                    style={{ padding: '0.65rem 1.25rem' }}
                  >
                    Keep Booking
                  </button>
                  <button
                    onClick={handleConfirmCancellation}
                    disabled={submittingCancel}
                    className="btn-primary"
                    style={{ padding: '0.65rem 1.5rem', background: 'var(--accent-red)', borderColor: 'var(--accent-red)' }}
                  >
                    {submittingCancel ? 'Processing...' : 'Confirm Cancellation'}
                  </button>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}

      {/* Cancellation Policies Modal */}
      {showPolicyModal && (
        <div
          className="modal-backdrop"
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.85)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '1.5rem'
          }}
        >
          <div
            className="glass-panel"
            style={{
              maxWidth: '640px',
              width: '100%',
              maxHeight: '85vh',
              overflowY: 'auto',
              padding: '2rem',
              position: 'relative',
              borderRadius: 'var(--radius-lg)'
            }}
          >
            <button
              onClick={() => setShowPolicyModal(false)}
              style={{
                position: 'absolute',
                top: '1.25rem',
                right: '1.25rem',
                background: 'none',
                border: 'none',
                color: 'var(--text-muted)',
                cursor: 'pointer'
              }}
            >
              <X size={20} />
            </button>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
              <ShieldCheck size={24} color="var(--accent-cyan)" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>CineSmart Cancellation Policies</h2>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
              Cancellation and refund eligibility is automatically determined based on the time remaining before the scheduled showtime.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
              {policies.map((p) => (
                <div
                  key={p.id}
                  style={{
                    background: 'var(--bg-glass)',
                    borderRadius: 'var(--radius-md)',
                    padding: '1.25rem',
                    border: '1px solid var(--border-subtle)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '1rem',
                    flexWrap: 'wrap'
                  }}
                >
                  <div style={{ maxWidth: '380px' }}>
                    <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--text-primary)', marginBottom: '0.25rem' }}>
                      {p.policyName}
                    </div>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0 }}>
                      {p.description}
                    </p>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '1.25rem', fontWeight: 800, color: Number(p.refundPercentage) > 0 ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                      {p.refundPercentage}% Refund
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      Fee: {p.cancellationFeePercent}%
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ background: 'rgba(255, 255, 255, 0.02)', padding: '0.85rem 1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
              <strong>Notice:</strong> Shows that have already started, tickets scanned at turnstiles, and past bookings are strictly ineligible for cancellation or refunds.
            </div>

            <button onClick={() => setShowPolicyModal(false)} className="btn-secondary" style={{ width: '100%' }}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
