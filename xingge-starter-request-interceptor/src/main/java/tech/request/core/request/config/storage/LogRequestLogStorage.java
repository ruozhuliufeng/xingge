/*
 * Copyright (c) 2024 行歌(xingge)
 * 日志文件请求日志存储实现
 * 
 * 功能说明：
 * - 实现RequestLogStorage接口的日志文件存储方式
 * - 支持自定义日志格式和级别
 * - 支持批量和异步存储
 */
package tech.request.core.request.config.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.model.RequestLogInfo;
import tech.request.core.request.storage.RequestLogStorage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 日志文件请求日志存储实现
 * 
 * <p>该类实现了RequestLogStorage接口，提供日志文件存储方式：</p>
 * <ul>
 *   <li>支持自定义日志格式</li>
 *   <li>支持自定义日志级别</li>
 *   <li>支持批量存储和异步存储</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   request:
 *     storage-type: LOG
 *     log:
 *       enabled: true
 *       level: INFO
 *       pattern: "[REQUEST-INTERCEPTOR] %s"
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
public class LogRequestLogStorage implements RequestLogStorage {
    
    private static final Logger log = LoggerFactory.getLogger(LogRequestLogStorage.class);
    
    /**
     * 请求拦截器配置属性
     */
    private final RequestInterceptorProperty property;
    
    /**
     * JSON对象映射器
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 异步执行线程池
     */
    private final ExecutorService executorService;
    
    /**
     * 构造函数
     * 
     * @param property 请求拦截器配置属性
     */
    public LogRequestLogStorage(RequestInterceptorProperty property) {
        this.property = property;
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(5);
    }
    
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        if (!isAvailable()) {
            return;
        }
        
        try {
            // 将日志信息转换为JSON字符串
            String logJson = objectMapper.writeValueAsString(logInfo);
            
            // 根据配置的日志格式格式化日志
            String logPattern = property.getLog().getPattern();
            String formattedLog = String.format(logPattern, logJson);
            
            // 根据配置的日志级别输出日志
            String logLevel = property.getLog().getLevel().toUpperCase();
            switch (logLevel) {
                case "DEBUG":
                    log.debug(formattedLog);
                    break;
                case "INFO":
                    log.info(formattedLog);
                    break;
                case "WARN":
                    log.warn(formattedLog);
                    break;
                case "ERROR":
                    log.error(formattedLog);
                    break;
                default:
                    log.info(formattedLog);
            }
        } catch (Exception e) {
            log.error("Failed to store request log to log file: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void batchStore(List<RequestLogInfo> logInfoList) throws Exception {
        if (!isAvailable() || logInfoList == null || logInfoList.isEmpty()) {
            return;
        }
        
        // 逐个存储日志
        for (RequestLogInfo logInfo : logInfoList) {
            store(logInfo);
        }
    }
    
    @Override
    public CompletableFuture<Void> storeAsync(RequestLogInfo logInfo) {
        return CompletableFuture.runAsync(() -> {
            try {
                store(logInfo);
            } catch (Exception e) {
                log.error("Async store request log to log file failed: {}", e.getMessage(), e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList) {
        return CompletableFuture.runAsync(() -> {
            try {
                batchStore(logInfoList);
            } catch (Exception e) {
                log.error("Async batch store request logs to log file failed: {}", e.getMessage(), e);
            }
        }, executorService);
    }
    
    @Override
    public boolean isAvailable() {
        return property.getLog().isEnabled();
    }
    
    @Override
    public String getStorageType() {
        return "LOG";
    }
    
    @Override
    public void destroy() throws Exception {
        executorService.shutdown();
    }
}