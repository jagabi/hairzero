package com.android.volley.misc;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class DiskLruCache implements Closeable {
    static final long ANY_SEQUENCE_NUMBER = -1;
    private static final String CLEAN = "CLEAN";
    private static final String DIRTY = "DIRTY";
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_BACKUP = "journal.bkp";
    static final String JOURNAL_FILE_TEMP = "journal.tmp";
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");
    static final String MAGIC = "libcore.io.DiskLruCache";
    /* access modifiers changed from: private */
    public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        public void write(int b) throws IOException {
        }
    };
    private static final String READ = "READ";
    private static final String REMOVE = "REMOVE";
    static final String VERSION_1 = "1";
    private final int appVersion;
    private final Callable<Void> cleanupCallable = new Callable<Void>() {
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0027, code lost:
            return null;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.Void call() throws java.lang.Exception {
            /*
                r4 = this;
                com.android.volley.misc.DiskLruCache r0 = com.android.volley.misc.DiskLruCache.this
                monitor-enter(r0)
                com.android.volley.misc.DiskLruCache r1 = com.android.volley.misc.DiskLruCache.this     // Catch:{ all -> 0x0028 }
                java.io.Writer r1 = r1.journalWriter     // Catch:{ all -> 0x0028 }
                r2 = 0
                if (r1 != 0) goto L_0x000e
                monitor-exit(r0)     // Catch:{ all -> 0x0028 }
                return r2
            L_0x000e:
                com.android.volley.misc.DiskLruCache r1 = com.android.volley.misc.DiskLruCache.this     // Catch:{ all -> 0x0028 }
                r1.trimToSize()     // Catch:{ all -> 0x0028 }
                com.android.volley.misc.DiskLruCache r1 = com.android.volley.misc.DiskLruCache.this     // Catch:{ all -> 0x0028 }
                boolean r1 = r1.journalRebuildRequired()     // Catch:{ all -> 0x0028 }
                if (r1 == 0) goto L_0x0026
                com.android.volley.misc.DiskLruCache r1 = com.android.volley.misc.DiskLruCache.this     // Catch:{ all -> 0x0028 }
                r1.rebuildJournal()     // Catch:{ all -> 0x0028 }
                com.android.volley.misc.DiskLruCache r1 = com.android.volley.misc.DiskLruCache.this     // Catch:{ all -> 0x0028 }
                r3 = 0
                int unused = r1.redundantOpCount = r3     // Catch:{ all -> 0x0028 }
            L_0x0026:
                monitor-exit(r0)     // Catch:{ all -> 0x0028 }
                return r2
            L_0x0028:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0028 }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.volley.misc.DiskLruCache.C07201.call():java.lang.Void");
        }
    };
    /* access modifiers changed from: private */
    public final File directory;
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    private final File journalFile;
    private final File journalFileBackup;
    private final File journalFileTmp;
    /* access modifiers changed from: private */
    public Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap<>(0, 0.75f, true);
    private long maxSize;
    private long nextSequenceNumber = 0;
    /* access modifiers changed from: private */
    public int redundantOpCount;
    private long size = 0;
    /* access modifiers changed from: private */
    public final int valueCount;

    private DiskLruCache(File directory2, int appVersion2, int valueCount2, long maxSize2) {
        File file = directory2;
        this.directory = file;
        this.appVersion = appVersion2;
        this.journalFile = new File(file, JOURNAL_FILE);
        this.journalFileTmp = new File(file, JOURNAL_FILE_TEMP);
        this.journalFileBackup = new File(file, JOURNAL_FILE_BACKUP);
        this.valueCount = valueCount2;
        this.maxSize = maxSize2;
    }

    public static DiskLruCache open(File directory2, int appVersion2, int valueCount2, long maxSize2) throws IOException {
        if (maxSize2 <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (valueCount2 > 0) {
            File backupFile = new File(directory2, JOURNAL_FILE_BACKUP);
            if (backupFile.exists()) {
                File journalFile2 = new File(directory2, JOURNAL_FILE);
                if (journalFile2.exists()) {
                    backupFile.delete();
                } else {
                    renameTo(backupFile, journalFile2, false);
                }
            }
            DiskLruCache diskLruCache = new DiskLruCache(directory2, appVersion2, valueCount2, maxSize2);
            if (diskLruCache.journalFile.exists()) {
                try {
                    diskLruCache.readJournal();
                    diskLruCache.processJournal();
                    diskLruCache.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(diskLruCache.journalFile, true), Utils.US_ASCII));
                    return diskLruCache;
                } catch (IOException journalIsCorrupt) {
                    System.out.println("DiskLruCache " + directory2 + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing");
                    diskLruCache.delete();
                }
            }
            directory2.mkdirs();
            DiskLruCache cache = new DiskLruCache(directory2, appVersion2, valueCount2, maxSize2);
            cache.rebuildJournal();
            return cache;
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private void readJournal() throws IOException {
        int lineCount;
        StrictLineReader reader = new StrictLineReader(new FileInputStream(this.journalFile), Utils.US_ASCII);
        try {
            String magic = reader.readLine();
            String version = reader.readLine();
            String appVersionString = reader.readLine();
            String valueCountString = reader.readLine();
            String blank = reader.readLine();
            if (!MAGIC.equals(magic) || !VERSION_1.equals(version) || !Integer.toString(this.appVersion).equals(appVersionString) || !Integer.toString(this.valueCount).equals(valueCountString) || !"".equals(blank)) {
                throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
            }
            lineCount = 0;
            while (true) {
                readJournalLine(reader.readLine());
                lineCount++;
            }
        } catch (EOFException e) {
            this.redundantOpCount = lineCount - this.lruEntries.size();
            Utils.closeQuietly(reader);
        } catch (Throwable th) {
            Utils.closeQuietly(reader);
            throw th;
        }
    }

    private void readJournalLine(String line) throws IOException {
        String key;
        int firstSpace = line.indexOf(32);
        if (firstSpace != -1) {
            int keyBegin = firstSpace + 1;
            int secondSpace = line.indexOf(32, keyBegin);
            if (secondSpace == -1) {
                key = line.substring(keyBegin);
                if (firstSpace == REMOVE.length() && line.startsWith(REMOVE)) {
                    this.lruEntries.remove(key);
                    return;
                }
            } else {
                key = line.substring(keyBegin, secondSpace);
            }
            Entry entry = this.lruEntries.get(key);
            if (entry == null) {
                entry = new Entry(key);
                this.lruEntries.put(key, entry);
            }
            if (secondSpace != -1 && firstSpace == CLEAN.length() && line.startsWith(CLEAN)) {
                String[] parts = line.substring(secondSpace + 1).split(" ");
                boolean unused = entry.readable = true;
                Editor unused2 = entry.currentEditor = null;
                entry.setLengths(parts);
            } else if (secondSpace == -1 && firstSpace == DIRTY.length() && line.startsWith(DIRTY)) {
                Editor unused3 = entry.currentEditor = new Editor(entry);
            } else if (secondSpace != -1 || firstSpace != READ.length() || !line.startsWith(READ)) {
                throw new IOException("unexpected journal line: " + line);
            }
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    private void processJournal() throws IOException {
        deleteIfExists(this.journalFileTmp);
        Iterator<Entry> i = this.lruEntries.values().iterator();
        while (i.hasNext()) {
            Entry entry = i.next();
            if (entry.currentEditor == null) {
                for (int t = 0; t < this.valueCount; t++) {
                    this.size += entry.lengths[t];
                }
            } else {
                Editor unused = entry.currentEditor = null;
                for (int t2 = 0; t2 < this.valueCount; t2++) {
                    deleteIfExists(entry.getCleanFile(t2));
                    deleteIfExists(entry.getDirtyFile(t2));
                }
                i.remove();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public synchronized void rebuildJournal() throws IOException {
        Writer writer = this.journalWriter;
        if (writer != null) {
            writer.close();
        }
        Writer writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFileTmp), Utils.US_ASCII));
        try {
            writer2.write(MAGIC);
            writer2.write("\n");
            writer2.write(VERSION_1);
            writer2.write("\n");
            writer2.write(Integer.toString(this.appVersion));
            writer2.write("\n");
            writer2.write(Integer.toString(this.valueCount));
            writer2.write("\n");
            writer2.write("\n");
            for (Entry entry : this.lruEntries.values()) {
                if (entry.currentEditor != null) {
                    writer2.write("DIRTY " + entry.key + 10);
                } else {
                    writer2.write("CLEAN " + entry.key + entry.getLengths() + 10);
                }
            }
            writer2.close();
            if (this.journalFile.exists()) {
                renameTo(this.journalFile, this.journalFileBackup, true);
            }
            renameTo(this.journalFileTmp, this.journalFile, false);
            this.journalFileBackup.delete();
            this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), Utils.US_ASCII));
        } catch (Throwable th) {
            writer2.close();
            throw th;
        }
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    private static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
        if (deleteDestination) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException();
        }
    }

    public synchronized Snapshot get(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        Entry entry = this.lruEntries.get(key);
        if (entry == null) {
            return null;
        }
        if (!entry.readable) {
            return null;
        }
        InputStream[] ins = new InputStream[this.valueCount];
        int i = 0;
        while (i < this.valueCount) {
            try {
                ins[i] = new FileInputStream(entry.getCleanFile(i));
                i++;
            } catch (FileNotFoundException e) {
                int i2 = 0;
                while (i2 < this.valueCount && ins[i2] != null) {
                    Utils.closeQuietly(ins[i2]);
                    i2++;
                }
                return null;
            }
        }
        this.redundantOpCount++;
        this.journalWriter.append("READ " + key + 10);
        if (journalRebuildRequired()) {
            this.executorService.submit(this.cleanupCallable);
        }
        return new Snapshot(key, entry.sequenceNumber, ins, entry.lengths);
    }

    public Editor edit(String key) throws IOException {
        return edit(key, -1);
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0021, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.android.volley.misc.DiskLruCache.Editor edit(java.lang.String r6, long r7) throws java.io.IOException {
        /*
            r5 = this;
            monitor-enter(r5)
            r5.checkNotClosed()     // Catch:{ all -> 0x0065 }
            r5.validateKey(r6)     // Catch:{ all -> 0x0065 }
            java.util.LinkedHashMap<java.lang.String, com.android.volley.misc.DiskLruCache$Entry> r0 = r5.lruEntries     // Catch:{ all -> 0x0065 }
            java.lang.Object r0 = r0.get(r6)     // Catch:{ all -> 0x0065 }
            com.android.volley.misc.DiskLruCache$Entry r0 = (com.android.volley.misc.DiskLruCache.Entry) r0     // Catch:{ all -> 0x0065 }
            r1 = -1
            int r1 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
            r2 = 0
            if (r1 == 0) goto L_0x0022
            if (r0 == 0) goto L_0x0020
            long r3 = r0.sequenceNumber     // Catch:{ all -> 0x0065 }
            int r1 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
            if (r1 == 0) goto L_0x0022
        L_0x0020:
            monitor-exit(r5)
            return r2
        L_0x0022:
            if (r0 != 0) goto L_0x0030
            com.android.volley.misc.DiskLruCache$Entry r1 = new com.android.volley.misc.DiskLruCache$Entry     // Catch:{ all -> 0x0065 }
            r1.<init>(r6)     // Catch:{ all -> 0x0065 }
            r0 = r1
            java.util.LinkedHashMap<java.lang.String, com.android.volley.misc.DiskLruCache$Entry> r1 = r5.lruEntries     // Catch:{ all -> 0x0065 }
            r1.put(r6, r0)     // Catch:{ all -> 0x0065 }
            goto L_0x0038
        L_0x0030:
            com.android.volley.misc.DiskLruCache$Editor r1 = r0.currentEditor     // Catch:{ all -> 0x0065 }
            if (r1 == 0) goto L_0x0038
            monitor-exit(r5)
            return r2
        L_0x0038:
            com.android.volley.misc.DiskLruCache$Editor r1 = new com.android.volley.misc.DiskLruCache$Editor     // Catch:{ all -> 0x0065 }
            r1.<init>(r0)     // Catch:{ all -> 0x0065 }
            com.android.volley.misc.DiskLruCache.Editor unused = r0.currentEditor = r1     // Catch:{ all -> 0x0065 }
            java.io.Writer r2 = r5.journalWriter     // Catch:{ all -> 0x0065 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0065 }
            r3.<init>()     // Catch:{ all -> 0x0065 }
            java.lang.String r4 = "DIRTY "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0065 }
            java.lang.StringBuilder r3 = r3.append(r6)     // Catch:{ all -> 0x0065 }
            r4 = 10
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0065 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0065 }
            r2.write(r3)     // Catch:{ all -> 0x0065 }
            java.io.Writer r2 = r5.journalWriter     // Catch:{ all -> 0x0065 }
            r2.flush()     // Catch:{ all -> 0x0065 }
            monitor-exit(r5)
            return r1
        L_0x0065:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.misc.DiskLruCache.edit(java.lang.String, long):com.android.volley.misc.DiskLruCache$Editor");
    }

    public File getDirectory() {
        return this.directory;
    }

    public synchronized long getMaxSize() {
        return this.maxSize;
    }

    public synchronized void setMaxSize(long maxSize2) {
        this.maxSize = maxSize2;
        this.executorService.submit(this.cleanupCallable);
    }

    public synchronized long size() {
        return this.size;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0112, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void completeEdit(com.android.volley.misc.DiskLruCache.Editor r11, boolean r12) throws java.io.IOException {
        /*
            r10 = this;
            monitor-enter(r10)
            com.android.volley.misc.DiskLruCache$Entry r0 = r11.entry     // Catch:{ all -> 0x0119 }
            com.android.volley.misc.DiskLruCache$Editor r1 = r0.currentEditor     // Catch:{ all -> 0x0119 }
            if (r1 != r11) goto L_0x0113
            if (r12 == 0) goto L_0x004e
            boolean r1 = r0.readable     // Catch:{ all -> 0x0119 }
            if (r1 != 0) goto L_0x004e
            r1 = 0
        L_0x0014:
            int r2 = r10.valueCount     // Catch:{ all -> 0x0119 }
            if (r1 >= r2) goto L_0x004e
            boolean[] r2 = r11.written     // Catch:{ all -> 0x0119 }
            boolean r2 = r2[r1]     // Catch:{ all -> 0x0119 }
            if (r2 == 0) goto L_0x0032
            java.io.File r2 = r0.getDirtyFile(r1)     // Catch:{ all -> 0x0119 }
            boolean r2 = r2.exists()     // Catch:{ all -> 0x0119 }
            if (r2 != 0) goto L_0x002f
            r11.abort()     // Catch:{ all -> 0x0119 }
            monitor-exit(r10)
            return
        L_0x002f:
            int r1 = r1 + 1
            goto L_0x0014
        L_0x0032:
            r11.abort()     // Catch:{ all -> 0x0119 }
            java.lang.IllegalStateException r2 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0119 }
            r3.<init>()     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = "Newly created entry didn't create value for index "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r3 = r3.append(r1)     // Catch:{ all -> 0x0119 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0119 }
            r2.<init>(r3)     // Catch:{ all -> 0x0119 }
            throw r2     // Catch:{ all -> 0x0119 }
        L_0x004e:
            r1 = 0
        L_0x004f:
            int r2 = r10.valueCount     // Catch:{ all -> 0x0119 }
            if (r1 >= r2) goto L_0x0083
            java.io.File r2 = r0.getDirtyFile(r1)     // Catch:{ all -> 0x0119 }
            if (r12 == 0) goto L_0x007d
            boolean r3 = r2.exists()     // Catch:{ all -> 0x0119 }
            if (r3 == 0) goto L_0x0080
            java.io.File r3 = r0.getCleanFile(r1)     // Catch:{ all -> 0x0119 }
            r2.renameTo(r3)     // Catch:{ all -> 0x0119 }
            long[] r4 = r0.lengths     // Catch:{ all -> 0x0119 }
            r4 = r4[r1]     // Catch:{ all -> 0x0119 }
            long r6 = r3.length()     // Catch:{ all -> 0x0119 }
            long[] r8 = r0.lengths     // Catch:{ all -> 0x0119 }
            r8[r1] = r6     // Catch:{ all -> 0x0119 }
            long r8 = r10.size     // Catch:{ all -> 0x0119 }
            long r8 = r8 - r4
            long r8 = r8 + r6
            r10.size = r8     // Catch:{ all -> 0x0119 }
            goto L_0x0080
        L_0x007d:
            deleteIfExists(r2)     // Catch:{ all -> 0x0119 }
        L_0x0080:
            int r1 = r1 + 1
            goto L_0x004f
        L_0x0083:
            int r1 = r10.redundantOpCount     // Catch:{ all -> 0x0119 }
            r2 = 1
            int r1 = r1 + r2
            r10.redundantOpCount = r1     // Catch:{ all -> 0x0119 }
            r1 = 0
            com.android.volley.misc.DiskLruCache.Editor unused = r0.currentEditor = r1     // Catch:{ all -> 0x0119 }
            boolean r1 = r0.readable     // Catch:{ all -> 0x0119 }
            r1 = r1 | r12
            r3 = 10
            if (r1 == 0) goto L_0x00ce
            boolean unused = r0.readable = r2     // Catch:{ all -> 0x0119 }
            java.io.Writer r1 = r10.journalWriter     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0119 }
            r2.<init>()     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = "CLEAN "
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = r0.key     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = r0.getLengths()     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0119 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0119 }
            r1.write(r2)     // Catch:{ all -> 0x0119 }
            if (r12 == 0) goto L_0x00f7
            long r1 = r10.nextSequenceNumber     // Catch:{ all -> 0x0119 }
            r3 = 1
            long r3 = r3 + r1
            r10.nextSequenceNumber = r3     // Catch:{ all -> 0x0119 }
            long unused = r0.sequenceNumber = r1     // Catch:{ all -> 0x0119 }
            goto L_0x00f7
        L_0x00ce:
            java.util.LinkedHashMap<java.lang.String, com.android.volley.misc.DiskLruCache$Entry> r1 = r10.lruEntries     // Catch:{ all -> 0x0119 }
            java.lang.String r2 = r0.key     // Catch:{ all -> 0x0119 }
            r1.remove(r2)     // Catch:{ all -> 0x0119 }
            java.io.Writer r1 = r10.journalWriter     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0119 }
            r2.<init>()     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = "REMOVE "
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.String r4 = r0.key     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = r2.append(r4)     // Catch:{ all -> 0x0119 }
            java.lang.StringBuilder r2 = r2.append(r3)     // Catch:{ all -> 0x0119 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0119 }
            r1.write(r2)     // Catch:{ all -> 0x0119 }
        L_0x00f7:
            java.io.Writer r1 = r10.journalWriter     // Catch:{ all -> 0x0119 }
            r1.flush()     // Catch:{ all -> 0x0119 }
            long r1 = r10.size     // Catch:{ all -> 0x0119 }
            long r3 = r10.maxSize     // Catch:{ all -> 0x0119 }
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 > 0) goto L_0x010a
            boolean r1 = r10.journalRebuildRequired()     // Catch:{ all -> 0x0119 }
            if (r1 == 0) goto L_0x0111
        L_0x010a:
            java.util.concurrent.ThreadPoolExecutor r1 = r10.executorService     // Catch:{ all -> 0x0119 }
            java.util.concurrent.Callable<java.lang.Void> r2 = r10.cleanupCallable     // Catch:{ all -> 0x0119 }
            r1.submit(r2)     // Catch:{ all -> 0x0119 }
        L_0x0111:
            monitor-exit(r10)
            return
        L_0x0113:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0119 }
            r1.<init>()     // Catch:{ all -> 0x0119 }
            throw r1     // Catch:{ all -> 0x0119 }
        L_0x0119:
            r11 = move-exception
            monitor-exit(r10)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.misc.DiskLruCache.completeEdit(com.android.volley.misc.DiskLruCache$Editor, boolean):void");
    }

    /* access modifiers changed from: private */
    public boolean journalRebuildRequired() {
        int i = this.redundantOpCount;
        return i >= 2000 && i >= this.lruEntries.size();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0095, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean remove(java.lang.String r8) throws java.io.IOException {
        /*
            r7 = this;
            monitor-enter(r7)
            r7.checkNotClosed()     // Catch:{ all -> 0x0099 }
            r7.validateKey(r8)     // Catch:{ all -> 0x0099 }
            java.util.LinkedHashMap<java.lang.String, com.android.volley.misc.DiskLruCache$Entry> r0 = r7.lruEntries     // Catch:{ all -> 0x0099 }
            java.lang.Object r0 = r0.get(r8)     // Catch:{ all -> 0x0099 }
            com.android.volley.misc.DiskLruCache$Entry r0 = (com.android.volley.misc.DiskLruCache.Entry) r0     // Catch:{ all -> 0x0099 }
            if (r0 == 0) goto L_0x0096
            com.android.volley.misc.DiskLruCache$Editor r1 = r0.currentEditor     // Catch:{ all -> 0x0099 }
            if (r1 == 0) goto L_0x0019
            goto L_0x0096
        L_0x0019:
            r1 = 0
        L_0x001a:
            int r2 = r7.valueCount     // Catch:{ all -> 0x0099 }
            if (r1 >= r2) goto L_0x005e
            java.io.File r2 = r0.getCleanFile(r1)     // Catch:{ all -> 0x0099 }
            boolean r3 = r2.exists()     // Catch:{ all -> 0x0099 }
            if (r3 == 0) goto L_0x0048
            boolean r3 = r2.delete()     // Catch:{ all -> 0x0099 }
            if (r3 == 0) goto L_0x002f
            goto L_0x0048
        L_0x002f:
            java.io.IOException r3 = new java.io.IOException     // Catch:{ all -> 0x0099 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0099 }
            r4.<init>()     // Catch:{ all -> 0x0099 }
            java.lang.String r5 = "failed to delete "
            java.lang.StringBuilder r4 = r4.append(r5)     // Catch:{ all -> 0x0099 }
            java.lang.StringBuilder r4 = r4.append(r2)     // Catch:{ all -> 0x0099 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0099 }
            r3.<init>(r4)     // Catch:{ all -> 0x0099 }
            throw r3     // Catch:{ all -> 0x0099 }
        L_0x0048:
            long r3 = r7.size     // Catch:{ all -> 0x0099 }
            long[] r5 = r0.lengths     // Catch:{ all -> 0x0099 }
            r5 = r5[r1]     // Catch:{ all -> 0x0099 }
            long r3 = r3 - r5
            r7.size = r3     // Catch:{ all -> 0x0099 }
            long[] r3 = r0.lengths     // Catch:{ all -> 0x0099 }
            r4 = 0
            r3[r1] = r4     // Catch:{ all -> 0x0099 }
            int r1 = r1 + 1
            goto L_0x001a
        L_0x005e:
            int r1 = r7.redundantOpCount     // Catch:{ all -> 0x0099 }
            r2 = 1
            int r1 = r1 + r2
            r7.redundantOpCount = r1     // Catch:{ all -> 0x0099 }
            java.io.Writer r1 = r7.journalWriter     // Catch:{ all -> 0x0099 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0099 }
            r3.<init>()     // Catch:{ all -> 0x0099 }
            java.lang.String r4 = "REMOVE "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0099 }
            java.lang.StringBuilder r3 = r3.append(r8)     // Catch:{ all -> 0x0099 }
            r4 = 10
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch:{ all -> 0x0099 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0099 }
            r1.append(r3)     // Catch:{ all -> 0x0099 }
            java.util.LinkedHashMap<java.lang.String, com.android.volley.misc.DiskLruCache$Entry> r1 = r7.lruEntries     // Catch:{ all -> 0x0099 }
            r1.remove(r8)     // Catch:{ all -> 0x0099 }
            boolean r1 = r7.journalRebuildRequired()     // Catch:{ all -> 0x0099 }
            if (r1 == 0) goto L_0x0094
            java.util.concurrent.ThreadPoolExecutor r1 = r7.executorService     // Catch:{ all -> 0x0099 }
            java.util.concurrent.Callable<java.lang.Void> r3 = r7.cleanupCallable     // Catch:{ all -> 0x0099 }
            r1.submit(r3)     // Catch:{ all -> 0x0099 }
        L_0x0094:
            monitor-exit(r7)
            return r2
        L_0x0096:
            r1 = 0
            monitor-exit(r7)
            return r1
        L_0x0099:
            r8 = move-exception
            monitor-exit(r7)
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.volley.misc.DiskLruCache.remove(java.lang.String):boolean");
    }

    public synchronized boolean isClosed() {
        return this.journalWriter == null;
    }

    private void checkNotClosed() {
        if (this.journalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void flush() throws IOException {
        checkNotClosed();
        trimToSize();
        this.journalWriter.flush();
    }

    public synchronized void close() throws IOException {
        if (this.journalWriter != null) {
            Iterator i$ = new ArrayList(this.lruEntries.values()).iterator();
            while (i$.hasNext()) {
                Entry entry = (Entry) i$.next();
                if (entry.currentEditor != null) {
                    entry.currentEditor.abort();
                }
            }
            trimToSize();
            this.journalWriter.close();
            this.journalWriter = null;
        }
    }

    /* access modifiers changed from: private */
    public void trimToSize() throws IOException {
        while (this.size > this.maxSize) {
            remove(this.lruEntries.entrySet().iterator().next().getKey());
        }
    }

    public void delete() throws IOException {
        close();
        Utils.deleteContents(this.directory);
    }

    private void validateKey(String key) {
        if (!LEGAL_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,64}: \"" + key + "\"");
        }
    }

    /* access modifiers changed from: private */
    public static String inputStreamToString(InputStream in) throws IOException {
        return Utils.readFully(new InputStreamReader(in, Utils.UTF_8));
    }

    public final class Snapshot implements Closeable {
        private final InputStream[] ins;
        private final String key;
        private final long[] lengths;
        private final long sequenceNumber;

        private Snapshot(String key2, long sequenceNumber2, InputStream[] ins2, long[] lengths2) {
            this.key = key2;
            this.sequenceNumber = sequenceNumber2;
            this.ins = ins2;
            this.lengths = lengths2;
        }

        public Editor edit() throws IOException {
            return DiskLruCache.this.edit(this.key, this.sequenceNumber);
        }

        public InputStream getInputStream(int index) {
            return this.ins[index];
        }

        public String getString(int index) throws IOException {
            return DiskLruCache.inputStreamToString(getInputStream(index));
        }

        public long getLength(int index) {
            return this.lengths[index];
        }

        public void close() {
            for (InputStream in : this.ins) {
                Utils.closeQuietly(in);
            }
        }
    }

    public final class Editor {
        private boolean committed;
        /* access modifiers changed from: private */
        public final Entry entry;
        /* access modifiers changed from: private */
        public boolean hasErrors;
        /* access modifiers changed from: private */
        public final boolean[] written;

        private Editor(Entry entry2) {
            this.entry = entry2;
            this.written = entry2.readable ? null : new boolean[DiskLruCache.this.valueCount];
        }

        /* Debug info: failed to restart local var, previous not found, register: 4 */
        public InputStream newInputStream(int index) throws IOException {
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor != this) {
                    throw new IllegalStateException();
                } else if (!this.entry.readable) {
                    return null;
                } else {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(this.entry.getCleanFile(index));
                        return fileInputStream;
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }
            }
        }

        public String getString(int index) throws IOException {
            InputStream in = newInputStream(index);
            if (in != null) {
                return DiskLruCache.inputStreamToString(in);
            }
            return null;
        }

        /* Debug info: failed to restart local var, previous not found, register: 6 */
        public OutputStream newOutputStream(int index) throws IOException {
            FileOutputStream outputStream;
            FaultHidingOutputStream faultHidingOutputStream;
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor == this) {
                    if (!this.entry.readable) {
                        this.written[index] = true;
                    }
                    File dirtyFile = this.entry.getDirtyFile(index);
                    try {
                        outputStream = new FileOutputStream(dirtyFile);
                    } catch (FileNotFoundException e) {
                        DiskLruCache.this.directory.mkdirs();
                        try {
                            outputStream = new FileOutputStream(dirtyFile);
                        } catch (FileNotFoundException e2) {
                            return DiskLruCache.NULL_OUTPUT_STREAM;
                        }
                    }
                    faultHidingOutputStream = new FaultHidingOutputStream(outputStream);
                } else {
                    throw new IllegalStateException();
                }
            }
            return faultHidingOutputStream;
        }

        public void set(int index, String value) throws IOException {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(newOutputStream(index), Utils.UTF_8);
                writer.write(value);
            } finally {
                Utils.closeQuietly(writer);
            }
        }

        public void commit() throws IOException {
            if (this.hasErrors) {
                DiskLruCache.this.completeEdit(this, false);
                DiskLruCache.this.remove(this.entry.key);
            } else {
                DiskLruCache.this.completeEdit(this, true);
            }
            this.committed = true;
        }

        public void abort() throws IOException {
            DiskLruCache.this.completeEdit(this, false);
        }

        public void abortUnlessCommitted() {
            if (!this.committed) {
                try {
                    abort();
                } catch (IOException e) {
                }
            }
        }

        private class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }

            public void write(int oneByte) {
                try {
                    this.out.write(oneByte);
                } catch (IOException e) {
                    boolean unused = Editor.this.hasErrors = true;
                }
            }

            public void write(byte[] buffer, int offset, int length) {
                try {
                    this.out.write(buffer, offset, length);
                } catch (IOException e) {
                    boolean unused = Editor.this.hasErrors = true;
                }
            }

            public void close() {
                try {
                    this.out.close();
                } catch (IOException e) {
                    boolean unused = Editor.this.hasErrors = true;
                }
            }

            public void flush() {
                try {
                    this.out.flush();
                } catch (IOException e) {
                    boolean unused = Editor.this.hasErrors = true;
                }
            }
        }
    }

    private final class Entry {
        /* access modifiers changed from: private */
        public Editor currentEditor;
        /* access modifiers changed from: private */
        public final String key;
        /* access modifiers changed from: private */
        public final long[] lengths;
        /* access modifiers changed from: private */
        public boolean readable;
        /* access modifiers changed from: private */
        public long sequenceNumber;

        private Entry(String key2) {
            this.key = key2;
            this.lengths = new long[DiskLruCache.this.valueCount];
        }

        public String getLengths() throws IOException {
            StringBuilder result = new StringBuilder();
            for (long size : this.lengths) {
                result.append(' ').append(size);
            }
            return result.toString();
        }

        /* access modifiers changed from: private */
        public void setLengths(String[] strings) throws IOException {
            if (strings.length == DiskLruCache.this.valueCount) {
                int i = 0;
                while (i < strings.length) {
                    try {
                        this.lengths[i] = Long.parseLong(strings[i]);
                        i++;
                    } catch (NumberFormatException e) {
                        throw invalidLengths(strings);
                    }
                }
                return;
            }
            throw invalidLengths(strings);
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        public File getCleanFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "." + i);
        }

        public File getDirtyFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "." + i + ".tmp");
        }
    }
}
