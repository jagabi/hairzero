package com.android.volley.toolbox.multipart;

import com.android.volley.misc.MultipartUtils;
import com.android.volley.toolbox.multipart.BasePart;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public final class StringPart extends BasePart {
    private final byte[] valueBytes;

    public StringPart(String name, String value, String charset) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        } else if (value != null) {
            final String partName = UrlEncodingHelper.encode(name, "US-ASCII");
            try {
                this.valueBytes = value.getBytes(charset == null ? "ISO-8859-1" : charset);
                this.headersProvider = new BasePart.IHeadersProvider() {
                    public String getContentDisposition() {
                        return String.format("Content-Disposition: form-data; name=\"%s\"", new Object[]{partName});
                    }

                    public String getContentType() {
                        return "Content-Type: text/plain";
                    }

                    public String getContentTransferEncoding() {
                        return "Content-Transfer-Encoding: 8bit";
                    }
                };
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Value may not be null");
        }
    }

    public StringPart(String name, String value) {
        this(name, value, (String) null);
    }

    public long getContentLength(Boundary boundary) {
        return (long) (getHeader(boundary).length + this.valueBytes.length + MultipartUtils.CRLF_BYTES.length);
    }

    public void writeTo(OutputStream out, Boundary boundary) throws IOException {
        out.write(getHeader(boundary));
        out.write(this.valueBytes);
        out.write(MultipartUtils.CRLF_BYTES);
    }
}
