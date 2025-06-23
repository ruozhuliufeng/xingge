package tech.msop.core.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import tech.msop.core.log.property.XingGeLogProperty;

/**
 * 行歌-日志配置
 */
@Configuration
@EnableConfigurationProperties(XingGeLogProperty.class)
public class XingGeLogConfig {
}
