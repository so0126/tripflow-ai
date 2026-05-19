package com.tripflow.ai.test.testchat.agent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tripflow.ai.common.chat.dto.MemoryBundle;
import com.tripflow.ai.common.chat.memory.builder.MemoryPromptBuilder;
import com.tripflow.ai.common.chat.memory.service.ChatMemoryService;
import com.tripflow.ai.common.chat.memory.service.MemoryRetrievalService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConversationJHTestAgent {

    private final ChatClient chatClient;
    private final ChatMemoryService chatMemoryService;
    private final MemoryRetrievalService memoryRetrievalService;

    @Autowired
    public ConversationJHTestAgent(
            ChatClient.Builder chatClientBuilder,
            ChatMemoryService chatMemoryService,
            MemoryRetrievalService memoryRetrievalService
    ) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemoryService = chatMemoryService;
        this.memoryRetrievalService = memoryRetrievalService;
    }

    public String chat(Long userId, String userMessage) {
        log.info("📥 사용자 입력 (userId: {}): {}", userId, userMessage);

        long start = System.currentTimeMillis();

        // 1️⃣ 메모리 조회 (Short + Long term)
        long memStart = System.currentTimeMillis();
        MemoryBundle memoryBundle =
                memoryRetrievalService.retrieveAll(userId, userMessage, null);
        long memEnd = System.currentTimeMillis();

        // 프롬프트 만들 때 사용되는 short/long 개수
        int usedShortCount = (memoryBundle.getShortMemory() != null)
                ? memoryBundle.getShortMemory().size() : 0;
        int usedLongCount = (memoryBundle.getLongMemory() != null)
                ? memoryBundle.getLongMemory().size() : 0;

        // 2️⃣ 메모리를 포함한 프롬프트 생성
        String memoryPrompt = MemoryPromptBuilder.build(memoryBundle, userMessage);

        // 3️⃣ LLM 호출
        long llmStart = System.currentTimeMillis();
        String llmResponse = chatClient
                .prompt()
                .system("""
                        당신은 친절한 채팅 어시스턴트입니다.
                        사용자와 자연스럽게 대화하세요.
                        이전 대화 내용을 참고하여 일관성 있는 답변을 제공하세요.
                        """)
                .user(memoryPrompt)
                .call()
                .content();
        long llmEnd = System.currentTimeMillis();

        // 4️⃣ 사용자/AI 메시지 저장
        chatMemoryService.add(userId, userMessage, "user");
        chatMemoryService.add(userId, llmResponse, "assistant");

        long end = System.currentTimeMillis();

        // 🔢 DB 기준 단기/장기 메모리 개수 (전체)
        int shortTotal = chatMemoryService.countShortTerm(userId);
        int longTotal  = chatMemoryService.countLongTerm(userId);

        long memTime   = memEnd - memStart;
        long llmTime   = llmEnd - llmStart;
        long totalTime = end - start;

        // 🧾 성능 / 메모리 로그 출력
        log.info("""
                🧪 Chat Performance (userId: {})
                  - Memory retrieval time : {} ms
                  - LLM call time         : {} ms
                  - Total chat() time     : {} ms
                  - Short-term used in prompt : {} 개
                  - Long-term used in prompt  : {} 개
                  - Short-term total in DB    : {} 개
                  - Long-term total in DB     : {} 개
                """,
                userId,
                memTime,
                llmTime,
                totalTime,
                usedShortCount,
                usedLongCount,
                shortTotal,
                longTotal
        );

        // 🗂 CSV 파일로도 성능 데이터 기록
        writePerfCsv(
                userId,
                memTime,
                llmTime,
                totalTime,
                usedShortCount,
                usedLongCount,
                shortTotal,
                longTotal
        );

        return llmResponse;
    }

    /**
     * 성능/메모리 데이터를 로컬 CSV 파일로 기록
     * 파일 위치: 프로젝트 실행 디렉토리 기준 logs/chat-memory-metrics.csv
     */
    private void writePerfCsv(Long userId,
                              long memTime,
                              long llmTime,
                              long totalTime,
                              int usedShort,
                              int usedLong,
                              int shortTotal,
                              int longTotal) {

        try {
            // 저장할 파일 경로
            Path path = Paths.get("logs", "chat-memory-metrics.csv");

            // logs 디렉토리 없으면 생성
            Files.createDirectories(path.getParent());

            // 타임스탬프 (ISO 포맷)
            String timestamp = OffsetDateTime.now()
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 파일이 없으면 헤더 한 번 기록
            if (!Files.exists(path)) {
                String header = String.join(",",
                        "timestamp",
                        "userId",
                        "memTime_ms",
                        "llmTime_ms",
                        "totalTime_ms",
                        "usedShort_in_prompt",
                        "usedLong_in_prompt",
                        "shortTotal_in_db",
                        "longTotal_in_db"
                ) + System.lineSeparator();

                Files.write(path, header.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            // 데이터 한 줄
            String line = String.join(",",
                    quote(timestamp),
                    String.valueOf(userId),
                    String.valueOf(memTime),
                    String.valueOf(llmTime),
                    String.valueOf(totalTime),
                    String.valueOf(usedShort),
                    String.valueOf(usedLong),
                    String.valueOf(shortTotal),
                    String.valueOf(longTotal)
            ) + System.lineSeparator();

            Files.write(path, line.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (Exception e) {
            log.warn("⚠ 성능 CSV 기록 중 오류 발생", e);
        }
    }

    // CSV용 간단 escape
    private String quote(String value) {
        if (value == null) return "\"\"";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
