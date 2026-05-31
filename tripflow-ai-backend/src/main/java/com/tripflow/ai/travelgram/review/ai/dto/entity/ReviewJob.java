package com.tripflow.ai.travelgram.review.ai.dto.entity;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReviewJob {
    private Long id;
    private String jobType;
    private String status;

    private Long reviewPostId;
    private Long photoGroupId;
    private Long photoId;

    private String errorMessage;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
