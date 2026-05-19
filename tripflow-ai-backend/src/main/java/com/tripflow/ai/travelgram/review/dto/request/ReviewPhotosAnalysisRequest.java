package com.tripflow.ai.travelgram.review.dto.request;

import java.util.List;

import lombok.Data;
@Data
public class ReviewPhotosAnalysisRequest {
    private Long photoGroupId;
    private List<String> summaries;
}
