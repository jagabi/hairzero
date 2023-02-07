package com.android.volley.misc;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class ImageUtils {
    /* access modifiers changed from: private */
    public static final Pattern BASE64_IMAGE_URI_PATTERN = Pattern.compile("^(?:.*;)?base64,.*");
    private static final String BASE64_URI_PREFIX = "base64,";
    private static final long MIN_NORMAL_CLASS = 32;
    private static final long MIN_SMALL_CLASS = 24;
    private static final String TAG = "ImageUtils";

    public interface InputStreamFactory {
        InputStream createInputStream() throws FileNotFoundException;
    }

    public static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        float n = 1.0f;
        while (((double) (n * 2.0f)) <= Math.min(((double) actualWidth) / ((double) desiredWidth), ((double) actualHeight) / ((double) desiredHeight))) {
            n *= 2.0f;
        }
        return (int) n;
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect) null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect) null, options);
    }

    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect) null, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, (Rect) null, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2;
            }
            for (long totalPixels = (long) ((width * height) / inSampleSize); totalPixels > ((long) (reqWidth * reqHeight * 2)); totalPixels /= 2) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static boolean isImageMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static Bitmap decodeStream(ContentResolver resolver, Uri uri, int maxSize) {
        InputStreamFactory factory = createInputStreamFactory(resolver, uri);
        try {
            Point bounds = getImageBounds(factory);
            if (bounds == null) {
                return null;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = Math.max(bounds.x / maxSize, bounds.y / maxSize);
            return decodeStream(factory, (Rect) null, opts);
        } catch (FileNotFoundException | IOException | IllegalArgumentException | SecurityException e) {
            return null;
        }
    }

    public static Bitmap decodeStream(ContentResolver resolver, Uri uri, BitmapFactory.Options opts) {
        try {
            return decodeStream(createInputStreamFactory(resolver, uri), (Rect) null, opts);
        } catch (FileNotFoundException | IllegalArgumentException | SecurityException e) {
            return null;
        }
    }

    public static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        try {
            return BitmapFactory.decodeStream(is, outPadding, opts);
        } catch (OutOfMemoryError oome) {
            Log.e(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options) threw an OOME", oome);
            return null;
        }
    }

    public static Bitmap decodeStream(InputStreamFactory factory, Rect outPadding, BitmapFactory.Options opts) throws FileNotFoundException {
        InputStream is = null;
        try {
            InputStream is2 = factory.createInputStream();
            int orientation = Exif.getOrientation(is2, -1);
            is2.close();
            InputStream is3 = factory.createInputStream();
            Bitmap originalBitmap = BitmapFactory.decodeStream(is3, outPadding, opts);
            if (is3 != null && originalBitmap == null) {
                if (!opts.inJustDecodeBounds) {
                    Log.w(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options): Image bytes cannot be decoded into a Bitmap");
                    throw new UnsupportedOperationException("Image bytes cannot be decoded into a Bitmap.");
                }
            }
            if (originalBitmap == null || orientation == 0) {
                if (is3 != null) {
                    try {
                        is3.close();
                    } catch (IOException e) {
                    }
                }
                return originalBitmap;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate((float) orientation);
            Bitmap createBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            if (is3 != null) {
                try {
                    is3.close();
                } catch (IOException e2) {
                }
            }
            return createBitmap;
        } catch (OutOfMemoryError oome) {
            Log.e(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options) threw an OOME", oome);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, "ImageUtils#decodeStream(InputStream, Rect, Options) threw an IOE", ioe);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e5) {
                }
            }
            throw th;
        }
    }

    public static Point getImageBounds(InputStreamFactory factory) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        decodeStream(factory, (Rect) null, opts);
        return new Point(opts.outWidth, opts.outHeight);
    }

    public static InputStreamFactory createInputStreamFactory(ContentResolver resolver, Uri uri) {
        if ("data".equals(uri.getScheme())) {
            return new DataInputStreamFactory(resolver, uri);
        }
        return new BaseInputStreamFactory(resolver, uri);
    }

    private static class BaseInputStreamFactory implements InputStreamFactory {
        protected final ContentResolver mResolver;
        protected final Uri mUri;

        public BaseInputStreamFactory(ContentResolver resolver, Uri uri) {
            this.mResolver = resolver;
            this.mUri = uri;
        }

        public InputStream createInputStream() throws FileNotFoundException {
            return this.mResolver.openInputStream(this.mUri);
        }
    }

    private static class DataInputStreamFactory extends BaseInputStreamFactory {
        private byte[] mData;

        public DataInputStreamFactory(ContentResolver resolver, Uri uri) {
            super(resolver, uri);
        }

        public InputStream createInputStream() throws FileNotFoundException {
            if (this.mData == null) {
                byte[] parseDataUri = parseDataUri(this.mUri);
                this.mData = parseDataUri;
                if (parseDataUri == null) {
                    return super.createInputStream();
                }
            }
            return new ByteArrayInputStream(this.mData);
        }

        private byte[] parseDataUri(Uri uri) {
            String ssp = uri.getSchemeSpecificPart();
            try {
                if (ssp.startsWith(ImageUtils.BASE64_URI_PREFIX)) {
                    return Base64.decode(ssp.substring(ImageUtils.BASE64_URI_PREFIX.length()), 8);
                }
                if (ImageUtils.BASE64_IMAGE_URI_PATTERN.matcher(ssp).matches()) {
                    return Base64.decode(ssp.substring(ssp.indexOf(ImageUtils.BASE64_URI_PREFIX) + ImageUtils.BASE64_URI_PREFIX.length()), 0);
                }
                return null;
            } catch (IllegalArgumentException ex) {
                Log.e(ImageUtils.TAG, "Mailformed data URI: " + ex);
                return null;
            }
        }
    }
}
