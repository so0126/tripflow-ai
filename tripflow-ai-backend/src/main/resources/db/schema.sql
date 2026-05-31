CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS payment_transactions, hotel_bookings, hotel_rate_plan_prices, hotel_rate_plans, hotel_rooms, hotels CASCADE;
DROP TABLE IF EXISTS chat_memory_vectors, chat_memories, checklist_items, checklists CASCADE;
DROP TABLE IF EXISTS review_jobs CASCADE;
DROP TABLE IF EXISTS ai_review_hashtags, ai_review_styles, ai_review_analysis CASCADE;
DROP TABLE IF EXISTS review_hashtags, review_hashtag_groups, review_photos, review_photo_groups, review_posts CASCADE;
DROP TABLE IF EXISTS image_search_candidates, image_search_sessions, image_places CASCADE;
DROP TABLE IF EXISTS toilets, travel_places, travel_place_categories CASCADE;
DROP TABLE IF EXISTS plan_snapshots, current_activities, plan_places, plan_days, plans CASCADE;
DROP TABLE IF EXISTS sns_tokens, user_identities, users CASCADE;

DROP TYPE IF EXISTS checklist_category_enum CASCADE;
DROP TYPE IF EXISTS image_action_type_enum CASCADE;
DROP TYPE IF EXISTS image_status_enum CASCADE;
DROP TYPE IF EXISTS travel_type_enum CASCADE;

CREATE TYPE checklist_category_enum AS ENUM ('LOCATION', 'GENERAL', 'WEATHER');
CREATE TYPE image_action_type_enum AS ENUM ('SAVE_ONLY', 'EDIT_ITINERARY', 'REPLACE');
CREATE TYPE image_status_enum AS ENUM ('PENDING', 'READY', 'FAILED');
CREATE TYPE travel_type_enum AS ENUM ('SOLO', 'GROUP', 'UNCLEAR');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMPTZ,
    name VARCHAR(50),
    email VARCHAR(1000) UNIQUE,
    profile_image_url VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE user_identities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, provider)
);

CREATE TABLE sns_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_access_token TEXT NOT NULL,
    page_access_token TEXT,
    expires_at TIMESTAMPTZ,
    account_type VARCHAR(50),
    ig_business_account VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE travel_place_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name TEXT NOT NULL,
    parent_code VARCHAR(50) REFERENCES travel_place_categories(code),
    level INTEGER NOT NULL,
    category_type TEXT
);

CREATE TABLE travel_places (
    id BIGSERIAL PRIMARY KEY,
    content_id TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    address TEXT,
    tel TEXT,
    zone_id VARCHAR(20),
    first_image TEXT,
    first_image2 TEXT,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    category_code VARCHAR(50) NOT NULL REFERENCES travel_place_categories(code),
    description TEXT,
    tags JSONB,
    embedding VECTOR(3072),
    detail_info TEXT,
    normalized_category VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(100),
    budget NUMERIC(19,2) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_ended BOOLEAN NOT NULL DEFAULT false,
    embedding VECTOR(3072),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE plan_days (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    day_index SMALLINT NOT NULL,
    title VARCHAR(50),
    plan_date DATE,
    embedding VECTOR(3072),
    UNIQUE (plan_id, day_index)
);

CREATE TABLE plan_places (
    id BIGSERIAL PRIMARY KEY,
    day_id BIGINT NOT NULL REFERENCES plan_days(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    place_name VARCHAR(64),
    address VARCHAR(200),
    first_image TEXT,
    first_image2 TEXT,
    lat NUMERIC(10,7),
    lng NUMERIC(10,7),
    expected_cost NUMERIC(19,2),
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    normalized_category VARCHAR(50),
    is_ended BOOLEAN NOT NULL DEFAULT false,
    embedding VECTOR(3072)
);

CREATE TABLE current_activities (
    id BIGSERIAL PRIMARY KEY,
    plan_place_id BIGINT NOT NULL UNIQUE REFERENCES plan_places(id) ON DELETE CASCADE,
    actual_cost NUMERIC(19,2),
    memo TEXT,
    memo_embedding VECTOR(3072),
    ended_at TIMESTAMPTZ
);

CREATE TABLE plan_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    version_no INTEGER NOT NULL,
    snapshot_json JSONB NOT NULL,
    embedding VECTOR(3072),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, version_no)
);

CREATE TABLE hotels (
    id BIGSERIAL PRIMARY KEY,
    external_hotel_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    hotel_name_local VARCHAR(200),
    district VARCHAR(100),
    neighborhood VARCHAR(100),
    star_rating NUMERIC(2,1),
    rating_score NUMERIC(3,2),
    review_count INTEGER NOT NULL DEFAULT 0,
    address_line1 VARCHAR(200),
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    has_free_wifi BOOLEAN NOT NULL DEFAULT false,
    has_parking BOOLEAN NOT NULL DEFAULT false,
    is_pet_friendly BOOLEAN NOT NULL DEFAULT false,
    is_family_friendly BOOLEAN NOT NULL DEFAULT false,
    has_24h_frontdesk BOOLEAN NOT NULL DEFAULT false,
    has_english_staff BOOLEAN NOT NULL DEFAULT false,
    near_metro BOOLEAN NOT NULL DEFAULT false,
    metro_station_name VARCHAR(100),
    airport_distance_km NUMERIC(6,2),
    has_breakfast_restaurant BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE hotel_rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    external_room_type_id VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    bed_type VARCHAR(100),
    room_size_sqm NUMERIC(6,2),
    max_occupancy INTEGER NOT NULL,
    has_kitchenette BOOLEAN NOT NULL DEFAULT false,
    is_accessible_room BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    UNIQUE (hotel_id, external_room_type_id)
);

CREATE TABLE hotel_rate_plans (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id) ON DELETE CASCADE,
    room_type_id BIGINT NOT NULL REFERENCES hotel_rooms(id) ON DELETE CASCADE,
    external_rate_plan_id VARCHAR(100) NOT NULL,
    name VARCHAR(200),
    meal_plan VARCHAR(100),
    includes_breakfast BOOLEAN NOT NULL DEFAULT false,
    currency CHAR(3) NOT NULL,
    base_price NUMERIC(12,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    UNIQUE (hotel_id, external_rate_plan_id)
);

CREATE TABLE hotel_rate_plan_prices (
    id BIGSERIAL PRIMARY KEY,
    rate_plan_id BIGINT NOT NULL REFERENCES hotel_rate_plans(id) ON DELETE CASCADE,
    stay_date DATE NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    fee_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    is_closed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    UNIQUE (rate_plan_id, stay_date)
);

CREATE TABLE hotel_bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    external_booking_id VARCHAR(100),
    hotel_id BIGINT NOT NULL REFERENCES hotels(id),
    room_type_id BIGINT NOT NULL REFERENCES hotel_rooms(id),
    rate_plan_id BIGINT NOT NULL REFERENCES hotel_rate_plans(id),
    checkin_date TIMESTAMPTZ NOT NULL,
    checkout_date TIMESTAMPTZ NOT NULL,
    nights INTEGER,
    adults_count INTEGER,
    children_count INTEGER,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    total_price NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    fee_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    guest_name VARCHAR(100),
    guest_email VARCHAR(255),
    guest_phone VARCHAR(50),
    provider_booking_meta JSONB,
    booked_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    hotel_booking_id BIGINT NOT NULL REFERENCES hotel_bookings(id) ON DELETE CASCADE,
    payment_method VARCHAR(50),
    provider_payment_id VARCHAR(100),
    amount NUMERIC(12,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'KRW',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    raw_response TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE review_posts (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    caption TEXT,
    is_posted BOOLEAN NOT NULL DEFAULT false,
    review_post_url TEXT,
    photo_group_id BIGINT,
    hashtag_group_id BIGINT,
    review_style_id BIGINT,
    travel_type travel_type_enum,
    overall_moods TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_photo_groups (
    id BIGSERIAL PRIMARY KEY,
    review_post_id BIGINT NOT NULL REFERENCES review_posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_photos (
    id BIGSERIAL PRIMARY KEY,
    photo_group_id BIGINT NOT NULL REFERENCES review_photo_groups(id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    order_index INTEGER NOT NULL DEFAULT 0,
    summary TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_jobs (
    id BIGSERIAL PRIMARY KEY,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_post_id BIGINT REFERENCES review_posts(id) ON DELETE CASCADE,
    photo_group_id BIGINT REFERENCES review_photo_groups(id) ON DELETE CASCADE,
    photo_id BIGINT REFERENCES review_photos(id) ON DELETE CASCADE,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT review_jobs_exactly_one_target_chk CHECK (
        (CASE WHEN review_post_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN photo_group_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN photo_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    ),
    CONSTRAINT review_jobs_job_type_chk CHECK (
        job_type IN ('PHOTO_ANALYSIS', 'TRIP_CONTEXT_ANALYSIS', 'STYLE_GENERATION')
    ),
    CONSTRAINT review_jobs_status_chk CHECK (
        status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')
    )
);

CREATE TABLE review_hashtag_groups (
    id BIGSERIAL PRIMARY KEY,
    review_post_id BIGINT NOT NULL REFERENCES review_posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE review_hashtags (
    id BIGSERIAL PRIMARY KEY,
    hashtag_group_id BIGINT NOT NULL REFERENCES review_hashtag_groups(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai_review_analysis (
    id BIGSERIAL PRIMARY KEY,
    review_post_id BIGINT NOT NULL REFERENCES review_posts(id) ON DELETE CASCADE,
    input_json JSONB,
    output_json JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai_review_styles (
    id BIGSERIAL PRIMARY KEY,
    review_analysis_id BIGINT NOT NULL REFERENCES ai_review_analysis(id) ON DELETE CASCADE,
    name VARCHAR(100),
    tone_code VARCHAR(50),
    caption TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ai_review_hashtags (
    id BIGSERIAL PRIMARY KEY,
    review_style_id BIGINT NOT NULL REFERENCES ai_review_styles(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE review_posts
    ADD CONSTRAINT review_posts_photo_group_fk FOREIGN KEY (photo_group_id) REFERENCES review_photo_groups(id),
    ADD CONSTRAINT review_posts_hashtag_group_fk FOREIGN KEY (hashtag_group_id) REFERENCES review_hashtag_groups(id),
    ADD CONSTRAINT review_posts_review_style_fk FOREIGN KEY (review_style_id) REFERENCES ai_review_styles(id);

CREATE TABLE checklists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_index SMALLINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE checklist_items (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    category checklist_category_enum NOT NULL,
    is_checked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chat_memories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(10) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chat_memory_vectors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding VECTOR(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE image_places (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    content TEXT,
    description TEXT,
    lat NUMERIC(10,7),
    lng NUMERIC(10,7),
    address VARCHAR(500),
    place_type VARCHAR(50),
    internal_original_url TEXT,
    internal_thumbnail_url TEXT,
    external_image_url TEXT,
    image_status image_status_enum NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ
);

CREATE TABLE image_search_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type image_action_type_enum NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE image_search_candidates (
    id BIGSERIAL PRIMARY KEY,
    image_search_session_id BIGINT NOT NULL REFERENCES image_search_sessions(id) ON DELETE CASCADE,
    image_place_id BIGINT NOT NULL REFERENCES image_places(id) ON DELETE CASCADE,
    is_selected BOOLEAN NOT NULL DEFAULT false,
    rank INTEGER NOT NULL,
    UNIQUE (image_search_session_id, image_place_id)
);

CREATE TABLE toilets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    lat NUMERIC(10,7) NOT NULL,
    lng NUMERIC(10,7) NOT NULL,
    address VARCHAR(500)
);

CREATE INDEX idx_travel_places_zone_id ON travel_places(zone_id);
CREATE INDEX idx_travel_places_normalized_category ON travel_places(normalized_category);
CREATE INDEX idx_plan_days_plan_id ON plan_days(plan_id);
CREATE INDEX idx_plan_places_day_id ON plan_places(day_id);
CREATE INDEX idx_plan_snapshots_user_version ON plan_snapshots(user_id, version_no DESC);
CREATE INDEX idx_checklists_user_id ON checklists(user_id);
CREATE INDEX idx_image_search_sessions_user_id ON image_search_sessions(user_id);
CREATE INDEX idx_hotel_bookings_user_id ON hotel_bookings(user_id);
CREATE INDEX idx_hotel_prices_date ON hotel_rate_plan_prices(stay_date);
CREATE INDEX idx_chat_memory_vectors_embedding ON chat_memory_vectors USING hnsw (embedding vector_cosine_ops);
