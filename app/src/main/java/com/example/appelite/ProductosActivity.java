package com.example.appelite;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProductosActivity extends AppCompatActivity {

    private RecyclerView rvProductos;
    private ProductosAdapter adapter;
    private final List<Producto> listaProductos = new ArrayList<>();
    private final List<Producto> listaProductosOriginal = new ArrayList<>();

    private DatabaseReference productosRef;

    // NUEVAS referencias de UI
    private MaterialButton btnCrearProducto;
    private ImageButton btnDescargarInventario;
    private EditText etBuscarProducto;
    private TextView tvTotalProductos;

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

        // --- Recycler + Adapter ---
        rvProductos = findViewById(R.id.rvProductos);
        rvProductos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductosAdapter(listaProductos, new ProductosAdapter.OnProductoClickListener() {
            @Override public void onEditar(Producto producto) { mostrarDialogoProducto(producto); }
            @Override public void onEliminar(Producto producto) { eliminarProducto(producto); }
        });
        rvProductos.setAdapter(adapter);

        // --- Firebase ---
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        cargarProductos();

        // --- Vistas nuevas (IDs del layout) ---
        btnCrearProducto = findViewById(R.id.btnCrearProducto);
        btnDescargarInventario = findViewById(R.id.btnDescargarInventario);
        etBuscarProducto = findViewById(R.id.etBuscarProducto);
        tvTotalProductos = findViewById(R.id.tvTotalProductos);
        
        // Botón de volver
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1) Conectar botón inferior "Crear producto"
        btnCrearProducto.setOnClickListener(v -> mostrarDialogoProducto(null));

        // 2) Filtrado en vivo
        etBuscarProducto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filtrar(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });

        // 3) Descargar inventario (CSV sencillo)
        btnDescargarInventario.setOnClickListener(v -> exportarCSV());
    }

    // Carga desde Firebase
    private void cargarProductos() {
        productosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaProductos.clear();
                listaProductosOriginal.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto p = ds.getValue(Producto.class);
                    if (p != null) {
                        listaProductos.add(p);
                        listaProductosOriginal.add(p);
                    }
                }
                adapter.setProductos(listaProductos);
                actualizarTotal();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProductosActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarTotal() {
        if (tvTotalProductos != null) {
            tvTotalProductos.setText(String.valueOf(listaProductos.size()));
        }
    }

    private void filtrar(String query) {
        String q = query.trim().toLowerCase();
        listaProductos.clear();
        if (q.isEmpty()) {
            listaProductos.addAll(listaProductosOriginal);
        } else {
            for (Producto p : listaProductosOriginal) {
                boolean coincide = false;
                
                // Buscar en nombre
                if (p.getNombre() != null && p.getNombre().toLowerCase().contains(q)) {
                    coincide = true;
                }
                // Buscar en descripción
                else if (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(q)) {
                    coincide = true;
                }
                // Buscar en código
                else if (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(q)) {
                    coincide = true;
                }
                // Buscar en categoría
                else if (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(q)) {
                    coincide = true;
                }
                
                if (coincide) {
                    listaProductos.add(p);
                }
            }
        }
        adapter.setProductos(listaProductos);
        actualizarTotal();
    }

    private void mostrarDialogoProducto(Producto productoEditar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_producto, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        EditText etNombre = view.findViewById(R.id.etNombreProducto);
        EditText etCodigo = view.findViewById(R.id.etCodigoProducto);
        EditText etDescripcion = view.findViewById(R.id.etDescripcionProducto);
        EditText etPrecio = view.findViewById(R.id.etPrecioProducto);
        EditText etStock = view.findViewById(R.id.etStockProducto);
        RadioGroup rgMoneda = view.findViewById(R.id.rgMoneda);
        RadioButton rbSoles = view.findViewById(R.id.rbSoles);
        RadioButton rbDolares = view.findViewById(R.id.rbDolares);
        Button btnGuardar = view.findViewById(R.id.btnGuardarProducto);

        if (productoEditar != null) {
            etNombre.setText(productoEditar.getNombre());
            etCodigo.setText(productoEditar.getCodigo());
            etDescripcion.setText(productoEditar.getDescripcion());
            etPrecio.setText(String.valueOf(productoEditar.getPrecio()));
            etStock.setText(String.valueOf(productoEditar.getStock()));
            String moneda = productoEditar.getMoneda() != null ? productoEditar.getMoneda() : "PEN";
            if ("USD".equals(moneda)) rbDolares.setChecked(true);
            else rbSoles.setChecked(true);
        } else {
            // Generar código automático para productos nuevos
            String codigoAuto = "PROD" + System.currentTimeMillis();
            etCodigo.setText(codigoAuto);
        }

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String codigo = etCodigo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String precioStr = etPrecio.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String moneda = (rgMoneda.getCheckedRadioButtonId() == R.id.rbDolares) ? "USD" : "PEN";

            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(codigo) ||
                    TextUtils.isEmpty(precioStr) || TextUtils.isEmpty(stockStr)) {
                Toast.makeText(this, "Nombre, código, precio y stock son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            double precio;
            int stock;
            try {
                precio = Double.parseDouble(precioStr);
                stock = Integer.parseInt(stockStr);
                if (precio < 0 || stock < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Precio y stock deben ser números positivos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (productoEditar == null) {
                String id = productosRef.push().getKey();
                if (id != null) {
                    Producto nuevo = new Producto(id, nombre, descripcion, precio, stock, moneda);
                    nuevo.setCodigo(codigo);
                    nuevo.setActivo(true);
                    nuevo.setFechaCreacion(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
                    productosRef.child(id).setValue(nuevo).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                Producto editado = new Producto(productoEditar.getId(), nombre, descripcion, precio, stock, moneda);
                editado.setCodigo(codigo);
                editado.setActivo(productoEditar.isActivo());
                editado.setFechaCreacion(productoEditar.getFechaCreacion());
                editado.setFechaActualizacion(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
                // Preservar otros campos si existen
                if (productoEditar.getPrecioCosto() > 0) editado.setPrecioCosto(productoEditar.getPrecioCosto());
                if (productoEditar.getStockMinimo() > 0) editado.setStockMinimo(productoEditar.getStockMinimo());
                if (productoEditar.getCategoria() != null) editado.setCategoria(productoEditar.getCategoria());
                if (productoEditar.getProveedor() != null) editado.setProveedor(productoEditar.getProveedor());
                if (productoEditar.getUnidadMedida() != null) editado.setUnidadMedida(productoEditar.getUnidadMedida());
                
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

    // --- Exportación simple a CSV en almacenamiento privado de la app ---
    private void exportarCSV() {
        if (listaProductosOriginal.isEmpty()) {
            Toast.makeText(this, "No hay productos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("id,nombre,descripcion,precio,stock,moneda\n");
        for (Producto p : listaProductosOriginal) {
            sb.append(s(p.getId())).append(',')
                    .append(s(p.getNombre())).append(',')
                    .append(s(p.getDescripcion())).append(',')
                    .append(p.getPrecio()).append(',')
                    .append(p.getStock()).append(',')
                    .append(s(p.getMoneda()))
                    .append('\n');
        }

        try {
            File dir = getExternalFilesDir(null); // /Android/data/<pkg>/files
            if (dir == null) dir = getFilesDir();
            File out = new File(dir, "inventario.csv");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(sb.toString().getBytes());
            }
            Toast.makeText(this, "CSV exportado: " + out.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al exportar CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private String s(String v) {
        return v == null ? "" : v.replace(",", " "); // evita romper CSV
    }
}
