package com.android.volley.toolbox.multipart;

import android.text.TextUtils;
import com.android.volley.misc.MultipartUtils;
import java.util.Random;
import org.apache.http.util.EncodingUtils;

class Boundary {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final String boundary;
    private final byte[] closingBoundary;
    private final byte[] startingBoundary;

    Boundary(String boundary2) {
        boundary2 = TextUtils.isEmpty(boundary2) ? generateBoundary() : boundary2;
        this.boundary = boundary2;
        this.startingBoundary = EncodingUtils.getAsciiBytes(MultipartUtils.BOUNDARY_PREFIX + boundary2 + MultipartUtils.CRLF);
        this.closingBoundary = EncodingUtils.getAsciiBytes(MultipartUtils.BOUNDARY_PREFIX + boundary2 + MultipartUtils.BOUNDARY_PREFIX + MultipartUtils.CRLF);
    }

    /* access modifiers changed from: package-private */
    public String getBoundary() {
        return this.boundary;
    }

    /* access modifiers changed from: package-private */
    public byte[] getStartingBoundary() {
        return this.startingBoundary;
    }

    /* access modifiers changed from: package-private */
    public byte[] getClosingBoundary() {
        return this.closingBoundary;
    }

    private static String generateBoundary() {
        Random rand = new Random();
        int count = rand.nextInt(11) + 30;
        StringBuilder buffer = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            char[] cArr = MULTIPART_CHARS;
            buffer.append(cArr[rand.nextInt(cArr.length)]);
        }
        return buffer.toString();
    }
}
