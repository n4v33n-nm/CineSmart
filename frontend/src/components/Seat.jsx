import React from 'react';

export function Seat({ seat, isSelected, onToggle }) {
  const isAvailable = seat.status === 'AVAILABLE';
  const isBooked = seat.status === 'BOOKED';
  const isHeld = seat.status === 'HELD';
  const isAccessible = seat.accessible;

  let bgColor = 'var(--seat-available)';
  let borderColor = 'var(--seat-available-border)';
  let textColor = '#fff';
  let cursor = 'pointer';

  if (isSelected) {
    bgColor = 'var(--seat-selected)';
    borderColor = '#fff';
    textColor = '#000';
  } else if (isBooked) {
    bgColor = 'var(--seat-booked)';
    borderColor = 'transparent';
    textColor = '#64748b';
    cursor = 'not-allowed';
  } else if (isHeld) {
    bgColor = 'rgba(245, 158, 11, 0.25)';
    borderColor = 'var(--accent-gold)';
    textColor = 'var(--accent-gold)';
    cursor = 'not-allowed';
  } else if (seat.tier === 'RECLINER') {
    bgColor = 'rgba(139, 92, 246, 0.25)';
    borderColor = 'var(--accent-purple)';
  } else if (seat.tier === 'PREMIUM') {
    bgColor = 'rgba(245, 158, 11, 0.2)';
    borderColor = 'var(--accent-gold)';
  } else if (isAccessible) {
    bgColor = 'rgba(59, 130, 246, 0.25)';
    borderColor = 'var(--seat-accessible)';
  }

  const ariaLabel = `${seat.label} — ${isSelected ? 'Selected' : seat.status}, Tier: ${seat.tier}, Price: $${Number(seat.price).toFixed(2)}`;

  return (
    <button
      type="button"
      onClick={() => isAvailable && onToggle(seat)}
      disabled={!isAvailable}
      aria-label={ariaLabel}
      title={ariaLabel}
      style={{
        width: '36px',
        height: '34px',
        margin: '3px',
        borderRadius: '8px 8px 4px 4px',
        background: bgColor,
        border: `1.5px solid ${borderColor}`,
        color: textColor,
        fontSize: '0.75rem',
        fontWeight: 600,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        cursor,
        transition: 'all 0.15s ease',
        boxShadow: isSelected ? '0 0 10px rgba(6, 182, 212, 0.6)' : 'none',
        transform: isSelected ? 'scale(1.1)' : 'scale(1.0)',
      }}
    >
      {seat.number}
    </button>
  );
}
