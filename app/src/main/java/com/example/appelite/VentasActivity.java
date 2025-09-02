package com.example.appelite;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VentasActivity extends AppCompatActivity {
    private EditText etBuscarCliente;
    private TextView tvNombreCliente;
    private RecyclerView rvVentaProductos;
    private TextView tvTotalVenta;
    private RadioGroup rgMonedaVenta;
    private RadioButton rbSolesVenta, rbDolaresVenta;
    private Button btnGuardarVenta;

    private VentaProductosAdapter adapter;
    private List<Producto> listaProductos;
    private List<VentaItem> ventaItems;
    private DatabaseReference clientesRef, productosRef, ventasRef;
    private Clientes clienteSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setupListeners();
        cargarProductos();
    }

    private void initViews() {
        etBuscarCliente = findViewById(R.id.etBuscarCliente);
        tvNombreCliente = findViewById(R.id.tvNombreCliente);
        rvVentaProductos = findViewById(R.id.rvVentaProductos);
        tvTotalVenta = findViewById(R.id.tvTotalVenta);
        rgMonedaVenta = findViewById(R.id.rgMonedaVenta);
        rbSolesVenta = findViewById(R.id.rbSolesVenta);
        rbDolaresVenta = findViewById(R.id.rbDolaresVenta);
        btnGuardarVenta = findViewById(R.id.btnGuardarVenta);

        rvVentaProductos.setLayoutManager(new LinearLayoutManager(this));
        listaProductos = new ArrayList<>();
        ventaItems = new ArrayList<>();
        
        adapter = new VentaProductosAdapter(listaProductos, ventaItems, this::calcularTotal);
        rvVentaProductos.setAdapter(adapter);

        rbSolesVenta.setChecked(true);
    }

    private void initFirebase() {
        clientesRef = FirebaseDatabase.getInstance().getReference("clientes");
        productosRef = FirebaseDatabase.getInstance().getReference("productos");
        ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
    }

    private void setupListeners() {
        etBuscarCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 8) {
                    buscarCliente(s.toString());
                } else {
                    clienteSeleccionado = null;
                    tvNombreCliente.setText("Nombre del cliente: ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgMonedaVenta.setOnCheckedChangeListener((group, checkedId) -> calcularTotal());

        btnGuardarVenta.setOnClickListener(v -> guardarVenta());
    }

    private void buscarCliente(String documento) {
        clientesRef.orderByChild("numeroDocumento").equalTo(documento)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                clienteSeleccionado = ds.getValue(Clientes.class);
                                if (clienteSeleccionado != null) {
                                    tvNombreCliente.setText("Cliente: " + clienteSeleccionado.getNombreCompleto());
                                }
                                break;
                            }
                        } else {
                            clienteSeleccionado = null;
                            tvNombreCliente.setText("Cliente no encontrado");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(VentasActivity.this, "Error al buscar cliente", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarProductos() {
        productosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaProductos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto p = ds.getValue(Producto.class);
                    if (p != null && p.getStock() > 0) {
                        listaProductos.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(VentasActivity.this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularTotal() {
        double total = 0;
        String monedaSeleccionada = rbDolaresVenta.isChecked() ? "USD" : "PEN";

        for (VentaItem item : ventaItems) {
            double precioItem = item.getPrecioUnitario() * item.getCantidad();
            
            // Conversión simple (en producción usar API de cambio)
            if (!item.getMoneda().equals(monedaSeleccionada)) {
                if (monedaSeleccionada.equals("USD") && item.getMoneda().equals("PEN")) {
                    precioItem = precioItem / 3.8; // Conversión PEN a USD
                } else if (monedaSeleccionada.equals("PEN") && item.getMoneda().equals("USD")) {
                    precioItem = precioItem * 3.8; // Conversión USD a PEN
                }
            }
            total += precioItem;
        }

        String simbolo = monedaSeleccionada.equals("USD") ? "$" : "S/";
        tvTotalVenta.setText("Total: " + simbolo + String.format("%.2f", total));
    }

    private void guardarVenta() {
        if (clienteSeleccionado == null) {
            Toast.makeText(this, "Seleccione un cliente válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ventaItems.isEmpty()) {
            Toast.makeText(this, "Agregue productos a la venta", Toast.LENGTH_SHORT).show();
            return;
        }

        String monedaVenta = rbDolaresVenta.isChecked() ? "USD" : "PEN";
        double total = calcularTotalVenta(monedaVenta);
        
        String ventaId = ventasRef.push().getKey();
        Venta nuevaVenta = new Venta(
                ventaId,
                clienteSeleccionado.getId(),
                clienteSeleccionado.getNombreCompleto(),
                clienteSeleccionado.getNumeroDocumento(),
                new ArrayList<>(ventaItems),
                total,
                monedaVenta,
                System.currentTimeMillis()
        );

        ventasRef.child(ventaId).setValue(nuevaVenta).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Cotización guardada exitosamente", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
            } else {
                Toast.makeText(this, "Error al guardar cotización", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calcularTotalVenta(String monedaVenta) {
        double total = 0;
        for (VentaItem item : ventaItems) {
            double precioItem = item.getPrecioUnitario() * item.getCantidad();
            if (!item.getMoneda().equals(monedaVenta)) {
                if (monedaVenta.equals("USD") && item.getMoneda().equals("PEN")) {
                    precioItem = precioItem / 3.8;
                } else if (monedaVenta.equals("PEN") && item.getMoneda().equals("USD")) {
                    precioItem = precioItem * 3.8;
                }
            }
            total += precioItem;
        }
        return total;
    }

    private void limpiarFormulario() {
        etBuscarCliente.setText("");
        tvNombreCliente.setText("Nombre del cliente: ");
        ventaItems.clear();
        clienteSeleccionado = null;
        rbSolesVenta.setChecked(true);
        calcularTotal();
        adapter.notifyDataSetChanged();
    }
}