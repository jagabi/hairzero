package com.liulishuo.okdownload.core.connection;

import com.liulishuo.okdownload.RedirectUtil;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadOkHttp3Connection implements DownloadConnection, DownloadConnection.Connected {
    final OkHttpClient client;
    private Request request;
    private final Request.Builder requestBuilder;
    Response response;

    DownloadOkHttp3Connection(OkHttpClient client2, Request.Builder requestBuilder2) {
        this.client = client2;
        this.requestBuilder = requestBuilder2;
    }

    DownloadOkHttp3Connection(OkHttpClient client2, String url) {
        this(client2, new Request.Builder().url(url));
    }

    public void addHeader(String name, String value) {
        this.requestBuilder.addHeader(name, value);
    }

    public DownloadConnection.Connected execute() throws IOException {
        Request build = this.requestBuilder.build();
        this.request = build;
        this.response = this.client.newCall(build).execute();
        return this;
    }

    public void release() {
        this.request = null;
        Response response2 = this.response;
        if (response2 != null) {
            response2.close();
        }
        this.response = null;
    }

    public Map<String, List<String>> getRequestProperties() {
        Request request2 = this.request;
        if (request2 != null) {
            return request2.headers().toMultimap();
        }
        return this.requestBuilder.build().headers().toMultimap();
    }

    public String getRequestProperty(String key) {
        Request request2 = this.request;
        if (request2 != null) {
            return request2.header(key);
        }
        return this.requestBuilder.build().header(key);
    }

    public int getResponseCode() throws IOException {
        Response response2 = this.response;
        if (response2 != null) {
            return response2.code();
        }
        throw new IOException("Please invoke execute first!");
    }

    public InputStream getInputStream() throws IOException {
        Response response2 = this.response;
        if (response2 != null) {
            ResponseBody body = response2.body();
            if (body != null) {
                return body.byteStream();
            }
            throw new IOException("no body found on response!");
        }
        throw new IOException("Please invoke execute first!");
    }

    public boolean setRequestMethod(String method) throws ProtocolException {
        this.requestBuilder.method(method, (RequestBody) null);
        return true;
    }

    public Map<String, List<String>> getResponseHeaderFields() {
        Response response2 = this.response;
        if (response2 == null) {
            return null;
        }
        return response2.headers().toMultimap();
    }

    public String getResponseHeaderField(String name) {
        Response response2 = this.response;
        if (response2 == null) {
            return null;
        }
        return response2.header(name);
    }

    public String getRedirectLocation() {
        Response priorRes = this.response.priorResponse();
        if (priorRes == null || !this.response.isSuccessful() || !RedirectUtil.isRedirect(priorRes.code())) {
            return null;
        }
        return this.response.request().url().toString();
    }

    public static class Factory implements DownloadConnection.Factory {
        private volatile OkHttpClient client;
        private OkHttpClient.Builder clientBuilder;

        public Factory setBuilder(OkHttpClient.Builder builder) {
            this.clientBuilder = builder;
            return this;
        }

        public OkHttpClient.Builder builder() {
            if (this.clientBuilder == null) {
                this.clientBuilder = new OkHttpClient.Builder();
            }
            return this.clientBuilder;
        }

        public DownloadConnection create(String url) throws IOException {
            if (this.client == null) {
                synchronized (Factory.class) {
                    if (this.client == null) {
                        OkHttpClient.Builder builder = this.clientBuilder;
                        this.client = builder != null ? builder.build() : new OkHttpClient();
                        this.clientBuilder = null;
                    }
                }
            }
            return new DownloadOkHttp3Connection(this.client, url);
        }
    }
}
