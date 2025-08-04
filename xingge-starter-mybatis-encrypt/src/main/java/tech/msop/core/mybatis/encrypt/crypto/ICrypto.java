package tech.msop.core.mybatis.encrypt.crypto;

import tech.msop.core.mybatis.encrypt.enums.Algorithm;

/**
 * 加解密处理
 *
 * @author ruozhuliufeng
 */
public interface ICrypto {

    /**
     * 加密
     *
     * @param algorithm 解密算法
     * @param value     加密前的值
     * @param key       密钥
     * @return 加密后的值
     * @throws Exception 异常
     */
    String encrypt(Algorithm algorithm, String value, String key) throws Exception;

    /**
     * 解密
     *
     * @param algorithm 解密算法
     * @param value     解密前的值
     * @param key       密钥
     * @return 解密后的值
     * @throws Exception 异常
     */
    String decrypt(Algorithm algorithm, String value, String key) throws Exception;
}
