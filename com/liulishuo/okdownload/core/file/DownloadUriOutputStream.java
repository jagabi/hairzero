package com.liulishuo.okdownload.core.file;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.file.DownloadOutputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DownloadUriOutputStream implements DownloadOutputStream {
    private final FileChannel channel;
    final FileOutputStream fos;
    final BufferedOutputStream out;
    final ParcelFileDescriptor pdf;

    public DownloadUriOutputStream(Context context, Uri uri, int bufferSize) throws FileNotFoundException {
        ParcelFileDescriptor pdf2 = context.getContentResolver().openFileDescriptor(uri, "rw");
        if (pdf2 != null) {
            this.pdf = pdf2;
            FileOutputStream fileOutputStream = new FileOutputStream(pdf2.getFileDescriptor());
            this.fos = fileOutputStream;
            this.channel = fileOutputStream.getChannel();
            this.out = new BufferedOutputStream(fileOutputStream, bufferSize);
            return;
        }
        throw new FileNotFoundException("result of " + uri + " is null!");
    }

    DownloadUriOutputStream(FileChannel channel2, ParcelFileDescriptor pdf2, FileOutputStream fos2, BufferedOutputStream out2) {
        this.channel = channel2;
        this.pdf = pdf2;
        this.fos = fos2;
        this.out = out2;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }

    public void close() throws IOException {
        this.out.close();
        this.fos.close();
    }

    public void flushAndSync() throws IOException {
        this.out.flush();
        this.pdf.getFileDescriptor().sync();
    }

    public void seek(long offset) throws IOException {
        this.channel.position(offset);
    }

    public void setLength(long newLength) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Os.posix_fallocate(this.pdf.getFileDescriptor(), 0, newLength);
            } catch (Throwable e1) {
                Util.m86w("DownloadUriOutputStream", "It can't pre-allocate length(" + newLength + ") on the sdk version(" + Build.VERSION.SDK_INT + "), because of " + e1);
            }
        } else {
            Util.m86w("DownloadUriOutputStream", "It can't pre-allocate length(" + newLength + ") on the sdk version(" + Build.VERSION.SDK_INT + ")");
        }
    }

    public static class Factory implements DownloadOutputStream.Factory {
        public DownloadOutputStream create(Context context, File file, int flushBufferSize) throws FileNotFoundException {
            return new DownloadUriOutputStream(context, Uri.fromFile(file), flushBufferSize);
        }

        public DownloadOutputStream create(Context context, Uri uri, int flushBufferSize) throws FileNotFoundException {
            return new DownloadUriOutputStream(context, uri, flushBufferSize);
        }

        public boolean supportSeek() {
            return true;
        }
    }
}
