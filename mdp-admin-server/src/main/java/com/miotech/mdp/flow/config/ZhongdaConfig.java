package com.miotech.mdp.flow.config;

import com.miotech.mdp.common.client.ZhongdaClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ZhongdaConfig {
    @Value("${zhongda.host}")
    private String host = null;

    @Value("${zhongda.token}")
    private String token;

    @Bean
    public ZhongdaClient createZhongda() {
        return new ZhongdaClient(host, token);
    }
}
