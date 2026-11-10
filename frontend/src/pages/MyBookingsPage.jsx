import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { QRCodeSVG, QRCodeCanvas } from 'qrcode.react';
import { jsPDF } from 'jspdf';
import { bookingService } from '../services/bookingService';
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
  Sparkles
} from 'lucide-react';

export function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(null);

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

  useEffect(() => {
    fetchBookings().then(() => {
      if (window.location.href.includes('ticket=true')) {
        const sample = {
          id: 101,
          bookingReference: 'CS-BKG-8492',
          movieTitle: 'Interstellar Odyssey',
          cinemaName: 'CineSmart Central',
          screenName: 'Screen 1 (IMAX 3D)',
          showStartTime: '2026-11-03T18:45:00',
          status: 'CONFIRMED',
          totalAmount: 980.00,
          seatLabels: ['D3', 'D4', 'D5', 'D6'],
        };
        handleViewTickets(sample);
      }
    });
  }, []);

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

  const handleCancelBooking = async (booking) => {
    const isPaid = booking.status === 'CONFIRMED';
    const confirmMsg = isPaid
      ? `Are you sure you want to cancel booking ${booking.bookingReference}? Your reserved seats will be released and a full simulated refund of ₹${Number(booking.totalAmount).toFixed(2)} will be initiated.`
      : `Are you sure you want to cancel pending booking ${booking.bookingReference}? Your held seats will be released immediately.`;

    if (!window.confirm(confirmMsg)) return;

    try {
      setActionLoading(booking.id);
      await bookingService.cancelBooking(booking.id);
      await fetchBookings();
      alert(`Booking ${booking.bookingReference} has been cancelled successfully.${isPaid ? ' Refund has been processed to your original payment method.' : ''}`);
    } catch (err) {
      alert(`Cancellation failed: ${err.message}`);
    } finally {
      setActionLoading(null);
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

  if (loading) {
    return <LoadingSpinner message="Retrieving your reservation history..." />;
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', paddingBottom: '4rem' }}>
      <div style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
          <Ticket size={28} color="var(--accent-red)" />
          <h1 style={{ fontSize: '2.2rem', fontWeight: 800 }}>My Bookings</h1>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>
          Review your upcoming movie tickets, track payment transactions, and manage reservations.
        </p>
      </div>

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
                      onClick={() => handleCancelBooking(booking)}
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
    </div>
  );
}
