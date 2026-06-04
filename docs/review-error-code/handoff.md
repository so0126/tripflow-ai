# Review 도메인 에러코드 핸드오프

## 현황

현재 리뷰 서비스에서 예외 처리는 `IllegalArgumentException`, `RuntimeException` 등 raw exception으로 던지고 있음.
프로젝트의 공통 에러 처리 구조(`BusinessException` + `BaseErrorCode`)를 사용하지 않아 일관성이 깨져 있음.

---

## 해야 할 것: `ReviewErrorCode` enum 생성

경로: `src/main/java/com/tripflow/ai/travelgram/review/exception/ReviewErrorCode.java`

참고 패턴: `SampleErrorCode.java` 복사 후 아래 에러코드 추가

```java
public enum ReviewErrorCode implements BaseErrorCode {

    // 400
    REVIEW_PHOTO_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 사진 파일이 비어있습니다."),

    // 404
    REVIEW_PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사진을 찾을 수 없습니다."),
    REVIEW_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리뷰 포스트를 찾을 수 없습니다."),

    // 500
    REVIEW_PHOTO_S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 업로드에 실패했습니다."),
    REVIEW_PHOTO_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일을 읽는 데 실패했습니다."),
    REVIEW_AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 사진 분석에 실패했습니다."),
    REVIEW_AI_INVALID_SUMMARY(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 결과가 유효하지 않습니다.");
}
```

---

## 교체 대상 목록

### `ReviewPhotoService.java`

| 위치 | 기존 코드 | 교체 코드 |
|------|-----------|-----------|
| `processSinglePhotoUpload()` - 파일 비어있음 | `throw new IllegalArgumentException("file is empty")` | `throw new BusinessException(ReviewErrorCode.REVIEW_PHOTO_EMPTY)` |
| `processSinglePhotoUpload()` - S3 업로드 실패 | `throw new RuntimeException("S3 upload failed", e)` | `throw new BusinessException(ReviewErrorCode.REVIEW_PHOTO_S3_UPLOAD_FAILED)` |
| `processSinglePhotoUpload()` - 이미지 바이트 읽기 실패 | `throw new RuntimeException("이미지 바이트 읽기 실패", e)` | `throw new BusinessException(ReviewErrorCode.REVIEW_PHOTO_READ_FAILED)` |
| `reanalyzePhoto()` - 사진 없음 | `throw new IllegalArgumentException("재분석할 사진을 찾을 수 없습니다...")` | `throw new BusinessException(ReviewErrorCode.REVIEW_PHOTO_NOT_FOUND)` |

### `ReviewPostService.java`

| 위치 | 기존 코드 | 교체 코드 |
|------|-----------|-----------|
| `analyzeTripContext()` - catch 블록 | `throw new RuntimeException(e)` | `throw new BusinessException(ReviewErrorCode.REVIEW_AI_ANALYSIS_FAILED)` |

### `ReviewPhotoAnalysisService.java`

| 위치 | 기존 코드 | 비고 |
|------|-----------|------|
| `analyzePhotoAndUpdateDb()` - `isUsableKoreanSummary` 실패 | `throw new IllegalStateException(...)` | 내부에서 catch → `FAILED` 상태로 DB 기록되므로 에러코드 적용 시 `REVIEW_AI_INVALID_SUMMARY` 사용. 상태 기록 로직은 유지할 것. |

---

## 참고 파일

- 에러코드 인터페이스: `common/global/exception/errorcode/BaseErrorCode.java`
- 공통 예외 클래스: `common/global/exception/BusinessException.java`
- 샘플 에러코드: `common/global/exception/errorcode/SampleErrorCode.java`
- 글로벌 에러코드: `common/global/exception/errorcode/GlobalErrorCode.java`
