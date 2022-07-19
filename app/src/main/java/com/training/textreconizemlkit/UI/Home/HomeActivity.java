package com.training.textreconizemlkit.UI.Home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.training.textreconizemlkit.Common;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.UI.ReadText.ReadTextActivity;
import com.training.textreconizemlkit.dialog.DetailsDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    LinearLayout lnCamera, lnAddPhoto;
    public static Context contextApp;
    String currentPhotoPath;
    public int REQUEST_CAPTURE_IMAGE = 1;
    public int REQUEST_GET_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        contextApp = this.getApplicationContext();

        initView();
        setOnclick();
    }

    private void setOnclick() {
        lnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(HomeActivity.this).withPermissions("android.permission.CAMERA")
                        .withListener(new MultiplePermissionsListener() {
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                    takePhotoOnCamera();
                                }
                                if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                    DetailsDialog.showDetailsDialog(HomeActivity.this);
                                }
                            }

                            public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).withErrorListener(dexterError -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
            }
        });

        lnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditPhotoScreen();
            }
        });
    }

    private void initView() {
        lnCamera = findViewById(R.id.ln_take_photo);
        lnAddPhoto = findViewById(R.id.ln_add_photo);
    }

    public void onActivityResult(int i, int i2, Intent data) {
        Common.isCameraCancel = true;
        if (i2 != -1) {
            super.onActivityResult(i, i2, data);
        } else if (i == REQUEST_CAPTURE_IMAGE) {
            Intent intent2 = new Intent(HomeActivity.this, ReadTextActivity.class);
            intent2.putExtra("image_path", currentPhotoPath);
            startActivity(intent2);
        } else if(i == REQUEST_GET_IMAGE){
            Intent intent2 = new Intent(HomeActivity.this, ReadTextActivity.class);

            Uri uri = data.getData();
            currentPhotoPath = uri.toString();
            intent2.putExtra("image_path", currentPhotoPath);
            startActivity(intent2);
        }
    }

    public void takePhotoOnCamera() {
        dispatchTakePictureIntent();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        this.getApplicationInfo().packageName + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    private void goToEditPhotoScreen() {
        Dexter.withContext(HomeActivity.this).withPermissions("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE").withListener(new MultiplePermissionsListener() {
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                Intent intent= new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Title"), REQUEST_GET_IMAGE);
            }

            public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).withErrorListener(dexterError -> Toast.makeText(HomeActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
    }

    public static Context getAppContext() {
        return contextApp;
    }
}
