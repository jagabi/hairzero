package com.android.volley.toolbox.multipart;

import com.android.volley.Response;
import java.io.IOException;
import java.io.OutputStream;

public class MultipartProgressEntity extends MultipartEntity {
    private Response.ProgressListener listener;

    public void setListener(Response.ProgressListener listener2) {
        this.listener = listener2;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (this.listener == null) {
            super.writeTo(outstream);
        } else {
            super.writeTo(new OutputStreamProgress(outstream, this.listener));
        }
    }
}
