package com.liulishuo.okdownload.core.interceptor.connect;

import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.download.DownloadChain;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import java.io.IOException;

public class CallServerInterceptor implements Interceptor.Connect {
    public DownloadConnection.Connected interceptConnect(DownloadChain chain) throws IOException {
        OkDownload.with().downloadStrategy().inspectNetworkOnWifi(chain.getTask());
        OkDownload.with().downloadStrategy().inspectNetworkAvailable();
        return chain.getConnectionOrCreate().execute();
    }
}
