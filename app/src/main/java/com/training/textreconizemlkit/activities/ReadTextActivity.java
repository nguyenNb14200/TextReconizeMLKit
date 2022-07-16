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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;

import java.io.IOException;

public class ReadTextActivity extends AppCompatActivity {

    ActivityReadTextBinding readTextBinding;

    ImageView imaCapture;
    TextRecognizer recognizer;
    Bitmap imageBitmap;
    int bitmapWidth;
    int bitmapHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReadTextBinding inflate = ActivityReadTextBinding.inflate(getLayoutInflater());
        this.readTextBinding = inflate;
        setContentView(inflate.getRoot());
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        try {
            initView();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setOneClick();
    }

    private void setOneClick() {
        readTextBinding.rlReadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTextOnphoto();
            }
        });

        readTextBinding.turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imageBitmap = rotatedBitmap;
                readTextBinding.imPhotoView.setImageBitmap(rotatedBitmap);
            }
        });

        readTextBinding.turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                imageBitmap = rotatedBitmap;
                readTextBinding.imPhotoView.setImageBitmap(rotatedBitmap);
            }
        });

        readTextBinding.icBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadTextActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() throws IOException {
        imageBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        if (imageBitmap == null) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("image_path"));
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }
        readTextBinding.imPhotoView.setImageBitmap(imageBitmap);

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
        String[] resultText = {""};
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            resultText[0] = resultText[0] + "\n \n" + blockText;

        }
        readTextBinding.tvTextResult.setText(resultText[0]);
        readTextBinding.srvTextResult.setVisibility(View.VISIBLE);
        readTextBinding.rlReadText.setVisibility(View.GONE);
    }
}
