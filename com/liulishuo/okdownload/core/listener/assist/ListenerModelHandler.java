package com.liulishuo.okdownload.core.listener.assist;

import android.util.SparseArray;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.listener.assist.ListenerModelHandler.ListenerModel;

public class ListenerModelHandler<T extends ListenerModel> implements ListenerAssist {
    private Boolean alwaysRecoverModel;
    private final ModelCreator<T> creator;
    final SparseArray<T> modelList = new SparseArray<>();
    volatile T singleTaskModel;

    interface ListenerModel {
        int getId();

        void onInfoValid(BreakpointInfo breakpointInfo);
    }

    public interface ModelCreator<T extends ListenerModel> {
        T create(int i);
    }

    ListenerModelHandler(ModelCreator<T> creator2) {
        this.creator = creator2;
    }

    public boolean isAlwaysRecoverAssistModel() {
        Boolean bool = this.alwaysRecoverModel;
        return bool != null && bool.booleanValue();
    }

    public void setAlwaysRecoverAssistModel(boolean isAlwaysRecoverModel) {
        this.alwaysRecoverModel = Boolean.valueOf(isAlwaysRecoverModel);
    }

    public void setAlwaysRecoverAssistModelIfNotSet(boolean isAlwaysRecoverAssistModel) {
        if (this.alwaysRecoverModel == null) {
            this.alwaysRecoverModel = Boolean.valueOf(isAlwaysRecoverAssistModel);
        }
    }

    /* access modifiers changed from: package-private */
    public T addAndGetModel(DownloadTask task, BreakpointInfo info) {
        T model = this.creator.create(task.getId());
        synchronized (this) {
            if (this.singleTaskModel == null) {
                this.singleTaskModel = model;
            } else {
                this.modelList.put(task.getId(), model);
            }
            if (info != null) {
                model.onInfoValid(info);
            }
        }
        return model;
    }

    /* access modifiers changed from: package-private */
    public T getOrRecoverModel(DownloadTask task, BreakpointInfo info) {
        int id = task.getId();
        T model = null;
        synchronized (this) {
            if (this.singleTaskModel != null && this.singleTaskModel.getId() == id) {
                model = this.singleTaskModel;
            }
        }
        if (model == null) {
            model = (ListenerModel) this.modelList.get(id);
        }
        if (model != null || !isAlwaysRecoverAssistModel()) {
            return model;
        }
        return addAndGetModel(task, info);
    }

    /* access modifiers changed from: package-private */
    public T removeOrCreate(DownloadTask task, BreakpointInfo info) {
        T model;
        int id = task.getId();
        synchronized (this) {
            if (this.singleTaskModel == null || this.singleTaskModel.getId() != id) {
                model = (ListenerModel) this.modelList.get(id);
                this.modelList.remove(id);
            } else {
                model = this.singleTaskModel;
                this.singleTaskModel = null;
            }
        }
        if (model == null) {
            model = this.creator.create(id);
            if (info != null) {
                model.onInfoValid(info);
            }
        }
        return model;
    }
}
