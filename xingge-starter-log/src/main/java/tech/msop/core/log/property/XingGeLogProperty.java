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
    }
}
