package com.liulishuo.okdownload;

import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener2;
import com.liulishuo.okdownload.core.listener.DownloadListenerBunch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadSerialQueue extends DownloadListener2 implements Runnable {
    static final int ID_INVALID = 0;
    private static final Executor SERIAL_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload DynamicSerial", false));
    private static final String TAG = "DownloadSerialQueue";
    DownloadListenerBunch listenerBunch;
    volatile boolean looping;
    volatile boolean paused;
    volatile DownloadTask runningTask;
    volatile boolean shutedDown;
    private final ArrayList<DownloadTask> taskList;

    public DownloadSerialQueue() {
        this((DownloadListener) null);
    }

    DownloadSerialQueue(DownloadListener listener, ArrayList<DownloadTask> taskList2) {
        this.shutedDown = false;
        this.looping = false;
        this.paused = false;
        this.listenerBunch = new DownloadListenerBunch.Builder().append(this).append(listener).build();
        this.taskList = taskList2;
    }

    public DownloadSerialQueue(DownloadListener listener) {
        this(listener, new ArrayList());
    }

    public void setListener(DownloadListener listener) {
        this.listenerBunch = new DownloadListenerBunch.Builder().append(this).append(listener).build();
    }

    public synchronized void enqueue(DownloadTask task) {
        this.taskList.add(task);
        Collections.sort(this.taskList);
        if (!this.paused && !this.looping) {
            this.looping = true;
            startNewLooper();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0043, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void pause() {
        /*
            r3 = this;
            monitor-enter(r3)
            boolean r0 = r3.paused     // Catch:{ all -> 0x0044 }
            if (r0 == 0) goto L_0x002b
            java.lang.String r0 = "DownloadSerialQueue"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0044 }
            r1.<init>()     // Catch:{ all -> 0x0044 }
            java.lang.String r2 = "require pause this queue(remain "
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0044 }
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r2 = r3.taskList     // Catch:{ all -> 0x0044 }
            int r2 = r2.size()     // Catch:{ all -> 0x0044 }
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0044 }
            java.lang.String r2 = "), butit has already been paused"
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0044 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0044 }
            com.liulishuo.okdownload.core.Util.m86w(r0, r1)     // Catch:{ all -> 0x0044 }
            monitor-exit(r3)
            return
        L_0x002b:
            r0 = 1
            r3.paused = r0     // Catch:{ all -> 0x0044 }
            com.liulishuo.okdownload.DownloadTask r0 = r3.runningTask     // Catch:{ all -> 0x0044 }
            if (r0 == 0) goto L_0x0042
            com.liulishuo.okdownload.DownloadTask r0 = r3.runningTask     // Catch:{ all -> 0x0044 }
            r0.cancel()     // Catch:{ all -> 0x0044 }
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r0 = r3.taskList     // Catch:{ all -> 0x0044 }
            r1 = 0
            com.liulishuo.okdownload.DownloadTask r2 = r3.runningTask     // Catch:{ all -> 0x0044 }
            r0.add(r1, r2)     // Catch:{ all -> 0x0044 }
            r0 = 0
            r3.runningTask = r0     // Catch:{ all -> 0x0044 }
        L_0x0042:
            monitor-exit(r3)
            return
        L_0x0044:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.DownloadSerialQueue.pause():void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0041, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void resume() {
        /*
            r3 = this;
            monitor-enter(r3)
            boolean r0 = r3.paused     // Catch:{ all -> 0x0042 }
            if (r0 != 0) goto L_0x002b
            java.lang.String r0 = "DownloadSerialQueue"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0042 }
            r1.<init>()     // Catch:{ all -> 0x0042 }
            java.lang.String r2 = "require resume this queue(remain "
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0042 }
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r2 = r3.taskList     // Catch:{ all -> 0x0042 }
            int r2 = r2.size()     // Catch:{ all -> 0x0042 }
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0042 }
            java.lang.String r2 = "), but it is still running"
            java.lang.StringBuilder r1 = r1.append(r2)     // Catch:{ all -> 0x0042 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0042 }
            com.liulishuo.okdownload.core.Util.m86w(r0, r1)     // Catch:{ all -> 0x0042 }
            monitor-exit(r3)
            return
        L_0x002b:
            r0 = 0
            r3.paused = r0     // Catch:{ all -> 0x0042 }
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r0 = r3.taskList     // Catch:{ all -> 0x0042 }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x0042 }
            if (r0 != 0) goto L_0x0040
            boolean r0 = r3.looping     // Catch:{ all -> 0x0042 }
            if (r0 != 0) goto L_0x0040
            r0 = 1
            r3.looping = r0     // Catch:{ all -> 0x0042 }
            r3.startNewLooper()     // Catch:{ all -> 0x0042 }
        L_0x0040:
            monitor-exit(r3)
            return
        L_0x0042:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.DownloadSerialQueue.resume():void");
    }

    public int getWorkingTaskId() {
        if (this.runningTask != null) {
            return this.runningTask.getId();
        }
        return 0;
    }

    public int getWaitingTaskCount() {
        return this.taskList.size();
    }

    public synchronized DownloadTask[] shutdown() {
        DownloadTask[] tasks;
        this.shutedDown = true;
        if (this.runningTask != null) {
            this.runningTask.cancel();
        }
        tasks = new DownloadTask[this.taskList.size()];
        this.taskList.toArray(tasks);
        this.taskList.clear();
        return tasks;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        r0.execute(r2.listenerBunch);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
            r2 = this;
        L_0x0000:
            boolean r0 = r2.shutedDown
            if (r0 != 0) goto L_0x002c
            monitor-enter(r2)
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r0 = r2.taskList     // Catch:{ all -> 0x0029 }
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x0029 }
            r1 = 0
            if (r0 != 0) goto L_0x0022
            boolean r0 = r2.paused     // Catch:{ all -> 0x0029 }
            if (r0 == 0) goto L_0x0013
            goto L_0x0022
        L_0x0013:
            java.util.ArrayList<com.liulishuo.okdownload.DownloadTask> r0 = r2.taskList     // Catch:{ all -> 0x0029 }
            java.lang.Object r0 = r0.remove(r1)     // Catch:{ all -> 0x0029 }
            com.liulishuo.okdownload.DownloadTask r0 = (com.liulishuo.okdownload.DownloadTask) r0     // Catch:{ all -> 0x0029 }
            monitor-exit(r2)     // Catch:{ all -> 0x0029 }
            com.liulishuo.okdownload.core.listener.DownloadListenerBunch r1 = r2.listenerBunch
            r0.execute(r1)
            goto L_0x0000
        L_0x0022:
            r0 = 0
            r2.runningTask = r0     // Catch:{ all -> 0x0029 }
            r2.looping = r1     // Catch:{ all -> 0x0029 }
            monitor-exit(r2)     // Catch:{ all -> 0x0029 }
            goto L_0x002c
        L_0x0029:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0029 }
            throw r0
        L_0x002c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.DownloadSerialQueue.run():void");
    }

    /* access modifiers changed from: package-private */
    public void startNewLooper() {
        SERIAL_EXECUTOR.execute(this);
    }

    public void taskStart(DownloadTask task) {
        this.runningTask = task;
    }

    public synchronized void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
        if (cause != EndCause.CANCELED && task == this.runningTask) {
            this.runningTask = null;
        }
    }
}
