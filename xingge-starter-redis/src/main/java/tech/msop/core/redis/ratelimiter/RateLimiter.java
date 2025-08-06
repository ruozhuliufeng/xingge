package tech.msop.core.redis.ratelimiter;



import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式 限流注解，默认速率 600/ms
 *
 * @author ruozhuliufeng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RateLimiter {
    /**
     * 限流的key，必须：保持唯一性
     *
     * @return key
     */
    String value();

    /**
     * 限流参数，可选，支持spring el # 读取方法参数和 @ 读取spring bean
     *
     * @return param
     */
    String param() default "";

    /**
     * 支持的最大请求，默认：100
     *
     * @return int
     */
    long max() default 100L;

    /**
     * 持续时间，默认：3600
     *
     * @return int
     */
    long ttl() default 1L;

    /**
     * 时间单位，默认分
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

}
