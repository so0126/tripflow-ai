-- 로컬 개발용 호텔 더미데이터입니다.
-- db/schema.sql 실행 후 적용하세요.

INSERT INTO hotels (
    external_hotel_id,
    name,
    hotel_name_local,
    district,
    neighborhood,
    star_rating,
    rating_score,
    review_count,
    address_line1,
    latitude,
    longitude,
    has_free_wifi,
    has_parking,
    is_pet_friendly,
    is_family_friendly,
    has_24h_frontdesk,
    has_english_staff,
    near_metro,
    metro_station_name,
    airport_distance_km,
    has_breakfast_restaurant,
    is_active,
    updated_at
)
VALUES
    (
        'SEOUL-MYEONGDONG-001',
        '트립플로우 명동 스테이',
        '트립플로우 명동 스테이',
        '중구',
        '명동',
        4.0,
        4.55,
        1284,
        '서울특별시 중구 명동길 58',
        37.5636870,
        126.9835590,
        true,
        false,
        false,
        true,
        true,
        true,
        true,
        '명동역',
        48.20,
        true,
        true,
        now()
    ),
    (
        'SEOUL-HONGDAE-002',
        '홍대 크리에이티브 호텔',
        '홍대 크리에이티브 호텔',
        '마포구',
        '홍대',
        3.5,
        4.38,
        876,
        '서울특별시 마포구 양화로 152',
        37.5562720,
        126.9237760,
        true,
        false,
        true,
        false,
        true,
        true,
        true,
        '홍대입구역',
        51.80,
        false,
        true,
        now()
    ),
    (
        'SEOUL-GANGNAM-003',
        '강남 시티 비즈니스 호텔',
        '강남 시티 비즈니스 호텔',
        '강남구',
        '역삼동',
        4.0,
        4.47,
        1019,
        '서울특별시 강남구 테헤란로26길 12',
        37.4999230,
        127.0364560,
        true,
        true,
        false,
        false,
        true,
        true,
        true,
        '역삼역',
        63.50,
        true,
        true,
        now()
    ),
    (
        'SEOUL-JONGNO-004',
        '종로 팰리스 부티크',
        '종로 팰리스 부티크',
        '종로구',
        '인사동',
        4.5,
        4.72,
        642,
        '서울특별시 종로구 인사동길 38',
        37.5729270,
        126.9860820,
        true,
        true,
        false,
        true,
        true,
        true,
        true,
        '안국역',
        49.60,
        true,
        true,
        now()
    ),
    (
        'SEOUL-YONGSAN-005',
        '용산 리버 레지던스',
        '용산 리버 레지던스',
        '용산구',
        '이촌동',
        4.0,
        4.31,
        531,
        '서울특별시 용산구 이촌로 95',
        37.5228570,
        126.9704140,
        true,
        true,
        true,
        true,
        true,
        false,
        true,
        '이촌역',
        54.40,
        false,
        true,
        now()
    ),
    (
        'SEOUL-JAMSIL-006',
        '잠실 레이크 패밀리 호텔',
        '잠실 레이크 패밀리 호텔',
        '송파구',
        '잠실동',
        3.5,
        4.22,
        714,
        '서울특별시 송파구 올림픽로 240',
        37.5110970,
        127.0981670,
        true,
        true,
        false,
        true,
        true,
        false,
        true,
        '잠실역',
        72.10,
        true,
        true,
        now()
    )
ON CONFLICT (external_hotel_id) DO UPDATE SET
    name = EXCLUDED.name,
    hotel_name_local = EXCLUDED.hotel_name_local,
    district = EXCLUDED.district,
    neighborhood = EXCLUDED.neighborhood,
    star_rating = EXCLUDED.star_rating,
    rating_score = EXCLUDED.rating_score,
    review_count = EXCLUDED.review_count,
    address_line1 = EXCLUDED.address_line1,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    has_free_wifi = EXCLUDED.has_free_wifi,
    has_parking = EXCLUDED.has_parking,
    is_pet_friendly = EXCLUDED.is_pet_friendly,
    is_family_friendly = EXCLUDED.is_family_friendly,
    has_24h_frontdesk = EXCLUDED.has_24h_frontdesk,
    has_english_staff = EXCLUDED.has_english_staff,
    near_metro = EXCLUDED.near_metro,
    metro_station_name = EXCLUDED.metro_station_name,
    airport_distance_km = EXCLUDED.airport_distance_km,
    has_breakfast_restaurant = EXCLUDED.has_breakfast_restaurant,
    is_active = EXCLUDED.is_active,
    updated_at = now();

WITH room_seed (
    external_hotel_id,
    external_room_type_id,
    name,
    bed_type,
    room_size_sqm,
    max_occupancy,
    has_kitchenette,
    is_accessible_room
) AS (
    VALUES
        ('SEOUL-MYEONGDONG-001', 'BASE-ROOM', '스탠다드 객실', '더블 침대 1개', 24.00, 2, false, false),
        ('SEOUL-HONGDAE-002', 'BASE-ROOM', '스탠다드 객실', '퀸 침대 1개', 22.00, 2, false, false),
        ('SEOUL-GANGNAM-003', 'BASE-ROOM', '스탠다드 객실', '퀸 침대 1개', 26.00, 2, false, false),
        ('SEOUL-JONGNO-004', 'BASE-ROOM', '스탠다드 객실', '퀸 침대 1개', 28.00, 2, false, false),
        ('SEOUL-YONGSAN-005', 'BASE-ROOM', '스탠다드 객실', '퀸 침대 1개', 32.00, 2, true, false),
        ('SEOUL-JAMSIL-006', 'BASE-ROOM', '스탠다드 객실', '더블 침대 1개', 25.00, 2, false, false)
)
INSERT INTO hotel_rooms (
    hotel_id,
    external_room_type_id,
    name,
    bed_type,
    room_size_sqm,
    max_occupancy,
    has_kitchenette,
    is_accessible_room,
    updated_at
)
SELECT
    h.id,
    rs.external_room_type_id,
    rs.name,
    rs.bed_type,
    rs.room_size_sqm,
    rs.max_occupancy,
    rs.has_kitchenette,
    rs.is_accessible_room,
    now()
FROM room_seed rs
JOIN hotels h ON h.external_hotel_id = rs.external_hotel_id
ON CONFLICT (hotel_id, external_room_type_id) DO UPDATE SET
    name = EXCLUDED.name,
    bed_type = EXCLUDED.bed_type,
    room_size_sqm = EXCLUDED.room_size_sqm,
    max_occupancy = EXCLUDED.max_occupancy,
    has_kitchenette = EXCLUDED.has_kitchenette,
    is_accessible_room = EXCLUDED.is_accessible_room,
    updated_at = now();

WITH rate_plan_seed (
    external_hotel_id,
    external_room_type_id,
    external_rate_plan_id,
    name,
    meal_plan,
    includes_breakfast,
    currency,
    base_price
) AS (
    VALUES
        ('SEOUL-MYEONGDONG-001', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'ROOM_ONLY', false, 'KRW', 128000.00),
        ('SEOUL-HONGDAE-002', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'ROOM_ONLY', false, 'KRW', 98000.00),
        ('SEOUL-GANGNAM-003', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'BREAKFAST', true, 'KRW', 158000.00),
        ('SEOUL-JONGNO-004', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'BREAKFAST', true, 'KRW', 196000.00),
        ('SEOUL-YONGSAN-005', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'ROOM_ONLY', false, 'KRW', 146000.00),
        ('SEOUL-JAMSIL-006', 'BASE-ROOM', 'BASE-RATE', '기본 숙박 요금', 'BREAKFAST', true, 'KRW', 118000.00)
)
INSERT INTO hotel_rate_plans (
    hotel_id,
    room_type_id,
    external_rate_plan_id,
    name,
    meal_plan,
    includes_breakfast,
    currency,
    base_price,
    updated_at
)
SELECT
    h.id,
    r.id,
    rps.external_rate_plan_id,
    rps.name,
    rps.meal_plan,
    rps.includes_breakfast,
    rps.currency,
    rps.base_price,
    now()
FROM rate_plan_seed rps
JOIN hotels h ON h.external_hotel_id = rps.external_hotel_id
JOIN hotel_rooms r
    ON r.hotel_id = h.id
   AND r.external_room_type_id = rps.external_room_type_id
ON CONFLICT (hotel_id, external_rate_plan_id) DO UPDATE SET
    room_type_id = EXCLUDED.room_type_id,
    name = EXCLUDED.name,
    meal_plan = EXCLUDED.meal_plan,
    includes_breakfast = EXCLUDED.includes_breakfast,
    currency = EXCLUDED.currency,
    base_price = EXCLUDED.base_price,
    updated_at = now();

WITH price_seed (
    external_hotel_id,
    external_rate_plan_id,
    weekday_price,
    weekend_price
) AS (
    VALUES
        ('SEOUL-MYEONGDONG-001', 'BASE-RATE', 128000.00, 154000.00),
        ('SEOUL-HONGDAE-002', 'BASE-RATE', 98000.00, 126000.00),
        ('SEOUL-GANGNAM-003', 'BASE-RATE', 158000.00, 188000.00),
        ('SEOUL-JONGNO-004', 'BASE-RATE', 196000.00, 238000.00),
        ('SEOUL-YONGSAN-005', 'BASE-RATE', 146000.00, 174000.00),
        ('SEOUL-JAMSIL-006', 'BASE-RATE', 118000.00, 144000.00)
),
generated_prices AS (
    SELECT
        p.id AS rate_plan_id,
        gs.stay_date::date AS stay_date,
        CASE
            WHEN EXTRACT(ISODOW FROM gs.stay_date) IN (5, 6) THEN ps.weekend_price
            ELSE ps.weekday_price
        END AS price
    FROM price_seed ps
    JOIN hotels h ON h.external_hotel_id = ps.external_hotel_id
    JOIN hotel_rate_plans p
        ON p.hotel_id = h.id
       AND p.external_rate_plan_id = ps.external_rate_plan_id
    CROSS JOIN generate_series(
        CURRENT_DATE,
        CURRENT_DATE + INTERVAL '45 days',
        INTERVAL '1 day'
    ) AS gs(stay_date)
)
INSERT INTO hotel_rate_plan_prices (
    rate_plan_id,
    stay_date,
    price,
    tax_amount,
    fee_amount,
    is_closed,
    updated_at
)
SELECT
    gp.rate_plan_id,
    gp.stay_date,
    gp.price,
    ROUND(gp.price * 0.10, 2),
    3000.00,
    false,
    now()
FROM generated_prices gp
ON CONFLICT (rate_plan_id, stay_date) DO UPDATE SET
    price = EXCLUDED.price,
    tax_amount = EXCLUDED.tax_amount,
    fee_amount = EXCLUDED.fee_amount,
    is_closed = EXCLUDED.is_closed,
    updated_at = now();
