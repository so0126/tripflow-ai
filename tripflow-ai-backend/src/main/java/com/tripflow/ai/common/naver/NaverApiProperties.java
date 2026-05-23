package com.tripflow.ai.common.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.api")
public record NaverApiProperties(
        String clientId,
        String clientSecret
) {
}
