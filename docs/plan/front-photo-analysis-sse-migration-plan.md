# Front Photo Analysis SSE Migration Plan

> 대상: `tripflow-ai-frontend/src/composables/travelgram/review/useReviewPhotoPolling.js`, `tripflow-ai-backend/src/main/java/com/tripflow/ai/travelgram/review/**`, `tripflow-ai-backend/src/main/java/com/tripflow/ai/common/sse/**`
> 목표: 리뷰 사진 AI 분석 상태 확인을 3초 간격 polling에서 서버 주도 SSE(Server-Sent Events)로 전환한다.

---

## 1. 배경

현재 리뷰 사진 업로드 플로우는 다음 순서로 동작한다.

1. 프론트 `PhotoUploader`가 사진 업로드를 시작한다.
2. 업로드 성공 후 프론트는 `startPolling()`을 호출한다.
3. `useReviewPhotoPolling`은 `setInterval(checkAnalysisStatus, 3000)`으로 3초마다 `/reviews/photos?photoGroupId=...`를 호출한다.
4. 백엔드는 현재 DB에 저장된 `ReviewPhoto.status`와 `summary`를 반환한다.
5. 프론트는 응답 배열을 기존 `uploadedImages`에 매핑하고, 모든 사진이 `SUCCESS` 또는 `FAILED`가 되면 polling을 종료한다.

이 구조는 구현이 단순하지만, AI 분석이 비동기로 끝나는 시점을 서버가 이미 알고 있는데도 클라이언트가 계속 물어봐야 한다는 비효율이 있다.

---

## 2. 현재 polling 구조의 한계

### 2.1 불필요한 요청 발생

사진 분석이 60초 걸리고 polling 주기가 3초라면, 사용자 1명당 약 20번의 상태 조회 요청이 발생한다. 사진 수가 많거나 동시 사용자가 늘면 `/reviews/photos` 조회 트래픽이 선형으로 증가한다.

### 2.2 최대 3초 지연

분석이 완료되어도 다음 polling tick까지 UI 반영이 늦어진다. SSE로 바꾸면 분석 완료 직후 서버가 이벤트를 보내므로 사용자가 더 빠르게 상태 변화를 볼 수 있다.

### 2.3 종료 조건이 약함

기존 hardening 문서에서 정리한 것처럼 polling 루프는 `allSettled` 중심으로 종료된다. 요청 실패, 장시간 `PENDING`, 브라우저 탭 전환 등 예외 상황을 별도로 다루지 않으면 무한 polling 또는 unhandled rejection이 발생할 수 있다.

### 2.4 서버에 이미 SSE 골격이 있음

백엔드에는 공통 SSE 컨트롤러와 서비스가 이미 존재한다. 다만 현재는 리뷰 사진 분석 완료 시점과 연결되어 있지 않다. 따라서 새 기술을 처음 도입하는 것보다는 기존 SSE 기반을 리뷰 사진 분석 이벤트에 맞게 다듬는 작업에 가깝다.

---

## 3. 전환 목표

### 3.1 기능 목표

- 사진 업로드 후 분석 상태를 polling이 아니라 SSE 이벤트로 갱신한다.
- 서버는 사진별 분석 완료/실패 시 이벤트를 발행한다.
- 프론트는 이벤트를 받아 해당 photoId의 `status`, `summary`만 부분 업데이트한다.
- 모든 사진이 `SUCCESS` 또는 `FAILED`가 되면 SSE 연결을 닫는다.
- SSE 연결 실패 또는 브라우저/프록시 환경 문제를 대비해 fallback polling을 유지한다.

### 3.2 비기능 목표

- 요청 수를 줄인다.
- UI 반영 지연을 줄인다.
- 연결 누수와 emitter 누수를 방지한다.
- 새로고침 또는 재접속 시에도 서버 상태를 재동기화할 수 있게 한다.
- 기존 `getReviewPhotos` API는 초기 스냅샷 조회 및 fallback 용도로 유지한다.

---

## 4. 권장 아키텍처

### 4.1 이벤트 단위

사진 분석 결과는 사진 단위로 발행한다.

이벤트 이름 예시:

```text
review-photo-analysis
```

이벤트 payload 예시:

```json
{
  "photoGroupId": 123,
  "photoId": 456,
  "status": "SUCCESS",
  "summary": "맑은 바다 앞에서 촬영한 감성적인 여행 사진이에요."
}
```

실패 payload 예시:

```json
{
  "photoGroupId": 123,
  "photoId": 456,
  "status": "FAILED",
  "summary": null
}
```

### 4.2 구독 단위 선택

두 가지 선택지가 있다.

#### 선택지 A: 기존 `/subscribe/{userId}` 재사용

장점:
- 이미 존재하는 SSE endpoint를 활용할 수 있다.
- 향후 알림, 채팅, 추천 완료 등 사용자 단위 이벤트를 한 연결에서 처리할 수 있다.

단점:
- 프론트에서 이벤트 payload의 `photoGroupId`를 검사해 현재 화면과 관련 없는 이벤트를 걸러야 한다.
- 사용자별 emitter 관리와 인증/권한 검증이 중요해진다.

#### 선택지 B: 리뷰 사진 분석 전용 endpoint 추가

예시:

```text
GET /reviews/photos/analysis-events?photoGroupId=123
```

장점:
- 화면/도메인 목적이 명확하다.
- 프론트 composable이 단순해진다.
- 해당 photoGroupId 화면을 떠날 때 연결을 닫기 쉽다.

단점:
- SSE endpoint가 도메인별로 늘어난다.
- 사용자 단위 통합 알림 구조와는 별개로 관리해야 한다.

### 4.3 권장안

1차 전환은 **선택지 B: 리뷰 사진 분석 전용 endpoint**를 권장한다.

이유:
- 현재 요구사항은 사용자 전체 알림이 아니라 특정 리뷰 작성 화면의 사진 분석 상태 동기화다.
- `photoGroupId`가 이미 프론트/백엔드 컨텍스트의 중심이다.
- 기존 공통 `SseService`는 보강 후 내부 구현으로 재사용하되, 외부 API는 도메인 전용으로 노출하는 편이 명확하다.

단, 장기적으로 사용자 알림/채팅/여행 추천 완료 등 여러 SSE 이벤트를 하나의 연결로 통합할 계획이 있다면 `/subscribe/{userId}` 기반으로 확장해도 된다.

### 4.4 설정 방법

SSE 전환 시 "코드만 추가"하면 끝나는 것이 아니라, 브라우저 `EventSource`, Spring MVC, CORS/인증, 개발 proxy, 운영 proxy 설정이 맞아야 한다. 1차 구현 기준 설정은 아래처럼 잡는다.

#### 4.4.1 백엔드 response 설정

SSE endpoint는 반드시 `text/event-stream`으로 내려야 한다. Spring MVC에서는 controller mapping에 `produces = MediaType.TEXT_EVENT_STREAM_VALUE`를 명시한다.

```java
@GetMapping(value = "/photos/analysis-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribePhotoAnalysis(@RequestParam("photoGroupId") Long photoGroupId) {
    return reviewPhotoAnalysisSseService.subscribe(photoGroupId);
}
```

전송 이벤트는 이름을 고정한다. 프론트는 이 이름으로 listener를 단다.

```java
emitter.send(
    SseEmitter.event()
        .name("review-photo-analysis")
        .data(event)
);
```

연결 직후에는 프론트가 연결 성공을 빠르게 알 수 있도록 `connect` 이벤트를 한 번 보내는 것을 권장한다.

```java
emitter.send(SseEmitter.event().name("connect").data("connected"));
```

#### 4.4.2 CORS/인증 설정

현재 프론트 axios는 `localStorage`의 `jwtToken`을 `Authorization: Bearer ...` 헤더로 붙인다. 하지만 브라우저 기본 `EventSource`는 커스텀 `Authorization` 헤더를 붙일 수 없다. 따라서 SSE 인증 설정은 아래 중 하나로 결정해야 한다.

| 방식 | 설정 | 장점 | 주의점 |
|---|---|---|---|
| Same-origin/proxy | 프론트에서 `/reviews/photos/analysis-events?...`처럼 상대 경로로 연결 | 기본 `EventSource` 사용 가능 | 개발/운영 proxy가 SSE를 통과시켜야 함 |
| Cookie/session | `new EventSource(url, { withCredentials: true })` | 표준 API 사용 가능 | 백엔드 CORS에서 `allowCredentials(true)`와 명시 origin 필요 |
| EventSource polyfill | polyfill로 Authorization header 전달 | Bearer token 유지 가능 | 의존성 추가 및 브라우저 호환성 검토 필요 |
| Query token | `?token=...` | 구현은 쉬움 | URL 로그/히스토리 노출 위험으로 비권장 |

1차 구현은 현재 SecurityConfig가 대부분 endpoint를 `permitAll`로 열어둔 상태라면 **Same-origin/proxy + 상대 경로 EventSource**가 가장 단순하다. 다만 인증을 실제로 강제할 계획이면 Cookie/session 또는 polyfill 중 하나를 먼저 정해야 한다.

CORS를 credential 기반으로 바꿀 경우에는 `allowedOriginPatterns("*")`와 `allowCredentials(true)` 조합을 피하고, 프론트 origin을 명시한다.

```java
registry.addMapping("/**")
    .allowedOrigins("http://localhost:8080", "http://localhost:5173")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

#### 4.4.3 프론트 EventSource URL 설정

현재 API base URL은 `process.env.VUE_APP_API_BASE_URL`을 사용한다. SSE도 같은 base URL 정책을 따라야 한다. 인증 header가 필요 없는 1차 구현이면 아래처럼 설정한다.

```js
const createReviewPhotoAnalysisEventSource = (photoGroupId) => {
  const baseURL = process.env.VUE_APP_API_BASE_URL || ''
  const query = new URLSearchParams({ photoGroupId: String(photoGroupId) })

  return new EventSource(`${baseURL}/reviews/photos/analysis-events?${query.toString()}`)
}
```

Cookie/session 기반이면 두 번째 인자를 추가한다.

```js
return new EventSource(url, { withCredentials: true })
```

주의: `EventSource`는 axios interceptor를 타지 않는다. 즉 `src/api/axios.js`에 있는 Authorization header 자동 주입은 SSE 연결에는 적용되지 않는다.

#### 4.4.4 개발 proxy 설정

프론트 dev server와 백엔드 포트가 다르면 cross-origin 이슈가 생긴다. 가장 단순한 방법은 Vue CLI dev server proxy를 써서 브라우저 입장에서는 same-origin으로 보이게 하는 것이다.

`tripflow-ai-frontend/vue.config.js`가 없다면 추가 예시는 다음과 같다.

```js
const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  devServer: {
    proxy: {
      '/reviews': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

이 방식을 쓰면 프론트 SSE URL은 baseURL 없이 상대 경로로 둘 수 있다.

```js
new EventSource(`/reviews/photos/analysis-events?photoGroupId=${photoGroupId}`)
```

#### 4.4.5 운영 proxy 설정

Nginx 같은 reverse proxy를 쓰면 SSE 응답을 buffering하지 않도록 설정해야 한다. 예시:

```nginx
location /reviews/photos/analysis-events {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Connection '';
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 1h;
    add_header X-Accel-Buffering no;
}
```

운영 환경에서 ALB/Cloudflare 같은 중간 proxy를 쓰면 idle timeout도 확인해야 한다. timeout보다 짧은 주기로 heartbeat comment 또는 heartbeat event를 보내는 설정을 권장한다.

#### 4.4.6 빠른 동작 확인 방법

백엔드가 실행 중이면 브라우저 대신 `curl`로 stream header와 이벤트 형식을 먼저 확인한다.

```bash
curl -N -H "Accept: text/event-stream" "http://localhost:8080/reviews/photos/analysis-events?photoGroupId=123"
```

정상이라면 연결이 바로 종료되지 않고, `event: connect` 또는 heartbeat가 보인다. 이후 테스트용으로 분석 완료 이벤트를 강제로 발행하거나 실제 사진 업로드를 수행해 `event: review-photo-analysis`가 내려오는지 확인한다.

---

## 5. 백엔드 구현 계획

### 5.1 DTO 추가

위치 예시:

```text
tripflow-ai-backend/src/main/java/com/tripflow/ai/travelgram/review/dto/response/ReviewPhotoAnalysisEvent.java
```

필드:

- `Long photoGroupId`
- `Long photoId`
- `String status`
- `String summary`

### 5.2 SSE emitter 저장 구조 개선

현재 공통 `SseService`는 `Map<String, SseEmitter>` 형태라 한 key에 한 연결만 저장한다. 리뷰 사진 전용으로는 아래 중 하나를 선택한다.

#### 간단안

```text
photoGroupId -> SseEmitter
```

한 브라우저 탭에서만 해당 리뷰 작성 화면을 연다는 가정이면 충분하다.

#### 안전안

```text
photoGroupId -> Set<SseEmitter>
```

새로고침, 중복 탭, 모바일/데스크톱 동시 접속까지 고려하면 안전안이 낫다.

권장: **안전안**.

### 5.3 리뷰 사진 분석 SSE 서비스 추가

위치 예시:

```text
tripflow-ai-backend/src/main/java/com/tripflow/ai/travelgram/review/service/ReviewPhotoAnalysisSseService.java
```

책임:

- `subscribe(photoGroupId)`
- `sendAnalysisResult(photoGroupId, event)`
- `removeEmitter(photoGroupId, emitter)`
- 연결 직후 `connect` 이벤트 또는 comment heartbeat 전송
- 전송 실패 시 emitter 제거 및 complete 처리

주의점:

- emitter가 없어도 예외를 던지지 않는다. 사용자가 화면을 떠난 상태일 수 있으므로 로그만 남긴다.
- `onCompletion`, `onTimeout`, `onError`에서 map 정리를 반드시 수행한다.
- timeout은 너무 짧게 잡지 않는다. 예: 30분 또는 1시간.
- 프록시 idle timeout이 있으면 heartbeat를 고려한다.

### 5.4 Controller endpoint 추가

위치: `ReviewController` 또는 별도 `ReviewPhotoAnalysisSseController`

예시:

```java
@GetMapping(value = "/photos/analysis-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter subscribePhotoAnalysis(@RequestParam("photoGroupId") Long photoGroupId) {
    return reviewPhotoAnalysisSseService.subscribe(photoGroupId);
}
```

권장: 기존 `ReviewController`가 커지고 있으므로 **별도 Controller**를 두는 편이 좋다.

### 5.5 분석 완료/실패 시 이벤트 발행

`ReviewPhotoAnalysisService.analyzePhotoAndUpdateDb`에서 DB 업데이트 직후 이벤트를 보낸다.

성공 흐름:

1. AI 분석 성공
2. `reviewPhotoDao.updatePhotoSummary(photoId, summary)`
3. `photoGroupId` 조회 또는 메서드 파라미터로 전달
4. `sendAnalysisResult(photoGroupId, SUCCESS event)`

실패 흐름:

1. catch 진입
2. `reviewPhotoDao.updatePhotoStatus(photoId, "FAILED")`
3. `photoGroupId` 조회 또는 메서드 파라미터로 전달
4. `sendAnalysisResult(photoGroupId, FAILED event)`

### 5.6 photoGroupId 전달 방식

현재 `analyzePhotoAndUpdateDb` 파라미터는 `photoId`, `contentType`, `imageBytes`다. SSE 이벤트에는 `photoGroupId`가 필요하므로 아래 중 하나를 선택한다.

#### 선택지 A: `photoId`로 DB 재조회

분석 완료/실패 후 `reviewPhotoDao.selectReviewPhotoById(photoId)`로 `photoGroupId`를 얻는다.

장점:
- 호출부 변경이 작다.

단점:
- 분석 완료마다 조회 1회가 추가된다.

#### 선택지 B: `analyzePhotoAndUpdateDb`에 `photoGroupId` 파라미터 추가

업로드/재분석 호출 시점에 이미 photoGroupId를 알고 있으므로 같이 넘긴다.

장점:
- 추가 조회가 없다.
- 이벤트 발행에 필요한 컨텍스트가 명확하다.

단점:
- 호출부 메서드 시그니처를 모두 수정해야 한다.

권장: **선택지 B**. 변경 범위가 크지 않고 이벤트 발행 컨텍스트가 명확해진다.

### 5.7 기존 `/reviews/photos` 유지

SSE로 전환해도 기존 목록 조회 API는 제거하지 않는다.

용도:

- 페이지 진입 시 초기 스냅샷 조회
- 새로고침 복원
- SSE 연결 실패 시 fallback polling
- 디버깅/운영 확인

---

## 6. 프론트 구현 계획

### 6.1 API 함수 추가

`travelgramApi.js`에 EventSource 생성 helper를 추가한다.

예시:

```js
const createReviewPhotoAnalysisEventSource = (photoGroupId) => {
  const baseURL = api.defaults.baseURL || ''
  const url = `${baseURL}/reviews/photos/analysis-events?photoGroupId=${encodeURIComponent(photoGroupId)}`
  return new EventSource(url, { withCredentials: true })
}
```

주의:

- 현재 axios instance의 baseURL, credential 정책, proxy 설정을 확인해야 한다.
- EventSource는 커스텀 헤더 설정이 어렵다. 인증이 Authorization header 기반이면 polyfill 또는 cookie 기반 인증/토큰 query 전략을 검토해야 한다.
- query token은 로그 노출 위험이 있으므로 가능하면 cookie/session 기반이 낫다.

### 6.2 composable 교체

현재 `useReviewPhotoPolling`을 바로 삭제하기보다 단계적으로 간다.

1. `useReviewPhotoAnalysisEvents.js` 추가
2. 내부에서 SSE를 우선 사용
3. SSE 연결 실패 시 기존 polling 함수 또는 fallback timer 사용
4. 안정화 후 `useReviewPhotoPolling` 제거 또는 fallback 전용으로 축소

반환값은 기존 view 변경을 줄이기 위해 최대한 유지한다.

기존 반환값:

- `totalCount`
- `settledCount`
- `successCount`
- `failedCount`
- `allSettled`
- `isAnalyzing`
- `canProceed`
- `startPolling`
- `stopPolling`
- `checkAnalysisStatus`
- `handleReanalyze`

전환 후 권장 반환값:

- `totalCount`
- `settledCount`
- `successCount`
- `failedCount`
- `allSettled`
- `isAnalyzing`
- `canProceed`
- `startAnalysisEvents`
- `stopAnalysisEvents`
- `syncAnalysisStatus`
- `handleReanalyze`
- `connectionStatus`: `'idle' | 'connecting' | 'open' | 'fallback' | 'closed' | 'error'`
- `analysisEventError`

### 6.3 이벤트 처리 로직

SSE 이벤트 수신 시:

1. payload JSON parse
2. `payload.photoGroupId`가 현재 `reviewStore.photoGroupId`와 같은지 확인
3. `uploadedImages`에서 `photoId` 매칭
4. 해당 이미지의 `status`, `summary` 업데이트
5. `allSettled`면 `stopAnalysisEvents()`

예시:

```js
source.addEventListener('review-photo-analysis', (event) => {
  const payload = JSON.parse(event.data)

  if (String(payload.photoGroupId) !== String(reviewStore.photoGroupId)) return

  const image = uploadedImages.value.find((item) => String(item.id) === String(payload.photoId))
  if (!image) return

  image.status = payload.status
  image.summary = payload.summary

  if (allSettled.value) stopAnalysisEvents()
})
```

### 6.4 연결 시작 시점

현재는 `PhotoUploader`의 `@upload-started="startPolling"`에서 polling을 시작한다.

SSE 전환 후에는 다음 중 하나를 선택한다.

#### 선택지 A: 업로드 시작 전에 SSE 연결

장점:
- 아주 빠르게 끝나는 분석 이벤트도 놓치지 않는다.

단점:
- 업로드 실패 시 불필요한 연결이 생길 수 있다.

#### 선택지 B: 업로드 성공 후 SSE 연결 + 즉시 스냅샷 조회

장점:
- 불필요한 연결이 적다.
- 기존 구조와 비슷하다.

단점:
- 업로드 직후 매우 빠르게 끝난 이벤트는 놓칠 수 있다. 단, 즉시 스냅샷 조회로 보완 가능하다.

권장: **선택지 A 또는 B+즉시 스냅샷**. 구현 안정성은 B+즉시 스냅샷이 좋다.

### 6.5 재분석 흐름

현재 재분석은 프론트에서 해당 image를 `PENDING`으로 바꾸고 `/reviews/photo/{photoId}/reanalyze` 호출 후 polling을 재시작한다.

SSE 전환 후:

1. image.status = `PENDING`
2. SSE 연결이 닫혀 있으면 다시 연결
3. `reanalyzePhoto(photoId)` 호출
4. 서버가 완료/실패 이벤트 발행
5. 이벤트 수신 후 UI 업데이트

주의:

- 재분석 버튼은 현재처럼 `FAILED`에서만 노출하는 것이 좋다.
- 서버도 이미 `FAILED` 상태만 재분석 허용하는 정책을 유지한다.

### 6.6 fallback polling

SSE가 항상 안정적으로 동작한다고 가정하지 않는다.

fallback 조건 예시:

- `EventSource.onerror` 발생
- 연결 시작 후 N초 동안 `open`이 되지 않음
- 브라우저가 EventSource를 지원하지 않음

fallback 정책:

- 기존 `getReviewPhotos`를 사용하되 hardening을 적용한다.
- 최대 시도 횟수 또는 최대 시간 제한을 둔다.
- 연속 실패 3회 이상이면 사용자에게 새로고침/재시도 안내를 노출한다.

---

## 7. 단계별 작업 순서

### Phase 1. 백엔드 SSE 안정화

- [ ] `SseService` 또는 신규 SSE 서비스에서 emitter null-safe 처리
- [ ] `onCompletion`, `onTimeout`, `onError` 정리 로직 보강
- [ ] 여러 연결을 지원할지 결정하고 자료구조 확정
- [ ] `TEXT_EVENT_STREAM_VALUE` produces 명시
- [ ] 연결 직후 `connect` 이벤트 또는 heartbeat 전송

### Phase 2. 리뷰 사진 분석 이벤트 발행

- [ ] `ReviewPhotoAnalysisEvent` DTO 추가
- [ ] 리뷰 사진 분석 SSE service/controller 추가
- [ ] `ReviewPhotoAnalysisService`에 SSE service 주입
- [ ] 분석 성공 시 `SUCCESS` 이벤트 발행
- [ ] 분석 실패 시 `FAILED` 이벤트 발행
- [ ] 재분석 성공/실패도 동일 이벤트로 처리

### Phase 3. 프론트 SSE composable 추가

- [ ] `createReviewPhotoAnalysisEventSource(photoGroupId)` API helper 추가
- [ ] `useReviewPhotoAnalysisEvents` composable 추가
- [ ] 기존 computed 상태값 유지
- [ ] 이벤트 수신 시 photoId 기준 부분 업데이트
- [ ] allSettled 시 연결 close
- [ ] unmount 시 연결 close

### Phase 4. fallback 및 복원

- [ ] SSE 연결 실패 시 fallback polling으로 전환
- [ ] fallback polling에 timeout/연속 실패 제한 적용
- [ ] 페이지 진입 시 `/reviews/photos` 초기 스냅샷 조회
- [ ] 새로고침 후 `PENDING` 사진이 있으면 SSE 재구독

### Phase 5. 정리

- [ ] 기존 `useReviewPhotoPolling` 이름/역할 정리
- [ ] 문서/주석에서 polling 중심 표현 수정
- [ ] 수동 테스트 시나리오 체크
- [ ] 운영 환경 proxy idle timeout 확인

---

## 8. 테스트 계획

### 8.1 백엔드 단위/통합 테스트

- [ ] SSE 구독 요청 시 `SseEmitter` 반환 확인
- [ ] 분석 성공 후 `review-photo-analysis` 이벤트 발행 확인
- [ ] 분석 실패 후 `FAILED` 이벤트 발행 확인
- [ ] 구독자가 없는 상태에서 이벤트 발행해도 예외가 전파되지 않는지 확인
- [ ] emitter timeout/completion/error 시 map에서 제거되는지 확인

### 8.2 프론트 테스트

- [ ] EventSource open 시 `connectionStatus = 'open'`
- [ ] `review-photo-analysis` 이벤트 수신 시 해당 이미지 상태 업데이트
- [ ] 현재 photoGroupId와 다른 이벤트는 무시
- [ ] 모든 사진 settle 시 EventSource close
- [ ] `onBeforeUnmount`에서 연결 close
- [ ] EventSource error 시 fallback polling 진입

### 8.3 수동 QA 시나리오

- [ ] 사진 1장 업로드 → SUCCESS 즉시 반영
- [ ] 사진 여러 장 업로드 → 각 사진별 완료 순서대로 반영
- [ ] AI 분석 실패 → FAILED 표시 및 재분석 버튼 노출
- [ ] 재분석 → PENDING 전환 후 SUCCESS/FAILED 이벤트 반영
- [ ] 업로드 직후 새로고침 → 기존 사진 스냅샷 복원 후 SSE 재구독
- [ ] 리뷰 작성 화면 이탈 → SSE 연결 종료
- [ ] SSE endpoint 강제 실패 → fallback polling으로 상태 반영

---

## 9. 리스크와 대응

### 9.1 EventSource 인증 문제

EventSource는 axios interceptor처럼 Authorization header를 자유롭게 붙이기 어렵다.

대응:
- 현재 인증 방식 확인
- cookie/session 기반이면 `withCredentials` 사용
- header 토큰 기반이면 event-source-polyfill 또는 backend token 교환 방식 검토

### 9.2 이벤트 유실

SSE 연결 전에 분석이 끝나면 이벤트를 놓칠 수 있다.

대응:
- 연결 시작 직후 `/reviews/photos`로 초기 스냅샷 조회
- 업로드 성공 직후에도 한 번 동기화
- `Last-Event-ID`까지 필요한지는 1차 구현에서는 보류

### 9.3 emitter 누수

브라우저가 비정상 종료되거나 네트워크가 끊기면 emitter가 map에 남을 수 있다.

대응:
- timeout 설정
- completion/timeout/error cleanup
- send 실패 시 remove
- 필요 시 heartbeat로 죽은 연결 정리

### 9.4 멀티 인스턴스 서버

서버가 여러 대면 인스턴스 A에 SSE 연결이 있고, 분석 완료 이벤트는 인스턴스 B에서 발생할 수 있다.

대응:
- 단일 인스턴스 개발/초기 운영에서는 in-memory emitter로 충분
- 멀티 인스턴스 운영이면 Redis pub/sub, message broker, sticky session 중 하나 필요
- 이 리스크는 배포 아키텍처 확정 후 별도 설계

### 9.5 프록시 buffering/timeout

Nginx, ALB, Cloudflare 등 중간 프록시가 SSE buffering 또는 idle timeout을 걸 수 있다.

대응:
- `Content-Type: text/event-stream` 확인
- proxy buffering off 설정 검토
- heartbeat comment/event 주기적 전송
- 운영 환경에서 장시간 연결 테스트

---

## 10. 최종 권장안

1. `/reviews/photos/analysis-events?photoGroupId=...` 전용 SSE endpoint를 추가한다.
2. 분석 성공/실패 시점에 `review-photo-analysis` 이벤트를 발행한다.
3. 프론트는 `EventSource` 기반 composable을 추가하되 기존 computed 계약은 유지한다.
4. 기존 `/reviews/photos`는 초기 스냅샷과 fallback polling 용도로 유지한다.
5. SSE 안정화 후 기존 polling 중심 composable을 정리한다.

이렇게 가면 현재 polling의 단순함을 크게 해치지 않으면서도, 요청 수 감소와 즉시 반영이라는 SSE의 장점을 얻을 수 있다.
