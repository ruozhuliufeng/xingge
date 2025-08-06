package tech.msop.core.tool.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 分布式锁异常
 * @author ruozhuliufeng
 */
@AllArgsConstructor
@Getter
@Setter
public class LockException extends RuntimeException{

    private static final long serialVersionUID = 6610083281801529147L;
    private String code;
    private String message;

    public static void error(String code,String message){
        throw new LockException(code,message);
    }
    public LockException(String message){
        super(message);
    }
}
