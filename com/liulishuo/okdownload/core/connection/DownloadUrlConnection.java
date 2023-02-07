package com.liulishuo.okdownload.core.connection;

import com.liulishuo.okdownload.IRedirectHandler;
import com.liulishuo.okdownload.RedirectUtil;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class DownloadUrlConnection implements DownloadConnection, DownloadConnection.Connected {
    private static final String TAG = "DownloadUrlConnection";
    private Configuration configuration;
    protected URLConnection connection;
    private IRedirectHandler redirectHandler;
    /* access modifiers changed from: private */
    public URL url;

    DownloadUrlConnection(URLConnection connection2) {
        this(connection2, (IRedirectHandler) new RedirectHandler());
    }

    DownloadUrlConnection(URLConnection connection2, IRedirectHandler redirectHandler2) {
        this.connection = connection2;
        this.url = connection2.getURL();
        this.redirectHandler = redirectHandler2;
    }

    public DownloadUrlConnection(String originUrl, Configuration configuration2) throws IOException {
        this(new URL(originUrl), configuration2);
    }

    public DownloadUrlConnection(URL url2, Configuration configuration2) throws IOException {
        this(url2, configuration2, new RedirectHandler());
    }

    public DownloadUrlConnection(URL url2, Configuration configuration2, IRedirectHandler redirectHandler2) throws IOException {
        this.configuration = configuration2;
        this.url = url2;
        this.redirectHandler = redirectHandler2;
        configUrlConnection();
    }

    public DownloadUrlConnection(String originUrl) throws IOException {
        this(originUrl, (Configuration) null);
    }

    /* access modifiers changed from: package-private */
    public void configUrlConnection() throws IOException {
        Util.m83d(TAG, "config connection for " + this.url);
        Configuration configuration2 = this.configuration;
        if (configuration2 == null || configuration2.proxy == null) {
            this.connection = this.url.openConnection();
        } else {
            this.connection = this.url.openConnection(this.configuration.proxy);
        }
        Configuration configuration3 = this.configuration;
        if (configuration3 != null) {
            if (configuration3.readTimeout != null) {
                this.connection.setReadTimeout(this.configuration.readTimeout.intValue());
            }
            if (this.configuration.connectTimeout != null) {
                this.connection.setConnectTimeout(this.configuration.connectTimeout.intValue());
            }
        }
    }

    public void addHeader(String name, String value) {
        this.connection.addRequestProperty(name, value);
    }

    public DownloadConnection.Connected execute() throws IOException {
        Map<String, List<String>> headerProperties = getRequestProperties();
        this.connection.connect();
        this.redirectHandler.handleRedirect(this, this, headerProperties);
        return this;
    }

    public int getResponseCode() throws IOException {
        URLConnection uRLConnection = this.connection;
        if (uRLConnection instanceof HttpURLConnection) {
            return ((HttpURLConnection) uRLConnection).getResponseCode();
        }
        return 0;
    }

    public InputStream getInputStream() throws IOException {
        return this.connection.getInputStream();
    }

    public boolean setRequestMethod(String method) throws ProtocolException {
        URLConnection uRLConnection = this.connection;
        if (!(uRLConnection instanceof HttpURLConnection)) {
            return false;
        }
        ((HttpURLConnection) uRLConnection).setRequestMethod(method);
        return true;
    }

    public Map<String, List<String>> getResponseHeaderFields() {
        return this.connection.getHeaderFields();
    }

    public String getResponseHeaderField(String name) {
        return this.connection.getHeaderField(name);
    }

    public String getRedirectLocation() {
        return this.redirectHandler.getRedirectLocation();
    }

    public void release() {
        try {
            InputStream inputStream = this.connection.getInputStream();
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
        }
    }

    public Map<String, List<String>> getRequestProperties() {
        return this.connection.getRequestProperties();
    }

    public String getRequestProperty(String key) {
        return this.connection.getRequestProperty(key);
    }

    public static class Factory implements DownloadConnection.Factory {
        private final Configuration configuration;

        public Factory() {
            this((Configuration) null);
        }

        public Factory(Configuration configuration2) {
            this.configuration = configuration2;
        }

        /* access modifiers changed from: package-private */
        public DownloadConnection create(URL url) throws IOException {
            return new DownloadUrlConnection(url, this.configuration);
        }

        public DownloadConnection create(String originUrl) throws IOException {
            return new DownloadUrlConnection(originUrl, this.configuration);
        }
    }

    public static class Configuration {
        /* access modifiers changed from: private */
        public Integer connectTimeout;
        /* access modifiers changed from: private */
        public Proxy proxy;
        /* access modifiers changed from: private */
        public Integer readTimeout;

        public Configuration proxy(Proxy proxy2) {
            this.proxy = proxy2;
            return this;
        }

        public Configuration readTimeout(int readTimeout2) {
            this.readTimeout = Integer.valueOf(readTimeout2);
            return this;
        }

        public Configuration connectTimeout(int connectTimeout2) {
            this.connectTimeout = Integer.valueOf(connectTimeout2);
            return this;
        }
    }

    static final class RedirectHandler implements IRedirectHandler {
        String redirectLocation;

        RedirectHandler() {
        }

        public void handleRedirect(DownloadConnection originalConnection, DownloadConnection.Connected originalConnected, Map<String, List<String>> headerProperties) throws IOException {
            int responseCode = originalConnected.getResponseCode();
            int redirectCount = 0;
            DownloadUrlConnection downloadUrlConnection = (DownloadUrlConnection) originalConnection;
            while (RedirectUtil.isRedirect(responseCode)) {
                downloadUrlConnection.release();
                redirectCount++;
                if (redirectCount <= 10) {
                    this.redirectLocation = RedirectUtil.getRedirectedUrl(originalConnected, responseCode);
                    URL unused = downloadUrlConnection.url = new URL(this.redirectLocation);
                    downloadUrlConnection.configUrlConnection();
                    Util.addRequestHeaderFields(headerProperties, downloadUrlConnection);
                    downloadUrlConnection.connection.connect();
                    responseCode = downloadUrlConnection.getResponseCode();
                } else {
                    throw new ProtocolException("Too many redirect requests: " + redirectCount);
                }
            }
        }

        public String getRedirectLocation() {
            return this.redirectLocation;
        }
    }
}
