package com.android.volley.misc;

import android.graphics.drawable.Drawable;
import android.view.View;

public class ViewCompat {
    public static void setBackground(View view, Drawable drawable) {
        if (Utils.hasJellyBean()) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
