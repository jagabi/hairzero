package com.example.cameraexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class Guide2 extends AppCompatActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_guide2);
        findViewById(C0111R.C0114id.btn_reset).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Guide2.this.startActivity(new Intent(Guide2.this, MainActivity.class));
            }
        });
    }
}
