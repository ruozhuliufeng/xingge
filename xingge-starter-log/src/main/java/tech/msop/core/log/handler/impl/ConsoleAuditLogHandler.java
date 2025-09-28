/*
 * Copyright (c) 2024 行歌(xingge)
 * 控制台审计日志处理器
 * 
 * 功能说明：
 * - 将审计日志输出到控制台
 * - 支持格式化输出
 * - 支持日志级别控制
 */
package tech.msop.core.log.handler.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tech.msop.core.log.handler.AuditLogHandler;
import tech.msop.core.log.model.AuditLogInfo;

import java.time.format.DateTimeFormatter;

/**
 * 控制台审计日志处理器
 * 
 * <p>该处理器将审计日志输出到控制台，特点：</p>
 * <ul>
 *   <li>简单易用，适合开发和测试环境</li>
 *   <li>支持格式化输出</li>
 *   <li>性能较好，无外部依赖</li>
 *   <li>支持不同日志级别</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   log:
 *     audit:
 *       handlers:
 *         console:
 *           enabled: true
 *           format: detailed
 *           level: INFO
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Component
@ConditionalOnProperty(
    prefix = "xg.log.audit.handlers.console",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ConsoleAuditLogHandler implements AuditLogHandler {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @return 处理结果
     */
    @Override
    public boolean handle(AuditLogInfo auditLogInfo) {
        try {
            String logMessage = formatLogMessage(auditLogInfo);
            
            // 根据状态选择日志级别
            switch (auditLogInfo.getStatus()) {
                case "ERROR":
                    System.err.println("[AUDIT] " + logMessage);
                    break;
                case "FAILURE":
                    System.err.println("[AUDIT] " + logMessage);
                    break;
                case "SUCCESS":
                default:
                    System.out.println("[AUDIT] " + logMessage);
                    break;
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("控制台审计日志处理失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 格式化日志消息
     * 
     * @param auditLogInfo 审计日志信息
     * @return 格式化后的消息
     */
    private String formatLogMessage(AuditLogInfo auditLogInfo) {
        StringBuilder sb = new StringBuilder();
        
        // 基本信息
        sb.append("操作=").append(auditLogInfo.getOperation())
          .append(", 模块=").append(auditLogInfo.getModule())
          .append(", 描述=").append(auditLogInfo.getDescription())
          .append(", 状态=").append(auditLogInfo.getStatus());
        
        // 用户信息
        if (auditLogInfo.getUserId() != null) {
            sb.append(", 用户ID=").append(auditLogInfo.getUserId());
        }
        if (auditLogInfo.getUsername() != null) {
            sb.append(", 用户名=").append(auditLogInfo.getUsername());
        }
        
        // 执行信息
        if (auditLogInfo.getMethodName() != null) {
            sb.append(", 方法=").append(auditLogInfo.getMethodName());
        }
        if (auditLogInfo.getExecutionTime() != null) {
            sb.append(", 耗时=").append(auditLogInfo.getExecutionTime()).append("ms");
        }
        
        // 网络信息
        if (auditLogInfo.getClientIp() != null) {
            sb.append(", IP=").append(auditLogInfo.getClientIp());
        }
        if (auditLogInfo.getRequestUrl() != null) {
            sb.append(", URL=").append(auditLogInfo.getRequestUrl());
        }
        
        // 时间信息
        if (auditLogInfo.getOperationTime() != null) {
            sb.append(", 时间=").append(auditLogInfo.getOperationTime().format(DATE_FORMATTER));
        }
        
        // 链路追踪
        if (auditLogInfo.getTraceId() != null) {
            sb.append(", TraceId=").append(auditLogInfo.getTraceId());
        }
        
        // 异常信息
        if (auditLogInfo.getExceptionInfo() != null) {
            sb.append(", 异常=").append(auditLogInfo.getExceptionInfo());
        }
        
        // 标签
        if (auditLogInfo.getTags() != null && !auditLogInfo.getTags().isEmpty()) {
            sb.append(", 标签=").append(String.join(",", auditLogInfo.getTags()));
        }
        
        return sb.toString();
    }
    
    @Override
    public String getHandlerName() {
        return "ConsoleAuditLogHandler";
    }
    
    @Override
    public int getPriority() {
        return 10; // 高优先级，确保控制台输出优先执行
    }
    
    @Override
    public void initialize() {
        System.out.println("控制台审计日志处理器已初始化");
    }
    
    @Override
    public void destroy() {
        System.out.println("控制台审计日志处理器已销毁");
    }
}