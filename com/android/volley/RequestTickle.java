package com.android.volley;

import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.android.volley.error.VolleyError;
import java.util.Map;

public class RequestTickle {
    private VolleyError error;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private final Network mNetwork;
    private Request<?> mRequest;
    private Response<?> response;

    public RequestTickle(Cache cache, Network network, ResponseDelivery delivery) {
        this.mCache = cache;
        this.mNetwork = network;
        this.mDelivery = delivery;
    }

    public RequestTickle(Cache cache, Network network) {
        this(cache, network, new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public <T> Request<T> add(Request<T> request) {
        this.mRequest = request;
        return request;
    }

    public void cancel() {
        Request<?> request = this.mRequest;
        if (request != null) {
            request.cancel();
        }
    }

    public Cache getCache() {
        return this.mCache;
    }

    public NetworkResponse start() {
        if (this.mRequest == null) {
            return null;
        }
        NetworkResponse networkResponse = null;
        long startTimeMs = SystemClock.elapsedRealtime();
        try {
            this.mRequest.addMarker("network-queue-take");
            if (this.mRequest.isCanceled()) {
                this.mRequest.finish("network-discard-cancelled");
                return null;
            }
            if (Build.VERSION.SDK_INT >= 14) {
                TrafficStats.setThreadStatsTag(this.mRequest.getTrafficStatsTag());
            }
            networkResponse = this.mNetwork.performRequest(this.mRequest);
            if (!networkResponse.notModified || !this.mRequest.hasHadResponseDelivered()) {
                this.response = this.mRequest.parseNetworkResponse(networkResponse);
                this.mRequest.addMarker("network-parse-complete");
                if (!(this.mCache == null || !this.mRequest.shouldCache() || this.response.cacheEntry == null)) {
                    this.mCache.put(this.mRequest.getCacheKey(), this.response.cacheEntry);
                    this.mRequest.addMarker("network-cache-written");
                }
                this.mRequest.markDelivered();
                this.mDelivery.postResponse(this.mRequest, this.response);
                if (networkResponse == null) {
                    return new NetworkResponse(0, (byte[]) null, (Map<String, String>) null, false);
                }
                return networkResponse;
            }
            this.mRequest.finish("not-modified");
            return networkResponse;
        } catch (VolleyError volleyError) {
            networkResponse = volleyError.networkResponse;
            volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
            parseAndDeliverNetworkError(this.mRequest, volleyError);
        } catch (Exception e) {
            VolleyLog.m81e(e, "Unhandled exception %s", e.toString());
            VolleyError volleyError2 = new VolleyError((Throwable) e);
            volleyError2.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
            this.mDelivery.postError(this.mRequest, volleyError2);
        }
    }

    public Response<?> getResponse() {
        return this.response;
    }

    public VolleyError getError() {
        return this.error;
    }

    private void parseAndDeliverNetworkError(Request<?> request, VolleyError volleyError) {
        VolleyError parseNetworkError = request.parseNetworkError(volleyError);
        this.error = parseNetworkError;
        this.mDelivery.postError(request, parseNetworkError);
    }
}
