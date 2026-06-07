# Backend Photo Analysis Async Executor Plan

> 대상: `tripflow-ai-backend/src/main/java/com/tripflow/ai/TripFlowAiApplication.java`, `tripflow-ai-backend/src/main/java/com/tripflow/ai/travelgram/review/service/ReviewPhotoAnalysisService.java`, 신규 `config/AsyncConfig.java`
> 목표: 리뷰 사진 AI 분석(`@Async`)을 Spring Boot 기본 스레드풀이 아니라 **사진 분석 전용 `ThreadPoolTaskExecutor`** 위에서 돌리고, 과부하 시 백프레셔/거부 정책을 명시한다.

---

## 1. 배경

리뷰 사진 업로드 후 AI 분석은 비동기로 처리된다.

- `TripFlowAiApplication`에 `@EnableAsync`가 켜져 있다. (`TripFlowAiApplication.java:13`)
- 실제 분석은 `ReviewPhotoAnalysisService.analyzePhotoAndUpdateDb(...)`가 맡고, 메서드에 `@Async`, `@Transactional`이 붙어 있다.
- 이 메서드는 OpenAI 호출 → 결과 검증 → `reviewPhotoDao.updatePhotoSummary` 또는 `updatePhotoStatus("FAILED")` 순으로 동작한다. 한 건당 수 초가 걸리는 **느린 I/O 작업**이다.

즉 "async 설정이 아예 없는" 상태가 아니라, **별도 Executor 빈을 정의하지 않아 Spring Boot 자동 구성 기본 풀에 얹혀 있는** 상태다.

---

## 2. 현재 기본값의 한계

`@Async`에 Executor를 지정하지 않으면, Spring Boot가 자동 구성한 `applicationTaskExecutor`(`spring.task.execution.*` 기본값)를 사용한다. 기본값은 대략 다음과 같다.

- core pool size: 8
- **queue capacity: 무제한 (`Integer.MAX_VALUE`)**
- max pool size: 무제한
- 스레드 이름: `task-1`, `task-2` …

여기서 문제 지점이 생긴다.

### 2.1 백프레셔가 없다

큐가 무제한이라, 사용자가 사진을 한꺼번에 많이 올리면 8개 스레드가 다 차고 나머지 작업이 **메모리 큐에 무한정 쌓인다.** 외부 OpenAI가 느려지거나 일시적으로 막히면 큐가 계속 커지면서 최악의 경우 OOM, 그리고 사용자 입장에서는 "분석이 영원히 안 끝나는" 체감 지연이 생긴다. 정석은 *bounded queue + rejection policy*로 유입 속도를 제한하는 것이다.

### 2.2 거부(reject) 정책이 정의되지 않음

큐가 무제한이면 "한계를 넘었을 때 무엇을 할지"를 정할 기회 자체가 없다. 유한 큐로 바꾸면 넘쳤을 때의 동작을 의도적으로 선택해야 한다.

### 2.3 관찰가능성

스레드 이름이 `task-1`이라, 운영 로그에서 "이 스레드가 사진 분석인지"를 구분할 수 없다. prefix를 주면 로그/스레드 덤프에서 바로 식별된다.

### 2.4 종료(shutdown) 시 작업 유실

배포로 앱이 내려갈 때 진행 중인 분석을 기다릴지 정해두지 않으면, 분석 중이던 사진이 `PENDING`에 멈춘 채 남을 수 있다.

---

## 3. 왜 "전용 Executor"인가 (B안)

선택지는 두 가지였다.

- **A안 — `application.yml`만 손보기**: `spring.task.execution.pool.queue-capacity`, `core-size`만 몇 줄 잡는다. 변경은 가장 작지만, 풀이 앱 전체 `@Async`와 공유된다.
- **B안 — 사진 분석 전용 `ThreadPoolTaskExecutor` 빈 + `@Async("photoAnalysisExecutor")`**: 사진 분석 워크로드를 다른 async 작업과 **격리**한다.

본 문서는 **B안**을 채택한다.

이유:

1. **격리(isolation)**: 사진 분석은 외부 API에 의존하는 느린 작업이다. 이게 폭주해도 향후 다른 `@Async` 작업(알림, 임베딩 등)의 풀을 잠식하지 않게 분리한다. CS 개념으로는 *bulkhead* 패턴.
2. **튜닝 독립성**: 사진 분석만의 pool size/queue/timeout을 그 워크로드 특성에 맞게 따로 조절할 수 있다.
3. **명시성**: `@Async("photoAnalysisExecutor")`로 "이 작업이 어느 풀에서 도는지"가 코드에 드러난다. 면접/리뷰에서 설명하기 좋다.

---

## 4. 설계

### 4.1 Executor 빈 정의

위치 예시: `tripflow-ai-backend/src/main/java/com/tripflow/ai/config/AsyncConfig.java`

```java
@Configuration
public class AsyncConfig {

    @Bean("photoAnalysisExecutor")
    public Executor photoAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);                 // 유한 큐 → 백프레셔
        executor.setThreadNamePrefix("photo-analysis-");
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()); // 넘치면 호출 스레드가 직접 실행 → 자연 속도 제한
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
```

값(4/8/50)은 출발점이다. 실제 동시 업로드량과 OpenAI 응답 시간을 보고 조정한다.

> 참고: `@EnableAsync`는 이미 `TripFlowAiApplication`에 있으므로 여기서 다시 붙이지 않는다.

### 4.2 분석 메서드에 풀 지정

`ReviewPhotoAnalysisService.analyzePhotoAndUpdateDb`의 `@Async`를 빈 이름과 함께 명시한다.

```java
@Async("photoAnalysisExecutor")
@Transactional
public void analyzePhotoAndUpdateDb(Long photoId, String contentType, byte[] imageBytes) { ... }
```

> 주의: `@Async`는 **프록시 기반**이라 같은 클래스 내부 호출(self-invocation)에서는 동작하지 않는다. 현재처럼 `ReviewPhotoService`가 **별도 빈인** `ReviewPhotoAnalysisService`를 주입받아 호출하는 구조를 그대로 유지해야 한다. (코드 주석에도 "반드시 별도 클래스에 있어야 `@Async`가 동작함"이라고 적혀 있는 그 이유다.)

### 4.3 거부 정책 선택

| 정책 | 동작 | 적합성 |
|---|---|---|
| `CallerRunsPolicy` | 요청 스레드가 직접 실행 → 유입 속도 자연 제한 | 1차 권장. 단, 업로드 요청 스레드가 분석 시간만큼 블로킹될 수 있음 유의 |
| `AbortPolicy`(기본) | `RejectedExecutionException` 던짐 | 호출부에서 잡아 즉시 `FAILED` 처리하고 재시도 UI로 넘길 거면 선택 가능 |

1차는 `CallerRunsPolicy`로 가되, 업로드 응답이 분석 때문에 느려지는 게 문제가 되면 `AbortPolicy` + 호출부에서 `FAILED` 기록으로 전환을 검토한다.

---

## 5. 단계별 작업 순서

- [ ] `config/AsyncConfig.java`에 `photoAnalysisExecutor` 빈 추가
- [ ] `ReviewPhotoAnalysisService`의 `@Async` → `@Async("photoAnalysisExecutor")`
- [ ] 호출 구조가 여전히 별도 빈 경유인지 확인 (self-invocation 없음)
- [ ] pool/queue 초기값 결정 및 주석으로 근거 남기기
- [ ] (선택) `AbortPolicy` 채택 시 호출부 거부 예외 → `FAILED` 처리 경로 추가

---

## 6. 검증

- [ ] `./gradlew bootRun` 후 사진 업로드 → 로그 스레드 이름이 `photo-analysis-*`로 찍히는지 확인
- [ ] 사진 여러 장 동시 업로드 → 풀 size만큼만 동시에 돌고 나머지는 큐에서 순차 처리되는지 확인
- [ ] 큐 한계를 넘는 양을 던졌을 때 거부 정책대로 동작하는지(`CallerRunsPolicy`면 호출 스레드 실행, `AbortPolicy`면 예외) 확인
- [ ] 분석 실패 사진이 여전히 `FAILED`로 기록되고 재분석 UI가 뜨는지 (기존 동작 회귀 없음)

---

## 7. 리스크와 대응

### 7.1 `CallerRunsPolicy`로 인한 업로드 응답 지연

큐가 가득 차면 업로드 요청 스레드가 분석을 직접 떠안아 응답이 느려진다.
대응: pool/queue를 실측 기반으로 충분히 잡거나, `AbortPolicy` + 즉시 `FAILED` 처리로 전환.

### 7.2 트랜잭션 경계

`@Async` 메서드는 호출 스레드와 **다른 스레드/다른 트랜잭션**에서 돈다. 현재처럼 메서드 자체에 `@Transactional`이 붙어 있어야 DB 업데이트가 정상 커밋된다. 이 조합을 깨지 않는다.

### 7.3 멀티 인스턴스

풀은 인스턴스별 in-memory다. 서버가 여러 대가 되면 인스턴스마다 독립 풀이 생긴다. 1차 단일 인스턴스 운영에서는 문제없고, 멀티 인스턴스로 가면 큐/백프레셔를 인프라 레벨(메시지 브로커)로 옮길지 별도 검토.

---

## 8. 최종 권장안

1. `photoAnalysisExecutor` 전용 `ThreadPoolTaskExecutor` 빈을 추가한다 (유한 큐 + 거부 정책 + 명명 + graceful shutdown).
2. 사진 분석 `@Async`를 이 풀로 지정한다.
3. 별도 빈 경유 호출 구조와 메서드 단위 `@Transactional`을 유지한다.
4. pool/queue/거부 정책은 실측 후 조정한다.

이렇게 하면 "동작은 하지만 기본값에 맡겨둔" 비동기 처리를, 과부하에도 예측 가능하게 버티고 운영 로그에서 식별 가능한 구조로 바꿀 수 있다.
