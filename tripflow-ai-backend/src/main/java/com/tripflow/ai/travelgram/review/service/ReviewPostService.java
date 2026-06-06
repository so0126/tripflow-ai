package com.tripflow.ai.travelgram.review.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripflow.ai.travelgram.review.ai.agent.ReviewImageAnalysisAgent;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiLog;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiStep;
import com.tripflow.ai.travelgram.review.dao.ReviewHashtagDao;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;
import com.tripflow.ai.travelgram.review.dao.ReviewPostDao;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewHashtagGroup;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPhotoGroup;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPost;
import com.tripflow.ai.travelgram.review.dto.response.PhotoAnalysisResult;
import com.tripflow.ai.travelgram.review.dto.response.ReviewCreateResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewPostService {

    private final ReviewPhotoDao reviewPhotoDao;
    private final ReviewPostDao reviewPostDao;
    private final ReviewHashtagDao reviewHashtagDao;
    private final ReviewImageAnalysisAgent reviewImageAnalysisAgent;

    @Transactional
    public ReviewCreateResponse createReview(Long planId) {
        // 멱등 처리: 같은 plan으로 재진입(새로고침/뒤로가기)해도 빈 draft가 쌓이지 않도록,
        // 아직 게시되지 않은 draft가 있으면 그대로 재사용한다.
        // (게시 완료분은 selectDraftByPlanId에서 제외되므로 plan당 리뷰는 여러 번 작성 가능)
        ReviewCreateResponse existingDraft = reviewPostDao.selectDraftByPlanId(planId);
        if (existingDraft != null) {
            return existingDraft;
        }

        ReviewPost post = ReviewPost.builder()
                .planId(planId)
                .build();

        reviewPostDao.insertDraft(post);

        ReviewPhotoGroup photoGroup = ReviewPhotoGroup.builder()
                .reviewPostId(post.getId())
                .build();
        ReviewHashtagGroup hashtagGroup = ReviewHashtagGroup.builder()
                .reviewPostId(post.getId())
                .build();

        reviewPhotoDao.insertReviewPhotoGroup(photoGroup);
        reviewHashtagDao.insertHashtagGroup(hashtagGroup);
        reviewPostDao.updateReviewPostGroupId(post.getId(), photoGroup.getId(), hashtagGroup.getId());

        return new ReviewCreateResponse(post.getId(), photoGroup.getId(), hashtagGroup.getId());
    }

    public void analyzeTripContext(Long photoGroupId) {
        long startedAt = System.nanoTime();

        List<String> summaryList = reviewPhotoDao.selectPhotoSummariesByPhotoGroupId(photoGroupId);
        if (summaryList.isEmpty()) {
            return;
        }

        try {
            PhotoAnalysisResult result = reviewImageAnalysisAgent.analyzeTripContext(summaryList);
            reviewPostDao.updateReviewPostMood(photoGroupId, result.getOverallMood(), result.getTravelType());

            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{}", ReviewAiLog.success(
                    ReviewAiStep.TRIP_CONTEXT_ANALYSIS,
                    null,
                    null,
                    photoGroupId,
                    null,
                    null,
                    elapsedMs));
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("{}", ReviewAiLog.fail(
                    ReviewAiStep.TRIP_CONTEXT_ANALYSIS,
                    null,
                    null,
                    photoGroupId,
                    null,
                    elapsedMs,
                    e), e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void selectStyle(Long reviewPostId, Long reviewStyleId) {
        reviewPostDao.updateReviewPostStyleIdById(reviewPostId, reviewStyleId);
    }

    @Transactional
    public void updateCaption(Long reviewPostId, String caption) {
        reviewPostDao.updateReviewPostCaptionIdById(reviewPostId, caption);
    }

    @Transactional
    public void updateHashtags(Long hashtagGroupId, List<String> names) {
        reviewHashtagDao.deleteHashtagsByHashtagGroupId(hashtagGroupId);

        if (names != null && !names.isEmpty()) {
            reviewHashtagDao.insertHashtagList(hashtagGroupId, names);
        }
    }
}
