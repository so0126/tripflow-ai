package com.tripflow.ai.supporter.checklist.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.supporter.checklist.dto.response.TravelDayResponse;

@Mapper
public interface ChecklistTravelDayDao {
    TravelDayResponse getTravelDay(
        @Param("planId") Long planId,
        @Param("dayIndex") Integer dayIndex
    );
}
