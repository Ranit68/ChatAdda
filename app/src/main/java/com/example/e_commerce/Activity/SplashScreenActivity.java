package com.example.e_commerce.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.e_commerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);



        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setIndeterminate(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                // If user is already signed in, go to MainActivity
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
            } else {
                // If user is not signed in, go to SignUpActivity
                startActivity(new Intent(SplashScreenActivity.this, SignUpActivity.class));
            }
            finish();
        }, 3000);
    }
}
