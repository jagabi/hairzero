package com.android.volley.toolbox;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestTickle;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.misc.NetUtils;
import com.android.volley.misc.Utils;
import java.io.File;
import java.io.UnsupportedEncodingException;

public class VolleyTickle {
    private static final String DEFAULT_CACHE_DIR = "volley";

    public static RequestTickle newRequestTickle(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        if (stack == null) {
            stack = Utils.hasHoneycomb() ? new HurlStack() : new HttpClientStack(AndroidHttpClient.newInstance(NetUtils.getUserAgent(context)));
        }
        return new RequestTickle(new DiskBasedCache(cacheDir), new BasicNetwork(stack));
    }

    public static RequestTickle newRequestTickle(Context context) {
        return newRequestTickle(context, (HttpStack) null);
    }

    public static String parseResponse(NetworkResponse response) {
        try {
            return new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            return new String(response.data);
        }
    }
}
