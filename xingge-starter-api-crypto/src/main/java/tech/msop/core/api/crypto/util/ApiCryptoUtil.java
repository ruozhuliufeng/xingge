package tech.msop.core.api.crypto.util;

import org.springframework.core.MethodParameter;
import tech.msop.core.api.crypto.annotation.decrypt.ApiDecrypt;
import tech.msop.core.api.crypto.annotation.encrypt.ApiEncrypt;
import tech.msop.core.api.crypto.bean.CryptoInfoBean;
import tech.msop.core.api.crypto.config.ApiCryptoProperties;
import tech.msop.core.api.crypto.enums.CryptoType;
import tech.msop.core.api.crypto.exception.EncryptBodyFailException;
import tech.msop.core.api.crypto.exception.EncryptMethodNotFoundException;
import tech.msop.core.api.crypto.exception.KeyNotConfiguredException;
import tech.msop.core.tool.utils.*;

import java.util.Objects;

/**
 * 辅助监测工具类
 *
 * @author ruozhuliufeng
 */
public class ApiCryptoUtil {

    /**
     * 获取方法控制器上的加密注解信息
     *
     * @param methodParameter 控制器方法
     * @return 加密注解信息
     */
    public static CryptoInfoBean getEncryptInfo(MethodParameter methodParameter) {
        ApiEncrypt encryptBody = ClassUtil.getAnnotation(methodParameter.getMethod(), ApiEncrypt.class);
        if (encryptBody == null) {
            return null;
        }
        return new CryptoInfoBean(encryptBody.value(), encryptBody.secretKey());
    }

    /**
     * 获取方法控制器上的解密注解信息
     *
     * @param methodParameter 控制器方法
     * @return 加密注解信息
     */
    public static CryptoInfoBean getDecryptInfo(MethodParameter methodParameter) {
        ApiDecrypt decryptBody = ClassUtil.getAnnotation(methodParameter.getMethod(), ApiDecrypt.class);
        if (decryptBody == null) {
            return null;
        }
        return new CryptoInfoBean(decryptBody.value(), decryptBody.secretKey());
    }

    /**
     * 选择加密方式并进行加密
     *
     * @param jsonData json 数据
     * @param infoBean 加密信息
     * @return 加密结果
     */
    public static String encryptData(ApiCryptoProperties properties,byte[] jsonData,CryptoInfoBean infoBean) {
        CryptoType type = infoBean.getType();
        if (type == null) {
            throw new EncryptMethodNotFoundException();
        }
        String secretKey = infoBean.getSecretKey();
        if(type == CryptoType.DES){
            secretKey = ApiCryptoUtil.checkSecretKey(properties.getDesKey(),secretKey,"DES");
            return DesUtil.encryptToBase64(jsonData,secretKey);
        }
        if (type == CryptoType.AES) {
            secretKey = ApiCryptoUtil.checkSecretKey(properties.getAesKey(),secretKey,"AES");
            return AesUtil.encryptToBase64(jsonData,secretKey);
        }
        if (type == CryptoType.RSA) {
            String privateKey = Objects.requireNonNull(properties.getRsaPrivateKey());
            return RsaUtil.encryptByPrivateKeyToBase64(privateKey,jsonData);
        }
        throw new EncryptBodyFailException();
    }

    /**
     * 选择解密方式并进行解密
     *
     * @param jsonData json 数据
     * @param infoBean 加密信息
     * @return 解密结果
     */
    public static byte[] decryptData(ApiCryptoProperties properties,byte[] jsonData,CryptoInfoBean infoBean) {
        CryptoType type = infoBean.getType();
        if (type == null) {
            throw new EncryptMethodNotFoundException();
        }
        String secretKey = infoBean.getSecretKey();
        if (type == CryptoType.DES) {
            secretKey = ApiCryptoUtil.checkSecretKey(properties.getDesKey(), secretKey, "DES");
            return DesUtil.decryptFormBase64(jsonData, secretKey);
        }
        if (type == CryptoType.AES) {
            secretKey = ApiCryptoUtil.checkSecretKey(properties.getAesKey(), secretKey, "AES");
            return AesUtil.decryptFormBase64(jsonData, secretKey);
        }
        if (type == CryptoType.RSA) {
            String publicKey = Objects.requireNonNull(properties.getRsaPrivateKey());
            return RsaUtil.decryptFromBase64(publicKey, jsonData);
        }
        throw new EncryptMethodNotFoundException();
    }

    /**
     * 校验加密密钥
     *
     * @param key 密钥
     * @param secretKey 密钥
     * @param keyName 加密类型
     * @return 密钥
     */
    public static String checkSecretKey(String key,String secretKey,String keyName) {
        if (StringUtil.isBlank(key) && StringUtil.isAllBlank(secretKey)) {
            throw new KeyNotConfiguredException(String.format("%s key is not configured! (%s 密钥未配置)",keyName, keyName));
        }
        return StringUtil.isBlank(secretKey) ? key : secretKey;
    }
}
