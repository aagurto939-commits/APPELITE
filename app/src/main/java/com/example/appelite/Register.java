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
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText txtApellidoNombre, txtCorreo, txtTelefono, txtContraseña;
    private Button btnRegistar;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
        txtApellidoNombre = findViewById(R.id.etFullName);
        txtCorreo = findViewById(R.id.etEmail);
        txtTelefono = findViewById(R.id.etPhone);
        txtContraseña = findViewById(R.id.etPassword);
        btnRegistar = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
    }

    private void setupEvents() {
        // Botón registrar
        btnRegistar.setOnClickListener(v -> attemptRegister());
        
        // TextView para ir al login
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String nombreusuario = txtApellidoNombre.getText().toString().trim();
        String telefonousuario = txtTelefono.getText().toString().trim();
        String emailusuario = txtCorreo.getText().toString().trim();
        String passusuario = txtContraseña.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(nombreusuario)) {
            txtApellidoNombre.setError("Nombre requerido");
            txtApellidoNombre.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(emailusuario)) {
            txtCorreo.setError("Email requerido");
            txtCorreo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(telefonousuario)) {
            txtTelefono.setError("Teléfono requerido");
            txtTelefono.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(passusuario)) {
            txtContraseña.setError("Contraseña requerida");
            txtContraseña.requestFocus();
            return;
        }

        if (passusuario.length() < 6) {
            txtContraseña.setError("Contraseña debe tener al menos 6 caracteres");
            txtContraseña.requestFocus();
            return;
        }

        // Mostrar loading
        btnRegistar.setText("Registrando...");
        btnRegistar.setEnabled(false);

        // Crear usuario con Firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailusuario, passusuario)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnRegistar.setText("Registrar");
                        btnRegistar.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Map<String, String> user = new HashMap<>();
                            user.put("username", nombreusuario);
                            user.put("Telefono", telefonousuario);
                            user.put("email", emailusuario);

                            FirebaseDatabase.getInstance().getReference().child("usuarios").child(UserId).setValue(user);
                            Toast.makeText(Register.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                            
                            // Ir al login después del registro exitoso
                            Intent intent = new Intent(Register.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Error en el registro";
                            Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}