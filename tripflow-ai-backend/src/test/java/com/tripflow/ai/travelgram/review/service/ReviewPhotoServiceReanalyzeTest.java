package com.tripflow.ai.travelgram.review.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tripflow.ai.common.global.exception.BusinessException;
import com.tripflow.ai.common.s3.service.S3Service;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPhoto;
import com.tripflow.ai.travelgram.review.exception.errorcode.ReviewErrorCode;

/**
 * ReviewPhotoService.reanalyzePhoto 검증 (FAILED 사진 개별 재분석).
 *
 * 흐름: 사진 조회 → status=PENDING 리셋 → S3에서 원본 다운로드 → analyzePhotoAndUpdateDb 재호출.
 *
 * @Async인 analyzePhotoAndUpdateDb는 별도 빈(reviewAnalysisService)에 있어야 프록시를 타므로,
 * 여기서는 그 빈을 mock으로 두고 "같은 photoId + 다운로드한 bytes로 재호출되는지"만 검증한다.
 *
 * NOTE: 현재 reanalyzePhoto는 미구현(UnsupportedOperationException)이라 이 테스트는 red다.
 */
public class ReviewPhotoServiceReanalyzeTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private ReviewPhotoDao reviewPhotoDao;

    @Mock
    private ReviewPhotoAnalysisService reviewAnalysisService;

    @InjectMocks
    private ReviewPhotoService reviewPhotoService;

    private static final Long PHOTO_ID = 42L;
    private static final String FILE_URL =
            "https://my-bucket.s3.ap-northeast-2.amazonaws.com/reviewPhotos/abc.jpg";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("FAILED 사진을 PENDING으로 리셋하고 다운로드한 원본으로 analyzePhotoAndUpdateDb를 재호출한다")
    public void reanalyzePhoto_resetsPendingAndRecallsAnalyze() {
        // given: FAILED 상태로 남은 사진
        ReviewPhoto photo = ReviewPhoto.builder()
                .id(PHOTO_ID)
                .reviewPostId(7L)
                .fileUrl(FILE_URL)
                .status("FAILED")
                .build();
        when(reviewPhotoDao.selectReviewPhotoById(PHOTO_ID)).thenReturn(photo);

        byte[] bytes = new byte[] { 1, 2, 3 };
        when(s3Service.downloadFile(FILE_URL)).thenReturn(bytes);

        // when
        reviewPhotoService.reanalyzePhoto(PHOTO_ID);

        // then: 폴링이 다시 진행 상태로 보이도록 PENDING 리셋
        verify(reviewPhotoDao).updatePhotoStatus(PHOTO_ID, "PENDING");
        // then: 같은 photoId + S3에서 받은 원본 bytes로 재분석 재호출
        verify(reviewAnalysisService).analyzePhotoAndUpdateDb(eq(PHOTO_ID), anyString(), eq(bytes));
    }

    @ParameterizedTest(name = "status={0} 사진은 재분석이 거부된다")
    @ValueSource(strings = { "SUCCESS", "PENDING" })
    @DisplayName("FAILED가 아닌 사진은 PHOTO_NOT_REANALYZABLE로 거부하고 다운로드/재분석을 트리거하지 않는다")
    public void reanalyzePhoto_rejectsWhenNotFailed(String status) {
        // given: FAILED가 아닌 상태(정상 결과 or 분석 진행 중)
        ReviewPhoto photo = ReviewPhoto.builder()
                .id(PHOTO_ID)
                .reviewPostId(7L)
                .fileUrl(FILE_URL)
                .status(status)
                .build();
        when(reviewPhotoDao.selectReviewPhotoById(PHOTO_ID)).thenReturn(photo);

        // when / then: 상태 충돌로 거부 (컨트롤러단에서 409로 매핑됨)
        assertThatThrownBy(() -> reviewPhotoService.reanalyzePhoto(PHOTO_ID))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getBaseErrorCode())
                .isEqualTo(ReviewErrorCode.PHOTO_NOT_REANALYZABLE);

        // then: 거부 시 S3 다운로드도, PENDING 리셋도, 재분석도 일어나지 않는다
        verify(s3Service, never()).downloadFile(anyString());
        verify(reviewPhotoDao, never()).updatePhotoStatus(eq(PHOTO_ID), anyString());
        verify(reviewAnalysisService, never()).analyzePhotoAndUpdateDb(any(), any(), any());
    }
}
