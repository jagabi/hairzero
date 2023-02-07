package com.liulishuo.okdownload.core.listener;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadListenerBunch implements DownloadListener {
    final DownloadListener[] listenerList;

    DownloadListenerBunch(DownloadListener[] listenerList2) {
        this.listenerList = listenerList2;
    }

    public void taskStart(DownloadTask task) {
        for (DownloadListener listener : this.listenerList) {
            listener.taskStart(task);
        }
    }

    public void connectTrialStart(DownloadTask task, Map<String, List<String>> requestHeaderFields) {
        for (DownloadListener listener : this.listenerList) {
            listener.connectTrialStart(task, requestHeaderFields);
        }
    }

    public void connectTrialEnd(DownloadTask task, int responseCode, Map<String, List<String>> responseHeaderFields) {
        for (DownloadListener listener : this.listenerList) {
            listener.connectTrialEnd(task, responseCode, responseHeaderFields);
        }
    }

    public void downloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
        for (DownloadListener listener : this.listenerList) {
            listener.downloadFromBeginning(task, info, cause);
        }
    }

    public void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
        for (DownloadListener listener : this.listenerList) {
            listener.downloadFromBreakpoint(task, info);
        }
    }

    public void connectStart(DownloadTask task, int blockIndex, Map<String, List<String>> requestHeaderFields) {
        for (DownloadListener listener : this.listenerList) {
            listener.connectStart(task, blockIndex, requestHeaderFields);
        }
    }

    public void connectEnd(DownloadTask task, int blockIndex, int responseCode, Map<String, List<String>> responseHeaderFields) {
        for (DownloadListener listener : this.listenerList) {
            listener.connectEnd(task, blockIndex, responseCode, responseHeaderFields);
        }
    }

    public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
        for (DownloadListener listener : this.listenerList) {
            listener.fetchStart(task, blockIndex, contentLength);
        }
    }

    public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
        for (DownloadListener listener : this.listenerList) {
            listener.fetchProgress(task, blockIndex, increaseBytes);
        }
    }

    public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
        for (DownloadListener listener : this.listenerList) {
            listener.fetchEnd(task, blockIndex, contentLength);
        }
    }

    public void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
        for (DownloadListener listener : this.listenerList) {
            listener.taskEnd(task, cause, realCause);
        }
    }

    public boolean contain(DownloadListener targetListener) {
        for (DownloadListener listener : this.listenerList) {
            if (listener == targetListener) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(DownloadListener targetListener) {
        int index = 0;
        while (true) {
            DownloadListener[] downloadListenerArr = this.listenerList;
            if (index >= downloadListenerArr.length) {
                return -1;
            }
            if (downloadListenerArr[index] == targetListener) {
                return index;
            }
            index++;
        }
    }

    public static class Builder {
        private List<DownloadListener> listenerList = new ArrayList();

        public DownloadListenerBunch build() {
            List<DownloadListener> list = this.listenerList;
            return new DownloadListenerBunch((DownloadListener[]) list.toArray(new DownloadListener[list.size()]));
        }

        public Builder append(DownloadListener listener) {
            if (listener != null && !this.listenerList.contains(listener)) {
                this.listenerList.add(listener);
            }
            return this;
        }

        public boolean remove(DownloadListener listener) {
            return this.listenerList.remove(listener);
        }
    }
}
