package tech.msop.core.api.crypto.exception;

/**
 * 加密方式未找到或未定义异常
 *
 * @author ruozhuliufeng
 */
public class EncryptMethodNotFoundException extends RuntimeException {
    public EncryptMethodNotFoundException() {
        super("Encryption method is not defined. (加密方式未定义)");
    }

}
