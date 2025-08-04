package tech.msop.core.mybatis.encrypt.enums;

import lombok.NoArgsConstructor;

/**
 * 加密方法枚举
 *
 * @author ruozhuliufeng
 */
@NoArgsConstructor
public enum Algorithm {
    /**
     * 不可逆加密 MD5
     * <p>
     * 对称加密： AES (速度快，可解密)
     */
    MD5,
    AES,
    ;
}
