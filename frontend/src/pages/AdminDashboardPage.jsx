import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '../services/adminService';
import { movieService } from '../services/movieService';
import { showService } from '../services/showService';
import { couponService } from '../services/couponService';
import { refundService } from '../services/refundService';
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
  ToggleRight,
  RotateCcw,
  CheckCircle2,
  AlertCircle,
  Percent,
  Sliders,
  RefreshCw,
  Ban
} from 'lucide-react';

export function AdminDashboardPage() {
  const [activeTab, setActiveTab] = useState('movies'); // 'movies' | 'shows' | 'screens' | 'coupons' | 'refunds' | 'policies'
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  // Data states
  const [movies, setMovies] = useState([]);
  const [shows, setShows] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [screens, setScreens] = useState([]);
  const [coupons, setCoupons] = useState([]);

  // Refund Management states
  const [refunds, setRefunds] = useState([]);
  const [refundStats, setRefundStats] = useState(null);
  const [refundsLoading, setRefundsLoading] = useState(false);
  const [refundFilter, setRefundFilter] = useState('ALL');
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [selectedRefundForReject, setSelectedRefundForReject] = useState(null);
  const [rejectionReasonInput, setRejectionReasonInput] = useState('');
  const [processingRefundId, setProcessingRefundId] = useState(null);

  // Cancellation Policies states
  const [policies, setPolicies] = useState([]);
  const [policiesLoading, setPoliciesLoading] = useState(false);
  const [showPolicyModal, setShowPolicyModal] = useState(false);
  const [editingPolicy, setEditingPolicy] = useState(null);
  const [policyForm, setPolicyForm] = useState({
    policyName: '',
    description: '',
    minHoursBeforeShow: 0,
    maxHoursBeforeShow: '',
    refundPercentage: 100,
    cancellationFeePercent: 0,
    displayOrder: 1,
    isActive: true
  });

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
      const [moviesData, showsData, cinemasData, couponsRes, statsRes] = await Promise.allSettled([
        movieService.getMovies(),
        showService.getShows(),
        adminService.getCinemas(),
        couponService.getAllCoupons(),
        refundService.getRefundStats()
      ]);

      if (moviesData.status === 'fulfilled') setMovies(moviesData.value || []);
      if (showsData.status === 'fulfilled') setShows(showsData.value || []);
      if (cinemasData.status === 'fulfilled') {
        const cList = cinemasData.value || [];
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
      if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
        setRefundStats(statsRes.value.data);
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

  // Refund Management Handlers
  const loadRefunds = async (status = refundFilter) => {
    try {
      setRefundsLoading(true);
      const [listRes, statsRes] = await Promise.allSettled([
        refundService.getAllRefunds(status),
        refundService.getRefundStats()
      ]);
      if (listRes.status === 'fulfilled' && listRes.value?.data) {
        setRefunds(listRes.value.data);
      }
      if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
        setRefundStats(statsRes.value.data);
      }
    } catch (err) {
      console.error('Failed to load refund data:', err);
    } finally {
      setRefundsLoading(false);
    }
  };

  const loadPolicies = async () => {
    try {
      setPoliciesLoading(true);
      const res = await refundService.getAllPolicies();
      if (res?.data) {
        setPolicies(res.data);
      }
    } catch (err) {
      console.error('Failed to load policies:', err);
    } finally {
      setPoliciesLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'refunds') {
      loadRefunds(refundFilter);
    } else if (activeTab === 'policies') {
      loadPolicies();
    }
  }, [activeTab, refundFilter]);

  const handleApproveRefund = async (refundId) => {
    try {
      setError(null);
      await refundService.approveRefund(refundId, { note: 'Approved by admin' });
      setSuccessMsg(`Refund request #${refundId} approved.`);
      await loadRefunds(refundFilter);
    } catch (err) {
      setError(err.message || 'Failed to approve refund.');
    }
  };

  const handleProcessRefund = async (refundId) => {
    try {
      setProcessingRefundId(refundId);
      setError(null);
      const res = await refundService.processRefund(refundId, {
        note: 'Processed via simulated payment gateway'
      });
      setSuccessMsg(`Refund #${refundId} processed successfully! Gateway Ref: ${res?.data?.gatewayRefundId || 'OK'}`);
      await loadRefunds(refundFilter);
    } catch (err) {
      setError(err.message || 'Failed to process refund.');
    } finally {
      setProcessingRefundId(null);
    }
  };

  const handleOpenRejectModal = (refund) => {
    setSelectedRefundForReject(refund);
    setRejectionReasonInput('');
    setRejectModalOpen(true);
  };

  const handleConfirmRejectRefund = async (e) => {
    e.preventDefault();
    if (!selectedRefundForReject) return;
    if (!rejectionReasonInput.trim()) {
      alert('Please specify a rejection reason.');
      return;
    }
    try {
      setError(null);
      await refundService.rejectRefund(selectedRefundForReject.id, {
        rejectionReason: rejectionReasonInput.trim(),
        note: 'Rejected via Admin Console'
      });
      setSuccessMsg(`Refund #${selectedRefundForReject.id} has been rejected.`);
      setRejectModalOpen(false);
      setSelectedRefundForReject(null);
      await loadRefunds(refundFilter);
    } catch (err) {
      setError(err.message || 'Failed to reject refund.');
    }
  };

  // Cancellation Policy Handlers
  const handleOpenCreatePolicy = () => {
    setEditingPolicy(null);
    setPolicyForm({
      policyName: '',
      description: '',
      minHoursBeforeShow: 0,
      maxHoursBeforeShow: '',
      refundPercentage: 100,
      cancellationFeePercent: 0,
      displayOrder: policies.length + 1,
      isActive: true
    });
    setShowPolicyModal(true);
  };

  const handleOpenEditPolicy = (policy) => {
    setEditingPolicy(policy);
    setPolicyForm({
      policyName: policy.policyName || '',
      description: policy.description || '',
      minHoursBeforeShow: policy.minHoursBeforeShow ?? 0,
      maxHoursBeforeShow: policy.maxHoursBeforeShow !== null && policy.maxHoursBeforeShow !== undefined ? policy.maxHoursBeforeShow : '',
      refundPercentage: policy.refundPercentage ?? 100,
      cancellationFeePercent: policy.cancellationFeePercent ?? 0,
      displayOrder: policy.displayOrder ?? 1,
      isActive: Boolean(policy.isActive)
    });
    setShowPolicyModal(true);
  };

  const handleSavePolicy = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      const payload = {
        policyName: policyForm.policyName.trim(),
        description: policyForm.description,
        minHoursBeforeShow: parseInt(policyForm.minHoursBeforeShow, 10),
        maxHoursBeforeShow: policyForm.maxHoursBeforeShow === '' || policyForm.maxHoursBeforeShow === null ? null : parseInt(policyForm.maxHoursBeforeShow, 10),
        refundPercentage: parseFloat(policyForm.refundPercentage),
        cancellationFeePercent: parseFloat(policyForm.cancellationFeePercent || 0),
        displayOrder: parseInt(policyForm.displayOrder || 1, 10),
        isActive: Boolean(policyForm.isActive)
      };

      if (editingPolicy) {
        await refundService.updatePolicy(editingPolicy.id, payload);
        setSuccessMsg(`Policy "${payload.policyName}" updated successfully.`);
      } else {
        await refundService.createPolicy(payload);
        setSuccessMsg(`Policy "${payload.policyName}" created successfully.`);
      }

      setShowPolicyModal(false);
      await loadPolicies();
    } catch (err) {
      setError(err.message || 'Failed to save cancellation policy.');
    }
  };

  const handleTogglePolicy = async (id) => {
    try {
      setError(null);
      await refundService.togglePolicy(id);
      setSuccessMsg('Policy status toggled.');
      await loadPolicies();
    } catch (err) {
      setError(err.message || 'Failed to toggle policy status.');
    }
  };

  const handleDeletePolicy = async (id, name) => {
    if (!window.confirm(`Are you sure you want to deactivate policy "${name}"?`)) return;
    try {
      setError(null);
      await refundService.deletePolicy(id);
      setSuccessMsg(`Policy "${name}" deactivated.`);
      await loadPolicies();
    } catch (err) {
      setError(err.message || 'Failed to deactivate policy.');
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
            Manage movies catalog, screen scheduling, promotional coupons, refunds and cancellation rules.
          </p>
        </div>

        {/* Tab Controls */}
        <div style={{ display: 'flex', gap: '0.5rem', background: 'var(--bg-glass)', padding: '0.35rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', flexWrap: 'wrap' }}>
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
          <button
            onClick={() => setActiveTab('refunds')}
            className={activeTab === 'refunds' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <RotateCcw size={15} />
            <span>Refunds {refundStats?.pendingRequests > 0 ? `(${refundStats.pendingRequests})` : ''}</span>
          </button>
          <button
            onClick={() => setActiveTab('policies')}
            className={activeTab === 'policies' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}
          >
            <ShieldCheck size={15} />
            <span>Policies</span>
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
            <span>Gate Scanner</span>
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
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '1rem' }}>Poster</th>
                  <th style={{ padding: '1rem' }}>Title & Genre</th>
                  <th style={{ padding: '1rem' }}>Duration</th>
                  <th style={{ padding: '1rem' }}>Language</th>
                  <th style={{ padding: '1rem' }}>Rating</th>
                  <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {movies.map((m) => (
                  <tr key={m.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '0.75rem 1rem', width: '60px' }}>
                      <img
                        src={m.posterUrl}
                        alt={m.title}
                        style={{ width: '45px', height: '60px', objectFit: 'cover', borderRadius: 'var(--radius-sm)' }}
                      />
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ fontWeight: 700 }}>{m.title}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{m.genre}</div>
                    </td>
                    <td style={{ padding: '1rem' }}>{m.durationMinutes} mins</td>
                    <td style={{ padding: '1rem' }}>{m.language}</td>
                    <td style={{ padding: '1rem' }}>
                      <span className="badge badge-gold" style={{ fontSize: '0.75rem' }}>{m.certification || 'UA'}</span>
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'right' }}>
                      <button
                        onClick={() => handleDeactivateMovie(m.id, m.title)}
                        className="btn-secondary"
                        style={{ padding: '0.35rem 0.65rem', color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.3)' }}
                        title="Deactivate Movie"
                      >
                        <Trash2 size={14} />
                      </button>
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
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Show Schedules & Seat Inventory</h2>
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
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '1rem' }}>Show ID</th>
                  <th style={{ padding: '1rem' }}>Movie Title</th>
                  <th style={{ padding: '1rem' }}>Cinema & Screen</th>
                  <th style={{ padding: '1rem' }}>Start Time</th>
                  <th style={{ padding: '1rem' }}>Base Price</th>
                  <th style={{ padding: '1rem' }}>Seats Left</th>
                  <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {shows.map((s) => (
                  <tr key={s.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                    <td style={{ padding: '1rem', fontWeight: 700 }}>#{s.id}</td>
                    <td style={{ padding: '1rem', fontWeight: 600 }}>{s.movieTitle}</td>
                    <td style={{ padding: '1rem' }}>
                      <div>{s.cinemaName}</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{s.screenName}</div>
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                        <Clock size={14} color="var(--accent-red)" />
                        <span>{new Date(s.startTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</span>
                      </div>
                    </td>
                    <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--accent-gold)' }}>
                      ₹{s.basePrice ? Number(s.basePrice).toFixed(2) : '200.00'}
                    </td>
                    <td style={{ padding: '1rem' }}>
                      <span className="badge badge-green" style={{ fontSize: '0.75rem' }}>
                        {s.availableSeats ?? 48} seats available
                      </span>
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'right' }}>
                      <button
                        onClick={() => handleCancelShow(s.id)}
                        className="btn-secondary"
                        style={{ padding: '0.35rem 0.65rem', color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.3)' }}
                        title="Cancel Show"
                      >
                        <XCircle size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: CINEMAS & SCREENS */}
      {activeTab === 'screens' && (
        <div>
          <div style={{ marginBottom: '1.5rem' }}>
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Cinema Complexes & Screens</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              Overview of configured auditoriums, seating layouts, and projection capabilities.
            </p>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.5rem' }}>
            {cinemas.map((c) => {
              const cinemaScreens = screens.filter((sc) => sc.cinemaId === c.id);
              return (
                <div key={c.id} className="glass-panel" style={{ padding: '1.5rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                    <MapPin size={18} color="var(--accent-red)" />
                    <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>{c.name}</h3>
                  </div>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '1rem' }}>
                    {c.address}, {c.city} • Ph: {c.phone}
                  </p>

                  <h4 style={{ fontSize: '0.85rem', textTransform: 'uppercase', color: 'var(--text-muted)', marginBottom: '0.75rem', letterSpacing: '0.05em' }}>
                    Screens & Capacities
                  </h4>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {cinemaScreens.map((sc) => (
                      <div
                        key={sc.id}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '0.65rem 0.85rem',
                          borderRadius: 'var(--radius-sm)',
                          background: 'rgba(255, 255, 255, 0.03)',
                          border: '1px solid var(--border-subtle)'
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{sc.name}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--accent-cyan)' }}>{sc.type || 'IMAX 3D'}</div>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <span className="badge badge-gold" style={{ fontSize: '0.75rem' }}>
                            {sc.totalSeats || 48} Seats
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* TAB 4: COUPONS */}
      {activeTab === 'coupons' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Promotional Offers & Coupons</h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem' }}>
                Create discount codes, set percentage or fixed discounts, configure spend limits and validity.
              </p>
            </div>
            <button
              onClick={handleOpenCreateCoupon}
              className="btn-primary"
              style={{ padding: '0.55rem 1.1rem', fontSize: '0.85rem' }}
            >
              <Plus size={16} />
              <span>Create Coupon</span>
            </button>
          </div>

          {coupons.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <Tag size={40} style={{ opacity: 0.3, marginBottom: '0.75rem' }} />
              <p>No promotional coupons created yet. Click "Create Coupon" to start an offer campaign.</p>
            </div>
          ) : (
            <div className="glass-panel" style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                    <th style={{ padding: '1rem' }}>Code</th>
                    <th style={{ padding: '1rem' }}>Description</th>
                    <th style={{ padding: '1rem' }}>Discount</th>
                    <th style={{ padding: '1rem' }}>Min Amount / Cap</th>
                    <th style={{ padding: '1rem' }}>Usage / Limit</th>
                    <th style={{ padding: '1rem' }}>Valid Until</th>
                    <th style={{ padding: '1rem' }}>Status</th>
                    <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {coupons.map((c) => {
                    const isExpired = c.validUntil && new Date(c.validUntil) < new Date();
                    return (
                      <tr key={c.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            display: 'inline-block',
                            padding: '0.25rem 0.6rem',
                            borderRadius: 'var(--radius-sm)',
                            background: 'rgba(229, 9, 20, 0.15)',
                            color: 'var(--accent-red)',
                            fontWeight: 700,
                            letterSpacing: '0.05em',
                            border: '1px dashed var(--accent-red)'
                          }}>
                            {c.code}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', maxWidth: '220px' }}>
                          <div style={{ fontWeight: 600 }}>{c.description}</div>
                        </td>
                        <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--accent-gold)' }}>
                          {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% OFF` : `₹${c.discountValue} FLAT`}
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.85rem' }}>
                          <div>Min: ₹{c.minBookingAmount || 0}</div>
                          {c.maxDiscountAmount ? (
                            <div style={{ color: 'var(--text-muted)' }}>Cap: ₹{c.maxDiscountAmount}</div>
                          ) : (
                            <div style={{ color: 'var(--text-muted)' }}>No Cap</div>
                          )}
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.85rem' }}>
                          <div>Used: {c.timesUsed || 0} / {c.usageLimit}</div>
                          <div style={{ color: 'var(--text-muted)' }}>Per user: {c.perUserLimit}</div>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.85rem' }}>
                          <div style={{ color: isExpired ? 'var(--accent-red)' : 'var(--text-secondary)' }}>
                            {c.validUntil ? new Date(c.validUntil).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : 'Never'}
                          </div>
                          {isExpired && <span style={{ fontSize: '0.75rem', color: 'var(--accent-red)' }}>Expired</span>}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.2rem 0.6rem',
                            borderRadius: 'var(--radius-full)',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            background: c.isActive && !isExpired ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                            color: c.isActive && !isExpired ? 'var(--accent-green)' : 'var(--accent-red)',
                            border: `1px solid ${c.isActive && !isExpired ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`
                          }}>
                            {c.isActive && !isExpired ? 'ACTIVE' : 'INACTIVE'}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleOpenEditCoupon(c)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem' }}
                              title="Edit Coupon"
                            >
                              <Edit3 size={14} />
                            </button>
                            <button
                              onClick={() => handleToggleCoupon(c.id)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem', color: c.isActive ? 'var(--accent-gold)' : 'var(--accent-green)' }}
                              title={c.isActive ? 'Deactivate Coupon' : 'Activate Coupon'}
                            >
                              {c.isActive ? <ToggleRight size={16} /> : <ToggleLeft size={16} />}
                            </button>
                            <button
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

      {/* TAB 5: REFUND MANAGEMENT */}
      {activeTab === 'refunds' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Refund Request Management</h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem' }}>
                Review customer cancellation requests, inspect fee deductions, and process simulated payment refunds.
              </p>
            </div>
            <button
              onClick={() => loadRefunds(refundFilter)}
              className="btn-secondary"
              style={{ padding: '0.55rem 1rem', fontSize: '0.85rem' }}
            >
              <RefreshCw size={15} />
              <span>Refresh</span>
            </button>
          </div>

          {/* Metric Stats Cards */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
            gap: '1rem',
            marginBottom: '1.5rem'
          }}>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Total Requests</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800 }}>{refundStats?.totalRequests || 0}</div>
            </div>
            <div className="glass-panel" style={{ padding: '1rem', borderColor: 'rgba(234, 179, 8, 0.4)' }}>
              <div style={{ color: 'var(--accent-gold)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Pending Review</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-gold)' }}>{refundStats?.pendingRequests || 0}</div>
            </div>
            <div className="glass-panel" style={{ padding: '1rem', borderColor: 'rgba(6, 182, 212, 0.4)' }}>
              <div style={{ color: 'var(--accent-cyan)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Approved</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-cyan)' }}>{refundStats?.approvedRequests || 0}</div>
            </div>
            <div className="glass-panel" style={{ padding: '1rem', borderColor: 'rgba(16, 185, 129, 0.4)' }}>
              <div style={{ color: 'var(--accent-green)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Completed</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-green)' }}>{refundStats?.completedRequests || 0}</div>
            </div>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Total Disbursed</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-green)' }}>₹{(refundStats?.totalRefundedAmount || 0).toFixed(2)}</div>
            </div>
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginBottom: '0.25rem' }}>Fees Retained</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--accent-gold)' }}>₹{(refundStats?.totalRetainedFees || 0).toFixed(2)}</div>
            </div>
          </div>

          {/* Filter Pills */}
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
            {['ALL', 'REQUESTED', 'APPROVED', 'PROCESSING', 'COMPLETED', 'REJECTED'].map((filterKey) => (
              <button
                key={filterKey}
                onClick={() => setRefundFilter(filterKey)}
                className={refundFilter === filterKey ? 'btn-primary' : 'btn-secondary'}
                style={{ padding: '0.4rem 0.85rem', fontSize: '0.8rem' }}
              >
                {filterKey}
              </button>
            ))}
          </div>

          {/* Refunds Table */}
          {refundsLoading ? (
            <LoadingSpinner message="Loading refund requests..." />
          ) : refunds.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <RotateCcw size={40} style={{ opacity: 0.3, marginBottom: '0.75rem' }} />
              <p>No refund requests found matching filter: {refundFilter}</p>
            </div>
          ) : (
            <div className="glass-panel" style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                    <th style={{ padding: '1rem' }}>ID & Requested</th>
                    <th style={{ padding: '1rem' }}>Booking Code</th>
                    <th style={{ padding: '1rem' }}>User / Email</th>
                    <th style={{ padding: '1rem' }}>Paid Amount</th>
                    <th style={{ padding: '1rem' }}>Fee Retained</th>
                    <th style={{ padding: '1rem' }}>Refund Due</th>
                    <th style={{ padding: '1rem' }}>Status</th>
                    <th style={{ padding: '1rem' }}>Gateway Ref</th>
                    <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {refunds.map((r) => {
                    const statusColors = {
                      REQUESTED: { bg: 'rgba(234, 179, 8, 0.15)', text: 'var(--accent-gold)', border: 'rgba(234, 179, 8, 0.4)' },
                      APPROVED: { bg: 'rgba(6, 182, 212, 0.15)', text: 'var(--accent-cyan)', border: 'rgba(6, 182, 212, 0.4)' },
                      PROCESSING: { bg: 'rgba(59, 130, 246, 0.15)', text: '#60a5fa', border: 'rgba(59, 130, 246, 0.4)' },
                      COMPLETED: { bg: 'rgba(16, 185, 129, 0.15)', text: 'var(--accent-green)', border: 'rgba(16, 185, 129, 0.4)' },
                      REJECTED: { bg: 'rgba(239, 68, 68, 0.15)', text: 'var(--accent-red)', border: 'rgba(239, 68, 68, 0.4)' }
                    };
                    const badge = statusColors[r.status] || { bg: 'rgba(255,255,255,0.1)', text: '#fff', border: 'var(--border-subtle)' };

                    return (
                      <tr key={r.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                        <td style={{ padding: '1rem' }}>
                          <div style={{ fontWeight: 700 }}>#{r.id}</div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            {r.requestedAt ? new Date(r.requestedAt).toLocaleString() : 'N/A'}
                          </div>
                        </td>
                        <td style={{ padding: '1rem', fontWeight: 600, color: 'var(--accent-gold)' }}>
                          {r.bookingReference}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <div>{r.userName || 'Customer'}</div>
                          <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{r.userEmail}</div>
                        </td>
                        <td style={{ padding: '1rem' }}>₹{r.originalBookingAmount?.toFixed(2)}</td>
                        <td style={{ padding: '1rem', color: 'var(--text-muted)' }}>₹{r.cancellationFee?.toFixed(2)}</td>
                        <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--accent-green)' }}>
                          ₹{r.refundAmount?.toFixed(2)}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.25rem 0.65rem',
                            borderRadius: 'var(--radius-full)',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            background: badge.bg,
                            color: badge.text,
                            border: `1px solid ${badge.border}`
                          }}>
                            {r.status}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                          {r.gatewayRefundId || (r.status === 'REJECTED' ? (
                            <span style={{ color: 'var(--accent-red)' }} title={r.rejectionReason}>
                              {r.rejectionReason || 'Rejected'}
                            </span>
                          ) : 'Pending')}
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                            {r.status === 'REQUESTED' && (
                              <>
                                <button
                                  onClick={() => handleApproveRefund(r.id)}
                                  className="btn-primary"
                                  style={{ padding: '0.35rem 0.65rem', fontSize: '0.78rem' }}
                                  title="Approve refund"
                                >
                                  <CheckCircle size={13} />
                                  <span>Approve</span>
                                </button>
                                <button
                                  onClick={() => handleOpenRejectModal(r)}
                                  className="btn-secondary"
                                  style={{ padding: '0.35rem 0.65rem', fontSize: '0.78rem', color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.4)' }}
                                  title="Reject request"
                                >
                                  <Ban size={13} />
                                  <span>Reject</span>
                                </button>
                              </>
                            )}

                            {r.status === 'APPROVED' && (
                              <>
                                <button
                                  onClick={() => handleProcessRefund(r.id)}
                                  disabled={processingRefundId === r.id}
                                  className="btn-primary"
                                  style={{
                                    padding: '0.35rem 0.65rem',
                                    fontSize: '0.78rem',
                                    background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
                                  }}
                                  title="Execute payment refund through gateway"
                                >
                                  <RotateCcw size={13} className={processingRefundId === r.id ? 'spin-animation' : ''} />
                                  <span>{processingRefundId === r.id ? 'Refunding...' : 'Process Refund'}</span>
                                </button>
                                <button
                                  onClick={() => handleOpenRejectModal(r)}
                                  className="btn-secondary"
                                  style={{ padding: '0.35rem 0.65rem', fontSize: '0.78rem', color: 'var(--accent-red)', borderColor: 'rgba(239, 68, 68, 0.4)' }}
                                >
                                  <Ban size={13} />
                                </button>
                              </>
                            )}

                            {r.status === 'COMPLETED' && (
                              <span style={{ fontSize: '0.8rem', color: 'var(--accent-green)', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                                <CheckCircle2 size={15} /> Settled
                              </span>
                            )}

                            {r.status === 'REJECTED' && (
                              <span style={{ fontSize: '0.8rem', color: 'var(--accent-red)', display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                                <XCircle size={15} /> Closed
                              </span>
                            )}
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

      {/* TAB 6: CANCELLATION POLICIES */}
      {activeTab === 'policies' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Cancellation & Refund Policies</h2>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem' }}>
                Configure dynamic cancellation rules, refund windows, and deduction percentages without code changes.
              </p>
            </div>
            <button
              onClick={handleOpenCreatePolicy}
              className="btn-primary"
              style={{ padding: '0.55rem 1.1rem', fontSize: '0.85rem' }}
            >
              <Plus size={16} />
              <span>Add New Policy</span>
            </button>
          </div>

          <div style={{
            background: 'rgba(6, 182, 212, 0.08)',
            border: '1px solid rgba(6, 182, 212, 0.3)',
            borderRadius: 'var(--radius-md)',
            padding: '1rem 1.25rem',
            marginBottom: '1.5rem',
            fontSize: '0.88rem',
            color: 'var(--text-secondary)'
          }}>
            <strong style={{ color: 'var(--accent-cyan)' }}>Rule Resolution Order:</strong> When a user cancels a booking, CineSmart evaluates active policies matching the time remaining until showtime (ordered by <code>min_hours_before_show DESC</code>). For instance:
            <ul style={{ margin: '0.5rem 0 0 1.25rem' }}>
              <li><strong>24+ Hours Before Show:</strong> 100% Refund (0% cancellation fee)</li>
              <li><strong>6 to 24 Hours Before Show:</strong> 75% Refund (25% cancellation fee)</li>
              <li><strong>0 to 6 Hours Before Show:</strong> 0% Refund (100% cancellation fee)</li>
            </ul>
          </div>

          {policiesLoading ? (
            <LoadingSpinner message="Loading cancellation policies..." />
          ) : policies.length === 0 ? (
            <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <ShieldCheck size={40} style={{ opacity: 0.3, marginBottom: '0.75rem' }} />
              <p>No cancellation policies configured. Click "Add New Policy" to create one.</p>
            </div>
          ) : (
            <div className="glass-panel" style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                    <th style={{ padding: '1rem' }}>Order</th>
                    <th style={{ padding: '1rem' }}>Policy Name</th>
                    <th style={{ padding: '1rem' }}>Time Window Before Show</th>
                    <th style={{ padding: '1rem' }}>Refund %</th>
                    <th style={{ padding: '1rem' }}>Cancellation Fee %</th>
                    <th style={{ padding: '1rem' }}>Status</th>
                    <th style={{ padding: '1rem', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {policies.map((p) => {
                    const windowLabel = p.maxHoursBeforeShow != null
                      ? `${p.minHoursBeforeShow}h - ${p.maxHoursBeforeShow}h before show`
                      : `More than ${p.minHoursBeforeShow}h before show`;

                    return (
                      <tr key={p.id} style={{ borderBottom: '1px solid var(--border-subtle)' }}>
                        <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--text-muted)' }}>
                          #{p.displayOrder}
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <div style={{ fontWeight: 700 }}>{p.policyName}</div>
                          {p.description && (
                            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>{p.description}</div>
                          )}
                        </td>
                        <td style={{ padding: '1rem', color: 'var(--accent-cyan)' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                            <Clock size={14} />
                            <span>{windowLabel}</span>
                          </div>
                        </td>
                        <td style={{ padding: '1rem', fontWeight: 700, color: 'var(--accent-green)' }}>
                          {p.refundPercentage}%
                        </td>
                        <td style={{ padding: '1rem', color: 'var(--accent-gold)' }}>
                          {p.cancellationFeePercent}%
                        </td>
                        <td style={{ padding: '1rem' }}>
                          <span style={{
                            padding: '0.2rem 0.6rem',
                            borderRadius: 'var(--radius-full)',
                            fontSize: '0.75rem',
                            fontWeight: 700,
                            background: p.isActive ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)',
                            color: p.isActive ? 'var(--accent-green)' : 'var(--accent-red)',
                            border: `1px solid ${p.isActive ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`
                          }}>
                            {p.isActive ? 'ACTIVE' : 'INACTIVE'}
                          </span>
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          <div style={{ display: 'inline-flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleOpenEditPolicy(p)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem' }}
                              title="Edit policy"
                            >
                              <Edit3 size={14} />
                            </button>
                            <button
                              onClick={() => handleTogglePolicy(p.id)}
                              className="btn-secondary"
                              style={{ padding: '0.35rem 0.55rem', color: p.isActive ? 'var(--accent-gold)' : 'var(--accent-green)' }}
                              title={p.isActive ? 'Deactivate policy' : 'Activate policy'}
                            >
                              {p.isActive ? <ToggleRight size={16} /> : <ToggleLeft size={16} />}
                            </button>
                            <button
                              onClick={() => handleDeletePolicy(p.id, p.policyName)}
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
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Genre *</label>
                  <input
                    type="text"
                    required
                    value={movieForm.genre}
                    onChange={(e) => setMovieForm({ ...movieForm, genre: e.target.value })}
                    placeholder="Action, Sci-Fi..."
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Duration (mins) *</label>
                  <input
                    type="number"
                    required
                    min="30"
                    value={movieForm.durationMinutes}
                    onChange={(e) => setMovieForm({ ...movieForm, durationMinutes: e.target.value })}
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
          <div className="glass-panel" style={{ maxWidth: '540px', width: '100%', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>Schedule Show & Inventory</h2>
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
                  {movies.map((m) => (
                    <option key={m.id} value={m.id}>{m.title} ({m.language})</option>
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
                  <option value="">-- Choose Auditorium --</option>
                  {screens.map((sc) => (
                    <option key={sc.id} value={sc.id}>{sc.cinemaName || 'Cinema'} - {sc.name} ({sc.type || 'IMAX 3D'})</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
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
                    min="50"
                    required
                    value={showForm.basePrice}
                    onChange={(e) => setShowForm({ ...showForm, basePrice: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ background: 'rgba(234, 179, 8, 0.1)', border: '1px solid rgba(234, 179, 8, 0.3)', padding: '0.85rem', borderRadius: 'var(--radius-sm)', fontSize: '0.8rem', color: 'var(--accent-gold)' }}>
                Scheduling automatically generates show-seat inventory records across all screen tiers (Standard, Premium, Recliner) with real-time 5-minute locking capability.
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowScheduleModal(false)} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  Publish Show
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: CREATE / EDIT COUPON */}
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
          <div className="glass-panel" style={{ maxWidth: '540px', width: '100%', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingCoupon ? `Edit Coupon: ${editingCoupon.code}` : 'Create New Promotional Offer'}
              </h2>
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

      {/* MODAL: REJECT REFUND REQUEST */}
      {rejectModalOpen && selectedRefundForReject && (
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
          <div className="glass-panel" style={{ maxWidth: '480px', width: '100%', padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1rem', color: 'var(--accent-red)' }}>
              <Ban size={24} />
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fff' }}>
                Reject Refund Request #{selectedRefundForReject.id}
              </h2>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: '1.25rem' }}>
              Specify the rationale for rejecting refund request for booking{' '}
              <strong style={{ color: 'var(--accent-gold)' }}>{selectedRefundForReject.bookingReference}</strong> (₹{selectedRefundForReject.refundAmount?.toFixed(2)}).
              This justification will be permanently visible to the customer.
            </p>

            <form onSubmit={handleConfirmRejectRefund}>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
                  Rejection Reason *
                </label>
                <textarea
                  rows={3}
                  required
                  placeholder="e.g. Showtime has already commenced / Violation of cinema terms"
                  value={rejectionReasonInput}
                  onChange={(e) => setRejectionReasonInput(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    borderRadius: 'var(--radius-sm)',
                    background: 'var(--bg-glass)',
                    border: '1px solid var(--border-subtle)',
                    color: 'var(--text-primary)',
                    resize: 'vertical'
                  }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button
                  type="button"
                  onClick={() => {
                    setRejectModalOpen(false);
                    setSelectedRefundForReject(null);
                  }}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                  style={{ background: 'linear-gradient(135deg, #ef4444 0%, #b91c1c 100%)' }}
                >
                  Confirm Rejection
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: CREATE / EDIT CANCELLATION POLICY */}
      {showPolicyModal && (
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
          <div className="glass-panel" style={{ maxWidth: '540px', width: '100%', padding: '2rem', maxHeight: '90vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.35rem', fontWeight: 700 }}>
                {editingPolicy ? `Edit Policy: ${editingPolicy.policyName}` : 'Add New Cancellation Policy'}
              </h2>
              <button onClick={() => setShowPolicyModal(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', fontSize: '1.5rem', cursor: 'pointer' }}>
                &times;
              </button>
            </div>

            <form onSubmit={handleSavePolicy} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Policy Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Standard Advance Cancellation"
                  value={policyForm.policyName}
                  onChange={(e) => setPolicyForm({ ...policyForm, policyName: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Customer Description</label>
                <input
                  type="text"
                  placeholder="e.g. 100% refund for cancellations at least 24 hours prior"
                  value={policyForm.description}
                  onChange={(e) => setPolicyForm({ ...policyForm, description: e.target.value })}
                  style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Min Hours Before Show *</label>
                  <input
                    type="number"
                    min="0"
                    required
                    value={policyForm.minHoursBeforeShow}
                    onChange={(e) => setPolicyForm({ ...policyForm, minHoursBeforeShow: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Max Hours Before Show</label>
                  <input
                    type="number"
                    min="0"
                    placeholder="Leave blank for unlimited"
                    value={policyForm.maxHoursBeforeShow}
                    onChange={(e) => setPolicyForm({ ...policyForm, maxHoursBeforeShow: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Refund Percentage (%) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    max="100"
                    required
                    value={policyForm.refundPercentage}
                    onChange={(e) => {
                      const refVal = parseFloat(e.target.value) || 0;
                      setPolicyForm({
                        ...policyForm,
                        refundPercentage: e.target.value,
                        cancellationFeePercent: (100 - refVal).toFixed(2)
                      });
                    }}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Cancellation Fee (%) *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    max="100"
                    required
                    value={policyForm.cancellationFeePercent}
                    onChange={(e) => setPolicyForm({ ...policyForm, cancellationFeePercent: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.3rem' }}>Display Order</label>
                  <input
                    type="number"
                    min="1"
                    value={policyForm.displayOrder}
                    onChange={(e) => setPolicyForm({ ...policyForm, displayOrder: e.target.value })}
                    style={{ width: '100%', padding: '0.65rem', borderRadius: 'var(--radius-sm)', background: 'var(--bg-glass)', border: '1px solid var(--border-subtle)', color: 'var(--text-primary)' }}
                  />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', paddingTop: '1.25rem' }}>
                  <label style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.88rem' }}>
                    <input
                      type="checkbox"
                      checked={policyForm.isActive}
                      onChange={(e) => setPolicyForm({ ...policyForm, isActive: e.target.checked })}
                      style={{ width: '16px', height: '16px', accentColor: 'var(--accent-red)' }}
                    />
                    <span>Policy is Active</span>
                  </label>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button type="button" onClick={() => setShowPolicyModal(false)} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingPolicy ? 'Update Policy' : 'Create Policy'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
