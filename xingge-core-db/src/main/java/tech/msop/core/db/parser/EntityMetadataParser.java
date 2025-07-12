/*
 * Copyright (c) 2025 xingge
 * 
 * 实体元数据解析器
 * 用于解析实体类上的注解并生成表元数据
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.parser;

import tech.msop.core.db.annotation.*;
import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体元数据解析器
 * 用于解析实体类上的注解并生成表元数据
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Slf4j
public class EntityMetadataParser {
    
    /**
     * 解析实体类，生成表元数据
     * 
     * @param entityClass 实体类
     * @return 表元数据
     */
    public static TableMetadata parseEntity(Class<?> entityClass) {
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class cannot be null");
        }
        
        log.debug("Parsing entity class: {}", entityClass.getName());
        
        // 检查是否有@Table注解
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            log.warn("Entity class {} does not have @Table annotation, skipping", entityClass.getName());
            return null;
        }
        
        // 构建表元数据
        TableMetadata.TableMetadataBuilder tableBuilder = TableMetadata.builder()
                .entityClass(entityClass)
                .autoMaintain(tableAnnotation.autoMaintain());
        
        // 设置表名
        String tableName = tableAnnotation.name();
        if (tableName.isEmpty()) {
            tableName = convertCamelCaseToSnakeCase(entityClass.getSimpleName());
        }
        tableBuilder.tableName(tableName);
        
        // 设置其他表属性
        if (!tableAnnotation.schema().isEmpty()) {
            tableBuilder.schema(tableAnnotation.schema());
        }
        if (!tableAnnotation.comment().isEmpty()) {
            tableBuilder.comment(tableAnnotation.comment());
        }
        if (!tableAnnotation.engine().isEmpty()) {
            tableBuilder.engine(tableAnnotation.engine());
        }
        if (!tableAnnotation.charset().isEmpty()) {
            tableBuilder.charset(tableAnnotation.charset());
        }
        if (!tableAnnotation.collate().isEmpty()) {
            tableBuilder.collate(tableAnnotation.collate());
        }
        
        // 解析字段
        parseFields(entityClass, tableBuilder);
        
        // 解析索引
        parseIndexes(entityClass, tableBuilder);
        
        TableMetadata tableMetadata = tableBuilder.build();
        log.debug("Parsed table metadata for {}: {}", entityClass.getName(), tableMetadata.getTableName());
        
        return tableMetadata;
    }
    
    /**
     * 解析实体类字段
     * 
     * @param entityClass 实体类
     * @param tableBuilder 表元数据构建器
     */
    private static void parseFields(Class<?> entityClass, TableMetadata.TableMetadataBuilder tableBuilder) {
        Field[] fields = entityClass.getDeclaredFields();
        
        for (Field field : fields) {
            // 跳过静态字段和transient字段
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            
            // 检查是否有@Column注解或@Id注解
            Column columnAnnotation = field.getAnnotation(Column.class);
            Id idAnnotation = field.getAnnotation(Id.class);
            
            // 如果既没有@Column也没有@Id注解，跳过
            if (columnAnnotation == null && idAnnotation == null) {
                continue;
            }
            
            ColumnMetadata columnMetadata = parseField(field, columnAnnotation, idAnnotation);
            tableBuilder.addColumn(columnMetadata);
            
            // 如果是主键，添加到主键列表
            if (columnMetadata.isPrimaryKey()) {
                tableBuilder.addPrimaryKey(columnMetadata.getColumnName());
            }
        }
    }
    
    /**
     * 解析单个字段
     * 
     * @param field 字段
     * @param columnAnnotation @Column注解
     * @param idAnnotation @Id注解
     * @return 列元数据
     */
    private static ColumnMetadata parseField(Field field, Column columnAnnotation, Id idAnnotation) {
        ColumnMetadata.ColumnMetadataBuilder columnBuilder = ColumnMetadata.builder()
                .field(field)
                .fieldName(field.getName())
                .javaType(field.getType());
        
        // 设置列名
        String columnName;
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            columnName = columnAnnotation.name();
        } else {
            columnName = convertCamelCaseToSnakeCase(field.getName());
        }
        columnBuilder.columnName(columnName);
        
        // 处理@Id注解
        if (idAnnotation != null) {
            columnBuilder.primaryKey(true)
                    .generationType(idAnnotation.strategy());
            
            if (!idAnnotation.sequenceName().isEmpty()) {
                columnBuilder.sequenceName(idAnnotation.sequenceName());
            }
            
            // 如果是自增策略，设置自增
            if (idAnnotation.strategy() == Id.GenerationType.IDENTITY) {
                columnBuilder.autoIncrement(true);
            }
        }
        
        // 处理@Column注解
        if (columnAnnotation != null) {
            if (!columnAnnotation.comment().isEmpty()) {
                columnBuilder.comment(columnAnnotation.comment());
            }
            if (!columnAnnotation.type().isEmpty()) {
                columnBuilder.dataType(columnAnnotation.type());
            }
            if (columnAnnotation.length() > 0) {
                columnBuilder.length(columnAnnotation.length());
            }
            if (columnAnnotation.precision() > 0) {
                columnBuilder.precision(columnAnnotation.precision());
            }
            if (columnAnnotation.scale() > 0) {
                columnBuilder.scale(columnAnnotation.scale());
            }
            
            columnBuilder.nullable(columnAnnotation.nullable())
                    .unique(columnAnnotation.unique())
                    .autoIncrement(columnAnnotation.autoIncrement());
            
            if (!columnAnnotation.defaultValue().isEmpty()) {
                columnBuilder.defaultValue(columnAnnotation.defaultValue());
            }
            if (!columnAnnotation.columnDefinition().isEmpty()) {
                columnBuilder.columnDefinition(columnAnnotation.columnDefinition());
            }
        } else {
            // 如果没有@Column注解，使用默认值
            columnBuilder.nullable(true);
        }
        
        return columnBuilder.build();
    }
    
    /**
     * 解析索引注解
     * 
     * @param entityClass 实体类
     * @param tableBuilder 表元数据构建器
     */
    private static void parseIndexes(Class<?> entityClass, TableMetadata.TableMetadataBuilder tableBuilder) {
        // 解析@Index注解
        Index indexAnnotation = entityClass.getAnnotation(Index.class);
        if (indexAnnotation != null) {
            IndexMetadata indexMetadata = parseIndex(indexAnnotation);
            tableBuilder.addIndex(indexMetadata);
        }
        
        // 解析@Indexes注解
        Indexes indexesAnnotation = entityClass.getAnnotation(Indexes.class);
        if (indexesAnnotation != null) {
            for (Index index : indexesAnnotation.value()) {
                IndexMetadata indexMetadata = parseIndex(index);
                tableBuilder.addIndex(indexMetadata);
            }
        }
    }
    
    /**
     * 解析单个索引注解
     * 
     * @param indexAnnotation 索引注解
     * @return 索引元数据
     */
    private static IndexMetadata parseIndex(Index indexAnnotation) {
        IndexMetadata.IndexMetadataBuilder indexBuilder = IndexMetadata.builder()
                .indexName(indexAnnotation.name())
                .unique(indexAnnotation.unique())
                .indexType(indexAnnotation.type());
        
        if (!indexAnnotation.comment().isEmpty()) {
            indexBuilder.comment(indexAnnotation.comment());
        }
        
        // 添加索引列
        for (String columnName : indexAnnotation.columnList()) {
            indexBuilder.addColumn(columnName);
        }
        
        return indexBuilder.build();
    }
    
    /**
     * 将驼峰命名转换为下划线命名
     * 
     * @param camelCase 驼峰命名字符串
     * @return 下划线命名字符串
     */
    private static String convertCamelCaseToSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        char[] chars = camelCase.toCharArray();
        
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
    
    /**
     * 检查实体类是否需要自动维护表结构
     * 
     * @param entityClass 实体类
     * @return 是否需要自动维护
     */
    public static boolean shouldAutoMaintain(Class<?> entityClass) {
        if (entityClass == null) {
            return false;
        }
        
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        return tableAnnotation != null && tableAnnotation.autoMaintain();
    }
    
    /**
     * 获取实体类对应的表名
     * 
     * @param entityClass 实体类
     * @return 表名
     */
    public static String getTableName(Class<?> entityClass) {
        if (entityClass == null) {
            return null;
        }
        
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            return null;
        }
        
        String tableName = tableAnnotation.name();
        if (tableName.isEmpty()) {
            tableName = convertCamelCaseToSnakeCase(entityClass.getSimpleName());
        }
        
        return tableName;
    }
}