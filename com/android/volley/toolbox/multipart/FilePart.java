package com.android.volley.toolbox.multipart;

import com.android.volley.misc.MultipartUtils;
import com.android.volley.toolbox.multipart.BasePart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class FilePart extends BasePart {
    private final File file;

    public FilePart(String name, File file2, String filename, String contentType) {
        if (file2 == null) {
            throw new IllegalArgumentException("File may not be null");
        } else if (name != null) {
            this.file = file2;
            final String partName = UrlEncodingHelper.encode(name, "US-ASCII");
            final String partFilename = UrlEncodingHelper.encode(filename == null ? file2.getName() : filename, "US-ASCII");
            final String partContentType = contentType == null ? MultipartUtils.CONTENT_TYPE_OCTET_STREAM : contentType;
            this.headersProvider = new BasePart.IHeadersProvider() {
                public String getContentDisposition() {
                    return String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", new Object[]{partName, partFilename});
                }

                public String getContentType() {
                    return "Content-Type: " + partContentType;
                }

                public String getContentTransferEncoding() {
                    return "Content-Transfer-Encoding: binary";
                }
            };
        } else {
            throw new IllegalArgumentException("Name may not be null");
        }
    }

    public long getContentLength(Boundary boundary) {
        return ((long) getHeader(boundary).length) + this.file.length() + ((long) MultipartUtils.CRLF_BYTES.length);
    }

    /* JADX INFO: finally extract failed */
    public void writeTo(OutputStream out, Boundary boundary) throws IOException {
        out.write(getHeader(boundary));
        InputStream in = new FileInputStream(this.file);
        try {
            byte[] tmp = new byte[4096];
            while (true) {
                int read = in.read(tmp);
                int l = read;
                if (read != -1) {
                    out.write(tmp, 0, l);
                } else {
                    in.close();
                    out.write(MultipartUtils.CRLF_BYTES);
                    return;
                }
            }
        } catch (Throwable th) {
            in.close();
            throw th;
        }
    }
}
