package com.tripflow.ai.planner.plan.dto.response;

import java.util.List;

import com.tripflow.ai.planner.plan.dto.entity.PlanDay;
import com.tripflow.ai.planner.plan.dto.entity.PlanPlace;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlanDayWithPlaces {
    private PlanDay day;
    private List<PlanPlace> places;
}
