package com.training.textreconizemlkit.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.google.mlkit.nl.translate.TranslateLanguage;
import com.training.textreconizemlkit.R;
import com.training.textreconizemlkit.data.Language;
import com.training.textreconizemlkit.databinding.DialogTranslateBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomDialogTranslate extends Dialog {
    public Activity activity;
    public DialogTranslateBinding binding = DialogTranslateBinding.inflate(getLayoutInflater());
    List<String> fromLanguageCode = new ArrayList<String>();
    List<String> toLanguageCode = new ArrayList<String>();
    List<String> fromLanguageName = new ArrayList<String>();
    List<String> toLanguageName = new ArrayList<String>();
    public List<Language> languages = new ArrayList<Language>();

    public CustomDialogTranslate(Activity activity) {
        super(activity, R.style.CustomDialogAddShortCut);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(binding.getRoot());
        initData();
        initSpiner();
    }

    private void initData() {
        fromLanguageCode = TranslateLanguage.getAllLanguages();
        for (int i = 0; i < fromLanguageCode.size(); i++) {
            Locale loc = new Locale(fromLanguageCode.get(i));
            String name = loc.getDisplayLanguage(loc);
            if(name == null) continue;
            fromLanguageName.add(name);
        }

        toLanguageCode   = TranslateLanguage.getAllLanguages();
        for (int i = 0; i < toLanguageCode.size(); i++) {
            Locale loc = new Locale(toLanguageCode.get(i));
            String name = loc.getDisplayLanguage(loc);
            if(name == null) continue;
            toLanguageName.add(name);
        }

        for (int i = 0; i < toLanguageCode.size(); i++) {
            languages.add(new Language(toLanguageName.get(i), toLanguageCode.get(i)));
        }
    }

    private void initSpiner() {
        ArrayAdapter<String> spinnerFromArrayAdapter = new ArrayAdapter<String>
                (activity, android.R.layout.simple_spinner_item,
                        fromLanguageName);
        spinnerFromArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        binding.spnLanguageFrom.setAdapter(spinnerFromArrayAdapter);

        ArrayAdapter<String> spinnerToArrayAdapter = new ArrayAdapter<String>
                (activity, android.R.layout.simple_spinner_item,
                        fromLanguageName);
        spinnerToArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        binding.spnLanguageTo.setAdapter(spinnerToArrayAdapter);
    }
}
