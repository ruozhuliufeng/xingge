# LogIndex 注解使用指南

## 概述

LogIndex 是行歌(xingge)日志模块提供的一个强大注解，用于自动将对象字段作为日志索引添加到 MDC（Mapped Diagnostic Context）中。通过 AOP 切面技术，LogIndex 能够在方法执行时自动提取带注解的字段值，并将其添加到日志上下文中，极大地提升了日志的可追踪性和可检索性。

## 核心特性

- 🎯 **自动索引提取**：自动扫描方法参数中带有 @LogIndex 注解的字段
- 🔧 **灵活配置**：支持自定义索引名称、前缀、启用状态等
- 🚀 **高性能**：基于 AOP 切面，对业务代码零侵入
- 🛡️ **安全可靠**：自动 MDC 清理，避免内存泄漏和上下文污染
- 📊 **丰富配置**：支持长度限制、嵌套扫描、调试模式等高级配置
- 🔍 **调试友好**：提供详细的调试日志和错误处理

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-log</artifactId>
    <version>最新版本</version>
</dependency>
```

### 2. 基本配置

在 `application.yml` 中添加配置：

```yaml
xg:
  log:
    enabled: true
    log-index:
      enabled: true
      debug-enabled: false
```

### 3. 使用注解

在需要作为日志索引的字段上添加 `@LogIndex` 注解：

```java
@Data
public class UserRequest {
    @LogIndex
    private String requestId;
    
    @LogIndex(name = "userId")
    private Long id;
    
    @LogIndex(name = "type", prefix = "CLIENT_")
    private String clientType;
    
    private String userName; // 不会被索引
}
```

### 4. 业务方法调用

```java
@Service
public class UserService {
    
    public String processUser(UserRequest request) {
        // LogIndex 切面会自动将注解字段添加到 MDC
        // MDC.put("requestId", request.getRequestId())
        // MDC.put("userId", request.getId().toString())
        // MDC.put("CLIENT_type", request.getClientType())
        
        log.info("开始处理用户请求"); // 日志中会包含 MDC 信息
        
        // 业务逻辑...
        
        return "SUCCESS";
        // 方法结束后，MDC 会被自动清理
    }
}
```

## 注解详解

### @LogIndex 注解属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `name` | String | "" | 索引名称，为空时使用字段名 |
| `prefix` | String | "" | 索引前缀，最终键名为 prefix + name |
| `enabled` | boolean | true | 是否启用该索引 |
| `description` | String | "" | 索引描述，用于文档说明 |

### 使用示例

```java
public class RequestInfo {
    // 基本用法：使用字段名作为索引名
    @LogIndex
    private String requestId;
    
    // 自定义索引名称
    @LogIndex(name = "userId")
    private Long id;
    
    // 添加前缀
    @LogIndex(name = "type", prefix = "REQ_")
    private String requestType;
    
    // 带描述信息
    @LogIndex(name = "sessionId", description = "用户会话标识")
    private String sessionId;
    
    // 条件启用（可通过配置动态控制）
    @LogIndex(name = "debug", enabled = false)
    private String debugInfo;
}
```

## 配置详解

### 完整配置示例

```yaml
xg:
  log:
    enabled: true
    log-index:
      # 基本配置
      enabled: true                    # 是否启用 LogIndex 切面
      clear-after-method: true         # 方法执行后是否清理 MDC
      debug-enabled: false             # 是否启用调试日志
      
      # 长度限制
      max-key-length: 100              # 索引键最大长度
      max-value-length: 500            # 索引值最大长度
      
      # 高级配置
      enable-nested-scan: false        # 是否启用嵌套对象扫描
```

### 日志格式配置

配置 logback 以显示 MDC 信息：

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId:-}] [%X{userId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId:-}] [%X{userId:-}] [%X{CLIENT_type:-}] %logger{36} - %msg%n"
```

## 使用场景

### 1. 请求追踪

```java
@Data
public class HttpRequestInfo {
    @LogIndex(name = "traceId")
    private String traceId;
    
    @LogIndex(name = "userId")
    private String userId;
    
    @LogIndex(name = "ip", prefix = "CLIENT_")
    private String clientIp;
}

@RestController
public class UserController {
    
    @PostMapping("/users")
    public ResponseEntity<String> createUser(HttpRequestInfo requestInfo, @RequestBody UserDto userDto) {
        // 自动添加 traceId, userId, CLIENT_ip 到 MDC
        log.info("开始创建用户");
        
        // 业务逻辑...
        
        log.info("用户创建成功");
        return ResponseEntity.ok("SUCCESS");
    }
}
```

### 2. 业务操作日志

```java
@Data
public class BusinessOperation {
    @LogIndex(prefix = "OP_")
    private String operationId;
    
    @LogIndex(name = "type")
    private String operationType;
    
    @LogIndex(name = "module", prefix = "BIZ_")
    private String businessModule;
}

@Service
public class BusinessService {
    
    public void executeOperation(BusinessOperation operation) {
        // 自动添加 OP_operationId, type, BIZ_module 到 MDC
        log.info("开始执行业务操作");
        
        try {
            // 业务逻辑...
            log.info("业务操作执行成功");
        } catch (Exception e) {
            log.error("业务操作执行失败", e);
        }
    }
}
```

### 3. 错误排查

```java
@Data
public class ErrorContext {
    @LogIndex(name = "errorCode")
    private String errorCode;
    
    @LogIndex(name = "userId")
    private String userId;
    
    @LogIndex(name = "feature", prefix = "ERR_")
    private String featureName;
}

@Service
public class ErrorHandlingService {
    
    public void handleError(ErrorContext context, Exception exception) {
        // 自动添加错误上下文到 MDC
        log.error("系统错误", exception);
        
        // 记录详细错误信息
        log.error("错误详情 - 代码: {}, 功能: {}", 
            context.getErrorCode(), context.getFeatureName());
    }
}
```

## 最佳实践

### 1. 索引命名规范

- 使用有意义的索引名称
- 保持命名一致性
- 使用前缀区分不同类型的索引

```java
// 推荐
@LogIndex(name = "requestId")
@LogIndex(name = "userId")
@LogIndex(name = "type", prefix = "CLIENT_")

// 不推荐
@LogIndex(name = "id")  // 太模糊
@LogIndex(name = "t")   // 太简短
```

### 2. 性能考虑

- 避免在高频调用的方法中使用过多索引
- 合理设置长度限制
- 生产环境关闭调试日志

```yaml
# 生产环境配置
xg:
  log:
    log-index:
      debug-enabled: false
      enable-nested-scan: false
      max-key-length: 100
      max-value-length: 500
```

### 3. 安全考虑

- 避免在索引中包含敏感信息
- 对敏感字段使用 `enabled = false`

```java
@Data
public class UserInfo {
    @LogIndex
    private String userId;
    
    // 敏感信息不建议索引
    @LogIndex(enabled = false)
    private String password;
    
    private String email; // 不索引
}
```

### 4. 日志格式优化

```yaml
logging:
  pattern:
    # 使用 :- 提供默认值，避免空值显示
    console: "%d{HH:mm:ss.SSS} [%X{requestId:-NONE}] [%X{userId:-ANON}] %-5level %logger{36} - %msg%n"
```

## 高级功能

### 1. 嵌套对象扫描

```yaml
xg:
  log:
    log-index:
      enable-nested-scan: true
```

```java
@Data
public class ComplexRequest {
    @LogIndex
    private String requestId;
    
    private UserInfo userInfo; // 会递归扫描其中的 @LogIndex 字段
}

@Data
public class UserInfo {
    @LogIndex(name = "userId")
    private String id;
    
    @LogIndex(name = "role")
    private String userRole;
}
```

### 2. 条件启用

```java
@Data
public class DebugRequest {
    @LogIndex
    private String requestId;
    
    // 可通过配置或运行时条件控制
    @LogIndex(name = "debug", enabled = false)
    private String debugInfo;
}
```

### 3. 自定义切面顺序

```java
@Component
@Order(0) // 在 LogIndexAspect 之前执行
public class CustomAspect {
    // 自定义切面逻辑
}
```

## 故障排除

### 常见问题

1. **MDC 信息不显示**
   - 检查日志格式配置是否包含 `%X{key}`
   - 确认 LogIndex 切面是否启用
   - 检查字段是否正确添加了注解

2. **性能问题**
   - 关闭调试日志
   - 禁用嵌套扫描
   - 减少索引字段数量

3. **MDC 污染**
   - 确保 `clear-after-method` 配置为 `true`
   - 检查是否有其他代码手动操作 MDC

### 调试模式

启用调试模式查看详细信息：

```yaml
xg:
  log:
    log-index:
      debug-enabled: true

logging:
  level:
    tech.msop.core.log.aspect.LogIndexAspect: DEBUG
```

## 版本兼容性

| LogIndex 版本 | Spring Boot 版本 | JDK 版本 |
|---------------|------------------|----------|
| 1.0.x         | 2.3.x - 2.7.x   | JDK 8+   |
| 1.1.x         | 2.6.x - 3.0.x   | JDK 11+  |

## 技术支持

如果您在使用过程中遇到问题，可以通过以下方式获取帮助：

- 📧 邮箱：support@msop.tech
- 📖 文档：[在线文档地址]
- 🐛 问题反馈：[GitHub Issues]
- 💬 技术交流：[技术交流群]

## 更新日志

### v1.0.0 (2025-01-20)
- ✨ 初始版本发布
- 🎯 支持基本的 LogIndex 注解功能
- 🔧 支持自定义索引名称和前缀
- 🛡️ 自动 MDC 清理机制
- 📊 丰富的配置选项
- 🔍 调试模式支持

---

**行歌(xingge) 日志模块** - 让日志追踪更简单、更高效！