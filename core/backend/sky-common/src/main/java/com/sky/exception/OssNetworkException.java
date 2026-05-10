package com.sky.exception;

public class OssNetworkException extends BaseException {

    public OssNetworkException(String message) {
        super(message);
    }

    public OssNetworkException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
