/*
 * Copyright (c) 2024 行歌(xingge)
 * 审计日志注解
 * 
 * 功能说明：
 * - 标记需要记录审计日志的方法
 * - 支持自定义操作类型、模块、描述等
 * - 支持多种输出方式配置
 */
package tech.msop.core.log.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 
 * <p>用于标记需要记录审计日志的方法，支持以下功能：</p>
 * <ul>
 *   <li>自定义操作类型和模块</li>
 *   <li>记录操作描述和详细信息</li>
 *   <li>支持多种输出方式</li>
 *   <li>支持条件启用</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * &#64;AuditLog(
 *     operation = "USER_LOGIN",
 *     module = "用户管理",
 *     description = "用户登录操作",
 *     includeArgs = true,
 *     includeResult = false
 * )
 * public LoginResult login(LoginRequest request) {
 *     // 业务逻辑
 * }
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    /**
     * 操作类型
     * 用于标识具体的操作类型，如：USER_LOGIN、DATA_UPDATE等
     * 
     * @return 操作类型
     */
    String operation() default "";
    
    /**
     * 业务模块
     * 用于标识操作所属的业务模块，如：用户管理、订单管理等
     * 
     * @return 业务模块
     */
    String module() default "";
    
    /**
     * 操作描述
     * 对操作的详细描述，支持SpEL表达式
     * 
     * @return 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录方法参数
     * 默认记录
     * 
     * @return 是否记录参数
     */
    boolean includeArgs() default true;
    
    /**
     * 是否记录方法返回值
     * 默认不记录（避免敏感信息泄露）
     * 
     * @return 是否记录返回值
     */
    boolean includeResult() default false;
    
    /**
     * 是否记录异常信息
     * 默认记录
     * 
     * @return 是否记录异常
     */
    boolean includeException() default true;
    
    /**
     * 是否启用该审计日志
     * 默认启用，可用于条件性控制
     * 
     * @return 是否启用
     */
    boolean enabled() default true;
    
    /**
     * 自定义标签
     * 用于对审计日志进行分类和过滤
     * 
     * @return 标签数组
     */
    String[] tags() default {};
    
    /**
     * 优先级
     * 用于标识操作的重要程度：1-低，2-中，3-高，4-紧急
     * 
     * @return 优先级
     */
    int priority() default 2;
    
    /**
     * 是否异步处理
     * 默认异步处理以提高性能
     * 
     * @return 是否异步
     */
    boolean async() default true;
}