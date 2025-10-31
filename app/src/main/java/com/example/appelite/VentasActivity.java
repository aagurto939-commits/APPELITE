package com.example.appelite;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class VentasActivity extends AppCompatActivity {

    private CardView cardVentasHoy, cardTotalVentas, cardGestionarCotizaciones;
    private TextView tvVentasHoyNumero, tvVentasHoyMonto;
    private TextView tvTotalVentasNumero, tvTotalVentasMonto;
    private ImageButton btnBack, btnNuevaVenta;
    
    // Filtro de fecha
    private LinearLayout btnFiltroFecha;
    private TextView txtFechaSeleccionada;
    
    // RecyclerView de ventas
    private RecyclerView recyclerVentas;
    private VentasAdapter ventasAdapter;
    private List<Venta> listaVentas;
    private List<Venta> listaVentasOriginal;
    
    private DatabaseReference db;
    private FirebaseAuth auth;
    private String fechaFiltro;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: VentasActivity onCreate iniciado");
        
        setContentView(R.layout.activity_ventas);
        System.out.println("DEBUG: setContentView ejecutado");
        
        // Inicializar vistas
        initViews();
        
        // Configurar RecyclerView
        setupRecyclerView();
        
        // Configurar filtro de fecha
        setupFiltroFecha();
        
        // Inicializar Firebase
        try {
            auth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance().getReference();
            System.out.println("DEBUG: Firebase inicializado correctamente");
            
            // Verificar si hay un usuario autenticado
            if (auth.getCurrentUser() != null) {
                System.out.println("DEBUG: Usuario autenticado: " + auth.getCurrentUser().getEmail());
                // Cargar datos del dashboard
                loadDashboardData();
                loadVentas();
            } else {
                System.out.println("DEBUG: No hay usuario autenticado");
                // Mostrar datos por defecto o redirigir a login
                loadDefaultData();
                cargarVentasPrueba();
            }
        } catch (Exception e) {
            System.out.println("ERROR inicializando Firebase: " + e.getMessage());
            e.printStackTrace();
            // Cargar datos por defecto en caso de error
            loadDefaultData();
            cargarVentasPrueba();
        }
        
        // Configurar botones y cards
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                System.out.println("DEBUG: Botón back presionado");
                finish();
            });
            System.out.println("DEBUG: Botón back configurado");
        } else {
            System.out.println("ERROR: btnBack es null");
        }
        
        // Configurar card de Gestionar Cotizaciones
        CardView cardGestionarCotizaciones = findViewById(R.id.cardGestionarCotizaciones);
        if (cardGestionarCotizaciones != null) {
            cardGestionarCotizaciones.setOnClickListener(v -> {
                System.out.println("DEBUG: Card Gestionar Cotizaciones presionado");
                try {
                    Intent intent = new Intent(this, CotizacionesActivity.class);
                    startActivity(intent);
                    System.out.println("DEBUG: Navegando a CotizacionesActivity");
                } catch (Exception e) {
                    System.out.println("ERROR navegando a CotizacionesActivity: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            System.out.println("DEBUG: Card Gestionar Cotizaciones configurado");
        } else {
            System.out.println("ERROR: cardGestionarCotizaciones es null");
        }
        
        System.out.println("DEBUG: VentasActivity onCreate completado exitosamente");
    }
    
    private void initViews() {
        try {
            // Cards principales
            cardVentasHoy = findViewById(R.id.cardVentasHoy);
            cardTotalVentas = findViewById(R.id.cardTotalVentas);
            cardGestionarCotizaciones = findViewById(R.id.cardGestionarCotizaciones);
            
            // TextViews de estadísticas
            tvVentasHoyNumero = findViewById(R.id.tvVentasHoyNumero);
            tvVentasHoyMonto = findViewById(R.id.tvVentasHoyMonto);
            tvTotalVentasNumero = findViewById(R.id.tvTotalVentasNumero);
            tvTotalVentasMonto = findViewById(R.id.tvTotalVentasMonto);
            
            // Botones de navegación
            btnBack = findViewById(R.id.btnBack);
            btnNuevaVenta = findViewById(R.id.btnNuevaVenta);
            
            // Filtro de fecha
            btnFiltroFecha = findViewById(R.id.btnFiltroFecha);
            txtFechaSeleccionada = findViewById(R.id.txtFechaSeleccionada);
            
            // RecyclerView
            recyclerVentas = findViewById(R.id.recyclerVentas);
            
            System.out.println("DEBUG: Todos los views inicializados correctamente");
        } catch (Exception e) {
            System.out.println("ERROR en initViews: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupRecyclerView() {
        listaVentas = new ArrayList<>();
        listaVentasOriginal = new ArrayList<>();
        ventasAdapter = new VentasAdapter(listaVentas);
        
        ventasAdapter.setOnVentaClickListener(new VentasAdapter.OnVentaClickListener() {
            @Override
            public void onVentaClick(Venta venta) {
                // TODO: Mostrar detalle de la venta
                Toast.makeText(VentasActivity.this, "Venta: " + venta.getId(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onVerDetalle(Venta venta) {
                // TODO: Navegar a detalle de venta
                Toast.makeText(VentasActivity.this, "Ver detalle: " + venta.getId(), Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerVentas.setLayoutManager(new LinearLayoutManager(this));
        recyclerVentas.setAdapter(ventasAdapter);
    }
    
    private void setupFiltroFecha() {
        // Inicializar formato de fecha
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        
        // Configurar fecha inicial como "Hoy"
        Calendar calendar = Calendar.getInstance();
        fechaFiltro = dateFormat.format(calendar.getTime());
        actualizarTextoFecha();
        
        // Configurar click listener para el botón de filtro
        btnFiltroFecha.setOnClickListener(v -> mostrarSelectorFecha());
    }
    
    private void mostrarSelectorFecha() {
        Calendar calendar = Calendar.getInstance();
        
        // Si ya hay una fecha seleccionada, usarla como fecha inicial
        if (fechaFiltro != null && !fechaFiltro.isEmpty()) {
            try {
                Date fecha = dateFormat.parse(fechaFiltro);
                calendar.setTime(fecha);
            } catch (Exception e) {
                System.out.println("Error parseando fecha: " + e.getMessage());
            }
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                fechaFiltro = dateFormat.format(calendar.getTime());
                actualizarTextoFecha();
                filtrarVentas();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void actualizarTextoFecha() {
        if (txtFechaSeleccionada == null || fechaFiltro == null) return;
        
        try {
            Date fecha = dateFormat.parse(fechaFiltro);
            Calendar calendar = Calendar.getInstance();
            Calendar fechaSeleccionada = Calendar.getInstance();
            fechaSeleccionada.setTime(fecha);
            
            // Verificar si es hoy
            if (dateFormat.format(calendar.getTime()).equals(fechaFiltro)) {
                txtFechaSeleccionada.setText("Hoy");
            } else {
                // Mostrar fecha formateada
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                txtFechaSeleccionada.setText(displayFormat.format(fecha));
            }
        } catch (Exception e) {
            System.out.println("Error actualizando texto fecha: " + e.getMessage());
            txtFechaSeleccionada.setText("Hoy");
        }
    }
    
    private void filtrarVentas() {
        listaVentas.clear();
        
        if (listaVentasOriginal.isEmpty() || fechaFiltro == null) {
            ventasAdapter.notifyDataSetChanged();
            return;
        }
        
        for (Venta venta : listaVentasOriginal) {
            try {
                // Filtrar por la fecha seleccionada
                if (fechaFiltro.equals(venta.getFechaVenta())) {
                    listaVentas.add(venta);
                }
            } catch (Exception e) {
                System.out.println("Error filtrando venta: " + e.getMessage());
            }
        }
        
        ventasAdapter.notifyDataSetChanged();
        
        // Mostrar mensaje si no hay ventas para la fecha seleccionada
        if (listaVentas.isEmpty()) {
            Toast.makeText(this, "No hay ventas para la fecha seleccionada", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void cargarVentasPrueba() {
        listaVentasOriginal.clear();
        
        // Obtener fecha actual
        Calendar calendar = Calendar.getInstance();
        String fechaHoy = dateFormat.format(calendar.getTime());
        
        // Datos de prueba para hoy
        List<Venta> ventasPrueba = new ArrayList<>();
        ventasPrueba.add(new Venta("1", "1", "JUAN PÉREZ GARCÍA", "DNI: 12345678", 
            fechaHoy, "22:30", 435.93, 0.0, 78.47, 514.40, "CONTADO", "USD", "COMPLETADA", null, "C000001"));
        ventasPrueba.add(new Venta("2", "2", "MARÍA RODRÍGUEZ LÓPEZ", "RUC: 20123456789", 
            fechaHoy, "18:15", 250.00, 25.00, 40.50, 265.50, "CRÉDITO", "PEN", "COMPLETADA", null, "C000002"));
        
        // Datos de prueba para ayer
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String fechaAyer = dateFormat.format(calendar.getTime());
        ventasPrueba.add(new Venta("3", "3", "CARLOS SILVA MORALES", "DNI: 87654321", 
            fechaAyer, "14:20", 180.00, 0.0, 32.40, 212.40, "CONTADO", "PEN", "COMPLETADA", null, "C000003"));
        
        // Datos de prueba para hace 3 días
        calendar.add(Calendar.DAY_OF_MONTH, -2);
        String fechaAntes = dateFormat.format(calendar.getTime());
        ventasPrueba.add(new Venta("4", "4", "ANA GUTIÉRREZ VEGA", "DNI: 11223344", 
            fechaAntes, "10:30", 320.00, 0.0, 57.60, 377.60, "CONTADO", "PEN", "COMPLETADA", null, "C000004"));
        
        listaVentasOriginal.addAll(ventasPrueba);
        filtrarVentas();
    }
    
    private void loadVentas() {
        if (auth == null || auth.getCurrentUser() == null) {
            cargarVentasPrueba();
            return;
        }
        
        db.child("ventas").child(auth.getCurrentUser().getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    listaVentasOriginal.clear();
                    
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Venta venta = ds.getValue(Venta.class);
                        if (venta != null) {
                            listaVentasOriginal.add(venta);
                        }
                    }
                    
                    filtrarVentas();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("ERROR cargando ventas: " + error.getMessage());
                    cargarVentasPrueba();
                }
            });
    }
    
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }
    
    private void setupClickListeners() {
        System.out.println("DEBUG: Configurando click listeners");
        
        // Botón de regreso
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                System.out.println("DEBUG: Botón back presionado");
                finish();
            });
            System.out.println("DEBUG: Listener del botón back configurado");
        } else {
            System.out.println("ERROR: btnBack es null");
        }
        
        // Botón nueva venta
        if (btnNuevaVenta != null) {
            btnNuevaVenta.setOnClickListener(v -> {
                // TODO: Implementar nueva venta
                // Intent intent = new Intent(this, NuevaVentaActivity.class);
                // startActivity(intent);
            });
        }
        
        // Click en card de ventas de hoy - mostrar detalle de ventas diarias
        if (cardVentasHoy != null) {
            cardVentasHoy.setOnClickListener(v -> {
                // TODO: Implementar detalle de ventas de hoy
                // Intent intent = new Intent(this, DetalleVentasActivity.class);
                // startActivity(intent);
            });
        }
        
        // Click en card gestionar cotizaciones - ir a cotizaciones
        if (cardGestionarCotizaciones != null) {
            cardGestionarCotizaciones.setOnClickListener(v -> {
                Intent intent = new Intent(this, CotizacionesActivity.class);
                startActivity(intent);
            });
        }
    }
    
    private void loadDashboardData() {
        System.out.println("DEBUG: Cargando datos del dashboard para usuario autenticado");
        loadVentasHoy();
        loadVentasTotales();
    }
    
    private void loadDefaultData() {
        System.out.println("DEBUG: Cargando datos por defecto (sin autenticación)");
        // Mostrar datos por defecto cuando no hay usuario autenticado
        if (tvVentasHoyNumero != null && tvVentasHoyMonto != null) {
            tvVentasHoyNumero.setText("0");
            tvVentasHoyMonto.setText("S/ 0.00");
        }
        if (tvTotalVentasNumero != null && tvTotalVentasMonto != null) {
            tvTotalVentasNumero.setText("0");
            tvTotalVentasMonto.setText("S/ 0.00");
        }
    }
    
    private void loadVentasHoy() {
        if (auth == null) {
            System.out.println("DEBUG: auth es null, no se puede cargar ventas de hoy");
            return;
        }
        
        if (auth.getCurrentUser() == null) {
            System.out.println("DEBUG: Usuario no autenticado, no se puede cargar ventas de hoy");
            return;
        }
        
        System.out.println("DEBUG: Cargando ventas de hoy desde Firebase para usuario: " + auth.getCurrentUser().getUid());
        
        // Cargar ventas de hoy desde Firebase
        String userId = auth.getCurrentUser().getUid();
        db.child("ventas").child(userId).child("hoy")
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long numero = snapshot.child("numero").getValue(Long.class);
                        Double monto = snapshot.child("monto").getValue(Double.class);
                        
                        if (tvVentasHoyNumero != null) {
                            tvVentasHoyNumero.setText(String.valueOf(numero != null ? numero : 0));
                        }
                        if (tvVentasHoyMonto != null) {
                            tvVentasHoyMonto.setText("S/ " + String.format("%.2f", monto != null ? monto : 0.0));
                        }
                    } else {
                        // Datos por defecto si no existen
                        if (tvVentasHoyNumero != null) tvVentasHoyNumero.setText("0");
                        if (tvVentasHoyMonto != null) tvVentasHoyMonto.setText("S/ 0.00");
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    System.out.println("ERROR cargando ventas de hoy: " + error.getMessage());
                    // Datos por defecto en caso de error
                    if (tvVentasHoyNumero != null) tvVentasHoyNumero.setText("0");
                    if (tvVentasHoyMonto != null) tvVentasHoyMonto.setText("S/ 0.00");
                }
            });
    }
    
    private void loadVentasTotales() {
        if (auth == null) {
            System.out.println("DEBUG: auth es null, no se puede cargar ventas totales");
            return;
        }
        
        if (auth.getCurrentUser() == null) {
            System.out.println("DEBUG: Usuario no autenticado, no se puede cargar ventas totales");
            return;
        }
        
        System.out.println("DEBUG: Cargando ventas totales desde Firebase para usuario: " + auth.getCurrentUser().getUid());
        
        // Cargar ventas totales desde Firebase
        String userId = auth.getCurrentUser().getUid();
        db.child("ventas").child(userId).child("totales")
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long numero = snapshot.child("numero").getValue(Long.class);
                        Double monto = snapshot.child("monto").getValue(Double.class);
                        
                        if (tvTotalVentasNumero != null) {
                            tvTotalVentasNumero.setText(String.valueOf(numero != null ? numero : 0));
                        }
                        if (tvTotalVentasMonto != null) {
                            tvTotalVentasMonto.setText("S/ " + String.format("%.2f", monto != null ? monto : 0.0));
                        }
                    } else {
                        // Datos por defecto si no existen
                        if (tvTotalVentasNumero != null) tvTotalVentasNumero.setText("0");
                        if (tvTotalVentasMonto != null) tvTotalVentasMonto.setText("S/ 0.00");
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    System.out.println("ERROR cargando ventas totales: " + error.getMessage());
                    // Datos por defecto en caso de error
                    if (tvTotalVentasNumero != null) tvTotalVentasNumero.setText("0");
                    if (tvTotalVentasMonto != null) tvTotalVentasMonto.setText("S/ 0.00");
                }
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
