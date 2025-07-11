/*
 * Copyright (c) 2024 行歌(xingge)
 * 复合请求日志存储实现
 * 
 * 功能说明：
 * - 支持多种存储类型的组合使用
 * - 可以同时输出到日志和保存到数据库
 * - 提供容错机制，单个存储失败不影响其他存储
 */
package tech.request.core.request.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.storage.RequestLogStorage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 复合请求日志存储实现
 * 
 * <p>该类支持同时使用多种存储实现，例如：</p>
 * <ul>
 *   <li>同时输出到日志文件和MongoDB</li>
 *   <li>同时保存到多个数据库</li>
 *   <li>同时发送到多个消息队列</li>
 * </ul>
 * 
 * <p>特性：</p>
 * <ul>
 *   <li>容错机制：单个存储失败不影响其他存储</li>
 *   <li>异步处理：支持并行执行多个存储操作</li>
 *   <li>统一管理：统一初始化和销毁所有存储实现</li>
 *   <li>灵活配置：可动态添加和移除存储实现</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
public class CompositeRequestLogStorage implements RequestLogStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(CompositeRequestLogStorage.class);
    
    /**
     * 存储实现列表
     */
    private final List<RequestLogStorage> storageList;
    
    /**
     * 异步执行器
     */
    private ExecutorService executorService;
    
    /**
     * 构造函数
     * 
     * @param storageList 存储实现列表
     */
    public CompositeRequestLogStorage(List<RequestLogStorage> storageList) {
        this.storageList = new ArrayList<>(storageList != null ? storageList : new ArrayList<>());
    }
    
    /**
     * 添加存储实现
     * 
     * @param storage 存储实现
     */
    public void addStorage(RequestLogStorage storage) {
        if (storage != null && !storageList.contains(storage)) {
            storageList.add(storage);
            logger.info("添加存储实现: {}", storage.getStorageType());
        }
    }
    
    /**
     * 移除存储实现
     * 
     * @param storage 存储实现
     */
    public void removeStorage(RequestLogStorage storage) {
        if (storage != null && storageList.remove(storage)) {
            logger.info("移除存储实现: {}", storage.getStorageType());
        }
    }
    
    /**
     * 获取存储实现数量
     * 
     * @return 存储实现数量
     */
    public int getStorageCount() {
        return storageList.size();
    }
    
    /**
     * 初始化复合存储服务
     * 
     * @throws Exception 初始化异常
     */
    @PostConstruct
    @Override
    public void initialize() throws Exception {
        try {
            // 初始化异步执行器
            executorService = Executors.newFixedThreadPool(
                Math.max(2, storageList.size()), 
                r -> {
                    Thread thread = new Thread(r, "composite-storage-");
                    thread.setDaemon(true);
                    return thread;
                }
            );
            
            // 初始化所有存储实现
            for (RequestLogStorage storage : storageList) {
                try {
                    storage.initialize();
                    logger.info("存储实现初始化成功: {}", storage.getStorageType());
                } catch (Exception e) {
                    logger.error("存储实现初始化失败: {}", storage.getStorageType(), e);
                    // 继续初始化其他存储实现
                }
            }
            
            logger.info("复合请求日志存储初始化成功，包含 {} 个存储实现", storageList.size());
        } catch (Exception e) {
            logger.error("复合请求日志存储初始化失败", e);
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
        if (logInfo == null) {
            return;
        }
        
        List<Exception> exceptions = new ArrayList<>();
        
        for (RequestLogStorage storage : storageList) {
            try {
                storage.store(logInfo);
            } catch (Exception e) {
                logger.error("存储实现 {} 存储失败: {}", storage.getStorageType(), logInfo.getRequestId(), e);
                exceptions.add(e);
                // 继续执行其他存储实现
            }
        }
        
        // 如果所有存储都失败，抛出异常
        if (!exceptions.isEmpty() && exceptions.size() == storageList.size()) {
            Exception firstException = exceptions.get(0);
            throw new Exception("所有存储实现都失败", firstException);
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
        
        List<Exception> exceptions = new ArrayList<>();
        
        for (RequestLogStorage storage : storageList) {
            try {
                storage.batchStore(logInfoList);
            } catch (Exception e) {
                logger.error("存储实现 {} 批量存储失败，数量: {}", storage.getStorageType(), logInfoList.size(), e);
                exceptions.add(e);
                // 继续执行其他存储实现
            }
        }
        
        // 如果所有存储都失败，抛出异常
        if (!exceptions.isEmpty() && exceptions.size() == storageList.size()) {
            Exception firstException = exceptions.get(0);
            throw new Exception("所有存储实现都失败", firstException);
        }
    }
    
    /**
     * 异步存储单个请求日志
     * 
     * @param logInfo 请求日志信息
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        if (logInfo == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 并行执行所有存储实现
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (RequestLogStorage storage : storageList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    storage.store(logInfo);
                } catch (Exception e) {
                    logger.error("异步存储失败 - 存储类型: {}, 请求ID: {}", 
                            storage.getStorageType(), logInfo.getRequestId(), e);
                }
            }, executorService);
            futures.add(future);
        }
        
        // 等待所有存储完成（忽略异常）
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    logger.error("部分异步存储操作失败", throwable);
                    return null;
                });
    }
    
    /**
     * 异步批量存储请求日志
     * 
     * @param logInfoList 请求日志信息列表
     * @return CompletableFuture对象
     */
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        if (logInfoList == null || logInfoList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        // 并行执行所有存储实现
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (RequestLogStorage storage : storageList) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    storage.batchStore(logInfoList);
                } catch (Exception e) {
                    logger.error("异步批量存储失败 - 存储类型: {}, 数量: {}", 
                            storage.getStorageType(), logInfoList.size(), e);
                }
            }, executorService);
            futures.add(future);
        }
        
        // 等待所有存储完成（忽略异常）
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    logger.error("部分异步批量存储操作失败", throwable);
                    return null;
                });
    }
    
    /**
     * 检查存储服务是否可用
     * 
     * @return true表示至少有一个存储可用，false表示所有存储都不可用
     */
    @Override
    public boolean isAvailable() {
        for (RequestLogStorage storage : storageList) {
            if (storage.isAvailable()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取存储类型名称
     * 
     * @return 存储类型名称
     */
    @Override
    public String getStorageType() {
        if (storageList.isEmpty()) {
            return "COMPOSITE(EMPTY)";
        }
        
        StringBuilder sb = new StringBuilder("COMPOSITE(");
        for (int i = 0; i < storageList.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(storageList.get(i).getStorageType());
        }
        sb.append(")");
        return sb.toString();
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
            // 销毁所有存储实现
            for (RequestLogStorage storage : storageList) {
                try {
                    storage.destroy();
                    logger.info("存储实现销毁成功: {}", storage.getStorageType());
                } catch (Exception e) {
                    logger.error("存储实现销毁失败: {}", storage.getStorageType(), e);
                    // 继续销毁其他存储实现
                }
            }
            
            // 关闭异步执行器
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                logger.info("复合存储异步执行器已关闭");
            }
            
            logger.info("复合请求日志存储销毁成功");
        } catch (Exception e) {
            logger.error("复合请求日志存储销毁失败", e);
            throw e;
        }
    }
}