package tech.msop.core.tool.function;

/**
 * 受检的 Runnable
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * Run this runable
     *
     * @throws Throwable 异常
     */
    void run() throws Throwable;
}
