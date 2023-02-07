package com.liulishuo.okdownload.core.download;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.dispatcher.CallbackDispatcher;
import com.liulishuo.okdownload.core.exception.InterruptException;
import com.liulishuo.okdownload.core.file.MultiPointOutputStream;
import com.liulishuo.okdownload.core.interceptor.BreakpointInterceptor;
import com.liulishuo.okdownload.core.interceptor.FetchDataInterceptor;
import com.liulishuo.okdownload.core.interceptor.Interceptor;
import com.liulishuo.okdownload.core.interceptor.RetryInterceptor;
import com.liulishuo.okdownload.core.interceptor.connect.CallServerInterceptor;
import com.liulishuo.okdownload.core.interceptor.connect.HeaderInterceptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadChain implements Runnable {
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkDownload Cancel Block", false));
    private static final String TAG = "DownloadChain";
    private final int blockIndex;
    private final DownloadCache cache;
    private final CallbackDispatcher callbackDispatcher;
    int connectIndex = 0;
    final List<Interceptor.Connect> connectInterceptorList = new ArrayList();
    private volatile DownloadConnection connection;
    volatile Thread currentThread;
    int fetchIndex = 0;
    final List<Interceptor.Fetch> fetchInterceptorList = new ArrayList();
    final AtomicBoolean finished = new AtomicBoolean(false);
    private final BreakpointInfo info;
    long noCallbackIncreaseBytes;
    private final Runnable releaseConnectionRunnable = new Runnable() {
        public void run() {
            DownloadChain.this.releaseConnection();
        }
    };
    private long responseContentLength;
    private final DownloadStore store;
    private final DownloadTask task;

    static DownloadChain createChain(int blockIndex2, DownloadTask task2, BreakpointInfo info2, DownloadCache cache2, DownloadStore store2) {
        return new DownloadChain(blockIndex2, task2, info2, cache2, store2);
    }

    private DownloadChain(int blockIndex2, DownloadTask task2, BreakpointInfo info2, DownloadCache cache2, DownloadStore store2) {
        this.blockIndex = blockIndex2;
        this.task = task2;
        this.cache = cache2;
        this.info = info2;
        this.store = store2;
        this.callbackDispatcher = OkDownload.with().callbackDispatcher();
    }

    public long getResponseContentLength() {
        return this.responseContentLength;
    }

    public void setResponseContentLength(long responseContentLength2) {
        this.responseContentLength = responseContentLength2;
    }

    public void cancel() {
        if (!this.finished.get() && this.currentThread != null) {
            this.currentThread.interrupt();
        }
    }

    public DownloadTask getTask() {
        return this.task;
    }

    public BreakpointInfo getInfo() {
        return this.info;
    }

    public int getBlockIndex() {
        return this.blockIndex;
    }

    public synchronized void setConnection(DownloadConnection connection2) {
        this.connection = connection2;
    }

    public DownloadCache getCache() {
        return this.cache;
    }

    public void setRedirectLocation(String location) {
        this.cache.setRedirectLocation(location);
    }

    public MultiPointOutputStream getOutputStream() {
        return this.cache.getOutputStream();
    }

    public synchronized DownloadConnection getConnection() {
        return this.connection;
    }

    public synchronized DownloadConnection getConnectionOrCreate() throws IOException {
        String url;
        if (!this.cache.isInterrupt()) {
            if (this.connection == null) {
                String redirectLocation = this.cache.getRedirectLocation();
                if (redirectLocation != null) {
                    url = redirectLocation;
                } else {
                    url = this.info.getUrl();
                }
                Util.m83d(TAG, "create connection on url: " + url);
                this.connection = OkDownload.with().connectionFactory().create(url);
            }
        } else {
            throw InterruptException.SIGNAL;
        }
        return this.connection;
    }

    public void increaseCallbackBytes(long increaseBytes) {
        this.noCallbackIncreaseBytes += increaseBytes;
    }

    public void flushNoCallbackIncreaseBytes() {
        if (this.noCallbackIncreaseBytes != 0) {
            this.callbackDispatcher.dispatch().fetchProgress(this.task, this.blockIndex, this.noCallbackIncreaseBytes);
            this.noCallbackIncreaseBytes = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void start() throws IOException {
        CallbackDispatcher dispatcher = OkDownload.with().callbackDispatcher();
        RetryInterceptor retryInterceptor = new RetryInterceptor();
        BreakpointInterceptor breakpointInterceptor = new BreakpointInterceptor();
        this.connectInterceptorList.add(retryInterceptor);
        this.connectInterceptorList.add(breakpointInterceptor);
        this.connectInterceptorList.add(new HeaderInterceptor());
        this.connectInterceptorList.add(new CallServerInterceptor());
        this.connectIndex = 0;
        DownloadConnection.Connected connected = processConnect();
        if (!this.cache.isInterrupt()) {
            dispatcher.dispatch().fetchStart(this.task, this.blockIndex, getResponseContentLength());
            FetchDataInterceptor fetchDataInterceptor = new FetchDataInterceptor(this.blockIndex, connected.getInputStream(), getOutputStream(), this.task);
            this.fetchInterceptorList.add(retryInterceptor);
            this.fetchInterceptorList.add(breakpointInterceptor);
            this.fetchInterceptorList.add(fetchDataInterceptor);
            this.fetchIndex = 0;
            dispatcher.dispatch().fetchEnd(this.task, this.blockIndex, processFetch());
            return;
        }
        throw InterruptException.SIGNAL;
    }

    public void resetConnectForRetry() {
        this.connectIndex = 1;
        releaseConnection();
    }

    public synchronized void releaseConnection() {
        if (this.connection != null) {
            this.connection.release();
            Util.m83d(TAG, "release connection " + this.connection + " task[" + this.task.getId() + "] block[" + this.blockIndex + "]");
        }
        this.connection = null;
    }

    public DownloadConnection.Connected processConnect() throws IOException {
        if (!this.cache.isInterrupt()) {
            List<Interceptor.Connect> list = this.connectInterceptorList;
            int i = this.connectIndex;
            this.connectIndex = i + 1;
            return list.get(i).interceptConnect(this);
        }
        throw InterruptException.SIGNAL;
    }

    public long processFetch() throws IOException {
        if (!this.cache.isInterrupt()) {
            List<Interceptor.Fetch> list = this.fetchInterceptorList;
            int i = this.fetchIndex;
            this.fetchIndex = i + 1;
            return list.get(i).interceptFetch(this);
        }
        throw InterruptException.SIGNAL;
    }

    public long loopFetch() throws IOException {
        if (this.fetchIndex == this.fetchInterceptorList.size()) {
            this.fetchIndex--;
        }
        return processFetch();
    }

    /* access modifiers changed from: package-private */
    public boolean isFinished() {
        return this.finished.get();
    }

    public DownloadStore getDownloadStore() {
        return this.store;
    }

    public void run() {
        if (!isFinished()) {
            this.currentThread = Thread.currentThread();
            try {
                start();
            } catch (IOException e) {
            } catch (Throwable th) {
                this.finished.set(true);
                releaseConnectionAsync();
                throw th;
            }
            this.finished.set(true);
            releaseConnectionAsync();
            return;
        }
        throw new IllegalAccessError("The chain has been finished!");
    }

    /* access modifiers changed from: package-private */
    public void releaseConnectionAsync() {
        EXECUTOR.execute(this.releaseConnectionRunnable);
    }
}
