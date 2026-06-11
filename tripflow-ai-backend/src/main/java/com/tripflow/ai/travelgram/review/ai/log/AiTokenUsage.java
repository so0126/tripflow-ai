package com.tripflow.ai.travelgram.review.ai.log;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * AI 호출 1건의 토큰 사용량 값객체.
 *
 * Spring AI 응답에서 토큰 사용량을 추출해 로깅에 쓰기 위한 불변 값.
 * 외부 응답은 usage 메타데이터가 비어 올 수 있으므로, 추출은 전부 {@link #from(ChatResponse)}의
 * null-guard를 거친다(= 경계에서 누락을 흡수). 측정값이 없으면 모든 필드가 null인 {@link #empty()}.
 *
 * <p>토큰 수는 Spring AI에서 {@link Integer}(null 가능)로 오지만, 누적/저장 편의와
 * 기존 컨벤션(common.chat의 tokenUsage가 Long)을 따라 {@link Long}으로 승격해 보관한다.
 *
 * <p>"토큰 0"과 "미측정(null)"은 다른 의미다 — 캐시 히트나 호출 실패처럼 AI를 안 탄 경우는
 * 0이 아니라 null로 남겨 구분한다.
 */
public record AiTokenUsage(Long promptTokens, Long completionTokens, Long totalTokens) {

    private static final AiTokenUsage EMPTY = new AiTokenUsage(null, null, null);

    /** 토큰을 측정하지 못한 경우(캐시 히트·응답 없음 등). 모든 필드 null. */
    public static AiTokenUsage empty() {
        return EMPTY;
    }

    /**
     * Spring AI 응답에서 토큰 사용량을 안전하게 추출한다.
     * response/metadata/usage 중 어느 단계든 null이면 {@link #empty()}를 반환한다.
     */
    public static AiTokenUsage from(ChatResponse response) {
        if (response == null || response.getMetadata() == null) {
            return empty();
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage == null) {
            return empty();
        }
        return new AiTokenUsage(
                toLong(usage.getPromptTokens()),
                toLong(usage.getCompletionTokens()),
                toLong(usage.getTotalTokens()));
    }

    private static Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }
}
