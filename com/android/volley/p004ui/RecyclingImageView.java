package com.android.volley.p004ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/* renamed from: com.android.volley.ui.RecyclingImageView */
public class RecyclingImageView extends ImageView {
    public RecyclingImageView(Context context) {
        super(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        setImageDrawable((Drawable) null);
        super.onDetachedFromWindow();
    }

    public void setImageDrawable(Drawable drawable) {
        Drawable previousDrawable = getDrawable();
        super.setImageDrawable(drawable);
        notifyDrawable(drawable, true);
        notifyDrawable(previousDrawable, false);
    }

    private static void notifyDrawable(Drawable drawable, boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int z = layerDrawable.getNumberOfLayers();
            for (int i = 0; i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }
}
