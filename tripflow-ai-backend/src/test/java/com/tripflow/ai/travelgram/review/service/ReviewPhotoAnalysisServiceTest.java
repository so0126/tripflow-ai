package com.tripflow.ai.travelgram.review.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tripflow.ai.travelgram.review.ai.agent.AiResult;
import com.tripflow.ai.travelgram.review.ai.agent.ReviewImageAnalysisAgent;
import com.tripflow.ai.travelgram.review.ai.log.AiTokenUsage;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;

/**
 * 사진 AI 분석의 성공/실패가 DB status에 정확히 기록되는지 검증.
 *
 * 배경: agent가 예외를 삼키고 "{}"를 반환하던 시절엔 실패가 SUCCESS로 잘못 기록돼
 *       프론트 폴링이 영원히 멈추지 못했다(무한루프). 조각 A에서 agent 예외 전파 +
 *       service catch→FAILED로 고쳤고, 이 테스트가 그 불변식을 고정한다.
 */
public class ReviewPhotoAnalysisServiceTest {

    @Mock
    private ReviewImageAnalysisAgent reviewImageAnalysisAgent;

    @Mock
    private ReviewPhotoDao reviewPhotoDao;

    @InjectMocks
    private ReviewPhotoAnalysisService reviewPhotoAnalysisService;

    private static final Long PHOTO_ID = 42L;
    private static final String CONTENT_TYPE = "image/jpeg";
    private static final byte[] IMAGE_BYTES = new byte[] { 1, 2, 3 };

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("agent가 예외를 던지면 summary는 기록되지 않고 status=FAILED로 기록된다")
    public void analyzePhoto_whenAgentThrows_recordsFailed() {
        // given: 분석 중 예외 발생 (예: 모델 호출 실패, 파싱 실패 등)
        when(reviewImageAnalysisAgent.analyzeReviewImage(CONTENT_TYPE, IMAGE_BYTES))
                .thenThrow(new RuntimeException("analysis failed"));

        // when: 예외는 service가 삼키고 FAILED로만 남겨야 한다 (호출자에게 전파 X)
        reviewPhotoAnalysisService.analyzePhotoAndUpdateDb(PHOTO_ID, CONTENT_TYPE, IMAGE_BYTES);

        // then: 실패가 SUCCESS로 새지 않음 — summary 업데이트는 한 번도 안 일어남
        verify(reviewPhotoDao, never()).updatePhotoSummary(anyLong(), anyString());
        // then: 해당 사진이 FAILED로 정확히 기록됨
        verify(reviewPhotoDao).updatePhotoStatus(PHOTO_ID, "FAILED");
    }

    @Test
    @DisplayName("agent가 성공하면 summary가 기록되고 FAILED로는 기록되지 않는다")
    public void analyzePhoto_whenAgentSucceeds_recordsSummary() {
        // given: 정상 분석 결과
        String summary = "해질녘 바다와 함께한 산책";
        when(reviewImageAnalysisAgent.analyzeReviewImage(CONTENT_TYPE, IMAGE_BYTES))
                .thenReturn(new AiResult<>(summary, AiTokenUsage.empty()));

        // when
        reviewPhotoAnalysisService.analyzePhotoAndUpdateDb(PHOTO_ID, CONTENT_TYPE, IMAGE_BYTES);

        // then: summary(=SUCCESS) 1회 기록, FAILED 기록 없음
        verify(reviewPhotoDao).updatePhotoSummary(PHOTO_ID, summary);
        verify(reviewPhotoDao, never()).updatePhotoStatus(anyLong(), eq("FAILED"));
    }

    /**
     * 예외가 안 났더라도 결과물이 쓸 수 없는 값이면 실패다.
     * - null / 빈 문자열 / 공백
     * - "{}" : 원래 버그에서 agent가 예외를 삼키고 뱉던 값
     * - 한글이 하나도 없는 값(영어만 등) : 한국어 후기 페르소나의 산출물이 아님
     *
     * 정상 후기는 장소명이 영어로 섞일 수 있으므로 "전부 한국어"가 아니라
     * "한글 최소 1자 포함"을 유효 기준으로 둔다.
     *
     * NOTE: 현재 서비스는 반환값을 검증하지 않으므로 이 테스트는 (의도적으로) 실패한다.
     *       검증 로직 추가 후 green이 되어야 한다. (TDD red)
     */
    @ParameterizedTest(name = "agent 반환값=\"{0}\" → FAILED")
    @NullSource
    @ValueSource(strings = { "", "   ", "{}", "Sunset walk by the sea", "12345" })
    @DisplayName("agent가 null/공백/{}/비한국어 값을 반환하면 SUCCESS가 아니라 FAILED로 기록된다")
    public void analyzePhoto_whenAgentReturnsInvalidSummary_recordsFailed(String invalidSummary) {
        // given: 예외는 없지만 결과물이 쓸 수 없는 값
        when(reviewImageAnalysisAgent.analyzeReviewImage(any(), any()))
                .thenReturn(new AiResult<>(invalidSummary, AiTokenUsage.empty()));

        // when
        reviewPhotoAnalysisService.analyzePhotoAndUpdateDb(PHOTO_ID, CONTENT_TYPE, IMAGE_BYTES);

        // then: 쓰레기값이 SUCCESS로 새지 않음 + FAILED로 기록됨
        verify(reviewPhotoDao, never()).updatePhotoSummary(anyLong(), anyString());
        verify(reviewPhotoDao).updatePhotoStatus(PHOTO_ID, "FAILED");
    }
}
