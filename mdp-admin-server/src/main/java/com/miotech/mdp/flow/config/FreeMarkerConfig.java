package com.miotech.mdp.flow.config;

import com.miotech.mdp.flow.util.AirflowUtil;
import freemarker.template.Template;
import freemarker.template.Version;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class FreeMarkerConfig {

    @Bean
    public Version getVersion() {
        return new Version("2.3.29");
    }

    @Bean
    @Primary
    public freemarker.template.Configuration getConfiguration(Version version) {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(version);
        configuration.setClassForTemplateLoading(this.getClass(), "/");
        return configuration;
    }

    @Bean
    public Template getTemplate(freemarker.template.Configuration configuration) throws IOException {
        return configuration.getTemplate(AirflowUtil.DAG_TEMPLATE_FILE_NAME);
    }
}
