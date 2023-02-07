package com.liulishuo.okdownload.core.interceptor;

import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.download.DownloadChain;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.exception.RetryException;
import com.liulishuo.okdownload.core.file.MultiPointOutputStream;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakpointInterceptor implements Interceptor.Connect, Interceptor.Fetch {
    private static final Pattern CONTENT_RANGE_RIGHT_VALUE = Pattern.compile(".*\\d+ *- *(\\d+) */ *\\d+");
    private static final String TAG = "BreakpointInterceptor";

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        DownloadConnection.Connected connected = chain.processConnect();
        BreakpointInfo info = chain.getInfo();
        if (!chain.getCache().isInterrupt()) {
            boolean z = true;
            if (info.getBlockCount() == 1 && !info.isChunked()) {
                long blockInstanceLength = getExactContentLengthRangeFrom0(connected);
                long infoInstanceLength = info.getTotalLength();
                if (blockInstanceLength > 0 && blockInstanceLength != infoInstanceLength) {
                    Util.m83d(TAG, "SingleBlock special check: the response instance-length[" + blockInstanceLength + "] isn't equal to the instance length from trial-connection[" + infoInstanceLength + "]");
                    if (info.getBlock(0).getRangeLeft() == 0) {
                        z = false;
                    }
                    boolean isFromBreakpoint = z;
                    BlockInfo newBlockInfo = new BlockInfo(0, blockInstanceLength);
                    info.resetBlockInfos();
                    info.addBlock(newBlockInfo);
                    if (!isFromBreakpoint) {
                        OkDownload.with().callbackDispatcher().dispatch().downloadFromBeginning(chain.getTask(), info, ResumeFailedCause.CONTENT_LENGTH_CHANGED);
                    } else {
                        Util.m86w(TAG, "Discard breakpoint because of on this special case, we have to download from beginning");
                        throw new RetryException("Discard breakpoint because of on this special case, we have to download from beginning");
                    }
                }
            }
            try {
                if (chain.getDownloadStore().update(info)) {
                    return connected;
                }
                throw new IOException("Update store failed!");
            } catch (Exception e) {
                throw new IOException("Update store failed!", e);
            }
        } else {
            throw InterruptException.SIGNAL;
        }
    }

    public long interceptFetch(DownloadChain chain) throws IOException {
        long contentLength = chain.getResponseContentLength();
        int blockIndex = chain.getBlockIndex();
        boolean isNotChunked = contentLength != -1;
        long fetchLength = 0;
        MultiPointOutputStream outputStream = chain.getOutputStream();
        while (true) {
            try {
                long processFetchLength = chain.loopFetch();
                if (processFetchLength == -1) {
                    break;
                }
                fetchLength += processFetchLength;
            } finally {
                chain.flushNoCallbackIncreaseBytes();
                if (!chain.getCache().isUserCanceled()) {
                    outputStream.done(blockIndex);
                }
            }
        }
        if (isNotChunked) {
            outputStream.inspectComplete(blockIndex);
            if (fetchLength != contentLength) {
                throw new IOException("Fetch-length isn't equal to the response content-length, " + fetchLength + "!= " + contentLength);
            }
        }
        return fetchLength;
    }

    /* access modifiers changed from: package-private */
    public long getExactContentLengthRangeFrom0(DownloadConnection.Connected connected) {
        String contentRangeField = connected.getResponseHeaderField(Util.CONTENT_RANGE);
        long contentLength = -1;
        if (!Util.isEmpty(contentRangeField)) {
            long rightRange = getRangeRightFromContentRange(contentRangeField);
            if (rightRange > 0) {
                contentLength = rightRange + 1;
            }
        }
        if (contentLength >= 0) {
            return contentLength;
        }
        String contentLengthField = connected.getResponseHeaderField(Util.CONTENT_LENGTH);
        if (!Util.isEmpty(contentLengthField)) {
            return Long.parseLong(contentLengthField);
        }
        return contentLength;
    }

    static long getRangeRightFromContentRange(String contentRange) {
        Matcher m = CONTENT_RANGE_RIGHT_VALUE.matcher(contentRange);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return -1;
    }
}
