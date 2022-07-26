package com.training.textreconizemlkit.UI.ReadText;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

public interface ActionWithText {
    void OpenDialogLoadingLanguage();
    void HideDialogLoadingLanguage();
    void OpenDialogConvertLanguage();
    void OnTranslateSuccess(String text);
    void OnTranslateFail(String text);
    void OnReadTextFail();
    void OnReadTextSuccess(FirebaseVisionText firebaseVisionText);
    void OnIdentifyLanguageSuccess(String language);
    void OnIdentifyLanguageFail();
}
