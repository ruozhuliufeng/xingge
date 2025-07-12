# xingge-starter-http HTTP客户端增强

## 概述

`xingge-starter-http` 是XingGe框架的HTTP客户端增强模块，提供统一的HTTP客户端配置、请求拦截、重试机制、负载均衡等功能。

## 🎯 主要功能

- **多客户端支持**：支持RestTemplate、OkHttp、Apache HttpClient等
- **统一配置**：提供统一的HTTP客户端配置管理
- **请求拦截**：支持请求和响应拦截处理
- **重试机制**：智能重试失败的HTTP请求
- **负载均衡**：支持多服务实例的负载均衡
- **连接池管理**：优化连接池配置，提升性能
- **监控统计**：提供HTTP请求的监控和统计功能

## 📦 支持的HTTP客户端

### RestTemplate
- **Spring官方**：Spring框架内置的HTTP客户端
- **同步调用**：适合传统的同步HTTP调用场景
- **拦截器支持**：丰富的拦截器生态

### OkHttp
- **高性能**：Square公司开源的高性能HTTP客户端
- **连接池**：内置连接池，支持HTTP/2
- **异步支持**：支持同步和异步调用

### Apache HttpClient
- **功能丰富**：Apache基金会的HTTP客户端
- **配置灵活**：提供丰富的配置选项
- **协议支持**：支持多种HTTP协议版本

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-http</artifactId>
</dependency>
```

### 2. 基础配置

```yaml
xingge:
  http:
    # 启用HTTP增强功能
    enabled: true
    
    # 默认客户端类型
    default-client: resttemplate
    
    # 连接配置
    connection:
      connect-timeout: 5000
      read-timeout: 10000
      max-connections: 200
      max-connections-per-route: 50
    
    # 重试配置
    retry:
      enabled: true
      max-attempts: 3
      delay: 1000
      multiplier: 2.0
    
    # 负载均衡
    load-balancer:
      enabled: true
      strategy: round-robin
```

### 3. 使用示例

```java
@Service
public class ApiService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private OkHttpClient okHttpClient;
    
    public String callApi() {
        // 使用RestTemplate
        return restTemplate.getForObject("https://api.example.com/data", String.class);
    }
    
    public String callApiWithOkHttp() throws IOException {
        // 使用OkHttp
        Request request = new Request.Builder()
            .url("https://api.example.com/data")
            .build();
        
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
```

## ⚙️ 详细配置

### RestTemplate配置

```yaml
xingge:
  http:
    resttemplate:
      # 连接池配置
      connection-pool:
        max-total: 200
        max-per-route: 50
        connection-timeout: 5000
        socket-timeout: 10000
        connection-request-timeout: 3000
      
      # 拦截器配置
      interceptors:
        - logging
        - retry
        - metrics
      
      # 错误处理
      error-handler:
        enabled: true
        ignore-4xx: false
        ignore-5xx: false
```

### OkHttp配置

```yaml
xingge:
  http:
    okhttp:
      # 连接池配置
      connection-pool:
        max-idle-connections: 50
        keep-alive-duration: 300000
      
      # 超时配置
      timeouts:
        connect-timeout: 5000
        read-timeout: 10000
        write-timeout: 10000
        call-timeout: 30000
      
      # 拦截器
      interceptors:
        - logging
        - retry
        - auth
```

### Apache HttpClient配置

```yaml
xingge:
  http:
    httpclient:
      # 连接管理
      connection-manager:
        max-total: 200
        max-per-route: 50
        validate-after-inactivity: 2000
      
      # 请求配置
      request-config:
        connect-timeout: 5000
        socket-timeout: 10000
        connection-request-timeout: 3000
        redirects-enabled: true
        max-redirects: 3
```

## 🔧 高级功能

### 1. 自定义拦截器

```java
@Component
public class CustomHttpInterceptor implements ClientHttpRequestInterceptor {
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        // 请求前处理
        request.getHeaders().add("X-Custom-Header", "custom-value");
        
        // 执行请求
        ClientHttpResponse response = execution.execute(request, body);
        
        // 响应后处理
        logResponse(response);
        
        return response;
    }
    
    private void logResponse(ClientHttpResponse response) {
        // 记录响应信息
    }
}
```

### 2. 重试策略

```java
@Configuration
public class RetryConfiguration {
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 重试策略
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 重试条件
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}
```

### 3. 负载均衡

```java
@Service
public class LoadBalancedApiService {
    
    @Autowired
    @LoadBalanced
    private RestTemplate loadBalancedRestTemplate;
    
    public String callService() {
        // 自动负载均衡到不同的服务实例
        return loadBalancedRestTemplate.getForObject(
            "http://user-service/api/users", 
            String.class
        );
    }
}
```

### 4. 熔断器集成

```java
@Service
public class CircuitBreakerApiService {
    
    @CircuitBreaker(name = "api-service", fallbackMethod = "fallbackMethod")
    public String callApi() {
        return restTemplate.getForObject("https://api.example.com/data", String.class);
    }
    
    public String fallbackMethod(Exception ex) {
        return "服务暂时不可用，请稍后重试";
    }
}
```

## 📊 监控和统计

### 1. 启用监控

```yaml
xingge:
  http:
    monitoring:
      enabled: true
      metrics:
        - request-count
        - response-time
        - error-rate
        - connection-pool
```

### 2. 自定义指标

```java
@Component
public class HttpMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer responseTimer;
    
    public HttpMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("http.requests.total")
            .description("Total HTTP requests")
            .register(meterRegistry);
        this.responseTimer = Timer.builder("http.requests.duration")
            .description("HTTP request duration")
            .register(meterRegistry);
    }
    
    public void recordRequest(String method, String uri, int status, long duration) {
        requestCounter.increment(
            Tags.of(
                "method", method,
                "uri", uri,
                "status", String.valueOf(status)
            )
        );
        
        responseTimer.record(duration, TimeUnit.MILLISECONDS);
    }
}
```

## 🔍 故障排查

### 1. 启用调试日志

```yaml
logging:
  level:
    tech.msop.http: DEBUG
    org.springframework.web.client: DEBUG
    okhttp3: DEBUG
```

### 2. 连接池监控

```java
@Component
public class ConnectionPoolMonitor {
    
    @EventListener
    public void onConnectionPoolStats(ConnectionPoolStatsEvent event) {
        log.info("连接池状态: 活跃连接={}, 空闲连接={}, 等待连接={}", 
            event.getActiveConnections(),
            event.getIdleConnections(),
            event.getPendingConnections());
    }
}
```

## 📋 最佳实践

### 1. 连接池配置

```yaml
# 根据业务量调整连接池大小
xingge:
  http:
    connection:
      # 总连接数 = 并发请求数 * 1.5
      max-connections: 300
      # 单路由连接数 = 单个服务并发数
      max-connections-per-route: 50
      # 连接超时要短，避免长时间等待
      connect-timeout: 3000
      # 读取超时根据业务调整
      read-timeout: 10000
```

### 2. 重试策略

```yaml
xingge:
  http:
    retry:
      # 只对幂等操作重试
      enabled: true
      max-attempts: 3
      # 指数退避
      delay: 1000
      multiplier: 2.0
      # 只重试特定异常
      retryable-exceptions:
        - java.net.SocketTimeoutException
        - java.net.ConnectException
```

### 3. 安全配置

```java
@Configuration
public class HttpSecurityConfiguration {
    
    @Bean
    public RestTemplate secureRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 添加认证拦截器
        restTemplate.getInterceptors().add(new AuthenticationInterceptor());
        
        // 配置SSL
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(createSecureHttpClient());
        restTemplate.setRequestFactory(factory);
        
        return restTemplate;
    }
    
    private CloseableHttpClient createSecureHttpClient() {
        // SSL配置
        SSLContext sslContext = SSLContexts.createDefault();
        SSLConnectionSocketFactory sslSocketFactory = 
            new SSLConnectionSocketFactory(sslContext);
        
        return HttpClients.custom()
            .setSSLSocketFactory(sslSocketFactory)
            .build();
    }
}
```

## 🤝 扩展开发

### 1. 自定义HTTP客户端

```java
@Configuration
public class CustomHttpClientConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "xingge.http.client", havingValue = "custom")
    public HttpClient customHttpClient() {
        return new CustomHttpClient();
    }
}
```

### 2. 插件开发

```java
public interface HttpClientPlugin {
    
    void configure(HttpClientBuilder builder);
    
    int getOrder();
}

@Component
public class LoggingPlugin implements HttpClientPlugin {
    
    @Override
    public void configure(HttpClientBuilder builder) {
        builder.addInterceptor(new LoggingInterceptor());
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
}
```

---

**xingge-starter-http** - 强化HTTP，提升体验！