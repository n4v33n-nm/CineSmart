import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { paymentService } from '../services/paymentService';
import { bookingService } from '../services/bookingService';
import {
  CreditCard,
  Smartphone,
  Building,
  Wallet,
  CheckCircle,
  AlertCircle,
  ShieldCheck,
  Ticket,
  Lock,
  Clock,
  RefreshCw,
  X,
  ChevronRight,
  QrCode
} from 'lucide-react';

export function PaymentModal({ booking, onClose, onBookingCompleted }) {
  const navigate = useNavigate();

  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [simulateSuccess, setSimulateSuccess] = useState(true);
  const [failureReason, setFailureReason] = useState('Simulated card decline: Insufficient balance');
  const [loading, setLoading] = useState(false);
  const [paymentResult, setPaymentResult] = useState(null);
  const [paymentError, setPaymentError] = useState(null);

  // Mock fields for user realism (never sent as real financial data)
  const [upiId, setUpiId] = useState('customer@okhdfcbank');
  const [selectedBank, setSelectedBank] = useState('HDFC Bank');
  const [selectedWallet, setSelectedWallet] = useState('Paytm');

  // Real-Time 5-minute Seat Lock Countdown
  const [timeLeft, setTimeLeft] = useState(() => {
    if (booking?.expiresAt) {
      const diff = Math.floor((new Date(booking.expiresAt).getTime() - Date.now()) / 1000);
      return diff > 0 ? diff : 300;
    }
    return 300; // 5 minutes standard
  });

  const handleExpire = async () => {
    try {
      if (booking?.id && !paymentResult) {
        await bookingService.cancelBooking(booking.id);
      }
    } catch (e) {
      // Ignored
    }
    alert('Your 5-minute seat reservation has expired. Your seats have been released back to availability.');
    onClose();
  };

  const handleCancelAndClose = async () => {
    try {
      if (booking?.id && !paymentResult) {
        await bookingService.cancelBooking(booking.id);
      }
    } catch (e) {
      // Ignored
    }
    onClose();
  };

  useEffect(() => {
    if (paymentResult || loading) return;
    if (timeLeft <= 0) {
      handleExpire();
      return;
    }
    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          handleExpire();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [timeLeft, paymentResult, loading]);

  const handlePay = async () => {
    try {
      setLoading(true);
      setPaymentError(null);

      const res = await paymentService.initiateAndProcessPayment(
        booking.id,
        paymentMethod,
        simulateSuccess,
        simulateSuccess ? null : failureReason
      );

      if (res.status === 'SUCCESS') {
        setPaymentResult(res);
        if (onBookingCompleted) {
          onBookingCompleted(res);
        }
      } else {
        setPaymentError({
          message: res.failureReason || 'Payment declined by simulated payment gateway.',
          details: res
        });
      }
    } catch (err) {
      setPaymentError({
        message: err.message || 'Payment processing failed. Your held seats have been released.',
        details: err
      });
    } finally {
      setLoading(false);
    }
  };

  const showTime = booking.showStartTime ? new Date(booking.showStartTime) : null;
  const timeFormatted = showTime ? showTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'TBD';
  const dateFormatted = showTime ? showTime.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }) : 'TBD';

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'rgba(0, 0, 0, 0.85)',
      backdropFilter: 'blur(10px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
      padding: '1rem',
      overflowY: 'auto'
    }}>
      <div className="glass-panel" style={{
        maxWidth: '620px',
        width: '100%',
        maxHeight: '90vh',
        overflowY: 'auto',
        padding: '2rem',
        borderRadius: 'var(--radius-lg)',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.75)',
        position: 'relative'
      }}>
        {/* Close Button (Only if not processing and not success) */}
        {!loading && !paymentResult && (
          <button
            onClick={handleCancelAndClose}
            style={{
              position: 'absolute',
              top: '1.25rem',
              right: '1.25rem',
              background: 'none',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer'
            }}
            title="Cancel payment"
          >
            <X size={20} />
          </button>
        )}

        {/* STATE 1: PAYMENT SUCCESS & TICKET CONFIRMATION */}
        {paymentResult ? (
          <div style={{ textAlign: 'center' }}>
            <div style={{
              width: '68px',
              height: '68px',
              borderRadius: '50%',
              background: 'rgba(16, 185, 129, 0.15)',
              border: '2px solid var(--accent-green)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.25rem'
            }}>
              <CheckCircle size={38} color="var(--accent-green)" />
            </div>

            <h2 style={{ fontSize: '1.8rem', fontWeight: 800, marginBottom: '0.35rem', color: 'var(--text-primary)' }}>
              Payment Successful!
            </h2>
            <p style={{ color: 'var(--accent-green)', fontWeight: 600, fontSize: '1rem', marginBottom: '1.5rem' }}>
              Booking Confirmed &bull; Reference: {booking.bookingReference}
            </p>

            {/* Digital Ticket Representation */}
            <div style={{
              background: 'linear-gradient(135deg, rgba(255,255,255,0.06) 0%, rgba(255,255,255,0.02) 100%)',
              border: '1px solid rgba(255,255,255,0.15)',
              borderRadius: 'var(--radius-md)',
              padding: '1.5rem',
              textAlign: 'left',
              marginBottom: '1.75rem',
              position: 'relative',
              overflow: 'hidden'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px dashed var(--border-subtle)', paddingBottom: '0.75rem', marginBottom: '1rem' }}>
                <div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    Digital Cinema Pass
                  </div>
                  <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--text-primary)' }}>
                    {booking.movieTitle}
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span className="badge badge-green">Confirmed</span>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem', fontSize: '0.88rem', marginBottom: '1.25rem' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.75rem' }}>Cinema & Screen</span>
                  <strong style={{ color: 'var(--text-secondary)' }}>{booking.cinemaName} &bull; {booking.screenName}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.75rem' }}>Date & Showtime</span>
                  <strong style={{ color: 'var(--text-secondary)' }}>{dateFormatted} at {timeFormatted}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.75rem' }}>Reserved Seats</span>
                  <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap', marginTop: '0.2rem' }}>
                    {booking.seats?.map((s) => (
                      <span key={s.id} style={{
                        padding: '0.15rem 0.45rem',
                        background: 'rgba(229, 9, 20, 0.15)',
                        border: '1px solid rgba(229, 9, 20, 0.4)',
                        borderRadius: '4px',
                        fontWeight: 700,
                        color: 'var(--accent-red)',
                        fontSize: '0.8rem'
                      }}>
                        {s.seatLabel || `${s.rowIdentifier}${s.columnNumber}`}
                      </span>
                    ))}
                  </div>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.75rem' }}>Amount Paid</span>
                  <strong style={{ color: 'var(--accent-green)', fontSize: '1.05rem' }}>
                    ₹{Number(paymentResult.amount || booking.totalAmount).toFixed(2)}
                  </strong>
                </div>
              </div>

              {/* Digital Tickets Line Items */}
              {paymentResult.tickets && paymentResult.tickets.length > 0 && (
                <div style={{ borderTop: '1px dashed var(--border-subtle)', paddingTop: '1rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '0.5rem' }}>
                    Ticket Identifiers & Verification QR Hash
                  </span>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
                    {paymentResult.tickets.map((t) => (
                      <div key={t.id || t.ticketCode} style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '0.8rem',
                        background: 'rgba(0,0,0,0.25)',
                        padding: '0.4rem 0.6rem',
                        borderRadius: '4px'
                      }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                          <Ticket size={14} color="var(--accent-gold)" />
                          <span style={{ fontWeight: 700, color: 'var(--accent-gold)' }}>{t.ticketCode}</span>
                          <span style={{ color: 'var(--text-muted)' }}>({t.seatLabel})</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: 'var(--text-muted)', fontSize: '0.7rem' }}>
                          <QrCode size={13} />
                          <span title={t.qrVerificationHash}>
                            {t.qrVerificationHash ? t.qrVerificationHash.substring(0, 10) + '...' : 'VERIFIED'}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div style={{ marginTop: '0.85rem', fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', justifyContent: 'space-between' }}>
                <span>Txn ID: {paymentResult.gatewayTxnId || paymentResult.transactionReference}</span>
                <span>Method: {paymentResult.paymentMethod}</span>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <Link to="/bookings" className="btn-primary" style={{ padding: '0.8rem 1.6rem' }}>
                <span>View My Bookings</span>
                <ChevronRight size={16} />
              </Link>
              <Link to="/movies" className="btn-secondary" style={{ padding: '0.8rem 1.6rem' }}>
                <span>Back to Movies</span>
              </Link>
            </div>
          </div>
        ) : paymentError ? (
          /* STATE 2: PAYMENT FAILURE SCREEN */
          <div style={{ textAlign: 'center' }}>
            <div style={{
              width: '68px',
              height: '68px',
              borderRadius: '50%',
              background: 'rgba(239, 68, 68, 0.15)',
              border: '2px solid var(--accent-red)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              margin: '0 auto 1.25rem'
            }}>
              <AlertCircle size={38} color="var(--accent-red)" />
            </div>

            <h2 style={{ fontSize: '1.75rem', fontWeight: 800, marginBottom: '0.4rem', color: 'var(--text-primary)' }}>
              Payment Failed
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', marginBottom: '1.5rem', maxWidth: '440px', margin: '0 auto 1.5rem' }}>
              Your booking was not confirmed. Your selected seats have been released back to availability.
            </p>

            <div style={{
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.25)',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem',
              textAlign: 'left',
              marginBottom: '1.75rem'
            }}>
              <div style={{ fontSize: '0.8rem', color: 'var(--accent-red)', fontWeight: 700, marginBottom: '0.35rem' }}>
                Gateway Rejection Notice:
              </div>
              <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                {paymentError.message}
              </div>
            </div>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
              <button onClick={onClose} className="btn-primary" style={{ padding: '0.8rem 1.6rem' }}>
                <RefreshCw size={16} />
                <span>Re-select Seats & Try Again</span>
              </button>
            </div>
          </div>
        ) : loading ? (
          /* STATE 3: PROCESSING SPINNER */
          <div style={{ textAlign: 'center', padding: '3rem 1rem' }}>
            <div style={{
              width: '64px',
              height: '64px',
              border: '3px solid var(--border-subtle)',
              borderTopColor: 'var(--accent-red)',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
              margin: '0 auto 1.5rem'
            }} />
            <h3 style={{ fontSize: '1.4rem', fontWeight: 700, marginBottom: '0.5rem' }}>
              Connecting to Payment Gateway...
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              Securing transaction token and processing {paymentMethod} payment.
            </p>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem', marginTop: '1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
              <Lock size={14} />
              <span>256-Bit Encrypted Simulated Transaction</span>
            </div>
          </div>
        ) : (
          /* STATE 4: CHECKOUT FORM */
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem' }}>
              <Lock size={20} color="var(--accent-red)" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Complete Your Payment</h2>
            </div>

            {/* Real-Time 5-Minute Seat Lock Banner */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '0.65rem 1rem',
              borderRadius: 'var(--radius-md)',
              background: timeLeft < 60 ? 'rgba(239, 68, 68, 0.15)' : 'rgba(245, 158, 11, 0.12)',
              border: `1px solid ${timeLeft < 60 ? 'var(--accent-red)' : 'var(--accent-gold)'}`,
              marginBottom: '1.25rem',
              fontSize: '0.85rem'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: timeLeft < 60 ? 'var(--accent-red)' : 'var(--accent-gold)', fontWeight: 600 }}>
                <Clock size={16} />
                <span>{timeLeft < 60 ? 'Warning: Seats releasing soon!' : 'Seats Temporarily Locked for You (5m)'}</span>
              </div>
              <div style={{
                fontFamily: 'monospace',
                fontWeight: 800,
                fontSize: '1.05rem',
                color: timeLeft < 60 ? 'var(--accent-red)' : 'var(--accent-gold)',
                letterSpacing: '0.05em'
              }}>
                {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
              </div>
            </div>

            {/* Order Summary Strip */}
            <div style={{
              background: 'var(--bg-glass)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '1.1rem',
              marginBottom: '1.5rem'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                <span style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-primary)' }}>
                  {booking.movieTitle}
                </span>
                <span style={{ fontWeight: 800, color: 'var(--accent-gold)', fontSize: '1.15rem' }}>
                  ₹{Number(booking.totalAmount).toFixed(2)}
                </span>
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                {booking.cinemaName} ({booking.screenName}) &bull; {booking.seats?.length} seat(s)
              </div>
              <div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
                {booking.seats?.map((s) => (
                  <span key={s.id} style={{
                    padding: '0.15rem 0.45rem',
                    background: 'rgba(255, 255, 255, 0.08)',
                    borderRadius: '3px',
                    fontSize: '0.75rem',
                    fontWeight: 700
                  }}>
                    {s.seatLabel || `${s.rowIdentifier}${s.columnNumber}`}
                  </span>
                ))}
              </div>
            </div>

            {/* Payment Method Tabs */}
            <div style={{ marginBottom: '1.5rem' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', display: 'block', marginBottom: '0.6rem' }}>
                Select Payment Method
              </span>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem' }}>
                {[
                  { id: 'UPI', label: 'UPI', icon: Smartphone },
                  { id: 'CARD', label: 'Cards', icon: CreditCard },
                  { id: 'NET_BANKING', label: 'NetBanking', icon: Building },
                  { id: 'WALLET', label: 'Wallet', icon: Wallet },
                ].map((item) => {
                  const Icon = item.icon;
                  const isSelected = paymentMethod === item.id;
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => setPaymentMethod(item.id)}
                      style={{
                        padding: '0.75rem 0.5rem',
                        borderRadius: 'var(--radius-md)',
                        border: isSelected ? '2px solid var(--accent-red)' : '1px solid var(--border-subtle)',
                        background: isSelected ? 'rgba(229, 9, 20, 0.15)' : 'var(--bg-glass)',
                        color: isSelected ? 'var(--text-primary)' : 'var(--text-secondary)',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: '0.4rem',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      <Icon size={18} color={isSelected ? 'var(--accent-red)' : 'var(--text-muted)'} />
                      <span style={{ fontSize: '0.78rem', fontWeight: 700 }}>{item.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Method Details (Simulated Inputs) */}
            <div style={{
              background: 'rgba(0, 0, 0, 0.2)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem',
              marginBottom: '1.5rem'
            }}>
              {paymentMethod === 'UPI' && (
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.4rem' }}>
                    Enter UPI ID / VPA
                  </label>
                  <input
                    type="text"
                    value={upiId}
                    onChange={(e) => setUpiId(e.target.value)}
                    placeholder="username@okhdfcbank"
                    className="input-field"
                    style={{ width: '100%', marginBottom: '0.75rem' }}
                  />
                  <div style={{ display: 'flex', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    <span>Supported:</span>
                    <strong style={{ color: 'var(--text-secondary)' }}>Google Pay, PhonePe, Paytm, BHIM</strong>
                  </div>
                </div>
              )}

              {paymentMethod === 'CARD' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
                      Card Number (Simulated)
                    </label>
                    <input
                      type="text"
                      disabled
                      value="•••• •••• •••• 4242"
                      className="input-field"
                      style={{ width: '100%', opacity: 0.8 }}
                    />
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                    <div>
                      <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
                        Expiry
                      </label>
                      <input
                        type="text"
                        disabled
                        value="12/28"
                        className="input-field"
                        style={{ width: '100%', opacity: 0.8 }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
                        CVV
                      </label>
                      <input
                        type="text"
                        disabled
                        value="•••"
                        className="input-field"
                        style={{ width: '100%', opacity: 0.8 }}
                      />
                    </div>
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    Test mode: No real credit/debit card numbers are persisted or processed.
                  </div>
                </div>
              )}

              {paymentMethod === 'NET_BANKING' && (
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.4rem' }}>
                    Choose Your Bank
                  </label>
                  <select
                    value={selectedBank}
                    onChange={(e) => setSelectedBank(e.target.value)}
                    className="input-field"
                    style={{ width: '100%' }}
                  >
                    <option value="HDFC Bank">HDFC Bank</option>
                    <option value="State Bank of India">State Bank of India</option>
                    <option value="ICICI Bank">ICICI Bank</option>
                    <option value="Axis Bank">Axis Bank</option>
                    <option value="Kotak Mahindra Bank">Kotak Mahindra Bank</option>
                  </select>
                </div>
              )}

              {paymentMethod === 'WALLET' && (
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.4rem' }}>
                    Select Digital Wallet
                  </label>
                  <select
                    value={selectedWallet}
                    onChange={(e) => setSelectedWallet(e.target.value)}
                    className="input-field"
                    style={{ width: '100%' }}
                  >
                    <option value="Paytm">Paytm Wallet</option>
                    <option value="Amazon Pay">Amazon Pay</option>
                    <option value="PhonePe">PhonePe Wallet</option>
                    <option value="Mobikwik">MobiKwik</option>
                  </select>
                </div>
              )}
            </div>

            {/* Test Simulation Controls */}
            <div style={{
              background: 'rgba(255, 255, 255, 0.03)',
              border: '1px dashed rgba(255, 255, 255, 0.2)',
              borderRadius: 'var(--radius-md)',
              padding: '0.85rem 1rem',
              marginBottom: '1.5rem',
              fontSize: '0.8rem'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                <span style={{ fontWeight: 700, color: 'var(--accent-gold)' }}>Gateway Simulation Mode:</span>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', cursor: 'pointer', color: simulateSuccess ? 'var(--accent-green)' : 'var(--text-muted)' }}>
                    <input
                      type="radio"
                      name="simulationMode"
                      checked={simulateSuccess}
                      onChange={() => setSimulateSuccess(true)}
                    />
                    <span>Success</span>
                  </label>
                  <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem', cursor: 'pointer', color: !simulateSuccess ? 'var(--accent-red)' : 'var(--text-muted)' }}>
                    <input
                      type="radio"
                      name="simulationMode"
                      checked={!simulateSuccess}
                      onChange={() => setSimulateSuccess(false)}
                    />
                    <span>Decline / Failure</span>
                  </label>
                </div>
              </div>
              {!simulateSuccess && (
                <div style={{ marginTop: '0.4rem', color: 'var(--accent-red)' }}>
                  Will simulate gateway decline to verify automatic seat release & failure recovery.
                </div>
              )}
            </div>

            {/* Pay Button */}
            <button
              onClick={handlePay}
              className="btn-primary"
              style={{
                width: '100%',
                justifyContent: 'center',
                padding: '0.9rem',
                fontSize: '1.05rem',
                fontWeight: 800,
                marginBottom: '0.75rem'
              }}
            >
              <span>Pay ₹{Number(booking.totalAmount).toFixed(2)} with {paymentMethod}</span>
              <ChevronRight size={18} />
            </button>

            <button
              onClick={onClose}
              className="btn-secondary"
              style={{ width: '100%', justifyContent: 'center', padding: '0.6rem' }}
            >
              Cancel Reservation & Release Seats
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
