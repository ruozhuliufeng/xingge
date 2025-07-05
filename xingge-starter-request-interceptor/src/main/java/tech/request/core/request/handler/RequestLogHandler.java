/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志处理器
 * 
 * 功能说明：
 * - 处理拦截到的请求和响应数据
 * - 构建请求日志信息
 * - 根据配置过滤数据
 * - 调用存储接口保存日志
 */
package tech.request.core.request.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.storage.RequestLogStorage;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 请求日志处理器
 * 
 * <p>该类负责处理拦截到的请求和响应数据，包括：</p>
 * <ul>
 *   <li>构建请求日志信息对象</li>
 *   <li>根据配置过滤敏感数据</li>
 *   <li>调用存储接口保存日志</li>
 *   <li>支持同步和异步处理模式</li>
 * </ul>
 * 
 * <p>处理流程：</p>
 * <ol>
 *   <li>接收请求和响应数据</li>
 *   <li>构建RequestLogInfo对象</li>
 *   <li>根据配置过滤数据</li>
 *   <li>调用存储接口保存</li>
 * </ol>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
public class RequestLogHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLogHandler.class);
    
    /**
     * 请求日志存储接口
     */
    @Autowired
    private RequestLogStorage requestLogStorage;
    
    /**
     * 请求拦截器配置属性
     */
    @Autowired
    private RequestInterceptorProperty properties;
    
    /**
     * 初始化处理器
     */
    @PostConstruct
    public void initialize() {
        logger.info("请求日志处理器初始化完成，存储类型: {}", requestLogStorage.getStorageType());
    }
    
    /**
     * 处理请求日志（同步方式）
     * 
     * @param clientType 客户端类型（如：OkHttp、RestTemplate、OpenFeign）
     * @param method 请求方法
     * @param url 请求URL
     * @param requestHeaders 请求头
     * @param requestBody 请求体
     * @param responseStatus 响应状态码
     * @param responseHeaders 响应头
     * @param responseBody 响应体
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param success 是否成功
     * @param errorMessage 错误信息
     */
    public void handleRequestLog(String clientType, String method, String url,
                               Map<String, String> requestHeaders, String requestBody,
                               int responseStatus, Map<String, String> responseHeaders, String responseBody,
                               LocalDateTime startTime, LocalDateTime endTime,
                               boolean success, String errorMessage) {
        try {
            RequestLogInfo logInfo = buildRequestLogInfo(clientType, method, url,
                    requestHeaders, requestBody, responseStatus, responseHeaders, responseBody,
                    startTime, endTime, success, errorMessage);
            
            requestLogStorage.store(logInfo);
        } catch (Exception e) {
            logger.error("处理请求日志失败: {} {}", method, url, e);
        }
    }
    
    /**
     * 处理请求日志（异步方式）
     * 
     * @param clientType 客户端类型
     * @param method 请求方法
     * @param url 请求URL
     * @param requestHeaders 请求头
     * @param requestBody 请求体
     * @param responseStatus 响应状态码
     * @param responseHeaders 响应头
     * @param responseBody 响应体
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param success 是否成功
     * @param errorMessage 错误信息
     * @return CompletableFuture对象
     */
    public CompletableFuture<Void> handleRequestLogAsync(String clientType, String method, String url,
                                                        Map<String, String> requestHeaders, String requestBody,
                                                        int responseStatus, Map<String, String> responseHeaders, String responseBody,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        boolean success, String errorMessage) {
        try {
            RequestLogInfo logInfo = buildRequestLogInfo(clientType, method, url,
                    requestHeaders, requestBody, responseStatus, responseHeaders, responseBody,
                    startTime, endTime, success, errorMessage);
            
            return requestLogStorage.storeAsync(logInfo);
        } catch (Exception e) {
            logger.error("异步处理请求日志失败: {} {}", method, url, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * 构建请求日志信息对象
     * 
     * @param clientType 客户端类型
     * @param method 请求方法
     * @param url 请求URL
     * @param requestHeaders 请求头
     * @param requestBody 请求体
     * @param responseStatus 响应状态码
     * @param responseHeaders 响应头
     * @param responseBody 响应体
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param success 是否成功
     * @param errorMessage 错误信息
     * @return 请求日志信息对象
     */
    private RequestLogInfo buildRequestLogInfo(String clientType, String method, String url,
                                             Map<String, String> requestHeaders, String requestBody,
                                             int responseStatus, Map<String, String> responseHeaders, String responseBody,
                                             LocalDateTime startTime, LocalDateTime endTime,
                                             boolean success, String errorMessage) {
        RequestLogInfo logInfo = new RequestLogInfo();
        
        // 基本信息
        logInfo.setRequestId(UUID.randomUUID().toString());
        logInfo.setClientType(clientType);
        logInfo.setMethod(method);
        logInfo.setUrl(url);
        logInfo.setStartTime(startTime);
        logInfo.setEndTime(endTime);
        logInfo.setSuccess(success);
        logInfo.setErrorMessage(errorMessage);
        
        // 计算耗时
        if (startTime != null && endTime != null) {
            long duration = java.time.Duration.between(startTime, endTime).toMillis();
            logInfo.setDuration(duration);
        }
        
        // 请求信息（根据配置决定是否包含）
        if (properties.isIncludeHeaders()) {
            logInfo.setRequestHeaders(requestHeaders);
        }
        
        if (properties.isIncludeRequestBody() && requestBody != null) {
            // 根据最大请求体大小限制
            if (requestBody.length() > properties.getMaxBodySize()) {
                logInfo.setRequestBody(requestBody.substring(0, (int) properties.getMaxBodySize()) + "... (截断)");
            } else {
                logInfo.setRequestBody(requestBody);
            }
        }
        
        // 响应信息
        logInfo.setResponseStatus(responseStatus);
        
        if (properties.isIncludeHeaders()) {
            logInfo.setResponseHeaders(responseHeaders);
        }
        
        if (properties.isIncludeResponseBody() && responseBody != null) {
            // 根据最大响应体大小限制
            if (responseBody.length() > properties.getMaxBodySize()) {
                logInfo.setResponseBody(responseBody.substring(0, (int) properties.getMaxBodySize()) + "... (截断)");
            } else {
                logInfo.setResponseBody(responseBody);
            }
        }
        
        return logInfo;
    }
    
    /**
     * 获取请求日志存储接口
     * 
     * @return 请求日志存储接口
     */
    public RequestLogStorage getRequestLogStorage() {
        return requestLogStorage;
    }
    
    /**
     * 获取配置属性
     * 
     * @return 配置属性
     */
    public RequestInterceptorProperty getProperties() {
        return properties;
    }
}