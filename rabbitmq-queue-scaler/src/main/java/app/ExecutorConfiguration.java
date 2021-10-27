package app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfiguration {
    @Bean
    public Executor executor() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }
}
