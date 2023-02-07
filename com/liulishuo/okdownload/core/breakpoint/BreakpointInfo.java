package com.liulishuo.okdownload.core.breakpoint;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.download.DownloadStrategy;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BreakpointInfo {
    private final List<BlockInfo> blockInfoList = new ArrayList();
    private boolean chunked;
    private String etag;
    private final DownloadStrategy.FilenameHolder filenameHolder;

    /* renamed from: id */
    final int f190id;
    final File parentFile;
    private File targetFile;
    private final boolean taskOnlyProvidedParentPath;
    private final String url;

    public BreakpointInfo(int id, String url2, File parentFile2, String filename) {
        this.f190id = id;
        this.url = url2;
        this.parentFile = parentFile2;
        if (Util.isEmpty(filename)) {
            this.filenameHolder = new DownloadStrategy.FilenameHolder();
            this.taskOnlyProvidedParentPath = true;
            return;
        }
        this.filenameHolder = new DownloadStrategy.FilenameHolder(filename);
        this.taskOnlyProvidedParentPath = false;
        this.targetFile = new File(parentFile2, filename);
    }

    BreakpointInfo(int id, String url2, File parentFile2, String filename, boolean taskOnlyProvidedParentPath2) {
        this.f190id = id;
        this.url = url2;
        this.parentFile = parentFile2;
        if (Util.isEmpty(filename)) {
            this.filenameHolder = new DownloadStrategy.FilenameHolder();
        } else {
            this.filenameHolder = new DownloadStrategy.FilenameHolder(filename);
        }
        this.taskOnlyProvidedParentPath = taskOnlyProvidedParentPath2;
    }

    public int getId() {
        return this.f190id;
    }

    public void setChunked(boolean chunked2) {
        this.chunked = chunked2;
    }

    public void addBlock(BlockInfo blockInfo) {
        this.blockInfoList.add(blockInfo);
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public boolean isLastBlock(int blockIndex) {
        return blockIndex == this.blockInfoList.size() - 1;
    }

    public boolean isSingleBlock() {
        return this.blockInfoList.size() == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isTaskOnlyProvidedParentPath() {
        return this.taskOnlyProvidedParentPath;
    }

    public BlockInfo getBlock(int blockIndex) {
        return this.blockInfoList.get(blockIndex);
    }

    public void resetInfo() {
        this.blockInfoList.clear();
        this.etag = null;
    }

    public void resetBlockInfos() {
        this.blockInfoList.clear();
    }

    public int getBlockCount() {
        return this.blockInfoList.size();
    }

    public void setEtag(String etag2) {
        this.etag = etag2;
    }

    public long getTotalOffset() {
        long offset = 0;
        ArrayList<BlockInfo> list = (ArrayList) ((ArrayList) this.blockInfoList).clone();
        int count = list.size();
        for (int i = 0; i < count; i++) {
            offset += list.get(i).getCurrentOffset();
        }
        return offset;
    }

    public long getTotalLength() {
        if (isChunked()) {
            return getTotalOffset();
        }
        long length = 0;
        Iterator<BlockInfo> it = ((ArrayList) ((ArrayList) this.blockInfoList).clone()).iterator();
        while (it.hasNext()) {
            length += it.next().getContentLength();
        }
        return length;
    }

    public String getEtag() {
        return this.etag;
    }

    public String getUrl() {
        return this.url;
    }

    public String getFilename() {
        return this.filenameHolder.get();
    }

    public DownloadStrategy.FilenameHolder getFilenameHolder() {
        return this.filenameHolder;
    }

    public File getFile() {
        String filename = this.filenameHolder.get();
        if (filename == null) {
            return null;
        }
        if (this.targetFile == null) {
            this.targetFile = new File(this.parentFile, filename);
        }
        return this.targetFile;
    }

    public BreakpointInfo copy() {
        BreakpointInfo info = new BreakpointInfo(this.f190id, this.url, this.parentFile, this.filenameHolder.get(), this.taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : this.blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public BreakpointInfo copyWithReplaceId(int replaceId) {
        BreakpointInfo info = new BreakpointInfo(replaceId, this.url, this.parentFile, this.filenameHolder.get(), this.taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : this.blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public void reuseBlocks(BreakpointInfo info) {
        this.blockInfoList.clear();
        this.blockInfoList.addAll(info.blockInfoList);
    }

    public BreakpointInfo copyWithReplaceIdAndUrl(int replaceId, String newUrl) {
        BreakpointInfo info = new BreakpointInfo(replaceId, newUrl, this.parentFile, this.filenameHolder.get(), this.taskOnlyProvidedParentPath);
        info.chunked = this.chunked;
        for (BlockInfo blockInfo : this.blockInfoList) {
            info.blockInfoList.add(blockInfo.copy());
        }
        return info;
    }

    public boolean isSameFrom(DownloadTask task) {
        if (!this.parentFile.equals(task.getParentFile()) || !this.url.equals(task.getUrl())) {
            return false;
        }
        String otherFilename = task.getFilename();
        if (otherFilename != null && otherFilename.equals(this.filenameHolder.get())) {
            return true;
        }
        if (!this.taskOnlyProvidedParentPath || !task.isFilenameFromResponse()) {
            return false;
        }
        if (otherFilename == null || otherFilename.equals(this.filenameHolder.get())) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "id[" + this.f190id + "] url[" + this.url + "] etag[" + this.etag + "] taskOnlyProvidedParentPath[" + this.taskOnlyProvidedParentPath + "] parent path[" + this.parentFile + "] filename[" + this.filenameHolder.get() + "] block(s):" + this.blockInfoList.toString();
    }
}
