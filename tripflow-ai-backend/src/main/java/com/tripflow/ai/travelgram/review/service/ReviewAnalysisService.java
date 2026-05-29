package com.tripflow.ai.travelgram.review.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.tripflow.ai.travelgram.review.ai.agent.ReviewImageAnalysisAgent;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiLog;
import com.tripflow.ai.travelgram.review.ai.log.ReviewAiStep;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final ReviewImageAnalysisAgent reviewImageAnalysisAgent;
    private final ReviewPhotoDao reviewPhotoDao;

    // ★ 핵심: 반드시 별도 클래스에 있어야 @Async가 동작함
    @Async 
    @Transactional
    public void analyzePhotoAndUpdateDb(Long photoId, String contentType, byte[] imageBytes) {
        long startedAt = System.nanoTime();
        try {
            // 1. AI 분석 (시간이 오래 걸리는 작업)
            String summary = reviewImageAnalysisAgent.analyzeReviewImage(contentType, imageBytes);

            // 2. 결과 DB 업데이트
            reviewPhotoDao.updatePhotoSummary(photoId, summary);

            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("{}", ReviewAiLog.success(
                    ReviewAiStep.PHOTO_ANALYSIS,
                    null,
                    null,
                    null,
                    photoId,
                    null,
                    elapsedMs));
        } catch (Exception e) {
            long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("{}", ReviewAiLog.fail(
                    ReviewAiStep.PHOTO_ANALYSIS,
                    null,
                    null,
                    null,
                    photoId,
                    elapsedMs,
                    e), e);
        }
    }
}
