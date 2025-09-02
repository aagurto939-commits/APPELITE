package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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

public class PantallaInicioModernaActivity extends AppCompatActivity {

    // ...existing code...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_inicio_moderna);

        findViewById(R.id.cardProductos).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductosActivity.class));
        });
        findViewById(R.id.cardServicios).setOnClickListener(v -> {
            startActivity(new Intent(this, ServiciosActivity.class));
        });
        findViewById(R.id.cardVentas).setOnClickListener(v -> {
            startActivity(new Intent(this, VentasActivity.class));
        });
        findViewById(R.id.cardClientes).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientesActivity.class));
        });
        findViewById(R.id.cardReportes).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportesActivity.class));
        });
        findViewById(R.id.cardSaldos).setOnClickListener(v -> {
            startActivity(new Intent(this, SaldosActivity.class));
        });
        findViewById(R.id.cardPagos).setOnClickListener(v -> {
            startActivity(new Intent(this, PagosActivity.class));
        });
        findViewById(R.id.cardCotizaciones).setOnClickListener(v -> {
            startActivity(new Intent(this, CotizacionesActivity.class));
        });
        // ...existing code...
    }
}
    
    // ...existing code...
