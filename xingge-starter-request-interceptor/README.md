# 行歌请求拦截器 (XingGe Request Interceptor)

一个功能强大的Spring Boot Starter，用于拦截和记录HTTP客户端请求，支持多种存储方式。

## 功能特性

- 🚀 **多客户端支持**：支持OkHttp、RestTemplate、OpenFeign等HTTP客户端
- 📊 **多存储方式**：支持日志输出、MongoDB存储等多种存储方式
- ⚡ **异步处理**：支持同步和异步存储模式，不影响业务性能
- 🔧 **灵活配置**：丰富的配置选项，可根据需求自定义
- 📝 **格式化输出**：美观的日志格式，便于查看和调试
- 🛡️ **数据过滤**：支持配置请求体、响应体大小限制

## 快速开始

### 1. 添加依赖

在你的Spring Boot项目中添加以下依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-request-interceptor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置文件

在`application.yml`中添加配置：

```yaml
xg:
  request:
    # 启用请求拦截器
    enabled: true
    # 存储类型：LOG（日志）、MONGO（MongoDB）
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