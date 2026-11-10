import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '../services/adminService';
import { movieService } from '../services/movieService';
import { showService } from '../services/showService';
import { couponService } from '../services/couponService';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import {
  ShieldCheck,
  Film,
  Calendar,
  Layers,
  Plus,
  Trash2,
  XCircle,
  CheckCircle,
  Clock,
  IndianRupee,
  Tv,
  MapPin,
  QrCode,
  Tag,
  Edit3,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';

export function AdminDashboardPage() {
  const [activeTab, setActiveTab] = useState('movies'); // 'movies' | 'shows' | 'screens'
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Data states
  const [movies, setMovies] = useState([]);
  const [shows, setShows] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [screens, setScreens] = useState([]);
  const [coupons, setCoupons] = useState([]);

  // Modal / Form states
  const [showMovieModal, setShowMovieModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [showCouponModal, setShowCouponModal] = useState(false);
  const [editingCoupon, setEditingCoupon] = useState(null);

  // Movie Form
  const [movieForm, setMovieForm] = useState({
    title: '',
    description: '',
    durationMinutes: 120,
    language: 'English',
    genre: 'Action',
    releaseDate: '2026-05-01',
    posterUrl: '',
    certification: 'PG-13'
  });

  // Show Form
  const [showForm, setShowForm] = useState({
    movieId: '',
    screenId: '',
    startTime: '',
    basePrice: 200.00
  });

  // Coupon Form
  const [couponForm, setCouponForm] = useState({
    code: '',
    description: '',
    discountType: 'PERCENTAGE',
    discountValue: 20,
    minBookingAmount: 200,
    maxDiscountAmount: 100,
    validUntil: '',
    usageLimit: 100,
    perUserLimit: 1,
    isActive: true
  });

  const loadAllData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [moviesData, showsData, cinemasData, couponsRes] = await Promise.allSettled([
        movieService.getMovies(),
        showService.getShows(),
        adminService.getCinemas(),
        couponService.getAllCoupons()
      ]);

      if (moviesData.status === 'fulfilled') setMovies(moviesData.value);
      if (showsData.status === 'fulfilled') setShows(showsData.value);
      if (cinemasData.status === 'fulfilled') {
        const cList = cinemasData.value;
        setCinemas(cList);
        if (cList.length > 0) {
          const screensList = [];
          for (const c of cList) {
            const cScreens = await adminService.getScreensByCinema(c.id);
            screensList.push(...cScreens);
          }
          setScreens(screensList);
        }
      }
      if (couponsRes.status === 'fulfilled' && couponsRes.value?.data) {
        setCoupons(couponsRes.value.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load administration data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  const handleCreateMovie = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await adminService.createMovie({
        ...movieForm,
        durationMinutes: parseInt(movieForm.durationMinutes, 10)
      });
      setSuccessMsg(`Movie "${movieForm.title}" added successfully!`);
      setShowMovieModal(false);
      setMovieForm({
        title: '',
        description: '',
        durationMinutes: 120,
        language: 'English',
        genre: 'Action',
        releaseDate: '2026-05-01',
        posterUrl: '',
        certification: 'PG-13'
      });
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Could not create movie.');
    }
  };

  const handleDeactivateMovie = async (movieId, title) => {
    if (!window.confirm(`Are you sure you want to deactivate "${title}"?`)) return;
    try {
      setError(null);
      await adminService.deactivateMovie(movieId);
      setSuccessMsg(`Movie "${title}" deactivated.`);
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Could not deactivate movie.');
    }
  };

  const handleCreateShow = async (e) => {
    e.preventDefault();
    if (!showForm.movieId || !showForm.screenId || !showForm.startTime) {
      setError('Please fill all show scheduling fields.');
      return;
    }

    try {
      setError(null);
      // Auto-compute an estimated end time (startTime + 2.5 hours)
      const start = new Date(showForm.startTime);
      const end = new Date(start.getTime() + 150 * 60000);

      await adminService.createShow({
        movieId: parseInt(showForm.movieId, 10),
        screenId: parseInt(showForm.screenId, 10),
        startTime: start.toISOString().slice(0, 19),
        endTime: end.toISOString().slice(0, 19),
        basePrice: parseFloat(showForm.basePrice)
      });

      setSuccessMsg('Show scheduled successfully with real-time seat inventory generated.');
      setShowScheduleModal(false);
      setShowForm({
        movieId: '',
        screenId: '',
        startTime: '',
        basePrice: 200.00
      });
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Could not schedule show.');
    }
  };

  const handleCancelShow = async (showId) => {
    if (!window.confirm('Are you sure you want to cancel this scheduled show?')) return;
    try {
      setError(null);
      await adminService.cancelShow(showId);
      setSuccessMsg('Show cancelled.');
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Could not cancel show.');
    }
  };

  const handleOpenCreateCoupon = () => {
    setEditingCoupon(null);
    const thirtyDaysLater = new Date();
    thirtyDaysLater.setDate(thirtyDaysLater.getDate() + 30);

    setCouponForm({
      code: '',
      description: '',
      discountType: 'PERCENTAGE',
      discountValue: 20,
      minBookingAmount: 200,
      maxDiscountAmount: 100,
      validUntil: thirtyDaysLater.toISOString().slice(0, 16),
      usageLimit: 100,
      perUserLimit: 1,
      isActive: true
    });
    setShowCouponModal(true);
  };

  const handleOpenEditCoupon = (coupon) => {
    setEditingCoupon(coupon);
    setCouponForm({
      code: coupon.code || '',
      description: coupon.description || '',
      discountType: coupon.discountType || 'PERCENTAGE',
      discountValue: coupon.discountValue || 0,
      minBookingAmount: coupon.minBookingAmount || 0,
      maxDiscountAmount: coupon.maxDiscountAmount || '',
      validUntil: coupon.validUntil ? coupon.validUntil.slice(0, 16) : '',
      usageLimit: coupon.usageLimit || 100,
      perUserLimit: coupon.perUserLimit || 1,
      isActive: Boolean(coupon.isActive)
    });
    setShowCouponModal(true);
  };

  const handleSaveCoupon = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      const payload = {
        code: couponForm.code.trim().toUpperCase(),
        description: couponForm.description,
        discountType: couponForm.discountType,
        discountValue: parseFloat(couponForm.discountValue),
        minBookingAmount: parseFloat(couponForm.minBookingAmount || 0),
        maxDiscountAmount: couponForm.maxDiscountAmount ? parseFloat(couponForm.maxDiscountAmount) : null,
        validUntil: couponForm.validUntil ? new Date(couponForm.validUntil).toISOString().slice(0, 19) : null,
        usageLimit: parseInt(couponForm.usageLimit, 10),
        perUserLimit: parseInt(couponForm.perUserLimit, 10),
        isActive: Boolean(couponForm.isActive)
      };

      if (editingCoupon) {
        await couponService.updateCoupon(editingCoupon.id, payload);
        setSuccessMsg(`Coupon '${payload.code}' updated successfully.`);
      } else {
        await couponService.createCoupon(payload);
        setSuccessMsg(`Coupon '${payload.code}' created successfully.`);
      }

      setShowCouponModal(false);
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Failed to save coupon.');
    }
  };

  const handleToggleCoupon = async (id) => {
    try {
      setError(null);
      await couponService.toggleCoupon(id);
      setSuccessMsg('Coupon status toggled.');
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Failed to toggle coupon status.');
    }
  };

  const handleDeleteCoupon = async (id, code) => {
    if (!window.confirm(`Are you sure you want to deactivate/delete coupon '${code}'?`)) return;
    try {
      setError(null);
      await couponService.deleteCoupon(id);
      setSuccessMsg(`Coupon '${code}' deactivated.`);
      await loadAllData();
    } catch (err) {
      setError(err.message || 'Failed to deactivate coupon.');
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading CineSmart Administrative Console..." />;
  }

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
            <ShieldCheck size={28} color="var(--accent-gold)" />
            <h1 style={{ fontSize: '2rem', fontWeight: 800 }}>Admin Console</h1>
            <span className="badge badge-gold" style={{ fontSize: '0.75rem' }}>ROLE_ADMIN</span>
          </div>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
            Manage movies catalog, screen scheduling, and cinema infrastructure.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--bg-glass)', padding: '0.35rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
          <button
            onClick={() => setActiveTab('movies')}
            className={activeTab === 'movies' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <Film size={15} />
            <span>Movies ({movies.length})</span>
          </button>
          <button
            onClick={() => setActiveTab('shows')}
            className={activeTab === 'shows' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <Calendar size={15} />
            <span>Shows ({shows.length})</span>
          </button>
          <button
            onClick={() => setActiveTab('screens')}
            className={activeTab === 'screens' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <Tv size={15} />
            <span>Cinemas & Screens</span>
          </button>
          <button
            onClick={() => setActiveTab('coupons')}
            className={activeTab === 'coupons' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <Tag size={15} />
            <span>Coupons ({coupons.length})</span>
          </button>
          <Link
            to="/verify-tickets"
            className="btn-secondary"
            style={{
              padding: '0.5rem 1rem',
              fontSize: '0.85rem',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
              borderColor: 'rgba(6, 182, 212, 0.4)',
              color: 'var(--accent-cyan)'
            }}
          >
            <QrCode size={15} />
            <span>Gate Ticket Scanner</span>
          </Link>
        </div>
      </div>

      {/* Notifications */}
      {successMsg && (
        <div style={{
          padding: '0.85rem 1.25rem',
          borderRadius: 'var(--radius-md)',
          background: 'rgba(16, 185, 129, 0.15)',
          border: '1px solid var(--accent-green)',
          color: 'var(--accent-green)',
          fontSize: '0.9rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '1.5rem'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <CheckCircle size={18} />
            <span>{successMsg}</span>
          </div>
          <button onClick={() => setSuccessMsg(null)} style={{ background: 'none', border: 'none', color: 'currentColor', cursor: 'pointer' }}>
            &times;
          </button>
        </div>
      )}

      {error && (
        <div style={{ marginBottom: '1.5rem' }}>
          <ErrorMessage message={error} />
        </div>
      )}

      {/* TAB 1: MOVIES */}
      {activeTab === 'movies' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Movie Catalog Management</h2>
            <button
              onClick={() => setShowMovieModal(true)}
              className="btn-primary"
              style={{ padding: '0.55rem 1.1rem', fontSize: '0.85rem' }}
            >
              <Plus size={16} />
              <span>Add New Movie</span>
            </button>
          </div>

          <div className="glass-panel" style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', background: 'rgba(255, 255, 255, 0.02)' }}>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Movie</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Genre / Language</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Duration</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Status</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600, textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {movies.map((m) => (
                  <tr key={m.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <img
                          src={m.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=100&q=80'}
                          alt={m.title}
                          style={{ width: '40px', height: '56px', borderRadius: '4px', objectFit: 'cover' }}
                        />
                        <div>
                          <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{m.title}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Rating: {m.certification}</div>
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                        <span className="badge badge-red">{m.genre}</span>
                        <span className="badge badge-cyan">{m.language}</span>
                      </div>
                    </td>
                    <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>
                      {m.durationMinutes} mins
                    </td>
                    <td style={{ padding: '1rem' }}>
                      {m.active ? (
                        <span className="badge badge-green">Active</span>
                      ) : (
                        <span className="badge badge-red">Deactivated</span>
                      )}
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'right' }}>
                      {m.active && (
                        <button
                          onClick={() => handleDeactivateMovie(m.id, m.title)}
                          className="btn-secondary"
                          style={{ padding: '0.35rem 0.75rem', fontSize: '0.75rem', borderColor: 'rgba(239, 68, 68, 0.4)', color: 'var(--accent-red)' }}
                          title="Deactivate Movie"
                        >
                          <Trash2 size={13} />
                          <span>Deactivate</span>
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: SHOWS */}
      {activeTab === 'shows' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Showtimes & Screen Inventory</h2>
            <button
              onClick={() => setShowScheduleModal(true)}
              className="btn-primary"
              style={{ padding: '0.55rem 1.1rem', fontSize: '0.85rem' }}
            >
              <Plus size={16} />
              <span>Schedule New Show</span>
            </button>
          </div>

          <div className="glass-panel" style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', background: 'rgba(255, 255, 255, 0.02)' }}>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Show Details</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Theater & Screen</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Price</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Seat Status</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Status</th>
                  <th style={{ padding: '1rem', color: 'var(--text-muted)', fontWeight: 600, textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {shows.map((s) => {
                  const startTime = new Date(s.startTime);
                  return (
                    <tr key={s.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                      <td style={{ padding: '1rem' }}>
                        <div style={{ fontWeight: 700, color: 'var(--text-primary)' }}>{s.movieTitle}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                          {startTime.toLocaleDateString([], { month: 'short', day: 'numeric' })} at {startTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      </td>
                      <td style={{ padding: '1rem' }}>
                        <div style={{ color: 'var(--text-secondary)' }}>{s.cinemaName}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Screen {s.screenNumber} ({s.screenType})</div>
                      </td>
                      <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--accent-gold)' }}>
                        ₹{Number(s.basePrice).toFixed(2)}
                      </td>
                      <td style={{ padding: '1rem' }}>
                        <span className="badge badge-cyan">{s.availableSeatCount} available</span>
                      </td>
                      <td style={{ padding: '1rem' }}>
                        <span className={`badge ${s.status === 'SCHEDULED' ? 'badge-green' : 'badge-red'}`}>
                          {s.status}
                        </span>
                      </td>
                      <td style={{ padding: '1rem', textAlign: 'right' }}>
                        {s.status === 'SCHEDULED' && (
                          <button
                            onClick={() => handleCancelShow(s.id)}
                            className="btn-secondary"
                            style={{ padding: '0.35rem 0.75rem', fontSize: '0.75rem', borderColor: 'rgba(239, 68, 68, 0.4)', color: 'var(--accent-red)' }}
                            title="Cancel Show"
                          >
                            <XCircle size={13} />
                            <span>Cancel</span>
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: THEATERS & SCREENS */}
      {activeTab === 'screens' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {cinemas.map((cinema) => (
            <div key={cinema.id} className="glass-panel" style={{ padding: '2rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
                <MapPin size={22} color="var(--accent-red)" />
                <h2 style={{ fontSize: '1.4rem', fontWeight: 700 }}>{cinema.name}</h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                {cinema.address}, {cinema.city} &bull; Total Screens: {cinema.totalScreens}
              </p>

              <h3 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '1rem' }}>
                Auditoriums & Capacity
              </h3>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
                {screens
                  .filter((sc) => sc.cinemaId === cinema.id)
                  .map((screen) => (
                    <div
                      key={screen.id}
                      style={{
                        padding: '1.25rem',
                        borderRadius: 'var(--radius-md)',
                        background: 'var(--bg-glass)',
                        border: '1px solid var(--border-subtle)'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>Screen {screen.screenNumber}</span>
                        <span className="badge badge-cyan">{screen.screenType}</span>
                      </div>
                      <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                        Physical Capacity: <strong style={{ color: 'var(--accent-gold)' }}>{screen.seatCapacity} seats</strong>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* TAB 4: COUPONS & PROMOTIONS MANAGEMENT */}
      {activeTab === 'coupons' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800, margin: '0 0 0.25rem 0' }}>
                Promotional Offers & Coupons
              </h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', margin: 0 }}>
                Configure percentage and flat discounts, usage caps, per-user limits, and expiration windows.
              </p>
            </div>
            <button onClick={handleOpenCreateCoupon} className="btn-primary" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Plus size={16} />
              <span>Create Coupon</span>
            </button>
          </div>

          {coupons.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3rem 1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <Tag size={40} color="var(--text-muted)" style={{ margin: '0 auto 1rem', opacity: 0.5 }} />
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--text-primary)' }}>
                No Coupons Created Yet
              </h3>
              <p style={{ fontSize: '0.9rem', maxWidth: '420px', margin: '0 auto 1.5rem' }}>
                Create promotional coupons to offer discounts on tickets, drive festive bookings, and reward loyal customers.
              </p>
              <button onClick={handleOpenCreateCoupon} className="btn-primary">
                Create First Coupon
              </button>
            </div>
          ) : (
            <div className="glass-panel" style={{ overflowX: 'auto', borderRadius: 'var(--radius-md)' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-subtle)', background: 'rgba(255, 255, 255, 0.02)' }}>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Code</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Discount</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Min / Max Cap</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Valid Until</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Usage</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600 }}>Status</th>
                    <th style={{ padding: '0.85rem 1rem', color: 'var(--text-muted)', fontWeight: 600, textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {coupons.map((c) => {
                    const isExpired = c.validUntil && new Date(c.validUntil) < new Date();
                    const isLimitReached = c.usageLimit && c.timesUsed >= c.usageLimit;

                    return (
                      <tr key={c.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.04)' }}>
                        <td style={{ padding: '0.85rem 1rem' }}>
                          <div style={{ fontWeight: 800, color: 'var(--accent-gold)', letterSpacing: '0.04em' }}>
                            {c.code}
                          </div>
                          {c.description && (
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                              {c.description}
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '0.85rem 1rem' }}>
                          <span className="badge badge-gold" style={{ fontSize: '0.75rem' }}>
                            {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT`}
                          </span>
                        </td>
                        <td style={{ padding: '0.85rem 1rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                          <div>Min: ₹{Number(c.minBookingAmount || 0).toFixed(2)}</div>
                          {c.maxDiscountAmount && <div>Max Cap: ₹{Number(c.maxDiscountAmount).toFixed(2)}</div>}
                        </td>
                        <td style={{ padding: '0.85rem 1rem', fontSize: '0.82rem', color: isExpired ? 'var(--accent-red)' : 'var(--text-secondary)' }}>
                          {c.validUntil ? new Date(c.validUntil).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' }) : 'Never'}
                          {isExpired && <span style={{ display: 'block', fontSize: '0.7rem', color: 'var(--accent-red)', fontWeight: 700 }}>Expired</span>}
                        </td>
                        <td style={{ padding: '0.85rem 1rem', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                          <div>{c.timesUsed || 0} / {c.usageLimit || '∞'} used</div>
                          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Max {c.perUserLimit || 1}/user</div>
                        </td>
                        <td style={{ padding: '0.85rem 1rem' }}>
                          <span className={`badge ${c.isActive && !isExpired && !isLimitReached ? 'badge-green' : 'badge-gold'}`} style={{ fontSize: '0.72rem' }}>
                            {c.isActive ? (isExpired ? 'Expired' : isLimitReached ? 'Exhausted' : 'Active') : 'Inactive'}
                          </span>
                        </td>
                        <td style={{ padding: '0.85rem 1rem', textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.5rem', alignItems: 'center' }}>
                            <button
                              type="button"
                              onClick={() => handleToggleCoupon(c.id)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.65rem', fontSize: '0.75rem' }}
                              title={c.isActive ? 'Deactivate Coupon' : 'Activate Coupon'}
                            >
                              {c.isActive ? <ToggleRight size={16} color="var(--accent-green)" /> : <ToggleLeft size={16} color="var(--text-muted)" />}
                              <span>{c.isActive ? 'Active' : 'Disabled'}</span>
                            </button>
                            <button
                              type="button"
                              onClick={() => handleOpenEditCoupon(c)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem' }}
                              title="Edit Coupon"
                            >
                              <Edit3 size={14} />
                            </button>
                            <button
                              type="button"
                              onClick={() => handleDeleteCoupon(c.id, c.code)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem', color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.3)' }}
                              title="Deactivate / Delete"
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* MODAL: ADD MOVIE */}
      {showMovieModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="glass-panel" style={{ maxWidth: '580px', width: '100%', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Add New Movie</h2>
              <button onClick={() => setShowMovieModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateMovie} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Movie Title *</label>
                <input
                  type="text"
                  required
                  value={movieForm.title}
                  onChange={(e) => setMovieForm({ ...movieForm, title: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Synopsis / Description *</label>
                <textarea
                  required
                  rows={3}
                  value={movieForm.description}
                  onChange={(e) => setMovieForm({ ...movieForm, description: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)', resize: 'vertical' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Duration (Minutes) *</label>
                  <input
                    type="number"
                    required
                    min={30}
                    max={360}
                    value={movieForm.durationMinutes}
                    onChange={(e) => setMovieForm({ ...movieForm, durationMinutes: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Genre *</label>
                  <input
                    type="text"
                    required
                    value={movieForm.genre}
                    onChange={(e) => setMovieForm({ ...movieForm, genre: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Language *</label>
                  <input
                    type="text"
                    required
                    value={movieForm.language}
                    onChange={(e) => setMovieForm({ ...movieForm, language: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Rating / Certification</label>
                  <input
                    type="text"
                    value={movieForm.certification}
                    onChange={(e) => setMovieForm({ ...movieForm, certification: e.target.value })}
                    placeholder="PG-13, R, etc."
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Poster Image URL</label>
                <input
                  type="url"
                  value={movieForm.posterUrl}
                  onChange={(e) => setMovieForm({ ...movieForm, posterUrl: e.target.value })}
                  placeholder="https://..."
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowMovieModal(false)} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Save Movie
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: SCHEDULE SHOW */}
      {showScheduleModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="glass-panel" style={{ maxWidth: '520px', width: '100%', padding: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Schedule Show</h2>
              <button onClick={() => setShowScheduleModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>
                &times;
              </button>
            </div>

            <form onSubmit={handleCreateShow} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Select Movie *</label>
                <select
                  required
                  value={showForm.movieId}
                  onChange={(e) => setShowForm({ ...showForm, movieId: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                >
                  <option value="">-- Choose Movie --</option>
                  {movies.filter((m) => m.active).map((m) => (
                    <option key={m.id} value={m.id}>{m.title}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Select Screen *</label>
                <select
                  required
                  value={showForm.screenId}
                  onChange={(e) => setShowForm({ ...showForm, screenId: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                >
                  <option value="">-- Choose Screen --</option>
                  {screens.map((sc) => (
                    <option key={sc.id} value={sc.id}>
                      Screen {sc.screenNumber} ({sc.screenType}) - Capacity: {sc.seatCapacity}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Show Start Time *</label>
                <input
                  type="datetime-local"
                  required
                  value={showForm.startTime}
                  onChange={(e) => setShowForm({ ...showForm, startTime: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Base Ticket Price (₹) *</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  min={1}
                  value={showForm.basePrice}
                  onChange={(e) => setShowForm({ ...showForm, basePrice: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowScheduleModal(false)} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Create Show & Seat Layout
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: ADD / EDIT COUPON */}
      {showCouponModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.85)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
          padding: '1rem'
        }}>
          <div className="glass-panel" style={{ maxWidth: '580px', width: '100%', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Tag size={20} color="var(--accent-gold)" />
                <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                  {editingCoupon ? `Edit Coupon: ${editingCoupon.code}` : 'Create Promotional Coupon'}
                </h2>
              </div>
              <button onClick={() => setShowCouponModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>
                &times;
              </button>
            </div>

            <form onSubmit={handleSaveCoupon} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Coupon Code *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. FESTIVE25"
                  value={couponForm.code}
                  onChange={(e) => setCouponForm({ ...couponForm, code: e.target.value.toUpperCase() })}
                  disabled={Boolean(editingCoupon)}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)', textTransform: 'uppercase' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Offer Description *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. 25% off on all weekend IMAX tickets"
                  value={couponForm.description}
                  onChange={(e) => setCouponForm({ ...couponForm, description: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Discount Type *</label>
                  <select
                    value={couponForm.discountType}
                    onChange={(e) => setCouponForm({ ...couponForm, discountType: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  >
                    <option value="PERCENTAGE">Percentage (%)</option>
                    <option value="FIXED_AMOUNT">Fixed Amount (₹)</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>
                    Discount Value ({couponForm.discountType === 'PERCENTAGE' ? '%' : '₹'}) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    required
                    value={couponForm.discountValue}
                    onChange={(e) => setCouponForm({ ...couponForm, discountValue: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Min Booking Amount (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={couponForm.minBookingAmount}
                    onChange={(e) => setCouponForm({ ...couponForm, minBookingAmount: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Max Discount Cap (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="Optional for percentage"
                    value={couponForm.maxDiscountAmount}
                    onChange={(e) => setCouponForm({ ...couponForm, maxDiscountAmount: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Global Usage Limit</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={couponForm.usageLimit}
                    onChange={(e) => setCouponForm({ ...couponForm, usageLimit: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Per-User Limit</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={couponForm.perUserLimit}
                    onChange={(e) => setCouponForm({ ...couponForm, perUserLimit: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Expiration Date & Time *</label>
                <input
                  type="datetime-local"
                  required
                  value={couponForm.validUntil}
                  onChange={(e) => setCouponForm({ ...couponForm, validUntil: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'rgba(15, 20, 34, 0.9)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div>
                <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.88rem' }}>
                  <input
                    type="checkbox"
                    checked={couponForm.isActive}
                    onChange={(e) => setCouponForm({ ...couponForm, isActive: e.target.checked })}
                    style={{ width: '16px', height: '16px', accentColor: 'var(--accent-red)' }}
                  />
                  <span>Coupon is Active and Available for Checkout</span>
                </label>
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowCouponModal(false)} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingCoupon ? 'Update Coupon' : 'Create Coupon'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
