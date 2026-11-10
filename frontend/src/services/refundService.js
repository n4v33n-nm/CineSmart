import { api } from './api';

export const refundService = {
  // Public policy endpoint
  getPublicPolicies: () => {
    return api.get('/cancellation-policies');
  },

  // Customer cancellation & refund operations
  getCancellationPreview: (bookingId) => {
    return api.get(`/bookings/${bookingId}/cancellation-preview`);
  },

  requestCancellation: (bookingId, payload = {}) => {
    return api.post(`/bookings/${bookingId}/cancel-request`, payload);
  },

  getMyRefunds: () => {
    return api.get('/refunds/my');
  },

  getRefundById: (id) => {
    return api.get(`/refunds/${id}`);
  },

  // Admin refund operations
  getAllRefunds: (status) => {
    const url = status && status !== 'ALL' ? `/admin/refunds?status=${status}` : '/admin/refunds';
    return api.get(url);
  },

  getRefundStats: () => {
    return api.get('/admin/refunds/stats');
  },

  approveRefund: (id, payload = {}) => {
    return api.post(`/admin/refunds/${id}/approve`, payload);
  },

  processRefund: (id, payload = {}) => {
    return api.post(`/admin/refunds/${id}/process`, payload);
  },

  rejectRefund: (id, payload = {}) => {
    return api.post(`/admin/refunds/${id}/reject`, payload);
  },

  // Admin policy operations
  getAllPolicies: () => {
    return api.get('/admin/cancellation-policies');
  },

  getPolicyById: (id) => {
    return api.get(`/admin/cancellation-policies/${id}`);
  },

  createPolicy: (policyData) => {
    return api.post('/admin/cancellation-policies', policyData);
  },

  updatePolicy: (id, policyData) => {
    return api.put(`/admin/cancellation-policies/${id}`, policyData);
  },

  togglePolicy: (id) => {
    return api.patch(`/admin/cancellation-policies/${id}/toggle`);
  },

  deletePolicy: (id) => {
    return api.delete(`/admin/cancellation-policies/${id}`);
  }
};

export default refundService;
