/*
 * Copyright (c) 2025 xingge
 * 
 * 索引注解
 * 用于定义数据库表的索引信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.annotation;

import java.lang.annotation.*;

/**
 * 索引注解
 * 用于定义数据库表的索引信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Indexes.class)
public @interface Index {
    
    /**
     * 索引名称
     * 如果不指定，则自动生成
     * 
     * @return 索引名称
     */
    String name() default "";
    
    /**
     * 索引列名
     * 当注解在类上时必须指定
     * 
     * @return 索引列名数组
     */
    String[] columnList() default {};
    
    /**
     * 是否唯一索引
     * 
     * @return 是否唯一
     */
    boolean unique() default false;
    
    /**
     * 索引类型
     * 
     * @return 索引类型
     */
    IndexType type() default IndexType.BTREE;
    
    /**
     * 索引注释
     * 
     * @return 索引注释
     */
    String comment() default "";
    
    /**
     * 索引类型枚举
     */
    enum IndexType {
        /**
         * B树索引（默认）
         */
        BTREE,
        
        /**
         * 哈希索引
         */
        HASH,
        
        /**
         * 全文索引
         */
        FULLTEXT,
        
        /**
         * 空间索引
         */
        SPATIAL
    }
}