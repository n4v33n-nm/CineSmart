import React, { useState } from 'react';

export function AnalyticsLineChart({
  data = [],
  xKey = 'period',
  yKey = 'revenue',
  title = 'Revenue Trend',
  subtitle = 'Daily revenue over time',
  color = '#E50914',
  height = 280,
  isCurrency = true
}) {
  const [hoverIndex, setHoverIndex] = useState(null);

  if (!data || data.length === 0) {
    return (
      <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', borderRadius: '16px', minHeight: `${height}px`, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <h4 style={{ margin: '0 0 0.5rem', color: '#fff', fontSize: '1.1rem' }}>{title}</h4>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: 0 }}>No time-series data available for the selected filters.</p>
      </div>
    );
  }

  const values = data.map(d => Number(d[yKey]) || 0);
  const maxVal = Math.max(...values, 100);
  const minVal = 0;

  const padding = { top: 25, right: 30, bottom: 40, left: 65 };
  const width = 700;
  const innerWidth = width - padding.left - padding.right;
  const innerHeight = height - padding.top - padding.bottom;

  const points = data.map((d, i) => {
    const val = Number(d[yKey]) || 0;
    const x = padding.left + (data.length > 1 ? (i / (data.length - 1)) * innerWidth : innerWidth / 2);
    const y = padding.top + innerHeight - ((val - minVal) / (maxVal - minVal || 1)) * innerHeight;
    return { x, y, val, label: d[xKey], raw: d };
  });

  const pathD = points.length === 1
    ? `M ${points[0].x} ${points[0].y}`
    : points.reduce((acc, curr, idx, arr) => {
        if (idx === 0) return `M ${curr.x} ${curr.y}`;
        const prev = arr[idx - 1];
        const cpX1 = prev.x + (curr.x - prev.x) / 2;
        const cpY1 = prev.y;
        const cpX2 = prev.x + (curr.x - prev.x) / 2;
        const cpY2 = curr.y;
        return `${acc} C ${cpX1} ${cpY1}, ${cpX2} ${cpY2}, ${curr.x} ${curr.y}`;
      }, '');

  const areaD = points.length > 1
    ? `${pathD} L ${points[points.length - 1].x} ${padding.top + innerHeight} L ${points[0].x} ${padding.top + innerHeight} Z`
    : '';

  const formatValue = (num) => {
    if (isCurrency) {
      return `₹${Number(num).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
    }
    return Number(num).toLocaleString();
  };

  const yTicks = [0, maxVal * 0.25, maxVal * 0.5, maxVal * 0.75, maxVal];

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

        {hoverIndex !== null && points[hoverIndex] && (
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
            <span style={{ color: 'var(--text-secondary)' }}>{points[hoverIndex].label}:</span>
            <span style={{ color }}>{formatValue(points[hoverIndex].val)}</span>
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
            <linearGradient id={`gradient-${color.replace('#', '')}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color} stopOpacity="0.45" />
              <stop offset="100%" stopColor={color} stopOpacity="0.0" />
            </linearGradient>
          </defs>

          {/* Grid lines & Y-ticks */}
          {yTicks.map((val, idx) => {
            const y = padding.top + innerHeight - ((val - minVal) / (maxVal - minVal || 1)) * innerHeight;
            return (
              <g key={`ytick-${idx}`}>
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
                  fontSize="11"
                  textAnchor="end"
                  fontWeight="500"
                >
                  {formatValue(val)}
                </text>
              </g>
            );
          })}

          {/* Area fill */}
          {areaD && (
            <path
              d={areaD}
              fill={`url(#gradient-${color.replace('#', '')})`}
            />
          )}

          {/* Line stroke */}
          {pathD && (
            <path
              d={pathD}
              fill="none"
              stroke={color}
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          )}

          {/* X Axis labels (sampling every nth if many points) */}
          {points.map((p, idx) => {
            const shouldShowLabel = points.length <= 10 || idx % Math.ceil(points.length / 8) === 0 || idx === points.length - 1;
            return (
              <g key={`xpoint-${idx}`}>
                {shouldShowLabel && (
                  <text
                    x={p.x}
                    y={height - 12}
                    fill="var(--text-secondary)"
                    fontSize="11"
                    textAnchor="middle"
                    fontWeight="500"
                  >
                    {p.label}
                  </text>
                )}
                {/* Invisible hover zone */}
                <rect
                  x={p.x - 15}
                  y={padding.top}
                  width="30"
                  height={innerHeight}
                  fill="transparent"
                  style={{ cursor: 'pointer' }}
                  onMouseEnter={() => setHoverIndex(idx)}
                />
              </g>
            );
          })}

          {/* Active hover indicator */}
          {hoverIndex !== null && points[hoverIndex] && (
            <g>
              <line
                x1={points[hoverIndex].x}
                y1={padding.top}
                x2={points[hoverIndex].x}
                y2={padding.top + innerHeight}
                stroke={color}
                strokeWidth="1.5"
                strokeDasharray="3 3"
              />
              <circle
                cx={points[hoverIndex].x}
                cy={points[hoverIndex].y}
                r="6"
                fill="#FFFFFF"
                stroke={color}
                strokeWidth="3"
                style={{ filter: `drop-shadow(0 0 8px ${color})` }}
              />
            </g>
          )}
        </svg>
      </div>
    </div>
  );
}
