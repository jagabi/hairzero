package com.liulishuo.okdownload.core.listener;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.assist.Listener4Assist;
import com.liulishuo.okdownload.core.listener.assist.ListenerAssist;
import com.liulishuo.okdownload.core.listener.assist.ListenerModelHandler;
import java.util.List;
import java.util.Map;

public abstract class DownloadListener4 implements DownloadListener, Listener4Assist.Listener4Callback, ListenerAssist {
    final Listener4Assist assist;

    DownloadListener4(Listener4Assist assist2) {
        this.assist = assist2;
        assist2.setCallback(this);
    }

    public DownloadListener4() {
        this(new Listener4Assist(new Listener4ModelCreator()));
    }

    public void setAssistExtend(Listener4Assist.AssistExtend assistExtend) {
        this.assist.setAssistExtend(assistExtend);
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

    public void connectTrialStart(DownloadTask task, Map<String, List<String>> map) {
    }

    public void connectTrialEnd(DownloadTask task, int responseCode, Map<String, List<String>> map) {
    }

    public final void downloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
        this.assist.infoReady(task, info, false);
    }

    public final void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
        this.assist.infoReady(task, info, true);
    }

    public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
    }

    public final void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
        this.assist.fetchProgress(task, blockIndex, increaseBytes);
    }

    public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
        this.assist.fetchEnd(task, blockIndex);
    }

    public final void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
        this.assist.taskEnd(task, cause, realCause);
    }

    static class Listener4ModelCreator implements ListenerModelHandler.ModelCreator<Listener4Assist.Listener4Model> {
        Listener4ModelCreator() {
        }

        public Listener4Assist.Listener4Model create(int id) {
            return new Listener4Assist.Listener4Model(id);
        }
    }
}
