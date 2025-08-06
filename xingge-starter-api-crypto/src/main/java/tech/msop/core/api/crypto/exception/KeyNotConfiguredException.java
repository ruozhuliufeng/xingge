package tech.msop.core.api.crypto.exception;

/**
 * 未配置KEY运行时异常
 *
 * @author ruozhuliufeng
 */
public class KeyNotConfiguredException extends RuntimeException {

    public KeyNotConfiguredException(String message) {
        super(message);
    }
}
