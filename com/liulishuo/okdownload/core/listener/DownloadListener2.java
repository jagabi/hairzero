package com.liulishuo.okdownload.core.listener;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import java.util.List;
import java.util.Map;

public abstract class DownloadListener2 implements DownloadListener {
    public void connectTrialStart(DownloadTask task, Map<String, List<String>> map) {
    }

    public void connectTrialEnd(DownloadTask task, int responseCode, Map<String, List<String>> map) {
    }

    public void downloadFromBeginning(DownloadTask task, BreakpointInfo info, ResumeFailedCause cause) {
    }

    public void downloadFromBreakpoint(DownloadTask task, BreakpointInfo info) {
    }

    public void connectStart(DownloadTask task, int blockIndex, Map<String, List<String>> map) {
    }

    public void connectEnd(DownloadTask task, int blockIndex, int responseCode, Map<String, List<String>> map) {
    }

    public void fetchStart(DownloadTask task, int blockIndex, long contentLength) {
    }

    public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
    }

    public void fetchEnd(DownloadTask task, int blockIndex, long contentLength) {
    }
}
