package com.liulishuo.okdownload.core.download;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.NamedRunnable;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadCall extends NamedRunnable implements Comparable<DownloadCall> {
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload Block", false));
    static final int MAX_COUNT_RETRY_FOR_PRECONDITION_FAILED = 1;
    private static final String TAG = "DownloadCall";
    public final boolean asyncExecuted;
    private final ArrayList<DownloadChain> blockChainList;
    volatile DownloadCache cache;
    volatile boolean canceled;
    private volatile Thread currentThread;
    volatile boolean finishing;
    private final DownloadStore store;
    public final DownloadTask task;

    private DownloadCall(DownloadTask task2, boolean asyncExecuted2, DownloadStore store2) {
        this(task2, asyncExecuted2, new ArrayList(), store2);
    }

    DownloadCall(DownloadTask task2, boolean asyncExecuted2, ArrayList<DownloadChain> runningBlockList, DownloadStore store2) {
        super("download call: " + task2.getId());
        this.task = task2;
        this.asyncExecuted = asyncExecuted2;
        this.blockChainList = runningBlockList;
        this.store = store2;
    }

    public static DownloadCall create(DownloadTask task2, boolean asyncExecuted2, DownloadStore store2) {
        return new DownloadCall(task2, asyncExecuted2, store2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        r1 = android.os.SystemClock.uptimeMillis();
        com.liulishuo.okdownload.OkDownload.with().downloadDispatcher().flyingCanceled(r9);
        r3 = r9.cache;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        if (r3 == null) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        r3.setUserCanceled();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        r4 = (java.util.List) r9.blockChainList.clone();
        r5 = r4.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0038, code lost:
        if (r5.hasNext() == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003a, code lost:
        r5.next().cancel();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        if (r4.isEmpty() == false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004c, code lost:
        if (r9.currentThread == null) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004e, code lost:
        com.liulishuo.okdownload.core.Util.m83d(TAG, "interrupt thread with cancel operation because of chains are not running " + r9.task.getId());
        r9.currentThread.interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0071, code lost:
        if (r3 == null) goto L_0x007a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0073, code lost:
        r3.getOutputStream().cancelAsync();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007a, code lost:
        com.liulishuo.okdownload.core.Util.m83d(TAG, "cancel task " + r9.task.getId() + " consume: " + (android.os.SystemClock.uptimeMillis() - r1) + "ms");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00ad, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean cancel() {
        /*
            r9 = this;
            monitor-enter(r9)
            boolean r0 = r9.canceled     // Catch:{ all -> 0x00ae }
            r1 = 0
            if (r0 == 0) goto L_0x0008
            monitor-exit(r9)     // Catch:{ all -> 0x00ae }
            return r1
        L_0x0008:
            boolean r0 = r9.finishing     // Catch:{ all -> 0x00ae }
            if (r0 == 0) goto L_0x000e
            monitor-exit(r9)     // Catch:{ all -> 0x00ae }
            return r1
        L_0x000e:
            r0 = 1
            r9.canceled = r0     // Catch:{ all -> 0x00ae }
            monitor-exit(r9)     // Catch:{ all -> 0x00ae }
            long r1 = android.os.SystemClock.uptimeMillis()
            com.liulishuo.okdownload.OkDownload r3 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher r3 = r3.downloadDispatcher()
            r3.flyingCanceled(r9)
            com.liulishuo.okdownload.core.download.DownloadCache r3 = r9.cache
            if (r3 == 0) goto L_0x0028
            r3.setUserCanceled()
        L_0x0028:
            java.util.ArrayList<com.liulishuo.okdownload.core.download.DownloadChain> r4 = r9.blockChainList
            java.lang.Object r4 = r4.clone()
            java.util.List r4 = (java.util.List) r4
            java.util.Iterator r5 = r4.iterator()
        L_0x0034:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x0044
            java.lang.Object r6 = r5.next()
            com.liulishuo.okdownload.core.download.DownloadChain r6 = (com.liulishuo.okdownload.core.download.DownloadChain) r6
            r6.cancel()
            goto L_0x0034
        L_0x0044:
            boolean r5 = r4.isEmpty()
            if (r5 == 0) goto L_0x0071
            java.lang.Thread r5 = r9.currentThread
            if (r5 == 0) goto L_0x0071
            java.lang.String r5 = "DownloadCall"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "interrupt thread with cancel operation because of chains are not running "
            java.lang.StringBuilder r6 = r6.append(r7)
            com.liulishuo.okdownload.DownloadTask r7 = r9.task
            int r7 = r7.getId()
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.liulishuo.okdownload.core.Util.m83d(r5, r6)
            java.lang.Thread r5 = r9.currentThread
            r5.interrupt()
        L_0x0071:
            if (r3 == 0) goto L_0x007a
            com.liulishuo.okdownload.core.file.MultiPointOutputStream r5 = r3.getOutputStream()
            r5.cancelAsync()
        L_0x007a:
            java.lang.String r5 = "DownloadCall"
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "cancel task "
            java.lang.StringBuilder r6 = r6.append(r7)
            com.liulishuo.okdownload.DownloadTask r7 = r9.task
            int r7 = r7.getId()
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = " consume: "
            java.lang.StringBuilder r6 = r6.append(r7)
            long r7 = android.os.SystemClock.uptimeMillis()
            long r7 = r7 - r1
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r7 = "ms"
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.liulishuo.okdownload.core.Util.m83d(r5, r6)
            return r0
        L_0x00ae:
            r0 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x00ae }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.download.DownloadCall.cancel():boolean");
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public boolean isFinishing() {
        return this.finishing;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x015d A[LOOP:0: B:1:0x0012->B:40:0x015d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x015b A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void execute() throws java.lang.InterruptedException {
        /*
            r13 = this;
            java.lang.Thread r0 = java.lang.Thread.currentThread()
            r13.currentThread = r0
            r0 = 0
            com.liulishuo.okdownload.OkDownload r1 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.file.ProcessFileStrategy r2 = r1.processFileStrategy()
            r13.inspectTaskStart()
        L_0x0012:
            com.liulishuo.okdownload.DownloadTask r3 = r13.task
            java.lang.String r3 = r3.getUrl()
            int r3 = r3.length()
            r4 = 1
            if (r3 > 0) goto L_0x0046
            com.liulishuo.okdownload.core.download.DownloadCache$PreError r3 = new com.liulishuo.okdownload.core.download.DownloadCache$PreError
            java.io.IOException r5 = new java.io.IOException
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "unexpected url: "
            java.lang.StringBuilder r6 = r6.append(r7)
            com.liulishuo.okdownload.DownloadTask r7 = r13.task
            java.lang.String r7 = r7.getUrl()
            java.lang.StringBuilder r6 = r6.append(r7)
            java.lang.String r6 = r6.toString()
            r5.<init>(r6)
            r3.<init>(r5)
            r13.cache = r3
            goto L_0x0173
        L_0x0046:
            boolean r3 = r13.canceled
            if (r3 == 0) goto L_0x004c
            goto L_0x0173
        L_0x004c:
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r3 = r13.store     // Catch:{ IOException -> 0x016a }
            com.liulishuo.okdownload.DownloadTask r5 = r13.task     // Catch:{ IOException -> 0x016a }
            int r5 = r5.getId()     // Catch:{ IOException -> 0x016a }
            com.liulishuo.okdownload.core.breakpoint.BreakpointInfo r3 = r3.get(r5)     // Catch:{ IOException -> 0x016a }
            if (r3 != 0) goto L_0x0063
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r5 = r13.store     // Catch:{ IOException -> 0x016a }
            com.liulishuo.okdownload.DownloadTask r6 = r13.task     // Catch:{ IOException -> 0x016a }
            com.liulishuo.okdownload.core.breakpoint.BreakpointInfo r5 = r5.createAndInsert(r6)     // Catch:{ IOException -> 0x016a }
            goto L_0x0064
        L_0x0063:
            r5 = r3
        L_0x0064:
            r13.setInfoToTask(r5)     // Catch:{ IOException -> 0x016a }
            boolean r3 = r13.canceled
            if (r3 == 0) goto L_0x006e
            goto L_0x0173
        L_0x006e:
            com.liulishuo.okdownload.core.download.DownloadCache r3 = r13.createCache(r5)
            r13.cache = r3
            com.liulishuo.okdownload.core.download.BreakpointRemoteCheck r6 = r13.createRemoteCheck(r5)
            r6.check()     // Catch:{ IOException -> 0x0165 }
            com.liulishuo.okdownload.DownloadTask r7 = r13.task
            java.lang.String r7 = r7.getRedirectLocation()
            r3.setRedirectLocation(r7)
            com.liulishuo.okdownload.core.file.FileLock r7 = r2.getFileLock()
            com.liulishuo.okdownload.DownloadTask r8 = r13.task
            java.io.File r8 = r8.getFile()
            java.lang.String r8 = r8.getAbsolutePath()
            r7.waitForRelease(r8)
            com.liulishuo.okdownload.OkDownload r7 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.download.DownloadStrategy r7 = r7.downloadStrategy()
            com.liulishuo.okdownload.DownloadTask r8 = r13.task
            long r9 = r6.getInstanceLength()
            r7.inspectAnotherSameInfo(r8, r5, r9)
            boolean r7 = r6.isResumable()     // Catch:{ IOException -> 0x0160 }
            java.lang.String r8 = " "
            java.lang.String r9 = "DownloadCall"
            if (r7 == 0) goto L_0x0103
            long r10 = r6.getInstanceLength()     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.core.download.BreakpointLocalCheck r7 = r13.createLocalCheck(r5, r10)     // Catch:{ IOException -> 0x0160 }
            r7.check()     // Catch:{ IOException -> 0x0160 }
            boolean r10 = r7.isDirty()     // Catch:{ IOException -> 0x0160 }
            if (r10 == 0) goto L_0x00f5
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0160 }
            r10.<init>()     // Catch:{ IOException -> 0x0160 }
            java.lang.String r11 = "breakpoint invalid: download from beginning because of local check is dirty "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadTask r11 = r13.task     // Catch:{ IOException -> 0x0160 }
            int r11 = r11.getId()     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r8 = r10.append(r8)     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r8 = r8.append(r7)     // Catch:{ IOException -> 0x0160 }
            java.lang.String r8 = r8.toString()     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.core.Util.m83d(r9, r8)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadTask r8 = r13.task     // Catch:{ IOException -> 0x0160 }
            r2.discardProcess(r8)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.core.cause.ResumeFailedCause r8 = r7.getCauseOrThrow()     // Catch:{ IOException -> 0x0160 }
            r13.assembleBlockAndCallbackFromBeginning(r5, r6, r8)     // Catch:{ IOException -> 0x0160 }
            goto L_0x0102
        L_0x00f5:
            com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher r8 = r1.callbackDispatcher()     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadListener r8 = r8.dispatch()     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadTask r9 = r13.task     // Catch:{ IOException -> 0x0160 }
            r8.downloadFromBreakpoint(r9, r5)     // Catch:{ IOException -> 0x0160 }
        L_0x0102:
            goto L_0x0134
        L_0x0103:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0160 }
            r7.<init>()     // Catch:{ IOException -> 0x0160 }
            java.lang.String r10 = "breakpoint invalid: download from beginning because of remote check not resumable "
            java.lang.StringBuilder r7 = r7.append(r10)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadTask r10 = r13.task     // Catch:{ IOException -> 0x0160 }
            int r10 = r10.getId()     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r7 = r7.append(r10)     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ IOException -> 0x0160 }
            java.lang.StringBuilder r7 = r7.append(r6)     // Catch:{ IOException -> 0x0160 }
            java.lang.String r7 = r7.toString()     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.core.Util.m83d(r9, r7)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.DownloadTask r7 = r13.task     // Catch:{ IOException -> 0x0160 }
            r2.discardProcess(r7)     // Catch:{ IOException -> 0x0160 }
            com.liulishuo.okdownload.core.cause.ResumeFailedCause r7 = r6.getCauseOrThrow()     // Catch:{ IOException -> 0x0160 }
            r13.assembleBlockAndCallbackFromBeginning(r5, r6, r7)     // Catch:{ IOException -> 0x0160 }
        L_0x0134:
            r13.start(r3, r5)
            boolean r7 = r13.canceled
            if (r7 == 0) goto L_0x013d
            goto L_0x0173
        L_0x013d:
            boolean r7 = r3.isPreconditionFailed()
            if (r7 == 0) goto L_0x0155
            int r7 = r0 + 1
            if (r0 >= r4) goto L_0x0154
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r0 = r13.store
            com.liulishuo.okdownload.DownloadTask r8 = r13.task
            int r8 = r8.getId()
            r0.remove(r8)
            r0 = 1
            goto L_0x0159
        L_0x0154:
            r0 = r7
        L_0x0155:
            r7 = 0
            r12 = r7
            r7 = r0
            r0 = r12
        L_0x0159:
            if (r0 != 0) goto L_0x015d
            r0 = r7
            goto L_0x0173
        L_0x015d:
            r0 = r7
            goto L_0x0012
        L_0x0160:
            r7 = move-exception
            r3.setUnknownError(r7)
            goto L_0x0173
        L_0x0165:
            r7 = move-exception
            r3.catchException(r7)
            goto L_0x0173
        L_0x016a:
            r3 = move-exception
            com.liulishuo.okdownload.core.download.DownloadCache$PreError r5 = new com.liulishuo.okdownload.core.download.DownloadCache$PreError
            r5.<init>(r3)
            r13.cache = r5
        L_0x0173:
            r13.finishing = r4
            java.util.ArrayList<com.liulishuo.okdownload.core.download.DownloadChain> r3 = r13.blockChainList
            r3.clear()
            com.liulishuo.okdownload.core.download.DownloadCache r3 = r13.cache
            boolean r4 = r13.canceled
            if (r4 != 0) goto L_0x01ba
            if (r3 != 0) goto L_0x0183
            goto L_0x01ba
        L_0x0183:
            r4 = 0
            boolean r5 = r3.isServerCanceled()
            if (r5 != 0) goto L_0x01b0
            boolean r5 = r3.isUnknownError()
            if (r5 != 0) goto L_0x01b0
            boolean r5 = r3.isPreconditionFailed()
            if (r5 == 0) goto L_0x0197
            goto L_0x01b0
        L_0x0197:
            boolean r5 = r3.isFileBusyAfterRun()
            if (r5 == 0) goto L_0x01a0
            com.liulishuo.okdownload.core.cause.EndCause r5 = com.liulishuo.okdownload.core.cause.EndCause.FILE_BUSY
            goto L_0x01b6
        L_0x01a0:
            boolean r5 = r3.isPreAllocateFailed()
            if (r5 == 0) goto L_0x01ad
            com.liulishuo.okdownload.core.cause.EndCause r5 = com.liulishuo.okdownload.core.cause.EndCause.PRE_ALLOCATE_FAILED
            java.io.IOException r4 = r3.getRealCause()
            goto L_0x01b6
        L_0x01ad:
            com.liulishuo.okdownload.core.cause.EndCause r5 = com.liulishuo.okdownload.core.cause.EndCause.COMPLETED
            goto L_0x01b6
        L_0x01b0:
            com.liulishuo.okdownload.core.cause.EndCause r5 = com.liulishuo.okdownload.core.cause.EndCause.ERROR
            java.io.IOException r4 = r3.getRealCause()
        L_0x01b6:
            r13.inspectTaskEnd(r3, r5, r4)
            return
        L_0x01ba:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.download.DownloadCall.execute():void");
    }

    private void inspectTaskStart() {
        this.store.onTaskStart(this.task.getId());
        OkDownload.with().callbackDispatcher().dispatch().taskStart(this.task);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000f, code lost:
        r3.store.onTaskEnd(r3.task.getId(), r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        if (r5 != com.liulishuo.okdownload.core.cause.EndCause.COMPLETED) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r3.store.markFileClear(r3.task.getId());
        com.liulishuo.okdownload.OkDownload.with().processFileStrategy().completeProcessStream(r4.getOutputStream(), r3.task);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003a, code lost:
        com.liulishuo.okdownload.OkDownload.with().callbackDispatcher().dispatch().taskEnd(r3.task, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004b, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inspectTaskEnd(com.liulishuo.okdownload.core.download.DownloadCache r4, com.liulishuo.okdownload.core.cause.EndCause r5, java.lang.Exception r6) {
        /*
            r3 = this;
            com.liulishuo.okdownload.core.cause.EndCause r0 = com.liulishuo.okdownload.core.cause.EndCause.CANCELED
            if (r5 == r0) goto L_0x004f
            monitor-enter(r3)
            boolean r0 = r3.canceled     // Catch:{ all -> 0x004c }
            if (r0 == 0) goto L_0x000b
            monitor-exit(r3)     // Catch:{ all -> 0x004c }
            return
        L_0x000b:
            r0 = 1
            r3.finishing = r0     // Catch:{ all -> 0x004c }
            monitor-exit(r3)     // Catch:{ all -> 0x004c }
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r0 = r3.store
            com.liulishuo.okdownload.DownloadTask r1 = r3.task
            int r1 = r1.getId()
            r0.onTaskEnd(r1, r5, r6)
            com.liulishuo.okdownload.core.cause.EndCause r0 = com.liulishuo.okdownload.core.cause.EndCause.COMPLETED
            if (r5 != r0) goto L_0x003a
            com.liulishuo.okdownload.core.breakpoint.DownloadStore r0 = r3.store
            com.liulishuo.okdownload.DownloadTask r1 = r3.task
            int r1 = r1.getId()
            r0.markFileClear(r1)
            com.liulishuo.okdownload.OkDownload r0 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.file.ProcessFileStrategy r0 = r0.processFileStrategy()
            com.liulishuo.okdownload.core.file.MultiPointOutputStream r1 = r4.getOutputStream()
            com.liulishuo.okdownload.DownloadTask r2 = r3.task
            r0.completeProcessStream(r1, r2)
        L_0x003a:
            com.liulishuo.okdownload.OkDownload r0 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher r0 = r0.callbackDispatcher()
            com.liulishuo.okdownload.DownloadListener r0 = r0.dispatch()
            com.liulishuo.okdownload.DownloadTask r1 = r3.task
            r0.taskEnd(r1, r5, r6)
            return
        L_0x004c:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x004c }
            throw r0
        L_0x004f:
            java.lang.IllegalAccessError r0 = new java.lang.IllegalAccessError
            java.lang.String r1 = "can't recognize cancelled on here"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.download.DownloadCall.inspectTaskEnd(com.liulishuo.okdownload.core.download.DownloadCache, com.liulishuo.okdownload.core.cause.EndCause, java.lang.Exception):void");
    }

    /* access modifiers changed from: package-private */
    public DownloadCache createCache(BreakpointInfo info) {
        return new DownloadCache(OkDownload.with().processFileStrategy().createProcessStream(this.task, info, this.store));
    }

    /* access modifiers changed from: package-private */
    public int getPriority() {
        return this.task.getPriority();
    }

    /* access modifiers changed from: package-private */
    public void start(DownloadCache cache2, BreakpointInfo info) throws InterruptedException {
        int blockCount = info.getBlockCount();
        List<DownloadChain> blockChainList2 = new ArrayList<>(info.getBlockCount());
        List<Integer> blockIndexList = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            BlockInfo blockInfo = info.getBlock(i);
            if (!Util.isCorrectFull(blockInfo.getCurrentOffset(), blockInfo.getContentLength())) {
                Util.resetBlockIfDirty(blockInfo);
                DownloadChain chain = DownloadChain.createChain(i, this.task, info, cache2, this.store);
                blockChainList2.add(chain);
                blockIndexList.add(Integer.valueOf(chain.getBlockIndex()));
            }
        }
        if (this.canceled == 0) {
            cache2.getOutputStream().setRequireStreamBlocks(blockIndexList);
            startBlocks(blockChainList2);
        }
    }

    /* access modifiers changed from: protected */
    public void interrupted(InterruptedException e) {
    }

    /* access modifiers changed from: protected */
    public void finished() {
        OkDownload.with().downloadDispatcher().finish(this);
        Util.m83d(TAG, "call is finished " + this.task.getId());
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* access modifiers changed from: package-private */
    public void startBlocks(List<DownloadChain> tasks) throws InterruptedException {
        ArrayList<Future> futures = new ArrayList<>(tasks.size());
        try {
            for (DownloadChain chain : tasks) {
                futures.add(submitChain(chain));
            }
            this.blockChainList.addAll(tasks);
            Iterator<Future> it = futures.iterator();
            while (it.hasNext()) {
                Future future = it.next();
                if (!future.isDone()) {
                    try {
                        future.get();
                    } catch (CancellationException | ExecutionException e) {
                    }
                }
            }
            this.blockChainList.removeAll(tasks);
        } catch (Throwable t) {
            this.blockChainList.removeAll(tasks);
            throw t;
        }
    }

    /* access modifiers changed from: package-private */
    public BreakpointLocalCheck createLocalCheck(BreakpointInfo info, long responseInstanceLength) {
        return new BreakpointLocalCheck(this.task, info, responseInstanceLength);
    }

    /* access modifiers changed from: package-private */
    public BreakpointRemoteCheck createRemoteCheck(BreakpointInfo info) {
        return new BreakpointRemoteCheck(this.task, info);
    }

    /* access modifiers changed from: package-private */
    public void setInfoToTask(BreakpointInfo info) {
        DownloadTask.TaskHideWrapper.setBreakpointInfo(this.task, info);
    }

    /* access modifiers changed from: package-private */
    public void assembleBlockAndCallbackFromBeginning(BreakpointInfo info, BreakpointRemoteCheck remoteCheck, ResumeFailedCause failedCause) {
        Util.assembleBlock(this.task, info, remoteCheck.getInstanceLength(), remoteCheck.isAcceptRange());
        OkDownload.with().callbackDispatcher().dispatch().downloadFromBeginning(this.task, info, failedCause);
    }

    /* access modifiers changed from: package-private */
    public Future<?> submitChain(DownloadChain chain) {
        return EXECUTOR.submit(chain);
    }

    public boolean equalsTask(DownloadTask task2) {
        return this.task.equals(task2);
    }

    public File getFile() {
        return this.task.getFile();
    }

    public int compareTo(DownloadCall o) {
        return o.getPriority() - getPriority();
    }
}
