package com.example.quizapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // Toolbar with back arrow
        setSupportActionBar(b.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        b.toolbar.setNavigationOnClickListener(v -> finish());

        // TODO: populate your profile fields here
    }
}
