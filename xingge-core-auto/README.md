# xingge-core-auto 自动配置

## 概述

`xingge-core-auto` 是XingGe框架的自动配置模块，提供Spring Boot自动配置功能，让开发者能够零配置使用XingGe框架的各项功能。

## 🎯 主要功能

- **自动配置**：基于Spring Boot的自动配置机制
- **条件装配**：根据类路径和配置智能装配Bean
- **配置属性**：提供统一的配置属性管理
- **扩展点**：支持自定义配置和扩展

## 📦 自动配置内容

### 异步处理器自动配置
- **AsyncProcessorAutoConfiguration**：自动配置异步处理器
- **条件**：当类路径存在AsyncProcessor时自动装配
- **配置前缀**：`xingge.async`

### 数据库自动配置
- **TableMaintenanceAutoConfiguration**：自动配置表结构维护
- **条件**：当存在DataSource时自动装配
- **配置前缀**：`xg.db.table-maintenance`

### 请求拦截器自动配置
- **RequestInterceptorAutoConfiguration**：自动配置请求拦截器
- **条件**：当启用请求拦截时自动装配
- **配置前缀**：`xg.request`

## 🚀 使用方式

### 1. 添加依赖

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-auto</artifactId>
</dependency>
```

### 2. 自动配置生效

无需任何额外配置，Spring Boot会自动扫描并加载配置：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        // XingGe框架自动配置已生效
    }
}
```

### 3. 配置属性

```yaml
# 异步处理器配置
xingge:
  async:
    enabled: true
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 100

# 数据库表维护配置
xg:
  db:
    table-maintenance:
      enabled: true
      entity-packages:
        - com.example.entity
      async-execution: true

# 请求拦截器配置
xg:
  request:
    enabled: true
    storage-type: LOG
```

## ⚙️ 自动配置详解

### 条件装配机制

```java
@Configuration
@ConditionalOnClass(AsyncProcessor.class)
@ConditionalOnProperty(prefix = "xingge.async", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AsyncProcessorProperties.class)
public class AsyncProcessorAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public AsyncProcessor asyncProcessor(AsyncProcessorProperties properties) {
        return new AsyncProcessor(properties);
    }
}
```

### 配置属性绑定

```java
@ConfigurationProperties(prefix = "xingge.async")
public class AsyncProcessorProperties {
    
    private boolean enabled = true;
    private int corePoolSize = 5;
    private int maxPoolSize = 20;
    private int queueCapacity = 100;
    
    // getter/setter...
}
```

## 🔧 自定义配置

### 1. 覆盖默认配置

```java
@Configuration
public class CustomAsyncConfiguration {
    
    @Bean
    @Primary
    public AsyncProcessor customAsyncProcessor() {
        AsyncProcessorProperties properties = new AsyncProcessorProperties();
        properties.setCorePoolSize(10);
        properties.setMaxPoolSize(50);
        return new AsyncProcessor(properties);
    }
}
```

### 2. 条件性配置

```java
@Configuration
@ConditionalOnProperty(name = "app.async.custom", havingValue = "true")
public class ConditionalAsyncConfiguration {
    
    @Bean
    public AsyncTaskExecutor customTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Custom-Async-");
        executor.initialize();
        return executor;
    }
}
```

### 3. 禁用自动配置

```java
@SpringBootApplication(exclude = {
    AsyncProcessorAutoConfiguration.class,
    TableMaintenanceAutoConfiguration.class
})
public class Application {
    // 禁用特定的自动配置
}
```

或通过配置文件：

```yaml
spring:
  autoconfigure:
    exclude:
      - tech.msop.core.auto.AsyncProcessorAutoConfiguration
      - tech.msop.core.auto.TableMaintenanceAutoConfiguration
```

## 📋 配置优先级

1. **用户自定义Bean** - 最高优先级
2. **@Primary注解的Bean** - 高优先级
3. **自动配置的Bean** - 默认优先级
4. **@ConditionalOnMissingBean** - 最低优先级

## 🔍 调试自动配置

### 1. 启用调试日志

```yaml
logging:
  level:
    org.springframework.boot.autoconfigure: DEBUG
    tech.msop.core.auto: DEBUG
```

### 2. 查看自动配置报告

```bash
# 启动时添加参数
java -jar app.jar --debug
```

### 3. 使用Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: conditions,configprops,beans
```

访问端点查看配置信息：
- `/actuator/conditions` - 查看条件装配结果
- `/actuator/configprops` - 查看配置属性
- `/actuator/beans` - 查看Bean装配情况

## ✨ 最佳实践

### 1. 配置文件组织

```yaml
# application.yml - 基础配置
xingge:
  async:
    enabled: true
    core-pool-size: 5

---
# application-dev.yml - 开发环境
xingge:
  async:
    core-pool-size: 2
    max-pool-size: 5

---
# application-prod.yml - 生产环境
xingge:
  async:
    core-pool-size: 10
    max-pool-size: 50
```

### 2. 环境特定配置

```java
@Configuration
@Profile("production")
public class ProductionConfiguration {
    
    @Bean
    @Primary
    public AsyncProcessor productionAsyncProcessor() {
        // 生产环境特定配置
        return new AsyncProcessor(productionProperties());
    }
}
```

### 3. 配置验证

```java
@ConfigurationProperties(prefix = "xingge.async")
@Validated
public class AsyncProcessorProperties {
    
    @Min(1)
    @Max(100)
    private int corePoolSize = 5;
    
    @Min(1)
    @Max(1000)
    private int maxPoolSize = 20;
    
    // getter/setter...
}
```

## 🤝 扩展开发

### 1. 创建自定义自动配置

```java
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyServiceProperties.class)
public class MyServiceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyServiceProperties properties) {
        return new MyService(properties);
    }
}
```

### 2. 注册自动配置

在 `META-INF/spring.factories` 中添加：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.MyServiceAutoConfiguration
```

---

**xingge-core-auto** - 零配置，即插即用！