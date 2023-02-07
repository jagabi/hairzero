package com.android.volley.misc;

import android.os.Build;

public abstract class Trace {
    public static void beginSection(String tag) {
        if (Build.VERSION.SDK_INT >= 18) {
            android.os.Trace.beginSection(tag);
        }
    }

    public static void endSection() {
        if (Build.VERSION.SDK_INT >= 18) {
            android.os.Trace.endSection();
        }
    }
}
