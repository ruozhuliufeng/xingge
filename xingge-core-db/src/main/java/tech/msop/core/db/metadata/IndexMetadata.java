/*
 * Copyright (c) 2025 xingge
 * 
 * 索引元数据类
 * 用于存储数据库索引的详细信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.metadata;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import tech.msop.core.db.annotation.Index;

import java.util.List;
import java.util.ArrayList;

/**
 * 索引元数据类
 * 用于存储数据库索引的详细信息
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexMetadata {
    
    /**
     * 自定义 Builder 类，添加 addColumn 方法
     */
    public static class IndexMetadataBuilder {
        private List<String> columnNames;
        private String indexName;
        private boolean unique;
        private Index.IndexType indexType;
        private String comment;
        private String tableName;
        
        /**
         * 添加索引列
         * 
         * @param columnName 列名
         * @return Builder 实例
         */
        public IndexMetadataBuilder addColumn(String columnName) {
            if (this.columnNames == null) {
                this.columnNames = new ArrayList<>();
            }
            this.columnNames.add(columnName);
            return this;
        }
        
        public IndexMetadataBuilder indexName(String indexName) {
            this.indexName = indexName;
            return this;
        }
        
        public IndexMetadataBuilder columnNames(List<String> columnNames) {
            this.columnNames = columnNames;
            return this;
        }
        
        public IndexMetadataBuilder unique(boolean unique) {
            this.unique = unique;
            return this;
        }
        
        public IndexMetadataBuilder indexType(Index.IndexType indexType) {
            this.indexType = indexType;
            return this;
        }
        
        public IndexMetadataBuilder comment(String comment) {
            this.comment = comment;
            return this;
        }
        
        public IndexMetadataBuilder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }
        
        public IndexMetadata build() {
            IndexMetadata metadata = new IndexMetadata();
            metadata.indexName = this.indexName;
            metadata.columnNames = this.columnNames != null ? this.columnNames : new ArrayList<>();
            metadata.unique = this.unique;
            metadata.indexType = this.indexType;
            metadata.comment = this.comment;
            metadata.tableName = this.tableName;
            return metadata;
        }
    }
    
    /**
     * 索引名称
     */
    private String indexName;
    
    /**
     * 索引列名列表
     */
    @Builder.Default
    private List<String> columnNames = new ArrayList<>();
    
    /**
     * 是否唯一索引
     */
    private boolean unique;
    
    /**
     * 索引类型
     */
    private Index.IndexType indexType;
    
    /**
     * 索引注释
     */
    private String comment;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 添加索引列
     * 
     * @param columnName 列名
     */
    public void addColumn(String columnName) {
        if (columnNames == null) {
            columnNames = new ArrayList<>();
        }
        columnNames.add(columnName);
    }
    
    /**
     * 检查是否包含指定列
     * 
     * @param columnName 列名
     * @return 是否包含
     */
    public boolean containsColumn(String columnName) {
        return columnNames != null && columnNames.contains(columnName);
    }
    
    /**
     * 获取索引列数量
     * 
     * @return 列数量
     */
    public int getColumnCount() {
        return columnNames != null ? columnNames.size() : 0;
    }
    
    /**
     * 检查是否为复合索引
     * 
     * @return 是否为复合索引
     */
    public boolean isCompositeIndex() {
        return getColumnCount() > 1;
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
     * 获取索引列名的字符串表示
     * 
     * @return 列名字符串，用逗号分隔
     */
    public String getColumnNamesAsString() {
        if (columnNames == null || columnNames.isEmpty()) {
            return "";
        }
        return String.join(", ", columnNames);
    }
    
    /**
     * 获取索引的完整定义信息
     * 
     * @return 索引定义信息字符串
     */
    public String getFullDefinition() {
        StringBuilder sb = new StringBuilder();
        sb.append("Index: ").append(indexName);
        sb.append(", Columns: [").append(getColumnNamesAsString()).append("]");
        sb.append(", Unique: ").append(unique);
        sb.append(", Type: ").append(indexType);
        if (hasComment()) {
            sb.append(", Comment: ").append(comment);
        }
        return sb.toString();
    }
    
    /**
     * 比较两个索引是否相同（基于索引名和列名）
     * 
     * @param other 另一个索引元数据
     * @return 是否相同
     */
    public boolean isSameAs(IndexMetadata other) {
        if (other == null) {
            return false;
        }
        
        // 比较索引名
        if (!indexName.equals(other.indexName)) {
            return false;
        }
        
        // 比较列名列表
        if (columnNames == null && other.columnNames == null) {
            return true;
        }
        if (columnNames == null || other.columnNames == null) {
            return false;
        }
        
        return columnNames.equals(other.columnNames) && 
               unique == other.unique && 
               indexType == other.indexType;
    }
}