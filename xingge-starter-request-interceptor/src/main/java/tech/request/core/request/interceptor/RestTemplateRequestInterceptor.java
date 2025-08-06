/*
 * Copyright (c) 2024 行歌(xingge)
 * RestTemplate请求拦截器
 * 
 * 功能说明：
 * - 拦截RestTemplate发起的HTTP请求
 * - 收集请求和响应数据
 * - 通过请求日志处理器记录日志
 */
package tech.request.core.request.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.handler.RequestLogHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * RestTemplate请求拦截器
 * 
 * <p>该类实现了Spring的ClientHttpRequestInterceptor接口，用于拦截RestTemplate发起的HTTP请求。</p>
 * <p>拦截器功能：</p>
 * <ul>
 *   <li>收集请求URL、方法、头信息和请求体</li>
 *   <li>收集响应状态码、头信息和响应体</li>
 *   <li>通过请求日志处理器记录日志</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.4
 * @since 2025-07-11
 */
public class RestTemplateRequestInterceptor implements ClientHttpRequestInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(RestTemplateRequestInterceptor.class);
    private static final String CLIENT_TYPE = "RestTemplate";
    
    /**
     * 请求日志处理器
     */
    private final RequestLogHandler requestLogHandler;
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * 构造函数
     * 
     * @param requestLogHandler 请求日志处理器
     * @param property 拦截器配置属性
     */
    public RestTemplateRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        this.requestLogHandler = requestLogHandler;
        this.property = property;
    }
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 如果拦截器被禁用，直接执行请求
        if (!property.isEnabled()) {
            return execution.execute(request, body);
        }
        
        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        
        // 提取请求信息
        String method = request.getMethod().name();
        String url = request.getURI().toString();
        Map<String, String> headers = extractHeaders(request);
        String requestBody = extractRequestBody(body);
        
        ClientHttpResponse response;
        LocalDateTime endTime = null;
        boolean success = true;
        String errorMessage = null;
        int statusCode = 0;
        Map<String, String> responseHeaders = null;
        String responseBody = null;
        
        try {
            // 执行请求
            response = execution.execute(request, body);
            endTime = LocalDateTime.now();
            
            // 提取响应信息
            statusCode = response.getStatusCode().value();
            responseHeaders = extractResponseHeaders(response);
            responseBody = extractResponseBody(response);
            
            success = statusCode >= 200 && statusCode < 300;
        } catch (IOException e) {
            endTime = LocalDateTime.now();
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // 记录请求日志
            requestLogHandler.handleRequestLog(CLIENT_TYPE, method, url, headers, requestBody,
                    statusCode, responseHeaders, responseBody, startTime, endTime, success, errorMessage);
        }
        
        // 返回原始响应
        return response;
    }
    
    /**
     * 提取请求头信息
     * 
     * @param request 请求对象
     * @return 请求头Map
     */
    private Map<String, String> extractHeaders(HttpRequest request) {
        if (!property.isIncludeHeaders() || request.getHeaders().isEmpty()) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(name, String.join(", ", values));
            }
        });
        return headerMap;
    }
    
    /**
     * 提取响应头信息
     * 
     * @param response 响应对象
     * @return 响应头Map
     */
    private Map<String, String> extractResponseHeaders(ClientHttpResponse response) {
        if (!property.isIncludeHeaders() || response.getHeaders().isEmpty()) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        response.getHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(name, String.join(", ", values));
            }
        });
        return headerMap;
    }
    
    /**
     * 提取请求体
     * 
     * @param body 请求体字节数组
     * @return 请求体字符串
     */
    private String extractRequestBody(byte[] body) {
        if (!property.isIncludeRequestBody() || body == null || body.length == 0) {
            return null;
        }
        
        try {
            String content = new String(body, StandardCharsets.UTF_8);
            
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
     * 提取响应体
     * 
     * @param response 响应对象
     * @return 响应体字符串
     */
    private String extractResponseBody(ClientHttpResponse response) {
        if (!property.isIncludeResponseBody()) {
            return null;
        }
        
        try {
            byte[] bodyBytes = StreamUtils.copyToByteArray(response.getBody());
            if (bodyBytes.length == 0) {
                return null;
            }
            
            String content = new String(bodyBytes, StandardCharsets.UTF_8);
            
            // 限制响应体大小
            if (StringUtils.hasText(content) && content.length() > property.getMaxBodySize()) {
                return content.substring(0, (int) property.getMaxBodySize()) + "... (truncated)";
            }
            
            return content;
        } catch (Exception e) {
            log.warn("Failed to extract response body: {}", e.getMessage());
            return "[Failed to extract response body]";
        }
    }
}