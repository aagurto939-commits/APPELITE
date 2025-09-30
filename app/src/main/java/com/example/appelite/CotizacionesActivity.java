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
            btnBack.setOnClickListener(v -> finish());
        }
        
        // Filtros
        if (btnTodas != null) {
            btnTodas.setOnClickListener(v -> aplicarFiltro("Todas"));
        }
        if (btnPendientes != null) {
            btnPendientes.setOnClickListener(v -> aplicarFiltro("Pendientes"));
        }
        if (btnAceptadas != null) {
            btnAceptadas.setOnClickListener(v -> aplicarFiltro("Aceptadas"));
        }
        if (btnVencidas != null) {
            btnVencidas.setOnClickListener(v -> aplicarFiltro("Vencidas"));
        }
        
        // FAB nueva cotización
        if (fabNuevaCotizacion != null) {
            fabNuevaCotizacion.setOnClickListener(v -> {
                Intent intent = new Intent(this, NuevaCotizacionActivity.class);
                startActivity(intent);
            });
        }
    }
    
    private void loadCotizaciones() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        cotizacionesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cotizaciones.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Cotizacion cotizacion = snapshot.getValue(Cotizacion.class);
                    if (cotizacion != null) {
                        cotizacion.setId(snapshot.getKey());
                        cotizaciones.add(cotizacion);
                    }
                }
                
                aplicarFiltro(filtroActual);
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CotizacionesActivity.this, 
                    "Error al cargar cotizaciones: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void aplicarFiltro(String filtro) {
        filtroActual = filtro;
        cotizacionesFiltradas.clear();
        
        switch (filtro) {
            case "Pendientes":
                for (Cotizacion c : cotizaciones) {
                    if ("Pendiente".equals(c.getEstado())) {
                        cotizacionesFiltradas.add(c);
                    }
                }
                break;
            case "Aceptadas":
                for (Cotizacion c : cotizaciones) {
                    if ("Aceptada".equals(c.getEstado())) {
                        cotizacionesFiltradas.add(c);
                    }
                }
                break;
            case "Vencidas":
                for (Cotizacion c : cotizaciones) {
                    if ("Vencida".equals(c.getEstado()) || c.estaVencida()) {
                        cotizacionesFiltradas.add(c);
                    }
                }
                break;
            default: // Todas
                cotizacionesFiltradas.addAll(cotizaciones);
                break;
        }
        
        adapter.notifyDataSetChanged();
        actualizarBotonesEstado(filtro);
        
        // Mostrar/ocultar mensaje vacío
        if (tvEmpty != null) {
            if (cotizacionesFiltradas.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerCotizaciones.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerCotizaciones.setVisibility(View.VISIBLE);
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
