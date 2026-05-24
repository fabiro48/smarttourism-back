package com.smarttourism.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable asynchronous method execution and scheduled tasks.
 *
 * <p>{@code @EnableAsync} allows methods annotated with {@code @Async} to run
 * in a separate thread pool, preventing blocking of the main application flow.
 *
 * <p>{@code @EnableScheduling} enables methods annotated with {@code @Scheduled}
 * to run at fixed intervals or cron expressions.
 *
 * <p>Validates: Requirements 10.5, 6.3
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Default async configuration is sufficient for this application
    // Spring Boot auto-configures a SimpleAsyncTaskExecutor
}
