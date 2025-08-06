/*
 * Copyright (c) 2025 xingge
 * 
 * 表元数据类
 * 用于存储数据库表的结构信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * 表元数据类
 * 用于存储数据库表的结构信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableMetadata {
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 模式名
     */
    private String schema;
    
    /**
     * 表注释
     */
    private String comment;
    
    /**
     * 表引擎（MySQL专用）
     */
    private String engine;
    
    /**
     * 字符集（MySQL专用）
     */
    private String charset;
    
    /**
     * 排序规则（MySQL专用）
     */
    private String collate;
    
    /**
     * 列信息列表
     */
    private List<ColumnMetadata> columns = new ArrayList<>();
    
    /**
     * 索引信息列表
     */
    private List<IndexMetadata> indexes = new ArrayList<>();
    
    /**
     * 主键列名列表
     */
    private List<String> primaryKeys = new ArrayList<>();
    
    /**
     * 实体类
     */
    private Class<?> entityClass;
    
    /**
     * 是否启用表结构自动维护
     */
    private boolean autoMaintain;
    
    /**
     * 添加列信息
     * 
     * @param columnMetadata 列元数据
     */
    public void addColumn(ColumnMetadata columnMetadata) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(columnMetadata);
    }
    
    /**
     * 添加索引信息
     * 
     * @param indexMetadata 索引元数据
     */
    public void addIndex(IndexMetadata indexMetadata) {
        if (indexes == null) {
            indexes = new ArrayList<>();
        }
        indexes.add(indexMetadata);
    }
    
    /**
     * 添加主键列
     * 
     * @param columnName 列名
     */
    public void addPrimaryKey(String columnName) {
        if (primaryKeys == null) {
            primaryKeys = new ArrayList<>();
        }
        primaryKeys.add(columnName);
    }
    
    /**
     * 根据列名查找列信息
     * 
     * @param columnName 列名
     * @return 列元数据
     */
    public ColumnMetadata findColumn(String columnName) {
        if (columns == null) {
            return null;
        }
        return columns.stream()
                .filter(column -> column.getColumnName().equals(columnName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据索引名查找索引信息
     * 
     * @param indexName 索引名
     * @return 索引元数据
     */
    public IndexMetadata findIndex(String indexName) {
        if (indexes == null) {
            return null;
        }
        return indexes.stream()
                .filter(index -> index.getIndexName().equals(indexName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否为主键列
     * 
     * @param columnName 列名
     * @return 是否为主键
     */
    public boolean isPrimaryKey(String columnName) {
        return primaryKeys != null && primaryKeys.contains(columnName);
    }
    
    /**
     * 获取模式名
     * 
     * @return 模式名
     */
    public String getSchemaName() {
        return schema;
    }
    
    /**
     * 获取主键列名列表
     * 
     * @return 主键列名列表
     */
    public List<String> getPrimaryKeyColumns() {
        return primaryKeys != null ? primaryKeys : new ArrayList<>();
    }
    
    /**
     * 创建Builder实例
     * 
     * @return TableMetadataBuilder实例
     */
    public static TableMetadataBuilder builder() {
        return new TableMetadataBuilder();
    }
    
    /**
     * TableMetadata的Builder类
     * 用于构建TableMetadata实例
     */
    public static class TableMetadataBuilder {
        private String tableName;
        private String schema;
        private String comment;
        private String engine;
        private String charset;
        private String collate;
        private List<ColumnMetadata> columns = new ArrayList<>();
        private List<IndexMetadata> indexes = new ArrayList<>();
        private List<String> primaryKeys = new ArrayList<>();
        private Class<?> entityClass;
        private boolean autoMaintain;
        
        /**
         * 设置表名
         * 
         * @param tableName 表名
         * @return Builder实例
         */
        public TableMetadataBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        /**
         * 设置模式名
         * 
         * @param schema 模式名
         * @return Builder实例
         */
        public TableMetadataBuilder schema(String schema) {
            this.schema = schema;
            return this;
        }
        
        /**
         * 设置表注释
         * 
         * @param comment 表注释
         * @return Builder实例
         */
        public TableMetadataBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }
        
        /**
         * 设置表引擎
         * 
         * @param engine 表引擎
         * @return Builder实例
         */
        public TableMetadataBuilder engine(String engine) {
            this.engine = engine;
            return this;
        }
        
        /**
         * 设置字符集
         * 
         * @param charset 字符集
         * @return Builder实例
         */
        public TableMetadataBuilder charset(String charset) {
            this.charset = charset;
            return this;
        }
        
        /**
         * 设置排序规则
         * 
         * @param collate 排序规则
         * @return Builder实例
         */
        public TableMetadataBuilder collate(String collate) {
            this.collate = collate;
            return this;
        }
        
        /**
         * 设置实体类
         * 
         * @param entityClass 实体类
         * @return Builder实例
         */
        public TableMetadataBuilder entityClass(Class<?> entityClass) {
            this.entityClass = entityClass;
            return this;
        }
        
        /**
         * 设置是否启用表结构自动维护
         * 
         * @param autoMaintain 是否启用
         * @return Builder实例
         */
        public TableMetadataBuilder autoMaintain(boolean autoMaintain) {
            this.autoMaintain = autoMaintain;
            return this;
        }
        
        /**
         * 添加列信息
         * 
         * @param columnMetadata 列元数据
         * @return Builder实例
         */
        public TableMetadataBuilder addColumn(ColumnMetadata columnMetadata) {
            if (this.columns == null) {
                this.columns = new ArrayList<>();
            }
            this.columns.add(columnMetadata);
            return this;
        }
        
        /**
         * 添加索引信息
         * 
         * @param indexMetadata 索引元数据
         * @return Builder实例
         */
        public TableMetadataBuilder addIndex(IndexMetadata indexMetadata) {
            if (this.indexes == null) {
                this.indexes = new ArrayList<>();
            }
            this.indexes.add(indexMetadata);
            return this;
        }
        
        /**
         * 添加主键列
         * 
         * @param columnName 列名
         * @return Builder实例
         */
        public TableMetadataBuilder addPrimaryKey(String columnName) {
            if (this.primaryKeys == null) {
                this.primaryKeys = new ArrayList<>();
            }
            this.primaryKeys.add(columnName);
            return this;
        }
        
        /**
         * 构建TableMetadata实例
         * 
         * @return TableMetadata实例
         */
        public TableMetadata build() {
            TableMetadata tableMetadata = new TableMetadata();
            tableMetadata.setTableName(this.tableName);
            tableMetadata.setSchema(this.schema);
            tableMetadata.setComment(this.comment);
            tableMetadata.setEngine(this.engine);
            tableMetadata.setCharset(this.charset);
            tableMetadata.setCollate(this.collate);
            tableMetadata.setColumns(this.columns != null ? this.columns : new ArrayList<>());
            tableMetadata.setIndexes(this.indexes != null ? this.indexes : new ArrayList<>());
            tableMetadata.setPrimaryKeys(this.primaryKeys != null ? this.primaryKeys : new ArrayList<>());
            tableMetadata.setEntityClass(this.entityClass);
            tableMetadata.setAutoMaintain(this.autoMaintain);
            return tableMetadata;
        }
    }
}