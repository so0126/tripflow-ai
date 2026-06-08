# Review: Photo/Hashtag Group 제거 — review_post_id 직접 참조로 평탄화

## 배경 (왜)

Travelgram 리뷰 도메인에는 사진과 해시태그를 묶는 중간 테이블이 있었다.

```
review_posts ──(photo_group_id)──▶ review_photo_groups ──◀(photo_group_id)── review_photos
review_posts ──(hashtag_group_id)─▶ review_hashtag_groups ─◀(hashtag_group_id)─ review_hashtags
```

문제는 **이 그룹이 `review_post`와 사실상 1:1**이라는 점이다. 한 리뷰에 사진 그룹은 1개, 해시태그 그룹도 1개만 존재한다. 즉 그룹 테이블은 "여러 묶음"을 표현하려고 만들어졌지만 실제로는 묶음이 하나뿐이라 **의미 없는 간접 계층(indirection)** 만 추가하고 있었다.

이 간접 계층 때문에 치르는 비용:

- **조회 비용**: 리뷰의 사진을 가져오려면 `review_posts → review_photo_groups → review_photos` 로 두 번 타고 들어가야 한다. 한 번의 JOIN/조회로 끝낼 일을 두 번에 나눠 한다.
- **양방향 FK의 순환 의존**: `review_posts.photo_group_id → review_photo_groups.id` 이면서 동시에 `review_photo_groups.review_post_id → review_posts.id` 다. 두 테이블이 서로를 가리켜 INSERT 순서가 꼬이고(누굴 먼저 넣지?), 스키마 끝에서 `ALTER TABLE ... ADD CONSTRAINT` 로 FK를 뒤늦게 거는 회피책까지 필요했다.
- **데이터 정합성 표현이 약함**: "그룹은 항상 1개"라는 사실이 스키마에 드러나지 않는다. 코드가 암묵적으로 지켜야 하는 불변식이라 깨지기 쉽다.

> 정규화는 "중복을 없애는 것"이지 "계층을 늘리는 것"이 아니다. 1:1 관계를 별도 테이블로 쪼개는 건 정규화가 아니라 오버엔지니어링에 가깝다. 자식(`review_photos`)이 손자가 아니라 직접 자식이 되도록 평탄화(flatten)하는 게 맞다.

## 목표 구조

그룹 테이블을 없애고 자식이 `review_post_id` 를 직접 참조한다.

```
review_posts ──◀(review_post_id)── review_photos
review_posts ──◀(review_post_id)── review_hashtags
```

- `review_photo_groups`, `review_hashtag_groups` 테이블 삭제
- `review_posts.photo_group_id`, `review_posts.hashtag_group_id` 컬럼 삭제
- `review_photos.photo_group_id` → `review_photos.review_post_id`
- `review_hashtags.hashtag_group_id` → `review_hashtags.review_post_id`
- 양방향 FK / 후행 `ALTER TABLE` FK 제거 (단방향 `review_style_id` FK만 남김)

## 작업 — 1단계: 스키마 (완료)

`tripflow-ai-backend/src/main/resources/db/schema.sql`

| 변경 | 내용 |
|------|------|
| `review_posts` | `photo_group_id`, `hashtag_group_id` 컬럼 제거 |
| `review_photo_groups` | 테이블 정의 삭제 (상단 `DROP TABLE`에는 레거시 정리용으로 유지) |
| `review_hashtag_groups` | 테이블 정의 삭제 (상단 `DROP TABLE`에는 유지) |
| `review_photos` | `photo_group_id BIGINT REFERENCES review_photo_groups` → `review_post_id BIGINT NOT NULL REFERENCES review_posts(id) ON DELETE CASCADE` |
| `review_hashtags` | `hashtag_group_id BIGINT REFERENCES review_hashtag_groups` → `review_post_id BIGINT NOT NULL REFERENCES review_posts(id) ON DELETE CASCADE` |
| `ALTER TABLE review_posts` | photo/hashtag group FK 제거, `review_style_id` FK만 유지 |
| 인덱스 | `idx_review_photos_review_post_id`, `idx_review_hashtags_review_post_id` 추가 (post 단위 조회가 잦음) |

> schema.sql은 부팅 시 전체 재생성 스크립트다. 운영 DB가 이미 그룹 테이블을 가지고 있어도 상단 `DROP TABLE ... CASCADE` 가 먼저 정리하므로 별도 마이그레이션 SQL 없이 재적용된다. (기존 데이터 보존이 필요하면 별도 `ALTER` 마이그레이션을 써야 하지만, 현재는 개발 단계라 재생성 전제.)

## 다음 조각 — 코드 마이그레이션 (TODO)

스키마만 바꾸면 그룹 테이블/컬럼을 참조하는 코드가 부팅·런타임에서 깨진다. 아래 순서로 **리뷰 가능한 작은 조각**으로 나눠 진행한다. (한 조각 = 한 PR 정도)

### 조각 A — 엔티티 정리
- `ReviewPhoto.photoGroupId` → `reviewPostId`
- `ReviewHashtag.hashtagGroupId` → `reviewPostId`
- `ReviewPost`: `photoGroupId`, `HashtagGroupId` 필드 삭제
- `ReviewPhotoGroup`, `ReviewHashtagGroup` 엔티티 클래스 삭제
- `ReviewCreateResponse`: `photoGroupId`, `hashtagGroupId` 필드 → 제거 또는 의미 재정의 (재진입 draft 응답이 그룹 id를 내려주던 부분)

### 조각 B — 매퍼 XML / DAO
- `ReviewPhotoMapper.xml`
  - `insertReviewPhotoGroup`, `selectPhotoGroupByPostId` 삭제
  - `insertReviewPhoto`: `photo_group_id` → `review_post_id`
  - `selectReviewPhotosByPhotoGroupId` → `selectReviewPhotosByReviewPostId` (WHERE `review_post_id`)
  - `selectPhotoSummariesByPhotoGroupId` → `...ByReviewPostId`
  - `updatePhotoOrder`: `photo_group_id` 조건 → `review_post_id`
  - (line 11 `SELECT id, ,created_at` 의 깨진 콤마도 같이 정리)
- `ReviewHashtagMapper.xml`
  - `insertHashtagGroup`, `selectHashtagGroupById`, `updateHashtagGroup`, `deleteHashtagGroupById` 삭제
  - `insertHashtagList`: `hashtag_group_id` → `review_post_id`
  - `deleteHashtagsByHashtagGroupId` → `deleteHashtagsByReviewPostId`
- `ReviewPostMapper.xml`
  - `insertDraft`: `photo_group_id, hashtag_group_id` 컬럼 제거
  - `selectDraftByPlanId` / `reviewCreateResponseMap`: 그룹 id 컬럼 제거
  - `selectReviewPostByPhotoGroupId` → `selectReviewPostById` 로 대체 (이미 id 기반 조회 존재)
  - `updateReviewPostGroupId` 삭제
  - `updateReviewPostMood`: WHERE `photo_group_id` → `id`(reviewPostId)
- DAO 인터페이스(`ReviewPhotoDao`, `ReviewHashtagDao`, `ReviewPostDao`) 메서드 시그니처를 위 변경에 맞춰 `reviewPostId` 기준으로 수정

### 조각 C — 서비스
- `ReviewPhotoService`, `ReviewPostService`: 그룹 생성/조회 로직 제거. draft 생성 시 그룹을 만들고 id를 다시 review_post에 UPDATE 하던 2단계가 사라지고, 사진/해시태그를 `reviewPostId` 로 바로 붙인다.
- 로그 식별자(`ReviewAiLog`)가 `photoGroupId` 를 쓰던 부분 → `reviewPostId` 로 교체

### 조각 D — 프론트엔드
- `travelgramApi.js`, `reviewStore.js`, `useReviewPhotoPolling.js`, `useReviewHashtagEditor.js`, `useReviewPhotoReorder.js`, `useReviewBootstrap.js`, `CreateTravelReview.vue`, `PhotoUploader.vue` 에서 `photoGroupId` / `hashtagGroupId` 를 주고받던 컨트랙트를 `reviewPostId` 기준으로 정렬
- 관련 SSE/폴링 문서(`front-photo-analysis-sse-migration-plan.md`, `front-photo-polling-hardening.md`)의 그룹 식별자 언급도 후속 정리

## 결과

- schema.sql이 review_post_id 기반 평탄 구조로 재작성됨
- 그룹이라는 간접 계층과 양방향 FK 순환이 사라져 INSERT 순서/후행 ALTER FK가 단순해짐
- 코드(엔티티~프론트) 마이그레이션은 조각 A~D로 분리, 이 문서가 그 로드맵

## 회고 / 확인 포인트

- **검증**: 코드 조각까지 끝낸 뒤 `./gradlew bootRun` 으로 실제 부팅(원격 DB 연결) → 리뷰 생성 플로우 end-to-end 확인.
- **데이터 보존 여부**: 현재는 재생성 전제. 만약 보존이 필요한 시점이 오면 그룹 id를 따라 자식 행의 `review_post_id` 를 backfill 하는 `ALTER` 마이그레이션을 별도로 작성해야 한다.
- **`ON DELETE CASCADE`**: review_post 삭제 시 사진/해시태그가 함께 지워지는 동작은 그대로 유지된다(이전엔 group을 경유, 이제는 직접).
