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
 * @version 0.0.2
 * @since 2025-07-11
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
        
        // 提取请求信息
        String method = request.httpMethod().name();
        String url = request.url();
        Map<String, String> requestHeaders = extractRequestHeaders(request);
        Map<String, Object> requestParams = extractQueryParams(url);
        String requestBody = extractRequestBody(request);
        
        Response response;
        try {
            // 执行请求
            response = delegate.execute(request, options);
        } catch (IOException e) {
            // 处理请求异常
            requestInterceptor.handleResponse(method, url, requestHeaders, requestParams, requestBody, null, null, null, e);
            throw e;
        }
        
        // 提取响应信息
        int statusCode = response.status();
        Map<String, String> responseHeaders = extractHeaders(response);
        
        // 克隆响应体，避免消费原始响应体
        Response.Body originalBody = response.body();
        byte[] bodyData = originalBody != null ? readBodyData(originalBody) : new byte[0];
        String responseBody = extractResponseBody(bodyData);
        
        // 处理响应信息
        requestInterceptor.handleResponse(method, url, requestHeaders, requestParams, requestBody, statusCode, responseHeaders, responseBody, null);
        
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
            java.io.InputStream inputStream = body.asInputStream();
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
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
    
    /**
     * 提取请求头信息
     * 
     * @param request 请求对象
     * @return 请求头Map
     */
    private Map<String, String> extractRequestHeaders(Request request) {
        if (!property.isIncludeHeaders() || request.headers().isEmpty()) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        request.headers().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(name, String.join(", ", values));
            }
        });
        return headerMap;
    }
    
    /**
     * 提取查询参数
     * 
     * @param url 请求URL
     * @return 查询参数Map
     */
    private Map<String, Object> extractQueryParams(String url) {
        if (!StringUtils.hasText(url) || !url.contains("?")) {
            return null;
        }
        
        try {
            String queryString = url.substring(url.indexOf("?") + 1);
            Map<String, Object> params = new HashMap<>();
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            return params.isEmpty() ? null : params;
        } catch (Exception e) {
            log.warn("Failed to extract query params: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 提取请求体
     * 
     * @param request 请求对象
     * @return 请求体字符串
     */
    private String extractRequestBody(Request request) {
        if (!property.isIncludeRequestBody() || request.body() == null || request.body().length == 0) {
            return null;
        }
        
        try {
            String content = new String(request.body(), StandardCharsets.UTF_8);
            
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
}