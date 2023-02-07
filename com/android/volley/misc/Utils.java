package com.android.volley.misc;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import com.liulishuo.okdownload.core.Util;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

public class Utils {
    public static final int ANIMATION_FADE_IN_TIME = 200;
    public static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    public static final String SCHEME_ASSETS = "asset";
    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_VIDEO = "video";
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public interface HorizontallyScrollable {
        boolean interceptMoveLeft(float f, float f2);

        boolean interceptMoveRight(float f, float f2);
    }

    private Utils() {
    }

    public static void enableStrictMode() {
        if (hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();
            if (hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= 8;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= 9;
    }

    public static boolean hasGingerbreadMR1() {
        return Build.VERSION.SDK_INT >= 10;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= 11;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= 12;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= 16;
    }

    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static long getUsableSpace(File path) {
        if (hasGingerbread()) {
            return path.getUsableSpace();
        }
        StatFs stats = new StatFs(path.getPath());
        return ((long) stats.getBlockSize()) * ((long) stats.getAvailableBlocks());
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (!isExternalMounted() || getExternalCacheDir(context) == null) {
            cachePath = context.getCacheDir().getPath();
        } else {
            cachePath = getExternalCacheDir(context).getPath();
        }
        Log.i("Cache dir", cachePath + File.separator + uniqueName);
        return new File(cachePath + File.separator + uniqueName);
    }

    private static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    private static boolean isExternalMounted() {
        if (hasGingerbread()) {
            return "mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable();
        }
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024];
            while (true) {
                int read = reader.read(buffer);
                int count = read;
                if (read == -1) {
                    return writer.toString();
                }
                writer.write(buffer, 0, count);
            }
        } finally {
            reader.close();
        }
    }

    public static void deleteContents(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            File[] arr$ = files;
            int len$ = arr$.length;
            int i$ = 0;
            while (i$ < len$) {
                File file = arr$[i$];
                if (file.isDirectory()) {
                    deleteContents(file);
                }
                if (file.delete()) {
                    i$++;
                } else {
                    throw new IOException("failed to delete file: " + file);
                }
            }
            return;
        }
        throw new IOException("not a readable directory: " + dir);
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }

    public static boolean isSpecialType(String url) {
        return url.startsWith(SCHEME_FILE) || url.startsWith(SCHEME_VIDEO) || url.startsWith(SCHEME_CONTENT) || url.startsWith(SCHEME_ANDROID_RESOURCE);
    }

    public static String getSchemeBaseUrl(String type, String url) {
        return type + "://" + url;
    }

    public static String getSchemeBaseUrl(String type, int id) {
        return type + "://" + id;
    }

    public static String getHeader(HttpResponse response, String key) {
        Header header = response.getFirstHeader(key);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

    public static boolean isSupportRange(HttpResponse response) {
        if (TextUtils.equals(getHeader(response, Util.ACCEPT_RANGES), "bytes")) {
            return true;
        }
        String value = getHeader(response, Util.CONTENT_RANGE);
        if (value == null || !value.startsWith("bytes")) {
            return false;
        }
        return true;
    }

    public static boolean isGzipContent(HttpResponse response) {
        return TextUtils.equals(getHeader(response, "Content-Encoding"), "gzip");
    }
}
