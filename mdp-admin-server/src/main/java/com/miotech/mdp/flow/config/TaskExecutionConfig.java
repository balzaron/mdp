package com.miotech.mdp.flow.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TaskExecutionConfig {


    @Value("${mdp.hostname}")
    private String mdpHost;

    @Value("${mdp.s3.bucket}")
    private String mdpS3Bucket;

    @Value("${mdp.s3.enable: true}")
    private Boolean mdpS3Enable;

    @Value("#{systemProperties['user.dir']}")
    private String baseDirectory;

    public String getLoggingDirectory() {
        return this.baseDirectory + "/logs";
    }

}
