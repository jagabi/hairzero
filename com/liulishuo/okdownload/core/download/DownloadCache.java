package com.liulishuo.okdownload.core.download;

import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.exception.FileBusyAfterRunException;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.exception.PreAllocateException;
import com.liulishuo.okdownload.core.exception.ResumeFailedException;
import com.liulishuo.okdownload.core.exception.ServerCanceledException;
import com.liulishuo.okdownload.core.file.MultiPointOutputStream;
import java.io.IOException;
import java.net.SocketException;

public class DownloadCache {
    private volatile boolean fileBusyAfterRun;
    private final MultiPointOutputStream outputStream;
    private volatile boolean preAllocateFailed;
    private volatile boolean preconditionFailed;
    private volatile IOException realCause;
    private String redirectLocation;
    private volatile boolean serverCanceled;
    private volatile boolean unknownError;
    private volatile boolean userCanceled;

    DownloadCache(MultiPointOutputStream outputStream2) {
        this.outputStream = outputStream2;
    }

    private DownloadCache() {
        this.outputStream = null;
    }

    /* access modifiers changed from: package-private */
    public MultiPointOutputStream getOutputStream() {
        MultiPointOutputStream multiPointOutputStream = this.outputStream;
        if (multiPointOutputStream != null) {
            return multiPointOutputStream;
        }
        throw new IllegalArgumentException();
    }

    /* access modifiers changed from: package-private */
    public void setRedirectLocation(String redirectLocation2) {
        this.redirectLocation = redirectLocation2;
    }

    /* access modifiers changed from: package-private */
    public String getRedirectLocation() {
        return this.redirectLocation;
    }

    /* access modifiers changed from: package-private */
    public boolean isPreconditionFailed() {
        return this.preconditionFailed;
    }

    public boolean isUserCanceled() {
        return this.userCanceled;
    }

    /* access modifiers changed from: package-private */
    public boolean isServerCanceled() {
        return this.serverCanceled;
    }

    /* access modifiers changed from: package-private */
    public boolean isUnknownError() {
        return this.unknownError;
    }

    /* access modifiers changed from: package-private */
    public boolean isFileBusyAfterRun() {
        return this.fileBusyAfterRun;
    }

    public boolean isPreAllocateFailed() {
        return this.preAllocateFailed;
    }

    /* access modifiers changed from: package-private */
    public IOException getRealCause() {
        return this.realCause;
    }

    /* access modifiers changed from: package-private */
    public ResumeFailedCause getResumeFailedCause() {
        return ((ResumeFailedException) this.realCause).getResumeFailedCause();
    }

    public boolean isInterrupt() {
        return this.preconditionFailed || this.userCanceled || this.serverCanceled || this.unknownError || this.fileBusyAfterRun || this.preAllocateFailed;
    }

    public void setPreconditionFailed(IOException realCause2) {
        this.preconditionFailed = true;
        this.realCause = realCause2;
    }

    /* access modifiers changed from: package-private */
    public void setUserCanceled() {
        this.userCanceled = true;
    }

    public void setFileBusyAfterRun() {
        this.fileBusyAfterRun = true;
    }

    public void setServerCanceled(IOException realCause2) {
        this.serverCanceled = true;
        this.realCause = realCause2;
    }

    public void setUnknownError(IOException realCause2) {
        this.unknownError = true;
        this.realCause = realCause2;
    }

    public void setPreAllocateFailed(IOException realCause2) {
        this.preAllocateFailed = true;
        this.realCause = realCause2;
    }

    public void catchException(IOException e) {
        if (!isUserCanceled()) {
            if (e instanceof ResumeFailedException) {
                setPreconditionFailed(e);
            } else if (e instanceof ServerCanceledException) {
                setServerCanceled(e);
            } else if (e == FileBusyAfterRunException.SIGNAL) {
                setFileBusyAfterRun();
            } else if (e instanceof PreAllocateException) {
                setPreAllocateFailed(e);
            } else if (e != InterruptException.SIGNAL) {
                setUnknownError(e);
                if (!(e instanceof SocketException)) {
                    Util.m83d("DownloadCache", "catch unknown error " + e);
                }
            }
        }
    }

    static class PreError extends DownloadCache {
        PreError(IOException realCause) {
            super((MultiPointOutputStream) null);
            setUnknownError(realCause);
        }
    }
}
