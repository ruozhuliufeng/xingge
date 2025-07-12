# xingge-starter-mybatis MyBatis增强

## 概述

`xingge-starter-mybatis` 是XingGe框架的MyBatis增强模块，提供自动化配置、通用CRUD、分页查询、多数据源、审计日志等功能，让MyBatis使用更加便捷和强大。

## 🎯 主要功能

- **自动配置**：零配置启用MyBatis增强功能
- **通用CRUD**：提供通用的增删改查操作
- **分页查询**：集成分页插件，支持多种数据库
- **多数据源**：支持动态数据源切换
- **审计日志**：自动记录数据变更历史
- **字段填充**：自动填充创建时间、更新时间等字段
- **逻辑删除**：支持逻辑删除功能
- **性能监控**：SQL执行性能监控和优化建议
- **代码生成**：自动生成Entity、Mapper、Service代码

## 📦 核心组件

### BaseMapper
- **通用接口**：提供常用的CRUD方法
- **类型安全**：泛型支持，编译时类型检查
- **扩展友好**：支持自定义方法扩展

### 分页插件
- **多数据库支持**：MySQL、PostgreSQL、Oracle等
- **性能优化**：智能分页，避免count查询
- **使用简单**：注解或参数方式启用分页

### 多数据源
- **动态切换**：运行时动态切换数据源
- **事务支持**：支持分布式事务
- **负载均衡**：支持读写分离和负载均衡

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-mybatis</artifactId>
</dependency>
```

### 2. 基础配置

```yaml
xingge:
  mybatis:
    # 启用MyBatis增强
    enabled: true
    
    # 通用CRUD配置
    base-mapper:
      enabled: true
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
    
    # 分页配置
    pagination:
      enabled: true
      default-page-size: 20
      max-page-size: 1000
      count-sql-parser: true
    
    # 审计配置
    audit:
      enabled: true
      create-time-field: createTime
      update-time-field: updateTime
      create-user-field: createUser
      update-user-field: updateUser
    
    # 性能监控
    performance:
      enabled: true
      slow-sql-threshold: 1000
      log-slow-sql: true

# MyBatis原生配置
mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.example.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
```

### 3. 实体类定义

```java
@TableName("sys_user")
public class User extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("phone")
    private String phone;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    // getter/setter...
}

@Data
public abstract class BaseEntity {
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
```

### 4. Mapper接口

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);
    
    /**
     * 分页查询用户
     */
    @Select("SELECT * FROM sys_user WHERE deleted = 0")
    IPage<User> selectUserPage(IPage<User> page);
    
    /**
     * 自定义复杂查询
     */
    List<User> selectUsersByCondition(@Param("condition") UserQueryCondition condition);
}
```

### 5. Service层使用

```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    /**
     * 分页查询用户
     */
    public IPage<User> getUserPage(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("username", keyword)
                   .or()
                   .like("email", keyword);
        }
        
        return this.page(page, wrapper);
    }
    
    /**
     * 创建用户
     */
    @Transactional
    public User createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        // 自动填充创建时间和创建用户
        this.save(user);
        
        return user;
    }
    
    /**
     * 逻辑删除用户
     */
    public boolean deleteUser(Long userId) {
        // 逻辑删除，实际是更新deleted字段
        return this.removeById(userId);
    }
}
```

## ⚙️ 详细配置

### 多数据源配置

```yaml
spring:
  datasource:
    # 主数据源
    primary:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/primary_db
      username: root
      password: password
    
    # 从数据源
    secondary:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/secondary_db
      username: root
      password: password

xingge:
  mybatis:
    # 多数据源配置
    multi-datasource:
      enabled: true
      primary: primary
      datasources:
        primary:
          type: com.zaxxer.hikari.HikariDataSource
          hikari:
            maximum-pool-size: 20
            minimum-idle: 5
        secondary:
          type: com.zaxxer.hikari.HikariDataSource
          hikari:
            maximum-pool-size: 10
            minimum-idle: 2
```

### 分页插件配置

```yaml
xingge:
  mybatis:
    pagination:
      enabled: true
      
      # 分页参数
      default-page-size: 20
      max-page-size: 1000
      
      # 优化配置
      count-sql-parser: true
      optimize-count-sql: true
      
      # 数据库方言
      db-type: mysql
      
      # 溢出处理
      overflow: false
```

### 代码生成配置

```yaml
xingge:
  mybatis:
    generator:
      enabled: true
      
      # 数据库配置
      datasource:
        url: jdbc:mysql://localhost:3306/test
        username: root
        password: password
      
      # 生成配置
      global:
        author: "XingGe Generator"
        output-dir: "src/main/java"
        open-dir: false
        swagger: true
      
      # 包配置
      package:
        parent: "com.example"
        entity: "entity"
        mapper: "mapper"
        service: "service"
        controller: "controller"
      
      # 策略配置
      strategy:
        # 表配置
        table:
          naming: underline_to_camel
          column-naming: underline_to_camel
          remove-table-prefix: "t_,sys_"
        
        # 实体配置
        entity:
          lombok: true
          chain-model: true
          table-field-annotation: true
          logic-delete-field: "deleted"
```

## 🔧 高级功能

### 1. 自定义字段填充

```java
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 创建用户
        Long currentUserId = getCurrentUserId();
        this.strictInsertFill(metaObject, "createUser", Long.class, currentUserId);
        
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新用户
        this.strictInsertFill(metaObject, "updateUser", Long.class, currentUserId);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新用户
        Long currentUserId = getCurrentUserId();
        this.strictUpdateFill(metaObject, "updateUser", Long.class, currentUserId);
    }
    
    private Long getCurrentUserId() {
        // 从安全上下文获取当前用户ID
        return SecurityContextHolder.getCurrentUserId();
    }
}
```

### 2. 动态数据源切换

```java
@Service
public class MultiDataSourceService {
    
    @DS("primary")
    public List<User> getPrimaryUsers() {
        return userMapper.selectList(null);
    }
    
    @DS("secondary")
    public List<Order> getSecondaryOrders() {
        return orderMapper.selectList(null);
    }
    
    @Transactional
    @DS("primary")
    public void createUserWithTransaction(User user) {
        userMapper.insert(user);
        // 事务内的操作都使用primary数据源
    }
}

// 编程式数据源切换
@Service
public class ProgrammaticDataSourceService {
    
    public void switchDataSource() {
        try {
            DynamicDataSourceContextHolder.push("secondary");
            // 使用secondary数据源的操作
            orderService.createOrder(order);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
```

### 3. 审计日志

```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    private Long id;
    
    private String tableName;
    private String operation;
    private String primaryKey;
    private String oldValues;
    private String newValues;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime operateTime;
    private String clientIp;
    
    // getter/setter...
}

@Component
public class AuditLogInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 记录操作前的数据
        Object oldData = getOldData(mappedStatement, parameter);
        
        // 执行原始操作
        Object result = invocation.proceed();
        
        // 记录审计日志
        recordAuditLog(mappedStatement, parameter, oldData, result);
        
        return result;
    }
    
    private void recordAuditLog(MappedStatement ms, Object param, Object oldData, Object result) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTableName(getTableName(ms));
        auditLog.setOperation(getOperation(ms));
        auditLog.setOldValues(JsonUtils.toJson(oldData));
        auditLog.setNewValues(JsonUtils.toJson(param));
        auditLog.setOperatorId(getCurrentUserId());
        auditLog.setOperateTime(LocalDateTime.now());
        
        auditLogService.save(auditLog);
    }
}
```

### 4. SQL性能监控

```java
@Component
public class SqlPerformanceInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = invocation.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录SQL执行时间
            recordSqlPerformance(invocation, executionTime);
            
            return result;
        } catch (Exception e) {
            // 记录SQL执行异常
            recordSqlError(invocation, e);
            throw e;
        }
    }
    
    private void recordSqlPerformance(Invocation invocation, long executionTime) {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        
        if (executionTime > slowSqlThreshold) {
            log.warn("慢SQL检测: {} 执行时间: {}ms", ms.getId(), executionTime);
            
            // 发送慢SQL告警
            alertService.sendSlowSqlAlert(ms.getId(), executionTime);
        }
        
        // 记录性能指标
        meterRegistry.timer("sql.execution.time", "mapper", ms.getId())
            .record(executionTime, TimeUnit.MILLISECONDS);
    }
}
```

## 📊 监控和优化

### 1. SQL监控配置

```yaml
xingge:
  mybatis:
    monitoring:
      enabled: true
      
      # SQL执行监控
      sql-monitor:
        enabled: true
        slow-sql-threshold: 1000
        log-slow-sql: true
        alert-slow-sql: true
      
      # 连接池监控
      connection-pool:
        enabled: true
        alert-threshold: 0.8
      
      # 缓存监控
      cache-monitor:
        enabled: true
        hit-rate-threshold: 0.7
```

### 2. 性能优化建议

```java
@Service
public class SqlOptimizationService {
    
    /**
     * 批量插入优化
     */
    @Transactional
    public void batchInsertUsers(List<User> users) {
        // 使用批量插入，避免循环单条插入
        userService.saveBatch(users, 1000);
    }
    
    /**
     * 分页查询优化
     */
    public IPage<User> optimizedPageQuery(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        
        // 禁用count查询，提升性能
        page.setSearchCount(false);
        
        return userMapper.selectPage(page, null);
    }
    
    /**
     * 缓存优化
     */
    @Cacheable(value = "users", key = "#userId")
    public User getCachedUser(Long userId) {
        return userMapper.selectById(userId);
    }
}
```

## 📋 最佳实践

### 1. 实体类设计

```java
// 好的实践
@TableName("sys_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Length(max = 50, message = "用户名长度不能超过50")
    private String username;
    
    @TableField(exist = false)
    private String confirmPassword;
    
    @TableLogic
    private Integer deleted;
}

// 避免的实践
public class BadUser {
    private Long id; // 缺少注解
    private String userName; // 字段名不规范
    public String password; // 不应该public
    // 缺少getter/setter
}
```

### 2. Mapper接口设计

```java
// 好的实践
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户（包含逻辑删除判断）
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(@Param("username") String username);
    
    /**
     * 复杂查询使用XML
     */
    List<UserVO> selectUserVOByCondition(@Param("condition") UserQueryCondition condition);
}

// 避免的实践
public interface BadUserMapper {
    // 没有继承BaseMapper，失去通用方法
    
    @Select("SELECT * FROM sys_user WHERE username = '${username}'") // SQL注入风险
    User findByUsername(String username);
    
    List<User> selectAll(); // 没有分页，可能导致内存溢出
}
```

### 3. Service层设计

```java
// 好的实践
@Service
@Transactional(readOnly = true)
public class UserService extends ServiceImpl<UserMapper, User> {
    
    @Transactional
    public User createUser(UserCreateRequest request) {
        // 参数验证
        validateCreateRequest(request);
        
        // 业务逻辑
        User user = convertToEntity(request);
        this.save(user);
        
        // 发布事件
        applicationEventPublisher.publishEvent(new UserCreatedEvent(user));
        
        return user;
    }
    
    public IPage<UserVO> getUserPage(UserQueryRequest request) {
        Page<User> page = new Page<>(request.getPageNum(), request.getPageSize());
        QueryWrapper<User> wrapper = buildQueryWrapper(request);
        
        IPage<User> userPage = this.page(page, wrapper);
        
        // 转换为VO
        return userPage.convert(this::convertToVO);
    }
}
```

## 🤝 扩展开发

### 1. 自定义BaseMapper

```java
public interface CustomBaseMapper<T> extends BaseMapper<T> {
    
    /**
     * 批量插入或更新
     */
    int insertOrUpdateBatch(@Param("list") List<T> list);
    
    /**
     * 逻辑删除恢复
     */
    int restore(@Param("id") Serializable id);
    
    /**
     * 物理删除
     */
    int deletePhysically(@Param("id") Serializable id);
}
```

### 2. 自定义插件

```java
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class CustomMybatisPlugin implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 自定义拦截逻辑
        return invocation.proceed();
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 设置插件属性
    }
}
```

---

**xingge-starter-mybatis** - 增强MyBatis，简化开发！