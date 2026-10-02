import { api } from './api';

export const adminService = {
  async createMovie(movieData) {
    const res = await api.post('/admin/movies', movieData);
    return res.data;
  },

  async updateMovie(id, movieData) {
    const res = await api.put(`/admin/movies/${id}`, movieData);
    return res.data;
  },

  async deactivateMovie(id) {
    const res = await api.delete(`/admin/movies/${id}`);
    return res.data;
  },

  async createShow(showData) {
    const res = await api.post('/admin/shows', showData);
    return res.data;
  },

  async cancelShow(id) {
    const res = await api.put(`/admin/shows/${id}/cancel`, {});
    return res.data;
  },

  async createScreen(screenData) {
    const res = await api.post('/admin/screens', screenData);
    return res.data;
  },

  async getCinemas() {
    const res = await api.get('/cinemas');
    return res.data || [];
  },

  async getScreensByCinema(cinemaId) {
    const res = await api.get(`/cinemas/${cinemaId}/screens`);
    return res.data || [];
  }
};
