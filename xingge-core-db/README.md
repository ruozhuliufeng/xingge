# xingge-core-db 表结构自动维护模块

## 概述

`xingge-core-db` 是一个基于注解的数据库表结构自动维护模块，类似于 JPA，支持 MySQL、PostgreSQL、SQL Server、Oracle 等多种数据库。通过在实体类上添加注解，可以自动创建和维护数据库表结构，包括表、列、索引等。

## 特性

- 🚀 **多数据库支持**: 支持 MySQL、PostgreSQL、SQL Server、Oracle
- 📝 **注解驱动**: 通过注解定义表结构，简单易用
- 🔄 **自动维护**: 应用启动时自动检查并更新表结构
- ⚡ **异步处理**: 集成异步处理器，表结构维护操作异步执行，不阻塞应用启动
- 🛡️ **安全可靠**: 支持备份、验证、回滚等安全机制
- ⚙️ **高度可配置**: 丰富的配置选项，满足不同场景需求
- 📊 **详细日志**: 完整的操作日志，便于问题排查

## 快速开始

### 1. 添加依赖

在你的 `pom.xml` 中添加依赖：

```xml
<!-- 数据库自动维护模块 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-db</artifactId>
    <version>0.0.4</version>
</dependency>

<!-- 核心工具模块（包含异步处理器） -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-core-tool</artifactId>
    <version>0.0.4</version>
</dependency>
```

### 2. 配置数据源

在 `application.yml` 中配置数据源：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 启用表结构自动维护

在 `application.yml` 中添加配置：

```yaml
# 异步处理器配置（可选，使用默认配置）
xingge:
  async:
    core-pool-size: 10
    max-pool-size: 50
    queue-capacity: 200

# 数据库表结构自动维护配置
xg:
  db:
    table-maintenance:
      enabled: true
      auto-execute-on-startup: true
      entity-packages:
        - com.example.entity
      print-sql: true
      # 异步执行表结构维护（推荐）
      async-execution: true
```

### 4. 创建实体类

```java
@Table(name = "sys_user", comment = "系统用户表")
@Indexes({
    @Index(name = "idx_username", columnNames = {"username"}, unique = true),
    @Index(name = "idx_email", columnNames = {"email"}, unique = true)
})
public class UserEntity {
    
    @Id(strategy = IdStrategy.AUTO)
    @Column(name = "id", comment = "主键ID", nullable = false, autoIncrement = true)
    private Long id;
    
    @Column(name = "username", comment = "用户名", length = 50, nullable = false, unique = true)
    private String username;
    
    @Column(name = "email", comment = "邮箱", length = 100, nullable = false)
    private String email;
    
    @Column(name = "create_time", comment = "创建时间", nullable = false)
    private LocalDateTime createTime;
    
    // getter/setter 方法...
}
```

### 5. 启动应用

启动 Spring Boot 应用，模块会自动扫描实体类并创建/更新对应的数据库表结构。

## 注解说明

### @Table

用于标识实体类对应的数据库表：

```java
@Table(
    name = "sys_user",              // 表名
    comment = "系统用户表",          // 表注释
    schema = "public",              // 模式名（可选）
    engine = "InnoDB",              // 存储引擎（MySQL）
    charset = "utf8mb4",            // 字符集（MySQL）
    collate = "utf8mb4_unicode_ci", // 排序规则（MySQL）
    autoMaintenance = true          // 是否启用自动维护
)
```

### @Column

用于标识实体字段对应的数据库列：

```java
@Column(
    name = "username",              // 列名
    comment = "用户名",             // 列注释
    dataType = "VARCHAR",           // 数据类型（可选）
    length = 50,                    // 长度
    precision = 10,                 // 精度（数值类型）
    scale = 2,                      // 标度（数值类型）
    nullable = false,               // 是否允许为空
    unique = true,                  // 是否唯一
    defaultValue = "''",            // 默认值
    autoIncrement = false,          // 是否自增
    columnDefinition = "VARCHAR(50) NOT NULL" // 完整列定义（可选）
)
```

### @Id

用于标识主键字段：

```java
@Id(
    strategy = IdStrategy.AUTO,     // 主键生成策略
    sequenceName = "seq_user_id"    // 序列名（Oracle/PostgreSQL）
)
```

主键生成策略：
- `AUTO`: 自动选择合适的策略
- `IDENTITY`: 使用数据库自增
- `SEQUENCE`: 使用序列（Oracle/PostgreSQL）
- `ASSIGNED`: 手动赋值
- `UUID`: 使用UUID

### @Index

用于定义索引：

```java
@Index(
    name = "idx_username",          // 索引名
    columnNames = {"username"},     // 列名数组
    unique = true,                  // 是否唯一索引
    type = IndexType.BTREE,         // 索引类型
    comment = "用户名唯一索引"       // 索引注释
)
```

### @Indexes

用于在一个实体类上定义多个索引：

```java
@Indexes({
    @Index(name = "idx_username", columnNames = {"username"}, unique = true),
    @Index(name = "idx_email", columnNames = {"email"}, unique = true),
    @Index(name = "idx_create_time", columnNames = {"create_time"})
})
```

## 配置选项

### 基础配置

```yaml
xg:
  db:
    table-maintenance:
      # 是否启用表结构自动维护
      enabled: true
      
      # 是否在应用启动时自动执行
      auto-execute-on-startup: true
      
      # 需要扫描的实体类包路径
      entity-packages:
        - com.example.entity
        - com.example.model
      
      # 排除的实体类（完全限定名）
      exclude-entities:
        - com.example.entity.TempEntity
      
      # 只包含的实体类（如果设置，则只处理这些实体）
      include-entities:
        - com.example.entity.UserEntity
```

### 安全配置

```yaml
xg:
  db:
    table-maintenance:
      # 是否允许删除列（谨慎使用）
      allow-drop-column: false
      
      # 是否允许删除索引
      allow-drop-index: true
      
      # 是否允许修改列类型（谨慎使用）
      allow-modify-column-type: false
      
      # 是否在执行前进行验证
      validate-before-execution: true
      
      # 是否在测试环境下执行
      execute-in-test-environment: true
```

### 命名配置

```yaml
xg:
  db:
    table-maintenance:
      # 默认schema名称
      default-schema: public
      
      # 表名前缀
      table-prefix: t_
      
      # 表名后缀
      table-suffix: _tab
      
      # 是否使用驼峰命名转下划线命名
      camel-case-to-underscore: true
```

### 执行配置

```yaml
xg:
  db:
    table-maintenance:
      # 是否打印SQL语句到日志
      print-sql: true
      
      # 执行超时时间（秒）
      execution-timeout-seconds: 300
      
      # 最大重试次数
      max-retry-count: 3
      
      # 是否在出错时继续执行其他表的维护
      continue-on-error: true
```

### 备份配置

```yaml
xg:
  db:
    table-maintenance:
      backup:
        # 是否启用备份
        enabled: true
        
        # 备份目录
        backup-directory: ./db-backup
        
        # 是否在执行前备份表结构
        backup-before-execution: true
        
        # 备份文件保留天数
        retention-days: 30
```

## 支持的数据库

### MySQL

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### PostgreSQL

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/test
    driver-class-name: org.postgresql.Driver
```

### SQL Server

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=test
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
```

### Oracle

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:xe
    driver-class-name: oracle.jdbc.OracleDriver
```

## 使用示例

### 完整的实体类示例

参考 `tech.msop.core.db.example.UserEntity` 类，展示了如何使用各种注解定义完整的表结构。

### 手动执行表维护

```java
@Autowired
private TableMaintenanceService tableMaintenanceService;

@Autowired
private EntityScanner entityScanner;

public void manualMaintenance() throws Exception {
    // 扫描实体类
    List<Class<?>> entities = entityScanner.scanEntities();
    
    // 执行表维护
    tableMaintenanceService.maintainTables(entities);
}
```

### 维护单个表

```java
public void maintainSingleTable() throws Exception {
    tableMaintenanceService.maintainTable(UserEntity.class);
}
```

### 验证表结构

```java
public void validateTable() throws Exception {
    TableMaintenanceService.TableValidationResult result = 
        tableMaintenanceService.validateTable(UserEntity.class);
    
    if (!result.isValid()) {
        System.out.println("表结构验证失败: " + result.getErrorMessage());
    }
}
```

## 注意事项

1. **生产环境使用**: 在生产环境中使用时，建议先在测试环境充分测试
2. **备份数据**: 执行表结构变更前，建议备份重要数据
3. **权限要求**: 确保数据库用户有足够的权限执行DDL操作
4. **性能影响**: 大表的结构变更可能需要较长时间，建议在业务低峰期执行
5. **版本兼容**: 不同数据库版本的SQL语法可能有差异，请确保兼容性

## 常见问题

### Q: 如何禁用某个实体的自动维护？

A: 在 `@Table` 注解中设置 `autoMaintenance = false`：

```java
@Table(name = "temp_table", autoMaintenance = false)
public class TempEntity {
    // ...
}
```

### Q: 如何自定义列的数据类型？

A: 使用 `@Column` 注解的 `columnDefinition` 属性：

```java
@Column(name = "data", columnDefinition = "JSON")
private String data;
```

### Q: 如何处理现有表的兼容性？

A: 模块会自动检测现有表结构，只添加缺失的列和索引，不会删除现有的列（除非明确配置允许）。

### Q: 如何在不同环境使用不同配置？

A: 使用 Spring Profile 功能：

```yaml
# application-dev.yml
xg:
  db:
    table-maintenance:
      enabled: true
      print-sql: true

# application-prod.yml
xg:
  db:
    table-maintenance:
      enabled: false
```

## 更新日志

### v0.0.4
- 初始版本发布
- 支持 MySQL、PostgreSQL、SQL Server、Oracle
- 基础的表结构自动维护功能
- 完整的注解体系
- 丰富的配置选项

## 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。

## 许可证

本项目采用 MIT 许可证。