package com.tripflow.ai.common.sse.dto;

import lombok.Data;

@Data
public class SseSendRequest {
  private String eventName;
  private Object data;
}
