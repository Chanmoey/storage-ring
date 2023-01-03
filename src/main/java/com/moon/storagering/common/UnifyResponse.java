package com.moon.storagering.common;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
public class UnifyResponse<T> {

    private final static int SUCCESS_CODE = 0;
    private final static String SUCCESS_MESSAGE = "SUCCESS";

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径，只有请求错误，发送异常，才需要把路径返回
     */
    private String request;

    /**
     * 返回数据
     */
    private T data;

    public UnifyResponse(int code, String message, String request, T data) {
        this.code = code;
        this.message = message;
        this.request = request;
        this.data = data;
    }

    public static <T> UnifyResponse<T> ok(T data) {
        return new UnifyResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, data);
    }

    public static <T> UnifyResponse<T> ok() {
        return new UnifyResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, null, null);
    }

    public static <T> UnifyResponse<T> fail(int code, String message, String request) {
        return new UnifyResponse<>(code, message, request, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
