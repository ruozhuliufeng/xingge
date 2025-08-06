# xingge-bom 依赖管理

## 概述

`xingge-bom` (Bill of Materials) 是XingGe框架的依赖管理模块，提供统一的版本管理和依赖声明。

## 🎯 主要功能

- **版本统一管理**：统一管理所有XingGe模块的版本
- **依赖冲突解决**：避免不同模块间的版本冲突
- **简化依赖声明**：无需在每个模块中指定版本号
- **第三方库管理**：统一管理常用第三方库的版本

## 📦 包含的依赖

### XingGe核心模块
- `xingge-core-tool` - 核心工具包
- `xingge-core-db` - 数据库核心包
- `xingge-core-auto` - 自动配置包

### XingGe Starter模块
- `xingge-starter-request-interceptor` - 请求拦截器
- `xingge-starter-http` - HTTP客户端
- `xingge-starter-log` - 日志增强
- `xingge-starter-mybatis` - MyBatis增强

### 第三方依赖
- **Spring Boot** - 2.7.x
- **Spring Framework** - 5.3.x
- **MyBatis** - 3.5.x
- **Jackson** - 2.13.x
- **Lombok** - 1.18.x

## 🚀 使用方式

### 1. 在父项目中引入BOM

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

### 2. 在子模块中使用依赖

```xml
<dependencies>
    <!-- 无需指定版本，由BOM统一管理 -->
    <dependency>
        <groupId>tech.msop</groupId>
        <artifactId>xingge-core-tool</artifactId>
    </dependency>
    
    <dependency>
        <groupId>tech.msop</groupId>
        <artifactId>xingge-starter-request-interceptor</artifactId>
    </dependency>
</dependencies>
```

## ✨ 优势

### 版本一致性
- **统一版本**：确保所有XingGe模块使用相同版本
- **兼容性保证**：经过测试的版本组合，确保模块间兼容
- **升级简化**：只需更新BOM版本即可升级所有模块

### 依赖管理
- **冲突解决**：自动解决传递依赖冲突
- **版本锁定**：防止意外的版本升级
- **清晰依赖**：明确的依赖关系和版本信息

### 开发效率
- **简化配置**：减少重复的版本声明
- **快速集成**：新项目可快速集成XingGe框架
- **维护便利**：集中管理所有依赖版本

## 📋 版本兼容性

| XingGe版本 | Spring Boot版本 | JDK版本 | 说明 |
|-----------|----------------|---------|------|
| 0.0.4     | 2.7.x          | 8+      | 当前版本 |
| 0.0.1     | 2.6.x          | 8+      | 初始版本 |

## 🔧 自定义版本

如果需要使用特定版本的依赖，可以在项目中显式声明：

```xml
<dependencies>
    <dependency>
        <groupId>tech.msop</groupId>
        <artifactId>xingge-core-tool</artifactId>
        <version>0.0.4-SNAPSHOT</version> <!-- 覆盖BOM中的版本 -->
    </dependency>
</dependencies>
```

## 📚 最佳实践

### 1. 项目结构建议

```
my-project/
├── pom.xml (引入xingge-bom)
├── my-service/
│   └── pom.xml (使用XingGe依赖，无需版本号)
└── my-web/
    └── pom.xml (使用XingGe依赖，无需版本号)
```

### 2. 版本升级策略

```xml
<!-- 升级XingGe框架只需更新BOM版本 -->
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-bom</artifactId>
    <version>0.0.4</version> <!-- 新版本 -->
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

### 3. 依赖排除

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-request-interceptor</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 🤝 贡献指南

如需添加新的依赖或更新版本：

1. 在 `pom.xml` 的 `<dependencyManagement>` 中添加依赖
2. 确保版本兼容性
3. 更新版本兼容性表格
4. 提交Pull Request

---

**xingge-bom** - 统一依赖，简化管理！