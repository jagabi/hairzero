package com.liulishuo.okdownload.core.exception;

import java.io.IOException;

public class RetryException extends IOException {
    public RetryException(String message) {
        super(message);
    }
}
