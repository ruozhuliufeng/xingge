package tech.msop.core.file.exception;

/**
 * 文件操作异常
 *
 * @author ruozhuliufeng
 */
public class FileOperationException extends RuntimeException {
    
    public FileOperationException(String message) {
        super(message);
    }
    
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FileOperationException(Throwable cause) {
        super(cause);
    }
}
