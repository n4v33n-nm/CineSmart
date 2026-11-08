// CineSmart Realistic Mock Data Service
// Supplies rich, production-grade domain data for demonstration and offline preview

function createPosterSvg(title, genre, color1, color2) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 560" width="400" height="560">
    <defs>
      <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="${color1}" />
        <stop offset="100%" stop-color="${color2}" />
      </linearGradient>
      <linearGradient id="overlay" x1="0%" y1="100%" x2="0%" y2="0%">
        <stop offset="0%" stop-color="#0b0f19" stop-opacity="0.95" />
        <stop offset="50%" stop-color="#0b0f19" stop-opacity="0.3" />
        <stop offset="100%" stop-color="#0b0f19" stop-opacity="0.1" />
      </linearGradient>
    </defs>
    <rect width="400" height="560" fill="url(#grad)" />
    <circle cx="200" cy="210" r="90" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="4" />
    <polygon points="180,180 235,210 180,240" fill="rgba(255,255,255,0.4)" />
    <rect width="400" height="560" fill="url(#overlay)" />
    <text x="200" y="380" font-family="-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif" font-size="24" font-weight="900" fill="#ffffff" text-anchor="middle">${title}</text>
    <rect x="140" y="410" width="120" height="26" rx="13" fill="rgba(255,255,255,0.2)" />
    <text x="200" y="428" font-family="-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif" font-size="12" font-weight="700" fill="#ffffff" text-anchor="middle" letter-spacing="2">${genre.toUpperCase()}</text>
  </svg>`;
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}

export const MOCK_MOVIES = [
  {
    id: 1,
    title: 'Interstellar Odyssey',
    synopsis: "A heroic team of astronauts embarks on humanity's greatest voyage through a newly discovered wormhole in deep space to find a habitable new world.",
    durationMinutes: 169,
    language: 'English',
    genre: 'Sci-Fi',
    ageRating: 'PG-13',
    posterUrl: createPosterSvg('Interstellar Odyssey', 'Sci-Fi', '#0f172a', '#0284c7'),
    releaseDate: '2026-05-15',
    isActive: true,
  },
  {
    id: 2,
    title: 'Dune: The Desert Prophecy',
    synopsis: 'Paul Atreides unites with the Fremen on a spiritual quest of vengeance against conspirators who destroyed his noble family on Arrakis.',
    durationMinutes: 166,
    language: 'English',
    genre: 'Sci-Fi',
    ageRating: 'PG-13',
    posterUrl: createPosterSvg('Dune: Prophecy', 'Sci-Fi', '#451a03', '#d97706'),
    releaseDate: '2026-06-01',
    isActive: true,
  },
  {
    id: 3,
    title: 'The Dark Knight Returns',
    synopsis: "Eight years after the Joker's reign of chaos, Batman returns to defend Gotham City from the enigmatic masked terrorist Bane.",
    durationMinutes: 164,
    language: 'English',
    genre: 'Action',
    ageRating: 'PG-13',
    posterUrl: createPosterSvg('The Dark Knight', 'Action', '#090d16', '#334155'),
    releaseDate: '2026-04-20',
    isActive: true,
  },
  {
    id: 4,
    title: 'Cyberpunk: Neon Horizon',
    synopsis: 'In a neon-drenched metropolis, an outlaw mercenary takes on mega-corporate syndicates to retrieve a stolen cybernetic neural implant.',
    durationMinutes: 142,
    language: 'English',
    genre: 'Action',
    ageRating: 'R',
    posterUrl: createPosterSvg('Cyberpunk: Neon', 'Action', '#4a044e', '#ec4899'),
    releaseDate: '2026-07-10',
    isActive: true,
  },
  {
    id: 5,
    title: 'Spider-Man: Multiverse Rift',
    synopsis: 'Teenager Miles Morales must unite with alternate universe web-slingers to defeat a reality-shattering extradimensional threat.',
    durationMinutes: 140,
    language: 'English',
    genre: 'Animation',
    ageRating: 'PG',
    posterUrl: createPosterSvg('Spider-Man: Rift', 'Animation', '#7f1d1d', '#2563eb'),
    releaseDate: '2026-08-05',
    isActive: true,
  }
];

export const MOCK_CINEMAS = [
  {
    id: 1,
    name: 'CineSmart Central',
    city: 'Metropolis',
    address: '100 Broadway Boulevard, Suite 500',
    contactNumber: '+1-555-9000'
  }
];

export const MOCK_SCREENS = [
  { id: 1, cinemaId: 1, screenNumber: 1, screenType: 'IMAX_3D', totalCapacity: 48 },
  { id: 2, cinemaId: 1, screenNumber: 2, screenType: 'DOLBY_ATMOS', totalCapacity: 48 }
];

export const MOCK_SHOWS = [
  {
    id: 1,
    movieId: 1,
    movieTitle: 'Interstellar Odyssey',
    screenId: 1,
    screenName: 'Screen 1 (IMAX 3D)',
    cinemaName: 'CineSmart Central',
    startTime: '2026-11-03T10:00:00',
    endTime: '2026-11-03T12:49:00',
    basePrice: 180.00,
    status: 'SCHEDULED',
    availableSeats: 34,
    totalSeats: 48
  },
  {
    id: 2,
    movieId: 1,
    movieTitle: 'Interstellar Odyssey',
    screenId: 1,
    screenName: 'Screen 1 (IMAX 3D)',
    cinemaName: 'CineSmart Central',
    startTime: '2026-11-03T13:30:00',
    endTime: '2026-11-03T16:19:00',
    basePrice: 220.00,
    status: 'SCHEDULED',
    availableSeats: 28,
    totalSeats: 48
  },
  {
    id: 3,
    movieId: 1,
    movieTitle: 'Interstellar Odyssey',
    screenId: 1,
    screenName: 'Screen 1 (IMAX 3D)',
    cinemaName: 'CineSmart Central',
    startTime: '2026-11-03T18:45:00',
    endTime: '2026-11-03T21:34:00',
    basePrice: 250.00,
    status: 'SCHEDULED',
    availableSeats: 16,
    totalSeats: 48
  },
  {
    id: 4,
    movieId: 2,
    movieTitle: 'Dune: The Desert Prophecy',
    screenId: 2,
    screenName: 'Screen 2 (Dolby Atmos)',
    cinemaName: 'CineSmart Central',
    startTime: '2026-11-03T14:00:00',
    endTime: '2026-11-03T16:46:00',
    basePrice: 200.00,
    status: 'SCHEDULED',
    availableSeats: 32,
    totalSeats: 48
  },
  {
    id: 5,
    movieId: 3,
    movieTitle: 'The Dark Knight Returns',
    screenId: 2,
    screenName: 'Screen 2 (Dolby Atmos)',
    cinemaName: 'CineSmart Central',
    startTime: '2026-11-03T19:30:00',
    endTime: '2026-11-03T22:14:00',
    basePrice: 250.00,
    status: 'SCHEDULED',
    availableSeats: 22,
    totalSeats: 48
  }
];

export function generateMockSeats(showId = 1) {
  const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
  const seats = [];
  let seatCounter = 1;

  rows.forEach((row, rowIndex) => {
    for (let col = 1; col <= 8; col++) {
      let tier = 'STANDARD';
      let price = 180;
      let accessible = false;

      if (row === 'A' && (col === 1 || col === 2)) {
        accessible = true;
        tier = 'ACCESSIBLE_WHEELCHAIR';
        price = 150;
      } else if (row === 'A' || row === 'B') {
        tier = 'STANDARD';
        price = 180;
      } else if (row === 'C' || row === 'D') {
        tier = 'PREMIUM';
        price = 220;
      } else {
        tier = 'RECLINER';
        price = 280;
      }

      // Pre-book a few realistic seats for a live atmosphere
      let status = 'AVAILABLE';
      if ((row === 'C' && (col === 3 || col === 4)) || (row === 'E' && (col === 1 || col === 2 || col === 7 || col === 8))) {
        status = 'BOOKED';
      } else if (row === 'B' && col === 5) {
        status = 'HELD';
      }

      seats.push({
        showSeatId: seatCounter++,
        seatId: seatCounter,
        label: `${row}${col}`,
        row: row,
        number: col,
        tier: tier,
        price: price,
        status: status,
        accessible: accessible
      });
    }
  });

  return {
    showId: Number(showId),
    movieTitle: 'Interstellar Odyssey',
    screenName: 'Screen 1 (IMAX 3D)',
    cinemaName: 'CineSmart Central',
    totalSeats: 48,
    availableSeats: seats.filter(s => s.status === 'AVAILABLE').length,
    seats: seats
  };
}

export function generateMockGroupRecommendations(showId, payload = {}) {
  const partySize = payload.partySize || 4;
  return {
    showId: Number(showId),
    partySize: partySize,
    message: `Identified 3 optimal seat arrangements for group of ${partySize}`,
    recommendations: [
      {
        rank: 1,
        matchScore: 98,
        description: `Contiguous Optical Center Cluster in Row D (Seats D3 - D${2 + partySize})`,
        tier: 'PREMIUM',
        totalPrice: partySize * 220,
        splitAcrossRows: false,
        seats: [
          { showSeatId: 27, label: 'D3', row: 'D', number: 3, tier: 'PREMIUM', price: 220, status: 'AVAILABLE' },
          { showSeatId: 28, label: 'D4', row: 'D', number: 4, tier: 'PREMIUM', price: 220, status: 'AVAILABLE' },
          { showSeatId: 29, label: 'D5', row: 'D', number: 5, tier: 'PREMIUM', price: 220, status: 'AVAILABLE' },
          { showSeatId: 30, label: 'D6', row: 'D', number: 6, tier: 'PREMIUM', price: 220, status: 'AVAILABLE' },
        ].slice(0, partySize)
      },
      {
        rank: 2,
        matchScore: 92,
        description: `Luxury Recliner Section in Row F (Seats F3 - F${2 + partySize})`,
        tier: 'RECLINER',
        totalPrice: partySize * 280,
        splitAcrossRows: false,
        seats: [
          { showSeatId: 43, label: 'F3', row: 'F', number: 3, tier: 'RECLINER', price: 280, status: 'AVAILABLE' },
          { showSeatId: 44, label: 'F4', row: 'F', number: 4, tier: 'RECLINER', price: 280, status: 'AVAILABLE' },
          { showSeatId: 45, label: 'F5', row: 'F', number: 5, tier: 'RECLINER', price: 280, status: 'AVAILABLE' },
          { showSeatId: 46, label: 'F6', row: 'F', number: 6, tier: 'RECLINER', price: 280, status: 'AVAILABLE' },
        ].slice(0, partySize)
      },
      {
        rank: 3,
        matchScore: 85,
        description: `Standard Center Tier in Row B (Seats B1 - B${partySize})`,
        tier: 'STANDARD',
        totalPrice: partySize * 180,
        splitAcrossRows: false,
        seats: [
          { showSeatId: 9, label: 'B1', row: 'B', number: 1, tier: 'STANDARD', price: 180, status: 'AVAILABLE' },
          { showSeatId: 10, label: 'B2', row: 'B', number: 2, tier: 'STANDARD', price: 180, status: 'AVAILABLE' },
          { showSeatId: 11, label: 'B3', row: 'B', number: 3, tier: 'STANDARD', price: 180, status: 'AVAILABLE' },
          { showSeatId: 12, label: 'B4', row: 'B', number: 4, tier: 'STANDARD', price: 180, status: 'AVAILABLE' },
        ].slice(0, partySize)
      }
    ]
  };
}

export const MOCK_BOOKINGS = [
  {
    id: 101,
    bookingReference: 'CS-BKG-8492',
    movieTitle: 'Interstellar Odyssey',
    posterUrl: createPosterSvg('Interstellar Odyssey', 'Sci-Fi', '#0f172a', '#0284c7'),
    cinemaName: 'CineSmart Central',
    screenName: 'Screen 1 (IMAX 3D)',
    showStartTime: '2026-11-03T18:45:00',
    status: 'CONFIRMED',
    totalAmount: 980.00,
    seatLabels: ['D3', 'D4', 'D5', 'D6'],
    seatsCount: 4,
    createdAt: '2026-11-02T19:20:00',
    tickets: [
      { id: 1, ticketCode: 'TKT-CS-8492-01', seatLabel: 'D3', tier: 'PREMIUM', price: 220, qrCodeData: 'CINESMART:8492:D3' },
      { id: 2, ticketCode: 'TKT-CS-8492-02', seatLabel: 'D4', tier: 'PREMIUM', price: 220, qrCodeData: 'CINESMART:8492:D4' },
      { id: 3, ticketCode: 'TKT-CS-8492-03', seatLabel: 'D5', tier: 'PREMIUM', price: 220, qrCodeData: 'CINESMART:8492:D5' },
      { id: 4, ticketCode: 'TKT-CS-8492-04', seatLabel: 'D6', tier: 'PREMIUM', price: 220, qrCodeData: 'CINESMART:8492:D6' },
    ]
  },
  {
    id: 102,
    bookingReference: 'CS-BKG-9120',
    movieTitle: 'Dune: The Desert Prophecy',
    posterUrl: createPosterSvg('Dune: Prophecy', 'Sci-Fi', '#451a03', '#d97706'),
    cinemaName: 'CineSmart Central',
    screenName: 'Screen 2 (Dolby Atmos)',
    showStartTime: '2026-11-04T14:00:00',
    status: 'WAITLISTED',
    totalAmount: 440.00,
    seatLabels: ['C3', 'C4'],
    seatsCount: 2,
    createdAt: '2026-11-02T16:10:00',
    waitlistPosition: 1,
    claimWindowExpiresAt: new Date(Date.now() + 8 * 60 * 1000).toISOString()
  }
];

export const MOCK_USER = {
  id: 1,
  fullName: 'Naveen Kumar',
  email: 'naveen@cinesmart.io',
  role: 'ROLE_ADMIN'
};
