package com.tripflow.ai.common.chat.pipeline;

import java.util.List;

import com.tripflow.ai.common.chat.intent.dto.IntentCommand;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PipelineResult {

    private final AiAgentResponse mainResponse;
    private final List<IntentCommand> additionalIntents;

    public boolean hasAdditional() {
        return additionalIntents != null && !additionalIntents.isEmpty();
    }
}