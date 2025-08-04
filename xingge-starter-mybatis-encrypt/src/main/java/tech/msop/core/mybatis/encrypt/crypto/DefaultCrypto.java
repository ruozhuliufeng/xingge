package tech.msop.core.mybatis.encrypt.crypto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.msop.core.mybatis.encrypt.enums.Algorithm;
import tech.msop.core.mybatis.encrypt.utils.AESUtil;
import tech.msop.core.mybatis.encrypt.utils.CryptoUtil;

@Slf4j
@AllArgsConstructor
public class DefaultCrypto implements ICrypto{
    private final static String KEY = "edcb87b4-68b1-466b-8f6d-256ef53e50f0";
    /**
     * 加密
     *
     * @param algorithm 解密算法
     * @param value     加密前的值
     * @param key       密钥
     * @return 加密后的值
     * @throws Exception 异常
     */
    @Override
    public String encrypt(Algorithm algorithm, String value, String key) throws Exception {
        String result;

        if (key == null || key.length() == 0) {
            key = KEY;
        }

        switch (algorithm) {
            case MD5:
                result = CryptoUtil.encryptBASE64(CryptoUtil.encryptMD5(value.getBytes()));
                break;
            case AES:
                result = AESUtil.encryptBase64(key, value);
                break;
            default:
                result = AESUtil.encryptBase64(key, value);
        }
        return result;
    }

    /**
     * 解密
     *
     * @param algorithm 解密算法
     * @param value     解密前的值
     * @param key       密钥
     * @return 解密后的值
     * @throws Exception 异常
     */
    @Override
    public String decrypt(Algorithm algorithm, String value, String key) throws Exception {
        String result;
        if (key == null || key.length() == 0) {
            key = KEY;
        }

        try {
            switch (algorithm) {
                case MD5:
                    log.debug("该算法不支持解密");
                    result = "";
                    break;
                case AES:
                    result = AESUtil.decryptBase64(key, value);
                    break;
                default:
                    result = AESUtil.decryptBase64(key, value);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            log.debug("值：‘" + value + "’不支持解密");
            result = "";
        }

        return result;
    }
}
