package com.liulishuo.okdownload;

import android.content.Context;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BreakpointStore;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher;
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher;
import com.liulishuo.okdownload.core.download.DownloadStrategy;
import com.liulishuo.okdownload.core.file.DownloadOutputStream;
import com.liulishuo.okdownload.core.file.DownloadUriOutputStream;
import com.liulishuo.okdownload.core.file.ProcessFileStrategy;

public class OkDownload {
    static volatile OkDownload singleton;
    private final BreakpointStore breakpointStore;
    private final CallbackDispatcher callbackDispatcher;
    private final DownloadConnection.Factory connectionFactory;
    private final Context context;
    private final DownloadDispatcher downloadDispatcher;
    private final DownloadStrategy downloadStrategy;
    DownloadMonitor monitor;
    private final DownloadOutputStream.Factory outputStreamFactory;
    private final ProcessFileStrategy processFileStrategy;

    OkDownload(Context context2, DownloadDispatcher downloadDispatcher2, CallbackDispatcher callbackDispatcher2, DownloadStore store, DownloadConnection.Factory connectionFactory2, DownloadOutputStream.Factory outputStreamFactory2, ProcessFileStrategy processFileStrategy2, DownloadStrategy downloadStrategy2) {
        this.context = context2;
        this.downloadDispatcher = downloadDispatcher2;
        this.callbackDispatcher = callbackDispatcher2;
        this.breakpointStore = store;
        this.connectionFactory = connectionFactory2;
        this.outputStreamFactory = outputStreamFactory2;
        this.processFileStrategy = processFileStrategy2;
        this.downloadStrategy = downloadStrategy2;
        downloadDispatcher2.setDownloadStore(Util.createRemitDatabase(store));
    }

    public DownloadDispatcher downloadDispatcher() {
        return this.downloadDispatcher;
    }

    public CallbackDispatcher callbackDispatcher() {
        return this.callbackDispatcher;
    }

    public BreakpointStore breakpointStore() {
        return this.breakpointStore;
    }

    public DownloadConnection.Factory connectionFactory() {
        return this.connectionFactory;
    }

    public DownloadOutputStream.Factory outputStreamFactory() {
        return this.outputStreamFactory;
    }

    public ProcessFileStrategy processFileStrategy() {
        return this.processFileStrategy;
    }

    public DownloadStrategy downloadStrategy() {
        return this.downloadStrategy;
    }

    public Context context() {
        return this.context;
    }

    public void setMonitor(DownloadMonitor monitor2) {
        this.monitor = monitor2;
    }

    public DownloadMonitor getMonitor() {
        return this.monitor;
    }

    public static OkDownload with() {
        if (singleton == null) {
            synchronized (OkDownload.class) {
                if (singleton == null) {
                    if (OkDownloadProvider.context != null) {
                        singleton = new Builder(OkDownloadProvider.context).build();
                    } else {
                        throw new IllegalStateException("context == null");
                    }
                }
            }
        }
        return singleton;
    }

    public static void setSingletonInstance(OkDownload okDownload) {
        if (singleton == null) {
            synchronized (OkDownload.class) {
                if (singleton == null) {
                    singleton = okDownload;
                } else {
                    throw new IllegalArgumentException("OkDownload must be null.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("OkDownload must be null.");
    }

    public static class Builder {
        private CallbackDispatcher callbackDispatcher;
        private DownloadConnection.Factory connectionFactory;
        private final Context context;
        private DownloadDispatcher downloadDispatcher;
        private DownloadStore downloadStore;
        private DownloadStrategy downloadStrategy;
        private DownloadMonitor monitor;
        private DownloadOutputStream.Factory outputStreamFactory;
        private ProcessFileStrategy processFileStrategy;

        public Builder(Context context2) {
            this.context = context2.getApplicationContext();
        }

        public Builder downloadDispatcher(DownloadDispatcher downloadDispatcher2) {
            this.downloadDispatcher = downloadDispatcher2;
            return this;
        }

        public Builder callbackDispatcher(CallbackDispatcher callbackDispatcher2) {
            this.callbackDispatcher = callbackDispatcher2;
            return this;
        }

        public Builder downloadStore(DownloadStore downloadStore2) {
            this.downloadStore = downloadStore2;
            return this;
        }

        public Builder connectionFactory(DownloadConnection.Factory connectionFactory2) {
            this.connectionFactory = connectionFactory2;
            return this;
        }

        public Builder outputStreamFactory(DownloadOutputStream.Factory outputStreamFactory2) {
            this.outputStreamFactory = outputStreamFactory2;
            return this;
        }

        public Builder processFileStrategy(ProcessFileStrategy processFileStrategy2) {
            this.processFileStrategy = processFileStrategy2;
            return this;
        }

        public Builder downloadStrategy(DownloadStrategy downloadStrategy2) {
            this.downloadStrategy = downloadStrategy2;
            return this;
        }

        public Builder monitor(DownloadMonitor monitor2) {
            this.monitor = monitor2;
            return this;
        }

        public OkDownload build() {
            if (this.downloadDispatcher == null) {
                this.downloadDispatcher = new DownloadDispatcher();
            }
            if (this.callbackDispatcher == null) {
                this.callbackDispatcher = new CallbackDispatcher();
            }
            if (this.downloadStore == null) {
                this.downloadStore = Util.createDefaultDatabase(this.context);
            }
            if (this.connectionFactory == null) {
                this.connectionFactory = Util.createDefaultConnectionFactory();
            }
            if (this.outputStreamFactory == null) {
                this.outputStreamFactory = new DownloadUriOutputStream.Factory();
            }
            if (this.processFileStrategy == null) {
                this.processFileStrategy = new ProcessFileStrategy();
            }
            if (this.downloadStrategy == null) {
                this.downloadStrategy = new DownloadStrategy();
            }
            OkDownload okDownload = new OkDownload(this.context, this.downloadDispatcher, this.callbackDispatcher, this.downloadStore, this.connectionFactory, this.outputStreamFactory, this.processFileStrategy, this.downloadStrategy);
            okDownload.setMonitor(this.monitor);
            Util.m83d("OkDownload", "downloadStore[" + this.downloadStore + "] connectionFactory[" + this.connectionFactory);
            return okDownload;
        }
    }
}
