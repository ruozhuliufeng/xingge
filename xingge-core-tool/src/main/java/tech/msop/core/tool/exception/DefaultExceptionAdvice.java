package tech.msop.core.tool.exception;

import tech.msop.core.tool.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;

/**
 * 通用异常处理
 *
 * @author ruozhuliufeng
 */
@Slf4j
@ResponseBody
public class DefaultExceptionAdvice {
    /**
     * IllegalArgumentException异常处理返回json，返回状态码：400
     *
     * @param e 异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result badRequestException(IllegalArgumentException e) {
        return defHandler("参数解析失败！", e);
    }

    /**
     * AccessDeniedException异常处理，返回状态码：403
     *
     * @param e AccessDeniedException异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result badMethodExpressException(AccessDeniedException e) {
        return defHandler("没有权限请求当前方法", e);
    }

    /**
     * HttpRequestMethodNotSupportedException异常处理，返回状态码：405
     *
     * @param e HttpRequestMethodNotSupportedException异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return defHandler("不支持当前请求方法", e);
    }

    /**
     * HttpMediaTypeNotSupportedException异常处理，返回状态码：415
     *
     * @param e HttpMediaTypeNotSupportedException异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return defHandler("不支持当前媒体类型", e);
    }

    /**
     * SQLException sql异常处理，返回状态码：500
     *
     * @param e SQLException异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SQLException.class)
    public Result handleSqlException(SQLException e) {
        return defHandler("服务运行SQLException异常", e);
    }

    /**
     * BusinessException 业务异常处理，返回状态码：500
     *
     * @param e BusinessException业务异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(BusinessException.class)
    public Result handleException(BusinessException e) {
        return defHandler("业务异常", e);
    }

    /**
     * IdempotencyException 幂等性异常处理，返回状态码：200
     *
     * @param e IdempotencyException异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(IdempotencyException.class)
    public Result handleException(IdempotencyException e) {
        return defHandler("业务异常", e);
    }

    /**
     * 所有异常统一处理，返回状态码：500
     *
     * @param e Exception 异常
     * @return Result
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        return Result.failed(e.getMessage());
    }

    private Result defHandler(String msg, Exception e) {
        log.error(msg, e);
        return Result.failed(msg);
    }
}
