/*
 * Copyright (c) 2025 xingge
 * 
 * 列映射注解
 * 用于标识实体字段对应的数据库列信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.annotation;

import java.lang.annotation.*;

/**
 * 列映射注解
 * 用于标识实体字段对应的数据库列信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {
    
    /**
     * 列名
     * 如果不指定，则使用字段名的下划线形式
     * 
     * @return 列名
     */
    String name() default "";
    
    /**
     * 列注释
     * 
     * @return 列注释
     */
    String comment() default "";
    
    /**
     * 数据类型
     * 如果不指定，则根据Java类型自动推断
     * 
     * @return 数据类型
     */
    String type() default "";
    
    /**
     * 列长度
     * 对于字符串类型有效
     * 
     * @return 列长度
     */
    int length() default 255;
    
    /**
     * 数值精度
     * 对于数值类型有效
     * 
     * @return 精度
     */
    int precision() default 0;
    
    /**
     * 数值标度
     * 对于数值类型有效
     * 
     * @return 标度
     */
    int scale() default 0;
    
    /**
     * 是否允许为空
     * 
     * @return 是否允许为空
     */
    boolean nullable() default true;
    
    /**
     * 是否唯一
     * 
     * @return 是否唯一
     */
    boolean unique() default false;
    
    /**
     * 默认值
     * 
     * @return 默认值
     */
    String defaultValue() default "";
    
    /**
     * 是否为自增列
     * 
     * @return 是否自增
     */
    boolean autoIncrement() default false;
    
    /**
     * 列定义（完整的列定义，会覆盖其他属性）
     * 
     * @return 列定义
     */
    String columnDefinition() default "";
}