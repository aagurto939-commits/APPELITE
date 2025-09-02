package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ocultar barra de estado y navegación para pantalla completa
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ImageView logoIcon = findViewById(R.id.logoIcon);
        TextView appName = findViewById(R.id.appName);
        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        TextView haveAccount = findViewById(R.id.haveAccount);

        // Animaciones
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        
        logoIcon.startAnimation(fadeIn);
        appName.startAnimation(slideUp);

        // Mostrar botón después de 2 segundos
        new Handler().postDelayed(() -> {
            btnGetStarted.setVisibility(View.VISIBLE);
            haveAccount.setVisibility(View.VISIBLE);
            btnGetStarted.startAnimation(fadeIn);
            haveAccount.startAnimation(fadeIn);
        }, 2000);

        // Click en Get Started
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Click en "I already have an account"
        haveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }
}
