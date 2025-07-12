/**
 * 异步处理器
 * 提供通用的异步任务处理能力，确保异常不会影响主业务流程
 * 
 * @author XingGe Framework
 * @since 1.0.0
 */
package tech.msop.core.tool.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 异步处理器
 * 提供异步执行任务的能力，自动捕获异常，确保不影响主业务流程
 */
@Component
public class AsyncProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncProcessor.class);

    /**
     * 异步执行无返回值的任务
     * 
     * @param task 要执行的任务
     * @param taskName 任务名称，用于日志记录
     */
    @Async
    public void executeAsync(Runnable task, String taskName) {
        try {
            logger.debug("开始执行异步任务: {}", taskName);
            task.run();
            logger.debug("异步任务执行完成: {}", taskName);
        } catch (Exception e) {
            logger.error("异步任务执行失败: {}, 错误信息: {}", taskName, e.getMessage(), e);
        }
    }

    /**
     * 异步执行无返回值的任务（简化版本，使用默认任务名称）
     * 
     * @param task 要执行的任务
     */
    @Async
    public void executeAsync(Runnable task) {
        executeAsync(task, "未命名任务");
    }

    /**
     * 异步执行有返回值的任务
     * 
     * @param task 要执行的任务
     * @param taskName 任务名称，用于日志记录
     * @param <T> 返回值类型
     * @return CompletableFuture包装的结果
     */
    @Async
    public <T> CompletableFuture<T> executeAsyncWithResult(Supplier<T> task, String taskName) {
        try {
            logger.debug("开始执行异步任务: {}", taskName);
            T result = task.get();
            logger.debug("异步任务执行完成: {}", taskName);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("异步任务执行失败: {}, 错误信息: {}", taskName, e.getMessage(), e);
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 异步执行有返回值的任务（简化版本，使用默认任务名称）
     * 
     * @param task 要执行的任务
     * @param <T> 返回值类型
     * @return CompletableFuture包装的结果
     */
    @Async
    public <T> CompletableFuture<T> executeAsyncWithResult(Supplier<T> task) {
        return executeAsyncWithResult(task, "未命名任务");
    }

    /**
     * 异步执行带参数的任务
     * 
     * @param task 要执行的任务
     * @param parameter 任务参数
     * @param taskName 任务名称，用于日志记录
     * @param <T> 参数类型
     */
    @Async
    public <T> void executeAsyncWithParam(Consumer<T> task, T parameter, String taskName) {
        try {
            logger.debug("开始执行异步任务: {}", taskName);
            task.accept(parameter);
            logger.debug("异步任务执行完成: {}", taskName);
        } catch (Exception e) {
            logger.error("异步任务执行失败: {}, 错误信息: {}", taskName, e.getMessage(), e);
        }
    }

    /**
     * 异步执行带参数的任务（简化版本，使用默认任务名称）
     * 
     * @param task 要执行的任务
     * @param parameter 任务参数
     * @param <T> 参数类型
     */
    @Async
    public <T> void executeAsyncWithParam(Consumer<T> task, T parameter) {
        executeAsyncWithParam(task, parameter, "未命名任务");
    }

    /**
     * 异步执行任务，并在完成后执行回调
     * 
     * @param task 要执行的任务
     * @param onSuccess 成功回调
     * @param onError 错误回调
     * @param taskName 任务名称，用于日志记录
     */
    @Async
    public void executeAsyncWithCallback(Runnable task, Runnable onSuccess, Consumer<Exception> onError, String taskName) {
        try {
            logger.debug("开始执行异步任务: {}", taskName);
            task.run();
            logger.debug("异步任务执行完成: {}", taskName);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception e) {
            logger.error("异步任务执行失败: {}, 错误信息: {}", taskName, e.getMessage(), e);
            if (onError != null) {
                try {
                    onError.accept(e);
                } catch (Exception callbackException) {
                    logger.error("异步任务错误回调执行失败: {}, 错误信息: {}", taskName, callbackException.getMessage(), callbackException);
                }
            }
        }
    }

    /**
     * 异步执行任务，并在完成后执行回调（简化版本，使用默认任务名称）
     * 
     * @param task 要执行的任务
     * @param onSuccess 成功回调
     * @param onError 错误回调
     */
    @Async
    public void executeAsyncWithCallback(Runnable task, Runnable onSuccess, Consumer<Exception> onError) {
        executeAsyncWithCallback(task, onSuccess, onError, "未命名任务");
    }
}