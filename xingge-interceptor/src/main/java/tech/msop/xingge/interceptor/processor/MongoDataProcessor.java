package tech.msop.xingge.interceptor.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.DataProcessor;
import tech.msop.xingge.interceptor.InterceptData;

import java.util.*;

/**
 * MongoDB数据处理器
 * 将拦截数据保存到MongoDB
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class MongoDataProcessor implements DataProcessor {

    private static final String PROCESSOR_TYPE = "mongodb";
    
    // 配置键
    private static final String CONFIG_COLLECTION_NAME = "collectionName";
    private static final String CONFIG_DATABASE_NAME = "databaseName";
    private static final String CONFIG_BATCH_SIZE = "batchSize";
    private static final String CONFIG_CREATE_INDEXES = "createIndexes";
    private static final String CONFIG_CUSTOM_FIELDS = "customFields";
    private static final String CONFIG_TTL_SECONDS = "ttlSeconds";
    
    @Autowired(required = false)
    private MongoTemplate mongoTemplate;
    
    @Autowired(required = false)
    private ObjectMapper objectMapper;
    
    private final List<Map<String, Object>> batchData = new ArrayList<>();
    private final Object batchLock = new Object();
    private final Set<String> initializedCollections = new HashSet<>();

    @Override
    public String getType() {
        return PROCESSOR_TYPE;
    }

    @Override
    public void process(InterceptData data, Map<String, Object> config) {
        try {
            String collectionName = (String) config.getOrDefault(CONFIG_COLLECTION_NAME, "intercept_data");
            Integer batchSize = (Integer) config.getOrDefault(CONFIG_BATCH_SIZE, 1);
            
            // 确保集合已初始化
            ensureCollectionInitialized(collectionName, config);
            
            // 转换数据为MongoDB文档
            Map<String, Object> document = convertToDocument(data, config);
            
            if (batchSize <= 1) {
                // 单条插入
                insertSingle(document, collectionName);
            } else {
                // 批量插入
                insertBatch(document, collectionName, batchSize, config);
            }
            
        } catch (Exception e) {
            log.error("MongoDB处理器处理数据时发生异常", e);
        }
    }

    @Override
    public int getPriority() {
        return 60;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (mongoTemplate == null) {
            log.error("MongoDB处理器配置验证失败：未找到MongoTemplate");
            return false;
        }
        
        String collectionName = (String) config.get(CONFIG_COLLECTION_NAME);
        if (collectionName != null && collectionName.trim().isEmpty()) {
            log.error("MongoDB处理器配置验证失败：集合名不能为空");
            return false;
        }
        
        return true;
    }

    @Override
    public void initialize(Map<String, Object> config) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    /**
     * 单条插入
     */
    private void insertSingle(Map<String, Object> document, String collectionName) {
        mongoTemplate.insert(document, collectionName);
        log.debug("成功插入拦截数据到MongoDB集合: {}", collectionName);
    }

    /**
     * 批量插入
     */
    private void insertBatch(Map<String, Object> document, String collectionName, int batchSize, Map<String, Object> config) {
        synchronized (batchLock) {
            batchData.add(document);
            
            if (batchData.size() >= batchSize) {
                flushBatch(collectionName);
            }
        }
    }

    /**
     * 刷新批量数据
     */
    private void flushBatch(String collectionName) {
        if (batchData.isEmpty()) {
            return;
        }
        
        mongoTemplate.insert(batchData, collectionName);
        log.debug("成功批量插入{}条拦截数据到MongoDB集合: {}", batchData.size(), collectionName);
        
        batchData.clear();
    }

    /**
     * 确保集合已初始化
     */
    private void ensureCollectionInitialized(String collectionName, Map<String, Object> config) {
        if (initializedCollections.contains(collectionName)) {
            return;
        }
        
        synchronized (initializedCollections) {
            if (initializedCollections.contains(collectionName)) {
                return;
            }
            
            // 创建索引
            Boolean createIndexes = (Boolean) config.getOrDefault(CONFIG_CREATE_INDEXES, true);
            if (createIndexes) {
                createIndexes(collectionName, config);
            }
            
            initializedCollections.add(collectionName);
        }
    }

    /**
     * 创建索引
     */
    private void createIndexes(String collectionName, Map<String, Object> config) {
        try {
            IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
            
            // 基础索引
            indexOps.ensureIndex(new Index().on("interceptType", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("interceptScope", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("method", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("path", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("responseStatus", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("clientIp", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("userId", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("sessionId", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("tenantId", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("applicationName", org.springframework.data.domain.Sort.Direction.ASC));
            indexOps.ensureIndex(new Index().on("timestamp", org.springframework.data.domain.Sort.Direction.DESC));
            
            // 复合索引
            indexOps.ensureIndex(new Index()
                    .on("interceptType", org.springframework.data.domain.Sort.Direction.ASC)
                    .on("timestamp", org.springframework.data.domain.Sort.Direction.DESC));
            
            indexOps.ensureIndex(new Index()
                    .on("userId", org.springframework.data.domain.Sort.Direction.ASC)
                    .on("timestamp", org.springframework.data.domain.Sort.Direction.DESC));
            
            indexOps.ensureIndex(new Index()
                    .on("tenantId", org.springframework.data.domain.Sort.Direction.ASC)
                    .on("timestamp", org.springframework.data.domain.Sort.Direction.DESC));
            
            // TTL索引（如果配置了过期时间）
            Integer ttlSeconds = (Integer) config.get(CONFIG_TTL_SECONDS);
            if (ttlSeconds != null && ttlSeconds > 0) {
                indexOps.ensureIndex(new Index()
                        .on("timestamp", org.springframework.data.domain.Sort.Direction.ASC)
                        .expire(ttlSeconds));
            }
            
            log.info("成功为MongoDB集合{}创建索引", collectionName);
            
        } catch (Exception e) {
            log.warn("为MongoDB集合{}创建索引时发生异常: {}", collectionName, e.getMessage());
        }
    }

    /**
     * 转换拦截数据为MongoDB文档
     */
    private Map<String, Object> convertToDocument(InterceptData data, Map<String, Object> config) {
        Map<String, Object> document = new HashMap<>();
        
        // 基础字段
        document.put("_id", data.getId());
        document.put("interceptType", data.getInterceptType());
        document.put("interceptScope", data.getInterceptScope());
        document.put("method", data.getMethod());
        document.put("url", data.getUrl());
        document.put("path", data.getPath());
        document.put("queryParams", data.getQueryParams());
        document.put("headers", data.getHeaders());
        document.put("requestBody", data.getRequestBody());
        document.put("responseStatus", data.getResponseStatus());
        document.put("responseHeaders", data.getResponseHeaders());
        document.put("responseBody", data.getResponseBody());
        document.put("duration", data.getDuration());
        document.put("clientIp", data.getClientIp());
        document.put("userAgent", data.getUserAgent());
        document.put("userId", data.getUserId());
        document.put("sessionId", data.getSessionId());
        document.put("tenantId", data.getTenantId());
        document.put("applicationName", data.getApplicationName());
        document.put("exception", data.getException());
        document.put("timestamp", data.getTimestamp());
        
        // 自定义字段
        @SuppressWarnings("unchecked")
        Map<String, String> customFields = (Map<String, String>) config.get(CONFIG_CUSTOM_FIELDS);
        if (customFields != null && !customFields.isEmpty()) {
            for (Map.Entry<String, String> entry : customFields.entrySet()) {
                String fieldName = entry.getKey();
                String expression = entry.getValue();
                Object value = evaluateExpression(data, expression);
                document.put(fieldName, value);
            }
        }
        
        // 添加元数据
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdTime", new Date());
        metadata.put("processor", PROCESSOR_TYPE);
        metadata.put("version", "1.0.0");
        document.put("_metadata", metadata);
        
        return document;
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
            case "${path}":
                return data.getPath();
            case "${clientIp}":
                return data.getClientIp();
            case "${userId}":
                return data.getUserId();
            case "${sessionId}":
                return data.getSessionId();
            case "${tenantId}":
                return data.getTenantId();
            case "${timestamp}":
                return data.getTimestamp();
            case "${date}":
                return new Date(data.getTimestamp().getTime());
            case "${year}":
                Calendar cal = Calendar.getInstance();
                cal.setTime(data.getTimestamp());
                return cal.get(Calendar.YEAR);
            case "${month}":
                cal = Calendar.getInstance();
                cal.setTime(data.getTimestamp());
                return cal.get(Calendar.MONTH) + 1;
            case "${day}":
                cal = Calendar.getInstance();
                cal.setTime(data.getTimestamp());
                return cal.get(Calendar.DAY_OF_MONTH);
            case "${hour}":
                cal = Calendar.getInstance();
                cal.setTime(data.getTimestamp());
                return cal.get(Calendar.HOUR_OF_DAY);
            default:
                return expression;
        }
    }

    /**
     * 强制刷新批量数据（用于应用关闭时）
     */
    public void forceFlushBatch(String collectionName) {
        synchronized (batchLock) {
            if (!batchData.isEmpty()) {
                flushBatch(collectionName);
            }
        }
    }
}