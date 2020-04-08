package com.miotech.mdp.flow.config;

import com.miotech.mdp.common.util.StringUtil;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class K8sClientConfig {

    @Value("${k8s.api.host}")
    private String host = null;

    @Value("${k8s.api.user}")
    private String user;

    @Value("${k8s.api.password}")
    private String password = null;

    @Value("${k8s.ssl.certDir}")
    private String sslCertDir = null;

    @Value("${k8s.api.namespace}")
    private String nameSpace = null;

    @Value("${k8s.s3fs.image}")
    private String s3FsImage = null;

    @Value("${k8s.s3fs.mnt_point: /opt/mdp}")
    private String s3FsMntPoint = null;

    @Value("${k8s.s3fs.share_volume_name: s3-data}")
    private String s3FsShareVolumeName = null;

    @Bean
    public KubernetesClient createK8sClient() {
        ConfigBuilder builder = new ConfigBuilder().withMasterUrl(host);
        if (!StringUtil.isNullOrEmpty(user)) {
            builder.withUsername(user);
        }
        if (!StringUtil.isNullOrEmpty(password)) {
            builder.withPassword(password);
        }
        if (!StringUtil.isNullOrEmpty(sslCertDir)) {
            builder.withCaCertFile(sslCertDir);
        }
        if (!StringUtil.isNullOrEmpty(nameSpace)) {
            builder.withNamespace(nameSpace);
        }
        Config config = builder.build();
        return new DefaultKubernetesClient(config);
    }
}
