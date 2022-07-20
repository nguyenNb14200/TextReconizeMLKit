package com.training.textreconizemlkit.UI.ReadText;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.gson.Gson;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.UI.Home.HomeActivity;
import com.training.textreconizemlkit.Units.GraphicOverlay;
import com.training.textreconizemlkit.Units.TextGraphic;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;
import com.training.textreconizemlkit.dialog.CustomDialogTranslate;
import com.training.textreconizemlkit.handle.CopyHandler;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReadTextActivity extends AppCompatActivity implements ActionWithText {

    ActivityReadTextBinding binding;

    TextRecognizer recognizer;
    Bitmap imageBitmap;
    Gson gson = new Gson();
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    CustomDialogTranslate mCustomDialogTranslate;
    final CopyHandler copyHandler = new CopyHandler(this);
    SweetAlertDialog pDialog;
    ReadTextPresenter mReadTextPresenter = new ReadTextPresenter(this, this);

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
        initData();
        try {
            initView();
        } catch (IOException e) {
            Toast.makeText(this, "something is wrong, error: " + e, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        initBehavior();
        setOneClick();
    }

    private void initBehavior() {
        binding.bottomSheet.tvTextTranslate.setMovementMethod(new ScrollingMovementMethod());
        binding.bottomSheet.tvTextResult.setMovementMethod(new ScrollingMovementMethod());
    }

    private Bitmap createScaleFactorUsingBitmap(Bitmap mSelectedImage) {
        // Determine how much to scale down the image
        float scaleFactor =
                Math.max(
                        (float) mSelectedImage.getWidth() / (float) binding.imPhotoView.getWidth(),
                        (float) mSelectedImage.getHeight() / (float) binding.imPhotoView.getHeight());

        Bitmap resizedBitmap =
                Bitmap.createScaledBitmap(
                        mSelectedImage,
                        (int) (mSelectedImage.getWidth() / scaleFactor),
                        (int) (mSelectedImage.getHeight() / scaleFactor),
                        true);

        return resizedBitmap;
    }

    private void initData() {
        mCustomDialogTranslate = new CustomDialogTranslate(ReadTextActivity.this);
    }

    private void setOneClick() {
        binding.rlReadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReadTextPresenter.readTextOnPhoto(imageBitmap);
            }
        });

        binding.turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageBitmap = mReadTextPresenter.turnLeft(imageBitmap);
                binding.imPhotoView.setImageBitmap(imageBitmap);
            }
        });

        binding.turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageBitmap = mReadTextPresenter.turnRight(imageBitmap);
                binding.imPhotoView.setImageBitmap(imageBitmap);
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
            String text = binding.bottomSheet.tvTextResult.getText().toString();
            mReadTextPresenter.createModelTranslate();
            pDialog.show();
            mReadTextPresenter.startTranslate(text);
            mCustomDialogTranslate.dismiss();
        });

        mCustomDialogTranslate.binding.btnNo.setOnClickListener(v -> {
            mCustomDialogTranslate.dismiss();
        });

        mCustomDialogTranslate.binding.spnLanguageTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mReadTextPresenter.toLanguage = mCustomDialogTranslate.languages.get(position).languageCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mCustomDialogTranslate.binding.spnLanguageFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mReadTextPresenter.fromLanguage = mCustomDialogTranslate.languages.get(position).languageCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupProcessDialog() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Downloading model translate");
        pDialog.setCancelable(false);
    }

    private void initView() throws IOException {
        imageBitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        if (imageBitmap == null) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("image_path"));
            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }
        binding.imPhotoView.setImageBitmap(imageBitmap);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageBitmap = createScaleFactorUsingBitmap(imageBitmap);
                binding.imPhotoView.setImageBitmap(imageBitmap);
            }
        }, 1000);
//        binding.imPhotoView.setImageBitmap(imageBitmap);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void OpenDialogLoadingLanguage() {
        pDialog.show();
    }

    @Override
    public void HideDialogLoadingLanguage() {
        pDialog.dismiss();
    }

    @Override
    public void OpenDialogConvertLanguage() {
        pDialog.setTitleText("Language coverting ....");
        pDialog.show();
    }

    @Override
    public void OnTranslateSuccess(String text) {
        pDialog.dismiss();
        binding.bottomSheet.tvTextTranslate.setText(text);
        binding.bottomSheet.tvTextTranslate.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnTranslateFail(String text) {
        pDialog.dismiss();
    }

    @Override
    public void OnReadTextFail() {
        Toast.makeText(ReadTextActivity.this, "Recognizer Fail", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnReadTextSuccess(FirebaseVisionText result) {

        String[] resultTextMuntipleLine = {""};
        String resultTextOneLine = "";

        //draw line text
        binding.graphicOverlay.clear();
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                for (FirebaseVisionText.Element element : line.getElements()) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(binding.graphicOverlay, element);
                    binding.graphicOverlay.add(textGraphic);
                }
            }
        }
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            resultTextMuntipleLine[0] = resultTextMuntipleLine[0] + "\n \n" + blockText;
            resultTextOneLine = resultTextOneLine + block;
        }
        RectF margin = calculateRectOnScreen(binding.imPhotoView);
        Toast.makeText(ReadTextActivity.this, "Recognizer success", Toast.LENGTH_SHORT).show();
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        binding.bottomSheet.tvTextResult.setText(resultTextMuntipleLine[0]);
    }

    public  RectF calculateRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(location[0], 0, 0, 0);
        binding.rlDrawBlock.setLayoutParams(lp);
        return new RectF(location[0], location[1], location[0] + view.getMeasuredWidth(), location[1] + view.getMeasuredHeight());
    }
}
