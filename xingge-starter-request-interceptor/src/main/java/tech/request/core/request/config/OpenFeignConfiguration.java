/*
 * Copyright (c) 2024 行歌(xingge)
 * OpenFeign客户端拦截器配置类
 * 
 * 功能说明：
 * - 配置OpenFeign客户端的请求拦截器
 * - 根据配置条件自动装配
 * - 支持拦截OpenFeign的请求和响应
 */
package tech.request.core.request.config;

import feign.Client;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.interceptor.FeignResponseInterceptor;
import tech.request.core.request.interceptor.OpenFeignRequestInterceptor;
import tech.request.core.request.interceptor.RequestLogHandler;

/**
 * OpenFeign客户端拦截器配置类
 * 
 * <p>该类负责配置OpenFeign客户端的请求拦截器，用于拦截OpenFeign发起的HTTP请求。</p>
 * <p>只有在满足以下条件时才会启用：</p>
 * <ul>
 *   <li>类路径中存在Feign</li>
 *   <li>配置中启用了OpenFeign拦截</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(prefix = "xg.request.http-client", name = "open-feign-enabled", havingValue = "true", matchIfMissing = true)
public class OpenFeignConfiguration {
    
    /**
     * 配置OpenFeign请求拦截器
     * 
     * @param requestLogHandler 请求日志处理器
     * @param property 拦截器配置属性
     * @return OpenFeign请求拦截器
     */
    @Bean
    public OpenFeignRequestInterceptor openFeignRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        return new OpenFeignRequestInterceptor(requestLogHandler, property);
    }
    
    /**
     * 配置Feign响应拦截器
     * 
     * @param client 原始Feign客户端
     * @param requestInterceptor OpenFeign请求拦截器
     * @param property 拦截器配置属性
     * @return Feign响应拦截器
     */
    @Bean
    public Client feignClient(Client client, OpenFeignRequestInterceptor requestInterceptor, RequestInterceptorProperty property) {
        return new FeignResponseInterceptor(client, requestInterceptor, property);
    }
}