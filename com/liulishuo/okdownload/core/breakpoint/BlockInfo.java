package com.liulishuo.okdownload.core.breakpoint;

import java.util.concurrent.atomic.AtomicLong;

public class BlockInfo {
    private final long contentLength;
    private final AtomicLong currentOffset;
    private final long startOffset;

    public BlockInfo(long startOffset2, long contentLength2) {
        this(startOffset2, contentLength2, 0);
    }

    public BlockInfo(long startOffset2, long contentLength2, long currentOffset2) {
        if (startOffset2 < 0 || ((contentLength2 < 0 && contentLength2 != -1) || currentOffset2 < 0)) {
            throw new IllegalArgumentException();
        }
        this.startOffset = startOffset2;
        this.contentLength = contentLength2;
        this.currentOffset = new AtomicLong(currentOffset2);
    }

    public long getCurrentOffset() {
        return this.currentOffset.get();
    }

    public long getStartOffset() {
        return this.startOffset;
    }

    public long getRangeLeft() {
        return this.startOffset + this.currentOffset.get();
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public long getRangeRight() {
        return (this.startOffset + this.contentLength) - 1;
    }

    public void increaseCurrentOffset(long increaseLength) {
        this.currentOffset.addAndGet(increaseLength);
    }

    public void resetBlock() {
        this.currentOffset.set(0);
    }

    public BlockInfo copy() {
        return new BlockInfo(this.startOffset, this.contentLength, this.currentOffset.get());
    }

    public String toString() {
        return "[" + this.startOffset + ", " + getRangeRight() + ")-current:" + this.currentOffset;
    }
}
