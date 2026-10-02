import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { movieService } from '../services/movieService';
import { MovieGrid } from '../components/MovieGrid';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Film, Users, ShieldCheck, TicketCheck, ArrowRight } from 'lucide-react';

export function HomePage() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadMovies() {
      try {
        setLoading(true);
        const data = await movieService.getMovies();
        setMovies(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    loadMovies();
  }, []);

  return (
    <div>
      {/* Hero Section */}
      <section style={{
        position: 'relative',
        borderRadius: 'var(--radius-lg)',
        overflow: 'hidden',
        padding: '4.5rem 2rem',
        marginBottom: '3.5rem',
        background: 'linear-gradient(135deg, rgba(229, 9, 20, 0.2) 0%, rgba(15, 20, 34, 0.95) 70%), url("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1600&q=80") center/cover',
        boxShadow: 'var(--shadow-md)',
        border: '1px solid var(--border-subtle)',
      }}>
        <div style={{ maxWidth: '680px' }}>
          <span className="badge badge-gold" style={{ marginBottom: '1rem' }}>
            Next-Generation Cinema Ticketing
          </span>
          <h1 style={{ fontSize: 'clamp(2.2rem, 5vw, 3.4rem)', marginBottom: '1.25rem', color: '#fff' }}>
            Experience Cinema The <span style={{ color: 'var(--accent-red)' }}>Smart</span> Way.
          </h1>
          <p style={{ fontSize: '1.1rem', color: 'var(--text-secondary)', marginBottom: '2rem', lineHeight: 1.6 }}>
            Seamless movie ticket reservations powered by Object-Oriented Analysis & Design. Explore real-time interactive seat layouts, showtimes, and instant bookings.
          </p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
            <Link to="/movies" className="btn-primary" style={{ padding: '0.85rem 1.75rem', fontSize: '1rem' }}>
              <span>Browse Now Showing</span>
              <ArrowRight size={18} />
            </Link>
          </div>
        </div>
      </section>

      {/* 3 Pillars Overview */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(245, 158, 11, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <Users size={22} color="var(--accent-gold)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Smart Group Seating</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Intelligent multi-criteria recommendation algorithm finding optimal contiguous and split-row arrangements for groups of 2 to 10.
            </p>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(6, 182, 212, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <ShieldCheck size={22} color="var(--accent-cyan)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Booking Recovery Engine</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Zero-lost-transaction state machine that safely manages payment dropouts with active background status reconciliation.
            </p>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <TicketCheck size={22} color="var(--accent-emerald)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Fair Seat Release & Waitlist</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              FIFO priority queuing with exclusive 10-minute claim reservation windows when sold-out seats become available.
            </p>
          </div>
        </div>
      </section>

      {/* Movies Grid Section */}
      <section>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
          <div>
            <h2 style={{ fontSize: '1.8rem', color: '#fff', marginBottom: '0.25rem' }}>Now Showing</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>Discover the latest blockbuster hits scheduled today</p>
          </div>
          <Link to="/movies" style={{ color: 'var(--accent-red)', fontWeight: 600, fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <span>View All</span>
            <ArrowRight size={16} />
          </Link>
        </div>

        {loading ? (
          <LoadingSpinner message="Fetching currently screening movies..." />
        ) : error ? (
          <ErrorMessage message={error} onRetry={() => window.location.reload()} />
        ) : (
          <MovieGrid movies={movies} />
        )}
      </section>
    </div>
  );
}
