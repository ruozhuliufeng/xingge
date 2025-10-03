package tech.msop.core.oss.config;

import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
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
import tech.msop.core.oss.rule.XingGeOssRule;
import tech.msop.core.oss.template.HuaweiObsTemplate;

/**
 * 华为 Obs 配置类
 *
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@AutoConfigureBefore(OssConfiguration.class)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnClass({ObsClient.class})
@ConditionalOnProperty(value = "ms.name", havingValue = "huaweiobs")
public class HuaweiObsConfiguration {

    private final OssProperties properties;

    @Bean
    @ConditionalOnMissingBean(OssRule.class)
    public OssRule ossRule() {
        return new XingGeOssRule(properties.getTenantMode());
    }

    @Bean
    @ConditionalOnMissingBean(ObsClient.class)
    public ObsClient obsClient() {
        // 创建ClientConfiguration ClientConfiguration是OSS Client的配置类，可以设置超时、proxy等参数。
        ObsConfiguration clientConfig = new ObsConfiguration();
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
        return new ObsClient(properties.getAccessKey(),properties.getSecretKey(),clientConfig);
    }

    @Bean
    @ConditionalOnBean({ObsClient.class,OssRule.class})
    @ConditionalOnMissingBean(HuaweiObsTemplate.class)
    public HuaweiObsTemplate huaweiObsTemplate(ObsClient client,OssRule ossRule) {
        return new HuaweiObsTemplate(client, properties, ossRule);
    }

}
