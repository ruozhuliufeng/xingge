package tech.msop.core.tool.function;

/**
 * 受检的 Supplier
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedSupplier<T> {

    /**
     * Run the Supplier
     *
     * @return T
     * @throws Throwable 异常
     */
    T get() throws Throwable;
}
