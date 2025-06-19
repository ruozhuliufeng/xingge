# Xingge Interceptor 拦截器模块

## 概述

Xingge Interceptor 是一个强大的HTTP请求/响应拦截器模块，支持拦截项目的外部请求和内部向外部发送的请求，并根据配置规则进行多种方式的数据处理。

## 功能特性

- **双向拦截**：支持拦截外部进入的HTTP请求和内部发出的HTTP请求
- **多客户端支持**：支持RestTemplate、OkHttp、Feign、HttpURLConnection等多种HTTP客户端
- **灵活配置**：支持基于URL模式、HTTP方法等条件的灵活拦截配置
- **多种处理器**：内置日志、MySQL、MongoDB、PostgreSQL、RocketMQ等多种数据处理器
- **异步处理**：支持异步数据处理，不影响业务性能
- **批量操作**：支持批量数据插入，提高处理效率
- **自动配置**：基于Spring Boot自动配置，开箱即用

## 支持的HTTP客户端

### 1. RestTemplate
- **拦截器**: `HttpClientInterceptor` 和 `HttpTemplateInterceptor`
- **说明**: Spring原生HTTP客户端，支持同步请求
- **自动配置**: 通过`HttpClientConfig`自动注册拦截器

### 2. OkHttp
- **拦截器**: `OkHttpInterceptor`
- **说明**: 高性能HTTP客户端，支持连接池和异步请求
- **自动配置**: 通过`HttpClientConfig`自动创建带拦截器的`OkHttpClient`

### 3. Feign
- **拦截器**: `FeignInterceptor`
- **说明**: 声明式HTTP客户端，常用于微服务间调用
- **自动配置**: 通过`FeignConfig`自动注册为`RequestInterceptor`

### 4. HttpURLConnection
- **拦截器**: `HttpClientInterceptor`
- **说明**: Java原生HTTP客户端
- **使用方式**: 通过RestTemplate包装使用

## 支持的处理器

1. **日志记录处理器** (`log`)
   - 支持不同日志级别
   - 支持MDC上下文设置
   - 可配置记录内容（请求头、请求体、响应体等）

2. **MySQL数据库处理器** (`mysql`)
   - 支持自定义表名
   - 支持批量插入
   - 自动创建表结构
   - 支持自定义字段

3. **MongoDB处理器** (`mongo`)
   - 支持自定义集合名和数据库名
   - 支持批量插入
   - 支持自动创建索引
   - 支持TTL过期设置

4. **PostgreSQL数据库处理器** (`postgresql`)
   - 支持自定义表名和Schema
   - 支持JSONB数据类型
   - 支持批量插入
   - 自动创建表结构和索引

5. **RocketMQ消息处理器** (`rocketmq`)
   - 支持自定义Topic和Tag
   - 支持多种消息格式
   - 支持自定义消息属性
   - 支持重试机制

## 快速开始

### 1. 添加依赖

在你的项目中添加以下依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-interceptor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
xingge:
  interceptor:
    enabled: true
    global:
      record-request-body: true
      record-response-body: true
    interceptors:
      - intercept-type: request
        intercept-scope: external
        include-patterns:
          - "/api/**"
        processors:
          - type: log
            async: false
            config:
              level: INFO
```

### 3. 启用自动配置

确保你的Spring Boot应用能够扫描到拦截器组件，或者在主类上添加：

```java
@SpringBootApplication
@ComponentScan(basePackages = {"your.package", "tech.msop.xingge.interceptor"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 配置说明

### 全局配置

```yaml
xingge:
  interceptor:
    global:
      record-request-body: true      # 是否记录请求体
      record-response-body: true     # 是否记录响应体
      thread-pool:                   # 线程池配置
        core-size: 5
        max-size: 20
        queue-capacity: 1000
```

### 拦截器配置

```yaml
interceptors:
  - intercept-type: request        # 拦截类型：request, response, all
    intercept-scope: external      # 拦截范围：external, internal, all
    include-patterns:              # 包含的URL模式
      - "/api/**"
    exclude-patterns:              # 排除的URL模式
      - "/api/health"
    methods:                       # HTTP方法过滤
      - GET
      - POST
    processors:                    # 处理器列表
      - type: log
        async: false
        config: {}
```

### 处理器配置

#### 日志处理器

```yaml
- type: log
  config:
    level: INFO                    # 日志级别
    include-headers: true          # 是否包含请求头
    include-request-body: true     # 是否包含请求体
    include-response-body: true    # 是否包含响应体
    mdc-keys:                      # MDC键列表
      - traceId
      - userId
```

#### MySQL处理器

```yaml
- type: mysql
  config:
    table-name: http_request_log   # 表名
    batch-size: 100               # 批量大小
    custom-fields:                # 自定义字段
      app_name: "${applicationName}"
```

#### MongoDB处理器

```yaml
- type: mongo
  config:
    collection-name: http_requests # 集合名
    database-name: logs           # 数据库名
    batch-size: 50               # 批量大小
    create-index: true           # 是否创建索引
    ttl-seconds: 2592000         # TTL过期时间（秒）
```

#### PostgreSQL处理器

```yaml
- type: postgresql
  config:
    table-name: requests         # 表名
    schema: logs                 # Schema名
    batch-size: 200             # 批量大小
    use-jsonb: true             # 是否使用JSONB
```

#### RocketMQ处理器

```yaml
- type: rocketmq
  config:
    topic: http-requests         # Topic名
    tag: external               # Tag
    producer-group: interceptor  # 生产者组
    name-server: localhost:9876  # NameServer地址
    send-timeout: 3000          # 发送超时时间
    retry-times: 2              # 重试次数
```

## 扩展开发

### 自定义处理器

实现 `DataProcessor` 接口来创建自定义处理器：

```java
@Component
public class CustomDataProcessor implements DataProcessor {
    
    @Override
    public String getType() {
        return "custom";
    }
    
    @Override
    public void processData(InterceptData data, Map<String, Object> config) {
        // 自定义处理逻辑
    }
    
    // 实现其他必要方法...
}
```

### 数据模型

`InterceptData` 包含以下主要字段：

- `id`: 唯一标识
- `interceptType`: 拦截类型
- `interceptScope`: 拦截范围
- `method`: HTTP方法
- `url`: 完整URL
- `path`: 请求路径
- `headers`: 请求头
- `requestBody`: 请求体
- `responseStatus`: 响应状态码
- `responseHeaders`: 响应头
- `responseBody`: 响应体
- `duration`: 请求耗时
- `timestamp`: 时间戳
- `clientIp`: 客户端IP
- `userId`: 用户ID
- `sessionId`: 会话ID
- `tenantId`: 租户ID

## 注意事项

1. **性能考虑**：建议对高频接口使用异步处理器
2. **存储空间**：注意配置合适的TTL和清理策略
3. **敏感数据**：避免记录敏感信息如密码、token等
4. **网络依赖**：确保数据库和消息队列的连接稳定性
5. **配置验证**：启动时会验证处理器配置的有效性

## 版本历史

- **1.0.0**: 初始版本，支持基本的拦截和处理功能

## 许可证

MIT License