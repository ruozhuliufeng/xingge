/*
 * Copyright (c) 2024 行歌(xingge)
 * MongoDB请求日志存储实现
 * 
 * 功能说明：
 * - 实现MongoDB存储方式的请求日志记录
 * - 支持自定义MongoDB连接地址和集合名称
 * - 支持批量存储和异步存储
 * - 提供连接状态检查和资源管理
 */
package tech.request.core.request.storage.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.StringUtils;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.storage.RequestLogStorage;
import tech.msop.core.tool.async.AsyncProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MongoDB请求日志存储实现类
 * 
 * <p>该类实现了MongoDB存储方式的请求日志记录功能，支持：</p>
 * <ul>
 *   <li>自定义MongoDB连接地址配置</li>
 *   <li>自定义集合名称，默认为t_request_interceptor_log</li>
 *   <li>批量存储优化性能</li>
 *   <li>异步存储避免阻塞主线程</li>
 *   <li>连接状态检查和异常处理</li>
 *   <li>资源自动管理和释放</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   request:
 *     storage-type: MONGO
 *     mongo:
 *       enabled: true
 *       uri: mongodb://localhost:27017
 *       database-name: logs
 *       collection-name: t_request_interceptor_log
 *       batch-size: 100
 * </pre>
 * 
 * @author 若竹流风
 * @version 0.0.4
 * @since 2025-07-11
 */
@ConditionalOnProperty(name = "xg.request.mongo.enabled", havingValue = "true")
public class MongoRequestLogStorage implements RequestLogStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoRequestLogStorage.class);
    
    /**
     * 请求拦截器配置属性
     */
    @Autowired
    private RequestInterceptorProperty properties;
    
    /**
     * Spring MongoDB模板（可选，如果项目中已配置）
     */
    @Autowired(required = false)
    private MongoTemplate mongoTemplate;
    
    /**
     * 自定义MongoDB客户端（当配置了自定义URI时使用）
     */
    private MongoClient customMongoClient;
    
    /**
     * MongoDB数据库对象
     */
    private MongoDatabase database;
    
    /**
     * MongoDB集合对象
     */
    private MongoCollection<Document> collection;
    
    /**
     * 异步处理器
     */
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 初始化MongoDB连接和集合
     * 
     * @throws Exception 初始化异常
     */
    @PostConstruct
    @Override
    public void initialize() throws Exception {
        try {
            RequestInterceptorProperty.MongoConfig mongoConfig = properties.getMongo();
            
            // 检查是否配置了自定义MongoDB URI
            if (StringUtils.hasText(mongoConfig.getUri())) {
                // 使用自定义URI创建MongoDB客户端
                logger.info("使用自定义MongoDB URI: {}", mongoConfig.getUri());
                customMongoClient = MongoClients.create(mongoConfig.getUri());
                database = customMongoClient.getDatabase(mongoConfig.getDatabaseName());
            } else if (mongoTemplate != null) {
                // 使用项目默认的MongoDB配置
                logger.info("使用项目默认MongoDB配置");
                database = mongoTemplate.getDb();
            } else {
                throw new IllegalStateException("未找到MongoDB配置，请配置mongo.uri或确保项目中已配置MongoTemplate");
            }
            
            // 获取集合
            collection = database.getCollection(mongoConfig.getCollectionName());
            
            logger.info("MongoDB请求日志存储初始化成功，数据库: {}, 集合: {}", 
                    database.getName(), mongoConfig.getCollectionName());
                    
        } catch (Exception e) {
            logger.error("MongoDB请求日志存储初始化失败", e);
            throw e;
        }
    }
    
    /**
     * 存储单个请求日志
     * 
     * @param logInfo 请求日志信息
     * @throws Exception 存储异常
     */
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        try {
            Document document = convertToDocument(logInfo);
            collection.insertOne(document);
            logger.debug("成功存储请求日志到MongoDB: {}", logInfo.getRequestId());
        } catch (Exception e) {
            logger.error("存储请求日志到MongoDB失败: {}", logInfo.getRequestId(), e);
            throw e;
        }
    }
    
    /**
     * 批量存储请求日志
     * 
     * @param logInfoList 请求日志信息列表
     * @throws Exception 存储异常
     */
    @Override
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        try {
            List<Document> documents = new ArrayList<>(logInfoList.size());
            for (RequestLogInfo logInfo : logInfoList) {
                documents.add(convertToDocument(logInfo));
            }
            
            collection.insertMany(documents);
            logger.debug("成功批量存储{}条请求日志到MongoDB", logInfoList.size());
        } catch (Exception e) {
            logger.error("批量存储请求日志到MongoDB失败，数量: {}", logInfoList.size(), e);
            throw e;
        }
    }
    
    /**
     * 异步存储单个请求日志
     * 
     * <p>默认为异步输出，不阻碍现有业务流程，所有异常通过日志输出</p>
     * 
     * @param logInfo 请求日志信息
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return asyncProcessor.executeAsyncWithResult(() -> {
            try {
                store(logInfo);
                return null;
            } catch (Exception e) {
                // 异常通过日志输出，不抛出异常以避免阻碍业务流程
                logger.error("异步存储请求日志到MongoDB失败: {}", logInfo.getRequestId(), e);
                return null;
            }
        });
    }
    
    /**
     * 异步批量存储请求日志
     * 
     * <p>默认为异步输出，不阻碍现有业务流程，所有异常通过日志输出</p>
     * 
     * @param logInfoList 请求日志信息列表
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return asyncProcessor.executeAsyncWithResult(() -> {
            try {
                batchStore(logInfoList);
                return null;
            } catch (Exception e) {
                // 异常通过日志输出，不抛出异常以避免阻碍业务流程
                logger.error("异步批量存储请求日志到MongoDB失败，数量: {}", 
                        logInfoList != null ? logInfoList.size() : 0, e);
                return null;
            }
        });
    }
    
    /**
     * 检查MongoDB存储服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    @Override
    public boolean isAvailable() {
        try {
            if (database != null) {
                // 尝试执行一个简单的操作来检查连接状态
                database.runCommand(new Document("ping", 1));
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.warn("MongoDB连接检查失败", e);
            return false;
        }
    }
    
    /**
     * 获取存储类型名称
     * 
     * @return 存储类型名称
     */
    @Override
    public String getStorageType() {
        return "MONGO";
    }
    
    /**
     * 销毁存储服务，释放资源
     * 
     * @throws Exception 销毁异常
     */
    @PreDestroy
    @Override
    public void destroy() throws Exception {
        try {
            // 关闭自定义MongoDB客户端
            if (customMongoClient != null) {
                customMongoClient.close();
                logger.info("自定义MongoDB客户端已关闭");
            }
            logger.info("MongoDB请求日志存储服务已销毁");
        } catch (Exception e) {
            logger.error("销毁MongoDB请求日志存储服务失败", e);
            throw e;
        }
    }
    
    /**
     * 将请求日志信息转换为MongoDB文档
     * 
     * @param logInfo 请求日志信息
     * @return MongoDB文档
     */
    private Document convertToDocument(RequestLogInfo logInfo) {
        Document document = new Document();
        
        // 基本信息
        document.append("requestId", logInfo.getRequestId())
                .append("url", logInfo.getUrl())
                .append("method", logInfo.getMethod())
                .append("clientType", logInfo.getClientType())
                .append("startTime", logInfo.getRequestTime() != null ? 
                        logInfo.getRequestTime().format(DATE_TIME_FORMATTER) : null)
                .append("endTime", logInfo.getResponseTime() != null ?
                        logInfo.getResponseTime().format(DATE_TIME_FORMATTER) : null)
                .append("duration", logInfo.getDuration())
                .append("success", logInfo.getSuccess())
                .append("errorMessage", logInfo.getErrorMessage())
                .append("createTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        
        // 请求信息
        if (logInfo.getRequestHeaders() != null) {
            document.append("requestHeaders", logInfo.getRequestHeaders());
        }
        if (logInfo.getRequestBody() != null) {
            document.append("requestBody", logInfo.getRequestBody());
        }
        
        // 响应信息
        document.append("responseStatus", logInfo.getResponseStatus());
        if (logInfo.getResponseHeaders() != null) {
            document.append("responseHeaders", logInfo.getResponseHeaders());
        }
        if (logInfo.getResponseBody() != null) {
            document.append("responseBody", logInfo.getResponseBody());
        }
        
        return document;
    }
}