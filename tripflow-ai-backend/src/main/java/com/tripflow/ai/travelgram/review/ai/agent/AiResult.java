package com.tripflow.ai.travelgram.review.ai.agent;

import com.tripflow.ai.travelgram.review.ai.log.AiTokenUsage;

/**
 * Agent의 AI 호출 1건 결과 — 파싱된 본문({@code content})과 토큰 사용량({@code usage})을 함께 담는다.
 *
 * Agent가 {@code .call().content()}로 문자열만 뽑던 것을, 응답에서 토큰 사용량까지 같이 들고 나와
 * 호출하는 Service가 로깅할 수 있게 한다. 로깅 책임은 그대로 Service가 가지며,
 * Agent는 "본문 + 사용량"을 반환만 한다.
 *
 * @param <T> 본문 타입 (예: 사진 요약 String, 분석 결과 DTO)
 */
public record AiResult<T>(T content, AiTokenUsage usage) {
}
