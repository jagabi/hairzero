package com.liulishuo.okdownload.core.dispatcher;

import android.os.SystemClock;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.IdentifiedTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.download.DownloadCall;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadDispatcher {
    private static final String TAG = "DownloadDispatcher";
    private volatile ExecutorService executorService;
    private final List<DownloadCall> finishingCalls;
    private final AtomicInteger flyingCanceledAsyncCallCount;
    int maxParallelRunningCount;
    private final List<DownloadCall> readyAsyncCalls;
    private final List<DownloadCall> runningAsyncCalls;
    private final List<DownloadCall> runningSyncCalls;
    private final AtomicInteger skipProceedCallCount;
    private DownloadStore store;

    public DownloadDispatcher() {
        this(new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());
    }

    DownloadDispatcher(List<DownloadCall> readyAsyncCalls2, List<DownloadCall> runningAsyncCalls2, List<DownloadCall> runningSyncCalls2, List<DownloadCall> finishingCalls2) {
        this.maxParallelRunningCount = 5;
        this.flyingCanceledAsyncCallCount = new AtomicInteger();
        this.skipProceedCallCount = new AtomicInteger();
        this.readyAsyncCalls = readyAsyncCalls2;
        this.runningAsyncCalls = runningAsyncCalls2;
        this.runningSyncCalls = runningSyncCalls2;
        this.finishingCalls = finishingCalls2;
    }

    public void setDownloadStore(DownloadStore store2) {
        this.store = store2;
    }

    /* access modifiers changed from: package-private */
    public synchronized ExecutorService getExecutorService() {
        if (this.executorService == null) {
            this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload Download", false));
        }
        return this.executorService;
    }

    public void enqueue(DownloadTask[] tasks) {
        this.skipProceedCallCount.incrementAndGet();
        enqueueLocked(tasks);
        this.skipProceedCallCount.decrementAndGet();
    }

    public void enqueue(DownloadTask task) {
        this.skipProceedCallCount.incrementAndGet();
        enqueueLocked(task);
        this.skipProceedCallCount.decrementAndGet();
    }

    private synchronized void enqueueLocked(DownloadTask[] tasks) {
        long startTime = SystemClock.uptimeMillis();
        Util.m83d(TAG, "start enqueueLocked for bunch task: " + tasks.length);
        List<DownloadTask> taskList = new ArrayList<>();
        Collections.addAll(taskList, tasks);
        if (taskList.size() > 1) {
            Collections.sort(taskList);
        }
        int originReadyAsyncCallSize = this.readyAsyncCalls.size();
        try {
            OkDownload.with().downloadStrategy().inspectNetworkAvailable();
            Collection<DownloadTask> completedTaskList = new ArrayList<>();
            Collection<DownloadTask> sameTaskConflictList = new ArrayList<>();
            Collection<DownloadTask> fileBusyList = new ArrayList<>();
            for (DownloadTask task : taskList) {
                if (!inspectCompleted(task, completedTaskList)) {
                    if (!inspectForConflict(task, sameTaskConflictList, fileBusyList)) {
                        enqueueIgnorePriority(task);
                    }
                }
            }
            OkDownload.with().callbackDispatcher().endTasks(completedTaskList, sameTaskConflictList, fileBusyList);
        } catch (UnknownHostException e) {
            OkDownload.with().callbackDispatcher().endTasksWithError(new ArrayList<>(taskList), e);
        }
        if (originReadyAsyncCallSize != this.readyAsyncCalls.size()) {
            Collections.sort(this.readyAsyncCalls);
        }
        Util.m83d(TAG, "end enqueueLocked for bunch task: " + tasks.length + " consume " + (SystemClock.uptimeMillis() - startTime) + "ms");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void enqueueLocked(com.liulishuo.okdownload.DownloadTask r4) {
        /*
            r3 = this;
            monitor-enter(r3)
            java.lang.String r0 = "DownloadDispatcher"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0041 }
            r1.<init>()     // Catch:{ all -> 0x0041 }
            java.lang.String r2 = "enqueueLocked for single task: "
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0041 }
            java.lang.StringBuilder r1 = r1.append(r4)     // Catch:{ all -> 0x0041 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0041 }
            com.liulishuo.okdownload.core.Util.m83d(r0, r1)     // Catch:{ all -> 0x0041 }
            boolean r0 = r3.inspectCompleted(r4)     // Catch:{ all -> 0x0041 }
            if (r0 == 0) goto L_0x0021
            monitor-exit(r3)
            return
        L_0x0021:
            boolean r0 = r3.inspectForConflict(r4)     // Catch:{ all -> 0x0041 }
            if (r0 == 0) goto L_0x0029
            monitor-exit(r3)
            return
        L_0x0029:
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r0 = r3.readyAsyncCalls     // Catch:{ all -> 0x0041 }
            int r0 = r0.size()     // Catch:{ all -> 0x0041 }
            r3.enqueueIgnorePriority(r4)     // Catch:{ all -> 0x0041 }
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r1 = r3.readyAsyncCalls     // Catch:{ all -> 0x0041 }
            int r1 = r1.size()     // Catch:{ all -> 0x0041 }
            if (r0 == r1) goto L_0x003f
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r1 = r3.readyAsyncCalls     // Catch:{ all -> 0x0041 }
            java.util.Collections.sort(r1)     // Catch:{ all -> 0x0041 }
        L_0x003f:
            monitor-exit(r3)
            return
        L_0x0041:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher.enqueueLocked(com.liulishuo.okdownload.DownloadTask):void");
    }

    private synchronized void enqueueIgnorePriority(DownloadTask task) {
        DownloadCall call = DownloadCall.create(task, true, this.store);
        if (runningAsyncSize() < this.maxParallelRunningCount) {
            this.runningAsyncCalls.add(call);
            getExecutorService().execute(call);
        } else {
            this.readyAsyncCalls.add(call);
        }
    }

    public void execute(DownloadTask task) {
        Util.m83d(TAG, "execute: " + task);
        synchronized (this) {
            if (!inspectCompleted(task)) {
                if (!inspectForConflict(task)) {
                    DownloadCall call = DownloadCall.create(task, false, this.store);
                    this.runningSyncCalls.add(call);
                    syncRunCall(call);
                }
            }
        }
    }

    public void cancelAll() {
        this.skipProceedCallCount.incrementAndGet();
        List<DownloadTask> taskList = new ArrayList<>();
        for (DownloadCall call : this.readyAsyncCalls) {
            taskList.add(call.task);
        }
        for (DownloadCall call2 : this.runningAsyncCalls) {
            taskList.add(call2.task);
        }
        for (DownloadCall call3 : this.runningSyncCalls) {
            taskList.add(call3.task);
        }
        if (!taskList.isEmpty()) {
            cancelLocked((IdentifiedTask[]) taskList.toArray(new DownloadTask[taskList.size()]));
        }
        this.skipProceedCallCount.decrementAndGet();
    }

    public void cancel(IdentifiedTask[] tasks) {
        this.skipProceedCallCount.incrementAndGet();
        cancelLocked(tasks);
        this.skipProceedCallCount.decrementAndGet();
        processCalls();
    }

    public boolean cancel(IdentifiedTask task) {
        this.skipProceedCallCount.incrementAndGet();
        boolean result = cancelLocked(task);
        this.skipProceedCallCount.decrementAndGet();
        processCalls();
        return result;
    }

    public boolean cancel(int id) {
        this.skipProceedCallCount.incrementAndGet();
        boolean result = cancelLocked((IdentifiedTask) DownloadTask.mockTaskForCompare(id));
        this.skipProceedCallCount.decrementAndGet();
        processCalls();
        return result;
    }

    private synchronized void cancelLocked(IdentifiedTask[] tasks) {
        long startCancelTime = SystemClock.uptimeMillis();
        Util.m83d(TAG, "start cancel bunch task manually: " + tasks.length);
        List<DownloadCall> needCallbackCalls = new ArrayList<>();
        List<DownloadCall> needCancelCalls = new ArrayList<>();
        try {
            int length = tasks.length;
            int i = 0;
            while (i < length) {
                try {
                    filterCanceledCalls(tasks[i], needCallbackCalls, needCancelCalls);
                    i++;
                } catch (Throwable th) {
                    th = th;
                    handleCanceledCalls(needCallbackCalls, needCancelCalls);
                    Util.m83d(TAG, "finish cancel bunch task manually: " + tasks.length + " consume " + (SystemClock.uptimeMillis() - startCancelTime) + "ms");
                    throw th;
                }
            }
            handleCanceledCalls(needCallbackCalls, needCancelCalls);
            Util.m83d(TAG, "finish cancel bunch task manually: " + tasks.length + " consume " + (SystemClock.uptimeMillis() - startCancelTime) + "ms");
        } catch (Throwable th2) {
            th = th2;
            handleCanceledCalls(needCallbackCalls, needCancelCalls);
            Util.m83d(TAG, "finish cancel bunch task manually: " + tasks.length + " consume " + (SystemClock.uptimeMillis() - startCancelTime) + "ms");
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public synchronized boolean cancelLocked(IdentifiedTask task) {
        List<DownloadCall> needCallbackCalls;
        List<DownloadCall> needCancelCalls;
        Util.m83d(TAG, "cancel manually: " + task.getId());
        needCallbackCalls = new ArrayList<>();
        needCancelCalls = new ArrayList<>();
        try {
            filterCanceledCalls(task, needCallbackCalls, needCancelCalls);
            handleCanceledCalls(needCallbackCalls, needCancelCalls);
        } catch (Throwable th) {
            handleCanceledCalls(needCallbackCalls, needCancelCalls);
            throw th;
        }
        return needCallbackCalls.size() > 0 || needCancelCalls.size() > 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003b, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void filterCanceledCalls(com.liulishuo.okdownload.core.IdentifiedTask r5, java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r6, java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r7) {
        /*
            r4 = this;
            monitor-enter(r4)
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r0 = r4.readyAsyncCalls     // Catch:{ all -> 0x0096 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x0096 }
        L_0x0007:
            boolean r1 = r0.hasNext()     // Catch:{ all -> 0x0096 }
            if (r1 == 0) goto L_0x003c
            java.lang.Object r1 = r0.next()     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.core.download.DownloadCall r1 = (com.liulishuo.okdownload.core.download.DownloadCall) r1     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            if (r2 == r5) goto L_0x0025
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0096 }
            int r3 = r5.getId()     // Catch:{ all -> 0x0096 }
            if (r2 != r3) goto L_0x0024
            goto L_0x0025
        L_0x0024:
            goto L_0x0007
        L_0x0025:
            boolean r2 = r1.isCanceled()     // Catch:{ all -> 0x0096 }
            if (r2 != 0) goto L_0x003a
            boolean r2 = r1.isFinishing()     // Catch:{ all -> 0x0096 }
            if (r2 == 0) goto L_0x0032
            goto L_0x003a
        L_0x0032:
            r0.remove()     // Catch:{ all -> 0x0096 }
            r6.add(r1)     // Catch:{ all -> 0x0096 }
            monitor-exit(r4)
            return
        L_0x003a:
            monitor-exit(r4)
            return
        L_0x003c:
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r0 = r4.runningAsyncCalls     // Catch:{ all -> 0x0096 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x0096 }
        L_0x0042:
            boolean r1 = r0.hasNext()     // Catch:{ all -> 0x0096 }
            if (r1 == 0) goto L_0x0068
            java.lang.Object r1 = r0.next()     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.core.download.DownloadCall r1 = (com.liulishuo.okdownload.core.download.DownloadCall) r1     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            if (r2 == r5) goto L_0x0060
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0096 }
            int r3 = r5.getId()     // Catch:{ all -> 0x0096 }
            if (r2 != r3) goto L_0x005f
            goto L_0x0060
        L_0x005f:
            goto L_0x0042
        L_0x0060:
            r6.add(r1)     // Catch:{ all -> 0x0096 }
            r7.add(r1)     // Catch:{ all -> 0x0096 }
            monitor-exit(r4)
            return
        L_0x0068:
            java.util.List<com.liulishuo.okdownload.core.download.DownloadCall> r0 = r4.runningSyncCalls     // Catch:{ all -> 0x0096 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x0096 }
        L_0x006e:
            boolean r1 = r0.hasNext()     // Catch:{ all -> 0x0096 }
            if (r1 == 0) goto L_0x0094
            java.lang.Object r1 = r0.next()     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.core.download.DownloadCall r1 = (com.liulishuo.okdownload.core.download.DownloadCall) r1     // Catch:{ all -> 0x0096 }
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            if (r2 == r5) goto L_0x008c
            com.liulishuo.okdownload.DownloadTask r2 = r1.task     // Catch:{ all -> 0x0096 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0096 }
            int r3 = r5.getId()     // Catch:{ all -> 0x0096 }
            if (r2 != r3) goto L_0x008b
            goto L_0x008c
        L_0x008b:
            goto L_0x006e
        L_0x008c:
            r6.add(r1)     // Catch:{ all -> 0x0096 }
            r7.add(r1)     // Catch:{ all -> 0x0096 }
            monitor-exit(r4)
            return
        L_0x0094:
            monitor-exit(r4)
            return
        L_0x0096:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher.filterCanceledCalls(com.liulishuo.okdownload.core.IdentifiedTask, java.util.List, java.util.List):void");
    }

    private synchronized void handleCanceledCalls(List<DownloadCall> needCallbackCalls, List<DownloadCall> needCancelCalls) {
        Util.m83d(TAG, "handle cancel calls, cancel calls: " + needCancelCalls.size());
        if (!needCancelCalls.isEmpty()) {
            for (DownloadCall call : needCancelCalls) {
                if (!call.cancel()) {
                    needCallbackCalls.remove(call);
                }
            }
        }
        Util.m83d(TAG, "handle cancel calls, callback cancel event: " + needCallbackCalls.size());
        if (!needCallbackCalls.isEmpty()) {
            if (needCallbackCalls.size() <= 1) {
                OkDownload.with().callbackDispatcher().dispatch().taskEnd(needCallbackCalls.get(0).task, EndCause.CANCELED, (Exception) null);
            } else {
                List<DownloadTask> callbackCanceledTasks = new ArrayList<>();
                for (DownloadCall call2 : needCallbackCalls) {
                    callbackCanceledTasks.add(call2.task);
                }
                OkDownload.with().callbackDispatcher().endTasksWithCanceled(callbackCanceledTasks);
            }
        }
    }

    public synchronized DownloadTask findSameTask(DownloadTask task) {
        Util.m83d(TAG, "findSameTask: " + task.getId());
        for (DownloadCall call : this.readyAsyncCalls) {
            if (!call.isCanceled()) {
                if (call.equalsTask(task)) {
                    return call.task;
                }
            }
        }
        for (DownloadCall call2 : this.runningAsyncCalls) {
            if (!call2.isCanceled()) {
                if (call2.equalsTask(task)) {
                    return call2.task;
                }
            }
        }
        for (DownloadCall call3 : this.runningSyncCalls) {
            if (!call3.isCanceled()) {
                if (call3.equalsTask(task)) {
                    return call3.task;
                }
            }
        }
        return null;
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public synchronized boolean isRunning(DownloadTask task) {
        Util.m83d(TAG, "isRunning: " + task.getId());
        for (DownloadCall call : this.runningSyncCalls) {
            if (!call.isCanceled()) {
                if (call.equalsTask(task)) {
                    return true;
                }
            }
        }
        for (DownloadCall call2 : this.runningAsyncCalls) {
            if (!call2.isCanceled()) {
                if (call2.equalsTask(task)) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean isPending(DownloadTask task) {
        Util.m83d(TAG, "isPending: " + task.getId());
        for (DownloadCall call : this.readyAsyncCalls) {
            if (!call.isCanceled()) {
                if (call.equalsTask(task)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void syncRunCall(DownloadCall call) {
        call.run();
    }

    public synchronized void flyingCanceled(DownloadCall call) {
        Util.m83d(TAG, "flying canceled: " + call.task.getId());
        if (call.asyncExecuted) {
            this.flyingCanceledAsyncCallCount.incrementAndGet();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public synchronized void finish(DownloadCall call) {
        Collection<DownloadCall> calls;
        boolean asyncExecuted = call.asyncExecuted;
        if (this.finishingCalls.contains(call)) {
            calls = this.finishingCalls;
        } else if (asyncExecuted) {
            calls = this.runningAsyncCalls;
        } else {
            calls = this.runningSyncCalls;
        }
        if (calls.remove(call)) {
            if (asyncExecuted && call.isCanceled()) {
                this.flyingCanceledAsyncCallCount.decrementAndGet();
            }
            if (asyncExecuted) {
                processCalls();
            }
        } else {
            throw new AssertionError("Call wasn't in-flight!");
        }
    }

    public synchronized boolean isFileConflictAfterRun(DownloadTask task) {
        Util.m83d(TAG, "is file conflict after run: " + task.getId());
        File file = task.getFile();
        if (file == null) {
            return false;
        }
        for (DownloadCall syncCall : this.runningSyncCalls) {
            if (!syncCall.isCanceled()) {
                if (syncCall.task != task) {
                    File otherFile = syncCall.task.getFile();
                    if (otherFile != null && file.equals(otherFile)) {
                        return true;
                    }
                }
            }
        }
        for (DownloadCall asyncCall : this.runningAsyncCalls) {
            if (!asyncCall.isCanceled()) {
                if (asyncCall.task != task) {
                    File otherFile2 = asyncCall.task.getFile();
                    if (otherFile2 != null && file.equals(otherFile2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean inspectForConflict(DownloadTask task) {
        return inspectForConflict(task, (Collection<DownloadTask>) null, (Collection<DownloadTask>) null);
    }

    private boolean inspectForConflict(DownloadTask task, Collection<DownloadTask> sameTaskList, Collection<DownloadTask> fileBusyList) {
        return inspectForConflict(task, this.readyAsyncCalls, sameTaskList, fileBusyList) || inspectForConflict(task, this.runningAsyncCalls, sameTaskList, fileBusyList) || inspectForConflict(task, this.runningSyncCalls, sameTaskList, fileBusyList);
    }

    /* access modifiers changed from: package-private */
    public boolean inspectCompleted(DownloadTask task) {
        return inspectCompleted(task, (Collection<DownloadTask>) null);
    }

    /* access modifiers changed from: package-private */
    public boolean inspectCompleted(DownloadTask task, Collection<DownloadTask> completedCollection) {
        if (!task.isPassIfAlreadyCompleted() || !StatusUtil.isCompleted(task)) {
            return false;
        }
        if (task.getFilename() == null && !OkDownload.with().downloadStrategy().validFilenameFromStore(task)) {
            return false;
        }
        OkDownload.with().downloadStrategy().validInfoOnCompleted(task, this.store);
        if (completedCollection != null) {
            completedCollection.add(task);
            return true;
        }
        OkDownload.with().callbackDispatcher().dispatch().taskEnd(task, EndCause.COMPLETED, (Exception) null);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean inspectForConflict(DownloadTask task, Collection<DownloadCall> calls, Collection<DownloadTask> sameTaskList, Collection<DownloadTask> fileBusyList) {
        CallbackDispatcher callbackDispatcher = OkDownload.with().callbackDispatcher();
        Iterator<DownloadCall> iterator = calls.iterator();
        while (iterator.hasNext()) {
            DownloadCall call = iterator.next();
            if (!call.isCanceled()) {
                if (!call.equalsTask(task)) {
                    File file = call.getFile();
                    File taskFile = task.getFile();
                    if (!(file == null || taskFile == null || !file.equals(taskFile))) {
                        if (fileBusyList != null) {
                            fileBusyList.add(task);
                        } else {
                            callbackDispatcher.dispatch().taskEnd(task, EndCause.FILE_BUSY, (Exception) null);
                        }
                        return true;
                    }
                } else if (call.isFinishing()) {
                    Util.m83d(TAG, "task: " + task.getId() + " is finishing, move it to finishing list");
                    this.finishingCalls.add(call);
                    iterator.remove();
                    return false;
                } else {
                    if (sameTaskList != null) {
                        sameTaskList.add(task);
                    } else {
                        callbackDispatcher.dispatch().taskEnd(task, EndCause.SAME_TASK_BUSY, (Exception) null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    private synchronized void processCalls() {
        if (this.skipProceedCallCount.get() <= 0) {
            if (runningAsyncSize() < this.maxParallelRunningCount) {
                if (!this.readyAsyncCalls.isEmpty()) {
                    Iterator<DownloadCall> i = this.readyAsyncCalls.iterator();
                    while (i.hasNext()) {
                        DownloadCall call = i.next();
                        i.remove();
                        DownloadTask task = call.task;
                        if (isFileConflictAfterRun(task)) {
                            OkDownload.with().callbackDispatcher().dispatch().taskEnd(task, EndCause.FILE_BUSY, (Exception) null);
                        } else {
                            this.runningAsyncCalls.add(call);
                            getExecutorService().execute(call);
                            if (runningAsyncSize() >= this.maxParallelRunningCount) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private int runningAsyncSize() {
        return this.runningAsyncCalls.size() - this.flyingCanceledAsyncCallCount.get();
    }

    public static void setMaxParallelRunningCount(int maxParallelRunningCount2) {
        DownloadDispatcher dispatcher = OkDownload.with().downloadDispatcher();
        if (dispatcher.getClass() == DownloadDispatcher.class) {
            dispatcher.maxParallelRunningCount = Math.max(1, maxParallelRunningCount2);
            return;
        }
        throw new IllegalStateException("The current dispatcher is " + dispatcher + " not DownloadDispatcher exactly!");
    }
}
