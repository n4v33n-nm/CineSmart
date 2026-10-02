import React from 'react';
import { MovieCard } from './MovieCard';
import { Film } from 'lucide-react';

export function MovieGrid({ movies = [] }) {
  if (movies.length === 0) {
    return (
      <div className="glass-panel" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
        <Film size={44} color="var(--text-muted)" style={{ margin: '0 auto 1rem auto' }} />
        <h3 style={{ color: 'var(--text-primary)', marginBottom: '0.5rem' }}>No Movies Found</h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
          Try clearing search filters or check back later for newly scheduled cinema releases.
        </p>
      </div>
    );
  }

  return (
    <div className="grid-cards">
      {movies.map((movie) => (
        <MovieCard key={movie.id} movie={movie} />
      ))}
    </div>
  );
}
