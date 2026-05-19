package com.tripflow.ai.travelgram.review.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.tripflow.ai.travelgram.review.ai.agent.ReviewImageAnalysisAgent;
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
        try {
            log.info("🤖 [Async] AI 분석 시작 - photoId: {}", photoId);

            // 1. AI 분석 (시간이 오래 걸리는 작업)
            String summary = reviewImageAnalysisAgent.analyzeReviewImage(contentType, imageBytes);

            // 2. 결과 DB 업데이트
            reviewPhotoDao.updatePhotoSummary(photoId, summary);

            log.info("✅ [Async] AI 분석 완료 및 저장 - photoId: {}", photoId);
        } catch (Exception e) {
            log.error("❌ [Async] AI 분석 실패 - photoId: {}", photoId, e);
        }
    }
}