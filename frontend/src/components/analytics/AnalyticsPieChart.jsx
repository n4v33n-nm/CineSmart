import React, { useState } from 'react';

const PALETTE = [
  '#E50914', // CineSmart Red
  '#10B981', // Emerald
  '#F59E0B', // Amber
  '#38BDF8', // Cyan
  '#8B5CF6', // Purple
  '#EC4899', // Pink
  '#14B8A6', // Teal
  '#F97316'  // Orange
];

export function AnalyticsPieChart({
  data = [],
  title = 'Distribution',
  subtitle,
  nameKey = 'name',
  valueKey = 'count',
  isCurrency = false,
  centerLabel = 'Total'
}) {
  const [hoverIndex, setHoverIndex] = useState(null);

  if (!data || data.length === 0) {
    return (
      <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', borderRadius: '16px', minHeight: '260px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <h4 style={{ margin: '0 0 0.5rem', color: '#fff', fontSize: '1.1rem' }}>{title}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>No distribution records available.</p>
      </div>
    );
  }

  const total = data.reduce((sum, item) => sum + (Number(item[valueKey]) || 0), 0);

  // SVG Donut calculation
  const size = 180;
  const strokeWidth = 24;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;

  let accumulatedPercent = 0;
  const segments = data.map((item, idx) => {
    const val = Number(item[valueKey]) || 0;
    const percent = total > 0 ? (val / total) * 100 : 0;
    const strokeDasharray = `${(percent / 100) * circumference} ${circumference}`;
    const strokeDashoffset = -((accumulatedPercent / 100) * circumference);
    accumulatedPercent += percent;

    return {
      name: item[nameKey] || `Item ${idx + 1}`,
      value: val,
      percent: item.percentage ? item.percentage : Math.round(percent * 10) / 10,
      color: PALETTE[idx % PALETTE.length],
      strokeDasharray,
      strokeDashoffset,
      raw: item
    };
  });

  const formatValue = (num) => {
    if (isCurrency) {
      return `₹${Number(num).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
    }
    return Number(num).toLocaleString();
  };

  return (
    <div
      className="glass-panel"
      style={{
        padding: '1.5rem',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      <div style={{ marginBottom: '1.25rem' }}>
        <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 700, color: '#fff' }}>{title}</h3>
        {subtitle && <p style={{ margin: '3px 0 0', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>{subtitle}</p>}
      </div>

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
        gap: '1.5rem',
        alignItems: 'center'
      }}>
        {/* Donut graphic */}
        <div style={{ position: 'relative', width: size, height: size, margin: '0 auto' }}>
          <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ transform: 'rotate(-90deg)' }}>
            {/* Background ring */}
            <circle
              cx={size / 2}
              cy={size / 2}
              r={radius}
              fill="transparent"
              stroke="rgba(255, 255, 255, 0.05)"
              strokeWidth={strokeWidth}
            />

            {/* Segments */}
            {segments.map((seg, idx) => {
              const isHovered = hoverIndex === idx;
              return (
                <circle
                  key={`seg-${idx}`}
                  cx={size / 2}
                  cy={size / 2}
                  r={radius}
                  fill="transparent"
                  stroke={seg.color}
                  strokeWidth={isHovered ? strokeWidth + 4 : strokeWidth}
                  strokeDasharray={seg.strokeDasharray}
                  strokeDashoffset={seg.strokeDashoffset}
                  style={{
                    cursor: 'pointer',
                    transition: 'all 0.25s ease',
                    filter: isHovered ? `drop-shadow(0 0 10px ${seg.color})` : 'none'
                  }}
                  onMouseEnter={() => setHoverIndex(idx)}
                  onMouseLeave={() => setHoverIndex(null)}
                />
              );
            })}
          </svg>

          {/* Center metric label */}
          <div style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            pointerEvents: 'none'
          }}>
            {hoverIndex !== null && segments[hoverIndex] ? (
              <>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
                  {segments[hoverIndex].name}
                </span>
                <span style={{ fontSize: '1.2rem', fontWeight: 800, color: segments[hoverIndex].color }}>
                  {segments[hoverIndex].percent}%
                </span>
              </>
            ) : (
              <>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  {centerLabel}
                </span>
                <span style={{ fontSize: '1.25rem', fontWeight: 800, color: '#FFFFFF' }}>
                  {formatValue(total)}
                </span>
              </>
            )}
          </div>
        </div>

        {/* Legend list */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.55rem' }}>
          {segments.map((seg, idx) => {
            const isHovered = hoverIndex === idx;
            return (
              <div
                key={`legend-${idx}`}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '4px 8px',
                  borderRadius: '6px',
                  background: isHovered ? 'rgba(255, 255, 255, 0.06)' : 'transparent',
                  cursor: 'pointer',
                  transition: 'background 0.2s ease'
                }}
                onMouseEnter={() => setHoverIndex(idx)}
                onMouseLeave={() => setHoverIndex(null)}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div style={{ width: '10px', height: '10px', borderRadius: '50%', background: seg.color, flexShrink: 0 }} />
                  <span style={{ fontSize: '0.85rem', color: isHovered ? '#FFFFFF' : 'var(--text-secondary)', fontWeight: isHovered ? 700 : 500 }}>
                    {seg.name}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
                  <span style={{ color: '#fff', fontWeight: 600 }}>
                    {formatValue(seg.value)}
                  </span>
                  <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem', minWidth: '36px', textAlign: 'right' }}>
                    {seg.percent}%
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
