package com.android.volley;

import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;
import android.os.SystemClock;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class NetworkDispatcher extends Thread {
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private final Network mNetwork;
    private final BlockingQueue<Request<?>> mQueue;
    private volatile boolean mQuit = false;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
        this.mQueue = queue;
        this.mNetwork = network;
        this.mCache = cache;
        this.mDelivery = delivery;
    }

    public void quit() {
        this.mQuit = true;
        interrupt();
    }

    private void addTrafficStatsTag(Request<?> request) {
        if (Build.VERSION.SDK_INT >= 14) {
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }
    }

    public void run() {
        NetworkResponse networkResponse;
        Process.setThreadPriority(10);
        while (true) {
            long startTimeMs = SystemClock.elapsedRealtime();
            try {
                Request<?> request = this.mQueue.take();
                try {
                    request.addMarker("network-queue-take");
                    if (request.isCanceled()) {
                        request.finish("network-discard-cancelled");
                    } else {
                        addTrafficStatsTag(request);
                        if (!Utils.isSpecialType(request.getUrl())) {
                            networkResponse = this.mNetwork.performRequest(request);
                            request.addMarker("network-http-complete");
                            if (networkResponse.notModified && request.hasHadResponseDelivered()) {
                                request.finish("not-modified");
                            }
                        } else {
                            networkResponse = new NetworkResponse(0, (byte[]) null, (Map<String, String>) null, false);
                        }
                        Response<?> response = request.parseNetworkResponse(networkResponse);
                        request.addMarker("network-parse-complete");
                        if (!(this.mCache == null || !request.shouldCache() || response.cacheEntry == null)) {
                            this.mCache.put(request.getCacheKey(), response.cacheEntry);
                            request.addMarker("network-cache-written");
                        }
                        request.markDelivered();
                        this.mDelivery.postResponse(request, response);
                    }
                } catch (VolleyError volleyError) {
                    volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                    parseAndDeliverNetworkError(request, volleyError);
                } catch (Exception e) {
                    VolleyLog.m81e(e, "Unhandled exception %s", e.toString());
                    VolleyError volleyError2 = new VolleyError((Throwable) e);
                    volleyError2.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
                    this.mDelivery.postError(request, volleyError2);
                }
            } catch (InterruptedException e2) {
                if (this.mQuit) {
                    return;
                }
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
        this.mDelivery.postError(request, request.parseNetworkError(error));
    }
}
