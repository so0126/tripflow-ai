package com.tripflow.ai.travelgram.review.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tripflow.ai.common.s3.service.S3Service;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPhoto;

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
                .photoGroupId(7L)
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
}
