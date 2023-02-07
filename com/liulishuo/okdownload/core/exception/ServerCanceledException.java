package com.liulishuo.okdownload.core.exception;

import java.io.IOException;

public class ServerCanceledException extends IOException {
    private final int responseCode;

    public ServerCanceledException(int responseCode2, long currentOffset) {
        super("Response code can't handled on internal " + responseCode2 + " with current offset " + currentOffset);
        this.responseCode = responseCode2;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}
