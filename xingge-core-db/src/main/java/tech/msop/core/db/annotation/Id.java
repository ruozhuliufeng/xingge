/*
 * Copyright (c) 2025 xingge
 * 
 * 主键注解
 * 用于标识实体类的主键字段
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.annotation;

import java.lang.annotation.*;

/**
 * 主键注解
 * 用于标识实体类的主键字段
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {
    
    /**
     * 主键生成策略
     * 
     * @return 生成策略
     */
    GenerationType strategy() default GenerationType.AUTO;
    
    /**
     * 序列名称（Oracle、PostgreSQL等数据库使用）
     * 
     * @return 序列名称
     */
    String sequenceName() default "";
    
    /**
     * 主键生成策略枚举
     */
    enum GenerationType {
        /**
         * 自动选择策略
         */
        AUTO,
        
        /**
         * 自增主键
         */
        IDENTITY,
        
        /**
         * 序列生成
         */
        SEQUENCE,
        
        /**
         * 手动赋值
         */
        ASSIGNED,
        
        /**
         * UUID生成
         */
        UUID
    }
}