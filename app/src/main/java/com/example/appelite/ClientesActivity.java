package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ClientesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClientesAdapter adapter;
    private List<Clientes> listaClientes;
    private DatabaseReference clientesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);

        recyclerView = findViewById(R.id.rcClientes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaClientes = new ArrayList<>();
        adapter = new ClientesAdapter(listaClientes);
        recyclerView.setAdapter(adapter);

        clientesRef = FirebaseDatabase.getInstance().getReference("clientes");

        // Escuchar cambios en Firebase para cargar clientes
        clientesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaClientes.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Clientes c = ds.getValue(Clientes.class);
                    if (c != null) {
                        listaClientes.add(c);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ClientesActivity.this, "Error al cargar clientes", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton btnAgregar = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(ClientesActivity.this, RegistrarCliente.class);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String nombreCompleto = data.getStringExtra("nombre"); // nombre completo
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
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show();
        }
    }
}
