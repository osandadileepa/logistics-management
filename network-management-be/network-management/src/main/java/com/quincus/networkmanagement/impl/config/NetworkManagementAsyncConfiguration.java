package com.quincus.networkmanagement.impl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class NetworkManagementAsyncConfiguration {

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor(NetworkManagementAsyncProperties networkManagementAsyncProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(networkManagementAsyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(networkManagementAsyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(networkManagementAsyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix(networkManagementAsyncProperties.getThreadNamePrefix());
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
