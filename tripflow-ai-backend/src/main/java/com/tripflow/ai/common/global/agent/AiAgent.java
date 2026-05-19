package com.tripflow.ai.common.global.agent;

import com.tripflow.ai.common.chat.intent.dto.IntentCommand;
import com.tripflow.ai.common.chat.pipeline.AiAgentResponse;

// AiAgent 라우터를 위한 인터페이스
public interface AiAgent {
    AiAgentResponse execute(IntentCommand command, Long userid);
}
