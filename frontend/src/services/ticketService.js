import { api } from './api';

export const ticketService = {
  async getTicketsForBooking(bookingId) {
    const res = await api.get(`/tickets/booking/${bookingId}`);
    return res.data || [];
  },

  async getTicketByCode(ticketCode) {
    const res = await api.get(`/tickets/${ticketCode}`);
    return res.data;
  },

  async verifyTicket(ticketCode, verificationHash) {
    const res = await api.post('/tickets/verify', { ticketCode, verificationHash });
    return res.data;
  },

  async getTicketStats() {
    const res = await api.get('/tickets/stats');
    return res.data;
  }
};
