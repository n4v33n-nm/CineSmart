// Centralized API Client with standard error handling, JWT injection, and seamless fallback
import {
  MOCK_MOVIES,
  MOCK_CINEMAS,
  MOCK_SCREENS,
  MOCK_SHOWS,
  generateMockSeats,
  generateMockGroupRecommendations,
  MOCK_BOOKINGS,
  MOCK_USER
} from './mockData';

const BASE_URL = '/api';

function generateMockAnalyticsOverview() {
  const dates = [];
  const now = new Date();
  for (let i = 14; i >= 0; i--) {
    const d = new Date(now);
    d.setDate(d.getDate() - i);
    dates.push(d.toISOString().split('T')[0]);
  }

  const dailyRevenue = dates.map((date, idx) => ({
    date,
    label: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    value: 1200 + (idx * 240) + ((idx % 3) * 350),
    count: 3 + (idx % 4)
  }));

  const dailyBookings = dates.map((date, idx) => ({
    date,
    label: new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
    value: 3 + (idx % 4),
    count: 3 + (idx % 4)
  }));

  return {
    kpi: {
      totalUsers: 48,
      totalBookings: 64,
      confirmedBookings: 52,
      cancelledBookings: 12,
      totalGrossRevenue: 38450.00,
      totalRefundAmount: 4200.00,
      netRevenue: 34250.00,
      totalMovies: 6,
      totalTheatres: 3,
      averageOccupancyPercentage: 74.8,
      confirmedRate: 81.3,
      cancellationRate: 18.7
    },
    revenue: {
      dailyRevenue,
      weeklyRevenue: [
        { label: 'Week 41', value: 7200, count: 12 },
        { label: 'Week 42', value: 9400, count: 16 },
        { label: 'Week 43', value: 10850, count: 18 },
        { label: 'Week 44', value: 11000, count: 18 }
      ],
      monthlyRevenue: [
        { label: 'August', value: 24500, count: 42 },
        { label: 'September', value: 31200, count: 54 },
        { label: 'October', value: 36800, count: 62 },
        { label: 'November', value: 38450, count: 64 }
      ],
      currentPeriodTotal: 38450.00,
      previousPeriodTotal: 31200.00,
      growthPercentage: 23.2
    },
    bookings: {
      dailyBookings,
      weeklyBookings: [
        { label: 'Week 41', value: 12, count: 12 },
        { label: 'Week 42', value: 16, count: 16 },
        { label: 'Week 43', value: 18, count: 18 },
        { label: 'Week 44', value: 18, count: 18 }
      ],
      monthlyBookings: [
        { label: 'August', value: 42, count: 42 },
        { label: 'September', value: 54, count: 54 },
        { label: 'October', value: 62, count: 62 },
        { label: 'November', value: 64, count: 64 }
      ],
      statusDistribution: [
        { category: 'CONFIRMED', value: 52, percentage: 81.25, color: '#10B981' },
        { category: 'CANCELLED', value: 12, percentage: 18.75, color: '#EF4444' }
      ],
      totalBookingsCount: 64
    },
    movies: {
      mostBookedMovies: [
        { category: 'Interstellar Odyssey', value: 26, percentage: 40.6, color: '#E50914' },
        { category: 'Dune: The Desert Prophecy', value: 18, percentage: 28.1, color: '#D97706' },
        { category: 'Cyber City 2099', value: 12, percentage: 18.8, color: '#06B6D4' },
        { category: 'The Dark Knight Returns', value: 8, percentage: 12.5, color: '#8B5CF6' }
      ],
      popularGenres: [
        { category: 'Sci-Fi', value: 44, percentage: 68.75, color: '#38BDF8' },
        { category: 'Action', value: 20, percentage: 31.25, color: '#F43F5E' }
      ],
      movieRevenue: [
        { category: 'Interstellar Odyssey', value: 17200, percentage: 44.7, color: '#E50914' },
        { category: 'Dune: The Desert Prophecy', value: 11400, percentage: 29.6, color: '#D97706' },
        { category: 'Cyber City 2099', value: 6250, percentage: 16.3, color: '#06B6D4' },
        { category: 'The Dark Knight Returns', value: 3600, percentage: 9.4, color: '#8B5CF6' }
      ]
    },
    theatres: {
      theatreBookings: [
        { category: 'CineSmart Central', value: 38, percentage: 59.4, color: '#E50914' },
        { category: 'Nexus Mall IMAX', value: 16, percentage: 25.0, color: '#06B6D4' },
        { category: 'Phoenix Grand', value: 10, percentage: 15.6, color: '#F59E0B' }
      ],
      theatreRevenue: [
        { category: 'CineSmart Central', value: 22800, percentage: 59.3, color: '#E50914' },
        { category: 'Nexus Mall IMAX', value: 9900, percentage: 25.7, color: '#06B6D4' },
        { category: 'Phoenix Grand', value: 5750, percentage: 15.0, color: '#F59E0B' }
      ],
      theatreOccupancy: [
        { category: 'CineSmart Central', value: 82.5, percentage: 82.5, color: '#10B981' },
        { category: 'Nexus Mall IMAX', value: 74.0, percentage: 74.0, color: '#38BDF8' },
        { category: 'Phoenix Grand', value: 67.8, percentage: 67.8, color: '#F59E0B' }
      ],
      showPerformance: [
        { showId: 1, movieTitle: 'Interstellar Odyssey', cinemaName: 'CineSmart Central', screenName: 'Screen 1 (IMAX 3D)', showTime: '18:45', ticketsSold: 28, occupancyPercent: 87.5, revenue: 14000 },
        { showId: 2, movieTitle: 'Dune: The Desert Prophecy', cinemaName: 'CineSmart Central', screenName: 'Screen 2 (Dolby Atmos)', showTime: '21:30', ticketsSold: 22, occupancyPercent: 78.6, revenue: 8800 },
        { showId: 3, movieTitle: 'Cyber City 2099', cinemaName: 'Nexus Mall IMAX', screenName: 'Screen 1 (IMAX 3D)', showTime: '16:00', ticketsSold: 18, occupancyPercent: 75.0, revenue: 7200 }
      ]
    },
    marketingRefunds: {
      totalCouponsApplied: 18,
      totalCouponDiscountAmount: 3200.00,
      topCoupons: [
        { category: 'WELCOME50', value: 12, percentage: 66.7, color: '#10B981' },
        { category: 'FESTIVE25', value: 4, percentage: 22.2, color: '#38BDF8' },
        { category: 'BLOCKBUSTER', value: 2, percentage: 11.1, color: '#F59E0B' }
      ],
      totalLoyaltyPointsEarned: 3845,
      totalLoyaltyPointsRedeemed: 1650,
      totalLoyaltyDiscountAmount: 1650.00,
      totalRefundRequests: 12,
      completedRefundsCount: 10,
      rejectedRefundsCount: 1,
      pendingRefundsCount: 1,
      cancellationRatePercent: 18.7,
      refundStatusDistribution: [
        { category: 'COMPLETED', value: 10, percentage: 83.3, color: '#10B981' },
        { category: 'REJECTED', value: 1, percentage: 8.3, color: '#EF4444' },
        { category: 'REQUESTED', value: 1, percentage: 8.3, color: '#F59E0B' }
      ]
    },
    filterOptions: {
      movies: [
        { id: 1, name: 'Interstellar Odyssey' },
        { id: 2, name: 'Dune: The Desert Prophecy' },
        { id: 3, name: 'Cyber City 2099' },
        { id: 4, name: 'The Dark Knight Returns' }
      ],
      cinemas: [
        { id: 1, name: 'CineSmart Central' },
        { id: 2, name: 'Nexus Mall IMAX' },
        { id: 3, name: 'Phoenix Grand' }
      ],
      shows: [
        { id: 1, name: 'Interstellar Odyssey - CineSmart Central (18:45)' },
        { id: 2, name: 'Dune - CineSmart Central (21:30)' },
        { id: 3, name: 'Cyber City 2099 - Nexus Mall (16:00)' }
      ],
      statuses: ['ALL', 'CONFIRMED', 'CANCELLED', 'HELD', 'PENDING']
    }
  };
}

function handleMockRequest(endpoint, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const [path, queryString] = endpoint.split('?');
  const body = options.body ? JSON.parse(options.body) : {};

  // 1. Movies
  if (path === '/movies' && method === 'GET') {
    return { data: MOCK_MOVIES };
  }
  const movieMatch = path.match(/^\/movies\/(\d+)$/);
  if (movieMatch && method === 'GET') {
    const id = Number(movieMatch[1]);
    const found = MOCK_MOVIES.find((m) => m.id === id) || MOCK_MOVIES[0];
    return { data: found };
  }
  const movieShowsMatch = path.match(/^\/movies\/(\d+)\/shows$/);
  if (movieShowsMatch && method === 'GET') {
    const id = Number(movieShowsMatch[1]);
    const shows = MOCK_SHOWS.filter((s) => s.movieId === id);
    return { data: shows.length ? shows : MOCK_SHOWS };
  }

  // 2. Shows & Seats
  const seatMapMatch = path.match(/^\/shows\/(\d+)\/seats$/);
  if (seatMapMatch && method === 'GET') {
    return { data: generateMockSeats(seatMapMatch[1]) };
  }
  const groupRecMatch = path.match(/^\/shows\/(\d+)\/group-seating\/recommendations$/);
  if (groupRecMatch && method === 'POST') {
    return { data: generateMockGroupRecommendations(groupRecMatch[1], body) };
  }
  const showMatch = path.match(/^\/shows\/(\d+)$/);
  if (showMatch && method === 'GET') {
    return { data: MOCK_SHOWS.find((s) => s.id === Number(showMatch[1])) || MOCK_SHOWS[0] };
  }
  if (path === '/shows' && method === 'GET') {
    return { data: MOCK_SHOWS };
  }

  // 3. Cinemas & Screens
  if (path === '/cinemas' && method === 'GET') {
    return { data: MOCK_CINEMAS };
  }
  if (path.startsWith('/cinemas/') && path.endsWith('/screens') && method === 'GET') {
    return { data: MOCK_SCREENS };
  }

  // 4. Bookings & Tickets
  if (path === '/bookings/my' && method === 'GET') {
    return { data: MOCK_BOOKINGS };
  }
  const bookingMatch = path.match(/^\/bookings\/(\d+)$/);
  if (bookingMatch && method === 'GET') {
    return { data: MOCK_BOOKINGS.find((b) => b.id === Number(bookingMatch[1])) || MOCK_BOOKINGS[0] };
  }
  if (path === '/bookings' && method === 'POST') {
    return {
      data: {
        id: 101,
        bookingReference: 'CS-BKG-8492',
        status: 'HELD',
        totalAmount: 980.00,
        movieTitle: 'Interstellar Odyssey',
        cinemaName: 'CineSmart Central',
        screenName: 'Screen 1 (IMAX 3D)',
        showStartTime: '2026-11-03T18:45:00',
        heldUntil: new Date(Date.now() + 10 * 60 * 1000).toISOString(),
        seatLabels: ['D3', 'D4', 'D5', 'D6']
      }
    };
  }
  if (path.startsWith('/tickets/booking/') && method === 'GET') {
    return { data: MOCK_BOOKINGS[0].tickets };
  }

  // 5. Payments
  if (path === '/payments' && method === 'POST') {
    return {
      data: {
        id: 501,
        status: 'SUCCESS',
        transactionId: 'TXN-CS-984210',
        bookingId: body.bookingId || 101,
        amount: 980.00,
        paymentMethod: body.paymentMethod || 'UPI',
        paidAt: new Date().toISOString()
      }
    };
  }

  // 6. Authentication
  if (path === '/auth/login' || path === '/auth/register') {
    return {
      data: {
        accessToken: 'mock-cinesmart-jwt-token-active',
        user: MOCK_USER
      }
    };
  }
  if (path === '/auth/me') {
    return { data: MOCK_USER };
  }

  // 7. Tickets
  if (path === '/tickets/stats' && method === 'GET') {
    return {
      data: {
        totalGenerated: 42,
        verifiedTickets: 18,
        unusedTickets: 24,
        invalidAttempts: 2,
        recentVerifications: [
          {
            id: 1,
            ticketCode: 'TKT-2026-DEMO01',
            verifiedBy: 'Staff Operator',
            result: 'SUCCESS',
            failureReason: 'Valid entry granted',
            attemptedAt: new Date().toISOString()
          }
        ]
      }
    };
  }
  if (path === '/tickets/verify' && method === 'POST') {
    const code = body.ticketCode || 'TKT-2026-DEMO01';
    return {
      data: {
        valid: true,
        ticketCode: code,
        status: 'VERIFIED_SUCCESSFULLY',
        message: 'Entry granted. Ticket successfully verified.',
        verifiedAt: new Date().toISOString(),
        verifiedBy: 'Staff Operator',
        ticket: {
          ticketCode: code,
          movieTitle: 'Interstellar Odyssey',
          cinemaName: 'CineSmart Central',
          screenName: 'Screen 1 (IMAX 3D)',
          showStartTime: '2026-11-03T18:45:00',
          seatLabel: 'D4',
          seatTier: 'PREMIUM',
          price: 350.00,
          status: 'USED'
        }
      }
    };
  }

  // 8. Recommendations (Phase 5)
  if (path === '/recommendations/home-sections' && method === 'GET') {
    const list = (MOCK_MOVIES || []).map((m, idx) => ({
      movie: m,
      score: 0.95 - (idx * 0.05),
      matchPercentage: 98 - (idx * 4),
      recommendationReason: idx === 0 ? 'Top Pick matching your genre preferences' : 'Popular in theaters right now',
      category: 'PERSONALIZED',
      matchHighlight: `${98 - (idx * 4)}% Match • Highly Recommended`
    }));
    return {
      data: {
        recommendedForYou: list.slice(0, 4),
        becauseYouWatched: {
          sourceMovie: MOCK_MOVIES[0],
          recommendations: list.slice(1, 4)
        },
        trendingMovies: list,
        popularMovies: list,
        newReleases: list,
        topRated: list,
        isPersonalized: true,
        preferredGenres: ['Sci-Fi', 'Action']
      }
    };
  }
  if (path === '/recommendations/personalized' && method === 'GET') {
    return {
      data: (MOCK_MOVIES || []).map((m, idx) => ({
        movie: m,
        score: 0.95 - (idx * 0.05),
        matchPercentage: 98 - (idx * 4),
        recommendationReason: 'Personalized based on your viewing history',
        category: 'PERSONALIZED',
        matchHighlight: `${98 - (idx * 4)}% Match • Must Watch`
      }))
    };
  }
  const similarMatch = path.match(/^\/recommendations\/movies\/(\d+)\/similar$/);
  if (similarMatch && method === 'GET') {
    const sourceId = Number(similarMatch[1]);
    const others = (MOCK_MOVIES || []).filter(m => m.id !== sourceId);
    return {
      data: others.map((m, idx) => ({
        movie: m,
        score: 0.90 - (idx * 0.05),
        matchPercentage: 95 - (idx * 4),
        recommendationReason: `Fans of movie #${sourceId} also loved this`,
        category: 'SIMILAR',
        matchHighlight: `${95 - (idx * 4)}% Match • Similar Genre`
      }))
    };
  }
  if (path === '/recommendations/preferences' && method === 'GET') {
    return {
      data: {
        userId: 1,
        email: 'user@cinesmart.com',
        preferredGenres: ['Sci-Fi', 'Action'],
        availableGenres: ['Action', 'Sci-Fi', 'Drama', 'Animation', 'Adventure', 'Crime', 'Comedy', 'Thriller', 'Horror']
      }
    };
  }
  if (path === '/recommendations/preferences' && method === 'PUT') {
    return {
      data: {
        userId: 1,
        email: 'user@cinesmart.com',
        preferredGenres: body.preferredGenres || ['Sci-Fi', 'Action'],
        availableGenres: ['Action', 'Sci-Fi', 'Drama', 'Animation', 'Adventure', 'Crime', 'Comedy', 'Thriller', 'Horror']
      }
    };
  }

  // 9. Admin Analytics fallbacks
  if (path.startsWith('/admin/analytics/')) {
    const overview = generateMockAnalyticsOverview();
    if (path.endsWith('/overview')) return { data: overview };
    if (path.endsWith('/kpi')) return { data: overview.kpi };
    if (path.endsWith('/revenue')) return { data: overview.revenue };
    if (path.endsWith('/bookings')) return { data: overview.bookings };
    if (path.endsWith('/movies')) return { data: overview.movies };
    if (path.endsWith('/theatres')) return { data: overview.theatres };
    if (path.endsWith('/marketing-refunds')) return { data: overview.marketingRefunds };
    if (path.endsWith('/filter-options')) return { data: overview.filterOptions };
    return { data: overview };
  }

  // 10. General Admin fallbacks
  if (path.startsWith('/admin/')) {
    return { data: { success: true, message: 'Admin operation recorded' } };
  }

  return { data: {} };
}

export async function request(endpoint, options = {}) {
  const token = localStorage.getItem('cinesmart_token');

  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    const text = await response.text();
    let data;

    try {
      data = text ? JSON.parse(text) : {};
    } catch {
      data = { message: text };
    }

    if (!response.ok) {
      const errorMsg = data.message || `Request failed with status ${response.status}`;
      const error = new Error(errorMsg);
      error.status = response.status;
      error.errorCode = data.errorCode || 'UNKNOWN_ERROR';
      error.details = data;
      throw error;
    }

    return data;
  } catch (error) {
    // If the server responded with an HTTP error code (400, 401, 403, 409, 500, etc.), propagate it
    if (error.status) {
      throw error;
    }
    // Only if network failed completely (e.g. backend server down), fallback for standalone demo
    console.warn(`[CineSmart API] Server unreachable for ${endpoint}, falling back to offline demo data:`, error);
    return handleMockRequest(endpoint, options);
  }
}

export const api = {
  get: (url, options) => request(url, { method: 'GET', ...options }),
  post: (url, body, options) => request(url, { method: 'POST', body: JSON.stringify(body), ...options }),
  put: (url, body, options) => request(url, { method: 'PUT', body: JSON.stringify(body), ...options }),
  patch: (url, body, options) => request(url, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined, ...options }),
  delete: (url, options) => request(url, { method: 'DELETE', ...options }),
};

export default api;
