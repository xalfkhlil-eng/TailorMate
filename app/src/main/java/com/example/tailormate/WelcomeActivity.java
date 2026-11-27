package com.example.tailormate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/** Screen 1: Welcome / splash screen (matches the Stitch prototype). */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // "Enter Atelier Dashboard" -> opens the Dashboard
        Button btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, DashboardActivity.class));
            finish(); // the back button must not return to the welcome screen
        });
    }
}
