package com.liulishuo.okdownload.core.interceptor.connect;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.download.DownloadChain;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeaderInterceptor implements Interceptor.Connect {
    private static final String TAG = "HeaderInterceptor";

    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        long contentLength;
        BreakpointInfo info = chain.getInfo();
        DownloadConnection connection = chain.getConnectionOrCreate();
        DownloadTask task = chain.getTask();
        Map<String, List<String>> userHeader = task.getHeaderMapFields();
        if (userHeader != null) {
            Util.addUserRequestHeaderField(userHeader, connection);
        }
        if (userHeader == null || !userHeader.containsKey("User-Agent")) {
            Util.addDefaultUserAgent(connection);
        }
        int blockIndex = chain.getBlockIndex();
        BlockInfo blockInfo = info.getBlock(blockIndex);
        if (blockInfo != null) {
            connection.addHeader(Util.RANGE, ("bytes=" + blockInfo.getRangeLeft() + "-") + blockInfo.getRangeRight());
            Util.m83d(TAG, "AssembleHeaderRange (" + task.getId() + ") block(" + blockIndex + ") downloadFrom(" + blockInfo.getRangeLeft() + ") currentOffset(" + blockInfo.getCurrentOffset() + ")");
            String etag = info.getEtag();
            if (!Util.isEmpty(etag)) {
                connection.addHeader(Util.IF_MATCH, etag);
            }
            if (!chain.getCache().isInterrupt()) {
                OkDownload.with().callbackDispatcher().dispatch().connectStart(task, blockIndex, connection.getRequestProperties());
                DownloadConnection.Connected connected = chain.processConnect();
                if (!chain.getCache().isInterrupt()) {
                    Map<String, List<String>> responseHeaderFields = connected.getResponseHeaderFields();
                    if (responseHeaderFields == null) {
                        responseHeaderFields = new HashMap<>();
                    }
                    OkDownload.with().callbackDispatcher().dispatch().connectEnd(task, blockIndex, connected.getResponseCode(), responseHeaderFields);
                    OkDownload.with().downloadStrategy().resumeAvailableResponseCheck(connected, blockIndex, info).inspect();
                    String contentLengthField = connected.getResponseHeaderField(Util.CONTENT_LENGTH);
                    if (contentLengthField == null || contentLengthField.length() == 0) {
                        contentLength = Util.parseContentLengthFromContentRange(connected.getResponseHeaderField(Util.CONTENT_RANGE));
                    } else {
                        contentLength = Util.parseContentLength(contentLengthField);
                    }
                    chain.setResponseContentLength(contentLength);
                    return connected;
                }
                DownloadChain downloadChain = chain;
                throw InterruptException.SIGNAL;
            }
            DownloadChain downloadChain2 = chain;
            throw InterruptException.SIGNAL;
        }
        DownloadChain downloadChain3 = chain;
        throw new IOException("No block-info found on " + blockIndex);
    }
}
