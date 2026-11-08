import { api } from './api';

export const ticketService = {
  async getTicketsForBooking(bookingId) {
    const res = await api.get(`/tickets/booking/${bookingId}`);
    return res.data || [];
  },

  async getTicketByCode(ticketCode) {
    const res = await api.get(`/tickets/${ticketCode}`);
    return res.data;
  }
};
