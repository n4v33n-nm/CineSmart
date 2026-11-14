import { api } from './api';

export const analyticsService = {
  /**
   * Fetch complete analytics overview with KPI, time series, breakdowns, and filter options
   */
  async getOverview(filters = {}) {
    const params = new URLSearchParams();
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.movieId) params.append('movieId', filters.movieId);
    if (filters.cinemaId) params.append('cinemaId', filters.cinemaId);
    if (filters.showId) params.append('showId', filters.showId);
    if (filters.status && filters.status !== 'ALL') params.append('status', filters.status);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await api.get(`/admin/analytics/overview${query}`);
    return res.data;
  },

  /**
   * Fetch summary KPI card metrics
   */
  async getKPI(filters = {}) {
    const params = new URLSearchParams();
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.movieId) params.append('movieId', filters.movieId);
    if (filters.cinemaId) params.append('cinemaId', filters.cinemaId);
    if (filters.showId) params.append('showId', filters.showId);
    if (filters.status && filters.status !== 'ALL') params.append('status', filters.status);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await api.get(`/admin/analytics/kpi${query}`);
    return res.data;
  },

  /**
   * Fetch revenue analytics (daily, weekly, monthly, comparison)
   */
  async getRevenueAnalytics(filters = {}) {
    const params = new URLSearchParams();
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    if (filters.movieId) params.append('movieId', filters.movieId);
    if (filters.cinemaId) params.append('cinemaId', filters.cinemaId);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await api.get(`/admin/analytics/revenue${query}`);
    return res.data;
  },

  /**
   * Fetch filter options (available movies, cinemas, shows, statuses)
   */
  async getFilterOptions() {
    const res = await api.get('/admin/analytics/filter-options');
    return res.data;
  }
};
