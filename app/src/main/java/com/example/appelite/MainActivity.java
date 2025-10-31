package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText txtEmail, txtPass;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
                
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        initViews();
        setupEvents();
    }

    private void initViews() {
        txtEmail = findViewById(R.id.txtEmailLogin);
        txtPass = findViewById(R.id.txtPassLogin);
        btnLogin = findViewById(R.id.btnLogin);
    tvRegister = findViewById(R.id.lblRegistrate);
    }

    private void setupEvents() {
        // Botón de login
        btnLogin.setOnClickListener(v -> attemptLogin());
        // Ir a registro
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = txtEmail.getText().toString().trim();
        String password = txtPass.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(email)) {
            txtEmail.setError("Email requerido");
            txtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            txtPass.setError("Contraseña requerida");
            txtPass.requestFocus();
            return;
        }

        if (password.length() < 6) {
            txtPass.setError("Contraseña debe tener al menos 6 caracteres");
            txtPass.requestFocus();
            return;
        }

        // Mostrar loading
        btnLogin.setText("Iniciando...");
        btnLogin.setEnabled(false);

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnLogin.setText("Iniciar Sesión");
                        btnLogin.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            // Login exitoso
                            Toast.makeText(MainActivity.this, "Bienvenido!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, PantallaInicioModernaActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Error en login - Manejo mejorado
                            String errorMessage = "Error de conexión";
                            if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("network") || exceptionMessage.contains("timeout")) {
                                        errorMessage = "Sin conexión a internet. Verifica tu red.";
                                    } else if (exceptionMessage.contains("INVALID_LOGIN_CREDENTIALS")) {
                                        errorMessage = "Email o contraseña incorrectos";
                                    } else if (exceptionMessage.contains("USER_NOT_FOUND")) {
                                        errorMessage = "Usuario no encontrado";
                                    } else {
                                        errorMessage = exceptionMessage;
                                    }
                                }
                            }
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setText("Iniciar Sesión");
                    btnLogin.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}