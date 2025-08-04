# MyBatis 处理字段加密

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
