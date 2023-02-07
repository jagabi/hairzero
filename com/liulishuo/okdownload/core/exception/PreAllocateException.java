package com.liulishuo.okdownload.core.exception;

import java.io.IOException;

public class PreAllocateException extends IOException {
    private final long freeSpace;
    private final long requireSpace;

    public PreAllocateException(long requireSpace2, long freeSpace2) {
        super("There is Free space less than Require space: " + freeSpace2 + " < " + requireSpace2);
        this.requireSpace = requireSpace2;
        this.freeSpace = freeSpace2;
    }

    public long getRequireSpace() {
        return this.requireSpace;
    }

    public long getFreeSpace() {
        return this.freeSpace;
    }
}
