# xingge-starter-actuate

## 模块简介

`xingge-starter-actuate` 是行歌框架中的HTTP缓存启动器模块，提供了基于注解的HTTP缓存控制功能，帮助开发者轻松实现Web应用的缓存策略。

## 主要功能

### HTTP缓存控制
- 提供 `@HttpCacheAble` 注解，支持方法级和类级的缓存控制
- 基于HTTP Cache-Control头实现浏览器缓存策略
- 支持灵活的缓存时间配置

## 核心注解

### @HttpCacheAble

用于标记需要进行HTTP缓存的方法或类。

**属性说明：**
- `value` / `maxAge`：缓存时间，单位为秒，默认值为0
  - 当 `maxAge > 0` 时：浏览器直接从缓存中获取资源
  - 当 `maxAge <= 0` 时：向服务器发送HTTP请求确认资源是否有修改

## 使用方式

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-actuate</artifactId>
    <version>0.0.4</version>
</dependency>
```

### 2. 使用示例

#### 方法级缓存

```java
@RestController
public class ResourceController {
    
    /**
     * 缓存30分钟
     */
    @GetMapping("/api/data")
    @HttpCacheAble(maxAge = 1800) // 30分钟 = 1800秒
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok("缓存数据");
    }
    
    /**
     * 不缓存，每次都向服务器确认
     */
    @GetMapping("/api/realtime")
    @HttpCacheAble(maxAge = 0)
    public ResponseEntity<String> getRealtimeData() {
        return ResponseEntity.ok("实时数据");
    }
}
```

#### 类级缓存

```java
/**
 * 整个Controller的所有方法都缓存1小时
 */
@RestController
@HttpCacheAble(maxAge = 3600) // 1小时 = 3600秒
public class StaticResourceController {
    
    @GetMapping("/api/config")
    public ResponseEntity<String> getConfig() {
        return ResponseEntity.ok("配置信息");
    }
    
    @GetMapping("/api/version")
    public ResponseEntity<String> getVersion() {
        return ResponseEntity.ok("版本信息");
    }
}
```

## 应用场景

1. **静态资源缓存**：对于不经常变化的静态资源（如配置信息、版本号等）设置较长的缓存时间
2. **API响应缓存**：对于计算密集型或数据库查询较重的API接口设置适当的缓存时间
3. **实时数据控制**：对于需要实时性的数据接口，设置缓存时间为0，确保数据的实时性
4. **性能优化**：通过合理的缓存策略减少服务器负载，提升用户体验

## 注意事项

1. 缓存时间的设置需要根据业务需求合理配置
2. 对于敏感数据或频繁变化的数据，建议设置较短的缓存时间或不缓存
3. 该模块主要控制浏览器端缓存，服务器端缓存需要结合其他缓存方案
4. 缓存策略的生效依赖于客户端（浏览器）对HTTP Cache-Control头的支持

## 版本信息

- 当前版本：0.0.4
- 最低JDK版本：1.8
- Spring Boot兼容版本：2.x及以上