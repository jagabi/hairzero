package com.liulishuo.okdownload;

import com.liulishuo.okdownload.core.connection.DownloadConnection;
import java.io.IOException;
import java.net.ProtocolException;

public class RedirectUtil {
    static final int HTTP_PERMANENT_REDIRECT = 308;
    static final int HTTP_TEMPORARY_REDIRECT = 307;
    public static final int MAX_REDIRECT_TIMES = 10;

    public static boolean isRedirect(int code) {
        return code == 301 || code == 302 || code == 303 || code == 300 || code == 307 || code == 308;
    }

    public static String getRedirectedUrl(DownloadConnection.Connected connected, int responseCode) throws IOException {
        String url = connected.getResponseHeaderField("Location");
        if (url != null) {
            return url;
        }
        throw new ProtocolException("Response code is " + responseCode + " but can't find Location field");
    }
}
