package com.tripflow.ai.travelgram.review.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class ReviewPhotoOrderUpdateRequest {
    private Long reviewPostId;
    private List<PhotoOrderItem> photos;
    
    @Data
    public static class PhotoOrderItem {
        private Long photoId;
        private Integer orderIndex;
    }
}
