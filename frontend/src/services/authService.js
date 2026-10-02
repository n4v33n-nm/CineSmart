import { api } from './api';

export const authService = {
  async register(data) {
    const res = await api.post('/auth/register', data);
    if (res.data?.accessToken) {
      localStorage.setItem('cinesmart_token', res.data.accessToken);
      localStorage.setItem('cinesmart_user', JSON.stringify(res.data.user));
    }
    return res.data;
  },

  async login(email, password) {
    const res = await api.post('/auth/login', { email, password });
    if (res.data?.accessToken) {
      localStorage.setItem('cinesmart_token', res.data.accessToken);
      localStorage.setItem('cinesmart_user', JSON.stringify(res.data.user));
    }
    return res.data;
  },

  async getCurrentUser() {
    const res = await api.get('/auth/me');
    return res.data;
  },

  logout() {
    localStorage.removeItem('cinesmart_token');
    localStorage.removeItem('cinesmart_user');
  },

  getStoredUser() {
    try {
      const userStr = localStorage.getItem('cinesmart_user');
      return userStr ? JSON.parse(userStr) : null;
    } catch {
      return null;
    }
  },

  isAuthenticated() {
    return !!localStorage.getItem('cinesmart_token');
  }
};
