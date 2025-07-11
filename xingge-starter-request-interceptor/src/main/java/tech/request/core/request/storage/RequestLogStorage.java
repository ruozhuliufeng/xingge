/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志存储接口
 * 
 * 功能说明：
 * - 定义请求日志存储的统一接口
 * - 支持多种存储实现方式
 * - 提供异步和同步存储方法
 */
package tech.request.core.request.storage;

import tech.request.core.request.model.RequestLogInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 请求日志存储接口
 * 
 * <p>该接口定义了请求日志存储的统一规范，支持多种存储方式：</p>
 * <ul>
 *   <li>日志文件存储</li>
 *   <li>数据库存储</li>
 *   <li>MongoDB存储</li>
 *   <li>消息队列存储（Kafka、RocketMQ）</li>
 *   <li>自定义接口存储</li>
 * </ul>
 * 
 * <p>实现类需要考虑以下因素：</p>
 * <ul>
 *   <li>性能优化（批量处理、异步处理）</li>
 *   <li>异常处理和重试机制</li>
 *   <li>资源管理和连接池</li>
 *   <li>配置参数的灵活性</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
public interface RequestLogStorage {
    
    /**
     * 存储单个请求日志
     * 
     * @param logInfo 请求日志信息
     * @throws Exception 存储异常
     */
    void store(RequestLogInfo logInfo) throws Exception;
    
    /**
     * 批量存储请求日志
     * 
     * @param logInfoList 请求日志信息列表
     * @throws Exception 存储异常
     */
    void batchStore(List<RequestLogInfo> logInfoList) throws Exception;
    
    /**
     * 异步存储单个请求日志
     * 
     * @param logInfo 请求日志信息
     * @return CompletableFuture对象
     */
    CompletableFuture<Void> storeAsync(RequestLogInfo logInfo);
    
    /**
     * 异步批量存储请求日志
     * 
     * @param logInfoList 请求日志信息列表
     * @return CompletableFuture对象
     */
    CompletableFuture<Void> batchStoreAsync(List<RequestLogInfo> logInfoList);
    
    /**
     * 检查存储服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    boolean isAvailable();
    
    /**
     * 获取存储类型名称
     * 
     * @return 存储类型名称
     */
    String getStorageType();
    
    /**
     * 初始化存储服务
     * 
     * @throws Exception 初始化异常
     */
    default void initialize() throws Exception {
        // 默认空实现，子类可以重写
    }
    
    /**
     * 销毁存储服务，释放资源
     * 
     * @throws Exception 销毁异常
     */
    default void destroy() throws Exception {
        // 默认空实现，子类可以重写
    }
}