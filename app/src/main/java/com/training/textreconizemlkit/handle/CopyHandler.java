package com.training.textreconizemlkit.handle;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.training.textreconizemlkit.R;


public class CopyHandler {
    Context context;

    public CopyHandler(Context context) {
        this.context = context;
    }

    public void copyText(String data, Activity activity){
        if (data.isEmpty()) {
            Toast.makeText(context, "Not has any text here", Toast.LENGTH_LONG).show();
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        Toast.makeText(context, context.getString(R.string.copy), Toast.LENGTH_LONG).show();
        ClipData clip = ClipData.newPlainText("simple text", data);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
        }
    }
    public void shareText(String data) {
        if (data.isEmpty()) {
            Toast.makeText(context, "Not has any text here", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, data);
            context.startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }
    public void copyTextAnother(String data, Activity activity){
        if (data.isEmpty()) {
            Toast.makeText(context, "Not has any text here", Toast.LENGTH_LONG).show();
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        Toast.makeText(context, context.getString(R.string.copy), Toast.LENGTH_LONG).show();
        ClipData clip = ClipData.newPlainText("simple text", data);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clip);
        }
    }
}
