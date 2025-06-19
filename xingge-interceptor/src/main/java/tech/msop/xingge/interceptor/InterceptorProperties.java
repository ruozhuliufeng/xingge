package tech.msop.xingge.interceptor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拦截器配置属性
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "xingge.interceptor")
public class InterceptorProperties {

    /**
     * 是否启用拦截器
     */
    private boolean enabled = false;

    /**
     * 拦截器配置列表
     */
    private List<InterceptorConfig> configs = new ArrayList<>();

    /**
     * 全局配置
     */
    private GlobalConfig global = new GlobalConfig();

    /**
     * 拦截器配置
     */
    @Data
    public static class InterceptorConfig {
        
        /**
         * 配置名称
         */
        private String name;
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 拦截类型：request（请求）、response（响应）、both（两者）
         */
        private String interceptType = "both";
        
        /**
         * 拦截范围：incoming（外部请求）、outgoing（内部请求）、both（两者）
         */
        private String interceptScope = "both";
        
        /**
         * URL 匹配模式列表
         */
        private List<String> urlPatterns = new ArrayList<>();
        
        /**
         * 排除的 URL 模式列表
         */
        private List<String> excludePatterns = new ArrayList<>();
        
        /**
         * 处理器配置列表
         */
        private List<ProcessorConfig> processors = new ArrayList<>();
    }

    /**
     * 处理器配置
     */
    @Data
    public static class ProcessorConfig {
        
        /**
         * 处理器类型：log、mysql、mongodb、postgresql、rocketmq
         */
        private String type;
        
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 是否异步处理
         */
        private boolean async = true;
        
        /**
         * 处理器特定配置
         */
        private Map<String, Object> config = new HashMap<>();
    }

    /**
     * 全局配置
     */
    @Data
    public static class GlobalConfig {
        
        /**
         * 默认异步处理
         */
        private boolean defaultAsync = true;
        
        /**
         * 线程池核心线程数
         */
        private int corePoolSize = 5;
        
        /**
         * 线程池最大线程数
         */
        private int maxPoolSize = 20;
        
        /**
         * 线程池队列容量
         */
        private int queueCapacity = 100;
        
        /**
         * 线程池线程名前缀
         */
        private String threadNamePrefix = "interceptor-";
        
        /**
         * 是否记录请求体
         */
        private boolean includeRequestBody = true;
        
        /**
         * 是否记录响应体
         */
        private boolean includeResponseBody = true;
        
        /**
         * 最大请求体大小（字节）
         */
        private long maxRequestBodySize = 1024 * 1024; // 1MB
        
        /**
         * 最大响应体大小（字节）
         */
        private long maxResponseBodySize = 1024 * 1024; // 1MB
    }
}