package com.miotech.mdp.flow.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.miotech.mdp.common.util.StringUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class S3ClientConfig {

    @Value("${s3.endpoint.url:https://s3.ap-northeast-1.amazonaws.com}")
    private String s3EndpointUrl = null;

    @Value("${s3.region:ap-northeast-1}")
    private String s3Region = null;

    @Value("${s3.secret_access_key}")
    private String s3SecretAccessKey = null;

    @Value("${s3.access_key_id}")
    private String s3AccessKeyId = null;

    @Bean
    public AmazonS3 createS3() {
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3AccessKeyId, s3SecretAccessKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(s3Region.toLowerCase()))
                .build();
    }
}
