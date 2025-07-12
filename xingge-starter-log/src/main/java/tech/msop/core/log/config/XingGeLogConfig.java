/*
 * Copyright (c) 2024 行歌(xingge)
 * 行歌日志模块配置类
 * 
 * 功能说明：
 * - 配置日志相关的Bean
 * - 启用LogIndex注解切面
 * - 管理日志模块的自动配置
 */
package tech.msop.core.log.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tech.msop.core.log.aspect.LogIndexAspect;
import tech.msop.core.log.property.XingGeLogProperty;

/**
 * 行歌日志模块配置类
 * 
 * <p>该配置类负责日志模块的自动配置，包括：</p>
 * <ul>
 *   <li>启用AspectJ自动代理</li>
 *   <li>配置LogIndex注解切面</li>
 *   <li>管理日志相关属性</li>
 * </ul>
 * 
 * <p>配置属性：</p>
 * <ul>
 *   <li>xg.log.enabled: 是否启用日志模块</li>
 *   <li>xg.log.log-index.enabled: 是否启用LogIndex切面</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2025-01-20
 */
@Configuration
@EnableConfigurationProperties(XingGeLogProperty.class)
@EnableAspectJAutoProxy
public class XingGeLogConfig {
    
    /**
     * 配置LogIndex注解切面
     * 
     * <p>当日志模块启用且LogIndex切面启用时，
     * 自动注册LogIndexAspect Bean。</p>
     * 
     * @return LogIndexAspect实例
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "xg.log.log-index", 
        name = "enabled", 
        havingValue = "true", 
        matchIfMissing = true
    )
    public LogIndexAspect logIndexAspect() {
        return new LogIndexAspect();
    }
}
