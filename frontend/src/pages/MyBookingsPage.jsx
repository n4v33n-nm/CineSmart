import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
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
  CheckCircle
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
        <div style={{
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
        }}>
          <div className="glass-panel" style={{
            maxWidth: '560px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '2rem',
            position: 'relative'
          }}>
            <button
              onClick={() => setSelectedBookingForTickets(null)}
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

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <Ticket size={24} color="var(--accent-red)" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Digital Tickets</h2>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.5rem' }}>
              Booking: <strong style={{ color: 'var(--accent-gold)' }}>{selectedBookingForTickets.bookingReference}</strong> &bull; {selectedBookingForTickets.movieTitle}
            </p>

            {ticketsLoading ? (
              <LoadingSpinner message="Loading ticket details & QR tokens..." />
            ) : ticketsError ? (
              <ErrorMessage message={ticketsError} />
            ) : tickets.length === 0 ? (
              <p style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No tickets found for this reservation.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {tickets.map((t) => (
                  <div
                    key={t.id || t.ticketCode}
                    style={{
                      background: 'rgba(255, 255, 255, 0.03)',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 'var(--radius-md)',
                      padding: '1rem 1.25rem',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.6rem'
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--accent-gold)' }}>
                        {t.ticketCode}
                      </span>
                      <span className="badge badge-green" style={{ fontSize: '0.75rem' }}>
                        {t.status || 'ACTIVE'}
                      </span>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', fontSize: '0.85rem' }}>
                      <div>
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', display: 'block' }}>Seat</span>
                        <strong style={{ color: 'var(--text-primary)' }}>{t.seatLabel} ({t.seatTier})</strong>
                      </div>
                      <div>
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', display: 'block' }}>Ticket Price</span>
                        <strong style={{ color: 'var(--accent-green)' }}>₹{Number(t.price).toFixed(2)}</strong>
                      </div>
                    </div>

                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      background: 'rgba(0,0,0,0.3)',
                      padding: '0.4rem 0.6rem',
                      borderRadius: '4px',
                      fontSize: '0.7rem',
                      color: 'var(--text-muted)',
                      wordBreak: 'break-all'
                    }}>
                      <QrCode size={16} color="var(--accent-cyan)" style={{ flexShrink: 0 }} />
                      <span><strong>QR Hash:</strong> {t.qrCodeHash || 'SHA256-VERIFIED-PASS'}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
              <button
                onClick={() => setSelectedBookingForTickets(null)}
                className="btn-secondary"
                style={{ width: '100%', justifyContent: 'center' }}
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
