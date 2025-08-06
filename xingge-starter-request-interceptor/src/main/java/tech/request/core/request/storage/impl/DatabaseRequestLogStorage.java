/*
 * Copyright (c) 2024 行歌(xingge)
 * 数据库请求日志存储实现
 * 
 * 功能说明：
 * - 将请求日志存储到数据库表中
 * - 自动维护表结构
 * - 支持批量插入和异步处理
 */
package tech.request.core.request.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.msop.core.tool.async.AsyncProcessor;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.storage.RequestLogStorage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库请求日志存储实现
 * 
 * <p>该实现将请求日志存储到数据库表中，支持以下特性：</p>
 * <ul>
 *   <li>自动创建和维护表结构</li>
 *   <li>支持批量插入提高性能</li>
 *   <li>异步处理避免阻塞业务</li>
 *   <li>事务支持保证数据一致性</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Component
@ConditionalOnProperty(name = "xg.request.database.enabled", havingValue = "true")
public class DatabaseRequestLogStorage implements RequestLogStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRequestLogStorage.class);
    
    /**
     * 表名
     */
    private static final String TABLE_NAME = "t_request_interceptor_log";
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * 请求拦截器配置属性
     */
    @Autowired
    private RequestInterceptorProperty properties;
    
    /**
     * JDBC模板
     */
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 数据源
     */
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * 异步处理器
     */
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    /**
     * 初始化方法
     */
    @PostConstruct
    public void initialize() throws Exception {
        if (jdbcTemplate == null) {
            logger.warn("JdbcTemplate未配置，数据库存储功能将不可用");
            return;
        }
        
        // 初始化表结构
        initializeTable();
        
        logger.info("数据库请求日志存储初始化完成，表名: {}", TABLE_NAME);
    }
    
    /**
     * 销毁方法
     */
    @PreDestroy
    public void destroy() {
        // AsyncProcessor由Spring容器管理，无需手动销毁
        logger.info("数据库请求日志存储销毁完成");
    }
    
    /**
     * 初始化表结构
     */
    private void initializeTable() throws Exception {
        if (!tableExists()) {
            createTable();
            logger.info("创建请求日志表: {}", TABLE_NAME);
        } else {
            // 检查表结构是否需要更新
            updateTableStructure();
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean tableExists() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, TABLE_NAME.toUpperCase(), new String[]{"TABLE"})) {
                return resultSet.next();
            }
        }
    }
    
    /**
     * 创建表
     */
    private void createTable() {
        String createTableSql = String.format(
            "CREATE TABLE %s (" +
            "    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID'," +
            "    request_id VARCHAR(100) NOT NULL COMMENT '请求唯一标识'," +
            "    client_type VARCHAR(50) COMMENT 'HTTP客户端类型'," +
            "    method VARCHAR(20) COMMENT '请求方法'," +
            "    url TEXT COMMENT '请求URL'," +
            "    client_ip VARCHAR(50) COMMENT '客户端IP地址'," +
            "    user_agent TEXT COMMENT '用户代理'," +
            "    request_headers TEXT COMMENT '请求头'," +
            "    request_params TEXT COMMENT '请求参数'," +
            "    request_body LONGTEXT COMMENT '请求体'," +
            "    response_status INT COMMENT '响应状态码'," +
            "    response_headers TEXT COMMENT '响应头'," +
            "    response_body LONGTEXT COMMENT '响应体'," +
            "    request_time DATETIME(3) COMMENT '请求时间'," +
            "    response_time DATETIME(3) COMMENT '响应时间'," +
            "    duration BIGINT COMMENT '执行耗时(毫秒)'," +
            "    success BOOLEAN COMMENT '是否成功'," +
            "    error_message TEXT COMMENT '错误信息'," +
            "    application_name VARCHAR(100) COMMENT '应用名称'," +
            "    environment VARCHAR(50) COMMENT '环境信息'," +
            "    create_time DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间'," +
            "    INDEX idx_request_id (request_id)," +
            "    INDEX idx_client_type (client_type)," +
            "    INDEX idx_method (method)," +
            "    INDEX idx_request_time (request_time)," +
            "    INDEX idx_success (success)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求拦截日志表'",
            TABLE_NAME);
        
        jdbcTemplate.execute(createTableSql);
    }
    
    /**
     * 更新表结构
     */
    private void updateTableStructure() {
        // 这里可以添加表结构升级逻辑
        // 例如：添加新字段、修改字段类型等
        logger.debug("检查表结构更新: {}", TABLE_NAME);
    }
    
    @Override
    @Transactional
    public void store(RequestLogInfo logInfo) throws Exception {
        if (jdbcTemplate == null) {
            logger.warn("JdbcTemplate未配置，跳过数据库存储");
            return;
        }
        
        String insertSql = String.format(
            "INSERT INTO %s (" +
            "    request_id, client_type, method, url, client_ip, user_agent," +
            "    request_headers, request_params, request_body," +
            "    response_status, response_headers, response_body," +
            "    request_time, response_time, duration, success, error_message," +
            "    application_name, environment" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME);
        
        try {
            jdbcTemplate.update(insertSql,
                logInfo.getRequestId(),
                logInfo.getClientType(),
                logInfo.getMethod(),
                logInfo.getUrl(),
                logInfo.getClientIp(),
                logInfo.getUserAgent(),
                convertMapToJson(logInfo.getRequestHeaders()),
                convertMapToJson(logInfo.getRequestParams()),
                logInfo.getRequestBody(),
                logInfo.getResponseStatus(),
                convertMapToJson(logInfo.getResponseHeaders()),
                logInfo.getResponseBody(),
                logInfo.getRequestTime(),
                logInfo.getResponseTime(),
                logInfo.getDuration(),
                logInfo.getSuccess(),
                logInfo.getErrorMessage(),
                logInfo.getApplicationName(),
                logInfo.getEnvironment()
            );
            
            logger.debug("成功保存请求日志到数据库: {}", logInfo.getRequestId());
        } catch (Exception e) {
            logger.error("保存请求日志到数据库失败: {}", logInfo.getRequestId(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        if (jdbcTemplate == null) {
            logger.warn("JdbcTemplate未配置，跳过数据库批量存储");
            return;
        }
        
        String insertSql = String.format(
            "INSERT INTO %s (" +
            "    request_id, client_type, method, url, client_ip, user_agent," +
            "    request_headers, request_params, request_body," +
            "    response_status, response_headers, response_body," +
            "    request_time, response_time, duration, success, error_message," +
            "    application_name, environment" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            TABLE_NAME);
        
        try {
            jdbcTemplate.batchUpdate(insertSql, logInfoList, logInfoList.size(),
                (ps, logInfo) -> {
                    ps.setString(1, logInfo.getRequestId());
                    ps.setString(2, logInfo.getClientType());
                    ps.setString(3, logInfo.getMethod());
                    ps.setString(4, logInfo.getUrl());
                    ps.setString(5, logInfo.getClientIp());
                    ps.setString(6, logInfo.getUserAgent());
                    ps.setString(7, convertMapToJson(logInfo.getRequestHeaders()));
                    ps.setString(8, convertMapToJson(logInfo.getRequestParams()));
                    ps.setString(9, logInfo.getRequestBody());
                    ps.setObject(10, logInfo.getResponseStatus());
                    ps.setString(11, convertMapToJson(logInfo.getResponseHeaders()));
                    ps.setString(12, logInfo.getResponseBody());
                    ps.setObject(13, logInfo.getRequestTime());
                    ps.setObject(14, logInfo.getResponseTime());
                    ps.setObject(15, logInfo.getDuration());
                    ps.setObject(16, logInfo.getSuccess());
                    ps.setString(17, logInfo.getErrorMessage());
                    ps.setString(18, logInfo.getApplicationName());
                    ps.setString(19, logInfo.getEnvironment());
                });
            
            logger.debug("成功批量保存{}条请求日志到数据库", logInfoList.size());
        } catch (Exception e) {
            logger.error("批量保存请求日志到数据库失败，数量: {}", logInfoList.size(), e);
            throw e;
        }
    }
    
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return asyncProcessor.executeAsyncWithResult(
            () -> {
                try {
                    store(logInfo);
                    return null;
                } catch (Exception e) {
                    logger.error("异步保存请求日志到数据库失败: {}", logInfo.getRequestId(), e);
                    throw new RuntimeException(e);
                }
            },
            "数据库存储请求日志-" + logInfo.getRequestId()
        );
    }
    
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return asyncProcessor.executeAsyncWithResult(
            () -> {
                try {
                    batchStore(logInfoList);
                    return null;
                } catch (Exception e) {
                    logger.error("异步批量保存请求日志到数据库失败，数量: {}", 
                               logInfoList != null ? logInfoList.size() : 0, e);
                    throw new RuntimeException(e);
                }
            },
            "数据库批量存储请求日志-" + (logInfoList != null ? logInfoList.size() : 0) + "条"
        );
    }
    
    @Override
    public boolean isAvailable() {
        return jdbcTemplate != null && properties.getDatabase().isEnabled();
    }
    
    @Override
    public String getStorageType() {
        return "DATABASE";
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private String convertMapToJson(Object map) {
        if (map == null) {
            return null;
        }
        
        try {
            // 这里可以使用Jackson或其他JSON库进行转换
            // 简单实现，实际项目中建议使用专业的JSON库
            return map.toString();
        } catch (Exception e) {
            logger.warn("转换Map到JSON失败: {}", e.getMessage());
            return map.toString();
        }
    }
}