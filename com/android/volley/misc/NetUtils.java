package com.android.volley.misc;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class NetUtils {
    private static final String TAG = "NetUtils";
    private static String mUserAgent = null;

    public static String getUserAgent(Context mContext) {
        if (mUserAgent == null) {
            mUserAgent = "volley/0";
            try {
                String packageName = mContext.getPackageName();
                mUserAgent = packageName + "/" + mContext.getPackageManager().getPackageInfo(packageName, 0).versionCode;
                Log.d(TAG, "User agent set to: " + mUserAgent);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Unable to find self by package name", e);
            }
        }
        return mUserAgent;
    }
}
