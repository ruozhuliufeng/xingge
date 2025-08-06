package tech.msop.core.mybatis.encrypt.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 类型
 */
@RequiredArgsConstructor
@Getter
public enum CryptoType {
    /**
     * 加密
     */
    ENCRYPT("encrypt"),
    /**
     * 解密
     */
    DECRYPT("decrypt"),

    ;
    /**
     * 对应加解密方法
     */
    private final String method;
}
