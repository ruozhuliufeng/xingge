/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求日志Feign客户端配置类
 * 
 * 功能说明：
 * - 配置RequestLogApiClient的Feign客户端
 * - 设置API存储相关的Feign配置
 */
package tech.request.core.request.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import tech.request.core.request.feign.RequestLogApiClient;

/**
 * 请求日志Feign客户端配置类
 * 
 * <p>该类负责配置RequestLogApiClient的Feign客户端，包括：</p>
 * <ul>
 *   <li>启用Feign客户端</li>
 *   <li>指定客户端接口</li>
 *   <li>设置配置类</li>
 * </ul>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Configuration
@ConditionalOnProperty(prefix = "xg.request.api", name = "enabled", havingValue = "true")
@EnableFeignClients(clients = RequestLogApiClient.class,
                   defaultConfiguration = tech.request.core.request.feign.RequestLogFeignConfiguration.class)
public class RequestLogFeignClientConfiguration {
    
}