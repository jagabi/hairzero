package com.android.volley.toolbox;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import androidx.collection.ArrayMap;
import androidx.fragment.app.FragmentManager;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.cache.BitmapCache;
import com.android.volley.error.VolleyError;
import com.android.volley.request.ImageRequest;
import java.util.Iterator;
import java.util.LinkedList;

public class ImageLoader {
    private int mBatchResponseDelayMs;
    /* access modifiers changed from: private */
    public final ArrayMap<String, BatchedImageRequest> mBatchedResponses;
    private final ImageCache mCache;
    private ContentResolver mContentResolver;
    private final Handler mHandler;
    private ArrayMap<String, String> mHeaders;
    /* access modifiers changed from: private */
    public final ArrayMap<String, BatchedImageRequest> mInFlightRequests;
    private final RequestQueue mRequestQueue;
    private Resources mResources;
    /* access modifiers changed from: private */
    public Runnable mRunnable;

    public interface ImageListener extends Response.ErrorListener {
        void onResponse(ImageContainer imageContainer, boolean z);
    }

    public ImageLoader(RequestQueue queue) {
        this(queue, BitmapCache.getInstance((FragmentManager) null));
    }

    public ImageLoader(RequestQueue queue, ImageCache imageCache) {
        this(queue, imageCache, (Resources) null);
    }

    public ImageLoader(RequestQueue queue, ImageCache imageCache, Resources resources) {
        this.mBatchResponseDelayMs = 100;
        this.mInFlightRequests = new ArrayMap<>();
        this.mBatchedResponses = new ArrayMap<>();
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mRequestQueue = queue;
        this.mCache = imageCache;
        this.mResources = resources;
    }

    /* access modifiers changed from: protected */
    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    /* access modifiers changed from: protected */
    public ImageCache getImageCache() {
        return this.mCache;
    }

    /* access modifiers changed from: protected */
    public Cache getCache() {
        return this.mRequestQueue.getCache();
    }

    public static ImageListener getImageListener(final ImageView view, final int defaultImageResId, final int errorImageResId) {
        return new ImageListener() {
            public void onErrorResponse(VolleyError error) {
                int i = errorImageResId;
                if (i != 0) {
                    view.setImageResource(i);
                }
            }

            public void onResponse(ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.getBitmap());
                    return;
                }
                int i = defaultImageResId;
                if (i != 0) {
                    view.setImageResource(i);
                }
            }
        };
    }

    public boolean isCached(String requestUrl, int maxWidth, int maxHeight) {
        return isCached(requestUrl, maxWidth, maxHeight, ImageView.ScaleType.CENTER_INSIDE);
    }

    public boolean isCached(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        throwIfNotOnMainThread();
        return this.mCache.getBitmap(getCacheKey(requestUrl, maxWidth, maxHeight, scaleType)) != null;
    }

    public ImageContainer get(String requestUrl, ImageListener listener) {
        return get(requestUrl, listener, 0, 0);
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight) {
        return get(requestUrl, imageListener, maxWidth, maxHeight, ImageView.ScaleType.CENTER_INSIDE);
    }

    public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        ImageListener imageListener2 = imageListener;
        throwIfNotOnMainThread();
        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
        Bitmap cachedBitmap = this.mCache.getBitmap(cacheKey);
        if (cachedBitmap != null) {
            ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, (String) null, (ImageListener) null);
            imageListener2.onResponse(container, true);
            return container;
        }
        ImageContainer imageContainer = new ImageContainer((Bitmap) null, requestUrl, cacheKey, imageListener);
        imageListener2.onResponse(imageContainer, true);
        BatchedImageRequest request = this.mInFlightRequests.get(cacheKey);
        if (request != null) {
            request.addContainer(imageContainer);
            return imageContainer;
        }
        Request<Bitmap> newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, scaleType, cacheKey);
        newRequest.setHeaders(this.mHeaders);
        this.mRequestQueue.add(newRequest);
        this.mInFlightRequests.put(cacheKey, new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    public ImageContainer set(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap bitmap) {
        throwIfNotOnMainThread();
        String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
        ImageContainer imageContainer = new ImageContainer(bitmap, requestUrl, cacheKey, imageListener);
        imageListener.onResponse(imageContainer, true);
        this.mCache.putBitmap(cacheKey, bitmap);
        getCache().put(requestUrl, Response.success(bitmap, HttpHeaderParser.parseBitmapCacheHeaders(bitmap)).cacheEntry);
        return imageContainer;
    }

    /* access modifiers changed from: protected */
    public Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, String cacheKey) {
        final String str = cacheKey;
        return new ImageRequest(requestUrl, this.mResources, this.mContentResolver, new Response.Listener<Bitmap>() {
            public void onResponse(Bitmap response) {
                ImageLoader.this.onGetImageSuccess(str, response);
            }
        }, maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                ImageLoader.this.onGetImageError(str, error);
            }
        });
    }

    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        this.mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /* access modifiers changed from: protected */
    public void onGetImageSuccess(String cacheKey, Bitmap response) {
        this.mCache.putBitmap(cacheKey, response);
        BatchedImageRequest request = this.mInFlightRequests.remove(cacheKey);
        if (request != null) {
            Bitmap unused = request.mResponseBitmap = response;
            batchResponse(cacheKey, request);
        }
    }

    /* access modifiers changed from: protected */
    public void setImageSuccess(String cacheKey, Bitmap response) {
        this.mCache.putBitmap(cacheKey, response);
        BatchedImageRequest request = this.mInFlightRequests.remove(cacheKey);
        if (request != null) {
            Bitmap unused = request.mResponseBitmap = response;
            batchResponse(cacheKey, request);
        }
    }

    /* access modifiers changed from: protected */
    public void onGetImageError(String cacheKey, VolleyError error) {
        BatchedImageRequest request = this.mInFlightRequests.remove(cacheKey);
        if (request != null) {
            request.setError(error);
            batchResponse(cacheKey, request);
        }
    }

    public class ImageContainer {
        /* access modifiers changed from: private */
        public Bitmap mBitmap;
        private final String mCacheKey;
        /* access modifiers changed from: private */
        public final ImageListener mListener;
        private final String mRequestUrl;

        public ImageContainer(Bitmap bitmap, String requestUrl, String cacheKey, ImageListener listener) {
            this.mBitmap = bitmap;
            this.mRequestUrl = requestUrl;
            this.mCacheKey = cacheKey;
            this.mListener = listener;
        }

        public void cancelRequest() {
            if (this.mListener != null) {
                BatchedImageRequest request = (BatchedImageRequest) ImageLoader.this.mInFlightRequests.get(this.mCacheKey);
                if (request == null) {
                    BatchedImageRequest request2 = (BatchedImageRequest) ImageLoader.this.mBatchedResponses.get(this.mCacheKey);
                    if (request2 != null) {
                        request2.removeContainerAndCancelIfNecessary(this);
                        if (request2.mContainers.size() == 0) {
                            ImageLoader.this.mBatchedResponses.remove(this.mCacheKey);
                        }
                    }
                } else if (request.removeContainerAndCancelIfNecessary(this)) {
                    ImageLoader.this.mInFlightRequests.remove(this.mCacheKey);
                }
            }
        }

        public Bitmap getBitmap() {
            return this.mBitmap;
        }

        public String getRequestUrl() {
            return this.mRequestUrl;
        }
    }

    private class BatchedImageRequest {
        /* access modifiers changed from: private */
        public final LinkedList<ImageContainer> mContainers;
        private VolleyError mError;
        private final Request<?> mRequest;
        /* access modifiers changed from: private */
        public Bitmap mResponseBitmap;

        public BatchedImageRequest(Request<?> request, ImageContainer container) {
            LinkedList<ImageContainer> linkedList = new LinkedList<>();
            this.mContainers = linkedList;
            this.mRequest = request;
            linkedList.add(container);
        }

        public void setError(VolleyError error) {
            this.mError = error;
        }

        public VolleyError getError() {
            return this.mError;
        }

        public void addContainer(ImageContainer container) {
            this.mContainers.add(container);
        }

        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            this.mContainers.remove(container);
            if (this.mContainers.size() != 0) {
                return false;
            }
            this.mRequest.cancel();
            return true;
        }
    }

    private void batchResponse(String cacheKey, BatchedImageRequest request) {
        this.mBatchedResponses.put(cacheKey, request);
        if (this.mRunnable == null) {
            C07274 r0 = new Runnable() {
                public void run() {
                    for (BatchedImageRequest bir : ImageLoader.this.mBatchedResponses.values()) {
                        Iterator i$ = bir.mContainers.iterator();
                        while (i$.hasNext()) {
                            ImageContainer container = (ImageContainer) i$.next();
                            if (container.mListener != null) {
                                if (bir.getError() == null) {
                                    Bitmap unused = container.mBitmap = bir.mResponseBitmap;
                                    container.mListener.onResponse(container, false);
                                } else {
                                    container.mListener.onErrorResponse(bir.getError());
                                }
                            }
                        }
                    }
                    ImageLoader.this.mBatchedResponses.clear();
                    Runnable unused2 = ImageLoader.this.mRunnable = null;
                }
            };
            this.mRunnable = r0;
            this.mHandler.postDelayed(r0, (long) this.mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }

    public static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth).append("#H").append(maxHeight).append(url).toString();
    }

    public static String getCacheKey(String url, int maxWidth, int maxHeight, ImageView.ScaleType scaleType) {
        if (scaleType == ImageView.ScaleType.CENTER_INSIDE) {
            return getCacheKey(url, maxWidth, maxHeight);
        }
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth).append("#H").append(maxHeight).append("#S").append(scaleType.ordinal()).append(url).toString();
    }

    public void setResources(Resources resources) {
        this.mResources = resources;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public void setContetResolver(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    public ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    public void setHeaders(ArrayMap<String, String> headers) {
        this.mHeaders = headers;
    }
}
