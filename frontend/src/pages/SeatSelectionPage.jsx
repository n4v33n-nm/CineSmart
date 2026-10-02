import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link, useLocation } from 'react-router-dom';
import { seatService } from '../services/seatService';
import { bookingService } from '../services/bookingService';
import { useAuth } from '../context/AuthContext';
import { SeatMap } from '../components/SeatMap';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import {
  Film,
  Calendar,
  Clock,
  ArrowLeft,
  CheckCircle,
  AlertCircle,
  Ticket,
  DollarSign,
  ChevronRight,
  RefreshCw
} from 'lucide-react';

const MAX_SEATS = 8;
const CONVENIENCE_FEE_PER_TICKET = 1.50;

export function SeatSelectionPage() {
  const { id: showId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  const [seatMapData, setSeatMapData] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingSuccess, setBookingSuccess] = useState(null);

  const fetchSeatMap = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await seatService.getSeatMapForShow(showId);
      setSeatMapData(data);
      // Remove any previously selected seats that are no longer AVAILABLE
      setSelectedSeats((prev) =>
        prev.filter((sel) => {
          const fresh = data.seats.find((s) => s.showSeatId === sel.showSeatId);
          return fresh && fresh.status === 'AVAILABLE';
        })
      );
    } catch (err) {
      setError(err.message || 'Unable to retrieve seat layout for this show.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSeatMap();
  }, [showId]);

  const handleSeatToggle = (seat) => {
    setSelectedSeats((prev) => {
      const exists = prev.some((s) => s.showSeatId === seat.showSeatId);
      if (exists) {
        return prev.filter((s) => s.showSeatId !== seat.showSeatId);
      }
      if (prev.length >= MAX_SEATS) {
        alert(`You can select up to ${MAX_SEATS} seats per booking.`);
        return prev;
      }
      return [...prev, seat];
    });
  };

  const handleConfirmBooking = async () => {
    if (!user) {
      // Direct user to login and remember current path
      navigate('/login', { state: { from: location } });
      return;
    }

    if (selectedSeats.length === 0) return;

    try {
      setBookingLoading(true);
      setError(null);
      const seatIds = selectedSeats.map((s) => s.showSeatId);
      const result = await bookingService.createBooking(showId, seatIds);
      setBookingSuccess(result);
    } catch (err) {
      setError(err.message || 'Booking creation failed. Some seats may have already been reserved.');
      // Refresh seat map to sync availability
      await fetchSeatMap();
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading live theater seat configuration..." />;
  }

  if (error && !seatMapData) {
    return (
      <div style={{ maxWidth: '800px', margin: '3rem auto' }}>
        <ErrorMessage message={error} onRetry={fetchSeatMap} />
        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <button onClick={() => navigate(-1)} className="btn-secondary">
            <ArrowLeft size={16} />
            <span>Go Back</span>
          </button>
        </div>
      </div>
    );
  }

  // Calculate pricing breakdown
  const subtotal = selectedSeats.reduce((sum, s) => sum + (Number(s.price) || 0), 0);
  const totalFees = selectedSeats.length * CONVENIENCE_FEE_PER_TICKET;
  const grandTotal = subtotal + totalFees;

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '5rem' }}>
      {/* Top Navigation */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <button
          onClick={() => navigate(-1)}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            background: 'none',
            border: 'none',
            color: 'var(--text-secondary)',
            cursor: 'pointer',
            fontSize: '0.9rem'
          }}
        >
          <ArrowLeft size={16} />
          <span>Back to movie details</span>
        </button>

        <button
          onClick={fetchSeatMap}
          className="btn-secondary"
          style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem' }}
          title="Refresh seat availability"
        >
          <RefreshCw size={14} />
          <span>Refresh Seats</span>
        </button>
      </div>

      {/* Show Header Summary */}
      <div className="glass-panel" style={{
        padding: '1.5rem 2rem',
        marginBottom: '2.5rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '1.5rem'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.3rem' }}>
            <Film size={20} color="var(--accent-red)" />
            <h1 style={{ fontSize: '1.6rem', fontWeight: 800 }}>{seatMapData?.movieTitle}</h1>
          </div>
          <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            {seatMapData?.cinemaName} &bull; <strong style={{ color: 'var(--text-primary)' }}>{seatMapData?.screenName}</strong>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <div style={{ textAlign: 'right' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Availability
            </span>
            <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent-cyan)' }}>
              {seatMapData?.availableSeats} <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>/ {seatMapData?.totalSeats} seats</span>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {/* Main Layout: Seat Map on Left/Center, Booking Summary on Right */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr minmax(300px, 340px)',
        gap: '2rem',
        alignItems: 'start'
      }}>
        {/* Interactive Theater Map */}
        <div className="glass-panel" style={{ padding: '2rem 1.5rem', overflowX: 'auto' }}>
          <SeatMap
            seats={seatMapData?.seats || []}
            selectedSeats={selectedSeats}
            onSeatToggle={handleSeatToggle}
          />
        </div>

        {/* Sticky Booking Summary Sidebar */}
        <div className="glass-panel" style={{ padding: '1.75rem', position: 'sticky', top: '90px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
            <Ticket size={20} color="var(--accent-red)" />
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Booking Summary</h2>
          </div>

          {selectedSeats.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '2.5rem 1rem', color: 'var(--text-muted)' }}>
              <p style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>No seats selected yet.</p>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Click on any available seat on the map to begin your reservation.
              </p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {/* Selected Seat Badges */}
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', display: 'block', marginBottom: '0.6rem' }}>
                  Selected Seats ({selectedSeats.length})
                </span>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem' }}>
                  {selectedSeats.map((s) => (
                    <div
                      key={s.showSeatId}
                      style={{
                        padding: '0.35rem 0.65rem',
                        borderRadius: 'var(--radius-sm)',
                        background: 'rgba(229, 9, 20, 0.15)',
                        border: '1px solid rgba(229, 9, 20, 0.4)',
                        fontSize: '0.85rem',
                        fontWeight: 700,
                        color: 'var(--accent-red)'
                      }}
                    >
                      {s.row}{s.number} <span style={{ fontSize: '0.75rem', fontWeight: 500, color: 'var(--text-secondary)' }}>(${Number(s.price).toFixed(2)})</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Price Calculation */}
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.9rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Tickets Subtotal</span>
                  <span>${subtotal.toFixed(2)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-secondary)' }}>
                  <span>Convenience Fee</span>
                  <span>${totalFees.toFixed(2)}</span>
                </div>
                <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '0.75rem', display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: '1.15rem', color: 'var(--text-primary)' }}>
                  <span>Total Amount</span>
                  <span style={{ color: 'var(--accent-gold)' }}>${grandTotal.toFixed(2)}</span>
                </div>
              </div>

              {/* Action Button */}
              <button
                onClick={handleConfirmBooking}
                disabled={bookingLoading}
                className="btn-primary"
                style={{ width: '100%', justifyContent: 'center', padding: '0.85rem', fontSize: '1rem', marginTop: '0.5rem' }}
              >
                {bookingLoading ? (
                  <span>Reserving Seats...</span>
                ) : user ? (
                  <>
                    <span>Confirm Booking</span>
                    <ChevronRight size={18} />
                  </>
                ) : (
                  <>
                    <span>Sign In to Book</span>
                    <ChevronRight size={18} />
                  </>
                )}
              </button>

              {!user && (
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textAlign: 'center' }}>
                  You will be prompted to login or create an account to finalize your reservation.
                </p>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Booking Success Modal */}
      {bookingSuccess && (
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
          zIndex: 999,
          padding: '1.5rem'
        }}>
          <div className="glass-panel" style={{
            maxWidth: '520px',
            width: '100%',
            padding: '2.5rem',
            textAlign: 'center',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              background: 'rgba(16, 185, 129, 0.15)',
              border: '2px solid var(--accent-green)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.5rem'
            }}>
              <CheckCircle size={36} color="var(--accent-green)" />
            </div>

            <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.5rem' }}>
              Booking Confirmed!
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginBottom: '1.5rem' }}>
              Your reservation has been created in the CineSmart system.
            </p>

            <div style={{
              background: 'var(--bg-glass)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem',
              textAlign: 'left',
              marginBottom: '2rem'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Booking Reference</span>
                <span style={{ fontWeight: 700, color: 'var(--accent-gold)' }}>{bookingSuccess.bookingReference}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Movie</span>
                <span style={{ fontWeight: 600 }}>{bookingSuccess.movieTitle}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Cinema / Screen</span>
                <span style={{ fontWeight: 600 }}>{bookingSuccess.cinemaName} ({bookingSuccess.screenName})</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Seats</span>
                <span style={{ fontWeight: 600 }}>
                  {bookingSuccess.seats?.map((s) => `${s.rowIdentifier}${s.columnNumber}`).join(', ')}
                </span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid var(--border-subtle)', paddingTop: '0.5rem', marginTop: '0.5rem' }}>
                <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Total Amount</span>
                <span style={{ fontWeight: 800, color: 'var(--accent-green)', fontSize: '1.1rem' }}>
                  ${Number(bookingSuccess.totalAmount).toFixed(2)}
                </span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <Link to="/bookings" className="btn-primary" style={{ padding: '0.75rem 1.5rem' }}>
                <span>View My Bookings</span>
                <ChevronRight size={16} />
              </Link>
              <Link to="/movies" className="btn-secondary" style={{ padding: '0.75rem 1.5rem' }}>
                <span>Back to Movies</span>
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
