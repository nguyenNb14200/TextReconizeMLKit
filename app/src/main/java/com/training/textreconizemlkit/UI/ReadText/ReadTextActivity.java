package com.training.textreconizemlkit.UI.ReadText;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.gson.Gson;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.UI.Home.HomeActivity;
import com.training.textreconizemlkit.Units.CropImage;
import com.training.textreconizemlkit.Units.GraphicOverlay;
import com.training.textreconizemlkit.Units.TextGraphic;
import com.training.textreconizemlkit.databinding.ActivityReadTextBinding;
import com.training.textreconizemlkit.dialog.CustomDialogTranslate;
import com.training.textreconizemlkit.handle.CopyHandler;

import java.io.IOException;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReadTextActivity extends AppCompatActivity implements ActionWithText {

    ActivityReadTextBinding binding;

    TextRecognizer recognizer;
    Bitmap imageBitmapPrimary;
    Bitmap imageBitmapCrop;
    Gson gson = new Gson();
    LinearLayout layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;
    CustomDialogTranslate mCustomDialogTranslate;
    final CopyHandler copyHandler = new CopyHandler(this);
    SweetAlertDialog pDialog;
    ReadTextPresenter mReadTextPresenter = new ReadTextPresenter(this, this);
    Boolean isReadText = true;
    Uri srouceUri;
    CropImage cropImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityReadTextBinding inflate = ActivityReadTextBinding.inflate(getLayoutInflater());
        this.binding = inflate;
        setContentView(inflate.getRoot());
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        cropImage = findViewById(R.id.im_crop_view);
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
                mReadTextPresenter.readTextOnPhoto(imageBitmapCrop);
                binding.graphicOverlay.setVisibility(View.VISIBLE);
            }
        });

        binding.turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageBitmapCrop = mReadTextPresenter.turnLeft(imageBitmapCrop);
                binding.imPhotoView.setImageBitmap(imageBitmapCrop);
            }
        });

        binding.turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageBitmapCrop = mReadTextPresenter.turnRight(imageBitmapCrop);
                binding.imPhotoView.setImageBitmap(imageBitmapCrop);
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

        binding.llCrop.setOnClickListener(v -> {
            cropImage.setVisibility(View.VISIBLE);
            cropImage.setImageBitmap(imageBitmapCrop);
            binding.icSave.setVisibility(View.VISIBLE);
            binding.imPhotoView.setVisibility(View.GONE);
            binding.graphicOverlay.setVisibility(View.GONE);
            calculateRectOnScreen(binding.imPhotoView);
        });

        binding.bottomSheet.imgIdentifyLanguage.setOnClickListener(v -> {
            mReadTextPresenter.IdentifyLanguage(binding.bottomSheet.tvTextResult.getText().toString());
        });

        binding.llScan.setOnClickListener(v -> {
            isReadText = false;
            mReadTextPresenter.readTextOnPhoto(imageBitmapCrop);
            binding.graphicOverlay.setVisibility(View.VISIBLE);
        });

        binding.icSave.setOnClickListener(v -> {
            imageBitmapCrop = cropImage.getImageBitmap();
            binding.icSave.setVisibility(View.GONE);
        });

        binding.llRefresh.setOnClickListener(v -> {
            binding.imPhotoView.setImageBitmap(imageBitmapPrimary);
            imageBitmapCrop = imageBitmapPrimary;
            binding.imPhotoView.setVisibility(View.VISIBLE);
            cropImage.setVisibility(View.GONE);
            cropImage.clearPaint();
            binding.graphicOverlay.setVisibility(View.GONE);
        });
    }

    private void setupProcessDialog() {
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Downloading model translate");
        pDialog.setCancelable(false);
    }

    private void initView() throws IOException {
        srouceUri = Uri.parse(getIntent().getStringExtra("image_path"));
        imageBitmapPrimary = BitmapFactory.decodeFile(getIntent().getStringExtra("image_path"));
        if (imageBitmapPrimary == null) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("image_path"));
            imageBitmapPrimary = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }
        binding.imPhotoView.setImageBitmap(imageBitmapPrimary);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageBitmapPrimary = createScaleFactorUsingBitmap(imageBitmapPrimary);
                binding.imPhotoView.setImageBitmap(imageBitmapPrimary);
                imageBitmapCrop = imageBitmapPrimary;
            }
        }, 500);
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
        if(isReadText){
            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                String blockText = block.getText();
                resultTextMuntipleLine[0] = resultTextMuntipleLine[0] + "\n" + blockText;
                resultTextOneLine = resultTextOneLine + block;
            }
            calculateRectOnScreen(binding.imPhotoView);
            Toast.makeText(ReadTextActivity.this, "Recognizer success", Toast.LENGTH_SHORT).show();
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            binding.bottomSheet.tvTextResult.setText(resultTextMuntipleLine[0]);
        } else {
            calculateRectOnScreen(binding.imPhotoView);
            Toast.makeText(this, "scan text success", Toast.LENGTH_SHORT).show();
        }
        isReadText = true;
    }

    @Override
    public void OnIdentifyLanguageSuccess(String language) {
        Locale loc = new Locale(language);
        String name = loc.getDisplayLanguage(loc);
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
    }

    @Override
    public void OnIdentifyLanguageFail() {
        Toast.makeText(this, "Identify Language Fail", Toast.LENGTH_LONG).show();
    }

    public void calculateRectOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(location[0], 0, 0, 0);
        binding.rlDrawBlock.setLayoutParams(lp);
    }
}
