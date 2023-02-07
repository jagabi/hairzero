package com.example.cameraexample;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.toolbox.Volley;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;

public class LoadingActivity extends AppCompatActivity {
    private int Check = -1;
    private String ImagePath = "";
    private int Steps = -1;
    private String WildImgPath = "";
    ProgressDialog customProgressDialog;
    private String imgPath = "";
    private String message = "";
    private String resizeName = "";

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0111R.layout.activity_loading);
        Intent intent = getIntent();
        this.imgPath = intent.getExtras().getString("Path");
        this.resizeName = intent.getExtras().getString("name");
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.customProgressDialog = progressDialog;
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.customProgressDialog.show();
        sendImage();
    }

    public void sendImage() {
        System.out.println(this.imgPath);
        SimpleMultiPartRequest smpr = new SimpleMultiPartRequest(1, "http://211.228.170.82/sub.php?jud=1 ", new Response.Listener<String>() {
            public void onResponse(String response) {
                new AlertDialog.Builder(LoadingActivity.this).setMessage((CharSequence) "응답:" + response).create().show();
                int JSONParse = LoadingActivity.this.JSONParse(response);
                LoadingActivity.this.getResult();
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoadingActivity.this, "ERROR", 0).show();
            }
        });
        smpr.addFile("img", this.imgPath);
        Volley.newRequestQueue(this).add(smpr);
    }

    public void getResult() {
        System.out.println(this.imgPath);
        Volley.newRequestQueue(this).add(new SimpleMultiPartRequest(1, "http://211.228.170.82/sub.php?jud=0&img=" + this.WildImgPath, new Response.Listener<String>() {
            public void onResponse(String response) {
                new AlertDialog.Builder(LoadingActivity.this).setMessage((CharSequence) "응답:" + response).create().show();
                if (LoadingActivity.this.JSONParse2(response) == 1) {
                    LoadingActivity.this.GotoResult();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoadingActivity.this, "ERROR", 0).show();
            }
        }));
    }

    public void checkFile() {
        System.out.println(((new File(this.imgPath).length() / 1024) / 1024) + " mb");
    }

    public void setMessage(String response) {
        this.message = response;
    }

    public void GotoResult() {
        int i = this.Steps;
        if (i == 0) {
            Intent intent = new Intent(this, C1135m1.class);
            intent.putExtra("ImgPath", this.ImagePath);
            startActivity(intent);
        } else if (i == 1) {
            Intent intent2 = new Intent(this, C1137m2.class);
            intent2.putExtra("ImgPath", this.ImagePath);
            startActivity(intent2);
        } else if (i == 2) {
            Intent intent3 = new Intent(this, C1139m3.class);
            intent3.putExtra("ImgPath", this.ImagePath);
            startActivity(intent3);
        } else {
            Toast.makeText(this, "ERROR", 0).show();
        }
    }

    public int JSONParse(String jsonStr) {
        new StringBuilder();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int tmpCheck = jsonObject.getInt("result");
            System.out.println(tmpCheck);
            String tmpImagePath = jsonObject.getString("src");
            System.out.println(tmpImagePath);
            this.Check = tmpCheck;
            System.out.println(this.Check);
            this.WildImgPath = tmpImagePath.substring(7) + ".jpg";
            System.out.println(this.WildImgPath);
            this.ImagePath = "http://211.228.170.82" + tmpImagePath + ".jpg";
            System.out.println(this.ImagePath);
            return 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int JSONParse2(String jsonStr) {
        new StringBuilder();
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            int tmpCheck = jsonObject.getInt("result");
            System.out.println(tmpCheck);
            String tmpImagePath = jsonObject.getString("src");
            System.out.println(tmpImagePath);
            this.Steps = tmpCheck;
            System.out.println(this.Steps);
            this.ImagePath = "http://211.228.170.82" + tmpImagePath;
            System.out.println(this.ImagePath);
            return 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
