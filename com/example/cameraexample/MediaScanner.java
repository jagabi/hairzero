package com.example.cameraexample;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;

public class MediaScanner {
    private static volatile MediaScanner mMediaInstance = null;
    private Context mContext;
    /* access modifiers changed from: private */
    public String mFilePath = "";
    /* access modifiers changed from: private */
    public MediaScannerConnection mMediaScanner = new MediaScannerConnection(this.mContext, new MediaScannerConnection.MediaScannerConnectionClient() {
        public void onMediaScannerConnected() {
            MediaScanner.this.mMediaScanner.scanFile(MediaScanner.this.mFilePath, (String) null);
        }

        public void onScanCompleted(String path, Uri uri) {
            System.out.println("::::MediaScan Success::::");
            MediaScanner.this.mMediaScanner.disconnect();
        }
    });

    public static MediaScanner getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (mMediaInstance == null) {
            mMediaInstance = new MediaScanner(context);
        }
        return mMediaInstance;
    }

    public static void releaseInstance() {
        if (mMediaInstance != null) {
            mMediaInstance = null;
        }
    }

    private MediaScanner(Context context) {
        this.mContext = context;
    }

    public void mediaScanning(String path) {
        if (!TextUtils.isEmpty(path)) {
            this.mFilePath = path;
            if (!this.mMediaScanner.isConnected()) {
                this.mMediaScanner.connect();
            }
        }
    }
}
