/**
 * Oracle数据库方言实现
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.dialect;

import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Oracle数据库方言实现类
 * 处理Oracle数据库特有的SQL语法和数据类型映射
 */
public class OracleDialect implements Dialect {
    
    /**
     * Java类型到Oracle数据库类型的映射
     */
    private static final Map<Class<?>, String> TYPE_MAPPING = new HashMap<>();
    
    static {
        // 字符串类型
        TYPE_MAPPING.put(String.class, "VARCHAR2");
        TYPE_MAPPING.put(char.class, "CHAR");
        TYPE_MAPPING.put(Character.class, "CHAR");
        
        // 数值类型
        TYPE_MAPPING.put(byte.class, "NUMBER(3)");
        TYPE_MAPPING.put(Byte.class, "NUMBER(3)");
        TYPE_MAPPING.put(short.class, "NUMBER(5)");
        TYPE_MAPPING.put(Short.class, "NUMBER(5)");
        TYPE_MAPPING.put(int.class, "NUMBER(10)");
        TYPE_MAPPING.put(Integer.class, "NUMBER(10)");
        TYPE_MAPPING.put(long.class, "NUMBER(19)");
        TYPE_MAPPING.put(Long.class, "NUMBER(19)");
        TYPE_MAPPING.put(float.class, "NUMBER(7,2)");
        TYPE_MAPPING.put(Float.class, "NUMBER(7,2)");
        TYPE_MAPPING.put(double.class, "NUMBER(15,2)");
        TYPE_MAPPING.put(Double.class, "NUMBER(15,2)");
        TYPE_MAPPING.put(BigDecimal.class, "NUMBER");
        
        // 布尔类型
        TYPE_MAPPING.put(boolean.class, "NUMBER(1)");
        TYPE_MAPPING.put(Boolean.class, "NUMBER(1)");
        
        // 日期时间类型
        TYPE_MAPPING.put(Date.class, "DATE");
        TYPE_MAPPING.put(java.sql.Date.class, "DATE");
        TYPE_MAPPING.put(java.sql.Time.class, "DATE");
        TYPE_MAPPING.put(java.sql.Timestamp.class, "TIMESTAMP");
        TYPE_MAPPING.put(LocalDate.class, "DATE");
        TYPE_MAPPING.put(LocalTime.class, "DATE");
        TYPE_MAPPING.put(LocalDateTime.class, "TIMESTAMP");
        
        // 二进制类型
        TYPE_MAPPING.put(byte[].class, "BLOB");
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
    
    @Override
    public boolean supports(String databaseProductName) {
        return databaseProductName != null && 
               databaseProductName.toLowerCase().contains("oracle");
    }
    
    @Override
    public String generateCreateTableSql(TableMetadata table) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("CREATE TABLE ");
        if (table.getSchemaName() != null && !table.getSchemaName().isEmpty()) {
            sql.append(escapeIdentifier(table.getSchemaName())).append(".");
        }
        sql.append(escapeIdentifier(table.getTableName())).append(" (\n");
        
        // 添加列定义
        List<String> columnDefinitions = new ArrayList<>();
        for (ColumnMetadata column : table.getColumns()) {
            columnDefinitions.add(getColumnDefinition(column));
        }
        
        // 添加主键约束
        if (!table.getPrimaryKeyColumns().isEmpty()) {
            columnDefinitions.add("  " + getPrimaryKeySql(table.getPrimaryKeyColumns()));
        }
        
        sql.append(String.join(",\n", columnDefinitions));
        sql.append("\n)");
        
        return sql.toString();
    }
    
    @Override
    public String generateAddColumnSql(String tableName, ColumnMetadata column) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " ADD " + 
               getColumnDefinition(column).trim();
    }
    
    @Override
    public String generateModifyColumnSql(String tableName, ColumnMetadata column) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " MODIFY " + 
               getColumnDefinition(column).trim();
    }
    
    @Override
    public String generateDropColumnSql(String tableName, String columnName) {
        return "ALTER TABLE " + escapeIdentifier(tableName) + " DROP COLUMN " + 
               escapeIdentifier(columnName);
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
        sql.append(" (");
        
        List<String> escapedColumns = new ArrayList<>();
        for (String column : index.getColumnNames()) {
            escapedColumns.add(escapeIdentifier(column));
        }
        sql.append(String.join(", ", escapedColumns));
        sql.append(")");
        
        return sql.toString();
    }
    
    @Override
    public String generateDropIndexSql(String tableName, String indexName) {
        return "DROP INDEX " + escapeIdentifier(indexName);
    }
    
    @Override
    public String generateTableExistsSql(String tableName, String schemaName) {
        if (schemaName != null && !schemaName.isEmpty()) {
            return "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER = ? AND TABLE_NAME = ?";
        } else {
            return "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
        }
    }
    
    @Override
    public String generateTableStructureSql(String tableName, String schema) {
        if (schema != null && !schema.isEmpty()) {
            return "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, " +
                   "NULLABLE, DATA_DEFAULT, COLUMN_ID " +
                   "FROM ALL_TAB_COLUMNS " +
                   "WHERE OWNER = ? AND TABLE_NAME = ? " +
                   "ORDER BY COLUMN_ID";
        } else {
            return "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, " +
                   "NULLABLE, DATA_DEFAULT, COLUMN_ID " +
                   "FROM USER_TAB_COLUMNS " +
                   "WHERE TABLE_NAME = ? " +
                   "ORDER BY COLUMN_ID";
        }
    }
    
    @Override
    public String generateIndexInfoSql(String tableName, String schema) {
        if (schema != null && !schema.isEmpty()) {
            return "SELECT i.INDEX_NAME, i.UNIQUENESS, ic.COLUMN_NAME, ic.COLUMN_POSITION " +
                   "FROM ALL_INDEXES i " +
                   "JOIN ALL_IND_COLUMNS ic ON i.INDEX_NAME = ic.INDEX_NAME AND i.OWNER = ic.INDEX_OWNER " +
                   "WHERE i.TABLE_OWNER = ? AND i.TABLE_NAME = ? " +
                   "ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION";
        } else {
            return "SELECT i.INDEX_NAME, i.UNIQUENESS, ic.COLUMN_NAME, ic.COLUMN_POSITION " +
                   "FROM USER_INDEXES i " +
                   "JOIN USER_IND_COLUMNS ic ON i.INDEX_NAME = ic.INDEX_NAME " +
                   "WHERE i.TABLE_NAME = ? " +
                   "ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION";
        }
    }
    
    public List<ColumnMetadata> getTableColumns(Connection connection, String tableName, String schemaName) throws SQLException {
        List<ColumnMetadata> columns = new ArrayList<>();
        
        String sql;
        if (schemaName != null && !schemaName.isEmpty()) {
            sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, " +
                  "NULLABLE, DATA_DEFAULT, COLUMN_ID " +
                  "FROM ALL_TAB_COLUMNS " +
                  "WHERE OWNER = ? AND TABLE_NAME = ? " +
                  "ORDER BY COLUMN_ID";
        } else {
            sql = "SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE, " +
                  "NULLABLE, DATA_DEFAULT, COLUMN_ID " +
                  "FROM USER_TAB_COLUMNS " +
                  "WHERE TABLE_NAME = ? " +
                  "ORDER BY COLUMN_ID";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (schemaName != null && !schemaName.isEmpty()) {
                stmt.setString(1, schemaName.toUpperCase());
                stmt.setString(2, tableName.toUpperCase());
            } else {
                stmt.setString(1, tableName.toUpperCase());
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ColumnMetadata column = new ColumnMetadata();
                    column.setColumnName(rs.getString("COLUMN_NAME"));
                    
                    String dataType = rs.getString("DATA_TYPE");
                    int dataLength = rs.getInt("DATA_LENGTH");
                    int dataPrecision = rs.getInt("DATA_PRECISION");
                    int dataScale = rs.getInt("DATA_SCALE");
                    
                    // 构建完整的数据类型
                    if ("NUMBER".equals(dataType)) {
                        if (dataPrecision > 0) {
                            if (dataScale > 0) {
                                dataType = "NUMBER(" + dataPrecision + "," + dataScale + ")";
                            } else {
                                dataType = "NUMBER(" + dataPrecision + ")";
                            }
                        }
                    } else if ("VARCHAR2".equals(dataType) || "CHAR".equals(dataType)) {
                        if (dataLength > 0) {
                            dataType = dataType + "(" + dataLength + ")";
                        }
                    }
                    
                    column.setDataType(dataType);
                    column.setLength(dataLength);
                    column.setPrecision(dataPrecision);
                    column.setScale(dataScale);
                    column.setNullable("Y".equals(rs.getString("NULLABLE")));
                    
                    String defaultValue = rs.getString("DATA_DEFAULT");
                    if (defaultValue != null) {
                        column.setDefaultValue(defaultValue.trim());
                    }
                    
                    columns.add(column);
                }
            }
        }
        
        return columns;
    }
    
    public List<String> getPrimaryKeyColumns(Connection connection, String tableName, String schemaName) throws SQLException {
        List<String> primaryKeys = new ArrayList<>();
        
        String sql;
        if (schemaName != null && !schemaName.isEmpty()) {
            sql = "SELECT COLUMN_NAME FROM ALL_CONS_COLUMNS " +
                  "WHERE OWNER = ? AND TABLE_NAME = ? AND CONSTRAINT_NAME IN (" +
                  "SELECT CONSTRAINT_NAME FROM ALL_CONSTRAINTS " +
                  "WHERE OWNER = ? AND TABLE_NAME = ? AND CONSTRAINT_TYPE = 'P') " +
                  "ORDER BY POSITION";
        } else {
            sql = "SELECT COLUMN_NAME FROM USER_CONS_COLUMNS " +
                  "WHERE TABLE_NAME = ? AND CONSTRAINT_NAME IN (" +
                  "SELECT CONSTRAINT_NAME FROM USER_CONSTRAINTS " +
                  "WHERE TABLE_NAME = ? AND CONSTRAINT_TYPE = 'P') " +
                  "ORDER BY POSITION";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (schemaName != null && !schemaName.isEmpty()) {
                stmt.setString(1, schemaName.toUpperCase());
                stmt.setString(2, tableName.toUpperCase());
                stmt.setString(3, schemaName.toUpperCase());
                stmt.setString(4, tableName.toUpperCase());
            } else {
                stmt.setString(1, tableName.toUpperCase());
                stmt.setString(2, tableName.toUpperCase());
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }
        }
        
        return primaryKeys;
    }
    
    public List<IndexMetadata> getTableIndexes(Connection connection, String tableName, String schemaName) throws SQLException {
        Map<String, IndexMetadata> indexMap = new HashMap<>();
        
        String sql;
        if (schemaName != null && !schemaName.isEmpty()) {
            sql = "SELECT i.INDEX_NAME, i.UNIQUENESS, ic.COLUMN_NAME, ic.COLUMN_POSITION " +
                  "FROM ALL_INDEXES i " +
                  "JOIN ALL_IND_COLUMNS ic ON i.OWNER = ic.INDEX_OWNER AND i.INDEX_NAME = ic.INDEX_NAME " +
                  "WHERE i.OWNER = ? AND i.TABLE_NAME = ? " +
                  "AND i.INDEX_TYPE = 'NORMAL' " +
                  "ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION";
        } else {
            sql = "SELECT i.INDEX_NAME, i.UNIQUENESS, ic.COLUMN_NAME, ic.COLUMN_POSITION " +
                  "FROM USER_INDEXES i " +
                  "JOIN USER_IND_COLUMNS ic ON i.INDEX_NAME = ic.INDEX_NAME " +
                  "WHERE i.TABLE_NAME = ? " +
                  "AND i.INDEX_TYPE = 'NORMAL' " +
                  "ORDER BY i.INDEX_NAME, ic.COLUMN_POSITION";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (schemaName != null && !schemaName.isEmpty()) {
                stmt.setString(1, schemaName.toUpperCase());
                stmt.setString(2, tableName.toUpperCase());
            } else {
                stmt.setString(1, tableName.toUpperCase());
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String uniqueness = rs.getString("UNIQUENESS");
                    String columnName = rs.getString("COLUMN_NAME");
                    
                    IndexMetadata index = indexMap.get(indexName);
                    if (index == null) {
                        index = new IndexMetadata();
                        index.setIndexName(indexName);
                        index.setUnique("UNIQUE".equals(uniqueness));
                        index.setTableName(tableName);
                        indexMap.put(indexName, index);
                    }
                    
                    index.addColumn(columnName);
                }
            }
        }
        
        return new ArrayList<>(indexMap.values());
    }
    
    @Override
    public String mapJavaTypeToDbType(Class<?> javaType, int length, int precision, int scale) {
        String baseType = TYPE_MAPPING.get(javaType);
        if (baseType == null) {
            baseType = "VARCHAR2"; // 默认类型
        }
        
        // 处理长度和精度
        if ("VARCHAR2".equals(baseType) || "CHAR".equals(baseType)) {
            if (length > 0) {
                return baseType + "(" + length + ")";
            } else {
                return baseType + "(255)"; // 默认长度
            }
        } else if ("NUMBER".equals(baseType)) {
            if (precision > 0) {
                if (scale > 0) {
                    return baseType + "(" + precision + "," + scale + ")";
                } else {
                    return baseType + "(" + precision + ")";
                }
            }
        }
        
        return baseType;
    }
    
    @Override
    public String getAutoIncrementSql() {
        // Oracle使用序列和触发器实现自增
        return "";
    }
    
    @Override
    public String getPrimaryKeySql(String columnName) {
        return "PRIMARY KEY (" + escapeIdentifier(columnName) + ")";
    }
    
    public String getPrimaryKeySql(List<String> columnNames) {
        List<String> escapedColumns = new ArrayList<>();
        for (String column : columnNames) {
            escapedColumns.add(escapeIdentifier(column));
        }
        return "PRIMARY KEY (" + String.join(", ", escapedColumns) + ")";
    }
    
    @Override
    public String getUniqueSql(String columnName) {
        return "UNIQUE (" + escapeIdentifier(columnName) + ")";
    }
    
    public String getUniqueConstraintSql(String constraintName, List<String> columnNames) {
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
        // Oracle需要使用COMMENT ON语句单独添加注释
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
            
            // 默认值
            if (column.hasDefaultValue()) {
                definition.append(" ").append(getDefaultValueSql(column.getDefaultValue()));
            }
        }
        
        return definition.toString();
    }
}