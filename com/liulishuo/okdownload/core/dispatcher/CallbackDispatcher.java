package com.liulishuo.okdownload.core.dispatcher;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadMonitor;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CallbackDispatcher {
    private static final String TAG = "CallbackDispatcher";
    private final DownloadListener transmit;
    private final Handler uiHandler;

    CallbackDispatcher(Handler handler, DownloadListener transmit2) {
        this.uiHandler = handler;
        this.transmit = transmit2;
    }

    public CallbackDispatcher() {
        Handler handler = new Handler(Looper.getMainLooper());
        this.uiHandler = handler;
        this.transmit = new DefaultTransmitListener(handler);
    }

    public boolean isFetchProcessMoment(DownloadTask task) {
        long minInterval = (long) task.getMinIntervalMillisCallbackProcess();
        return minInterval <= 0 || SystemClock.uptimeMillis() - DownloadTask.TaskHideWrapper.getLastCallbackProcessTs(task) >= minInterval;
    }

    public void endTasksWithError(final Collection<DownloadTask> errorCollection, final Exception realCause) {
        if (errorCollection.size() > 0) {
            Util.m83d(TAG, "endTasksWithError error[" + errorCollection.size() + "] realCause: " + realCause);
            Iterator<DownloadTask> iterator = errorCollection.iterator();
            while (iterator.hasNext()) {
                DownloadTask task = iterator.next();
                if (!task.isAutoCallbackToUIThread()) {
                    task.getListener().taskEnd(task, EndCause.ERROR, realCause);
                    iterator.remove();
                }
            }
            this.uiHandler.post(new Runnable() {
                public void run() {
                    for (DownloadTask task : errorCollection) {
                        task.getListener().taskEnd(task, EndCause.ERROR, realCause);
                    }
                }
            });
        }
    }

    public void endTasks(final Collection<DownloadTask> completedTaskCollection, final Collection<DownloadTask> sameTaskConflictCollection, final Collection<DownloadTask> fileBusyCollection) {
        if (completedTaskCollection.size() != 0 || sameTaskConflictCollection.size() != 0 || fileBusyCollection.size() != 0) {
            Util.m83d(TAG, "endTasks completed[" + completedTaskCollection.size() + "] sameTask[" + sameTaskConflictCollection.size() + "] fileBusy[" + fileBusyCollection.size() + "]");
            if (completedTaskCollection.size() > 0) {
                Iterator<DownloadTask> iterator = completedTaskCollection.iterator();
                while (iterator.hasNext()) {
                    DownloadTask task = iterator.next();
                    if (!task.isAutoCallbackToUIThread()) {
                        task.getListener().taskEnd(task, EndCause.COMPLETED, (Exception) null);
                        iterator.remove();
                    }
                }
            }
            if (sameTaskConflictCollection.size() > 0) {
                Iterator<DownloadTask> iterator2 = sameTaskConflictCollection.iterator();
                while (iterator2.hasNext()) {
                    DownloadTask task2 = iterator2.next();
                    if (!task2.isAutoCallbackToUIThread()) {
                        task2.getListener().taskEnd(task2, EndCause.SAME_TASK_BUSY, (Exception) null);
                        iterator2.remove();
                    }
                }
            }
            if (fileBusyCollection.size() > 0) {
                Iterator<DownloadTask> iterator3 = fileBusyCollection.iterator();
                while (iterator3.hasNext()) {
                    DownloadTask task3 = iterator3.next();
                    if (!task3.isAutoCallbackToUIThread()) {
                        task3.getListener().taskEnd(task3, EndCause.FILE_BUSY, (Exception) null);
                        iterator3.remove();
                    }
                }
            }
            if (completedTaskCollection.size() != 0 || sameTaskConflictCollection.size() != 0 || fileBusyCollection.size() != 0) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        for (DownloadTask task : completedTaskCollection) {
                            task.getListener().taskEnd(task, EndCause.COMPLETED, (Exception) null);
                        }
                        for (DownloadTask task2 : sameTaskConflictCollection) {
                            task2.getListener().taskEnd(task2, EndCause.SAME_TASK_BUSY, (Exception) null);
                        }
                        for (DownloadTask task3 : fileBusyCollection) {
                            task3.getListener().taskEnd(task3, EndCause.FILE_BUSY, (Exception) null);
                        }
                    }
                });
            }
        }
    }

    public void endTasksWithCanceled(final Collection<DownloadTask> canceledCollection) {
        if (canceledCollection.size() > 0) {
            Util.m83d(TAG, "endTasksWithCanceled canceled[" + canceledCollection.size() + "]");
            Iterator<DownloadTask> iterator = canceledCollection.iterator();
            while (iterator.hasNext()) {
                DownloadTask task = iterator.next();
                if (!task.isAutoCallbackToUIThread()) {
                    task.getListener().taskEnd(task, EndCause.CANCELED, (Exception) null);
                    iterator.remove();
                }
            }
            this.uiHandler.post(new Runnable() {
                public void run() {
                    for (DownloadTask task : canceledCollection) {
                        task.getListener().taskEnd(task, EndCause.CANCELED, (Exception) null);
                    }
                }
            });
        }
    }

    public DownloadListener dispatch() {
        return this.transmit;
    }

    static class DefaultTransmitListener implements DownloadListener {
        private final Handler uiHandler;

        DefaultTransmitListener(Handler uiHandler2) {
            this.uiHandler = uiHandler2;
        }

        public void taskStart(final DownloadTask task) {
            Util.m83d(CallbackDispatcher.TAG, "taskStart: " + task.getId());
            inspectTaskStart(task);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().taskStart(task);
                    }
                });
            } else {
                task.getListener().taskStart(task);
            }
        }

        public void connectTrialStart(final DownloadTask task, final Map<String, List<String>> headerFields) {
            Util.m83d(CallbackDispatcher.TAG, "-----> start trial task(" + task.getId() + ") " + headerFields);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().connectTrialStart(task, headerFields);
                    }
                });
            } else {
                task.getListener().connectTrialStart(task, headerFields);
            }
        }

        public void connectTrialEnd(final DownloadTask task, final int responseCode, final Map<String, List<String>> headerFields) {
            Util.m83d(CallbackDispatcher.TAG, "<----- finish trial task(" + task.getId() + ") code[" + responseCode + "]" + headerFields);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().connectTrialEnd(task, responseCode, headerFields);
                    }
                });
            } else {
                task.getListener().connectTrialEnd(task, responseCode, headerFields);
            }
        }

        public void downloadFromBeginning(final DownloadTask task, final BreakpointInfo info, final ResumeFailedCause cause) {
            Util.m83d(CallbackDispatcher.TAG, "downloadFromBeginning: " + task.getId());
            inspectDownloadFromBeginning(task, info, cause);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().downloadFromBeginning(task, info, cause);
                    }
                });
            } else {
                task.getListener().downloadFromBeginning(task, info, cause);
            }
        }

        public void downloadFromBreakpoint(final DownloadTask task, final BreakpointInfo info) {
            Util.m83d(CallbackDispatcher.TAG, "downloadFromBreakpoint: " + task.getId());
            inspectDownloadFromBreakpoint(task, info);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().downloadFromBreakpoint(task, info);
                    }
                });
            } else {
                task.getListener().downloadFromBreakpoint(task, info);
            }
        }

        public void connectStart(final DownloadTask task, final int blockIndex, final Map<String, List<String>> requestHeaderFields) {
            Util.m83d(CallbackDispatcher.TAG, "-----> start connection task(" + task.getId() + ") block(" + blockIndex + ") " + requestHeaderFields);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().connectStart(task, blockIndex, requestHeaderFields);
                    }
                });
            } else {
                task.getListener().connectStart(task, blockIndex, requestHeaderFields);
            }
        }

        public void connectEnd(DownloadTask task, int blockIndex, int responseCode, Map<String, List<String>> requestHeaderFields) {
            Util.m83d(CallbackDispatcher.TAG, "<----- finish connection task(" + task.getId() + ") block(" + blockIndex + ") code[" + responseCode + "]" + requestHeaderFields);
            if (task.isAutoCallbackToUIThread()) {
                final DownloadTask downloadTask = task;
                final int i = blockIndex;
                final int i2 = responseCode;
                final Map<String, List<String>> map = requestHeaderFields;
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        downloadTask.getListener().connectEnd(downloadTask, i, i2, map);
                    }
                });
                return;
            }
            task.getListener().connectEnd(task, blockIndex, responseCode, requestHeaderFields);
        }

        public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
            Util.m83d(CallbackDispatcher.TAG, "fetchStart: " + task.getId());
            if (task.isAutoCallbackToUIThread()) {
                final DownloadTask downloadTask = task;
                final int i = blockIndex;
                final long j = contentLength;
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        downloadTask.getListener().fetchStart(downloadTask, i, j);
                    }
                });
                return;
            }
            task.getListener().fetchStart(task, blockIndex, contentLength);
        }

        public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
            if (task.getMinIntervalMillisCallbackProcess() > 0) {
                DownloadTask.TaskHideWrapper.setLastCallbackProcessTs(task, SystemClock.uptimeMillis());
            }
            if (task.isAutoCallbackToUIThread()) {
                final DownloadTask downloadTask = task;
                final int i = blockIndex;
                final long j = increaseBytes;
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        downloadTask.getListener().fetchProgress(downloadTask, i, j);
                    }
                });
                return;
            }
            task.getListener().fetchProgress(task, blockIndex, increaseBytes);
        }

        public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
            Util.m83d(CallbackDispatcher.TAG, "fetchEnd: " + task.getId());
            if (task.isAutoCallbackToUIThread()) {
                final DownloadTask downloadTask = task;
                final int i = blockIndex;
                final long j = contentLength;
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        downloadTask.getListener().fetchEnd(downloadTask, i, j);
                    }
                });
                return;
            }
            task.getListener().fetchEnd(task, blockIndex, contentLength);
        }

        public void taskEnd(final DownloadTask task, final EndCause cause, final Exception realCause) {
            if (cause == EndCause.ERROR) {
                Util.m83d(CallbackDispatcher.TAG, "taskEnd: " + task.getId() + " " + cause + " " + realCause);
            }
            inspectTaskEnd(task, cause, realCause);
            if (task.isAutoCallbackToUIThread()) {
                this.uiHandler.post(new Runnable() {
                    public void run() {
                        task.getListener().taskEnd(task, cause, realCause);
                    }
                });
            } else {
                task.getListener().taskEnd(task, cause, realCause);
            }
        }

        /* access modifiers changed from: package-private */
        public void inspectDownloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
            DownloadMonitor monitor = OkDownload.with().getMonitor();
            if (monitor != null) {
                monitor.taskDownloadFromBreakpoint(task, info);
            }
        }

        /* access modifiers changed from: package-private */
        public void inspectDownloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
            DownloadMonitor monitor = OkDownload.with().getMonitor();
            if (monitor != null) {
                monitor.taskDownloadFromBeginning(task, info, cause);
            }
        }

        /* access modifiers changed from: package-private */
        public void inspectTaskStart(DownloadTask task) {
            DownloadMonitor monitor = OkDownload.with().getMonitor();
            if (monitor != null) {
                monitor.taskStart(task);
            }
        }

        /* access modifiers changed from: package-private */
        public void inspectTaskEnd(DownloadTask task, EndCause cause, Exception realCause) {
            DownloadMonitor monitor = OkDownload.with().getMonitor();
            if (monitor != null) {
                monitor.taskEnd(task, cause, realCause);
            }
        }
    }
}
