package tech.msop.core.tool.function;

import org.springframework.lang.Nullable;

/**
 * 受检的Function
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Run the Function
     *
     * @param t T
     * @return R
     * @throws Throwable 异常
     */
    @Nullable
    R apply(@Nullable T t) throws Throwable;
}
