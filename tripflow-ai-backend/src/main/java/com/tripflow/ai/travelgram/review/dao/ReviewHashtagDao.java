package com.tripflow.ai.travelgram.review.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.travelgram.review.dto.entity.ReviewHashtagGroup;


@Mapper
public interface ReviewHashtagDao {
    void insertHashtagGroup(ReviewHashtagGroup group);
    void insertHashtagList(@Param("hashtagGroupId") Long hashtagGroupId, 
                           @Param("names") List<String> names);
    void deleteHashtagsByHashtagGroupId(@Param("hashtagGroupId") Long hashtagGroupId);
    
}
