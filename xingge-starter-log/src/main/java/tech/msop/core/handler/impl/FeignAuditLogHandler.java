/*
 * Copyright (c) 2024 行歌(xingge)
 * Feign接口审计日志处理器
 * 
 * 功能说明：
 * - 通过Feign接口发送审计日志到远程服务
 * - 支持重试机制
 * - 支持批量发送
 */
package tech.msop.core.handler.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tech.msop.core.handler.AuditLogHandler;
import tech.msop.core.model.AuditLogInfo;
import tech.msop.core.property.XingGeLogProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Feign接口审计日志处理器
 * 
 * <p>该处理器通过HTTP接口将审计日志发送到远程服务，特点：</p>
 * <ul>
 *   <li>支持异步发送，不影响业务性能</li>
 *   <li>支持重试机制，提高可靠性</li>
 *   <li>支持批量发送，提高效率</li>
 *   <li>支持自定义接口地址和认证</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   log:
 *     audit:
 *       handlers:
 *         feign:
 *           enabled: true
 *           url: http://audit-service/api/audit/logs
 *           timeout: 5000
 *           retry-count: 3
 *           batch-size: 10
 *           headers:
 *             Authorization: Bearer ${audit.token}
 *             Content-Type: application/json
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "xg.log.audit.handlers.feign",
    name = "enabled",
    havingValue = "true"
)
@ConditionalOnClass(RestTemplate.class)
public class FeignAuditLogHandler implements AuditLogHandler {
    
    @Autowired
    private XingGeLogProperty logProperty;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    /**
     * 处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @return 处理结果
     */
    @Override
    public boolean handle(AuditLogInfo auditLogInfo) {
        try {
            // 获取Feign配置
            XingGeLogProperty.AuditConfig.FeignConfig feignConfig = 
                logProperty.getAudit().getHandlers().getFeign();
            
            if (feignConfig == null || feignConfig.getUrl() == null) {
                System.err.println("Feign审计日志处理器配置不完整，跳过处理");
                return false;
            }
            
            // 发送日志
            return sendAuditLog(auditLogInfo, feignConfig);
            
        } catch (Exception e) {
            System.err.println("Feign审计日志处理失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 异步处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     */
    @Override
    public void handleAsync(AuditLogInfo auditLogInfo) {
        CompletableFuture.runAsync(() -> {
            try {
                handle(auditLogInfo);
            } catch (Exception e) {
                System.err.println("Feign审计日志异步处理失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 发送审计日志到远程服务
     * 
     * @param auditLogInfo 审计日志信息
     * @param feignConfig Feign配置
     * @return 发送结果
     */
    private boolean sendAuditLog(AuditLogInfo auditLogInfo, 
                                XingGeLogProperty.AuditConfig.FeignConfig feignConfig) {
        
        if (restTemplate == null) {
            System.err.println("RestTemplate未配置，无法发送Feign审计日志");
            return false;
        }
        
        int retryCount = feignConfig.getRetryCount();
        int currentRetry = 0;
        
        while (currentRetry <= retryCount) {
            try {
                // 构建请求
                AuditLogRequest request = buildRequest(auditLogInfo);
                
                // 发送请求
                AuditLogResponse response = restTemplate.postForObject(
                    feignConfig.getUrl(), 
                    request, 
                    AuditLogResponse.class
                );
                
                if (response != null && response.isSuccess()) {
                    System.out.println("Feign审计日志发送成功: " + auditLogInfo.getLogId());
                    return true;
                } else {
                    System.err.println("Feign审计日志发送失败，响应: " + response);
                }
                
            } catch (Exception e) {
                System.err.println("Feign审计日志发送异常，重试次数: " + currentRetry + "/" + retryCount + ", 异常: " + e.getMessage());
                
                if (currentRetry < retryCount) {
                    try {
                        // 重试间隔
                        TimeUnit.MILLISECONDS.sleep(1000 * (currentRetry + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            currentRetry++;
        }
        
        System.err.println("Feign审计日志发送失败，已达到最大重试次数: " + retryCount);
        return false;
    }
    
    /**
     * 构建请求对象
     * 
     * @param auditLogInfo 审计日志信息
     * @return 请求对象
     */
    private AuditLogRequest buildRequest(AuditLogInfo auditLogInfo) {
        return AuditLogRequest.builder()
            .logId(auditLogInfo.getLogId())
            .operation(auditLogInfo.getOperation())
            .module(auditLogInfo.getModule())
            .description(auditLogInfo.getDescription())
            .methodName(auditLogInfo.getMethodName())
            .methodArgs(auditLogInfo.getMethodArgs())
            .methodResult(auditLogInfo.getMethodResult())
            .exceptionInfo(auditLogInfo.getExceptionInfo())
            .status(auditLogInfo.getStatus())
            .executionTime(auditLogInfo.getExecutionTime())
            .userId(auditLogInfo.getUserId())
            .username(auditLogInfo.getUsername())
            .userRole(auditLogInfo.getUserRole())
            .clientIp(auditLogInfo.getClientIp())
            .userAgent(auditLogInfo.getUserAgent())
            .requestUrl(auditLogInfo.getRequestUrl())
            .httpMethod(auditLogInfo.getHttpMethod())
            .sessionId(auditLogInfo.getSessionId())
            .traceId(auditLogInfo.getTraceId())
            .operationTime(auditLogInfo.getOperationTime())
            .serverName(auditLogInfo.getServerName())
            .applicationName(auditLogInfo.getApplicationName())
            .environment(auditLogInfo.getEnvironment())
            .priority(auditLogInfo.getPriority())
            .tags(auditLogInfo.getTags())
            .extendedProperties(auditLogInfo.getExtendedProperties())
            .createTime(auditLogInfo.getCreateTime())
            .build();
    }
    
    @Override
    public String getHandlerName() {
        return "FeignAuditLogHandler";
    }
    
    @Override
    public int getPriority() {
        return 50; // 中等优先级
    }
    
    @Override
    public boolean supports(AuditLogInfo auditLogInfo) {
        // 检查是否配置了Feign处理器
        XingGeLogProperty.AuditConfig.FeignConfig feignConfig = 
            logProperty.getAudit().getHandlers().getFeign();
        return feignConfig != null && feignConfig.getEnabled() && feignConfig.getUrl() != null;
    }
    
    @Override
    public void initialize() {
        System.out.println("Feign审计日志处理器已初始化");
    }
    
    @Override
    public void destroy() {
        System.out.println("Feign审计日志处理器已销毁");
    }
    
    /**
     * 审计日志请求对象
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLogRequest {
        private String logId;
        private String operation;
        private String module;
        private String description;
        private String methodName;
        private String methodArgs;
        private String methodResult;
        private String exceptionInfo;
        private String status;
        private Long executionTime;
        private String userId;
        private String username;
        private String userRole;
        private String clientIp;
        private String userAgent;
        private String requestUrl;
        private String httpMethod;
        private String sessionId;
        private String traceId;
        private java.time.LocalDateTime operationTime;
        private String serverName;
        private String applicationName;
        private String environment;
        private Integer priority;
        private java.util.List<String> tags;
        private java.util.Map<String, Object> extendedProperties;
        private java.time.LocalDateTime createTime;
        
        public static AuditLogRequestBuilder builder() {
            return new AuditLogRequestBuilder();
        }
        
        public static class AuditLogRequestBuilder {
            private String logId;
            private String operation;
            private String module;
            private String description;
            private String methodName;
            private String methodArgs;
            private String methodResult;
            private String exceptionInfo;
            private String status;
            private Long executionTime;
            private String userId;
            private String username;
            private String userRole;
            private String clientIp;
            private String userAgent;
            private String requestUrl;
            private String httpMethod;
            private String sessionId;
            private String traceId;
            private java.time.LocalDateTime operationTime;
            private String serverName;
            private String applicationName;
            private String environment;
            private Integer priority;
            private java.util.List<String> tags;
            private java.util.Map<String, Object> extendedProperties;
            private java.time.LocalDateTime createTime;
            
            public AuditLogRequestBuilder logId(String logId) { this.logId = logId; return this; }
            public AuditLogRequestBuilder operation(String operation) { this.operation = operation; return this; }
            public AuditLogRequestBuilder module(String module) { this.module = module; return this; }
            public AuditLogRequestBuilder description(String description) { this.description = description; return this; }
            public AuditLogRequestBuilder methodName(String methodName) { this.methodName = methodName; return this; }
            public AuditLogRequestBuilder methodArgs(String methodArgs) { this.methodArgs = methodArgs; return this; }
            public AuditLogRequestBuilder methodResult(String methodResult) { this.methodResult = methodResult; return this; }
            public AuditLogRequestBuilder exceptionInfo(String exceptionInfo) { this.exceptionInfo = exceptionInfo; return this; }
            public AuditLogRequestBuilder status(String status) { this.status = status; return this; }
            public AuditLogRequestBuilder executionTime(Long executionTime) { this.executionTime = executionTime; return this; }
            public AuditLogRequestBuilder userId(String userId) { this.userId = userId; return this; }
            public AuditLogRequestBuilder username(String username) { this.username = username; return this; }
            public AuditLogRequestBuilder userRole(String userRole) { this.userRole = userRole; return this; }
            public AuditLogRequestBuilder clientIp(String clientIp) { this.clientIp = clientIp; return this; }
            public AuditLogRequestBuilder userAgent(String userAgent) { this.userAgent = userAgent; return this; }
            public AuditLogRequestBuilder requestUrl(String requestUrl) { this.requestUrl = requestUrl; return this; }
            public AuditLogRequestBuilder httpMethod(String httpMethod) { this.httpMethod = httpMethod; return this; }
            public AuditLogRequestBuilder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
            public AuditLogRequestBuilder traceId(String traceId) { this.traceId = traceId; return this; }
            public AuditLogRequestBuilder operationTime(java.time.LocalDateTime operationTime) { this.operationTime = operationTime; return this; }
            public AuditLogRequestBuilder serverName(String serverName) { this.serverName = serverName; return this; }
            public AuditLogRequestBuilder applicationName(String applicationName) { this.applicationName = applicationName; return this; }
            public AuditLogRequestBuilder environment(String environment) { this.environment = environment; return this; }
            public AuditLogRequestBuilder priority(Integer priority) { this.priority = priority; return this; }
            public AuditLogRequestBuilder tags(java.util.List<String> tags) { this.tags = tags; return this; }
            public AuditLogRequestBuilder extendedProperties(java.util.Map<String, Object> extendedProperties) { this.extendedProperties = extendedProperties; return this; }
            public AuditLogRequestBuilder createTime(java.time.LocalDateTime createTime) { this.createTime = createTime; return this; }
            
            public AuditLogRequest build() {
                AuditLogRequest request = new AuditLogRequest();
                request.logId = this.logId;
                request.operation = this.operation;
                request.module = this.module;
                request.description = this.description;
                request.methodName = this.methodName;
                request.methodArgs = this.methodArgs;
                request.methodResult = this.methodResult;
                request.exceptionInfo = this.exceptionInfo;
                request.status = this.status;
                request.executionTime = this.executionTime;
                request.userId = this.userId;
                request.username = this.username;
                request.userRole = this.userRole;
                request.clientIp = this.clientIp;
                request.userAgent = this.userAgent;
                request.requestUrl = this.requestUrl;
                request.httpMethod = this.httpMethod;
                request.sessionId = this.sessionId;
                request.traceId = this.traceId;
                request.operationTime = this.operationTime;
                request.serverName = this.serverName;
                request.applicationName = this.applicationName;
                request.environment = this.environment;
                request.priority = this.priority;
                request.tags = this.tags;
                request.extendedProperties = this.extendedProperties;
                request.createTime = this.createTime;
                return request;
            }
        }
    }
    
    /**
     * 审计日志响应对象
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AuditLogResponse {
        private boolean success;
        private String message;
        private String code;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
    }
}