package com.training.textreconizemlkit.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;
import com.training.textreconizemlkit.handle.CopyHandler;

import java.io.IOException;

public class ReadTextActivity extends AppCompatActivity {

    ActivityReadTextBinding binding;

    TextRecognizer recognizer;
    Bitmap imageBitmap;
    Gson gson = new Gson();
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    final CopyHandler copyHandler = new CopyHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReadTextBinding inflate = ActivityReadTextBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        setBottomSheetBehavior();
        try {
            initView();
        } catch (IOException e) {
            Toast.makeText(this, "something is wrong, error: " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        setOneClick();
    }

    private void setBottomSheetBehavior() {
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void setOneClick() {
        binding.rlReadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTextOnphoto();
            }
        });

        binding.turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imageBitmap = rotatedBitmap;
                binding.imPhotoView.setImageBitmap(rotatedBitmap);
            }
        });

        binding.turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imageBitmap = rotatedBitmap;
                binding.imPhotoView.setImageBitmap(rotatedBitmap);
            }
        });

        binding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadTextActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        binding.bottomSheet.icCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        binding.bottomSheet.imgIcCopy.setOnClickListener(v -> {
            copyHandler.copyText(binding.bottomSheet.tvTextResult.getText().toString(), this);
        });

        binding.bottomSheet.imgIcShare.setOnClickListener(v -> {
            copyHandler.shareText(binding.bottomSheet.tvTextResult.getText().toString());
        });
    }

    private void initView() throws IOException {
        imageBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        if (imageBitmap == null) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("image_path"));
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }
        binding.imPhotoView.setImageBitmap(imageBitmap);

        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void readTextOnphoto() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        Toast.makeText(ReadTextActivity.this, "Recognizer success", Toast.LENGTH_SHORT).show();
                        getResult(firebaseVisionText);
                        Log.d("AAAAA", gson.toJson(firebaseVisionText));
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ReadTextActivity.this, "Recognizer Fail", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void getResult(FirebaseVisionText result) {
        String[] resultTextMuntipleLine = {""};
        String resultTextOneLine ="";
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            resultTextMuntipleLine[0] = resultTextMuntipleLine[0] + "\n \n" + blockText;
            resultTextOneLine = resultTextOneLine + block;
        }
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        binding.bottomSheet.tvTextResult.setText(resultTextMuntipleLine[0]);
    }
}
