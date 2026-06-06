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

## 2. 세 가지 작업

### #2 에러 처리 (가장 작음)

**문제**: `checkAnalysisStatus`에 try/catch가 없어 요청 실패 시 unhandled rejection. 루프는 계속 돈다.

**계획**:
- `checkAnalysisStatus` 본문을 try/catch로 감싼다.
- **연속 실패 카운터** 도입: 성공 시 0으로 리셋, catch에서 +1.
- **3회 연속 실패하면** `stopPolling()` + 에러 상태 노출(기존 alert 패턴 재사용, "상태 확인에 실패했어요. 새로고침 해주세요").

**CS 포인트**: 일시적(네트워크 깜빡임) vs 지속적 실패 구분 — 한 번 실패로 루프를 죽이지도, 영원히 실패하며 돌지도 않게 하는 **bounded retry**.

### #3 무한 폴링 방지 (#2와 같은 조각)

**문제**: 고정 3초 + 상한 없음. 분석이 영영 `PENDING`이면 무한 폴링.

**계획**:
- 시도 횟수(또는 시작 시각)를 추적해 상한을 둔다. 예: 최대 60회(≈180초).
- 상한 초과 시 `stopPolling()` + 타임아웃 상태 노출("분석이 지연돼요. 잠시 후 재시도").
- (선택, 후순위) 백오프: `setInterval` → 재귀 `setTimeout`으로 3s→5s→8s 점증. AI가 느릴 때 서버 부하 완화. **1차는 고정 간격 + 하드 상한**으로, 백오프는 나중에.

**CS 포인트**: bounded work / timeout. 백오프는 thundering load 완화.

**공유 설계**: #2·#3은 `pollingError`(또는 `'polling'|'error'|'timeout'` 상태값) 1개 + 카운터 2개(연속실패, 시도횟수)로 함께 처리한다. 둘 다 종료 = `stopPolling()` + 사용자 표시이므로 같은 기계장치.

**중요 디테일**: `handleReanalyze`가 폴링을 재시작하므로, **카운터 리셋을 `startPolling` 안에** 넣어야 재분석 시 예산이 새로 주어진다.

### #1 새로고침 복원 (rehydration, 별도 조각·후순위)

**문제**: 새로고침하면 `uploadedImages`가 리셋 → `totalCount=0` → 진행 중이던 분석을 못 따라간다.

**계획**:
- `onMounted`에서 `createReview`(멱등 처리됨 → 기존 draft의 `photoGroupId` 반환)로 그룹 id를 받은 뒤, `getReviewPhotos`로 서버 사진을 불러와 `uploadedImages`를 **서버 상태로 시드**한다.
- 매핑: 서버 사진 → `{ id, url, status, summary, orderIndex, uploading:false }`.
- 시드 후 `PENDING`이 하나라도 있으면 `startPolling()` → 새로고침해도 이어진다.

**선결 컨트랙트 확인**: `ReviewPhoto` 엔티티가 사진 URL을 `fileUrl`로 주는지 `url`로 주는지(`PhotoUploader`는 `url` 기대), `orderIndex`를 주는지 확인 후 매핑 확정.

**우선순위 메모**: "일회성 플로우" 결정상 원래 가치가 낮았으나, `createReview` 멱등화로 데이터가 서버에 남아 품이 줄었다. 그래도 #2·#3보다 뒤 — 컨트랙트 확인 + 새 상태 흐름이 더 큼.

---

## 3. 추천 순서

1. **#2 + #3 한 조각** — 작고 컨트랙트 리스크 없음. 견고성 즉시 상승.
2. **#1 따로** — `ReviewPhoto` URL/order 필드 확인 후.

**과한 추상화 경고**: 셋 다 폴링 한 곳에서만 쓰므로 컴포넌트 안 인라인 유지. 재사용이 실제로 생기면 그때 `useReviewPhotoPolling.js`로 추출(참고: `front-review-refactor-v2.md`에 해당 컴포저블 계획이 있으나 현재는 인라인 상태).

---

## 4. 검증

- **#2**: 백엔드 내리거나 네트워크 차단 → 폴링이 3회 후 멈추고 에러 안내가 뜨는지. 복구 후 재분석으로 다시 도는지.
- **#3**: 응답을 계속 `PENDING`으로 두고(또는 짧은 상한으로 임시 설정) → 상한 초과 시 타임아웃 안내가 뜨고 루프가 멈추는지.
- **#1**: 분석 중 새로고침 → 사진과 진행 상태가 복원되고 폴링이 이어지는지. 게시 완료 후 재진입 시엔 새 draft라 빈 상태인지.

---

## 5. 연관 변경 (완료)

- `ReviewPostService.createReview` 멱등화(get-or-create) — 같은 plan 재진입 시 빈 draft가 쌓이지 않고, #1 rehydration의 데이터 출처가 됨.
