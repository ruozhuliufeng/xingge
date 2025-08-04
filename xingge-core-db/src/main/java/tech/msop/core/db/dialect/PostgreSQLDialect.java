/*
 * Copyright (c) 2025 xingge
 * 
 * PostgreSQL数据库方言实现
 * 处理PostgreSQL特有的SQL语法和数据类型映射
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.dialect;

import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * PostgreSQL数据库方言实现
 * 处理PostgreSQL特有的SQL语法和数据类型映射
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
public class PostgreSQLDialect implements Dialect {
    
    private static final Map<Class<?>, String> TYPE_MAPPING = new HashMap<>();
    
    static {
        // 字符串类型
        TYPE_MAPPING.put(String.class, "VARCHAR");
        TYPE_MAPPING.put(char.class, "CHAR");
        TYPE_MAPPING.put(Character.class, "CHAR");
        
        // 数值类型
        TYPE_MAPPING.put(byte.class, "SMALLINT");
        TYPE_MAPPING.put(Byte.class, "SMALLINT");
        TYPE_MAPPING.put(short.class, "SMALLINT");
        TYPE_MAPPING.put(Short.class, "SMALLINT");
        TYPE_MAPPING.put(int.class, "INTEGER");
        TYPE_MAPPING.put(Integer.class, "INTEGER");
        TYPE_MAPPING.put(long.class, "BIGINT");
        TYPE_MAPPING.put(Long.class, "BIGINT");
        TYPE_MAPPING.put(float.class, "REAL");
        TYPE_MAPPING.put(Float.class, "REAL");
        TYPE_MAPPING.put(double.class, "DOUBLE PRECISION");
        TYPE_MAPPING.put(Double.class, "DOUBLE PRECISION");
        TYPE_MAPPING.put(BigDecimal.class, "NUMERIC");
        
        // 布尔类型
        TYPE_MAPPING.put(boolean.class, "BOOLEAN");
        TYPE_MAPPING.put(Boolean.class, "BOOLEAN");
        
        // 日期时间类型
        TYPE_MAPPING.put(Date.class, "TIMESTAMP");
        TYPE_MAPPING.put(LocalDate.class, "DATE");
        TYPE_MAPPING.put(LocalTime.class, "TIME");
        TYPE_MAPPING.put(LocalDateTime.class, "TIMESTAMP");
        
        // 二进制类型
        TYPE_MAPPING.put(byte[].class, "BYTEA");
    }
    
    @Override
    public String generateCreateTableSql(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        
        if (tableMetadata.getSchema() != null && !tableMetadata.getSchema().isEmpty()) {
            sql.append(escapeIdentifier(tableMetadata.getSchema())).append(".");
        }
        
        sql.append(escapeIdentifier(tableMetadata.getTableName()));
        sql.append(" (\n");
        
        // 添加列定义
        List<String> columnDefinitions = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        for (ColumnMetadata column : tableMetadata.getColumns()) {
            columnDefinitions.add(getColumnDefinition(column));
            if (column.isPrimaryKey()) {
                primaryKeys.add(escapeIdentifier(column.getColumnName()));
            }
        }
        
        sql.append(String.join(",\n  ", columnDefinitions));
        
        // 添加主键约束
        if (!primaryKeys.isEmpty()) {
            sql.append(",\n  PRIMARY KEY (");
            sql.append(String.join(", ", primaryKeys));
            sql.append(")");
        }
        
        sql.append("\n)");
        
        return sql.toString();
    }
    
    @Override
    public String generateAddColumnSql(String tableName, ColumnMetadata columnMetadata) {
        return String.format("ALTER TABLE %s ADD COLUMN %s",
                escapeIdentifier(tableName),
                getColumnDefinition(columnMetadata));
    }
    
    @Override
    public String generateModifyColumnSql(String tableName, ColumnMetadata columnMetadata) {
        // PostgreSQL使用ALTER COLUMN语法
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(escapeIdentifier(tableName));
        sql.append(" ALTER COLUMN ").append(escapeIdentifier(columnMetadata.getColumnName()));
        
        // 修改数据类型
        String dataType = columnMetadata.getDataType();
        if (dataType == null && columnMetadata.getJavaType() != null) {
            dataType = mapJavaTypeToDbType(columnMetadata.getJavaType(), 
                    columnMetadata.getLength(), columnMetadata.getPrecision(), columnMetadata.getScale());
        }
        sql.append(" TYPE ").append(dataType);
        
        return sql.toString();
    }
    
    @Override
    public String generateDropColumnSql(String tableName, String columnName) {
        return String.format("ALTER TABLE %s DROP COLUMN %s",
                escapeIdentifier(tableName),
                escapeIdentifier(columnName));
    }
    
    @Override
    public String generateCreateIndexSql(String tableName, IndexMetadata indexMetadata) {
        StringBuilder sql = new StringBuilder();
        
        if (indexMetadata.isUnique()) {
            sql.append("CREATE UNIQUE INDEX ");
        } else {
            sql.append("CREATE INDEX ");
        }
        
        sql.append(escapeIdentifier(indexMetadata.getIndexName()));
        sql.append(" ON ").append(escapeIdentifier(tableName));
        
        // 添加索引类型
        if (indexMetadata.getIndexType() != null) {
            switch (indexMetadata.getIndexType()) {
                case BTREE:
                    sql.append(" USING BTREE");
                    break;
                case HASH:
                    sql.append(" USING HASH");
                    break;
                case FULLTEXT:
                    sql.append(" USING GIN"); // PostgreSQL使用GIN索引实现全文搜索
                    break;
                case SPATIAL:
                    sql.append(" USING GIST"); // PostgreSQL使用GIST索引实现空间索引
                    break;
            }
        }
        
        sql.append(" (");
        List<String> escapedColumns = new ArrayList<>();
        for (String column : indexMetadata.getColumnNames()) {
            escapedColumns.add(escapeIdentifier(column));
        }
        sql.append(String.join(", ", escapedColumns));
        sql.append(")");
        
        return sql.toString();
    }
    
    @Override
    public String generateDropIndexSql(String tableName, String indexName) {
        return String.format("DROP INDEX IF EXISTS %s", escapeIdentifier(indexName));
    }
    
    @Override
    public String generateTableExistsSql(String tableName, String schema) {
        String schemaName = schema != null ? schema : "public";
        return "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + tableName + "' AND table_schema = '" + schemaName + "'";
    }

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public boolean supports(String databaseProductName) {
        return databaseProductName != null && databaseProductName.toLowerCase().contains("postgresql");
    }
    
    @Override
    public String generateTableStructureSql(String tableName, String schema) {
        String schemaName = schema != null ? schema : "public";
        return "SELECT column_name, data_type, character_maximum_length, numeric_precision, " +
                "numeric_scale, is_nullable, column_default, '' as column_comment, " +
                "CASE WHEN column_default LIKE 'nextval%' THEN 'YES' ELSE 'NO' END as is_identity " +
                "FROM information_schema.columns WHERE table_name = '" + tableName + "' " +
                "AND table_schema = '" + schemaName + "' ORDER BY ordinal_position";
    }
    
    @Override
    public String generateIndexInfoSql(String tableName, String schema) {
        String schemaName = schema != null ? schema : "public";
        return "SELECT i.relname as index_name, a.attname as column_name, ix.indisunique as is_unique " +
                "FROM pg_class t, pg_class i, pg_index ix, pg_attribute a, pg_namespace n " +
                "WHERE t.oid = ix.indrelid AND i.oid = ix.indexrelid AND a.attrelid = t.oid " +
                "AND a.attnum = ANY(ix.indkey) AND t.relkind = 'r' AND n.oid = t.relnamespace " +
                "AND t.relname = '" + tableName + "' AND n.nspname = '" + schemaName + "' " +
                "AND NOT ix.indisprimary ORDER BY i.relname, a.attnum";
    }
    
    @Override
    public String mapJavaTypeToDbType(Class<?> javaType, int length, int precision, int scale) {
        String baseType = TYPE_MAPPING.get(javaType);
        if (baseType == null) {
            baseType = "VARCHAR"; // 默认类型
        }
        
        // 处理长度和精度
        if ("VARCHAR".equals(baseType) || "CHAR".equals(baseType)) {
            if (length > 0) {
                return baseType + "(" + length + ")";
            } else {
                return baseType + "(255)"; // 默认长度
            }
        } else if ("NUMERIC".equals(baseType)) {
            if (precision > 0) {
                if (scale > 0) {
                    return baseType + "(" + precision + "," + scale + ")";
                } else {
                    return baseType + "(" + precision + ")";
                }
            } else {
                return baseType + "(10,2)"; // 默认精度
            }
        }
        
        return baseType;
    }
    
    @Override
    public String getAutoIncrementSql() {
        return "GENERATED BY DEFAULT AS IDENTITY";
    }
    
    @Override
    public String getPrimaryKeySql(String columnName) {
        return "PRIMARY KEY (" + escapeIdentifier(columnName) + ")";
    }
    
    @Override
    public String getUniqueSql(String columnName) {
        return "UNIQUE (" + escapeIdentifier(columnName) + ")";
    }
    
    @Override
    public String getNotNullSql() {
        return "NOT NULL";
    }
    
    /**
     * 获取多列主键约束的SQL片段
     * 
     * @param columnNames 主键列名列表
     * @return 主键约束SQL片段
     */
    public String getPrimaryKeySql(List<String> columnNames) {
        List<String> escapedColumns = new ArrayList<>();
        for (String column : columnNames) {
            escapedColumns.add(escapeIdentifier(column));
        }
        return "PRIMARY KEY (" + String.join(", ", escapedColumns) + ")";
    }
    
    /**
     * 获取多列唯一约束的SQL片段
     * 
     * @param constraintName 约束名称
     * @param columnNames 列名列表
     * @return 唯一约束SQL片段
     */
    public String getUniqueSql(String constraintName, List<String> columnNames) {
        List<String> escapedColumns = new ArrayList<>();
        for (String column : columnNames) {
            escapedColumns.add(escapeIdentifier(column));
        }
        return "CONSTRAINT " + escapeIdentifier(constraintName) + " UNIQUE (" + String.join(", ", escapedColumns) + ")";
    }
    
    @Override
    public String getDefaultValueSql(String defaultValue) {
        return "DEFAULT " + defaultValue;
    }
    
    @Override
    public String getCommentSql(String comment) {
        // PostgreSQL需要单独的COMMENT语句
        return "";
    }
    
    @Override
    public String escapeIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }
    
    /**
     * 获取列定义SQL
     * 
     * @param column 列元数据
     * @return 列定义SQL
     */
    private String getColumnDefinition(ColumnMetadata column) {
        StringBuilder definition = new StringBuilder();
        
        definition.append("  ").append(escapeIdentifier(column.getColumnName()));
        
        // 使用自定义列定义或生成列定义
        if (column.hasColumnDefinition()) {
            definition.append(" ").append(column.getColumnDefinition());
        } else {
            // 数据类型
            String dataType = column.getDataType();
            if (dataType == null && column.getJavaType() != null) {
                dataType = mapJavaTypeToDbType(column.getJavaType(), 
                        column.getLength(), column.getPrecision(), column.getScale());
            }
            definition.append(" ").append(dataType);
            
            // 是否允许为空
            if (!column.isNullable()) {
                definition.append(" ").append(getNotNullSql());
            }
            
            // 自增
            if (column.isAutoIncrement()) {
                definition.append(" ").append(getAutoIncrementSql());
            }
            
            // 默认值
            if (column.hasDefaultValue()) {
                definition.append(" ").append(getDefaultValueSql(column.getDefaultValue()));
            }
        }
        
        return definition.toString();
    }
}