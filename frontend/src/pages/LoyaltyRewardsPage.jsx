import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { loyaltyService } from '../services/loyaltyService';
import { couponService } from '../services/couponService';
import {
  Gift,
  Award,
  Sparkles,
  Tag,
  ArrowUpRight,
  ArrowDownRight,
  RefreshCw,
  Copy,
  Check,
  ShieldCheck,
  Ticket
} from 'lucide-react';

export function LoyaltyRewardsPage() {
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copiedCode, setCopiedCode] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [accRes, txnRes, couponRes] = await Promise.allSettled([
        loyaltyService.getBalance(),
        loyaltyService.getTransactions(),
        couponService.getActiveCoupons()
      ]);

      if (accRes.status === 'fulfilled' && accRes.value?.data) {
        setAccount(accRes.value.data);
      }
      if (txnRes.status === 'fulfilled' && txnRes.value?.data) {
        setTransactions(txnRes.value.data);
      }
      if (couponRes.status === 'fulfilled' && couponRes.value?.data) {
        setCoupons(couponRes.value.data);
      }
    } catch (e) {
      console.warn('Failed to load rewards data:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const copyToClipboard = (code) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2500);
  };

  return (
    <div className="container" style={{ padding: '2.5rem 1.5rem', maxWidth: '1100px', margin: '0 auto' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
            <Gift size={28} color="var(--accent-gold)" />
            <h1 style={{ fontSize: '2rem', fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>
              CineSmart Rewards Club
            </h1>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', margin: 0 }}>
            Earn points on every confirmed movie ticket and redeem for instant discounts at checkout.
          </p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="btn-secondary"
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.6rem 1rem' }}
        >
          <RefreshCw size={15} className={loading ? 'spin' : ''} />
          <span>Refresh Balance</span>
        </button>
      </div>

      {/* Rewards Stats Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '1.25rem',
        marginBottom: '2.5rem'
      }}>
        {/* Active Points Balance Card */}
        <div className="glass-panel" style={{
          padding: '1.5rem',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid rgba(245, 158, 11, 0.3)',
          background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(0, 0, 0, 0.3) 100%)',
          position: 'relative'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              Available Reward Points
            </span>
            <Sparkles size={20} color="var(--accent-gold)" />
          </div>
          <div style={{ fontSize: '2.4rem', fontWeight: 900, color: 'var(--accent-gold)', lineHeight: 1.1, marginBottom: '0.4rem' }}>
            {account ? account.pointsBalance : 0} <span style={{ fontSize: '1.1rem', fontWeight: 600 }}>pts</span>
          </div>
          <div style={{ fontSize: '0.85rem', color: 'var(--accent-green)', fontWeight: 600 }}>
            = ₹{account ? Number(account.rupeeValue || account.pointsBalance || 0).toFixed(2) : '0.00'} instant discount value
          </div>
        </div>

        {/* Lifetime Earned Card */}
        <div className="glass-panel" style={{
          padding: '1.5rem',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border-subtle)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              Lifetime Points Earned
            </span>
            <Award size={20} color="var(--accent-red)" />
          </div>
          <div style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--text-primary)', lineHeight: 1.1, marginBottom: '0.4rem' }}>
            {account ? account.lifetimePointsEarned : 0} <span style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-muted)' }}>pts</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            1 Point awarded per ₹10 spent
          </div>
        </div>

        {/* Lifetime Redeemed Card */}
        <div className="glass-panel" style={{
          padding: '1.5rem',
          borderRadius: 'var(--radius-lg)',
          border: '1px solid var(--border-subtle)'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', fontWeight: 600 }}>
              Lifetime Points Redeemed
            </span>
            <Ticket size={20} color="#38bdf8" />
          </div>
          <div style={{ fontSize: '2.2rem', fontWeight: 800, color: 'var(--text-primary)', lineHeight: 1.1, marginBottom: '0.4rem' }}>
            {account ? account.lifetimePointsRedeemed : 0} <span style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-muted)' }}>pts</span>
          </div>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
            Total savings: ₹{account ? Number(account.lifetimePointsRedeemed || 0).toFixed(2) : '0.00'}
          </div>
        </div>
      </div>

      {/* Available Coupons Banner Section */}
      <div style={{ marginBottom: '3rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
          <Tag size={20} color="var(--accent-gold)" />
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, margin: 0 }}>
            Active Promotional Offers & Coupons
          </h2>
        </div>

        {coupons.length === 0 ? (
          <div className="glass-panel" style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No active promotional coupons at this moment. Check back soon for festive discounts!
          </div>
        ) : (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: '1rem'
          }}>
            {coupons.map((c) => (
              <div
                key={c.id || c.code}
                className="glass-panel"
                style={{
                  padding: '1.25rem',
                  borderRadius: 'var(--radius-md)',
                  border: '1px dashed rgba(245, 158, 11, 0.35)',
                  background: 'rgba(255, 255, 255, 0.02)',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between'
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                    <span style={{
                      fontWeight: 900,
                      color: 'var(--accent-gold)',
                      fontSize: '1.15rem',
                      letterSpacing: '0.05em'
                    }}>
                      {c.code}
                    </span>
                    <span className="badge badge-gold" style={{ fontSize: '0.72rem' }}>
                      {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT OFF`}
                    </span>
                  </div>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0 0 0.75rem' }}>
                    {c.description}
                  </p>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                    {c.minBookingAmount > 0 && <span>Min order: ₹{Number(c.minBookingAmount).toFixed(2)} &bull; </span>}
                    {c.maxDiscountAmount > 0 && <span>Max discount: ₹{Number(c.maxDiscountAmount).toFixed(2)} &bull; </span>}
                    {c.validUntil && <span>Expires: {new Date(c.validUntil).toLocaleDateString()}</span>}
                  </div>
                </div>

                <button
                  type="button"
                  onClick={() => copyToClipboard(c.code)}
                  className="btn-secondary"
                  style={{
                    width: '100%',
                    justifyContent: 'center',
                    fontSize: '0.8rem',
                    padding: '0.5rem',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.4rem'
                  }}
                >
                  {copiedCode === c.code ? (
                    <>
                      <Check size={14} color="var(--accent-green)" />
                      <span style={{ color: 'var(--accent-green)', fontWeight: 700 }}>Code Copied!</span>
                    </>
                  ) : (
                    <>
                      <Copy size={14} />
                      <span>Copy Promo Code</span>
                    </>
                  )}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Loyalty Points Transaction History */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
          <Sparkles size={20} color="var(--accent-gold)" />
          <h2 style={{ fontSize: '1.35rem', fontWeight: 800, margin: 0 }}>
            Points Transaction History
          </h2>
        </div>

        {transactions.length === 0 ? (
          <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <Gift size={36} color="var(--text-muted)" style={{ margin: '0 auto 0.75rem', opacity: 0.6 }} />
            <p style={{ margin: 0, fontSize: '0.95rem' }}>No loyalty points transactions yet.</p>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Book your favorite movie tickets to earn your first reward points!
            </p>
            <Link to="/movies" className="btn-primary" style={{ display: 'inline-flex', marginTop: '1rem', padding: '0.6rem 1.2rem' }}>
              Explore Movies
            </Link>
          </div>
        ) : (
          <div className="glass-panel" style={{ overflowX: 'auto', borderRadius: 'var(--radius-md)' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', background: 'rgba(255, 255, 255, 0.02)' }}>
                  <th style={{ padding: '0.85rem 1.25rem', color: 'var(--text-muted)', fontWeight: 600 }}>Date</th>
                  <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Type</th>
                  <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Points Change</th>
                  <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Booking Ref</th>
                  <th style={{ padding: '0.85rem 1.25rem', color: 'var(--text-muted)', fontWeight: 600 }}>Details</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((t) => {
                  const isEarned = t.transactionType === 'EARNED';
                  const isRedeemed = t.transactionType === 'REDEEMED';
                  const isReversed = t.transactionType === 'REVERSED';

                  return (
                    <tr key={t.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                      <td style={{ padding: '0.85rem 1.25rem', color: 'var(--text-secondary)' }}>
                        {t.createdAt ? new Date(t.createdAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : '—'}
                      </td>
                      <td style={{ padding: '0.85rem 1rem' }}>
                        <span
                          className={`badge ${
                            isEarned ? 'badge-green' : isRedeemed ? 'badge-blue' : 'badge-gold'
                          }`}
                          style={{ fontSize: '0.72rem' }}
                        >
                          {t.transactionType}
                        </span>
                      </td>
                      <td style={{ padding: '0.85rem 1rem', fontWeight: 800 }}>
                        <span style={{
                          color: t.points > 0 ? 'var(--accent-green)' : 'var(--accent-red)',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.2rem'
                        }}>
                          {t.points > 0 ? <ArrowUpRight size={14} /> : <ArrowDownRight size={14} />}
                          {t.points > 0 ? `+${t.points}` : t.points} pts
                        </span>
                      </td>
                      <td style={{ padding: '0.85rem 1rem', fontFamily: 'monospace', color: 'var(--accent-gold)' }}>
                        {t.bookingReference || '—'}
                      </td>
                      <td style={{ padding: '0.85rem 1.25rem', color: 'var(--text-secondary)', fontSize: '0.82rem' }}>
                        {t.description || '—'}
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
export default LoyaltyRewardsPage;

