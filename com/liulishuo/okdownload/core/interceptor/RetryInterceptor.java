package com.liulishuo.okdownload.core.interceptor;

import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.download.DownloadCache;
import com.liulishuo.okdownload.core.download.DownloadChain;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.exception.RetryException;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import java.io.IOException;

public class RetryInterceptor implements Interceptor.Connect, Interceptor.Fetch {
    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        DownloadCache cache = chain.getCache();
        while (true) {
            try {
                if (!cache.isInterrupt()) {
                    return chain.processConnect();
                }
                throw InterruptException.SIGNAL;
            } catch (IOException e) {
                if (e instanceof RetryException) {
                    chain.resetConnectForRetry();
                } else {
                    chain.getCache().catchException(e);
                    chain.getOutputStream().catchBlockConnectException(chain.getBlockIndex());
                    throw e;
                }
            }
        }
    }

    public long interceptFetch(DownloadChain chain) throws IOException {
        try {
            return chain.processFetch();
        } catch (IOException e) {
            chain.getCache().catchException(e);
            throw e;
        }
    }
}
