package com.tripflow.ai.supporter.imageSearch.dto.response;

import lombok.Data;

@Data
public class CandidateWithPlaceResponse {
    private Long candidateId;
    private Boolean isSelected;
    private Long rank;
    private ImagePlaceResponse place;
}
