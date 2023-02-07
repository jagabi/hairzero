package com.android.volley.toolbox.multipart;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UrlEncodingHelper {
    public static String encode(String content, String encoding) {
        try {
            return URLEncoder.encode(content, encoding != null ? encoding : "ISO-8859-1");
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }
}
