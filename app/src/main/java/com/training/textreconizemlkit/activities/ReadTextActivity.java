package com.training.textreconizemlkit.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
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
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;
import com.training.textreconizemlkit.dialog.CustomDialogTranslate;
import com.training.textreconizemlkit.handle.CopyHandler;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReadTextActivity extends AppCompatActivity {

    ActivityReadTextBinding binding;

    TextRecognizer recognizer;
    Bitmap imageBitmap;
    Gson gson = new Gson();
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    CustomDialogTranslate mCustomDialogTranslate;
    String fromLanguage = "";
    String toLanguage   = "";
    TranslatorOptions options;
    final CopyHandler copyHandler = new CopyHandler(this);
    Translator modelTranslator;
    SweetAlertDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReadTextBinding inflate = ActivityReadTextBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());

        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        setupProcessDialog();
        setBottomSheetBehavior();
        initData();
        try {
            initView();
        } catch (IOException e) {
            Toast.makeText(this, "something is wrong, error: " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        initBehavior();
        setOneClick();
    }

    private void initBehavior() {
        binding.bottomSheet.tvTextTranslate.setMovementMethod(new ScrollingMovementMethod());
        binding.bottomSheet.tvTextTranslate.setMovementMethod(new ScrollingMovementMethod());
    }


    private void initData() {
        mCustomDialogTranslate = new CustomDialogTranslate(ReadTextActivity.this);
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
                readTextOnPhoto();
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

        binding.bottomSheet.imgIcTranslate.setOnClickListener(v -> {
            mCustomDialogTranslate.show();
        });

        mCustomDialogTranslate.binding.btnConfirm.setOnClickListener(v -> {
            createModelTranslate();
            pDialog.show();
            startTranslate();
            mCustomDialogTranslate.dismiss();
        });

        mCustomDialogTranslate.binding.btnNo.setOnClickListener(v -> {
            mCustomDialogTranslate.dismiss();
        });

        mCustomDialogTranslate.binding.spnLanguageTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguage = mCustomDialogTranslate.languages.get(position).languageCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCustomDialogTranslate.binding.spnLanguageFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               fromLanguage = mCustomDialogTranslate.languages.get(position).languageCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupProcessDialog(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Downloading model translate");
        pDialog.setCancelable(false);
    }

    private void createModelTranslate(){
        options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(fromLanguage)
                        .setTargetLanguage(toLanguage)
                        .build();
        modelTranslator =  Translation.getClient(options);
    }

    private void startTranslate(){
        pDialog.show();
        modelTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                Toast.makeText(ReadTextActivity.this, "downloadMode success", Toast.LENGTH_SHORT).show();
                                translateText();
                                pDialog.dismiss();
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ReadTextActivity.this, "downloadMode false", Toast.LENGTH_SHORT).show();
                                Log.d("downloadMode", e.toString());
                                pDialog.dismiss();
                            }
                        });
    }

    private void translateText(){
        pDialog.setTitleText("Language coverting ....");
        pDialog.show();
        String text = binding.bottomSheet.tvTextResult.getText().toString();
        modelTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                pDialog.dismiss();
                                binding.bottomSheet.tvTextTranslate.setText(o.toString());
                                binding.bottomSheet.tvTextTranslate.setVisibility(View.VISIBLE);
                                Toast.makeText(ReadTextActivity.this, "Translate success", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pDialog.dismiss();
                                Toast.makeText(ReadTextActivity.this, "Translate fail", Toast.LENGTH_SHORT).show();
                            }
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

    private void readTextOnPhoto() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        Toast.makeText(ReadTextActivity.this, "Recognizer success", Toast.LENGTH_SHORT).show();
                        getResultTextRecognize(firebaseVisionText);
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

    private void getResultTextRecognize(FirebaseVisionText result) {
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
