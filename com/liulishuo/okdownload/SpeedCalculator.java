package com.liulishuo.okdownload;

import android.os.SystemClock;
import com.liulishuo.okdownload.core.Util;

public class SpeedCalculator {
    long allIncreaseBytes;
    long beginTimestamp;
    long bytesPerSecond;
    long endTimestamp;
    long increaseBytes;
    long timestamp;

    public synchronized void reset() {
        this.timestamp = 0;
        this.increaseBytes = 0;
        this.bytesPerSecond = 0;
        this.beginTimestamp = 0;
        this.endTimestamp = 0;
        this.allIncreaseBytes = 0;
    }

    /* access modifiers changed from: package-private */
    public long nowMillis() {
        return SystemClock.uptimeMillis();
    }

    public synchronized void downloading(long increaseBytes2) {
        if (this.timestamp == 0) {
            long nowMillis = nowMillis();
            this.timestamp = nowMillis;
            this.beginTimestamp = nowMillis;
        }
        this.increaseBytes += increaseBytes2;
        this.allIncreaseBytes += increaseBytes2;
    }

    public synchronized void flush() {
        long nowMillis = nowMillis();
        long sinceNowIncreaseBytes = this.increaseBytes;
        long durationMillis = Math.max(1, nowMillis - this.timestamp);
        this.increaseBytes = 0;
        this.timestamp = nowMillis;
        this.bytesPerSecond = (long) ((((float) sinceNowIncreaseBytes) / ((float) durationMillis)) * 1000.0f);
    }

    public long getInstantBytesPerSecondAndFlush() {
        flush();
        return this.bytesPerSecond;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public synchronized long getBytesPerSecondAndFlush() {
        long interval = nowMillis() - this.timestamp;
        if (interval < 1000) {
            long j = this.bytesPerSecond;
            if (j != 0) {
                return j;
            }
        }
        if (this.bytesPerSecond == 0 && interval < 500) {
            return 0;
        }
        return getInstantBytesPerSecondAndFlush();
    }

    public synchronized long getBytesPerSecondFromBegin() {
        long endTimestamp2;
        endTimestamp2 = this.endTimestamp;
        if (endTimestamp2 == 0) {
            endTimestamp2 = nowMillis();
        }
        return (long) ((((float) this.allIncreaseBytes) / ((float) Math.max(1, endTimestamp2 - this.beginTimestamp))) * 1000.0f);
    }

    public synchronized void endTask() {
        this.endTimestamp = nowMillis();
    }

    public String instantSpeed() {
        return getSpeedWithSIAndFlush();
    }

    public String speed() {
        return humanReadableSpeed(getBytesPerSecondAndFlush(), true);
    }

    public String lastSpeed() {
        return humanReadableSpeed(this.bytesPerSecond, true);
    }

    public synchronized long getInstantSpeedDurationMillis() {
        return nowMillis() - this.timestamp;
    }

    public String getSpeedWithBinaryAndFlush() {
        return humanReadableSpeed(getInstantBytesPerSecondAndFlush(), false);
    }

    public String getSpeedWithSIAndFlush() {
        return humanReadableSpeed(getInstantBytesPerSecondAndFlush(), true);
    }

    public String averageSpeed() {
        return speedFromBegin();
    }

    public String speedFromBegin() {
        return humanReadableSpeed(getBytesPerSecondFromBegin(), true);
    }

    private static String humanReadableSpeed(long bytes, boolean si) {
        return Util.humanReadableBytes(bytes, si) + "/s";
    }
}
