package com.tripflow.ai.common.chat.pipeline;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tripflow.ai.common.chat.intent.dto.IntentCommand;
import com.tripflow.ai.common.global.agent.AiAgent;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiAgentRouter {

    private final Map<String, AiAgent> aiAgentMap;
    private Map<Class<? extends AiAgent>, AiAgent> agentByClass;

    @PostConstruct
    public void init() {
        agentByClass = aiAgentMap.values().stream()
            .collect(Collectors.toMap(AiAgent::getClass, Function.identity()));
    }

    public AiAgentResponse route(IntentCommand command, Long userId) {
        log.info("🔀 === AiAgentRouter 시작 ===");
        log.info("📋 인텐트 명령: {}", command);
        log.info("👤 사용자 ID: {}", userId);

        Class<? extends AiAgent> clazz = command.getIntent().getAgentClass();
        log.info("🎯 목표 에이전트 클래스: {}", clazz != null ? clazz.getSimpleName() : "없음");

        AiAgent agent = agentByClass.get(clazz);
        log.info("🤖 찾은 에이전트: {}", agent != null ? agent.getClass().getSimpleName() : "없음");
        log.info("📦 사용 가능한 에이전트 목록: {}", aiAgentMap.keySet());

        if (agent == null) {
            log.warn("❌ 에이전트를 찾을 수 없음: {}", clazz);
            return AiAgentResponse.of("지원하지 않는 기능이에요.");
        }

        log.info("✅ 에이전트 실행: {}", agent.getClass().getSimpleName());
        return agent.execute(command, userId);

        // return switch (command.getCategory()) {
        // // case PLANNER -> planner.execute(command);
        // // case SUPPORTER -> supporter.execute(command);
        // // case TRAVELGRAM -> travelgram.execute(command);
        // default -> AiAgentResponse.of("지원하지 않는 기능이에요.");
        // };
    }
}
