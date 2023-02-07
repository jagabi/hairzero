package com.liulishuo.okdownload.core;

import java.io.File;

public abstract class IdentifiedTask {
    public static final File EMPTY_FILE = new File("");
    public static final String EMPTY_URL = "";

    public abstract String getFilename();

    public abstract int getId();

    public abstract File getParentFile();

    /* access modifiers changed from: protected */
    public abstract File getProvidedPathFile();

    public abstract String getUrl();

    public boolean compareIgnoreId(IdentifiedTask another) {
        if (!getUrl().equals(another.getUrl()) || getUrl().equals("") || getParentFile().equals(EMPTY_FILE)) {
            return false;
        }
        if (getProvidedPathFile().equals(another.getProvidedPathFile())) {
            return true;
        }
        if (!getParentFile().equals(another.getParentFile())) {
            return false;
        }
        String filename = getFilename();
        String anotherFilename = another.getFilename();
        if (anotherFilename == null || filename == null || !anotherFilename.equals(filename)) {
            return false;
        }
        return true;
    }
}
