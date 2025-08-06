/*
 * Copyright (c) 2025 xingge
 * 
 * 表结构维护服务
 * 负责数据库表结构的自动创建、更新和维护
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
package tech.msop.core.db.service;

import tech.msop.core.db.dialect.Dialect;
import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.IndexMetadata;
import tech.msop.core.db.metadata.TableMetadata;
import tech.msop.core.db.parser.EntityMetadataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表结构维护服务
 * 负责数据库表结构的自动创建、更新和维护
 * 
 * @author ruozhuliufeng
 * @since 2025-07-11
 */
@Slf4j
@Service
public class TableMaintenanceService {
    
    private final DataSource dataSource;
    private final Dialect dialect;
    private final EntityMetadataParser entityMetadataParser;
    
    /**
     * 构造函数
     * 
     * @param dataSource 数据源
     * @param dialect 数据库方言
     * @param entityMetadataParser 实体元数据解析器
     */
    public TableMaintenanceService(DataSource dataSource, Dialect dialect, EntityMetadataParser entityMetadataParser) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.entityMetadataParser = entityMetadataParser;
    }
    
    /**
     * 默认构造函数（用于Spring自动装配）
     */
    @Autowired
    public TableMaintenanceService(DataSource dataSource, Dialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.entityMetadataParser = null;
    }
    
    /**
     * 维护单个实体类对应的表结构
     * 
     * @param entityClass 实体类
     */
    public void maintainTable(Class<?> entityClass) {
        if (!EntityMetadataParser.shouldAutoMaintain(entityClass)) {
            log.debug("Entity {} does not require auto maintenance, skipping", entityClass.getName());
            return;
        }
        
        try {
            TableMetadata expectedMetadata = EntityMetadataParser.parseEntity(entityClass);
            if (expectedMetadata == null) {
                log.warn("Failed to parse entity metadata for {}", entityClass.getName());
                return;
            }
            
            maintainTable(expectedMetadata);
        } catch (Exception e) {
            log.error("Failed to maintain table for entity {}", entityClass.getName(), e);
            throw new RuntimeException("Failed to maintain table for entity " + entityClass.getName(), e);
        }
    }
    
    /**
     * 维护表结构
     * 
     * @param expectedMetadata 期望的表元数据
     */
    public void maintainTable(TableMetadata expectedMetadata) {
        String tableName = expectedMetadata.getTableName();
        log.info("Starting table maintenance for: {}", tableName);
        
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, tableName)) {
                // 表不存在，创建表
                createTable(connection, expectedMetadata);
            } else {
                // 表存在，检查并更新表结构
                updateTableStructure(connection, expectedMetadata);
            }
            
            log.info("Table maintenance completed for: {}", tableName);
        } catch (SQLException e) {
            log.error("Failed to maintain table: {}", tableName, e);
            throw new RuntimeException("Failed to maintain table: " + tableName, e);
        }
    }
    
    /**
     * 创建表
     * 
     * @param connection 数据库连接
     * @param tableMetadata 表元数据
     * @throws SQLException SQL异常
     */
    private void createTable(Connection connection, TableMetadata tableMetadata) throws SQLException {
        String tableName = tableMetadata.getTableName();
        log.info("Creating table: {}", tableName);
        
        String createTableSql = dialect.generateCreateTableSql(tableMetadata);
        log.debug("Create table SQL: {}", createTableSql);
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSql);
            log.info("Table created successfully: {}", tableName);
        }
        
        // 创建索引
        createIndexes(connection, tableName, tableMetadata.getIndexes());
    }
    
    /**
     * 更新表结构
     * 
     * @param connection 数据库连接
     * @param expectedMetadata 期望的表元数据
     * @throws SQLException SQL异常
     */
    private void updateTableStructure(Connection connection, TableMetadata expectedMetadata) throws SQLException {
        String tableName = expectedMetadata.getTableName();
        log.info("Updating table structure: {}", tableName);
        
        // 获取当前表结构
        TableMetadata currentMetadata = getTableStructure(connection, tableName);
        
        // 比较并更新列
        updateColumns(connection, tableName, currentMetadata, expectedMetadata);
        
        // 比较并更新索引
        updateIndexes(connection, tableName, currentMetadata.getIndexes(), expectedMetadata.getIndexes());
        
        log.info("Table structure updated: {}", tableName);
    }
    
    /**
     * 更新列结构
     * 
     * @param connection 数据库连接
     * @param tableName 表名
     * @param currentMetadata 当前表元数据
     * @param expectedMetadata 期望的表元数据
     * @throws SQLException SQL异常
     */
    private void updateColumns(Connection connection, String tableName, 
                              TableMetadata currentMetadata, TableMetadata expectedMetadata) throws SQLException {
        
        Map<String, ColumnMetadata> currentColumns = currentMetadata.getColumns().stream()
                .collect(Collectors.toMap(ColumnMetadata::getColumnName, col -> col));
        
        Map<String, ColumnMetadata> expectedColumns = expectedMetadata.getColumns().stream()
                .collect(Collectors.toMap(ColumnMetadata::getColumnName, col -> col));
        
        try (Statement statement = connection.createStatement()) {
            // 添加新列
            for (ColumnMetadata expectedColumn : expectedMetadata.getColumns()) {
                String columnName = expectedColumn.getColumnName();
                if (!currentColumns.containsKey(columnName)) {
                    String addColumnSql = dialect.generateAddColumnSql(tableName, expectedColumn);
                    log.info("Adding column {} to table {}", columnName, tableName);
                    log.debug("Add column SQL: {}", addColumnSql);
                    statement.execute(addColumnSql);
                }
            }
            
            // 修改现有列（如果需要）
            for (ColumnMetadata expectedColumn : expectedMetadata.getColumns()) {
                String columnName = expectedColumn.getColumnName();
                ColumnMetadata currentColumn = currentColumns.get(columnName);
                
                if (currentColumn != null && needsColumnUpdate(currentColumn, expectedColumn)) {
                    String modifyColumnSql = dialect.generateModifyColumnSql(tableName, expectedColumn);
                    log.info("Modifying column {} in table {}", columnName, tableName);
                    log.debug("Modify column SQL: {}", modifyColumnSql);
                    statement.execute(modifyColumnSql);
                }
            }
            
            // 注意：这里不删除列，因为删除列是危险操作，可能导致数据丢失
            // 如果需要删除列，可以在这里添加相应逻辑
        }
    }
    
    /**
     * 检查列是否需要更新
     * 
     * @param currentColumn 当前列
     * @param expectedColumn 期望列
     * @return 是否需要更新
     */
    private boolean needsColumnUpdate(ColumnMetadata currentColumn, ColumnMetadata expectedColumn) {
        // 简单的比较逻辑，可以根据需要扩展
        return !currentColumn.getDataType().equalsIgnoreCase(expectedColumn.getDataType()) ||
               currentColumn.isNullable() != expectedColumn.isNullable() ||
               currentColumn.isAutoIncrement() != expectedColumn.isAutoIncrement();
    }
    
    /**
     * 更新索引
     * 
     * @param connection 数据库连接
     * @param tableName 表名
     * @param currentIndexes 当前索引列表
     * @param expectedIndexes 期望索引列表
     * @throws SQLException SQL异常
     */
    private void updateIndexes(Connection connection, String tableName,
                              List<IndexMetadata> currentIndexes, List<IndexMetadata> expectedIndexes) throws SQLException {
        
        Map<String, IndexMetadata> currentIndexMap = currentIndexes.stream()
                .collect(Collectors.toMap(IndexMetadata::getIndexName, idx -> idx));
        
        Map<String, IndexMetadata> expectedIndexMap = expectedIndexes.stream()
                .collect(Collectors.toMap(IndexMetadata::getIndexName, idx -> idx));
        
        try (Statement statement = connection.createStatement()) {
            // 删除不需要的索引
            for (IndexMetadata currentIndex : currentIndexes) {
                String indexName = currentIndex.getIndexName();
                if (!expectedIndexMap.containsKey(indexName)) {
                    String dropIndexSql = dialect.generateDropIndexSql(tableName, indexName);
                    log.info("Dropping index {} from table {}", indexName, tableName);
                    log.debug("Drop index SQL: {}", dropIndexSql);
                    statement.execute(dropIndexSql);
                }
            }
            
            // 创建新索引
            for (IndexMetadata expectedIndex : expectedIndexes) {
                String indexName = expectedIndex.getIndexName();
                IndexMetadata currentIndex = currentIndexMap.get(indexName);
                
                if (currentIndex == null) {
                    // 索引不存在，创建新索引
                    String createIndexSql = dialect.generateCreateIndexSql(tableName, expectedIndex);
                    log.info("Creating index {} on table {}", indexName, tableName);
                    log.debug("Create index SQL: {}", createIndexSql);
                    statement.execute(createIndexSql);
                } else if (!currentIndex.isSameAs(expectedIndex)) {
                    // 索引存在但不同，先删除再创建
                    String dropIndexSql = dialect.generateDropIndexSql(tableName, indexName);
                    String createIndexSql = dialect.generateCreateIndexSql(tableName, expectedIndex);
                    
                    log.info("Recreating index {} on table {}", indexName, tableName);
                    log.debug("Drop index SQL: {}", dropIndexSql);
                    log.debug("Create index SQL: {}", createIndexSql);
                    
                    statement.execute(dropIndexSql);
                    statement.execute(createIndexSql);
                }
            }
        }
    }
    
    /**
     * 创建索引
     * 
     * @param connection 数据库连接
     * @param tableName 表名
     * @param indexes 索引列表
     * @throws SQLException SQL异常
     */
    private void createIndexes(Connection connection, String tableName, List<IndexMetadata> indexes) throws SQLException {
        if (indexes == null || indexes.isEmpty()) {
            return;
        }
        
        try (Statement statement = connection.createStatement()) {
            for (IndexMetadata index : indexes) {
                String createIndexSql = dialect.generateCreateIndexSql(tableName, index);
                log.info("Creating index {} on table {}", index.getIndexName(), tableName);
                log.debug("Create index SQL: {}", createIndexSql);
                statement.execute(createIndexSql);
            }
        }
    }
    
    /**
     * 批量维护多个实体类对应的表结构
     * 
     * @param entityClasses 实体类列表
     */
    public void maintainTables(List<Class<?>> entityClasses) {
        if (entityClasses == null || entityClasses.isEmpty()) {
            log.info("No entity classes to maintain");
            return;
        }
        
        log.info("Starting batch table maintenance for {} entities", entityClasses.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Class<?> entityClass : entityClasses) {
            try {
                maintainTable(entityClass);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to maintain table for entity {}", entityClass.getName(), e);
                failureCount++;
            }
        }
        
        log.info("Batch table maintenance completed. Success: {}, Failure: {}", successCount, failureCount);
    }
    
    /**
     * 检查表是否存在
     * 
     * @param connection 数据库连接
     * @param tableName 表名
     * @return 是否存在
     */
    private boolean tableExists(Connection connection, String tableName) {
        try {
            String sql = dialect.generateTableExistsSql(tableName, null);
            try (Statement statement = connection.createStatement();
                 java.sql.ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check if table exists: {}", tableName, e);
            throw new RuntimeException("Failed to check if table exists: " + tableName, e);
        }
    }
    
    /**
     * 检查表是否存在
     * 
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            return tableExists(connection, tableName);
        } catch (SQLException e) {
            log.error("Failed to check if table exists: {}", tableName, e);
            return false;
        }
    }
    
    /**
     * 获取表结构信息
     * 
     * @param connection 数据库连接
     * @param tableName 表名
     * @return 表元数据
     */
    private TableMetadata getTableStructure(Connection connection, String tableName) {
        try {
            // 这里需要实现具体的表结构查询逻辑
            // 暂时返回一个空的表元数据，避免编译错误
            return TableMetadata.builder()
                    .tableName(tableName)
                    .build();
        } catch (Exception e) {
            log.error("Failed to get table structure: {}", tableName, e);
            throw new RuntimeException("Failed to get table structure: " + tableName, e);
        }
    }
    
    /**
     * 获取表结构信息
     * 
     * @param tableName 表名
     * @return 表元数据
     */
    public TableMetadata getTableStructure(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            return getTableStructure(connection, tableName);
        } catch (SQLException e) {
            log.error("Failed to get table structure: {}", tableName, e);
            throw new RuntimeException("Failed to get table structure: " + tableName, e);
        }
    }
    
    /**
     * 验证实体类与数据库表结构的一致性
     * 
     * @param entityClass 实体类
     * @return 验证结果
     */
    public TableValidationResult validateTable(Class<?> entityClass) {
        TableMetadata expectedMetadata = EntityMetadataParser.parseEntity(entityClass);
        if (expectedMetadata == null) {
            return TableValidationResult.builder()
                    .valid(false)
                    .message("Failed to parse entity metadata")
                    .build();
        }
        
        String tableName = expectedMetadata.getTableName();
        
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, tableName)) {
                return TableValidationResult.builder()
                        .valid(false)
                        .message("Table does not exist: " + tableName)
                        .build();
            }
            
            TableMetadata currentMetadata = getTableStructure(connection, tableName);
            return validateTableStructure(currentMetadata, expectedMetadata);
            
        } catch (SQLException e) {
            log.error("Failed to validate table: {}", tableName, e);
            return TableValidationResult.builder()
                    .valid(false)
                    .message("Failed to validate table: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 验证表结构
     * 
     * @param currentMetadata 当前表元数据
     * @param expectedMetadata 期望表元数据
     * @return 验证结果
     */
    private TableValidationResult validateTableStructure(TableMetadata currentMetadata, TableMetadata expectedMetadata) {
        TableValidationResult.TableValidationResultBuilder resultBuilder = TableValidationResult.builder()
                .valid(true);
        
        StringBuilder messageBuilder = new StringBuilder();
        
        // 验证列
        Map<String, ColumnMetadata> currentColumns = currentMetadata.getColumns().stream()
                .collect(Collectors.toMap(ColumnMetadata::getColumnName, col -> col));
        
        for (ColumnMetadata expectedColumn : expectedMetadata.getColumns()) {
            String columnName = expectedColumn.getColumnName();
            ColumnMetadata currentColumn = currentColumns.get(columnName);
            
            if (currentColumn == null) {
                resultBuilder.valid(false);
                messageBuilder.append("Missing column: ").append(columnName).append("; ");
            } else if (needsColumnUpdate(currentColumn, expectedColumn)) {
                resultBuilder.valid(false);
                messageBuilder.append("Column mismatch: ").append(columnName).append("; ");
            }
        }
        
        // 验证索引
        Map<String, IndexMetadata> currentIndexes = currentMetadata.getIndexes().stream()
                .collect(Collectors.toMap(IndexMetadata::getIndexName, idx -> idx));
        
        for (IndexMetadata expectedIndex : expectedMetadata.getIndexes()) {
            String indexName = expectedIndex.getIndexName();
            IndexMetadata currentIndex = currentIndexes.get(indexName);
            
            if (currentIndex == null) {
                resultBuilder.valid(false);
                messageBuilder.append("Missing index: ").append(indexName).append("; ");
            } else if (!currentIndex.isSameAs(expectedIndex)) {
                resultBuilder.valid(false);
                messageBuilder.append("Index mismatch: ").append(indexName).append("; ");
            }
        }
        
        String message = messageBuilder.toString();
        if (message.isEmpty()) {
            message = "Table structure is valid";
        }
        
        return resultBuilder.message(message).build();
    }
    
    /**
     * 表验证结果
     */
    @lombok.Data
    @lombok.Builder
    public static class TableValidationResult {
        private boolean valid;
        private String message;
    }
}