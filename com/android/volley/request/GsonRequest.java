package com.android.volley.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private final Class<T> clazz;
    private final Gson gson = new Gson();
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private final Map<String, String> params;

    public GsonRequest(String url, Class<T> clazz2, Map<String, String> headers2, Response.Listener<T> listener2, Response.ErrorListener errorListener) {
        super(0, url, errorListener);
        this.clazz = clazz2;
        this.headers = headers2;
        this.params = null;
        this.listener = listener2;
    }

    public GsonRequest(int type, String url, Class<T> clazz2, Map<String, String> headers2, Map<String, String> params2, Response.Listener<T> listener2, Response.ErrorListener errorListener) {
        super(type, url, errorListener);
        this.clazz = clazz2;
        this.headers = headers2;
        this.params = params2;
        this.listener = listener2;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> map = this.headers;
        return map != null ? map : super.getHeaders();
    }

    /* access modifiers changed from: protected */
    public Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> map = this.params;
        return map != null ? map : super.getParams();
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(T response) {
        Response.Listener<T> listener2 = this.listener;
        if (listener2 != null) {
            listener2.onResponse(response);
        }
    }

    public final Class<T> getClazz() {
        return this.clazz;
    }

    /* access modifiers changed from: protected */
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(this.gson.fromJson(new String(response.data, HttpHeaderParser.parseCharset(response.headers)), this.clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError((Throwable) e));
        } catch (JsonSyntaxException e2) {
            return Response.error(new ParseError((Throwable) e2));
        }
    }
}
