package com.sky.exception;

public class OssAuthException extends BaseException {

    public OssAuthException(String message) {
        super(message);
    }

    public OssAuthException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
