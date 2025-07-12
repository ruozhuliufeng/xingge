/*
 * Copyright (c) 2025 xingge
 * 
 * 列元数据类
 * 用于存储数据库列的详细信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.metadata;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.msop.core.db.annotation.Id;

import java.lang.reflect.Field;

/**
 * 列元数据类
 * 用于存储数据库列的详细信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadata {
    
    /**
     * 列名
     */
    private String columnName;
    
    /**
     * 列注释
     */
    private String comment;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 列长度
     */
    private int length;
    
    /**
     * 数值精度
     */
    private int precision;
    
    /**
     * 数值标度
     */
    private int scale;
    
    /**
     * 是否允许为空
     */
    private boolean nullable;
    
    /**
     * 是否唯一
     */
    private boolean unique;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 是否为自增列
     */
    private boolean autoIncrement;
    
    /**
     * 是否为主键
     */
    private boolean primaryKey;
    
    /**
     * 主键生成策略
     */
    private Id.GenerationType generationType;
    
    /**
     * 序列名称（Oracle、PostgreSQL等数据库使用）
     */
    private String sequenceName;
    
    /**
     * 列定义（完整的列定义，会覆盖其他属性）
     */
    private String columnDefinition;
    
    /**
     * Java字段类型
     */
    private Class<?> javaType;
    
    /**
     * Java字段
     */
    private Field field;
    
    /**
     * 字段在实体类中的名称
     */
    private String fieldName;
    
    /**
     * 检查是否有自定义列定义
     * 
     * @return 是否有自定义列定义
     */
    public boolean hasColumnDefinition() {
        return columnDefinition != null && !columnDefinition.trim().isEmpty();
    }
    
    /**
     * 检查是否有默认值
     * 
     * @return 是否有默认值
     */
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.trim().isEmpty();
    }
    
    /**
     * 检查是否有注释
     * 
     * @return 是否有注释
     */
    public boolean hasComment() {
        return comment != null && !comment.trim().isEmpty();
    }
    
    /**
     * 获取完整的列定义信息
     * 
     * @return 列定义信息字符串
     */
    public String getFullDefinition() {
        StringBuilder sb = new StringBuilder();
        sb.append("Column: ").append(columnName);
        sb.append(", Type: ").append(dataType);
        if (length > 0) {
            sb.append("(").append(length);
            if (scale > 0) {
                sb.append(",").append(scale);
            }
            sb.append(")");
        } else if (precision > 0) {
            sb.append("(").append(precision);
            if (scale > 0) {
                sb.append(",").append(scale);
            }
            sb.append(")");
        }
        sb.append(", Nullable: ").append(nullable);
        sb.append(", PrimaryKey: ").append(primaryKey);
        sb.append(", AutoIncrement: ").append(autoIncrement);
        if (hasDefaultValue()) {
            sb.append(", Default: ").append(defaultValue);
        }
        if (hasComment()) {
            sb.append(", Comment: ").append(comment);
        }
        return sb.toString();
    }
}