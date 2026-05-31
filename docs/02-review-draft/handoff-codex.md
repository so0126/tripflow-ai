# Review AI 비동기 전환 Handoff

## 목적
- `STYLE_GENERATION`과 사진 분석 파이프라인을 비동기 처리로 전환한다.
- 상태는 도메인 테이블에 흩뿌리지 않고 `review_job` 하나로 관리한다.
- 결과는 기존 테이블에 저장하고, 진행 상태는 job 테이블이 책임진다.
- 다만 비동기는 "응답 경로 분리"이지, AI 생성 자체를 자동으로 빠르게 만드는 방법은 아니다.

## 전제
- DB는 PostgreSQL이다.
- `review_job` 테이블을 새로 추가했다.
- FK는 `review_post_id`, `photo_group_id`, `photo_id`로 분리해서 유지한다.
- `review_photo.summary`는 결과값 저장용이고, 상태 판단용이 아니다.
- `AiReviewService.createPlanInputJson()` + `ReviewInputJsonAssembler`는 이미 공통 context build 역할을 하고 있다.

## 작업 순서

### 1. `review_job`을 상태 관리의 중심으로 둔다
- `review_job`은 비동기 작업 상태를 추적한다.
- 최소 상태값은 `PENDING`, `RUNNING`, `SUCCESS`, `FAILED`다.
- 실패 시 `error_message`에 원인을 저장한다.

### 2. `STYLE_GENERATION`부터 job으로 감싼다
- 스타일 생성은 AI 1회 호출로 4개 스타일을 한 번에 생성한다.
- 비동기화 대상은 4개 스타일 개별이 아니라 `STYLE_GENERATION` 작업 전체다.
- 요청이 오면 `review_job`을 먼저 만들고, 백그라운드에서 실제 AI 호출을 수행한다.
- 이 변경만으로 AI 생성 시간이 줄어들지는 않는다. 줄어드는 것은 HTTP 요청 대기 시간과 타임아웃 위험이다.
- 실제 생성 시간을 줄이려면 프롬프트 압축, 출력 길이 축소, 모델 변경 같은 별도 최적화가 필요하다.

### 3. 사진 분석도 동일한 패턴으로 붙인다
- 사진별 분석은 `PHOTO_ANALYSIS` job으로 관리한다.
- 각 사진은 `photo_id` FK로 연결한다.
- 분석 결과는 `review_photo.summary`에 저장하고, 성공/실패는 `review_job.status`로 판단한다.

### 4. `TRIP_CONTEXT_ANALYSIS`는 중간 단계로 연결한다
- 사진 분석 job이 모두 `SUCCESS`가 되면 실행한다.
- 이 단계는 `photo_group_id` FK로 관리한다.
- 결과는 `review_posts.overall_moods`, `travel_type` 업데이트로 반영한다.

### 5. 상태 조회 API를 추가한다
- 프론트가 진행 상황을 볼 수 있게 job 조회 API가 필요하다.
- 최소한 `jobId` 기준 조회는 있어야 한다.
- 필요하면 `reviewPostId`, `photoGroupId`, `photoId` 기준 조회도 추가한다.

### 6. 로그는 최소화한다
- agent 내부 성공 로그는 제거하거나 최소화한다.
- 실패 로그는 유지한다.
- 운영 추적은 `review_job`이 맡고, 로그는 보조 수단으로 둔다.

### 6-1. 속도 최적화는 비동기와 분리해서 본다
- `STYLE_GENERATION`이 느린 이유가 AI 출력 자체라면, 비동기화만으로는 체감 속도 문제가 해결되지 않는다.
- 이미 `context build`는 존재하므로, 다음 최적화는 입력 JSON 압축과 프롬프트 축소를 우선 검토한다.
- 4개 스타일을 각각 병렬 호출하는 방식은 비용 증가와 일관성 저하 가능성 때문에 1차 선택으로 두지 않는다.

### 7. 순차 파이프라인으로 연결한다
- 흐름은 `PHOTO_ANALYSIS -> TRIP_CONTEXT_ANALYSIS -> STYLE_GENERATION` 순서다.
- 각 단계는 다음 단계의 선행 조건이 된다.
- 시작과 종료, 실패 지점은 모두 `review_job`에서 추적한다.

## 구현 우선순위
1. `STYLE_GENERATION`을 `review_job`에 연결
2. `review_job` 상태 전이 유틸리티 추가
3. `PHOTO_ANALYSIS` job 연결
4. `TRIP_CONTEXT_ANALYSIS` job 연결
5. 상태 조회 API 추가
6. 로그 정리

## 판단 기준
- “무엇이 실패했는가”는 `review_job.status`와 `error_message`로 본다.
- “무슨 결과가 나왔는가”는 기존 결과 테이블로 본다.
- `summary != null`만으로는 실패를 구분할 수 없으므로 상태 판단에 쓰지 않는다.
- `STYLE_GENERATION`은 "한 번의 AI 호출에서 4개 결과를 만드는 작업"으로 본다.
- 결과를 4번 따로 만드는 구조는 비용이 늘 수 있으므로, 우선은 현재 context build를 유지한 채 호출을 가볍게 만드는 방향을 검토한다.
