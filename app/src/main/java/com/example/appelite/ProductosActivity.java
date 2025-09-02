package com.example.appelite;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class ProductosActivity extends AppCompatActivity {
    private RecyclerView rvProductos;
    private ProductosAdapter adapter;
    private List<Producto> listaProductos;
    private DatabaseReference productosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_productos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvProductos = findViewById(R.id.rvProductos);
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        adapter = new ProductosAdapter(listaProductos, new ProductosAdapter.OnProductoClickListener() {
            @Override
            public void onEditar(Producto producto) {
                mostrarDialogoProducto(producto);
            }
            @Override
            public void onEliminar(Producto producto) {
                eliminarProducto(producto);
            }
        });
        rvProductos.setAdapter(adapter);

        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        cargarProductos();

        FloatingActionButton fabAgregar = findViewById(R.id.fabAgregarProducto);
        fabAgregar.setOnClickListener(v -> mostrarDialogoProducto(null));
    }

    private void cargarProductos() {
        productosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaProductos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto p = ds.getValue(Producto.class);
                    if (p != null) listaProductos.add(p);
                }
                adapter.setProductos(listaProductos);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductosActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoProducto(Producto productoEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText etNombre = view.findViewById(R.id.etNombreProducto);
        EditText etDescripcion = view.findViewById(R.id.etDescripcionProducto);
        EditText etPrecio = view.findViewById(R.id.etPrecioProducto);
        EditText etStock = view.findViewById(R.id.etStockProducto);
        RadioGroup rgMoneda = view.findViewById(R.id.rgMoneda);
        RadioButton rbSoles = view.findViewById(R.id.rbSoles);
        RadioButton rbDolares = view.findViewById(R.id.rbDolares);
        Button btnGuardar = view.findViewById(R.id.btnGuardarProducto);

        if (productoEditar != null) {
            etNombre.setText(productoEditar.getNombre());
            etDescripcion.setText(productoEditar.getDescripcion());
            etPrecio.setText(String.valueOf(productoEditar.getPrecio()));
            etStock.setText(String.valueOf(productoEditar.getStock()));
            if ("USD".equals(productoEditar.getMoneda())) rbDolares.setChecked(true);
            else rbSoles.setChecked(true);
        }

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String precioStr = etPrecio.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String moneda = rbDolares.isChecked() ? "USD" : "PEN";

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(precioStr) || TextUtils.isEmpty(stockStr)) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            double precio;
            int stock;
            try {
                precio = Double.parseDouble(precioStr);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Precio o stock inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productoEditar == null) {
                String id = productosRef.push().getKey();
                Producto nuevo = new Producto(id, nombre, descripcion, precio, stock, moneda);
                productosRef.child(id).setValue(nuevo).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show();
                });
            } else {
                Producto editado = new Producto(productoEditar.getId(), nombre, descripcion, precio, stock, moneda);
                productosRef.child(productoEditar.getId()).setValue(editado).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void eliminarProducto(Producto producto) {
        productosRef.child(producto.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
        });
    }
}