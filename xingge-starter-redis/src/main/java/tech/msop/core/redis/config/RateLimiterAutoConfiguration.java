package tech.msop.core.redis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import tech.msop.core.redis.ratelimiter.RedisRateLimiterAspect;
import tech.msop.core.redis.ratelimiter.RedisRateLimiterClient;

import java.util.List;

/**
 * 基于Redis 的分布式限流自动配置
 *
 * @author ruozhuliufeng
 */
@AutoConfiguration
@ConditionalOnProperty(value = "ms.redis.rate-limiter.enabled",havingValue = "true")
public class RateLimiterAutoConfiguration {

    @SuppressWarnings("unchecked")
    private RedisScript<List<Long>> redisRateLimiterScipt(){
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("META-INF/scripts/ms_rate_limiter.lua")));
        redisScript.setResultType(List.class);
        return redisScript;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiterClient redisRateLimiter(StringRedisTemplate redisTemplate, Environment environment){
        RedisScript<List<Long>> redisScript = redisRateLimiterScipt();
        return new RedisRateLimiterClient(redisTemplate,redisScript,environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiterAspect redisRateLimiterAspect(RedisRateLimiterClient redisRateLimiterClient){
        return new RedisRateLimiterAspect(redisRateLimiterClient);
    }
}
