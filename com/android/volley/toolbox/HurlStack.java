package com.android.volley.toolbox;

import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.misc.MultipartUtils;
import com.android.volley.request.MultiPartRequest;
import com.android.volley.toolbox.HttpClientStack;
import com.liulishuo.okdownload.core.Util;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

public class HurlStack implements HttpStack {
    private final SSLSocketFactory mSslSocketFactory;
    private UrlRewriter mUrlRewriter;
    private String mUserAgent;

    public interface UrlRewriter {
        String rewriteUrl(String str);
    }

    public HurlStack() {
        this((UrlRewriter) null);
    }

    public HurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, (SSLSocketFactory) null);
    }

    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        this.mUrlRewriter = urlRewriter;
        this.mSslSocketFactory = sslSocketFactory;
    }

    public HurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory, String userAgent) {
        this.mUrlRewriter = urlRewriter;
        this.mSslSocketFactory = sslSocketFactory;
        this.mUserAgent = userAgent;
    }

    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws AuthFailureError, IOException {
        Request<?> request2 = request;
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        UrlRewriter urlRewriter = this.mUrlRewriter;
        if (urlRewriter != null) {
            String rewritten = urlRewriter.rewriteUrl(url);
            if (rewritten != null) {
                url = rewritten;
            } else {
                throw new IOException("URL blocked by rewriter: " + url);
            }
        }
        HttpURLConnection connection = openConnection(new URL(url), request2);
        if (!TextUtils.isEmpty(this.mUserAgent)) {
            connection.setRequestProperty("User-Agent", this.mUserAgent);
        }
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        if (request2 instanceof MultiPartRequest) {
            setConnectionParametersForMultipartRequest(connection, request2);
        } else {
            setConnectionParametersForRequest(connection, request2);
        }
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        if (connection.getResponseCode() != -1) {
            StatusLine responseStatus = new BasicStatusLine(protocolVersion, connection.getResponseCode(), connection.getResponseMessage());
            BasicHttpResponse response = new BasicHttpResponse(responseStatus);
            if (hasResponseBody(request.getMethod(), responseStatus.getStatusCode())) {
                response.setEntity(entityFromConnection(connection));
            }
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    response.addHeader(new BasicHeader(header.getKey(), (String) header.getValue().get(0)));
                }
            }
            return response;
        }
        throw new IOException("Could not retrieve response code from HttpUrlConnection.");
    }

    private static boolean hasResponseBody(int requestMethod, int responseCode) {
        return (requestMethod == 4 || (100 <= responseCode && responseCode < 200) || responseCode == 204 || responseCode == 304) ? false : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:103:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0228 A[SYNTHETIC, Splitter:B:68:0x0228] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02d4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void setConnectionParametersForMultipartRequest(java.net.HttpURLConnection r27, com.android.volley.Request<?> r28) throws java.io.IOException, java.net.ProtocolException {
        /*
            r1 = r27
            r0 = r28
            com.android.volley.request.MultiPartRequest r0 = (com.android.volley.request.MultiPartRequest) r0
            java.lang.String r2 = r0.getProtocolCharset()
            long r3 = java.lang.System.currentTimeMillis()
            r5 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 / r5
            int r3 = (int) r3
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r4 = "--"
            java.lang.StringBuilder r0 = r0.append(r4)
            java.lang.StringBuilder r0 = r0.append(r3)
            java.lang.String r5 = r0.toString()
            java.lang.String r0 = "POST"
            r1.setRequestMethod(r0)
            r6 = 1
            r1.setDoOutput(r6)
            r7 = 2
            java.lang.Object[] r0 = new java.lang.Object[r7]
            r8 = 0
            r0[r8] = r2
            java.lang.Integer r9 = java.lang.Integer.valueOf(r3)
            r0[r6] = r9
            java.lang.String r9 = "multipart/form-data; charset=%s; boundary=%s"
            java.lang.String r0 = java.lang.String.format(r9, r0)
            java.lang.String r9 = "Content-Type"
            r1.setRequestProperty(r9, r0)
            r0 = r28
            com.android.volley.request.MultiPartRequest r0 = (com.android.volley.request.MultiPartRequest) r0
            java.util.Map r9 = r0.getMultipartParams()
            r0 = r28
            com.android.volley.request.MultiPartRequest r0 = (com.android.volley.request.MultiPartRequest) r0
            java.util.Map r10 = r0.getFilesToUpload()
            r0 = r28
            com.android.volley.request.MultiPartRequest r0 = (com.android.volley.request.MultiPartRequest) r0
            boolean r0 = r0.isFixedStreamingMode()
            if (r0 == 0) goto L_0x0067
            int r0 = com.android.volley.misc.MultipartUtils.getContentLengthForMultipartRequest(r5, r9, r10)
            r1.setFixedLengthStreamingMode(r0)
            goto L_0x006a
        L_0x0067:
            r1.setChunkedStreamingMode(r8)
        L_0x006a:
            r11 = r28
            com.android.volley.Response$ProgressListener r11 = (com.android.volley.Response.ProgressListener) r11
            r12 = 0
            java.io.OutputStream r0 = r27.getOutputStream()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r13 = r0
            java.io.PrintWriter r0 = new java.io.PrintWriter     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.io.OutputStreamWriter r14 = new java.io.OutputStreamWriter     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r14.<init>(r13, r2)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r0.<init>(r14, r6)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r12 = r0
            java.util.Set r0 = r9.keySet()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
        L_0x0087:
            boolean r14 = r0.hasNext()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.lang.String r15 = "\r\n"
            if (r14 == 0) goto L_0x0107
            java.lang.Object r14 = r0.next()     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r14 = (java.lang.String) r14     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.Object r16 = r9.get(r14)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            com.android.volley.request.MultiPartRequest$MultiPartParam r16 = (com.android.volley.request.MultiPartRequest.MultiPartParam) r16     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r17 = r16
            java.io.PrintWriter r7 = r12.append(r5)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r7 = r7.append(r15)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r8 = "Content-Disposition: form-data; name=\"%s\""
            r19 = r0
            java.lang.Object[] r0 = new java.lang.Object[r6]     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r18 = 0
            r0[r18] = r14     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r0 = java.lang.String.format(r8, r0)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r7.append(r0)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r7.<init>()     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r8 = "Content-Type: "
            java.lang.StringBuilder r7 = r7.append(r8)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r8 = r17
            java.lang.String r6 = r8.contentType     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.StringBuilder r6 = r7.append(r6)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r6 = r6.toString()     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r6)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.lang.String r6 = r8.value     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r6)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r0.flush()     // Catch:{ Exception -> 0x00fc, all -> 0x00f1 }
            r0 = r19
            r6 = 1
            r7 = 2
            r8 = 0
            goto L_0x0087
        L_0x00f1:
            r0 = move-exception
            r16 = r2
            r22 = r3
            r25 = r9
            r26 = r10
            goto L_0x02d2
        L_0x00fc:
            r0 = move-exception
            r16 = r2
            r22 = r3
            r25 = r9
            r26 = r10
            goto L_0x02ca
        L_0x0107:
            r19 = r0
            java.util.Set r0 = r10.keySet()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r6 = r0
        L_0x0112:
            boolean r0 = r6.hasNext()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            if (r0 == 0) goto L_0x028a
            java.lang.Object r0 = r6.next()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r7 = r0
            java.io.File r0 = new java.io.File     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.lang.Object r8 = r10.get(r7)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.lang.String r8 = (java.lang.String) r8     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r0.<init>(r8)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            r8 = r0
            boolean r0 = r8.exists()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            if (r0 == 0) goto L_0x0268
            boolean r0 = r8.isDirectory()     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            if (r0 != 0) goto L_0x0246
            java.io.PrintWriter r0 = r12.append(r5)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x02c1, all -> 0x02b7 }
            java.lang.String r14 = "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\""
            r16 = r2
            r1 = 2
            java.lang.Object[] r2 = new java.lang.Object[r1]     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            r18 = 0
            r2[r18] = r7     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.lang.String r19 = r8.getName()     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            r17 = 1
            r2[r17] = r19     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.lang.String r2 = java.lang.String.format(r14, r2)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.io.PrintWriter r0 = r0.append(r2)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.lang.String r2 = "Content-Type: application/octet-stream"
            java.io.PrintWriter r0 = r0.append(r2)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.lang.String r2 = "Content-Transfer-Encoding: binary"
            java.io.PrintWriter r0 = r0.append(r2)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            r0.flush()     // Catch:{ Exception -> 0x023d, all -> 0x0234 }
            r2 = 0
            java.io.FileInputStream r0 = new java.io.FileInputStream     // Catch:{ all -> 0x0218 }
            r0.<init>(r8)     // Catch:{ all -> 0x0218 }
            r14 = 0
            r20 = r2
            long r1 = r8.length()     // Catch:{ all -> 0x0209 }
            int r1 = (int) r1     // Catch:{ all -> 0x0209 }
            java.io.BufferedInputStream r2 = new java.io.BufferedInputStream     // Catch:{ all -> 0x0209 }
            r2.<init>(r0)     // Catch:{ all -> 0x0209 }
            r20 = 0
            r21 = r0
            r0 = 1024(0x400, float:1.435E-42)
            byte[] r0 = new byte[r0]     // Catch:{ all -> 0x01fc }
        L_0x0194:
            int r22 = r2.read(r0)     // Catch:{ all -> 0x01fc }
            r23 = r22
            if (r22 <= 0) goto L_0x01c9
            r22 = r3
            r3 = r23
            r23 = r6
            r6 = 0
            r13.write(r0, r6, r3)     // Catch:{ all -> 0x01bf }
            int r14 = r14 + r3
            r24 = r7
            long r6 = (long) r14
            r25 = r9
            r26 = r10
            long r9 = (long) r1
            r11.onProgress(r6, r9)     // Catch:{ all -> 0x01f9 }
            r20 = r3
            r3 = r22
            r6 = r23
            r7 = r24
            r9 = r25
            r10 = r26
            goto L_0x0194
        L_0x01bf:
            r0 = move-exception
            r24 = r7
            r25 = r9
            r26 = r10
            r1 = r0
            goto L_0x0226
        L_0x01c9:
            r22 = r3
            r24 = r7
            r25 = r9
            r26 = r10
            r3 = r23
            r23 = r6
            r13.flush()     // Catch:{ all -> 0x01f9 }
            r2.close()     // Catch:{ IOException -> 0x01dd }
        L_0x01dc:
            goto L_0x01e4
        L_0x01dd:
            r0 = move-exception
            r1 = r0
            r0 = r1
            r0.printStackTrace()     // Catch:{ Exception -> 0x02b5 }
            goto L_0x01dc
        L_0x01e4:
            java.io.PrintWriter r0 = r12.append(r15)     // Catch:{ Exception -> 0x02b5 }
            r0.flush()     // Catch:{ Exception -> 0x02b5 }
            r1 = r27
            r2 = r16
            r3 = r22
            r6 = r23
            r9 = r25
            r10 = r26
            goto L_0x0112
        L_0x01f9:
            r0 = move-exception
            r1 = r0
            goto L_0x0226
        L_0x01fc:
            r0 = move-exception
            r22 = r3
            r23 = r6
            r24 = r7
            r25 = r9
            r26 = r10
            r1 = r0
            goto L_0x0226
        L_0x0209:
            r0 = move-exception
            r22 = r3
            r23 = r6
            r24 = r7
            r25 = r9
            r26 = r10
            r1 = r0
            r2 = r20
            goto L_0x0226
        L_0x0218:
            r0 = move-exception
            r20 = r2
            r22 = r3
            r23 = r6
            r24 = r7
            r25 = r9
            r26 = r10
            r1 = r0
        L_0x0226:
            if (r2 == 0) goto L_0x0232
            r2.close()     // Catch:{ IOException -> 0x022c }
            goto L_0x0232
        L_0x022c:
            r0 = move-exception
            r3 = r0
            r0 = r3
            r0.printStackTrace()     // Catch:{ Exception -> 0x02b5 }
        L_0x0232:
            throw r1     // Catch:{ Exception -> 0x02b5 }
        L_0x0234:
            r0 = move-exception
            r22 = r3
            r25 = r9
            r26 = r10
            goto L_0x02d2
        L_0x023d:
            r0 = move-exception
            r22 = r3
            r25 = r9
            r26 = r10
            goto L_0x02ca
        L_0x0246:
            r16 = r2
            r22 = r3
            r23 = r6
            r24 = r7
            r25 = r9
            r26 = r10
            java.io.IOException r0 = new java.io.IOException     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r1 = "File is a directory: %s"
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r3 = r8.getAbsolutePath()     // Catch:{ Exception -> 0x02b5 }
            r4 = 0
            r2[r4] = r3     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r1 = java.lang.String.format(r1, r2)     // Catch:{ Exception -> 0x02b5 }
            r0.<init>(r1)     // Catch:{ Exception -> 0x02b5 }
            throw r0     // Catch:{ Exception -> 0x02b5 }
        L_0x0268:
            r16 = r2
            r22 = r3
            r23 = r6
            r24 = r7
            r25 = r9
            r26 = r10
            java.io.IOException r0 = new java.io.IOException     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r1 = "File not found: %s"
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r3 = r8.getAbsolutePath()     // Catch:{ Exception -> 0x02b5 }
            r4 = 0
            r2[r4] = r3     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r1 = java.lang.String.format(r1, r2)     // Catch:{ Exception -> 0x02b5 }
            r0.<init>(r1)     // Catch:{ Exception -> 0x02b5 }
            throw r0     // Catch:{ Exception -> 0x02b5 }
        L_0x028a:
            r16 = r2
            r22 = r3
            r23 = r6
            r25 = r9
            r26 = r10
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x02b5 }
            r0.<init>()     // Catch:{ Exception -> 0x02b5 }
            java.lang.StringBuilder r0 = r0.append(r5)     // Catch:{ Exception -> 0x02b5 }
            java.lang.StringBuilder r0 = r0.append(r4)     // Catch:{ Exception -> 0x02b5 }
            java.lang.String r0 = r0.toString()     // Catch:{ Exception -> 0x02b5 }
            java.io.PrintWriter r0 = r12.append(r0)     // Catch:{ Exception -> 0x02b5 }
            java.io.PrintWriter r0 = r0.append(r15)     // Catch:{ Exception -> 0x02b5 }
            r0.flush()     // Catch:{ Exception -> 0x02b5 }
        L_0x02b1:
            r12.close()
            goto L_0x02d0
        L_0x02b5:
            r0 = move-exception
            goto L_0x02ca
        L_0x02b7:
            r0 = move-exception
            r16 = r2
            r22 = r3
            r25 = r9
            r26 = r10
            goto L_0x02d2
        L_0x02c1:
            r0 = move-exception
            r16 = r2
            r22 = r3
            r25 = r9
            r26 = r10
        L_0x02ca:
            r0.printStackTrace()     // Catch:{ all -> 0x02d1 }
            if (r12 == 0) goto L_0x02d0
            goto L_0x02b1
        L_0x02d0:
            return
        L_0x02d1:
            r0 = move-exception
        L_0x02d2:
            if (r12 == 0) goto L_0x02d7
            r12.close()
        L_0x02d7:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.toolbox.HurlStack.setConnectionParametersForMultipartRequest(java.net.HttpURLConnection, com.android.volley.Request):void");
    }

    private static HttpEntity entityFromConnection(HttpURLConnection connection) {
        InputStream inputStream;
        BasicHttpEntity entity = new BasicHttpEntity();
        try {
            inputStream = connection.getInputStream();
        } catch (IOException e) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength((long) connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    /* access modifiers changed from: protected */
    public HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
        return connection;
    }

    private HttpURLConnection openConnection(URL url, Request<?> request) throws IOException {
        SSLSocketFactory sSLSocketFactory;
        HttpURLConnection connection = createConnection(url);
        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        if ("https".equals(url.getProtocol()) && (sSLSocketFactory = this.mSslSocketFactory) != null) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sSLSocketFactory);
        }
        return connection;
    }

    static void setConnectionParametersForRequest(HttpURLConnection connection, Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case -1:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty(MultipartUtils.HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    out.write(postBody);
                    out.close();
                    return;
                }
                return;
            case 0:
                connection.setRequestMethod("GET");
                return;
            case 1:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                return;
            case 2:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                return;
            case 3:
                connection.setRequestMethod("DELETE");
                return;
            case 4:
                connection.setRequestMethod(Util.METHOD_HEAD);
                return;
            case 5:
                connection.setRequestMethod("OPTIONS");
                return;
            case 6:
                connection.setRequestMethod("TRACE");
                return;
            case 7:
                if (request.shouldOverridePatch()) {
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("X-HTTP-Method-Override", HttpClientStack.HttpPatch.METHOD_NAME);
                } else {
                    connection.setRequestMethod(HttpClientStack.HttpPatch.METHOD_NAME);
                }
                addBodyIfExists(connection, request);
                return;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request) throws IOException, AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            Response.ProgressListener progressListener = null;
            if (request instanceof Response.ProgressListener) {
                progressListener = (Response.ProgressListener) request;
            }
            connection.setDoOutput(true);
            connection.addRequestProperty(MultipartUtils.HEADER_CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (progressListener != null) {
                int transferredBytes = 0;
                int totalSize = body.length;
                int offset = 0;
                int chunkSize = Math.min(2048, Math.max(totalSize - 0, 0));
                while (chunkSize > 0 && offset + chunkSize <= totalSize) {
                    out.write(body, offset, chunkSize);
                    transferredBytes += chunkSize;
                    progressListener.onProgress((long) transferredBytes, (long) totalSize);
                    offset += chunkSize;
                    chunkSize = Math.min(chunkSize, Math.max(totalSize - offset, 0));
                }
            } else {
                out.write(body);
            }
            out.close();
        }
    }
}
