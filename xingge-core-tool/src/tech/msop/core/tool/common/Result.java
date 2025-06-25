package tech.msop.core.tool.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一返回工具类
 *
 * @author ruozhuliufeng
 * @date 2025-06-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    /**
     * 泛型数据
     */
    private T data;
    /**
     * 异常编码
     */
    private Integer code;
    /**
     * 异常信息
     */
    private String msg;
    /**
     * dataMap
     */
    private final Map<String, Object> dataMap = new HashMap<String, Object>();

    public Result(ICodeResult codeResult) {
        this(null, codeResult.getCode(), codeResult.getMessage());
    }

    public Result(ICodeResult codeResult, String message) {
        this(null, codeResult.getCode(), message);
    }

    public Result(ICodeResult codeResult, T data) {
        this(data, codeResult.getCode(), codeResult.getMessage());
    }

    public Result(ICodeResult codeResult, T data, String message) {
        this(data, codeResult.getCode(), message);
    }

    public static <T> Result<T> succeed(String msg) {
        return of(null, CodeEnum.SUCCESS.getCode(), msg);
    }

    public static <T> Result<T> succeed(T model, String msg) {
        return of(model, CodeEnum.SUCCESS.getCode(), msg);
    }

    public static <T> Result<T> succeed(T model) {
        return of(model, CodeEnum.SUCCESS.getCode(), "");
    }

    public static <T> Result<T> succeed() {
        return succeed("操作成功");
    }

    public static <T> Result<T> succeed(Integer code, String msg) {
        return new Result<T>(null, code, msg);
    }

    public static <T> Result<T> succeed(ICodeResult codeResult) {
        return new Result<>(codeResult);
    }

    public static <T> Result<T> succeed(ICodeResult codeResult, String message) {
        return new Result<>(codeResult, message);
    }

    public static <T> Result<T> succeed(T data, ICodeResult codeResult) {
        return new Result<>(codeResult, data);
    }

    public static <T> Result<T> succeed(T data, ICodeResult codeResult, String message) {
        return new Result<>(codeResult, data, message);
    }

    public static <T> Result<T> of(T datas, Integer code, String msg) {
        return new Result<>(datas, code, msg);
    }

    public static <T> Result<T> failed(String msg) {
        return of(null, CodeEnum.FAILURE.getCode(), msg);
    }

    public static <T> Result<T> failed(T model, String msg) {
        return of(model, CodeEnum.FAILURE.getCode(), msg);
    }

    public static <T> Result<T> failed() {
        return failed("操作失败");
    }

    public static <T> Result<T> failed(Integer code, String msg) {
        return new Result<T>(null, code, msg);
    }

    public static <T> Result<T> failed(ICodeResult codeResult) {
        return new Result<>(codeResult);
    }

    public static <T> Result<T> failed(ICodeResult codeResult, String message) {
        return new Result<>(codeResult, message);
    }

    public static <T> Result<T> failed(T data, ICodeResult codeResult) {
        return new Result<>(codeResult, data);
    }

    public static <T> Result<T> failed(T data, ICodeResult codeResult, String message) {
        return new Result<>(codeResult, data, message);
    }


}
