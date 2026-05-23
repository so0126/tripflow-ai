package com.tripflow.ai.common.embedding;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.travel-place.import.naver",
        name = {"enabled", "schedule.enabled"},
        havingValue = "true"
)
public class NaverTravelPlaceScheduledRunner {

    private final NaverTravelPlaceImportService importService;

    public NaverTravelPlaceScheduledRunner(NaverTravelPlaceImportService importService) {
        this.importService = importService;
    }

    @Scheduled(cron = "${app.travel-place.import.naver.cron:0 0 3 * * SUN}")
    public void run() {
        importService.importTravelPlaces();
    }
}
