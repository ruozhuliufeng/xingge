package tech.msop.core.api.crypto.exception;

/**
 * 解密数据失败异常
 *
 * @author ruozhuliufeng
 */
public class DecryptBodyFailException extends RuntimeException {

    public DecryptBodyFailException(String message) {
        super(message);
    }
}
