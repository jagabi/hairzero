package com.liulishuo.okdownload.core.interceptor;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher;
import com.liulishuo.okdownload.core.download.DownloadChain;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.file.MultiPointOutputStream;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import java.io.IOException;
import java.io.InputStream;

public class FetchDataInterceptor implements Interceptor.Fetch {
    private final int blockIndex;
    private final CallbackDispatcher dispatcher = OkDownload.with().callbackDispatcher();
    private final InputStream inputStream;
    private final MultiPointOutputStream outputStream;
    private final byte[] readBuffer;
    private final DownloadTask task;

    public FetchDataInterceptor(int blockIndex2, InputStream inputStream2, MultiPointOutputStream outputStream2, DownloadTask task2) {
        this.blockIndex = blockIndex2;
        this.inputStream = inputStream2;
        this.readBuffer = new byte[task2.getReadBufferSize()];
        this.outputStream = outputStream2;
        this.task = task2;
    }

    public long interceptFetch(DownloadChain chain) throws IOException {
        if (!chain.getCache().isInterrupt()) {
            OkDownload.with().downloadStrategy().inspectNetworkOnWifi(chain.getTask());
            int fetchLength = this.inputStream.read(this.readBuffer);
            if (fetchLength == -1) {
                return (long) fetchLength;
            }
            this.outputStream.write(this.blockIndex, this.readBuffer, fetchLength);
            chain.increaseCallbackBytes((long) fetchLength);
            if (this.dispatcher.isFetchProcessMoment(this.task)) {
                chain.flushNoCallbackIncreaseBytes();
            }
            return (long) fetchLength;
        }
        throw InterruptException.SIGNAL;
    }
}
