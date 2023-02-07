package com.liulishuo.okdownload.core.exception;

import java.io.IOException;

public class InterruptException extends IOException {
    public static final InterruptException SIGNAL = new InterruptException() {
        public void printStackTrace() {
            throw new IllegalAccessError("Stack is ignored for signal");
        }
    };

    private InterruptException() {
        super("Interrupted");
    }
}
