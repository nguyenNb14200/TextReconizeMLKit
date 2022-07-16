package com.training.textreconizemlkit.dialog;

import android.app.Activity;
import android.app.AlertDialog;

public class DetailsDialog {
    public static void showDetailsDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GO SETTINGS", (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        builder.show();
    }
}