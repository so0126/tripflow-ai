package com.tripflow.ai.planner.hotel.dao;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.planner.hotel.dto.entity.HotelBookingSummary.HotelBookingSummaryResponse;

@Mapper
public interface HotelBookingSummaryDao {
    Optional<HotelBookingSummaryResponse> findByUserId(@Param("userId") Long userId);
}
