package com.tripflow.ai.common.chat.intent.dto.response;

import java.util.List;

import com.tripflow.ai.common.chat.intent.dto.IntentItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IntentResponse {

    private List<IntentItem> intents;
    
}