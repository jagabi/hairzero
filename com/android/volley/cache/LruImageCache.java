package com.android.volley.cache;

import android.graphics.Bitmap;
import android.os.Build;
import androidx.collection.LruCache;
import com.android.volley.toolbox.ImageCache;

public class LruImageCache implements ImageCache {
    private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(((int) (Runtime.getRuntime().maxMemory() / 1024)) / 8) {
        /* access modifiers changed from: protected */
        public int sizeOf(String key, Bitmap bitmap) {
            if (Build.VERSION.SDK_INT >= 12) {
                return bitmap.getByteCount() / 1024;
            }
            return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
        }
    };

    public Bitmap getBitmap(String key) {
        return this.mLruCache.get(key);
    }

    public void putBitmap(String key, Bitmap bitmap) {
        this.mLruCache.put(key, bitmap);
    }

    public void invalidateBitmap(String url) {
        this.mLruCache.remove(url);
    }

    public void clear() {
        this.mLruCache.evictAll();
    }
}
