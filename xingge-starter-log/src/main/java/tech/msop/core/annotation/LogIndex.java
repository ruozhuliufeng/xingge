/*
 * Copyright (c) 2024 行歌(xingge)
 * 日志索引注解
 * 
 * 功能说明：
 * - 标记需要作为日志索引的字段
 * - 支持自定义索引名称和前缀
 * - 用于MDC日志上下文管理
 */
package tech.msop.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志索引注解
 * 
 * <p>该注解用于标记RequestLogInfo中需要作为日志索引的字段，
 * 当字段被标记后，会自动将字段值添加到MDC中，便于日志追踪和检索。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code
 * @LogIndex(name = "requestId", prefix = "REQ_")
 * private String requestId;
 * 
 * @LogIndex(name = "clientType")
 * private String clientType;
 * }
 * </pre>
 * 
 * @author 若竹流风
 * @version 0.0.2
 * @since 2025-07-11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogIndex {
    
    /**
     * 索引名称
     * 
     * <p>如果不指定，则使用字段名作为索引名称</p>
     * 
     * @return 索引名称
     */
    String name() default "";
    
    /**
     * 索引前缀
     * 
     * <p>最终的MDC键名为：prefix + name</p>
     * 
     * @return 索引前缀
     */
    String prefix() default "";
    
    /**
     * 是否启用该索引
     * 
     * <p>可以通过此属性动态控制索引的启用状态</p>
     * 
     * @return true表示启用，false表示禁用
     */
    boolean enabled() default true;
    
    /**
     * 索引描述
     * 
     * <p>用于文档说明，不影响功能</p>
     * 
     * @return 索引描述
     */
    String description() default "";
}