# 작업로그 — 01. 리뷰 입력 데이터 정비 (장소 데이터 흐름)

> 형식: 배경 / 작업 / 결과 / 회고
> 메모: 작업이 한 번에 여러 개로 번진 구간이라, "작업"은 목록으로만 적는다. 매끄러운 줄글로 안 이어져도 됨.
> 표시: ⚠️ = 아직 내가 100% 판단/소유하지 못한 지점(면접 전 한 번 더 짚을 것)

---

## 배경

기존 일정 생성은 TourAPI 기반 장소 데이터를 썼다. 그런데 리뷰 생성 프롬프트에 쓰려니
TourAPI는 "관광용 공공데이터"라 서울 일반 장소·카테고리·주소 품질이 부실했다.
→ 리뷰 입력 데이터의 질을 올리려고 **네이버 Local Search 기반 수집**으로 전환했다.

## 작업

- 네이버 Local Search로 서울 장소 수집 (`NaverTravelPlaceImportService`)
  - "서울" 미포함 주소·좌표 없는 항목은 후보에서 제외, zone_id 매핑되는 것만 저장
- **중복 키 설계**: 네이버 응답엔 고유 ID가 없음 → `content_id = "naver-local-" + sha256(title + "|" + address)`
  로 합성 고유키를 직접 만듦
- **중복 방지 2겹**:
  1. 실행 중(메모리): 시작 시 DB의 기존 content_id를 `knownContentIds` set에 로드, 이미 있으면 skip
  2. DB 레벨: `INSERT ... ON CONFLICT (content_id) DO UPDATE` 업서트 → 재실행해도 중복 행 안 생김
- 설정값을 세터/`@Value`에서 **record 기반 properties로 분리** (`NaverImportProperties`, `NaverApiProperties`)
  - ⚠️ 단, `TravelPlaceEmbeddingBackfillRunner`는 아직 `@Value`를 씀 (일관성 안 맞음)
- **실행 경로 분리**:
  - `NaverTravelPlaceApplicationRunner` — 서버 시작 시 일회성 수집, 완료 후 종료 옵션
  - `NaverTravelPlaceScheduledRunner` — `@Scheduled(cron = 매주 일요일 03:00)` 정기 갱신
  - 둘 다 `@ConditionalOnProperty`로 켜고 끔
- 수집 시 **insert 시점에 임베딩을 인라인으로 생성**해서 함께 저장
- 임베딩 백필 러너(`TravelPlaceEmbeddingBackfillRunner`): `embedding IS NULL`인 행만(또는 force 시 전체) 재임베딩
- 호텔: seed를 구할 수 없어 데모 데이터로 대체 (⚠️ planner/hotel 쪽, 이 작업 범위 밖이라 코드 재확인 필요)
- FF 매퍼를 summary 형태로 바꿔 요약을 보여주는 형태로 변경 (⚠️ travelgram/review 쪽, 코드 재확인 필요)

## 결과

- 서울 장소 데이터를 네이버 기반으로 확보 → 일정 생성·리뷰 생성 입력으로 사용 가능
- 합성키 + 업서트 덕분에 **수집 재실행이 안전**(중복 누적 없음)
- 일반 서버 실행과 수집/백필성 작업을 분리 → 평소엔 안 돌고 필요할 때만 실행

## 회고

- **일요일 스케줄러는 과한 설계였다.** "장소는 바뀌니까 주기 갱신이 필요하다"고 생각했지만,
  실제 갱신 수요를 측정한 적이 없다. 내 로드맵 철학("측정 안 한 최적화는 안 한다")과 충돌.
  → 지금이라면 일회성 수집만 두고 스케줄러는 보류했을 것.
- **임베딩 백필 러너는 지금 거의 필요 없다.** 수집이 이미 insert 시점에 임베딩을 넣기 때문.
  seed처럼 임베딩 없이 들어온 행에만 의미가 있음. (지금 데이터 규모에선 사실상 놀고 있음)
- **작업이 한 번에 여러 개로 번졌다.** 장소 수집만 하려다 스케줄러·백필까지 손댔다.
  → 다음부터는 조각 단위로 끊고, 끊을 때마다 이 로그 한 줄씩.
- ⚠️ **판단 필요(내 코드로 만들 지점):** 합성키 `sha256(title+address)`는
  "같은 이름 다른 지점"이나 "주소 표기 차이(서울/서울특별시)"에 취약할 수 있다.
  이 키 설계가 충분한지 한 번 더 따져볼 것.
