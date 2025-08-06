# xingge-core-tool 核心工具模块

## 概述

`xingge-core-tool` 是XingGe框架的核心工具模块，提供了丰富的工具类和异步处理能力，是框架的基础组件之一。

## 🚀 主要功能

- **异步处理器 (AsyncProcessor)**: 提供统一的异步任务处理能力
- **工具类集合**: 包含字符串、日期、集合等常用工具类
- **Spring工具**: Spring容器相关的工具类
- **函数式编程支持**: 提供函数式编程相关的工具

## 📦 快速开始

### 1. 添加依赖

在你的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-tool</artifactId>
    <version>0.0.4</version>
</dependency>
```

### 2. 启用异步处理

在Spring Boot应用中，异步处理器会自动配置。你也可以在配置文件中自定义参数：

```yaml
xingge:
  async:
    # 核心线程数
    core-pool-size: 10
    # 最大线程数
    max-pool-size: 50
    # 队列容量
    queue-capacity: 200
    # 线程名前缀
    thread-name-prefix: "xingge-async-"
    # 线程空闲时间（秒）
    keep-alive-seconds: 60
    # 是否等待任务完成后关闭
    wait-for-tasks-to-complete-on-shutdown: true
    # 等待关闭的超时时间（秒）
    await-termination-seconds: 60
```

## ⚡ 异步处理器 (AsyncProcessor)

### 功能特性

- **多种执行模式**: 支持无返回值、有返回值、带参数、带回调等多种异步执行模式
- **异常安全**: 自动捕获异常，确保不影响主业务流程
- **任务命名**: 支持为任务指定名称，便于日志追踪和调试
- **统一管理**: 所有异步任务通过统一的线程池执行，避免资源浪费
- **配置灵活**: 支持通过配置文件自定义线程池参数

### 使用示例

#### 1. 注入异步处理器

```java
@Service
public class UserService {
    
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    // 业务方法...
}
```

#### 2. 异步执行无返回值任务

```java
// 基本用法
asyncProcessor.executeAsync(() -> {
    // 异步执行的任务逻辑
    System.out.println("异步任务执行中...");
});

// 带任务名称
asyncProcessor.executeAsync(() -> {
    // 发送邮件
    emailService.sendEmail(user.getEmail(), "欢迎注册");
}, "发送欢迎邮件");
```

#### 3. 异步执行有返回值任务

```java
// 异步计算并获取结果
CompletableFuture<String> future = asyncProcessor.executeAsyncWithResult(() -> {
    // 执行耗时计算
    return performComplexCalculation();
}, "复杂计算任务");

// 获取结果
String result = future.get(); // 阻塞等待结果

// 或者异步处理结果
future.thenAccept(result -> {
    System.out.println("计算结果: " + result);
});
```

#### 4. 异步执行带参数任务

```java
// 异步处理用户数据
User userData = getUserData();
asyncProcessor.executeAsyncWithParam(user -> {
    // 处理用户数据
    processUserData(user);
    updateUserCache(user);
}, userData, "用户数据处理");
```

#### 5. 异步执行带回调任务

```java
// 异步任务带成功和失败回调
asyncProcessor.executeAsyncWithCallback(
    // 主任务
    () -> {
        // 执行文件上传
        uploadFileToCloud(file);
    },
    // 成功回调
    () -> {
        System.out.println("文件上传成功");
        notifyUser("上传完成");
    },
    // 失败回调
    ex -> {
        System.err.println("文件上传失败: " + ex.getMessage());
        notifyUser("上传失败，请重试");
    },
    "文件上传任务"
);
```

### 实际应用场景

#### 1. 日志异步存储

```java
@Service
public class LogService {
    
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    public void saveLog(LogInfo logInfo) {
        // 异步保存日志，不影响主业务
        asyncProcessor.executeAsync(() -> {
            logRepository.save(logInfo);
        }, "保存操作日志");
    }
}
```

#### 2. 缓存异步更新

```java
@Service
public class CacheService {
    
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    public void updateCache(String key, Object value) {
        // 异步更新缓存
        asyncProcessor.executeAsync(() -> {
            redisTemplate.opsForValue().set(key, value);
        }, "更新缓存-" + key);
    }
}
```

#### 3. 消息异步发送

```java
@Service
public class NotificationService {
    
    @Autowired
    private AsyncProcessor asyncProcessor;
    
    public void sendNotification(User user, String message) {
        // 异步发送多种类型的通知
        asyncProcessor.executeAsync(() -> {
            // 发送邮件
            emailService.send(user.getEmail(), message);
        }, "发送邮件通知");
        
        asyncProcessor.executeAsync(() -> {
            // 发送短信
            smsService.send(user.getPhone(), message);
        }, "发送短信通知");
        
        asyncProcessor.executeAsync(() -> {
            // 推送消息
            pushService.push(user.getId(), message);
        }, "推送消息通知");
    }
}
```

## 🛠️ 工具类

### 字符串工具 (StringUtil)

提供字符串处理相关的工具方法：

```java
// 判断字符串是否为空
boolean isEmpty = StringUtil.isEmpty(str);

// 判断字符串是否不为空
boolean isNotEmpty = StringUtil.isNotEmpty(str);

// 字符串格式化
String formatted = StringUtil.format("Hello {}", "World");
```

### 集合工具 (CollectionUtil)

提供集合处理相关的工具方法：

```java
// 判断集合是否为空
boolean isEmpty = CollectionUtil.isEmpty(list);

// 判断集合是否不为空
boolean isNotEmpty = CollectionUtil.isNotEmpty(list);

// 安全的集合操作
List<String> safeList = CollectionUtil.emptyIfNull(list);
```

### Spring工具 (SpringUtil)

提供Spring容器相关的工具方法：

```java
// 获取Bean
UserService userService = SpringUtil.getBean(UserService.class);

// 获取Bean（按名称）
Object bean = SpringUtil.getBean("userService");

// 获取应用上下文
ApplicationContext context = SpringUtil.getApplicationContext();
```

## 📋 配置参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xingge.async.core-pool-size` | int | 5 | 核心线程数 |
| `xingge.async.max-pool-size` | int | 20 | 最大线程数 |
| `xingge.async.queue-capacity` | int | 100 | 队列容量 |
| `xingge.async.thread-name-prefix` | String | "async-processor-" | 线程名前缀 |
| `xingge.async.keep-alive-seconds` | int | 60 | 线程空闲时间（秒） |
| `xingge.async.wait-for-tasks-to-complete-on-shutdown` | boolean | true | 是否等待任务完成后关闭 |
| `xingge.async.await-termination-seconds` | int | 60 | 等待关闭的超时时间（秒） |

## 🔧 高级配置

### 自定义线程池配置

如果需要更精细的控制，可以自定义线程池配置：

```java
@Configuration
public class AsyncConfig {
    
    @Bean(name = "customAsyncExecutor")
    public Executor customAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("custom-async-");
        executor.setKeepAliveSeconds(120);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

### 异常处理策略

异步处理器内置了异常处理机制，所有异常都会被捕获并记录到日志中，不会影响主业务流程。如果需要自定义异常处理，可以使用带回调的方法：

```java
asyncProcessor.executeAsyncWithCallback(
    () -> {
        // 可能抛出异常的任务
        riskyOperation();
    },
    () -> {
        // 成功回调
        handleSuccess();
    },
    ex -> {
        // 自定义异常处理
        if (ex instanceof SpecificException) {
            handleSpecificException((SpecificException) ex);
        } else {
            handleGenericException(ex);
        }
    },
    "风险操作任务"
);
```

## 📈 性能优势

1. **统一线程池管理**: 避免创建多个独立线程池，减少资源消耗
2. **异步执行**: 不阻塞主业务流程，提升响应速度
3. **异常隔离**: 异步任务异常不会影响主业务
4. **资源复用**: 线程池复用，减少线程创建销毁开销
5. **配置灵活**: 可根据业务需求调整线程池参数

## 🔍 监控和调试

异步处理器提供了详细的日志记录，便于监控和调试：

```
2024-01-01 10:30:15.123 DEBUG [async-processor-1] AsyncProcessor - 开始执行异步任务: 发送欢迎邮件
2024-01-01 10:30:15.456 DEBUG [async-processor-1] AsyncProcessor - 异步任务执行完成: 发送欢迎邮件
2024-01-01 10:30:15.789 ERROR [async-processor-2] AsyncProcessor - 异步任务执行失败: 文件上传任务, 错误信息: 网络连接超时
```

## 🤝 最佳实践

1. **合理命名任务**: 为异步任务指定有意义的名称，便于日志追踪
2. **避免长时间阻塞**: 异步任务应该是相对轻量的，避免长时间阻塞线程
3. **异常处理**: 对于重要的异步任务，建议使用带回调的方法处理异常
4. **资源清理**: 确保异步任务中使用的资源能够正确释放
5. **监控线程池**: 定期监控线程池的使用情况，根据需要调整参数

---

**xingge-core-tool** - 让异步处理更简单！