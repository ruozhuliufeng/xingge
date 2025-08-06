package tech.msop.core.api.crypto.exception;

/**
 * 加密数据失败异常
 *
 * @author ruozhuliufeng
 */
public class EncryptBodyFailException extends RuntimeException {
    public EncryptBodyFailException(){
        super("Encrypted data failed. (加密数据失败)");
    }

    public EncryptBodyFailException(String message) {
        super(message);
    }
}
