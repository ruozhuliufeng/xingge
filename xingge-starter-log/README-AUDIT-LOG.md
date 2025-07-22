# 审计日志功能使用指南

## 概述

审计日志功能是 `xingge-starter-log` 模块的核心特性之一，通过 `@AuditLog` 注解提供了强大的业务操作审计能力。该功能支持多种处理器，可以将审计日志输出到控制台、发送到远程服务或保存到数据库。

## 核心特性

- **零侵入设计**：通过注解方式，无需修改业务逻辑
- **多处理器支持**：控制台、Feign接口、数据库三种处理方式
- **灵活配置**：支持细粒度的配置控制
- **异步处理**：支持同步和异步两种处理模式
- **丰富的上下文信息**：自动收集用户、请求、环境等信息
- **优先级控制**：支持处理器优先级排序
- **条件启用**：支持基于环境或条件的动态启用

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-log</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基本配置

在 `application.yml` 中添加基本配置：

```yaml
xg:
  log:
    enabled: true
    audit:
      enabled: true
      handlers:
        console:
          enabled: true
```

### 3. 使用注解

在需要审计的方法上添加 `@AuditLog` 注解：

```java
@Service
public class UserService {
    
    @AuditLog(
        operation = "USER_LOGIN",
        module = "用户管理",
        description = "用户登录系统"
    )
    public String login(String username, String password) {
        // 业务逻辑
        return "登录成功";
    }
}
```

### 4. 运行应用

启动应用后，当调用被注解的方法时，审计日志会自动记录并输出到配置的处理器中。

## 注解详解

### @AuditLog 注解属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `operation` | String | "" | 操作类型，建议使用大写英文和下划线 |
| `module` | String | "" | 所属模块名称 |
| `description` | String | "" | 操作描述 |
| `recordArgs` | boolean | false | 是否记录方法参数 |
| `recordResult` | boolean | false | 是否记录方法返回值 |
| `recordException` | boolean | true | 是否记录异常信息 |
| `enabled` | String | "true" | 是否启用，支持SpEL表达式 |
| `tags` | String[] | {} | 自定义标签 |
| `priority` | Priority | NORMAL | 优先级：LOW、NORMAL、HIGH、CRITICAL |
| `async` | boolean | true | 是否异步处理 |

### 使用示例

```java
// 基本使用
@AuditLog(
    operation = "USER_CREATE",
    module = "用户管理",
    description = "创建新用户"
)
public User createUser(User user) {
    // 业务逻辑
}

// 记录参数和返回值
@AuditLog(
    operation = "USER_UPDATE",
    module = "用户管理",
    description = "更新用户信息",
    recordArgs = true,
    recordResult = true,
    priority = AuditLogInfo.Priority.HIGH
)
public User updateUser(Long id, User user) {
    // 业务逻辑
}

// 条件启用（仅在生产环境记录）
@AuditLog(
    operation = "PASSWORD_RESET",
    module = "安全管理",
    description = "重置密码",
    enabled = "#{@environment.acceptsProfiles('prod')}",
    recordArgs = false,
    priority = AuditLogInfo.Priority.CRITICAL
)
public void resetPassword(Long userId, String newPassword) {
    // 业务逻辑
}
```

## 配置详解

### 完整配置示例

```yaml
xg:
  log:
    enabled: true
    audit:
      # 是否启用审计日志
      enabled: true
      # 是否启用调试日志
      debug-enabled: false
      # 默认是否异步处理
      default-async: true
      
      handlers:
        # 控制台处理器
        console:
          enabled: true
          format: detailed  # simple | detailed
          level: INFO       # DEBUG | INFO | WARN | ERROR
        
        # Feign接口处理器
        feign:
          enabled: false
          url: http://audit-service/api/audit/logs
          timeout: 5000
          retry-count: 3
          batch-size: 10
          headers:
            Authorization: Bearer your-token
            Content-Type: application/json
        
        # 数据库处理器
        database:
          enabled: false
          table-name: audit_log
          auto-create-table: true
          batch-size: 100
          retention-days: 90
```

### 处理器配置说明

#### 1. 控制台处理器

控制台处理器将审计日志输出到应用程序的控制台。

**配置项：**
- `enabled`：是否启用（默认：true）
- `format`：日志格式，`simple`（简单格式）或 `detailed`（详细格式）
- `level`：日志级别，支持 DEBUG、INFO、WARN、ERROR

#### 2. Feign接口处理器

Feign接口处理器通过HTTP接口将审计日志发送到远程服务。

**配置项：**
- `enabled`：是否启用（默认：false）
- `url`：审计日志接口URL
- `timeout`：请求超时时间（毫秒）
- `retry-count`：重试次数
- `batch-size`：批量发送大小
- `headers`：自定义请求头

**依赖要求：**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

#### 3. 数据库处理器

数据库处理器将审计日志保存到指定的数据库表中。

**配置项：**
- `enabled`：是否启用（默认：false）
- `table-name`：审计日志表名（默认：audit_log）
- `auto-create-table`：是否自动创建表（默认：true）
- `batch-size`：批量插入大小（默认：100）
- `retention-days`：数据保留天数（默认：90天，0表示不自动清理）

**依赖要求：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<!-- 数据库驱动，如MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

**数据库表结构：**
```sql
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id VARCHAR(64) NOT NULL COMMENT '日志ID',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    module VARCHAR(100) COMMENT '模块名称',
    description TEXT COMMENT '操作描述',
    method_name VARCHAR(200) COMMENT '方法名称',
    method_args TEXT COMMENT '方法参数',
    method_result TEXT COMMENT '方法返回值',
    exception_info TEXT COMMENT '异常信息',
    status VARCHAR(20) NOT NULL COMMENT '执行状态',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    user_id VARCHAR(100) COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    user_role VARCHAR(100) COMMENT '用户角色',
    client_ip VARCHAR(50) COMMENT '客户端IP',
    user_agent TEXT COMMENT '用户代理',
    request_url VARCHAR(500) COMMENT '请求URL',
    http_method VARCHAR(10) COMMENT 'HTTP方法',
    session_id VARCHAR(100) COMMENT '会话ID',
    trace_id VARCHAR(100) COMMENT '追踪ID',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    server_name VARCHAR(100) COMMENT '服务器名称',
    application_name VARCHAR(100) COMMENT '应用名称',
    environment VARCHAR(50) COMMENT '环境标识',
    priority VARCHAR(20) COMMENT '优先级',
    tags TEXT COMMENT '标签',
    extended_properties TEXT COMMENT '扩展属性',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_log_id (log_id),
    INDEX idx_operation (operation),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_create_time (create_time)
) COMMENT='审计日志表';
```

## 使用场景

### 1. 用户操作审计

记录用户的关键操作，如登录、注销、权限变更等：

```java
@AuditLog(
    operation = "USER_LOGIN",
    module = "认证管理",
    description = "用户登录",
    priority = AuditLogInfo.Priority.HIGH
)
public LoginResult login(String username, String password) {
    // 登录逻辑
}
```

### 2. 数据变更审计

记录重要数据的增删改操作：

```java
@AuditLog(
    operation = "DATA_UPDATE",
    module = "数据管理",
    description = "更新关键数据",
    recordArgs = true,
    recordResult = true,
    priority = AuditLogInfo.Priority.CRITICAL
)
public void updateCriticalData(Long id, Object data) {
    // 数据更新逻辑
}
```

### 3. 安全操作审计

记录安全相关的操作：

```java
@AuditLog(
    operation = "PERMISSION_GRANT",
    module = "权限管理",
    description = "授予权限",
    recordArgs = true,
    priority = AuditLogInfo.Priority.CRITICAL,
    tags = {"安全", "权限"}
)
public void grantPermission(Long userId, String permission) {
    // 权限授予逻辑
}
```

### 4. 业务流程审计

记录关键业务流程的执行：

```java
@AuditLog(
    operation = "ORDER_PROCESS",
    module = "订单管理",
    description = "处理订单",
    recordArgs = true,
    recordResult = true,
    async = false  // 同步处理确保一致性
)
public OrderResult processOrder(Order order) {
    // 订单处理逻辑
}
```

## 最佳实践

### 1. 命名规范

- **操作类型**：使用大写英文和下划线，如 `USER_LOGIN`、`DATA_UPDATE`
- **模块名称**：使用中文或英文，保持简洁明了
- **操作描述**：详细描述操作内容，便于后续查询和分析

### 2. 性能优化

- **异步处理**：对于非关键操作，建议使用异步处理提高性能
- **批量处理**：配置合适的批量大小，减少I/O操作
- **选择性记录**：根据业务需要选择是否记录参数和返回值

```java
// 高频操作使用异步处理
@AuditLog(
    operation = "FILE_UPLOAD",
    module = "文件管理",
    description = "上传文件",
    async = true,
    recordArgs = false  // 不记录文件内容
)
public String uploadFile(MultipartFile file) {
    // 文件上传逻辑
}
```

### 3. 安全考虑

- **敏感信息**：避免记录密码、密钥等敏感信息
- **数据脱敏**：对敏感数据进行脱敏处理
- **访问控制**：确保审计日志的访问权限控制

```java
// 敏感操作不记录参数
@AuditLog(
    operation = "PASSWORD_CHANGE",
    module = "安全管理",
    description = "修改密码",
    recordArgs = false,  // 不记录密码参数
    priority = AuditLogInfo.Priority.CRITICAL
)
public void changePassword(Long userId, String oldPassword, String newPassword) {
    // 密码修改逻辑
}
```

### 4. 监控和告警

- **关键操作监控**：对CRITICAL优先级的操作设置监控
- **异常告警**：对审计日志中的异常进行告警
- **统计分析**：定期分析审计日志，发现异常模式

## 高级功能

### 1. 自定义处理器

可以实现 `AuditLogHandler` 接口创建自定义处理器：

```java
@Component
public class CustomAuditLogHandler implements AuditLogHandler {
    
    @Override
    public void handle(AuditLogInfo logInfo) {
        // 自定义处理逻辑
    }
    
    @Override
    public String getName() {
        return "custom";
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}
```

### 2. 条件启用

支持使用SpEL表达式进行条件启用：

```java
// 仅在生产环境启用
@AuditLog(
    operation = "SENSITIVE_OPERATION",
    enabled = "#{@environment.acceptsProfiles('prod')}"
)
public void sensitiveOperation() {
    // 敏感操作
}

// 基于配置属性启用
@AuditLog(
    operation = "DEBUG_OPERATION",
    enabled = "#{@auditConfig.debugEnabled}"
)
public void debugOperation() {
    // 调试操作
}
```

### 3. 扩展属性

可以通过扩展属性添加自定义信息：

```java
@AuditLog(
    operation = "CUSTOM_OPERATION",
    module = "自定义模块",
    description = "自定义操作"
)
public void customOperation() {
    // 在方法中可以通过MDC添加扩展信息
    MDC.put("customField", "customValue");
    // 业务逻辑
}
```

## 故障排除

### 1. 审计日志未生效

**可能原因：**
- 配置未启用：检查 `xg.log.audit.enabled` 配置
- 注解位置错误：确保注解在Spring管理的Bean的public方法上
- 切面未生效：检查是否启用了AspectJ自动代理

**解决方案：**
```yaml
# 启用调试日志查看详细信息
logging:
  level:
    tech.msop.core.log: DEBUG
```

### 2. 处理器未执行

**可能原因：**
- 处理器未启用：检查对应处理器的enabled配置
- 依赖缺失：检查是否添加了必要的依赖
- 配置错误：检查处理器的具体配置项

### 3. 性能问题

**可能原因：**
- 同步处理：大量操作使用同步处理导致性能下降
- 记录过多信息：记录了大量参数或返回值
- 批量大小不当：批量处理配置不合理

**解决方案：**
- 启用异步处理
- 选择性记录信息
- 调整批量处理大小

## 版本兼容性

| 版本 | Spring Boot | Spring Cloud | JDK |
|------|-------------|--------------|-----|
| 1.0.0 | 2.7+ | 2021.0+ | 8+ |

## 技术支持

如果在使用过程中遇到问题，可以通过以下方式获取支持：

1. **查看日志**：启用DEBUG级别日志查看详细信息
2. **检查配置**：确认配置文件的正确性
3. **查看文档**：参考本文档和示例代码
4. **社区支持**：在项目仓库提交Issue

## 更新日志

### v1.0.0
- 初始版本发布
- 支持控制台、Feign接口、数据库三种处理器
- 支持同步和异步处理
- 支持条件启用和优先级控制
- 提供完整的配置选项和使用示例