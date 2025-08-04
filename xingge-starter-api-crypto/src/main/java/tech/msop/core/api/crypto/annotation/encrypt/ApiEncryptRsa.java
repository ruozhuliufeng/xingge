package tech.msop.core.api.crypto.annotation.encrypt;

import tech.msop.core.api.crypto.enums.CryptoType;

import java.lang.annotation.*;

/**
 * RSA 加密
 *
 * @author ruozhuliufeng
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ApiEncrypt(CryptoType.RSA)
public @interface ApiEncryptRsa {

}
