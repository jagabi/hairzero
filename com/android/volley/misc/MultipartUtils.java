package com.android.volley.misc;

import com.android.volley.request.MultiPartRequest;
import java.io.File;
import java.util.Map;
import org.apache.http.util.EncodingUtils;

public class MultipartUtils {
    public static final String BINARY = "binary";
    public static final int BINARY_LENGTH = BINARY.getBytes().length;
    public static final String BOUNDARY_PREFIX = "--";
    public static final int BOUNDARY_PREFIX_LENGTH = BOUNDARY_PREFIX.getBytes().length;
    public static final String COLON_SPACE = ": ";
    public static final int COLON_SPACE_LENGTH = COLON_SPACE.getBytes().length;
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; charset=%s; boundary=%s";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    public static final int CONTENT_TYPE_OCTET_STREAM_LENGTH = CONTENT_TYPE_OCTET_STREAM.getBytes().length;
    public static final String CRLF = "\r\n";
    public static final byte[] CRLF_BYTES = EncodingUtils.getAsciiBytes(CRLF);
    public static final int CRLF_LENGTH = CRLF.getBytes().length;
    public static final String EIGHT_BIT = "8bit";
    public static final String FILENAME = "filename=\"%s\"";
    public static final String FORM_DATA = "form-data; name=\"%s\"";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final int HEADER_CONTENT_DISPOSITION_LENGTH = "Content-Disposition".getBytes().length;
    public static final String HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final int HEADER_CONTENT_TRANSFER_ENCODING_LENGTH = HEADER_CONTENT_TRANSFER_ENCODING.getBytes().length;
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final int HEADER_CONTENT_TYPE_LENGTH = HEADER_CONTENT_TYPE.getBytes().length;
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String SEMICOLON_SPACE = "; ";

    public static int getContentLengthForMultipartRequest(String boundary, Map<String, MultiPartRequest.MultiPartParam> multipartParams, Map<String, String> filesToUpload) {
        int boundaryLength = boundary.getBytes().length;
        int contentLength = 0;
        for (String key : multipartParams.keySet()) {
            MultiPartRequest.MultiPartParam param = multipartParams.get(key);
            int i = CRLF_LENGTH;
            int i2 = boundaryLength + i + HEADER_CONTENT_DISPOSITION_LENGTH;
            int i3 = COLON_SPACE_LENGTH;
            contentLength += i2 + i3 + String.format(FORM_DATA, new Object[]{key}).getBytes().length + i + HEADER_CONTENT_TYPE_LENGTH + i3 + param.contentType.getBytes().length + i + i + param.value.getBytes().length + i;
        }
        for (String key2 : filesToUpload.keySet()) {
            File file = new File(filesToUpload.get(key2));
            int i4 = CRLF_LENGTH;
            int i5 = boundaryLength + i4 + HEADER_CONTENT_DISPOSITION_LENGTH;
            int i6 = COLON_SPACE_LENGTH;
            contentLength += i5 + i6 + String.format("form-data; name=\"%s\"; filename=\"%s\"", new Object[]{key2, file.getName()}).getBytes().length + i4 + HEADER_CONTENT_TYPE_LENGTH + i6 + CONTENT_TYPE_OCTET_STREAM_LENGTH + i4 + HEADER_CONTENT_TRANSFER_ENCODING_LENGTH + i6 + BINARY_LENGTH + i4 + i4 + ((int) file.length()) + i4;
        }
        return contentLength + BOUNDARY_PREFIX_LENGTH + boundaryLength + CRLF_LENGTH;
    }
}
