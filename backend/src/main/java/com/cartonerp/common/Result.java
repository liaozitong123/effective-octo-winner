package com.cartonerp.common;

public class Result<T> {
    private int code;
    private String message;
    private T data;
    private long total;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(T data, String message) {
        Result<T> r = ok(data);
        r.message = message;
        return r;
    }

    public static <T> Result<T> okWithTotal(T data, long total) {
        Result<T> r = ok(data);
        r.total = total;
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public long getTotal() { return total; }
}
