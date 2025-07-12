/*
 * Copyright (c) 2025 xingge
 *
 * MySQL数据库方言实现
 * 处理MySQL特有的SQL语法和数据类型映射
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
 * MySQL数据库方言实现
 * 处理MySQL特有的SQL语法和数据类型映射
 *
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
public class MySQLDialect implements Dialect {

    private static final Map<Class<?>, String> TYPE_MAPPING = new HashMap<>();

    static {
        // 字符串类型
        TYPE_MAPPING.put(String.class, "VARCHAR");
        TYPE_MAPPING.put(char.class, "CHAR");
        TYPE_MAPPING.put(Character.class, "CHAR");

        // 数值类型
        TYPE_MAPPING.put(byte.class, "TINYINT");
        TYPE_MAPPING.put(Byte.class, "TINYINT");
        TYPE_MAPPING.put(short.class, "SMALLINT");
        TYPE_MAPPING.put(Short.class, "SMALLINT");
        TYPE_MAPPING.put(int.class, "INT");
        TYPE_MAPPING.put(Integer.class, "INT");
        TYPE_MAPPING.put(long.class, "BIGINT");
        TYPE_MAPPING.put(Long.class, "BIGINT");
        TYPE_MAPPING.put(float.class, "FLOAT");
        TYPE_MAPPING.put(Float.class, "FLOAT");
        TYPE_MAPPING.put(double.class, "DOUBLE");
        TYPE_MAPPING.put(Double.class, "DOUBLE");
        TYPE_MAPPING.put(BigDecimal.class, "DECIMAL");

        // 布尔类型
        TYPE_MAPPING.put(boolean.class, "TINYINT(1)");
        TYPE_MAPPING.put(Boolean.class, "TINYINT(1)");

        // 日期时间类型
        TYPE_MAPPING.put(Date.class, "DATETIME");
        TYPE_MAPPING.put(LocalDate.class, "DATE");
        TYPE_MAPPING.put(LocalTime.class, "TIME");
        TYPE_MAPPING.put(LocalDateTime.class, "DATETIME");

        // 二进制类型
        TYPE_MAPPING.put(byte[].class, "BLOB");
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }

    @Override
    public boolean supports(String databaseProductName) {
        return databaseProductName != null && 
               databaseProductName.toLowerCase().contains("mysql");
    }

    @Override
    public String generateCreateTableSql(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ");
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

        // 添加表选项
        if (tableMetadata.getEngine() != null) {
            sql.append(" ENGINE=").append(tableMetadata.getEngine());
        } else {
            sql.append(" ENGINE=InnoDB");
        }

        if (tableMetadata.getCharset() != null) {
            sql.append(" DEFAULT CHARSET=").append(tableMetadata.getCharset());
        } else {
            sql.append(" DEFAULT CHARSET=utf8mb4");
        }

        if (tableMetadata.getCollate() != null) {
            sql.append(" COLLATE=").append(tableMetadata.getCollate());
        }

        if (tableMetadata.getComment() != null && !tableMetadata.getComment().trim().isEmpty()) {
            sql.append(" COMMENT='").append(escapeString(tableMetadata.getComment())).append("'");
        }

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
        return String.format("ALTER TABLE %s MODIFY COLUMN %s",
                escapeIdentifier(tableName),
                getColumnDefinition(columnMetadata));
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
                    sql = new StringBuilder("CREATE FULLTEXT INDEX ");
                    sql.append(escapeIdentifier(indexMetadata.getIndexName()));
                    sql.append(" ON ").append(escapeIdentifier(tableName));
                    sql.append(" (").append(String.join(", ", escapedColumns)).append(")");
                    break;
                case SPATIAL:
                    sql = new StringBuilder("CREATE SPATIAL INDEX ");
                    sql.append(escapeIdentifier(indexMetadata.getIndexName()));
                    sql.append(" ON ").append(escapeIdentifier(tableName));
                    sql.append(" (").append(String.join(", ", escapedColumns)).append(")");
                    break;
            }
        }

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
        if (schema != null && !schema.trim().isEmpty()) {
            return "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + tableName + "' AND table_schema = '" + schema + "'";
        } else {
            return "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = '" + tableName + "' AND table_schema = DATABASE()";
        }
    }

    @Override
    public String generateTableStructureSql(String tableName, String schema) {
        if (schema != null && !schema.trim().isEmpty()) {
            return "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, " +
                   "NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, " +
                   "CASE WHEN EXTRA = 'auto_increment' THEN 'YES' ELSE 'NO' END as IS_AUTOINCREMENT, " +
                   "CASE WHEN COLUMN_KEY = 'PRI' THEN 'YES' ELSE 'NO' END as IS_PRIMARY_KEY " +
                   "FROM information_schema.columns WHERE table_name = '" + tableName + "' AND table_schema = '" + schema + "' " +
                   "ORDER BY ORDINAL_POSITION";
        } else {
            return "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, " +
                   "NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT, " +
                   "CASE WHEN EXTRA = 'auto_increment' THEN 'YES' ELSE 'NO' END as IS_AUTOINCREMENT, " +
                   "CASE WHEN COLUMN_KEY = 'PRI' THEN 'YES' ELSE 'NO' END as IS_PRIMARY_KEY " +
                   "FROM information_schema.columns WHERE table_name = '" + tableName + "' AND table_schema = DATABASE() " +
                   "ORDER BY ORDINAL_POSITION";
        }
    }

    @Override
    public String generateIndexInfoSql(String tableName, String schema) {
        if (schema != null && !schema.trim().isEmpty()) {
            return "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE, INDEX_TYPE, SEQ_IN_INDEX " +
                   "FROM information_schema.statistics " +
                   "WHERE table_name = '" + tableName + "' AND table_schema = '" + schema + "' " +
                   "AND INDEX_NAME != 'PRIMARY' " +
                   "ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        } else {
            return "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE, INDEX_TYPE, SEQ_IN_INDEX " +
                   "FROM information_schema.statistics " +
                   "WHERE table_name = '" + tableName + "' AND table_schema = DATABASE() " +
                   "AND INDEX_NAME != 'PRIMARY' " +
                   "ORDER BY INDEX_NAME, SEQ_IN_INDEX";
        }
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
        } else if ("DECIMAL".equals(baseType)) {
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
        return "AUTO_INCREMENT";
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
     * @param columnNames    列名列表
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
        return "COMMENT '" + escapeString(comment) + "'";
    }

    @Override
    public String escapeIdentifier(String identifier) {
        return "`" + identifier + "`";
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

            // 注释
            if (column.hasComment()) {
                definition.append(" ").append(getCommentSql(column.getComment()));
            }
        }

        return definition.toString();
    }

    /**
     * 转义字符串
     *
     * @param str 原字符串
     * @return 转义后的字符串
     */
    private String escapeString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }
}