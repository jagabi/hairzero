package com.android.volley.p004ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.volley.Response;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.ImageLoader;

/* renamed from: com.android.volley.ui.NetworkImageView */
public class NetworkImageView extends ImageView {
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
    public Response.Listener<Bitmap> mListener;
    private int mMaxImageHeight;
    private int mMaxImageWidth;
    protected String mUrl;

    public NetworkImageView(Context context) {
        this(context, (AttributeSet) null);
    }

    public NetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int[] iArr = {16843033};
        this.attrsArray = iArr;
        this.mFadeInImage = true;
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

    public void setImageListener(Response.Listener<Bitmap> listener) {
        this.mListener = listener;
    }

    public Response.Listener<Bitmap> getImageListener() {
        return this.mListener;
    }

    /* access modifiers changed from: package-private */
    public void loadImageIfNecessary(final boolean isInLayoutPass) {
        boolean wrapHeight;
        boolean wrapWidth;
        int maxHeight;
        int maxWidth;
        int width = getWidth();
        int height = getHeight();
        ImageView.ScaleType scaleType = getScaleType();
        boolean z = true;
        int maxHeight2 = 0;
        if (getLayoutParams() != null) {
            wrapWidth = getLayoutParams().width == -2;
            wrapHeight = getLayoutParams().height == -2;
        } else {
            wrapWidth = false;
            wrapHeight = false;
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
                int maxWidth2 = i;
                int maxHeight3 = this.mMaxImageHeight;
                if (maxHeight3 == 0) {
                    maxHeight3 = loader.getMaxImageHeight();
                }
                maxWidth = maxWidth2;
                maxHeight = maxHeight3;
            } else {
                int maxWidth3 = wrapWidth ? 0 : width;
                if (!wrapHeight) {
                    maxHeight2 = height;
                }
                maxWidth = maxWidth3;
                maxHeight = maxHeight2;
            }
            this.mImageContainer = this.mImageLoader.get(this.mUrl, new ImageLoader.ImageListener() {
                public void onErrorResponse(VolleyError error) {
                    if (NetworkImageView.this.mErrorImageId != 0) {
                        NetworkImageView networkImageView = NetworkImageView.this;
                        networkImageView.setImageResource(networkImageView.mErrorImageId);
                    }
                }

                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && isInLayoutPass) {
                        NetworkImageView.this.post(new Runnable() {
                            public void run() {
                                C07301.this.onResponse(response, false);
                            }
                        });
                    } else if (response.getBitmap() != null) {
                        NetworkImageView.this.setAnimateImageBitmap(response.getBitmap(), NetworkImageView.this.mFadeInImage);
                        if (NetworkImageView.this.mListener != null) {
                            NetworkImageView.this.mListener.onResponse(response.getBitmap());
                        }
                    } else if (NetworkImageView.this.mDefaultImageId != 0) {
                        NetworkImageView networkImageView = NetworkImageView.this;
                        networkImageView.setImageResource(networkImageView.mDefaultImageId);
                    }
                }
            }, maxWidth, maxHeight, scaleType);
        }
    }

    /* access modifiers changed from: private */
    public void setAnimateImageBitmap(final Bitmap bitmap, boolean fadeIn) {
        Drawable initialDrawable;
        if (fadeIn && Utils.hasHoneycombMR1()) {
            animate().scaleY(0.95f).scaleX(0.95f).alpha(0.0f).setDuration(getDrawable() == null ? 0 : 100).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    NetworkImageView.this.setImageBitmap(bitmap);
                    NetworkImageView.this.animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).setDuration(100).setListener((Animator.AnimatorListener) null);
                }
            });
        } else if (fadeIn) {
            if (getDrawable() != null) {
                initialDrawable = getDrawable();
            } else {
                initialDrawable = transparentDrawable;
            }
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{initialDrawable, new BitmapDrawable(getResources(), bitmap)});
            setImageDrawable(td);
            td.startTransition(200);
        } else {
            setImageBitmap(bitmap);
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaultImageOrNull() {
        int i = this.mDefaultImageId;
        if (i != 0) {
            setImageResource(i);
        } else {
            setImageBitmap((Bitmap) null);
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
            setImageBitmap((Bitmap) null);
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
