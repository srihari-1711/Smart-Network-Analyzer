package com.simats.smartnetworkanalyzerfrontend.data.repository;

public class Resource<T> {
    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    public final Status status;
    public final T data;
    public final String message;
    public final String errorCode;

    private Resource(Status status, T data, String message, String errorCode) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null, null);
    }

    public static <T> Resource<T> error(String message, String errorCode, T data) {
        return new Resource<>(Status.ERROR, data, message, errorCode);
    }

    public static <T> Resource<T> error(String message, String errorCode) {
        return new Resource<>(Status.ERROR, null, message, errorCode);
    }

    public static <T> Resource<T> error(String message) {
        return new Resource<>(Status.ERROR, null, message, null);
    }

    public Status getStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
}
