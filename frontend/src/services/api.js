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

  // 8. Admin fallbacks
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
