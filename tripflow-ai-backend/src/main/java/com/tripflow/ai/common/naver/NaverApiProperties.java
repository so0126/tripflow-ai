package com.tripflow.ai.common.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "naver.api")
public class NaverApiProperties {
    private String clientId;
    private String clientSecret;
}
