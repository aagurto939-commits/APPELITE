package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegistrarCliente extends AppCompatActivity {

    private TextInputEditText etNombre, etCorreo, etDireccion, etTelefono, etNumeroDocumento;
    private Spinner spinnerTipoDocumento;
    private String tipoDocumentoSeleccionado = "DNI";
    private boolean esEdicion = false;
    private String clienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_cliente);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefono = findViewById(R.id.etTelefono);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);

        MaterialButton btnRegistrar = findViewById(R.id.btnRegistrarCliente);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelarCliente);
        MaterialButton btnBuscarDocumento = findViewById(R.id.btnBuscarDocumento);

        // Verificar si es edición o creación
        verificarModoEdicion();

        // Configurar spinner de tipo de documento
        setupSpinnerTipoDocumento();

        btnRegistrar.setOnClickListener(v -> registrarCliente());
        btnCancelar.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnBuscarDocumento.setOnClickListener(v -> buscarDocumento());
    }

    private void setupSpinnerTipoDocumento() {
        // Crear array de tipos de documento
        String[] tiposDocumento = {"DNI", "RUC"};
        
        // Crear adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, tiposDocumento);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Configurar spinner
        spinnerTipoDocumento.setAdapter(adapter);
        
        // Listener para cambios en el spinner
        spinnerTipoDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoDocumentoSeleccionado = tiposDocumento[position];
                actualizarHintDocumento();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tipoDocumentoSeleccionado = "DNI";
            }
        });
    }

    private void actualizarHintDocumento() {
        if (tipoDocumentoSeleccionado.equals("RUC")) {
            etNumeroDocumento.setHint("Ingrese RUC (11 dígitos)");
        } else {
            etNumeroDocumento.setHint("Ingrese DNI (8 dígitos)");
        }
        etNumeroDocumento.setText(""); // Limpiar campo al cambiar tipo
    }

    private void verificarModoEdicion() {
        Intent intent = getIntent();
        esEdicion = intent.getBooleanExtra("editar_cliente", false);
        
        if (esEdicion) {
            // Cambiar título y botón
            TextView titulo = findViewById(R.id.header).findViewById(android.R.id.text1);
            if (titulo != null) {
                titulo.setText("Editar Cliente");
            }
            
            MaterialButton btnRegistrar = findViewById(R.id.btnRegistrarCliente);
            btnRegistrar.setText("Actualizar Cliente");
            
            // Cargar datos del cliente
            cargarDatosCliente();
        }
    }

    private void cargarDatosCliente() {
        Intent intent = getIntent();
        clienteId = intent.getStringExtra("cliente_id");
        
        etNombre.setText(intent.getStringExtra("cliente_nombre"));
        etDireccion.setText(intent.getStringExtra("cliente_direccion"));
        etCorreo.setText(intent.getStringExtra("cliente_correo"));
        etTelefono.setText(intent.getStringExtra("cliente_telefono"));
        etNumeroDocumento.setText(intent.getStringExtra("cliente_num_doc"));
        
        String tipoDoc = intent.getStringExtra("cliente_tipo_doc");
        if (tipoDoc != null) {
            tipoDocumentoSeleccionado = tipoDoc;
            // Seleccionar en spinner después de configurar
            seleccionarTipoDocumento(tipoDoc);
        }
    }

    private void seleccionarTipoDocumento(String tipo) {
        if (spinnerTipoDocumento != null) {
            String[] tipos = {"DNI", "RUC"};
            for (int i = 0; i < tipos.length; i++) {
                if (tipos[i].equals(tipo)) {
                    spinnerTipoDocumento.setSelection(i);
                    break;
                }
            }
        }
    }

    private void registrarCliente() {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreo.getText().toString().trim();
            String direccion = etDireccion.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();
            String numeroDoc = etNumeroDocumento.getText().toString().trim();

            // Validaciones básicas
            if (nombre.isEmpty() || correo.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || numeroDoc.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

        if (tipoDocumentoSeleccionado.equals("DNI") && numeroDoc.length() != 8) {
                Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

        if (tipoDocumentoSeleccionado.equals("RUC") && numeroDoc.length() != 11) {
                Toast.makeText(this, "El RUC debe tener 11 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

        if (esEdicion) {
            // Mostrar confirmación para edición
            mostrarConfirmacionEdicion(nombre, correo, direccion, telefono, numeroDoc);
        } else {
            // Enviar datos directamente para creación
            enviarDatos(nombre, correo, direccion, telefono, numeroDoc);
        }
    }

    private void mostrarConfirmacionEdicion(String nombre, String correo, String direccion, String telefono, String numeroDoc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Actualización");
        builder.setMessage("¿Está seguro de actualizar este cliente?\n\n" +
                "Cliente: " + nombre + "\n" +
                "Documento: " + tipoDocumentoSeleccionado + ": " + numeroDoc);
        
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            enviarDatos(nombre, correo, direccion, telefono, numeroDoc);
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Personalizar colores de los botones
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void enviarDatos(String nombre, String correo, String direccion, String telefono, String numeroDoc) {
            Intent resultado = new Intent();
            resultado.putExtra("nombre", nombre);
            resultado.putExtra("correo", correo);
            resultado.putExtra("direccion", direccion);
            resultado.putExtra("telefono", telefono);
            resultado.putExtra("numDoc", numeroDoc);
        resultado.putExtra("tipoDoc", tipoDocumentoSeleccionado);
        
        if (esEdicion) {
            resultado.putExtra("cliente_id", clienteId);
        }

            setResult(RESULT_OK, resultado);
            finish();
    }

    private void buscarDocumento() {
        String numeroDoc = etNumeroDocumento.getText().toString().trim();
        
        if (numeroDoc.isEmpty()) {
            Toast.makeText(this, "Ingrese el número de documento", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato según tipo de documento
        if (tipoDocumentoSeleccionado.equals("DNI") && numeroDoc.length() != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipoDocumentoSeleccionado.equals("RUC") && numeroDoc.length() != 11) {
            Toast.makeText(this, "El RUC debe tener 11 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar loading
        Toast.makeText(this, "Buscando información...", Toast.LENGTH_SHORT).show();

        // Simular consulta a API (aquí implementarías la llamada real)
        consultarAPI(numeroDoc);
    }

    private void consultarAPI(String numeroDoc) {
        if (tipoDocumentoSeleccionado.equals("DNI")) {
            consultarDNI(numeroDoc);
        } else if (tipoDocumentoSeleccionado.equals("RUC")) {
            consultarRUC(numeroDoc);
        }
    }

    private void consultarDNI(String dni) {
        // API gratuita real de Perú - DNI API
        String url = "https://api.apis.net.pe/v1/dni?numero=" + dni;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "AppElite/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegistrarCliente.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        
                        // Verificar si hay error en la respuesta
                        if (json.has("error")) {
                            runOnUiThread(() -> {
                                Toast.makeText(RegistrarCliente.this, "DNI no encontrado en RENIEC", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                        
                        String nombre = json.optString("nombres", "") + " " + 
                                      json.optString("apellidoPaterno", "") + " " + 
                                      json.optString("apellidoMaterno", "");
                        String direccion = json.optString("direccion", "");
                        
                        runOnUiThread(() -> {
                            if (!nombre.trim().isEmpty() && !nombre.trim().equals(" ")) {
                                etNombre.setText(nombre.trim());
                                if (!direccion.isEmpty()) {
                                    etDireccion.setText(direccion);
                                }
                                Toast.makeText(RegistrarCliente.this, "Datos encontrados en RENIEC", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegistrarCliente.this, "DNI no encontrado en RENIEC", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegistrarCliente.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(RegistrarCliente.this, "Error en la consulta: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void mostrarDatosPruebaDNI(String dni) {
        // Datos de prueba para DNI (simulación local)
        String[] nombres = {
            "Juan Carlos Pérez García",
            "María Elena Rodríguez López", 
            "Carlos Alberto Silva Morales",
            "Ana Patricia Fernández Torres",
            "Luis Miguel Vargas Herrera"
        };
        
        String[] direcciones = {
            "Av. Principal 123, Lima",
            "Jr. San Martín 456, Arequipa", 
            "Av. Grau 789, Trujillo",
            "Jr. Lima 321, Chiclayo",
            "Av. Tacna 654, Piura"
        };
        
        // Usar el último dígito del DNI para seleccionar datos
        int indice = Integer.parseInt(dni.substring(dni.length() - 1)) % nombres.length;
        
        etNombre.setText(nombres[indice]);
        etDireccion.setText(direcciones[indice]);
        Toast.makeText(this, "Datos de prueba (DNI: " + dni + ")", Toast.LENGTH_SHORT).show();
    }

    private void consultarRUC(String ruc) {
        // API gratuita real de Perú - SUNAT API
        String url = "https://api.apis.net.pe/v1/ruc?numero=" + ruc;
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "AppElite/1.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(RegistrarCliente.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        
                        // Verificar si hay error en la respuesta
                        if (json.has("error")) {
                            runOnUiThread(() -> {
                                Toast.makeText(RegistrarCliente.this, "RUC no encontrado en SUNAT", Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                        
                        String razonSocial = json.optString("nombre", "");
                        String direccion = json.optString("direccion", "");
                        
                        runOnUiThread(() -> {
                            if (!razonSocial.isEmpty()) {
                                etNombre.setText(razonSocial);
                                if (!direccion.isEmpty()) {
                                    etDireccion.setText(direccion);
                                }
                                Toast.makeText(RegistrarCliente.this, "Datos encontrados en SUNAT", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegistrarCliente.this, "RUC no encontrado en SUNAT", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegistrarCliente.this, "Error al procesar datos", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(RegistrarCliente.this, "Error en la consulta: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void mostrarDatosPruebaRUC(String ruc) {
        // Datos de prueba para RUC (simulación local)
        String[] empresas = {
            "EMPRESA COMERCIAL EJEMPLO S.A.C.",
            "DISTRIBUIDORA GENERAL DEL NORTE S.R.L.",
            "CONSTRUCTORA ANDINA S.A.",
            "SERVICIOS INTEGRALES PERÚ S.A.C.",
            "IMPORTADORA Y EXPORTADORA PACÍFICO S.A."
        };
        
        String[] direcciones = {
            "Av. Comercial 123, Lima",
            "Jr. Industrial 456, Trujillo", 
            "Av. Constructora 789, Arequipa",
            "Jr. Servicios 321, Chiclayo",
            "Av. Internacional 654, Callao"
        };
        
        // Usar el último dígito del RUC para seleccionar datos
        int indice = Integer.parseInt(ruc.substring(ruc.length() - 1)) % empresas.length;
        
        etNombre.setText(empresas[indice]);
        etDireccion.setText(direcciones[indice]);
        Toast.makeText(this, "Datos de prueba (RUC: " + ruc + ")", Toast.LENGTH_SHORT).show();
    }
}
