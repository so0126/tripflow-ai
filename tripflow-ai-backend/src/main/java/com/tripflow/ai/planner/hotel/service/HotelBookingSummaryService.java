package com.tripflow.ai.planner.hotel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripflow.ai.planner.hotel.dao.HotelBookingSummaryDao;
import com.tripflow.ai.planner.hotel.dto.entity.HotelBookingSummary.HotelBookingSummaryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelBookingSummaryService {

    private final HotelBookingSummaryDao hotelBookingSummaryDao;

    @Transactional(readOnly = true)
    public HotelBookingSummaryResponse getBookingSummaryByUserId(Long userId) {
        return hotelBookingSummaryDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 유저의 호텔 예약이 없습니다. userId=" + userId));
    }
}
