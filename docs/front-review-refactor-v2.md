# Front Review Refactor V2

> 대상: `tripflow-ai-frontend/src/views/travelgram/review/*`, `tripflow-ai-frontend/src/components/travelgram/review/*`
> 목표: 후기 플로우의 화면/상태/UI 경계를 다시 정리하고, 과한 분리를 줄이면서 공통 패턴을 통일한다.

---

## 1. 지금 다시 보는 이유

1차 분리로 `view` 안의 스크립트가 줄었지만, 아직 경계가 완전히 깔끔하지 않다.

- 어떤 로직은 `composable`로 빼는 게 맞았고
- 어떤 건 그냥 `view` 안 `script setup`에 두는 편이 더 단순하다
- 공통 UI도 아직 책임이 겹치는 부분이 있다

그래서 V2는 "더 많이 쪼개기"가 아니라, **적절한 수준으로 다시 통일하기**가 목적이다.

---

## 2. 현재 상태 요약

### View

- `CreateTravelReview.vue`
- `PhotoOrder.vue`
- `CaptionSelect.vue`
- `HashtagSelect.vue`
- `EditPage.vue`
- `InstagramPreview.vue`
- `CompleteReview.vue`

### Review component

- `ReviewLoadingState.vue`
- `ReviewErrorState.vue`
- `ReviewStyleCard.vue`
- `ReviewTagPill.vue`

### Review composable

- `useReviewBootstrap.js`
- `useReviewPhotoPolling.js`
- `useReviewPhotoReorder.js`
- `useReviewStyleSelection.js`
- `useReviewHashtagEditor.js`
- `useReviewCaptionEditor.js`
- `useInstagramPreview.js`

---

## 3. 무엇을 통일할지

### 3.1 페이지 shell 통일

지금 후기 화면들은 공통으로 아래 구조를 반복한다.

- `TravelgramHeader`
- `.travelgram-page`
- `.page-inner`
- 섹션 카드
- `NavigationButtons`

통일 방향:

- `ReviewStepLayout` 같은 상위 shell component 도입
- header, page inner width, bottom navigation 패턴을 공통화
- 각 view는 본문 섹션만 넘긴다

---

### 3.2 async 상태 통일

통일 대상:

- `PhotoOrder.vue`
- `CaptionSelect.vue`
- `CreateTravelReview.vue`의 업로드/분석 상태

현재 방향 유지:

- `ReviewLoadingState` = 로딩 전용
- `ReviewErrorState` = 에러 + retry 전용

주의:

- loading과 error를 합쳐서 한 컴포넌트로 유지하지 않는다
- 비동기 아닌 화면에는 억지로 넣지 않는다

---

### 3.3 사진 렌더링 통일

통일 후보:

- `EditPage.vue`
- `InstagramPreview.vue`

공통으로 뽑을 것:

- 사진 한 장 보여주는 방식
- 현재 index 표시
- 이전/다음 버튼
- 대표 사진/미리보기 badge 표현

분리 원칙:

- UI는 통일
- 동작은 view/composable 별로 유지

---

### 3.4 카드/pill 통일

통일 후보:

- `ReviewStyleCard`
- `ReviewTagPill`

정리할 것:

- active/selected variant 규칙
- hover 스타일
- border/shadow 톤
- 아이콘 위치

주의:

- 클래스 조립을 view마다 다시 하지 않기
- variant prop 이름을 더 늘리지 않기

---

### 3.5 CreateTravelReview 전용 UI 분해

통일/분리 후보:

- `ReviewHeroSummary`
- `ReviewItineraryAccordion`
- `ReviewUploadPanel`
- `ReviewAnalysisStatus`

이 화면은 진입점이라서 가장 무겁다.

분리 순서:

1. hero summary
2. itinerary accordion
3. upload panel
4. analysis status

---

## 4. 무엇을 유지할지

### 유지

- route 진입 및 route 이동
- 단계 완료 시점의 submit flow
- 화면 고유 문구
- store에 저장해야 하는 흐름 상태

### 유지하되 단순화

- `NavigationButtons`
- `TravelgramHeader`
- 각 view의 `goBack`, `goNext`

---

## 5. composable 경계 재검토

V2에서 중요한 판단은 여기다.

### composable로 두기 좋은 것

- 상태 전개가 핵심인 로직
- API 호출과 응답 정규화
- 여러 view에서 다시 쓸 가능성이 있는 흐름
- 복잡한 파생 상태

### view script 안에 두는 게 더 나은 것

- 해당 화면에서만 쓰는 아주 작은 헬퍼
- DOM에 직접 붙는 단순 스크롤
- 템플릿이 읽기 쉬워질 정도로만 있는 보조 함수

### 다시 생각할 후보

- `useInstagramPreview`
- `useReviewCaptionEditor`

이 둘은 경계가 가장 애매하다.

예:

- 사진 index 이동
- copy text 조립
- caption byte 계산

이런 건 composable이 맞을 수 있지만,
너무 얇으면 그냥 view script 안에 있어도 충분하다.

---

## 6. 추천 구현 순서

1. `ReviewStepLayout` 설계
2. `ReviewLoadingState` / `ReviewErrorState` 유지 확정
3. `EditPage` / `InstagramPreview` 사진 UI 공통화
4. `ReviewStyleCard` / `ReviewTagPill` 스타일 통일
5. `CreateTravelReview` 전용 UI 분해
6. 애매한 composable은 view로 되돌릴지 판단

---

## 7. 완료 기준

V2가 끝났다고 볼 기준:

- 후기 화면들이 같은 shell을 쓴다
- loading/error 표현이 두 종류로 정리된다
- 사진 UI와 pill/card 스타일이 일관된다
- composable이 "정말 상태/흐름"만 남긴다
- 과하게 얇은 composable이 남지 않는다

