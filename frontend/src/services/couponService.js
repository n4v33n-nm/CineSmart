import { api } from './api';

export const couponService = {
  // Public / Customer APIs
  getActiveCoupons: async () => {
    return api.get('/coupons/active');
  },

  validateCoupon: async (code, bookingAmount) => {
    return api.post('/coupons/validate', { code, bookingAmount });
  },

  // Admin APIs
  getAllCoupons: async () => {
    return api.get('/admin/coupons');
  },

  createCoupon: async (couponData) => {
    return api.post('/admin/coupons', couponData);
  },

  updateCoupon: async (id, couponData) => {
    return api.put(`/admin/coupons/${id}`, couponData);
  },

  toggleCoupon: async (id) => {
    return api.put(`/admin/coupons/${id}/toggle`);
  },

  deleteCoupon: async (id) => {
    return api.delete(`/admin/coupons/${id}`);
  }
};
