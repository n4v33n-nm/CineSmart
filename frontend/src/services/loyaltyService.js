import { api } from './api';

export const loyaltyService = {
  getBalance: async () => {
    return api.get('/loyalty/balance');
  },

  getTransactions: async () => {
    return api.get('/loyalty/transactions');
  },

  calculateRedemption: async (points, currentTotal) => {
    return api.post('/loyalty/calculate-redemption', { points, currentTotal });
  }
};
