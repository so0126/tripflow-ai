package com.tripflow.ai.travelgram.review.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.travelgram.review.dto.entity.ReviewPost;
import com.tripflow.ai.travelgram.review.dto.response.ReviewCreateResponse;

@Mapper
public interface ReviewPostDao {
    void insertDraft(ReviewPost post);

    /** 재진입 시 재사용할 미게시(draft) 리뷰 1건. 없으면 null. */
    ReviewCreateResponse selectDraftByPlanId(@Param("planId") Long planId);
    void updateReviewPostGroupId(
        @Param("reviewPostId")Long reviewPostId, 
        @Param("photoGroupId")Long photoGroupId, 
        @Param("hashtagGroupId") Long hashtagGroupId);
    void updateReviewPostMood(
        @Param("photoGroupId") Long photoGroupId, 
        @Param("overallMoods") String overallMoods, 
        @Param("travelType") String travelType);
    ReviewPost selectReviewPostById(Long reviewPostId);
    ReviewPost selectReviewPostByPhotoGroupId(@Param("photoGroupId") Long photoGroupId);
    void updateReviewPostStyleIdById(
        @Param("reviewPostId") Long reviewPostId,
        @Param("reviewStyleId")Long reviewStyleId);
    void updateReviewPostCaptionIdById(
        @Param("reviewPostId") Long reviewPostId,
        @Param("caption") String caption);
    

    
}
