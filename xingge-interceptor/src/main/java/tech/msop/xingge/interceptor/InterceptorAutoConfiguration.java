package tech.msop.xingge.interceptor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 拦截器自动配置类
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "xingge.interceptor", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(InterceptorProperties.class)
@ComponentScan(basePackages = "tech.msop.xingge.interceptor")
public class InterceptorAutoConfiguration {
    
}