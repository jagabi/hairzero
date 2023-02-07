package com.android.volley;

import android.app.Application;
import android.text.TextUtils;
import com.android.volley.toolbox.Volley;

public class ApplicationController extends Application {
    public static final String TAG = "VolleyPatterns";
    private static ApplicationController sInstance;
    private RequestQueue mRequestQueue;

    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized ApplicationController getInstance() {
        ApplicationController applicationController;
        synchronized (ApplicationController.class) {
            applicationController = sInstance;
        }
        return applicationController;
    }

    public RequestQueue getRequestQueue() {
        if (this.mRequestQueue == null) {
            this.mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return this.mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.m79d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        RequestQueue requestQueue = this.mRequestQueue;
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
}
