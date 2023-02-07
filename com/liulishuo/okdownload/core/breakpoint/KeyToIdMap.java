package com.liulishuo.okdownload.core.breakpoint;

import android.util.SparseArray;
import com.liulishuo.okdownload.DownloadTask;
import java.util.HashMap;

public class KeyToIdMap {
    private final SparseArray<String> idToKeyMap;
    private final HashMap<String, Integer> keyToIdMap;

    KeyToIdMap() {
        this(new HashMap(), new SparseArray());
    }

    KeyToIdMap(HashMap<String, Integer> keyToIdMap2, SparseArray<String> idToKeyMap2) {
        this.keyToIdMap = keyToIdMap2;
        this.idToKeyMap = idToKeyMap2;
    }

    public Integer get(DownloadTask task) {
        Integer candidate = this.keyToIdMap.get(generateKey(task));
        if (candidate != null) {
            return candidate;
        }
        return null;
    }

    public void remove(int id) {
        String key = this.idToKeyMap.get(id);
        if (key != null) {
            this.keyToIdMap.remove(key);
            this.idToKeyMap.remove(id);
        }
    }

    public void add(DownloadTask task, int id) {
        String key = generateKey(task);
        this.keyToIdMap.put(key, Integer.valueOf(id));
        this.idToKeyMap.put(id, key);
    }

    /* access modifiers changed from: package-private */
    public String generateKey(DownloadTask task) {
        return task.getUrl() + task.getUri() + task.getFilename();
    }
}
