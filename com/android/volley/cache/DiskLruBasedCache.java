package com.android.volley.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.android.volley.Cache;
import com.android.volley.VolleyLog;
import com.android.volley.cache.DiskBasedCache;
import com.android.volley.misc.DiskLruCache;
import com.android.volley.misc.IOUtils;
import com.android.volley.misc.ImageUtils;
import com.android.volley.misc.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiskLruBasedCache implements Cache {
    private static final int APP_VERSION = 1;
    /* access modifiers changed from: private */
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final int DEFAULT_DISK_CACHE_SIZE = 10485760;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final int DEFAULT_MEM_CACHE_SIZE = 5120;
    private static final int DISK_CACHE_INDEX = 0;
    private static int IO_BUFFER_SIZE = 8192;
    private static final String TAG = "DiskLruImageCache";
    private static final int VALUE_COUNT = 1;
    private ImageCacheParams mCacheParams;
    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = 70;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private DiskLruCache mDiskLruCache;

    public DiskLruBasedCache(File root) {
        this.mCacheParams = new ImageCacheParams(root);
    }

    public DiskLruBasedCache(ImageCacheParams cacheParams) {
        this.mCacheParams = cacheParams;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void putBitmap(String data, Bitmap value) {
        if (data != null && value != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    String key = hashKeyForDisk(data);
                    OutputStream out = null;
                    try {
                        DiskLruCache.Snapshot snapshot = this.mDiskLruCache.get(key);
                        if (snapshot == null) {
                            DiskLruCache.Editor editor = this.mDiskLruCache.edit(key);
                            if (editor != null) {
                                out = editor.newOutputStream(0);
                                value.compress(this.mCacheParams.compressFormat, this.mCacheParams.compressQuality, out);
                                editor.commit();
                                out.close();
                            }
                        } else {
                            snapshot.getInputStream(0).close();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException e2) {
                        Log.e(TAG, "addBitmapToCache - " + e2);
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e3) {
                        try {
                            Log.e(TAG, "addBitmapToCache - " + e3);
                            if (out != null) {
                                out.close();
                            }
                        } catch (Throwable th) {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e4) {
                                }
                            }
                            throw th;
                        }
                    }
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public Bitmap getBitmap(String data) {
        String key = hashKeyForDisk(data);
        Bitmap bitmap = null;
        synchronized (this.mDiskCacheLock) {
            while (this.mDiskCacheStarting) {
                try {
                    this.mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            DiskLruCache diskLruCache = this.mDiskLruCache;
            if (diskLruCache != null) {
                InputStream inputStream = null;
                try {
                    DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
                    if (!(snapshot == null || (inputStream = snapshot.getInputStream(0)) == null)) {
                        bitmap = ImageUtils.decodeSampledBitmapFromDescriptor(((FileInputStream) inputStream).getFD(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                        }
                    }
                } catch (IOException e3) {
                    try {
                        Log.e(TAG, "getBitmapFromDiskCache - " + e3);
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Throwable th) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                }
            }
        }
        return bitmap;
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
            com.android.volley.misc.DiskLruCache r2 = r3.mDiskLruCache     // Catch:{ IOException -> 0x0017 }
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.cache.DiskLruBasedCache.containsKey(java.lang.String):boolean");
    }

    public void clearCache() {
        try {
            this.mDiskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return this.mDiskLruCache.getDirectory();
    }

    public void initDiskCache() {
        synchronized (this.mDiskCacheLock) {
            DiskLruCache diskLruCache = this.mDiskLruCache;
            if (diskLruCache == null || diskLruCache.isClosed()) {
                File diskCacheDir = this.mCacheParams.diskCacheDir;
                if (this.mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (Utils.getUsableSpace(diskCacheDir) > ((long) this.mCacheParams.diskCacheSize)) {
                        try {
                            this.mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, (long) this.mCacheParams.diskCacheSize);
                        } catch (IOException e) {
                            this.mCacheParams.diskCacheDir = null;
                            VolleyLog.m80e("initDiskCache - " + e, new Object[0]);
                        }
                    }
                }
            }
            this.mDiskCacheStarting = false;
            this.mDiskCacheLock.notifyAll();
        }
    }

    public static class ImageCacheParams {
        public Bitmap.CompressFormat compressFormat = DiskLruBasedCache.DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = 70;
        public File diskCacheDir;
        public boolean diskCacheEnabled = true;
        public int diskCacheSize = DiskLruBasedCache.DEFAULT_DISK_CACHE_SIZE;
        public boolean initDiskCacheOnCreate = false;
        public int memCacheSize = DiskLruBasedCache.DEFAULT_MEM_CACHE_SIZE;
        public boolean memoryCacheEnabled = true;

        public ImageCacheParams(File rootDirectory, int maxCacheSizeInBytes) {
            this.diskCacheDir = rootDirectory;
            this.memCacheSize = maxCacheSizeInBytes;
        }

        public ImageCacheParams(Context context, String rootDirectory, int maxCacheSizeInBytes) {
            this.diskCacheDir = Utils.getDiskCacheDir(context, rootDirectory);
            this.memCacheSize = maxCacheSizeInBytes;
        }

        public ImageCacheParams(Context context, String rootDirectory) {
            this.diskCacheDir = Utils.getDiskCacheDir(context, rootDirectory);
        }

        public ImageCacheParams(File rootDirectory) {
            this.diskCacheDir = rootDirectory;
        }

        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be between 0.01 and 0.8 (inclusive)");
            }
            this.memCacheSize = Math.round((((float) Runtime.getRuntime().maxMemory()) * percent) / 1024.0f);
        }
    }

    public static String hashKeyForDisk(String key) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            return bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(key.hashCode());
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        return String.valueOf(key.substring(0, firstHalfLength).hashCode()) + String.valueOf(key.substring(firstHalfLength).hashCode());
    }

    public File getFileForKey(String key) {
        return new File(this.mCacheParams.diskCacheDir, key + ".0");
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public Cache.Entry get(String data) {
        String key = hashKeyForDisk(data);
        if (data == null) {
            return null;
        }
        synchronized (this.mDiskCacheLock) {
            while (this.mDiskCacheStarting) {
                try {
                    this.mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (this.mDiskLruCache != null) {
                InputStream inputStream = null;
                File file = getFileForKey(key);
                try {
                    DiskLruCache.Snapshot snapshot = this.mDiskLruCache.get(key);
                    if (snapshot != null && (inputStream = snapshot.getInputStream(0)) != null) {
                        IOUtils.CountingInputStream cis = new IOUtils.CountingInputStream(inputStream);
                        Cache.Entry cacheEntry = DiskBasedCache.CacheHeader.readHeader(cis).toCacheEntry(IOUtils.streamToBytes(cis, (int) (file.length() - cis.getBytesRead())));
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e2) {
                            }
                        }
                        return cacheEntry;
                    } else if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (IOException e4) {
                    remove(key);
                    Log.e(TAG, "getDiskLruBasedCache - " + e4);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                        }
                    }
                    return null;
                } catch (OutOfMemoryError e6) {
                    try {
                        VolleyLog.m80e("Caught OOM for %d byte image, path=%s: %s", Long.valueOf(file.length()), file.getAbsolutePath(), e6.toString());
                        return null;
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e7) {
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void put(String data, Cache.Entry value) {
        if (data != null && value != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    String key = hashKeyForDisk(data);
                    OutputStream out = null;
                    try {
                        DiskLruCache.Editor editor = this.mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(0);
                            new DiskBasedCache.CacheHeader(key, value).writeHeader(out);
                            out.write(value.data);
                            editor.commit();
                            out.close();
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException e2) {
                        Log.e(TAG, "putDiskLruBasedCache - " + e2);
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e3) {
                        try {
                            Log.e(TAG, "putDiskLruBasedCache - " + e3);
                            if (out != null) {
                                out.close();
                            }
                        } catch (Throwable th) {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e4) {
                                }
                            }
                            throw th;
                        }
                    }
                }
            }
        }
    }

    public void initialize() {
        initDiskCache();
    }

    public void invalidate(String key, boolean fullExpire) {
        Cache.Entry entry = get(key);
        if (entry != null) {
            entry.softTtl = -1;
            if (fullExpire) {
                entry.ttl = -1;
            }
            put(key, entry);
        }
    }

    public void remove(String data) {
        if (data != null) {
            synchronized (this.mDiskCacheLock) {
                if (this.mDiskLruCache != null) {
                    try {
                        this.mDiskLruCache.remove(hashKeyForDisk(data));
                    } catch (IOException e) {
                        Log.e(TAG, "removeDiskLruBasedCache - " + e);
                    } catch (Exception e2) {
                        Log.e(TAG, "removeDiskLruBasedCache - " + e2);
                    }
                }
            }
        }
    }

    public void clear() {
        synchronized (this.mDiskCacheLock) {
            this.mDiskCacheStarting = true;
            DiskLruCache diskLruCache = this.mDiskLruCache;
            if (diskLruCache != null && !diskLruCache.isClosed()) {
                try {
                    this.mDiskLruCache.delete();
                } catch (IOException e) {
                    Log.e(TAG, "clearCache - " + e);
                }
                this.mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    public void flush() {
        synchronized (this.mDiskCacheLock) {
            DiskLruCache diskLruCache = this.mDiskLruCache;
            if (diskLruCache != null) {
                try {
                    diskLruCache.flush();
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    public void close() {
        synchronized (this.mDiskCacheLock) {
            DiskLruCache diskLruCache = this.mDiskLruCache;
            if (diskLruCache != null) {
                try {
                    if (!diskLruCache.isClosed()) {
                        this.mDiskLruCache.close();
                        this.mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }
}
