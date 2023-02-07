package com.liulishuo.okdownload.core;

public abstract class NamedRunnable implements Runnable {
    protected final String name;

    /* access modifiers changed from: protected */
    public abstract void execute() throws InterruptedException;

    /* access modifiers changed from: protected */
    public abstract void finished();

    /* access modifiers changed from: protected */
    public abstract void interrupted(InterruptedException interruptedException);

    public NamedRunnable(String name2) {
        this.name = name2;
    }

    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(this.name);
        try {
            execute();
        } catch (InterruptedException e) {
            interrupted(e);
        } catch (Throwable th) {
            Thread.currentThread().setName(oldName);
            finished();
            throw th;
        }
        Thread.currentThread().setName(oldName);
        finished();
    }
}
