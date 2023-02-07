package com.example.cameraexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

/* renamed from: com.example.cameraexample.m2 */
public class C1137m2 extends AppCompatActivity {
    private String ImagePath = "";
    ImageView imageView;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_good);
        this.ImagePath = getIntent().getExtras().getString("ImgPath");
        this.imageView = (ImageView) findViewById(C0111R.C0114id.imageView3);
        sendImageRequest();
        findViewById(C0111R.C0114id.btn_reset).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                C1137m2.this.startActivity(new Intent(C1137m2.this, MainActivity.class));
            }
        });
    }

    public void sendImageRequest() {
        new ImageLoadTask(this.ImagePath, this.imageView).execute((Params[]) new Void[0]);
    }
}
