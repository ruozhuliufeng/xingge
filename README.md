# 行歌 (XingGe Framework)

一个基于Spring Boot的企业级开发框架，提供了丰富的功能模块和工具类，帮助开发者快速构建高质量的企业应用。

## 🚀 项目特性

- **模块化设计**：采用模块化架构，各功能模块独立，可按需引入
- **开箱即用**：提供丰富的Starter模块，零配置快速集成
- **高性能**：内置异步处理器，优化性能表现
- **多数据库支持**：支持MySQL、PostgreSQL、Oracle、SQL Server等主流数据库
- **完善的监控**：内置请求拦截器，支持多种存储方式
- **企业级特性**：提供完整的日志、缓存、工具类等企业开发必备功能

## 📦 模块介绍

### 核心模块

| 模块 | 描述 | 版本 |
|------|------|------|
| **xingge-core-tool** | 核心工具类模块，提供通用工具类和异步处理器 | 0.0.4 |
| **xingge-core-db** | 数据库核心模块，提供表结构自动维护功能 | 0.0.4 |
| **xingge-core-auto** | 自动配置模块，提供Spring Boot自动配置支持 | 0.0.4 |

### Starter模块

| 模块 | 描述 | 版本 |
|------|------|------|
| **xingge-starter-request-interceptor** | HTTP请求拦截器，支持多种存储方式 | 0.0.4 |
| **xingge-starter-http** | HTTP客户端增强模块 | 0.0.4 |
| **xingge-starter-log** | 日志增强模块 | 0.0.4 |
| **xingge-starter-mybatis** | MyBatis增强模块 | 0.0.4 |

### 其他模块

| 模块 | 描述 | 版本 |
|------|------|------|
| **xingge-bom** | 依赖管理模块，统一管理版本 | 0.0.4 |
| **xingge-project-test** | 测试项目模块 | 0.0.4 |

## 🔧 快速开始

### 1. 添加BOM依赖

在你的父项目`pom.xml`中添加BOM依赖：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>tech.msop</groupId>
            <artifactId>xingge-bom</artifactId>
            <version>0.0.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 引入所需模块

根据需要在项目中引入相应的模块：

```xml
<!-- 核心工具模块 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-tool</artifactId>
</dependency>

<!-- 数据库自动维护模块 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-db</artifactId>
</dependency>

<!-- 请求拦截器模块 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-request-interceptor</artifactId>
</dependency>
```

### 3. 配置文件

在`application.yml`中添加相应配置：

```yaml
# 异步处理器配置
xingge:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 200
    thread-name-prefix: "xingge-async-"

# 数据库表结构自动维护
xg:
  db:
    table-maintenance:
      enabled: true
      auto-execute-on-startup: true
      entity-packages:
        - com.example.entity

  # 请求拦截器
  request:
    enabled: true
    storage-type: LOG
    include-headers: true
    include-request-body: true
    include-response-body: true
```

## ⚡ 核心功能

### 异步处理器 (AsyncProcessor)

提供统一的异步处理能力，支持多种异步执行模式：

```java
@Autowired
private AsyncProcessor asyncProcessor;

// 异步执行无返回值任务
asyncProcessor.executeAsync(() -> {
    // 异步任务逻辑
}, "任务名称");

// 异步执行有返回值任务
CompletableFuture<String> future = asyncProcessor.executeAsyncWithResult(() -> {
    return "处理结果";
}, "任务名称");

// 异步执行带参数任务
asyncProcessor.executeAsyncWithParam(data -> {
    // 处理数据
}, userData, "数据处理任务");

// 异步执行带回调任务
asyncProcessor.executeAsyncWithCallback(
    () -> { /* 主任务 */ },
    () -> { /* 成功回调 */ },
    ex -> { /* 失败回调 */ },
    "回调任务"
);
```

### 表结构自动维护

通过注解自动创建和维护数据库表结构：

```java
@Table(name = "sys_user", comment = "系统用户表")
@Indexes({
    @Index(name = "idx_username", columnNames = {"username"}, unique = true)
})
public class UserEntity {
    
    @Id(strategy = IdStrategy.AUTO)
    @Column(name = "id", comment = "主键ID", nullable = false)
    private Long id;
    
    @Column(name = "username", comment = "用户名", length = 50, nullable = false)
    private String username;
    
    // getter/setter...
}
```

### HTTP请求拦截

自动拦截和记录HTTP请求，支持多种存储方式：

```java
@RestController
public class ApiController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/api/data")
    public String getData() {
        // 请求会被自动拦截和记录
        return restTemplate.getForObject("https://api.example.com/data", String.class);
    }
}
```

## 📚 文档链接

- [xingge-core-tool 核心工具模块](./xingge-core-tool/README.md)
- [xingge-core-db 数据库模块](./xingge-core-db/README.md)
- [xingge-starter-request-interceptor 请求拦截器](./xingge-starter-request-interceptor/README.md)

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目。

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 🔗 相关链接

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven Central](https://search.maven.org/)

---

**XingGe Framework** - 让企业级开发更简单！