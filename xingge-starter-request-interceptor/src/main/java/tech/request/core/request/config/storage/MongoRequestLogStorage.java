/*
 * Copyright (c) 2024 行歌(xingge)
 * MongoDB请求日志存储实现
 * 
 * 功能说明：
 * - 实现RequestLogStorage接口的MongoDB存储方式
 * - 支持自定义MongoDB连接配置
 * - 支持批量和异步存储
 */
package tech.request.core.request.config.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.storage.RequestLogStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MongoDB请求日志存储实现
 * 
 * <p>该类实现了RequestLogStorage接口，提供MongoDB存储方式：</p>
 * <ul>
 *   <li>支持自定义MongoDB连接配置</li>
 *   <li>支持批量存储和异步存储</li>
 *   <li>支持自定义集合名称</li>
 *   <li>支持自定义数据库名称</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   request:
 *     storage-type: MONGO
 *     mongo:
 *       enabled: true
 *       collection-name: request_logs
 *       database-name: logs
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
public class MongoRequestLogStorage implements RequestLogStorage, InitializingBean, DisposableBean {
    
    private static final Logger log = LoggerFactory.getLogger(MongoRequestLogStorage.class);
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * MongoDB客户端
     */
    private MongoClient mongoClient;
    
    /**
     * MongoDB数据库
     */
    private MongoDatabase database;
    
    /**
     * MongoDB集合
     */
    private MongoCollection<Document> collection;
    
    /**
     * 异步执行线程池
     */
    private final ExecutorService executorService;
    
    /**
     * 构造函数
     * 
     * @param property 请求拦截器配置属性
     */
    public MongoRequestLogStorage(RequestInterceptorProperty property) {
        this.property = property;
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        initialize();
    }
    
    @Override
    public void destroy() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
        }
        executorService.shutdown();
    }
    
    @Override
    public void initialize() throws Exception {
        // 检查MongoDB配置是否启用
        if (!property.getMongo().isEnabled()) {
            log.warn("MongoDB storage is not enabled in configuration");
            return;
        }
        
        try {
            // 获取MongoDB连接URI
            // 如果配置了自定义URI，则使用自定义URI，否则使用默认URI
            String mongoUri = "mongodb://localhost:27017";
            
            // 创建MongoDB客户端
            mongoClient = MongoClients.create(mongoUri);
            
            // 获取数据库名称，如果未配置则使用默认值
            String databaseName = property.getMongo().getDatabaseName();
            if (!StringUtils.hasText(databaseName)) {
                databaseName = "logs";
            }
            database = mongoClient.getDatabase(databaseName);
            
            // 获取集合名称，如果未配置则使用默认值
            String collectionName = property.getMongo().getCollectionName();
            if (!StringUtils.hasText(collectionName)) {
                collectionName = "t_request_interceptor_log";
            }
            collection = database.getCollection(collectionName);
            
            log.info("MongoDB storage initialized with database: {}, collection: {}", databaseName, collectionName);
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB storage: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        if (!isAvailable()) {
            log.warn("MongoDB storage is not available");
            return;
        }
        
        try {
            // 将RequestLogInfo转换为Document
            Document document = convertToDocument(logInfo);
            
            // 插入文档
            collection.insertOne(document);
            
            log.debug("Stored request log to MongoDB: {}", logInfo.getRequestId());
        } catch (Exception e) {
            log.error("Failed to store request log to MongoDB: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (!isAvailable() || logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        try {
            // 将RequestLogInfo列表转换为Document列表
            List<Document> documents = new ArrayList<>(logInfoList.size());
            for (RequestLogInfo logInfo : logInfoList) {
                documents.add(convertToDocument(logInfo));
            }
            
            // 批量插入文档
            collection.insertMany(documents);
            
            log.debug("Batch stored {} request logs to MongoDB", logInfoList.size());
        } catch (Exception e) {
            log.error("Failed to batch store request logs to MongoDB: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                store(logInfo);
            } catch (Exception e) {
                log.error("Async store request log to MongoDB failed: {}", e.getMessage(), e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return CompletableFuture.runAsync(() -> {
            try {
                batchStore(logInfoList);
            } catch (Exception e) {
                log.error("Async batch store request logs to MongoDB failed: {}", e.getMessage(), e);
            }
        }, executorService);
    }
    
    @Override
    public boolean isAvailable() {
        return mongoClient != null && collection != null;
    }
    
    @Override
    public String getStorageType() {
        return "MONGO";
    }
    
    /**
     * 将RequestLogInfo转换为MongoDB Document
     * 
     * @param logInfo 请求日志信息
     * @return MongoDB Document
     */
    private Document convertToDocument(RequestLogInfo logInfo) {
        Document document = new Document();
        
        // 设置基本字段
        document.append("requestId", logInfo.getRequestId());
        document.append("requestTime", logInfo.getRequestTime());
        document.append("responseTime", logInfo.getResponseTime());
        document.append("duration", logInfo.getDuration());
        document.append("clientType", logInfo.getClientType());
        document.append("method", logInfo.getMethod());
        document.append("url", logInfo.getUrl());
        document.append("responseStatus", logInfo.getResponseStatus());
        document.append("success", logInfo.getSuccess());
        document.append("clientIp", logInfo.getClientIp());
        document.append("userAgent", logInfo.getUserAgent());
        document.append("appName", logInfo.getApplicationName());
        document.append("environment", logInfo.getEnvironment());
        
        // 设置请求头
        if (logInfo.getRequestHeaders() != null) {
            document.append("requestHeaders", new Document(logInfo.getRequestHeaders()));
        }
        
        // 设置请求参数
        if (logInfo.getRequestParams() != null) {
            document.append("requestParams", new Document(logInfo.getRequestParams()));
        }
        
        // 设置请求体
        if (StringUtils.hasText(logInfo.getRequestBody())) {
            document.append("requestBody", logInfo.getRequestBody());
        }
        
        // 设置响应头
        if (logInfo.getResponseHeaders() != null) {
            document.append("responseHeaders", new Document(logInfo.getResponseHeaders()));
        }
        
        // 设置响应体
        if (StringUtils.hasText(logInfo.getResponseBody())) {
            document.append("responseBody", logInfo.getResponseBody());
        }
        
        // 设置错误信息
        if (StringUtils.hasText(logInfo.getErrorMessage())) {
            document.append("errorMessage", logInfo.getErrorMessage());
        }
        
        return document;
    }
}