import { api } from './api';

export const bookingService = {
  async getMyBookings() {
    const res = await api.get('/bookings/my');
    return res.data || [];
  },

  async getBookingById(id) {
    const res = await api.get(`/bookings/${id}`);
    return res.data;
  },

  async createBooking(showId, showSeatIds) {
    const res = await api.post('/bookings', { showId, showSeatIds });
    return res.data;
  },

  async cancelBooking(id) {
    const res = await api.post(`/bookings/${id}/cancel`);
    return res.data;
  },

  async getBookingTickets(id) {
    const res = await api.get(`/tickets/booking/${id}`);
    return res.data || [];
  }
};
