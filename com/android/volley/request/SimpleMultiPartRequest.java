package com.android.volley.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.UnsupportedEncodingException;

public class SimpleMultiPartRequest extends MultiPartRequest<String> {
    private Response.Listener<String> mListener;

    public SimpleMultiPartRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mListener = listener;
    }

    public SimpleMultiPartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(1, url, listener, errorListener);
        this.mListener = listener;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(String response) {
        Response.Listener<String> listener = this.mListener;
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    /* access modifiers changed from: protected */
    public Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}
