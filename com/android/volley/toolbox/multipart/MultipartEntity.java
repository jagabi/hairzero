package com.android.volley.toolbox.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.entity.AbstractHttpEntity;

public class MultipartEntity extends AbstractHttpEntity implements Cloneable {
    private Boundary boundary;
    private List<Part> parts;

    public MultipartEntity(String boundaryStr) {
        this.parts = new ArrayList();
        this.boundary = new Boundary(boundaryStr);
        setContentType("multipart/form-data; boundary=\"" + this.boundary.getBoundary() + '\"');
    }

    public MultipartEntity() {
        this((String) null);
    }

    public void addPart(Part part) {
        this.parts.add(part);
    }

    public boolean isRepeatable() {
        return true;
    }

    public String getBoundary() {
        return this.boundary.getBoundary();
    }

    public long getContentLength() {
        long result = 0;
        for (Part part : this.parts) {
            result += part.getContentLength(this.boundary);
        }
        return result + ((long) this.boundary.getClosingBoundary().length);
    }

    public InputStream getContent() throws IOException {
        return null;
    }

    public void writeTo(OutputStream out) throws IOException {
        if (out != null) {
            for (Part part : this.parts) {
                part.writeTo(out, this.boundary);
            }
            out.write(this.boundary.getClosingBoundary());
            out.flush();
            return;
        }
        throw new IllegalArgumentException("Output stream may not be null");
    }

    public boolean isStreaming() {
        return false;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("MultipartEntity does not support cloning");
    }
}
