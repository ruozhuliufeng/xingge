package tech.msop.core.mybatis.encrypt.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * AES 工具类
 *
 * @author ruozhuliufeng
 */
@Slf4j
public class AESUtil {


    private final static String ALGORITHM = "AES";

    /**
     * @param key     密钥
     * @param content 需要加密的字符串
     * @return 密文字节数组
     */
    private static byte[] encrypt(String key, String content) {
        byte[] rawKey = genKey(key.getBytes());
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encypted = cipher.doFinal(content.getBytes());
            return encypted;
        } catch (Exception e) {
            return null;
        }
    }

    public static String encryptBase64(String key, String content) {
        byte[] encrypt = encrypt(key, content);
        return Base64.getEncoder().encodeToString(encrypt);
    }

    public static String decryptBase64(String key, String content) {
        byte[] decodeContent = Base64.getDecoder().decode(content);
        return decrypt(key, decodeContent);
    }


    /**
     * @param encrypted 密文字节数组
     * @param key       密钥
     * @return 解密后的字符串
     */
    private static String decrypt(String key, byte[] encrypted) {
        byte[] rawKey = genKey(key.getBytes());
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawKey, ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted);
        } catch (Exception e) {
            log.debug("解密失败以为你返回原值");
            return "";
        }
    }

    /**
     * @param seed 种子数据
     * @return 密钥数据
     */
    private static byte[] genKey(byte[] seed) {
        byte[] rawKey = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed);
            // AES加密数据块分组长度必须为128比特，密钥长度可以是128比特、192比特、256比特中的任意一个
            kgen.init(128, secureRandom);
            SecretKey secretKey = kgen.generateKey();
            rawKey = secretKey.getEncoded();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return rawKey;
    }

}

