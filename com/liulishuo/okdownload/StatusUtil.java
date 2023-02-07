package com.liulishuo.okdownload;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointStore;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import java.io.File;

public class StatusUtil {

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        IDLE,
        UNKNOWN
    }

    public static boolean isSameTaskPendingOrRunning(DownloadTask task) {
        return OkDownload.with().downloadDispatcher().findSameTask(task) != null;
    }

    public static Status getStatus(DownloadTask task) {
        Status status = isCompletedOrUnknown(task);
        if (status == Status.COMPLETED) {
            return Status.COMPLETED;
        }
        DownloadDispatcher dispatcher = OkDownload.with().downloadDispatcher();
        if (dispatcher.isPending(task)) {
            return Status.PENDING;
        }
        if (dispatcher.isRunning(task)) {
            return Status.RUNNING;
        }
        return status;
    }

    public static Status getStatus(String url, String parentPath, String filename) {
        return getStatus(createFinder(url, parentPath, filename));
    }

    public static boolean isCompleted(DownloadTask task) {
        return isCompletedOrUnknown(task) == Status.COMPLETED;
    }

    public static Status isCompletedOrUnknown(DownloadTask task) {
        BreakpointStore store = OkDownload.with().breakpointStore();
        BreakpointInfo info = store.get(task.getId());
        String filename = task.getFilename();
        File parentFile = task.getParentFile();
        File targetFile = task.getFile();
        if (info != null) {
            if (!info.isChunked() && info.getTotalLength() <= 0) {
                return Status.UNKNOWN;
            }
            if (targetFile != null && targetFile.equals(info.getFile()) && targetFile.exists() && info.getTotalOffset() == info.getTotalLength()) {
                return Status.COMPLETED;
            }
            if (filename == null && info.getFile() != null && info.getFile().exists()) {
                return Status.IDLE;
            }
            if (targetFile != null && targetFile.equals(info.getFile()) && targetFile.exists()) {
                return Status.IDLE;
            }
        } else if (store.isOnlyMemoryCache() || store.isFileDirty(task.getId())) {
            return Status.UNKNOWN;
        } else {
            if (targetFile != null && targetFile.exists()) {
                return Status.COMPLETED;
            }
            String filename2 = store.getResponseFilename(task.getUrl());
            if (filename2 != null && new File(parentFile, filename2).exists()) {
                return Status.COMPLETED;
            }
        }
        return Status.UNKNOWN;
    }

    public static boolean isCompleted(String url, String parentPath, String filename) {
        return isCompleted(createFinder(url, parentPath, filename));
    }

    public static BreakpointInfo getCurrentInfo(String url, String parentPath, String filename) {
        return getCurrentInfo(createFinder(url, parentPath, filename));
    }

    public static BreakpointInfo getCurrentInfo(DownloadTask task) {
        BreakpointStore store = OkDownload.with().breakpointStore();
        BreakpointInfo info = store.get(store.findOrCreateId(task));
        if (info == null) {
            return null;
        }
        return info.copy();
    }

    static DownloadTask createFinder(String url, String parentPath, String filename) {
        return new DownloadTask.Builder(url, parentPath, filename).build();
    }
}
