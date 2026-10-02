import React from 'react';
import { Film } from 'lucide-react';

export function Footer() {
  return (
    <footer style={{ background: 'var(--bg-secondary)', borderTop: '1px solid var(--border-subtle)', padding: '2.5rem 1.5rem', marginTop: 'auto' }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto', display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center', gap: '1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <Film size={20} color="var(--accent-red)" />
          <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>CineSmart</span>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            — Smart Movie Ticket Booking System (OOAD Capstone)
          </span>
        </div>

        <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem', display: 'flex', gap: '1.5rem' }}>
          <span>Phase 2: Foundation & Core Infrastructure</span>
          <span>•</span>
          <span>Java 17 • Spring Boot 3 • React 18 • PostgreSQL</span>
        </div>
      </div>
    </footer>
  );
}
