package tech.msop.core.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.serializer.RedisSerializer;
import tech.msop.core.redis.serializer.ProtostuffSerializer;

/**
 * ProtoStuff 序列化配置
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration(before = RedisTemplateConfiguration.class)
@ConditionalOnClass(name = "io.protostuff.Schema")
public class ProtoStuffSerializerConfiguration implements XingGeRedisSerializerConfigAble {
    /**
     * 序列化接口
     *
     * @param properties 配置
     * @return RedisSerializer
     */
    @Bean
    @ConditionalOnMissingBean
    @Override
    public RedisSerializer<Object> redisSerializer(XingGeRedisProperties properties) {
        if (XingGeRedisProperties.SerializerType.ProtoStuff == properties.getSerializerType()){
            return new ProtostuffSerializer();
        }
        return defaultRedisSerializer(properties);
    }
}
