package com.liulishuo.okdownload.core.listener.assist;

import android.util.SparseArray;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.assist.Listener4Assist.Listener4Model;
import com.liulishuo.okdownload.core.listener.assist.ListenerModelHandler;

public class Listener4Assist<T extends Listener4Model> implements ListenerAssist {
    private AssistExtend assistExtend;
    Listener4Callback callback;
    private final ListenerModelHandler<T> modelHandler;

    public interface AssistExtend {
        boolean dispatchBlockEnd(DownloadTask downloadTask, int i, Listener4Model listener4Model);

        boolean dispatchFetchProgress(DownloadTask downloadTask, int i, long j, Listener4Model listener4Model);

        boolean dispatchInfoReady(DownloadTask downloadTask, BreakpointInfo breakpointInfo, boolean z, Listener4Model listener4Model);

        boolean dispatchTaskEnd(DownloadTask downloadTask, EndCause endCause, Exception exc, Listener4Model listener4Model);
    }

    public interface Listener4Callback {
        void blockEnd(DownloadTask downloadTask, int i, BlockInfo blockInfo);

        void infoReady(DownloadTask downloadTask, BreakpointInfo breakpointInfo, boolean z, Listener4Model listener4Model);

        void progress(DownloadTask downloadTask, long j);

        void progressBlock(DownloadTask downloadTask, int i, long j);

        void taskEnd(DownloadTask downloadTask, EndCause endCause, Exception exc, Listener4Model listener4Model);
    }

    Listener4Assist(ListenerModelHandler<T> handler) {
        this.modelHandler = handler;
    }

    public Listener4Assist(ListenerModelHandler.ModelCreator<T> creator) {
        this.modelHandler = new ListenerModelHandler<>(creator);
    }

    public void setCallback(Listener4Callback callback2) {
        this.callback = callback2;
    }

    public void setAssistExtend(AssistExtend assistExtend2) {
        this.assistExtend = assistExtend2;
    }

    public AssistExtend getAssistExtend() {
        return this.assistExtend;
    }

    public void infoReady(DownloadTask task, BreakpointInfo info, boolean fromBreakpoint) {
        Listener4Callback listener4Callback;
        Listener4Model model = (Listener4Model) this.modelHandler.addAndGetModel(task, info);
        AssistExtend assistExtend2 = this.assistExtend;
        if ((assistExtend2 == null || !assistExtend2.dispatchInfoReady(task, info, fromBreakpoint, model)) && (listener4Callback = this.callback) != null) {
            listener4Callback.infoReady(task, info, fromBreakpoint, model);
        }
    }

    public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
        Listener4Callback listener4Callback;
        Listener4Model model = (Listener4Model) this.modelHandler.getOrRecoverModel(task, task.getInfo());
        if (model != null) {
            long blockCurrentOffset = model.blockCurrentOffsetMap.get(blockIndex).longValue() + increaseBytes;
            model.blockCurrentOffsetMap.put(blockIndex, Long.valueOf(blockCurrentOffset));
            model.currentOffset += increaseBytes;
            AssistExtend assistExtend2 = this.assistExtend;
            if ((assistExtend2 == null || !assistExtend2.dispatchFetchProgress(task, blockIndex, increaseBytes, model)) && (listener4Callback = this.callback) != null) {
                listener4Callback.progressBlock(task, blockIndex, blockCurrentOffset);
                this.callback.progress(task, model.currentOffset);
            }
        }
    }

    public void fetchEnd(DownloadTask task, int blockIndex) {
        Listener4Callback listener4Callback;
        Listener4Model model = (Listener4Model) this.modelHandler.getOrRecoverModel(task, task.getInfo());
        if (model != null) {
            AssistExtend assistExtend2 = this.assistExtend;
            if ((assistExtend2 == null || !assistExtend2.dispatchBlockEnd(task, blockIndex, model)) && (listener4Callback = this.callback) != null) {
                listener4Callback.blockEnd(task, blockIndex, model.info.getBlock(blockIndex));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void taskEnd(com.liulishuo.okdownload.DownloadTask r3, com.liulishuo.okdownload.core.cause.EndCause r4, java.lang.Exception r5) {
        /*
            r2 = this;
            monitor-enter(r2)
            com.liulishuo.okdownload.core.listener.assist.ListenerModelHandler<T> r0 = r2.modelHandler     // Catch:{ all -> 0x0022 }
            com.liulishuo.okdownload.core.breakpoint.BreakpointInfo r1 = r3.getInfo()     // Catch:{ all -> 0x0022 }
            com.liulishuo.okdownload.core.listener.assist.ListenerModelHandler$ListenerModel r0 = r0.removeOrCreate(r3, r1)     // Catch:{ all -> 0x0022 }
            com.liulishuo.okdownload.core.listener.assist.Listener4Assist$Listener4Model r0 = (com.liulishuo.okdownload.core.listener.assist.Listener4Assist.Listener4Model) r0     // Catch:{ all -> 0x0022 }
            com.liulishuo.okdownload.core.listener.assist.Listener4Assist$AssistExtend r1 = r2.assistExtend     // Catch:{ all -> 0x0022 }
            if (r1 == 0) goto L_0x0019
            boolean r1 = r1.dispatchTaskEnd(r3, r4, r5, r0)     // Catch:{ all -> 0x0022 }
            if (r1 == 0) goto L_0x0019
            monitor-exit(r2)
            return
        L_0x0019:
            com.liulishuo.okdownload.core.listener.assist.Listener4Assist$Listener4Callback r1 = r2.callback     // Catch:{ all -> 0x0022 }
            if (r1 == 0) goto L_0x0020
            r1.taskEnd(r3, r4, r5, r0)     // Catch:{ all -> 0x0022 }
        L_0x0020:
            monitor-exit(r2)
            return
        L_0x0022:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.liulishuo.okdownload.core.listener.assist.Listener4Assist.taskEnd(com.liulishuo.okdownload.DownloadTask, com.liulishuo.okdownload.core.cause.EndCause, java.lang.Exception):void");
    }

    public boolean isAlwaysRecoverAssistModel() {
        return this.modelHandler.isAlwaysRecoverAssistModel();
    }

    public void setAlwaysRecoverAssistModel(boolean isAlwaysRecoverAssistModel) {
        this.modelHandler.setAlwaysRecoverAssistModel(isAlwaysRecoverAssistModel);
    }

    public void setAlwaysRecoverAssistModelIfNotSet(boolean isAlwaysRecoverAssistModel) {
        this.modelHandler.setAlwaysRecoverAssistModelIfNotSet(isAlwaysRecoverAssistModel);
    }

    public static class Listener4Model implements ListenerModelHandler.ListenerModel {
        SparseArray<Long> blockCurrentOffsetMap;
        long currentOffset;

        /* renamed from: id */
        private final int f192id;
        BreakpointInfo info;

        public Listener4Model(int id) {
            this.f192id = id;
        }

        /* access modifiers changed from: package-private */
        public SparseArray<Long> getBlockCurrentOffsetMap() {
            return this.blockCurrentOffsetMap;
        }

        public long getCurrentOffset() {
            return this.currentOffset;
        }

        public long getBlockCurrentOffset(int blockIndex) {
            return this.blockCurrentOffsetMap.get(blockIndex).longValue();
        }

        public SparseArray<Long> cloneBlockCurrentOffsetMap() {
            return this.blockCurrentOffsetMap.clone();
        }

        public BreakpointInfo getInfo() {
            return this.info;
        }

        public int getId() {
            return this.f192id;
        }

        public void onInfoValid(BreakpointInfo info2) {
            this.info = info2;
            this.currentOffset = info2.getTotalOffset();
            SparseArray<Long> blockCurrentOffsetMap2 = new SparseArray<>();
            int blockCount = info2.getBlockCount();
            for (int i = 0; i < blockCount; i++) {
                blockCurrentOffsetMap2.put(i, Long.valueOf(info2.getBlock(i).getCurrentOffset()));
            }
            this.blockCurrentOffsetMap = blockCurrentOffsetMap2;
        }
    }
}
