/*
 * Copyright (c) 2025 xingge
 * 
 * SQL Server数据库方言实现
 * 处理SQL Server特有的SQL语法和数据类型映射
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.dialect;

import tech.msop.core.db.annotation.Index;
import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * SQL Server数据库方言实现
 * 处理SQL Server特有的SQL语法和数据类型映射
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
public class SQLServerDialect implements Dialect {
    
    private static final Map<Class<?>, String> TYPE_MAPPING = new HashMap<>();
    
    static {
        // 字符串类型
        TYPE_MAPPING.put(String.class, "NVARCHAR");
        TYPE_MAPPING.put(char.class, "NCHAR");
        TYPE_MAPPING.put(Character.class, "NCHAR");
        
        // 数值类型
        TYPE_MAPPING.put(byte.class, "TINYINT");
        TYPE_MAPPING.put(Byte.class, "TINYINT");
        TYPE_MAPPING.put(short.class, "SMALLINT");
        TYPE_MAPPING.put(Short.class, "SMALLINT");
        TYPE_MAPPING.put(int.class, "INT");
        TYPE_MAPPING.put(Integer.class, "INT");
        TYPE_MAPPING.put(long.class, "BIGINT");
        TYPE_MAPPING.put(Long.class, "BIGINT");
        TYPE_MAPPING.put(float.class, "REAL");
        TYPE_MAPPING.put(Float.class, "REAL");
        TYPE_MAPPING.put(double.class, "FLOAT");
        TYPE_MAPPING.put(Double.class, "FLOAT");
        TYPE_MAPPING.put(BigDecimal.class, "DECIMAL");
        
        // 布尔类型
        TYPE_MAPPING.put(boolean.class, "BIT");
        TYPE_MAPPING.put(Boolean.class, "BIT");
        
        // 日期时间类型
        TYPE_MAPPING.put(Date.class, "DATETIME2");
        TYPE_MAPPING.put(LocalDate.class, "DATE");
        TYPE_MAPPING.put(LocalTime.class, "TIME");
        TYPE_MAPPING.put(LocalDateTime.class, "DATETIME2");
        
        // 二进制类型
        TYPE_MAPPING.put(byte[].class, "VARBINARY");
    }
    
    @Override
    public String generateCreateTableSql(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='");
        sql.append(tableMetadata.getTableName());
        sql.append("' AND xtype='U') CREATE TABLE ");
        
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
        return String.format("ALTER TABLE %s ADD %s",
                escapeIdentifier(tableName),
                getColumnDefinition(columnMetadata).trim());
    }
    
    @Override
    public String generateModifyColumnSql(String tableName, ColumnMetadata columnMetadata) {
        return String.format("ALTER TABLE %s ALTER COLUMN %s",
                escapeIdentifier(tableName),
                getColumnDefinition(columnMetadata).trim());
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
        return String.format("DROP INDEX %s ON %s",
                escapeIdentifier(indexName),
                escapeIdentifier(tableName));
    }
    
    @Override
    public String generateTableExistsSql(String tableName, String schema) {
        return "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "'";
    }

    @Override
    public String getDatabaseType() {
        return "SQL Server";
    }

    @Override
    public boolean supports(String databaseProductName) {
        return databaseProductName != null && databaseProductName.toLowerCase().contains("microsoft");
    }
    
    @Override
    public String generateTableStructureSql(String tableName, String schema) {
        return "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, " +
                "NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, '' as REMARKS, " +
                "COLUMNPROPERTY(OBJECT_ID(TABLE_SCHEMA+'.'+TABLE_NAME), COLUMN_NAME, 'IsIdentity') as IS_IDENTITY " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + tableName + "' ORDER BY ORDINAL_POSITION";
    }
    
    @Override
    public String generateIndexInfoSql(String tableName, String schema) {
        return "SELECT i.name as INDEX_NAME, c.name as COLUMN_NAME, i.is_unique as IS_UNIQUE " +
                "FROM sys.indexes i " +
                "INNER JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id " +
                "INNER JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id " +
                "INNER JOIN sys.tables t ON i.object_id = t.object_id " +
                "WHERE t.name = '" + tableName + "' AND i.is_primary_key = 0 ORDER BY i.name, ic.key_ordinal";
    }
    
    @Override
    public String mapJavaTypeToDbType(Class<?> javaType, int length, int precision, int scale) {
        String baseType = TYPE_MAPPING.get(javaType);
        if (baseType == null) {
            baseType = "NVARCHAR"; // 默认类型
        }
        
        // 处理长度和精度
        if ("NVARCHAR".equals(baseType) || "NCHAR".equals(baseType)) {
            if (length > 0) {
                return baseType + "(" + length + ")";
            } else {
                return baseType + "(255)"; // 默认长度
            }
        } else if ("DECIMAL".equals(baseType)) {
            if (precision > 0) {
                if (scale > 0) {
                    return baseType + "(" + precision + "," + scale + ")";
                } else {
                    return baseType + "(" + precision + ")";
                }
            } else {
                return baseType + "(18,2)"; // 默认精度
            }
        } else if ("VARBINARY".equals(baseType)) {
            if (length > 0) {
                return baseType + "(" + length + ")";
            } else {
                return baseType + "(MAX)"; // 默认为MAX
            }
        }
        
        return baseType;
    }
    
    @Override
    public String getAutoIncrementSql() {
        return "IDENTITY(1,1)";
    }
    
    @Override
    public String getPrimaryKeySql(String columnName) {
        return "PRIMARY KEY (" + escapeIdentifier(columnName) + ")";
    }
    
    @Override
    public String getUniqueSql(String columnName) {
        return "UNIQUE (" + escapeIdentifier(columnName) + ")";
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
    public String getNotNullSql() {
        return "NOT NULL";
    }
    
    @Override
    public String getDefaultValueSql(String defaultValue) {
        return "DEFAULT " + defaultValue;
    }
    
    @Override
    public String getCommentSql(String comment) {
        // SQL Server需要使用扩展属性来添加注释
        return "";
    }
    
    @Override
    public String escapeIdentifier(String identifier) {
        return "[" + identifier + "]";
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
            
            // 自增
            if (column.isAutoIncrement()) {
                definition.append(" ").append(getAutoIncrementSql());
            }
            
            // 是否允许为空
            if (!column.isNullable()) {
                definition.append(" ").append(getNotNullSql());
            }
            
            // 默认值
            if (column.hasDefaultValue()) {
                definition.append(" ").append(getDefaultValueSql(column.getDefaultValue()));
            }
        }
        
        return definition.toString();
    }
}