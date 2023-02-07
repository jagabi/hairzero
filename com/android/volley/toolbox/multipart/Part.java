package com.android.volley.toolbox.multipart;

import java.io.IOException;
import java.io.OutputStream;

public interface Part {
    long getContentLength(Boundary boundary);

    void writeTo(OutputStream outputStream, Boundary boundary) throws IOException;
}
