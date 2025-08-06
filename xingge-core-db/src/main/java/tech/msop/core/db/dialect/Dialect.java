/*
 * Copyright (c) 2025 xingge
 * 
 * 数据库方言接口
 * 用于处理不同数据库的SQL语法差异
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.dialect;

import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;

/**
 * 数据库方言接口
 * 用于处理不同数据库的SQL语法差异
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
public interface Dialect {
    
    /**
     * 获取数据库类型
     * 
     * @return 数据库类型
     */
    String getDatabaseType();
    
    /**
     * 是否支持当前数据库
     * 
     * @param databaseProductName 数据库产品名称
     * @return 是否支持
     */
    boolean supports(String databaseProductName);
    
    /**
     * 生成创建表的SQL语句
     * 
     * @param tableMetadata 表元数据
     * @return 创建表SQL
     */
    String generateCreateTableSql(TableMetadata tableMetadata);
    
    /**
     * 生成添加列的SQL语句
     * 
     * @param tableName 表名
     * @param columnMetadata 列元数据
     * @return 添加列SQL
     */
    String generateAddColumnSql(String tableName, ColumnMetadata columnMetadata);
    
    /**
     * 生成修改列的SQL语句
     * 
     * @param tableName 表名
     * @param columnMetadata 列元数据
     * @return 修改列SQL
     */
    String generateModifyColumnSql(String tableName, ColumnMetadata columnMetadata);
    
    /**
     * 生成删除列的SQL语句
     * 
     * @param tableName 表名
     * @param columnName 列名
     * @return 删除列SQL
     */
    String generateDropColumnSql(String tableName, String columnName);
    
    /**
     * 生成创建索引的SQL语句
     * 
     * @param tableName 表名
     * @param indexMetadata 索引元数据
     * @return 创建索引SQL
     */
    String generateCreateIndexSql(String tableName, IndexMetadata indexMetadata);
    
    /**
     * 生成删除索引的SQL语句
     * 
     * @param tableName 表名
     * @param indexName 索引名
     * @return 删除索引SQL
     */
    String generateDropIndexSql(String tableName, String indexName);
    
    /**
     * 生成查询表是否存在的SQL语句
     * 
     * @param tableName 表名
     * @param schema 模式名
     * @return 查询表存在SQL
     */
    String generateTableExistsSql(String tableName, String schema);
    
    /**
     * 生成查询表结构的SQL语句
     * 
     * @param tableName 表名
     * @param schema 模式名
     * @return 查询表结构SQL
     */
    String generateTableStructureSql(String tableName, String schema);
    
    /**
     * 生成查询索引信息的SQL语句
     * 
     * @param tableName 表名
     * @param schema 模式名
     * @return 查询索引信息SQL
     */
    String generateIndexInfoSql(String tableName, String schema);
    
    /**
     * 将Java类型映射为数据库类型
     * 
     * @param javaType Java类型
     * @param length 长度
     * @param precision 精度
     * @param scale 标度
     * @return 数据库类型
     */
    String mapJavaTypeToDbType(Class<?> javaType, int length, int precision, int scale);
    
    /**
     * 获取自增主键的SQL片段
     * 
     * @return 自增主键SQL片段
     */
    String getAutoIncrementSql();
    
    /**
     * 获取主键约束的SQL片段
     * 
     * @param columnName 主键列名
     * @return 主键约束SQL片段
     */
    String getPrimaryKeySql(String columnName);
    
    /**
     * 获取唯一约束的SQL片段
     * 
     * @param columnName 列名
     * @return 唯一约束SQL片段
     */
    String getUniqueSql(String columnName);
    
    /**
     * 获取非空约束的SQL片段
     * 
     * @return 非空约束SQL片段
     */
    String getNotNullSql();
    
    /**
     * 获取默认值的SQL片段
     * 
     * @param defaultValue 默认值
     * @return 默认值SQL片段
     */
    String getDefaultValueSql(String defaultValue);
    
    /**
     * 获取注释的SQL片段
     * 
     * @param comment 注释
     * @return 注释SQL片段
     */
    String getCommentSql(String comment);
    
    /**
     * 转义标识符（表名、列名等）
     * 
     * @param identifier 标识符
     * @return 转义后的标识符
     */
    String escapeIdentifier(String identifier);
}