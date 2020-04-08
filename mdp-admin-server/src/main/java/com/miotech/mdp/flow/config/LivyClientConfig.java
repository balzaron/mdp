package com.miotech.mdp.flow.config;

import com.miotech.mdp.common.client.spark.LivyClient;
import com.miotech.mdp.common.client.spark.SparkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LivyClientConfig {

    @Value("${livy.host}")
    private String host = null;

    @Value("${livy.proxyUser}")
    private String proxyUser = null;

    @Value("${livy.queue: default}")
    private String queue;

    @Value("${yarn.host}")
    private String yarnHost = null;

    @Value("${yarn.port: 8088}")
    private Integer yarnPort = null;

    @Bean
    public LivyClient createLivyClient() {
        return new LivyClient(host, queue, proxyUser);
    }

    @Bean
    public SparkClient createSparkClient() {
        return new SparkClient(yarnHost, yarnPort);
    }
}
