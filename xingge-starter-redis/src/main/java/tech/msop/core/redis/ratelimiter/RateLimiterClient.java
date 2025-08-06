package tech.msop.core.redis.ratelimiter;

import tech.msop.core.tool.function.CheckedSupplier;
import tech.msop.core.tool.utils.Exceptions;

import java.util.concurrent.TimeUnit;

/**
 * RateLimiter 限流 Client
 *
 * @author ruozhuliufeng
 */
public interface RateLimiterClient {
    /**
     * 服务是否被限流
     *
     * @param key      自定义的key，唯一
     * @param max      支持的最大请求
     * @param ttl      时间
     * @param timeUnit 时间单位
     * @return 是否允许
     */
    boolean isAllowed(String key, long max, long ttl, TimeUnit timeUnit);

    /**
     * 服务是否被限流
     *
     * @param key 自定义的key，唯一
     * @param max 支持的最大请求
     * @param ttl 时间,单位默认为秒（seconds）
     * @return 是否允许
     */
    default boolean isAllowed(String key, long max, long ttl) {
        return this.isAllowed(key, max, ttl, TimeUnit.SECONDS);
    }

    /**
     * 服务限流，被限制时抛出RateLimiterException 异常，需自行处理异常
     *
     * @param key      自定义的key
     * @param max      支持的最大请求
     * @param ttl      时间
     * @param supplier Supplier 函数式
     * @param <T>      泛型
     * @return 函数执行结果
     */
    default <T> T allow(String key, long max, long ttl, CheckedSupplier<T> supplier) {
        return allow(key, max, ttl, TimeUnit.SECONDS, supplier);
    }

    /**
     * 服务限流，被限制时抛出RateLimiterException 异常，需自行处理异常
     *
     * @param key      自定义的key
     * @param max      支持的最大请求
     * @param ttl      时间
     * @param timeUnit 时间单位
     * @param supplier Supplier 函数式
     * @param <T>      泛型
     * @return 函数执行结果
     */
    default <T> T allow(String key, long max, long ttl, TimeUnit timeUnit, CheckedSupplier<T> supplier) {
        boolean isAllowed = this.isAllowed(key, max, ttl, timeUnit);
        if (isAllowed) {
            try {
                return supplier.get();
            } catch (Throwable throwable) {
                throw Exceptions.unchecked(throwable);
            }
        }
        throw new RateLimiterException(key, max, ttl, timeUnit);
    }
}
