/*
 * Copyright (c) 2024 行歌(xingge)
 * 数据库审计日志处理器
 * 
 * 功能说明：
 * - 将审计日志保存到数据库
 * - 支持多种数据库类型
 * - 支持批量插入优化
 */
package tech.msop.core.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.msop.core.handler.AuditLogHandler;
import tech.msop.core.model.AuditLogInfo;
import tech.msop.core.property.XingGeLogProperty;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库审计日志处理器
 * 
 * @author 星歌
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "xg.log.audit.handlers.database.enabled", havingValue = "true")
@ConditionalOnClass({JdbcTemplate.class, DataSource.class})
public class DatabaseAuditLogHandler implements AuditLogHandler {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private XingGeLogProperty logProperty;
    
    /**
     * 默认表名
     */
    private static final String DEFAULT_TABLE_NAME = "audit_log";
    
    /**
     * 插入SQL模板
     */
    private static final String INSERT_SQL_TEMPLATE = 
        "INSERT INTO %s (" +
        "log_id, operation, module, description, method_name, method_args, method_result, " +
        "exception_info, status, execution_time, user_id, username, user_role, " +
        "client_ip, user_agent, request_url, http_method, session_id, trace_id, " +
        "operation_time, server_name, application_name, environment, priority, " +
        "tags, extended_properties, create_time" +
        ") VALUES (" +
        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
        ")";
    
    /**
     * 创建表SQL（MySQL）
     */
    private static final String CREATE_TABLE_SQL_MYSQL = 
        "CREATE TABLE IF NOT EXISTS %s (" +
        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
        "log_id VARCHAR(64) NOT NULL," +
        "operation VARCHAR(100)," +
        "module VARCHAR(100)," +
        "description TEXT," +
        "method_name VARCHAR(200)," +
        "method_args TEXT," +
        "method_result TEXT," +
        "exception_info TEXT," +
        "status VARCHAR(20)," +
        "execution_time BIGINT," +
        "user_id VARCHAR(64)," +
        "username VARCHAR(100)," +
        "user_role VARCHAR(100)," +
        "client_ip VARCHAR(45)," +
        "user_agent TEXT," +
        "request_url VARCHAR(500)," +
        "http_method VARCHAR(10)," +
        "session_id VARCHAR(64)," +
        "trace_id VARCHAR(64)," +
        "operation_time DATETIME," +
        "server_name VARCHAR(100)," +
        "application_name VARCHAR(100)," +
        "environment VARCHAR(20)," +
        "priority INT," +
        "tags TEXT," +
        "extended_properties TEXT," +
        "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
        "INDEX idx_log_id (log_id)," +
        "INDEX idx_operation (operation)," +
        "INDEX idx_user_id (user_id)," +
        "INDEX idx_operation_time (operation_time)," +
        "INDEX idx_trace_id (trace_id)" +
        ")";
    
    /**
     * 处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handle(AuditLogInfo auditLogInfo) {
        try {
            if (jdbcTemplate == null) {
                System.out.println("JdbcTemplate未配置，无法保存数据库审计日志");
                return false;
            }
            
            // 获取数据库配置
            XingGeLogProperty.AuditConfig.DatabaseConfig dbConfig = 
                logProperty.getAudit().getHandlers().getDatabase();
            
            String tableName = dbConfig.getTableName();
            if (tableName == null || tableName.trim().isEmpty()) {
                tableName = DEFAULT_TABLE_NAME;
            }
            
            // 自动创建表（如果启用）
            if (dbConfig.getAutoCreateTable()) {
                createTableIfNotExists(tableName);
            }
            
            // 插入审计日志
            return insertAuditLog(auditLogInfo, tableName);
            
        } catch (Exception e) {
            System.err.println("数据库审计日志处理失败: " + e.getMessage());
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
                System.err.println("数据库审计日志异步处理失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 插入审计日志到数据库
     * 
     * @param auditLogInfo 审计日志信息
     * @param tableName 表名
     * @return 插入结果
     */
    private boolean insertAuditLog(AuditLogInfo auditLogInfo, String tableName) {
        try {
            String sql = String.format(INSERT_SQL_TEMPLATE, tableName);
            
            int result = jdbcTemplate.update(sql,
                auditLogInfo.getLogId(),
                auditLogInfo.getOperation(),
                auditLogInfo.getModule(),
                auditLogInfo.getDescription(),
                auditLogInfo.getMethodName(),
                auditLogInfo.getMethodArgs(),
                auditLogInfo.getMethodResult(),
                auditLogInfo.getExceptionInfo(),
                auditLogInfo.getStatus(),
                auditLogInfo.getExecutionTime(),
                auditLogInfo.getUserId(),
                auditLogInfo.getUsername(),
                auditLogInfo.getUserRole(),
                auditLogInfo.getClientIp(),
                auditLogInfo.getUserAgent(),
                auditLogInfo.getRequestUrl(),
                auditLogInfo.getHttpMethod(),
                auditLogInfo.getSessionId(),
                auditLogInfo.getTraceId(),
                auditLogInfo.getOperationTime() != null ? 
                    Timestamp.valueOf(auditLogInfo.getOperationTime()) : null,
                auditLogInfo.getServerName(),
                auditLogInfo.getApplicationName(),
                auditLogInfo.getEnvironment(),
                auditLogInfo.getPriority(),
                auditLogInfo.getTags() != null ? 
                    String.join(",", auditLogInfo.getTags()) : null,
                auditLogInfo.getExtendedProperties() != null ? 
                    auditLogInfo.getExtendedProperties().toString() : null,
                auditLogInfo.getCreateTime() != null ? 
                    Timestamp.valueOf(auditLogInfo.getCreateTime()) : 
                    Timestamp.valueOf(LocalDateTime.now())
            );
            
            if (result > 0) {
                System.out.println("数据库审计日志保存成功: " + auditLogInfo.getLogId());
                return true;
            } else {
                System.out.println("数据库审计日志保存失败，影响行数为0: " + auditLogInfo.getLogId());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("插入审计日志到数据库失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 创建表（如果不存在）
     * 
     * @param tableName 表名
     */
    private void createTableIfNotExists(String tableName) {
        try {
            String sql = String.format(CREATE_TABLE_SQL_MYSQL, tableName);
            jdbcTemplate.execute(sql);
            System.out.println("审计日志表创建成功或已存在: " + tableName);
        } catch (Exception e) {
            System.out.println("创建审计日志表失败: " + tableName + ", 错误: " + e.getMessage());
        }
    }
    
    @Override
    public String getHandlerName() {
        return "DatabaseAuditLogHandler";
    }
    
    @Override
    public int getPriority() {
        return 90; // 较低优先级，确保其他处理器先执行
    }
    
    @Override
    public boolean supports(AuditLogInfo auditLogInfo) {
        // 检查是否配置了数据库处理器
        XingGeLogProperty.AuditConfig.DatabaseConfig dbConfig = 
            logProperty.getAudit().getHandlers().getDatabase();
        return dbConfig != null && dbConfig.getEnabled() && jdbcTemplate != null;
    }
    
    @Override
    public void initialize() {
        System.out.println("数据库审计日志处理器已初始化");
        
        // 检查数据库连接
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                System.out.println("数据库连接正常");
            } catch (Exception e) {
                System.out.println("数据库连接异常: " + e.getMessage());
            }
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("数据库审计日志处理器已销毁");
    }
}