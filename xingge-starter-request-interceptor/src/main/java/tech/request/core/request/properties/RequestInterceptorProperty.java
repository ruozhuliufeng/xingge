/*
 * Copyright (c) 2024 行歌(xingge)
 * 请求拦截器配置属性
 * 
 * 功能说明：
 * - 定义请求拦截器的所有配置选项
 * - 支持多种存储方式配置
 * - 支持多种HTTP客户端拦截配置
 */
package tech.request.core.request.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 请求拦截器配置属性类
 * 
 * <p>该类定义了请求拦截器的所有配置选项，包括：</p>
 * <ul>
 *   <li>是否启用拦截器</li>
 *   <li>数据存储方式配置</li>
 *   <li>各种HTTP客户端的拦截配置</li>
 *   <li>日志记录配置</li>
 * </ul>
 * 
 * <p>配置示例：</p>
 * <pre>
 * xg:
 *   request:
 *     enabled: true
 *     storage-type: LOG
 *     log:
 *       enabled: true
 *       include-headers: true
 *       include-body: true
 *     database:
 *       enabled: false
 *       table-name: request_log
 * </pre>
 * 
 * @author 若竹流风
 * @version 1.0.0
 * @since 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "xg.request")
public class RequestInterceptorProperty {
    
    /**
     * 是否启用请求拦截器
     */
    private boolean enabled = true;
    
    /**
     * 数据存储类型
     */
    private StorageType storageType = StorageType.LOG;
    
    /**
     * 是否拦截请求头
     */
    private boolean includeHeaders = true;
    
    /**
     * 是否拦截请求体
     */
    private boolean includeRequestBody = true;
    
    /**
     * 是否拦截响应体
     */
    private boolean includeResponseBody = true;
    
    /**
     * 最大请求体大小（字节），超过此大小的请求体将被截断
     */
    private long maxBodySize = 1024 * 1024; // 1MB
    
    /**
     * 日志配置
     */
    private LogConfig log = new LogConfig();
    
    /**
     * 数据库配置
     */
    private DatabaseConfig database = new DatabaseConfig();
    
    /**
     * MongoDB配置
     */
    private MongoConfig mongo = new MongoConfig();
    
    /**
     * Kafka配置
     */
    private KafkaConfig kafka = new KafkaConfig();
    
    /**
     * RocketMQ配置
     */
    private RocketMqConfig rocketMq = new RocketMqConfig();
    
    /**
     * 接口配置
     */
    private ApiConfig api = new ApiConfig();
    
    /**
     * HTTP客户端配置
     */
    private HttpClientConfig httpClient = new HttpClientConfig();
    
    /**
     * 数据存储类型枚举
     */
    public enum StorageType {
        /**
         * 日志文件
         */
        LOG,
        /**
         * 数据库
         */
        DATABASE,
        /**
         * MongoDB
         */
        MONGO,
        /**
         * Kafka消息队列
         */
        KAFKA,
        /**
         * RocketMQ消息队列
         */
        ROCKET_MQ,
        /**
         * 自定义接口
         */
        API
    }
    
    /**
     * 日志配置类
     */
    public static class LogConfig {
        /**
         * 是否启用日志存储
         */
        private boolean enabled = true;
        
        /**
         * 日志级别
         */
        private String level = "INFO";
        
        /**
         * 日志格式
         */
        private String pattern = "[REQUEST-INTERCEPTOR] %s";
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
    
    /**
     * 数据库配置类
     */
    public static class DatabaseConfig {
        /**
         * 是否启用数据库存储
         */
        private boolean enabled = false;
        
        /**
         * 表名
         */
        private String tableName = "request_log";
        
        /**
         * 批量插入大小
         */
        private int batchSize = 100;
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public void setTableName(String tableName) {
            this.tableName = tableName;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }
    
    /**
     * MongoDB配置类
     */
    public static class MongoConfig {
        /**
         * 是否启用MongoDB存储
         */
        private boolean enabled = false;
        
        /**
         * MongoDB连接地址
         */
        private String uri;
        
        /**
         * 集合名称
         */
        private String collectionName = "t_request_interceptor_log";
        
        /**
         * 数据库名称
         */
        private String databaseName = "request_logs";
        
        /**
         * 批量插入大小
         */
        private int batchSize = 100;
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getCollectionName() {
            return collectionName;
        }
        
        public void setCollectionName(String collectionName) {
            this.collectionName = collectionName;
        }
        
        public String getDatabaseName() {
            return databaseName;
        }
        
        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
        
        public String getUri() {
            return uri;
        }
        
        public void setUri(String uri) {
            this.uri = uri;
        }
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }
    
    /**
     * Kafka配置类
     */
    public static class KafkaConfig {
        /**
         * 是否启用Kafka存储
         */
        private boolean enabled = false;
        
        /**
         * 主题名称
         */
        private String topic = "request-log";
        
        /**
         * 分区数
         */
        private int partitions = 1;
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getTopic() {
            return topic;
        }
        
        public void setTopic(String topic) {
            this.topic = topic;
        }
        
        public int getPartitions() {
            return partitions;
        }
        
        public void setPartitions(int partitions) {
            this.partitions = partitions;
        }
    }
    
    /**
     * RocketMQ配置类
     */
    public static class RocketMqConfig {
        /**
         * 是否启用RocketMQ存储
         */
        private boolean enabled = false;
        
        /**
         * 主题名称
         */
        private String topic = "request-log";
        
        /**
         * 标签
         */
        private String tag = "REQUEST";
        
        /**
         * 生产者组
         */
        private String producerGroup = "request-interceptor-producer";
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getTopic() {
            return topic;
        }
        
        public void setTopic(String topic) {
            this.topic = topic;
        }
        
        public String getTag() {
            return tag;
        }
        
        public void setTag(String tag) {
            this.tag = tag;
        }
        
        public String getProducerGroup() {
            return producerGroup;
        }
        
        public void setProducerGroup(String producerGroup) {
            this.producerGroup = producerGroup;
        }
    }
    
    /**
     * 接口配置类
     */
    public static class ApiConfig {
        /**
         * 是否启用接口存储
         */
        private boolean enabled = false;
        
        /**
         * 接口URL
         */
        private String url;
        
        /**
         * 请求方法
         */
        private String method = "POST";
        
        /**
         * 连接超时时间（毫秒）
         */
        private int connectTimeout = 5000;
        
        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 10000;
        
        // getter和setter方法
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public int getConnectTimeout() {
            return connectTimeout;
        }
        
        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
        
        public int getReadTimeout() {
            return readTimeout;
        }
        
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
    
    /**
     * HTTP客户端配置类
     */
    public static class HttpClientConfig {
        /**
         * 是否拦截OkHttp请求
         */
        private boolean okHttpEnabled = true;
        
        /**
         * 是否拦截RestTemplate请求
         */
        private boolean restTemplateEnabled = true;
        
        /**
         * 是否拦截OpenFeign请求
         */
        private boolean openFeignEnabled = true;
        
        // getter和setter方法
        public boolean isOkHttpEnabled() {
            return okHttpEnabled;
        }
        
        public void setOkHttpEnabled(boolean okHttpEnabled) {
            this.okHttpEnabled = okHttpEnabled;
        }
        
        public boolean isRestTemplateEnabled() {
            return restTemplateEnabled;
        }
        
        public void setRestTemplateEnabled(boolean restTemplateEnabled) {
            this.restTemplateEnabled = restTemplateEnabled;
        }
        
        public boolean isOpenFeignEnabled() {
            return openFeignEnabled;
        }
        
        public void setOpenFeignEnabled(boolean openFeignEnabled) {
            this.openFeignEnabled = openFeignEnabled;
        }
    }
    
    // 主类的getter和setter方法
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public StorageType getStorageType() {
        return storageType;
    }
    
    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }
    
    public boolean isIncludeHeaders() {
        return includeHeaders;
    }
    
    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }
    
    public boolean isIncludeRequestBody() {
        return includeRequestBody;
    }
    
    public void setIncludeRequestBody(boolean includeRequestBody) {
        this.includeRequestBody = includeRequestBody;
    }
    
    public boolean isIncludeResponseBody() {
        return includeResponseBody;
    }
    
    public void setIncludeResponseBody(boolean includeResponseBody) {
        this.includeResponseBody = includeResponseBody;
    }
    
    public long getMaxBodySize() {
        return maxBodySize;
    }
    
    public void setMaxBodySize(long maxBodySize) {
        this.maxBodySize = maxBodySize;
    }
    
    public LogConfig getLog() {
        return log;
    }
    
    public void setLog(LogConfig log) {
        this.log = log;
    }
    
    public DatabaseConfig getDatabase() {
        return database;
    }
    
    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }
    
    public MongoConfig getMongo() {
        return mongo;
    }
    
    public void setMongo(MongoConfig mongo) {
        this.mongo = mongo;
    }
    
    public KafkaConfig getKafka() {
        return kafka;
    }
    
    public void setKafka(KafkaConfig kafka) {
        this.kafka = kafka;
    }
    
    public RocketMqConfig getRocketMq() {
        return rocketMq;
    }
    
    public void setRocketMq(RocketMqConfig rocketMq) {
        this.rocketMq = rocketMq;
    }
    
    public ApiConfig getApi() {
        return api;
    }
    
    public void setApi(ApiConfig api) {
        this.api = api;
    }
    
    public HttpClientConfig getHttpClient() {
        return httpClient;
    }
    
    public void setHttpClient(HttpClientConfig httpClient) {
        this.httpClient = httpClient;
    }
}