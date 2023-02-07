package com.example.cameraexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private Button btn_guide;
    private String imageFilePath;
    private MediaScanner mMediaScanner;
    PermissionListener permissionListener = new PermissionListener() {
        public void onPermissionGranted() {
            Toast.makeText(MainActivity.this.getApplicationContext(), "권한이 허용됨", 0).show();
        }

        public void onPermissionDenied(List<String> list) {
            Toast.makeText(MainActivity.this.getApplicationContext(), "권한이 거부됨", 0).show();
        }
    };
    private Uri photoUri;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_main);
        ((TedPermission.Builder) ((TedPermission.Builder) ((TedPermission.Builder) ((TedPermission.Builder) TedPermission.with(getApplicationContext()).setPermissionListener(this.permissionListener)).setRationaleMessage((CharSequence) "카메라 권한이 필요합니다.")).setDeniedMessage((CharSequence) "거부하셨습니다.")).setPermissions("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.CAMERA")).check();
        findViewById(C0111R.C0114id.btn_start).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, Guide.class));
            }
        });
        findViewById(C0111R.C0114id.btn_guide).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, Guide2.class));
            }
        });
    }
}
