package com.android.volley.cache.plus;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.ParseError;
import com.android.volley.misc.ImageUtils;
import com.android.volley.misc.Utils;
import com.android.volley.p004ui.RecyclingBitmapDrawable;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.File;
import java.io.FileNotFoundException;

public class ImageRequest extends Request<BitmapDrawable> {
    private static final float IMAGE_BACKOFF_MULT = 2.0f;
    private static final int IMAGE_MAX_RETRIES = 2;
    private static final int IMAGE_TIMEOUT_MS = 1000;
    private static final boolean PREFER_QUALITY_OVER_SPEED = false;
    private static final Object sDecodeLock = new Object();
    private final BitmapFactory.Options defaultOptions = getDefaultOptions();
    private ContentResolver mContentResolver;
    private final Bitmap.Config mDecodeConfig;
    private final Response.Listener<BitmapDrawable> mListener;
    private final int mMaxHeight;
    private final int mMaxWidth;
    private Resources mResources;

    public ImageRequest(String url, Resources resources, ContentResolver contentResolver, Response.Listener<BitmapDrawable> listener, int maxWidth, int maxHeight, Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(0, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(1000, 2, 2.0f));
        this.mResources = resources;
        this.mContentResolver = contentResolver;
        this.mListener = listener;
        this.mDecodeConfig = decodeConfig;
        this.mMaxWidth = maxWidth;
        this.mMaxHeight = maxHeight;
    }

    public Request.Priority getPriority() {
        return Request.Priority.LOW;
    }

    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }
        if (maxPrimary == 0) {
            return (int) (((double) actualPrimary) * (((double) maxSecondary) / ((double) actualSecondary)));
        } else if (maxSecondary == 0) {
            return maxPrimary;
        } else {
            double ratio = ((double) actualSecondary) / ((double) actualPrimary);
            int resized = maxPrimary;
            if (((double) resized) * ratio > ((double) maxSecondary)) {
                return (int) (((double) maxSecondary) / ratio);
            }
            return resized;
        }
    }

    /* access modifiers changed from: protected */
    public Response<BitmapDrawable> parseNetworkResponse(NetworkResponse response) {
        synchronized (sDecodeLock) {
            try {
                if (getUrl().startsWith(Utils.SCHEME_VIDEO)) {
                    Response<BitmapDrawable> doVideoFileParse = doVideoFileParse();
                    return doVideoFileParse;
                } else if (getUrl().startsWith(Utils.SCHEME_FILE)) {
                    Response<BitmapDrawable> doFileParse = doFileParse();
                    return doFileParse;
                } else if (getUrl().startsWith(Utils.SCHEME_ANDROID_RESOURCE)) {
                    Response<BitmapDrawable> doResourceParse = doResourceParse();
                    return doResourceParse;
                } else if (getUrl().startsWith(Utils.SCHEME_CONTENT)) {
                    Response<BitmapDrawable> doContentParse = doContentParse();
                    return doContentParse;
                } else {
                    Response<BitmapDrawable> doParse = doParse(response);
                    return doParse;
                }
            } catch (OutOfMemoryError e) {
                VolleyLog.m80e("Caught OOM for %d byte image, url=%s", Integer.valueOf(response.data.length), getUrl());
                return Response.error(new ParseError((Throwable) e));
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    private Response<BitmapDrawable> doVideoFileParse() {
        Bitmap bitmap;
        BitmapDrawable drawable;
        String requestUrl = getUrl();
        File bitmapFile = new File(requestUrl.substring(8, requestUrl.length()));
        if (!bitmapFile.exists() || !bitmapFile.isFile()) {
            return Response.error(new ParseError((Throwable) new FileNotFoundException(String.format("File not found: %s", new Object[]{bitmapFile.getAbsolutePath()}))));
        }
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = this.mDecodeConfig;
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            bitmap = getVideoFrame(bitmapFile.getAbsolutePath());
            addMarker("read-full-size-image-from-file");
        } else {
            decodeOptions.inJustDecodeBounds = true;
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;
            int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);
            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = getVideoFrame(bitmapFile.getAbsolutePath());
            addMarker(String.format("read-from-file-scaled-times-%d", new Object[]{Integer.valueOf(decodeOptions.inSampleSize)}));
            if (tempBitmap == null || (tempBitmap.getWidth() <= desiredWidth && tempBitmap.getHeight() <= desiredHeight)) {
                bitmap = tempBitmap;
            } else {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
                addMarker("scaling-read-from-file-bitmap");
            }
        }
        if (bitmap == null) {
            return Response.error(new ParseError());
        }
        if (Utils.hasHoneycomb()) {
            drawable = new BitmapDrawable(this.mResources, bitmap);
        } else {
            drawable = new RecyclingBitmapDrawable(this.mResources, bitmap);
        }
        return Response.success(drawable, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
    }

    private Bitmap getVideoFrame(String path) {
        return ThumbnailUtils.createVideoThumbnail(path, 1);
    }

    private Response<BitmapDrawable> doFileParse() {
        Bitmap bitmap;
        BitmapDrawable drawable;
        String requestUrl = getUrl();
        File bitmapFile = new File(requestUrl.substring(7, requestUrl.length()));
        if (!bitmapFile.exists() || !bitmapFile.isFile()) {
            return Response.error(new ParseError((Throwable) new FileNotFoundException(String.format("File not found: %s", new Object[]{bitmapFile.getAbsolutePath()}))));
        }
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = this.mDecodeConfig;
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            bitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), decodeOptions);
            addMarker("read-full-size-image-from-file");
        } else {
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;
            int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);
            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), decodeOptions);
            addMarker(String.format("read-from-file-scaled-times-%d", new Object[]{Integer.valueOf(decodeOptions.inSampleSize)}));
            if (tempBitmap == null || (tempBitmap.getWidth() <= desiredWidth && tempBitmap.getHeight() <= desiredHeight)) {
                bitmap = tempBitmap;
            } else {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
                addMarker("scaling-read-from-file-bitmap");
            }
        }
        if (bitmap == null) {
            return Response.error(new ParseError());
        }
        if (Utils.hasHoneycomb()) {
            drawable = new BitmapDrawable(this.mResources, bitmap);
        } else {
            drawable = new RecyclingBitmapDrawable(this.mResources, bitmap);
        }
        return Response.success(drawable, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
    }

    private Response<BitmapDrawable> doContentParse() {
        Bitmap bitmap;
        BitmapDrawable drawable;
        if (this.mContentResolver == null) {
            return Response.error(new ParseError("Content Resolver instance is null"));
        }
        Uri imageUri = Uri.parse(getUrl());
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = this.mDecodeConfig;
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            bitmap = ImageUtils.decodeStream(this.mContentResolver, imageUri, decodeOptions);
            addMarker("read-full-size-image-from-resource");
        } else {
            decodeOptions.inJustDecodeBounds = true;
            ImageUtils.decodeStream(this.mContentResolver, imageUri, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;
            int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);
            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = ImageUtils.decodeStream(this.mContentResolver, imageUri, decodeOptions);
            addMarker(String.format("read-from-resource-scaled-times-%d", new Object[]{Integer.valueOf(decodeOptions.inSampleSize)}));
            if (tempBitmap == null || (tempBitmap.getWidth() <= desiredWidth && tempBitmap.getHeight() <= desiredHeight)) {
                bitmap = tempBitmap;
            } else {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
                addMarker("scaling-read-from-resource-bitmap");
            }
        }
        if (bitmap == null) {
            return Response.error(new ParseError());
        }
        if (Utils.hasHoneycomb()) {
            drawable = new BitmapDrawable(this.mResources, bitmap);
        } else {
            drawable = new RecyclingBitmapDrawable(this.mResources, bitmap);
        }
        return Response.success(drawable, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
    }

    private Response<BitmapDrawable> doResourceParse() {
        Bitmap bitmap;
        BitmapDrawable drawable;
        if (this.mResources == null) {
            return Response.error(new ParseError());
        }
        int resourceId = Integer.valueOf(Uri.parse(getUrl()).getLastPathSegment()).intValue();
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = this.mDecodeConfig;
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            bitmap = BitmapFactory.decodeResource(this.mResources, resourceId, decodeOptions);
            addMarker("read-full-size-image-from-resource");
        } else {
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(this.mResources, resourceId, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;
            int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);
            decodeOptions.inJustDecodeBounds = false;
            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeResource(this.mResources, resourceId, decodeOptions);
            addMarker(String.format("read-from-resource-scaled-times-%d", new Object[]{Integer.valueOf(decodeOptions.inSampleSize)}));
            if (tempBitmap == null || (tempBitmap.getWidth() <= desiredWidth && tempBitmap.getHeight() <= desiredHeight)) {
                bitmap = tempBitmap;
            } else {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
                addMarker("scaling-read-from-resource-bitmap");
            }
        }
        if (bitmap == null) {
            return Response.error(new ParseError());
        }
        if (Utils.hasHoneycomb()) {
            drawable = new BitmapDrawable(this.mResources, bitmap);
        } else {
            drawable = new RecyclingBitmapDrawable(this.mResources, bitmap);
        }
        return Response.success(drawable, HttpHeaderParser.parseBitmapCacheHeaders(bitmap));
    }

    private Response<BitmapDrawable> doParse(NetworkResponse response) {
        Bitmap bitmap;
        BitmapDrawable drawable;
        byte[] data = response.data;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inInputShareable = true;
        decodeOptions.inPurgeable = true;
        decodeOptions.inPreferredConfig = this.mDecodeConfig;
        if (this.mMaxWidth == 0 && this.mMaxHeight == 0) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
        } else {
            decodeOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            int actualWidth = decodeOptions.outWidth;
            int actualHeight = decodeOptions.outHeight;
            int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);
            decodeOptions.inJustDecodeBounds = false;
            if (Utils.hasGingerbreadMR1()) {
                decodeOptions.inPreferQualityOverSpeed = false;
            }
            decodeOptions.inSampleSize = ImageUtils.findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
            if (tempBitmap == null || (tempBitmap.getWidth() <= desiredWidth && tempBitmap.getHeight() <= desiredHeight)) {
                bitmap = tempBitmap;
            } else {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                tempBitmap.recycle();
            }
        }
        if (bitmap == null) {
            return Response.error(new ParseError(response));
        }
        if (Utils.hasHoneycomb()) {
            drawable = new BitmapDrawable(this.mResources, bitmap);
        } else {
            drawable = new RecyclingBitmapDrawable(this.mResources, bitmap);
        }
        return Response.success(drawable, HttpHeaderParser.parseCacheHeaders(response));
    }

    /* access modifiers changed from: protected */
    public void deliverResponse(BitmapDrawable response) {
        this.mListener.onResponse(response);
    }

    public static BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
        decodeBitmapOptions.inDither = false;
        decodeBitmapOptions.inScaled = false;
        decodeBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        decodeBitmapOptions.inSampleSize = 1;
        if (Utils.hasHoneycomb()) {
            decodeBitmapOptions.inMutable = true;
        }
        return decodeBitmapOptions;
    }

    private BitmapFactory.Options getOptions() {
        BitmapFactory.Options result = new BitmapFactory.Options();
        copyOptions(this.defaultOptions, result);
        return result;
    }

    private static void copyOptions(BitmapFactory.Options from, BitmapFactory.Options to) {
        if (Build.VERSION.SDK_INT >= 11) {
            copyOptionsHoneycomb(from, to);
        } else if (Build.VERSION.SDK_INT >= 10) {
            copyOptionsGingerbreadMr1(from, to);
        } else {
            copyOptionsFroyo(from, to);
        }
    }

    private static void copyOptionsHoneycomb(BitmapFactory.Options from, BitmapFactory.Options to) {
        copyOptionsGingerbreadMr1(from, to);
        to.inMutable = from.inMutable;
    }

    private static void copyOptionsGingerbreadMr1(BitmapFactory.Options from, BitmapFactory.Options to) {
        copyOptionsFroyo(from, to);
        to.inPreferQualityOverSpeed = from.inPreferQualityOverSpeed;
    }

    private static void copyOptionsFroyo(BitmapFactory.Options from, BitmapFactory.Options to) {
        to.inDensity = from.inDensity;
        to.inDither = from.inDither;
        to.inInputShareable = from.inInputShareable;
        to.inPreferredConfig = from.inPreferredConfig;
        to.inPurgeable = from.inPurgeable;
        to.inSampleSize = from.inSampleSize;
        to.inScaled = from.inScaled;
        to.inScreenDensity = from.inScreenDensity;
        to.inTargetDensity = from.inTargetDensity;
    }
}
