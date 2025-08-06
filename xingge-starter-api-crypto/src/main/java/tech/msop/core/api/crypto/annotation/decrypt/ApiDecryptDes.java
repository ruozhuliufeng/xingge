package tech.msop.core.api.crypto.annotation.decrypt;

import org.springframework.core.annotation.AliasFor;
import tech.msop.core.api.crypto.enums.CryptoType;

import java.lang.annotation.*;

/**
 * <p>DES解密含有{@link org.springframework.web.bind.annotation.RequestBody}注解的参数请求数据</p>
 *
 * @author ruozhuilufeng
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ApiDecrypt(CryptoType.DES)
public @interface ApiDecryptDes {

    /**
     * Alias for {@link ApiDecrypt#secretKey()}
     *
     * @return 私钥
     */
    @AliasFor(annotation = ApiDecrypt.class)
    String secretKey() default "";
}
