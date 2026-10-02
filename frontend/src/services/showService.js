import { api } from './api';

export const showService = {
  async getShows(movieId, date) {
    const params = new URLSearchParams();
    if (movieId) params.append('movieId', movieId);
    if (date) params.append('date', date);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await api.get(`/shows${query}`);
    return res.data || [];
  },

  async getShowById(id) {
    const res = await api.get(`/shows/${id}`);
    return res.data;
  },

  async getShowsForMovie(movieId) {
    const res = await api.get(`/movies/${movieId}/shows`);
    return res.data || [];
  }
};
