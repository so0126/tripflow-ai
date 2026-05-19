package com.tripflow.ai.supporter.imageSearch.dto.response;

import java.time.OffsetDateTime;

import com.tripflow.ai.supporter.imageSearch.dto.entity.ActionType;

import lombok.Data;

@Data
public class ImageSearchSessionResponse {
    private Long id;
    private Long userId;
    private OffsetDateTime createdAt;
    private ActionType actionType;
}
