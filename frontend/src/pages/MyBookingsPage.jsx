import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { bookingService } from '../services/bookingService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Ticket, Calendar, Clock, MapPin, Film, Armchair, ArrowRight, ShieldCheck } from 'lucide-react';

export function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchBookings() {
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
    }
    fetchBookings();
  }, []);

  const getStatusBadge = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="badge badge-green">Confirmed</span>;
      case 'PENDING_PAYMENT':
        return <span className="badge badge-gold">Pending Payment</span>;
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
          Review your upcoming movie tickets and reservation details.
        </p>
      </div>

      {error ? (
        <ErrorMessage message={error} />
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

            return (
              <div
                key={booking.id}
                className="glass-panel"
                style={{
                  padding: '1.75rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '1.25rem',
                  borderLeft: '4px solid var(--accent-red)'
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
                  <div>
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
                      Total Paid
                    </span>
                    <div style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                      ${Number(booking.totalAmount).toFixed(2)}
                    </div>
                  </div>
                </div>

                {/* Seats List */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    <Armchair size={15} />
                    <span>Reserved Seats:</span>
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
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
