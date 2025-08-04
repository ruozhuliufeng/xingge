package tech.msop.core.mybatis.encrypt.annotation;

import tech.msop.core.mybatis.encrypt.desensitize.DefaultDesensitize;
import tech.msop.core.mybatis.encrypt.desensitize.IDesensitize;

import java.lang.annotation.*;

/**
 * 脱敏注解
 *
 * @author ruozhuliufeng
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface FieldDesensitize {
    /**
     * 填充值
     */
    String fillValue() default "*";

    /**
     * 脱敏器
     */
    Class<? extends IDesensitize> desensitize() default DefaultDesensitize.class;
}
