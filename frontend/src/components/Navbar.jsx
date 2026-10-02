import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Film, User, LogOut, ShieldCheck, Ticket, LogIn } from 'lucide-react';

export function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="glass-panel" style={{ position: 'sticky', top: 0, zIndex: 100, borderLeft: 'none', borderRight: 'none', borderTop: 'none', borderRadius: 0 }}>
      <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '0.85rem 1.5rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', textDecoration: 'none' }}>
          <div style={{ width: '38px', height: '38px', borderRadius: '10px', background: 'linear-gradient(135deg, #e50914, #ff334b)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 12px rgba(229, 9, 20, 0.4)' }}>
            <Film size={22} color="#fff" />
          </div>
          <span style={{ fontSize: '1.45rem', fontWeight: 800, letterSpacing: '-0.03em', background: 'linear-gradient(to right, #ffffff, #cbd5e1)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Cine<span style={{ color: 'var(--accent-red)', WebkitTextFillColor: 'var(--accent-red)' }}>Smart</span>
          </span>
        </Link>

        <nav style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
          <Link to="/movies" style={{ color: 'var(--text-secondary)', fontWeight: 500, fontSize: '0.95rem', transition: 'color 0.2s' }}>
            Movies
          </Link>

          {user ? (
            <>
              <Link to="/bookings" style={{ color: 'var(--text-secondary)', fontWeight: 500, fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <Ticket size={17} />
                <span>My Bookings</span>
              </Link>

              {isAdmin && (
                <Link to="/admin" className="badge badge-gold" style={{ textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.3rem', padding: '0.35rem 0.65rem' }}>
                  <ShieldCheck size={14} />
                  <span>Admin Panel</span>
                </Link>
              )}

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', paddingLeft: '0.5rem', borderLeft: '1px solid var(--border-subtle)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem', fontSize: '0.9rem', color: 'var(--text-primary)' }}>
                  <User size={16} color="var(--accent-cyan)" />
                  <span style={{ maxWidth: '140px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {user.fullName || user.email}
                  </span>
                </div>
                <button onClick={handleLogout} className="btn-secondary" style={{ padding: '0.4rem 0.8rem', fontSize: '0.85rem' }} title="Log out">
                  <LogOut size={15} />
                  <span>Logout</span>
                </button>
              </div>
            </>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Link to="/login" className="btn-secondary" style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}>
                <LogIn size={16} />
                <span>Login</span>
              </Link>
              <Link to="/register" className="btn-primary" style={{ padding: '0.5rem 1.1rem', fontSize: '0.9rem' }}>
                Register
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
