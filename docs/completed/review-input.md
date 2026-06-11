# 작업로그 — 01. 리뷰 입력 데이터 정비

## 배경

- 리뷰 생성을 안정적으로 만들기 위해 여행 계획과 장소 데이터가 충분히 채워져 있어야 했다.
- 기존 데이터가 없어 장소 수집 방식과 저장 구조를 함께 정비했다.

## 작업

**스키마 설계**
- 구조화된 문서나 배열 데이터는 `jsonb` 사용
  - `travel_places.tags` — 태그 필터링 가능성
  - `hotel_bookings.provider_booking_meta` — 예약 부가정보 구조화
  - `ai_review_analysis.input_json`, `output_json` — AI 요청/응답 유연하게 보관
  - `plan_snapshots.snapshot_json` — 여행 계획 전체를 버전 단위로 저장

**장소 데이터 확보**
- demo seed + 임베딩 백필로 시작했으나 리뷰 품질 문제로 Naver Local Search API로 전환
- 관리용 배치 작업이라 MyBatis 매퍼 대신 JDBC Template 사용
- 네이버 응답에 고유 ID가 없어 장소명 + 주소 조합으로 SHA 해싱해 중복 키 생성
  - 한계: "서울" / "서울특별시" 같은 표기 차이는 다른 데이터로 인식됨

**설정 분리**
- 네이버 API 인증 정보와 import 실행 설정을 분리
- 읽기 전용 설정은 record 기반으로 정리
  - 미완: `TravelPlaceEmbeddingBackfillRunner.java`는 아직 `@Value` 사용 중

**호텔 관련**
- 호텔 부킹 매퍼 2개를 요약 매퍼(`HotelBookingFFMapper.xml`)로 정리
- `providerBookingMeta` — jsonb 타입 에러로 `Map` + `ObjectMapper`로 JSON 직렬화 처리
- 프론트에서 서버 생성 메타데이터를 다시 전송하지 않도록 정리
- null-safe 처리 추가

**보류한 작업**
- 오프셋 기반 적재 실패 방지 — 1,000건 수준이라 우선순위 낮음
- 호텔 데이터 네이버 연동 — 스키마 불일치로 보류

## 결과

- 리뷰 생성에 필요한 입력 데이터 흐름을 확보했다.
- 장소 데이터는 중복 관리가 가능해졌고, 재실행에도 비교적 안전한 구조가 되었다.

## 회고

- 작업 범위가 쉽게 넓어져서, 목표를 벗어나는 기능은 다음으로 분리하는 편이 낫다.
- 이해하지 못한 작업은 커밋하지 않는 것을 원칙으로 해야 한다.
