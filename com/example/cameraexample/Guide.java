package com.example.cameraexample;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Guide extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private static final String TAG = "d";
    private Button btn_guide;
    private Context context;
    private String imageFilePath;
    String imgPath;
    private MediaScanner mMediaScanner;
    /* access modifiers changed from: private */
    public Uri photoUri;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_guide);
        this.context = getApplicationContext();
        findViewById(C0111R.C0114id.btn_camera).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                if (intent.resolveActivity(Guide.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = Guide.this.createImageFile();
                    } catch (IOException e) {
                    }
                    if (photoFile != null) {
                        Guide guide = Guide.this;
                        Uri unused = guide.photoUri = FileProvider.getUriForFile(guide.getApplicationContext(), Guide.this.getPackageName(), photoFile);
                        intent.putExtra("output", Guide.this.photoUri);
                        Guide.this.startActivityForResult(intent, Guide.REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        findViewById(C0111R.C0114id.btn_reset).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Guide.this.startActivity(new Intent(Guide.this, MainActivity.class));
            }
        });
    }

    /* access modifiers changed from: private */
    public File createImageFile() throws IOException {
        File image = File.createTempFile("TEST_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        this.imageFilePath = image.getAbsolutePath();
        return image;
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int exifDegree;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == -1) {
            Bitmap bitmap = BitmapFactory.decodeFile(this.imageFilePath);
            ExifInterface exif = null;
            this.imgPath = saveBitmapToJpeg(this.context, rotateImage(bitmap, 270.0f), "tmp");
            System.out.println(this.imgPath);
            try {
                exif = new ExifInterface(this.imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exif != null) {
                exifDegree = exifOrientationToDegress(exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_EXIF_VERSION, 1));
            } else {
                exifDegree = 0;
            }
            String filename = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault()).format(new Date(System.currentTimeMillis()));
            String strFolderName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "HONGDROID" + File.separator;
            File file = new File(strFolderName);
            if (!file.exists()) {
                file.mkdirs();
            }
            File f = new File(strFolderName + "/" + filename + ".png");
            String path = f.getPath();
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }
            rotate(bitmap, (float) exifDegree).compress(Bitmap.CompressFormat.PNG, 1, fOut);
            try {
                fOut.flush();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            Intent intent = new Intent(this, LoadingActivity.class);
            intent.putExtra("Path", this.imgPath);
            intent.putExtra("name", filename);
            startActivity(intent);
        }
    }

    private int exifOrientationToDegress(int exifOrientation) {
        if (exifOrientation == 6) {
            return 90;
        }
        if (exifOrientation == 3) {
            return 180;
        }
        if (exifOrientation == 8) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /* access modifiers changed from: package-private */
    public String getRealPathFromUri(Uri uri) {
        Cursor cursor = new CursorLoader(this, uri, new String[]{"_data"}, (String) null, (String[]) null, (String) null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public static String saveBitmapToJpeg(Context context2, Bitmap bitmap, String name) {
        int realimagesize = 500000;
        int quality = 100;
        File tempFile = new File(context2.getCacheDir(), name + ".jpg");
        try {
            tempFile.createNewFile();
            while (realimagesize >= 500000) {
                if (quality < 0) {
                    return "tobig";
                }
                FileOutputStream out = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                realimagesize = (int) tempFile.length();
                quality -= 20;
                out.close();
            }
            System.out.println("imagelocation resizefilesize result: " + realimagesize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return tempFile.getAbsolutePath();
    }

    public Bitmap rotateImage(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
}
