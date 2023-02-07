package com.liulishuo.okdownload.core.breakpoint;

import android.util.SparseArray;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.IdentifiedTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BreakpointStoreOnCache implements DownloadStore {
    public static final int FIRST_ID = 1;
    private final List<Integer> fileDirtyList;
    private final KeyToIdMap keyToIdMap;
    private final HashMap<String, String> responseFilenameMap;
    private final List<Integer> sortedOccupiedIds;
    private final SparseArray<BreakpointInfo> storedInfos;
    private final SparseArray<IdentifiedTask> unStoredTasks;

    public BreakpointStoreOnCache() {
        this(new SparseArray(), new ArrayList(), new HashMap());
    }

    BreakpointStoreOnCache(SparseArray<BreakpointInfo> storedInfos2, List<Integer> fileDirtyList2, HashMap<String, String> responseFilenameMap2, SparseArray<IdentifiedTask> unStoredTasks2, List<Integer> sortedOccupiedIds2, KeyToIdMap keyToIdMap2) {
        this.unStoredTasks = unStoredTasks2;
        this.fileDirtyList = fileDirtyList2;
        this.storedInfos = storedInfos2;
        this.responseFilenameMap = responseFilenameMap2;
        this.sortedOccupiedIds = sortedOccupiedIds2;
        this.keyToIdMap = keyToIdMap2;
    }

    public BreakpointStoreOnCache(SparseArray<BreakpointInfo> storedInfos2, List<Integer> fileDirtyList2, HashMap<String, String> responseFilenameMap2) {
        this.unStoredTasks = new SparseArray<>();
        this.storedInfos = storedInfos2;
        this.fileDirtyList = fileDirtyList2;
        this.responseFilenameMap = responseFilenameMap2;
        this.keyToIdMap = new KeyToIdMap();
        int count = storedInfos2.size();
        this.sortedOccupiedIds = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            this.sortedOccupiedIds.add(Integer.valueOf(storedInfos2.valueAt(i).f190id));
        }
        Collections.sort(this.sortedOccupiedIds);
    }

    public BreakpointInfo get(int id) {
        return this.storedInfos.get(id);
    }

    public BreakpointInfo createAndInsert(DownloadTask task) {
        int id = task.getId();
        BreakpointInfo newInfo = new BreakpointInfo(id, task.getUrl(), task.getParentFile(), task.getFilename());
        synchronized (this) {
            this.storedInfos.put(id, newInfo);
            this.unStoredTasks.remove(id);
        }
        return newInfo;
    }

    public void onTaskStart(int id) {
    }

    public void onSyncToFilesystemSuccess(BreakpointInfo info, int blockIndex, long increaseLength) throws IOException {
        BreakpointInfo onCacheOne = this.storedInfos.get(info.f190id);
        if (info == onCacheOne) {
            onCacheOne.getBlock(blockIndex).increaseCurrentOffset(increaseLength);
            return;
        }
        throw new IOException("Info not on store!");
    }

    public boolean update(BreakpointInfo breakpointInfo) {
        String filename = breakpointInfo.getFilename();
        if (breakpointInfo.isTaskOnlyProvidedParentPath() && filename != null) {
            this.responseFilenameMap.put(breakpointInfo.getUrl(), filename);
        }
        BreakpointInfo onCacheOne = this.storedInfos.get(breakpointInfo.f190id);
        if (onCacheOne == null) {
            return false;
        }
        if (onCacheOne == breakpointInfo) {
            return true;
        }
        synchronized (this) {
            this.storedInfos.put(breakpointInfo.f190id, breakpointInfo.copy());
        }
        return true;
    }

    public void onTaskEnd(int id, EndCause cause, Exception exception) {
        if (cause == EndCause.COMPLETED) {
            remove(id);
        }
    }

    public BreakpointInfo getAfterCompleted(int id) {
        return null;
    }

    public boolean markFileDirty(int id) {
        if (this.fileDirtyList.contains(Integer.valueOf(id))) {
            return false;
        }
        synchronized (this.fileDirtyList) {
            if (this.fileDirtyList.contains(Integer.valueOf(id))) {
                return false;
            }
            this.fileDirtyList.add(Integer.valueOf(id));
            return true;
        }
    }

    public boolean markFileClear(int id) {
        boolean remove;
        synchronized (this.fileDirtyList) {
            remove = this.fileDirtyList.remove(Integer.valueOf(id));
        }
        return remove;
    }

    public synchronized void remove(int id) {
        this.storedInfos.remove(id);
        if (this.unStoredTasks.get(id) == null) {
            this.sortedOccupiedIds.remove(Integer.valueOf(id));
        }
        this.keyToIdMap.remove(id);
    }

    public synchronized int findOrCreateId(DownloadTask task) {
        Integer candidate = this.keyToIdMap.get(task);
        if (candidate != null) {
            return candidate.intValue();
        }
        int size = this.storedInfos.size();
        int i = 0;
        while (i < size) {
            BreakpointInfo info = this.storedInfos.valueAt(i);
            if (info == null || !info.isSameFrom(task)) {
                i++;
            } else {
                return info.f190id;
            }
        }
        int unStoredSize = this.unStoredTasks.size();
        for (int i2 = 0; i2 < unStoredSize; i2++) {
            IdentifiedTask another = this.unStoredTasks.valueAt(i2);
            if (another != null) {
                if (another.compareIgnoreId(task)) {
                    return another.getId();
                }
            }
        }
        int i3 = allocateId();
        this.unStoredTasks.put(i3, task.mock(i3));
        this.keyToIdMap.add(task, i3);
        return i3;
    }

    public BreakpointInfo findAnotherInfoFromCompare(DownloadTask task, BreakpointInfo ignored) {
        SparseArray<BreakpointInfo> clonedMap;
        synchronized (this) {
            clonedMap = this.storedInfos.clone();
        }
        int size = clonedMap.size();
        for (int i = 0; i < size; i++) {
            BreakpointInfo info = clonedMap.valueAt(i);
            if (info != ignored && info.isSameFrom(task)) {
                return info;
            }
        }
        return null;
    }

    public boolean isOnlyMemoryCache() {
        return true;
    }

    public boolean isFileDirty(int id) {
        return this.fileDirtyList.contains(Integer.valueOf(id));
    }

    public String getResponseFilename(String url) {
        return this.responseFilenameMap.get(url);
    }

    /* access modifiers changed from: package-private */
    public synchronized int allocateId() {
        int newId;
        newId = 0;
        int index = 0;
        int preId = 0;
        int i = 0;
        while (true) {
            if (i >= this.sortedOccupiedIds.size()) {
                break;
            }
            Integer curIdObj = this.sortedOccupiedIds.get(i);
            if (curIdObj == null) {
                index = i;
                newId = preId + 1;
                break;
            }
            int curId = curIdObj.intValue();
            if (preId == 0) {
                if (curId != 1) {
                    newId = 1;
                    index = 0;
                    break;
                }
                preId = curId;
            } else if (curId != preId + 1) {
                newId = preId + 1;
                index = i;
                break;
            } else {
                preId = curId;
            }
            i++;
        }
        if (newId == 0) {
            if (this.sortedOccupiedIds.isEmpty()) {
                newId = 1;
            } else {
                List<Integer> list = this.sortedOccupiedIds;
                newId = list.get(list.size() - 1).intValue() + 1;
                index = this.sortedOccupiedIds.size();
            }
        }
        this.sortedOccupiedIds.add(index, Integer.valueOf(newId));
        return newId;
    }
}
