package tech.msop.core.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的Consumer
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Run the Consumer
     *
     * @param t T
     * @throws Throwable 异常
     */
    @Nullable
    void accept(@Nullable T t) throws Throwable;
}
