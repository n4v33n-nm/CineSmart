import React from 'react';

export function LoadingSpinner({ message = 'Loading cinema experience...' }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '3.5rem 1rem', minHeight: '260px' }}>
      <div style={{
        width: '46px',
        height: '46px',
        border: '3px solid rgba(229, 9, 20, 0.2)',
        borderTopColor: 'var(--accent-red)',
        borderRadius: '50%',
        animation: 'spin 0.8s linear infinite',
      }} />
      <p style={{ marginTop: '1.25rem', color: 'var(--text-secondary)', fontSize: '0.95rem', fontWeight: 500 }}>
        {message}
      </p>
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
