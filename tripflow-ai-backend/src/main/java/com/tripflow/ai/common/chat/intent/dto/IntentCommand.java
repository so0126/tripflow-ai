package com.tripflow.ai.common.chat.intent.dto;

import java.util.Map;

import com.tripflow.ai.common.chat.intent.CategoryType;
import com.tripflow.ai.common.chat.intent.IntentType;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class IntentCommand {
    private String originalUserMessage;
    private CategoryType category;
    private IntentType intent;
    private double confidence;
    private Map<String, Object> arguments;
    private String requiredUrl;
}