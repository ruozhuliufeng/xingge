/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志信息模型
 * 
 * 功能说明：
 * - 封装HTTP请求和响应的详细信息
 * - 支持序列化和反序列化
 * - 提供统一的数据结构用于存储
 */
package tech.request.core.request.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 请求日志信息模型类
 * 
 * <p>该类封装了HTTP请求和响应的所有相关信息，包括：</p>
 * <ul>
 *   <li>请求基本信息（URL、方法、参数等）</li>
 *   <li>请求头和响应头</li>
 *   <li>请求体和响应体</li>
 *   <li>执行时间和状态信息</li>
 * </ul>
 * 
 * <p>支持多种序列化格式，可用于：</p>
 * <ul>
 *   <li>日志记录</li>
 *   <li>数据库存储</li>
 *   <li>消息队列传输</li>
 *   <li>接口调用</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestLogInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 请求唯一标识
     */
    private String requestId;
    
    /**
     * 请求时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime requestTime;
    
    /**
     * 响应时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime responseTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    
    /**
     * HTTP客户端类型
     */
    private String clientType;
    
    /**
     * 请求方法
     */
    private String method;
    
    /**
     * 请求URL
     */
    private String url;
    
    /**
     * 请求头
     */
    private Map<String, String> requestHeaders;
    
    /**
     * 请求参数
     */
    private Map<String, Object> requestParams;
    
    /**
     * 请求体
     */
    private String requestBody;
    
    /**
     * 响应状态码
     */
    private Integer responseStatus;
    
    /**
     * 响应头
     */
    private Map<String, String> responseHeaders;
    
    /**
     * 响应体
     */
    private String responseBody;
    
    /**
     * 异常信息
     */
    private String errorMessage;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 客户端IP地址
     */
    private String clientIp;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * 环境信息
     */
    private String environment;
    
    /**
     * 默认构造函数
     */
    public RequestLogInfo() {
        this.requestTime = LocalDateTime.now();
        this.requestId = generateRequestId();
    }
    
    /**
     * 生成请求唯一标识
     * 
     * @return 请求ID
     */
    private String generateRequestId() {
        return System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * 计算执行耗时
     */
    public void calculateDuration() {
        if (requestTime != null && responseTime != null) {
            this.duration = java.time.Duration.between(requestTime, responseTime).toMillis();
        }
    }
    
    /**
     * 设置响应时间并计算耗时
     * 
     * @param responseTime 响应时间
     */
    public void setResponseTimeAndCalculateDuration(LocalDateTime responseTime) {
        this.responseTime = responseTime;
        calculateDuration();
    }
    
    // Getter和Setter方法
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
    
    public LocalDateTime getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(LocalDateTime responseTime) {
        this.responseTime = responseTime;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public String getClientType() {
        return clientType;
    }
    
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }
    
    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public Map<String, Object> getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public Integer getResponseStatus() {
        return responseStatus;
    }
    
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }
    
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
    
    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
    
    public String getResponseBody() {
        return responseBody;
    }
    
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    @Override
    public String toString() {
        return "RequestLogInfo{" +
                "requestId='" + requestId + '\'' +
                ", requestTime=" + requestTime +
                ", responseTime=" + responseTime +
                ", duration=" + duration +
                ", clientType='" + clientType + '\'' +
                ", method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", responseStatus=" + responseStatus +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}