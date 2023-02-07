package com.android.volley.toolbox;

import android.graphics.Bitmap;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.misc.MultipartUtils;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

public class HttpHeaderParser {
    public static Cache.Entry parseCacheHeaders(NetworkResponse response) {
        String serverEtag;
        Map<String, String> headers;
        byte[] bArr;
        long lastModified;
        boolean hasCacheControl;
        NetworkResponse networkResponse = response;
        long now = System.currentTimeMillis();
        long serverDate = 0;
        long lastModified2 = 0;
        long serverExpires = 0;
        long softExpire = 0;
        long finalExpire = 0;
        long maxAge = 0;
        long staleWhileRevalidate = 0;
        if (networkResponse != null) {
            long serverDate2 = 0;
            Map<String, String> headers2 = networkResponse.headers;
            String headerValue = headers2.get("Date");
            if (headerValue != null) {
                serverDate2 = parseDateAsEpoch(headerValue);
            }
            String headerValue2 = headers2.get("Cache-Control");
            if (headerValue2 != null) {
                boolean hasCacheControl2 = true;
                lastModified = 0;
                String[] tokens = headerValue2.split(",");
                int i = 0;
                while (i < tokens.length) {
                    String headerValue3 = headerValue2;
                    String token = tokens[i].trim();
                    if (token.equals("no-cache") || token.equals("no-store")) {
                        hasCacheControl2 = false;
                    } else if (token.startsWith("max-age=")) {
                        hasCacheControl2 = true;
                        try {
                            maxAge = Long.parseLong(token.substring(8));
                        } catch (Exception e) {
                        }
                    } else if (token.startsWith("stale-while-revalidate=")) {
                        try {
                            staleWhileRevalidate = Long.parseLong(token.substring(23));
                        } catch (Exception e2) {
                        }
                    } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                        maxAge = 0;
                    }
                    i++;
                    headerValue2 = headerValue3;
                }
                hasCacheControl = hasCacheControl2;
            } else {
                lastModified = 0;
                hasCacheControl = false;
            }
            String headerValue4 = headers2.get("Expires");
            if (headerValue4 != null) {
                serverExpires = parseDateAsEpoch(headerValue4);
            }
            String headerValue5 = headers2.get("Last-Modified");
            if (headerValue5 != null) {
                lastModified2 = parseDateAsEpoch(headerValue5);
            } else {
                lastModified2 = lastModified;
            }
            String str = headerValue5;
            String serverEtag2 = headers2.get("ETag");
            if (hasCacheControl) {
                softExpire = now + (maxAge * 1000);
                finalExpire = softExpire + (1000 * staleWhileRevalidate);
                boolean z = hasCacheControl;
                headers = headers2;
                serverDate = serverDate2;
                long j = now;
                serverEtag = serverEtag2;
                long j2 = j;
            } else if (serverDate2 <= 0 || serverExpires < serverDate2) {
                boolean z2 = hasCacheControl;
                headers = headers2;
                serverDate = serverDate2;
                long j3 = now;
                serverEtag = serverEtag2;
                long j4 = j3;
            } else {
                softExpire = now + (serverExpires - serverDate2);
                finalExpire = softExpire;
                boolean z3 = hasCacheControl;
                headers = headers2;
                serverDate = serverDate2;
                long j5 = now;
                serverEtag = serverEtag2;
                long j6 = j5;
            }
        } else {
            headers = null;
            long j7 = now;
            serverEtag = null;
            long j8 = j7;
        }
        Cache.Entry entry = new Cache.Entry();
        if (networkResponse == null) {
            long j9 = serverExpires;
            bArr = null;
            long j10 = j9;
        } else {
            long j11 = serverExpires;
            bArr = networkResponse.data;
        }
        entry.data = bArr;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = finalExpire;
        entry.serverDate = serverDate;
        entry.lastModified = lastModified2;
        entry.responseHeaders = headers;
        return entry;
    }

    public static Cache.Entry parseBitmapCacheHeaders(Bitmap bitmap) {
        NetworkResponse response = null;
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            response = new NetworkResponse(stream.toByteArray());
        }
        return parseCacheHeaders(response);
    }

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        NetworkResponse networkResponse = response;
        long now = System.currentTimeMillis();
        Map<String, String> headers = networkResponse.headers;
        long serverDate = 0;
        String headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }
        long j = now;
        Cache.Entry entry = new Cache.Entry();
        entry.data = networkResponse.data;
        entry.etag = headers.get("ETag");
        entry.softTtl = 180000 + now;
        entry.ttl = 86400000 + now;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;
    }

    public static Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response, long soft_expire, long expire) {
        NetworkResponse networkResponse = response;
        long now = System.currentTimeMillis();
        Map<String, String> headers = networkResponse.headers;
        long serverDate = 0;
        String headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }
        long j = now;
        Cache.Entry entry = new Cache.Entry();
        entry.data = networkResponse.data;
        entry.etag = headers.get("ETag");
        entry.softTtl = now + soft_expire;
        entry.ttl = now + expire;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;
        return entry;
    }

    public static long parseDateAsEpoch(String dateStr) {
        try {
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e) {
            return 0;
        }
    }

    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = headers.get(MultipartUtils.HEADER_CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2 && pair[0].equals("charset")) {
                    return pair[1];
                }
            }
        }
        return defaultCharset;
    }

    public static String parseCharset(Map<String, String> headers) {
        return parseCharset(headers, "ISO-8859-1");
    }
}
