package com.tripflow.ai.common.embedding;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.travel-place.import.naver")
public class NaverImportProperties {
    private boolean enabled = false;
    private int targetCount = 1000;
    private String cron = "0 0 3 * * SUN";
    private Startup startup = new Startup();
    private Schedule schedule = new Schedule();

    @Getter
    @Setter
    public static class Startup {
        private boolean enabled = false;
        private boolean exitOnComplete = false;
    }

    @Getter
    @Setter
    public static class Schedule {
        private boolean enabled = false;
    }
}
