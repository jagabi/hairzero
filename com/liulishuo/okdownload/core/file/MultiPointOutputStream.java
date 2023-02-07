package com.liulishuo.okdownload.core.file;

import android.net.Uri;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.SparseArray;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.exception.PreAllocateException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class MultiPointOutputStream {
    private static final ExecutorService FILE_IO_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload file io", false));
    private static final String TAG = "MultiPointOutputStream";
    final AtomicLong allNoSyncLength;
    boolean canceled;
    final StreamsState doneState;
    private volatile boolean firstOutputStream;
    private final int flushBufferSize;
    private final BreakpointInfo info;
    private final boolean isPreAllocateLength;
    final AtomicLong lastSyncTimestamp;
    ArrayList<Integer> noMoreStreamList;
    final SparseArray<AtomicLong> noSyncLengthMap;
    final SparseArray<DownloadOutputStream> outputStreamMap;
    final SparseArray<Thread> parkedRunBlockThreadMap;
    private String path;
    List<Integer> requireStreamBlocks;
    volatile Thread runSyncThread;
    StreamsState state;
    private final DownloadStore store;
    private final boolean supportSeek;
    private final int syncBufferIntervalMills;
    private final int syncBufferSize;
    IOException syncException;
    volatile Future syncFuture;
    private final Runnable syncRunnable;
    private final DownloadTask task;

    MultiPointOutputStream(DownloadTask task2, BreakpointInfo info2, DownloadStore store2, Runnable syncRunnable2) {
        this.outputStreamMap = new SparseArray<>();
        this.noSyncLengthMap = new SparseArray<>();
        this.allNoSyncLength = new AtomicLong();
        this.lastSyncTimestamp = new AtomicLong();
        this.canceled = false;
        this.parkedRunBlockThreadMap = new SparseArray<>();
        this.doneState = new StreamsState();
        this.state = new StreamsState();
        this.firstOutputStream = true;
        this.task = task2;
        this.flushBufferSize = task2.getFlushBufferSize();
        this.syncBufferSize = task2.getSyncBufferSize();
        this.syncBufferIntervalMills = task2.getSyncBufferIntervalMills();
        this.info = info2;
        this.store = store2;
        this.supportSeek = OkDownload.with().outputStreamFactory().supportSeek();
        this.isPreAllocateLength = OkDownload.with().processFileStrategy().isPreAllocateLength(task2);
        this.noMoreStreamList = new ArrayList<>();
        if (syncRunnable2 == null) {
            this.syncRunnable = new Runnable() {
                public void run() {
                    MultiPointOutputStream.this.runSyncDelayException();
                }
            };
        } else {
            this.syncRunnable = syncRunnable2;
        }
        File file = task2.getFile();
        if (file != null) {
            this.path = file.getAbsolutePath();
        }
    }

    public MultiPointOutputStream(DownloadTask task2, BreakpointInfo info2, DownloadStore store2) {
        this(task2, info2, store2, (Runnable) null);
    }

    public synchronized void write(int blockIndex, byte[] bytes, int length) throws IOException {
        if (!this.canceled) {
            outputStream(blockIndex).write(bytes, 0, length);
            this.allNoSyncLength.addAndGet((long) length);
            this.noSyncLengthMap.get(blockIndex).addAndGet((long) length);
            inspectAndPersist();
        }
    }

    public void cancelAsync() {
        FILE_IO_EXECUTOR.execute(new Runnable() {
            public void run() {
                MultiPointOutputStream.this.cancel();
            }
        });
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:67:0x0129=Splitter:B:67:0x0129, B:51:0x00ca=Splitter:B:51:0x00ca} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void cancel() {
        /*
            r8 = this;
            monitor-enter(r8)
            java.util.List<java.lang.Integer> r0 = r8.requireStreamBlocks     // Catch:{ all -> 0x0185 }
            if (r0 != 0) goto L_0x0007
            monitor-exit(r8)
            return
        L_0x0007:
            boolean r1 = r8.canceled     // Catch:{ all -> 0x0185 }
            if (r1 == 0) goto L_0x000d
            monitor-exit(r8)
            return
        L_0x000d:
            r1 = 1
            r8.canceled = r1     // Catch:{ all -> 0x0185 }
            java.util.ArrayList<java.lang.Integer> r2 = r8.noMoreStreamList     // Catch:{ all -> 0x0185 }
            r2.addAll(r0)     // Catch:{ all -> 0x0185 }
            r0 = 0
            java.util.concurrent.atomic.AtomicLong r2 = r8.allNoSyncLength     // Catch:{ all -> 0x0128 }
            long r2 = r2.get()     // Catch:{ all -> 0x0128 }
            r4 = 0
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 > 0) goto L_0x007f
            java.util.List<java.lang.Integer> r1 = r8.requireStreamBlocks     // Catch:{ all -> 0x0185 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x0185 }
        L_0x0028:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x0185 }
            if (r2 == 0) goto L_0x0070
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x0185 }
            java.lang.Integer r2 = (java.lang.Integer) r2     // Catch:{ all -> 0x0185 }
            int r3 = r2.intValue()     // Catch:{ IOException -> 0x003c }
            r8.close(r3)     // Catch:{ IOException -> 0x003c }
            goto L_0x006f
        L_0x003c:
            r3 = move-exception
            java.lang.String r4 = "MultiPointOutputStream"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0185 }
            r5.<init>()     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "OutputStream close failed task["
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r6 = r8.task     // Catch:{ all -> 0x0185 }
            int r6 = r6.getId()     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "] block["
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "]"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r3)     // Catch:{ all -> 0x0185 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.Util.m83d(r4, r5)     // Catch:{ all -> 0x0185 }
        L_0x006f:
            goto L_0x0028
        L_0x0070:
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r1 = r8.store     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r2 = r8.task     // Catch:{ all -> 0x0185 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.cause.EndCause r3 = com.liulishuo.okdownload.core.cause.EndCause.CANCELED     // Catch:{ all -> 0x0185 }
            r1.onTaskEnd(r2, r3, r0)     // Catch:{ all -> 0x0185 }
            monitor-exit(r8)
            return
        L_0x007f:
            java.util.concurrent.Future r2 = r8.syncFuture     // Catch:{ all -> 0x0128 }
            if (r2 == 0) goto L_0x00ca
            java.util.concurrent.Future r2 = r8.syncFuture     // Catch:{ all -> 0x00c8 }
            boolean r2 = r2.isDone()     // Catch:{ all -> 0x00c8 }
            if (r2 != 0) goto L_0x00ca
            r8.inspectValidPath()     // Catch:{ all -> 0x00c8 }
            com.liulishuo.okdownload.OkDownload r2 = com.liulishuo.okdownload.OkDownload.with()     // Catch:{ all -> 0x00c8 }
            com.liulishuo.okdownload.core.file.ProcessFileStrategy r2 = r2.processFileStrategy()     // Catch:{ all -> 0x00c8 }
            com.liulishuo.okdownload.core.file.FileLock r2 = r2.getFileLock()     // Catch:{ all -> 0x00c8 }
            java.lang.String r3 = r8.path     // Catch:{ all -> 0x00c8 }
            r2.increaseLock(r3)     // Catch:{ all -> 0x00c8 }
            r2 = -1
            r8.ensureSync(r1, r2)     // Catch:{ all -> 0x00b5 }
            com.liulishuo.okdownload.OkDownload r1 = com.liulishuo.okdownload.OkDownload.with()     // Catch:{ all -> 0x0128 }
            com.liulishuo.okdownload.core.file.ProcessFileStrategy r1 = r1.processFileStrategy()     // Catch:{ all -> 0x0128 }
            com.liulishuo.okdownload.core.file.FileLock r1 = r1.getFileLock()     // Catch:{ all -> 0x0128 }
            java.lang.String r2 = r8.path     // Catch:{ all -> 0x0128 }
            r1.decreaseLock(r2)     // Catch:{ all -> 0x0128 }
            goto L_0x00ca
        L_0x00b5:
            r1 = move-exception
            com.liulishuo.okdownload.OkDownload r2 = com.liulishuo.okdownload.OkDownload.with()     // Catch:{ all -> 0x00c8 }
            com.liulishuo.okdownload.core.file.ProcessFileStrategy r2 = r2.processFileStrategy()     // Catch:{ all -> 0x00c8 }
            com.liulishuo.okdownload.core.file.FileLock r2 = r2.getFileLock()     // Catch:{ all -> 0x00c8 }
            java.lang.String r3 = r8.path     // Catch:{ all -> 0x00c8 }
            r2.decreaseLock(r3)     // Catch:{ all -> 0x00c8 }
            throw r1     // Catch:{ all -> 0x00c8 }
        L_0x00c8:
            r1 = move-exception
            goto L_0x0129
        L_0x00ca:
            java.util.List<java.lang.Integer> r1 = r8.requireStreamBlocks     // Catch:{ all -> 0x0185 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x0185 }
        L_0x00d0:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x0185 }
            if (r2 == 0) goto L_0x0118
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x0185 }
            java.lang.Integer r2 = (java.lang.Integer) r2     // Catch:{ all -> 0x0185 }
            int r3 = r2.intValue()     // Catch:{ IOException -> 0x00e4 }
            r8.close(r3)     // Catch:{ IOException -> 0x00e4 }
            goto L_0x0117
        L_0x00e4:
            r3 = move-exception
            java.lang.String r4 = "MultiPointOutputStream"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0185 }
            r5.<init>()     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "OutputStream close failed task["
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r6 = r8.task     // Catch:{ all -> 0x0185 }
            int r6 = r6.getId()     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "] block["
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r2)     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = "]"
            java.lang.StringBuilder r5 = r5.append(r6)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r5 = r5.append(r3)     // Catch:{ all -> 0x0185 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.Util.m83d(r4, r5)     // Catch:{ all -> 0x0185 }
        L_0x0117:
            goto L_0x00d0
        L_0x0118:
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r1 = r8.store     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r2 = r8.task     // Catch:{ all -> 0x0185 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.cause.EndCause r3 = com.liulishuo.okdownload.core.cause.EndCause.CANCELED     // Catch:{ all -> 0x0185 }
            r1.onTaskEnd(r2, r3, r0)     // Catch:{ all -> 0x0185 }
            monitor-exit(r8)
            return
        L_0x0128:
            r1 = move-exception
        L_0x0129:
            java.util.List<java.lang.Integer> r2 = r8.requireStreamBlocks     // Catch:{ all -> 0x0185 }
            java.util.Iterator r2 = r2.iterator()     // Catch:{ all -> 0x0185 }
        L_0x012f:
            boolean r3 = r2.hasNext()     // Catch:{ all -> 0x0185 }
            if (r3 == 0) goto L_0x0177
            java.lang.Object r3 = r2.next()     // Catch:{ all -> 0x0185 }
            java.lang.Integer r3 = (java.lang.Integer) r3     // Catch:{ all -> 0x0185 }
            int r4 = r3.intValue()     // Catch:{ IOException -> 0x0143 }
            r8.close(r4)     // Catch:{ IOException -> 0x0143 }
            goto L_0x0176
        L_0x0143:
            r4 = move-exception
            java.lang.String r5 = "MultiPointOutputStream"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0185 }
            r6.<init>()     // Catch:{ all -> 0x0185 }
            java.lang.String r7 = "OutputStream close failed task["
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r7 = r8.task     // Catch:{ all -> 0x0185 }
            int r7 = r7.getId()     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0185 }
            java.lang.String r7 = "] block["
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r6 = r6.append(r3)     // Catch:{ all -> 0x0185 }
            java.lang.String r7 = "]"
            java.lang.StringBuilder r6 = r6.append(r7)     // Catch:{ all -> 0x0185 }
            java.lang.StringBuilder r6 = r6.append(r4)     // Catch:{ all -> 0x0185 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.Util.m83d(r5, r6)     // Catch:{ all -> 0x0185 }
        L_0x0176:
            goto L_0x012f
        L_0x0177:
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r2 = r8.store     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.DownloadTask r3 = r8.task     // Catch:{ all -> 0x0185 }
            int r3 = r3.getId()     // Catch:{ all -> 0x0185 }
            com.liulishuo.okdownload.core.cause.EndCause r4 = com.liulishuo.okdownload.core.cause.EndCause.CANCELED     // Catch:{ all -> 0x0185 }
            r2.onTaskEnd(r3, r4, r0)     // Catch:{ all -> 0x0185 }
            throw r1     // Catch:{ all -> 0x0185 }
        L_0x0185:
            r0 = move-exception
            monitor-exit(r8)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.file.MultiPointOutputStream.cancel():void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    public void done(int blockIndex) throws IOException {
        this.noMoreStreamList.add(Integer.valueOf(blockIndex));
        try {
            IOException iOException = this.syncException;
            if (iOException == null) {
                if (this.syncFuture != null && !this.syncFuture.isDone()) {
                    AtomicLong noSyncLength = this.noSyncLengthMap.get(blockIndex);
                    if (noSyncLength != null && noSyncLength.get() > 0) {
                        inspectStreamState(this.doneState);
                        ensureSync(this.doneState.isNoMoreStream, blockIndex);
                    }
                } else if (this.syncFuture == null) {
                    Util.m83d(TAG, "OutputStream done but no need to ensure sync, because the sync job not run yet. task[" + this.task.getId() + "] block[" + blockIndex + "]");
                } else {
                    Util.m83d(TAG, "OutputStream done but no need to ensure sync, because the syncFuture.isDone[" + this.syncFuture.isDone() + "] task[" + this.task.getId() + "] block[" + blockIndex + "]");
                }
                return;
            }
            throw iOException;
        } finally {
            close(blockIndex);
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureSync(boolean isNoMoreStream, int blockIndex) {
        if (this.syncFuture != null && !this.syncFuture.isDone()) {
            if (!isNoMoreStream) {
                this.parkedRunBlockThreadMap.put(blockIndex, Thread.currentThread());
            }
            if (this.runSyncThread != null) {
                unparkThread(this.runSyncThread);
            } else {
                while (!isRunSyncThreadValid()) {
                    parkThread(25);
                }
                unparkThread(this.runSyncThread);
            }
            if (isNoMoreStream) {
                unparkThread(this.runSyncThread);
                try {
                    this.syncFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                }
            } else {
                parkThread();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRunSyncThreadValid() {
        return this.runSyncThread != null;
    }

    public void inspectComplete(int blockIndex) throws IOException {
        BlockInfo blockInfo = this.info.getBlock(blockIndex);
        if (!Util.isCorrectFull(blockInfo.getCurrentOffset(), blockInfo.getContentLength())) {
            throw new IOException("The current offset on block-info isn't update correct, " + blockInfo.getCurrentOffset() + " != " + blockInfo.getContentLength() + " on " + blockIndex);
        }
    }

    /* access modifiers changed from: package-private */
    public void inspectAndPersist() throws IOException {
        IOException iOException = this.syncException;
        if (iOException != null) {
            throw iOException;
        } else if (this.syncFuture == null) {
            synchronized (this.syncRunnable) {
                if (this.syncFuture == null) {
                    this.syncFuture = executeSyncRunnableAsync();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void close(int blockIndex) throws IOException {
        DownloadOutputStream outputStream = this.outputStreamMap.get(blockIndex);
        if (outputStream != null) {
            outputStream.close();
            this.outputStreamMap.remove(blockIndex);
            Util.m83d(TAG, "OutputStream close task[" + this.task.getId() + "] block[" + blockIndex + "]");
        }
    }

    /* access modifiers changed from: package-private */
    public void parkThread(long milliseconds) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(milliseconds));
    }

    /* access modifiers changed from: package-private */
    public void parkThread() {
        LockSupport.park();
    }

    /* access modifiers changed from: package-private */
    public void unparkThread(Thread thread) {
        LockSupport.unpark(thread);
    }

    /* access modifiers changed from: package-private */
    public Future executeSyncRunnableAsync() {
        return FILE_IO_EXECUTOR.submit(this.syncRunnable);
    }

    /* access modifiers changed from: package-private */
    public void inspectStreamState(StreamsState state2) {
        state2.newNoMoreStreamBlockList.clear();
        int noMoreStreamBlockCount = new HashSet<>((List) this.noMoreStreamList.clone()).size();
        if (noMoreStreamBlockCount != this.requireStreamBlocks.size()) {
            Util.m83d(TAG, "task[" + this.task.getId() + "] current need fetching block count " + this.requireStreamBlocks.size() + " is not equal to no more stream block count " + noMoreStreamBlockCount);
            state2.isNoMoreStream = false;
        } else {
            Util.m83d(TAG, "task[" + this.task.getId() + "] current need fetching block count " + this.requireStreamBlocks.size() + " is equal to no more stream block count " + noMoreStreamBlockCount);
            state2.isNoMoreStream = true;
        }
        SparseArray<DownloadOutputStream> streamMap = this.outputStreamMap.clone();
        int size = streamMap.size();
        for (int i = 0; i < size; i++) {
            int blockIndex = streamMap.keyAt(i);
            if (this.noMoreStreamList.contains(Integer.valueOf(blockIndex)) && !state2.noMoreStreamBlockList.contains(Integer.valueOf(blockIndex))) {
                state2.noMoreStreamBlockList.add(Integer.valueOf(blockIndex));
                state2.newNoMoreStreamBlockList.add(Integer.valueOf(blockIndex));
            }
        }
    }

    public void setRequireStreamBlocks(List<Integer> requireStreamBlocks2) {
        this.requireStreamBlocks = requireStreamBlocks2;
    }

    public void catchBlockConnectException(int blockIndex) {
        this.noMoreStreamList.add(Integer.valueOf(blockIndex));
    }

    static class StreamsState {
        boolean isNoMoreStream;
        List<Integer> newNoMoreStreamBlockList = new ArrayList();
        List<Integer> noMoreStreamBlockList = new ArrayList();

        StreamsState() {
        }

        /* access modifiers changed from: package-private */
        public boolean isStreamsEndOrChanged() {
            return this.isNoMoreStream || this.newNoMoreStreamBlockList.size() > 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void runSyncDelayException() {
        try {
            runSync();
        } catch (IOException e) {
            this.syncException = e;
            Util.m86w(TAG, "Sync to breakpoint-store for task[" + this.task.getId() + "] failed with cause: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void runSync() throws IOException {
        Util.m83d(TAG, "OutputStream start flush looper task[" + this.task.getId() + "] with syncBufferIntervalMills[" + this.syncBufferIntervalMills + "] syncBufferSize[" + this.syncBufferSize + "]");
        this.runSyncThread = Thread.currentThread();
        long nextParkMills = (long) this.syncBufferIntervalMills;
        flushProcess();
        while (true) {
            parkThread(nextParkMills);
            inspectStreamState(this.state);
            if (this.state.isStreamsEndOrChanged()) {
                Util.m83d(TAG, "runSync state change isNoMoreStream[" + this.state.isNoMoreStream + "] newNoMoreStreamBlockList[" + this.state.newNoMoreStreamBlockList + "]");
                if (this.allNoSyncLength.get() > 0) {
                    flushProcess();
                }
                for (Integer blockIndex : this.state.newNoMoreStreamBlockList) {
                    Thread parkedThread = this.parkedRunBlockThreadMap.get(blockIndex.intValue());
                    this.parkedRunBlockThreadMap.remove(blockIndex.intValue());
                    if (parkedThread != null) {
                        unparkThread(parkedThread);
                    }
                }
                if (this.state.isNoMoreStream) {
                    break;
                }
            } else if (isNoNeedFlushForLength()) {
                nextParkMills = (long) this.syncBufferIntervalMills;
            } else {
                nextParkMills = getNextParkMillisecond();
                if (nextParkMills <= 0) {
                    flushProcess();
                    nextParkMills = (long) this.syncBufferIntervalMills;
                }
            }
        }
        int size = this.parkedRunBlockThreadMap.size();
        for (int i = 0; i < size; i++) {
            Thread parkedThread2 = this.parkedRunBlockThreadMap.valueAt(i);
            if (parkedThread2 != null) {
                unparkThread(parkedThread2);
            }
        }
        this.parkedRunBlockThreadMap.clear();
        Util.m83d(TAG, "OutputStream stop flush looper task[" + this.task.getId() + "]");
    }

    /* access modifiers changed from: package-private */
    public boolean isNoNeedFlushForLength() {
        return this.allNoSyncLength.get() < ((long) this.syncBufferSize);
    }

    /* access modifiers changed from: package-private */
    public long getNextParkMillisecond() {
        return ((long) this.syncBufferIntervalMills) - (now() - this.lastSyncTimestamp.get());
    }

    /* access modifiers changed from: package-private */
    public long now() {
        return SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: package-private */
    public void flushProcess() throws IOException {
        int size;
        boolean success;
        synchronized (this.noSyncLengthMap) {
            size = this.noSyncLengthMap.size();
        }
        SparseArray<Long> increaseLengthMap = new SparseArray<>(size);
        int i = 0;
        while (i < size) {
            try {
                int blockIndex = this.outputStreamMap.keyAt(i);
                long noSyncLength = this.noSyncLengthMap.get(blockIndex).get();
                if (noSyncLength > 0) {
                    increaseLengthMap.put(blockIndex, Long.valueOf(noSyncLength));
                    this.outputStreamMap.get(blockIndex).flushAndSync();
                }
                i++;
            } catch (IOException ex) {
                Util.m86w(TAG, "OutputStream flush and sync data to filesystem failed " + ex);
                success = false;
            }
        }
        success = true;
        if (success) {
            int increaseLengthSize = increaseLengthMap.size();
            long allIncreaseLength = 0;
            for (int i2 = 0; i2 < increaseLengthSize; i2++) {
                int blockIndex2 = increaseLengthMap.keyAt(i2);
                long noSyncLength2 = increaseLengthMap.valueAt(i2).longValue();
                this.store.onSyncToFilesystemSuccess(this.info, blockIndex2, noSyncLength2);
                allIncreaseLength += noSyncLength2;
                this.noSyncLengthMap.get(blockIndex2).addAndGet(-noSyncLength2);
                Util.m83d(TAG, "OutputStream sync success (" + this.task.getId() + ") block(" + blockIndex2 + ")  syncLength(" + noSyncLength2 + ") currentOffset(" + this.info.getBlock(blockIndex2).getCurrentOffset() + ")");
            }
            this.allNoSyncLength.addAndGet(-allIncreaseLength);
            this.lastSyncTimestamp.set(SystemClock.uptimeMillis());
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized DownloadOutputStream outputStream(int blockIndex) throws IOException {
        DownloadOutputStream outputStream;
        Uri uri;
        outputStream = this.outputStreamMap.get(blockIndex);
        if (outputStream == null) {
            boolean isFileScheme = Util.isUriFileScheme(this.task.getUri());
            if (isFileScheme) {
                File file = this.task.getFile();
                if (file != null) {
                    File parentFile = this.task.getParentFile();
                    if (!parentFile.exists()) {
                        if (!parentFile.mkdirs()) {
                            throw new IOException("Create parent folder failed!");
                        }
                    }
                    if (file.createNewFile()) {
                        Util.m83d(TAG, "Create new file: " + file.getName());
                    }
                    uri = Uri.fromFile(file);
                } else {
                    throw new FileNotFoundException("Filename is not ready!");
                }
            } else {
                uri = this.task.getUri();
            }
            outputStream = OkDownload.with().outputStreamFactory().create(OkDownload.with().context(), uri, this.flushBufferSize);
            if (this.supportSeek) {
                long seekPoint = this.info.getBlock(blockIndex).getRangeLeft();
                if (seekPoint > 0) {
                    outputStream.seek(seekPoint);
                    Util.m83d(TAG, "Create output stream write from (" + this.task.getId() + ") block(" + blockIndex + ") " + seekPoint);
                }
            }
            if (this.firstOutputStream) {
                this.store.markFileDirty(this.task.getId());
            }
            if (!this.info.isChunked() && this.firstOutputStream && this.isPreAllocateLength) {
                long totalLength = this.info.getTotalLength();
                if (isFileScheme) {
                    File file2 = this.task.getFile();
                    long requireSpace = totalLength - file2.length();
                    if (requireSpace > 0) {
                        inspectFreeSpace(new StatFs(file2.getAbsolutePath()), requireSpace);
                        outputStream.setLength(totalLength);
                    }
                } else {
                    outputStream.setLength(totalLength);
                }
            }
            synchronized (this.noSyncLengthMap) {
                this.outputStreamMap.put(blockIndex, outputStream);
                this.noSyncLengthMap.put(blockIndex, new AtomicLong());
            }
            this.firstOutputStream = false;
        }
        return outputStream;
    }

    /* access modifiers changed from: package-private */
    public void inspectFreeSpace(StatFs statFs, long requireSpace) throws PreAllocateException {
        long freeSpace = Util.getFreeSpaceBytes(statFs);
        if (freeSpace < requireSpace) {
            throw new PreAllocateException(requireSpace, freeSpace);
        }
    }

    private void inspectValidPath() {
        if (this.path == null && this.task.getFile() != null) {
            this.path = this.task.getFile().getAbsolutePath();
        }
    }
}
