package tech.msop.core.oss.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
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
import tech.msop.core.oss.template.TencentCosTemplate;

/**
 * Tencent COS 配置类
 *
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@AutoConfigureBefore(OssConfiguration.class)
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnClass({COSClient.class})
@ConditionalOnProperty(value = "ms.name", havingValue = "tencentcos")
public class TencentCosConfiguration {

    private final OssProperties properties;
    private final OssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(COSClient.class)
    public COSClient cosClient() {
        // 初始化用户信息
        COSCredentials credentials = new BasicCOSCredentials(properties.getAccessKey(), properties.getSecretKey());
        // 设置bucket的区域  COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        Region region = new Region(properties.getRegion());
        // 创建ClientConfiguration ClientConfiguration是OSS Client的配置类，可以设置超时、proxy等参数。
        ClientConfig clientConfig = new ClientConfig(region);
        // 设置OSSClient可以请求的最大建立连接数，默认为1024个。
        clientConfig.setMaxConnectionsCount(1024);
        // 设置Socket层传输数据的超时时间，默认为50000毫秒。
        clientConfig.setSocketTimeout(50000);
        // 设置建立连接的超时时间，默认为5000毫秒。
        clientConfig.setConnectionTimeout(5000);
        // 设置从连接池获取连接的超时时间，默认为5000毫秒。
        clientConfig.setConnectionRequestTimeout(5000);
        return new COSClient(credentials, clientConfig);
    }

    @Bean
    @ConditionalOnBean(COSClient.class)
    @ConditionalOnMissingBean(TencentCosTemplate.class)
    public TencentCosTemplate tencentCosTemplate(COSClient client) {
        return new TencentCosTemplate(client, properties, ossRule);
    }

}
