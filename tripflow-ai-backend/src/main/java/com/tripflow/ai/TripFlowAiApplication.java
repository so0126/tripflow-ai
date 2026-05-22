package com.tripflow.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@EnableAsync
@SpringBootApplication
public class TripFlowAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripFlowAiApplication.class, args);
	}

}
