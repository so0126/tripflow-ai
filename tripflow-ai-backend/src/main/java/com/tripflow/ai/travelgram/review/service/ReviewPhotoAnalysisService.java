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
public class ReviewPhotoAnalysisService {

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

            // 1-1. 결과 검증: 예외가 안 났어도 쓸 수 없는 값이면 실패로 본다.
            //      (모델이 분석을 거부하면 영어 거절문/빈 값/"{}" 등을 뱉을 수 있음)
            //      여기서 예외를 던지면 아래 catch가 받아 FAILED로 통일 처리한다.
            if (!isUsableKoreanSummary(summary)) {
                throw new IllegalStateException("분석 결과가 유효한 한국어 요약이 아님: " + summary);
            }

            // 2. 결과 DB 업데이트 (summary + status=SUCCESS를 한 번에)
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
            // 분석 실패 → status=FAILED로 기록 (프론트 폴링이 멈추고 재시도 UI를 띄울 수 있게)
            reviewPhotoDao.updatePhotoStatus(photoId, "FAILED");
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

    /**
     * 사진 요약이 "쓸 수 있는 한국어 한 문장"인지 검증한다.
     * - null / 빈 문자열 / 공백 → 무효
     * - 한글(음절 가-힣 + 자모)이 하나도 없으면 → 무효 (영어 거절문, "{}" 등)
     *
     * 정상 요약은 장소명이 영어로 섞일 수 있으므로 "전부 한국어"가 아니라
     * "한글 최소 1자 포함"을 기준으로 둔다. 정규식 [가-힣] 대신
     * UnicodeScript.HANGUL을 쓰면 완성형 음절과 자모를 모두 잡는다.
     */
    private boolean isUsableKoreanSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            return false;
        }
        return summary.codePoints()
                .anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HANGUL);
    }
}
