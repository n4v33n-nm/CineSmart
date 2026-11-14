import React from 'react';
import { Search } from 'lucide-react';

export function SearchBar({
  search,
  value,
  onSearchChange,
  onChange,
  selectedGenre,
  onGenreSelect,
  placeholder = 'Search movies by title or cast...'
}) {
  const query = value !== undefined ? value : (search || '');

  const handleChange = (e) => {
    const val = e.target.value;
    if (onChange) onChange(val);
    if (onSearchChange) onSearchChange(val);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <div style={{ position: 'relative', width: '100%', maxWidth: '600px' }}>
        <Search
          size={18}
          color="var(--text-muted)"
          style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }}
        />
        <input
          type="text"
          className="form-input"
          placeholder={placeholder}
          value={query}
          onChange={handleChange}
          style={{ paddingLeft: '2.6rem' }}
        />
      </div>

      {onGenreSelect && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', alignItems: 'center' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginRight: '0.25rem' }}>Genre:</span>
          {['All', 'Sci-Fi', 'Action', 'Animation', 'Drama'].map((g) => {
            const isActive = (g === 'All' && !selectedGenre) || selectedGenre?.toLowerCase() === g.toLowerCase();
            return (
              <button
                key={g}
                onClick={() => onGenreSelect(g === 'All' ? '' : g)}
                style={{
                  padding: '0.35rem 0.85rem',
                  borderRadius: 'var(--radius-full)',
                  fontSize: '0.85rem',
                  fontWeight: 500,
                  background: isActive ? 'var(--accent-red)' : 'var(--bg-glass)',
                  color: isActive ? '#fff' : 'var(--text-secondary)',
                  border: `1px solid ${isActive ? 'var(--accent-red)' : 'var(--border-subtle)'}`,
                  transition: 'all 0.2s',
                }}
              >
                {g}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

