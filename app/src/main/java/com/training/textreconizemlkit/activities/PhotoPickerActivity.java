package com.training.textreconizemlkit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.training.textreconizemlkit.databinding.ActivitiPhotoPickerBinding;

public class PhotoPickerActivity extends AppCompatActivity {

    ActivitiPhotoPickerBinding binding = ActivitiPhotoPickerBinding.inflate(getLayoutInflater());

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());



    }
}
