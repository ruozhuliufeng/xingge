package tech.msop.core.oss.config;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
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
import tech.msop.core.oss.template.AliossTemplate;

/**
 * Alioss 配置类
 *
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@AutoConfigureBefore(OssConfiguration.class)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnClass({OSSClient.class})
@ConditionalOnProperty(value = "ms.name", havingValue = "alioss")
public class AliossConfiguration {

    private final OssProperties properties;
    private final OssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(OSSClient.class)
    public OSSClient ossClient() {
        // 创建ClientConfiguration ClientConfiguration是OSS Client的配置类，可以设置超时、proxy等参数。
        ClientConfiguration clientConfig = new ClientConfiguration();
        // 设置OSSClient可以请求的最大建立连接数，默认为1024个。
        clientConfig.setMaxConnections(1024);
        // 设置Socket层传输数据的超时时间，默认为50000毫秒。
        clientConfig.setSocketTimeout(50000);
        // 设置建立连接的超时时间，默认为5000毫秒。
        clientConfig.setConnectionTimeout(5000);
        // 设置从连接池获取连接的超时时间，默认为5000毫秒。
        clientConfig.setConnectionRequestTimeout(5000);
        // 设置连接空闲超时时间，默认为600000毫秒
        clientConfig.setIdleConnectionTime(600000);
        // 设置失败请求重试次数，默认为3次。
        clientConfig.setMaxErrorRetry(3);
        CredentialsProvider clientCredentialsProvider = new DefaultCredentialProvider(properties.getAccessKey(),properties.getSecretKey());
        return new OSSClient(properties.getEndpoint(), clientCredentialsProvider, clientConfig);
    }

    @Bean
    @ConditionalOnBean(OSSClient.class)
    @ConditionalOnMissingBean(AliossTemplate.class)
    public AliossTemplate aliossTemplate(OSSClient client){
        return new AliossTemplate(client,properties,ossRule);
    }

}
