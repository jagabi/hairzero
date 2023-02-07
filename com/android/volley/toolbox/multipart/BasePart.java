package com.android.volley.toolbox.multipart;

import com.android.volley.misc.MultipartUtils;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

abstract class BasePart implements Part {
    private byte[] header;
    protected IHeadersProvider headersProvider;

    protected interface IHeadersProvider {
        String getContentDisposition();

        String getContentTransferEncoding();

        String getContentType();
    }

    BasePart() {
    }

    /* access modifiers changed from: protected */
    public byte[] getHeader(Boundary boundary) {
        if (this.header == null) {
            this.header = generateHeader(boundary);
        }
        return this.header;
    }

    private byte[] generateHeader(Boundary boundary) {
        if (this.headersProvider != null) {
            ByteArrayBuffer buf = new ByteArrayBuffer(256);
            append(buf, boundary.getStartingBoundary());
            append(buf, this.headersProvider.getContentDisposition());
            append(buf, MultipartUtils.CRLF_BYTES);
            append(buf, this.headersProvider.getContentType());
            append(buf, MultipartUtils.CRLF_BYTES);
            append(buf, MultipartUtils.CRLF_BYTES);
            return buf.toByteArray();
        }
        throw new RuntimeException("Uninitialized headersProvider");
    }

    private static void append(ByteArrayBuffer buf, String data) {
        append(buf, EncodingUtils.getAsciiBytes(data));
    }

    private static void append(ByteArrayBuffer buf, byte[] data) {
        buf.append(data, 0, data.length);
    }
}
