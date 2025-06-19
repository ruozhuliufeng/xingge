package tech.msop.xingge.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.msop.xingge.interceptor.InterceptorProperties.ProcessorConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 数据处理器管理器
 * 负责管理和调度所有数据处理器
 * 
 * @author 若竹流风
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataProcessorManager {

    @Autowired
    private List<DataProcessor> dataProcessors;
    
    @Autowired
    private InterceptorProperties interceptorProperties;
    
    // 处理器映射
    private final Map<String, DataProcessor> processorMap = new ConcurrentHashMap<>();
    
    // 异步执行器
    private ExecutorService asyncExecutor;
    
    // 定时任务执行器（用于批量处理等）
    private ScheduledExecutorService scheduledExecutor;

    @PostConstruct
    public void initialize() {
        // 初始化处理器映射
        initializeProcessors();
        
        // 初始化线程池
        initializeExecutors();
        
        log.info("数据处理器管理器初始化完成，共加载{}个处理器: {}", 
                processorMap.size(), processorMap.keySet());
    }

    @PreDestroy
    public void destroy() {
        // 关闭线程池
        shutdownExecutors();
        
        // 销毁所有处理器
        destroyProcessors();
        
        log.info("数据处理器管理器已销毁");
    }

    /**
     * 处理拦截数据
     * 
     * @param data 拦截数据
     * @param processorConfigs 处理器配置列表
     */
    public void processData(InterceptData data, List<ProcessorConfig> processorConfigs) {
        if (data == null || processorConfigs == null || processorConfigs.isEmpty()) {
            return;
        }
        
        // 按优先级排序处理器配置
        List<ProcessorConfig> sortedConfigs = processorConfigs.stream()
                .sorted(Comparator.comparingInt(config -> {
                    DataProcessor processor = processorMap.get(config.getType());
                    return processor != null ? processor.getPriority() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());
        
        // 分离同步和异步处理器
        List<ProcessorTask> syncTasks = new ArrayList<>();
        List<ProcessorTask> asyncTasks = new ArrayList<>();
        
        for (ProcessorConfig config : sortedConfigs) {
            DataProcessor processor = processorMap.get(config.getType());
            if (processor == null) {
                log.warn("未找到类型为{}的数据处理器", config.getType());
                continue;
            }
            
            // 验证配置
            if (!processor.validateConfig(config.getConfig())) {
                log.warn("处理器{}的配置验证失败，跳过处理", config.getType());
                continue;
            }
            
            ProcessorTask task = new ProcessorTask(processor, data, config);
            
            if (config.isAsync() && processor.supportsAsync()) {
                asyncTasks.add(task);
            } else {
                syncTasks.add(task);
            }
        }
        
        // 执行同步处理器
        executeSyncTasks(syncTasks);
        
        // 执行异步处理器
        executeAsyncTasks(asyncTasks);
    }

    /**
     * 获取处理器
     * 
     * @param type 处理器类型
     * @return 处理器实例
     */
    public DataProcessor getProcessor(String type) {
        return processorMap.get(type);
    }

    /**
     * 获取所有处理器类型
     * 
     * @return 处理器类型列表
     */
    public Set<String> getProcessorTypes() {
        return new HashSet<>(processorMap.keySet());
    }

    /**
     * 检查处理器是否存在
     * 
     * @param type 处理器类型
     * @return true-存在，false-不存在
     */
    public boolean hasProcessor(String type) {
        return processorMap.containsKey(type);
    }

    /**
     * 初始化处理器
     */
    private void initializeProcessors() {
        if (dataProcessors == null || dataProcessors.isEmpty()) {
            log.warn("未找到任何数据处理器");
            return;
        }
        
        for (DataProcessor processor : dataProcessors) {
            try {
                String type = processor.getType();
                if (type == null || type.trim().isEmpty()) {
                    log.warn("处理器{}的类型为空，跳过注册", processor.getClass().getSimpleName());
                    continue;
                }
                
                if (processorMap.containsKey(type)) {
                    log.warn("处理器类型{}已存在，跳过重复注册", type);
                    continue;
                }
                
                // 初始化处理器
                processor.initialize(interceptorProperties.getGlobal() != null ? 
                        interceptorProperties.getGlobal().getConfig() : new HashMap<>());
                
                processorMap.put(type, processor);
                log.debug("成功注册数据处理器: {} -> {}", type, processor.getClass().getSimpleName());
                
            } catch (Exception e) {
                log.error("初始化处理器{}时发生异常", processor.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 初始化执行器
     */
    private void initializeExecutors() {
        InterceptorProperties.GlobalConfig globalConfig = interceptorProperties.getGlobal();
        
        int corePoolSize = 2;
        int maximumPoolSize = 10;
        long keepAliveTime = 60L;
        int queueCapacity = 1000;
        
        if (globalConfig != null && globalConfig.getThreadPool() != null) {
            corePoolSize = globalConfig.getThreadPool().getOrDefault("corePoolSize", corePoolSize);
            maximumPoolSize = globalConfig.getThreadPool().getOrDefault("maximumPoolSize", maximumPoolSize);
            keepAliveTime = globalConfig.getThreadPool().getOrDefault("keepAliveTime", (int) keepAliveTime);
            queueCapacity = globalConfig.getThreadPool().getOrDefault("queueCapacity", queueCapacity);
        }
        
        // 创建异步执行器
        asyncExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "interceptor-async-" + (++counter));
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 创建定时任务执行器
        scheduledExecutor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "interceptor-scheduled-" + (++counter));
                thread.setDaemon(true);
                return thread;
            }
        });
        
        log.info("数据处理器线程池初始化完成: corePoolSize={}, maximumPoolSize={}, queueCapacity={}", 
                corePoolSize, maximumPoolSize, queueCapacity);
    }

    /**
     * 执行同步任务
     */
    private void executeSyncTasks(List<ProcessorTask> tasks) {
        for (ProcessorTask task : tasks) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("执行同步处理器任务时发生异常: {}", task.getProcessor().getType(), e);
            }
        }
    }

    /**
     * 执行异步任务
     */
    private void executeAsyncTasks(List<ProcessorTask> tasks) {
        for (ProcessorTask task : tasks) {
            asyncExecutor.submit(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("执行异步处理器任务时发生异常: {}", task.getProcessor().getType(), e);
                }
            });
        }
    }

    /**
     * 关闭执行器
     */
    private void shutdownExecutors() {
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 销毁处理器
     */
    private void destroyProcessors() {
        for (DataProcessor processor : processorMap.values()) {
            try {
                processor.destroy();
            } catch (Exception e) {
                log.warn("销毁处理器{}时发生异常", processor.getType(), e);
            }
        }
        processorMap.clear();
    }

    /**
     * 处理器任务
     */
    private static class ProcessorTask {
        private final DataProcessor processor;
        private final InterceptData data;
        private final ProcessorConfig config;

        public ProcessorTask(DataProcessor processor, InterceptData data, ProcessorConfig config) {
            this.processor = processor;
            this.data = data;
            this.config = config;
        }

        public void run() {
            processor.process(data, config.getConfig());
        }

        public DataProcessor getProcessor() {
            return processor;
        }
    }
}