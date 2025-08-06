package tech.msop.core.redis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.time.Duration;


/**
 * Cache Key 封装
 *
 * @author ruozhuliufeng
 */
@AllArgsConstructor
@ToString
@Getter
public class CacheKey {
    /**
     * redis key
     */
    private final String key;
    /**
     * 超时时间 秒
     */
    @Nullable
    private Duration expire;

    public CacheKey(String key) {
        this.key = key;
    }
}
