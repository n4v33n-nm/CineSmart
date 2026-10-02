import React from 'react';
import { Link } from 'react-router-dom';
import { Calendar, Clock, Armchair, ChevronRight } from 'lucide-react';

export function ShowCard({ show }) {
  const dateObj = new Date(show.startTime);
  const timeFormatted = dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const dateFormatted = dateObj.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' });

  return (
    <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '1rem', flexWrap: 'wrap' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', flexWrap: 'wrap' }}>
        <div>
          <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-primary)' }}>
            {timeFormatted}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
            <Calendar size={13} />
            <span>{dateFormatted}</span>
          </div>
        </div>

        <div style={{ borderLeft: '1px solid var(--border-subtle)', paddingLeft: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.2rem' }}>
            <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
              Screen {show.screenNumber}
            </span>
            <span className="badge badge-cyan" style={{ fontSize: '0.7rem' }}>
              {show.screenType}
            </span>
          </div>
          <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            {show.cinemaName}
          </div>
        </div>

        <div style={{ borderLeft: '1px solid var(--border-subtle)', paddingLeft: '1.25rem' }}>
          <div style={{ color: 'var(--accent-gold)', fontWeight: 700, fontSize: '1.1rem' }}>
            ${Number(show.basePrice).toFixed(2)}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
            <Armchair size={13} />
            <span>{show.availableSeatCount} seats available</span>
          </div>
        </div>
      </div>

      <Link to={`/shows/${show.id}/seats`} className="btn-primary" style={{ padding: '0.6rem 1.25rem', fontSize: '0.9rem' }}>
        <span>Select Seats</span>
        <ChevronRight size={16} />
      </Link>
    </div>
  );
}
