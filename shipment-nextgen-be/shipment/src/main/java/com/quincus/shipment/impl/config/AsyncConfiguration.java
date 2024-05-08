package com.quincus.shipment.impl.config;

import com.quincus.shipment.impl.web.exception.async.AsyncExceptionHandler;
import com.quincus.web.common.utility.logging.MdcTaskWrapper;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor(InternalExecutorProperties asyncProperties) {
        return createExecutor(asyncProperties);
    }

    @Bean(name = "externalApiExecutor")
    public Executor externalApiExecutor(ExternalApiExecutorProperties asyncProperties) {
        return createExecutor(asyncProperties);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncExceptionHandler();
    }

    private Executor createExecutor(AsyncProperties executorProperties) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                super.execute(new MdcTaskWrapper(task));
            }
        };
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setThreadNamePrefix(executorProperties.getThreadNamePrefix());
        executor.setAwaitTerminationSeconds(executorProperties.getAwaitTerminationSeconds());
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}