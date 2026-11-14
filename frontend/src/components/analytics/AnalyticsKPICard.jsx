import React from 'react';

export function AnalyticsKPICard({
  title,
  value,
  subtext,
  badgeText,
  badgeColor = '#10B981',
  badgeBg = 'rgba(16, 185, 129, 0.15)',
  icon: Icon,
  iconColor = '#E50914',
  iconBg = 'rgba(229, 9, 20, 0.15)'
}) {
  return (
    <div
      className="glass-panel"
      style={{
        padding: '1.4rem',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.75) 100%)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        transition: 'transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease',
        position: 'relative',
        overflow: 'hidden'
      }}
      onMouseOver={(e) => {
        e.currentTarget.style.transform = 'translateY(-4px)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.2)';
        e.currentTarget.style.boxShadow = '0 12px 30px rgba(0, 0, 0, 0.35)';
      }}
      onMouseOut={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
        e.currentTarget.style.boxShadow = 'none';
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '0.9rem' }}>
        <div style={{
          width: '42px',
          height: '42px',
          borderRadius: '12px',
          background: iconBg,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0
        }}>
          {Icon && <Icon size={20} color={iconColor} />}
        </div>

        {badgeText && (
          <span style={{
            fontSize: '0.75rem',
            fontWeight: 700,
            padding: '3px 8px',
            borderRadius: '999px',
            background: badgeBg,
            color: badgeColor,
            border: `1px solid ${badgeColor}33`,
            letterSpacing: '0.02em'
          }}>
            {badgeText}
          </span>
        )}
      </div>

      <div>
        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 500, marginBottom: '0.35rem' }}>
          {title}
        </div>
        <div style={{ fontSize: '1.85rem', fontWeight: 800, color: '#FFFFFF', letterSpacing: '-0.02em', lineHeight: 1.1 }}>
          {value}
        </div>
        {subtext && (
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.45rem' }}>
            {subtext}
          </div>
        )}
      </div>
    </div>
  );
}
