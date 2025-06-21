package tech.msop.core.tool.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一返回工具类
 *
 * @author ruozhuliufeng
 * @date 2021-08-04
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private T datas;
    private Integer code;
    private String msg;

    public Result(CodeEnum codeEnum) {
        this(null, codeEnum.getCode(), codeEnum.getMessage());
    }

    public Result(CodeEnum codeEnum, String message) {
        this(null, codeEnum.getCode(), message);
    }

    public Result(CodeEnum codeEnum, T data) {
        this(data, codeEnum.getCode(), codeEnum.getMessage());
    }

    public Result(CodeEnum codeEnum, T data, String message) {
        this(data, codeEnum.getCode(), message);
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

    public static <T> Result<T> succeed(CodeEnum codeEnum) {
        return new Result<>(codeEnum);
    }

    public static <T> Result<T> succeed(CodeEnum codeEnum, String message) {
        return new Result<>(codeEnum, message);
    }

    public static <T> Result<T> succeed(T data, CodeEnum codeEnum) {
        return new Result<>(codeEnum, data);
    }

    public static <T> Result<T> succeed(T data, CodeEnum codeEnum, String message) {
        return new Result<>(codeEnum, data, message);
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

    public static <T> Result<T> failed(CodeEnum codeEnum) {
        return new Result<>(codeEnum);
    }

    public static <T> Result<T> failed(CodeEnum codeEnum, String message) {
        return new Result<>(codeEnum, message);
    }

    public static <T> Result<T> failed(T data, CodeEnum codeEnum) {
        return new Result<>(codeEnum, data);
    }

    public static <T> Result<T> failed(T data, CodeEnum codeEnum, String message) {
        return new Result<>(codeEnum, data, message);
    }


}
