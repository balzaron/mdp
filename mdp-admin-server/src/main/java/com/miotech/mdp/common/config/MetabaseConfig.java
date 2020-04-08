package com.miotech.mdp.common.config;

import com.google.protobuf.InvalidProtocolBufferException;
import com.miotech.mdp.common.client.metabase.MetabaseClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetabaseConfig {

    @Value("${metabase.auth.username}")
    private String username = null;

    @Value("${metabase.auth.password}")
    private String password = null;

    @Value("${metabase.auth.sessioncode}")
    private String sessioncode = null;

    @Bean
    public MetabaseClient createMetaClient() throws InvalidProtocolBufferException {
        if (sessioncode != null && !sessioncode.isEmpty()) {
            return new MetabaseClient(sessioncode);
        }
        return new MetabaseClient(username, password);
    }
}
