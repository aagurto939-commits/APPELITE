package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
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
    
    private DatabaseReference db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);
        
        initViews();
        initFirebase();
        setupClickListeners();
        loadDashboardData();
    }
    
    private void initViews() {
        cardVentasHoy = findViewById(R.id.cardVentasHoy);
        cardTotalVentas = findViewById(R.id.cardTotalVentas);
        cardGestionarCotizaciones = findViewById(R.id.cardGestionarCotizaciones);
        
        tvVentasHoyNumero = findViewById(R.id.tvVentasHoyNumero);
        tvVentasHoyMonto = findViewById(R.id.tvVentasHoyMonto);
        tvTotalVentasNumero = findViewById(R.id.tvTotalVentasNumero);
        tvTotalVentasMonto = findViewById(R.id.tvTotalVentasMonto);
    }
    
    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
    }
    
    private void setupClickListeners() {
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
        loadVentasHoy();
        loadVentasTotales();
    }
    
    private void loadVentasHoy() {
        if (auth.getCurrentUser() == null) return;
        
        if (tvVentasHoyNumero != null && tvVentasHoyMonto != null) {
            tvVentasHoyNumero.setText("0");
            tvVentasHoyMonto.setText("S/ 0.00");
        }
    }
    
    private void loadVentasTotales() {
        if (auth.getCurrentUser() == null) return;
        
        if (tvTotalVentasNumero != null && tvTotalVentasMonto != null) {
            tvTotalVentasNumero.setText("0");
            tvTotalVentasMonto.setText("S/ 0.00");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
