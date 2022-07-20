package com.training.textreconizemlkit.UI.ReadText;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class ReadTextPresenter {

    ActionWithText actionWithText;
    Context context;

    TranslatorOptions options;
    Translator modelTranslator;
    public String fromLanguage = "";
    public String toLanguage = "";

    public ReadTextPresenter(ActionWithText actionWithText, Context context) {
        this.actionWithText = actionWithText;
        this.context = context;
    }

    public void readTextOnPhoto(Bitmap imageBitmap) {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        actionWithText.OnReadTextSuccess(firebaseVisionText);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                actionWithText.OnReadTextFail();
                            }
                        });
    }

    public Bitmap turnLeft(Bitmap imageBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        imageBitmap = rotatedBitmap;
        return imageBitmap;
    }

    public Bitmap turnRight(Bitmap imageBitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, imageBitmap.getWidth(), imageBitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        imageBitmap = rotatedBitmap;
        return imageBitmap;
    }

    public void createModelTranslate() {
        options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(fromLanguage)
                        .setTargetLanguage(toLanguage)
                        .build();
        modelTranslator = Translation.getClient(options);
    }

    public void startTranslate(String text) {
        actionWithText.OpenDialogLoadingLanguage();
        modelTranslator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                translateText(text);
                                actionWithText.HideDialogLoadingLanguage();
                            }

                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("downloadMode", e.toString());
                                actionWithText.HideDialogLoadingLanguage();
                            }
                        });
    }

    public void translateText(String text) {
        actionWithText.OpenDialogConvertLanguage();
        modelTranslator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                actionWithText.HideDialogLoadingLanguage();
                                actionWithText.OnTranslateSuccess(o.toString());
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                actionWithText.HideDialogLoadingLanguage();
                            }
                        });
    }
}
