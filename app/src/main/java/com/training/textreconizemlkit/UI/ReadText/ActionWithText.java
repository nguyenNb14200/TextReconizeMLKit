package com.training.textreconizemlkit.UI.ReadText;

public interface ActionWithText {
    void OpenDialogLoadingLanguage();
    void HideDialogLoadingLanguage();
    void OpenDialogConvertLanguage();
    void OnTranslateSuccess(String text);
    void OnTranslateFail(String text);
    void OnReadTextFail();
    void OnReadTextSuccess(String text);
}
