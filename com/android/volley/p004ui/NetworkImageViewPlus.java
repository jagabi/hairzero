package com.android.volley.p004ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import com.android.volley.Response;
import com.android.volley.cache.plus.ImageLoader;
import com.android.volley.cache.plus.SimpleImageLoader;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;

/* renamed from: com.android.volley.ui.NetworkImageViewPlus */
public class NetworkImageViewPlus extends RecyclingImageView {
    private static final int HALF_FADE_IN_TIME = 100;
    private static final ColorDrawable transparentDrawable = new ColorDrawable(17170445);
    private final int[] attrsArray;
    int mDefaultImageId;
    int mErrorImageId;
    /* access modifiers changed from: private */
    public boolean mFadeInImage;
    protected ImageLoader.ImageContainer mImageContainer;
    protected ImageLoader mImageLoader;
    /* access modifiers changed from: private */
    public Response.Listener<BitmapDrawable> mListener;
    private int mMaxImageHeight;
    private int mMaxImageWidth;
    protected String mUrl;

    public NetworkImageViewPlus(Context context) {
        this(context, (AttributeSet) null);
    }

    public NetworkImageViewPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int[] iArr = {16843033};
        this.attrsArray = iArr;
        this.mFadeInImage = false;
        this.mMaxImageHeight = 0;
        this.mMaxImageWidth = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, iArr);
        setDefaultImageResId(a.getResourceId(0, 0));
        a.recycle();
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        loadImageIfNecessary(false);
    }

    public void setResetImageUrl(String url, ImageLoader imageLoader) {
        this.mImageContainer = null;
        this.mUrl = url;
        this.mImageLoader = imageLoader;
        loadImageIfNecessary(false);
    }

    public void setDefaultImageResId(int defaultImage) {
        this.mDefaultImageId = defaultImage;
    }

    public void setErrorImageResId(int errorImage) {
        this.mErrorImageId = errorImage;
    }

    public void setMaxImageSize(int maxImageWidth, int maxImageHeight) {
        this.mMaxImageWidth = maxImageWidth;
        this.mMaxImageHeight = maxImageHeight;
    }

    public void setMaxImageSize(int maxImageSize) {
        setMaxImageSize(maxImageSize, maxImageSize);
    }

    public void setFadeInImage(boolean fadeInImage) {
        this.mFadeInImage = fadeInImage;
    }

    public void setImageListener(Response.Listener<BitmapDrawable> listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void loadImageIfNecessary(final boolean isInLayoutPass) {
        int maxWidth;
        int width = getWidth();
        int height = getHeight();
        boolean wrapWidth = false;
        boolean wrapHeight = false;
        boolean z = true;
        int maxHeight = 0;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == -2;
            wrapHeight = getLayoutParams().height == -2;
        }
        if (!wrapWidth || !wrapHeight) {
            z = false;
        }
        boolean isFullyWrapContent = z;
        if (width != 0 || height != 0 || isFullyWrapContent) {
            if (TextUtils.isEmpty(this.mUrl)) {
                ImageLoader.ImageContainer imageContainer = this.mImageContainer;
                if (imageContainer != null) {
                    imageContainer.cancelRequest();
                    this.mImageContainer = null;
                }
                setDefaultImageOrNull();
                return;
            }
            ImageLoader.ImageContainer imageContainer2 = this.mImageContainer;
            if (!(imageContainer2 == null || imageContainer2.getRequestUrl() == null)) {
                if (!this.mImageContainer.getRequestUrl().equals(this.mUrl)) {
                    this.mImageContainer.cancelRequest();
                    setDefaultImageOrNull();
                } else {
                    return;
                }
            }
            ImageLoader imageLoader = this.mImageLoader;
            if (imageLoader instanceof SimpleImageLoader) {
                SimpleImageLoader loader = (SimpleImageLoader) imageLoader;
                int i = this.mMaxImageWidth;
                if (i == 0) {
                    i = loader.getMaxImageWidth();
                }
                maxWidth = i;
                int i2 = this.mMaxImageHeight;
                if (i2 == 0) {
                    i2 = loader.getMaxImageHeight();
                }
                maxHeight = i2;
            } else {
                maxWidth = wrapWidth ? 0 : width;
                if (!wrapHeight) {
                    maxHeight = height;
                }
            }
            this.mImageContainer = this.mImageLoader.get(this.mUrl, new ImageLoader.ImageListener() {
                public void onErrorResponse(VolleyError error) {
                    if (NetworkImageViewPlus.this.mErrorImageId != 0) {
                        NetworkImageViewPlus networkImageViewPlus = NetworkImageViewPlus.this;
                        networkImageViewPlus.setImageResource(networkImageViewPlus.mErrorImageId);
                    }
                }

                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && isInLayoutPass) {
                        NetworkImageViewPlus.this.post(new Runnable() {
                            public void run() {
                                C07331.this.onResponse(response, false);
                            }
                        });
                    } else if (response.getBitmap() != null) {
                        NetworkImageViewPlus.this.setAnimateImageBitmap(response.getBitmap(), NetworkImageViewPlus.this.mFadeInImage);
                        if (NetworkImageViewPlus.this.mListener != null) {
                            NetworkImageViewPlus.this.mListener.onResponse(response.getBitmap());
                        }
                    } else if (NetworkImageViewPlus.this.mDefaultImageId != 0) {
                        NetworkImageViewPlus networkImageViewPlus = NetworkImageViewPlus.this;
                        networkImageViewPlus.setImageResource(networkImageViewPlus.mDefaultImageId);
                    }
                }
            }, maxWidth, maxHeight);
        }
    }

    /* access modifiers changed from: private */
    public void setAnimateImageBitmap(final BitmapDrawable bitmap, boolean fadeIn) {
        Drawable initialDrawable;
        if (fadeIn && Utils.hasHoneycombMR1()) {
            animate().scaleY(0.95f).scaleX(0.95f).alpha(0.0f).setDuration(getDrawable() == null ? 0 : 100).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    NetworkImageViewPlus.this.setImageDrawable(bitmap);
                    NetworkImageViewPlus.this.animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).setDuration(100).setListener((Animator.AnimatorListener) null);
                }
            });
        } else if (fadeIn) {
            if (getDrawable() != null) {
                initialDrawable = getDrawable();
            } else {
                initialDrawable = transparentDrawable;
            }
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{initialDrawable, bitmap});
            setImageDrawable(td);
            td.startTransition(200);
        } else {
            setImageDrawable(bitmap);
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaultImageOrNull() {
        int i = this.mDefaultImageId;
        if (i != 0) {
            setImageResource(i);
        } else {
            setImageDrawable((Drawable) null);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mImageLoader != null) {
            loadImageIfNecessary(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ImageLoader.ImageContainer imageContainer = this.mImageContainer;
        if (imageContainer != null) {
            imageContainer.cancelRequest();
            setImageDrawable((Drawable) null);
            this.mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
