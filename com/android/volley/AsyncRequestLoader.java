package com.android.volley;

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.android.volley.toolbox.VolleyTickle;
import com.google.gson.Gson;

public class AsyncRequestLoader<T> extends AsyncTaskLoader<T> {
    private T data;
    private VolleyError error;
    private GsonRequest<T> request;

    public AsyncRequestLoader(Context context, GsonRequest<T> request2) {
        super(context);
        this.request = request2;
    }

    public T loadInBackground() {
        RequestTickle requestTickle = VolleyTickle.newRequestTickle(getContext());
        requestTickle.add(this.request);
        NetworkResponse networkResponse = requestTickle.start();
        if (networkResponse.statusCode >= 200 && networkResponse.statusCode < 300) {
            this.data = new Gson().fromJson(VolleyTickle.parseResponse(networkResponse), this.request.getClazz());
        }
        this.error = new VolleyError(networkResponse);
        return this.data;
    }

    /* access modifiers changed from: protected */
    public void onStartLoading() {
        super.onStartLoading();
        T t = this.data;
        if (t != null) {
            deliverResult(t);
        }
        if (this.data == null || takeContentChanged()) {
            forceLoad();
        }
    }

    public void deliverResult(T data2) {
        if (!isReset() || data2 == null) {
            T oldData = this.data;
            this.data = data2;
            if (isStarted()) {
                super.deliverResult(data2);
            }
            if (oldData != null && oldData != data2) {
                releaseResources(oldData);
                return;
            }
            return;
        }
        releaseResources(data2);
    }

    /* access modifiers changed from: protected */
    public void onStopLoading() {
        cancelLoad();
    }

    /* access modifiers changed from: protected */
    public void onReset() {
        onStopLoading();
        T t = this.data;
        if (t != null) {
            releaseResources(t);
            this.data = null;
        }
    }

    public void onCanceled(T data2) {
        super.onCanceled(data2);
        releaseResources(data2);
        this.request.cancel();
    }

    private void releaseResources(T t) {
    }

    public VolleyError getError() {
        return this.error;
    }
}
