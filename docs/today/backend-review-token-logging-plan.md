# Backend Review Token Usage Logging Plan

> 대상 도메인: `tripflow-ai-backend` 의 `travelgram/review` (리뷰 영역만)
> 목표: 리뷰 영역 AI 호출의 **토큰 사용량(prompt/completion/total)** 을 로그에 남겨, 이미 있는 응답 시간 로그와 합쳐 **AI 호출 비용 관찰가능성**을 완성한다.

---

## 1. 배경

리뷰 영역 AI 호출은 이미 **응답 시간(`elapsedMs`)** 을 `ReviewAiLog`로 남기고 있다. 하지만 **토큰 사용량은 측정조차 안 하고 버린다.** 운영에서 "이 분석이 얼마나 걸렸나"는 보이는데 "얼마를 썼나(비용)"는 안 보인다.

- 응답 시간은 Service 계층에서 `System.nanoTime()`으로 재고 `ReviewAiLog`에 기록 중. (`ReviewPhotoAnalysisService.java:38`, 그 외 `ReviewAiService.createAndSaveStyles`, `ReviewPostService.analyzeTripContext`)
- Spring AI(1.0.3) 응답 객체 `ChatResponse`에 토큰 사용량 메타데이터가 들어 있는데, Agent들이 `.call().content()`로 **문자열만 뽑고 응답 객체를 버려서** 토큰 정보가 사라진다.

즉 "로그 인프라가 없는" 게 아니라, **이미 있는 로그(`elapsedMs`)에 토큰 한 축만 더 얹으면** 되는 상태다.

---

## 2. 현재 상태와 한계

### 2.1 Agent가 토큰을 버린다

리뷰 영역 AI 호출 지점(모두 `.call().content()`):

| 위치 | 메서드 | 용도 |
|---|---|---|
| `ReviewImageAnalysisAgent.java:63` | `analyzeReviewImage` | 사진 1장 → 한국어 요약 (Vision) |
| `ReviewImageAnalysisAgent.java:105` | `analyzeTripContext` | 요약들 → 분위기/여행유형 |
| `ReviewStyleGenerateAgent` | `generateStyles` | 4종 스타일 캡션/해시태그 |
| `PlanTitleGenerateAgent` | `generatePlanTitle` | 플랜 제목 |
| `TrendSearchAgent` | `generateTrend` | 트렌드 (tool-use 포함) |

`.content()`는 `ChatResponse`에서 텍스트만 꺼내는 메서드다. `.call().chatResponse()`로 받으면 `getMetadata().getUsage()`로 토큰에 접근할 수 있는데, 지금은 그 경로 자체를 안 탄다.

### 2.2 로그 스키마에 토큰 필드가 없다

`ReviewAiLog`(record, `ReviewAiLog.java:3`)는 `elapsedMs`까지만 있고 토큰 필드가 없다. 팩토리는 `start` / `success` / `fail` 세 개. (`:15` / `:19` / `:29`)

---

## 3. 설계

### 3.1 레이어 경계 — 로깅 책임은 Service가 유지

현재 패턴(응답 시간 측정·`ReviewAiLog` 기록을 **Service**가 소유, Agent는 외부 호출 어댑터)을 깨지 않는다.

- **Agent**: 외부 호출만 책임진다. 변경은 "content만 반환" → "content + 토큰 사용량 반환"으로 **반환값만 확장**.
- **Service**: Agent가 준 토큰을 기존 `elapsedMs`와 함께 `ReviewAiLog.success`에 실어 기록.

> Agent가 직접 로깅하지 않는 이유: 로깅/관찰 책임이 Service에 모여 있어야 한 곳만 보면 된다(책임 분리). Agent는 "OpenAI에 말 거는 어댑터"라는 단일 책임을 유지.

### 3.2 컨트랙트

**(a) 토큰 값객체 신규** — `travelgram/review/ai/log/AiTokenUsage.java`
```java
public record AiTokenUsage(Long promptTokens, Long completionTokens, Long totalTokens) {
    // ChatResponse에서 안전하게 추출. usage가 null이면 모든 값 null.
    public static AiTokenUsage from(ChatResponse response) { ... }
    public static AiTokenUsage empty() { return new AiTokenUsage(null, null, null); }
}
```
- Spring AI `Usage.getPromptTokens()/getCompletionTokens()/getTotalTokens()`는 `Integer`(null 가능) → `Long`으로 승격해 보관(기존 `ChatMemory.tokenUsage`가 `Long`인 것과 일관).
- **null-guard 필수**: `response`·`metadata`·`usage` 어느 단계든 null일 수 있음 → null이면 `empty()`.

**(b) `ReviewAiLog` 확장**
- 필드 3개 추가: `Long promptTokens, completionTokens, totalTokens` (모두 nullable).
- `success(...)`에 토큰을 받는 **오버로드** 추가(기존 시그니처는 토큰 null로 위임 → 하위호환).
- `fail(...)`은 그대로. 실패는 응답 자체가 없을 수 있어 토큰이 의미 없음(null).

**(c) Agent 반환값 확장** — 메서드별 작은 래퍼 record로 content + usage 동시 반환.
```java
// 예: 사진 분석
record AnalyzedImage(String summary, AiTokenUsage usage) {}
// agent 내부: .call().content() → .call().chatResponse()
ChatResponse resp = chatClient.prompt().messages(...).call().chatResponse();
return new AnalyzedImage(resp.getResult().getOutput().getText(), AiTokenUsage.from(resp));
```

### 3.3 Spring AI 1.0.3 호출 경로
`chatClient.prompt()...call()` → `CallResponseSpec.chatResponse()` → `ChatResponse.getMetadata().getUsage()` → `getPromptTokens()/getCompletionTokens()/getTotalTokens()`(Integer, null 가능).

---

## 4. 단계별 작업 순서

리뷰 가능한 단위로 쪼갠다. `→ CS`는 그 작업에 걸린 CS 개념 한 줄.

- [ ] **조각 1 — `AiTokenUsage` 값객체 + `from(ChatResponse)` null-guard**
  → CS: *null object* / 방어적 매핑 — 외부 응답의 누락 필드를 경계에서 흡수.
- [ ] **조각 2 — `ReviewAiLog` 토큰 3필드 + `success` 오버로드**
  기존 `success` 호출부는 토큰 null로 위임 → 하위호환.
  → CS: 로그 **스키마 확장**을 nullable로 — 기존 기록 경로를 안 깨고 점진 도입.
- [ ] **조각 3 (파일럿) — 사진 분석 경로 1줄기만 적용**
  `ReviewImageAnalysisAgent.analyzeReviewImage`가 content+usage 반환 → `ReviewPhotoAnalysisService`가 추출해 `success`에 토큰 실어 로깅. 단위 테스트(stub `ChatResponse`로 토큰 매핑 검증).
  → CS: 한 경로로 **수직 슬라이스** 먼저 검증 후 수평 전파.
- [ ] **조각 4 (전파) — 나머지 4개 호출에 동일 패턴 확장**
  `analyzeTripContext`, `ReviewStyleGenerateAgent`, `PlanTitleGenerateAgent`, `TrendSearchAgent`.
  → CS: 검증된 패턴의 반복 적용 — 변경 리스크 분산.
- [ ] **조각 5 (후속, 선택) — 비용 환산/집계**
  total 토큰 → 모델 단가로 비용 추정, 리뷰 1건당 누적 등. 별도 검토.
  → CS: 원시 지표(토큰) ↔ 파생 지표(비용)의 분리.

---

## 5. 검증

- [ ] **단위** — stub `ChatResponse`(usage 있음/없음 둘 다)로 `AiTokenUsage.from`이 정상 매핑/`empty()` 처리하는지. `ReviewAiLog.success` 오버로드가 토큰을 담는지.
- [ ] **라이브(bootRun)** — 사진 업로드 후 `ReviewAiLog` 로그에 `promptTokens/completionTokens/totalTokens`가 실제 숫자로 찍히는지. (응답 시간 `elapsedMs`와 한 줄에 같이)
- [ ] **회귀** — 기존 `success`/`fail` 호출부(토큰 안 주는 곳)·캐시 히트 경로가 토큰 null로 그대로 동작하는지.

---

## 6. 리스크와 대응

### 6.1 `usage`가 null일 수 있음
일부 응답/프로바이더에서 usage 메타데이터가 비어 올 수 있다. → `AiTokenUsage.from`에서 단계별 null-guard, null이면 `empty()`. 로그엔 토큰 null로 남고 NPE 없음.

### 6.2 시그니처 변경의 리플
Agent 반환 타입이 바뀌면 호출 Service와 기존 테스트가 영향받는다. → 파일럿(조각 3)으로 한 경로만 먼저 바꿔 리플 범위를 좁히고, 테스트를 같이 수정한 뒤 전파(조각 4).

### 6.3 실패/캐시 경로
실패 시엔 응답이 없어 토큰도 없음(`fail`은 그대로). `ReviewAiService`의 멱등성 캐시 히트 경로는 AI 호출이 없으므로 토큰 null로 둔다 — "캐시 히트라 토큰 0이 아니라 미측정(null)"임을 구분.
