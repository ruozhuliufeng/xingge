/*
 * Copyright (c) 2024 行歌(xingge)
 * OkHttp请求拦截器
 * 
 * 功能说明：
 * - 拦截OkHttp发起的HTTP请求
 * - 收集请求和响应数据
 * - 通过请求日志处理器记录日志
 */
package tech.request.core.request.interceptor;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.model.RequestLogInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OkHttp请求拦截器
 * 
 * <p>该类实现了OkHttp的Interceptor接口，用于拦截OkHttp发起的HTTP请求。</p>
 * <p>拦截器功能：</p>
 * <ul>
 *   <li>收集请求URL、方法、头信息和请求体</li>
 *   <li>收集响应状态码、头信息和响应体</li>
 *   <li>通过请求日志处理器记录日志</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
public class OkHttpRequestInterceptor implements Interceptor {
    
    private static final Logger log = LoggerFactory.getLogger(OkHttpRequestInterceptor.class);
    private static final String CLIENT_TYPE = "OkHttp";
    
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
    public OkHttpRequestInterceptor(RequestLogHandler requestLogHandler, RequestInterceptorProperty property) {
        this.requestLogHandler = requestLogHandler;
        this.property = property;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 如果拦截器被禁用，直接执行请求
        if (!property.isEnabled()) {
            return chain.proceed(chain.request());
        }
        
        Request request = chain.request();
        RequestLogInfo logInfo = new RequestLogInfo();
        
        // 提取请求信息
        String method = request.method();
        String url = request.url().toString();
        Map<String, String> headers = extractHeaders(request.headers());
        String requestBody = extractRequestBody(request);
        
        // 处理请求开始事件
        requestLogHandler.handleRequestStart(logInfo, method, url, headers, null, requestBody, CLIENT_TYPE);
        
        Response response;
        try {
            // 执行请求
            response = chain.proceed(request);
        } catch (IOException e) {
            // 处理请求异常
            requestLogHandler.handleRequestComplete(logInfo, null, null, null, e);
            throw e;
        }
        
        // 提取响应信息
        int statusCode = response.code();
        Map<String, String> responseHeaders = extractHeaders(response.headers());
        String responseBody = extractResponseBody(response);
        
        // 处理请求完成事件
        requestLogHandler.handleRequestComplete(logInfo, statusCode, responseHeaders, responseBody, null);
        
        // 返回原始响应
        return response;
    }
    
    /**
     * 提取请求头信息
     * 
     * @param headers 请求头
     * @return 请求头Map
     */
    private Map<String, String> extractHeaders(Headers headers) {
        if (!property.isIncludeHeaders() || headers == null) {
            return null;
        }
        
        Map<String, String> headerMap = new HashMap<>();
        Set<String> names = headers.names();
        for (String name : names) {
            headerMap.put(name, headers.get(name));
        }
        return headerMap;
    }
    
    /**
     * 提取请求体
     * 
     * @param request 请求对象
     * @return 请求体字符串
     */
    private String extractRequestBody(Request request) {
        if (!property.isIncludeRequestBody() || request.body() == null) {
            return null;
        }
        
        try {
            RequestBody requestBody = request.body();
            if (requestBody == null) {
                return null;
            }
            
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return buffer.readString(StandardCharsets.UTF_8);
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
    private String extractResponseBody(Response response) {
        if (!property.isIncludeResponseBody() || response.body() == null) {
            return null;
        }
        
        try {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return null;
            }
            
            // 克隆响应体，避免消费原始响应体
            ResponseBody clonedBody = responseBody.source().getBuffer().clone().readByteString();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            String body = source.getBuffer().clone().readString(StandardCharsets.UTF_8);
            
            // 限制响应体大小
            if (StringUtils.hasText(body) && body.length() > property.getMaxBodySize()) {
                return body.substring(0, (int) property.getMaxBodySize()) + "... (truncated)";
            }
            
            return body;
        } catch (Exception e) {
            log.warn("Failed to extract response body: {}", e.getMessage());
            return "[Failed to extract response body]";
        }
    }
}