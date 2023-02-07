package com.liulishuo.okdownload;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.IdentifiedTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener2;
import com.liulishuo.okdownload.core.listener.DownloadListenerBunch;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadContext {
    private static final Executor SERIAL_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 30, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload Serial", false));
    private static final String TAG = "DownloadContext";
    final DownloadContextListener contextListener;
    private final QueueSet set;
    volatile boolean started;
    /* access modifiers changed from: private */
    public final DownloadTask[] tasks;
    private Handler uiHandler;

    DownloadContext(DownloadTask[] tasks2, DownloadContextListener contextListener2, QueueSet set2, Handler uiHandler2) {
        this(tasks2, contextListener2, set2);
        this.uiHandler = uiHandler2;
    }

    DownloadContext(DownloadTask[] tasks2, DownloadContextListener contextListener2, QueueSet set2) {
        this.started = false;
        this.tasks = tasks2;
        this.contextListener = contextListener2;
        this.set = set2;
    }

    public boolean isStarted() {
        return this.started;
    }

    public DownloadTask[] getTasks() {
        return this.tasks;
    }

    public void startOnSerial(DownloadListener listener) {
        start(listener, true);
    }

    public void startOnParallel(DownloadListener listener) {
        start(listener, false);
    }

    public void start(DownloadListener listener, boolean isSerial) {
        final DownloadListener targetListener;
        long startTime = SystemClock.uptimeMillis();
        Util.m83d(TAG, "start " + isSerial);
        this.started = true;
        if (this.contextListener != null) {
            targetListener = new DownloadListenerBunch.Builder().append(listener).append(new QueueAttachListener(this, this.contextListener, this.tasks.length)).build();
        } else {
            targetListener = listener;
        }
        if (isSerial) {
            final List<DownloadTask> scheduleTaskList = new ArrayList<>();
            Collections.addAll(scheduleTaskList, this.tasks);
            Collections.sort(scheduleTaskList);
            executeOnSerialExecutor(new Runnable() {
                public void run() {
                    for (DownloadTask task : scheduleTaskList) {
                        if (!DownloadContext.this.isStarted()) {
                            DownloadContext.this.callbackQueueEndOnSerialLoop(task.isAutoCallbackToUIThread());
                            return;
                        }
                        task.execute(targetListener);
                    }
                }
            });
        } else {
            DownloadTask.enqueue(this.tasks, targetListener);
        }
        Util.m83d(TAG, "start finish " + isSerial + " " + (SystemClock.uptimeMillis() - startTime) + "ms");
    }

    public AlterContext alter() {
        return new AlterContext(this);
    }

    public void stop() {
        if (this.started) {
            OkDownload.with().downloadDispatcher().cancel((IdentifiedTask[]) this.tasks);
        }
        this.started = false;
    }

    /* access modifiers changed from: private */
    public void callbackQueueEndOnSerialLoop(boolean isAutoCallbackToUIThread) {
        DownloadContextListener downloadContextListener = this.contextListener;
        if (downloadContextListener != null) {
            if (isAutoCallbackToUIThread) {
                if (this.uiHandler == null) {
                    this.uiHandler = new Handler(Looper.getMainLooper());
                }
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        DownloadContext.this.contextListener.queueEnd(DownloadContext.this);
                    }
                });
                return;
            }
            downloadContextListener.queueEnd(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void executeOnSerialExecutor(Runnable runnable) {
        SERIAL_EXECUTOR.execute(runnable);
    }

    public Builder toBuilder() {
        return new Builder(this.set, new ArrayList(Arrays.asList(this.tasks))).setListener(this.contextListener);
    }

    public static class Builder {
        final ArrayList<DownloadTask> boundTaskList;
        private DownloadContextListener listener;
        private final QueueSet set;

        public Builder() {
            this(new QueueSet());
        }

        public Builder(QueueSet set2) {
            this(set2, new ArrayList());
        }

        public Builder(QueueSet set2, ArrayList<DownloadTask> taskArrayList) {
            this.set = set2;
            this.boundTaskList = taskArrayList;
        }

        public Builder setListener(DownloadContextListener listener2) {
            this.listener = listener2;
            return this;
        }

        public Builder bindSetTask(DownloadTask task) {
            int index = this.boundTaskList.indexOf(task);
            if (index >= 0) {
                this.boundTaskList.set(index, task);
            } else {
                this.boundTaskList.add(task);
            }
            return this;
        }

        public DownloadTask bind(String url) {
            if (this.set.uri != null) {
                return bind(new DownloadTask.Builder(url, this.set.uri).setFilenameFromResponse(true));
            }
            throw new IllegalArgumentException("If you want to bind only with url, you have to provide parentPath on QueueSet!");
        }

        public DownloadTask bind(DownloadTask.Builder taskBuilder) {
            if (this.set.headerMapFields != null) {
                taskBuilder.setHeaderMapFields(this.set.headerMapFields);
            }
            if (this.set.readBufferSize != null) {
                taskBuilder.setReadBufferSize(this.set.readBufferSize.intValue());
            }
            if (this.set.flushBufferSize != null) {
                taskBuilder.setFlushBufferSize(this.set.flushBufferSize.intValue());
            }
            if (this.set.syncBufferSize != null) {
                taskBuilder.setSyncBufferSize(this.set.syncBufferSize.intValue());
            }
            if (this.set.wifiRequired != null) {
                taskBuilder.setWifiRequired(this.set.wifiRequired.booleanValue());
            }
            if (this.set.syncBufferIntervalMillis != null) {
                taskBuilder.setSyncBufferIntervalMillis(this.set.syncBufferIntervalMillis.intValue());
            }
            if (this.set.autoCallbackToUIThread != null) {
                taskBuilder.setAutoCallbackToUIThread(this.set.autoCallbackToUIThread.booleanValue());
            }
            if (this.set.minIntervalMillisCallbackProcess != null) {
                taskBuilder.setMinIntervalMillisCallbackProcess(this.set.minIntervalMillisCallbackProcess.intValue());
            }
            if (this.set.passIfAlreadyCompleted != null) {
                taskBuilder.setPassIfAlreadyCompleted(this.set.passIfAlreadyCompleted.booleanValue());
            }
            DownloadTask task = taskBuilder.build();
            if (this.set.tag != null) {
                task.setTag(this.set.tag);
            }
            this.boundTaskList.add(task);
            return task;
        }

        public void unbind(DownloadTask task) {
            this.boundTaskList.remove(task);
        }

        public void unbind(int id) {
            for (DownloadTask task : (List) this.boundTaskList.clone()) {
                if (task.getId() == id) {
                    this.boundTaskList.remove(task);
                }
            }
        }

        public DownloadContext build() {
            return new DownloadContext((DownloadTask[]) this.boundTaskList.toArray(new DownloadTask[this.boundTaskList.size()]), this.listener, this.set);
        }
    }

    public static class QueueSet {
        /* access modifiers changed from: private */
        public Boolean autoCallbackToUIThread;
        /* access modifiers changed from: private */
        public Integer flushBufferSize;
        /* access modifiers changed from: private */
        public Map<String, List<String>> headerMapFields;
        /* access modifiers changed from: private */
        public Integer minIntervalMillisCallbackProcess;
        /* access modifiers changed from: private */
        public Boolean passIfAlreadyCompleted;
        /* access modifiers changed from: private */
        public Integer readBufferSize;
        /* access modifiers changed from: private */
        public Integer syncBufferIntervalMillis;
        /* access modifiers changed from: private */
        public Integer syncBufferSize;
        /* access modifiers changed from: private */
        public Object tag;
        /* access modifiers changed from: private */
        public Uri uri;
        /* access modifiers changed from: private */
        public Boolean wifiRequired;

        public Map<String, List<String>> getHeaderMapFields() {
            return this.headerMapFields;
        }

        public void setHeaderMapFields(Map<String, List<String>> headerMapFields2) {
            this.headerMapFields = headerMapFields2;
        }

        public Uri getDirUri() {
            return this.uri;
        }

        public QueueSet setParentPathUri(Uri uri2) {
            this.uri = uri2;
            return this;
        }

        public QueueSet setParentPathFile(File parentPathFile) {
            if (!parentPathFile.isFile()) {
                this.uri = Uri.fromFile(parentPathFile);
                return this;
            }
            throw new IllegalArgumentException("parent path only accept directory path");
        }

        public QueueSet setParentPath(String parentPath) {
            return setParentPathFile(new File(parentPath));
        }

        public int getReadBufferSize() {
            Integer num = this.readBufferSize;
            if (num == null) {
                return 4096;
            }
            return num.intValue();
        }

        public QueueSet setReadBufferSize(int readBufferSize2) {
            this.readBufferSize = Integer.valueOf(readBufferSize2);
            return this;
        }

        public QueueSet setWifiRequired(Boolean wifiRequired2) {
            this.wifiRequired = wifiRequired2;
            return this;
        }

        public boolean isWifiRequired() {
            Boolean bool = this.wifiRequired;
            if (bool == null) {
                return false;
            }
            return bool.booleanValue();
        }

        public int getFlushBufferSize() {
            Integer num = this.flushBufferSize;
            if (num == null) {
                return 16384;
            }
            return num.intValue();
        }

        public QueueSet setFlushBufferSize(int flushBufferSize2) {
            this.flushBufferSize = Integer.valueOf(flushBufferSize2);
            return this;
        }

        public int getSyncBufferSize() {
            Integer num = this.syncBufferSize;
            if (num == null) {
                return 65536;
            }
            return num.intValue();
        }

        public QueueSet setSyncBufferSize(int syncBufferSize2) {
            this.syncBufferSize = Integer.valueOf(syncBufferSize2);
            return this;
        }

        public int getSyncBufferIntervalMillis() {
            Integer num = this.syncBufferIntervalMillis;
            if (num == null) {
                return 2000;
            }
            return num.intValue();
        }

        public QueueSet setSyncBufferIntervalMillis(int syncBufferIntervalMillis2) {
            this.syncBufferIntervalMillis = Integer.valueOf(syncBufferIntervalMillis2);
            return this;
        }

        public boolean isAutoCallbackToUIThread() {
            Boolean bool = this.autoCallbackToUIThread;
            if (bool == null) {
                return true;
            }
            return bool.booleanValue();
        }

        public QueueSet setAutoCallbackToUIThread(Boolean autoCallbackToUIThread2) {
            this.autoCallbackToUIThread = autoCallbackToUIThread2;
            return this;
        }

        public int getMinIntervalMillisCallbackProcess() {
            Integer num = this.minIntervalMillisCallbackProcess;
            if (num == null) {
                return 3000;
            }
            return num.intValue();
        }

        public QueueSet setMinIntervalMillisCallbackProcess(Integer minIntervalMillisCallbackProcess2) {
            this.minIntervalMillisCallbackProcess = minIntervalMillisCallbackProcess2;
            return this;
        }

        public Object getTag() {
            return this.tag;
        }

        public QueueSet setTag(Object tag2) {
            this.tag = tag2;
            return this;
        }

        public boolean isPassIfAlreadyCompleted() {
            Boolean bool = this.passIfAlreadyCompleted;
            if (bool == null) {
                return true;
            }
            return bool.booleanValue();
        }

        public QueueSet setPassIfAlreadyCompleted(boolean passIfAlreadyCompleted2) {
            this.passIfAlreadyCompleted = Boolean.valueOf(passIfAlreadyCompleted2);
            return this;
        }

        public Builder commit() {
            return new Builder(this);
        }
    }

    static class QueueAttachListener extends DownloadListener2 {
        private final DownloadContextListener contextListener;
        private final DownloadContext hostContext;
        private final AtomicInteger remainCount;

        QueueAttachListener(DownloadContext context, DownloadContextListener contextListener2, int taskCount) {
            this.remainCount = new AtomicInteger(taskCount);
            this.contextListener = contextListener2;
            this.hostContext = context;
        }

        public void taskStart(DownloadTask task) {
        }

        public void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
            int remainCount2 = this.remainCount.decrementAndGet();
            this.contextListener.taskEnd(this.hostContext, task, cause, realCause, remainCount2);
            if (remainCount2 <= 0) {
                this.contextListener.queueEnd(this.hostContext);
                Util.m83d(DownloadContext.TAG, "taskEnd and remainCount " + remainCount2);
            }
        }
    }

    public static class AlterContext {
        private final DownloadContext context;

        AlterContext(DownloadContext context2) {
            this.context = context2;
        }

        public AlterContext replaceTask(DownloadTask oldTask, DownloadTask newTask) {
            DownloadTask[] tasks = this.context.tasks;
            for (int i = 0; i < tasks.length; i++) {
                if (tasks[i] == oldTask) {
                    tasks[i] = newTask;
                }
            }
            return this;
        }
    }
}
