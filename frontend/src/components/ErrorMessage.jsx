import React from 'react';
import { AlertCircle, RotateCcw } from 'lucide-react';

export function ErrorMessage({ message = 'An error occurred', onRetry }) {
  return (
    <div className="glass-panel" style={{ padding: '1.5rem', borderLeft: '4px solid var(--accent-red)', margin: '1.5rem 0' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.85rem' }}>
        <AlertCircle size={22} color="var(--accent-red)" style={{ flexShrink: 0, marginTop: '2px' }} />
        <div style={{ flex: 1 }}>
          <h4 style={{ color: '#fff', fontSize: '1rem', marginBottom: '0.25rem' }}>Something went wrong</h4>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.5 }}>
            {message}
          </p>
          {onRetry && (
            <button onClick={onRetry} className="btn-secondary" style={{ marginTop: '0.85rem', padding: '0.4rem 0.85rem', fontSize: '0.85rem' }}>
              <RotateCcw size={14} />
              <span>Retry</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
