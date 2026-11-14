import React, { useState } from 'react';

export function AnalyticsBarChart({
  data = [],
  title = 'Performance Breakdown',
  subtitle,
  color = '#38BDF8',
  horizontal = false,
  isCurrency = false,
  valueKey = 'value',
  countKey = 'count',
  nameKey = 'name',
  height = 280
}) {
  const [hoverIndex, setHoverIndex] = useState(null);

  if (!data || data.length === 0) {
    return (
      <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', borderRadius: '16px', minHeight: `${height}px`, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <h4 style={{ margin: '0 0 0.5rem', color: '#fff', fontSize: '1.1rem' }}>{title}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>No records found for the active filter set.</p>
      </div>
    );
  }

  const formatVal = (item) => {
    if (isCurrency && item[valueKey] !== undefined) {
      return `₹${Number(item[valueKey]).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
    }
    const val = item[valueKey] !== undefined && Number(item[valueKey]) > 0 ? item[valueKey] : item[countKey];
    return Number(val || 0).toLocaleString();
  };

  // Vertical layout rendering
  const values = data.map(d => Number(isCurrency ? d[valueKey] : (d[countKey] || d[valueKey])) || 0);
  const maxVal = Math.max(...values, 10);

  const width = 600;
  const padding = { top: 20, right: 20, bottom: 45, left: 55 };
  const innerWidth = width - padding.left - padding.right;
  const innerHeight = height - padding.top - padding.bottom;

  const barWidth = Math.min(48, Math.max(16, (innerWidth / data.length) * 0.65));
  const step = innerWidth / data.length;

  return (
    <div
      className="glass-panel"
      style={{
        padding: '1.5rem',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)',
        position: 'relative',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
        <div>
          <h3 style={{ margin: 0, fontSize: '1.15rem', fontWeight: 700, color: '#fff' }}>{title}</h3>
          {subtitle && <p style={{ margin: '3px 0 0', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>{subtitle}</p>}
        </div>

        {hoverIndex !== null && data[hoverIndex] && (
          <div style={{
            background: 'rgba(0, 0, 0, 0.85)',
            border: `1px solid ${color}`,
            borderRadius: '8px',
            padding: '4px 10px',
            fontSize: '0.8rem',
            fontWeight: 700,
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.5)'
          }}>
            <span style={{ color: 'var(--text-secondary)' }}>{data[hoverIndex][nameKey]}:</span>
            <span style={{ color }}>{formatVal(data[hoverIndex])}</span>
            {data[hoverIndex].percentage > 0 && (
              <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                ({data[hoverIndex].percentage}%)
              </span>
            )}
          </div>
        )}
      </div>

      <div style={{ width: '100%', position: 'relative' }}>
        <svg
          viewBox={`0 0 ${width} ${height}`}
          style={{ width: '100%', height: 'auto', overflow: 'visible' }}
          onMouseLeave={() => setHoverIndex(null)}
        >
          <defs>
            <linearGradient id={`bar-gradient-${color.replace('#', '')}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity="1" />
              <stop offset="100%" stopColor={color} stopOpacity="0.45" />
            </linearGradient>
          </defs>

          {/* Grid lines */}
          {[0, maxVal * 0.5, maxVal].map((val, idx) => {
            const y = padding.top + innerHeight - (val / (maxVal || 1)) * innerHeight;
            return (
              <g key={`grid-${idx}`}>
                <line
                  x1={padding.left}
                  y1={y}
                  x2={width - padding.right}
                  y2={y}
                  stroke="rgba(255, 255, 255, 0.07)"
                  strokeDasharray="4 4"
                />
                <text
                  x={padding.left - 10}
                  y={y + 4}
                  fill="var(--text-muted)"
                  fontSize="10"
                  textAnchor="end"
                  fontWeight="500"
                >
                  {isCurrency ? `₹${Math.round(val)}` : Math.round(val)}
                </text>
              </g>
            );
          })}

          {/* Bars */}
          {data.map((item, idx) => {
            const val = Number(isCurrency ? item[valueKey] : (item[countKey] || item[valueKey])) || 0;
            const barHeight = Math.max(4, (val / (maxVal || 1)) * innerHeight);
            const x = padding.left + idx * step + (step - barWidth) / 2;
            const y = padding.top + innerHeight - barHeight;
            const isHovered = hoverIndex === idx;

            // Trim label if long
            const rawName = item[nameKey] || '';
            const truncatedName = rawName.length > 12 ? `${rawName.substring(0, 10)}...` : rawName;

            return (
              <g
                key={`bar-${idx}`}
                style={{ cursor: 'pointer' }}
                onMouseEnter={() => setHoverIndex(idx)}
              >
                <rect
                  x={x}
                  y={y}
                  width={barWidth}
                  height={barHeight}
                  rx="6"
                  ry="6"
                  fill={`url(#bar-gradient-${color.replace('#', '')})`}
                  opacity={isHovered ? 1 : 0.85}
                  stroke={isHovered ? '#FFFFFF' : 'none'}
                  strokeWidth="1.5"
                  style={{
                    transition: 'all 0.2s ease',
                    filter: isHovered ? `drop-shadow(0 0 10px ${color})` : 'none'
                  }}
                />

                {/* X Axis Label */}
                <text
                  x={x + barWidth / 2}
                  y={height - 12}
                  fill={isHovered ? '#FFFFFF' : 'var(--text-secondary)'}
                  fontSize="10.5"
                  textAnchor="middle"
                  fontWeight={isHovered ? '700' : '500'}
                >
                  {truncatedName}
                </text>
              </g>
            );
          })}
        </svg>
      </div>
    </div>
  );
}
