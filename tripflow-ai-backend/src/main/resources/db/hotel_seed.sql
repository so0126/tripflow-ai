-- 호텔 더미데이터 채우기

INSERT INTO hotels (
    id, external_hotel_id, name, hotel_name_local, district, neighborhood, star_rating,
    rating_score, review_count, address_line1, latitude, longitude, has_free_wifi,
    has_parking, near_metro, metro_station_name, is_pet_friendly, is_family_friendly,
    has_24h_frontdesk, has_english_staff, airport_distance_km, has_breakfast_restaurant, is_active
)
VALUES
    (2, 'hotel-seed-002', '인사 스테이 호텔', '인사 스테이 호텔', '종로구', '인사동', 3.0,
     4.21, 184, '서울특별시 종로구 인사동길 32', 37.5738120, 126.9861200,
     true, false, true, '안국역', false, true, true, true, 49.20, true, true),
    (3, 'hotel-seed-003', '북촌 한옥 부티크 호텔', '북촌 한옥 부티크 호텔', '종로구', '북촌', 4.0,
     4.48, 132, '서울특별시 종로구 북촌로 58', 37.5818240, 126.9849130,
     true, false, false, null, false, true, true, true, 50.10, true, true),
    (4, 'hotel-seed-004', '강남 시티 비즈니스 호텔', '강남 시티 비즈니스 호텔', '강남구', '역삼동', 4.0,
     4.35, 326, '서울특별시 강남구 테헤란로 142', 37.4999340, 127.0365420,
     true, true, true, '역삼역', false, false, true, true, 26.40, true, true),
    (5, 'hotel-seed-005', '청담 프리미어 호텔', '청담 프리미어 호텔', '강남구', '청담동', 5.0,
     4.71, 289, '서울특별시 강남구 도산대로 521', 37.5251140, 127.0479320,
     true, true, true, '청담역', true, true, true, true, 27.80, true, true),
    (6, 'hotel-seed-006', '홍대 라이프스타일 호텔', '홍대 라이프스타일 호텔', '마포구', '홍대', 3.0,
     4.18, 241, '서울특별시 마포구 양화로 188', 37.5571920, 126.9253810,
     true, false, true, '홍대입구역', true, false, true, true, 45.60, false, true),
    (7, 'hotel-seed-007', '이태원 어반 스테이', '이태원 어반 스테이', '용산구', '이태원', 2.0,
     3.92, 98, '서울특별시 용산구 이태원로 211', 37.5347210, 126.9946120,
     true, false, true, '이태원역', false, false, true, true, 39.30, false, true),
    (8, 'hotel-seed-008', '잠실 레이크뷰 호텔', '잠실 레이크뷰 호텔', '송파구', '석촌호수', 4.0,
     4.42, 207, '서울특별시 송파구 석촌호수로 216', 37.5108460, 127.1058710,
     true, true, true, '잠실역', false, true, true, true, 31.50, true, true),
    (9, 'hotel-seed-009', '성수 디자인 호텔', '성수 디자인 호텔', '성동구', '성수동', 3.0,
     4.26, 156, '서울특별시 성동구 연무장길 45', 37.5445810, 127.0559610,
     true, false, true, '성수역', true, false, true, false, 33.70, true, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rooms (
    id, hotel_id, external_room_type_id, name, bed_type, room_size_sqm, max_occupancy,
    has_kitchenette, is_accessible_room
)
VALUES
    (2, 2, 'room-seed-002', '싱글', '싱글', 19.00, 1, false, false),
    (3, 2, 'room-seed-003', '스탠다드 더블', '더블', 26.00, 2, false, false),
    (4, 2, 'room-seed-004', '패밀리룸', '더블+싱글', 48.00, 4, false, true),
    (5, 3, 'room-seed-005', '스탠다드 트윈', '트윈', 28.00, 2, false, false),
    (6, 3, 'room-seed-006', '디럭스 더블', '더블', 34.00, 2, false, false),
    (7, 3, 'room-seed-007', '스위트', '킹', 62.00, 2, true, false),
    (8, 4, 'room-seed-008', '싱글', '싱글', 20.00, 1, false, true),
    (9, 4, 'room-seed-009', '스탠다드 더블', '더블', 27.00, 2, false, false),
    (10, 4, 'room-seed-010', '디럭스 더블', '더블', 36.00, 2, false, false),
    (11, 5, 'room-seed-011', '디럭스 더블', '더블', 38.00, 2, false, false),
    (12, 5, 'room-seed-012', '패밀리룸', '더블+더블', 55.00, 4, true, true),
    (13, 5, 'room-seed-013', '스위트', '킹', 76.00, 2, true, false),
    (14, 6, 'room-seed-014', '싱글', '싱글', 18.00, 1, false, false),
    (15, 6, 'room-seed-015', '스탠다드 트윈', '트윈', 29.00, 2, false, false),
    (16, 7, 'room-seed-016', '싱글', '싱글', 18.00, 1, false, false),
    (17, 7, 'room-seed-017', '스탠다드 더블', '더블', 24.00, 2, false, false),
    (18, 8, 'room-seed-018', '스탠다드 더블', '더블', 28.00, 2, false, false),
    (19, 8, 'room-seed-019', '패밀리룸', '더블+싱글', 52.00, 4, false, true),
    (20, 8, 'room-seed-020', '스위트', '킹', 68.00, 2, true, false),
    (21, 9, 'room-seed-021', '스탠다드 트윈', '트윈', 27.00, 2, false, false),
    (22, 9, 'room-seed-022', '디럭스 더블', '더블', 33.00, 2, false, false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rate_plans (
    id, hotel_id, room_type_id, external_rate_plan_id, name, meal_plan,
    includes_breakfast, currency, base_price
)
VALUES
    (2, 2, 2, 'rate-seed-002', '자유 취소 요금', '객실만', false, 'KRW', 78000.00),
    (3, 2, 2, 'rate-seed-003', '조식 포함 요금', '조식 포함', true, 'KRW', 93000.00),
    (4, 2, 3, 'rate-seed-004', '자유 취소 요금', '객실만', false, 'KRW', 118000.00),
    (5, 2, 3, 'rate-seed-005', '조식 포함 요금', '조식 포함', true, 'KRW', 138000.00),
    (6, 2, 4, 'rate-seed-006', '자유 취소 요금', '객실만', false, 'KRW', 185000.00),
    (7, 2, 4, 'rate-seed-007', '조식 포함 요금', '조식 포함', true, 'KRW', 225000.00),
    (8, 3, 5, 'rate-seed-008', '자유 취소 요금', '객실만', false, 'KRW', 145000.00),
    (9, 3, 5, 'rate-seed-009', '조식 포함 요금', '조식 포함', true, 'KRW', 168000.00),
    (10, 3, 6, 'rate-seed-010', '자유 취소 요금', '객실만', false, 'KRW', 198000.00),
    (11, 3, 6, 'rate-seed-011', '조식 포함 요금', '조식 포함', true, 'KRW', 225000.00),
    (12, 3, 7, 'rate-seed-012', '자유 취소 요금', '객실만', false, 'KRW', 310000.00),
    (13, 3, 7, 'rate-seed-013', '조식 포함 요금', '조식 포함', true, 'KRW', 345000.00),
    (14, 4, 8, 'rate-seed-014', '자유 취소 요금', '객실만', false, 'KRW', 89000.00),
    (15, 4, 8, 'rate-seed-015', '조식 포함 요금', '조식 포함', true, 'KRW', 109000.00),
    (16, 4, 9, 'rate-seed-016', '자유 취소 요금', '객실만', false, 'KRW', 138000.00),
    (17, 4, 9, 'rate-seed-017', '조식 포함 요금', '조식 포함', true, 'KRW', 163000.00),
    (18, 4, 10, 'rate-seed-018', '자유 취소 요금', '객실만', false, 'KRW', 210000.00),
    (19, 4, 10, 'rate-seed-019', '조식 포함 요금', '조식 포함', true, 'KRW', 238000.00),
    (20, 5, 11, 'rate-seed-020', '자유 취소 요금', '객실만', false, 'KRW', 280000.00),
    (21, 5, 11, 'rate-seed-021', '조식 포함 요금', '조식 포함', true, 'KRW', 320000.00),
    (22, 5, 12, 'rate-seed-022', '자유 취소 요금', '객실만', false, 'KRW', 390000.00),
    (23, 5, 12, 'rate-seed-023', '조식 포함 요금', '조식 포함', true, 'KRW', 450000.00),
    (24, 5, 13, 'rate-seed-024', '자유 취소 요금', '객실만', false, 'KRW', 520000.00),
    (25, 5, 13, 'rate-seed-025', '조식 포함 요금', '조식 포함', true, 'KRW', 580000.00),
    (26, 6, 14, 'rate-seed-026', '자유 취소 요금', '객실만', false, 'KRW', 72000.00),
    (27, 6, 14, 'rate-seed-027', '조식 포함 요금', '조식 포함', true, 'KRW', 84000.00),
    (28, 6, 15, 'rate-seed-028', '자유 취소 요금', '객실만', false, 'KRW', 112000.00),
    (29, 6, 15, 'rate-seed-029', '조식 포함 요금', '조식 포함', true, 'KRW', 132000.00),
    (30, 7, 16, 'rate-seed-030', '자유 취소 요금', '객실만', false, 'KRW', 68000.00),
    (31, 7, 16, 'rate-seed-031', '조식 포함 요금', '조식 포함', true, 'KRW', 80000.00),
    (32, 7, 17, 'rate-seed-032', '자유 취소 요금', '객실만', false, 'KRW', 96000.00),
    (33, 7, 17, 'rate-seed-033', '조식 포함 요금', '조식 포함', true, 'KRW', 112000.00),
    (34, 8, 18, 'rate-seed-034', '자유 취소 요금', '객실만', false, 'KRW', 155000.00),
    (35, 8, 18, 'rate-seed-035', '조식 포함 요금', '조식 포함', true, 'KRW', 182000.00),
    (36, 8, 19, 'rate-seed-036', '자유 취소 요금', '객실만', false, 'KRW', 245000.00),
    (37, 8, 19, 'rate-seed-037', '조식 포함 요금', '조식 포함', true, 'KRW', 288000.00),
    (38, 8, 20, 'rate-seed-038', '자유 취소 요금', '객실만', false, 'KRW', 360000.00),
    (39, 8, 20, 'rate-seed-039', '조식 포함 요금', '조식 포함', true, 'KRW', 410000.00),
    (40, 9, 21, 'rate-seed-040', '자유 취소 요금', '객실만', false, 'KRW', 105000.00),
    (41, 9, 21, 'rate-seed-041', '조식 포함 요금', '조식 포함', true, 'KRW', 125000.00),
    (42, 9, 22, 'rate-seed-042', '자유 취소 요금', '객실만', false, 'KRW', 165000.00),
    (43, 9, 22, 'rate-seed-043', '조식 포함 요금', '조식 포함', true, 'KRW', 188000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 2, CURRENT_DATE + offset_day, 78000.00, 7800.00, 2340.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 3, CURRENT_DATE + offset_day, 93000.00, 9300.00, 2790.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 4, CURRENT_DATE + offset_day, 118000.00, 11800.00, 3540.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 5, CURRENT_DATE + offset_day, 138000.00, 13800.00, 4140.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 6, CURRENT_DATE + offset_day, 185000.00, 18500.00, 5550.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 7, CURRENT_DATE + offset_day, 225000.00, 22500.00, 6750.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 8, CURRENT_DATE + offset_day, 145000.00, 14500.00, 4350.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 9, CURRENT_DATE + offset_day, 168000.00, 16800.00, 5040.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 10, CURRENT_DATE + offset_day, 198000.00, 19800.00, 5940.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 11, CURRENT_DATE + offset_day, 225000.00, 22500.00, 6750.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 12, CURRENT_DATE + offset_day, 310000.00, 31000.00, 9300.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 13, CURRENT_DATE + offset_day, 345000.00, 34500.00, 10350.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 14, CURRENT_DATE + offset_day, 89000.00, 8900.00, 2670.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 15, CURRENT_DATE + offset_day, 109000.00, 10900.00, 3270.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 16, CURRENT_DATE + offset_day, 138000.00, 13800.00, 4140.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 17, CURRENT_DATE + offset_day, 163000.00, 16300.00, 4890.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 18, CURRENT_DATE + offset_day, 210000.00, 21000.00, 6300.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 19, CURRENT_DATE + offset_day, 238000.00, 23800.00, 7140.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 20, CURRENT_DATE + offset_day, 280000.00, 28000.00, 8400.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 21, CURRENT_DATE + offset_day, 320000.00, 32000.00, 9600.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 22, CURRENT_DATE + offset_day, 390000.00, 39000.00, 11700.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 23, CURRENT_DATE + offset_day, 450000.00, 45000.00, 13500.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 24, CURRENT_DATE + offset_day, 520000.00, 52000.00, 15600.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 25, CURRENT_DATE + offset_day, 580000.00, 58000.00, 17400.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 26, CURRENT_DATE + offset_day, 72000.00, 7200.00, 2160.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 27, CURRENT_DATE + offset_day, 84000.00, 8400.00, 2520.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 28, CURRENT_DATE + offset_day, 112000.00, 11200.00, 3360.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 29, CURRENT_DATE + offset_day, 132000.00, 13200.00, 3960.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 30, CURRENT_DATE + offset_day, 68000.00, 6800.00, 2040.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 31, CURRENT_DATE + offset_day, 80000.00, 8000.00, 2400.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 32, CURRENT_DATE + offset_day, 96000.00, 9600.00, 2880.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 33, CURRENT_DATE + offset_day, 112000.00, 11200.00, 3360.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 34, CURRENT_DATE + offset_day, 155000.00, 15500.00, 4650.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 35, CURRENT_DATE + offset_day, 182000.00, 18200.00, 5460.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 36, CURRENT_DATE + offset_day, 245000.00, 24500.00, 7350.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 37, CURRENT_DATE + offset_day, 288000.00, 28800.00, 8640.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 38, CURRENT_DATE + offset_day, 360000.00, 36000.00, 10800.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 39, CURRENT_DATE + offset_day, 410000.00, 41000.00, 12300.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 40, CURRENT_DATE + offset_day, 105000.00, 10500.00, 3150.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 41, CURRENT_DATE + offset_day, 125000.00, 12500.00, 3750.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 42, CURRENT_DATE + offset_day, 165000.00, 16500.00, 4950.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 43, CURRENT_DATE + offset_day, 188000.00, 18800.00, 5640.00, false
FROM generate_series(0, 29) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

SELECT setval('hotels_id_seq', COALESCE((SELECT max(id) FROM hotels), 1), true);
SELECT setval('hotel_rooms_id_seq', COALESCE((SELECT max(id) FROM hotel_rooms), 1), true);
SELECT setval('hotel_rate_plans_id_seq', COALESCE((SELECT max(id) FROM hotel_rate_plans), 1), true);
SELECT setval('hotel_rate_plan_prices_id_seq', COALESCE((SELECT max(id) FROM hotel_rate_plan_prices), 1), true);
