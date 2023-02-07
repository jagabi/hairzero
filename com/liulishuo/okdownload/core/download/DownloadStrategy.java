package com.liulishuo.okdownload.core.download;

import android.net.ConnectivityManager;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.exception.NetworkPolicyException;
import com.liulishuo.okdownload.core.exception.ResumeFailedException;
import com.liulishuo.okdownload.core.exception.ServerCanceledException;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadStrategy {
    private static final long FOUR_CONNECTION_UPPER_LIMIT = 104857600;
    private static final long ONE_CONNECTION_UPPER_LIMIT = 1048576;
    private static final String TAG = "DownloadStrategy";
    private static final long THREE_CONNECTION_UPPER_LIMIT = 52428800;
    private static final Pattern TMP_FILE_NAME_PATTERN = Pattern.compile(".*\\\\|/([^\\\\|/|?]*)\\??");
    private static final long TWO_CONNECTION_UPPER_LIMIT = 5242880;
    Boolean isHasAccessNetworkStatePermission = null;
    private ConnectivityManager manager = null;

    public ResumeAvailableResponseCheck resumeAvailableResponseCheck(DownloadConnection.Connected connected, int blockIndex, BreakpointInfo info) {
        return new ResumeAvailableResponseCheck(connected, blockIndex, info);
    }

    public int determineBlockCount(DownloadTask task, long totalLength) {
        if (task.getSetConnectionCount() != null) {
            return task.getSetConnectionCount().intValue();
        }
        if (totalLength < ONE_CONNECTION_UPPER_LIMIT) {
            return 1;
        }
        if (totalLength < TWO_CONNECTION_UPPER_LIMIT) {
            return 2;
        }
        if (totalLength < THREE_CONNECTION_UPPER_LIMIT) {
            return 3;
        }
        if (totalLength < FOUR_CONNECTION_UPPER_LIMIT) {
            return 4;
        }
        return 5;
    }

    public long reuseIdledSameInfoThresholdBytes() {
        return 10240;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
        r0 = com.liulishuo.okdownload.OkDownload.with().breakpointStore();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean inspectAnotherSameInfo(com.liulishuo.okdownload.DownloadTask r8, com.liulishuo.okdownload.core.breakpoint.BreakpointInfo r9, long r10) {
        /*
            r7 = this;
            boolean r0 = r8.isFilenameFromResponse()
            r1 = 0
            if (r0 != 0) goto L_0x0008
            return r1
        L_0x0008:
            com.liulishuo.okdownload.OkDownload r0 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.breakpoint.BreakpointStore r0 = r0.breakpointStore()
            com.liulishuo.okdownload.core.breakpoint.BreakpointInfo r2 = r0.findAnotherInfoFromCompare(r8, r9)
            if (r2 != 0) goto L_0x0017
            return r1
        L_0x0017:
            int r3 = r2.getId()
            r0.remove(r3)
            long r3 = r2.getTotalOffset()
            com.liulishuo.okdownload.OkDownload r5 = com.liulishuo.okdownload.OkDownload.with()
            com.liulishuo.okdownload.core.download.DownloadStrategy r5 = r5.downloadStrategy()
            long r5 = r5.reuseIdledSameInfoThresholdBytes()
            int r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r3 > 0) goto L_0x0033
            return r1
        L_0x0033:
            java.lang.String r3 = r2.getEtag()
            if (r3 == 0) goto L_0x0048
            java.lang.String r3 = r2.getEtag()
            java.lang.String r4 = r9.getEtag()
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x0048
            return r1
        L_0x0048:
            long r3 = r2.getTotalLength()
            int r3 = (r3 > r10 ? 1 : (r3 == r10 ? 0 : -1))
            if (r3 == 0) goto L_0x0051
            return r1
        L_0x0051:
            java.io.File r3 = r2.getFile()
            if (r3 == 0) goto L_0x007f
            java.io.File r3 = r2.getFile()
            boolean r3 = r3.exists()
            if (r3 != 0) goto L_0x0062
            goto L_0x007f
        L_0x0062:
            r9.reuseBlocks(r2)
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "Reuse another same info: "
            java.lang.StringBuilder r1 = r1.append(r3)
            java.lang.StringBuilder r1 = r1.append(r9)
            java.lang.String r1 = r1.toString()
            java.lang.String r3 = "DownloadStrategy"
            com.liulishuo.okdownload.core.Util.m83d(r3, r1)
            r1 = 1
            return r1
        L_0x007f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.download.DownloadStrategy.inspectAnotherSameInfo(com.liulishuo.okdownload.DownloadTask, com.liulishuo.okdownload.core.breakpoint.BreakpointInfo, long):boolean");
    }

    public boolean isUseMultiBlock(boolean isAcceptRange) {
        if (!OkDownload.with().outputStreamFactory().supportSeek()) {
            return false;
        }
        return isAcceptRange;
    }

    public void inspectFilenameFromResume(String filenameOnStore, DownloadTask task) {
        if (Util.isEmpty(task.getFilename())) {
            task.getFilenameHolder().set(filenameOnStore);
        }
    }

    public void validFilenameFromResponse(String responseFileName, DownloadTask task, BreakpointInfo info) throws IOException {
        if (Util.isEmpty(task.getFilename())) {
            String filename = determineFilename(responseFileName, task);
            if (Util.isEmpty(task.getFilename())) {
                synchronized (task) {
                    if (Util.isEmpty(task.getFilename())) {
                        task.getFilenameHolder().set(filename);
                        info.getFilenameHolder().set(filename);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public String determineFilename(String responseFileName, DownloadTask task) throws IOException {
        if (!Util.isEmpty(responseFileName)) {
            return responseFileName;
        }
        String url = task.getUrl();
        Matcher m = TMP_FILE_NAME_PATTERN.matcher(url);
        String filename = null;
        while (m.find()) {
            filename = m.group(1);
        }
        if (Util.isEmpty(filename)) {
            filename = Util.md5(url);
        }
        if (filename != null) {
            return filename;
        }
        throw new IOException("Can't find valid filename.");
    }

    public boolean validFilenameFromStore(DownloadTask task) {
        String filename = OkDownload.with().breakpointStore().getResponseFilename(task.getUrl());
        if (filename == null) {
            return false;
        }
        task.getFilenameHolder().set(filename);
        return true;
    }

    public void validInfoOnCompleted(DownloadTask task, DownloadStore store) {
        long size;
        BreakpointInfo info = store.getAfterCompleted(task.getId());
        if (info == null) {
            info = new BreakpointInfo(task.getId(), task.getUrl(), task.getParentFile(), task.getFilename());
            if (Util.isUriContentScheme(task.getUri())) {
                size = Util.getSizeFromContentUri(task.getUri());
            } else {
                File file = task.getFile();
                if (file == null) {
                    Util.m86w(TAG, "file is not ready on valid info for task on complete state " + task);
                    size = 0;
                } else {
                    size = file.length();
                }
            }
            info.addBlock(new BlockInfo(0, size, size));
        }
        DownloadTask.TaskHideWrapper.setBreakpointInfo(task, info);
    }

    public static class FilenameHolder {
        private volatile String filename;
        private final boolean filenameProvidedByConstruct = false;

        public FilenameHolder() {
        }

        public FilenameHolder(String filename2) {
            this.filename = filename2;
        }

        /* access modifiers changed from: package-private */
        public void set(String filename2) {
            this.filename = filename2;
        }

        public String get() {
            return this.filename;
        }

        public boolean isFilenameProvidedByConstruct() {
            return this.filenameProvidedByConstruct;
        }

        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (!(obj instanceof FilenameHolder)) {
                return false;
            }
            if (this.filename != null) {
                return this.filename.equals(((FilenameHolder) obj).filename);
            }
            if (((FilenameHolder) obj).filename == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            if (this.filename == null) {
                return 0;
            }
            return this.filename.hashCode();
        }
    }

    public static class ResumeAvailableResponseCheck {
        private int blockIndex;
        private DownloadConnection.Connected connected;
        private BreakpointInfo info;

        protected ResumeAvailableResponseCheck(DownloadConnection.Connected connected2, int blockIndex2, BreakpointInfo info2) {
            this.connected = connected2;
            this.info = info2;
            this.blockIndex = blockIndex2;
        }

        public void inspect() throws IOException {
            BlockInfo blockInfo = this.info.getBlock(this.blockIndex);
            int code = this.connected.getResponseCode();
            boolean z = true;
            ResumeFailedCause resumeFailedCause = OkDownload.with().downloadStrategy().getPreconditionFailedCause(code, blockInfo.getCurrentOffset() != 0, this.info, this.connected.getResponseHeaderField(Util.ETAG));
            if (resumeFailedCause == null) {
                DownloadStrategy downloadStrategy = OkDownload.with().downloadStrategy();
                if (blockInfo.getCurrentOffset() == 0) {
                    z = false;
                }
                if (downloadStrategy.isServerCanceled(code, z)) {
                    throw new ServerCanceledException(code, blockInfo.getCurrentOffset());
                }
                return;
            }
            throw new ResumeFailedException(resumeFailedCause);
        }
    }

    public ResumeFailedCause getPreconditionFailedCause(int responseCode, boolean isAlreadyProceed, BreakpointInfo info, String responseEtag) {
        String localEtag = info.getEtag();
        if (responseCode == 412) {
            return ResumeFailedCause.RESPONSE_PRECONDITION_FAILED;
        }
        if (!Util.isEmpty(localEtag) && !Util.isEmpty(responseEtag) && !responseEtag.equals(localEtag)) {
            return ResumeFailedCause.RESPONSE_ETAG_CHANGED;
        }
        if (responseCode == 201 && isAlreadyProceed) {
            return ResumeFailedCause.RESPONSE_CREATED_RANGE_NOT_FROM_0;
        }
        if (responseCode != 205 || !isAlreadyProceed) {
            return null;
        }
        return ResumeFailedCause.RESPONSE_RESET_RANGE_NOT_FROM_0;
    }

    public boolean isServerCanceled(int responseCode, boolean isAlreadyProceed) {
        if (responseCode != 206 && responseCode != 200) {
            return true;
        }
        if (responseCode != 200 || !isAlreadyProceed) {
            return false;
        }
        return true;
    }

    public void inspectNetworkAvailable() throws UnknownHostException {
        if (this.isHasAccessNetworkStatePermission == null) {
            this.isHasAccessNetworkStatePermission = Boolean.valueOf(Util.checkPermission("android.permission.ACCESS_NETWORK_STATE"));
        }
        if (this.isHasAccessNetworkStatePermission.booleanValue()) {
            if (this.manager == null) {
                this.manager = (ConnectivityManager) OkDownload.with().context().getSystemService("connectivity");
            }
            if (!Util.isNetworkAvailable(this.manager)) {
                throw new UnknownHostException("network is not available!");
            }
        }
    }

    public void inspectNetworkOnWifi(DownloadTask task) throws IOException {
        if (this.isHasAccessNetworkStatePermission == null) {
            this.isHasAccessNetworkStatePermission = Boolean.valueOf(Util.checkPermission("android.permission.ACCESS_NETWORK_STATE"));
        }
        if (task.isWifiRequired()) {
            if (this.isHasAccessNetworkStatePermission.booleanValue()) {
                if (this.manager == null) {
                    this.manager = (ConnectivityManager) OkDownload.with().context().getSystemService("connectivity");
                }
                if (Util.isNetworkNotOnWifiType(this.manager)) {
                    throw new NetworkPolicyException();
                }
                return;
            }
            throw new IOException("required for access network state but don't have the permission of Manifest.permission.ACCESS_NETWORK_STATE, please declare this permission first on your AndroidManifest, so we can handle the case of downloading required wifi state.");
        }
    }
}
