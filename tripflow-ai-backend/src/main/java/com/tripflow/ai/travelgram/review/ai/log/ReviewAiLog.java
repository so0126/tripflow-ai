package com.tripflow.ai.travelgram.review.ai.log;

public record ReviewAiLog(
        ReviewAiStep step,
        Long planId,
        Long reviewPostId,
        Long photoGroupId,
        Long photoId,
        Long analysisId,
        Long elapsedMs,
        boolean success,
        String errorType,
        String errorMessage
) {

    public static ReviewAiLog start(ReviewAiStep step, Long planId, Long reviewPostId, Long photoGroupId, Long photoId) {
        return new ReviewAiLog(step, planId, reviewPostId, photoGroupId, photoId, null, null, true, null, null);
    }

    public static ReviewAiLog success(
            ReviewAiStep step,
            Long planId,
            Long reviewPostId,
            Long photoGroupId,
            Long photoId,
            Long analysisId,
            long elapsedMs) {
        return new ReviewAiLog(step, planId, reviewPostId, photoGroupId, photoId, analysisId, elapsedMs, true, null, null);
    }

    public static ReviewAiLog fail(
            ReviewAiStep step,
            Long planId,
            Long reviewPostId,
            Long photoGroupId,
            Long photoId,
            long elapsedMs,
            Throwable cause) {
        return new ReviewAiLog(
                step,
                planId,
                reviewPostId,
                photoGroupId,
                photoId,
                null,
                elapsedMs,
                false,
                cause == null ? null : cause.getClass().getSimpleName(),
                cause == null ? null : cause.getMessage());
    }
}
