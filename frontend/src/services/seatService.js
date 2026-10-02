import { api } from './api';

export const seatService = {
  async getSeatMapForShow(showId) {
    const res = await api.get(`/shows/${showId}/seats`);
    return res.data;
  }
};
