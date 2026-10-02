import React from 'react';
import { Seat } from './Seat';
import { Armchair, CheckCircle } from 'lucide-react';

export function SeatMap({ seats = [], selectedSeats = [], onSeatToggle }) {
  // Group seats by row identifier
  const rows = {};
  seats.forEach((seat) => {
    if (!rows[seat.row]) {
      rows[seat.row] = [];
    }
    rows[seat.row].push(seat);
  });

  const rowKeys = Object.keys(rows).sort();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
      {/* Curved Screen Banner */}
      <div style={{ width: '100%', maxWidth: '640px', marginBottom: '3rem', textAlign: 'center' }}>
        <div style={{
          height: '14px',
          width: '100%',
          background: 'linear-gradient(to bottom, rgba(6, 182, 212, 0.7), rgba(6, 182, 212, 0.05))',
          borderRadius: '50% 50% 0 0 / 100% 100% 0 0',
          boxShadow: '0 -4px 20px rgba(6, 182, 212, 0.4)',
        }} />
        <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.2em', marginTop: '0.4rem', display: 'inline-block' }}>
          SCREEN THIS WAY
        </span>
      </div>

      {/* Seating Grid */}
      <div style={{ overflowX: 'auto', maxWidth: '100%', paddingBottom: '1rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', alignItems: 'center' }}>
          {rowKeys.map((rowKey) => {
            const rowSeats = rows[rowKey].sort((a, b) => a.number - b.number);
            return (
              <div key={rowKey} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ width: '24px', textAlign: 'center', fontWeight: 700, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  {rowKey}
                </span>

                <div style={{ display: 'flex', alignItems: 'center' }}>
                  {rowSeats.map((seat) => {
                    const isSelected = selectedSeats.some((s) => s.showSeatId === seat.showSeatId);
                    return (
                      <Seat
                        key={seat.showSeatId}
                        seat={seat}
                        isSelected={isSelected}
                        onToggle={onSeatToggle}
                      />
                    );
                  })}
                </div>

                <span style={{ width: '24px', textAlign: 'center', fontWeight: 700, color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  {rowKey}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Legend */}
      <div className="glass-panel" style={{ padding: '1rem 1.5rem', display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '1.5rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'var(--seat-available)', border: '1px solid var(--seat-available-border)' }} />
          <span>Available</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'var(--seat-selected)', border: '1px solid #fff' }} />
          <span>Selected</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'rgba(245, 158, 11, 0.3)', border: '1px solid var(--accent-gold)' }} />
          <span>Held</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'var(--seat-booked)' }} />
          <span>Booked</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'rgba(139, 92, 246, 0.3)', border: '1px solid var(--accent-purple)' }} />
          <span>Recliner (+$6)</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '16px', height: '16px', borderRadius: '4px', background: 'rgba(59, 130, 246, 0.3)', border: '1px solid var(--seat-accessible)' }} />
          <span>Accessible</span>
        </div>
      </div>
    </div>
  );
}
