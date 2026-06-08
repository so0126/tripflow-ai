package com.tripflow.ai.travelgram.review.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.travelgram.review.dto.entity.ReviewPhoto;


@Mapper
public interface ReviewPhotoDao {

    void insertReviewPhoto(ReviewPhoto reviewPhoto);
    void updatePhotoOrder(@Param("photoId") Long photoId,
                      @Param("orderIndex") Integer orderIndex,
                      @Param("reviewPostId") Long reviewPostId);

    void updatePhotoSummary(@Param("photoId") Long photoId, @Param("summary") String summary);
    void updatePhotoStatus(@Param("photoId") Long photoId, @Param("status") String status);
    ReviewPhoto selectReviewPhotoById(Long id);
    List<ReviewPhoto> selectReviewPhotosByReviewPostId(Long reviewPostId);
    List<String> selectPhotoSummariesByReviewPostId(Long reviewPostId);


    void deleteReviewPhoto(Long id);
}