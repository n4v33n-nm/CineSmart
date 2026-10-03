import { api } from './api';

export const seatService = {
  async getSeatMapForShow(showId) {
    const res = await api.get(`/shows/${showId}/seats`);
    return res.data;
  },

  async getGroupRecommendations(showId, payload) {
    const res = await api.post(`/shows/${showId}/group-seating/recommendations`, payload);
    return res.data;
  }
};
