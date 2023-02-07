package com.android.volley.toolbox;

import android.os.SystemClock;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyLog;
import com.android.volley.error.ServerError;
import com.android.volley.error.VolleyError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.impl.cookie.DateUtils;

public class BasicNetwork implements Network {
    protected static final boolean DEBUG = VolleyLog.DEBUG;
    private static int DEFAULT_POOL_SIZE = 4096;
    private static int SLOW_REQUEST_THRESHOLD_MS = 3000;
    protected final HttpStack mHttpStack;
    protected final ByteArrayPool mPool;

    public BasicNetwork(HttpStack httpStack) {
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    public BasicNetwork(HttpStack httpStack, ByteArrayPool pool) {
        this.mHttpStack = httpStack;
        this.mPool = pool;
    }

    /* Debug info: failed to restart local var, previous not found, register: 23 */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x0188 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x012d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.volley.NetworkResponse performRequest(com.android.volley.Request<?> r24) throws com.android.volley.error.VolleyError {
        /*
            r23 = this;
            r7 = r23
            r8 = r24
            long r9 = android.os.SystemClock.elapsedRealtime()
        L_0x0008:
            r1 = 0
            r2 = 0
            java.util.Map r3 = java.util.Collections.emptyMap()
            r11 = 0
            java.util.HashMap r0 = new java.util.HashMap     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            r0.<init>()     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            com.android.volley.Cache$Entry r4 = r24.getCacheEntry()     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            r7.addCacheHeaders(r0, r4)     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            com.android.volley.toolbox.HttpStack r4 = r7.mHttpStack     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            org.apache.http.HttpResponse r4 = r4.performRequest(r8, r0)     // Catch:{ SocketTimeoutException -> 0x01c9, ConnectTimeoutException -> 0x01bd, MalformedURLException -> 0x019f, IOException -> 0x0128 }
            r12 = r4
            org.apache.http.StatusLine r6 = r12.getStatusLine()     // Catch:{ SocketTimeoutException -> 0x0124, ConnectTimeoutException -> 0x0120, MalformedURLException -> 0x011c, IOException -> 0x0119 }
            int r1 = r6.getStatusCode()     // Catch:{ SocketTimeoutException -> 0x0124, ConnectTimeoutException -> 0x0120, MalformedURLException -> 0x011c, IOException -> 0x0119 }
            r15 = r1
            org.apache.http.Header[] r1 = r12.getAllHeaders()     // Catch:{ SocketTimeoutException -> 0x0124, ConnectTimeoutException -> 0x0120, MalformedURLException -> 0x011c, IOException -> 0x0119 }
            java.util.Map r1 = convertHeaders(r1)     // Catch:{ SocketTimeoutException -> 0x0124, ConnectTimeoutException -> 0x0120, MalformedURLException -> 0x011c, IOException -> 0x0119 }
            r14 = r1
            r1 = 304(0x130, float:4.26E-43)
            if (r15 != r1) goto L_0x0087
            com.android.volley.Cache$Entry r1 = r24.getCacheEntry()     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            if (r1 != 0) goto L_0x0054
            com.android.volley.NetworkResponse r3 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            r17 = 304(0x130, float:4.26E-43)
            r18 = 0
            r20 = 1
            long r4 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            long r21 = r4 - r9
            r16 = r3
            r19 = r14
            r16.<init>(r17, r18, r19, r20, r21)     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            return r3
        L_0x0054:
            java.util.Map<java.lang.String, java.lang.String> r3 = r1.responseHeaders     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            r3.putAll(r14)     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            com.android.volley.NetworkResponse r3 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            r17 = 304(0x130, float:4.26E-43)
            byte[] r4 = r1.data     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            java.util.Map<java.lang.String, java.lang.String> r5 = r1.responseHeaders     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            r20 = 1
            long r18 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            long r21 = r18 - r9
            r16 = r3
            r18 = r4
            r19 = r5
            r16.<init>(r17, r18, r19, r20, r21)     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            return r3
        L_0x0073:
            r0 = move-exception
            r1 = r12
            r3 = r14
            goto L_0x0129
        L_0x0078:
            r0 = move-exception
            r1 = r12
            r3 = r14
            goto L_0x01a0
        L_0x007d:
            r0 = move-exception
            r1 = r12
            r3 = r14
            goto L_0x01be
        L_0x0082:
            r0 = move-exception
            r1 = r12
            r3 = r14
            goto L_0x01ca
        L_0x0087:
            org.apache.http.HttpEntity r1 = r12.getEntity()     // Catch:{ SocketTimeoutException -> 0x0114, ConnectTimeoutException -> 0x010f, MalformedURLException -> 0x010a, IOException -> 0x0106 }
            if (r1 == 0) goto L_0x0098
            org.apache.http.HttpEntity r1 = r12.getEntity()     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            byte[] r1 = r7.entityToBytes(r8, r1)     // Catch:{ SocketTimeoutException -> 0x0082, ConnectTimeoutException -> 0x007d, MalformedURLException -> 0x0078, IOException -> 0x0073 }
            r20 = r1
            goto L_0x009c
        L_0x0098:
            byte[] r1 = new byte[r11]     // Catch:{ SocketTimeoutException -> 0x0114, ConnectTimeoutException -> 0x010f, MalformedURLException -> 0x010a, IOException -> 0x0106 }
            r20 = r1
        L_0x009c:
            long r1 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x00ff, ConnectTimeoutException -> 0x00f8, MalformedURLException -> 0x00f1, IOException -> 0x00eb }
            long r21 = r1 - r9
            r1 = r23
            r2 = r21
            r4 = r24
            r5 = r20
            r1.logSlowRequests(r2, r4, r5, r6)     // Catch:{ SocketTimeoutException -> 0x00ff, ConnectTimeoutException -> 0x00f8, MalformedURLException -> 0x00f1, IOException -> 0x00eb }
            r1 = 200(0xc8, float:2.8E-43)
            if (r15 < r1) goto L_0x00cb
            r1 = 299(0x12b, float:4.19E-43)
            if (r15 > r1) goto L_0x00cb
            com.android.volley.NetworkResponse r1 = new com.android.volley.NetworkResponse     // Catch:{ SocketTimeoutException -> 0x00ff, ConnectTimeoutException -> 0x00f8, MalformedURLException -> 0x00f1, IOException -> 0x00eb }
            r17 = 0
            long r2 = android.os.SystemClock.elapsedRealtime()     // Catch:{ SocketTimeoutException -> 0x00ff, ConnectTimeoutException -> 0x00f8, MalformedURLException -> 0x00f1, IOException -> 0x00eb }
            long r18 = r2 - r9
            r13 = r1
            r3 = r14
            r14 = r15
            r2 = r15
            r15 = r20
            r16 = r3
            r13.<init>(r14, r15, r16, r17, r18)     // Catch:{ SocketTimeoutException -> 0x00e5, ConnectTimeoutException -> 0x00df, MalformedURLException -> 0x00d9, IOException -> 0x00d3 }
            return r1
        L_0x00cb:
            r3 = r14
            r2 = r15
            java.io.IOException r1 = new java.io.IOException     // Catch:{ SocketTimeoutException -> 0x00e5, ConnectTimeoutException -> 0x00df, MalformedURLException -> 0x00d9, IOException -> 0x00d3 }
            r1.<init>()     // Catch:{ SocketTimeoutException -> 0x00e5, ConnectTimeoutException -> 0x00df, MalformedURLException -> 0x00d9, IOException -> 0x00d3 }
            throw r1     // Catch:{ SocketTimeoutException -> 0x00e5, ConnectTimeoutException -> 0x00df, MalformedURLException -> 0x00d9, IOException -> 0x00d3 }
        L_0x00d3:
            r0 = move-exception
            r1 = r12
            r2 = r20
            goto L_0x0129
        L_0x00d9:
            r0 = move-exception
            r1 = r12
            r2 = r20
            goto L_0x01a0
        L_0x00df:
            r0 = move-exception
            r1 = r12
            r2 = r20
            goto L_0x01be
        L_0x00e5:
            r0 = move-exception
            r1 = r12
            r2 = r20
            goto L_0x01ca
        L_0x00eb:
            r0 = move-exception
            r3 = r14
            r1 = r12
            r2 = r20
            goto L_0x0129
        L_0x00f1:
            r0 = move-exception
            r3 = r14
            r1 = r12
            r2 = r20
            goto L_0x01a0
        L_0x00f8:
            r0 = move-exception
            r3 = r14
            r1 = r12
            r2 = r20
            goto L_0x01be
        L_0x00ff:
            r0 = move-exception
            r3 = r14
            r1 = r12
            r2 = r20
            goto L_0x01ca
        L_0x0106:
            r0 = move-exception
            r3 = r14
            r1 = r12
            goto L_0x0129
        L_0x010a:
            r0 = move-exception
            r3 = r14
            r1 = r12
            goto L_0x01a0
        L_0x010f:
            r0 = move-exception
            r3 = r14
            r1 = r12
            goto L_0x01be
        L_0x0114:
            r0 = move-exception
            r3 = r14
            r1 = r12
            goto L_0x01ca
        L_0x0119:
            r0 = move-exception
            r1 = r12
            goto L_0x0129
        L_0x011c:
            r0 = move-exception
            r1 = r12
            goto L_0x01a0
        L_0x0120:
            r0 = move-exception
            r1 = r12
            goto L_0x01be
        L_0x0124:
            r0 = move-exception
            r1 = r12
            goto L_0x01ca
        L_0x0128:
            r0 = move-exception
        L_0x0129:
            r4 = 0
            r5 = 0
            if (r1 == 0) goto L_0x0188
            org.apache.http.StatusLine r6 = r1.getStatusLine()
            int r4 = r6.getStatusCode()
            r6 = 2
            java.lang.Object[] r6 = new java.lang.Object[r6]
            java.lang.Integer r12 = java.lang.Integer.valueOf(r4)
            r6[r11] = r12
            r11 = 1
            java.lang.String r12 = r24.getUrl()
            r6[r11] = r12
            java.lang.String r11 = "Unexpected response code %d for %s"
            com.android.volley.VolleyLog.m80e(r11, r6)
            if (r2 == 0) goto L_0x0182
            com.android.volley.NetworkResponse r6 = new com.android.volley.NetworkResponse
            r16 = 0
            long r11 = android.os.SystemClock.elapsedRealtime()
            long r17 = r11 - r9
            r12 = r6
            r13 = r4
            r14 = r2
            r15 = r3
            r12.<init>(r13, r14, r15, r16, r17)
            r5 = r6
            r6 = 500(0x1f4, float:7.0E-43)
            if (r4 >= r6) goto L_0x017c
            r6 = 401(0x191, float:5.62E-43)
            if (r4 == r6) goto L_0x0171
            r6 = 403(0x193, float:5.65E-43)
            if (r4 != r6) goto L_0x016b
            goto L_0x0171
        L_0x016b:
            com.android.volley.error.VolleyError r6 = new com.android.volley.error.VolleyError
            r6.<init>((com.android.volley.NetworkResponse) r5)
            throw r6
        L_0x0171:
            com.android.volley.error.AuthFailureError r6 = new com.android.volley.error.AuthFailureError
            r6.<init>((com.android.volley.NetworkResponse) r5)
            java.lang.String r11 = "auth"
            attemptRetryOnException(r11, r8, r6)
            goto L_0x01d5
        L_0x017c:
            com.android.volley.error.ServerError r6 = new com.android.volley.error.ServerError
            r6.<init>(r5)
            throw r6
        L_0x0182:
            com.android.volley.error.NetworkError r6 = new com.android.volley.error.NetworkError
            r6.<init>((com.android.volley.NetworkResponse) r5)
            throw r6
        L_0x0188:
            com.android.volley.error.NoConnectionError r6 = new com.android.volley.error.NoConnectionError
            com.android.volley.NetworkResponse r11 = new com.android.volley.NetworkResponse
            r13 = -1
            r14 = 0
            r16 = 0
            long r17 = android.os.SystemClock.elapsedRealtime()
            long r17 = r17 - r9
            r12 = r11
            r15 = r3
            r12.<init>(r13, r14, r15, r16, r17)
            r6.<init>(r11, r0)
            throw r6
        L_0x019f:
            r0 = move-exception
        L_0x01a0:
            java.lang.RuntimeException r4 = new java.lang.RuntimeException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Bad URL "
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r6 = r24.getUrl()
            java.lang.StringBuilder r5 = r5.append(r6)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5, r0)
            throw r4
        L_0x01bd:
            r0 = move-exception
        L_0x01be:
            com.android.volley.error.TimeoutError r4 = new com.android.volley.error.TimeoutError
            r4.<init>()
            java.lang.String r5 = "connection"
            attemptRetryOnException(r5, r8, r4)
            goto L_0x01d4
        L_0x01c9:
            r0 = move-exception
        L_0x01ca:
            com.android.volley.error.TimeoutError r4 = new com.android.volley.error.TimeoutError
            r4.<init>()
            java.lang.String r5 = "socket"
            attemptRetryOnException(r5, r8, r4)
        L_0x01d4:
        L_0x01d5:
            goto L_0x0008
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.toolbox.BasicNetwork.performRequest(com.android.volley.Request):com.android.volley.NetworkResponse");
    }

    private void logSlowRequests(long requestLifetime, Request<?> request, byte[] responseContents, StatusLine statusLine) {
        if (DEBUG || requestLifetime > ((long) SLOW_REQUEST_THRESHOLD_MS)) {
            Object[] objArr = new Object[5];
            objArr[0] = request;
            objArr[1] = Long.valueOf(requestLifetime);
            objArr[2] = responseContents != null ? Integer.valueOf(responseContents.length) : "null";
            objArr[3] = Integer.valueOf(statusLine.getStatusCode());
            objArr[4] = Integer.valueOf(request.getRetryPolicy().getCurrentRetryCount());
            VolleyLog.m79d("HTTP response for request=<%s> [lifetime=%d], [size=%s], [rc=%d], [retryCount=%s]", objArr);
        }
    }

    private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception) throws VolleyError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();
        try {
            retryPolicy.retry(exception);
            request.addMarker(String.format("%s-retry [timeout=%s]", new Object[]{logPrefix, Integer.valueOf(oldTimeout)}));
        } catch (VolleyError e) {
            request.addMarker(String.format("%s-timeout-giveup [timeout=%s]", new Object[]{logPrefix, Integer.valueOf(oldTimeout)}));
            throw e;
        }
    }

    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        if (entry != null) {
            if (entry.etag != null) {
                headers.put("If-None-Match", entry.etag);
            }
            if (entry.lastModified > 0) {
                headers.put("If-Modified-Since", DateUtils.formatDate(new Date(entry.lastModified)));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logError(String what, String url, long start) {
        VolleyLog.m82v("HTTP ERROR(%s) %d ms to fetch %s", what, Long.valueOf(SystemClock.elapsedRealtime() - start), url);
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    private byte[] entityToBytes(Request<?> request, HttpEntity entity) throws IOException, ServerError {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(this.mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        long totalSize = (long) ((int) entity.getContentLength());
        Response.ProgressListener progressListener = null;
        try {
            if (request instanceof Response.ProgressListener) {
                progressListener = (Response.ProgressListener) request;
            }
            InputStream in = entity.getContent();
            if (in != null) {
                buffer = this.mPool.getBuf(1024);
                int transferredBytes = 0;
                while (true) {
                    int read = in.read(buffer);
                    int count = read;
                    if (read == -1) {
                        break;
                    }
                    bytes.write(buffer, 0, count);
                    transferredBytes += count;
                    if (progressListener != null) {
                        progressListener.onProgress((long) transferredBytes, totalSize);
                    }
                }
                return bytes.toByteArray();
            }
            throw new ServerError();
        } finally {
            try {
                entity.consumeContent();
            } catch (IOException e) {
                VolleyLog.m82v("Error occured when calling consumingContent", new Object[0]);
            }
            this.mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
