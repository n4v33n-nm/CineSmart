import React, { useState, useEffect } from 'react';
import { movieService } from '../services/movieService';
import { MovieGrid } from '../components/MovieGrid';
import { SearchBar } from '../components/SearchBar';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { Film, Filter } from 'lucide-react';

const GENRES = ['All', 'Action', 'Sci-Fi', 'Drama', 'Adventure', 'Crime', 'Comedy', 'Thriller'];

export function MovieListPage() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('All');

  useEffect(() => {
    async function fetchMovies() {
      try {
        setLoading(true);
        setError(null);
        const genreParam = selectedGenre === 'All' ? null : selectedGenre;
        const data = await movieService.getMovies(genreParam, searchQuery || null);
        setMovies(data);
      } catch (err) {
        setError(err.message || 'Failed to load movies.');
      } finally {
        setLoading(false);
      }
    }

    const timer = setTimeout(() => {
      fetchMovies();
    }, 200);

    return () => clearTimeout(timer);
  }, [searchQuery, selectedGenre]);

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '1rem 0 3rem' }}>
      {/* Header */}
      <div style={{ marginBottom: '2.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
          <Film size={26} color="var(--accent-red)" />
          <h1 style={{ fontSize: '2.2rem', fontWeight: 800 }}>Explore Movies</h1>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '1rem' }}>
          Browse currently running movies, filter by genre, or search by title.
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        gap: '1.25rem',
        marginBottom: '2.5rem'
      }}>
        <div style={{ maxWidth: '500px' }}>
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search movie title or cast..."
          />
        </div>

        {/* Genre Pill Filters */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.85rem', marginRight: '0.5rem' }}>
            <Filter size={15} />
            <span>Genre:</span>
          </div>
          {GENRES.map((genre) => {
            const isActive = selectedGenre === genre;
            return (
              <button
                key={genre}
                onClick={() => setSelectedGenre(genre)}
                style={{
                  padding: '0.45rem 1rem',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  border: isActive ? '1px solid var(--accent-red)' : '1px solid var(--border-subtle)',
                  background: isActive ? 'rgba(229, 9, 20, 0.2)' : 'var(--bg-glass)',
                  color: isActive ? '#fff' : 'var(--text-secondary)',
                  transition: 'var(--transition-fast)'
                }}
              >
                {genre}
              </button>
            );
          })}
        </div>
      </div>

      {/* Content State */}
      {loading ? (
        <LoadingSpinner message="Searching movie catalog..." />
      ) : error ? (
        <ErrorMessage
          message={error}
          onRetry={() => {
            setSelectedGenre('All');
            setSearchQuery('');
          }}
        />
      ) : (
        <MovieGrid
          movies={movies}
          emptyMessage={`No movies found matching "${searchQuery || selectedGenre}". Try refining your search.`}
        />
      )}
    </div>
  );
}
