package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
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
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
                
        scrollView = findViewById(R.id.main);
        
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomInset = Math.max(systemBars.bottom, imeInsets.bottom);
            
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            
            if (scrollView != null) {
                scrollView.setPadding(
                    scrollView.getPaddingLeft(),
                    scrollView.getPaddingTop(),
                    scrollView.getPaddingRight(),
                    bottomInset);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(scrollView);

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

        // Verificar conexión a internet primero
        android.util.Log.d("MainActivity", "Intentando login con: " + email);
        
        // Agregar timeout manual (30 segundos)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!btnLogin.isEnabled()) {
                // Si después de 30 segundos el botón sigue deshabilitado, hubo un problema
                android.util.Log.e("MainActivity", "Timeout en login después de 30 segundos");
                btnLogin.setText("Iniciar Sesión");
                btnLogin.setEnabled(true);
                Toast.makeText(MainActivity.this, "Tiempo de espera agotado. Verifica tu conexión a internet.", Toast.LENGTH_LONG).show();
            }
        }, 30000); // 30 segundos
        
        // Agregar timeout y mejor manejo de errores
        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        // Verificar si ya hay un usuario logueado
        if (auth.getCurrentUser() != null) {
            android.util.Log.d("MainActivity", "Usuario ya autenticado, redirigiendo...");
            Intent intent = new Intent(MainActivity.this, PantallaInicioModernaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        android.util.Log.d("MainActivity", "onComplete llamado, isSuccessful: " + task.isSuccessful());
                        
                        btnLogin.setText("Iniciar Sesión");
                        btnLogin.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            // Login exitoso
                            android.util.Log.d("MainActivity", "Login exitoso, redirigiendo...");
                            Toast.makeText(MainActivity.this, "Bienvenido!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, PantallaInicioModernaActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Error en login - Manejo mejorado
                            android.util.Log.e("MainActivity", "Error en login", task.getException());
                            String errorMessage = "Error de conexión";
                            if (task.getException() != null) {
                                Exception exception = task.getException();
                                String exceptionMessage = exception.getMessage();
                                android.util.Log.e("MainActivity", "Exception message: " + exceptionMessage);
                                
                                if (exceptionMessage != null) {
                                    if (exceptionMessage.contains("network") || exceptionMessage.contains("timeout") || exceptionMessage.contains("NetworkError")) {
                                        errorMessage = "Sin conexión a internet. Verifica tu red.";
                                    } else if (exceptionMessage.contains("INVALID_LOGIN_CREDENTIALS") || exceptionMessage.contains("invalid") || exceptionMessage.contains("wrong-password")) {
                                        errorMessage = "Email o contraseña incorrectos";
                                    } else if (exceptionMessage.contains("USER_NOT_FOUND") || exceptionMessage.contains("user-not-found")) {
                                        errorMessage = "Usuario no encontrado";
                                    } else if (exceptionMessage.contains("too-many-requests")) {
                                        errorMessage = "Demasiados intentos. Intenta más tarde.";
                                    } else if (exceptionMessage.contains("user-disabled")) {
                                        errorMessage = "Usuario deshabilitado";
                                    } else {
                                        errorMessage = "Error: " + exceptionMessage;
                                    }
                                }
                            }
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MainActivity", "onFailure llamado", e);
                    btnLogin.setText("Iniciar Sesión");
                    btnLogin.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}