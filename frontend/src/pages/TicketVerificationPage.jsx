import React, { useState, useEffect } from 'react';
import { ticketService } from '../services/ticketService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import {
  QrCode,
  ShieldCheck,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  RefreshCw,
  Search,
  Film,
  MapPin,
  Calendar,
  Clock,
  Armchair,
  Camera,
  Check,
  Ban,
  UserCheck,
  History,
  Sparkles
} from 'lucide-react';

export function TicketVerificationPage() {
  const [ticketInput, setTicketInput] = useState('');
  const [hashInput, setHashInput] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [verificationResult, setVerificationResult] = useState(null);
  const [verificationError, setVerificationError] = useState(null);

  // Statistics
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [statsError, setStatsError] = useState(null);

  // Scanner Simulator / Camera states
  const [cameraActive, setCameraActive] = useState(false);

  const fetchStats = async () => {
    try {
      setStatsLoading(true);
      setStatsError(null);
      const data = await ticketService.getTicketStats();
      setStats(data);
    } catch (err) {
      setStatsError(err.message || 'Unable to load ticket statistics');
    } finally {
      setStatsLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const handleVerify = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    if (!ticketInput.trim()) return;

    let targetCode = ticketInput.trim();
    let targetHash = hashInput.trim();

    // Check if input was a scanned JSON payload from the QR code SVG
    try {
      if (targetCode.startsWith('{') && targetCode.endsWith('}')) {
        const parsed = JSON.parse(targetCode);
        if (parsed.ticketCode) targetCode = parsed.ticketCode;
        if (parsed.hash) targetHash = parsed.hash;
      }
    } catch {
      // Not JSON, treat as raw ticket code
    }

    try {
      setVerifying(true);
      setVerificationResult(null);
      setVerificationError(null);

      const response = await ticketService.verifyTicket(targetCode, targetHash || null);
      setVerificationResult(response);
      setTicketInput('');
      setHashInput('');
      await fetchStats();
    } catch (err) {
      setVerificationError({
        message: err.message || 'Ticket verification failed.',
        code: err.errorCode || 'VERIFICATION_ERROR',
        ticketCode: targetCode
      });
      await fetchStats();
    } finally {
      setVerifying(false);
    }
  };

  const handleQuickFill = (code) => {
    setTicketInput(code);
    setVerificationResult(null);
    setVerificationError(null);
  };

  return (
    <div style={{ maxWidth: '1180px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.4rem' }}>
            <div
              style={{
                width: '40px',
                height: '40px',
                borderRadius: '10px',
                background: 'linear-gradient(135deg, var(--accent-cyan), #0284c7)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: 'var(--shadow-cyan)'
              }}
            >
              <QrCode size={22} color="#fff" />
            </div>
            <h1 style={{ fontSize: '2.1rem', fontWeight: 800 }}>Theatre Gate Ticket Verification</h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
            Verify cinema passes, inspect cryptographic QR tokens, and atomically register entry admissions.
          </p>
        </div>

        <button
          onClick={fetchStats}
          disabled={statsLoading}
          className="btn-secondary"
          style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', padding: '0.55rem 1rem', fontSize: '0.85rem' }}
          title="Refresh Statistics & Verification History"
        >
          <RefreshCw size={15} className={statsLoading ? 'spin' : ''} />
          <span>Refresh Telemetry</span>
        </button>
      </div>

      {/* Metrics Row */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '1.25rem',
          marginBottom: '2.25rem'
        }}
      >
        {/* Metric 1 */}
        <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', borderLeft: '4px solid var(--accent-cyan)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
              Total Issued
            </span>
            <QrCode size={18} color="var(--accent-cyan)" />
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--text-primary)' }}>
            {stats ? stats.totalGenerated : '—'}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>All confirmed bookings</span>
        </div>

        {/* Metric 2 */}
        <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', borderLeft: '4px solid var(--accent-emerald)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
              Admitted / Used
            </span>
            <CheckCircle2 size={18} color="var(--accent-emerald)" />
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-emerald)' }}>
            {stats ? stats.verifiedTickets : '—'}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Validated at gate entrance</span>
        </div>

        {/* Metric 3 */}
        <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', borderLeft: '4px solid var(--accent-gold)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
              Active / Unused
            </span>
            <ShieldCheck size={18} color="var(--accent-gold)" />
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-gold)' }}>
            {stats ? stats.unusedTickets : '—'}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Awaiting admission</span>
        </div>

        {/* Metric 4 */}
        <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', borderLeft: '4px solid var(--accent-red)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontWeight: 600 }}>
              Rejected Attempts
            </span>
            <AlertTriangle size={18} color="var(--accent-red)" />
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: 'var(--accent-red)' }}>
            {stats ? stats.invalidAttempts : '—'}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Tampered, cancelled, or duplicate</span>
        </div>
      </div>

      {/* Main Verification Grid */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))',
          gap: '1.75rem',
          marginBottom: '2.5rem'
        }}
      >
        {/* Left Column: Verification Scanner & Terminal */}
        <div className="glass-panel" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <UserCheck size={20} color="var(--accent-cyan)" />
              <h2 style={{ fontSize: '1.2rem', fontWeight: 700 }}>Scan or Input Ticket</h2>
            </div>
            <button
              onClick={() => setCameraActive(!cameraActive)}
              className="btn-secondary"
              style={{
                fontSize: '0.78rem',
                padding: '0.35rem 0.65rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.35rem',
                borderColor: cameraActive ? 'var(--accent-cyan)' : 'var(--border-subtle)',
                color: cameraActive ? 'var(--accent-cyan)' : 'var(--text-secondary)'
              }}
            >
              <Camera size={14} />
              <span>{cameraActive ? 'Camera Active' : 'Enable Camera'}</span>
            </button>
          </div>

          {/* Interactive Simulated Camera / Viewfinder */}
          {cameraActive && (
            <div
              style={{
                height: '180px',
                borderRadius: 'var(--radius-md)',
                background: '#040711',
                border: '2px dashed var(--accent-cyan)',
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                overflow: 'hidden'
              }}
            >
              {/* Scan Line Animation */}
              <div
                style={{
                  position: 'absolute',
                  top: '10%',
                  left: '10%',
                  right: '10%',
                  height: '2px',
                  background: 'var(--accent-cyan)',
                  boxShadow: '0 0 12px var(--accent-cyan)',
                  animation: 'pulse 2s infinite'
                }}
              />
              <QrCode size={48} color="rgba(6, 182, 212, 0.4)" style={{ marginBottom: '0.5rem' }} />
              <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                Point optical scanner or webcam at visitor's ticket QR code
              </span>
              <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                Barcode/QR scanner gun auto-fills the input below
              </span>
            </div>
          )}

          {/* Input Form */}
          <form onSubmit={handleVerify} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                Ticket Code / Scanned QR JSON <span style={{ color: 'var(--accent-red)' }}>*</span>
              </label>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  value={ticketInput}
                  onChange={(e) => setTicketInput(e.target.value)}
                  placeholder="e.g. TKT-2026-B817E2 or paste QR string"
                  className="form-input"
                  style={{
                    fontFamily: 'monospace',
                    fontSize: '0.95rem',
                    textTransform: 'uppercase',
                    paddingRight: '2.5rem'
                  }}
                  autoFocus
                />
                <Search size={18} color="var(--text-muted)" style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)' }} />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                Cryptographic Token / QR Hash <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 400 }}>(Optional - extracted automatically)</span>
              </label>
              <input
                type="text"
                value={hashInput}
                onChange={(e) => setHashInput(e.target.value)}
                placeholder="SHA-256 verification hash"
                className="form-input"
                style={{ fontFamily: 'monospace', fontSize: '0.82rem' }}
              />
            </div>

            <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.25rem' }}>
              <button
                type="submit"
                disabled={verifying || !ticketInput.trim()}
                className="btn-primary"
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  padding: '0.75rem 1.25rem',
                  fontSize: '0.92rem'
                }}
              >
                {verifying ? (
                  <>
                    <RefreshCw size={16} className="spin" />
                    <span>Verifying Entry...</span>
                  </>
                ) : (
                  <>
                    <ShieldCheck size={18} />
                    <span>Verify & Admit Ticket</span>
                  </>
                )}
              </button>

              <button
                type="button"
                onClick={() => {
                  setTicketInput('');
                  setHashInput('');
                  setVerificationResult(null);
                  setVerificationError(null);
                }}
                className="btn-secondary"
                style={{ padding: '0.75rem 1rem', fontSize: '0.85rem' }}
              >
                Clear
              </button>
            </div>
          </form>

          {/* Quick Helper for Staff Testing */}
          <div
            style={{
              background: 'rgba(255, 255, 255, 0.02)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
              padding: '0.75rem 1rem',
              fontSize: '0.78rem',
              color: 'var(--text-secondary)'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontWeight: 600, color: 'var(--accent-cyan)', marginBottom: '0.3rem' }}>
              <Sparkles size={14} />
              <span>Gate Operator Tip:</span>
            </div>
            <span>
              You can connect any USB/Bluetooth 2D barcode scanner gun directly. The scanner will automatically stream the QR payload into the input box and trigger instant atomic entry validation.
            </span>
          </div>
        </div>

        {/* Right Column: Live Decision Outcome Display */}
        <div
          className="glass-panel"
          style={{
            padding: '1.75rem',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            minHeight: '360px',
            border: verificationResult
              ? '2px solid var(--accent-emerald)'
              : verificationError
              ? '2px solid var(--accent-red)'
              : '1px solid var(--border-subtle)'
          }}
        >
          {verificationResult ? (
            /* SUCCESS OUTCOME */
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              {/* Glowing Banner */}
              <div
                style={{
                  background: 'rgba(16, 185, 129, 0.15)',
                  border: '1px solid var(--accent-emerald)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1rem 1.25rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem'
                }}
              >
                <CheckCircle2 size={32} color="var(--accent-emerald)" style={{ flexShrink: 0 }} />
                <div>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--accent-emerald)', letterSpacing: '0.02em' }}>
                    ENTRY GRANTED &bull; TICKET VERIFIED
                  </h3>
                  <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', margin: 0 }}>
                    {verificationResult.message || 'Ticket is valid. Marked as USED in database.'}
                  </p>
                </div>
              </div>

              {/* Verified Details Card */}
              {verificationResult.ticket && (
                <div
                  style={{
                    background: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    padding: '1.25rem',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.85rem'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                      Ticket Code
                    </span>
                    <span style={{ fontSize: '1rem', fontWeight: 800, fontFamily: 'monospace', color: 'var(--accent-cyan)' }}>
                      {verificationResult.ticket.ticketCode}
                    </span>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                    {verificationResult.ticket.posterUrl ? (
                      <img
                        src={verificationResult.ticket.posterUrl}
                        alt="Movie"
                        style={{ width: '45px', height: '65px', objectFit: 'cover', borderRadius: '4px' }}
                      />
                    ) : (
                      <div
                        style={{
                          width: '45px',
                          height: '65px',
                          borderRadius: '4px',
                          background: 'rgba(255,255,255,0.05)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center'
                        }}
                      >
                        <Film size={20} color="var(--accent-red)" />
                      </div>
                    )}
                    <div>
                      <h4 style={{ fontSize: '1.05rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                        {verificationResult.ticket.movieTitle}
                      </h4>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                        <MapPin size={12} color="var(--accent-red)" />
                        <span>{verificationResult.ticket.cinemaName} &bull; {verificationResult.ticket.screenName || `Screen ${verificationResult.ticket.screenNumber || 1}`}</span>
                      </div>
                    </div>
                  </div>

                  {/* Seat & Booking Info */}
                  <div
                    style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(3, 1fr)',
                      gap: '0.5rem',
                      background: 'rgba(0, 0, 0, 0.3)',
                      padding: '0.65rem 0.85rem',
                      borderRadius: 'var(--radius-sm)',
                      fontSize: '0.82rem'
                    }}
                  >
                    <div>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block' }}>SEAT</span>
                      <strong style={{ color: 'var(--accent-gold)' }}>
                        {verificationResult.ticket.seatLabel} ({verificationResult.ticket.seatTier})
                      </strong>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block' }}>PRICE</span>
                      <strong style={{ color: 'var(--accent-green)' }}>
                        ₹{Number(verificationResult.ticket.price).toFixed(2)}
                      </strong>
                    </div>
                    <div>
                      <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block' }}>ADMISSION TIME</span>
                      <strong style={{ color: 'var(--text-primary)' }}>
                        {new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                      </strong>
                    </div>
                  </div>

                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>Operator: <strong>{verificationResult.verifiedBy || 'Gate Staff'}</strong></span>
                    <span className="badge badge-emerald" style={{ fontSize: '0.7rem' }}>
                      <Check size={11} /> STATUS: USED
                    </span>
                  </div>
                </div>
              )}
            </div>
          ) : verificationError ? (
            /* FAILURE OUTCOME */
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div
                style={{
                  background: 'rgba(239, 68, 68, 0.15)',
                  border: '1px solid var(--accent-red)',
                  borderRadius: 'var(--radius-md)',
                  padding: '1.25rem',
                  display: 'flex',
                  alignItems: 'flex-start',
                  gap: '0.85rem'
                }}
              >
                <XCircle size={36} color="var(--accent-red)" style={{ flexShrink: 0, marginTop: '2px' }} />
                <div>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--accent-red)', marginBottom: '0.35rem' }}>
                    ENTRY REJECTED
                  </h3>
                  <p style={{ fontSize: '0.9rem', color: 'var(--text-primary)', marginBottom: '0.5rem', fontWeight: 600 }}>
                    {verificationError.message}
                  </p>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    Error Classification: <strong style={{ color: 'var(--accent-gold)' }}>{verificationError.code}</strong>
                  </div>
                  {verificationError.ticketCode && (
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                      Submitted Code: <strong style={{ color: 'var(--accent-cyan)', fontFamily: 'monospace' }}>{verificationError.ticketCode}</strong>
                    </div>
                  )}
                </div>
              </div>

              <div
                style={{
                  background: 'rgba(255, 255, 255, 0.02)',
                  border: '1px solid var(--border-subtle)',
                  borderRadius: 'var(--radius-sm)',
                  padding: '0.85rem 1rem',
                  fontSize: '0.8rem',
                  color: 'var(--text-secondary)'
                }}
              >
                <strong style={{ color: 'var(--accent-red)', display: 'block', marginBottom: '0.25rem' }}>
                  Safety Protocol:
                </strong>
                If this ticket was previously validated, inspect the patron's receipt or consult the supervisor terminal before allowing access. Do not permit entry if the ticket is marked CANCELLED or VOIDED.
              </div>
            </div>
          ) : (
            /* WAITING STATE */
            <div style={{ textAlign: 'center', padding: '2rem 1rem' }}>
              <div
                style={{
                  width: '64px',
                  height: '64px',
                  borderRadius: '50%',
                  background: 'rgba(255, 255, 255, 0.04)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  margin: '0 auto 1.25rem'
                }}
              >
                <QrCode size={32} color="var(--text-muted)" />
              </div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '0.4rem' }}>
                Gate Terminal Ready
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', maxWidth: '320px', margin: '0 auto' }}>
                Scan a ticket QR code with your scanner gun or input the ticket identifier on the left to verify validity.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Verification Audit Log Table */}
      <div className="glass-panel" style={{ padding: '1.75rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <History size={20} color="var(--accent-gold)" />
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Recent Verification Audit Logs</h2>
          </div>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Showing latest {stats?.recentVerifications?.length || 0} attempts
          </span>
        </div>

        {statsLoading ? (
          <LoadingSpinner message="Loading gate access logs..." />
        ) : !stats?.recentVerifications || stats.recentVerifications.length === 0 ? (
          <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '2rem 0' }}>
            No recent verification entries recorded yet.
          </p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem 1rem' }}>Attempted At</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Ticket Code</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Operator</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Result</th>
                  <th style={{ padding: '0.75rem 1rem' }}>Remarks / Reason</th>
                  <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentVerifications.map((log, idx) => {
                  const isSuccess = log.result === 'SUCCESS';
                  const dateStr = log.attemptedAt ? new Date(log.attemptedAt).toLocaleString([], { dateStyle: 'short', timeStyle: 'medium' }) : '—';

                  return (
                    <tr
                      key={log.id || idx}
                      style={{
                        borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
                        background: idx % 2 === 0 ? 'transparent' : 'rgba(255, 255, 255, 0.015)'
                      }}
                    >
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-secondary)' }}>
                        {dateStr}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', fontFamily: 'monospace', fontWeight: 700, color: 'var(--accent-cyan)' }}>
                        {log.ticketCode || 'UNKNOWN'}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-primary)' }}>
                        {log.verifiedBy || 'System Staff'}
                      </td>
                      <td style={{ padding: '0.75rem 1rem' }}>
                        {isSuccess ? (
                          <span className="badge badge-emerald" style={{ fontSize: '0.72rem' }}>
                            <Check size={11} style={{ marginRight: '3px' }} /> GRANTED
                          </span>
                        ) : log.result === 'ALREADY_USED' ? (
                          <span className="badge badge-gold" style={{ fontSize: '0.72rem' }}>
                            ALREADY USED
                          </span>
                        ) : (
                          <span className="badge badge-red" style={{ fontSize: '0.72rem' }}>
                            {log.result}
                          </span>
                        )}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', color: 'var(--text-secondary)', maxWidth: '280px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {log.failureReason || 'Valid entry granted'}
                      </td>
                      <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                        {log.ticketCode && (
                          <button
                            onClick={() => handleQuickFill(log.ticketCode)}
                            className="btn-secondary"
                            style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }}
                            title="Test this code again"
                          >
                            Inspect
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
