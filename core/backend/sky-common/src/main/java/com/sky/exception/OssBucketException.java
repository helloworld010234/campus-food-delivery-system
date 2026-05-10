package com.sky.exception;

public class OssBucketException extends BaseException {

    public OssBucketException(String message) {
        super(message);
    }

    public OssBucketException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
