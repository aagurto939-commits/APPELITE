package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrarCliente extends AppCompatActivity {

    private EditText etNombre, etCorreo, etDireccion, etTelefono, etNumeroDocumento;
    private ToggleButton toggleDniRuc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_cliente);

        etNombre = findViewById(R.id.et_nombre);
        etCorreo = findViewById(R.id.et_correo);
        etDireccion = findViewById(R.id.et_direccion);
        etTelefono = findViewById(R.id.et_telefono);
        etNumeroDocumento = findViewById(R.id.et_numero_documento);
        toggleDniRuc = findViewById(R.id.toggle_dni_ruc);

        Button btnRegistrar = findViewById(R.id.btn_registrarcliente);
        Button btnCancelar  = findViewById(R.id.btn_cancelarcliente);

        // Cambiar hint y longitud según el tipo de documento seleccionado
        toggleDniRuc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { // RUC
                etNumeroDocumento.setHint("Ingrese RUC");
                etNumeroDocumento.setText("");
                etNumeroDocumento.setMaxEms(11); // Opcional, no siempre funciona bien
            } else { // DNI
                etNumeroDocumento.setHint("Ingrese DNI");
                etNumeroDocumento.setText("");
                etNumeroDocumento.setMaxEms(8);
            }
        });

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String numeroDoc = etNumeroDocumento.getText().toString().trim();
            String tipoDoc = toggleDniRuc.isChecked() ? "RUC" : "DNI";

            // Validaciones básicas
            if (nombre.isEmpty() || correo.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || numeroDoc.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tipoDoc.equals("DNI") && numeroDoc.length() != 8) {
                Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tipoDoc.equals("RUC") && numeroDoc.length() != 11) {
                Toast.makeText(this, "El RUC debe tener 11 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviar datos a ClientesActivity
            Intent resultado = new Intent();
            resultado.putExtra("nombre", nombre);
            resultado.putExtra("correo", correo);
            resultado.putExtra("direccion", direccion);
            resultado.putExtra("telefono", telefono);
            resultado.putExtra("numDoc", numeroDoc);
            resultado.putExtra("tipoDoc", tipoDoc);

            setResult(RESULT_OK, resultado);
            finish();
        });
        btnCancelar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
