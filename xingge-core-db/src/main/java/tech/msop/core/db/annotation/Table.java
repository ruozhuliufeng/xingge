/*
 * Copyright (c) 2025 xingge
 * 
 * 表映射注解
 * 用于标识实体类对应的数据库表信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.annotation;

import java.lang.annotation.*;

/**
 * 表映射注解
 * 用于标识实体类对应的数据库表信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table {
    
    /**
     * 表名
     * 如果不指定，则使用类名的下划线形式
     * 
     * @return 表名
     */
    String name() default "";
    
    /**
     * 表注释
     * 
     * @return 表注释
     */
    String comment() default "";
    
    /**
     * 数据库schema
     * 
     * @return schema名称
     */
    String schema() default "";
    
    /**
     * 表引擎（MySQL专用）
     * 
     * @return 表引擎
     */
    String engine() default "InnoDB";
    
    /**
     * 字符集（MySQL专用）
     * 
     * @return 字符集
     */
    String charset() default "utf8mb4";
    
    /**
     * 排序规则（MySQL专用）
     * 
     * @return 排序规则
     */
    String collate() default "utf8mb4_unicode_ci";
    
    /**
     * 是否启用表结构自动维护
     * 
     * @return 是否启用
     */
    boolean autoMaintain() default true;
}