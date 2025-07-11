/*
 * Copyright (c) 2024 行歌(xingge)
 * OkHttp客户端拦截器配置类
 * 
 * 功能说明：
 * - 配置OkHttp客户端的请求拦截器
 * - 根据配置条件自动装配
 * - 支持拦截OkHttp的请求和响应
 */
package tech.request.core.request.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.interceptor.OkHttpRequestInterceptor;
import tech.request.core.request.handler.RequestLogHandler;

/**
 * OkHttp客户端拦截器配置类
 * 
 * <p>该类负责配置OkHttp客户端的请求拦截器，用于拦截OkHttp发起的HTTP请求。</p>
 * <p>只有在满足以下条件时才会启用：</p>
 * <ul>
 *   <li>类路径中存在OkHttpClient</li>
 *   <li>配置中启用了OkHttp拦截</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
@Configuration
@ConditionalOnClass(OkHttpClient.class)
@ConditionalOnProperty(prefix = "xg.request.http-client", name = "ok-http-enabled", havingValue = "true", matchIfMissing = true)
public class OkHttpConfiguration {
    
    /**
     * 配置OkHttp请求拦截器
     * 
     * @param requestLogHandler 请求日志处理器
     * @param property 拦截器配置属性
     * @return OkHttp请求拦截器
     */
    @Bean
    public OkHttpRequestInterceptor okHttpRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        return new OkHttpRequestInterceptor(requestLogHandler, property);
    }
    
    /**
     * 配置OkHttpClient，添加请求拦截器
     * 
     * @param interceptor OkHttp请求拦截器
     * @return OkHttpClient.Builder
     */
    @Bean
    @ConditionalOnClass(OkHttpClient.class)
    @org.springframework.context.annotation.Primary
    public OkHttpClient.Builder xinggeOkHttpClientBuilder(OkHttpRequestInterceptor interceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor);
    }
}