package com.android.volley.cache.plus;

import android.graphics.drawable.BitmapDrawable;

public interface ImageCache {
    void clear();

    BitmapDrawable getBitmap(String str);

    void invalidateBitmap(String str);

    void putBitmap(String str, BitmapDrawable bitmapDrawable);
}
