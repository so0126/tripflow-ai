package com.tripflow.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tripflow.ai.common.embedding.EmbeddingBackfillProperties;
import com.tripflow.ai.common.embedding.NaverImportProperties;
import com.tripflow.ai.common.naver.NaverApiProperties;

@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({NaverImportProperties.class, EmbeddingBackfillProperties.class, NaverApiProperties.class})
@SpringBootApplication
public class TripFlowAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripFlowAiApplication.class, args);
	}

}
