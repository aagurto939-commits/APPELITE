package com.example.appelite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
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
    
    private static final int REQUEST_CODE_NUEVO_PRODUCTO = 1001;

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
    private Chip chipTodas, chipStockBajo, chipSinStock;
    
    // NUEVOS botones de categorías
    private ImageButton btnAgregarCategoria;
    private MaterialButton btnTodasCategorias;
    private LinearLayout layoutCategorias;
    private List<String> categoriasDisponibles = new ArrayList<>();
    private String categoriaSeleccionada = "Todas las categorías";

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
        chipTodas = findViewById(R.id.chipTodas);
        chipStockBajo = findViewById(R.id.chipStockBajo);
        chipSinStock = findViewById(R.id.chipSinStock);
        
        // --- NUEVOS botones de categorías ---
        btnAgregarCategoria = findViewById(R.id.btnAgregarCategoria);
        btnTodasCategorias = findViewById(R.id.btnTodasCategorias);
        layoutCategorias = findViewById(R.id.layoutCategorias);
        
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

        // 4) Configurar filtros
        setupFiltros();
        
        // 5) NUEVOS botones de categorías
        setupCategorias();
        
        // 6) Configurar botón "Todas las categorías"
        btnTodasCategorias.setOnClickListener(v -> seleccionarCategoria("Todas las categorías"));
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
        
        // Las categorías se cargan automáticamente desde Firebase
        // No necesitamos recargar aquí
    }


    private void mostrarDialogoProducto(Producto productoEditar) {
        // Abrir la nueva actividad en lugar del diálogo
        Intent intent = new Intent(this, NuevoProductoActivity.class);
        
        // Pasar datos del producto si estamos editando
        if (productoEditar != null) {
            intent.putExtra("producto_editar", true);
            intent.putExtra("producto_id", productoEditar.getId()); // ID del producto para edición
            intent.putExtra("producto_nombre", productoEditar.getNombre());
            intent.putExtra("producto_codigo", productoEditar.getCodigo());
            intent.putExtra("producto_descripcion", productoEditar.getDescripcion());
            intent.putExtra("producto_precio", productoEditar.getPrecio());
            intent.putExtra("producto_precio_costo", productoEditar.getPrecioCosto());
            intent.putExtra("producto_stock", productoEditar.getStock());
            intent.putExtra("producto_stock_minimo", productoEditar.getStockMinimo());
            intent.putExtra("producto_categoria", productoEditar.getCategoria());
            intent.putExtra("producto_moneda", productoEditar.getMoneda());
        }
        
        startActivityForResult(intent, REQUEST_CODE_NUEVO_PRODUCTO);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_NUEVO_PRODUCTO) {
            if (resultCode == RESULT_OK) {
                // Recargar productos cuando se guarda uno nuevo
                cargarProductos();
                Toast.makeText(this, "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void setupFiltros() {
        chipTodas.setOnClickListener(v -> aplicarFiltro("todas"));
        chipStockBajo.setOnClickListener(v -> aplicarFiltro("stock_bajo"));
        chipSinStock.setOnClickListener(v -> aplicarFiltro("sin_stock"));
    }
    
    private void setupCategorias() {
        // Botón agregar categoría - navegar a pantalla de categorías
        btnAgregarCategoria.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoriasActivity.class);
            startActivity(intent);
        });
        
        // Cargar categorías desde Firebase
        cargarCategoriasDesdeFirebase();
    }
    
    private void mostrarDialogoAgregarCategoria() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Nueva Categoría");
        
        final EditText input = new EditText(this);
        input.setHint("Nombre de la categoría");
        builder.setView(input);
        
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombreCategoria = input.getText().toString().trim();
            if (!nombreCategoria.isEmpty()) {
                agregarCategoria(nombreCategoria);
            }
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void agregarCategoria(String nombreCategoria) {
        if (!categoriasDisponibles.contains(nombreCategoria)) {
            categoriasDisponibles.add(nombreCategoria);
            actualizarCategoriasUI();
            Toast.makeText(this, "Categoría '" + nombreCategoria + "' agregada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "La categoría ya existe", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void cargarCategoriasDesdeFirebase() {
        DatabaseReference categoriasRef = FirebaseDatabase.getInstance().getReference("categorias");
        categoriasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoriasDisponibles.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String categoria = snapshot.getValue(String.class);
                    if (categoria != null && !categoria.isEmpty()) {
                        categoriasDisponibles.add(categoria);
                    }
                }
                actualizarCategoriasUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, cargar desde productos existentes como fallback
                cargarCategoriasExistentes();
            }
        });
    }
    
    private void cargarCategoriasExistentes() {
        // Cargar categorías desde los productos existentes (fallback)
        categoriasDisponibles.clear();
        for (Producto producto : listaProductos) {
            if (producto.getCategoria() != null && !producto.getCategoria().isEmpty() 
                && !categoriasDisponibles.contains(producto.getCategoria())) {
                categoriasDisponibles.add(producto.getCategoria());
            }
        }
        actualizarCategoriasUI();
    }
    
    private void actualizarCategoriasUI() {
        // Limpiar categorías existentes (excepto "Todas las categorías")
        layoutCategorias.removeViews(1, layoutCategorias.getChildCount() - 1);
        
        // Agregar categorías dinámicamente
        for (String categoria : categoriasDisponibles) {
            MaterialButton btnCategoria = new MaterialButton(this);
            btnCategoria.setText(categoria);
            btnCategoria.setTextColor(getResources().getColor(R.color.text_secondary));
            btnCategoria.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
            btnCategoria.setStrokeWidth(1);
            btnCategoria.setCornerRadius(20);
            btnCategoria.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            
            // Margen entre botones
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnCategoria.getLayoutParams();
            params.setMargins(8, 0, 0, 0);
            btnCategoria.setLayoutParams(params);
            
            // Click listener
            btnCategoria.setOnClickListener(v -> seleccionarCategoria(categoria));
            
            layoutCategorias.addView(btnCategoria);
        }
        
        // Actualizar estado visual del botón seleccionado
        actualizarEstadoBotones();
    }
    
    private void seleccionarCategoria(String categoria) {
        categoriaSeleccionada = categoria;
        actualizarEstadoBotones();
        filtrarPorCategoria(categoria);
        Toast.makeText(this, "Mostrando: " + categoria, Toast.LENGTH_SHORT).show();
    }
    
    private void actualizarEstadoBotones() {
        // Resetear todos los botones
        btnTodasCategorias.setTextColor(getResources().getColor(R.color.text_secondary));
        btnTodasCategorias.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
        btnTodasCategorias.setStrokeWidth(1);
        
        // Actualizar botones de categorías dinámicas
        for (int i = 1; i < layoutCategorias.getChildCount(); i++) {
            MaterialButton btn = (MaterialButton) layoutCategorias.getChildAt(i);
            btn.setTextColor(getResources().getColor(R.color.text_secondary));
            btn.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.text_secondary)));
            btn.setStrokeWidth(1);
        }
        
        // Marcar el botón seleccionado
        if ("Todas las categorías".equals(categoriaSeleccionada)) {
            btnTodasCategorias.setTextColor(getResources().getColor(R.color.gold_primary));
            btnTodasCategorias.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.gold_primary)));
            btnTodasCategorias.setStrokeWidth(2);
        } else {
            for (int i = 1; i < layoutCategorias.getChildCount(); i++) {
                MaterialButton btn = (MaterialButton) layoutCategorias.getChildAt(i);
                if (categoriaSeleccionada.equals(btn.getText().toString())) {
                    btn.setTextColor(getResources().getColor(R.color.gold_primary));
                    btn.setStrokeColor(ColorStateList.valueOf(getResources().getColor(R.color.gold_primary)));
                    btn.setStrokeWidth(2);
                    break;
                }
            }
        }
    }
    
    private void filtrarPorCategoria(String categoria) {
        if ("Todas las categorías".equals(categoria)) {
            adapter.notifyDataSetChanged();
        } else {
            // Filtrar productos por categoría
            List<Producto> productosFiltrados = new ArrayList<>();
            for (Producto producto : listaProductos) {
                if (categoria.equals(producto.getCategoria())) {
                    productosFiltrados.add(producto);
                }
            }
            // Actualizar la lista del adapter
            adapter.notifyDataSetChanged();
        }
    }

    private void aplicarFiltro(String tipoFiltro) {
        // Desmarcar todos los chips
        chipTodas.setChecked(false);
        chipStockBajo.setChecked(false);
        chipSinStock.setChecked(false);

        // Marcar el chip seleccionado
        switch (tipoFiltro) {
            case "todas":
                chipTodas.setChecked(true);
                break;
            case "stock_bajo":
                chipStockBajo.setChecked(true);
                break;
            case "sin_stock":
                chipSinStock.setChecked(true);
                break;
        }

        // Aplicar filtro
        String query = etBuscarProducto.getText().toString().trim();
        filtrarConTipo(query, tipoFiltro);
    }

    private void filtrarConTipo(String query, String tipoFiltro) {
        String q = query.toLowerCase();
        listaProductos.clear();

        for (Producto p : listaProductosOriginal) {
            boolean coincideTexto = q.isEmpty() || 
                (p.getNombre() != null && p.getNombre().toLowerCase().contains(q)) ||
                (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(q)) ||
                (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(q)) ||
                (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(q));

            boolean coincideFiltro = true;
            switch (tipoFiltro) {
                case "stock_bajo":
                    coincideFiltro = p.tieneStockBajo() && p.getStockMinimo() > 0;
                    break;
                case "sin_stock":
                    coincideFiltro = p.getStock() == 0;
                    break;
                case "todas":
                default:
                    coincideFiltro = true;
                    break;
            }

            if (coincideTexto && coincideFiltro) {
                listaProductos.add(p);
            }
        }

        adapter.setProductos(listaProductos);
        actualizarTotal();
    }

    private void filtrar(String query) {
        String tipoFiltro = "todas";
        if (chipStockBajo.isChecked()) tipoFiltro = "stock_bajo";
        else if (chipSinStock.isChecked()) tipoFiltro = "sin_stock";

        filtrarConTipo(query, tipoFiltro);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar categorías cuando regresemos de la pantalla de categorías
        cargarCategoriasDesdeFirebase();
    }
}
