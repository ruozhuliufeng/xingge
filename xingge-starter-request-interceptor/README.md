# 行歌请求拦截器 (XingGe Request Interceptor)

一个功能强大的Spring Boot Starter，用于拦截和记录HTTP客户端请求，支持多种存储方式。

## 功能特性

- 🚀 **多客户端支持**：支持OkHttp、RestTemplate、OpenFeign等HTTP客户端
- 📊 **多存储方式**：支持日志输出、MongoDB存储、数据库存储等多种存储方式
- ⚡ **统一异步处理**：集成XingGe异步处理器，统一管理异步任务，优化性能
- 🔧 **灵活配置**：丰富的配置选项，可根据需求自定义
- 📝 **格式化输出**：美观的日志格式，便于查看和调试
- 🛡️ **数据过滤**：支持配置请求体、响应体大小限制
- 🎯 **资源优化**：使用统一线程池，避免资源浪费

## 快速开始

### 1. 添加依赖

在你的Spring Boot项目中添加以下依赖：

```xml
<!-- 请求拦截器模块 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-request-interceptor</artifactId>
    <version>0.0.2</version>
</dependency>

<!-- 核心工具模块（包含异步处理器，自动引入） -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-tool</artifactId>
    <version>0.0.2</version>
</dependency>
```

### 2. 配置文件

在`application.yml`中添加配置：

```yaml
# 异步处理器配置（可选，使用默认配置）
xingge:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 200
    thread-name-prefix: "xingge-async-"

# 请求拦截器配置
xg:
  request:
    # 启用请求拦截器
    enabled: true
    # 存储类型：LOG（日志）、MONGO（MongoDB）、DATABASE（数据库）
    storage-type: LOG
    # 是否包含请求头
    include-headers: true
    # 是否包含请求体
    include-request-body: true
    # 是否包含响应体
    include-response-body: true
    # 最大请求体大小（字节）
    max-body-size: 10240
    
    # 日志存储配置
    log:
      enabled: true
      level: INFO
      pattern: "[REQUEST-INTERCEPTOR] %s"
    
    # HTTP客户端拦截配置
    http-client:
      intercept-okhttp: true
      intercept-rest-template: true
      intercept-open-feign: true
```

### 3. 使用示例

配置完成后，框架会自动启用请求拦截器，所有HTTP请求将自动被拦截和记录：

```java
@RestController
public class TestController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/test")
    public String test() {
        // 这个请求会被自动拦截和记录
        String result = restTemplate.getForObject("https://api.example.com/data", String.class);
        return result;
    }
}
```

**注意**：无需添加任何注解，框架会通过Spring Boot自动配置机制自动启用。

## ⚡ 异步处理优势

从版本 0.0.2 开始，请求拦截器集成了XingGe异步处理器，带来以下优势：

### 统一线程池管理
- **之前**：每个存储实现都创建独立的线程池，资源浪费
- **现在**：所有异步任务使用统一的异步处理器，资源利用率更高

### 更好的异常处理
- **自动异常捕获**：异步任务异常不会影响主业务流程
- **详细日志记录**：提供任务级别的日志追踪
- **任务命名**：每个异步任务都有明确的名称，便于调试

### 配置更灵活
- **全局配置**：通过 `xingge.async` 配置项统一管理线程池参数
- **动态调整**：可根据业务需求调整线程池大小
- **监控友好**：提供详细的执行日志，便于性能监控

### 性能提升示例

```java
// 异步存储请求日志，不阻塞主业务
@RestController
public class ApiController {
    
    @GetMapping("/api/users")
    public List<User> getUsers() {
        // 1. 执行主业务逻辑
        List<User> users = userService.getAllUsers();
        
        // 2. 请求信息会被异步拦截和存储，不影响响应时间
        return users;
    }
}
```

**性能对比**：
- **同步存储**：响应时间 = 业务处理时间 + 日志存储时间
- **异步存储**：响应时间 ≈ 业务处理时间（日志存储异步执行）
```

## 存储方式配置

### 日志输出存储

将请求信息格式化输出到日志文件：

```yaml
xg:
  request:
    storage-type: LOG
    log:
      enabled: true
      level: INFO  # 日志级别：DEBUG、INFO、WARN、ERROR
      pattern: "[HTTP-REQUEST] %s"  # 日志输出格式
```

**输出效果：**
```
2024-01-01 10:30:15.123 INFO  [HTTP-REQUEST] 
┌─ 请求信息 ─────────────────────────────────────────────────────────────
│ 请求ID: 12345678-1234-1234-1234-123456789012
│ 客户端类型: RestTemplate
│ 请求方法: GET
│ 请求URL: https://api.example.com/users/1
│ 开始时间: 2024-01-01 10:30:15.100
│ 结束时间: 2024-01-01 10:30:15.123
│ 耗时: 23ms
│ 执行状态: 成功
├─ 响应信息 ───────────────────────────────────────────────────────────
│ 响应状态: 200
├─ 响应体 ─────────────────────────────────────────────────────────────
│ {"id": 1, "name": "张三", "email": "zhangsan@example.com"}
└───────────────────────────────────────────────────────────────────────
```

### MongoDB存储

将请求信息存储到MongoDB数据库：

```yaml
xg:
  request:
    storage-type: MONGO
    mongo:
      enabled: true
      # 自定义MongoDB连接地址（可选）
      uri: "mongodb://localhost:27017/request_logs"
      # 集合名称（默认：t_request_interceptor_log）
      collection-name: "http_request_logs"
      # 批量插入大小
      batch-size: 100
```

**使用默认MongoDB配置：**

如果不指定`uri`，将使用项目的默认MongoDB配置：

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/myapp

xg:
  request:
    storage-type: MONGO
    mongo:
      enabled: true
      # 不配置uri，使用上面的默认配置
      collection-name: "t_request_interceptor_log"
```

## 配置参数说明

### 基础配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xg.request.enabled` | boolean | true | 是否启用请求拦截器 |
| `xg.request.storage-type` | string | LOG | 存储类型：LOG、MONGO |
| `xg.request.include-headers` | boolean | true | 是否包含请求头信息 |
| `xg.request.include-request-body` | boolean | true | 是否包含请求体信息 |
| `xg.request.include-response-body` | boolean | true | 是否包含响应体信息 |
| `xg.request.max-body-size` | long | 10240 | 最大请求体大小（字节） |

### 日志存储配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xg.request.log.enabled` | boolean | true | 是否启用日志输出 |
| `xg.request.log.level` | string | INFO | 日志级别 |
| `xg.request.log.pattern` | string | "[REQUEST-INTERCEPTOR] %s" | 日志输出格式 |

### MongoDB存储配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xg.request.mongo.enabled` | boolean | false | 是否启用MongoDB存储 |
| `xg.request.mongo.uri` | string | - | MongoDB连接地址（可选） |
| `xg.request.mongo.collection-name` | string | t_request_interceptor_log | 集合名称 |
| `xg.request.mongo.batch-size` | int | 100 | 批量插入大小 |

### HTTP客户端配置

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xg.request.http-client.intercept-okhttp` | boolean | true | 是否拦截OkHttp请求 |
| `xg.request.http-client.intercept-rest-template` | boolean | true | 是否拦截RestTemplate请求 |
| `xg.request.http-client.intercept-open-feign` | boolean | true | 是否拦截OpenFeign请求 |

## 高级用法

### 自定义存储实现

你可以实现`RequestLogStorage`接口来创建自定义存储方式：

```java
@Component
public class CustomRequestLogStorage implements RequestLogStorage {
    
    @Override
    public void store(RequestLogInfo logInfo) throws Exception {
        // 自定义存储逻辑
    }
    
    @Override
    public String getStorageType() {
        return "CUSTOM";
    }
    
    // 实现其他必要方法...
}
```

### 条件化启用

可以根据环境或其他条件启用拦截器：

```yaml
# 仅在开发环境启用
spring:
  profiles:
    active: dev

---
spring:
  profiles: dev
  
xg:
  request:
    enabled: true
    storage-type: LOG

---
spring:
  profiles: prod
  
xg:
  request:
    enabled: false
```

## 性能考虑

- **异步处理**：默认使用异步方式存储日志，不会阻塞业务请求
- **批量存储**：MongoDB存储支持批量插入，提高性能
- **大小限制**：可配置请求体和响应体的最大大小，避免内存溢出
- **条件启用**：可根据环境灵活启用/禁用功能

## 注意事项

1. **敏感信息**：请注意不要记录包含敏感信息的请求，如密码、令牌等
2. **存储空间**：长期运行时注意日志文件或数据库的存储空间
3. **性能影响**：虽然使用异步处理，但仍会有一定的性能开销
4. **MongoDB依赖**：使用MongoDB存储时，需要确保MongoDB服务可用

## 版本历史

- **1.0.0**：初始版本，支持日志输出和MongoDB存储

## 许可证

Copyright (c) 2024 行歌(xingge)

## 联系我们

如有问题或建议，请联系：
- 作者：若竹流风
- 邮箱：support@msop.tech