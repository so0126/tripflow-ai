package com.tripflow.ai.travelgram.review.ai.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.travelgram.review.ai.dto.entity.ReviewJob;

@Mapper
public interface ReviewJobDao {
    void insertReviewJob(ReviewJob reviewJob);

    ReviewJob selectReviewJobById(Long id);

    List<ReviewJob> selectReviewJobsByReviewPostId(@Param("reviewPostId") Long reviewPostId);

    List<ReviewJob> selectReviewJobsByPhotoGroupId(@Param("photoGroupId") Long photoGroupId);

    List<ReviewJob> selectReviewJobsByPhotoId(@Param("photoId") Long photoId);

    void updateReviewJobStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("errorMessage") String errorMessage,
            @Param("updatedAt") OffsetDateTime updatedAt);
}
