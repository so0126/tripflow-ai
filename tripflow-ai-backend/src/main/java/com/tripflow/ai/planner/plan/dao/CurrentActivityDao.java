package com.tripflow.ai.planner.plan.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripflow.ai.planner.plan.dto.entity.CurrentActivity;

@Mapper
public interface CurrentActivityDao {

    void insertCurrentActivity(CurrentActivity currentActivity);

    void updateCurrentActivity(CurrentActivity currentActivity);

    void updatePlanplaceIsEnded(Long planPlaceId);

    CurrentActivity selectCurrentActivityById(Long id);
    CurrentActivity selectCurrentActivityByPlanPlaceId(Long placeId);
    List<CurrentActivity> selectCurrentActivitiesByPlanPlaceIds(@Param("planPlaceIds") List<Long> planPlaceIds);

    // List<CurrentActivity> selectCurrentActivitiesByPlanId(Long planPlaceId);
    
    void deleteCurrentActivity(Long id);
}
