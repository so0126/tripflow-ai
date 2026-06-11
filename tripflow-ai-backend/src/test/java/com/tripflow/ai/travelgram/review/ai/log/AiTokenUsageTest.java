package com.tripflow.ai.travelgram.review.ai.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * AiTokenUsage.from(ChatResponse)의 토큰 추출 및 null-guard 검증.
 *
 * 핵심 불변식: 외부 응답의 usage 메타데이터가 어느 단계든 비어 와도 NPE 없이 empty()로 흡수한다.
 * (방어 코드를 호출부마다 흩지 않고 값객체 생성 시점 한 곳에 모은다.)
 */
public class AiTokenUsageTest {

    @Test
    @DisplayName("usage가 있으면 prompt/completion/total을 Long으로 매핑한다")
    public void from_whenUsagePresent_mapsTokens() {
        Usage usage = mock(Usage.class);
        when(usage.getPromptTokens()).thenReturn(10);
        when(usage.getCompletionTokens()).thenReturn(20);
        when(usage.getTotalTokens()).thenReturn(30);

        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getUsage()).thenReturn(usage);

        ChatResponse response = mock(ChatResponse.class);
        when(response.getMetadata()).thenReturn(metadata);

        AiTokenUsage result = AiTokenUsage.from(response);

        assertThat(result.promptTokens()).isEqualTo(10L);
        assertThat(result.completionTokens()).isEqualTo(20L);
        assertThat(result.totalTokens()).isEqualTo(30L);
    }

    @Test
    @DisplayName("response가 null이면 empty()를 반환한다")
    public void from_whenResponseNull_returnsEmpty() {
        AiTokenUsage result = AiTokenUsage.from(null);

        assertThat(result.promptTokens()).isNull();
        assertThat(result.completionTokens()).isNull();
        assertThat(result.totalTokens()).isNull();
    }

    @Test
    @DisplayName("metadata가 null이면 empty()를 반환한다")
    public void from_whenMetadataNull_returnsEmpty() {
        ChatResponse response = mock(ChatResponse.class);
        when(response.getMetadata()).thenReturn(null);

        AiTokenUsage result = AiTokenUsage.from(response);

        assertThat(result.totalTokens()).isNull();
    }

    @Test
    @DisplayName("usage가 null이면 empty()를 반환한다")
    public void from_whenUsageNull_returnsEmpty() {
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        when(metadata.getUsage()).thenReturn(null);

        ChatResponse response = mock(ChatResponse.class);
        when(response.getMetadata()).thenReturn(metadata);

        AiTokenUsage result = AiTokenUsage.from(response);

        assertThat(result.totalTokens()).isNull();
    }
}
