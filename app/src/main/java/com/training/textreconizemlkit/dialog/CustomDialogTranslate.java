package com.training.textreconizemlkit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import com.training.textreconizemlkit.R;

public class CustomDialogTranslate extends Dialog {
    public Activity activity;
    public Button btnNo, btnYes;

    public CustomDialogTranslate(Activity activity) {
        super(activity, R.style.CustomDialogAddShortCut);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_translate);

        btnNo = findViewById(R.id.btn_no);
        btnYes = findViewById(R.id.btn_yes);
    }
}
