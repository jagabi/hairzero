package com.android.volley.toolbox.multipart;

import com.android.volley.Response;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamProgress extends OutputStream {
    private long bytesWritten = 0;
    private final OutputStream outstream;
    private final Response.ProgressListener progressListener;
    private long totalSize = 0;

    public OutputStreamProgress(OutputStream outstream2, Response.ProgressListener progressListener2) {
        this.outstream = outstream2;
        this.progressListener = progressListener2;
    }

    public void write(int b) throws IOException {
        this.outstream.write(b);
        Response.ProgressListener progressListener2 = this.progressListener;
        if (progressListener2 != null) {
            long j = this.bytesWritten + 1;
            this.bytesWritten = j;
            progressListener2.onProgress(j, this.totalSize);
        }
    }

    public void write(byte[] b) throws IOException {
        this.outstream.write(b);
        Response.ProgressListener progressListener2 = this.progressListener;
        if (progressListener2 != null) {
            long length = this.bytesWritten + ((long) b.length);
            this.bytesWritten = length;
            progressListener2.onProgress(length, this.totalSize);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.outstream.write(b, off, len);
        Response.ProgressListener progressListener2 = this.progressListener;
        if (progressListener2 != null) {
            long j = this.bytesWritten + ((long) len);
            this.bytesWritten = j;
            progressListener2.onProgress(j, this.totalSize);
        }
    }

    public void flush() throws IOException {
        this.outstream.flush();
    }

    public void close() throws IOException {
        this.outstream.close();
    }
}
