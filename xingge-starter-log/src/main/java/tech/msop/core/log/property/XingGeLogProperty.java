/*
 * Copyright (c) 2024 行歌(xingge)
 * 行歌日志模块配置属性类
 * 
 * 功能说明：
 * - 定义日志模块的配置属性
 * - 支持LogIndex切面配置
 * - 提供灵活的开关控制
 */
package tech.msop.core.log.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * 行歌日志模块配置属性类
 * 
 * <p>该类定义了日志模块的所有配置属性，包括：</p>
 * <ul>
 *   <li>日志模块总开关</li>
 *   <li>LogIndex切面配置</li>
 *   <li>其他日志相关配置</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   log:
 *     enabled: true
 *     log-index:
 *       enabled: true
 *       clear-after-method: true
 *       debug-enabled: false
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@ConfigurationProperties(prefix = "xg.log")
@Data
public class XingGeLogProperty {
    
    /**
     * 是否启用日志模块
     * 默认启用
     */
    private Boolean enabled = true;
    
    /**
     * LogIndex切面配置
     */
    private LogIndexConfig logIndex = new LogIndexConfig();
    
    /**
     * 审计日志配置
     */
    private AuditConfig audit = new AuditConfig();
    
    /**
     * 获取LogIndex配置
     * @return LogIndex配置
     */
    public LogIndexConfig getLogIndex() {
        return logIndex;
    }
    
    /**
     * 获取启用状态
     * @return 启用状态
     */
    public Boolean getEnabled() {
        return enabled;
    }
    
    /**
     * 获取审计日志配置
     * @return 审计日志配置
     */
    public AuditConfig getAudit() {
        return audit;
    }
    
    /**
     * LogIndex切面配置类
     */
    @Data
    public static class LogIndexConfig {
        
        /**
         * 是否启用LogIndex切面
         * 默认启用
         */
        private Boolean enabled = true;
        
        /**
         * 是否在方法执行后清理MDC
         * 默认启用，建议保持启用以避免内存泄漏
         */
        private Boolean clearAfterMethod = true;
        
        /**
         * 是否启用调试日志
         * 默认关闭，开启后会输出详细的切面处理日志
         */
        private Boolean debugEnabled = false;
        
        /**
         * 索引键的最大长度
         * 超过此长度的索引键会被截断
         * 默认100个字符
         */
        private Integer maxKeyLength = 100;
        
        /**
         * 索引值的最大长度
         * 超过此长度的索引值会被截断
         * 默认500个字符
         */
        private Integer maxValueLength = 500;
        
        /**
         * 是否启用嵌套对象扫描
         * 默认关闭，开启后会递归扫描对象内部的LogIndex字段
         */
        private Boolean enableNestedScan = false;
        
        /**
         * 获取调试启用状态
         * @return 调试启用状态
         */
        public Boolean getDebugEnabled() {
            return debugEnabled;
        }
        
        /**
         * 获取方法执行后清理状态
         * @return 清理状态
         */
        public Boolean getClearAfterMethod() {
            return clearAfterMethod;
        }
        
        /**
         * 获取嵌套扫描启用状态
         * @return 嵌套扫描启用状态
         */
        public Boolean getEnableNestedScan() {
            return enableNestedScan;
        }
        
        /**
         * 获取最大键长度
         * @return 最大键长度
         */
        public Integer getMaxKeyLength() {
            return maxKeyLength;
        }
        
        /**
         * 获取最大值长度
         * @return 最大值长度
         */
        public Integer getMaxValueLength() {
            return maxValueLength;
        }
    }
    
    /**
     * 审计日志配置类
     */
    @Data
    public static class AuditConfig {
        
        /**
         * 是否启用审计日志
         * 默认启用
         */
        private Boolean enabled = true;
        
        /**
         * 是否启用调试日志
         * 默认关闭
         */
        private Boolean debugEnabled = false;
        
        /**
         * 默认是否异步处理
         * 默认异步处理以提高性能
         */
        private Boolean defaultAsync = true;
        
        /**
         * 审计日志处理器配置
         */
        private HandlersConfig handlers = new HandlersConfig();
        
        /**
         * 获取处理器配置
         * @return 处理器配置
         */
        public HandlersConfig getHandlers() {
            return handlers;
        }
        
        /**
         * 获取启用状态
         * @return 启用状态
         */
        public Boolean getEnabled() {
            return enabled;
        }
        
        /**
         * 处理器配置类
         */
        @Data
        public static class HandlersConfig {
            
            /**
             * 控制台处理器配置
             */
            private ConsoleConfig console = new ConsoleConfig();
            
            /**
             * Feign接口处理器配置
             */
            private FeignConfig feign = new FeignConfig();
            
            /**
             * 数据库处理器配置
             */
            private DatabaseConfig database = new DatabaseConfig();
            
            /**
             * 获取控制台配置
             * @return 控制台配置
             */
            public ConsoleConfig getConsole() {
                return console;
            }
            
            /**
             * 获取Feign配置
             * @return Feign配置
             */
            public FeignConfig getFeign() {
                return feign;
            }
            
            /**
             * 获取数据库配置
             * @return 数据库配置
             */
            public DatabaseConfig getDatabase() {
                return database;
            }
        }
        
        /**
         * 控制台处理器配置
         */
        @Data
        public static class ConsoleConfig {
            
            /**
             * 是否启用控制台处理器
             * 默认启用
             */
            private Boolean enabled = true;
            
            /**
             * 日志格式：simple、detailed
             * 默认详细格式
             */
            private String format = "detailed";
            
            /**
             * 日志级别：DEBUG、INFO、WARN、ERROR
             * 默认INFO
             */
            private String level = "INFO";
        }
        
        /**
         * Feign接口处理器配置
         */
        @Data
        public static class FeignConfig {
            
            /**
             * 是否启用Feign处理器
             * 默认关闭
             */
            private Boolean enabled = false;
            
            /**
             * 审计日志接口URL
             */
            private String url;
            
            /**
             * 请求超时时间（毫秒）
             * 默认5秒
             */
            private Integer timeout = 5000;
            
            /**
             * 重试次数
             * 默认3次
             */
            private Integer retryCount = 3;
            
            /**
             * 批量发送大小
             * 默认10条
             */
            private Integer batchSize = 10;
            
            /**
             * 自定义请求头
             */
            private Map<String, String> headers = new HashMap<>();
            
            /**
             * 获取URL
             * @return URL
             */
            public String getUrl() {
                return url;
            }
            
            /**
             * 获取请求头
             * @return 请求头
             */
            public Map<String, String> getHeaders() {
                return headers;
            }
            
            /**
             * 获取启用状态
             * @return 启用状态
             */
            public Boolean getEnabled() {
                return enabled;
            }
            
            /**
             * 获取重试次数
             * @return 重试次数
             */
            public Integer getRetryCount() {
                return retryCount;
            }
        }
        
        /**
         * 数据库处理器配置
         */
        @Data
        public static class DatabaseConfig {
            
            /**
             * 是否启用数据库处理器
             * 默认关闭
             */
            private Boolean enabled = false;
            
            /**
             * 审计日志表名
             * 默认audit_log
             */
            private String tableName = "audit_log";
            
            /**
             * 是否自动创建表
             * 默认启用
             */
            private Boolean autoCreateTable = true;
            
            /**
             * 批量插入大小
             * 默认100条
             */
            private Integer batchSize = 100;
            
            /**
             * 数据保留天数
             * 默认90天，0表示不自动清理
             */
            private Integer retentionDays = 90;
            
            /**
             * 获取启用状态
             * @return 启用状态
             */
            public Boolean getEnabled() {
                return enabled;
            }
            
            /**
             * 获取自动创建表状态
             * @return 自动创建表状态
             */
            public Boolean getAutoCreateTable() {
                return autoCreateTable;
            }
            
            /**
             * 获取表名
             * @return 表名
             */
            public String getTableName() {
                return tableName;
            }
        }
    }
}
