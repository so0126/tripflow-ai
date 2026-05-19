-- 로컬 개발용 기본 데이터입니다.
-- embedding 값은 DB 부팅/검색 smoke test용 더미 벡터입니다.
-- 실제 여행지 의미 검색을 하려면 app.embedding.backfill.enabled=true 로 실행해
-- travel_places.embedding 을 OpenAI 임베딩으로 다시 채워야 합니다.

INSERT INTO users (id, is_active, name, email, created_at, updated_at)
VALUES (1, true, '데모 사용자', 'demo@tripflow.ai', now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO travel_place_categories (id, code, name, parent_code, level, category_type)
VALUES
    (1, 'ROOT', '전체', NULL, 0, 'ROOT'),
    (2, 'SPOT', '명소', 'ROOT', 1, 'PLACE'),
    (3, 'FOOD', '맛집', 'ROOT', 1, 'PLACE'),
    (4, 'CAFE', '카페', 'ROOT', 1, 'PLACE'),
    (5, 'SHOPPING', '쇼핑', 'ROOT', 1, 'PLACE'),
    (6, 'EVENT', '행사', 'ROOT', 1, 'PLACE'),
    (7, 'STAY', '숙소', 'ROOT', 1, 'PLACE'),
    (8, 'ETC', '기타', 'ROOT', 1, 'PLACE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO travel_places (
    id, content_id, title, address, tel, zone_id, first_image, first_image2,
    lat, lng, category_code, description, tags, embedding, detail_info, normalized_category
)
VALUES
    (1, 'seed-001', '남산서울타워', '서울특별시 용산구 남산공원길 105', NULL, 'SEOUL-YONGSAN', NULL, NULL,
     37.5511694, 126.9882266, 'SPOT',
     '서울 야경과 도심 전망을 보기 좋은 남산 대표 전망 명소입니다.',
     '["전망","야경","서울명소","데이트"]'::jsonb,
     array_fill(0.001::real, ARRAY[3072])::vector,
     '케이블카, 산책로, 전망대를 함께 묶어 반나절 코스로 잡기 좋습니다.', 'SPOT'),
    (2, 'seed-002', '광장시장', '서울특별시 종로구 창경궁로 88', NULL, 'SEOUL-JONGNO', NULL, NULL,
     37.5700390, 126.9996030, 'FOOD',
     '빈대떡, 마약김밥, 육회 등 서울 로컬 음식을 한 번에 즐길 수 있는 전통시장입니다.',
     '["맛집","시장","로컬푸드","한식"]'::jsonb,
     array_fill(0.002::real, ARRAY[3072])::vector,
     '점심이나 이른 저녁 식사 코스로 넣기 좋고 종로, 을지로, 청계천 일정과 연결하기 쉽습니다.', 'FOOD'),
    (3, 'seed-003', '성수동 카페거리', '서울특별시 성동구 성수동 일대', NULL, 'SEOUL-SEONGDONG', NULL, NULL,
     37.5445810, 127.0559610, 'CAFE',
     '리모델링 창고형 카페, 편집숍, 팝업스토어가 모여 있는 감성 산책 구역입니다.',
     '["카페","감성","산책","편집숍"]'::jsonb,
     array_fill(0.003::real, ARRAY[3072])::vector,
     '오후 카페 투어와 쇼핑, 사진 촬영을 함께 구성하기 좋은 코스입니다.', 'CAFE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO plans (id, user_id, title, budget, start_date, end_date, is_ended, embedding, created_at, updated_at)
VALUES (1, 1, '서울 감성 2일 여행', 300000.00, CURRENT_DATE, CURRENT_DATE + 1, false,
        array_fill(0.004::real, ARRAY[3072])::vector, now(), now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO plan_days (id, plan_id, day_index, title, plan_date, embedding)
VALUES
    (1, 1, 1, '1일차', CURRENT_DATE, array_fill(0.005::real, ARRAY[3072])::vector),
    (2, 1, 2, '2일차', CURRENT_DATE + 1, array_fill(0.006::real, ARRAY[3072])::vector)
ON CONFLICT (id) DO NOTHING;

INSERT INTO plan_places (
    id, day_id, title, place_name, address, first_image, first_image2, lat, lng,
    expected_cost, start_at, end_at, normalized_category, is_ended, embedding
)
VALUES
    (1, 1, '남산서울타워 전망 관람', '남산서울타워', '서울특별시 용산구 남산공원길 105', NULL, NULL,
     37.5511694, 126.9882266, 21000.00, CURRENT_DATE + TIME '10:00', CURRENT_DATE + TIME '12:00',
     'SPOT', false, array_fill(0.007::real, ARRAY[3072])::vector),
    (2, 1, '광장시장 점심', '광장시장', '서울특별시 종로구 창경궁로 88', NULL, NULL,
     37.5700390, 126.9996030, 30000.00, CURRENT_DATE + TIME '13:00', CURRENT_DATE + TIME '15:00',
     'FOOD', false, array_fill(0.008::real, ARRAY[3072])::vector),
    (3, 2, '성수동 카페 투어', '성수동 카페거리', '서울특별시 성동구 성수동 일대', NULL, NULL,
     37.5445810, 127.0559610, 20000.00, CURRENT_DATE + 1 + TIME '11:00', CURRENT_DATE + 1 + TIME '13:00',
     'CAFE', false, array_fill(0.009::real, ARRAY[3072])::vector)
ON CONFLICT (id) DO NOTHING;

INSERT INTO plan_snapshots (id, user_id, version_no, snapshot_json, embedding, created_at)
VALUES (
    1,
    1,
    1,
    '{"title":"서울 감성 2일 여행","days":[{"dayIndex":1,"places":["남산서울타워","광장시장"]},{"dayIndex":2,"places":["성수동 카페거리"]}]}'::jsonb,
    array_fill(0.010::real, ARRAY[3072])::vector,
    now()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO chat_memory_vectors (id, user_id, content, embedding, created_at)
VALUES (1, 1, '사용자는 서울 감성 여행과 로컬 맛집 코스를 선호한다.', array_fill(0.011::real, ARRAY[1536])::vector, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotels (
    id, external_hotel_id, name, hotel_name_local, district, neighborhood, star_rating,
    rating_score, review_count, address_line1, latitude, longitude, has_free_wifi,
    has_parking, near_metro, metro_station_name, is_active
)
VALUES (
    1, 'hotel-seed-001', 'TripFlow Demo Hotel', '트립플로우 데모 호텔', '중구', '명동',
    4.0, 4.50, 128, '서울특별시 중구 명동 일대', 37.5637600, 126.9857300,
    true, true, true, '명동역', true
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rooms (
    id, hotel_id, external_room_type_id, name, bed_type, room_size_sqm, max_occupancy
)
VALUES (1, 1, 'room-seed-001', '스탠다드 더블', '더블', 24.00, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rate_plans (
    id, hotel_id, room_type_id, external_rate_plan_id, name, meal_plan,
    includes_breakfast, currency, base_price
)
VALUES (1, 1, 1, 'rate-seed-001', '자유 취소 요금', '객실만', false, 'KRW', 120000.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hotel_rate_plan_prices (rate_plan_id, stay_date, price, tax_amount, fee_amount, is_closed)
SELECT 1, CURRENT_DATE + offset_day, 120000.00, 12000.00, 3000.00, false
FROM generate_series(0, 14) AS offset_day
ON CONFLICT (rate_plan_id, stay_date) DO NOTHING;

INSERT INTO image_places (
    id, name, content, description, lat, lng, address, place_type,
    external_image_url, image_status
)
VALUES
    (1, '남산서울타워', '서울 야경과 전망대 이미지 후보', '서울 대표 랜드마크 이미지 후보입니다.', 37.5511694, 126.9882266,
     '서울특별시 용산구 남산공원길 105', 'ATTRACTION', NULL, 'READY'),
    (2, '성수동 카페거리', '성수동 카페와 거리 이미지 후보', '카페 투어용 이미지 후보입니다.', 37.5445810, 127.0559610,
     '서울특별시 성동구 성수동 일대', 'CAFE', NULL, 'READY')
ON CONFLICT (id) DO NOTHING;

INSERT INTO toilets (id, name, lat, lng, address)
VALUES
    (1, '서울시청 인근 공중화장실', 37.5665000, 126.9780000, '서울특별시 중구 세종대로 일대')
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', COALESCE((SELECT max(id) FROM users), 1), true);
SELECT setval('travel_place_categories_id_seq', COALESCE((SELECT max(id) FROM travel_place_categories), 1), true);
SELECT setval('travel_places_id_seq', COALESCE((SELECT max(id) FROM travel_places), 1), true);
SELECT setval('plans_id_seq', COALESCE((SELECT max(id) FROM plans), 1), true);
SELECT setval('plan_days_id_seq', COALESCE((SELECT max(id) FROM plan_days), 1), true);
SELECT setval('plan_places_id_seq', COALESCE((SELECT max(id) FROM plan_places), 1), true);
SELECT setval('plan_snapshots_id_seq', COALESCE((SELECT max(id) FROM plan_snapshots), 1), true);
SELECT setval('chat_memory_vectors_id_seq', COALESCE((SELECT max(id) FROM chat_memory_vectors), 1), true);
SELECT setval('hotels_id_seq', COALESCE((SELECT max(id) FROM hotels), 1), true);
SELECT setval('hotel_rooms_id_seq', COALESCE((SELECT max(id) FROM hotel_rooms), 1), true);
SELECT setval('hotel_rate_plans_id_seq', COALESCE((SELECT max(id) FROM hotel_rate_plans), 1), true);
SELECT setval('hotel_rate_plan_prices_id_seq', COALESCE((SELECT max(id) FROM hotel_rate_plan_prices), 1), true);
SELECT setval('image_places_id_seq', COALESCE((SELECT max(id) FROM image_places), 1), true);
SELECT setval('toilets_id_seq', COALESCE((SELECT max(id) FROM toilets), 1), true);
