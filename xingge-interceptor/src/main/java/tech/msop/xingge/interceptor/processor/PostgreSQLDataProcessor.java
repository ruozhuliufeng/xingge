package tech.msop.xingge.interceptor.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessor;
import tech.msop.xingge.interceptor.InterceptData;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL数据库处理器
 * 将拦截数据保存到PostgreSQL数据库指定表
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class PostgreSQLDataProcessor implements DataProcessor {

    private static final String PROCESSOR_TYPE = "postgresql";
    
    // 配置键
    private static final String CONFIG_TABLE_NAME = "tableName";
    private static final String CONFIG_SCHEMA_NAME = "schemaName";
    private static final String CONFIG_BATCH_SIZE = "batchSize";
    private static final String CONFIG_CUSTOM_FIELDS = "customFields";
    private static final String CONFIG_USE_JSONB = "useJsonb";
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    private final List<InterceptData> batchData = new ArrayList<>();
    private final Object batchLock = new Object();

    @Override
    public String getType() {
        return PROCESSOR_TYPE;
    }

    @Override
    public void process(InterceptData data, Map<String, Object> config) {
        try {
            String tableName = getFullTableName(config);
            if (tableName == null) {
                log.error("PostgreSQL处理器缺少表名配置");
                return;
            }
            
            Integer batchSize = (Integer) config.getOrDefault(CONFIG_BATCH_SIZE, 1);
            
            if (batchSize <= 1) {
                // 单条插入
                insertSingle(data, tableName, config);
            } else {
                // 批量插入
                insertBatch(data, tableName, batchSize, config);
            }
            
        } catch (Exception e) {
            log.error("PostgreSQL处理器处理数据时发生异常", e);
        }
    }

    @Override
    public int getPriority() {
        return 55;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        String tableName = (String) config.get(CONFIG_TABLE_NAME);
        if (tableName == null || tableName.trim().isEmpty()) {
            log.error("PostgreSQL处理器配置验证失败：缺少表名");
            return false;
        }
        
        if (jdbcTemplate == null) {
            log.error("PostgreSQL处理器配置验证失败：未找到JdbcTemplate");
            return false;
        }
        
        return true;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        
        String tableName = getFullTableName(config);
        if (tableName != null) {
            // 检查表是否存在，如果不存在则创建
            createTableIfNotExists(tableName, config);
        }
    }

    /**
     * 获取完整表名（包含schema）
     */
    private String getFullTableName(Map<String, Object> config) {
        String tableName = (String) config.get(CONFIG_TABLE_NAME);
        if (tableName == null || tableName.trim().isEmpty()) {
            return null;
        }
        
        String schemaName = (String) config.get(CONFIG_SCHEMA_NAME);
        if (schemaName != null && !schemaName.trim().isEmpty()) {
            return schemaName + "." + tableName;
        }
        
        return tableName;
    }

    /**
     * 单条插入
     */
    private void insertSingle(InterceptData data, String tableName, Map<String, Object> config) {
        String sql = buildInsertSQL(tableName, config);
        Object[] params = buildInsertParams(data, config);
        
        jdbcTemplate.update(sql, params);
        log.debug("成功插入拦截数据到PostgreSQL表: {}", tableName);
    }

    /**
     * 批量插入
     */
    private void insertBatch(InterceptData data, String tableName, int batchSize, Map<String, Object> config) {
        synchronized (batchLock) {
            batchData.add(data);
            
            if (batchData.size() >= batchSize) {
                flushBatch(tableName, config);
            }
        }
    }

    /**
     * 刷新批量数据
     */
    private void flushBatch(String tableName, Map<String, Object> config) {
        if (batchData.isEmpty()) {
            return;
        }
        
        String sql = buildInsertSQL(tableName, config);
        List<Object[]> batchParams = new ArrayList<>();
        
        for (InterceptData data : batchData) {
            batchParams.add(buildInsertParams(data, config));
        }
        
        jdbcTemplate.batchUpdate(sql, batchParams);
        log.debug("成功批量插入{}条拦截数据到PostgreSQL表: {}", batchData.size(), tableName);
        
        batchData.clear();
    }

    /**
     * 构建插入SQL
     */
    private String buildInsertSQL(String tableName, Map<String, Object> config) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        
        // 基础字段
        sql.append("id, intercept_type, intercept_scope, method, url, path, ");
        sql.append("query_params, headers, request_body, response_status, response_headers, ");
        sql.append("response_body, duration, client_ip, user_agent, user_id, session_id, ");
        sql.append("tenant_id, application_name, exception, created_time");
        
        // 自定义字段
        @SuppressWarnings("unchecked")
        Map<String, String> customFields = (Map<String, String>) config.get(CONFIG_CUSTOM_FIELDS);
        if (customFields != null && !customFields.isEmpty()) {
            for (String fieldName : customFields.keySet()) {
                sql.append(", ").append(fieldName);
            }
        }
        
        sql.append(") VALUES (");
        
        // 基础字段占位符
        sql.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
        
        // 自定义字段占位符
        if (customFields != null && !customFields.isEmpty()) {
            for (int i = 0; i < customFields.size(); i++) {
                sql.append(", ?");
            }
        }
        
        sql.append(")");
        
        return sql.toString();
    }

    /**
     * 构建插入参数
     */
    private Object[] buildInsertParams(InterceptData data, Map<String, Object> config) {
        List<Object> params = new ArrayList<>();
        
        Boolean useJsonb = (Boolean) config.getOrDefault(CONFIG_USE_JSONB, true);
        
        // 基础字段参数
        params.add(data.getId());
        params.add(data.getInterceptType());
        params.add(data.getInterceptScope());
        params.add(data.getMethod());
        params.add(data.getUrl());
        params.add(data.getPath());
        
        if (useJsonb) {
            // 使用JSONB类型存储复杂数据
            params.add(mapToJson(data.getQueryParams()));
            params.add(mapToJson(data.getHeaders()));
        } else {
            // 使用TEXT类型存储
            params.add(mapToJson(data.getQueryParams()));
            params.add(mapToJson(data.getHeaders()));
        }
        
        params.add(data.getRequestBody());
        params.add(data.getResponseStatus());
        
        if (useJsonb) {
            params.add(mapToJson(data.getResponseHeaders()));
        } else {
            params.add(mapToJson(data.getResponseHeaders()));
        }
        
        params.add(data.getResponseBody());
        params.add(data.getDuration());
        params.add(data.getClientIp());
        params.add(data.getUserAgent());
        params.add(data.getUserId());
        params.add(data.getSessionId());
        params.add(data.getTenantId());
        params.add(data.getApplicationName());
        params.add(data.getException());
        params.add(new Timestamp(data.getTimestamp().getTime()));
        
        // 自定义字段参数
        @SuppressWarnings("unchecked")
        Map<String, String> customFields = (Map<String, String>) config.get(CONFIG_CUSTOM_FIELDS);
        if (customFields != null && !customFields.isEmpty()) {
            for (String expression : customFields.values()) {
                Object value = evaluateExpression(data, expression);
                params.add(value);
            }
        }
        
        return params.toArray();
    }

    /**
     * 创建表（如果不存在）
     */
    private void createTableIfNotExists(String tableName, Map<String, Object> config) {
        try {
            String createTableSQL = buildCreateTableSQL(tableName, config);
            jdbcTemplate.execute(createTableSQL);
            
            // 创建索引
            createIndexes(tableName);
            
            log.info("成功创建PostgreSQL拦截数据表: {}", tableName);
        } catch (Exception e) {
            log.debug("PostgreSQL表{}可能已存在，跳过创建: {}", tableName, e.getMessage());
        }
    }

    /**
     * 构建创建表SQL
     */
    private String buildCreateTableSQL(String tableName, Map<String, Object> config) {
        Boolean useJsonb = (Boolean) config.getOrDefault(CONFIG_USE_JSONB, true);
        String jsonType = useJsonb ? "JSONB" : "TEXT";
        
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id VARCHAR(64) PRIMARY KEY, " +
                "intercept_type VARCHAR(32), " +
                "intercept_scope VARCHAR(32), " +
                "method VARCHAR(16), " +
                "url TEXT, " +
                "path VARCHAR(512), " +
                "query_params " + jsonType + ", " +
                "headers " + jsonType + ", " +
                "request_body TEXT, " +
                "response_status INTEGER, " +
                "response_headers " + jsonType + ", " +
                "response_body TEXT, " +
                "duration BIGINT, " +
                "client_ip VARCHAR(64), " +
                "user_agent TEXT, " +
                "user_id VARCHAR(64), " +
                "session_id VARCHAR(128), " +
                "tenant_id VARCHAR(64), " +
                "application_name VARCHAR(128), " +
                "exception TEXT, " +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
    }

    /**
     * 创建索引
     */
    private void createIndexes(String tableName) {
        try {
            String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_intercept_type ON " + tableName + " (intercept_type)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_intercept_scope ON " + tableName + " (intercept_scope)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_method ON " + tableName + " (method)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_path ON " + tableName + " (path)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_response_status ON " + tableName + " (response_status)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_client_ip ON " + tableName + " (client_ip)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_user_id ON " + tableName + " (user_id)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_session_id ON " + tableName + " (session_id)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_tenant_id ON " + tableName + " (tenant_id)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_application_name ON " + tableName + " (application_name)",
                "CREATE INDEX IF NOT EXISTS idx_" + getTableNameOnly(tableName) + "_created_time ON " + tableName + " (created_time DESC)"
            };
            
            for (String indexSQL : indexes) {
                jdbcTemplate.execute(indexSQL);
            }
            
            log.debug("成功为PostgreSQL表{}创建索引", tableName);
            
        } catch (Exception e) {
            log.warn("为PostgreSQL表{}创建索引时发生异常: {}", tableName, e.getMessage());
        }
    }

    /**
     * 获取表名（不包含schema）
     */
    private String getTableNameOnly(String fullTableName) {
        if (fullTableName.contains(".")) {
            return fullTableName.substring(fullTableName.lastIndexOf(".") + 1);
        }
        return fullTableName;
    }

    /**
     * 将Map转换为JSON字符串
     */
    private String mapToJson(Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.warn("转换Map到JSON失败", e);
            return map.toString();
        }
    }

    /**
     * 评估表达式（简单实现）
     */
    private Object evaluateExpression(InterceptData data, String expression) {
        // 这里可以实现更复杂的表达式解析
        // 目前只支持简单的字段引用
        switch (expression) {
            case "${id}":
                return data.getId();
            case "${interceptType}":
                return data.getInterceptType();
            case "${method}":
                return data.getMethod();
            case "${url}":
                return data.getUrl();
            case "${clientIp}":
                return data.getClientIp();
            case "${userId}":
                return data.getUserId();
            case "${timestamp}":
                return new Timestamp(data.getTimestamp().getTime());
            default:
                return expression;
        }
    }

    /**
     * 强制刷新批量数据（用于应用关闭时）
     */
    public void forceFlushBatch(String tableName, Map<String, Object> config) {
        synchronized (batchLock) {
            if (!batchData.isEmpty()) {
                flushBatch(tableName, config);
            }
        }
    }
}