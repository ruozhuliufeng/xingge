/**
 * 数据库方言工厂类
 * 
 * @author xingge-core-db
 * @since 1.0.0
 */
package tech.msop.core.db.dialect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库方言工厂类
 * 根据数据库类型自动选择合适的方言实现
 */
public class DialectFactory {
    
    /**
     * 方言实例缓存
     */
    private static final Map<String, Dialect> DIALECT_CACHE = new HashMap<>();
    
    /**
     * 数据库产品名称到方言类的映射
     */
    private static final Map<String, Class<? extends Dialect>> DIALECT_MAPPING = new HashMap<>();
    
    static {
        // 注册内置方言
        DIALECT_MAPPING.put("MySQL", MySQLDialect.class);
        DIALECT_MAPPING.put("PostgreSQL", PostgreSQLDialect.class);
        DIALECT_MAPPING.put("Microsoft SQL Server", SQLServerDialect.class);
        DIALECT_MAPPING.put("Oracle", OracleDialect.class);
        DIALECT_MAPPING.put("H2", H2Dialect.class);
        
        // 添加别名支持
        DIALECT_MAPPING.put("mysql", MySQLDialect.class);
        DIALECT_MAPPING.put("postgresql", PostgreSQLDialect.class);
        DIALECT_MAPPING.put("sqlserver", SQLServerDialect.class);
        DIALECT_MAPPING.put("oracle", OracleDialect.class);
        DIALECT_MAPPING.put("postgres", PostgreSQLDialect.class);
        DIALECT_MAPPING.put("h2", H2Dialect.class);
    }
    
    /**
     * 根据数据源获取对应的数据库方言
     * 
     * @param dataSource 数据源
     * @return 数据库方言实例
     * @throws SQLException 获取数据库连接或元数据时发生异常
     * @throws IllegalArgumentException 不支持的数据库类型
     */
    public static Dialect getDialect(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            return getDialect(databaseProductName);
        }
    }
    
    /**
     * 根据数据库连接获取对应的数据库方言
     * 
     * @param connection 数据库连接
     * @return 数据库方言实例
     * @throws SQLException 获取数据库元数据时发生异常
     * @throws IllegalArgumentException 不支持的数据库类型
     */
    public static Dialect getDialect(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseProductName = metaData.getDatabaseProductName();
        return getDialect(databaseProductName);
    }
    
    /**
     * 根据数据库产品名称获取对应的数据库方言
     * 
     * @param databaseProductName 数据库产品名称
     * @return 数据库方言实例
     * @throws IllegalArgumentException 不支持的数据库类型
     */
    public static Dialect getDialect(String databaseProductName) {
        if (databaseProductName == null || databaseProductName.trim().isEmpty()) {
            throw new IllegalArgumentException("数据库产品名称不能为空");
        }
        
        // 从缓存中获取
        Dialect dialect = DIALECT_CACHE.get(databaseProductName);
        if (dialect != null) {
            return dialect;
        }
        
        // 查找匹配的方言类
        Class<? extends Dialect> dialectClass = findDialectClass(databaseProductName);
        if (dialectClass == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + databaseProductName);
        }
        
        // 创建方言实例
        try {
            dialect = dialectClass.getDeclaredConstructor().newInstance();
            DIALECT_CACHE.put(databaseProductName, dialect);
            return dialect;
        } catch (Exception e) {
            throw new RuntimeException("创建数据库方言实例失败: " + dialectClass.getName(), e);
        }
    }
    
    /**
     * 注册自定义数据库方言
     * 
     * @param databaseProductName 数据库产品名称
     * @param dialectClass 方言实现类
     */
    public static void registerDialect(String databaseProductName, Class<? extends Dialect> dialectClass) {
        if (databaseProductName == null || databaseProductName.trim().isEmpty()) {
            throw new IllegalArgumentException("数据库产品名称不能为空");
        }
        if (dialectClass == null) {
            throw new IllegalArgumentException("方言实现类不能为空");
        }
        
        DIALECT_MAPPING.put(databaseProductName, dialectClass);
        // 清除缓存，强制重新创建实例
        DIALECT_CACHE.remove(databaseProductName);
    }
    
    /**
     * 检查是否支持指定的数据库类型
     * 
     * @param databaseProductName 数据库产品名称
     * @return 是否支持
     */
    public static boolean isSupported(String databaseProductName) {
        return findDialectClass(databaseProductName) != null;
    }
    
    /**
     * 获取所有支持的数据库类型
     * 
     * @return 支持的数据库类型集合
     */
    public static String[] getSupportedDatabases() {
        return DIALECT_MAPPING.keySet().toArray(new String[0]);
    }
    
    /**
     * 清除方言缓存
     */
    public static void clearCache() {
        DIALECT_CACHE.clear();
    }
    
    /**
     * 查找匹配的方言类
     * 
     * @param databaseProductName 数据库产品名称
     * @return 方言类，如果未找到则返回null
     */
    private static Class<? extends Dialect> findDialectClass(String databaseProductName) {
        // 精确匹配
        Class<? extends Dialect> dialectClass = DIALECT_MAPPING.get(databaseProductName);
        if (dialectClass != null) {
            return dialectClass;
        }
        
        // 忽略大小写匹配
        for (Map.Entry<String, Class<? extends Dialect>> entry : DIALECT_MAPPING.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(databaseProductName)) {
                return entry.getValue();
            }
        }
        
        // 模糊匹配（包含关系）
        String lowerProductName = databaseProductName.toLowerCase();
        for (Map.Entry<String, Class<? extends Dialect>> entry : DIALECT_MAPPING.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (lowerProductName.contains(key) || key.contains(lowerProductName)) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private DialectFactory() {
        throw new UnsupportedOperationException("工厂类不允许实例化");
    }
}