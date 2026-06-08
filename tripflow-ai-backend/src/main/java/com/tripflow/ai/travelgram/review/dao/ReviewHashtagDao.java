package com.tripflow.ai.travelgram.review.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface ReviewHashtagDao {
    void insertHashtagList(@Param("reviewPostId") Long reviewPostId,
                           @Param("names") List<String> names);
    void deleteHashtagsByReviewPostId(@Param("reviewPostId") Long reviewPostId);

}
