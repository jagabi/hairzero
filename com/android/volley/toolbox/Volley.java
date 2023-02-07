package com.android.volley.toolbox;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import com.android.volley.RequestQueue;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.misc.NetUtils;
import com.android.volley.misc.Utils;
import java.io.File;

public class Volley {
    private static final String DEFAULT_CACHE_DIR = "volley";

    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        if (stack == null) {
            stack = Utils.hasHoneycomb() ? new HurlStack() : new HttpClientStack(AndroidHttpClient.newInstance(NetUtils.getUserAgent(context)));
        }
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), new BasicNetwork(stack));
        queue.start();
        return queue;
    }

    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, (HttpStack) null);
    }
}
