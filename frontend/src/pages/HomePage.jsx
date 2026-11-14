import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { movieService } from '../services/movieService';
import { recommendationService } from '../services/recommendationService';
import { RecommendationCard } from '../components/RecommendationCard';
import { MovieGrid } from '../components/MovieGrid';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorMessage } from '../components/ErrorMessage';
import { useAuth } from '../context/AuthContext';
import {
  Film, Users, ShieldCheck, TicketCheck, ArrowRight,
  Sparkles, TrendingUp, Star, Flame, Award, Heart, Check, Settings2
} from 'lucide-react';

export function HomePage() {
  const { user } = useAuth();
  const [movies, setMovies] = useState([]);
  const [recSections, setRecSections] = useState({
    recommendedForYou: [],
    becauseYouWatched: null,
    trendingMovies: [],
    popularMovies: [],
    newReleases: [],
    topRated: [],
    isPersonalized: false,
    preferredGenres: []
  });
  const [loading, setLoading] = useState(true);
  const [recLoading, setRecLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activePreferences, setActivePreferences] = useState([]);
  const [availableGenres, setAvailableGenres] = useState([
    'Action', 'Sci-Fi', 'Drama', 'Animation', 'Adventure', 'Crime', 'Comedy', 'Thriller'
  ]);
  const [showGenreSelector, setShowGenreSelector] = useState(false);
  const [savingPrefs, setSavingPrefs] = useState(false);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        const [moviesData, recData] = await Promise.all([
          movieService.getMovies().catch(() => []),
          recommendationService.getHomeSections().catch(() => null)
        ]);

        setMovies(moviesData || []);
        if (recData) {
          setRecSections(recData);
          if (recData.preferredGenres && recData.preferredGenres.length > 0) {
            setActivePreferences(recData.preferredGenres);
          }
        }
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
        setRecLoading(false);
      }
    }
    loadData();
  }, [user]);

  const toggleGenrePreference = async (genre) => {
    const updated = activePreferences.includes(genre)
      ? activePreferences.filter(g => g !== genre)
      : [...activePreferences, genre];

    setActivePreferences(updated);

    if (user) {
      try {
        setSavingPrefs(true);
        await recommendationService.updateUserPreferences(updated);
        // Refresh recommendations to reflect updated preferences immediately
        const freshSections = await recommendationService.getHomeSections();
        if (freshSections) {
          setRecSections(freshSections);
        }
      } catch (e) {
        console.error('Failed to update genre preferences', e);
      } finally {
        setSavingPrefs(false);
      }
    }
  };

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', paddingBottom: '4rem' }}>
      {/* Hero Section */}
      <section style={{
        position: 'relative',
        borderRadius: 'var(--radius-lg)',
        overflow: 'hidden',
        padding: '4.5rem 2.5rem',
        marginBottom: '3rem',
        background: 'linear-gradient(135deg, rgba(229, 9, 20, 0.25) 0%, rgba(15, 20, 34, 0.95) 70%), url("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1600&q=80") center/cover',
        boxShadow: 'var(--shadow-md)',
        border: '1px solid var(--border-subtle)',
      }}>
        <div style={{ maxWidth: '720px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', marginBottom: '1rem' }}>
            <span className="badge badge-gold">
              AI & Algorithmic Cinema Intelligence
            </span>
            {recSections.isPersonalized && (
              <span style={{
                background: 'rgba(16, 185, 129, 0.2)',
                border: '1px solid #10B981',
                color: '#10B981',
                padding: '4px 10px',
                borderRadius: '999px',
                fontSize: '0.75rem',
                fontWeight: 700,
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px'
              }}>
                <Sparkles size={12} />
                Tailored for {user?.fullName || 'You'}
              </span>
            )}
          </div>

          <h1 style={{ fontSize: 'clamp(2.2rem, 5vw, 3.4rem)', marginBottom: '1.25rem', color: '#fff', fontWeight: 800, lineHeight: 1.15 }}>
            Experience Cinema The <span style={{ color: 'var(--accent-red)' }}>Smart</span> Way.
          </h1>
          <p style={{ fontSize: '1.1rem', color: 'var(--text-secondary)', marginBottom: '2rem', lineHeight: 1.6 }}>
            Seamless movie ticket reservations powered by transparent recommendation algorithms, seat locking, digital QR passes, and instant showtime access.
          </p>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
            <Link to="/movies" className="btn-primary" style={{ padding: '0.85rem 1.75rem', fontSize: '1rem', borderRadius: '12px' }}>
              <span>Browse All Movies</span>
              <ArrowRight size={18} />
            </Link>

            <button
              onClick={() => setShowGenreSelector(!showGenreSelector)}
              className="btn-secondary"
              style={{
                padding: '0.85rem 1.5rem',
                fontSize: '0.95rem',
                borderRadius: '12px',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem'
              }}
            >
              <Settings2 size={16} />
              <span>{showGenreSelector ? 'Hide Preferences' : 'Tune Preferences'}</span>
            </button>
          </div>
        </div>
      </section>

      {/* Genre Preference Tuning Bar (Interactive) */}
      {showGenreSelector && (
        <section
          className="glass-panel"
          style={{
            padding: '1.5rem 2rem',
            marginBottom: '3rem',
            borderRadius: '16px',
            border: '1px solid rgba(229, 9, 20, 0.3)',
            background: 'linear-gradient(180deg, rgba(229, 9, 20, 0.08) 0%, rgba(15, 20, 32, 0.9) 100%)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <h3 style={{ fontSize: '1.15rem', color: '#fff', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Sparkles size={18} color="var(--accent-red)" />
                <span>Personalize Your Movie Taste</span>
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '4px 0 0' }}>
                Select your favorite genres. Recommendations adapt dynamically to your taste profile.
              </p>
            </div>
            {savingPrefs && (
              <span style={{ fontSize: '0.85rem', color: 'var(--accent-gold)' }}>
                Updating recommendations in real-time...
              </span>
            )}
          </div>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.6rem' }}>
            {availableGenres.map((genre) => {
              const isSelected = activePreferences.includes(genre);
              return (
                <button
                  key={genre}
                  onClick={() => toggleGenrePreference(genre)}
                  style={{
                    padding: '0.5rem 1.1rem',
                    borderRadius: '999px',
                    fontSize: '0.85rem',
                    fontWeight: 600,
                    cursor: 'pointer',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '6px',
                    border: isSelected ? '1px solid var(--accent-red)' : '1px solid rgba(255, 255, 255, 0.12)',
                    background: isSelected ? 'rgba(229, 9, 20, 0.25)' : 'rgba(255, 255, 255, 0.04)',
                    color: isSelected ? '#FFFFFF' : 'var(--text-secondary)',
                    transition: 'all 0.2s ease',
                    boxShadow: isSelected ? '0 0 12px rgba(229, 9, 20, 0.3)' : 'none'
                  }}
                >
                  {isSelected && <Check size={14} color="var(--accent-red)" />}
                  <span>{genre}</span>
                </button>
              );
            })}
          </div>
        </section>
      )}

      {/* 3 Architecture Pillars */}
      <section style={{ marginBottom: '3.5rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
          <div className="glass-panel" style={{ padding: '1.75rem', borderRadius: '16px' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(245, 158, 11, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <Users size={22} color="var(--accent-gold)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Smart Group Seating</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Intelligent multi-criteria recommendation algorithm finding optimal contiguous and split-row arrangements for groups of 2 to 10.
            </p>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem', borderRadius: '16px' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(6, 182, 212, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <ShieldCheck size={22} color="var(--accent-cyan)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Booking Recovery Engine</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Zero-lost-transaction state machine that safely manages payment dropouts with active background status reconciliation.
            </p>
          </div>

          <div className="glass-panel" style={{ padding: '1.75rem', borderRadius: '16px' }}>
            <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '1rem' }}>
              <TicketCheck size={22} color="var(--accent-emerald)" />
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fff' }}>Fair Seat Release & Waitlist</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              FIFO priority queuing with exclusive 10-minute claim reservation windows when sold-out seats become available.
            </p>
          </div>
        </div>
      </section>

      {/* SECTION 1: RECOMMENDED FOR YOU */}
      {recSections.recommendedForYou && recSections.recommendedForYou.length > 0 && (
        <section style={{ marginBottom: '3.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Sparkles size={24} color="var(--accent-red)" />
                <h2 style={{ fontSize: '1.8rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  Recommended For You
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                {recSections.isPersonalized
                  ? 'Ranked using your genre affinity, ratings, and ticket purchase habits'
                  : 'Top blockbuster picks curated by popularity and audience ratings'}
              </p>
            </div>
            <span style={{
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              padding: '4px 12px',
              borderRadius: '999px',
              fontSize: '0.8rem',
              color: 'var(--text-secondary)'
            }}>
              {recSections.recommendedForYou.length} recommendations
            </span>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.recommendedForYou.map((rec) => (
              <RecommendationCard key={`rec-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* SECTION 2: BECAUSE YOU WATCHED SIMILAR MOVIES */}
      {recSections.becauseYouWatched && recSections.becauseYouWatched.recommendations && recSections.becauseYouWatched.recommendations.length > 0 && (
        <section style={{
          marginBottom: '3.5rem',
          padding: '2rem',
          borderRadius: '20px',
          background: 'linear-gradient(135deg, rgba(30, 41, 59, 0.5) 0%, rgba(15, 20, 32, 0.8) 100%)',
          border: '1px solid rgba(255, 255, 255, 0.08)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Film size={24} color="var(--accent-gold)" />
                <h2 style={{ fontSize: '1.7rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  Because You Watched <span style={{ color: 'var(--accent-gold)' }}>{recSections.becauseYouWatched.sourceMovie.title}</span>
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                Patrons who enjoyed {recSections.becauseYouWatched.sourceMovie.genre} also reserved seats for these titles
              </p>
            </div>
            <Link
              to={`/movies/${recSections.becauseYouWatched.sourceMovie.id}`}
              style={{
                color: 'var(--accent-gold)',
                fontSize: '0.85rem',
                fontWeight: 600,
                textDecoration: 'none',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <span>View Source Movie</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.becauseYouWatched.recommendations.map((rec) => (
              <RecommendationCard key={`byw-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* SECTION 3: TRENDING MOVIES */}
      {recSections.trendingMovies && recSections.trendingMovies.length > 0 && (
        <section style={{ marginBottom: '3.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Flame size={24} color="#EF4444" />
                <h2 style={{ fontSize: '1.8rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  Trending Movies
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                Fastest-filling screenings and highest ticket booking velocity this week
              </p>
            </div>
            <span className="badge badge-red" style={{ fontSize: '0.75rem' }}>
              🔥 Box Office Heat
            </span>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.trendingMovies.map((rec) => (
              <RecommendationCard key={`trend-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* SECTION 4: POPULAR MOVIES */}
      {recSections.popularMovies && recSections.popularMovies.length > 0 && (
        <section style={{ marginBottom: '3.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Star size={24} color="var(--accent-gold)" />
                <h2 style={{ fontSize: '1.8rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  Popular Movies
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                All-time audience favorites with the highest total ticket reservation volume
              </p>
            </div>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.popularMovies.map((rec) => (
              <RecommendationCard key={`pop-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* SECTION 5: NEW RELEASES */}
      {recSections.newReleases && recSections.newReleases.length > 0 && (
        <section style={{ marginBottom: '3.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Film size={24} color="var(--accent-cyan)" />
                <h2 style={{ fontSize: '1.8rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  New Releases
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                Recently added titles hitting CineSmart screens now
              </p>
            </div>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.newReleases.map((rec) => (
              <RecommendationCard key={`new-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* SECTION 6: TOP RATED MOVIES */}
      {recSections.topRated && recSections.topRated.length > 0 && (
        <section style={{ marginBottom: '3.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                <Award size={24} color="#A78BFA" />
                <h2 style={{ fontSize: '1.8rem', color: '#fff', fontWeight: 800, margin: 0 }}>
                  Top Rated Movies
                </h2>
              </div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', margin: 0 }}>
                Critically acclaimed blockbusters with top reviews from moviegoers
              </p>
            </div>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
            gap: '1.5rem'
          }}>
            {recSections.topRated.map((rec) => (
              <RecommendationCard key={`top-${rec.movie.id}`} recommendation={rec} />
            ))}
          </div>
        </section>
      )}

      {/* All Movies Catalog Grid Section */}
      <section>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.75rem' }}>
          <div>
            <h2 style={{ fontSize: '1.8rem', color: '#fff', marginBottom: '0.25rem', fontWeight: 800 }}>Now Showing Catalog</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>Explore the complete library of movies running across all screens</p>
          </div>
          <Link to="/movies" style={{ color: 'var(--accent-red)', fontWeight: 600, fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <span>View All</span>
            <ArrowRight size={16} />
          </Link>
        </div>

        {loading ? (
          <LoadingSpinner message="Fetching currently screening movies..." />
        ) : error ? (
          <ErrorMessage message={error} onRetry={() => window.location.reload()} />
        ) : (
          <MovieGrid movies={movies} />
        )}
      </section>
    </div>
  );
}
