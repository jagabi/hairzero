package com.android.volley.cache;

import android.content.Context;
import android.graphics.Bitmap;
import com.android.volley.misc.DiskLruCache;
import com.android.volley.toolbox.ImageCache;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class DiskLruImageCache implements ImageCache {
    private static final int APP_VERSION = 1;
    private static int IO_BUFFER_SIZE = 8192;
    private static final int VALUE_COUNT = 1;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private int mCompressQuality = 70;
    private DiskLruCache mDiskCache;

    public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize, Bitmap.CompressFormat compressFormat, int quality) {
        try {
            this.mDiskCache = DiskLruCache.open(getDiskCacheDir(context, uniqueName), 1, 1, (long) diskCacheSize);
            this.mCompressFormat = compressFormat;
            this.mCompressQuality = quality;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            OutputStream out2 = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            boolean compress = bitmap.compress(this.mCompressFormat, this.mCompressQuality, out2);
            out2.close();
            return compress;
        } catch (Throwable th) {
            if (out != null) {
                out.close();
            }
            throw th;
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        return new File(context.getCacheDir().getPath() + File.separator + uniqueName);
    }

    public void putBitmap(String key, Bitmap data) {
        DiskLruCache.Editor editor = null;
        try {
            DiskLruCache.Editor editor2 = this.mDiskCache.edit(key);
            if (editor2 != null) {
                if (writeBitmapToFile(data, editor2)) {
                    this.mDiskCache.flush();
                    editor2.commit();
                    return;
                }
                editor2.abort();
            }
        } catch (IOException e) {
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException e2) {
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        if (r1 != null) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0027, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
        if (r1 == null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0034, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.Bitmap getBitmap(java.lang.String r6) {
        /*
            r5 = this;
            r0 = 0
            r1 = 0
            com.android.volley.misc.DiskLruCache r2 = r5.mDiskCache     // Catch:{ IOException -> 0x002d }
            com.android.volley.misc.DiskLruCache$Snapshot r2 = r2.get(r6)     // Catch:{ IOException -> 0x002d }
            r1 = r2
            if (r1 != 0) goto L_0x0012
            r2 = 0
            if (r1 == 0) goto L_0x0011
            r1.close()
        L_0x0011:
            return r2
        L_0x0012:
            r2 = 0
            java.io.InputStream r2 = r1.getInputStream(r2)     // Catch:{ IOException -> 0x002d }
            if (r2 == 0) goto L_0x0025
            java.io.BufferedInputStream r3 = new java.io.BufferedInputStream     // Catch:{ IOException -> 0x002d }
            int r4 = IO_BUFFER_SIZE     // Catch:{ IOException -> 0x002d }
            r3.<init>(r2, r4)     // Catch:{ IOException -> 0x002d }
            android.graphics.Bitmap r4 = android.graphics.BitmapFactory.decodeStream(r3)     // Catch:{ IOException -> 0x002d }
            r0 = r4
        L_0x0025:
            if (r1 == 0) goto L_0x0034
        L_0x0027:
            r1.close()
            goto L_0x0034
        L_0x002b:
            r2 = move-exception
            goto L_0x0035
        L_0x002d:
            r2 = move-exception
            r2.printStackTrace()     // Catch:{ all -> 0x002b }
            if (r1 == 0) goto L_0x0034
            goto L_0x0027
        L_0x0034:
            return r0
        L_0x0035:
            if (r1 == 0) goto L_0x003a
            r1.close()
        L_0x003a:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.cache.DiskLruImageCache.getBitmap(java.lang.String):android.graphics.Bitmap");
    }

    public void invalidateBitmap(String url) {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001b, code lost:
        if (r1 == null) goto L_0x001e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001e, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000f, code lost:
        if (r1 != null) goto L_0x0011;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        r1.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean containsKey(java.lang.String r4) {
        /*
            r3 = this;
            r0 = 0
            r1 = 0
            com.android.volley.misc.DiskLruCache r2 = r3.mDiskCache     // Catch:{ IOException -> 0x0017 }
            com.android.volley.misc.DiskLruCache$Snapshot r2 = r2.get(r4)     // Catch:{ IOException -> 0x0017 }
            r1 = r2
            if (r1 == 0) goto L_0x000d
            r2 = 1
            goto L_0x000e
        L_0x000d:
            r2 = 0
        L_0x000e:
            r0 = r2
            if (r1 == 0) goto L_0x001e
        L_0x0011:
            r1.close()
            goto L_0x001e
        L_0x0015:
            r2 = move-exception
            goto L_0x001f
        L_0x0017:
            r2 = move-exception
            r2.printStackTrace()     // Catch:{ all -> 0x0015 }
            if (r1 == 0) goto L_0x001e
            goto L_0x0011
        L_0x001e:
            return r0
        L_0x001f:
            if (r1 == 0) goto L_0x0024
            r1.close()
        L_0x0024:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.cache.DiskLruImageCache.containsKey(java.lang.String):boolean");
    }

    public void clearCache() {
        try {
            this.mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return this.mDiskCache.getDirectory();
    }

    public void clear() {
        clearCache();
    }
}
