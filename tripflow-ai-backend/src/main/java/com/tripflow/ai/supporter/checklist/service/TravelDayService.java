package com.tripflow.ai.supporter.checklist.service;

import org.springframework.stereotype.Service;

import com.tripflow.ai.supporter.checklist.dao.ChecklistTravelDayDao;
import com.tripflow.ai.supporter.checklist.dto.response.TravelDayResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelDayService {
    
    private final ChecklistTravelDayDao travelDayDao;
    
    public TravelDayResponse getTravelDay(Long planId, Integer dayIndex) {
        log.info("📅 Getting travel day - planId: {}, dayIndex: {}", planId, dayIndex);
        
        TravelDayResponse result = travelDayDao.getTravelDay(planId, dayIndex);
        
        if (result == null) {
            log.warn("⚠️ Travel day not found - planId: {}, dayIndex: {}", planId, dayIndex);
            return null;
        }
        
        log.info("✅ Found travel day: '{}' with {} places", 
            result.getDayTitle(), 
            result.getPlaces() != null ? result.getPlaces().size() : 0);
        
        return result;
    }
}
