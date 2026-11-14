-- ============================================================================
-- CINESMART PHASE 5: PERSONALIZED MOVIE RECOMMENDATION SYSTEM
-- Database Schema Migration & Seed Updates
-- ============================================================================

-- 1. Add Rating, Release Date, and Total Reviews to movies
ALTER TABLE movies ADD COLUMN IF NOT EXISTS rating NUMERIC(3, 1) DEFAULT 8.0;
ALTER TABLE movies ADD COLUMN IF NOT EXISTS release_date DATE DEFAULT CURRENT_DATE;
ALTER TABLE movies ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;

-- 2. Add Preferred Genres to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_genres VARCHAR(200);

-- 3. Populate realistic ratings, release dates, and review counts for movies
UPDATE movies SET rating = 8.9, release_date = '2026-09-15', total_reviews = 1420 WHERE id = 1; -- Interstellar Odyssey (Sci-Fi)
UPDATE movies SET rating = 8.7, release_date = '2026-10-01', total_reviews = 980 WHERE id = 2;  -- Dune: The Desert Prophecy (Sci-Fi)
UPDATE movies SET rating = 9.1, release_date = '2026-08-20', total_reviews = 2850 WHERE id = 3; -- The Dark Knight Returns (Action)
UPDATE movies SET rating = 8.2, release_date = '2026-10-25', total_reviews = 640 WHERE id = 4;  -- Cyberpunk: Neon Horizon (Action)
UPDATE movies SET rating = 8.6, release_date = '2026-11-05', total_reviews = 1120 WHERE id = 5; -- Spider-Man: Multiverse Rift (Animation)

-- Fallback for any other movies
UPDATE movies SET rating = 8.0 WHERE rating IS NULL;
UPDATE movies SET release_date = CURRENT_DATE WHERE release_date IS NULL;
UPDATE movies SET total_reviews = 100 WHERE total_reviews IS NULL;

-- 4. Set preferred genres for seeded users
UPDATE users SET preferred_genres = 'Sci-Fi,Action' WHERE id = 4; -- Naveen
UPDATE users SET preferred_genres = 'Action,Animation' WHERE id = 3; -- Jane Customer

-- 5. Add Performance Indexes
CREATE INDEX IF NOT EXISTS idx_movies_rating ON movies(rating DESC);
CREATE INDEX IF NOT EXISTS idx_movies_release_date ON movies(release_date DESC);
CREATE INDEX IF NOT EXISTS idx_movies_genre ON movies(genre);
CREATE INDEX IF NOT EXISTS idx_movies_active ON movies(is_active);
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at);
