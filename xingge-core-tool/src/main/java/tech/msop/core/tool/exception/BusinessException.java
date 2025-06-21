package tech.msop.core.tool.exception;

import tech.msop.core.tool.model.CodeEnum;
import lombok.Getter;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 6610083281801529147L;

    @Getter
    private final CodeEnum codeEnum;

    public BusinessException(String message) {
        super(message);
        this.codeEnum = CodeEnum.FAILURE;
    }

    public BusinessException(CodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.codeEnum = codeEnum;
    }

    public BusinessException(CodeEnum codeEnum,Throwable cause){
        super(cause);
        this.codeEnum = codeEnum;
    }


    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
