package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import java.util.*;

public class CotizacionesActivity extends AppCompatActivity {
    
    private ImageButton btnBack;
    private TextView tvTitle, tvEmpty;
    private Button btnTodas, btnPendientes, btnAceptadas, btnVencidas;
    private RecyclerView recyclerCotizaciones;
    private FloatingActionButton fabNuevaCotizacion;
    
    private DatabaseReference cotizacionesRef;
    private FirebaseAuth auth;
    private List<Cotizacion> cotizaciones;
    private List<Cotizacion> cotizacionesFiltradas;
    private CotizacionesAdapter adapter;
    
    private String filtroActual = "Todas";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cotizaciones);
        
        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
        loadCotizaciones();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        // Botones de filtro
        btnTodas = findViewById(R.id.btnTodas);
        btnPendientes = findViewById(R.id.btnPendientes);
        btnAceptadas = findViewById(R.id.btnAceptadas);
        btnVencidas = findViewById(R.id.btnVencidas);
        
        recyclerCotizaciones = findViewById(R.id.recyclerCotizaciones);
        fabNuevaCotizacion = findViewById(R.id.fabNuevaCotizacion);
        
        // Configurar título
        if (tvTitle != null) {
            tvTitle.setText("Cotizaciones");
        }
    }
    
    private void initFirebase() {
        cotizacionesRef = FirebaseDatabase.getInstance().getReference("cotizaciones");
        auth = FirebaseAuth.getInstance();
    }
    
    private void setupRecyclerView() {
        cotizaciones = new ArrayList<>();
        cotizacionesFiltradas = new ArrayList<>();
        adapter = new CotizacionesAdapter(this, cotizacionesFiltradas);
        
        recyclerCotizaciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerCotizaciones.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                System.out.println("DEBUG: Botón back presionado en Cotizaciones");
                finish();
            });
        }
        
        // Filtros con debug
        if (btnTodas != null) {
            btnTodas.setOnClickListener(v -> {
                System.out.println("DEBUG: Filtro 'Todas' seleccionado");
                aplicarFiltro("Todas");
            });
        }
        if (btnPendientes != null) {
            btnPendientes.setOnClickListener(v -> {
                System.out.println("DEBUG: Filtro 'Pendientes' seleccionado");
                aplicarFiltro("Pendientes");
            });
        }
        if (btnAceptadas != null) {
            btnAceptadas.setOnClickListener(v -> {
                System.out.println("DEBUG: Filtro 'Aceptadas' seleccionado");
                aplicarFiltro("Aceptadas");
            });
        }
        if (btnVencidas != null) {
            btnVencidas.setOnClickListener(v -> {
                System.out.println("DEBUG: Filtro 'Vencidas' seleccionado");
                aplicarFiltro("Vencidas");
            });
        }
        
        // FAB nueva cotización
        if (fabNuevaCotizacion != null) {
            fabNuevaCotizacion.setOnClickListener(v -> {
                System.out.println("DEBUG: FAB Nueva Cotización presionado");
                try {
                    Intent intent = new Intent(this, NuevaCotizacionActivity.class);
                    startActivity(intent);
                    System.out.println("DEBUG: Navegando a NuevaCotizacionActivity");
                } catch (Exception e) {
                    System.out.println("ERROR navegando a NuevaCotizacionActivity: " + e.getMessage());
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadCotizaciones() {
        if (auth == null || auth.getCurrentUser() == null) {
            System.out.println("DEBUG: Usuario no autenticado, cargando datos de prueba");
            // Cargar datos de prueba para demostrar los filtros
            cargarDatosPrueba();
            return;
        }
        
        System.out.println("DEBUG: Cargando cotizaciones desde Firebase para usuario: " + auth.getCurrentUser().getUid());
        
        cotizacionesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("DEBUG: Datos recibidos de Firebase");
                cotizaciones.clear();
                
                int contador = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Cotizacion cotizacion = snapshot.getValue(Cotizacion.class);
                        if (cotizacion != null) {
                            cotizacion.setId(snapshot.getKey());
                            cotizaciones.add(cotizacion);
                            contador++;
                            System.out.println("DEBUG: Cotización cargada - ID: " + snapshot.getKey() + ", Estado: " + cotizacion.getEstado());
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR procesando cotización: " + e.getMessage());
                    }
                }
                
                System.out.println("DEBUG: Total cotizaciones cargadas: " + contador);
                aplicarFiltro(filtroActual);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("ERROR cargando cotizaciones: " + error.getMessage());
                Toast.makeText(CotizacionesActivity.this, 
                    "Error al cargar cotizaciones: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                
                // Cargar datos de prueba en caso de error
                cargarDatosPrueba();
            }
        });
    }
    
    private void cargarDatosPrueba() {
        System.out.println("DEBUG: Cargando datos de prueba para demostrar filtros");
        cotizaciones.clear();
        
        // Datos de prueba para demostrar los filtros
        Cotizacion cot1 = new Cotizacion();
        cot1.setId("1");
        cot1.setCliente("Andrea Agurto");
        cot1.setEstado("Pendiente");
        cot1.setTotal(215.50);
        cotizaciones.add(cot1);
        
        Cotizacion cot2 = new Cotizacion();
        cot2.setId("2");
        cot2.setCliente("Ana López");
        cot2.setEstado("Aceptada");
        cot2.setTotal(550.00);
        cotizaciones.add(cot2);
        
        Cotizacion cot3 = new Cotizacion();
        cot3.setId("3");
        cot3.setCliente("Carlos Mendoza");
        cot3.setEstado("Vencida");
        cot3.setTotal(120.75);
        cotizaciones.add(cot3);
        
        Cotizacion cot4 = new Cotizacion();
        cot4.setId("4");
        cot4.setCliente("María García");
        cot4.setEstado("Pendiente");
        cot4.setTotal(890.25);
        cotizaciones.add(cot4);
        
        System.out.println("DEBUG: Datos de prueba cargados: " + cotizaciones.size() + " cotizaciones");
        aplicarFiltro(filtroActual);
    }
    
    private void aplicarFiltro(String filtro) {
        System.out.println("DEBUG: Aplicando filtro: " + filtro);
        filtroActual = filtro;
        cotizacionesFiltradas.clear();
        
        int contadorOriginal = cotizaciones.size();
        int contadorFiltrado = 0;
        
        switch (filtro) {
            case "Pendientes":
                for (Cotizacion c : cotizaciones) {
                    if ("Pendiente".equals(c.getEstado())) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                    }
                }
                break;
            case "Aceptadas":
                for (Cotizacion c : cotizaciones) {
                    if ("Aceptada".equals(c.getEstado())) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                    }
                }
                break;
            case "Vencidas":
                for (Cotizacion c : cotizaciones) {
                    if ("Vencida".equals(c.getEstado()) || c.estaVencida()) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                    }
                }
                break;
            default: // Todas
                cotizacionesFiltradas.addAll(cotizaciones);
                contadorFiltrado = contadorOriginal;
                break;
        }
        
        System.out.println("DEBUG: Filtro aplicado - Original: " + contadorOriginal + ", Filtrado: " + contadorFiltrado);
        
        adapter.notifyDataSetChanged();
        actualizarBotonesEstado(filtro);
        
        // Mostrar/ocultar mensaje vacío
        if (tvEmpty != null) {
            if (cotizacionesFiltradas.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerCotizaciones.setVisibility(View.GONE);
                System.out.println("DEBUG: Mostrando estado vacío para filtro: " + filtro);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerCotizaciones.setVisibility(View.VISIBLE);
                System.out.println("DEBUG: Mostrando " + cotizacionesFiltradas.size() + " cotizaciones");
            }
        }
    }
    
    private void actualizarBotonesEstado(String filtroActivo) {
        // Resetear todos los botones
        resetBotones();
        
        // Activar el botón seleccionado
        switch (filtroActivo) {
            case "Todas":
                if (btnTodas != null) btnTodas.setSelected(true);
                break;
            case "Pendientes":
                if (btnPendientes != null) btnPendientes.setSelected(true);
                break;
            case "Aceptadas":
                if (btnAceptadas != null) btnAceptadas.setSelected(true);
                break;
            case "Vencidas":
                if (btnVencidas != null) btnVencidas.setSelected(true);
                break;
        }
    }
    
    private void resetBotones() {
        if (btnTodas != null) btnTodas.setSelected(false);
        if (btnPendientes != null) btnPendientes.setSelected(false);
        if (btnAceptadas != null) btnAceptadas.setSelected(false);
        if (btnVencidas != null) btnVencidas.setSelected(false);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve a la actividad
        if (cotizacionesRef != null) {
            loadCotizaciones();
        }
    }
}
