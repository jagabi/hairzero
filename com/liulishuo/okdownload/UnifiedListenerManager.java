package com.liulishuo.okdownload;

import android.util.SparseArray;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.assist.ListenerAssist;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnifiedListenerManager {
    final List<Integer> autoRemoveListenerIdList = new ArrayList();
    final DownloadListener hostListener = new DownloadListener() {
        public void taskStart(DownloadTask task) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.taskStart(task);
                    }
                }
            }
        }

        public void connectTrialStart(DownloadTask task, Map<String, List<String>> requestHeaderFields) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.connectTrialStart(task, requestHeaderFields);
                    }
                }
            }
        }

        public void connectTrialEnd(DownloadTask task, int responseCode, Map<String, List<String>> responseHeaderFields) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.connectTrialEnd(task, responseCode, responseHeaderFields);
                    }
                }
            }
        }

        public void downloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.downloadFromBeginning(task, info, cause);
                    }
                }
            }
        }

        public void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.downloadFromBreakpoint(task, info);
                    }
                }
            }
        }

        public void connectStart(DownloadTask task, int blockIndex, Map<String, List<String>> requestHeaderFields) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.connectStart(task, blockIndex, requestHeaderFields);
                    }
                }
            }
        }

        public void connectEnd(DownloadTask task, int blockIndex, int responseCode, Map<String, List<String>> responseHeaderFields) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.connectEnd(task, blockIndex, responseCode, responseHeaderFields);
                    }
                }
            }
        }

        public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.fetchStart(task, blockIndex, contentLength);
                    }
                }
            }
        }

        public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.fetchProgress(task, blockIndex, increaseBytes);
                    }
                }
            }
        }

        public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.fetchEnd(task, blockIndex, contentLength);
                    }
                }
            }
        }

        public void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
            DownloadListener[] listeners = UnifiedListenerManager.getThreadSafeArray(task, UnifiedListenerManager.this.realListenerMap);
            if (listeners != null) {
                for (DownloadListener realOne : listeners) {
                    if (realOne != null) {
                        realOne.taskEnd(task, cause, realCause);
                    }
                }
                if (UnifiedListenerManager.this.autoRemoveListenerIdList.contains(Integer.valueOf(task.getId()))) {
                    UnifiedListenerManager.this.detachListener(task.getId());
                }
            }
        }
    };
    final SparseArray<ArrayList<DownloadListener>> realListenerMap = new SparseArray<>();

    public synchronized void detachListener(int id) {
        this.realListenerMap.remove(id);
    }

    public synchronized void addAutoRemoveListenersWhenTaskEnd(int id) {
        if (!this.autoRemoveListenerIdList.contains(Integer.valueOf(id))) {
            this.autoRemoveListenerIdList.add(Integer.valueOf(id));
        }
    }

    public synchronized void removeAutoRemoveListenersWhenTaskEnd(int id) {
        this.autoRemoveListenerIdList.remove(Integer.valueOf(id));
    }

    public synchronized void detachListener(DownloadListener listener) {
        int count = this.realListenerMap.size();
        List<Integer> needRemoveKeyList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            List<DownloadListener> listenerList = this.realListenerMap.valueAt(i);
            if (listenerList != null) {
                listenerList.remove(listener);
                if (listenerList.isEmpty()) {
                    needRemoveKeyList.add(Integer.valueOf(this.realListenerMap.keyAt(i)));
                }
            }
        }
        for (Integer intValue : needRemoveKeyList) {
            this.realListenerMap.remove(intValue.intValue());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean detachListener(com.liulishuo.okdownload.DownloadTask r5, com.liulishuo.okdownload.DownloadListener r6) {
        /*
            r4 = this;
            monitor-enter(r4)
            int r0 = r5.getId()     // Catch:{ all -> 0x0023 }
            android.util.SparseArray<java.util.ArrayList<com.liulishuo.okdownload.DownloadListener>> r1 = r4.realListenerMap     // Catch:{ all -> 0x0023 }
            java.lang.Object r1 = r1.get(r0)     // Catch:{ all -> 0x0023 }
            java.util.List r1 = (java.util.List) r1     // Catch:{ all -> 0x0023 }
            if (r1 != 0) goto L_0x0012
            r2 = 0
            monitor-exit(r4)
            return r2
        L_0x0012:
            boolean r2 = r1.remove(r6)     // Catch:{ all -> 0x0023 }
            boolean r3 = r1.isEmpty()     // Catch:{ all -> 0x0023 }
            if (r3 == 0) goto L_0x0021
            android.util.SparseArray<java.util.ArrayList<com.liulishuo.okdownload.DownloadListener>> r3 = r4.realListenerMap     // Catch:{ all -> 0x0023 }
            r3.remove(r0)     // Catch:{ all -> 0x0023 }
        L_0x0021:
            monitor-exit(r4)
            return r2
        L_0x0023:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.UnifiedListenerManager.detachListener(com.liulishuo.okdownload.DownloadTask, com.liulishuo.okdownload.DownloadListener):boolean");
    }

    public synchronized void attachListener(DownloadTask task, DownloadListener listener) {
        int id = task.getId();
        ArrayList<DownloadListener> listenerList = this.realListenerMap.get(id);
        if (listenerList == null) {
            listenerList = new ArrayList<>();
            this.realListenerMap.put(id, listenerList);
        }
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
            if (listener instanceof ListenerAssist) {
                ((ListenerAssist) listener).setAlwaysRecoverAssistModelIfNotSet(true);
            }
        }
    }

    public synchronized void attachAndEnqueueIfNotRun(DownloadTask task, DownloadListener listener) {
        attachListener(task, listener);
        if (!isTaskPendingOrRunning(task)) {
            task.enqueue(this.hostListener);
        }
    }

    public synchronized void enqueueTaskWithUnifiedListener(DownloadTask task, DownloadListener listener) {
        attachListener(task, listener);
        task.enqueue(this.hostListener);
    }

    public synchronized void executeTaskWithUnifiedListener(DownloadTask task, DownloadListener listener) {
        attachListener(task, listener);
        task.execute(this.hostListener);
    }

    public DownloadListener getHostListener() {
        return this.hostListener;
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskPendingOrRunning(DownloadTask task) {
        return StatusUtil.isSameTaskPendingOrRunning(task);
    }

    /* access modifiers changed from: private */
    public static DownloadListener[] getThreadSafeArray(DownloadTask task, SparseArray<ArrayList<DownloadListener>> realListenerMap2) {
        ArrayList<DownloadListener> listenerList = realListenerMap2.get(task.getId());
        if (listenerList == null || listenerList.size() <= 0) {
            return null;
        }
        DownloadListener[] copyList = new DownloadListener[listenerList.size()];
        listenerList.toArray(copyList);
        return copyList;
    }
}
