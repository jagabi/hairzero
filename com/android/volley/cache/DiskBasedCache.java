package com.android.volley.cache;

import android.os.SystemClock;
import com.android.volley.Cache;
import com.android.volley.VolleyLog;
import com.android.volley.misc.IOUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class DiskBasedCache implements Cache {
    private static final int CACHE_MAGIC = 538247942;
    private static final int DEFAULT_DISK_USAGE_BYTES = 5242880;
    private static final float HYSTERESIS_FACTOR = 0.9f;
    private final int CACHE_LOAD_THREADS;
    private final CacheContainer mEntries;
    private final int mMaxCacheSizeInBytes;
    /* access modifiers changed from: private */
    public final File mRootDirectory;

    public DiskBasedCache(File rootDirectory, int maxCacheSizeInBytes) {
        this.CACHE_LOAD_THREADS = 2;
        this.mEntries = new CacheContainer();
        this.mRootDirectory = rootDirectory;
        this.mMaxCacheSizeInBytes = maxCacheSizeInBytes;
    }

    public DiskBasedCache(File rootDirectory) {
        this(rootDirectory, DEFAULT_DISK_USAGE_BYTES);
    }

    public synchronized void clear() {
        File[] files = this.mRootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        this.mEntries.clear();
        VolleyLog.m79d("Cache cleared.", new Object[0]);
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public synchronized Cache.Entry get(String key) {
        CacheHeader entry = this.mEntries.get((Object) key);
        if (entry == null) {
            return null;
        }
        File file = getFileForKey(key);
        IOUtils.CountingInputStream cis = null;
        try {
            IOUtils.CountingInputStream cis2 = new IOUtils.CountingInputStream(new BufferedInputStream(new FileInputStream(file)));
            CacheHeader.readHeader(cis2);
            Cache.Entry cacheEntry = entry.toCacheEntry(IOUtils.streamToBytes(cis2, (int) (file.length() - cis2.getBytesRead())));
            try {
                cis2.close();
                return cacheEntry;
            } catch (IOException e) {
                return null;
            }
        } catch (IOException e2) {
            VolleyLog.m79d("%s: %s", file.getAbsolutePath(), e2.toString());
            remove(key);
            if (cis != null) {
                try {
                    cis.close();
                } catch (IOException e3) {
                    return null;
                }
            }
            return null;
        } catch (OutOfMemoryError e4) {
            try {
                VolleyLog.m80e("Caught OOM for %d byte image, path=%s: %s", Long.valueOf(file.length()), file.getAbsolutePath(), e4.toString());
                return null;
            } finally {
                if (cis != null) {
                    try {
                        cis.close();
                    } catch (IOException e5) {
                        return null;
                    }
                }
            }
        }
    }

    public synchronized void initialize() {
        this.mEntries.initialize();
    }

    public synchronized void invalidate(String key, boolean fullExpire) {
        Cache.Entry entry = get(key);
        if (entry != null) {
            entry.softTtl = -1;
            if (fullExpire) {
                entry.ttl = -1;
            }
            put(key, entry);
        }
    }

    public synchronized void put(String key, Cache.Entry entry) {
        pruneIfNeeded(entry.data.length);
        File file = getFileForKey(key);
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
            CacheHeader e = new CacheHeader(key, entry);
            if (e.writeHeader(fos)) {
                fos.write(entry.data);
                fos.close();
                putEntry(key, e);
            } else {
                fos.close();
                VolleyLog.m79d("Failed to write header for %s", file.getAbsolutePath());
                throw new IOException();
            }
        } catch (IOException e2) {
            if (!file.delete()) {
                VolleyLog.m79d("Could not clean up file %s", file.getAbsolutePath());
            }
        }
    }

    public synchronized void remove(String key) {
        boolean deleted = getFileForKey(key).delete();
        removeEntry(key);
        if (!deleted) {
            VolleyLog.m79d("Could not delete cache entry for key=%s, filename=%s", key, getFilenameForKey(key));
        }
    }

    /* access modifiers changed from: private */
    public String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        return String.valueOf(key.substring(0, firstHalfLength).hashCode()) + String.valueOf(key.substring(firstHalfLength).hashCode());
    }

    public File getFileForKey(String key) {
        return new File(this.mRootDirectory, getFilenameForKey(key));
    }

    private void pruneIfNeeded(int neededSpace) {
        int i = neededSpace;
        if (this.mEntries.isLoaded() && this.mEntries.getTotalSize() + ((long) i) >= ((long) this.mMaxCacheSizeInBytes)) {
            if (VolleyLog.DEBUG) {
                VolleyLog.m82v("Pruning old cache entries.", new Object[0]);
            }
            long before = this.mEntries.getTotalSize();
            int prunedFiles = 0;
            long startTime = SystemClock.elapsedRealtime();
            Iterator<Map.Entry<String, CacheHeader>> iterator = this.mEntries.entrySet().iterator();
            while (iterator.hasNext()) {
                CacheHeader e = iterator.next().getValue();
                if (!getFileForKey(e.key).delete()) {
                    VolleyLog.m79d("Could not delete cache entry for key=%s, filename=%s", e.key, getFilenameForKey(e.key));
                }
                iterator.remove();
                prunedFiles++;
                if (((float) (this.mEntries.getTotalSize() + ((long) i))) < ((float) this.mMaxCacheSizeInBytes) * HYSTERESIS_FACTOR) {
                    break;
                }
            }
            if (VolleyLog.DEBUG) {
                VolleyLog.m82v("pruned %d files, %d bytes, %d ms", Integer.valueOf(prunedFiles), Long.valueOf(this.mEntries.getTotalSize() - before), Long.valueOf(SystemClock.elapsedRealtime() - startTime));
            }
        }
    }

    private void putEntry(String key, CacheHeader entry) {
        this.mEntries.put(key, entry);
    }

    private void removeEntry(String key) {
        if (this.mEntries.get((Object) key) != null) {
            this.mEntries.remove((Object) key);
        }
    }

    private class CacheContainer extends ConcurrentHashMap<String, CacheHeader> {
        private boolean mInitialized = false;
        /* access modifiers changed from: private */
        public final Map<String, Future<CacheHeader>> mLoadingFiles = new ConcurrentHashMap();
        /* access modifiers changed from: private */
        public final PriorityBlockingQueue<Runnable> mQueue = new PriorityBlockingQueue<>();
        /* access modifiers changed from: private */
        public AtomicLong mTotalSize = new AtomicLong(0);

        public CacheContainer() {
            super(16, 0.75f, 2);
        }

        /* Debug info: failed to restart local var, previous not found, register: 12 */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0037, code lost:
            return;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public synchronized void initialize() {
            /*
                r12 = this;
                monitor-enter(r12)
                boolean r0 = r12.mInitialized     // Catch:{ all -> 0x0084 }
                if (r0 == 0) goto L_0x0007
                monitor-exit(r12)
                return
            L_0x0007:
                r0 = 1
                r12.mInitialized = r0     // Catch:{ all -> 0x0084 }
                com.android.volley.cache.DiskBasedCache r1 = com.android.volley.cache.DiskBasedCache.this     // Catch:{ all -> 0x0084 }
                java.io.File r1 = r1.mRootDirectory     // Catch:{ all -> 0x0084 }
                boolean r1 = r1.exists()     // Catch:{ all -> 0x0084 }
                r2 = 0
                if (r1 != 0) goto L_0x0038
                com.android.volley.cache.DiskBasedCache r1 = com.android.volley.cache.DiskBasedCache.this     // Catch:{ all -> 0x0084 }
                java.io.File r1 = r1.mRootDirectory     // Catch:{ all -> 0x0084 }
                boolean r1 = r1.mkdirs()     // Catch:{ all -> 0x0084 }
                if (r1 != 0) goto L_0x0036
                java.lang.String r1 = "Unable to create cache dir %s"
                java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ all -> 0x0084 }
                com.android.volley.cache.DiskBasedCache r3 = com.android.volley.cache.DiskBasedCache.this     // Catch:{ all -> 0x0084 }
                java.io.File r3 = r3.mRootDirectory     // Catch:{ all -> 0x0084 }
                java.lang.String r3 = r3.getAbsolutePath()     // Catch:{ all -> 0x0084 }
                r0[r2] = r3     // Catch:{ all -> 0x0084 }
                com.android.volley.VolleyLog.m80e(r1, r0)     // Catch:{ all -> 0x0084 }
            L_0x0036:
                monitor-exit(r12)
                return
            L_0x0038:
                com.android.volley.cache.DiskBasedCache r1 = com.android.volley.cache.DiskBasedCache.this     // Catch:{ all -> 0x0084 }
                java.io.File r1 = r1.mRootDirectory     // Catch:{ all -> 0x0084 }
                java.io.File[] r1 = r1.listFiles()     // Catch:{ all -> 0x0084 }
                if (r1 != 0) goto L_0x0046
                monitor-exit(r12)
                return
            L_0x0046:
                java.lang.String r3 = "Loading %d files from cache"
                java.lang.Object[] r0 = new java.lang.Object[r0]     // Catch:{ all -> 0x0084 }
                int r4 = r1.length     // Catch:{ all -> 0x0084 }
                java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ all -> 0x0084 }
                r0[r2] = r4     // Catch:{ all -> 0x0084 }
                com.android.volley.VolleyLog.m79d(r3, r0)     // Catch:{ all -> 0x0084 }
                java.util.concurrent.ThreadPoolExecutor r0 = new java.util.concurrent.ThreadPoolExecutor     // Catch:{ all -> 0x0084 }
                r6 = 2
                r7 = 2
                r8 = 10
                java.util.concurrent.TimeUnit r10 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x0084 }
                java.util.concurrent.PriorityBlockingQueue<java.lang.Runnable> r11 = r12.mQueue     // Catch:{ all -> 0x0084 }
                r5 = r0
                r5.<init>(r6, r7, r8, r10, r11)     // Catch:{ all -> 0x0084 }
                r2 = r1
                int r3 = r2.length     // Catch:{ all -> 0x0084 }
                r4 = 0
            L_0x0065:
                if (r4 >= r3) goto L_0x0082
                r5 = r2[r4]     // Catch:{ all -> 0x0084 }
                com.android.volley.cache.DiskBasedCache$CacheContainer$HeaderParserCallable r6 = new com.android.volley.cache.DiskBasedCache$CacheContainer$HeaderParserCallable     // Catch:{ all -> 0x0084 }
                r6.<init>(r5)     // Catch:{ all -> 0x0084 }
                com.android.volley.cache.DiskBasedCache$CacheContainer$ReorderingFutureTask r7 = new com.android.volley.cache.DiskBasedCache$CacheContainer$ReorderingFutureTask     // Catch:{ all -> 0x0084 }
                r7.<init>(r6)     // Catch:{ all -> 0x0084 }
                java.util.Map<java.lang.String, java.util.concurrent.Future<com.android.volley.cache.DiskBasedCache$CacheHeader>> r8 = r12.mLoadingFiles     // Catch:{ all -> 0x0084 }
                java.lang.String r9 = r5.getName()     // Catch:{ all -> 0x0084 }
                r8.put(r9, r7)     // Catch:{ all -> 0x0084 }
                r0.execute(r7)     // Catch:{ all -> 0x0084 }
                int r4 = r4 + 1
                goto L_0x0065
            L_0x0082:
                monitor-exit(r12)
                return
            L_0x0084:
                r0 = move-exception
                monitor-exit(r12)
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.volley.cache.DiskBasedCache.CacheContainer.initialize():void");
        }

        private class ReorderingFutureTask extends FutureTask<CacheHeader> implements Comparable<ReorderingFutureTask> {
            private int mGetRequests = 0;

            public ReorderingFutureTask(Callable<CacheHeader> callable) {
                super(callable);
            }

            public CacheHeader get() throws InterruptedException, ExecutionException {
                this.mGetRequests++;
                if (CacheContainer.this.mQueue.contains(this)) {
                    CacheContainer.this.mQueue.remove(this);
                    CacheContainer.this.mQueue.add(this);
                }
                return (CacheHeader) super.get();
            }

            public int compareTo(ReorderingFutureTask another) {
                int i = this.mGetRequests;
                int i2 = another.mGetRequests;
                if (i > i2) {
                    return -1;
                }
                return i < i2 ? 1 : 0;
            }
        }

        private class HeaderParserCallable implements Callable<CacheHeader> {
            private final File file;

            public HeaderParserCallable(File file2) {
                this.file = file2;
            }

            public CacheHeader call() throws Exception {
                BufferedInputStream fis = null;
                try {
                    fis = new BufferedInputStream(new FileInputStream(this.file));
                    CacheHeader entry = CacheHeader.readHeader(fis);
                    entry.size = this.file.length();
                    Object unused = CacheContainer.super.put(entry.key, entry);
                    CacheContainer.this.mTotalSize.getAndAdd(entry.size);
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                    CacheContainer.this.mLoadingFiles.remove(this.file.getName());
                    return entry;
                } catch (IOException e2) {
                    File file2 = this.file;
                    if (file2 != null) {
                        file2.delete();
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e3) {
                        }
                    }
                    CacheContainer.this.mLoadingFiles.remove(this.file.getName());
                    return null;
                } catch (Throwable th) {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e4) {
                        }
                    }
                    CacheContainer.this.mLoadingFiles.remove(this.file.getName());
                    throw th;
                }
            }
        }

        private void waitForCache() {
            while (this.mLoadingFiles.size() > 0) {
                Iterator<Map.Entry<String, Future<CacheHeader>>> iterator = this.mLoadingFiles.entrySet().iterator();
                if (iterator.hasNext()) {
                    try {
                        iterator.next().getValue().get();
                    } catch (InterruptedException | ExecutionException e) {
                    }
                }
            }
        }

        private void waitForKey(Object key) {
            Future<CacheHeader> future;
            if (!isLoaded() && (future = this.mLoadingFiles.get(DiskBasedCache.this.getFilenameForKey((String) key))) != null) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                }
            }
        }

        public boolean isLoaded() {
            return this.mLoadingFiles.size() == 0;
        }

        public long getTotalSize() {
            return this.mTotalSize.get();
        }

        public CacheHeader get(Object key) {
            waitForKey(key);
            return (CacheHeader) super.get(key);
        }

        public boolean containsKey(Object key) {
            waitForKey(key);
            return super.containsKey(key);
        }

        public CacheHeader put(String key, CacheHeader entry) {
            waitForKey(key);
            if (super.containsKey(key)) {
                this.mTotalSize.getAndAdd(entry.size - ((CacheHeader) super.get(key)).size);
            } else {
                this.mTotalSize.getAndAdd(entry.size);
            }
            return (CacheHeader) super.put(key, entry);
        }

        public CacheHeader remove(Object key) {
            waitForKey(key);
            if (super.containsKey(key)) {
                this.mTotalSize.getAndAdd(((CacheHeader) super.get(key)).size * -1);
            }
            return (CacheHeader) super.remove(key);
        }

        public void clear() {
            waitForCache();
            this.mTotalSize.getAndSet(0);
            super.clear();
        }
    }

    static class CacheHeader {
        public String etag;
        public String key;
        public long lastModified;
        public Map<String, String> responseHeaders;
        public long serverDate;
        public long size;
        public long softTtl;
        public long ttl;

        private CacheHeader() {
        }

        public CacheHeader(String key2, Cache.Entry entry) {
            this.key = key2;
            this.size = (long) entry.data.length;
            this.etag = entry.etag;
            this.serverDate = entry.serverDate;
            this.lastModified = entry.lastModified;
            this.ttl = entry.ttl;
            this.softTtl = entry.softTtl;
            this.responseHeaders = entry.responseHeaders;
        }

        public static CacheHeader readHeader(InputStream is) throws IOException {
            CacheHeader entry = new CacheHeader();
            if (IOUtils.readInt(is) == DiskBasedCache.CACHE_MAGIC) {
                entry.key = IOUtils.readString(is);
                String readString = IOUtils.readString(is);
                entry.etag = readString;
                if (readString.equals("")) {
                    entry.etag = null;
                }
                entry.serverDate = IOUtils.readLong(is);
                entry.lastModified = IOUtils.readLong(is);
                entry.ttl = IOUtils.readLong(is);
                entry.softTtl = IOUtils.readLong(is);
                entry.responseHeaders = IOUtils.readStringStringMap(is);
                return entry;
            }
            throw new IOException();
        }

        public Cache.Entry toCacheEntry(byte[] data) {
            Cache.Entry e = new Cache.Entry();
            e.data = data;
            e.etag = this.etag;
            e.serverDate = this.serverDate;
            e.lastModified = this.lastModified;
            e.ttl = this.ttl;
            e.softTtl = this.softTtl;
            e.responseHeaders = this.responseHeaders;
            return e;
        }

        public boolean writeHeader(OutputStream os) {
            try {
                IOUtils.writeInt(os, DiskBasedCache.CACHE_MAGIC);
                IOUtils.writeString(os, this.key);
                String str = this.etag;
                if (str == null) {
                    str = "";
                }
                IOUtils.writeString(os, str);
                IOUtils.writeLong(os, this.serverDate);
                IOUtils.writeLong(os, this.lastModified);
                IOUtils.writeLong(os, this.ttl);
                IOUtils.writeLong(os, this.softTtl);
                IOUtils.writeStringStringMap(this.responseHeaders, os);
                os.flush();
                return true;
            } catch (IOException e) {
                VolleyLog.m79d("%s", e.toString());
                return false;
            }
        }
    }

    public void flush() {
    }

    public void close() {
    }
}
