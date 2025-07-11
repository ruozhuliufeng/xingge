/*
 * Copyright (c) 2024 行歌(xingge)
 * OpenFeign请求拦截器
 * 
 * 功能说明：
 * - 拦截OpenFeign发起的HTTP请求
 * - 收集请求和响应数据
 * - 通过请求日志处理器记录日志
 */
package tech.request.core.request.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.handler.RequestLogHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenFeign请求拦截器
 * 
 * <p>该类实现了Feign的RequestInterceptor接口，用于拦截OpenFeign发起的HTTP请求。</p>
 * <p>拦截器功能：</p>
 * <ul>
 *   <li>收集请求URL、方法、头信息和请求体</li>
 *   <li>收集响应状态码、头信息和响应体</li>
 *   <li>通过请求日志处理器记录日志</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
public class OpenFeignRequestInterceptor implements RequestInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(OpenFeignRequestInterceptor.class);
    private static final String CLIENT_TYPE = "OpenFeign";
    
    /**
     * 请求日志处理器
     */
    private final RequestLogHandler requestLogHandler;
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * 线程本地变量，用于存储请求开始时间
     */
    private final ThreadLocal<LocalDateTime> requestStartTimeThreadLocal = new ThreadLocal<>();
    
    /**
     * 构造函数
     * 
     * @param requestLogHandler 请求日志处理器
     * @param property 拦截器配置属性
     */
    public OpenFeignRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        this.requestLogHandler = requestLogHandler;
        this.property = property;
    }
    
    @Override
    public void apply(RequestTemplate template) {
        // 如果拦截器被禁用，直接返回
        if (!property.isEnabled()) {
            return;
        }
        
        // 记录请求开始时间
        LocalDateTime startTime = LocalDateTime.now();
        requestStartTimeThreadLocal.set(startTime);
        
        // 添加请求ID到请求头，用于跟踪
        String requestId = System.currentTimeMillis() + "-" + Thread.currentThread().getId();
        template.header("X-Request-ID", requestId);
    }
    
    /**
     * 提取请求头信息
     * 
     * @param template 请求模板
     * @return 请求头Map
     */
    private Map<String, String> extractHeaders(RequestTemplate template) {
        if (!property.isIncludeHeaders() || template.headers().isEmpty()) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        template.headers().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(name, String.join(", ", values));
            }
        });
        return headerMap;
    }
    
    /**
     * 提取请求体
     * 
     * @param template 请求模板
     * @return 请求体字符串
     */
    private String extractRequestBody(RequestTemplate template) {
        if (!property.isIncludeRequestBody() || template.body() == null || template.body().length == 0) {
            return null;
        }
        
        try {
            String content = new String(template.body(), StandardCharsets.UTF_8);
            
            // 限制请求体大小
            if (StringUtils.hasText(content) && content.length() > property.getMaxBodySize()) {
                return content.substring(0, (int) property.getMaxBodySize()) + "... (truncated)";
            }
            
            return content;
        } catch (Exception e) {
            log.warn("Failed to extract request body: {}", e.getMessage());
            return "[Failed to extract request body]";
        }
    }
    
    /**
     * 提取查询参数
     * 
     * @param template 请求模板
     * @return 查询参数Map
     */
    private Map<String, Object> extractQueryParams(RequestTemplate template) {
        if (template.queries().isEmpty()) {
            return null;
        }
        
        Map<String, Object> params = new HashMap<>();
        template.queries().forEach((name, values) -> {
            if (!values.isEmpty()) {
                if (values.size() == 1) {
                    params.put(name, values.iterator().next());
                } else {
                    params.put(name, values);
                }
            }
        });
        return params;
    }
    
    /**
     * 处理响应信息
     * 
     * @param method 请求方法
     * @param url 请求URL
     * @param requestHeaders 请求头
     * @param requestParams 请求参数
     * @param requestBody 请求体
     * @param statusCode 响应状态码
     * @param responseHeaders 响应头
     * @param responseBody 响应体
     * @param error 异常信息
     */
    public void handleResponse(String method, String url, Map<String, String> requestHeaders, 
                              Map<String, Object> requestParams, String requestBody,
                              Integer statusCode, Map<String, String> responseHeaders, 
                              String responseBody, Throwable error) {
        LocalDateTime startTime = requestStartTimeThreadLocal.get();
        if (startTime != null) {
            try {
                LocalDateTime endTime = LocalDateTime.now();
                boolean success = error == null && (statusCode == null || statusCode < 400);
                String errorMessage = error != null ? error.getMessage() : null;
                
                // 记录请求日志
                requestLogHandler.handleRequestLog(CLIENT_TYPE, method, url, 
                    requestHeaders, requestBody, statusCode != null ? statusCode : 0, 
                    responseHeaders, responseBody, startTime, endTime, 
                    success, errorMessage);
            } finally {
                // 清理线程本地变量
                requestStartTimeThreadLocal.remove();
            }
        }
    }
}