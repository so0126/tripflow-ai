package com.tripflow.ai.planner.hotel.dto.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

public class HotelBookingSummary {

    @Data
    public static class HotelBookingSummaryResponse {
        private Long id;
        private Long userId;
        private String hotelName;
        private String roomType;
        private LocalDateTime checkinDate;
        private LocalDateTime checkoutDate;

        @JsonIgnore
        private LocalDateTime createdAt;
    }
}
