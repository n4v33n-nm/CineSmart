import { api } from './api';

export const movieService = {
  async getMovies(genre, search) {
    const params = new URLSearchParams();
    if (genre) params.append('genre', genre);
    if (search) params.append('search', search);

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await api.get(`/movies${query}`);
    return res.data || [];
  },

  async getMovieById(id) {
    const res = await api.get(`/movies/${id}`);
    return res.data;
  },

  async getShowsForMovie(movieId) {
    const res = await api.get(`/movies/${movieId}/shows`);
    return res.data || [];
  }
};
