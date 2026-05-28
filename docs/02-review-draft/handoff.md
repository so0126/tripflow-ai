# H2. 실패 흔적 없음

## DB
```sql
ALTER TABLE ai_review_analysis
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'READY';
```
status: READY / FAILED

## 흐름
- 메서드 `@Transactional` 제거
- try { AI 호출 → saveReady } catch(e) { saveFailed }
- saveReady / saveFailed 각각 `@Transactional` →  **별도 `@Service`로 분리**
- catch: `log.error(reviewPostId, analysisId, cause)` + `RuntimeException(cause)` 전파

## H1 가드 분기
- READY → 기존 결과 반환
- FAILED → 실패 응답 (재시도 X)
- 없음 → 신규 생성

## 미결 결정
**FAILED 응답**: HTTP status 분리 vs 200 + 상태 필드 — 컨트롤러 보고 결정

## 작업 순서
1. schema.sql + DB ALTER
2. 트랜잭션 분리 방식 결정
3. saveReady / saveFailed 서비스 분리 + mapper XML
4. createAndSaveStyles 흐름 변경
5. H1 가드 분기 수정
6. 컨트롤러 응답 형태
7. 프론트 catch + 로딩 가드
8. 수동 검증 (정상 / AI 실패 / 새로고침 / FAILED 후 재요청)

## 범위 밖
PENDING, 재시도 API, M1, M2, UNIQUE 제약