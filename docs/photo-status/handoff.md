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
  - [x] A+ 결과값 검증: agent가 null/공백/"{}"/한글없음(영어 거절문 등)을 반환하면 예외→catch→FAILED. `isUsableKoreanSummary`(UnicodeScript.HANGUL). 테스트 `ReviewPhotoAnalysisServiceTest`(예외/성공/무효값 파라미터라이즈드) green.
- [x] B — 프론트 폴링 전환 + 차단 (CreateTravelReview.vue: 파생상태 status 기반(settled/success/failed), 폴링 종료=allSettled, canProceed=전부 SUCCESS, FAILED 안내 alert). summary 의존 제거.
- [~] C — 재시도 (서비스/S3 완료, 컨트롤러·프론트 잔여)
  - [x] `S3Service.downloadFile(fileUrl)→byte[]` (deleteFile key추출 재사용, try-with-resources). 테스트 `S3ServiceTest` green.
  - [x] `ReviewPhotoService.reanalyzePhoto(photoId)`: 조회→**다운로드→PENDING 리셋→async 재호출** 순서(PENDING 먼저 찍고 다운로드 실패하면 무한루프 재발하므로 순서 주의). contentType=확장자 추론(png 외 jpeg). 없는 photoId→IllegalArgumentException. 테스트 `ReviewPhotoServiceReanalyzeTest` green.
  - [ ] 컨트롤러 `POST /reviews/photo/{photoId}/reanalyze` (IllegalArgumentException→404 매핑 확인 필요)
  - [ ] 프론트: FAILED 사진 재시도 버튼(PhotoUploader)→재분석 호출→재폴링
  - [ ] (보류) "FAILED만 재분석 허용" 가드 — 현재는 SUCCESS/PENDING도 재분석됨. 컨트롤러 권한체크와 함께 결정.

## 테스트 인프라
- 프론트: Vitest + @vue/test-utils + jsdom 도입(`vitest.config.js`, `npm test`/`test:run`). 스모크 `NavigationButtons.spec.js` green.
