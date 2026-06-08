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

        // 사진/해시태그는 더 이상 그룹을 경유하지 않고 reviewPostId로 직접 연결되므로,
        // draft를 만들면 끝. 이후 사진 업로드/해시태그 저장이 post.getId()를 FK로 사용한다.
        return new ReviewCreateResponse(post.getId());
    }

    public void analyzeTripContext(Long reviewPostId) {
        long startedAt = System.nanoTime();

        List<String> summaryList = reviewPhotoDao.selectPhotoSummariesByReviewPostId(reviewPostId);
        if (summaryList.isEmpty()) {
            return;
        }

        try {
            PhotoAnalysisResult result = reviewImageAnalysisAgent.analyzeTripContext(summaryList);
            reviewPostDao.updateReviewPostMood(reviewPostId, result.getOverallMood(), result.getTravelType());

            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{}", ReviewAiLog.success(
                    ReviewAiStep.TRIP_CONTEXT_ANALYSIS,
                    null,
                    reviewPostId,
                    null,
                    null,
                    elapsedMs));
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("{}", ReviewAiLog.fail(
                    ReviewAiStep.TRIP_CONTEXT_ANALYSIS,
                    null,
                    reviewPostId,
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
    public void updateHashtags(Long reviewPostId, List<String> names) {
        reviewHashtagDao.deleteHashtagsByReviewPostId(reviewPostId);

        if (names != null && !names.isEmpty()) {
            reviewHashtagDao.insertHashtagList(reviewPostId, names);
        }
    }
}
