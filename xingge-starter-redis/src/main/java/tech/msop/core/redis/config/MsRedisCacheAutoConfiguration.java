package tech.msop.core.redis.config;


import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展redis cache 支持注解cacheName 添加超时时间
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration
@EnableConfigurationProperties(CacheProperties.class)
public class MsRedisCacheAutoConfiguration {
    /**
     * 序列化方式
     */
    private final RedisSerializer<Object> redisSerializer;
    private final CacheProperties cacheProperties;
    private final CacheManagerCustomizers customizerInvoker;
    @Nullable
    private final RedisCacheConfiguration redisCacheConfiguration;

    public MsRedisCacheAutoConfiguration(RedisSerializer<Object> redisSerializer, CacheProperties cacheProperties, CacheManagerCustomizers customizerInvoker, @Nullable RedisCacheConfiguration redisCacheConfiguration) {
        this.redisSerializer = redisSerializer;
        this.cacheProperties = cacheProperties;
        this.customizerInvoker = customizerInvoker;
        this.redisCacheConfiguration = redisCacheConfiguration;
    }

    @Primary
    @Bean("redisCacheManager")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        RedisCacheConfiguration cacheConfiguration = this.determineConfiguration();
        List<String> cacheNames = this.cacheProperties.getCacheNames();
        Map<String, RedisCacheConfiguration> initialCaches = new LinkedHashMap<>();
        if (!cacheNames.isEmpty()) {
            Map<String, RedisCacheConfiguration> cacheConfigurationMap = new LinkedHashMap<>(cacheNames.size());
            cacheNames.forEach(it -> cacheConfigurationMap.put(it, cacheConfiguration));
            initialCaches.putAll(cacheConfigurationMap);
        }
        boolean allowInFlightCacheCreation = true;
        boolean enableTransactions = false;
        RedisAutoCacheManager cacheManager = new RedisAutoCacheManager(redisCacheWriter, cacheConfiguration, initialCaches, allowInFlightCacheCreation);
        cacheManager.setTransactionAware(enableTransactions);
        return this.customizerInvoker.customize(cacheManager);
    }

    private RedisCacheConfiguration determineConfiguration() {
        if (this.redisCacheConfiguration != null) {
            return this.redisCacheConfiguration;
        } else {
            CacheProperties.Redis redisProperties = this.cacheProperties.getRedis();
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
            config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
            if (redisProperties.getTimeToLive() != null) {
                config = config.entryTtl(redisProperties.getTimeToLive());
            }
            if (redisProperties.getKeyPrefix() != null) {
                config = config.prefixKeysWith(redisProperties.getKeyPrefix());
            }
            if (!redisProperties.isCacheNullValues()) {
                config = config.disableCachingNullValues();
            }
            if (!redisProperties.isUseKeyPrefix()) {
                config = config.disableKeyPrefix();
            }
            return config;
        }
    }
}
