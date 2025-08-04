/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求拦截器自动配置类
 * 
 * 功能说明：
 * - 自动配置请求拦截器相关组件
 * - 根据配置条件装配不同的存储实现
 * - 配置各种HTTP客户端的拦截器
 */
package tech.request.core.request.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.request.core.request.handler.RequestLogHandler;
import tech.request.core.request.properties.RequestInterceptorProperty;
import tech.request.core.request.storage.RequestLogStorage;
import tech.request.core.request.storage.impl.ApiRequestLogStorage;
import tech.request.core.request.storage.impl.DatabaseRequestLogStorage;
import tech.request.core.request.storage.impl.LogRequestLogStorage;
import tech.request.core.request.storage.impl.MongoRequestLogStorage;

/**
 * 请求拦截器自动配置类
 * 
 * <p>该类负责根据配置自动装配请求拦截器相关组件，包括：</p>
 * <ul>
 *   <li>各种存储实现</li>
 *   <li>HTTP客户端拦截器</li>
 *   <li>请求日志处理器</li>
 * </ul>
 * 
 * <p>通过条件装配，只有在满足特定条件时才会创建相应的Bean。</p>
 * 
 * @author 若竹流风
 * @version 0.0.3
 * @since 2025-07-11
 */
@Configuration
@EnableConfigurationProperties(RequestInterceptorProperty.class)
@ConditionalOnProperty(prefix = "xg.request", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    OkHttpConfiguration.class,
    RestTemplateConfiguration.class,
    OpenFeignConfiguration.class,
    RequestLogFeignClientConfiguration.class
})
public class RequestInterceptorAutoConfiguration {
    
    /**
     * 配置日志存储实现
     * 
     * @return 日志存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "xg.request.log", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LogRequestLogStorage logStorage() {
        return new LogRequestLogStorage();
    }
    
    /**
     * 配置MongoDB存储实现
     * 
     * @return MongoDB存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "xg.request.mongo", name = "enabled", havingValue = "true")
    public MongoRequestLogStorage mongoStorage() {
        return new MongoRequestLogStorage();
    }
    
    /**
     * 配置API存储实现
     * 
     * @return API存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "xg.request.api", name = "enabled", havingValue = "true")
    public ApiRequestLogStorage apiStorage() {
        return new ApiRequestLogStorage();
    }
    
    /**
     * 配置数据库存储实现
     * 
     * @return 数据库存储实现
     */
    @Bean
    @ConditionalOnProperty(prefix = "xg.request.database", name = "enabled", havingValue = "true")
    public DatabaseRequestLogStorage databaseStorage() {
        return new DatabaseRequestLogStorage();
    }
    
    /**
     * 配置复合存储实现（支持多种存储类型）
     * 
     * @param properties 配置属性
     * @param logStorage 日志存储实现（可选）
     * @param mongoStorage MongoDB存储实现（可选）
     * @param apiStorage API存储实现（可选）
     * @param databaseStorage 数据库存储实现（可选）
     * @return 复合存储实现或单一存储实现
     */
    @Bean
    @ConditionalOnMissingBean(RequestLogStorage.class)
    public RequestLogStorage requestLogStorage(RequestInterceptorProperty properties,
                                             @Autowired(required = false) LogRequestLogStorage logStorage,
                                             @Autowired(required = false) MongoRequestLogStorage mongoStorage,
                                             @Autowired(required = false) ApiRequestLogStorage apiStorage,
                                             @Autowired(required = false) DatabaseRequestLogStorage databaseStorage) {
        
        java.util.List<RequestInterceptorProperty.StorageType> storageTypes = properties.getStorageTypes();
        
        // 如果只有一种存储类型，直接返回对应的实现
        if (storageTypes.size() == 1) {
            RequestInterceptorProperty.StorageType storageType = storageTypes.get(0);
            switch (storageType) {
                case LOG:
                    return logStorage != null ? logStorage : new LogRequestLogStorage();
                case MONGO:
                    return mongoStorage != null ? mongoStorage : new MongoRequestLogStorage();
                case API:
                    return apiStorage != null ? apiStorage : new ApiRequestLogStorage();
                case DATABASE:
                    return databaseStorage != null ? databaseStorage : new DatabaseRequestLogStorage();
                default:
                    return new LogRequestLogStorage(); // 默认使用日志存储
            }
        }
        
        // 多种存储类型，创建复合存储实现
        java.util.List<RequestLogStorage> storageList = new java.util.ArrayList<>();
        
        for (RequestInterceptorProperty.StorageType storageType : storageTypes) {
            switch (storageType) {
                case LOG:
                    storageList.add(logStorage != null ? logStorage : new LogRequestLogStorage());
                    break;
                case MONGO:
                    storageList.add(mongoStorage != null ? mongoStorage : new MongoRequestLogStorage());
                    break;
                case API:
                    storageList.add(apiStorage != null ? apiStorage : new ApiRequestLogStorage());
                    break;
                case DATABASE:
                    storageList.add(databaseStorage != null ? databaseStorage : new DatabaseRequestLogStorage());
                    break;
                default:
                    // 忽略不支持的存储类型
                    break;
            }
        }
        
        // 如果没有有效的存储实现，使用默认的日志存储
        if (storageList.isEmpty()) {
            storageList.add(new LogRequestLogStorage());
        }
        
        return new tech.request.core.request.storage.impl.CompositeRequestLogStorage(storageList);
    }
    
    /**
     * 配置请求日志处理器
     * 
     * @param requestLogStorage 请求日志存储接口
     * @param properties 请求拦截器配置属性
     * @return 请求日志处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestLogHandler requestLogHandler(RequestLogStorage requestLogStorage, RequestInterceptorProperty properties) {
        return new RequestLogHandler(requestLogStorage, properties);
    }
}