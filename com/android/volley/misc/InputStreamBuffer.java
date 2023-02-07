package com.android.volley.misc;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class InputStreamBuffer {
    private static final boolean DEBUG = false;
    private static final int DEBUG_MAX_BUFFER_SIZE = 80;
    private static final String TAG = "InputStreamBuffer";
    private boolean mAutoAdvance;
    private byte[] mBuffer;
    private int mFilled = 0;
    private InputStream mInputStream;
    private int mOffset = 0;

    public InputStreamBuffer(InputStream inputStream, int bufferSize, boolean autoAdvance) {
        this.mInputStream = inputStream;
        if (bufferSize > 0) {
            this.mBuffer = new byte[leastPowerOf2(bufferSize)];
            this.mAutoAdvance = autoAdvance;
            return;
        }
        throw new IllegalArgumentException(String.format("Buffer size %d must be positive.", new Object[]{Integer.valueOf(bufferSize)}));
    }

    public byte get(int index) throws IllegalStateException, IndexOutOfBoundsException {
        Trace.beginSection("get");
        if (has(index)) {
            Trace.endSection();
            return this.mBuffer[index - this.mOffset];
        }
        Trace.endSection();
        throw new IndexOutOfBoundsException(String.format("Index %d beyond length.", new Object[]{Integer.valueOf(index)}));
    }

    public boolean has(int index) throws IllegalStateException, IndexOutOfBoundsException {
        Trace.beginSection("has");
        int i = this.mOffset;
        if (index >= i) {
            int i2 = index - i;
            if (i2 >= this.mFilled || i2 >= this.mBuffer.length) {
                Trace.endSection();
                return fill(index);
            }
            Trace.endSection();
            return true;
        }
        Trace.endSection();
        throw new IllegalStateException(String.format("Index %d is before buffer %d", new Object[]{Integer.valueOf(index), Integer.valueOf(this.mOffset)}));
    }

    public void advanceTo(int index) throws IllegalStateException, IndexOutOfBoundsException {
        Trace.beginSection("advance to");
        int i = index - this.mOffset;
        if (i <= 0) {
            Trace.endSection();
            return;
        }
        int i2 = this.mFilled;
        if (i < i2) {
            shiftToBeginning(i);
            this.mOffset = index;
            this.mFilled -= i;
        } else if (this.mInputStream != null) {
            int burn = i - i2;
            boolean empty = false;
            int fails = 0;
            while (true) {
                if (burn <= 0) {
                    break;
                }
                try {
                    long burned = this.mInputStream.skip((long) burn);
                    if (burned <= 0) {
                        fails++;
                    } else {
                        burn = (int) (((long) burn) - burned);
                    }
                    if (fails >= 5) {
                        empty = true;
                        break;
                    }
                } catch (IOException e) {
                    empty = true;
                }
            }
            if (empty) {
                this.mInputStream = null;
            }
            this.mOffset = index - burn;
            this.mFilled = 0;
        } else {
            this.mOffset = index;
            this.mFilled = 0;
        }
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, String.format("advanceTo %d buffer: %s", new Object[]{Integer.valueOf(i), this}));
        }
        Trace.endSection();
    }

    private boolean fill(int index) {
        Trace.beginSection("fill");
        int i = this.mOffset;
        if (index >= i) {
            int i2 = index - i;
            if (this.mInputStream == null) {
                Trace.endSection();
                return false;
            }
            int length = i2 + 1;
            if (length > this.mBuffer.length) {
                if (this.mAutoAdvance) {
                    advanceTo(index);
                    i2 = index - this.mOffset;
                } else {
                    int length2 = leastPowerOf2(length);
                    Log.w(TAG, String.format("Increasing buffer length from %d to %d. Bad buffer size chosen, or advanceTo() not called.", new Object[]{Integer.valueOf(this.mBuffer.length), Integer.valueOf(length2)}));
                    this.mBuffer = copyOf(this.mBuffer, length2);
                }
            }
            int read = -1;
            try {
                InputStream inputStream = this.mInputStream;
                byte[] bArr = this.mBuffer;
                int i3 = this.mFilled;
                read = inputStream.read(bArr, i3, bArr.length - i3);
            } catch (IOException e) {
            }
            if (read != -1) {
                this.mFilled += read;
            } else {
                this.mInputStream = null;
            }
            if (Log.isLoggable(TAG, 3)) {
                Log.d(TAG, String.format("fill %d      buffer: %s", new Object[]{Integer.valueOf(i2), this}));
            }
            Trace.endSection();
            if (i2 < this.mFilled) {
                return true;
            }
            return false;
        }
        Trace.endSection();
        throw new IllegalStateException(String.format("Index %d is before buffer %d", new Object[]{Integer.valueOf(index), Integer.valueOf(this.mOffset)}));
    }

    public static byte[] copyOf(byte[] original, int newLength) {
        if (Utils.hasGingerbread()) {
            return Arrays.copyOf(original, newLength);
        }
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    private void shiftToBeginning(int i) {
        if (i < this.mBuffer.length) {
            for (int j = 0; j + i < this.mFilled; j++) {
                byte[] bArr = this.mBuffer;
                bArr[j] = bArr[j + i];
            }
            return;
        }
        throw new IndexOutOfBoundsException(String.format("Index %d out of bounds. Length %d", new Object[]{Integer.valueOf(i), Integer.valueOf(this.mBuffer.length)}));
    }

    public String toString() {
        return String.format("+%d+%d [%d]", new Object[]{Integer.valueOf(this.mOffset), Integer.valueOf(this.mBuffer.length), Integer.valueOf(this.mFilled)});
    }

    public String toDebugString() {
        Trace.beginSection("to debug string");
        StringBuilder sb = new StringBuilder();
        sb.append("+").append(this.mOffset);
        sb.append("+").append(this.mBuffer.length);
        sb.append(" [");
        int i = 0;
        while (i < this.mBuffer.length && i < 80) {
            if (i > 0) {
                sb.append(",");
            }
            if (i < this.mFilled) {
                sb.append(String.format("%02X", new Object[]{Byte.valueOf(this.mBuffer[i])}));
            } else {
                sb.append("__");
            }
            i++;
        }
        if (this.mInputStream != null) {
            sb.append("...");
        }
        sb.append("]");
        Trace.endSection();
        return sb.toString();
    }

    private static int leastPowerOf2(int n) {
        int n2 = n - 1;
        int n3 = n2 | (n2 >> 1);
        int n4 = n3 | (n3 >> 2);
        int n5 = n4 | (n4 >> 4);
        int n6 = n5 | (n5 >> 8);
        return (n6 | (n6 >> 16)) + 1;
    }
}
