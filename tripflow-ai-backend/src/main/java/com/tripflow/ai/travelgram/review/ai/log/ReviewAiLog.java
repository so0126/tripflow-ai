package com.tripflow.ai.travelgram.review.ai.log;

public record ReviewAiLog(
        ReviewAiStep step,
        Long planId,
        Long reviewPostId,
        Long photoId,
        Long analysisId,
        Long elapsedMs,
        Long promptTokens,
        Long completionTokens,
        Long totalTokens,
        boolean success,
        String errorType,
        String errorMessage
) {

    public static ReviewAiLog start(ReviewAiStep step, Long planId, Long reviewPostId, Long photoId) {
        return new ReviewAiLog(step, planId, reviewPostId, photoId, null, null, null, null, null, true, null, null);
    }

    /**
     * 토큰 정보가 없는(또는 측정 안 한) 성공 로그. 토큰 3필드는 null.
     * AI를 안 탄 경로(캐시 히트 등)나 토큰 도입 전 호출부가 그대로 쓴다.
     */
    public static ReviewAiLog success(
            ReviewAiStep step,
            Long planId,
            Long reviewPostId,
            Long photoId,
            Long analysisId,
            long elapsedMs) {
        return success(step, planId, reviewPostId, photoId, analysisId, elapsedMs, AiTokenUsage.empty());
    }

    /** 토큰 사용량을 함께 남기는 성공 로그. usage가 empty면 토큰 3필드는 null로 위임된다. */
    public static ReviewAiLog success(
            ReviewAiStep step,
            Long planId,
            Long reviewPostId,
            Long photoId,
            Long analysisId,
            long elapsedMs,
            AiTokenUsage usage) {
        AiTokenUsage u = usage == null ? AiTokenUsage.empty() : usage;
        return new ReviewAiLog(
                step,
                planId,
                reviewPostId,
                photoId,
                analysisId,
                elapsedMs,
                u.promptTokens(),
                u.completionTokens(),
                u.totalTokens(),
                true,
                null,
                null);
    }

    public static ReviewAiLog fail(
            ReviewAiStep step,
            Long planId,
            Long reviewPostId,
            Long photoId,
            long elapsedMs,
            Throwable cause) {
        return new ReviewAiLog(
                step,
                planId,
                reviewPostId,
                photoId,
                null,
                elapsedMs,
                null,
                null,
                null,
                false,
                cause == null ? null : cause.getClass().getSimpleName(),
                cause == null ? null : cause.getMessage());
    }
}
