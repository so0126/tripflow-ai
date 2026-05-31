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
            Long photoGroupId, // 👈 JSON 대신 그냥 받음
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
            ReviewPhotoUploadResponse response = processSinglePhotoUpload(file, photoGroupId, currentOrder);
            results.add(response);
        }

        return results;
    }

    // 내부 처리 메서드 (파라미터가 DTO에서 단순 변수들로 바뀜)
    private ReviewPhotoUploadResponse processSinglePhotoUpload(
            MultipartFile file,
            Long photoGroupId,
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
                .photoGroupId(photoGroupId)
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


    public List<ReviewPhoto> getReviewPhotos(Long photoGroupId) {
        return reviewPhotoDao.selectReviewPhotosByPhotoGroupId(photoGroupId);
    }

    @Transactional
    public void updatePhotoOrder(ReviewPhotoOrderUpdateRequest request) {
        for (PhotoOrderItem item : request.getPhotos()) {
            reviewPhotoDao.updatePhotoOrder(
                    item.getPhotoId(),
                    item.getOrderIndex(),
                    request.getPhotoGroupId());
        }
    }
}
