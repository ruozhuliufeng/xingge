package tech.msop.core.tool.async;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步处理器自动配置类
 * 
 * @author XingGe Framework
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncProcessorAutoConfiguration.AsyncProcessorProperties.class)
@Import(AsyncProcessorConfiguration.class)
public class AsyncProcessorAutoConfiguration {

    /**
     * 异步处理器Bean
     * 
     * @return AsyncProcessor实例
     */
    @Bean
    @ConditionalOnMissingBean
    public AsyncProcessor asyncProcessor() {
        return new AsyncProcessor();
    }

    /**
     * 异步处理器配置属性
     */
    @ConfigurationProperties(prefix = "xg.async")
    public static class AsyncProcessorProperties {
        
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

        // Getters and Setters
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
}