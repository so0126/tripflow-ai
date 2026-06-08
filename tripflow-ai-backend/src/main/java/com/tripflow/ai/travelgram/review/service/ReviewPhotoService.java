package com.tripflow.ai.travelgram.review.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tripflow.ai.common.s3.service.S3Service;
import com.tripflow.ai.travelgram.review.dao.ReviewPhotoDao;
import com.tripflow.ai.travelgram.review.dto.entity.ReviewPhoto;
import com.tripflow.ai.travelgram.review.dto.request.ReviewPhotoOrderUpdateRequest;
import com.tripflow.ai.travelgram.review.dto.request.ReviewPhotoOrderUpdateRequest.PhotoOrderItem;
import com.tripflow.ai.travelgram.review.dto.response.ReviewPhotoUploadResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewPhotoService {

    private final S3Service s3Service;

    private final ReviewPhotoDao reviewPhotoDao;
    private final ReviewPhotoAnalysisService reviewAnalysisService;

    // ======================================
    // 2) 사진 업로드 (JSON 파싱 로직 완전 삭제 버전)
    // ======================================
    public List<ReviewPhotoUploadResponse> uploadPhotosBatch(
            List<MultipartFile> files,
            Long reviewPostId, // 👈 JSON 대신 그냥 받음
            Integer startOrderIndex // 👈 JSON 대신 그냥 받음
    ) {

        // 1. 결과 담을 리스트
        List<ReviewPhotoUploadResponse> results = new ArrayList<>();

        // 2. 파일 리스트를 돌면서 순서대로 처리
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // ⭐ 핵심 로직: 순서는 (시작번호 + 현재 인덱스)로 자동 계산
            int currentOrder = startOrderIndex + i;
            // 3. 내부 메서드로 처리 위임
            ReviewPhotoUploadResponse response = processSinglePhotoUpload(file, reviewPostId, currentOrder);
            results.add(response);
        }

        return results;
    }

    // 내부 처리 메서드 (파라미터가 DTO에서 단순 변수들로 바뀜)
    private ReviewPhotoUploadResponse processSinglePhotoUpload(
            MultipartFile file,
            Long reviewPostId,
            int orderIndex) {
        // 1) 파일 비어있으면 예외 처리
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }
        // 2) 확장자 추출
        String originalName = file.getOriginalFilename();

        String folder = "reviewPhotos/";
        // 3) UUID 파일명 생성
        if (originalName == null || !originalName.contains(".")) {
            originalName = "unknown_" + UUID.randomUUID();
        }
        String ext = "";
        int idx = originalName.lastIndexOf(".");
        if (idx > -1) {
            ext = originalName.substring(idx);
        }
        String storedName = folder + UUID.randomUUID().toString() + ext;
        // 4) S3 업로드
        String s3Url;
        try {
            s3Url = s3Service.uploadFile(file, storedName);
        } catch (Exception e) {
            throw new RuntimeException("S3 upload failed", e);
        }

        // 2. DB 저장 (AI 요약(summary)은 일단 null 또는 "분석 중..."으로 저장)
        ReviewPhoto photo = ReviewPhoto.builder()
                .reviewPostId(reviewPostId)
                .orderIndex(orderIndex)
                .fileUrl(s3Url)
                .summary(null) // 나중에 채워짐
                .build();

        reviewPhotoDao.insertReviewPhoto(photo);

        try {
            reviewAnalysisService.analyzePhotoAndUpdateDb(
                    photo.getId(),
                    file.getContentType(),
                    file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("이미지 바이트 읽기 실패", e);
        }

        return new ReviewPhotoUploadResponse(photo.getId(), photo.getFileUrl(), photo.getOrderIndex());

    }


    public List<ReviewPhoto> getReviewPhotos(Long reviewPostId) {
        return reviewPhotoDao.selectReviewPhotosByReviewPostId(reviewPostId);
    }

    /**
     * FAILED 사진을 개별 재분석한다.
     * 사진 조회 → S3 원본 다운로드 → status=PENDING 리셋 → analyzePhotoAndUpdateDb 재호출.
     *
     * 순서 주의: PENDING 리셋을 다운로드보다 먼저 하면, 다운로드 실패 시 사진이 PENDING에
     * 영영 갇혀 폴링이 다시 무한루프에 빠진다. 그래서 다운로드 성공을 확인한 뒤 PENDING으로 바꾼다.
     */
    public void reanalyzePhoto(Long photoId) {
        ReviewPhoto photo = reviewPhotoDao.selectReviewPhotoById(photoId);
        if (photo == null) {
            throw new IllegalArgumentException("재분석할 사진을 찾을 수 없습니다: photoId=" + photoId);
        }

        // FAILED 상태에서만 재시도 전이를 허용한다.
        // - SUCCESS 재분석: 멀쩡한 결과를 날리고 OpenAI 비용만 낭비
        // - PENDING 재분석: 이미 @Async로 도는 분석을 중복 트리거 → 같은 photoId에 분석이 경합하며 멱등성 깨짐
        // 프론트가 FAILED 사진에만 버튼을 노출하지만, photoId로 직접 호출하면 우회되므로 서버에서도 막는다.
        // ("FAILED".equals(...)는 status가 null이어도 안전하게 거부한다.)
        if (!"FAILED".equals(photo.getStatus())) {
            throw new IllegalStateException(
                    "FAILED 상태 사진만 재분석할 수 있습니다: photoId=" + photoId + ", status=" + photo.getStatus());
        }

        // 1) S3에서 원본 재사용 (실패하면 여기서 던져지고 status는 기존 FAILED로 유지)
        byte[] imageBytes = s3Service.downloadFile(photo.getFileUrl());
        String contentType = resolveContentType(photo.getFileUrl());

        // 2) 폴링이 다시 "진행 중"으로 보이도록 PENDING 리셋
        reviewPhotoDao.updatePhotoStatus(photoId, "PENDING");

        // 3) 별도 빈(@Async)으로 재분석 트리거 → 성공/실패가 다시 status에 기록됨
        reviewAnalysisService.analyzePhotoAndUpdateDb(photoId, contentType, imageBytes);
    }

    /**
     * fileUrl 확장자로 contentType을 추론한다. (ReviewPhoto에 contentType 컬럼이 없으므로)
     * 업로드 시 JPG/PNG만 허용하므로 png 외에는 jpeg로 본다.
     */
    private String resolveContentType(String fileUrl) {
        String lower = fileUrl.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }

    @Transactional
    public void updatePhotoOrder(ReviewPhotoOrderUpdateRequest request) {
        for (PhotoOrderItem item : request.getPhotos()) {
            reviewPhotoDao.updatePhotoOrder(
                    item.getPhotoId(),
                    item.getOrderIndex(),
                    request.getReviewPostId());
        }
    }
}
