package com.android.volley.p004ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.ScaleGestureDetectorCompat;
import com.android.volley.C0109R;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.Utils;
import com.android.volley.toolbox.ImageLoader;

/* renamed from: com.android.volley.ui.PhotoView */
public class PhotoView extends NetworkImageView implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, Utils.HorizontallyScrollable {
    private static final float CROPPED_SIZE = 256.0f;
    private static final float DOUBLE_TAP_SCALE_FACTOR = 1.5f;
    private static final long ROTATE_ANIMATION_DURATION = 500;
    private static final long SNAP_DELAY = 250;
    private static final long SNAP_DURATION = 100;
    private static final float SNAP_THRESHOLD = 20.0f;
    private static final long ZOOM_ANIMATION_DURATION = 300;
    private static Paint sCropDimPaint;
    private static Paint sCropPaint;
    private static int sCropSize;
    private static boolean sInitialized;
    private static int sTouchSlopSquare;
    private static Bitmap sVideoImage;
    private static Bitmap sVideoNotReadyImage;
    private boolean mAllowCrop;
    private Rect mCropRect = new Rect();
    private int mCropSize;
    private boolean mDoubleTapDebounce;
    private boolean mDoubleTapOccurred;
    private boolean mDoubleTapToZoomEnabled = true;
    private float mDownFocusX;
    private float mDownFocusY;
    private Matrix mDrawMatrix;
    private Drawable mDrawable;
    private View.OnClickListener mExternalClickListener;
    private int mFixedHeight = -1;
    private boolean mFullScreen;
    private GestureDetectorCompat mGestureDetector;
    private boolean mHaveLayout;
    private boolean mIsDoubleTouch;
    private Matrix mMatrix = new Matrix();
    private float mMaxInitialScaleFactor = 2.0f;
    private float mMaxScale;
    private float mMinScale;
    private Matrix mOriginalMatrix = new Matrix();
    private boolean mQuickScaleEnabled;
    private RotateRunnable mRotateRunnable;
    private float mRotation;
    private ScaleGestureDetector mScaleGetureDetector;
    private ScaleRunnable mScaleRunnable;
    private SnapRunnable mSnapRunnable;
    private RectF mTempDst = new RectF();
    private RectF mTempSrc = new RectF();
    private boolean mTransformsEnabled;
    private RectF mTranslateRect = new RectF();
    private TranslateRunnable mTranslateRunnable;
    private float[] mValues = new float[9];
    private byte[] mVideoBlob;
    private boolean mVideoReady;

    public PhotoView(Context context) {
        super(context);
        initialize();
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public boolean onTouchEvent(MotionEvent event) {
        ScaleGestureDetector scaleGestureDetector = this.mScaleGetureDetector;
        if (scaleGestureDetector == null || this.mGestureDetector == null) {
            return true;
        }
        scaleGestureDetector.onTouchEvent(event);
        this.mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case 1:
            case 3:
                if (!this.mTranslateRunnable.mRunning) {
                    snap();
                    break;
                }
                break;
        }
        return true;
    }

    public boolean onDoubleTap(MotionEvent e) {
        this.mDoubleTapOccurred = true;
        if (!this.mQuickScaleEnabled) {
            return scale(e);
        }
        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        switch (e.getAction()) {
            case 0:
                if (!this.mQuickScaleEnabled) {
                    return false;
                }
                this.mDownFocusX = e.getX();
                this.mDownFocusY = e.getY();
                return false;
            case 1:
                if (this.mQuickScaleEnabled != 0) {
                    return scale(e);
                }
                return false;
            case 2:
                if (!this.mQuickScaleEnabled || !this.mDoubleTapOccurred) {
                    return false;
                }
                int deltaX = (int) (e.getX() - this.mDownFocusX);
                int deltaY = (int) (e.getY() - this.mDownFocusY);
                if ((deltaX * deltaX) + (deltaY * deltaY) <= sTouchSlopSquare) {
                    return false;
                }
                this.mDoubleTapOccurred = false;
                return false;
            default:
                return false;
        }
    }

    private boolean scale(MotionEvent e) {
        boolean handled = false;
        if (this.mDoubleTapToZoomEnabled && this.mTransformsEnabled && this.mDoubleTapOccurred) {
            if (!this.mDoubleTapDebounce) {
                float currentScale = getScale();
                this.mScaleRunnable.start(currentScale, Math.min(this.mMaxScale, Math.max(this.mMinScale, DOUBLE_TAP_SCALE_FACTOR * currentScale)), e.getX(), e.getY());
                handled = true;
            }
            this.mDoubleTapDebounce = false;
        }
        this.mDoubleTapOccurred = false;
        return handled;
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        View.OnClickListener onClickListener = this.mExternalClickListener;
        if (onClickListener != null && !this.mIsDoubleTouch) {
            onClickListener.onClick(this);
        }
        this.mIsDoubleTouch = false;
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!this.mTransformsEnabled) {
            return true;
        }
        translate(-distanceX, -distanceY);
        return true;
    }

    public boolean onDown(MotionEvent e) {
        if (!this.mTransformsEnabled) {
            return true;
        }
        this.mTranslateRunnable.stop();
        this.mSnapRunnable.stop();
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.mTransformsEnabled) {
            return true;
        }
        this.mTranslateRunnable.start(velocityX, velocityY);
        return true;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        if (!this.mTransformsEnabled) {
            return true;
        }
        this.mIsDoubleTouch = false;
        scale(detector.getScaleFactor() * getScale(), detector.getFocusX(), detector.getFocusY());
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (this.mTransformsEnabled) {
            this.mScaleRunnable.stop();
            this.mIsDoubleTouch = true;
        }
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        if (this.mTransformsEnabled && this.mIsDoubleTouch) {
            this.mDoubleTapDebounce = true;
            resetTransformations();
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mExternalClickListener = listener;
    }

    public boolean interceptMoveLeft(float origX, float origY) {
        if (!this.mTransformsEnabled) {
            return false;
        }
        if (this.mTranslateRunnable.mRunning) {
            return true;
        }
        this.mMatrix.getValues(this.mValues);
        this.mTranslateRect.set(this.mTempSrc);
        this.mMatrix.mapRect(this.mTranslateRect);
        float viewWidth = (float) getWidth();
        float transX = this.mValues[2];
        float drawWidth = this.mTranslateRect.right - this.mTranslateRect.left;
        if (!this.mTransformsEnabled || drawWidth <= viewWidth || transX == 0.0f) {
            return false;
        }
        return true;
    }

    public boolean interceptMoveRight(float origX, float origY) {
        if (!this.mTransformsEnabled) {
            return false;
        }
        if (this.mTranslateRunnable.mRunning) {
            return true;
        }
        this.mMatrix.getValues(this.mValues);
        this.mTranslateRect.set(this.mTempSrc);
        this.mMatrix.mapRect(this.mTranslateRect);
        float viewWidth = (float) getWidth();
        float transX = this.mValues[2];
        float drawWidth = this.mTranslateRect.right - this.mTranslateRect.left;
        if (!this.mTransformsEnabled || drawWidth <= viewWidth) {
            return false;
        }
        if (transX != 0.0f && viewWidth >= drawWidth + transX) {
            return false;
        }
        return true;
    }

    public void clear() {
        this.mGestureDetector = null;
        this.mScaleGetureDetector = null;
        this.mDrawable = null;
        this.mScaleRunnable.stop();
        this.mScaleRunnable = null;
        this.mTranslateRunnable.stop();
        this.mTranslateRunnable = null;
        this.mSnapRunnable.stop();
        this.mSnapRunnable = null;
        this.mRotateRunnable.stop();
        this.mRotateRunnable = null;
        setOnClickListener((View.OnClickListener) null);
        this.mExternalClickListener = null;
        this.mDoubleTapOccurred = false;
    }

    public void bindResource(int resourceId) {
        bindDrawable(getResources().getDrawable(resourceId));
    }

    public void bindDrawable(Drawable drawable) {
        Drawable drawable2;
        boolean changed = false;
        if (!(drawable == null || drawable == (drawable2 = this.mDrawable))) {
            if (drawable2 != null) {
                drawable2.setCallback((Drawable.Callback) null);
            }
            this.mDrawable = drawable;
            this.mMinScale = 0.0f;
            drawable.setCallback(this);
            changed = true;
        }
        configureBounds(changed);
        invalidate();
    }

    public void bindPhoto(Bitmap photoBitmap) {
        Drawable drawable = this.mDrawable;
        boolean currentDrawableIsBitmapDrawable = drawable instanceof BitmapDrawable;
        boolean changed = !currentDrawableIsBitmapDrawable;
        if (drawable != null && currentDrawableIsBitmapDrawable) {
            if (photoBitmap != ((BitmapDrawable) drawable).getBitmap()) {
                changed = (photoBitmap == null || (this.mDrawable.getIntrinsicWidth() == photoBitmap.getWidth() && this.mDrawable.getIntrinsicHeight() == photoBitmap.getHeight())) ? false : true;
                this.mMinScale = 0.0f;
                this.mDrawable = null;
            } else {
                return;
            }
        }
        if (this.mDrawable == null && photoBitmap != null) {
            this.mDrawable = new BitmapDrawable(getResources(), photoBitmap);
        }
        configureBounds(changed);
        invalidate();
    }

    public Bitmap getPhoto() {
        Drawable drawable = this.mDrawable;
        if (drawable == null || !(drawable instanceof BitmapDrawable)) {
            return null;
        }
        return ((BitmapDrawable) drawable).getBitmap();
    }

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public byte[] getVideoData() {
        return this.mVideoBlob;
    }

    public boolean isVideo() {
        return this.mVideoBlob != null;
    }

    public boolean isVideoReady() {
        return this.mVideoBlob != null && this.mVideoReady;
    }

    public boolean isPhotoBound() {
        return this.mDrawable != null;
    }

    public void setFullScreen(boolean fullScreen, boolean animate) {
        if (fullScreen != this.mFullScreen) {
            this.mFullScreen = fullScreen;
            requestLayout();
            invalidate();
        }
    }

    public void enableAllowCrop(boolean allowCrop) {
        if (allowCrop && this.mHaveLayout) {
            throw new IllegalArgumentException("Cannot set crop after view has been laid out");
        } else if (allowCrop || !this.mAllowCrop) {
            this.mAllowCrop = allowCrop;
        } else {
            throw new IllegalArgumentException("Cannot unset crop mode");
        }
    }

    public Bitmap getCroppedPhoto() {
        if (!this.mAllowCrop) {
            return null;
        }
        Bitmap croppedBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        Canvas croppedCanvas = new Canvas(croppedBitmap);
        int cropWidth = this.mCropRect.right - this.mCropRect.left;
        float scaleWidth = CROPPED_SIZE / ((float) cropWidth);
        float scaleHeight = CROPPED_SIZE / ((float) cropWidth);
        Matrix matrix = new Matrix(this.mDrawMatrix);
        matrix.postTranslate((float) (-this.mCropRect.left), (float) (-this.mCropRect.top));
        matrix.postScale(scaleWidth, scaleHeight);
        if (this.mDrawable != null) {
            croppedCanvas.concat(matrix);
            this.mDrawable.draw(croppedCanvas);
        }
        return croppedBitmap;
    }

    public void resetTransformations() {
        this.mMatrix.set(this.mOriginalMatrix);
        invalidate();
    }

    public void rotateClockwise() {
        rotate(90.0f, true);
    }

    public void rotateCounterClockwise() {
        rotate(-90.0f, true);
    }

    public void rotateTo(float degree) {
        rotate(degree % 360.0f, true);
    }

    public void rotateTo(float degree, boolean animate) {
        rotate(degree % 360.0f, animate);
    }

    public float getRotationDegree() {
        return this.mRotation % 360.0f;
    }

    public void setRotationDegree(float degree) {
        this.mRotation = degree % 360.0f;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mDrawable != null) {
            int saveCount = canvas.getSaveCount();
            canvas.save();
            Matrix matrix = this.mDrawMatrix;
            if (matrix != null) {
                canvas.concat(matrix);
            }
            this.mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
            if (this.mVideoBlob != null) {
                Bitmap videoImage = this.mVideoReady ? sVideoImage : sVideoNotReadyImage;
                canvas.drawBitmap(videoImage, (float) ((getWidth() - videoImage.getWidth()) / 2), (float) ((getHeight() - videoImage.getHeight()) / 2), (Paint) null);
            }
            this.mTranslateRect.set(this.mDrawable.getBounds());
            Matrix matrix2 = this.mDrawMatrix;
            if (matrix2 != null) {
                matrix2.mapRect(this.mTranslateRect);
            }
            if (this.mAllowCrop) {
                int previousSaveCount = canvas.getSaveCount();
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), sCropDimPaint);
                canvas.save();
                canvas.clipRect(this.mCropRect);
                Matrix matrix3 = this.mDrawMatrix;
                if (matrix3 != null) {
                    canvas.concat(matrix3);
                }
                this.mDrawable.draw(canvas);
                canvas.restoreToCount(previousSaveCount);
                canvas.drawRect(this.mCropRect, sCropPaint);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mHaveLayout = true;
        int layoutWidth = getWidth();
        int layoutHeight = getHeight();
        if (this.mAllowCrop) {
            int min = Math.min(sCropSize, Math.min(layoutWidth, layoutHeight));
            this.mCropSize = min;
            int cropLeft = (layoutWidth - min) / 2;
            int cropTop = (layoutHeight - min) / 2;
            this.mCropRect.set(cropLeft, cropTop, cropLeft + min, min + cropTop);
        }
        configureBounds(changed);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = this.mFixedHeight;
        if (i != -1) {
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(i, Integer.MIN_VALUE));
            setMeasuredDimension(getMeasuredWidth(), this.mFixedHeight);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean verifyDrawable(Drawable drawable) {
        return this.mDrawable == drawable || super.verifyDrawable(drawable);
    }

    public void invalidateDrawable(Drawable drawable) {
        if (this.mDrawable == drawable) {
            invalidate();
        } else {
            super.invalidateDrawable(drawable);
        }
    }

    public void setFixedHeight(int fixedHeight) {
        boolean adjustBounds = fixedHeight != this.mFixedHeight;
        this.mFixedHeight = fixedHeight;
        setMeasuredDimension(getMeasuredWidth(), this.mFixedHeight);
        if (adjustBounds) {
            configureBounds(true);
            requestLayout();
        }
    }

    public void enableImageTransforms(boolean enable) {
        this.mTransformsEnabled = enable;
        if (!enable) {
            resetTransformations();
        }
    }

    public boolean isImageTransformsEnabled() {
        return this.mTransformsEnabled;
    }

    private void configureBounds(boolean changed) {
        Drawable drawable = this.mDrawable;
        if (drawable != null && this.mHaveLayout) {
            int dwidth = drawable.getIntrinsicWidth();
            int dheight = this.mDrawable.getIntrinsicHeight();
            boolean fits = (dwidth < 0 || getWidth() == dwidth) && (dheight < 0 || getHeight() == dheight);
            this.mDrawable.setBounds(0, 0, dwidth, dheight);
            if (changed || (this.mMinScale == 0.0f && this.mDrawable != null && this.mHaveLayout)) {
                generateMatrix();
                generateScale();
            }
            this.mMatrix.postRotate(getRotationDegree(), (float) (getWidth() / 2), (float) (getHeight() / 2));
            if (fits || this.mMatrix.isIdentity()) {
                this.mDrawMatrix = null;
            } else {
                this.mDrawMatrix = this.mMatrix;
            }
        }
    }

    private void generateMatrix() {
        int dwidth = this.mDrawable.getIntrinsicWidth();
        int dheight = this.mDrawable.getIntrinsicHeight();
        int vwidth = this.mAllowCrop ? sCropSize : getWidth();
        int vheight = this.mAllowCrop ? sCropSize : getHeight();
        if (!((dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight)) || this.mAllowCrop) {
            this.mTempSrc.set(0.0f, 0.0f, (float) dwidth, (float) dheight);
            if (this.mAllowCrop) {
                this.mTempDst.set(this.mCropRect);
            } else {
                this.mTempDst.set(0.0f, 0.0f, (float) vwidth, (float) vheight);
            }
            float f = this.mMaxInitialScaleFactor;
            RectF scaledDestination = new RectF(((float) (vwidth / 2)) - ((((float) dwidth) * f) / 2.0f), ((float) (vheight / 2)) - ((((float) dheight) * f) / 2.0f), ((float) (vwidth / 2)) + ((((float) dwidth) * f) / 2.0f), ((float) (vheight / 2)) + ((((float) dheight) * f) / 2.0f));
            if (this.mTempDst.contains(scaledDestination)) {
                this.mMatrix.setRectToRect(this.mTempSrc, scaledDestination, Matrix.ScaleToFit.CENTER);
            } else {
                this.mMatrix.setRectToRect(this.mTempSrc, this.mTempDst, Matrix.ScaleToFit.CENTER);
            }
        } else {
            this.mMatrix.reset();
        }
        this.mOriginalMatrix.set(this.mMatrix);
    }

    private void generateScale() {
        int dwidth = this.mDrawable.getIntrinsicWidth();
        int dheight = this.mDrawable.getIntrinsicHeight();
        int vwidth = this.mAllowCrop ? getCropSize() : getWidth();
        int vheight = this.mAllowCrop ? getCropSize() : getHeight();
        if (dwidth >= vwidth || dheight >= vheight || this.mAllowCrop) {
            this.mMinScale = getScale();
        } else {
            this.mMinScale = 1.0f;
        }
        this.mMaxScale = Math.max(this.mMinScale * 8.0f, 8.0f);
    }

    private int getCropSize() {
        int i = this.mCropSize;
        return i > 0 ? i : sCropSize;
    }

    private float getScale() {
        return (float) Math.sqrt((double) (((float) Math.pow((double) getValue(this.mMatrix, 0), 2.0d)) + ((float) Math.pow((double) getValue(this.mMatrix, 3), 2.0d))));
    }

    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(this.mValues);
        return this.mValues[whichValue];
    }

    /* access modifiers changed from: private */
    public void scale(float newScale, float centerX, float centerY) {
        this.mMatrix.postRotate(-this.mRotation, (float) (getWidth() / 2), (float) (getHeight() / 2));
        float factor = Math.min(Math.max(newScale, this.mMinScale), this.mMaxScale) / getScale();
        this.mMatrix.postScale(factor, factor, centerX, centerY);
        snap();
        this.mMatrix.postRotate(this.mRotation, (float) (getWidth() / 2), (float) (getHeight() / 2));
        invalidate();
    }

    /* access modifiers changed from: private */
    public boolean translate(float tx, float ty) {
        float translateX;
        float translateY;
        this.mTranslateRect.set(this.mTempSrc);
        this.mMatrix.mapRect(this.mTranslateRect);
        float maxTop = 0.0f;
        float maxLeft = this.mAllowCrop ? (float) this.mCropRect.left : 0.0f;
        float maxRight = (float) (this.mAllowCrop ? this.mCropRect.right : getWidth());
        float l = this.mTranslateRect.left;
        float r = this.mTranslateRect.right;
        if (this.mAllowCrop) {
            translateX = Math.max(maxLeft - this.mTranslateRect.right, Math.min(maxRight - this.mTranslateRect.left, tx));
        } else if (r - l < maxRight - maxLeft) {
            translateX = (((maxRight - maxLeft) - (r + l)) / 2.0f) + maxLeft;
        } else {
            translateX = Math.max(maxRight - r, Math.min(maxLeft - l, tx));
        }
        if (this.mAllowCrop) {
            maxTop = (float) this.mCropRect.top;
        }
        float maxBottom = (float) (this.mAllowCrop ? this.mCropRect.bottom : getHeight());
        float t = this.mTranslateRect.top;
        float b = this.mTranslateRect.bottom;
        if (this.mAllowCrop) {
            translateY = Math.max(maxTop - this.mTranslateRect.bottom, Math.min(maxBottom - this.mTranslateRect.top, ty));
        } else if (b - t < maxBottom - maxTop) {
            translateY = maxTop + (((maxBottom - maxTop) - (b + t)) / 2.0f);
        } else {
            translateY = Math.max(maxBottom - b, Math.min(maxTop - t, ty));
        }
        this.mMatrix.postTranslate(translateX, translateY);
        invalidate();
        return translateX == tx && translateY == ty;
    }

    /* access modifiers changed from: private */
    public void snap() {
        float translateX;
        float translateY;
        this.mTranslateRect.set(this.mTempSrc);
        this.mMatrix.mapRect(this.mTranslateRect);
        float maxTop = 0.0f;
        float maxLeft = this.mAllowCrop ? (float) this.mCropRect.left : 0.0f;
        float maxRight = (float) (this.mAllowCrop ? this.mCropRect.right : getWidth());
        float l = this.mTranslateRect.left;
        float r = this.mTranslateRect.right;
        if (r - l < maxRight - maxLeft) {
            translateX = (((maxRight - maxLeft) - (r + l)) / 2.0f) + maxLeft;
        } else if (l > maxLeft) {
            translateX = maxLeft - l;
        } else if (r < maxRight) {
            translateX = maxRight - r;
        } else {
            translateX = 0.0f;
        }
        if (this.mAllowCrop) {
            maxTop = (float) this.mCropRect.top;
        }
        float maxBottom = (float) (this.mAllowCrop ? this.mCropRect.bottom : getHeight());
        float t = this.mTranslateRect.top;
        float b = this.mTranslateRect.bottom;
        if (b - t < maxBottom - maxTop) {
            translateY = (((maxBottom - maxTop) - (b + t)) / 2.0f) + maxTop;
        } else if (t > maxTop) {
            translateY = maxTop - t;
        } else if (b < maxBottom) {
            translateY = maxBottom - b;
        } else {
            translateY = 0.0f;
        }
        if (Math.abs(translateX) > SNAP_THRESHOLD || Math.abs(translateY) > SNAP_THRESHOLD) {
            this.mSnapRunnable.start(translateX, translateY);
            return;
        }
        this.mMatrix.postTranslate(translateX, translateY);
        invalidate();
    }

    /* access modifiers changed from: private */
    public void rotate(float degrees, boolean animate) {
        if (animate) {
            this.mRotateRunnable.start(degrees);
            return;
        }
        this.mRotation += degrees;
        this.mMatrix.postRotate(degrees, (float) (getWidth() / 2), (float) (getHeight() / 2));
        invalidate();
    }

    private void initialize() {
        Context context = getContext();
        if (!sInitialized) {
            sInitialized = true;
            Resources resources = context.getApplicationContext().getResources();
            sCropSize = resources.getDimensionPixelSize(C0109R.dimen.photo_crop_width);
            Paint paint = new Paint();
            sCropDimPaint = paint;
            paint.setAntiAlias(true);
            sCropDimPaint.setColor(resources.getColor(C0109R.C0110color.photo_crop_dim_color));
            sCropDimPaint.setStyle(Paint.Style.FILL);
            Paint paint2 = new Paint();
            sCropPaint = paint2;
            paint2.setAntiAlias(true);
            sCropPaint.setColor(resources.getColor(C0109R.C0110color.photo_crop_highlight_color));
            sCropPaint.setStyle(Paint.Style.STROKE);
            sCropPaint.setStrokeWidth(resources.getDimension(C0109R.dimen.photo_crop_stroke_width));
            int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            sTouchSlopSquare = touchSlop * touchSlop;
        }
        this.mGestureDetector = new GestureDetectorCompat(context, this, (Handler) null);
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, this);
        this.mScaleGetureDetector = scaleGestureDetector;
        this.mQuickScaleEnabled = ScaleGestureDetectorCompat.isQuickScaleEnabled((Object) scaleGestureDetector);
        this.mScaleRunnable = new ScaleRunnable(this);
        this.mTranslateRunnable = new TranslateRunnable(this);
        this.mSnapRunnable = new SnapRunnable(this);
        this.mRotateRunnable = new RotateRunnable(this);
    }

    /* renamed from: com.android.volley.ui.PhotoView$ScaleRunnable */
    private static class ScaleRunnable implements Runnable {
        private float mCenterX;
        private float mCenterY;
        private final PhotoView mHeader;
        private boolean mRunning;
        private float mStartScale;
        private long mStartTime;
        private boolean mStop;
        private float mTargetScale;
        private float mVelocity;
        private boolean mZoomingIn;

        public ScaleRunnable(PhotoView header) {
            this.mHeader = header;
        }

        public boolean start(float startScale, float targetScale, float centerX, float centerY) {
            if (this.mRunning) {
                return false;
            }
            this.mCenterX = centerX;
            this.mCenterY = centerY;
            this.mTargetScale = targetScale;
            this.mStartTime = System.currentTimeMillis();
            this.mStartScale = startScale;
            float f = this.mTargetScale;
            this.mZoomingIn = f > startScale;
            this.mVelocity = (f - startScale) / 300.0f;
            this.mRunning = true;
            this.mStop = false;
            this.mHeader.post(this);
            return true;
        }

        public void stop() {
            this.mRunning = false;
            this.mStop = true;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x002c, code lost:
            if (r9.mZoomingIn == (r4 > r5)) goto L_0x002e;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            /*
                r9 = this;
                boolean r0 = r9.mStop
                if (r0 == 0) goto L_0x0005
                return
            L_0x0005:
                long r0 = java.lang.System.currentTimeMillis()
                long r2 = r9.mStartTime
                long r2 = r0 - r2
                float r4 = r9.mStartScale
                float r5 = r9.mVelocity
                float r6 = (float) r2
                float r5 = r5 * r6
                float r4 = r4 + r5
                com.android.volley.ui.PhotoView r5 = r9.mHeader
                float r6 = r9.mCenterX
                float r7 = r9.mCenterY
                r5.scale(r4, r6, r7)
                float r5 = r9.mTargetScale
                int r6 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
                if (r6 == 0) goto L_0x002e
                boolean r6 = r9.mZoomingIn
                int r7 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
                if (r7 <= 0) goto L_0x002b
                r7 = 1
                goto L_0x002c
            L_0x002b:
                r7 = 0
            L_0x002c:
                if (r6 != r7) goto L_0x003a
            L_0x002e:
                com.android.volley.ui.PhotoView r6 = r9.mHeader
                float r7 = r9.mCenterX
                float r8 = r9.mCenterY
                r6.scale(r5, r7, r8)
                r9.stop()
            L_0x003a:
                boolean r5 = r9.mStop
                if (r5 != 0) goto L_0x0043
                com.android.volley.ui.PhotoView r5 = r9.mHeader
                r5.post(r9)
            L_0x0043:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.volley.p004ui.PhotoView.ScaleRunnable.run():void");
        }
    }

    /* renamed from: com.android.volley.ui.PhotoView$TranslateRunnable */
    private static class TranslateRunnable implements Runnable {
        private static final float DECELERATION_RATE = 1000.0f;
        private static final long NEVER = -1;
        private final PhotoView mHeader;
        private long mLastRunTime = -1;
        /* access modifiers changed from: private */
        public boolean mRunning;
        private boolean mStop;
        private float mVelocityX;
        private float mVelocityY;

        public TranslateRunnable(PhotoView header) {
            this.mHeader = header;
        }

        public boolean start(float velocityX, float velocityY) {
            if (this.mRunning) {
                return false;
            }
            this.mLastRunTime = -1;
            this.mVelocityX = velocityX;
            this.mVelocityY = velocityY;
            this.mStop = false;
            this.mRunning = true;
            this.mHeader.post(this);
            return true;
        }

        public void stop() {
            this.mRunning = false;
            this.mStop = true;
        }

        public void run() {
            if (!this.mStop) {
                long now = System.currentTimeMillis();
                long j = this.mLastRunTime;
                float delta = j != -1 ? ((float) (now - j)) / DECELERATION_RATE : 0.0f;
                boolean didTranslate = this.mHeader.translate(this.mVelocityX * delta, this.mVelocityY * delta);
                this.mLastRunTime = now;
                float slowDown = DECELERATION_RATE * delta;
                float f = this.mVelocityX;
                if (f > 0.0f) {
                    float f2 = f - slowDown;
                    this.mVelocityX = f2;
                    if (f2 < 0.0f) {
                        this.mVelocityX = 0.0f;
                    }
                } else {
                    float f3 = f + slowDown;
                    this.mVelocityX = f3;
                    if (f3 > 0.0f) {
                        this.mVelocityX = 0.0f;
                    }
                }
                float f4 = this.mVelocityY;
                if (f4 > 0.0f) {
                    float f5 = f4 - slowDown;
                    this.mVelocityY = f5;
                    if (f5 < 0.0f) {
                        this.mVelocityY = 0.0f;
                    }
                } else {
                    float f6 = f4 + slowDown;
                    this.mVelocityY = f6;
                    if (f6 > 0.0f) {
                        this.mVelocityY = 0.0f;
                    }
                }
                if ((this.mVelocityX == 0.0f && this.mVelocityY == 0.0f) || !didTranslate) {
                    stop();
                    this.mHeader.snap();
                }
                if (!this.mStop) {
                    this.mHeader.post(this);
                }
            }
        }
    }

    /* renamed from: com.android.volley.ui.PhotoView$SnapRunnable */
    private static class SnapRunnable implements Runnable {
        private static final long NEVER = -1;
        private final PhotoView mHeader;
        private boolean mRunning;
        private long mStartRunTime = -1;
        private boolean mStop;
        private float mTranslateX;
        private float mTranslateY;

        public SnapRunnable(PhotoView header) {
            this.mHeader = header;
        }

        public boolean start(float translateX, float translateY) {
            if (this.mRunning) {
                return false;
            }
            this.mStartRunTime = -1;
            this.mTranslateX = translateX;
            this.mTranslateY = translateY;
            this.mStop = false;
            this.mRunning = true;
            this.mHeader.postDelayed(this, PhotoView.SNAP_DELAY);
            return true;
        }

        public void stop() {
            this.mRunning = false;
            this.mStop = true;
        }

        public void run() {
            float transX;
            float transY;
            if (!this.mStop) {
                long now = System.currentTimeMillis();
                long j = this.mStartRunTime;
                float delta = j != -1 ? (float) (now - j) : 0.0f;
                if (j == -1) {
                    this.mStartRunTime = now;
                }
                if (delta >= 100.0f) {
                    transY = this.mTranslateX;
                    transX = this.mTranslateY;
                } else {
                    float transX2 = (this.mTranslateX / (100.0f - delta)) * 10.0f;
                    float transY2 = (this.mTranslateY / (100.0f - delta)) * 10.0f;
                    if (Math.abs(transX2) > Math.abs(this.mTranslateX) || transX2 == Float.NaN) {
                        transX2 = this.mTranslateX;
                    }
                    if (Math.abs(transY2) > Math.abs(this.mTranslateY) || transY2 == Float.NaN) {
                        float f = transX2;
                        transX = this.mTranslateY;
                        transY = f;
                    } else {
                        float f2 = transX2;
                        transX = transY2;
                        transY = f2;
                    }
                }
                boolean unused = this.mHeader.translate(transY, transX);
                float f3 = this.mTranslateX - transY;
                this.mTranslateX = f3;
                float f4 = this.mTranslateY - transX;
                this.mTranslateY = f4;
                if (f3 == 0.0f && f4 == 0.0f) {
                    stop();
                }
                if (!this.mStop) {
                    this.mHeader.post(this);
                }
            }
        }
    }

    /* renamed from: com.android.volley.ui.PhotoView$RotateRunnable */
    private static class RotateRunnable implements Runnable {
        private static final long NEVER = -1;
        private float mAppliedRotation;
        private final PhotoView mHeader;
        private long mLastRuntime;
        private boolean mRunning;
        private boolean mStop;
        private float mTargetRotation;
        private float mVelocity;

        public RotateRunnable(PhotoView header) {
            this.mHeader = header;
        }

        public void start(float rotation) {
            if (!this.mRunning) {
                this.mTargetRotation = rotation;
                this.mVelocity = rotation / 500.0f;
                this.mAppliedRotation = 0.0f;
                this.mLastRuntime = -1;
                this.mStop = false;
                this.mRunning = true;
                this.mHeader.post(this);
            }
        }

        public void stop() {
            this.mRunning = false;
            this.mStop = true;
        }

        public void run() {
            if (!this.mStop) {
                if (this.mAppliedRotation != this.mTargetRotation) {
                    long now = System.currentTimeMillis();
                    long j = this.mLastRuntime;
                    float rotationAmount = this.mVelocity * ((float) (j != -1 ? now - j : 0));
                    float f = this.mAppliedRotation;
                    float f2 = this.mTargetRotation;
                    if ((f < f2 && f + rotationAmount > f2) || (f > f2 && f + rotationAmount < f2)) {
                        rotationAmount = f2 - f;
                    }
                    this.mHeader.rotate(rotationAmount, false);
                    float f3 = this.mAppliedRotation + rotationAmount;
                    this.mAppliedRotation = f3;
                    if (f3 == this.mTargetRotation) {
                        stop();
                    }
                    this.mLastRuntime = now;
                }
                if (!this.mStop) {
                    this.mHeader.post(this);
                }
            }
        }
    }

    public void setMaxInitialScale(float f) {
        this.mMaxInitialScaleFactor = f;
    }

    public void loadImageIfNecessary(final boolean isInLayoutPass) {
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
                if (this.mImageContainer != null) {
                    this.mImageContainer.cancelRequest();
                    this.mImageContainer = null;
                }
                setDefaultImageOrNull();
                return;
            }
            if (!(this.mImageContainer == null || this.mImageContainer.getRequestUrl() == null)) {
                if (!this.mImageContainer.getRequestUrl().equals(this.mUrl)) {
                    this.mImageContainer.cancelRequest();
                    setDefaultImageOrNull();
                } else {
                    return;
                }
            }
            int maxWidth = wrapWidth ? 0 : width;
            if (!wrapHeight) {
                maxHeight = height;
            }
            this.mImageContainer = this.mImageLoader.get(this.mUrl, new ImageLoader.ImageListener() {
                public void onErrorResponse(VolleyError error) {
                    if (PhotoView.this.mErrorImageId != 0) {
                        PhotoView photoView = PhotoView.this;
                        photoView.bindResource(photoView.mErrorImageId);
                    }
                }

                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                    if (isImmediate && isInLayoutPass) {
                        PhotoView.this.post(new Runnable() {
                            public void run() {
                                C07361.this.onResponse(response, false);
                            }
                        });
                    } else if (response.getBitmap() != null) {
                        PhotoView.this.bindPhoto(response.getBitmap());
                        if (PhotoView.this.getImageListener() != null) {
                            PhotoView.this.getImageListener().onResponse(response.getBitmap());
                        }
                    } else if (PhotoView.this.mDefaultImageId != 0) {
                        PhotoView photoView = PhotoView.this;
                        photoView.bindResource(photoView.mDefaultImageId);
                    }
                }
            }, maxWidth, maxHeight);
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaultImageOrNull() {
        if (this.mDefaultImageId != 0) {
            bindResource(this.mDefaultImageId);
        } else {
            bindPhoto((Bitmap) null);
        }
    }

    public int getActualHeight() {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }

    public int getActualWidth() {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }
}
