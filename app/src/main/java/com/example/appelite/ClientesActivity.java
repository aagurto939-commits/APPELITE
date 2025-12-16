package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ClientesActivity extends AppCompatActivity implements ClientesAdapter.OnClienteClickListener {

    private RecyclerView recyclerView;
    private ClientesAdapter adapter;
    private List<Clientes> listaClientes;
    private List<Clientes> listaClientesOriginal;
    private DatabaseReference clientesRef;
    private EditText etBuscarCliente;
    private ImageButton btnLimpiarBusqueda;
    private MaterialButton btnTotalClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_clientes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.rcClientes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaClientes = new ArrayList<>();
        listaClientesOriginal = new ArrayList<>();
        adapter = new ClientesAdapter(listaClientes);
        adapter.setOnClienteClickListener(this);
        recyclerView.setAdapter(adapter);

        // Inicializar vistas de búsqueda
        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        btnLimpiarBusqueda = findViewById(R.id.btnLimpiarBusqueda);
        btnTotalClientes = findViewById(R.id.btnTotalClientes);

        clientesRef = FirebaseDatabase.getInstance().getReference("clientes");

        // Configurar funcionalidad de búsqueda
        setupBusqueda();

        // Escuchar cambios en Firebase para cargar clientes
        clientesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaClientesOriginal.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Clientes c = ds.getValue(Clientes.class);
                    if (c != null) {
                        listaClientesOriginal.add(c);
                    }
                }
                actualizarLista();
                actualizarContador();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ClientesActivity.this, "Error al cargar clientes", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(ClientesActivity.this, RegistrarCliente.class);
            startActivityForResult(intent, 1);
        });

        // Botón de volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // Crear nuevo cliente
            String nombreCompleto = data.getStringExtra("nombre");
            String direccion = data.getStringExtra("direccion");
            String correo = data.getStringExtra("correo");
            String tipoDoc = data.getStringExtra("tipoDoc");
            String numDoc = data.getStringExtra("numDoc");
            String telefono = data.getStringExtra("telefono");

            String id = clientesRef.push().getKey();
            Clientes nuevoCliente = new Clientes(nombreCompleto, direccion, correo, tipoDoc, numDoc, telefono, id);

            if (id != null) {
                clientesRef.child(id).setValue(nuevoCliente).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ClientesActivity.this, "Cliente registrado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ClientesActivity.this, "Error al registrar cliente", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            // Editar cliente existente
            String clienteId = data.getStringExtra("cliente_id");
            String nombreCompleto = data.getStringExtra("nombre");
            String direccion = data.getStringExtra("direccion");
            String correo = data.getStringExtra("correo");
            String tipoDoc = data.getStringExtra("tipoDoc");
            String numDoc = data.getStringExtra("numDoc");
            String telefono = data.getStringExtra("telefono");

            if (clienteId != null) {
                Clientes clienteEditado = new Clientes(nombreCompleto, direccion, correo, tipoDoc, numDoc, telefono, clienteId);
                clientesRef.child(clienteId).setValue(clienteEditado).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ClientesActivity.this, "Cliente actualizado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ClientesActivity.this, "Error al actualizar cliente", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBusqueda() {
        // Listener para búsqueda en tiempo real
        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarClientes(s.toString());
                // Mostrar/ocultar botón limpiar
                btnLimpiarBusqueda.setVisibility(s.length() > 0 ? ImageButton.VISIBLE : ImageButton.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón limpiar búsqueda
        btnLimpiarBusqueda.setOnClickListener(v -> {
            etBuscarCliente.setText("");
            btnLimpiarBusqueda.setVisibility(ImageButton.GONE);
        });
    }

    private void filtrarClientes(String texto) {
        listaClientes.clear();
        
        if (texto.isEmpty()) {
            // Si no hay texto, mostrar todos los clientes
            listaClientes.addAll(listaClientesOriginal);
        } else {
            // Filtrar clientes por nombre, documento o teléfono
            String textoBusqueda = texto.toLowerCase().trim();
            
            for (Clientes cliente : listaClientesOriginal) {
                if (cliente == null) continue;
                
                // Validar que los campos no sean null antes de usar toLowerCase()
                String nombre = cliente.getNombreCompleto();
                String numeroDoc = cliente.getNumeroDocumento();
                String telefono = cliente.getTelefono();
                String correo = cliente.getCorreo();
                String tipoDoc = cliente.getTipoDocumento();
                
                boolean coincide = false;
                
                if (nombre != null && nombre.toLowerCase().contains(textoBusqueda)) {
                    coincide = true;
                } else if (numeroDoc != null && numeroDoc.contains(textoBusqueda)) {
                    coincide = true;
                } else if (telefono != null && telefono.contains(textoBusqueda)) {
                    coincide = true;
                } else if (correo != null && correo.toLowerCase().contains(textoBusqueda)) {
                    coincide = true;
                } else if (tipoDoc != null && tipoDoc.toLowerCase().contains(textoBusqueda)) {
                    coincide = true;
                }
                
                if (coincide) {
                    listaClientes.add(cliente);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        actualizarContador();
    }

    private void actualizarLista() {
        String textoBusqueda = etBuscarCliente.getText().toString();
        filtrarClientes(textoBusqueda);
    }

    private void actualizarContador() {
        if (btnTotalClientes != null && listaClientes != null) {
            int total = listaClientes.size();
            btnTotalClientes.setText("Total: " + total);
        }
    }

    // Implementar métodos de la interfaz OnClienteClickListener
    @Override
    public void onEditarCliente(Clientes cliente) {
        mostrarDialogoConfirmarEdicion(cliente);
    }

    @Override
    public void onEliminarCliente(Clientes cliente) {
        mostrarDialogoConfirmarEliminacion(cliente);
    }

    private void mostrarDialogoConfirmarEdicion(Clientes cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Edición");
        builder.setMessage("¿Está seguro de editar este cliente?\n\n" +
                "Cliente: " + cliente.getNombreCompleto() + "\n" +
                "Documento: " + cliente.getTipoDocumento() + ": " + cliente.getNumeroDocumento());
        
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Abrir actividad de edición
            Intent intent = new Intent(ClientesActivity.this, RegistrarCliente.class);
            intent.putExtra("editar_cliente", true);
            intent.putExtra("cliente_id", cliente.getId());
            intent.putExtra("cliente_nombre", cliente.getNombreCompleto());
            intent.putExtra("cliente_direccion", cliente.getDireccion());
            intent.putExtra("cliente_correo", cliente.getCorreo());
            intent.putExtra("cliente_telefono", cliente.getTelefono());
            intent.putExtra("cliente_tipo_doc", cliente.getTipoDocumento());
            intent.putExtra("cliente_num_doc", cliente.getNumeroDocumento());
            startActivityForResult(intent, 2); // Código diferente para edición
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

    private void mostrarDialogoConfirmarEliminacion(Clientes cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Eliminación");
        builder.setMessage("¿Está seguro de eliminar este cliente?\n\n" +
                "Cliente: " + cliente.getNombreCompleto() + "\n" +
                "Documento: " + cliente.getTipoDocumento() + ": " + cliente.getNumeroDocumento());
        
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            eliminarCliente(cliente);
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

    private void eliminarCliente(Clientes cliente) {
        if (cliente.getId() != null) {
            clientesRef.child(cliente.getId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Cliente eliminado exitosamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al eliminar cliente", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
