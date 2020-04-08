package com.miotech.mdp.quality.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author: shanyue.gao
 * @date: 2020/1/3 5:05 PM
 */

@Configuration
public class ThreadPoolConfig {

    @Value("${datatest.executor.corePoolSize}")
    private int core;

    @Value("${datatest.executor.maxPoolSize}")
    private int max;

    @Value("${datatest.executor.keepAliveSeconds}")
    private int alive;

    @Value("${datatest.executor.queueCapacity}")
    private int queue;

    @Bean(name = "caseExecutorPool")
    public ThreadPoolTaskExecutor caseExecutorPool() {
        int corePoolSize = core;
        int maxPoolSize = max;
        int keepAliveSeconds = alive;
        int queueCapacity = queue;
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        return executor;
    }
}
