/*
 * Copyright (c) 2024 行歌(xingge)
 * RestTemplate客户端拦截器配置类
 * 
 * 功能说明：
 * - 配置RestTemplate客户端的请求拦截器
 * - 根据配置条件自动装配
 * - 支持拦截RestTemplate的请求和响应
 */
package tech.request.core.request.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.handler.RequestLogHandler;
import tech.request.core.request.interceptor.RestTemplateRequestInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * RestTemplate客户端拦截器配置类
 * 
 * <p>该类负责配置RestTemplate客户端的请求拦截器，用于拦截RestTemplate发起的HTTP请求。</p>
 * <p>只有在满足以下条件时才会启用：</p>
 * <ul>
 *   <li>类路径中存在RestTemplate</li>
 *   <li>配置中启用了RestTemplate拦截</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Configuration
@ConditionalOnClass(RestTemplate.class)
@ConditionalOnProperty(prefix = "xg.request.http-client", name = "rest-template-enabled", havingValue = "true", matchIfMissing = true)
public class RestTemplateConfiguration {
    
    /**
     * 配置RestTemplate请求拦截器
     * 
     * @param requestLogHandler 请求日志处理器
     * @param property 拦截器配置属性
     * @return RestTemplate请求拦截器
     */
    @Bean
    public RestTemplateRequestInterceptor restTemplateRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        return new RestTemplateRequestInterceptor(requestLogHandler, property);
    }
    
    /**
     * 配置RestTemplate，添加请求拦截器
     * 
     * @param interceptor RestTemplate请求拦截器
     * @return RestTemplate
     */
    @Bean
    @ConditionalOnClass(RestTemplate.class)
    public RestTemplate restTemplate(RestTemplateRequestInterceptor interceptor) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}