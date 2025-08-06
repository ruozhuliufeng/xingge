# xingge-starter-api-crypto

## 模块简介

`xingge-starter-api-crypto` 是行歌框架中的API加密解密启动器模块，提供了基于注解的API请求和响应数据加密解密功能，支持多种加密算法，帮助开发者轻松实现API数据的安全传输。

## 主要功能

### 多种加密算法支持
- **DES加密**：对称加密算法，适用于一般安全需求
- **AES加密**：高级加密标准，安全性更高的对称加密
- **RSA加密**：非对称加密算法，适用于高安全性要求场景

### 灵活的加密控制
- 支持方法级、类级和参数级的加密解密控制
- 支持请求数据解密和响应数据加密
- 支持全局配置和方法级别的密钥配置

## 核心注解

### @ApiEncrypt

用于标记需要加密响应数据的方法或类。

**属性说明：**
- `value`：加密类型（DES、AES、RSA）
- `secretKey`：私钥，可选，未设置时使用全局配置的私钥

### @ApiDecrypt

用于标记需要解密请求数据的方法、类或参数。

**属性说明：**
- `value`：解密类型（DES、AES、RSA）
- `secretKey`：私钥，可选，未设置时使用全局配置的私钥

### 专用注解

#### 加密专用注解
- `@ApiEncryptAes`：AES加密专用注解
- `@ApiEncryptDes`：DES加密专用注解
- `@ApiEncryptRsa`：RSA加密专用注解

#### 解密专用注解
- `@ApiDecryptAes`：AES解密专用注解
- `@ApiDecryptDes`：DES解密专用注解
- `@ApiDecryptRsa`：RSA解密专用注解

## 使用方式

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>tech.msop</groupId>
    <artifactId>xingge-starter-api-crypto</artifactId>
    <version>0.0.3</version>
</dependency>
```

### 2. 配置密钥

在 `application.yml` 中配置全局密钥：

```yaml
xingge:
  crypto:
    aes:
      secret-key: "your-aes-secret-key"
    des:
      secret-key: "your-des-secret-key"
    rsa:
      private-key: "your-rsa-private-key"
      public-key: "your-rsa-public-key"
```

### 3. 使用示例

#### 基本加密解密

```java
@RestController
public class UserController {
    
    /**
     * 解密请求数据，加密响应数据
     */
    @PostMapping("/api/user")
    @ApiDecrypt(CryptoType.AES)  // 解密请求数据
    @ApiEncrypt(CryptoType.AES)  // 加密响应数据
    public User createUser(@RequestBody User user) {
        // 处理业务逻辑
        return userService.save(user);
    }
    
    /**
     * 使用专用注解
     */
    @PostMapping("/api/user/secure")
    @ApiDecryptRsa  // RSA解密请求
    @ApiEncryptRsa  // RSA加密响应
    public User createSecureUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

#### 类级别加密

```java
/**
 * 整个Controller的所有方法都使用AES加密
 */
@RestController
@ApiEncrypt(CryptoType.AES)
@ApiDecrypt(CryptoType.AES)
public class SecureController {
    
    @PostMapping("/api/secure/data")
    public ResponseData processData(@RequestBody RequestData data) {
        return dataService.process(data);
    }
    
    /**
     * 方法级别覆盖类级别配置
     */
    @PostMapping("/api/secure/important")
    @ApiEncrypt(value = CryptoType.RSA, secretKey = "method-specific-key")
    public ImportantData processImportantData(@RequestBody ImportantData data) {
        return dataService.processImportant(data);
    }
}
```

#### 参数级别解密

```java
@RestController
public class MixedController {
    
    /**
     * 只对特定参数进行解密
     */
    @PostMapping("/api/mixed")
    public ResponseEntity<String> processData(
            @RequestParam String publicData,
            @ApiDecrypt(CryptoType.AES) @RequestBody SecretData secretData) {
        // publicData 不加密，secretData 需要解密
        return ResponseEntity.ok("处理完成");
    }
}
```

#### 不同加密算法示例

```java
@RestController
public class CryptoExampleController {
    
    /**
     * DES加密示例
     */
    @PostMapping("/api/des")
    @ApiDecryptDes
    @ApiEncryptDes
    public Data processDes(@RequestBody Data data) {
        return dataService.process(data);
    }
    
    /**
     * AES加密示例
     */
    @PostMapping("/api/aes")
    @ApiDecryptAes
    @ApiEncryptAes
    public Data processAes(@RequestBody Data data) {
        return dataService.process(data);
    }
    
    /**
     * RSA加密示例
     */
    @PostMapping("/api/rsa")
    @ApiDecryptRsa
    @ApiEncryptRsa
    public Data processRsa(@RequestBody Data data) {
        return dataService.process(data);
    }
}
```

## 应用场景

1. **敏感数据传输**：用户密码、身份证号、银行卡号等敏感信息的安全传输
2. **API安全防护**：防止API数据在传输过程中被窃取或篡改
3. **合规要求**：满足数据安全合规要求，如GDPR、等保等
4. **分级加密**：根据数据敏感程度选择不同的加密算法
5. **接口对接**：与第三方系统对接时的数据加密需求

## 加密算法选择建议

| 加密算法 | 安全级别 | 性能 | 适用场景 |
|---------|---------|------|----------|
| DES | 低 | 高 | 一般数据，性能要求高 |
| AES | 高 | 中 | 敏感数据，平衡安全与性能 |
| RSA | 最高 | 低 | 极敏感数据，安全要求最高 |

## 注意事项

1. **密钥管理**：确保密钥的安全存储，避免硬编码在代码中
2. **性能考虑**：加密解密会增加系统开销，需要根据业务需求合理选择
3. **兼容性**：确保客户端支持相应的加密解密算法
4. **错误处理**：合理处理加密解密过程中可能出现的异常
5. **密钥轮换**：定期更换密钥以提高安全性
6. **日志安全**：避免在日志中记录敏感的加密数据或密钥信息

## 版本信息

- 当前版本：0.0.3
- 最低JDK版本：1.8
- Spring Boot兼容版本：2.x及以上
- Spring Cloud兼容版本：2020.x及以上