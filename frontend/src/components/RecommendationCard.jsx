import React from 'react';
import { Link } from 'react-router-dom';
import { Clock, Globe, Film, Star, Sparkles, TrendingUp, Award, Heart } from 'lucide-react';

export function RecommendationCard({ recommendation, compact = false }) {
  if (!recommendation || !recommendation.movie) return null;

  const { movie, matchPercentage, recommendationReason, matchHighlight, category } = recommendation;
  const posterUrl = movie.posterUrl || 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&q=80';

  // Choose badge color based on match percentage
  const isHighMatch = matchPercentage >= 90;
  const matchColor = isHighMatch ? '#10B981' : matchPercentage >= 80 ? '#3B82F6' : '#F59E0B';
  const matchBg = isHighMatch
    ? 'rgba(16, 185, 129, 0.2)'
    : matchPercentage >= 80
    ? 'rgba(59, 130, 246, 0.2)'
    : 'rgba(245, 158, 11, 0.2)';

  return (
    <div
      className="glass-panel"
      style={{
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        background: 'linear-gradient(180deg, rgba(26, 32, 48, 0.6) 0%, rgba(15, 20, 32, 0.8) 100%)',
        transition: 'transform 0.28s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.28s ease, border-color 0.28s ease',
        position: 'relative',
        boxShadow: '0 8px 24px rgba(0, 0, 0, 0.35)'
      }}
      onMouseOver={(e) => {
        e.currentTarget.style.transform = 'translateY(-6px)';
        e.currentTarget.style.borderColor = 'rgba(229, 9, 20, 0.5)';
        e.currentTarget.style.boxShadow = '0 16px 36px rgba(229, 9, 20, 0.18)';
      }}
      onMouseOut={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.08)';
        e.currentTarget.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.35)';
      }}
    >
      {/* Poster Media Box */}
      <div style={{ position: 'relative', width: '100%', paddingTop: compact ? '110%' : '135%', overflow: 'hidden', background: '#080c14' }}>
        <img
          src={posterUrl}
          alt={movie.title}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            transition: 'transform 0.45s ease'
          }}
          onMouseOver={(e) => (e.currentTarget.style.transform = 'scale(1.06)')}
          onMouseOut={(e) => (e.currentTarget.style.transform = 'scale(1.0)')}
        />

        {/* Top Badges Overlay */}
        <div style={{
          position: 'absolute',
          top: '10px',
          left: '10px',
          right: '10px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '6px',
          zIndex: 2
        }}>
          {/* Match Score Badge */}
          {matchPercentage > 0 && (
            <div
              style={{
                background: matchBg,
                backdropFilter: 'blur(8px)',
                border: `1px solid ${matchColor}`,
                color: matchColor,
                padding: '4px 10px',
                borderRadius: '999px',
                fontSize: '0.8rem',
                fontWeight: 800,
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                boxShadow: '0 4px 12px rgba(0,0,0,0.5)'
              }}
            >
              <Sparkles size={12} />
              <span>{matchPercentage}% Match</span>
            </div>
          )}

          {/* Rating Badge */}
          <div
            style={{
              background: 'rgba(0, 0, 0, 0.75)',
              backdropFilter: 'blur(8px)',
              border: '1px solid rgba(255, 215, 0, 0.4)',
              color: '#FFD700',
              padding: '4px 8px',
              borderRadius: '8px',
              fontSize: '0.8rem',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '4px'
            }}
          >
            <Star size={12} fill="#FFD700" />
            <span>{movie.rating ? Number(movie.rating).toFixed(1) : '8.5'}</span>
          </div>
        </div>

        {/* Bottom Poster Genre Pill */}
        <div style={{ position: 'absolute', bottom: '10px', left: '10px', zIndex: 2 }}>
          <span
            style={{
              background: 'rgba(15, 23, 42, 0.85)',
              backdropFilter: 'blur(8px)',
              border: '1px solid rgba(56, 189, 248, 0.4)',
              color: '#38BDF8',
              padding: '3px 9px',
              borderRadius: '999px',
              fontSize: '0.75rem',
              fontWeight: 700,
              textTransform: 'uppercase',
              letterSpacing: '0.04em'
            }}
          >
            {movie.genre}
          </span>
        </div>
      </div>

      {/* Card Content */}
      <div style={{ padding: '1.1rem', display: 'flex', flexDirection: 'column', flex: 1, gap: '0.5rem' }}>
        <h3
          style={{
            fontSize: '1.1rem',
            fontWeight: 700,
            color: '#FFFFFF',
            margin: 0,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap'
          }}
          title={movie.title}
        >
          {movie.title}
        </h3>

        {/* Meta row */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.8rem',
          color: 'var(--text-secondary)',
          fontSize: '0.8rem'
        }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Clock size={13} />
            {movie.durationMinutes}m
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
            <Globe size={13} />
            {movie.language}
          </span>
          <span style={{
            marginLeft: 'auto',
            background: 'rgba(255,255,255,0.06)',
            padding: '2px 6px',
            borderRadius: '4px',
            fontSize: '0.7rem'
          }}>
            {movie.ageRating}
          </span>
        </div>

        {/* Transparent Recommendation Reason */}
        {recommendationReason && (
          <div
            style={{
              background: 'rgba(229, 9, 20, 0.08)',
              borderLeft: '3px solid var(--accent-red)',
              borderRadius: '0 8px 8px 0',
              padding: '6px 10px',
              fontSize: '0.75rem',
              color: '#E2E8F0',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '6px',
              margin: '0.3rem 0',
              lineHeight: 1.35
            }}
          >
            <Sparkles size={13} color="var(--accent-red)" style={{ flexShrink: 0, marginTop: '2px' }} />
            <span>{recommendationReason}</span>
          </div>
        )}

        {/* Action Button */}
        <Link
          to={`/movies/${movie.id}`}
          className="btn-primary"
          style={{
            marginTop: 'auto',
            width: '100%',
            padding: '0.6rem 0.9rem',
            fontSize: '0.85rem',
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '0.4rem',
            borderRadius: '10px'
          }}
        >
          <Film size={15} />
          <span>Book Tickets</span>
        </Link>
      </div>
    </div>
  );
}
