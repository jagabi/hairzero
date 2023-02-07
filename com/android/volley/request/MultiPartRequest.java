package com.android.volley.request;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import java.util.HashMap;
import java.util.Map;

public abstract class MultiPartRequest<T> extends Request<T> implements Response.ProgressListener {
    private static final String PROTOCOL_CHARSET = "utf-8";
    public static final int TIMEOUT_MS = 30000;
    private boolean isFixedStreamingMode;
    private Map<String, String> mFileUploads = null;
    private Response.Listener<T> mListener;
    private Map<String, MultiPartParam> mMultipartParams = null;
    private Response.ProgressListener mProgressListener;

    /* access modifiers changed from: protected */
    public abstract Response<T> parseNetworkResponse(NetworkResponse networkResponse);

    public MultiPartRequest(int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, Request.Priority.NORMAL, errorListener, new DefaultRetryPolicy(30000, 0, 1.0f));
        this.mListener = listener;
        this.mMultipartParams = new HashMap();
        this.mFileUploads = new HashMap();
    }

    public MultiPartRequest<T> addMultipartParam(String name, String contentType, String value) {
        this.mMultipartParams.put(name, new MultiPartParam(contentType, value));
        return this;
    }

    public MultiPartRequest<T> addStringParam(String name, String value) {
        this.mMultipartParams.put(name, new MultiPartParam("text/plain", value));
        return this;
    }

    public MultiPartRequest<T> addFile(String name, String filePath) {
        this.mFileUploads.put(name, filePath);
        return this;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(T response) {
        Response.Listener<T> listener = this.mListener;
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    public void setOnProgressListener(Response.ProgressListener listener) {
        this.mProgressListener = listener;
    }

    public void onProgress(long transferredBytes, long totalSize) {
        Response.ProgressListener progressListener = this.mProgressListener;
        if (progressListener != null) {
            progressListener.onProgress(transferredBytes, totalSize);
        }
    }

    public static final class MultiPartParam {
        public String contentType;
        public String value;

        public MultiPartParam(String contentType2, String value2) {
            this.contentType = contentType2;
            this.value = value2;
        }
    }

    public Map<String, MultiPartParam> getMultipartParams() {
        return this.mMultipartParams;
    }

    public Map<String, String> getFilesToUpload() {
        return this.mFileUploads;
    }

    public String getProtocolCharset() {
        return PROTOCOL_CHARSET;
    }

    public boolean isFixedStreamingMode() {
        return this.isFixedStreamingMode;
    }

    public void setFixedStreamingMode(boolean isFixedStreamingMode2) {
        this.isFixedStreamingMode = isFixedStreamingMode2;
    }
}
