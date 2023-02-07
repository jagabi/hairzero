package com.example.cameraexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

/* renamed from: com.example.cameraexample.m3 */
public class C1139m3 extends AppCompatActivity {
    private String ImagePath = "";
    ImageView imageView;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_good);
        this.ImagePath = getIntent().getExtras().getString("ImgPath");
        this.imageView = (ImageView) findViewById(C0111R.C0114id.imageView4);
        sendImageRequest();
        findViewById(C0111R.C0114id.btn_reset).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                C1139m3.this.startActivity(new Intent(C1139m3.this, MainActivity.class));
            }
        });
    }

    public void sendImageRequest() {
        new ImageLoadTask(this.ImagePath, this.imageView).execute((Params[]) new Void[0]);
    }
}
