package com.example.cameraexample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.android.volley.misc.AsyncTask;
import java.net.URL;
import java.util.HashMap;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private static HashMap<String, Bitmap> bitmapHash = new HashMap<>();
    private ImageView imageView;
    private String urlStr;

    public ImageLoadTask(String urlStr2, ImageView imageView2) {
        this.urlStr = urlStr2;
        this.imageView = imageView2;
    }

    /* access modifiers changed from: protected */
    public void onPreExecute() {
        super.onPreExecute();
    }

    /* access modifiers changed from: protected */
    public Bitmap doInBackground(Void... voids) {
        Bitmap oldbitmap;
        try {
            if (bitmapHash.containsKey(this.urlStr) && (oldbitmap = bitmapHash.remove(this.urlStr)) != null) {
                oldbitmap.recycle();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(this.urlStr).openConnection().getInputStream());
            bitmapHash.put(this.urlStr, bitmap);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        this.imageView.setImageBitmap(bitmap);
        this.imageView.invalidate();
    }
}
