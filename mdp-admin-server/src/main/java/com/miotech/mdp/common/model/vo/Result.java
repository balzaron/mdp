package com.miotech.mdp.common.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.mdp.common.constant.ErrorCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: shanyue.gao
 * @date: 2019/12/23 6:00 PM
 */
@Data
@NoArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 返回处理消息
     */
    @JsonProperty("message")
    private String message = "";

    /**
     * 返回代码
     */
    @JsonProperty("code")
    private Integer code = 0;

    /**
     * 返回数据对象 data
     */
    @JsonProperty("result")
    private T result;

    public static <T> Result<T> success() {
        return new Result<>();
    }

    public static <T> Result<T> success(String message) {
        return success(message, null);
    }

    public static <T> Result<T> success(T resultObj) {
        return success("", resultObj);
    }

    public static <T> Result<T> success(String message,
                                        T resultObj) {
        Result<T> result = new Result<>();
        result.setMessage(message);
        result.setResult(resultObj);
        return result;
    }

    public static <T> Result<T> error() {
        return error(ErrorCode.FAILED.getCode(), ErrorCode.FAILED.getMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> error(String msg) {
        return error(ErrorCode.FAILED.getCode(), msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

}
