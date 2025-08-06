package tech.msop.core.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import tech.msop.core.redis.cache.RedisRepository;
import tech.msop.core.redis.serializer.RedisKeySerializer;

/**
 * RedisTemplate 配置
 *
 * @author ruozhuliufeng
 */
@EnableCaching
@AutoConfiguration(before = {RedisAutoConfiguration.class})
@EnableConfigurationProperties(MsRedisProperties.class)
public class RedisTemplateConfiguration implements MsRedisSerializerConfigAble {

    /**
     * value 值 序列化
     *
     * @param properties 配置
     * @return RedisSerializer
     */
    @Bean
    @ConditionalOnMissingBean(RedisSerializer.class)
    @Override
    public RedisSerializer<Object> redisSerializer(MsRedisProperties properties) {
        return defaultRedisSerializer(properties);
    }

    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                       RedisSerializer<Object> redisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // key 序列化
        RedisKeySerializer keySerializer = new RedisKeySerializer();
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        // value 序列化
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(ValueOperations.class)
    public ValueOperations valueOperations(RedisTemplate redisTemplate) {
        return redisTemplate.opsForValue();
    }

    @Bean
    public RedisRepository redisRepository(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        return new RedisRepository(redisTemplate,stringRedisTemplate);
    }
}
