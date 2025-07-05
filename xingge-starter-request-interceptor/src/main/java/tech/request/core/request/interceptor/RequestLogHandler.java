/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志处理器
 * 
 * 功能说明：
 * - 处理拦截到的请求和响应数据
 * - 构建请求日志信息
 * - 调用存储接口保存日志
 */
package tech.request.core.request.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.storage.RequestLogStorage;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 请求日志处理器
 * 
 * <p>该类负责处理拦截到的请求和响应数据，包括：</p>
 * <ul>
 *   <li>构建请求日志信息</li>
 *   <li>根据配置过滤请求和响应数据</li>
 *   <li>调用存储接口保存日志</li>
 * </ul>
 * 
 * <p>支持同步和异步处理模式。</p>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
public class RequestLogHandler {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLogHandler.class);
    
    /**
     * 请求日志存储接口
     */
    private final RequestLogStorage storage;
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * 构造函数
     * 
     * @param storage 请求日志存储接口
     * @param property 请求拦截器配置属性
     */
    public RequestLogHandler(RequestLogStorage storage, RequestInterceptorProperty property) {
        this.storage = storage;
        this.property = property;
    }
    
    /**
     * 处理请求开始事件
     * 
     * @param logInfo 请求日志信息
     * @param method 请求方法
     * @param url 请求URL
     * @param headers 请求头
     * @param params 请求参数
     * @param body 请求体
     * @param clientType HTTP客户端类型
     */
    public void handleRequestStart(RequestLogInfo logInfo, String method, String url,
                                  Map<String, String> headers, Map<String, Object> params,
                                  String body, String clientType) {
        // 设置请求基本信息
        logInfo.setMethod(method);
        logInfo.setUrl(url);
        logInfo.setClientType(clientType);
        
        // 根据配置设置请求头
        if (property.isIncludeHeaders() && headers != null) {
            logInfo.setRequestHeaders(headers);
            
            // 提取常用头信息
            String userAgent = headers.get("User-Agent");
            if (StringUtils.hasText(userAgent)) {
                logInfo.setUserAgent(userAgent);
            }
            
            String clientIp = headers.get("X-Forwarded-For");
            if (!StringUtils.hasText(clientIp)) {
                clientIp = headers.get("X-Real-IP");
            }
            if (StringUtils.hasText(clientIp)) {
                logInfo.setClientIp(clientIp);
            }
        }
        
        // 设置请求参数
        if (params != null && !params.isEmpty()) {
            logInfo.setRequestParams(params);
        }
        
        // 根据配置设置请求体
        if (property.isIncludeRequestBody() && StringUtils.hasText(body)) {
            // 限制请求体大小
            if (body.length() > property.getMaxBodySize()) {
                logInfo.setRequestBody(body.substring(0, (int) property.getMaxBodySize()) + "... (truncated)");
            } else {
                logInfo.setRequestBody(body);
            }
        }
    }
    
    /**
     * 处理请求完成事件
     * 
     * @param logInfo 请求日志信息
     * @param status 响应状态码
     * @param headers 响应头
     * @param body 响应体
     * @param error 异常信息
     * @return 完成的请求日志信息
     */
    public RequestLogInfo handleRequestComplete(RequestLogInfo logInfo, Integer status,
                                              Map<String, String> headers, String body,
                                              Throwable error) {
        // 设置响应时间和计算耗时
        logInfo.setResponseTimeAndCalculateDuration(LocalDateTime.now());
        
        // 设置响应状态
        logInfo.setResponseStatus(status);
        logInfo.setSuccess(error == null && (status == null || (status >= 200 && status < 300)));
        
        // 根据配置设置响应头
        if (property.isIncludeHeaders() && headers != null) {
            logInfo.setResponseHeaders(headers);
        }
        
        // 根据配置设置响应体
        if (property.isIncludeResponseBody() && StringUtils.hasText(body)) {
            // 限制响应体大小
            if (body.length() > property.getMaxBodySize()) {
                logInfo.setResponseBody(body.substring(0, (int) property.getMaxBodySize()) + "... (truncated)");
            } else {
                logInfo.setResponseBody(body);
            }
        }
        
        // 设置错误信息
        if (error != null) {
            logInfo.setErrorMessage(error.getMessage());
        }
        
        // 异步存储日志
        storeLogAsync(logInfo);
        
        return logInfo;
    }
    
    /**
     * 异步存储日志
     * 
     * @param logInfo 请求日志信息
     * @return CompletableFuture对象
     */
    public CompletableFuture<Void> storeLogAsync(RequestLogInfo logInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                storage.store(logInfo);
            } catch (Exception e) {
                log.error("Failed to store request log: {}", e.getMessage(), e);
            }
        });
    }
    
    /**
     * 同步存储日志
     * 
     * @param logInfo 请求日志信息
     */
    public void storeLog(RequestLogInfo logInfo) {
        try {
            storage.store(logInfo);
        } catch (Exception e) {
            log.error("Failed to store request log: {}", e.getMessage(), e);
        }
    }
}