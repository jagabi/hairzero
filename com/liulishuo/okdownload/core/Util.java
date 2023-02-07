package com.liulishuo.okdownload.core;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;
import com.android.volley.misc.Utils;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnCache;
import com.liulishuo.okdownload.core.breakpoint.DownloadStore;
import com.liulishuo.okdownload.core.connection.DownloadConnection;
import com.liulishuo.okdownload.core.connection.DownloadUrlConnection;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final int CHUNKED_CONTENT_LENGTH = -1;
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String ETAG = "Etag";
    public static final String IF_MATCH = "If-Match";
    public static final String METHOD_HEAD = "HEAD";
    public static final String RANGE = "Range";
    public static final int RANGE_NOT_SATISFIABLE = 416;
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String USER_AGENT = "User-Agent";
    public static final String VALUE_CHUNKED = "chunked";
    private static Logger logger = new EmptyLogger();

    public interface Logger {
        /* renamed from: d */
        void mo16491d(String str, String str2);

        /* renamed from: e */
        void mo16492e(String str, String str2, Exception exc);

        /* renamed from: i */
        void mo16493i(String str, String str2);

        /* renamed from: w */
        void mo16494w(String str, String str2);
    }

    public static class EmptyLogger implements Logger {
        /* renamed from: e */
        public void mo16492e(String tag, String msg, Exception e) {
        }

        /* renamed from: w */
        public void mo16494w(String tag, String msg) {
        }

        /* renamed from: d */
        public void mo16491d(String tag, String msg) {
        }

        /* renamed from: i */
        public void mo16493i(String tag, String msg) {
        }
    }

    public static void enableConsoleLog() {
        logger = null;
    }

    public static void setLogger(Logger l) {
        logger = l;
    }

    public static Logger getLogger() {
        return logger;
    }

    /* renamed from: e */
    public static void m84e(String tag, String msg, Exception e) {
        Logger logger2 = logger;
        if (logger2 != null) {
            logger2.mo16492e(tag, msg, e);
        } else {
            Log.e(tag, msg, e);
        }
    }

    /* renamed from: w */
    public static void m86w(String tag, String msg) {
        Logger logger2 = logger;
        if (logger2 != null) {
            logger2.mo16494w(tag, msg);
        } else {
            Log.w(tag, msg);
        }
    }

    /* renamed from: d */
    public static void m83d(String tag, String msg) {
        Logger logger2 = logger;
        if (logger2 != null) {
            logger2.mo16491d(tag, msg);
        } else {
            Log.d(tag, msg);
        }
    }

    /* renamed from: i */
    public static void m85i(String tag, String msg) {
        Logger logger2 = logger;
        if (logger2 != null) {
            logger2.mo16493i(tag, msg);
        } else {
            Log.i(tag, msg);
        }
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(daemon);
                return result;
            }
        };
    }

    public static String md5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
        }
        if (hash == null) {
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 255) < 16) {
                hex.append('0');
            }
            hex.append(Integer.toHexString(b & 255));
        }
        return hex.toString();
    }

    public static boolean isCorrectFull(long fetchedLength, long contentLength) {
        return fetchedLength == contentLength;
    }

    public static void resetBlockIfDirty(BlockInfo info) {
        boolean isDirty = false;
        if (info.getCurrentOffset() < 0) {
            isDirty = true;
        } else if (info.getCurrentOffset() > info.getContentLength()) {
            isDirty = true;
        }
        if (isDirty) {
            m86w("resetBlockIfDirty", "block is dirty so have to reset: " + info);
            info.resetBlock();
        }
    }

    public static long getFreeSpaceBytes(StatFs statFs) {
        if (Build.VERSION.SDK_INT >= 18) {
            return statFs.getAvailableBytes();
        }
        return ((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize());
    }

    public static String humanReadableBytes(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < ((long) unit)) {
            return bytes + " B";
        }
        int exp = (int) (Math.log((double) bytes) / Math.log((double) unit));
        return String.format(Locale.ENGLISH, "%.1f %sB", new Object[]{Double.valueOf(((double) bytes) / Math.pow((double) unit, (double) exp)), (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i")});
    }

    public static DownloadStore createDefaultDatabase(Context context) {
        try {
            return (DownloadStore) Class.forName("com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite").getDeclaredConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            return new BreakpointStoreOnCache();
        }
    }

    public static DownloadStore createRemitDatabase(DownloadStore originStore) {
        DownloadStore finalStore = originStore;
        try {
            finalStore = (DownloadStore) originStore.getClass().getMethod("createRemitSelf", new Class[0]).invoke(originStore, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        }
        m83d("Util", "Get final download store is " + finalStore);
        return finalStore;
    }

    public static DownloadConnection.Factory createDefaultConnectionFactory() {
        try {
            return (DownloadConnection.Factory) Class.forName("com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection$Factory").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            return new DownloadUrlConnection.Factory();
        }
    }

    public static void assembleBlock(DownloadTask task, BreakpointInfo info, long instanceLength, boolean isAcceptRange) {
        int blockCount;
        if (OkDownload.with().downloadStrategy().isUseMultiBlock(isAcceptRange)) {
            blockCount = OkDownload.with().downloadStrategy().determineBlockCount(task, instanceLength);
        } else {
            blockCount = 1;
        }
        info.resetBlockInfos();
        long eachLength = instanceLength / ((long) blockCount);
        long startOffset = 0;
        long contentLength = 0;
        for (int i = 0; i < blockCount; i++) {
            startOffset += contentLength;
            if (i == 0) {
                contentLength = (instanceLength % ((long) blockCount)) + eachLength;
            } else {
                contentLength = eachLength;
            }
            info.addBlock(new BlockInfo(startOffset, contentLength));
        }
    }

    public static long parseContentLength(String contentLength) {
        if (contentLength == null) {
            return -1;
        }
        try {
            return Long.parseLong(contentLength);
        } catch (NumberFormatException e) {
            m83d("Util", "parseContentLength failed parse for '" + contentLength + "'");
            return -1;
        }
    }

    public static boolean isNetworkNotOnWifiType(ConnectivityManager manager) {
        if (manager == null) {
            m86w("Util", "failed to get connectivity manager!");
            return true;
        }
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || info.getType() != 1) {
            return true;
        }
        return false;
    }

    public static boolean checkPermission(String permission) {
        return OkDownload.with().context().checkCallingOrSelfPermission(permission) == 0;
    }

    public static long parseContentLengthFromContentRange(String contentRange) {
        if (contentRange == null || contentRange.length() == 0) {
            return -1;
        }
        try {
            Matcher m = Pattern.compile("bytes (\\d+)-(\\d+)/\\d+").matcher(contentRange);
            if (m.find()) {
                return (Long.parseLong(m.group(2)) - Long.parseLong(m.group(1))) + 1;
            }
        } catch (Exception e) {
            m86w("Util", "parse content-length from content-range failed " + e);
        }
        return -1;
    }

    public static boolean isUriContentScheme(Uri uri) {
        return uri.getScheme().equals(Utils.SCHEME_CONTENT);
    }

    public static boolean isUriFileScheme(Uri uri) {
        return uri.getScheme().equals(Utils.SCHEME_FILE);
    }

    public static String getFilenameFromContentUri(Uri contentUri) {
        Cursor cursor = OkDownload.with().context().getContentResolver().query(contentUri, (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return null;
        }
        try {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex("_display_name"));
        } finally {
            cursor.close();
        }
    }

    public static File getParentFile(File file) {
        File candidate = file.getParentFile();
        return candidate == null ? new File("/") : candidate;
    }

    public static long getSizeFromContentUri(Uri contentUri) {
        Cursor cursor = OkDownload.with().context().getContentResolver().query(contentUri, (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            return 0;
        }
        try {
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex("_size"));
        } finally {
            cursor.close();
        }
    }

    public static boolean isNetworkAvailable(ConnectivityManager manager) {
        if (manager == null) {
            m86w("Util", "failed to get connectivity manager!");
            return true;
        }
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        return true;
    }

    public static void inspectUserHeader(Map<String, List<String>> headerField) throws IOException {
        if (headerField.containsKey(IF_MATCH) || headerField.containsKey(RANGE)) {
            throw new IOException("If-Match and Range only can be handle by internal!");
        }
    }

    public static void addUserRequestHeaderField(Map<String, List<String>> userHeaderField, DownloadConnection connection) throws IOException {
        inspectUserHeader(userHeaderField);
        addRequestHeaderFields(userHeaderField, connection);
    }

    public static void addRequestHeaderFields(Map<String, List<String>> headerFields, DownloadConnection connection) {
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                connection.addHeader(key, value);
            }
        }
    }

    public static void addDefaultUserAgent(DownloadConnection connection) {
        connection.addHeader("User-Agent", "OkDownload/1.0.5");
    }
}
