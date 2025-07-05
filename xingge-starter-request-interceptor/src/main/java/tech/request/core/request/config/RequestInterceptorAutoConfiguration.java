/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求拦截器自动配置类
 * 
 * 功能说明：
 * - 自动配置请求拦截器相关组件
 * - 根据配置条件装配不同的存储实现
 * - 配置各种HTTP客户端的拦截器
 */
package tech.request.core.request.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.handler.RequestLogHandler;
import tech.request.core.request.storage.impl.LogRequestLogStorage;
import tech.request.core.request.storage.impl.MongoRequestLogStorage;
import tech.msop.interceptor.*;
import tech.request.core.request.storage.RequestLogStorage;

/**
 * 请求拦截器自动配置类
 * 
 * <p>该类负责根据配置自动装配请求拦截器相关组件，包括：</p>
 * <ul>
 *   <li>各种存储实现</li>
 *   <li>HTTP客户端拦截器</li>
 *   <li>请求日志处理器</li>
 * </ul>
 * 
 * <p>通过条件装配，只有在满足特定条件时才会创建相应的Bean。</p>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableConfigurationProperties(RequestInterceptorProperty.class)
@ConditionalOnProperty(prefix = "xg.request", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    OkHttpConfiguration.class,
    RestTemplateConfiguration.class,
    OpenFeignConfiguration.class
})
public class RequestInterceptorAutoConfiguration {
    
    /**
     * 配置日志存储实现
     * 
     * @return 日志存储实现
     */
    @Bean
    @ConditionalOnMissingBean(RequestLogStorage.class)
    @ConditionalOnProperty(prefix = "xg.request", name = "storage-type", havingValue = "LOG", matchIfMissing = true)
    public RequestLogStorage logStorage() {
        return new LogRequestLogStorage();
    }
    
    /**
     * 配置MongoDB存储实现
     * 
     * @return MongoDB存储实现
     */
    @Bean
    @ConditionalOnMissingBean(RequestLogStorage.class)
    @ConditionalOnProperty(prefix = "xg.request", name = "storage-type", havingValue = "MONGO")
    public RequestLogStorage mongoStorage() {
        return new MongoRequestLogStorage();
    }
    
    /**
     * 配置请求日志处理器
     * 
     * @param storage 日志存储实现
     * @param property 拦截器配置属性
     * @return 请求日志处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestLogHandler requestLogHandler(RequestLogStorage storage, RequestInterceptorProperty property) {
        return new RequestLogHandler(storage, property);
    }
}