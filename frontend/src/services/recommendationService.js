import { api } from './api';

export const recommendationService = {
  /**
   * Fetch all 6 recommendation sections for the homepage
   */
  async getHomeSections() {
    const res = await api.get('/recommendations/home-sections');
    return res.data || {
      recommendedForYou: [],
      becauseYouWatched: null,
      trendingMovies: [],
      popularMovies: [],
      newReleases: [],
      topRated: [],
      isPersonalized: false,
      preferredGenres: []
    };
  },

  /**
   * Fetch personalized recommendations with a specified limit
   */
  async getPersonalized(limit = 6) {
    const res = await api.get(`/recommendations/personalized?limit=${limit}`);
    return res.data || [];
  },

  /**
   * Fetch similar movies for a movie details page ("More Like This")
   */
  async getSimilarMovies(movieId, limit = 4) {
    const res = await api.get(`/recommendations/movies/${movieId}/similar?limit=${limit}`);
    return res.data || [];
  },

  /**
   * Fetch current user's preferred genres
   */
  async getUserPreferences() {
    const res = await api.get('/recommendations/preferences');
    return res.data || { preferredGenres: [], availableGenres: [] };
  },

  /**
   * Update current user's preferred genres
   */
  async updateUserPreferences(preferredGenres) {
    const res = await api.put('/recommendations/preferences', { preferredGenres });
    return res.data;
  }
};
