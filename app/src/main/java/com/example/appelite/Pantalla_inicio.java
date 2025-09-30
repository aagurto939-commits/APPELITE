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
                CardView cardProductos = findViewById(R.id.cardInventario); // Cambiado a un ID que sí existe
                if (cardProductos != null) {
                    cardProductos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, ProductosActivity.class);
                            startActivity(intent);
                        }
                    });
                }

                // Servicios - Temporalmente deshabilitado por problema de ID en R.java
                // CardView cardServicios = findViewById(R.id.cardServicios);
                // if (cardServicios != null) {
                //     cardServicios.setOnClickListener(new View.OnClickListener() {
                //         @Override
                //         public void onClick(View v) {
                //             Intent intent = new Intent(Pantalla_inicio.this, ServiciosActivity.class);
                //             startActivity(intent);
                //         }
                //     });
                // }

                // Ventas
                CardView cardVentas = findViewById(R.id.cardFacturacion);
                if (cardVentas != null) {
                    cardVentas.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, VentasActivity.class);
                            startActivity(intent);
                        }
                    });
                }

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
                if (cardReportes != null) {
                    cardReportes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, ReportesActivity.class);
                            startActivity(intent);
                        }
                    });
                }

                // Saldos (usando cardProveedores como alternativa)
                CardView cardSaldos = findViewById(R.id.cardProveedores);
                if (cardSaldos != null) {
                    cardSaldos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, SaldosActivity.class);
                            startActivity(intent);
                        }
                    });
                }

                // Pagos (usando cardConfiguracion como alternativa)
                CardView cardPagos = findViewById(R.id.cardConfiguracion);
                if (cardPagos != null) {
                    cardPagos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, PagosActivity.class);
                            startActivity(intent);
                        }
                    });
                }

                // Servicios Postventa
                CardView cardServicios = findViewById(R.id.cardServicios);
                if (cardServicios != null) {
                    cardServicios.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Pantalla_inicio.this, ServiciosActivity.class);
                            startActivity(intent);
                        }
                    });
                }
        }
}
