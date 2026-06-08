package com.tripflow.ai.travelgram.review.dto.entity;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewPost {
    private Long id;
    private String caption;
    
    private Long planId;

    private Long reviewStyleId;
    
    private Boolean isPosted;
    private String reviewPostUrl;

    private String travelType;
    private String overallMoods;
    private OffsetDateTime createdAt;
}
