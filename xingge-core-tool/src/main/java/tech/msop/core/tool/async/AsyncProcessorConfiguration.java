/**
 * 异步处理器配置类
 * 配置异步执行的线程池和相关参数
 * 
 * @author XingGe Framework
 * @since 1.0.0
 */
package tech.msop.core.tool.async;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步处理器配置类
 * 提供异步执行的线程池配置
 */
@Configuration
@EnableAsync
public class AsyncProcessorConfiguration {

    /**
     * 异步处理器配置属性
     */
    public static class AsyncProcessorProperties {
        
        /**
         * 是否启用异步处理
         */
        private boolean enabled = true;
        
        /**
         * 核心线程数
         */
        private int corePoolSize = 5;
        
        /**
         * 最大线程数
         */
        private int maxPoolSize = 20;
        
        /**
         * 队列容量
         */
        private int queueCapacity = 100;
        
        /**
         * 线程名前缀
         */
        private String threadNamePrefix = "async-processor-";
        
        /**
         * 线程空闲时间（秒）
         */
        private int keepAliveSeconds = 60;
        
        /**
         * 是否等待任务完成后关闭
         */
        private boolean waitForTasksToCompleteOnShutdown = true;
        
        /**
         * 等待关闭的超时时间（秒）
         */
        private int awaitTerminationSeconds = 60;

        // Getter and Setter methods
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public boolean isWaitForTasksToCompleteOnShutdown() {
            return waitForTasksToCompleteOnShutdown;
        }

        public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
            this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
        }

        public int getAwaitTerminationSeconds() {
            return awaitTerminationSeconds;
        }

        public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
            this.awaitTerminationSeconds = awaitTerminationSeconds;
        }
    }

    /**
     * 配置异步执行器
     * 
     * @return 异步执行器
     */
    @Bean(name = "asyncProcessorExecutor")
    public Executor asyncProcessorExecutor(AsyncProcessorProperties properties) {
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 设置核心线程数
        executor.setCorePoolSize(properties.getCorePoolSize());
        
        // 设置最大线程数
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        
        // 设置队列容量
        executor.setQueueCapacity(properties.getQueueCapacity());
        
        // 设置线程名前缀
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        
        // 设置线程空闲时间
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        
        // 设置拒绝策略：由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 设置是否等待任务完成后关闭
        executor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        
        // 设置等待关闭的超时时间
        executor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        
        // 初始化执行器
        executor.initialize();
        
        return executor;
    }

    /**
     * 注册异步处理器配置属性Bean
     * 
     * @return 异步处理器配置属性
     */
    @Bean
    public AsyncProcessorProperties asyncProcessorProperties() {
        return new AsyncProcessorProperties();
    }
}