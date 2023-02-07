package com.android.volley.cache;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.http.AndroidHttpClient;
import android.widget.ImageView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.NetUtils;
import com.android.volley.misc.Utils;
import com.android.volley.p004ui.PhotoView;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageCache;
import com.android.volley.toolbox.ImageLoader;
import java.util.ArrayList;

public class SimpleImageLoader extends ImageLoader {
    protected static final String CACHE_DIR = "images";
    private static final int HALF_FADE_IN_TIME = 100;
    private static final ColorDrawable transparentDrawable = new ColorDrawable(17170445);
    private boolean mFadeInImage;
    private int mMaxImageHeight;
    private int mMaxImageWidth;
    private ArrayList<Drawable> mPlaceHolderDrawables;

    public interface ImageLoaderProvider {
        SimpleImageLoader getImageLoaderInstance();
    }

    public SimpleImageLoader(RequestQueue queue) {
        this(queue, (ImageCache) BitmapCache.getInstance((FragmentManager) null));
    }

    public SimpleImageLoader(RequestQueue queue, ImageCache imageCache) {
        this(queue, imageCache, (Resources) null);
    }

    public SimpleImageLoader(RequestQueue queue, ImageCache imageCache, Resources resources) {
        super(queue, imageCache, resources);
        this.mFadeInImage = true;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
    }

    public SimpleImageLoader(FragmentActivity activity) {
        super(newRequestQueue(activity, (DiskLruBasedCache.ImageCacheParams) null), BitmapImageCache.getInstance(activity.getSupportFragmentManager()), activity.getResources());
        this.mFadeInImage = true;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
    }

    public SimpleImageLoader(FragmentActivity activity, DiskLruBasedCache.ImageCacheParams imageCacheParams) {
        super(newRequestQueue(activity, imageCacheParams), BitmapImageCache.getInstance(activity.getSupportFragmentManager(), imageCacheParams), activity.getResources());
        this.mFadeInImage = true;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
    }

    public SimpleImageLoader(Context context) {
        super(newRequestQueue(context, (DiskLruBasedCache.ImageCacheParams) null), BitmapImageCache.getInstance((FragmentManager) null), context.getResources());
        this.mFadeInImage = true;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
    }

    public SimpleImageLoader(Context context, DiskLruBasedCache.ImageCacheParams imageCacheParams) {
        super(newRequestQueue(context, imageCacheParams), BitmapImageCache.getInstance((FragmentManager) null, imageCacheParams), context.getResources());
        this.mFadeInImage = true;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
    }

    public void startProcessingQueue() {
        getRequestQueue().start();
    }

    public void stopProcessingQueue() {
        getRequestQueue().stop();
    }

    public void clearCache() {
        getCache().clear();
    }

    public void flushCache() {
        getImageCache().clear();
        getCache().flush();
    }

    public void closeCache() {
        getCache().close();
    }

    public boolean isCached(String key) {
        return getCache().get(key) != null;
    }

    @Deprecated
    public void invalidate(String key) {
        getImageCache().invalidateBitmap(getCacheKey(key, this.mMaxImageWidth, this.mMaxImageHeight));
        getCache().invalidate(key, true);
    }

    public void invalidate(String key, ImageView view) {
        getImageCache().invalidateBitmap(getCacheKey(key, this.mMaxImageWidth, this.mMaxImageHeight, view.getScaleType()));
        getCache().invalidate(key, true);
        invalidate(key);
    }

    public SimpleImageLoader setFadeInImage(boolean fadeInImage) {
        this.mFadeInImage = fadeInImage;
        return this;
    }

    public SimpleImageLoader setMaxImageSize(int maxImageWidth, int maxImageHeight) {
        this.mMaxImageWidth = maxImageWidth;
        this.mMaxImageHeight = maxImageHeight;
        return this;
    }

    public SimpleImageLoader setDefaultDrawable(int defaultPlaceHolderResId) {
        ArrayList<Drawable> arrayList = new ArrayList<>(1);
        this.mPlaceHolderDrawables = arrayList;
        arrayList.add(defaultPlaceHolderResId == -1 ? null : getResources().getDrawable(defaultPlaceHolderResId));
        return this;
    }

    public SimpleImageLoader setDefaultDrawables(ArrayList<Drawable> placeHolderDrawables) {
        this.mPlaceHolderDrawables = placeHolderDrawables;
        return this;
    }

    public SimpleImageLoader setMaxImageSize(int maxImageSize) {
        return setMaxImageSize(maxImageSize, maxImageSize);
    }

    public int getMaxImageWidth() {
        return this.mMaxImageWidth;
    }

    public int getMaxImageHeight() {
        return this.mMaxImageHeight;
    }

    public ImageLoader.ImageContainer get(String requestUrl, ImageView imageView) {
        return get(requestUrl, imageView, 0);
    }

    public ImageLoader.ImageContainer get(String requestUrl, ImageView imageView, int maxImageWidth, int maxImageHeight) {
        ArrayList<Drawable> arrayList = this.mPlaceHolderDrawables;
        return get(requestUrl, imageView, arrayList != null ? arrayList.get(0) : null, maxImageWidth, maxImageHeight);
    }

    public ImageLoader.ImageContainer get(String requestUrl, ImageView imageView, int placeHolderIndex) {
        ArrayList<Drawable> arrayList = this.mPlaceHolderDrawables;
        return get(requestUrl, imageView, arrayList != null ? arrayList.get(placeHolderIndex) : null, this.mMaxImageWidth, this.mMaxImageHeight);
    }

    public ImageLoader.ImageContainer get(String requestUrl, ImageView imageView, Drawable placeHolder) {
        return get(requestUrl, imageView, placeHolder, this.mMaxImageWidth, this.mMaxImageHeight);
    }

    public ImageLoader.ImageContainer get(String requestUrl, ImageView imageView, Drawable placeHolder, int maxWidth, int maxHeight) {
        ImageLoader.ImageContainer imageContainer = (imageView.getTag() == null || !(imageView.getTag() instanceof ImageLoader.ImageContainer)) ? null : (ImageLoader.ImageContainer) imageView.getTag();
        String recycledImageUrl = imageContainer != null ? imageContainer.getRequestUrl() : null;
        if (requestUrl != null && requestUrl.equals(recycledImageUrl)) {
            return imageContainer;
        }
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            imageView.setTag((Object) null);
        }
        if (requestUrl != null) {
            ImageLoader.ImageContainer imageContainer2 = get(requestUrl, getImageListener(getResources(), imageView, placeHolder, this.mFadeInImage), maxWidth, maxHeight, imageView.getScaleType());
            imageView.setTag(imageContainer2);
            return imageContainer2;
        }
        if (!(imageView instanceof PhotoView)) {
            imageView.setImageDrawable(placeHolder);
        }
        imageView.setTag((Object) null);
        return imageContainer;
    }

    public ImageLoader.ImageContainer set(String requestUrl, ImageView imageView, Bitmap bitmap) {
        return set(requestUrl, imageView, 0, bitmap);
    }

    public ImageLoader.ImageContainer set(String requestUrl, ImageView imageView, int placeHolderIndex, Bitmap bitmap) {
        ArrayList<Drawable> arrayList = this.mPlaceHolderDrawables;
        return set(requestUrl, imageView, arrayList != null ? arrayList.get(placeHolderIndex) : null, this.mMaxImageWidth, this.mMaxImageHeight, bitmap);
    }

    public ImageLoader.ImageContainer set(String requestUrl, ImageView imageView, Drawable placeHolder, Bitmap bitmap) {
        return set(requestUrl, imageView, placeHolder, this.mMaxImageWidth, this.mMaxImageHeight, bitmap);
    }

    public ImageLoader.ImageContainer set(String requestUrl, ImageView imageView, Drawable placeHolder, int maxWidth, int maxHeight, Bitmap bitmap) {
        ImageView imageView2 = imageView;
        ImageLoader.ImageContainer imageContainer = (imageView.getTag() == null || !(imageView.getTag() instanceof ImageLoader.ImageContainer)) ? null : (ImageLoader.ImageContainer) imageView.getTag();
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            imageView.setTag((Object) null);
        }
        if (requestUrl != null) {
            Drawable drawable = placeHolder;
            ImageLoader.ImageContainer imageContainer2 = set(requestUrl, getImageListener(getResources(), imageView, placeHolder, this.mFadeInImage), maxWidth, maxHeight, imageView.getScaleType(), bitmap);
            imageView.setTag(imageContainer2);
            return imageContainer2;
        }
        Drawable drawable2 = placeHolder;
        if (!(imageView2 instanceof PhotoView)) {
            imageView.setImageDrawable(placeHolder);
        }
        imageView.setTag((Object) null);
        return imageContainer;
    }

    public static ImageLoader.ImageListener getImageListener(final Resources resources, final ImageView imageView, final Drawable placeHolder, final boolean fadeInImage) {
        return new ImageLoader.ImageListener() {
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageView.setTag((Object) null);
                if (response.getBitmap() != null) {
                    ImageView imageView = imageView;
                    boolean z = true;
                    if (imageView instanceof PhotoView) {
                        PhotoView photoView = (PhotoView) imageView;
                        Bitmap bitmap = response.getBitmap();
                        Resources resources = resources;
                        if (!fadeInImage || isImmediate) {
                            z = false;
                        }
                        SimpleImageLoader.setPhotoImageBitmap(photoView, bitmap, resources, z);
                        return;
                    }
                    Bitmap bitmap2 = response.getBitmap();
                    Resources resources2 = resources;
                    if (!fadeInImage || isImmediate) {
                        z = false;
                    }
                    SimpleImageLoader.setImageBitmap(imageView, bitmap2, resources2, z);
                    return;
                }
                ImageView imageView2 = imageView;
                if (!(imageView2 instanceof PhotoView)) {
                    imageView2.setImageDrawable(placeHolder);
                }
            }

            public void onErrorResponse(VolleyError volleyError) {
            }
        };
    }

    private static RequestQueue newRequestQueue(Context context, DiskLruBasedCache.ImageCacheParams imageCacheParams) {
        Cache cache;
        Network network = new BasicNetwork(Utils.hasHoneycomb() ? new HurlStack() : new HttpClientStack(AndroidHttpClient.newInstance(NetUtils.getUserAgent(context))));
        if (imageCacheParams != null) {
            cache = new DiskLruBasedCache(imageCacheParams);
        } else {
            cache = new DiskLruBasedCache(Utils.getDiskCacheDir(context, CACHE_DIR));
        }
        RequestQueue queue = new RequestQueue(cache, network);
        queue.start();
        return queue;
    }

    /* access modifiers changed from: private */
    public static void setImageBitmap(final ImageView imageView, final Bitmap bitmap, Resources resources, boolean fadeIn) {
        Drawable initialDrawable;
        if (fadeIn && Utils.hasHoneycombMR1()) {
            imageView.animate().scaleY(0.95f).scaleX(0.95f).alpha(0.0f).setDuration(imageView.getDrawable() == null ? 0 : 100).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    imageView.setImageBitmap(bitmap);
                    imageView.animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).setDuration(100).setListener((Animator.AnimatorListener) null);
                }
            });
        } else if (fadeIn) {
            if (imageView.getDrawable() != null) {
                initialDrawable = imageView.getDrawable();
            } else {
                initialDrawable = transparentDrawable;
            }
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{initialDrawable, new BitmapDrawable(resources, bitmap)});
            imageView.setImageDrawable(td);
            td.startTransition(200);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /* access modifiers changed from: private */
    public static void setPhotoImageBitmap(final PhotoView imageView, final Bitmap bitmap, Resources resources, boolean fadeIn) {
        Drawable initialDrawable;
        if (fadeIn && Utils.hasHoneycombMR1()) {
            imageView.animate().scaleY(0.95f).scaleX(0.95f).alpha(0.0f).setDuration(imageView.getDrawable() == null ? 0 : 100).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    imageView.bindPhoto(bitmap);
                    imageView.animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).setDuration(100).setListener((Animator.AnimatorListener) null);
                }
            });
        } else if (fadeIn) {
            if (imageView.getDrawable() != null) {
                initialDrawable = imageView.getDrawable();
            } else {
                initialDrawable = transparentDrawable;
            }
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{initialDrawable, new BitmapDrawable(resources, bitmap)});
            imageView.bindDrawable(td);
            td.startTransition(200);
        } else {
            imageView.bindPhoto(bitmap);
        }
    }
}
