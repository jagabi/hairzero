package com.liulishuo.okdownload;

import android.net.Uri;
import android.util.SparseArray;
import com.liulishuo.okdownload.core.IdentifiedTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.download.DownloadStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadTask extends IdentifiedTask implements Comparable<DownloadTask> {
    private final boolean autoCallbackToUIThread;
    private final Integer connectionCount;
    private final File directoryFile;
    private final boolean filenameFromResponse;
    private final DownloadStrategy.FilenameHolder filenameHolder;
    private final int flushBufferSize;
    private final Map<String, List<String>> headerMapFields;

    /* renamed from: id */
    private final int f188id;
    private BreakpointInfo info;
    private final Boolean isPreAllocateLength;
    private volatile SparseArray<Object> keyTagMap;
    private final AtomicLong lastCallbackProcessTimestamp = new AtomicLong();
    private volatile DownloadListener listener;
    private final int minIntervalMillisCallbackProcess;
    private final boolean passIfAlreadyCompleted;
    private final int priority;
    /* access modifiers changed from: private */
    public final File providedPathFile;
    private final int readBufferSize;
    private String redirectLocation;
    private final int syncBufferIntervalMills;
    private final int syncBufferSize;
    private Object tag;
    private File targetFile;
    private final Uri uri;
    /* access modifiers changed from: private */
    public final String url;
    private final boolean wifiRequired;

    public DownloadTask(String url2, Uri uri2, int priority2, int readBufferSize2, int flushBufferSize2, int syncBufferSize2, int syncBufferIntervalMills2, boolean autoCallbackToUIThread2, int minIntervalMillisCallbackProcess2, Map<String, List<String>> headerMapFields2, String filename, boolean passIfAlreadyCompleted2, boolean wifiRequired2, Boolean filenameFromResponse2, Integer connectionCount2, Boolean isPreAllocateLength2) {
        Boolean filenameFromResponse3;
        String filename2 = filename;
        this.url = url2;
        this.uri = uri2;
        this.priority = priority2;
        this.readBufferSize = readBufferSize2;
        this.flushBufferSize = flushBufferSize2;
        this.syncBufferSize = syncBufferSize2;
        this.syncBufferIntervalMills = syncBufferIntervalMills2;
        this.autoCallbackToUIThread = autoCallbackToUIThread2;
        this.minIntervalMillisCallbackProcess = minIntervalMillisCallbackProcess2;
        this.headerMapFields = headerMapFields2;
        this.passIfAlreadyCompleted = passIfAlreadyCompleted2;
        this.wifiRequired = wifiRequired2;
        this.connectionCount = connectionCount2;
        this.isPreAllocateLength = isPreAllocateLength2;
        if (Util.isUriFileScheme(uri2)) {
            File file = new File(uri2.getPath());
            if (filenameFromResponse2 != null) {
                if (filenameFromResponse2.booleanValue()) {
                    if (!file.exists() || !file.isFile()) {
                        if (!Util.isEmpty(filename)) {
                            Util.m86w("DownloadTask", "Discard filename[" + filename2 + "] because you set filenameFromResponse=true");
                            filename2 = null;
                        }
                        this.directoryFile = file;
                        filenameFromResponse3 = filenameFromResponse2;
                    } else {
                        throw new IllegalArgumentException("If you want filename from response please make sure you provide path is directory " + file.getPath());
                    }
                } else if (file.exists() && file.isDirectory() && Util.isEmpty(filename)) {
                    throw new IllegalArgumentException("If you don't want filename from response please make sure you have already provided valid filename or not directory path " + file.getPath());
                } else if (Util.isEmpty(filename)) {
                    filename2 = file.getName();
                    this.directoryFile = Util.getParentFile(file);
                    filenameFromResponse3 = filenameFromResponse2;
                } else {
                    this.directoryFile = file;
                    filenameFromResponse3 = filenameFromResponse2;
                }
            } else if (!file.exists() || !file.isDirectory()) {
                filenameFromResponse3 = false;
                if (file.exists()) {
                    if (Util.isEmpty(filename) || file.getName().equals(filename2)) {
                        filename2 = file.getName();
                        this.directoryFile = Util.getParentFile(file);
                    } else {
                        throw new IllegalArgumentException("Uri already provided filename!");
                    }
                } else if (Util.isEmpty(filename)) {
                    filename2 = file.getName();
                    this.directoryFile = Util.getParentFile(file);
                } else {
                    this.directoryFile = file;
                }
            } else {
                filenameFromResponse3 = true;
                this.directoryFile = file;
            }
            this.filenameFromResponse = filenameFromResponse3.booleanValue();
        } else {
            this.filenameFromResponse = false;
            this.directoryFile = new File(uri2.getPath());
            Boolean bool = filenameFromResponse2;
        }
        if (Util.isEmpty(filename2)) {
            this.filenameHolder = new DownloadStrategy.FilenameHolder();
            this.providedPathFile = this.directoryFile;
        } else {
            this.filenameHolder = new DownloadStrategy.FilenameHolder(filename2);
            File file2 = new File(this.directoryFile, filename2);
            this.targetFile = file2;
            this.providedPathFile = file2;
        }
        this.f188id = OkDownload.with().breakpointStore().findOrCreateId(this);
    }

    public boolean isFilenameFromResponse() {
        return this.filenameFromResponse;
    }

    public Map<String, List<String>> getHeaderMapFields() {
        return this.headerMapFields;
    }

    public int getId() {
        return this.f188id;
    }

    public String getFilename() {
        return this.filenameHolder.get();
    }

    public boolean isPassIfAlreadyCompleted() {
        return this.passIfAlreadyCompleted;
    }

    public boolean isWifiRequired() {
        return this.wifiRequired;
    }

    public DownloadStrategy.FilenameHolder getFilenameHolder() {
        return this.filenameHolder;
    }

    public Uri getUri() {
        return this.uri;
    }

    public String getUrl() {
        return this.url;
    }

    public void setRedirectLocation(String redirectUrl) {
        this.redirectLocation = redirectUrl;
    }

    public String getRedirectLocation() {
        return this.redirectLocation;
    }

    /* access modifiers changed from: protected */
    public File getProvidedPathFile() {
        return this.providedPathFile;
    }

    public File getParentFile() {
        return this.directoryFile;
    }

    public File getFile() {
        String filename = this.filenameHolder.get();
        if (filename == null) {
            return null;
        }
        if (this.targetFile == null) {
            this.targetFile = new File(this.directoryFile, filename);
        }
        return this.targetFile;
    }

    public int getReadBufferSize() {
        return this.readBufferSize;
    }

    public int getFlushBufferSize() {
        return this.flushBufferSize;
    }

    public int getSyncBufferSize() {
        return this.syncBufferSize;
    }

    public int getSyncBufferIntervalMills() {
        return this.syncBufferIntervalMills;
    }

    public boolean isAutoCallbackToUIThread() {
        return this.autoCallbackToUIThread;
    }

    public int getMinIntervalMillisCallbackProcess() {
        return this.minIntervalMillisCallbackProcess;
    }

    public Integer getSetConnectionCount() {
        return this.connectionCount;
    }

    public Boolean getSetPreAllocateLength() {
        return this.isPreAllocateLength;
    }

    public int getConnectionCount() {
        BreakpointInfo breakpointInfo = this.info;
        if (breakpointInfo == null) {
            return 0;
        }
        return breakpointInfo.getBlockCount();
    }

    public Object getTag(int key) {
        if (this.keyTagMap == null) {
            return null;
        }
        return this.keyTagMap.get(key);
    }

    public Object getTag() {
        return this.tag;
    }

    public BreakpointInfo getInfo() {
        if (this.info == null) {
            this.info = OkDownload.with().breakpointStore().get(this.f188id);
        }
        return this.info;
    }

    /* access modifiers changed from: package-private */
    public long getLastCallbackProcessTs() {
        return this.lastCallbackProcessTimestamp.get();
    }

    /* access modifiers changed from: package-private */
    public void setLastCallbackProcessTs(long lastCallbackProcessTimestamp2) {
        this.lastCallbackProcessTimestamp.set(lastCallbackProcessTimestamp2);
    }

    /* access modifiers changed from: package-private */
    public void setBreakpointInfo(BreakpointInfo info2) {
        this.info = info2;
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public synchronized com.liulishuo.okdownload.DownloadTask addTag(int r2, java.lang.Object r3) {
        /*
            r1 = this;
            monitor-enter(r1)
            android.util.SparseArray<java.lang.Object> r0 = r1.keyTagMap     // Catch:{ all -> 0x001f }
            if (r0 != 0) goto L_0x0018
            monitor-enter(r1)     // Catch:{ all -> 0x001f }
            android.util.SparseArray<java.lang.Object> r0 = r1.keyTagMap     // Catch:{ all -> 0x0013 }
            if (r0 != 0) goto L_0x0011
            android.util.SparseArray r0 = new android.util.SparseArray     // Catch:{ all -> 0x0013 }
            r0.<init>()     // Catch:{ all -> 0x0013 }
            r1.keyTagMap = r0     // Catch:{ all -> 0x0013 }
        L_0x0011:
            monitor-exit(r1)     // Catch:{ all -> 0x0013 }
            goto L_0x0018
        L_0x0013:
            r0 = move-exception
        L_0x0014:
            monitor-exit(r1)     // Catch:{ all -> 0x0016 }
            throw r0     // Catch:{ all -> 0x001f }
        L_0x0016:
            r0 = move-exception
            goto L_0x0014
        L_0x0018:
            android.util.SparseArray<java.lang.Object> r0 = r1.keyTagMap     // Catch:{ all -> 0x001f }
            r0.put(r2, r3)     // Catch:{ all -> 0x001f }
            monitor-exit(r1)
            return r1
        L_0x001f:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.DownloadTask.addTag(int, java.lang.Object):com.liulishuo.okdownload.DownloadTask");
    }

    public synchronized void removeTag(int key) {
        if (this.keyTagMap != null) {
            this.keyTagMap.remove(key);
        }
    }

    public synchronized void removeTag() {
        this.tag = null;
    }

    public void setTag(Object tag2) {
        this.tag = tag2;
    }

    public void replaceListener(DownloadListener listener2) {
        this.listener = listener2;
    }

    public static void enqueue(DownloadTask[] tasks, DownloadListener listener2) {
        for (DownloadTask task : tasks) {
            task.listener = listener2;
        }
        OkDownload.with().downloadDispatcher().enqueue(tasks);
    }

    public void enqueue(DownloadListener listener2) {
        this.listener = listener2;
        OkDownload.with().downloadDispatcher().enqueue(this);
    }

    public void execute(DownloadListener listener2) {
        this.listener = listener2;
        OkDownload.with().downloadDispatcher().execute(this);
    }

    public void cancel() {
        OkDownload.with().downloadDispatcher().cancel((IdentifiedTask) this);
    }

    public static void cancel(DownloadTask[] tasks) {
        OkDownload.with().downloadDispatcher().cancel((IdentifiedTask[]) tasks);
    }

    public DownloadListener getListener() {
        return this.listener;
    }

    public int getPriority() {
        return this.priority;
    }

    public Builder toBuilder(String anotherUrl, Uri anotherUri) {
        Builder builder = new Builder(anotherUrl, anotherUri).setPriority(this.priority).setReadBufferSize(this.readBufferSize).setFlushBufferSize(this.flushBufferSize).setSyncBufferSize(this.syncBufferSize).setSyncBufferIntervalMillis(this.syncBufferIntervalMills).setAutoCallbackToUIThread(this.autoCallbackToUIThread).setMinIntervalMillisCallbackProcess(this.minIntervalMillisCallbackProcess).setHeaderMapFields(this.headerMapFields).setPassIfAlreadyCompleted(this.passIfAlreadyCompleted);
        if (Util.isUriFileScheme(anotherUri) && !new File(anotherUri.getPath()).isFile() && Util.isUriFileScheme(this.uri) && this.filenameHolder.get() != null && !new File(this.uri.getPath()).getName().equals(this.filenameHolder.get())) {
            builder.setFilename(this.filenameHolder.get());
        }
        return builder;
    }

    public Builder toBuilder() {
        return toBuilder(this.url, this.uri);
    }

    public void setTags(DownloadTask oldTask) {
        this.tag = oldTask.tag;
        this.keyTagMap = oldTask.keyTagMap;
    }

    public int compareTo(DownloadTask o) {
        return o.getPriority() - getPriority();
    }

    public static class Builder {
        public static final boolean DEFAULT_AUTO_CALLBACK_TO_UI_THREAD = true;
        public static final int DEFAULT_FLUSH_BUFFER_SIZE = 16384;
        public static final boolean DEFAULT_IS_WIFI_REQUIRED = false;
        public static final int DEFAULT_MIN_INTERVAL_MILLIS_CALLBACK_PROCESS = 3000;
        public static final boolean DEFAULT_PASS_IF_ALREADY_COMPLETED = true;
        public static final int DEFAULT_READ_BUFFER_SIZE = 4096;
        public static final int DEFAULT_SYNC_BUFFER_INTERVAL_MILLIS = 2000;
        public static final int DEFAULT_SYNC_BUFFER_SIZE = 65536;
        private boolean autoCallbackToUIThread;
        private Integer connectionCount;
        private String filename;
        private int flushBufferSize;
        private volatile Map<String, List<String>> headerMapFields;
        private Boolean isFilenameFromResponse;
        private Boolean isPreAllocateLength;
        private boolean isWifiRequired;
        private int minIntervalMillisCallbackProcess;
        private boolean passIfAlreadyCompleted;
        private int priority;
        private int readBufferSize;
        private int syncBufferIntervalMillis;
        private int syncBufferSize;
        final Uri uri;
        final String url;

        public Builder(String url2, String parentPath, String filename2) {
            this(url2, Uri.fromFile(new File(parentPath)));
            if (Util.isEmpty(filename2)) {
                this.isFilenameFromResponse = true;
            } else {
                this.filename = filename2;
            }
        }

        public Builder(String url2, File file) {
            this.readBufferSize = 4096;
            this.flushBufferSize = 16384;
            this.syncBufferSize = 65536;
            this.syncBufferIntervalMillis = 2000;
            this.autoCallbackToUIThread = true;
            this.minIntervalMillisCallbackProcess = 3000;
            this.passIfAlreadyCompleted = true;
            this.isWifiRequired = false;
            this.url = url2;
            this.uri = Uri.fromFile(file);
        }

        public Builder(String url2, Uri uri2) {
            this.readBufferSize = 4096;
            this.flushBufferSize = 16384;
            this.syncBufferSize = 65536;
            this.syncBufferIntervalMillis = 2000;
            this.autoCallbackToUIThread = true;
            this.minIntervalMillisCallbackProcess = 3000;
            this.passIfAlreadyCompleted = true;
            this.isWifiRequired = false;
            this.url = url2;
            this.uri = uri2;
            if (Util.isUriContentScheme(uri2)) {
                this.filename = Util.getFilenameFromContentUri(uri2);
            }
        }

        public Builder setPreAllocateLength(boolean preAllocateLength) {
            this.isPreAllocateLength = Boolean.valueOf(preAllocateLength);
            return this;
        }

        public Builder setConnectionCount(int connectionCount2) {
            this.connectionCount = Integer.valueOf(connectionCount2);
            return this;
        }

        public Builder setFilenameFromResponse(Boolean filenameFromResponse) {
            if (Util.isUriFileScheme(this.uri)) {
                this.isFilenameFromResponse = filenameFromResponse;
                return this;
            }
            throw new IllegalArgumentException("Uri isn't file scheme we can't let filename from response");
        }

        public Builder setAutoCallbackToUIThread(boolean autoCallbackToUIThread2) {
            this.autoCallbackToUIThread = autoCallbackToUIThread2;
            return this;
        }

        public Builder setMinIntervalMillisCallbackProcess(int minIntervalMillisCallbackProcess2) {
            this.minIntervalMillisCallbackProcess = minIntervalMillisCallbackProcess2;
            return this;
        }

        public Builder setHeaderMapFields(Map<String, List<String>> headerMapFields2) {
            this.headerMapFields = headerMapFields2;
            return this;
        }

        public synchronized void addHeader(String key, String value) {
            if (this.headerMapFields == null) {
                this.headerMapFields = new HashMap();
            }
            List<String> valueList = this.headerMapFields.get(key);
            if (valueList == null) {
                valueList = new ArrayList<>();
                this.headerMapFields.put(key, valueList);
            }
            valueList.add(value);
        }

        public Builder setPriority(int priority2) {
            this.priority = priority2;
            return this;
        }

        public Builder setReadBufferSize(int readBufferSize2) {
            if (readBufferSize2 >= 0) {
                this.readBufferSize = readBufferSize2;
                return this;
            }
            throw new IllegalArgumentException("Value must be positive!");
        }

        public Builder setFlushBufferSize(int flushBufferSize2) {
            if (flushBufferSize2 >= 0) {
                this.flushBufferSize = flushBufferSize2;
                return this;
            }
            throw new IllegalArgumentException("Value must be positive!");
        }

        public Builder setSyncBufferSize(int syncBufferSize2) {
            if (syncBufferSize2 >= 0) {
                this.syncBufferSize = syncBufferSize2;
                return this;
            }
            throw new IllegalArgumentException("Value must be positive!");
        }

        public Builder setSyncBufferIntervalMillis(int syncBufferIntervalMillis2) {
            if (syncBufferIntervalMillis2 >= 0) {
                this.syncBufferIntervalMillis = syncBufferIntervalMillis2;
                return this;
            }
            throw new IllegalArgumentException("Value must be positive!");
        }

        public Builder setFilename(String filename2) {
            this.filename = filename2;
            return this;
        }

        public Builder setPassIfAlreadyCompleted(boolean passIfAlreadyCompleted2) {
            this.passIfAlreadyCompleted = passIfAlreadyCompleted2;
            return this;
        }

        public Builder setWifiRequired(boolean wifiRequired) {
            this.isWifiRequired = wifiRequired;
            return this;
        }

        public DownloadTask build() {
            return new DownloadTask(this.url, this.uri, this.priority, this.readBufferSize, this.flushBufferSize, this.syncBufferSize, this.syncBufferIntervalMillis, this.autoCallbackToUIThread, this.minIntervalMillisCallbackProcess, this.headerMapFields, this.filename, this.passIfAlreadyCompleted, this.isWifiRequired, this.isFilenameFromResponse, this.connectionCount, this.isPreAllocateLength);
        }
    }

    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (!(obj instanceof DownloadTask)) {
            return false;
        }
        DownloadTask another = (DownloadTask) obj;
        if (another.f188id == this.f188id) {
            return true;
        }
        return compareIgnoreId(another);
    }

    public int hashCode() {
        return (this.url + this.providedPathFile.toString() + this.filenameHolder.get()).hashCode();
    }

    public String toString() {
        return super.toString() + "@" + this.f188id + "@" + this.url + "@" + this.directoryFile.toString() + "/" + this.filenameHolder.get();
    }

    public static MockTaskForCompare mockTaskForCompare(int id) {
        return new MockTaskForCompare(id);
    }

    public MockTaskForCompare mock(int id) {
        return new MockTaskForCompare(id, this);
    }

    public static class TaskHideWrapper {
        public static long getLastCallbackProcessTs(DownloadTask task) {
            return task.getLastCallbackProcessTs();
        }

        public static void setLastCallbackProcessTs(DownloadTask task, long lastCallbackProcessTimestamp) {
            task.setLastCallbackProcessTs(lastCallbackProcessTimestamp);
        }

        public static void setBreakpointInfo(DownloadTask task, BreakpointInfo info) {
            task.setBreakpointInfo(info);
        }
    }

    public static class MockTaskForCompare extends IdentifiedTask {
        final String filename;

        /* renamed from: id */
        final int f189id;
        final File parentFile;
        final File providedPathFile;
        final String url;

        public MockTaskForCompare(int id) {
            this.f189id = id;
            this.url = "";
            this.providedPathFile = EMPTY_FILE;
            this.filename = null;
            this.parentFile = EMPTY_FILE;
        }

        public MockTaskForCompare(int id, DownloadTask task) {
            this.f189id = id;
            this.url = task.url;
            this.parentFile = task.getParentFile();
            this.providedPathFile = task.providedPathFile;
            this.filename = task.getFilename();
        }

        public int getId() {
            return this.f189id;
        }

        public String getUrl() {
            return this.url;
        }

        /* access modifiers changed from: protected */
        public File getProvidedPathFile() {
            return this.providedPathFile;
        }

        public File getParentFile() {
            return this.parentFile;
        }

        public String getFilename() {
            return this.filename;
        }
    }
}
