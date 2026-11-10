import { api } from './api';

export const paymentService = {
  async initiateAndProcessPayment(bookingId, paymentMethod, simulateSuccess = true, failureReason = null, idempotencyKey = null, couponCode = null, loyaltyPointsToRedeem = 0) {
    const res = await api.post('/payments', {
      bookingId,
      paymentMethod,
      simulateSuccess,
      failureReason,
      idempotencyKey,
      couponCode: couponCode || undefined,
      loyaltyPointsToRedeem: loyaltyPointsToRedeem ? Number(loyaltyPointsToRedeem) : undefined,
    });
    return res.data;
  },

  async getPaymentById(id) {
    const res = await api.get(`/payments/${id}`);
    return res.data;
  },

  async getPaymentsForBooking(bookingId) {
    const res = await api.get(`/payments/booking/${bookingId}`);
    return res.data || [];
  },

  async refundPayment(id, reason) {
    const res = await api.post(`/payments/${id}/refund`, { reason });
    return res.data;
  }
};
