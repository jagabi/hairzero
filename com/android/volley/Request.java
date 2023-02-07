package com.android.volley;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.android.volley.Cache;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

public abstract class Request<T> implements Comparable<Request<T>> {
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    private Cache.Entry mCacheEntry;
    private boolean mCanceled;
    private final int mDefaultTrafficStatsTag;
    private final Response.ErrorListener mErrorListener;
    /* access modifiers changed from: private */
    public final VolleyLog.MarkerLog mEventLog;
    private final int mMethod;
    private Map<String, String> mParams;
    private Priority mPriority;
    private Map<String, String> mRequestHeaders;
    private RequestQueue mRequestQueue;
    private boolean mResponseDelivered;
    private RetryPolicy mRetryPolicy;
    private Integer mSequence;
    private boolean mShouldCache;
    private boolean mShouldOverridePatch;
    private Object mTag;
    private final String mUrl;

    public interface Method {
        public static final int DELETE = 3;
        public static final int DEPRECATED_GET_OR_POST = -1;
        public static final int GET = 0;
        public static final int HEAD = 4;
        public static final int OPTIONS = 5;
        public static final int PATCH = 7;
        public static final int POST = 1;
        public static final int PUT = 2;
        public static final int TRACE = 6;
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    /* access modifiers changed from: protected */
    public abstract void deliverResponse(T t);

    /* access modifiers changed from: protected */
    public abstract Response<T> parseNetworkResponse(NetworkResponse networkResponse);

    public Request(int method, String url, Response.ErrorListener listener) {
        this(method, url, listener, (RetryPolicy) null);
    }

    public Request(int method, String url, Response.ErrorListener listener, RetryPolicy retryPolicy) {
        this(method, url, Priority.NORMAL, listener, retryPolicy);
    }

    public Request(int method, String url, Priority priority, Response.ErrorListener listener, RetryPolicy retryPolicy) {
        this.mEventLog = VolleyLog.MarkerLog.ENABLED ? new VolleyLog.MarkerLog() : null;
        this.mShouldCache = true;
        this.mCanceled = false;
        this.mResponseDelivered = false;
        this.mCacheEntry = null;
        this.mRequestHeaders = null;
        this.mParams = null;
        this.mShouldOverridePatch = false;
        this.mMethod = method;
        this.mUrl = url;
        this.mPriority = priority;
        this.mErrorListener = listener;
        setRetryPolicy(retryPolicy == null ? new DefaultRetryPolicy() : retryPolicy);
        this.mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    private static int findDefaultTrafficStatsTag(String url) {
        Uri uri;
        String host;
        if (TextUtils.isEmpty(url) || (uri = Uri.parse(url)) == null || (host = uri.getHost()) == null) {
            return 0;
        }
        return host.hashCode();
    }

    public int getMethod() {
        return this.mMethod;
    }

    public Request<?> setTag(Object tag) {
        this.mTag = tag;
        return this;
    }

    public Object getTag() {
        return this.mTag;
    }

    public Response.ErrorListener getErrorListener() {
        return this.mErrorListener;
    }

    public int getTrafficStatsTag() {
        return this.mDefaultTrafficStatsTag;
    }

    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        this.mRetryPolicy = retryPolicy;
        return this;
    }

    public void addMarker(String tag) {
        try {
            if (VolleyLog.MarkerLog.ENABLED) {
                this.mEventLog.add(tag, Thread.currentThread().getId());
            }
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void finish(final String tag) {
        RequestQueue requestQueue = this.mRequestQueue;
        if (requestQueue != null) {
            requestQueue.finish(this);
        }
        if (VolleyLog.MarkerLog.ENABLED) {
            try {
                final long threadId = Thread.currentThread().getId();
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            Request.this.mEventLog.add(tag, threadId);
                            Request.this.mEventLog.finish(toString());
                        }
                    });
                    return;
                }
                this.mEventLog.add(tag, threadId);
                this.mEventLog.finish(toString());
            } catch (Exception e) {
            }
        }
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.mRequestQueue = requestQueue;
    }

    public final void setSequence(int sequence) {
        this.mSequence = Integer.valueOf(sequence);
    }

    public final int getSequence() {
        Integer num = this.mSequence;
        if (num != null) {
            return num.intValue();
        }
        throw new IllegalStateException("getSequence called before setSequence");
    }

    public String getUrl() {
        try {
            if (!(this.mMethod != 0 || getParams() == null || getParams().size() == 0)) {
                String encodedParams = getEncodedUrlParams();
                String extra = "";
                if (encodedParams != null && encodedParams.length() > 0) {
                    if (!this.mUrl.endsWith("?")) {
                        extra = extra + "?";
                    }
                    extra = extra + encodedParams;
                }
                return this.mUrl + extra;
            }
        } catch (AuthFailureError e) {
        }
        return this.mUrl;
    }

    public String getCacheKey() {
        return getUrl();
    }

    public void setCacheEntry(Cache.Entry entry) {
        this.mCacheEntry = entry;
    }

    public Cache.Entry getCacheEntry() {
        return this.mCacheEntry;
    }

    public void cancel() {
        this.mCanceled = true;
    }

    public boolean isCanceled() {
        return this.mCanceled;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> map = this.mRequestHeaders;
        return map == null ? Collections.emptyMap() : map;
    }

    public void setHeaders(Map<String, String> headers) {
        this.mRequestHeaders = headers;
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getPostParams() throws AuthFailureError {
        return getParams();
    }

    /* access modifiers changed from: protected */
    public String getPostParamsEncoding() {
        return getParamsEncoding();
    }

    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    public byte[] getPostBody() throws AuthFailureError {
        Map<String, String> postParams = getPostParams();
        if (postParams == null || postParams.size() <= 0) {
            return null;
        }
        return encodeParameters(postParams, getPostParamsEncoding());
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> map = this.mParams;
        return map == null ? Collections.emptyMap() : map;
    }

    public void setParams(Map<String, String> params) {
        this.mParams = params;
    }

    /* access modifiers changed from: protected */
    public String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params == null || params.size() <= 0) {
            return null;
        }
        return encodeParameters(params, getParamsEncoding());
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                    encodedParams.append('&');
                }
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    public String getEncodedUrlParams() throws AuthFailureError {
        StringBuilder encodedParams = new StringBuilder();
        String paramsEncoding = getParamsEncoding();
        try {
            for (Map.Entry<String, String> entry : getParams().entrySet()) {
                if (entry.getValue() != null) {
                    encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                    encodedParams.append('&');
                }
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    public final void setShouldCache(boolean shouldCache) {
        this.mShouldCache = shouldCache;
    }

    public final boolean shouldCache() {
        if (this.mMethod == 0) {
            return this.mShouldCache & true;
        }
        return false;
    }

    public void setPriority(Priority priority) throws IllegalStateException {
        if (this.mRequestQueue == null) {
            this.mPriority = priority;
            return;
        }
        throw new IllegalStateException("Cannot change priority after adding to request queue");
    }

    public Priority getPriority() {
        return this.mPriority;
    }

    public final int getTimeoutMs() {
        return this.mRetryPolicy.getCurrentTimeout();
    }

    public RetryPolicy getRetryPolicy() {
        return this.mRetryPolicy;
    }

    public void markDelivered() {
        this.mResponseDelivered = true;
    }

    public boolean hasHadResponseDelivered() {
        return this.mResponseDelivered;
    }

    /* access modifiers changed from: protected */
    public VolleyError parseNetworkError(VolleyError volleyError) {
        return volleyError;
    }

    public void deliverError(VolleyError error) {
        Response.ErrorListener errorListener = this.mErrorListener;
        if (errorListener != null) {
            errorListener.onErrorResponse(error);
        }
    }

    public int compareTo(Request<T> other) {
        int i;
        int i2;
        Priority left = getPriority();
        Priority right = other.getPriority();
        if (left == right) {
            i2 = this.mSequence.intValue();
            i = other.mSequence.intValue();
        } else {
            i2 = right.ordinal();
            i = left.ordinal();
        }
        return i2 - i;
    }

    public String toString() {
        return (this.mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + ("0x" + Integer.toHexString(getTrafficStatsTag())) + " " + getPriority() + " " + this.mSequence;
    }

    public final void overridePatch(boolean override) {
        this.mShouldOverridePatch = override;
    }

    public final boolean shouldOverridePatch() {
        return this.mShouldOverridePatch;
    }
}
