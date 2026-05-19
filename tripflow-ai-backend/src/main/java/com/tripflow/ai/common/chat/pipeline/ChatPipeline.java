package com.tripflow.ai.common.chat.pipeline;

import com.tripflow.ai.common.chat.intent.dto.request.IntentRequest;

public interface ChatPipeline {
    PipelineResult execute(IntentRequest request, Long userId);
}
