package tech.msop.core.oss.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.msop.core.oss.propertis.OssProperties;
import tech.msop.core.oss.rule.OssRule;
import tech.msop.core.oss.rule.XingGeOssRule;

/**
 * Oss 配置类
 *
 * @author ruozhuliufeng
 */
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@EnableConfigurationProperties(OssProperties.class)
public class OssConfiguration {

    private final OssProperties ossProperties;

    @Bean
    @ConditionalOnMissingBean(OssRule.class)
    public OssRule ossRule() {
        return new XingGeOssRule(ossProperties.getTenantMode());
    }
}
