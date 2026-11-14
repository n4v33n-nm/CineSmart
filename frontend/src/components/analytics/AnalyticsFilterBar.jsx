import React from 'react';
import { Filter, Calendar, RotateCcw, RefreshCw } from 'lucide-react';

export function AnalyticsFilterBar({
  filters,
  onChange,
  onReset,
  onRefresh,
  loading = false,
  options = { movies: [], cinemas: [], shows: [], statuses: [] }
}) {
  const handlePreset = (days) => {
    if (days === 'ALL') {
      onChange({ ...filters, startDate: '', endDate: '', preset: 'ALL' });
      return;
    }
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - Number(days));

    const fmt = (d) => d.toISOString().split('T')[0];
    onChange({
      ...filters,
      startDate: fmt(start),
      endDate: fmt(end),
      preset: `${days}D`
    });
  };

  return (
    <div
      className="glass-panel"
      style={{
        padding: '1.25rem 1.5rem',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.6) 0%, rgba(15, 20, 32, 0.85) 100%)',
        marginBottom: '2rem',
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem'
      }}
    >
      {/* Top Header & Presets */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Filter size={18} color="var(--accent-red)" />
          <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#fff' }}>Analytics Filters</span>
        </div>

        {/* Date presets */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
          {[
            { label: '7 Days', val: 7, key: '7D' },
            { label: '30 Days', val: 30, key: '30D' },
            { label: '90 Days', val: 90, key: '90D' },
            { label: 'All Time', val: 'ALL', key: 'ALL' }
          ].map((p) => {
            const isActive = filters.preset === p.key;
            return (
              <button
                key={p.key}
                type="button"
                onClick={() => handlePreset(p.val)}
                style={{
                  padding: '4px 12px',
                  borderRadius: '999px',
                  fontSize: '0.8rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  border: isActive ? '1px solid var(--accent-red)' : '1px solid rgba(255, 255, 255, 0.1)',
                  background: isActive ? 'rgba(229, 9, 20, 0.25)' : 'rgba(255, 255, 255, 0.04)',
                  color: isActive ? '#FFFFFF' : 'var(--text-secondary)',
                  transition: 'all 0.2s ease'
                }}
              >
                {p.label}
              </button>
            );
          })}

          <button
            type="button"
            onClick={onRefresh}
            disabled={loading}
            className="btn-secondary"
            style={{ padding: '4px 10px', fontSize: '0.8rem', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '4px' }}
            title="Refresh Data"
          >
            <RefreshCw size={13} className={loading ? 'spin' : ''} />
            <span>Refresh</span>
          </button>

          <button
            type="button"
            onClick={onReset}
            className="btn-secondary"
            style={{ padding: '4px 10px', fontSize: '0.8rem', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '4px' }}
            title="Reset Filters"
          >
            <RotateCcw size={13} />
            <span>Reset</span>
          </button>
        </div>
      </div>

      {/* Inputs Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '0.85rem'
      }}>
        {/* Start Date */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            From Date
          </label>
          <input
            type="date"
            value={filters.startDate || ''}
            onChange={(e) => onChange({ ...filters, startDate: e.target.value, preset: 'CUSTOM' })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          />
        </div>

        {/* End Date */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            To Date
          </label>
          <input
            type="date"
            value={filters.endDate || ''}
            onChange={(e) => onChange({ ...filters, endDate: e.target.value, preset: 'CUSTOM' })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          />
        </div>

        {/* Movie Filter */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            Filter by Movie
          </label>
          <select
            value={filters.movieId || ''}
            onChange={(e) => onChange({ ...filters, movieId: e.target.value || null })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          >
            <option value="">All Movies ({options.movies?.length || 0})</option>
            {options.movies?.map((m) => (
              <option key={m.id} value={m.id}>
                {m.title} ({m.genre})
              </option>
            ))}
          </select>
        </div>

        {/* Theatre Filter */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            Filter by Theatre
          </label>
          <select
            value={filters.cinemaId || ''}
            onChange={(e) => onChange({ ...filters, cinemaId: e.target.value || null })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          >
            <option value="">All Theatres ({options.cinemas?.length || 0})</option>
            {options.cinemas?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} ({c.city})
              </option>
            ))}
          </select>
        </div>

        {/* Show Filter */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            Filter by Show
          </label>
          <select
            value={filters.showId || ''}
            onChange={(e) => onChange({ ...filters, showId: e.target.value || null })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          >
            <option value="">All Shows</option>
            {options.shows?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.label}
              </option>
            ))}
          </select>
        </div>

        {/* Status Filter */}
        <div>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '4px' }}>
            Booking Status
          </label>
          <select
            value={filters.status || 'ALL'}
            onChange={(e) => onChange({ ...filters, status: e.target.value })}
            style={{
              width: '100%',
              padding: '6px 10px',
              borderRadius: '8px',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              background: 'rgba(0, 0, 0, 0.35)',
              color: '#fff',
              fontSize: '0.82rem'
            }}
          >
            <option value="ALL">All Statuses</option>
            {options.statuses?.map((st) => (
              <option key={st} value={st}>
                {st}
              </option>
            ))}
          </select>
        </div>
      </div>
    </div>
  );
}
