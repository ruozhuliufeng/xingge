package tech.msop.core.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的 Callable
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedCallable<T> {

    /**
     * Run this callable
     *
     * @return result
     * @throws Throwable 受检异常
     */
    @Nullable
    T call() throws Throwable;
}
