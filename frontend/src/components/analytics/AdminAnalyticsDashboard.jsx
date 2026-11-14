import React, { useState, useEffect } from 'react';
import { analyticsService } from '../../services/analyticsService';
import { AnalyticsKPICard } from './AnalyticsKPICard';
import { AnalyticsLineChart } from './AnalyticsLineChart';
import { AnalyticsBarChart } from './AnalyticsBarChart';
import { AnalyticsPieChart } from './AnalyticsPieChart';
import { AnalyticsFilterBar } from './AnalyticsFilterBar';
import { LoadingSpinner } from '../LoadingSpinner';
import { ErrorMessage } from '../ErrorMessage';
import {
  Users, Ticket, CheckCircle2, XCircle, IndianRupee,
  RotateCcw, Film, Building2, Percent, TrendingUp,
  Tag, Award, ShieldAlert, BarChart3, PieChart, LineChart
} from 'lucide-react';

export function AdminAnalyticsDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [filters, setFilters] = useState({
    preset: '30D',
    startDate: '',
    endDate: '',
    movieId: '',
    cinemaId: '',
    showId: '',
    status: 'ALL'
  });

  const [activeRevenueTab, setActiveRevenueTab] = useState('daily'); // 'daily' | 'weekly' | 'monthly'
  const [activeBookingTab, setActiveBookingTab] = useState('daily'); // 'daily' | 'weekly' | 'monthly'

  useEffect(() => {
    // Initial 30-day preset dates
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);
    const fmt = (d) => d.toISOString().split('T')[0];

    setFilters(prev => ({
      ...prev,
      startDate: fmt(start),
      endDate: fmt(end)
    }));
  }, []);

  useEffect(() => {
    fetchAnalytics();
  }, [filters.startDate, filters.endDate, filters.movieId, filters.cinemaId, filters.showId, filters.status]);

  async function fetchAnalytics() {
    try {
      setLoading(true);
      setError(null);
      const res = await analyticsService.getOverview(filters);
      setData(res);
    } catch (err) {
      setError(err.message || 'Failed to load analytics records.');
    } finally {
      setLoading(false);
    }
  }

  const handleResetFilters = () => {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);
    const fmt = (d) => d.toISOString().split('T')[0];

    setFilters({
      preset: '30D',
      startDate: fmt(start),
      endDate: fmt(end),
      movieId: '',
      cinemaId: '',
      showId: '',
      status: 'ALL'
    });
  };

  const kpi = data?.kpi || {};
  const revenue = data?.revenue || {};
  const bookings = data?.bookings || {};
  const movies = data?.movies || {};
  const theatres = data?.theatres || {};
  const mkt = data?.marketingRefunds || {};
  const filterOptions = data?.filterOptions || { movies: [], cinemas: [], shows: [], statuses: [] };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Filters Bar */}
      <AnalyticsFilterBar
        filters={filters}
        onChange={setFilters}
        onReset={handleResetFilters}
        onRefresh={fetchAnalytics}
        loading={loading}
        options={filterOptions}
      />

      {loading && !data ? (
        <LoadingSpinner message="Aggregating PostgreSQL telemetry, ticket sales, and financial ledgers..." />
      ) : error ? (
        <ErrorMessage message={error} onRetry={fetchAnalytics} />
      ) : (
        <>
          {/* SECTION 1: 9 SUMMARY KPI CARDS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem' }}>
              <BarChart3 size={20} color="var(--accent-red)" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                Executive Key Performance Indicators (KPIs)
              </h2>
            </div>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '1.25rem'
            }}>
              {/* Total Revenue */}
              <AnalyticsKPICard
                title="Gross Revenue"
                value={`₹${Number(kpi.totalRevenue || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
                subtext="Confirmed ticket payments"
                badgeText={`${kpi.confirmedRate || 0}% Confirmed`}
                badgeColor="#10B981"
                badgeBg="rgba(16, 185, 129, 0.15)"
                icon={IndianRupee}
                iconColor="#10B981"
                iconBg="rgba(16, 185, 129, 0.15)"
              />

              {/* Net Revenue */}
              <AnalyticsKPICard
                title="Net Revenue"
                value={`₹${Number(kpi.netRevenue || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
                subtext="Gross less approved refunds"
                badgeText="Audited"
                badgeColor="#38BDF8"
                badgeBg="rgba(56, 189, 248, 0.15)"
                icon={TrendingUp}
                iconColor="#38BDF8"
                iconBg="rgba(56, 189, 248, 0.15)"
              />

              {/* Total Bookings */}
              <AnalyticsKPICard
                title="Total Bookings"
                value={Number(kpi.totalBookings || 0).toLocaleString()}
                subtext="Total transactions initiated"
                badgeText="All Channels"
                badgeColor="#F59E0B"
                badgeBg="rgba(245, 158, 11, 0.15)"
                icon={Ticket}
                iconColor="#F59E0B"
                iconBg="rgba(245, 158, 11, 0.15)"
              />

              {/* Confirmed Bookings */}
              <AnalyticsKPICard
                title="Confirmed Bookings"
                value={Number(kpi.confirmedBookings || 0).toLocaleString()}
                subtext="Successfully paid & ticketed"
                badgeText={`${kpi.confirmedRate || 0}% Conv.`}
                badgeColor="#10B981"
                badgeBg="rgba(16, 185, 129, 0.15)"
                icon={CheckCircle2}
                iconColor="#10B981"
                iconBg="rgba(16, 185, 129, 0.15)"
              />

              {/* Cancelled Bookings */}
              <AnalyticsKPICard
                title="Cancelled Bookings"
                value={Number(kpi.cancelledBookings || 0).toLocaleString()}
                subtext="Voided reservations"
                badgeText={`${kpi.cancellationRate || 0}% Rate`}
                badgeColor="#EF4444"
                badgeBg="rgba(239, 68, 68, 0.15)"
                icon={XCircle}
                iconColor="#EF4444"
                iconBg="rgba(239, 68, 68, 0.15)"
              />

              {/* Refund Amount */}
              <AnalyticsKPICard
                title="Refund Amount"
                value={`₹${Number(kpi.refundAmount || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`}
                subtext="Disbursed to patrons"
                badgeText="Verified"
                badgeColor="#A78BFA"
                badgeBg="rgba(167, 139, 250, 0.15)"
                icon={RotateCcw}
                iconColor="#A78BFA"
                iconBg="rgba(167, 139, 250, 0.15)"
              />

              {/* Average Occupancy */}
              <AnalyticsKPICard
                title="Average Occupancy"
                value={`${kpi.averageOccupancyPercentage || 0}%`}
                subtext="Seats booked vs screen capacity"
                badgeText="Utilization"
                badgeColor="#F43F5E"
                badgeBg="rgba(244, 63, 94, 0.15)"
                icon={Percent}
                iconColor="#F43F5E"
                iconBg="rgba(244, 63, 94, 0.15)"
              />

              {/* Registered Users */}
              <AnalyticsKPICard
                title="Registered Users"
                value={Number(kpi.totalUsers || 0).toLocaleString()}
                subtext="Patrons with accounts"
                badgeText="Accounts"
                badgeColor="#06B6D4"
                badgeBg="rgba(6, 182, 212, 0.15)"
                icon={Users}
                iconColor="#06B6D4"
                iconBg="rgba(6, 182, 212, 0.15)"
              />

              {/* Catalog Scope */}
              <AnalyticsKPICard
                title="Theatres & Movies"
                value={`${kpi.totalTheatres || 0} / ${kpi.totalMovies || 0}`}
                subtext="Active venues & running titles"
                badgeText="Venues"
                badgeColor="#EAB308"
                badgeBg="rgba(234, 179, 8, 0.15)"
                icon={Building2}
                iconColor="#EAB308"
                iconBg="rgba(234, 179, 8, 0.15)"
              />
            </div>
          </section>

          {/* SECTION 2: REVENUE ANALYTICS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <LineChart size={20} color="var(--accent-red)" />
                <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                  Revenue Analytics & Growth Velocity
                </h2>
              </div>

              {/* Granularity Toggles */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                {['daily', 'weekly', 'monthly'].map((tab) => (
                  <button
                    key={tab}
                    type="button"
                    onClick={() => setActiveRevenueTab(tab)}
                    style={{
                      padding: '4px 12px',
                      borderRadius: '8px',
                      fontSize: '0.8rem',
                      fontWeight: 600,
                      cursor: 'pointer',
                      border: activeRevenueTab === tab ? '1px solid var(--accent-red)' : '1px solid rgba(255, 255, 255, 0.1)',
                      background: activeRevenueTab === tab ? 'rgba(229, 9, 20, 0.25)' : 'rgba(255, 255, 255, 0.04)',
                      color: activeRevenueTab === tab ? '#fff' : 'var(--text-secondary)',
                      textTransform: 'capitalize'
                    }}
                  >
                    {tab}
                  </button>
                ))}
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
              {/* Primary Time Series Chart */}
              <div style={{ gridColumn: 'span 2' }}>
                <AnalyticsLineChart
                  data={
                    activeRevenueTab === 'daily'
                      ? revenue.dailyRevenue
                      : activeRevenueTab === 'weekly'
                      ? revenue.weeklyRevenue
                      : revenue.monthlyRevenue
                  }
                  xKey="period"
                  yKey="revenue"
                  title={`${activeRevenueTab.toUpperCase()} Revenue Timeline`}
                  subtitle="Confirmed payments across all active screenings"
                  color="#E50914"
                  height={280}
                  isCurrency={true}
                />
              </div>

              {/* Revenue Period Comparison Card */}
              <div
                className="glass-panel"
                style={{
                  padding: '1.5rem',
                  borderRadius: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)'
                }}
              >
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff', margin: '0 0 0.4rem' }}>
                    Revenue Comparison
                  </h3>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', margin: 0 }}>
                    Current period vs historical benchmark
                  </p>
                </div>

                <div style={{ margin: '1.5rem 0' }}>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '2rem', fontWeight: 800, color: '#fff' }}>
                      ₹{Number(revenue.currentPeriodRevenue || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                    </span>
                    <span style={{
                      fontSize: '0.85rem',
                      fontWeight: 700,
                      color: revenue.growthPercentage >= 0 ? '#10B981' : '#EF4444',
                      background: revenue.growthPercentage >= 0 ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                      padding: '2px 8px',
                      borderRadius: '999px'
                    }}>
                      {revenue.growthPercentage >= 0 ? `+${revenue.growthPercentage}%` : `${revenue.growthPercentage}%`}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    Previous period: ₹{Number(revenue.previousPeriodRevenue || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                  </div>
                </div>

                <div style={{
                  padding: '0.85rem',
                  borderRadius: '10px',
                  background: 'rgba(255, 255, 255, 0.04)',
                  border: '1px solid rgba(255, 255, 255, 0.08)',
                  fontSize: '0.8rem',
                  color: 'var(--text-secondary)',
                  lineHeight: 1.5
                }}>
                  💡 Revenue definition excludes unconfirmed holds, failed payment transactions, and unverified simulations to maintain 100% financial accuracy.
                </div>
              </div>
            </div>
          </section>

          {/* SECTION 3: BOOKING ANALYTICS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Ticket size={20} color="var(--accent-gold)" />
                <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                  Booking Flow & Status Distribution
                </h2>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
              {/* Daily Bookings Chart */}
              <AnalyticsLineChart
                data={bookings.dailyBookings}
                xKey="period"
                yKey="bookingsCount"
                title="Daily Reservation Volume"
                subtitle="Total bookings initiated per day"
                color="#F59E0B"
                height={260}
                isCurrency={false}
              />

              {/* Booking Status Distribution Donut */}
              <AnalyticsPieChart
                data={bookings.statusDistribution}
                title="Booking Status Distribution"
                subtitle="Lifecycle breakdown of all reservations"
                nameKey="name"
                valueKey="count"
                isCurrency={false}
                centerLabel="Bookings"
              />
            </div>
          </section>

          {/* SECTION 4: MOVIE & GENRE ANALYTICS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem' }}>
              <Film size={20} color="var(--accent-cyan)" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                Movie Performance & Genre Popularity
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
              {/* Most Booked Movies */}
              <AnalyticsBarChart
                data={movies.mostBookedMovies}
                title="Most Booked Movies"
                subtitle="Total confirmed reservations per movie"
                color="#38BDF8"
                countKey="count"
                nameKey="name"
                isCurrency={false}
                height={260}
              />

              {/* Popular Genres Pie */}
              <AnalyticsPieChart
                data={movies.popularGenres}
                title="Popular Movie Genres"
                subtitle="Audience share by genre"
                nameKey="name"
                valueKey="count"
                isCurrency={false}
                centerLabel="Patrons"
              />

              {/* Movie-wise Revenue Bar */}
              <AnalyticsBarChart
                data={movies.movieWiseRevenue}
                title="Movie-wise Revenue Breakdown"
                subtitle="Gross box office revenue generated by each title"
                color="#10B981"
                valueKey="value"
                nameKey="name"
                isCurrency={true}
                height={260}
              />
            </div>
          </section>

          {/* SECTION 5: THEATRE & SCREEN OCCUPANCY ANALYTICS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem' }}>
              <Building2 size={20} color="#A78BFA" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                Theatre Venues & Screen Occupancy
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
              {/* Theatre-wise Bookings */}
              <AnalyticsBarChart
                data={theatres.theatreWiseBookings}
                title="Theatre-wise Reservations"
                subtitle="Booking volume by cinema hall"
                color="#A78BFA"
                countKey="count"
                nameKey="name"
                isCurrency={false}
                height={260}
              />

              {/* Theatre-wise Revenue */}
              <AnalyticsBarChart
                data={theatres.theatreWiseRevenue}
                title="Theatre-wise Revenue"
                subtitle="Gross revenue by cinema venue"
                color="#F59E0B"
                valueKey="value"
                nameKey="name"
                isCurrency={true}
                height={260}
              />

              {/* Screen Occupancy Percentages */}
              <AnalyticsBarChart
                data={theatres.screenOccupancy}
                title="Screen Capacity Utilization"
                subtitle="Average occupancy percentage per hall"
                color="#EC4899"
                valueKey="percentage"
                nameKey="name"
                isCurrency={false}
                height={260}
              />
            </div>

            {/* Show-wise Performance Table */}
            {theatres.showPerformance && theatres.showPerformance.length > 0 && (
              <div
                className="glass-panel"
                style={{
                  marginTop: '1.5rem',
                  padding: '1.5rem',
                  borderRadius: '16px',
                  overflowX: 'auto'
                }}
              >
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff', margin: '0 0 1rem' }}>
                  Top Performing Showtimes
                </h3>

                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', color: 'var(--text-muted)' }}>
                      <th style={{ padding: '8px 12px' }}>Showtime & Title</th>
                      <th style={{ padding: '8px 12px' }}>Reference</th>
                      <th style={{ padding: '8px 12px' }}>Tickets Sold</th>
                      <th style={{ padding: '8px 12px' }}>Occupancy</th>
                      <th style={{ padding: '8px 12px', textAlign: 'right' }}>Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {theatres.showPerformance.map((s, idx) => (
                      <tr
                        key={`show-perf-${idx}`}
                        style={{
                          borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
                          transition: 'background 0.2s'
                        }}
                        onMouseOver={(e) => (e.currentTarget.style.background = 'rgba(255, 255, 255, 0.03)')}
                        onMouseOut={(e) => (e.currentTarget.style.background = 'transparent')}
                      >
                        <td style={{ padding: '10px 12px', color: '#fff', fontWeight: 600 }}>{s.name}</td>
                        <td style={{ padding: '10px 12px', color: 'var(--text-secondary)' }}>{s.extra}</td>
                        <td style={{ padding: '10px 12px', color: '#fff' }}>{s.count} seats</td>
                        <td style={{ padding: '10px 12px' }}>
                          <span style={{
                            padding: '2px 8px',
                            borderRadius: '999px',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            background: s.percentage >= 50 ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                            color: s.percentage >= 50 ? '#10B981' : '#F59E0B'
                          }}>
                            {s.percentage}%
                          </span>
                        </td>
                        <td style={{ padding: '10px 12px', textAlign: 'right', color: '#10B981', fontWeight: 700 }}>
                          ₹{Number(s.value || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          {/* SECTION 6: PROMOTIONS, LOYALTY & REFUND ANALYTICS */}
          <section>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '1.25rem' }}>
              <Tag size={20} color="#F43F5E" />
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff', margin: 0 }}>
                Coupons, Loyalty Rewards & Refund Management
              </h2>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
              {/* Coupon Usage */}
              <div
                className="glass-panel"
                style={{
                  padding: '1.5rem',
                  borderRadius: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '1rem' }}>
                  <Tag size={16} color="var(--accent-red)" />
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff', margin: 0 }}>
                    Promotional Coupon Redemptions
                  </h3>
                </div>

                {mkt.couponUsage && mkt.couponUsage.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                    {mkt.couponUsage.map((c, idx) => (
                      <div
                        key={`coupon-${idx}`}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '8px 12px',
                          borderRadius: '8px',
                          background: 'rgba(255, 255, 255, 0.04)',
                          border: '1px solid rgba(255, 255, 255, 0.08)'
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 700, color: '#fff', fontSize: '0.9rem' }}>{c.name}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{c.count} times applied</div>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ color: 'var(--accent-gold)', fontWeight: 700, fontSize: '0.9rem' }}>
                            -₹{Number(c.value || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                          </div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Total Savings</div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                    No coupons applied during this timeframe.
                  </p>
                )}
              </div>

              {/* Loyalty Points Metrics */}
              <div
                className="glass-panel"
                style={{
                  padding: '1.5rem',
                  borderRadius: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '1rem' }}>
                  <Award size={16} color="var(--accent-gold)" />
                  <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#fff', margin: 0 }}>
                    Loyalty Rewards Circulation
                  </h3>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', margin: '0.5rem 0' }}>
                  <div style={{ padding: '0.85rem', borderRadius: '10px', background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.2)' }}>
                    <div style={{ fontSize: '0.75rem', color: 'var(--accent-gold)', fontWeight: 600 }}>POINTS EARNED</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#fff', marginTop: '2px' }}>
                      {Number(mkt.loyaltyPointsEarnedTotal || 0).toLocaleString()}
                    </div>
                  </div>

                  <div style={{ padding: '0.85rem', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
                    <div style={{ fontSize: '0.75rem', color: '#10B981', fontWeight: 600 }}>POINTS REDEEMED</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#fff', marginTop: '2px' }}>
                      {Number(mkt.loyaltyPointsRedeemedTotal || 0).toLocaleString()}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px', borderRadius: '8px', background: 'rgba(255, 255, 255, 0.04)' }}>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Loyalty Discount Savings</span>
                  <span style={{ fontSize: '0.95rem', fontWeight: 700, color: '#10B981' }}>
                    ₹{Number(mkt.loyaltyDiscountTotal || 0).toLocaleString('en-IN', { maximumFractionDigits: 0 })}
                  </span>
                </div>
              </div>

              {/* Refund Statistics Distribution */}
              <AnalyticsPieChart
                data={mkt.refundStatusDistribution}
                title="Refund Claims by State"
                subtitle="REQUESTED, APPROVED, COMPLETED & REJECTED"
                nameKey="name"
                valueKey="count"
                isCurrency={false}
                centerLabel="Refunds"
              />
            </div>
          </section>
        </>
      )}
    </div>
  );
}
