package com.tripflow.ai.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 작업용 Executor 빈 정의.
 *
 * 리뷰 사진 AI 분석(@Async)을 Spring Boot 자동 구성 기본 풀(무제한 큐)이 아니라
 * 사진 분석 전용 풀에서 돌린다. 목적은 두 가지:
 * - 격리(bulkhead): 외부 OpenAI에 의존하는 느린 작업이 폭주해도 다른 @Async 작업의 풀을 잠식하지 않게 분리.
 * - 백프레셔: 유한 큐 + 거부 정책으로 유입 속도를 제한해, 과부하 시 메모리에 무한정 쌓이는 것을 막는다.
 *
 * @EnableAsync는 TripFlowAiApplication에 이미 있으므로 여기서 다시 붙이지 않는다.
 */
@Configuration
public class AsyncConfig {

    @Bean("photoAnalysisExecutor")
    public Executor photoAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 동시 분석 스레드 수. 사진 분석은 OpenAI 응답 대기가 대부분인 I/O 바운드라
        // CPU 코어 수에 묶이지 않는다. 4(core)~8(max)는 출발점이며,
        // 실제 동시 업로드량과 OpenAI 응답 시간을 보고 조정한다.
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);

        // 유한 큐 → 백프레셔. 무제한 큐였다면 폭주 시 OOM까지 갈 수 있다.
        // 50은 "max 스레드가 다 찼을 때 추가로 버틸 버퍼" 크기. 실측 후 조정.
        executor.setQueueCapacity(50);

        // 운영 로그/스레드 덤프에서 사진 분석 스레드를 바로 식별하기 위한 prefix.
        executor.setThreadNamePrefix("photo-analysis-");

        // 큐까지 가득 차면 호출(요청) 스레드가 직접 실행한다.
        // → 새 작업 유입이 자연스럽게 느려지는 속도 제한 효과. (1차 정책, 5장 조각 5 참고)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 배포로 앱이 내려갈 때 진행 중인 분석을 최대 30초까지 기다린다.
        // → 분석 중이던 사진이 PENDING에 멈춘 채 유실되는 것을 줄인다.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}
