package com.tripflow.ai.common.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.travel-place.import.naver")
public record NaverImportProperties(
        boolean enabled,
        @DefaultValue("1000") int targetCount,
        @DefaultValue("0 0 3 * * SUN") String cron,
        @DefaultValue Startup startup,
        @DefaultValue Schedule schedule
) {
    public record Startup(
            boolean enabled,
            boolean exitOnComplete
    ) {
    }

    public record Schedule(
            boolean enabled
    ) {
    }
}
