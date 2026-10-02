import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, Globe, Film } from 'lucide-react';

export function MovieCard({ movie }) {
  const posterUrl = movie.posterUrl || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80';

  return (
    <div className="glass-panel" style={{ overflow: 'hidden', display: 'flex', flexDirection: 'column', transition: 'transform 0.25s ease, box-shadow 0.25s ease' }}>
      <div style={{ position: 'relative', width: '100%', paddingTop: '140%', overflow: 'hidden', background: '#0b0f19' }}>
        <img
          src={posterUrl}
          alt={movie.title}
          style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', objectFit: 'cover', transition: 'transform 0.4s ease' }}
          onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.05)')}
          onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1.0)')}
        />
        <div style={{ position: 'absolute', top: '12px', right: '12px', display: 'flex', gap: '6px' }}>
          <span className="badge badge-gold" style={{ background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(6px)' }}>
            {movie.ageRating}
          </span>
        </div>
        <div style={{ position: 'absolute', bottom: '12px', left: '12px' }}>
          <span className="badge badge-cyan" style={{ background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(6px)' }}>
            {movie.genre}
          </span>
        </div>
      </div>

      <div style={{ padding: '1.25rem', display: 'flex', flexDirection: 'column', flex: 1 }}>
        <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={movie.title}>
          {movie.title}
        </h3>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1.2rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Clock size={14} />
            {movie.durationMinutes} min
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Globe size={14} />
            {movie.language}
          </span>
        </div>

        <Link
          to={`/movies/${movie.id}`}
          className="btn-primary"
          style={{ marginTop: 'auto', width: '100%', padding: '0.65rem 1rem', fontSize: '0.9rem' }}
        >
          <Film size={16} />
          <span>View Showtimes</span>
        </Link>
      </div>
    </div>
  );
}
