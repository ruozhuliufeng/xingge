/**
 * 数据库配置类
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库配置类
 * 提供数据库相关的基础配置
 */
@ConfigurationProperties(prefix = "xg.db")
public class XingGeDataBaseCconfig {
    
    /**
     * 是否启用数据库功能
     */
    private boolean enabled = true;
    
    /**
     * 数据库类型（自动检测）
     */
    private String databaseType;
    
    /**
     * 默认schema名称
     */
    private String defaultSchema;
    
    /**
     * 是否打印SQL语句
     */
    private boolean showSql = false;
    
    /**
     * 是否格式化SQL语句
     */
    private boolean formatSql = false;

    /**
     * 数据库表结构配置
     */
    private TableMaintenanceConfig tableMaintenance;
    
    /**
     * 获取是否启用数据库功能
     * 
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置是否启用数据库功能
     * 
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 获取数据库类型
     * 
     * @return 数据库类型
     */
    public String getDatabaseType() {
        return databaseType;
    }
    
    /**
     * 设置数据库类型
     * 
     * @param databaseType 数据库类型
     */
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }
    
    /**
     * 获取默认schema名称
     * 
     * @return 默认schema名称
     */
    public String getDefaultSchema() {
        return defaultSchema;
    }
    
    /**
     * 设置默认schema名称
     * 
     * @param defaultSchema 默认schema名称
     */
    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }
    
    /**
     * 获取是否打印SQL语句
     * 
     * @return 是否打印SQL
     */
    public boolean isShowSql() {
        return showSql;
    }
    
    /**
     * 设置是否打印SQL语句
     * 
     * @param showSql 是否打印SQL
     */
    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }
    
    /**
     * 获取是否格式化SQL语句
     * 
     * @return 是否格式化SQL
     */
    public boolean isFormatSql() {
        return formatSql;
    }
    
    /**
     * 设置是否格式化SQL语句
     * 
     * @param formatSql 是否格式化SQL
     */
    public void setFormatSql(boolean formatSql) {
        this.formatSql = formatSql;
    }

    public TableMaintenanceConfig getTableMaintenance() {
        return tableMaintenance;
    }

    public void setTableMaintenance(TableMaintenanceConfig tableMaintenance) {
        this.tableMaintenance = tableMaintenance;
    }

    public static class TableMaintenanceConfig {

        /**
         * 是否启用表结构自动维护
         */
        private boolean enabled = false;

        /**
         * 是否在应用启动时自动执行表结构维护
         */
        private boolean autoExecuteOnStartup = true;

        /**
         * 是否允许删除列（谨慎使用）
         */
        private boolean allowDropColumn = false;

        /**
         * 是否允许删除索引
         */
        private boolean allowDropIndex = true;

        /**
         * 是否允许修改列类型（谨慎使用）
         */
        private boolean allowModifyColumnType = false;

        /**
         * 是否在执行前进行验证
         */
        private boolean validateBeforeExecution = true;

        /**
         * 是否打印SQL语句到日志
         */
        private boolean printSql = true;

        /**
         * 是否在测试环境下执行（防止误操作生产环境）
         */
        private boolean executeInTestEnvironment = true;

        /**
         * 需要扫描的实体类包路径
         */
        private List<String> entityPackages = new ArrayList<>();

        /**
         * 排除的实体类名称（完全限定名）
         */
        private List<String> excludeEntities = new ArrayList<>();

        /**
         * 只包含的实体类名称（完全限定名），如果设置了此项，则只处理这些实体
         */
        private List<String> includeEntities = new ArrayList<>();

        /**
         * 数据库schema名称（可选）
         */
        private String defaultSchema;

        /**
         * 表名前缀
         */
        private String tablePrefix;

        /**
         * 表名后缀
         */
        private String tableSuffix;

        /**
         * 是否使用驼峰命名转下划线命名
         */
        private boolean camelCaseToUnderscore = true;

        /**
         * 执行超时时间（秒）
         */
        private int executionTimeoutSeconds = 300;

        /**
         * 最大重试次数
         */
        private int maxRetryCount = 3;

        /**
         * 是否在出错时继续执行其他表的维护
         */
        private boolean continueOnError = true;

        /**
         * 是否启用异步处理
         */
        private boolean asyncEnabled = true;

        /**
         * 异步线程池核心线程数
         */
        private int asyncCorePoolSize = 2;

        /**
         * 异步线程池最大线程数
         */
        private int asyncMaxPoolSize = 4;

        /**
         * 异步线程池队列容量
         */
        private int asyncQueueCapacity = 100;

        /**
         * 异步线程池线程名前缀
         */
        private String asyncThreadNamePrefix = "table-maintenance-";

        /**
         * 备份相关配置
         */
        private BackupConfig backup = new BackupConfig();

        /**
         * 获取是否启用表结构自动维护
         *
         * @return 是否启用
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用表结构自动维护
         *
         * @param enabled 是否启用
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 获取是否在应用启动时自动执行表结构维护
         *
         * @return 是否自动执行
         */
        public boolean isAutoExecuteOnStartup() {
            return autoExecuteOnStartup;
        }

        /**
         * 设置是否在应用启动时自动执行表结构维护
         *
         * @param autoExecuteOnStartup 是否自动执行
         */
        public void setAutoExecuteOnStartup(boolean autoExecuteOnStartup) {
            this.autoExecuteOnStartup = autoExecuteOnStartup;
        }

        /**
         * 获取是否允许删除列
         *
         * @return 是否允许删除列
         */
        public boolean isAllowDropColumn() {
            return allowDropColumn;
        }

        /**
         * 设置是否允许删除列
         *
         * @param allowDropColumn 是否允许删除列
         */
        public void setAllowDropColumn(boolean allowDropColumn) {
            this.allowDropColumn = allowDropColumn;
        }

        /**
         * 获取是否允许删除索引
         *
         * @return 是否允许删除索引
         */
        public boolean isAllowDropIndex() {
            return allowDropIndex;
        }

        /**
         * 设置是否允许删除索引
         *
         * @param allowDropIndex 是否允许删除索引
         */
        public void setAllowDropIndex(boolean allowDropIndex) {
            this.allowDropIndex = allowDropIndex;
        }

        /**
         * 获取是否允许修改列类型
         *
         * @return 是否允许修改列类型
         */
        public boolean isAllowModifyColumnType() {
            return allowModifyColumnType;
        }

        /**
         * 设置是否允许修改列类型
         *
         * @param allowModifyColumnType 是否允许修改列类型
         */
        public void setAllowModifyColumnType(boolean allowModifyColumnType) {
            this.allowModifyColumnType = allowModifyColumnType;
        }

        /**
         * 获取是否在执行前进行验证
         *
         * @return 是否验证
         */
        public boolean isValidateBeforeExecution() {
            return validateBeforeExecution;
        }

        /**
         * 设置是否在执行前进行验证
         *
         * @param validateBeforeExecution 是否验证
         */
        public void setValidateBeforeExecution(boolean validateBeforeExecution) {
            this.validateBeforeExecution = validateBeforeExecution;
        }

        /**
         * 获取是否打印SQL语句到日志
         *
         * @return 是否打印SQL
         */
        public boolean isPrintSql() {
            return printSql;
        }

        /**
         * 设置是否打印SQL语句到日志
         *
         * @param printSql 是否打印SQL
         */
        public void setPrintSql(boolean printSql) {
            this.printSql = printSql;
        }

        /**
         * 获取是否在测试环境下执行
         *
         * @return 是否在测试环境执行
         */
        public boolean isExecuteInTestEnvironment() {
            return executeInTestEnvironment;
        }

        /**
         * 设置是否在测试环境下执行
         *
         * @param executeInTestEnvironment 是否在测试环境执行
         */
        public void setExecuteInTestEnvironment(boolean executeInTestEnvironment) {
            this.executeInTestEnvironment = executeInTestEnvironment;
        }

        /**
         * 获取需要扫描的实体类包路径
         *
         * @return 实体类包路径列表
         */
        public List<String> getEntityPackages() {
            return entityPackages;
        }

        /**
         * 设置需要扫描的实体类包路径
         *
         * @param entityPackages 实体类包路径列表
         */
        public void setEntityPackages(List<String> entityPackages) {
            this.entityPackages = entityPackages;
        }

        /**
         * 获取排除的实体类名称
         *
         * @return 排除的实体类名称列表
         */
        public List<String> getExcludeEntities() {
            return excludeEntities;
        }

        /**
         * 设置排除的实体类名称
         *
         * @param excludeEntities 排除的实体类名称列表
         */
        public void setExcludeEntities(List<String> excludeEntities) {
            this.excludeEntities = excludeEntities;
        }

        /**
         * 获取只包含的实体类名称
         *
         * @return 只包含的实体类名称列表
         */
        public List<String> getIncludeEntities() {
            return includeEntities;
        }

        /**
         * 设置只包含的实体类名称
         *
         * @param includeEntities 只包含的实体类名称列表
         */
        public void setIncludeEntities(List<String> includeEntities) {
            this.includeEntities = includeEntities;
        }

        /**
         * 获取默认schema名称
         *
         * @return 默认schema名称
         */
        public String getDefaultSchema() {
            return defaultSchema;
        }

        /**
         * 设置默认schema名称
         *
         * @param defaultSchema 默认schema名称
         */
        public void setDefaultSchema(String defaultSchema) {
            this.defaultSchema = defaultSchema;
        }

        /**
         * 获取表名前缀
         *
         * @return 表名前缀
         */
        public String getTablePrefix() {
            return tablePrefix;
        }

        /**
         * 设置表名前缀
         *
         * @param tablePrefix 表名前缀
         */
        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }

        /**
         * 获取表名后缀
         *
         * @return 表名后缀
         */
        public String getTableSuffix() {
            return tableSuffix;
        }

        /**
         * 设置表名后缀
         *
         * @param tableSuffix 表名后缀
         */
        public void setTableSuffix(String tableSuffix) {
            this.tableSuffix = tableSuffix;
        }

        /**
         * 获取是否使用驼峰命名转下划线命名
         *
         * @return 是否转换命名
         */
        public boolean isCamelCaseToUnderscore() {
            return camelCaseToUnderscore;
        }

        /**
         * 设置是否使用驼峰命名转下划线命名
         *
         * @param camelCaseToUnderscore 是否转换命名
         */
        public void setCamelCaseToUnderscore(boolean camelCaseToUnderscore) {
            this.camelCaseToUnderscore = camelCaseToUnderscore;
        }

        /**
         * 获取执行超时时间
         *
         * @return 超时时间（秒）
         */
        public int getExecutionTimeoutSeconds() {
            return executionTimeoutSeconds;
        }

        /**
         * 设置执行超时时间
         *
         * @param executionTimeoutSeconds 超时时间（秒）
         */
        public void setExecutionTimeoutSeconds(int executionTimeoutSeconds) {
            this.executionTimeoutSeconds = executionTimeoutSeconds;
        }

        /**
         * 获取最大重试次数
         *
         * @return 最大重试次数
         */
        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        /**
         * 设置最大重试次数
         *
         * @param maxRetryCount 最大重试次数
         */
        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

        /**
         * 获取是否在出错时继续执行
         *
         * @return 是否继续执行
         */
        public boolean isContinueOnError() {
            return continueOnError;
        }

        /**
         * 设置是否在出错时继续执行
         *
         * @param continueOnError 是否继续执行
         */
        public void setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
        }

        /**
         * 获取备份配置
         *
         * @return 备份配置
         */
        public BackupConfig getBackup() {
            return backup;
        }

        /**
         * 设置备份配置
         *
         * @param backup 备份配置
         */
        public void setBackup(BackupConfig backup) {
            this.backup = backup;
        }

        /**
         * 获取是否启用异步处理
         *
         * @return 是否启用异步处理
         */
        public boolean isAsyncEnabled() {
            return asyncEnabled;
        }

        /**
         * 设置是否启用异步处理
         *
         * @param asyncEnabled 是否启用异步处理
         */
        public void setAsyncEnabled(boolean asyncEnabled) {
            this.asyncEnabled = asyncEnabled;
        }

        /**
         * 获取异步线程池核心线程数
         *
         * @return 核心线程数
         */
        public int getAsyncCorePoolSize() {
            return asyncCorePoolSize;
        }

        /**
         * 设置异步线程池核心线程数
         *
         * @param asyncCorePoolSize 核心线程数
         */
        public void setAsyncCorePoolSize(int asyncCorePoolSize) {
            this.asyncCorePoolSize = asyncCorePoolSize;
        }

        /**
         * 获取异步线程池最大线程数
         *
         * @return 最大线程数
         */
        public int getAsyncMaxPoolSize() {
            return asyncMaxPoolSize;
        }

        /**
         * 设置异步线程池最大线程数
         *
         * @param asyncMaxPoolSize 最大线程数
         */
        public void setAsyncMaxPoolSize(int asyncMaxPoolSize) {
            this.asyncMaxPoolSize = asyncMaxPoolSize;
        }

        /**
         * 获取异步线程池队列容量
         *
         * @return 队列容量
         */
        public int getAsyncQueueCapacity() {
            return asyncQueueCapacity;
        }

        /**
         * 设置异步线程池队列容量
         *
         * @param asyncQueueCapacity 队列容量
         */
        public void setAsyncQueueCapacity(int asyncQueueCapacity) {
            this.asyncQueueCapacity = asyncQueueCapacity;
        }

        /**
         * 获取异步线程池线程名前缀
         *
         * @return 线程名前缀
         */
        public String getAsyncThreadNamePrefix() {
            return asyncThreadNamePrefix;
        }

        /**
         * 设置异步线程池线程名前缀
         *
         * @param asyncThreadNamePrefix 线程名前缀
         */
        public void setAsyncThreadNamePrefix(String asyncThreadNamePrefix) {
            this.asyncThreadNamePrefix = asyncThreadNamePrefix;
        }
    }

    /**
     * 备份配置内部类
     */
    public static class BackupConfig {

        /**
         * 是否启用备份
         */
        private boolean enabled = false;

        /**
         * 备份目录
         */
        private String backupDirectory = "./db-backup";

        /**
         * 是否在执行前备份表结构
         */
        private boolean backupBeforeExecution = true;

        /**
         * 备份文件保留天数
         */
        private int retentionDays = 30;

        /**
         * 获取是否启用备份
         *
         * @return 是否启用备份
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * 设置是否启用备份
         *
         * @param enabled 是否启用备份
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * 获取备份目录
         *
         * @return 备份目录
         */
        public String getBackupDirectory() {
            return backupDirectory;
        }

        /**
         * 设置备份目录
         *
         * @param backupDirectory 备份目录
         */
        public void setBackupDirectory(String backupDirectory) {
            this.backupDirectory = backupDirectory;
        }

        /**
         * 获取是否在执行前备份表结构
         *
         * @return 是否备份
         */
        public boolean isBackupBeforeExecution() {
            return backupBeforeExecution;
        }

        /**
         * 设置是否在执行前备份表结构
         *
         * @param backupBeforeExecution 是否备份
         */
        public void setBackupBeforeExecution(boolean backupBeforeExecution) {
            this.backupBeforeExecution = backupBeforeExecution;
        }

        /**
         * 获取备份文件保留天数
         *
         * @return 保留天数
         */
        public int getRetentionDays() {
            return retentionDays;
        }

        /**
         * 设置备份文件保留天数
         *
         * @param retentionDays 保留天数
         */
        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }
}
