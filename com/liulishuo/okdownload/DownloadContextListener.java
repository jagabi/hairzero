package com.liulishuo.okdownload;

import com.liulishuo.okdownload.core.cause.EndCause;

public interface DownloadContextListener {
    void queueEnd(DownloadContext downloadContext);

    void taskEnd(DownloadContext downloadContext, DownloadTask downloadTask, EndCause endCause, Exception exc, int i);
}
