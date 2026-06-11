# Front Photo Polling Hardening

> 대상: `tripflow-ai-frontend/src/views/travelgram/review/CreateTravelReview.vue` (사진 AI 분석 폴링)
> 목표: 사진 분석 상태 폴링 루프를 견고하게 만든다 — 에러 처리, 무한 폴링 방지, (선택) 새로고침 복원.

---

## 1. 배경 — 지금 폴링의 한계

`CreateTravelReview.vue`는 사진 업로드 후 3초 간격으로 `getReviewPhotos`를 호출해 각 사진의 AI 분석 상태(`PENDING`/`SUCCESS`/`FAILED`)를 확인한다.

지금 이 루프의 **종료 조건은 `allSettled`(전부 SUCCESS/FAILED) 하나뿐**이다. 즉 폴링을 "작은 상태기계"로 보면 `done` 상태만 있고 `failed`(요청이 계속 실패), `gave-up`(영원히 settle 안 됨) 상태가 빠져 있다.

관련 코드:
- `checkAnalysisStatus` — `:232`
- `startPolling` (`setInterval` 3000) — `:248`
- `stopPolling` — `:265`
- `handleReanalyze` (재분석 시 폴링 재시작) — `:253`

확인된 컨트랙트:
- 응답은 전역 래퍼 `{success, status, data, error}` → 실제 사진 배열은 `res.data.data` (`:233`).
- `PhotoUploader`는 각 항목을 `img.url`로 렌더(`PhotoUploader.vue:27`) → 서버 사진을 `{id, url, status, orderIndex}` 형태로 넣으면 컴포넌트 수정 없이 그려진다.

---

## 2. 작업 분해안

### 2.1 공통 상태기계 정리

먼저 폴링을 하나의 작은 상태기계로 정리한다.

필요한 상태는 아래 4개다.

- `pollingStatus`: `idle | polling | error | timeout`
- `pollingFailCount`: 연속 실패 횟수
- `pollingAttemptCount`: 총 시도 횟수
- `pollingTimer`: 현재 폴링 핸들러

공통 규칙은 다음과 같다.

- [x] `pollingStatus`를 추가한다.
- [x] `pollingFailCount`를 추가한다.
- [x] `pollingAttemptCount`를 추가한다.
- [x] `pollingTimer`를 추가한다.
- [x] `startPolling()`에서 실패 카운터와 시도 카운터를 모두 초기화한다.
- [x] `stopPolling()`은 타이머 해제와 상태 종료만 책임지도록 맞춘다.
- [x] 성공 응답이 오면 `pollingFailCount`를 0으로 리셋한다.
- [x] 종료 조건을 `allSettled`, 연속 실패, 최대 시도 횟수, 수동 종료 4가지로 정리한다.

이 정리를 먼저 해두면 `#2`, `#3`, `#1`이 서로 상태를 덮어쓰지 않는다.

### 2.2 #2 에러 처리

**문제**

`checkAnalysisStatus`에 try/catch가 없어 요청 실패 시 unhandled rejection이 발생하고, 인터벌은 계속 돈다.

**작업**

- [x] `checkAnalysisStatus()`를 try/catch로 감싼다.
- [x] 성공 응답 처리 직후 `pollingFailCount`를 0으로 리셋한다.
- [x] catch에서 `pollingFailCount`를 1 증가시킨다.
- [x] 실패 횟수가 3회 이상이면 `stopPolling()`을 호출한다.
- [x] 실패 종료 상태를 `pollingStatus = 'error'`로 맞춘다.
- [x] 요청 실패 안내 문구를 분리한다.
- [x] 타임아웃 안내 문구를 분리한다.
- [x] 기존 alert 패턴과 충돌하지 않도록 표시 위치를 정한다.

**의도**

한 번의 네트워크 흔들림으로 루프를 죽이지도 않고, 계속 실패하는데도 영원히 돌지 않게 한다.

### 2.3 #3 무한 폴링 방지

**문제**

고정 3초 간격에 상한이 없어서 분석이 계속 `PENDING`이면 무한 폴링이 된다.

**작업**

- [x] `pollingAttemptCount`를 증가시키는 지점을 정한다.
- [x] 최대 시도 횟수를 60회로 둔다.
- [x] 최대 시도 횟수 초과 시 `stopPolling()`을 호출한다.
- [x] 타임아웃 종료 상태를 `pollingStatus = 'timeout'`으로 맞춘다.
- [x] 타임아웃 안내 문구를 분리한다.
- [ ] 이번 범위에서는 백오프를 넣지 않는다.
- [ ] 필요 시 다음 단계에서 `setTimeout` 기반으로 바꾸는 것을 메모해 둔다.

**의도**

bounded work를 보장해서 분석이 영영 끝나지 않는 경우에도 UI와 타이머가 영구 점유되지 않게 한다.

백오프는 이번 작업에서 제외한다.

- 재분석, 실패 카운터, 타임아웃 상한과 같이 섞이면 상태 전이가 더 헷갈린다.
- AI 분석이 오래 걸릴 때 서버 조회 부하를 줄이려는 목적이 있을 때 다음 단계에서 검토한다.
- `3s -> 5s -> 8s`처럼 점진적으로 간격을 늘려 불필요한 요청을 줄이는 방식은 후순위다.

### 2.4 #2와 #3의 연결 규칙

이 두 작업은 같은 루프 안에서 같이 작동해야 한다.

- [x] `startPolling()` 실행 시 두 카운터를 동시에 초기화한다.
- [x] `checkAnalysisStatus()` 성공 시 실패 카운터를 리셋한다.
- [x] 실패 누적 시 `error`로 종료한다.
- [x] `PENDING` 장기 지속 시 `timeout`으로 종료한다.
- [x] `handleReanalyze()`에서 기존 폴링을 종료한 뒤 새로 시작한다.
- [x] 재분석 시 이전 실패 이력이 다음 분석에 남지 않는지 확인한다.

이 규칙을 지키면 재분석 시 이전 실패 이력이 다음 분석을 오염시키지 않는다.

### 2.5 #1 새로고침 복원

**문제**

새로고침하면 `uploadedImages`가 비어서 `totalCount=0`이 되고, 진행 중이던 분석 상태를 다시 못 따라간다.

**작업**

- [x] `onMounted`에서 `createReview()`를 호출한다.
- [x] 응답에서 `photoGroupId`를 확보한다.
- [x] `getReviewPhotos()`로 서버 상태를 다시 가져온다.
- [x] 응답 사진 배열을 `uploadedImages`에 시드한다.
- [x] 서버 사진을 `PhotoUploader`가 기대하는 구조로 매핑한다.
- [x] `id` 필드를 맞춘다.
- [x] `url` 필드를 맞춘다.
- [x] `status` 필드를 맞춘다.
- [x] `summary` 필드를 맞춘다.
- [x] `orderIndex` 필드를 맞춘다.
- [x] `uploading: false`로 초기화한다.
- [x] 시드 후 `PENDING`이 하나라도 있으면 `startPolling()`을 다시 호출한다.
- [ ] 새로고침 후에도 진행 중 상태가 이어지는지 확인한다.

**선결 확인**

확인 결과, `GET /reviews/photos`의 `ReviewPhoto` 응답은 다음 필드를 가진다.

- `id`
- `reviewPostId`
- `fileUrl`
- `orderIndex`
- `summary`
- `status`
- `createdAt`

즉 새로고침 복원 시에는 서버의 `fileUrl`을 프론트 `uploadedImages[].url`로 매핑하면 된다. `PhotoUploader`는 `url`을 기대하므로, 이 매핑만 맞추면 컴포넌트 수정 없이 복원할 수 있다.

**의도**

새로고침을 해도 서버를 진실의 원천으로 다시 읽어서 진행 중 상태를 복원한다.

---

## 3. 추천 순서

- [x] `#2`와 `#3`을 먼저 묶어서 처리한다.
- [x] `#1`은 `ReviewPhoto` 응답 필드 확인 후 별도로 처리한다.
- [x] 폴링 하드닝 후 재분석 흐름을 다시 확인한다.
- [x] 새로고침 복원은 서버 응답 계약 확인 후 진행한다.

이 순서를 추천하는 이유는 다음과 같다.

- [x] `#2`와 `#3`은 같은 상태기계라 한 번에 고치는 편이 덜 흔들린다.
- [x] `#1`은 서버 응답 필드와 초기 시드 흐름을 확인해야 해서 계약 리스크가 있다.
- [x] `#2`와 `#3`만으로도 현재 가장 큰 문제인 무한 루프와 unhandled rejection을 먼저 제거할 수 있다.

**과한 추상화 경고**

이번 범위에서는 컴포넌트 안에 인라인으로 유지한다. 재사용이 실제로 생기면 그때 `useReviewPhotoPolling.js`로 추출한다. 참고로 `front-review-refactor-v2.md`의 컴포저블 계획과도 방향이 맞는다.

---

## 4. 검증

- [ ] `#2` 검증: 백엔드를 내리거나 네트워크를 차단했을 때 3회 실패 후 멈추고 에러 안내가 뜨는지 확인한다.
- [ ] `#3` 검증: 응답을 계속 `PENDING`으로 유지했을 때 상한 도달 후 타임아웃 안내가 뜨고 루프가 멈추는지 확인한다.
- [ ] `#1` 검증: 분석 중 새로고침했을 때 사진과 진행 상태가 복원되고 폴링이 이어지는지 확인한다.
- [ ] 공통 검증: 재분석 후에는 카운터가 초기화된 상태로 다시 추적되는지 확인한다.
- [ ] 공통 검증: `stopPolling()` 이후 타이머가 남지 않는지 확인한다.

---

## 5. 연관 변경

- [ ] `ReviewPostService.createReview` 멱등화(get-or-create)가 `#1` 복원의 데이터 출처인지 확인한다.
- [ ] 같은 plan으로 다시 들어와도 서버에 남아 있는 draft를 기준으로 사진 상태를 복원할 수 있는지 확인한다.

---

## 6. 다듬기

- [ ] 재분석 성공 시 `stopPolling()` 후 `startPolling()` 재시작 흐름을 더 매끈하게 다듬는다.
- [ ] 재분석 직후 `idle`을 거치는 UI 깜빡임이 있는지 확인한다.
- [ ] 재분석 중에는 이전 실패 알림이 잠깐 보이지 않도록 표시 우선순위를 정리한다.
- [ ] `handleReanalyze()`의 상태 전이를 주석으로 더 명확하게 남긴다.
