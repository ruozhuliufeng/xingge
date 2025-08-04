package tech.msop.core.mybatis.encrypt.annotation;

import tech.msop.core.mybatis.encrypt.crypto.DefaultCrypto;
import tech.msop.core.mybatis.encrypt.crypto.ICrypto;
import tech.msop.core.mybatis.encrypt.enums.Algorithm;

import java.lang.annotation.*;

/**
 * 字段加密
 *
 * @author ruozhuliufeng
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface FieldEncrypt {

    /**
     * 密钥
     */
    String key() default "";

    /**
     * 加密解密算法
     */
    Algorithm algorithm() default Algorithm.AES;

    /**
     * 加密解密器
     *
     * @return Class
     */
    Class<? extends ICrypto> crypto() default DefaultCrypto.class;
}
