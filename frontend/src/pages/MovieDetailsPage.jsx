import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { movieService } from '../services/movieService';
import { ShowCard } from '../components/ShowCard';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Clock, Calendar, Globe, Film, ArrowLeft, Ticket, Shield } from 'lucide-react';

export function MovieDetailsPage() {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        setError(null);
        const [movieData, showsData] = await Promise.all([
          movieService.getMovieById(id),
          movieService.getShowsForMovie(id)
        ]);
        setMovie(movieData);
        setShows(showsData);
      } catch (err) {
        setError(err.message || 'Unable to load movie details.');
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [id]);

  if (loading) {
    return <LoadingSpinner message="Loading movie details and showtimes..." />;
  }

  if (error || !movie) {
    return (
      <div style={{ maxWidth: '800px', margin: '3rem auto' }}>
        <ErrorMessage message={error || 'Movie not found.'} />
        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <Link to="/movies" className="btn-secondary">
            <ArrowLeft size={16} />
            <span>Return to Catalog</span>
          </Link>
        </div>
      </div>
    );
  }

  const hours = Math.floor(movie.durationMinutes / 60);
  const mins = movie.durationMinutes % 60;
  const durationStr = `${hours > 0 ? `${hours}h ` : ''}${mins}m`;

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Back button */}
      <div style={{ marginBottom: '1.5rem' }}>
        <Link to="/movies" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: 'var(--text-secondary)', textDecoration: 'none', fontSize: '0.9rem', transition: 'color 0.2s' }}>
          <ArrowLeft size={16} />
          <span>Back to all movies</span>
        </Link>
      </div>

      {/* Hero Movie Presentation */}
      <div className="glass-panel" style={{
        padding: '2.5rem',
        marginBottom: '3rem',
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
        gap: '2.5rem',
        alignItems: 'start'
      }}>
        {/* Poster */}
        <div style={{
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          boxShadow: 'var(--shadow-lg)',
          border: '1px solid var(--border-subtle)',
          aspectRatio: '2/3',
          maxHeight: '440px'
        }}>
          <img
            src={movie.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80'}
            alt={movie.title}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </div>

        {/* Info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.75rem', flexWrap: 'wrap' }}>
              <span className="badge badge-red">{movie.genre}</span>
              <span className="badge badge-cyan">{movie.language}</span>
              <span className="badge badge-gold" style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <Shield size={12} />
                <span>{movie.certification || 'PG-13'}</span>
              </span>
            </div>
            <h1 style={{ fontSize: 'clamp(2rem, 4vw, 2.75rem)', fontWeight: 800, lineHeight: 1.15, marginBottom: '0.5rem' }}>
              {movie.title}
            </h1>
          </div>

          {/* Metadata chips */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', color: 'var(--text-secondary)', fontSize: '0.9rem', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Clock size={16} color="var(--accent-gold)" />
              <span>{durationStr}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Calendar size={16} color="var(--accent-gold)" />
              <span>{new Date(movie.releaseDate).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Globe size={16} color="var(--accent-gold)" />
              <span>{movie.language}</span>
            </div>
          </div>

          <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '1.25rem' }}>
            <h3 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.6rem' }}>
              Synopsis
            </h3>
            <p style={{ color: 'var(--text-secondary)', lineHeight: 1.7, fontSize: '0.98rem' }}>
              {movie.description}
            </p>
          </div>
        </div>
      </div>

      {/* Shows & Screenings Section */}
      <section>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.5rem' }}>
          <Ticket size={24} color="var(--accent-red)" />
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700 }}>Available Showtimes</h2>
        </div>

        {shows.length === 0 ? (
          <div className="glass-panel" style={{ padding: '3rem 2rem', textAlign: 'center' }}>
            <Film size={40} color="var(--text-muted)" style={{ margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>No Screenings Scheduled</h3>
            <p style={{ color: 'var(--text-secondary)', maxWidth: '460px', margin: '0 auto 1.5rem' }}>
              There are currently no active showtimes for this title. Please check back later or explore other movies.
            </p>
            <Link to="/movies" className="btn-secondary">
              Browse Other Movies
            </Link>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {shows.map((show) => (
              <ShowCard key={show.id} show={show} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
