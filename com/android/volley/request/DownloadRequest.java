package com.android.volley.request;

import com.android.volley.Request;
import com.android.volley.Response;

public class DownloadRequest extends Request<String> implements Response.ProgressListener {
    private final String mDownloadPath;
    private final Response.Listener<String> mListener;
    private Response.ProgressListener mProgressListener;

    public DownloadRequest(String url, String download_path, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(0, url, errorListener);
        this.mDownloadPath = download_path;
        this.mListener = listener;
    }

    public void setOnProgressListener(Response.ProgressListener listener) {
        this.mProgressListener = listener;
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(String response) {
        Response.Listener<String> listener = this.mListener;
        if (listener != null) {
            listener.onResponse(response);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0024, code lost:
        if (android.text.TextUtils.isEmpty((java.lang.CharSequence) null) == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002f, code lost:
        if (android.text.TextUtils.isEmpty((java.lang.CharSequence) null) == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0031, code lost:
        r0 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        if (android.text.TextUtils.isEmpty(r0) == false) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004c, code lost:
        return com.android.volley.Response.success(r0, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(r5));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0017, code lost:
        if (android.text.TextUtils.isEmpty(r0) != false) goto L_0x0031;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.volley.Response<java.lang.String> parseNetworkResponse(com.android.volley.NetworkResponse r5) {
        /*
            r4 = this;
            r0 = 0
            byte[] r1 = r5.data     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            java.lang.String r3 = r4.mDownloadPath     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            r2.<init>(r3)     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            r2.write(r1)     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            r2.close()     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            java.lang.String r3 = r4.mDownloadPath     // Catch:{ UnsupportedEncodingException -> 0x0034, FileNotFoundException -> 0x0027, IOException -> 0x001c }
            r0 = r3
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x0044
            goto L_0x0031
        L_0x001a:
            r1 = move-exception
            goto L_0x004d
        L_0x001c:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x001a }
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x0044
            goto L_0x0031
        L_0x0027:
            r1 = move-exception
            r1.printStackTrace()     // Catch:{ all -> 0x001a }
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x0044
        L_0x0031:
            java.lang.String r0 = ""
            goto L_0x0044
        L_0x0034:
            r1 = move-exception
            java.lang.String r2 = new java.lang.String     // Catch:{ all -> 0x001a }
            byte[] r3 = r5.data     // Catch:{ all -> 0x001a }
            r2.<init>(r3)     // Catch:{ all -> 0x001a }
            r0 = r2
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 == 0) goto L_0x0044
            goto L_0x0031
        L_0x0044:
            com.android.volley.Cache$Entry r1 = com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(r5)
            com.android.volley.Response r1 = com.android.volley.Response.success(r0, r1)
            return r1
        L_0x004d:
            boolean r2 = android.text.TextUtils.isEmpty(r0)
            if (r2 == 0) goto L_0x0055
            java.lang.String r0 = ""
        L_0x0055:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.request.DownloadRequest.parseNetworkResponse(com.android.volley.NetworkResponse):com.android.volley.Response");
    }

    public void onProgress(long transferredBytes, long totalSize) {
        Response.ProgressListener progressListener = this.mProgressListener;
        if (progressListener != null) {
            progressListener.onProgress(transferredBytes, totalSize);
        }
    }
}
