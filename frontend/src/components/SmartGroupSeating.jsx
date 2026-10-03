import React, { useState } from 'react';
import { seatService } from '../services/seatService';
import {
  Users,
  Sparkles,
  Sliders,
  Check,
  AlertCircle,
  ChevronDown,
  ChevronUp,
  Info,
  DollarSign
} from 'lucide-react';

export function SmartGroupSeating({ showId, availableSeatsCount, onApplyRecommendation, currentSelectedSeats = [] }) {
  const [isOpen, setIsOpen] = useState(false);
  const [partySize, setPartySize] = useState(4);
  const [preferredTier, setPreferredTier] = useState('');
  const [preferredRow, setPreferredRow] = useState('');
  const [allowSplitRows, setAllowSplitRows] = useState(true);
  const [requireAccessibility, setRequireAccessibility] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [response, setResponse] = useState(null);
  const [selectedRank, setSelectedRank] = useState(null);

  const handleFindSeats = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError(null);
    setSelectedRank(null);

    try {
      const payload = {
        showId: Number(showId),
        partySize: Number(partySize),
        preferredTier: preferredTier || null,
        preferredRow: preferredRow || null,
        allowSplitRows,
        requireAccessibility
      };

      const result = await seatService.getGroupRecommendations(showId, payload);
      setResponse(result);

      // Auto-apply the #1 best match if recommendations are returned
      if (result && result.recommendations && result.recommendations.length > 0) {
        handleSelectRecommendation(result.recommendations[0]);
      }
    } catch (err) {
      setError(err.message || 'Failed to generate group seating recommendations.');
      setResponse(null);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectRecommendation = (rec) => {
    setSelectedRank(rec.rank);
    if (onApplyRecommendation && rec.seats) {
      onApplyRecommendation(rec.seats);
    }
  };

  return (
    <div className="glass-panel" style={{
      padding: '1.25rem 1.5rem',
      marginBottom: '2rem',
      border: '1px solid rgba(6, 182, 212, 0.3)',
      background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.8) 0%, rgba(6, 182, 212, 0.08) 100%)',
      borderRadius: 'var(--radius-md)',
      boxShadow: '0 4px 20px rgba(0, 0, 0, 0.3)'
    }}>
      {/* Header bar / Toggle */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        cursor: 'pointer',
        userSelect: 'none'
      }} onClick={() => setIsOpen(!isOpen)}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            width: '38px',
            height: '38px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, var(--accent-red), var(--accent-gold))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 2px 10px rgba(229, 9, 20, 0.4)'
          }}>
            <Sparkles size={20} color="#fff" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
                Smart Group Seating
              </h3>
              <span style={{
                fontSize: '0.7rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                padding: '0.15rem 0.5rem',
                borderRadius: '999px',
                background: 'rgba(6, 182, 212, 0.2)',
                color: 'var(--accent-cyan)',
                border: '1px solid rgba(6, 182, 212, 0.4)'
              }}>
                Phase 3 Engine
              </span>
            </div>
            <p style={{ margin: '0.15rem 0 0 0', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
              Automatically finds and scores the best contiguous or split-row seats for your group.
            </p>
          </div>
        </div>

        <button
          type="button"
          className="btn-secondary"
          style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem', gap: '0.4rem' }}
          onClick={(e) => {
            e.stopPropagation();
            setIsOpen(!isOpen);
          }}
        >
          <span>{isOpen ? 'Collapse' : 'Configure Group'}</span>
          {isOpen ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      </div>

      {/* Expanded Configuration and Results Body */}
      {isOpen && (
        <div style={{ marginTop: '1.5rem', borderTop: '1px solid var(--border-subtle)', paddingTop: '1.25rem' }}>
          {/* Controls Form */}
          <form onSubmit={handleFindSeats} style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', alignItems: 'end', marginBottom: '1.5rem' }}>
            {/* Party Size */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                <Users size={14} style={{ display: 'inline', marginRight: '0.3rem', verticalAlign: '-2px' }} />
                Group Size (People)
              </label>
              <select
                value={partySize}
                onChange={(e) => setPartySize(Number(e.target.value))}
                style={{
                  width: '100%',
                  padding: '0.55rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg-card)',
                  color: 'var(--text-primary)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.9rem',
                  fontWeight: 600
                }}
              >
                {[2, 3, 4, 5, 6, 7, 8, 9, 10].map((n) => (
                  <option key={n} value={n}>{n} People</option>
                ))}
              </select>
            </div>

            {/* Preferred Tier */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                Preferred Tier
              </label>
              <select
                value={preferredTier}
                onChange={(e) => setPreferredTier(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.55rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg-card)',
                  color: 'var(--text-primary)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.9rem'
                }}
              >
                <option value="">Any Tier</option>
                <option value="STANDARD">Standard</option>
                <option value="PREMIUM">Premium</option>
                <option value="RECLINER">Recliner</option>
              </select>
            </div>

            {/* Preferred Row */}
            <div>
              <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                Preferred Row (Optional)
              </label>
              <select
                value={preferredRow}
                onChange={(e) => setPreferredRow(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.55rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg-card)',
                  color: 'var(--text-primary)',
                  border: '1px solid var(--border-subtle)',
                  fontSize: '0.9rem'
                }}
              >
                <option value="">No Preference (Auto-Sweet Spot)</option>
                <option value="A">Row A (Front / Accessible)</option>
                <option value="B">Row B</option>
                <option value="C">Row C</option>
                <option value="D">Row D</option>
                <option value="E">Row E (Optimal Optical Center)</option>
                <option value="F">Row F</option>
              </select>
            </div>

            {/* Options Checkboxes */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', justifyContent: 'center' }}>
              <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', cursor: 'pointer', color: 'var(--text-secondary)' }}>
                <input
                  type="checkbox"
                  checked={allowSplitRows}
                  onChange={(e) => setAllowSplitRows(e.target.checked)}
                  style={{ accentColor: 'var(--accent-cyan)' }}
                />
                <span>Allow split across 2 adjacent rows</span>
              </label>
              <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', cursor: 'pointer', color: 'var(--text-secondary)' }}>
                <input
                  type="checkbox"
                  checked={requireAccessibility}
                  onChange={(e) => setRequireAccessibility(e.target.checked)}
                  style={{ accentColor: 'var(--seat-accessible)' }}
                />
                <span>Wheelchair accessible needed</span>
              </label>
            </div>

            {/* Find Seats Action */}
            <div>
              <button
                type="submit"
                disabled={loading}
                className="btn-primary"
                style={{
                  width: '100%',
                  padding: '0.65rem 1rem',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  background: 'linear-gradient(135deg, var(--accent-red), #b91c1c)'
                }}
              >
                <Sparkles size={16} />
                <span>{loading ? 'Analyzing Seating...' : 'Find Best Seats'}</span>
              </button>
            </div>
          </form>

          {/* Feedback Messages */}
          {error && (
            <div style={{
              padding: '0.85rem 1rem',
              borderRadius: 'var(--radius-sm)',
              background: 'rgba(239, 68, 68, 0.15)',
              border: '1px solid rgba(239, 68, 68, 0.4)',
              color: '#fca5a5',
              fontSize: '0.85rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              marginBottom: '1rem'
            }}>
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {response && response.recommendations && response.recommendations.length === 0 && (
            <div style={{
              padding: '1.25rem',
              borderRadius: 'var(--radius-sm)',
              background: 'rgba(245, 158, 11, 0.12)',
              border: '1px solid rgba(245, 158, 11, 0.35)',
              color: 'var(--accent-gold)',
              fontSize: '0.9rem',
              textAlign: 'center'
            }}>
              <p style={{ fontWeight: 600, margin: '0 0 0.3rem 0' }}>{response.message}</p>
              <p style={{ margin: 0, fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Try reducing your party size, enabling "Allow split across 2 rows", or selecting individual seats manually on the map.
              </p>
            </div>
          )}

          {/* Recommendation Cards */}
          {response && response.recommendations && response.recommendations.length > 0 && (
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--accent-cyan)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Top Recommended Arrangements ({response.recommendations.length})
                </span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  Click any option to preview and select on the seat map
                </span>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
                {response.recommendations.map((rec) => {
                  const isSelected = selectedRank === rec.rank;
                  const seatLabels = rec.seats ? rec.seats.map((s) => `${s.row}${s.number}`).join(', ') : '';

                  return (
                    <div
                      key={rec.rank}
                      onClick={() => handleSelectRecommendation(rec)}
                      style={{
                        padding: '1.1rem',
                        borderRadius: 'var(--radius-sm)',
                        background: isSelected ? 'rgba(6, 182, 212, 0.15)' : 'var(--bg-glass)',
                        border: isSelected ? '2px solid var(--accent-cyan)' : '1px solid var(--border-subtle)',
                        boxShadow: isSelected ? '0 0 15px rgba(6, 182, 212, 0.3)' : 'none',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'space-between',
                        gap: '0.75rem'
                      }}
                    >
                      <div>
                        {/* Top Badges */}
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                          <span style={{
                            padding: '0.2rem 0.5rem',
                            borderRadius: '4px',
                            background: rec.rank === 1 ? 'linear-gradient(135deg, var(--accent-red), #b91c1c)' : 'rgba(255, 255, 255, 0.1)',
                            color: '#fff',
                            fontSize: '0.75rem',
                            fontWeight: 800,
                            letterSpacing: '0.05em'
                          }}>
                            {rec.rank === 1 ? '★ Best Match (#1)' : `Option #${rec.rank}`}
                          </span>

                          <div style={{
                            fontSize: '0.9rem',
                            fontWeight: 800,
                            color: rec.score >= 90 ? 'var(--accent-green)' : 'var(--accent-gold)'
                          }}>
                            {rec.score}% Match
                          </div>
                        </div>

                        {/* Title & Description */}
                        <h4 style={{ margin: '0 0 0.3rem 0', fontSize: '1rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                          {rec.description}
                        </h4>
                        <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                          {rec.explanation}
                        </p>

                        {/* Seat badges preview */}
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.3rem', marginTop: '0.4rem' }}>
                          {rec.seats?.map((s) => (
                            <span
                              key={s.showSeatId}
                              style={{
                                padding: '0.2rem 0.45rem',
                                borderRadius: '4px',
                                background: 'rgba(255, 255, 255, 0.08)',
                                border: '1px solid var(--border-subtle)',
                                fontSize: '0.75rem',
                                fontWeight: 700,
                                color: 'var(--text-primary)'
                              }}
                            >
                              {s.row}{s.number}
                            </span>
                          ))}
                        </div>
                      </div>

                      {/* Bottom Footer: Price and Select CTA */}
                      <div style={{
                        borderTop: '1px solid var(--border-subtle)',
                        paddingTop: '0.65rem',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                      }}>
                        <div>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block' }}>Total ({rec.seats?.length} Tickets)</span>
                          <span style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--accent-gold)' }}>
                            ${Number(rec.totalPrice).toFixed(2)}
                          </span>
                        </div>

                        <button
                          type="button"
                          className={isSelected ? 'btn-primary' : 'btn-secondary'}
                          style={{
                            padding: '0.35rem 0.85rem',
                            fontSize: '0.8rem',
                            gap: '0.35rem',
                            borderColor: isSelected ? 'var(--accent-cyan)' : 'var(--border-subtle)'
                          }}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleSelectRecommendation(rec);
                          }}
                        >
                          {isSelected ? (
                            <>
                              <Check size={14} color="#fff" />
                              <span>Selected</span>
                            </>
                          ) : (
                            <span>Select Arrangement</span>
                          )}
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
