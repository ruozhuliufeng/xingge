/**
 * 数据库配置类
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库配置类
 * 提供数据库相关的基础配置
 */
@Component
@ConfigurationProperties(prefix = "xg.db")
public class DbConfig {
    
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
}
