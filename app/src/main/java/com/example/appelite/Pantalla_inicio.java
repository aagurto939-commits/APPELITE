package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Pantalla_inicio extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_pantalla_inicio_moderna);

                // Productos
                CardView cardProductos = findViewById(R.id.cardProductos);
                cardProductos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, ProductosActivity.class);
                                startActivity(intent);
                        }
                });

                // Servicios
                CardView cardServicios = findViewById(R.id.cardServicios);
                cardServicios.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, ServiciosActivity.class);
                                startActivity(intent);
                        }
                });

                // Ventas
                CardView cardVentas = findViewById(R.id.cardVentas);
                cardVentas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, VentasActivity.class);
                                startActivity(intent);
                        }
                });

                // Clientes
                CardView cardClientes = findViewById(R.id.cardClientes);
                cardClientes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, RegistrarCliente.class);
                                startActivity(intent);
                        }
                });

                // Reportes
                CardView cardReportes = findViewById(R.id.cardReportes);
                cardReportes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, ReportesActivity.class);
                                startActivity(intent);
                        }
                });

                // Saldos
                CardView cardSaldos = findViewById(R.id.cardSaldos);
                cardSaldos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, SaldosActivity.class);
                                startActivity(intent);
                        }
                });

                // Pagos
                CardView cardPagos = findViewById(R.id.cardPagos);
                cardPagos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                Intent intent = new Intent(Pantalla_inicio.this, PagosActivity.class);
                                startActivity(intent);
                        }
                });

                // Cotizaciones
                CardView cardCotizaciones = findViewById(R.id.cardCotizaciones);
                cardCotizaciones.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                // ⚠️ Debes crear esta Activity si aún no existe
                                Intent intent = new Intent(Pantalla_inicio.this, CotizacionesActivity.class);
                                startActivity(intent);
                        }
                });
        }
}
