package com.android.volley;

import java.util.Collections;
import java.util.Map;

public interface Cache {
    void clear();

    void close();

    void flush();

    Entry get(String str);

    void initialize();

    void invalidate(String str, boolean z);

    void put(String str, Entry entry);

    void remove(String str);

    public static class Entry {
        public byte[] data;
        public String etag;
        public long lastModified;
        public Map<String, String> responseHeaders = Collections.emptyMap();
        public long serverDate;
        public long softTtl;
        public long ttl;

        public boolean isExpired() {
            long j = this.ttl;
            return j != 0 && j < System.currentTimeMillis();
        }

        public boolean refreshNeeded() {
            long j = this.softTtl;
            return j != 0 && j < System.currentTimeMillis();
        }
    }
}
