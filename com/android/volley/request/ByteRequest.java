package com.android.volley.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.misc.MultipartUtils;
import com.android.volley.toolbox.HttpHeaderParser;

public class ByteRequest extends Request<byte[]> {
    private final Response.Listener<byte[]> mListener;

    public ByteRequest(String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        this(0, url, listener, errorListener);
    }

    public ByteRequest(int method, String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(byte[] response) {
        Response.Listener<byte[]> listener = this.mListener;
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    /* access modifiers changed from: protected */
    public Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    public String getBodyContentType() {
        return MultipartUtils.CONTENT_TYPE_OCTET_STREAM;
    }
}
