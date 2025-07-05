/*
 * Copyright (c) 2024 行歌(xingge)
 * Feign响应拦截器
 * 
 * 功能说明：
 * - 拦截OpenFeign的HTTP响应
 * - 收集响应数据
 * - 配合OpenFeignRequestInterceptor使用
 */
package tech.request.core.request.interceptor;

import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Feign响应拦截器
 * 
 * <p>该类包装了Feign的Client接口，用于拦截OpenFeign的HTTP响应。</p>
 * <p>拦截器功能：</p>
 * <ul>
 *   <li>收集响应状态码、头信息和响应体</li>
 *   <li>将响应信息传递给OpenFeignRequestInterceptor处理</li>
 * </ul>
 * <p>注意：该拦截器需要与OpenFeignRequestInterceptor配合使用。</p>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
public class FeignResponseInterceptor implements Client {
    
    private static final Logger log = LoggerFactory.getLogger(FeignResponseInterceptor.class);
    
    /**
     * 原始Feign客户端
     */
    private final Client delegate;
    
    /**
     * OpenFeign请求拦截器
     */
    private final OpenFeignRequestInterceptor requestInterceptor;
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * 构造函数
     * 
     * @param delegate 原始Feign客户端
     * @param requestInterceptor OpenFeign请求拦截器
     * @param property 拦截器配置属性
     */
    public FeignResponseInterceptor(Client delegate, OpenFeignRequestInterceptor requestInterceptor, RequestInterceptorProperty property) {
        this.delegate = delegate;
        this.requestInterceptor = requestInterceptor;
        this.property = property;
    }
    
    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        // 如果拦截器被禁用，直接执行请求
        if (!property.isEnabled()) {
            return delegate.execute(request, options);
        }
        
        Response response;
        try {
            // 执行请求
            response = delegate.execute(request, options);
        } catch (IOException e) {
            // 处理请求异常
            requestInterceptor.handleResponse(null, null, null, e);
            throw e;
        }
        
        // 提取响应信息
        int statusCode = response.status();
        Map<String, String> headers = extractHeaders(response);
        
        // 克隆响应体，避免消费原始响应体
        Response.Body originalBody = response.body();
        byte[] bodyData = originalBody != null ? readBodyData(originalBody) : new byte[0];
        String responseBody = extractResponseBody(bodyData);
        
        // 处理响应信息
        requestInterceptor.handleResponse(statusCode, headers, responseBody, null);
        
        // 创建新的响应对象，包含原始响应体
        return Response.builder()
                .status(response.status())
                .reason(response.reason())
                .headers(response.headers())
                .request(response.request())
                .body(bodyData)
                .build();
    }
    
    /**
     * 读取响应体数据
     * 
     * @param body 响应体
     * @return 响应体字节数组
     */
    private byte[] readBodyData(Response.Body body) {
        try {
            return body.asInputStream().readAllBytes();
        } catch (IOException e) {
            log.warn("Failed to read response body: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * 提取响应头信息
     * 
     * @param response 响应对象
     * @return 响应头Map
     */
    private Map<String, String> extractHeaders(Response response) {
        if (!property.isIncludeHeaders() || response.headers().isEmpty()) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        response.headers().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(name, String.join(", ", values));
            }
        });
        return headerMap;
    }
    
    /**
     * 提取响应体
     * 
     * @param bodyData 响应体字节数组
     * @return 响应体字符串
     */
    private String extractResponseBody(byte[] bodyData) {
        if (!property.isIncludeResponseBody() || bodyData == null || bodyData.length == 0) {
            return null;
        }
        
        try {
            String content = new String(bodyData, StandardCharsets.UTF_8);
            
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