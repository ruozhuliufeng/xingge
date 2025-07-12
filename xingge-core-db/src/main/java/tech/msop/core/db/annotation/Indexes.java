/*
 * Copyright (c) 2025 xingge
 * 
 * 多索引注解容器
 * 用于支持在一个实体类上定义多个索引
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.annotation;

import java.lang.annotation.*;

/**
 * 多索引注解容器
 * 用于支持在一个实体类上定义多个索引
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Indexes {
    
    /**
     * 索引数组
     * 
     * @return 索引注解数组
     */
    Index[] value();
}