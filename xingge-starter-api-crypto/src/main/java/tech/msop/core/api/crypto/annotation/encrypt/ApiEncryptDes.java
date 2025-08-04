package tech.msop.core.api.crypto.annotation.encrypt;

import org.springframework.core.annotation.AliasFor;
import tech.msop.core.api.crypto.enums.CryptoType;

import java.lang.annotation.*;

/**
 * DES 加密
 *
 * @author ruozhuliufeng
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ApiEncrypt(CryptoType.DES)
public @interface ApiEncryptDes {
    /**
     * Alias for {@link ApiEncrypt#secretKey()}
     *
     * @return 私钥
     */
    @AliasFor(annotation = ApiEncrypt.class)
    String secretKey() default "";
}
