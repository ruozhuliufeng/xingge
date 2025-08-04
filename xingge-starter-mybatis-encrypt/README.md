# xingge-starter-mybatis-encrypt

## 模块简介

`xingge-starter-mybatis-encrypt` 是行歌框架中基于 MyBatis 实现的字段加密和脱敏模块。该模块提供了数据库字段的自动加密存储和查询时解密功能，同时支持敏感数据的脱敏处理，确保数据安全性和隐私保护。

## 核心功能

### 1. 字段加密 (@FieldEncrypt)
- **功能描述**：对数据库字段进行自动加密存储和解密查询
- **支持算法**：MD5（不可逆）、AES（对称加密，可解密）
- **自定义加密器**：支持自定义加密实现

### 2. 字段脱敏 (@FieldDesensitize)
- **功能描述**：对敏感数据进行脱敏处理，保护用户隐私
- **自定义脱敏器**：支持自定义脱敏规则
- **填充字符**：可配置脱敏填充字符

## 配置解密密钥
```yaml
ms:
  mybatis:
    crypto:
      key: xxxxxxxxxxxxxx  # AES 密钥，不填使用默认生成的UUID
```

## 字段加密

> 目前仅支持字段的AES加密与MD5加密，可自定义加密解密器

在类的字段上使用注解@FieldEncrypt

```java
import tech.msop.core.mybatis.encrypt.annotation.FieldEncrypt;
import tech.msop.core.mybatis.encrypt.crypto.DefaultCrypto;
import tech.msop.core.mybatis.encrypt.enums.Algorithm;

public class Demo {
    /**
     * key: 自定义加密解密的key,若不填写,取值配置文件中的key,若未获取,取值默认生成的UUID
     * algorithm: 加解密方法,当前仅支持AES与MD5,后续扩展,默认AES。请注意：MD5是不可逆转的，不支持解密
     * crypto: 加密解密器,可自定义类实现ICrypto接口,默认使用默认接口
     */
    @FieldEncrypt(key = "xxxxxxxxxx", algorithm = Algorithm.AES,crypto=DefaultCrypto.class)
    private String encryptField;
}

```

## 字段脱敏

> 自定义脱敏器与填充值

在类的字段上使用注解@FieldDesensitize

```java
import tech.msop.core.mybatis.encrypt.annotation.FieldDesensitize;
import tech.msop.core.mybatis.encrypt.desensitize.DefaultDesensitize;

public class Demo {
    /**
     * fillValue: 数据脱敏填充值,默认为*
     * desenitize: 数据脱敏器，可通过实现IDesensitize接口实现自定义的数据脱敏器,默认为默认实现的脱敏器
     */
    @FieldDesensitize(fillValue = "*", desensitize = DefaultDesensitize.class)
    private String desensitizeField;
}
```

## 支持的加密算法

| 算法 | 类型 | 特点 | 适用场景 |
|------|------|------|----------|
| MD5 | 不可逆加密 | 单向哈希，不可解密 | 密码存储、数据校验 |
| AES | 对称加密 | 可加密可解密，速度快 | 敏感信息存储 |

## 应用场景

1. **用户密码加密**：使用MD5或AES算法对用户密码进行加密存储
2. **敏感信息保护**：对身份证号、手机号、邮箱等敏感信息进行加密
3. **数据脱敏展示**：在日志、报表中对敏感数据进行脱敏处理
4. **合规性要求**：满足数据保护法规要求

## 注意事项

1. **密钥管理**：加密密钥应妥善保管，建议使用配置中心管理
2. **算法选择**：
   - MD5适用于不需要解密的场景（如密码）
   - AES适用于需要解密的场景（如敏感信息）
3. **性能考虑**：加密解密会增加一定的性能开销，建议在必要字段上使用
4. **数据迁移**：启用加密后，历史数据需要进行迁移处理
5. **脱敏规则**：根据业务需求制定合适的脱敏规则

## 版本信息

- **当前版本**：0.0.3
- **依赖框架**：MyBatis
- **JDK要求**：JDK 8+

---

*该模块是行歌框架的重要组成部分，为数据安全提供了可靠的技术保障。*
