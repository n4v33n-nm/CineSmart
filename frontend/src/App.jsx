import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { ProtectedRoute } from './components/ProtectedRoute';

import { HomePage } from './pages/HomePage';
import { MovieListPage } from './pages/MovieListPage';
import { MovieDetailsPage } from './pages/MovieDetailsPage';
import { SeatSelectionPage } from './pages/SeatSelectionPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { TicketVerificationPage } from './pages/TicketVerificationPage';
import { LoyaltyRewardsPage } from './pages/LoyaltyRewardsPage';

export function App() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', background: 'var(--bg-primary)' }}>
      <Navbar />

      <main style={{ flex: 1, width: '100%', maxWidth: '1280px', margin: '0 auto', padding: '1.5rem 1.5rem 3rem' }}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/movies" element={<MovieListPage />} />
          <Route path="/movies/:id" element={<MovieDetailsPage />} />
          <Route path="/shows/:id/seats" element={<SeatSelectionPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Customer Routes */}
          <Route
            path="/bookings"
            element={
              <ProtectedRoute>
                <MyBookingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/rewards"
            element={
              <ProtectedRoute>
                <LoyaltyRewardsPage />
              </ProtectedRoute>
            }
          />

          {/* Protected Admin Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute adminOnly>
                <AdminDashboardPage />
              </ProtectedRoute>
            }
          />

          {/* Protected Staff & Admin Ticket Verification */}
          <Route
            path="/verify-tickets"
            element={
              <ProtectedRoute staffOrAdminOnly>
                <TicketVerificationPage />
              </ProtectedRoute>
            }
          />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <Footer />
    </div>
  );
}

export default App;
