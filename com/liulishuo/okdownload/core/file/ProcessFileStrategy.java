package com.liulishuo.okdownload.core.file;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import java.io.File;
import java.io.IOException;

public class ProcessFileStrategy {
    private final FileLock fileLock = new FileLock();

    public MultiPointOutputStream createProcessStream(DownloadTask task, BreakpointInfo info, DownloadStore store) {
        return new MultiPointOutputStream(task, info, store);
    }

    public void completeProcessStream(MultiPointOutputStream processOutputStream, DownloadTask task) {
    }

    public void discardProcess(DownloadTask task) throws IOException {
        File file = task.getFile();
        if (file != null && file.exists() && !file.delete()) {
            throw new IOException("Delete file failed!");
        }
    }

    public FileLock getFileLock() {
        return this.fileLock;
    }

    public boolean isPreAllocateLength(DownloadTask task) {
        if (!OkDownload.with().outputStreamFactory().supportSeek()) {
            return false;
        }
        if (task.getSetPreAllocateLength() != null) {
            return task.getSetPreAllocateLength().booleanValue();
        }
        return true;
    }
}
