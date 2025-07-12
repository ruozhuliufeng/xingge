package tech.msop.core.db.dialect;

import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * H2数据库方言实现
 * 主要用于测试环境
 *
 * @author msop
 * @since 1.0.0
 */
public class H2Dialect implements Dialect {

    private static final Map<Class<?>, String> TYPE_MAPPING = new HashMap<>();

    static {
        // 基础类型映射
        TYPE_MAPPING.put(String.class, "VARCHAR");
        TYPE_MAPPING.put(Integer.class, "INT");
        TYPE_MAPPING.put(int.class, "INT");
        TYPE_MAPPING.put(Long.class, "BIGINT");
        TYPE_MAPPING.put(long.class, "BIGINT");
        TYPE_MAPPING.put(Short.class, "SMALLINT");
        TYPE_MAPPING.put(short.class, "SMALLINT");
        TYPE_MAPPING.put(Byte.class, "TINYINT");
        TYPE_MAPPING.put(byte.class, "TINYINT");
        TYPE_MAPPING.put(Boolean.class, "BOOLEAN");
        TYPE_MAPPING.put(boolean.class, "BOOLEAN");
        TYPE_MAPPING.put(Float.class, "REAL");
        TYPE_MAPPING.put(float.class, "REAL");
        TYPE_MAPPING.put(Double.class, "DOUBLE");
        TYPE_MAPPING.put(double.class, "DOUBLE");
        TYPE_MAPPING.put(BigDecimal.class, "DECIMAL");
        TYPE_MAPPING.put(java.util.Date.class, "TIMESTAMP");
        TYPE_MAPPING.put(LocalDateTime.class, "TIMESTAMP");
        TYPE_MAPPING.put(LocalDate.class, "DATE");
        TYPE_MAPPING.put(byte[].class, "BLOB");
    }

    @Override
    public String mapJavaTypeToDbType(Class<?> javaType, int length, int precision, int scale) {
        String baseType = TYPE_MAPPING.get(javaType);
        if (baseType == null) {
            baseType = "VARCHAR";
        }

        switch (baseType) {
            case "VARCHAR":
                return length > 0 ? "VARCHAR(" + length + ")" : "VARCHAR(255)";
            case "DECIMAL":
                if (precision > 0 && scale >= 0) {
                    return "DECIMAL(" + precision + "," + scale + ")";
                } else if (precision > 0) {
                    return "DECIMAL(" + precision + ")";
                }
                return "DECIMAL(19,2)";
            default:
                return baseType;
        }
    }

    @Override
    public String generateCreateTableSql(TableMetadata tableMetadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(escapeIdentifier(tableMetadata.getTableName())).append(" (\n");

        List<String> columnDefinitions = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();

        for (ColumnMetadata column : tableMetadata.getColumns()) {
            columnDefinitions.add("  " + getColumnDefinition(column));
            if (column.isPrimaryKey()) {
                primaryKeys.add(escapeIdentifier(column.getColumnName()));
            }
        }

        sql.append(String.join(",\n", columnDefinitions));

        if (!primaryKeys.isEmpty()) {
            sql.append(",\n  PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        sql.append("\n)");

        if (tableMetadata.getComment() != null && !tableMetadata.getComment().trim().isEmpty()) {
            sql.append(" COMMENT '").append(escapeString(tableMetadata.getComment())).append("'");
        }

        return sql.toString();
    }

    @Override
    public String generateAddColumnSql(String tableName, ColumnMetadata column) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " ADD COLUMN " + getColumnDefinition(column);
    }

    @Override
    public String generateModifyColumnSql(String tableName, ColumnMetadata column) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " ALTER COLUMN " + getColumnDefinition(column);
    }

    @Override
    public String generateDropColumnSql(String tableName, String columnName) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " DROP COLUMN " + escapeIdentifier(columnName);
    }

    @Override
    public String generateCreateIndexSql(String tableName, IndexMetadata index) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (index.isUnique()) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ").append(escapeIdentifier(index.getIndexName()));
        sql.append(" ON ").append(escapeIdentifier(tableName));
        sql.append(" (").append(String.join(", ", index.getColumnNames().stream()
                .map(this::escapeIdentifier).toArray(String[]::new))).append(")");
        return sql.toString();
    }

    @Override
    public String generateDropIndexSql(String tableName, String indexName) {
        return "DROP INDEX " + escapeIdentifier(indexName);
    }

    @Override
    public String generateTableExistsSql(String tableName, String schema) {
        return "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER('" + tableName + "')";
    }

    @Override
    public String generateTableStructureSql(String tableName, String schema) {
        return "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, " +
                "NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, REMARKS, IS_IDENTITY " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = UPPER('" + tableName + "') ORDER BY ORDINAL_POSITION";
    }

    @Override
    public String generateIndexInfoSql(String tableName, String schema) {
        return "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE UPPER(TABLE_NAME) = UPPER('" + tableName + "') AND INDEX_NAME NOT LIKE 'PRIMARY%' ORDER BY INDEX_NAME, ORDINAL_POSITION";
    }

    @Override
    public String getDatabaseType() {
        return "H2";
    }

    @Override
    public boolean supports(String databaseProductName) {
        return databaseProductName != null && databaseProductName.toLowerCase().contains("h2");
    }

    // 保留原有的实现方法供内部使用
    public boolean isTableExists(DataSource dataSource, String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("检查表是否存在失败: " + tableName, e);
        }
    }

    public TableMetadata getTableStructure(DataSource dataSource, String tableName) {
        TableMetadata metadata = new TableMetadata();
        metadata.setTableName(tableName);

        // 获取表信息
        String tableInfoSql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = UPPER(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(tableInfoSql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    metadata.setComment(rs.getString("TABLE_COMMENT"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取表信息失败: " + tableName, e);
        }

        // 获取列信息
        String columnSql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, " +
                "NUMERIC_SCALE, IS_NULLABLE, COLUMN_DEFAULT, REMARKS, IS_IDENTITY " +
                "FROM INFORMATION_SCHEMA.COLUMNS WHERE UPPER(TABLE_NAME) = UPPER(?) ORDER BY ORDINAL_POSITION";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(columnSql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ColumnMetadata column = new ColumnMetadata();
                    column.setColumnName(rs.getString("COLUMN_NAME"));
                    column.setDataType(rs.getString("DATA_TYPE"));
                    column.setLength(rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
                    column.setPrecision(rs.getInt("NUMERIC_PRECISION"));
                    column.setScale(rs.getInt("NUMERIC_SCALE"));
                    column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                    column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                    column.setComment(rs.getString("REMARKS"));
                    column.setAutoIncrement("YES".equals(rs.getString("IS_IDENTITY")));
                    metadata.addColumn(column);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取列信息失败: " + tableName, e);
        }

        // 获取主键信息
        String primaryKeySql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                "WHERE UPPER(TABLE_NAME) = UPPER(?) AND CONSTRAINT_NAME LIKE 'PRIMARY%' ORDER BY ORDINAL_POSITION";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(primaryKeySql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    metadata.addPrimaryKey(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取主键信息失败: " + tableName, e);
        }

        // 获取索引信息
        String indexSql = "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE FROM INFORMATION_SCHEMA.INDEXES " +
                "WHERE UPPER(TABLE_NAME) = UPPER(?) AND INDEX_NAME NOT LIKE 'PRIMARY%' ORDER BY INDEX_NAME, ORDINAL_POSITION";
        Map<String, IndexMetadata> indexMap = new HashMap<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(indexSql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    boolean nonUnique = rs.getBoolean("NON_UNIQUE");

                    IndexMetadata index = indexMap.computeIfAbsent(indexName, k -> {
                        IndexMetadata idx = new IndexMetadata();
                        idx.setIndexName(indexName);
                        idx.setUnique(!nonUnique);
                        idx.setTableName(tableName);
                        return idx;
                    });
                    index.addColumn(columnName);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取索引信息失败: " + tableName, e);
        }

        for (IndexMetadata index : indexMap.values()) {
            metadata.addIndex(index);
        }

        return metadata;
    }

    @Override
    public String getAutoIncrementSql() {
        return "IDENTITY";
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
        return "`" + identifier.replace("`", "``") + "`";
    }

    /**
     * 转义字符串
     */
    private String escapeString(String str) {
        return str.replace("'", "''");
    }

    /**
     * 获取列定义SQL
     */
    private String getColumnDefinition(ColumnMetadata column) {
        StringBuilder sql = new StringBuilder();
        sql.append(escapeIdentifier(column.getColumnName()));

        // 使用自定义列定义
        if (column.hasColumnDefinition()) {
            sql.append(" ").append(column.getColumnDefinition());
            return sql.toString();
        }

        // 数据类型
        String dataType = column.getDataType();
        if (dataType == null || dataType.trim().isEmpty()) {
            dataType = mapJavaTypeToDbType(column.getJavaType(), column.getLength(), column.getPrecision(), column.getScale());
        }
        sql.append(" ").append(dataType);

        // 自增
        if (column.isAutoIncrement()) {
            sql.append(" ").append(getAutoIncrementSql());
        }

        // 非空约束
        if (!column.isNullable()) {
            sql.append(" ").append(getNotNullSql());
        }

        // 默认值
        if (column.hasDefaultValue()) {
            sql.append(" ").append(getDefaultValueSql(column.getDefaultValue()));
        }

        // 注释
        if (column.hasComment()) {
            sql.append(" ").append(getCommentSql(column.getComment()));
        }

        return sql.toString();
    }
}