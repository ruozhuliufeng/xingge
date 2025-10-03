package tech.msop.core.oss.config;

import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.msop.core.oss.propertis.OssProperties;
import tech.msop.core.oss.rule.OssRule;
import tech.msop.core.oss.template.MinioTemplate;

/**
 * Minio 配置类
 *
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@AutoConfigureBefore(OssConfiguration.class)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnClass({MinioClient.class})
@ConditionalOnProperty(value = "ms.name", havingValue = "minio")
public class MinioConfiguration {

    private final OssProperties properties;
    private final OssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnBean(MinioClient.class)
    @ConditionalOnMissingBean(MinioTemplate.class)
    public MinioTemplate minioTemplate(MinioClient client){
        return new MinioTemplate(client,ossRule,properties);
    }

}
