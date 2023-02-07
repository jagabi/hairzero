package com.liulishuo.okdownload.core.download;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.exception.FileBusyAfterRunException;
import com.liulishuo.okdownload.core.exception.ServerCanceledException;
import java.io.IOException;

public class BreakpointRemoteCheck {
    private boolean acceptRange;
    ResumeFailedCause failedCause;
    private final BreakpointInfo info;
    private long instanceLength;
    private boolean resumable;
    private final DownloadTask task;

    public BreakpointRemoteCheck(DownloadTask task2, BreakpointInfo info2) {
        this.task = task2;
        this.info = info2;
    }

    public String toString() {
        return "acceptRange[" + this.acceptRange + "] resumable[" + this.resumable + "] failedCause[" + this.failedCause + "] instanceLength[" + this.instanceLength + "] " + super.toString();
    }

    public ResumeFailedCause getCause() {
        return this.failedCause;
    }

    public ResumeFailedCause getCauseOrThrow() {
        ResumeFailedCause resumeFailedCause = this.failedCause;
        if (resumeFailedCause != null) {
            return resumeFailedCause;
        }
        throw new IllegalStateException("No cause find with resumable: " + this.resumable);
    }

    public boolean isResumable() {
        return this.resumable;
    }

    public boolean isAcceptRange() {
        return this.acceptRange;
    }

    public long getInstanceLength() {
        return this.instanceLength;
    }

    public void check() throws IOException {
        DownloadStrategy downloadStrategy = OkDownload.with().downloadStrategy();
        ConnectTrial connectTrial = createConnectTrial();
        connectTrial.executeTrial();
        boolean isAcceptRange = connectTrial.isAcceptRange();
        boolean isChunked = connectTrial.isChunked();
        long instanceLength2 = connectTrial.getInstanceLength();
        String responseEtag = connectTrial.getResponseEtag();
        String responseFilename = connectTrial.getResponseFilename();
        int responseCode = connectTrial.getResponseCode();
        downloadStrategy.validFilenameFromResponse(responseFilename, this.task, this.info);
        this.info.setChunked(isChunked);
        this.info.setEtag(responseEtag);
        if (!OkDownload.with().downloadDispatcher().isFileConflictAfterRun(this.task)) {
            boolean z = true;
            ResumeFailedCause resumeFailedCause = downloadStrategy.getPreconditionFailedCause(responseCode, this.info.getTotalOffset() != 0, this.info, responseEtag);
            boolean z2 = resumeFailedCause == null;
            this.resumable = z2;
            this.failedCause = resumeFailedCause;
            this.instanceLength = instanceLength2;
            this.acceptRange = isAcceptRange;
            if (!isTrialSpecialPass(responseCode, instanceLength2, z2)) {
                if (this.info.getTotalOffset() == 0) {
                    z = false;
                }
                if (downloadStrategy.isServerCanceled(responseCode, z)) {
                    throw new ServerCanceledException(responseCode, this.info.getTotalOffset());
                }
                return;
            }
            return;
        }
        throw FileBusyAfterRunException.SIGNAL;
    }

    /* access modifiers changed from: package-private */
    public boolean isTrialSpecialPass(int responseCode, long instanceLength2, boolean isResumable) {
        if (responseCode != 416 || instanceLength2 < 0 || !isResumable) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public ConnectTrial createConnectTrial() {
        return new ConnectTrial(this.task, this.info);
    }
}
