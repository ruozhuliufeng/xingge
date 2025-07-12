# xingge-starter-log 日志增强

## 概述

`xingge-starter-log` 是XingGe框架的日志增强模块，提供统一的日志配置、结构化日志、日志聚合、性能监控等功能，让日志管理更加智能和高效。

## 🎯 主要功能

- **统一日志配置**：提供开箱即用的日志配置模板
- **结构化日志**：支持JSON格式的结构化日志输出
- **链路追踪**：集成分布式链路追踪功能
- **性能监控**：自动记录方法执行时间和性能指标
- **敏感信息脱敏**：自动脱敏敏感信息
- **日志聚合**：支持多种日志收集和聚合方案
- **异步日志**：高性能异步日志输出
- **动态日志级别**：运行时动态调整日志级别

## 📦 支持的日志框架

### Logback
- **Spring Boot默认**：Spring Boot默认的日志实现
- **配置灵活**：支持XML和Groovy配置
- **性能优秀**：高性能的日志框架

### Log4j2
- **Apache出品**：Apache基金会的日志框架
- **异步优化**：优秀的异步日志性能
- **插件丰富**：丰富的插件生态

### SLF4J
- **门面模式**：统一的日志门面接口
- **框架无关**：可以切换不同的日志实现
- **广泛支持**：被大多数Java框架支持

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-log</artifactId>
</dependency>
```

### 2. 基础配置

```yaml
xingge:
  log:
    # 启用日志增强功能
    enabled: true
    
    # 日志级别
    level: INFO
    
    # 结构化日志
    structured:
      enabled: true
      format: json
    
    # 链路追踪
    tracing:
      enabled: true
      trace-id-header: X-Trace-Id
    
    # 性能监控
    performance:
      enabled: true
      slow-threshold: 1000
    
    # 敏感信息脱敏
    desensitization:
      enabled: true
      patterns:
        - phone
        - email
        - idcard
        - password
```

### 3. 使用示例

```java
@Service
@Slf4j
public class UserService {
    
    @LogPerformance
    public User getUserById(Long id) {
        log.info("查询用户信息, userId={}", id);
        
        User user = userRepository.findById(id);
        
        // 结构化日志
        StructuredLogger.info("user_query")
            .field("userId", id)
            .field("userName", user.getName())
            .field("userStatus", user.getStatus())
            .log();
        
        return user;
    }
    
    @LogTrace
    public void updateUser(User user) {
        log.info("更新用户信息, user={}", user);
        userRepository.save(user);
    }
}
```

## ⚙️ 详细配置

### 结构化日志配置

```yaml
xingge:
  log:
    structured:
      enabled: true
      format: json
      
      # 字段配置
      fields:
        timestamp: "@timestamp"
        level: "level"
        logger: "logger"
        message: "message"
        thread: "thread"
        trace-id: "traceId"
        span-id: "spanId"
      
      # 额外字段
      additional-fields:
        application: "${spring.application.name}"
        environment: "${spring.profiles.active}"
        hostname: "${HOSTNAME:localhost}"
```

### 链路追踪配置

```yaml
xingge:
  log:
    tracing:
      enabled: true
      
      # 追踪ID配置
      trace-id:
        header-name: "X-Trace-Id"
        mdc-key: "traceId"
        generator: uuid
      
      # Span ID配置
      span-id:
        header-name: "X-Span-Id"
        mdc-key: "spanId"
        generator: snowflake
      
      # 传播配置
      propagation:
        http-headers: true
        mq-headers: true
        async-tasks: true
```

### 性能监控配置

```yaml
xingge:
  log:
    performance:
      enabled: true
      
      # 慢查询阈值(毫秒)
      slow-threshold: 1000
      
      # 监控范围
      monitor:
        controllers: true
        services: true
        repositories: true
        custom-annotations: true
      
      # 输出格式
      output:
        include-parameters: true
        include-return-value: false
        max-parameter-length: 1000
```

### 脱敏配置

```yaml
xingge:
  log:
    desensitization:
      enabled: true
      
      # 预定义模式
      patterns:
        phone:
          regex: "(\\d{3})\\d{4}(\\d{4})"
          replacement: "$1****$2"
        email:
          regex: "(\\w+)@(\\w+)"
          replacement: "$1***@$2"
        idcard:
          regex: "(\\d{6})\\d{8}(\\d{4})"
          replacement: "$1********$2"
      
      # 自定义模式
      custom-patterns:
        - name: "credit-card"
          regex: "(\\d{4})\\d{8}(\\d{4})"
          replacement: "$1****$2"
```

## 🔧 高级功能

### 1. 自定义日志注解

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog {
    
    /**
     * 业务模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 是否记录参数
     */
    boolean includeArgs() default true;
    
    /**
     * 是否记录返回值
     */
    boolean includeResult() default false;
}

@Service
public class OrderService {
    
    @BusinessLog(module = "订单", operation = "创建订单")
    public Order createOrder(OrderRequest request) {
        // 业务逻辑
        return order;
    }
}
```

### 2. 结构化日志工具

```java
@Component
public class StructuredLogger {
    
    public static LogBuilder info(String event) {
        return new LogBuilder(LogLevel.INFO, event);
    }
    
    public static LogBuilder error(String event) {
        return new LogBuilder(LogLevel.ERROR, event);
    }
    
    public static class LogBuilder {
        private final Map<String, Object> fields = new HashMap<>();
        private final LogLevel level;
        private final String event;
        
        public LogBuilder(LogLevel level, String event) {
            this.level = level;
            this.event = event;
            this.fields.put("event", event);
            this.fields.put("timestamp", Instant.now());
        }
        
        public LogBuilder field(String key, Object value) {
            this.fields.put(key, value);
            return this;
        }
        
        public LogBuilder exception(Throwable throwable) {
            this.fields.put("exception", throwable.getClass().getSimpleName());
            this.fields.put("error_message", throwable.getMessage());
            return this;
        }
        
        public void log() {
            String jsonLog = JsonUtils.toJson(fields);
            switch (level) {
                case INFO:
                    log.info(jsonLog);
                    break;
                case ERROR:
                    log.error(jsonLog);
                    break;
                // 其他级别...
            }
        }
    }
}
```

### 3. 动态日志级别

```java
@RestController
@RequestMapping("/admin/log")
public class LogLevelController {
    
    @Autowired
    private LoggingSystem loggingSystem;
    
    @PostMapping("/level")
    public ResponseEntity<String> changeLogLevel(
            @RequestParam String logger,
            @RequestParam String level) {
        
        LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
        loggingSystem.setLogLevel(logger, logLevel);
        
        return ResponseEntity.ok("日志级别已更新");
    }
    
    @GetMapping("/level")
    public ResponseEntity<Map<String, String>> getLogLevels() {
        // 返回当前日志级别配置
        return ResponseEntity.ok(getCurrentLogLevels());
    }
}
```

### 4. 日志聚合集成

```yaml
xingge:
  log:
    aggregation:
      enabled: true
      
      # ELK集成
      elk:
        enabled: true
        elasticsearch:
          hosts: ["localhost:9200"]
          index-pattern: "app-logs-%{+YYYY.MM.dd}"
        logstash:
          host: "localhost"
          port: 5044
      
      # Fluentd集成
      fluentd:
        enabled: false
        host: "localhost"
        port: 24224
        tag: "app.logs"
      
      # 自定义输出
      custom:
        enabled: false
        appender-class: "com.example.CustomLogAppender"
```

## 📊 监控和分析

### 1. 日志指标收集

```java
@Component
public class LogMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter errorCounter;
    private final Counter warnCounter;
    private final Timer performanceTimer;
    
    public LogMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.errorCounter = Counter.builder("log.errors.total")
            .description("Total error logs")
            .register(meterRegistry);
        this.warnCounter = Counter.builder("log.warnings.total")
            .description("Total warning logs")
            .register(meterRegistry);
        this.performanceTimer = Timer.builder("method.execution.time")
            .description("Method execution time")
            .register(meterRegistry);
    }
    
    @EventListener
    public void onLogEvent(LogEvent event) {
        if (event.getLevel() == Level.ERROR) {
            errorCounter.increment();
        } else if (event.getLevel() == Level.WARN) {
            warnCounter.increment();
        }
    }
}
```

### 2. 日志分析工具

```java
@Service
public class LogAnalysisService {
    
    public LogStatistics analyzeLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        // 分析指定时间范围内的日志
        return LogStatistics.builder()
            .totalLogs(getTotalLogs(start, end))
            .errorCount(getErrorCount(start, end))
            .warnCount(getWarnCount(start, end))
            .avgResponseTime(getAvgResponseTime(start, end))
            .topErrors(getTopErrors(start, end))
            .build();
    }
    
    public List<PerformanceIssue> findPerformanceIssues() {
        // 查找性能问题
        return performanceLogRepository.findSlowMethods();
    }
}
```

## 🔍 故障排查

### 1. 日志调试

```yaml
# 启用详细日志
logging:
  level:
    tech.msop.log: DEBUG
    org.springframework.boot.logging: DEBUG
    
# 输出到文件
logging:
  file:
    name: logs/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. 性能分析

```java
@Component
public class LogPerformanceAnalyzer {
    
    @EventListener
    public void onPerformanceLog(PerformanceLogEvent event) {
        if (event.getExecutionTime() > 5000) {
            log.warn("检测到慢方法: {} 执行时间: {}ms", 
                event.getMethodName(), 
                event.getExecutionTime());
            
            // 发送告警
            alertService.sendSlowMethodAlert(event);
        }
    }
}
```

## 📋 最佳实践

### 1. 日志级别使用

```java
@Service
public class BestPracticeService {
    
    public void demonstrateLogLevels() {
        // ERROR: 系统错误，需要立即处理
        log.error("数据库连接失败", exception);
        
        // WARN: 警告信息，可能影响功能
        log.warn("缓存未命中，使用数据库查询");
        
        // INFO: 重要的业务信息
        log.info("用户登录成功, userId={}", userId);
        
        // DEBUG: 调试信息，生产环境关闭
        log.debug("方法参数: {}", parameters);
        
        // TRACE: 详细的执行路径
        log.trace("进入方法: {}", methodName);
    }
}
```

### 2. 结构化日志实践

```java
public class StructuredLogExample {
    
    public void goodExample() {
        // 好的实践：结构化信息
        StructuredLogger.info("user_operation")
            .field("operation", "login")
            .field("userId", userId)
            .field("ip", clientIp)
            .field("userAgent", userAgent)
            .field("success", true)
            .log();
    }
    
    public void badExample() {
        // 不好的实践：非结构化信息
        log.info("用户 {} 从 {} 使用 {} 登录成功", userId, clientIp, userAgent);
    }
}
```

### 3. 异常日志处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        // 业务异常：记录WARN级别
        StructuredLogger.warn("business_exception")
            .field("errorCode", e.getErrorCode())
            .field("errorMessage", e.getMessage())
            .field("userId", getCurrentUserId())
            .log();
        
        return ResponseEntity.badRequest().body(new ErrorResponse(e));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleSystemException(Exception e) {
        // 系统异常：记录ERROR级别
        StructuredLogger.error("system_exception")
            .field("exceptionType", e.getClass().getSimpleName())
            .field("errorMessage", e.getMessage())
            .field("stackTrace", getStackTrace(e))
            .exception(e)
            .log();
        
        return ResponseEntity.status(500).body(new ErrorResponse("系统异常"));
    }
}
```

## 🤝 扩展开发

### 1. 自定义日志处理器

```java
public interface LogProcessor {
    
    void process(LogEvent event);
    
    boolean supports(LogEvent event);
    
    int getOrder();
}

@Component
public class SecurityLogProcessor implements LogProcessor {
    
    @Override
    public void process(LogEvent event) {
        if (isSecurityEvent(event)) {
            // 安全日志特殊处理
            securityAuditService.audit(event);
        }
    }
    
    @Override
    public boolean supports(LogEvent event) {
        return event.getLogger().startsWith("security");
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
}
```

### 2. 自定义脱敏器

```java
public interface DataDesensitizer {
    
    String desensitize(String data);
    
    boolean supports(String fieldName);
}

@Component
public class CustomDesensitizer implements DataDesensitizer {
    
    @Override
    public String desensitize(String data) {
        // 自定义脱敏逻辑
        return data.replaceAll("(\\d{4})\\d{8}(\\d{4})", "$1****$2");
    }
    
    @Override
    public boolean supports(String fieldName) {
        return "bankCard".equals(fieldName);
    }
}
```

---

**xingge-starter-log** - 智能日志，洞察一切！