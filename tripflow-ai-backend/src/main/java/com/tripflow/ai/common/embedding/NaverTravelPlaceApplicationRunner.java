package com.tripflow.ai.common.embedding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "app.travel-place.import.naver",
        name = {"enabled", "startup.enabled"},
        havingValue = "true"
)
public class NaverTravelPlaceApplicationRunner implements ApplicationRunner {

    private final NaverTravelPlaceImportService importService;
    private final NaverImportProperties importProperties;
    private final ConfigurableApplicationContext applicationContext;

    public NaverTravelPlaceApplicationRunner(
            NaverTravelPlaceImportService importService,
            NaverImportProperties importProperties,
            ConfigurableApplicationContext applicationContext
    ) {
        this.importService = importService;
        this.importProperties = importProperties;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        importService.importTravelPlaces();
        closeIfNeeded();
    }

    private void closeIfNeeded() {
        if (importProperties.getStartup().isExitOnComplete()) {
            log.info("Naver travel place import requested application shutdown.");
            applicationContext.close();
        }
    }
}
