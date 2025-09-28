/*
 * Copyright (c) 2024 行歌(xingge)
 * 审计日志处理器接口
 * 
 * 功能说明：
 * - 定义审计日志处理的统一接口
 * - 支持多种处理方式的扩展
 * - 提供异步和同步处理能力
 */
package tech.msop.core.log.handler;

import tech.msop.core.log.model.AuditLogInfo;

/**
 * 审计日志处理器接口
 * 
 * <p>该接口定义了审计日志处理的统一规范，支持：</p>
 * <ul>
 *   <li>同步和异步处理</li>
 *   <li>批量处理</li>
 *   <li>处理器优先级</li>
 *   <li>条件处理</li>
 * </ul>
 * 
 * <p>实现类需要考虑：</p>
 * <ul>
 *   <li>线程安全</li>
 *   <li>异常处理</li>
 *   <li>性能优化</li>
 *   <li>资源管理</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
public interface AuditLogHandler {
    
    /**
     * 处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @return 处理结果，true表示成功，false表示失败
     */
    boolean handle(AuditLogInfo auditLogInfo);
    
    /**
     * 异步处理审计日志
     * 
     * @param auditLogInfo 审计日志信息
     */
    default void handleAsync(AuditLogInfo auditLogInfo) {
        // 默认实现：在新线程中执行同步处理
        new Thread(() -> {
            try {
                handle(auditLogInfo);
            } catch (Exception e) {
                // 记录处理异常，但不抛出
                System.err.println("审计日志异步处理失败: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 获取处理器名称
     * 
     * @return 处理器名称
     */
    String getHandlerName();
    
    /**
     * 获取处理器优先级
     * 数值越小优先级越高，默认为100
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * 判断是否支持处理指定的审计日志
     * 
     * @param auditLogInfo 审计日志信息
     * @return true表示支持，false表示不支持
     */
    default boolean supports(AuditLogInfo auditLogInfo) {
        return true;
    }
    
    /**
     * 判断处理器是否启用
     * 
     * @return true表示启用，false表示禁用
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * 初始化处理器
     * 在处理器被注册时调用
     */
    default void initialize() {
        // 默认空实现
    }
    
    /**
     * 销毁处理器
     * 在应用关闭时调用，用于清理资源
     */
    default void destroy() {
        // 默认空实现
    }
}