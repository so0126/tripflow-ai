package com.tripflow.ai.travelgram.review.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tripflow.ai.travelgram.review.service.ReviewPhotoService;
import com.tripflow.ai.travelgram.review.service.ReviewPostService;

/**
 * 재분석 엔드포인트가 서비스 예외를 실제 HTTP 상태로 매핑하는지 검증하는 웹 슬라이스 스모크.
 *
 * 서비스 단위테스트(ReviewPhotoServiceReanalyzeTest)는 "FAILED 아니면 IllegalStateException"까지만 본다.
 * 여기서는 그 예외가 GlobalExceptionHandler를 거쳐 **409 Conflict**로 나가는지(매핑 배선)를 증명한다.
 *
 * - @WebMvcTest는 컨트롤러 + @ControllerAdvice만 올리므로 DB/OpenAI/S3 빈은 뜨지 않는다.
 * - 서비스는 @MockitoBean으로 대체해 컨트롤러→핸들러 경로만 격리한다.
 * - addFilters=false: 시큐리티 필터를 빼서 인증 무관하게 매핑만 검증(보안 규칙은 이 테스트 범위 밖).
 */
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerReanalyzeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewPhotoService reviewPhotoService;

    @MockitoBean
    private ReviewPostService reviewPostService;

    private static final long PHOTO_ID = 42L;

    @Test
    @DisplayName("FAILED 아닌 사진 재분석 → 서비스가 IllegalStateException → 409 Conflict")
    void reanalyze_notFailed_returns409() throws Exception {
        doThrow(new IllegalStateException("FAILED 상태 사진만 재분석할 수 있습니다"))
                .when(reviewPhotoService).reanalyzePhoto(eq(PHOTO_ID));

        mockMvc.perform(post("/reviews/photo/{photoId}/reanalyze", PHOTO_ID))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("없는 photoId 재분석 → 서비스가 IllegalArgumentException → 404 Not Found")
    void reanalyze_missingPhoto_returns404() throws Exception {
        doThrow(new IllegalArgumentException("재분석할 사진을 찾을 수 없습니다: photoId=" + PHOTO_ID))
                .when(reviewPhotoService).reanalyzePhoto(eq(PHOTO_ID));

        mockMvc.perform(post("/reviews/photo/{photoId}/reanalyze", PHOTO_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FAILED 사진 재분석 성공 → 200 OK")
    void reanalyze_failed_returns200() throws Exception {
        doNothing().when(reviewPhotoService).reanalyzePhoto(eq(PHOTO_ID));

        mockMvc.perform(post("/reviews/photo/{photoId}/reanalyze", PHOTO_ID))
                .andExpect(status().isOk());
    }
}
