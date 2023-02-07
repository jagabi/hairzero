package com.android.volley.cache;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.android.volley.VolleyLog;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.ImageCache;

public class BitmapCache implements ImageCache {
    private static final float DEFAULT_MEM_CACHE_PERCENT = 0.15f;
    private static final String TAG = "BitmapCache";
    private LruCache<String, Bitmap> mMemoryCache;

    private BitmapCache(int memCacheSize) {
        init(memCacheSize);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.android.volley.cache.BitmapCache} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.android.volley.cache.BitmapCache getInstance(androidx.fragment.app.FragmentManager r3, java.lang.String r4, int r5) {
        /*
            r0 = 0
            r1 = 0
            if (r3 == 0) goto L_0x000f
            com.android.volley.cache.BitmapCache$RetainFragment r1 = getRetainFragment(r3, r4)
            java.lang.Object r2 = r1.getObject()
            r0 = r2
            com.android.volley.cache.BitmapCache r0 = (com.android.volley.cache.BitmapCache) r0
        L_0x000f:
            if (r0 != 0) goto L_0x001c
            com.android.volley.cache.BitmapCache r2 = new com.android.volley.cache.BitmapCache
            r2.<init>(r5)
            r0 = r2
            if (r1 == 0) goto L_0x001c
            r1.setObject(r0)
        L_0x001c:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.cache.BitmapCache.getInstance(androidx.fragment.app.FragmentManager, java.lang.String, int):com.android.volley.cache.BitmapCache");
    }

    public static BitmapCache getInstance(FragmentManager fragmentManager, int memCacheSize) {
        return getInstance(fragmentManager, TAG, memCacheSize);
    }

    public static BitmapCache getInstance(FragmentManager fragmentManager, float memCachePercent) {
        return getInstance(fragmentManager, calculateMemCacheSize(memCachePercent));
    }

    public static BitmapCache getInstance(FragmentManager fragmentManger) {
        return getInstance(fragmentManger, (float) DEFAULT_MEM_CACHE_PERCENT);
    }

    private void init(int memCacheSize) {
        VolleyLog.m79d(TAG, "Memory cache created (size = " + memCacheSize + "KB)");
        this.mMemoryCache = new LruCache<String, Bitmap>(memCacheSize) {
            /* access modifiers changed from: protected */
            public int sizeOf(String key, Bitmap bitmap) {
                int bitmapSize = BitmapCache.getBitmapSize(bitmap) / 1024;
                if (bitmapSize == 0) {
                    return 1;
                }
                return bitmapSize;
            }
        };
    }

    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data != null && bitmap != null) {
            synchronized (this.mMemoryCache) {
                if (this.mMemoryCache.get(data) == null) {
                    VolleyLog.m79d(TAG, "Memory cache put - " + data);
                    this.mMemoryCache.put(data, bitmap);
                }
            }
        }
    }

    public Bitmap getBitmapFromMemCache(String data) {
        if (data == null) {
            return null;
        }
        synchronized (this.mMemoryCache) {
            Bitmap memBitmap = this.mMemoryCache.get(data);
            if (memBitmap != null) {
                VolleyLog.m79d(TAG, "Memory cache hit - " + data);
                return memBitmap;
            }
            VolleyLog.m79d(TAG, "Memory cache miss - " + data);
            return null;
        }
    }

    public void clearCache() {
        LruCache<String, Bitmap> lruCache = this.mMemoryCache;
        if (lruCache != null) {
            lruCache.evictAll();
            VolleyLog.m79d(TAG, "Memory cache cleared");
        }
    }

    public static int calculateMemCacheSize(float percent) {
        if (percent >= 0.05f && percent <= 0.8f) {
            return Math.round((((float) Runtime.getRuntime().maxMemory()) * percent) / 1024.0f);
        }
        throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.05 and 0.8 (inclusive)");
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (Utils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    private static RetainFragment getRetainFragment(FragmentManager fm, String fragmentTag) {
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(fragmentTag);
        if (mRetainFragment != null) {
            return mRetainFragment;
        }
        RetainFragment mRetainFragment2 = new RetainFragment();
        fm.beginTransaction().add((Fragment) mRetainFragment2, fragmentTag).commitAllowingStateLoss();
        return mRetainFragment2;
    }

    public Bitmap getBitmap(String key) {
        return getBitmapFromMemCache(key);
    }

    public void putBitmap(String key, Bitmap bitmap) {
        addBitmapToCache(key, bitmap);
    }

    public void invalidateBitmap(String url) {
        if (url != null) {
            synchronized (this.mMemoryCache) {
                VolleyLog.m79d(TAG, "Memory cache remove - " + url);
                this.mMemoryCache.remove(url);
            }
        }
    }

    public void clear() {
        clearCache();
    }

    public static class RetainFragment extends Fragment {
        private Object mObject;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setObject(Object object) {
            this.mObject = object;
        }

        public Object getObject() {
            return this.mObject;
        }
    }
}
