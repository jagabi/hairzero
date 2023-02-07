package com.android.volley.toolbox;

import android.graphics.Bitmap;

public interface ImageCache {
    void clear();

    Bitmap getBitmap(String str);

    void invalidateBitmap(String str);

    void putBitmap(String str, Bitmap bitmap);
}
