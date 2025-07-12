/*
 * Copyright (c) 2025 xingge
 * 
 * 异步表结构维护服务
 * 负责数据库表结构的异步创建、更新和维护
 * 不影响主流程，支持表结构智能对比和更新
 * 
 * @author ruozhuliufeng
 * @since 2025-07-12
 */
package tech.msop.core.db.service;

import tech.msop.core.db.config.XingGeDataBaseCconfig;
import tech.msop.core.db.dialect.Dialect;
import tech.msop.core.db.metadata.ColumnMetadata;
import tech.msop.core.db.metadata.TableMetadata;
import tech.msop.core.db.parser.EntityMetadataParser;
import tech.msop.core.tool.async.AsyncProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 异步表结构维护服务
 * 提供异步的表结构维护功能，不影响主流程
 * 支持表结构智能对比和增量更新
 * 
 * @author ruozhuliufeng
 * @since 2025-07-12
 */
@Slf4j
@Service
public class AsyncTableMaintenanceService {
    
    private final DataSource dataSource;
    private final Dialect dialect;
    private final EntityMetadataParser entityMetadataParser;
    private final TableMaintenanceService tableMaintenanceService;
    private final XingGeDataBaseCconfig config;
    private final AsyncProcessor asyncProcessor;
    
    /**
     * 维护任务状态缓存
     * key: 表名, value: 维护状态
     */
    private final Map<String, MaintenanceStatus> maintenanceStatusMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * 
     * @param dataSource 数据源
     * @param dialect 数据库方言
     * @param entityMetadataParser 实体元数据解析器
     * @param tableMaintenanceService 表维护服务
     * @param config 配置
     */
    @Autowired
    public AsyncTableMaintenanceService(DataSource dataSource, 
                                      Dialect dialect,
                                      EntityMetadataParser entityMetadataParser,
                                      TableMaintenanceService tableMaintenanceService,
                                      XingGeDataBaseCconfig config,
                                      AsyncProcessor asyncProcessor) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.entityMetadataParser = entityMetadataParser;
        this.tableMaintenanceService = tableMaintenanceService;
        this.config = config;
        this.asyncProcessor = asyncProcessor;
    }
    
    /**
     * 异步维护单个实体类对应的表结构
     * 
     * @param entityClass 实体类
     * @return 异步任务结果
     */
    public CompletableFuture<MaintenanceResult> maintainTableAsync(Class<?> entityClass) {
        if (!config.getTableMaintenance().isAsyncEnabled()) {
            log.debug("异步处理未启用，使用同步方式处理表: {}", entityClass.getSimpleName());
            return CompletableFuture.completedFuture(maintainTableSync(entityClass));
        }
        
        return asyncProcessor.executeAsyncWithResult(
            () -> maintainTableInternal(entityClass),
            "维护表结构-" + entityClass.getSimpleName()
        );
    }
    
    /**
     * 异步维护多个实体类对应的表结构
     * 
     * @param entityClasses 实体类列表
     * @return 异步任务结果
     */
    public CompletableFuture<List<MaintenanceResult>> maintainTablesAsync(List<Class<?>> entityClasses) {
        if (!config.getTableMaintenance().isAsyncEnabled()) {
            log.debug("异步处理未启用，使用同步方式处理 {} 个表", entityClasses.size());
            return CompletableFuture.completedFuture(maintainTablesSync(entityClasses));
        }
        
        return asyncProcessor.executeAsyncWithResult(
            () -> {
                List<MaintenanceResult> results = entityClasses.parallelStream()
                    .map(this::maintainTableInternal)
                    .collect(Collectors.toList());
                
                long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
                long failureCount = results.size() - successCount;
                
                log.info("异步批量表结构维护完成。成功: {}, 失败: {}", successCount, failureCount);
                return results;
            },
            "批量维护表结构-" + entityClasses.size() + "个表"
        );
    }
    
    /**
     * 智能表结构对比和维护
     * 只有在表结构不一致时才进行更新
     * 
     * @param entityClass 实体类
     * @return 维护结果
     */
    private MaintenanceResult maintainTableInternal(Class<?> entityClass) {
        String tableName = null;
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查是否需要自动维护
            if (!EntityMetadataParser.shouldAutoMaintain(entityClass)) {
                log.debug("实体 {} 不需要自动维护，跳过", entityClass.getName());
                return MaintenanceResult.skipped(entityClass.getSimpleName(), "不需要自动维护");
            }
            
            // 解析实体元数据
            TableMetadata expectedMetadata = EntityMetadataParser.parseEntity(entityClass);
            if (expectedMetadata == null) {
                log.warn("解析实体元数据失败: {}", entityClass.getName());
                return MaintenanceResult.failure(entityClass.getSimpleName(), "解析实体元数据失败");
            }
            
            tableName = expectedMetadata.getTableName();
            
            // 检查维护状态，避免重复维护
            MaintenanceStatus status = maintenanceStatusMap.get(tableName);
            if (status != null && status.isInProgress()) {
                log.debug("表 {} 正在维护中，跳过", tableName);
                return MaintenanceResult.skipped(tableName, "正在维护中");
            }
            
            // 标记为维护中
            maintenanceStatusMap.put(tableName, MaintenanceStatus.inProgress());
            
            try (Connection connection = dataSource.getConnection()) {
                MaintenanceResult result;
                
                if (!tableMaintenanceService.tableExists(tableName)) {
                    // 表不存在，创建表
                    log.info("表 {} 不存在，开始创建", tableName);
                    tableMaintenanceService.maintainTable(expectedMetadata);
                    result = MaintenanceResult.success(tableName, "表创建成功", 
                        System.currentTimeMillis() - startTime);
                } else {
                    // 表存在，智能对比表结构
                    result = smartUpdateTableStructure(connection, expectedMetadata, startTime);
                }
                
                // 更新维护状态
                maintenanceStatusMap.put(tableName, MaintenanceStatus.completed(result.isSuccess()));
                return result;
                
            } catch (SQLException e) {
                log.error("维护表 {} 时发生数据库异常", tableName, e);
                maintenanceStatusMap.put(tableName, MaintenanceStatus.failed(e.getMessage()));
                return MaintenanceResult.failure(tableName, "数据库异常: " + e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("维护表结构时发生异常: {}", entityClass.getName(), e);
            if (tableName != null) {
                maintenanceStatusMap.put(tableName, MaintenanceStatus.failed(e.getMessage()));
            }
            return MaintenanceResult.failure(entityClass.getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * 智能更新表结构
     * 对比当前表结构和期望表结构，只在不一致时进行更新
     * 
     * @param connection 数据库连接
     * @param expectedMetadata 期望的表元数据
     * @param startTime 开始时间
     * @return 维护结果
     */
    private MaintenanceResult smartUpdateTableStructure(Connection connection, 
                                                       TableMetadata expectedMetadata, 
                                                       long startTime) {
        String tableName = expectedMetadata.getTableName();
        
        try {
            // 获取当前表结构
            TableMetadata currentMetadata = tableMaintenanceService.getTableStructure(tableName);
            if (currentMetadata == null) {
                log.warn("无法获取表 {} 的当前结构", tableName);
                return MaintenanceResult.failure(tableName, "无法获取当前表结构");
            }
            
            // 智能对比表结构
            StructureComparisonResult comparison = compareTableStructures(currentMetadata, expectedMetadata);
            
            if (comparison.isIdentical()) {
                log.debug("表 {} 结构一致，无需更新", tableName);
                return MaintenanceResult.success(tableName, "表结构一致，无需更新", 
                    System.currentTimeMillis() - startTime);
            }
            
            // 表结构不一致，进行更新
            log.info("表 {} 结构不一致，开始更新。差异: {}", tableName, comparison.getDifferences());
            
            // 执行表结构更新
            tableMaintenanceService.maintainTable(expectedMetadata);
            
            return MaintenanceResult.success(tableName, 
                String.format("表结构更新成功。差异: %s", comparison.getDifferences()), 
                System.currentTimeMillis() - startTime);
                
        } catch (Exception e) {
            log.error("智能更新表 {} 结构时发生异常", tableName, e);
            return MaintenanceResult.failure(tableName, "更新表结构异常: " + e.getMessage());
        }
    }
    
    /**
     * 对比表结构
     * 
     * @param currentMetadata 当前表元数据
     * @param expectedMetadata 期望的表元数据
     * @return 对比结果
     */
    private StructureComparisonResult compareTableStructures(TableMetadata currentMetadata, 
                                                           TableMetadata expectedMetadata) {
        StructureComparisonResult result = new StructureComparisonResult();
        
        // 对比列结构
        Map<String, ColumnMetadata> currentColumns = currentMetadata.getColumns().stream()
            .collect(Collectors.toMap(ColumnMetadata::getColumnName, col -> col));
        
        Map<String, ColumnMetadata> expectedColumns = expectedMetadata.getColumns().stream()
            .collect(Collectors.toMap(ColumnMetadata::getColumnName, col -> col));
        
        // 检查新增列
        for (String columnName : expectedColumns.keySet()) {
            if (!currentColumns.containsKey(columnName)) {
                result.addDifference("新增列: " + columnName);
            }
        }
        
        // 检查列变更
        for (Map.Entry<String, ColumnMetadata> entry : expectedColumns.entrySet()) {
            String columnName = entry.getKey();
            ColumnMetadata expectedColumn = entry.getValue();
            ColumnMetadata currentColumn = currentColumns.get(columnName);
            
            if (currentColumn != null && needsColumnUpdate(currentColumn, expectedColumn)) {
                result.addDifference("修改列: " + columnName);
            }
        }
        
        // 检查删除列（如果允许）
        if (config.getTableMaintenance().isAllowDropColumn()) {
            for (String columnName : currentColumns.keySet()) {
                if (!expectedColumns.containsKey(columnName)) {
                    result.addDifference("删除列: " + columnName);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 检查列是否需要更新
     * 
     * @param currentColumn 当前列元数据
     * @param expectedColumn 期望的列元数据
     * @return 是否需要更新
     */
    private boolean needsColumnUpdate(ColumnMetadata currentColumn, ColumnMetadata expectedColumn) {
        // 数据类型对比
        if (!currentColumn.getDataType().equalsIgnoreCase(expectedColumn.getDataType())) {
            return true;
        }
        
        // 可空性对比
        if (currentColumn.isNullable() != expectedColumn.isNullable()) {
            return true;
        }
        
        // 自增属性对比
        if (currentColumn.isAutoIncrement() != expectedColumn.isAutoIncrement()) {
            return true;
        }
        
        // 长度对比（如果适用）
        if (expectedColumn.getLength() > 0 && currentColumn.getLength() != expectedColumn.getLength()) {
            return true;
        }
        
        // 精度对比（如果适用）
        if (expectedColumn.getPrecision() > 0 && currentColumn.getPrecision() != expectedColumn.getPrecision()) {
            return true;
        }
        
        // 小数位数对比（如果适用）
        if (expectedColumn.getScale() > 0 && currentColumn.getScale() != expectedColumn.getScale()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 同步方式维护表结构（兜底方案）
     * 
     * @param entityClass 实体类
     * @return 维护结果
     */
    private MaintenanceResult maintainTableSync(Class<?> entityClass) {
        try {
            tableMaintenanceService.maintainTable(entityClass);
            return MaintenanceResult.success(entityClass.getSimpleName(), "同步维护成功", 0);
        } catch (Exception e) {
            return MaintenanceResult.failure(entityClass.getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * 同步方式维护多个表结构（兜底方案）
     * 
     * @param entityClasses 实体类列表
     * @return 维护结果列表
     */
    private List<MaintenanceResult> maintainTablesSync(List<Class<?>> entityClasses) {
        return entityClasses.stream()
            .map(this::maintainTableSync)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取表维护状态
     * 
     * @param tableName 表名
     * @return 维护状态
     */
    public MaintenanceStatus getMaintenanceStatus(String tableName) {
        return maintenanceStatusMap.get(tableName);
    }
    
    /**
     * 清理维护状态缓存
     */
    public void clearMaintenanceStatus() {
        maintenanceStatusMap.clear();
    }
    
    /**
     * 维护结果类
     */
    public static class MaintenanceResult {
        private final String tableName;
        private final boolean success;
        private final String message;
        private final long executionTimeMs;
        
        private MaintenanceResult(String tableName, boolean success, String message, long executionTimeMs) {
            this.tableName = tableName;
            this.success = success;
            this.message = message;
            this.executionTimeMs = executionTimeMs;
        }
        
        public static MaintenanceResult success(String tableName, String message, long executionTimeMs) {
            return new MaintenanceResult(tableName, true, message, executionTimeMs);
        }
        
        public static MaintenanceResult failure(String tableName, String message) {
            return new MaintenanceResult(tableName, false, message, 0);
        }
        
        public static MaintenanceResult skipped(String tableName, String reason) {
            return new MaintenanceResult(tableName, true, "跳过: " + reason, 0);
        }
        
        // Getters
        public String getTableName() { return tableName; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        
        @Override
        public String toString() {
            return String.format("MaintenanceResult{tableName='%s', success=%s, message='%s', executionTimeMs=%d}",
                tableName, success, message, executionTimeMs);
        }
    }
    
    /**
     * 维护状态类
     */
    public static class MaintenanceStatus {
        private final boolean inProgress;
        private final boolean completed;
        private final boolean success;
        private final String errorMessage;
        private final long timestamp;
        
        private MaintenanceStatus(boolean inProgress, boolean completed, boolean success, String errorMessage) {
            this.inProgress = inProgress;
            this.completed = completed;
            this.success = success;
            this.errorMessage = errorMessage;
            this.timestamp = System.currentTimeMillis();
        }
        
        public static MaintenanceStatus inProgress() {
            return new MaintenanceStatus(true, false, false, null);
        }
        
        public static MaintenanceStatus completed(boolean success) {
            return new MaintenanceStatus(false, true, success, null);
        }
        
        public static MaintenanceStatus failed(String errorMessage) {
            return new MaintenanceStatus(false, true, false, errorMessage);
        }
        
        // Getters
        public boolean isInProgress() { return inProgress; }
        public boolean isCompleted() { return completed; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 表结构对比结果类
     */
    private static class StructureComparisonResult {
        private final List<String> differences = new java.util.ArrayList<>();
        
        public void addDifference(String difference) {
            differences.add(difference);
        }
        
        public boolean isIdentical() {
            return differences.isEmpty();
        }
        
        public String getDifferences() {
            return String.join(", ", differences);
        }
    }
}