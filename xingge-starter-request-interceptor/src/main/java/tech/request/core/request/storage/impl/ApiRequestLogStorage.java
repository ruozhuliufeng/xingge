/*
 * Copyright (c) 2024 行歌(xingge)
 * API请求日志存储实现
 * 
 * 功能说明：
 * - 通过Feign接口将请求日志发送到指定API
 * - 支持自定义API端点配置
 * - 提供重试机制和异常处理
 */
package tech.request.core.request.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.storage.RequestLogStorage;
import tech.request.core.request.feign.RequestLogApiClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * API请求日志存储实现
 * 
 * <p>该实现通过Feign客户端将请求日志发送到指定的API端点，
 * 支持异步发送和批量发送功能。</p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *   <li>基于Feign的HTTP客户端调用</li>
 *   <li>支持异步和批量处理</li>
 *   <li>提供重试机制</li>
 *   <li>异常容错处理</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Component
@ConditionalOnProperty(name = "xg.request.api.enabled", havingValue = "true")
public class ApiRequestLogStorage implements RequestLogStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiRequestLogStorage.class);
    
    /**
     * 请求拦截器配置属性
     */
    @Autowired
    private RequestInterceptorProperty properties;
    
    /**
     * 请求日志API客户端
     */
    @Autowired(required = false)
    private RequestLogApiClient requestLogApiClient;
    
    /**
     * 异步执行器
     */
    private ExecutorService executorService;
    
    /**
     * 初始化方法
     */
    @PostConstruct
    public void initialize() {
        // 创建异步执行器
        int threadPoolSize = properties.getThreadPoolSize();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread thread = new Thread(r, "api-request-log-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
        
        logger.info("API请求日志存储初始化完成，API端点: {}, 线程池大小: {}", 
                   properties.getApi().getUrl(), threadPoolSize);
    }
    
    /**
     * 销毁方法
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("API请求日志存储已销毁");
    }
    
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        if (requestLogApiClient == null) {
            logger.warn("RequestLogApiClient未配置，跳过API存储");
            return;
        }
        
        try {
            requestLogApiClient.saveRequestLog(logInfo);
            logger.debug("成功发送请求日志到API: {}", logInfo.getRequestId());
        } catch (Exception e) {
            logger.error("发送请求日志到API失败: {}", logInfo.getRequestId(), e);
            throw e;
        }
    }
    
    @Override
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        if (requestLogApiClient == null) {
            logger.warn("RequestLogApiClient未配置，跳过API批量存储");
            return;
        }
        
        try {
            requestLogApiClient.batchSaveRequestLog(logInfoList);
            logger.debug("成功批量发送{}条请求日志到API", logInfoList.size());
        } catch (Exception e) {
            logger.error("批量发送请求日志到API失败，数量: {}", logInfoList.size(), e);
            throw e;
        }
    }
    
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                store(logInfo);
            } catch (Exception e) {
                logger.error("异步发送请求日志到API失败: {}", logInfo.getRequestId(), e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return CompletableFuture.runAsync(() -> {
            try {
                batchStore(logInfoList);
            } catch (Exception e) {
                logger.error("异步批量发送请求日志到API失败，数量: {}", 
                           logInfoList != null ? logInfoList.size() : 0, e);
            }
        }, executorService);
    }
    
    @Override
    public boolean isAvailable() {
        return requestLogApiClient != null && properties.getApi().isEnabled();
    }
    
    @Override
    public String getStorageType() {
        return "API";
    }
}