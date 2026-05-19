package com.tripflow.ai.travelgram.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ReviewPhotoUploadResponse {
    private Long id;
    private String fileUrl;
    private Integer orderIndex;
}