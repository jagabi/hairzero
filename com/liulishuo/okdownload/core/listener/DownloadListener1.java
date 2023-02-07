package com.liulishuo.okdownload.core.listener;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;
import com.liulishuo.okdownload.core.listener.assist.ListenerAssist;
import java.util.List;
import java.util.Map;

public abstract class DownloadListener1 implements DownloadListener, Listener1Assist.Listener1Callback, ListenerAssist {
    final Listener1Assist assist;

    DownloadListener1(Listener1Assist assist2) {
        this.assist = assist2;
        assist2.setCallback(this);
    }

    public DownloadListener1() {
        this(new Listener1Assist());
    }

    public boolean isAlwaysRecoverAssistModel() {
        return this.assist.isAlwaysRecoverAssistModel();
    }

    public void setAlwaysRecoverAssistModel(boolean isAlwaysRecoverAssistModel) {
        this.assist.setAlwaysRecoverAssistModel(isAlwaysRecoverAssistModel);
    }

    public void setAlwaysRecoverAssistModelIfNotSet(boolean isAlwaysRecoverAssistModel) {
        this.assist.setAlwaysRecoverAssistModelIfNotSet(isAlwaysRecoverAssistModel);
    }

    public final void taskStart(DownloadTask task) {
        this.assist.taskStart(task);
    }

    public void connectTrialStart(DownloadTask task, Map<String, List<String>> map) {
    }

    public void connectTrialEnd(DownloadTask task, int responseCode, Map<String, List<String>> map) {
    }

    public void downloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
        this.assist.downloadFromBeginning(task, info, cause);
    }

    public void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
        this.assist.downloadFromBreakpoint(task, info);
    }

    public void connectStart(DownloadTask task, int blockIndex, Map<String, List<String>> map) {
    }

    public void connectEnd(DownloadTask task, int blockIndex, int responseCode, Map<String, List<String>> map) {
        this.assist.connectEnd(task);
    }

    public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
    }

    public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
        this.assist.fetchProgress(task, increaseBytes);
    }

    public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
    }

    public final void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
        this.assist.taskEnd(task, cause, realCause);
    }
}
