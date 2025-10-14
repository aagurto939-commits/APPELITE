package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VentasActivity extends AppCompatActivity {

    private CardView cardVentasHoy, cardTotalVentas, cardGestionarCotizaciones;
    private TextView tvVentasHoyNumero, tvVentasHoyMonto;
    private TextView tvTotalVentasNumero, tvTotalVentasMonto;
    private ImageButton btnBack, btnNuevaVenta;
    
    private DatabaseReference db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: VentasActivity onCreate iniciado");
        
        setContentView(R.layout.activity_ventas);
        System.out.println("DEBUG: setContentView ejecutado");
        
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
            } else {
                System.out.println("DEBUG: No hay usuario autenticado");
                // Mostrar datos por defecto o redirigir a login
                loadDefaultData();
            }
        } catch (Exception e) {
            System.out.println("ERROR inicializando Firebase: " + e.getMessage());
            e.printStackTrace();
            // Cargar datos por defecto en caso de error
            loadDefaultData();
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
            cardVentasHoy = findViewById(R.id.cardVentasHoy);
            cardTotalVentas = findViewById(R.id.cardTotalVentas);
            cardGestionarCotizaciones = findViewById(R.id.cardGestionarCotizaciones);
            
            tvVentasHoyNumero = findViewById(R.id.tvVentasHoyNumero);
            tvVentasHoyMonto = findViewById(R.id.tvVentasHoyMonto);
            tvTotalVentasNumero = findViewById(R.id.tvTotalVentasNumero);
            tvTotalVentasMonto = findViewById(R.id.tvTotalVentasMonto);
            
            btnBack = findViewById(R.id.btnBack);
            btnNuevaVenta = findViewById(R.id.btnNuevaVenta);
            
            System.out.println("DEBUG: Todos los views inicializados correctamente");
        } catch (Exception e) {
            System.out.println("ERROR en initViews: " + e.getMessage());
            e.printStackTrace();
        }
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
