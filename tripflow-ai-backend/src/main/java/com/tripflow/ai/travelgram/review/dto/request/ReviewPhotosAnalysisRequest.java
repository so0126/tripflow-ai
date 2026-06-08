package com.tripflow.ai.travelgram.review.dto.request;

import java.util.List;

import lombok.Data;
@Data
public class ReviewPhotosAnalysisRequest {
    private Long reviewPostId;
    private List<String> summaries;
}
