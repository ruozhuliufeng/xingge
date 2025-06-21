package tech.msop.core.tool.exception;

/**
 * 分布式锁异常
 * @author ruozhuliufeng
 */
public class LockException extends RuntimeException{

    private static final long serialVersionUID = 6610083281801529147L;

    public LockException(String message){
        super(message);
    }
}
