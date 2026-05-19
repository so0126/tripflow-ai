package com.tripflow.ai.test.testchat.controller;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tripflow.ai.test.testchat.agent.ConversationJHTestAgent;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/test/chat")
@CrossOrigin(origins = "*")
@Slf4j
public class ConversationJHTestController {

    @Autowired
    private ConversationJHTestAgent conversationTestAgent;

    @Autowired
    private ChatMemory chatMemory;

    @GetMapping
    public String index() {
        return "testChat";
    }

    @PostMapping("/api/{userId}")
    @ResponseBody
    public String chat(
            @PathVariable("userId") Long userId,
            @RequestParam("message") String message  // ← 이름 명시
    ) {
        log.info("📥 메시지 수신 - userId: {}, message: {}", userId, message);
        return conversationTestAgent.chat(userId, message);
    }

    @GetMapping("/api/{userId}/clear")
    @ResponseBody
    public String clearMemory(@PathVariable("userId") Long userId) {
        String conversationId = "test_user_" + userId;
        chatMemory.clear(conversationId);
        log.info("✅ 메모리 삭제: {}", conversationId);
        return "Memory cleared for user " + userId;
    }
}