package tech.msop.core.redis.serializer;

import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 将redis key 序列化为字符串
 *
 * <p>
 * spring cache中的简单基本类型直接使用StringRedisSerializer会有问题
 * </p>
 *
 * @author ruozhuliufeng
 */
public class RedisKeySerializer implements RedisSerializer<Object> {

    private final Charset charset;
    private final ConversionService converter;

    public RedisKeySerializer() {
        this(StandardCharsets.UTF_8);
    }

    public RedisKeySerializer(Charset charset) {
        Objects.requireNonNull(charset, "Charset不能为空！");
        this.charset = charset;
        this.converter = DefaultConversionService.getSharedInstance();
    }


    @Override
    public byte[] serialize(Object object) throws SerializationException {
        Objects.requireNonNull(object, "Redis Key 不能为空!");
        String key;
        if (object instanceof SimpleKey) {
            key = "";
        } else if (object instanceof String) {
            key = (String) object;
        } else {
            key = converter.convert(object, String.class);
        }
        return key.getBytes(this.charset);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        // redis key 会用到反序列化
        if (bytes == null) {
            return null;
        }
        return new String(bytes, charset);
    }
}
