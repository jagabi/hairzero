package com.liulishuo.okdownload.core.listener;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

public abstract class DownloadListener3 extends DownloadListener1 {
    /* access modifiers changed from: protected */
    public abstract void canceled(DownloadTask downloadTask);

    /* access modifiers changed from: protected */
    public abstract void completed(DownloadTask downloadTask);

    /* access modifiers changed from: protected */
    public abstract void error(DownloadTask downloadTask, Exception exc);

    /* access modifiers changed from: protected */
    public abstract void started(DownloadTask downloadTask);

    /* access modifiers changed from: protected */
    public abstract void warn(DownloadTask downloadTask);

    public final void taskStart(DownloadTask task, Listener1Assist.Listener1Model model) {
        started(task);
    }

    /* renamed from: com.liulishuo.okdownload.core.listener.DownloadListener3$1 */
    static /* synthetic */ class C10691 {
        static final /* synthetic */ int[] $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause;

        static {
            int[] iArr = new int[EndCause.values().length];
            $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause = iArr;
            try {
                iArr[EndCause.COMPLETED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[EndCause.CANCELED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[EndCause.ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[EndCause.PRE_ALLOCATE_FAILED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[EndCause.FILE_BUSY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[EndCause.SAME_TASK_BUSY.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public void taskEnd(DownloadTask task, EndCause cause, Exception realCause, Listener1Assist.Listener1Model model) {
        switch (C10691.$SwitchMap$com$liulishuo$okdownload$core$cause$EndCause[cause.ordinal()]) {
            case 1:
                completed(task);
                return;
            case 2:
                canceled(task);
                return;
            case 3:
            case 4:
                error(task, realCause);
                return;
            case 5:
            case 6:
                warn(task);
                return;
            default:
                Util.m86w("DownloadListener3", "Don't support " + cause);
                return;
        }
    }
}
