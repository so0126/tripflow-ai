# Front Review Refactor Plan

> 대상 범위: `tripflow-ai-frontend/src/views/travelgram/review/*`
> 목표: 후기 생성/편집 플로우에서 화면 로직과 UI를 분리해서 유지보수 가능한 단위로 쪼갠다.
> 원칙: **상태/흐름은 composable**, **반복되는 UI는 component**, **페이지 파일은 조립만 담당**한다.

---

## 1. 왜 이 작업을 먼저 하나

현재 후기 플로우는 여러 단계로 나뉘어 있지만, 각 화면의 `<script setup>`이 커지고 있다.

대표적으로 아래 책임이 한 파일 안에 같이 들어 있다.

- 데이터 초기화
- API 호출
- 상태 계산
- 단계 이동
- UI 이벤트 핸들링
- 일부 화면 렌더링 전용 상태

이 구조는 기능 추가보다 수정 비용이 먼저 커진다.

- 같은 로직이 화면마다 흩어진다.
- 로딩 / 에러 / 재시도 패턴이 매번 조금씩 다르다.
- store가 영속 상태와 화면 상태를 같이 떠안는다.

그래서 이번 작업은 "일단 다 component로 빼기"가 아니라, **재사용 가능한 상태 흐름만 composable로 먼저 빼고**, **진짜 반복되는 UI만 component로 분리**하는 방향으로 잡는다.

---

## 2. 현재 구조 요약

후기 플로우는 아래 화면으로 이어진다.

1. `CreateTravelReview.vue`
2. `PhotoOrder.vue`
3. `CaptionSelect.vue`
4. `HashtagSelect.vue`
5. `EditPage.vue`
6. `InstagramPreview.vue`
7. `CompleteReview.vue`

공통적으로 사용하는 상태는 `src/store/reviewStore.js`에 모여 있다.

현재 확인되는 문제는 다음과 같다.

- 화면별 로직이 길고 중복된다.
- `TravelgramHeader`, `NavigationButtons`, `PageHeader`, `StepHeader` 같은 공통 UI import가 산재한다.
- `EditPage.vue`와 `InstagramPreview.vue`는 사진 캐러셀 성격의 로직이 겹친다.
- `CreateTravelReview.vue`는 초기화, 일정 가공, 업로드, 폴링, 재분석까지 모두 가지고 있다.
- `CaptionSelect.vue`는 스타일 생성 로딩과 선택/제출 상태를 함께 처리한다.
- `HashtagSelect.vue`는 태그 토글, 커스텀 태그, 저장까지 모두 한 파일에 있다.

---

## 3. 분리 기준

### composable로 뺄 것

아래 조건 중 하나라도 해당하면 composable 후보로 본다.

- 화면 렌더링 자체보다 상태 전개가 핵심인 경우
- API 호출과 그 결과 정규화가 중심인 경우
- 여러 화면에서 같은 흐름을 재사용할 가능성이 있는 경우
- 복잡한 파생 상태가 많은 경우

### component로 뺄 것

아래 조건을 만족해야 component 후보로 본다.

- UI 구조가 독립적이다.
- props / emits로 입력과 출력을 정리할 수 있다.
- 최소 2개 화면 이상에서 재사용되거나, 단일 화면이라도 큰 덩어리로 분리할 가치가 있다.

### 빼지 않을 것

- 페이지별 route 진입/이동 책임
- 각 단계의 최종 submit 버튼 흐름
- 화면 고유 문구와 단계별 안내 문장

---

## 4. Composable 후보

### 4.1 `useReviewBootstrap`

대상 파일:

- `CreateTravelReview.vue`

담당할 일:

- plan detail 조회
- review 생성 호출
- `reviewStore` 초기화
- `isReady` / 초기 진입 상태 관리

왜 필요한가:

- `CreateTravelReview.vue`는 첫 진입 시 한 번에 해야 할 일이 너무 많다.
- 초기화 순서가 바뀌면 다음 단계 전부가 흔들린다.

산출 형태:

- `planId`, `planTitle` 입력
- `currentPlanInfo`, `isReady`, `fetchPlanDetail`, `createReviewSession` 반환

---

### 4.2 `useReviewPhotoPolling`

대상 파일:

- `CreateTravelReview.vue`

담당할 일:

- 업로드된 사진 상태 집계
- polling start / stop
- server status 동기화
- reanalyze 요청
- `SUCCESS / FAILED` 기준 종료 판단

왜 필요한가:

- 분석 상태 체크는 UI보다 흐름 로직에 가깝다.
- 현재는 `setInterval` / `clearInterval` / 재분석 / 상태 계산이 한 파일에 섞여 있다.

산출 형태:

- `uploadedImages`
- `isAnalyzing`, `failedCount`, `settledCount`, `allSettled`
- `startPolling`, `stopPolling`, `handleReanalyze`

---

### 4.3 `useReviewPhotoReorder`

대상 파일:

- `PhotoOrder.vue`

담당할 일:

- 사진 순서 변경
- 대표 사진 선택
- `canProceed` 계산
- 저장 후 다음 단계 이동 전 준비

왜 필요한가:

- 순서 변경 로직 자체는 UI와 분리 가능하다.
- 나중에 드래그 앤 드롭으로 바꾸더라도 상태 흐름을 유지할 수 있다.

산출 형태:

- `photos`, `mainPhotoId`
- `selectMain`, `moveUp`, `moveDown`
- `canProceed`

---

### 4.4 `useReviewStyleSelection`

대상 파일:

- `CaptionSelect.vue`

담당할 일:

- 스타일 목록 생성 호출
- 재시도용 로딩/에러 상태
- 선택된 스타일 인덱스
- 선택 후 store 반영 및 저장 API 호출

왜 필요한가:

- 스타일 생성은 1회성 요청이지만 실패/재시도 패턴이 중요하다.
- 화면 코드보다 "언제 생성하고 언제 저장하는지"가 핵심이다.

산출 형태:

- `isLoading`, `hasError`, `selectedIndex`, `canProceed`
- `loadStyles`, `selectStyle`, `goNext`

---

### 4.5 `useReviewHashtagEditor`

대상 파일:

- `HashtagSelect.vue`

담당할 일:

- AI 추천 태그와 사용자 선택 태그 동기화
- 태그 토글
- 커스텀 태그 추가
- 최종 저장 payload 생성

왜 필요한가:

- 현재는 `Set` 기반 선택 상태와 store 값이 동시에 움직인다.
- 입력/선택/저장 규칙이 섞여 있어서 테스트하기 어렵다.

산출 형태:

- `aiTags`, `selectedSet`, `newTagInput`, `selectedCount`
- `toggleTag`, `addCustomTag`, `persistTags`

---

### 4.6 `useReviewCaptionEditor`

대상 파일:

- `EditPage.vue`

담당할 일:

- caption 동기화
- byte length 계산
- 사진 carousel index 관리
- 이전/다음 이동
- 저장 API 호출

왜 필요한가:

- 현재는 `document.querySelector`로 DOM을 직접 건드린다.
- 이 로직은 composable로 옮기면 DOM 의존을 줄이고, 이후 carousel component와도 붙이기 쉽다.

산출 형태:

- `caption`, `captionByteLength`, `currentPhotoIndex`
- `prevPhoto`, `nextPhoto`, `scrollToPhoto`, `saveCaption`

---

### 4.7 `useInstagramPreview`

대상 파일:

- `InstagramPreview.vue`

담당할 일:

- current photo index 관리
- copy text 생성
- user info 계산
- publish flow 준비

왜 필요한가:

- 미리보기는 화면이 단순해 보여도 파생 상태가 많다.
- 복사 텍스트와 인덱스 이동은 재사용 가능한 순수 로직으로 분리하기 좋다.

산출 형태:

- `currentIndex`, `currentPhoto`, `userInfo`, `likes`
- `prevPhoto`, `nextPhoto`, `copyToClipboard`, `publish`

---

## 5. Component 후보

### 5.1 `ReviewSectionCard`

역할:

- 후기 흐름에서 자주 반복되는 카드 래퍼

적용 후보:

- `PhotoOrder.vue`
- `CaptionSelect.vue`
- `HashtagSelect.vue`
- `EditPage.vue`
- `InstagramPreview.vue`

포인트:

- 제목, 서브텍스트, 본문 slot을 받아서 레이아웃만 담당한다.
- 카드 그림자/패딩/섹션 간격을 통일할 수 있다.

---

### 5.2 `ReviewStatusPanel`

역할:

- 로딩 / 에러 / 재시도 UI를 통일

적용 후보:

- `PhotoOrder.vue`의 loading overlay / error overlay
- `CaptionSelect.vue`의 spinner / retry

포인트:

- 로딩 문구와 재시도 UI가 파일마다 달라질 필요가 없다.
- `variant`만 받아서 표시를 바꿀 수 있다.

---

### 5.3 `ReviewPhotoCarousel`

역할:

- 사진 목록 + 현재 사진 이동 + 대표 사진 표시

적용 후보:

- `EditPage.vue`
- `InstagramPreview.vue`

포인트:

- 두 화면 모두 사진을 좌우 이동하면서 본다.
- 다만 `EditPage`는 편집 중심, `InstagramPreview`는 게시 미리보기 중심이므로 props를 너무 복잡하게 만들지 않는다.

---

### 5.4 `ReviewStyleCard`

역할:

- AI 스타일 선택 카드 UI

적용 후보:

- `CaptionSelect.vue`

포인트:

- 스타일 이름, 설명, 해시태그 미리보기를 카드 단위로 분리한다.
- 선택 상태 표현을 컴포넌트 내부로 숨길 수 있다.

---

### 5.5 `ReviewTagPillGroup`

역할:

- 선택된 태그 / 추천 태그 / 사용자 태그 pill 렌더링

적용 후보:

- `HashtagSelect.vue`

포인트:

- 선택 상태에 따라 variant만 바꿔 렌더링한다.
- 토글 액션은 부모 composable에서 처리한다.

---

### 5.6 `ReviewHeroSummary`

역할:

- `CreateTravelReview.vue` 상단 여행 요약 카드

적용 후보:

- `CreateTravelReview.vue`

포인트:

- 목적지, 일정, 예산, 대표 이미지, CTA를 한 덩어리로 묶는다.
- 페이지 상단 진입 인상을 담당하므로 UI component로 분리 가치가 있다.

---

### 5.7 `ReviewItineraryAccordion`

역할:

- AI 참고용 일정 접기/펼치기 영역

적용 후보:

- `CreateTravelReview.vue`

포인트:

- `PlanDayTimeline`을 감싸는 표시용 wrapper.
- 펼침 상태와 안내 문구를 한 곳에서 관리한다.

---

## 6. 추천 분리 순서

### Phase 1. 화면 로직부터 분리

1. `CreateTravelReview.vue`
2. `CaptionSelect.vue`
3. `HashtagSelect.vue`
4. `EditPage.vue`
5. `InstagramPreview.vue`
6. `PhotoOrder.vue`

이 순서로 가는 이유:

- 초기 진입과 업로드 분석이 가장 복잡하다.
- 그 다음은 스타일/태그 선택처럼 API 중심 흐름을 정리한다.
- 마지막에 편집/미리보기를 정리하면 DOM 의존 로직을 덜 남길 수 있다.

### Phase 2. 반복 UI component화

1. `ReviewStatusPanel`
2. `ReviewPhotoCarousel`
3. `ReviewStyleCard`
4. `ReviewTagPillGroup`
5. `ReviewSectionCard`

이 순서로 가는 이유:

- 로직 분리 후에야 UI 반복 여부가 더 정확히 보인다.
- 먼저 UI를 쪼개면 props 설계가 흔들릴 가능성이 있다.

---

## 7. 파일별 작업 메모

| 파일 | 현재 책임 | 1차 분리 방향 |
|---|---|---|
| `CreateTravelReview.vue` | 일정 조회, review 생성, 업로드, polling, 재분석 | bootstrap / polling composable + hero UI component |
| `PhotoOrder.vue` | 순서 변경, 대표 사진 선택, 저장 후 분석 | reorder composable + status panel |
| `CaptionSelect.vue` | 스타일 생성, 선택, 저장 | style selection composable + style card |
| `HashtagSelect.vue` | 태그 선택, 커스텀 태그, 저장 | hashtag editor composable + tag pill group |
| `EditPage.vue` | 사진 편집, 캡션, 해시태그, 저장 | caption editor composable + photo carousel |
| `InstagramPreview.vue` | 미리보기, 카피, 발행 | preview composable + photo carousel |

---

## 8. 리팩토링 원칙

1. 페이지 파일은 route 진입과 최종 submit만 남긴다.
2. composable은 상태와 행위만 제공하고, 마크업을 가지지 않는다.
3. component는 props/emits로만 통신한다.
4. store는 단계 간 공유가 필요한 데이터만 유지한다.
5. DOM 직접 조작은 마지막에 제거한다.
6. 한 번에 모든 걸 쪼개지 말고, 화면 하나씩 옮긴다.

---

## 9. 완료 기준

아래가 만족되면 이 리팩토링 계획은 완료로 본다.

- `review` 관련 페이지의 `<script setup>`이 짧아지고 역할이 명확해진다.
- 업로드 / 스타일 생성 / 태그 편집 / 미리보기 흐름이 각각 하나의 composable로 읽힌다.
- 공통 UI는 component로 분리되어 재사용된다.
- `document.querySelector` 같은 직접 DOM 조작이 사라진다.
- 로딩 / 에러 / 재시도 UI가 화면마다 비슷한 형태로 정리된다.

---

## 10. 다음 작업

이 문서를 기준으로 실제 코드를 건드릴 때는 아래 순서가 적절하다.

1. `CreateTravelReview.vue`에서 bootstrap + polling 분리
2. `CaptionSelect.vue`와 `HashtagSelect.vue`의 상태 로직 분리
3. `EditPage.vue`의 caption/editor 로직 분리
4. `InstagramPreview.vue`의 preview state 분리
5. 공통 UI component 추출

