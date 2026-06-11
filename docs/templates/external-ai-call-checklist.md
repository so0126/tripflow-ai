# 외부 AI 호출(분석/생성) 추가 체크리스트

> 리뷰처럼 OpenAI 호출 단계를 새로 붙일 때 그대로 복사해 요청에 붙여넣는 스니펫.
> "①부터 하고 멈춰서 보여줘" 식으로 단계마다 검증 틈을 둔다.

---

## 붙여넣기용 (요청에 그대로 복사)

```
외부 AI 호출 단계를 추가/수정하려고 해. 아래 ①~⑥ 순서로 점검하면서,
①부터 하고 멈춰서 결과 보여줘.

① 단계 식별 — ReviewAiStep(PHOTO_ANALYSIS / TRIP_CONTEXT_ANALYSIS / STYLE_GENERATION)에
   새 단계가 있는가. 로그·에러가 "세 단계 중 어디"인지 드러나야 한다.

② 동기 vs 비동기 경계 — 실패가 어떻게 전달되는가
   - 동기(요청-응답 안): 실패 시 ReviewErrorCode 502(BAD_GATEWAY) 계열로 던진다
     (예: TRIP_CONTEXT_GENERATION_FAILED, STYLE_GENERATION_FAILED).
   - 비동기(@Async): 핸들러가 예외를 못 받는다 → status=FAILED 기록 + 프론트 폴링→재분석 복구.
   - @Async 메서드는 반드시 별도 @Service 클래스에 둔다(self-invocation은 프록시를 안 타서 무효).

③ 결과 검증 — 예외가 안 나도 쓸 수 있는 값인가
   - 모델이 거절문/빈 값/"{}"를 뱉을 수 있다. 예외만 믿지 말고 값 자체를 검증한다
     (예: isUsableKoreanSummary처럼 "쓸 수 있는 결과" 기준을 코드로 둔다).
   - 무효면 정상 실패 경로(②)로 흘려보낸다.

④ 멱등성 — 다시 불러도 안전한가
   - 이미 결과가 있으면 AI 재호출 없이 기존 결과를 반환한다.

⑤ 장애 대응 — 외부 API가 느리거나 죽으면
   - 타임아웃이 걸려 있는가(무한 대기로 스레드를 잡지 않는가).
   - 재시도를 넣는다면 몇 회/어떤 조건인지 명시한다.

⑥ 로그 — 시작/성공/실패 + 소요시간
   - ReviewAiLog.success/fail로 단계·소요시간(elapsedMs)을 남긴다(성공 info / 실패 error).
```

---

## 메모

- 이 체크리스트는 새로 배우는 게 아니라 `ReviewPhotoAnalysisService` 등에서 **이미 잘하고 있던 것**을 빠뜨리지 않도록 말로 굳힌 것.
- "항상 적용돼야 하는 짧은 규칙"(예: @Async 별도 클래스)은 `.claude/CLAUDE.md` / `AGENTS.md`로 옮겨도 좋다. 이 파일은 "리뷰 AI 붙일 때만" 쓰는 긴 버전.
- codex/다른 도구에도 그대로 붙여넣어 쓸 수 있다(이식성 목적).
