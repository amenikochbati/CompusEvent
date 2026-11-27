package com.example.compusevent;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        NavigationHelper.setup(this, "À propos", "Whats'Happen");

        MaterialButton btnHome = findViewById(R.id.btn_about_home);
        btnHome.setOnClickListener(v -> NavigationHelper.goHome(this));
    }
}
