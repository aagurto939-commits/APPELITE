package com.example.appelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PantallaInicioModernaActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_inicio_moderna);

        // Configurar Toolbar - usando el ActionBar por defecto por ahora
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Configurar DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar botón menú hamburguesa
        ImageButton btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Configurar Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onBottomNavigationItemSelected);

        // Configurar clicks de las cards (usando los nuevos IDs)
        setupCardClicks();
    }

    private void setupCardClicks() {
        // Servicios Postventa
        findViewById(R.id.cardServicios).setOnClickListener(v -> {
            startActivity(new Intent(this, ServiciosActivity.class));
        });
        
        // Servicios PostVenta - Usando alternativa con View.OnClickListener general
        // findViewById(R.id.cardServiciosPostventa).setOnClickListener(v -> {
        //     startActivity(new Intent(this, ServiciosActivity.class));
        // });
        
        // Por ahora comentado hasta resolver problema de generación de ID
        
        // Clientes
        findViewById(R.id.cardClientes).setOnClickListener(v -> {
            startActivity(new Intent(this, ClientesActivity.class));
        });
        
        // Inventario
        findViewById(R.id.cardInventario).setOnClickListener(v -> {
            System.out.println("DEBUG: Card Inventario clickeado");
            Toast.makeText(this, "Abriendo Inventario...", Toast.LENGTH_SHORT).show();
            try {
                startActivity(new Intent(this, ProductosActivity.class));
                System.out.println("DEBUG: ProductosActivity iniciada exitosamente");
            } catch (Exception e) {
                System.out.println("DEBUG: Error al abrir ProductosActivity: " + e.getMessage());
                Toast.makeText(this, "Error al abrir Inventario: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        // Proveedores
        findViewById(R.id.cardProveedores).setOnClickListener(v -> {
            // startActivity(new Intent(this, ProveedoresActivity.class));
        });
        
        // Ventas
        findViewById(R.id.cardFacturacion).setOnClickListener(v -> {
            startActivity(new Intent(this, VentasActivity.class));
        });
        
        // Reportes
        findViewById(R.id.cardReportes).setOnClickListener(v -> {
            // startActivity(new Intent(this, ReportesActivity.class));
        });
        
        // Configuración
        findViewById(R.id.cardConfiguracion).setOnClickListener(v -> {
            // startActivity(new Intent(this, ConfiguracionActivity.class));
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        // Manejar navegación del drawer
        if (id == R.id.nav_home) {
            // Ya estamos en home
        } else if (id == R.id.nav_clientes) {
            startActivity(new Intent(this, ClientesActivity.class));
            
        // SERVICIOS
        } else if (id == R.id.nav_servicios) {
            startActivity(new Intent(this, ServiciosActivity.class));
        } else if (id == R.id.nav_tipos_servicios) {
            Toast.makeText(this, "Tipos de Servicios - En desarrollo", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_historial_servicios) {
            Toast.makeText(this, "Historial de Servicios - En desarrollo", Toast.LENGTH_SHORT).show();
            
        // VENTAS
        } else if (id == R.id.nav_cotizaciones) {
            startActivity(new Intent(this, CotizacionesActivity.class));
        } else if (id == R.id.nav_ver_ventas) {
            startActivity(new Intent(this, VerVentasActivity.class));
        } else if (id == R.id.nav_nueva_venta) {
            Toast.makeText(this, "Nueva Venta - En desarrollo", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_ventas_dashboard) {
            startActivity(new Intent(this, VentasActivity.class));
            
        // INVENTARIO
        } else if (id == R.id.nav_productos) {
            startActivity(new Intent(this, ProductosActivity.class));
        } else if (id == R.id.nav_inventario) {
            startActivity(new Intent(this, ProductosActivity.class)); // Misma actividad por ahora
        } else if (id == R.id.nav_reporte_inventario) {
            startActivity(new Intent(this, ReporteInventarioActivity.class));
        } else if (id == R.id.nav_stock_bajo) {
            Toast.makeText(this, "Mostrando productos con stock bajo...", Toast.LENGTH_SHORT).show();
            // TODO: Filtrar productos con stock bajo
            
        // PROVEEDORES
        } else if (id == R.id.nav_proveedores) {
            Toast.makeText(this, "Gestión de Proveedores - En desarrollo", Toast.LENGTH_SHORT).show();
            // TODO: Implementar ProveedoresActivity
        } else if (id == R.id.nav_compras) {
            Toast.makeText(this, "Gestión de Compras - En desarrollo", Toast.LENGTH_SHORT).show();
            // TODO: Implementar ComprasActivity
        } else if (id == R.id.nav_ordenes_compra) {
            Toast.makeText(this, "Órdenes de Compra - En desarrollo", Toast.LENGTH_SHORT).show();
            // TODO: Implementar OrdenesCompraActivity
            
        // OTROS
        } else if (id == R.id.nav_reportes) {
            startActivity(new Intent(this, ReportesActivity.class));
        } else if (id == R.id.nav_configuracion) {
            // startActivity(new Intent(this, ConfiguracionActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean onBottomNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_inicio) {
            // Ya estamos en inicio
            return true;
        } else if (id == R.id.nav_proyectos) {
            // startActivity(new Intent(this, ProyectosActivity.class));
            return true;
        } else if (id == R.id.nav_cotizaciones) {
            startActivity(new Intent(this, CotizacionesActivity.class));
            return true;
        } else if (id == R.id.nav_notificaciones) {
            // startActivity(new Intent(this, NotificacionesActivity.class));
            return true;
        } else if (id == R.id.nav_perfil) {
            // startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }
        
        return false;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    // Método para manejar click en servicios
    public void onServiciosClick(android.view.View view) {
        startActivity(new Intent(this, ServiciosActivity.class));
    }
}
    
    // ...existing code...
