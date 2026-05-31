# 사진 분석 상태 컬럼 — 폴링 무한루프 방지

작성일: 2026-05-31

## 배경
- PHOTO_ANALYSIS는 업로드 시 `@Async`로 자동 트리거(`ReviewAnalysisService.analyzePhotoAndUpdateDb`). 프론트(`CreateTravelReview.vue`)는 `getReviewPhotos` 폴링으로 **`summary`가 non-null이면 완료**로 판정하고, 전부 완료돼야 폴링을 멈추고 다음 단계로 진행.
- 실패 시 `summary`가 null로 남아 → **폴링 무한루프 + 다음 단계 진입 불가**.
- 게다가 agent `analyzeReviewImage`가 예외를 삼키고 `"{}"`를 반환 → service의 `catch`가 안 타서 **실패가 성공으로 기록**됨.

## 정책 (확정)
- 부분 실패 = **막고 재시도**. 사진이 하나라도 FAILED/PENDING이면 다음 단계 차단. FAILED 사진은 **개별 재분석**.

## 핵심 결정 / 주의
- **agent 예외 전파가 전제**: `analyzeReviewImage`가 예외를 던져야 service `catch`에서 `FAILED`를 찍을 수 있음. 안 하면 이 작업 전체가 무의미.
- **상태조회는 신규 API 불필요**: `getReviewPhotos`가 이미 `List<ReviewPhoto>`(엔티티)를 폴링에 내려줌 → 엔티티에 `status` 한 칸 추가하면 응답에 자동 포함.
- **S3 다운로드 없음**: `S3Service`에 upload/delete만 있음. 재분석(C)에서 `downloadFile(fileUrl)→byte[]` 추가 필요. `deleteFile`의 URL→key 추출 로직 재사용.
- status 값: `PENDING` / `SUCCESS` / `FAILED`.

## 조각 A — 백엔드: 상태를 DB에 정확히 기록
- `schema.sql`: `review_photos.status` 추가 (`NOT NULL DEFAULT 'PENDING'`, CHECK in (PENDING, SUCCESS, FAILED))
- `ReviewPhoto` 엔티티에 `status` 필드, mapper select에 status 포함
- agent `analyzeReviewImage` 예외 전파(삼키기 제거)
- `ReviewAnalysisService.analyzePhotoAndUpdateDb`: 성공 → `SUCCESS`+summary, catch → `FAILED`
- → 실패가 비로소 `FAILED`로 남음

## 조각 B — 프론트: 폴링 전환 + 진행 차단
- `CreateTravelReview.vue` 폴링을 status 종료(SUCCESS/FAILED) 기준으로 중단(summary 의존 제거)
- `canProceed`: **전부 SUCCESS일 때만** 진행 (FAILED/PENDING 있으면 막기)
- FAILED 사진 표시
- → 무한루프 해결 + "막기" 완성

## 조각 C — 재시도
- `S3Service.downloadFile(fileUrl)→byte[]` 추가
- 재분석 API (`POST /reviews/photo/{photoId}/reanalyze`): 사진 조회 → S3 다운로드 → `analyzePhotoAndUpdateDb` 재호출(status PENDING부터)
- 프론트: FAILED 사진에 재시도 버튼 → 재분석 호출 → 다시 폴링
- → "재시도" 완성

## 범위 밖
- 폴링 타임아웃/최대횟수 가드 (선택적 견고성)
- `review_jobs` 테이블 (미사용, 별도 폐기 검토)

## 진행
- [x] A — 백엔드 상태 기록 (schema status 컬럼+CHECK, ReviewPhoto.status, mapper select/updatePhotoSummary(=SUCCESS)/updatePhotoStatus, dao, agent 예외전파, ReviewPhotoAnalysisService catch→FAILED). compileJava 통과.
- [ ] B — 프론트 폴링 전환 + 차단
- [ ] C — 재시도
