package com.example.cameraexample;

import android.app.Dialog;
import android.content.Context;

public class ProgressDialog extends Dialog {
    public ProgressDialog(Context context) {
        super(context);
        requestWindowFeature(1);
        setContentView(C0111R.layout.dialog_progress);
    }
}
