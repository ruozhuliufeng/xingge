/*
 * Copyright (c) 2024 行歌(xingge)
 * 审计日志信息模型
 * 
 * 功能说明：
 * - 封装审计日志的所有信息
 * - 支持序列化和反序列化
 * - 提供便捷的构建方法
 */
package tech.msop.core.log.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志信息模型
 * 
 * <p>该类封装了审计日志的所有信息，包括：</p>
 * <ul>
 *   <li>基本信息：操作类型、模块、描述等</li>
 *   <li>执行信息：方法名、参数、返回值、异常等</li>
 *   <li>上下文信息：用户、IP、时间等</li>
 *   <li>扩展信息：标签、优先级、自定义属性等</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志ID（唯一标识）
     */
    private String logId;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 业务模块
     */
    private String module;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 方法名（类名.方法名）
     */
    private String methodName;
    
    /**
     * 方法参数（JSON格式）
     */
    private String methodArgs;
    
    /**
     * 方法返回值（JSON格式）
     */
    private String methodResult;
    
    /**
     * 异常信息
     */
    private String exceptionInfo;
    
    /**
     * 执行状态：SUCCESS、FAILURE、ERROR
     */
    private String status;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;
    
    /**
     * 操作用户ID
     */
    private String userId;
    
    /**
     * 操作用户名
     */
    private String username;
    
    /**
     * 用户角色
     */
    private String userRole;
    
    /**
     * 客户端IP地址
     */
    private String clientIp;
    
    /**
     * 用户代理（浏览器信息）
     */
    private String userAgent;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * HTTP方法
     */
    private String httpMethod;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 链路追踪ID
     */
    private String traceId;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 服务器名称
     */
    private String serverName;
    
    /**
     * 应用名称
     */
    private String applicationName;
    
    /**
     * 环境标识（dev、test、prod）
     */
    private String environment;
    
    /**
     * 优先级（1-4）
     */
    private Integer priority;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 获取执行状态枚举
     */
    public enum Status {
        /**
         * 成功
         */
        SUCCESS,
        /**
         * 业务失败
         */
        FAILURE,
        /**
         * 系统错误
         */
        ERROR
    }
    
    /**
     * 获取优先级枚举
     */
    public enum Priority {
        /**
         * 低优先级
         */
        LOW(1),
        /**
         * 中优先级
         */
        MEDIUM(2),
        /**
         * 高优先级
         */
        HIGH(3),
        /**
         * 紧急
         */
        URGENT(4);
        
        private final int value;
        
        Priority(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    // 手动添加getter方法以解决Lombok问题
    public String getLogId() {
        return logId;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getModule() {
        return module;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public String getMethodArgs() {
        return methodArgs;
    }
    
    public String getMethodResult() {
        return methodResult;
    }
    
    public String getExceptionInfo() {
        return exceptionInfo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Long getExecutionTime() {
        return executionTime;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getTraceId() {
        return traceId;
    }
    
    public LocalDateTime getOperationTime() {
        return operationTime;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public Map<String, Object> getExtendedProperties() {
        return extendedProperties;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    /**
     * 创建Builder实例
     * @return Builder实例
     */
    public static AuditLogInfoBuilder builder() {
        return new AuditLogInfoBuilder();
    }
    
    /**
     * Builder类
     */
    public static class AuditLogInfoBuilder {
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
        private LocalDateTime operationTime;
        private String serverName;
        private String applicationName;
        private String environment;
        private Integer priority;
        private List<String> tags;
        private Map<String, Object> extendedProperties;
        private LocalDateTime createTime;
        
        public AuditLogInfoBuilder logId(String logId) {
            this.logId = logId;
            return this;
        }
        
        public AuditLogInfoBuilder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public AuditLogInfoBuilder module(String module) {
            this.module = module;
            return this;
        }
        
        public AuditLogInfoBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public AuditLogInfoBuilder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }
        
        public AuditLogInfoBuilder methodArgs(String methodArgs) {
            this.methodArgs = methodArgs;
            return this;
        }
        
        public AuditLogInfoBuilder methodResult(String methodResult) {
            this.methodResult = methodResult;
            return this;
        }
        
        public AuditLogInfoBuilder exceptionInfo(String exceptionInfo) {
            this.exceptionInfo = exceptionInfo;
            return this;
        }
        
        public AuditLogInfoBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public AuditLogInfoBuilder executionTime(Long executionTime) {
            this.executionTime = executionTime;
            return this;
        }
        
        public AuditLogInfoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public AuditLogInfoBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public AuditLogInfoBuilder userRole(String userRole) {
            this.userRole = userRole;
            return this;
        }
        
        public AuditLogInfoBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }
        
        public AuditLogInfoBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public AuditLogInfoBuilder requestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }
        
        public AuditLogInfoBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public AuditLogInfoBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public AuditLogInfoBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }
        
        public AuditLogInfoBuilder operationTime(LocalDateTime operationTime) {
            this.operationTime = operationTime;
            return this;
        }
        
        public AuditLogInfoBuilder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }
        
        public AuditLogInfoBuilder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }
        
        public AuditLogInfoBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }
        
        public AuditLogInfoBuilder priority(Integer priority) {
            this.priority = priority;
            return this;
        }
        
        public AuditLogInfoBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }
        
        public AuditLogInfoBuilder extendedProperties(Map<String, Object> extendedProperties) {
            this.extendedProperties = extendedProperties;
            return this;
        }
        
        public AuditLogInfoBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }
        
        public AuditLogInfo build() {
            AuditLogInfo auditLogInfo = new AuditLogInfo();
            auditLogInfo.logId = this.logId;
            auditLogInfo.operation = this.operation;
            auditLogInfo.module = this.module;
            auditLogInfo.description = this.description;
            auditLogInfo.methodName = this.methodName;
            auditLogInfo.methodArgs = this.methodArgs;
            auditLogInfo.methodResult = this.methodResult;
            auditLogInfo.exceptionInfo = this.exceptionInfo;
            auditLogInfo.status = this.status;
            auditLogInfo.executionTime = this.executionTime;
            auditLogInfo.userId = this.userId;
            auditLogInfo.username = this.username;
            auditLogInfo.userRole = this.userRole;
            auditLogInfo.clientIp = this.clientIp;
            auditLogInfo.userAgent = this.userAgent;
            auditLogInfo.requestUrl = this.requestUrl;
            auditLogInfo.httpMethod = this.httpMethod;
            auditLogInfo.sessionId = this.sessionId;
            auditLogInfo.traceId = this.traceId;
            auditLogInfo.operationTime = this.operationTime;
            auditLogInfo.serverName = this.serverName;
            auditLogInfo.applicationName = this.applicationName;
            auditLogInfo.environment = this.environment;
            auditLogInfo.priority = this.priority;
            auditLogInfo.tags = this.tags;
            auditLogInfo.extendedProperties = this.extendedProperties;
            auditLogInfo.createTime = this.createTime;
            return auditLogInfo;
        }
    }
}