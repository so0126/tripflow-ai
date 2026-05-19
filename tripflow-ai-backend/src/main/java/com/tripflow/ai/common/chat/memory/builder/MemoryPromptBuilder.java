package com.tripflow.ai.common.chat.memory.builder;

import java.util.List;
import java.util.stream.Collectors;

import com.tripflow.ai.common.chat.dto.ChatMemory;
import com.tripflow.ai.common.chat.dto.ChatMemoryVector;
import com.tripflow.ai.common.chat.dto.MemoryBundle;

public class MemoryPromptBuilder {
    private static final int SHORT_MEM_LIMIT = 10;   // shortMemory 중 최근 N개만 직접 포함 (더 오래된 건 요약으로 교체가능)
    private static final int MAX_CHARS = 3000;       // 전체 prompt 가 너무 길어지지 않도록 안전장치

    public static String build(MemoryBundle bundle, String userMessage) {
        StringBuilder sb = new StringBuilder();

        // 🔹 대전제: 불필요한 설명 제거, LLM에게 필요한 구조만 제공
        sb.append("### Conversation History (Recent)\n");
        List<ChatMemory> shortMem = bundle.getShortMemory();

        if (shortMem != null && !shortMem.isEmpty()) {
            List<ChatMemory> truncated =
                    shortMem.stream().limit(SHORT_MEM_LIMIT).collect(Collectors.toList());

            for (ChatMemory m : truncated) {
                sb.append(m.getRole()).append(": ")
                        .append(truncate(m.getContent(), 400))
                        .append("\n");
            }
        } else {
            sb.append("(none)\n");
        }

        // 🔹 Long-term memory (이미 유사도 높은 순서로 정렬된 상태)
        sb.append("\n### Relevant Long-term Memories\n");
        List<ChatMemoryVector> longMem = bundle.getLongMemory();

        if (longMem != null && !longMem.isEmpty()) {
            for (ChatMemoryVector doc : longMem) {
                sb.append("- ")
                        .append(truncate(doc.getContent(), 300))
                        .append("\n");
            }
        } else {
            sb.append("(none)\n");
        }

        // 🔹 Context (옵션)
        sb.append("\n### Context\n");
        if (bundle.getContext() != null) {
            sb.append(bundle.getContext().toString()).append("\n");
        } else {
            sb.append("(none)\n");
        }

        // 🔹 User message
        sb.append("\n### User Message\n");
        sb.append(userMessage).append("\n");

        // 🔹 최종 Task: 딱 한 문장으로!
        sb.append("\n### Task\n");
        sb.append("현재 질문을 우선하여 답변하고, 필요한 경우 short/long memory에서 관련 정보를 사용하라.\n");

        String prompt = sb.toString();
        return prompt.length() > MAX_CHARS ? prompt.substring(0, MAX_CHARS) : prompt;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
