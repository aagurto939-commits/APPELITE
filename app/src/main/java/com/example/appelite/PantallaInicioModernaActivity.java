package com.example.appelite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.SharedPreferences;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantallaInicioModernaActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private BadgeDrawable notificacionesBadge;
    private static final int REQUEST_POST_NOTIFICATIONS = 501;
    
    // TextViews para estadísticas
    private TextView tvTotalClientes;
    private TextView tvTotalProductos;
    private TextView tvTotalCotizaciones;
    private TextView tvTotalVentasHoy;
    
    // Referencias de Firebase
    private DatabaseReference clientesRef;
    private DatabaseReference productosRef;
    private DatabaseReference cotizacionesRef;
    private DatabaseReference ventasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activar edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_inicio_moderna);
        
        // Aplicar top inset solo al contenedor principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

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
        
        // Aplicar bottom inset solo al BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
        
        // Ajustar padding inferior del ScrollView para que no quede espacio en blanco ni se oculte tras la BottomNavigationView
        android.widget.ScrollView scrollView = findViewById(R.id.scrollViewContent);
        if (scrollView != null) {
            scrollView.setClipToPadding(false);
            scrollView.post(() -> {
                int bottomNavHeight = bottomNavigationView != null ? bottomNavigationView.getHeight() : 0;
                Insets insets = ViewCompat.getRootWindowInsets(scrollView) != null
                        ? ViewCompat.getRootWindowInsets(scrollView).getInsets(WindowInsetsCompat.Type.systemBars())
                        : Insets.NONE;
                int extraPadding = bottomNavHeight + insets.bottom + dpToPx(16);
                scrollView.setPadding(scrollView.getPaddingLeft(),
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        extraPadding);
            });
        }

        // Configurar clicks de las cards (usando los nuevos IDs)
        setupCardClicks();

        // Inicializar TextViews de estadísticas
        initStatsViews();
        
        // Inicializar Firebase y cargar estadísticas
        initFirebase();
        cargarEstadisticas();

        NotificationHelper.createNotificationChannel(this);
        ServiciosNotificationScheduler.scheduleDailyReminderIfNeeded(this);
        requestNotificationPermissionIfNeeded();
        setupNotificationBadge();
    }
    
    private void initStatsViews() {
        tvTotalClientes = findViewById(R.id.tvTotalClientes);
        tvTotalProductos = findViewById(R.id.tvTotalProductos);
        tvTotalCotizaciones = findViewById(R.id.tvTotalCotizaciones);
        tvTotalVentasHoy = findViewById(R.id.tvTotalVentasHoy);
    }
    
    private void initFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        clientesRef = database.getReference("clientes");
        productosRef = database.getReference("productos");
        cotizacionesRef = database.getReference("cotizaciones");
        ventasRef = database.getReference("ventas");
    }
    
    private void cargarEstadisticas() {
        cargarTotalClientes();
        cargarTotalProductos();
        cargarTotalCotizaciones();
        cargarVentasHoy();
    }
    
    private void cargarTotalClientes() {
        clientesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                if (tvTotalClientes != null) {
                    tvTotalClientes.setText(String.valueOf(total));
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                if (tvTotalClientes != null) {
                    tvTotalClientes.setText("0");
                }
            }
        });
    }
    
    private void cargarTotalProductos() {
        productosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long total = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto producto = ds.getValue(Producto.class);
                    if (producto != null && producto.isActivo()) {
                        total++;
                    }
                }
                if (tvTotalProductos != null) {
                    tvTotalProductos.setText(String.valueOf(total));
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                if (tvTotalProductos != null) {
                    tvTotalProductos.setText("0");
                }
            }
        });
    }
    
    private void cargarTotalCotizaciones() {
        cotizacionesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                if (tvTotalCotizaciones != null) {
                    tvTotalCotizaciones.setText(String.valueOf(total));
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                if (tvTotalCotizaciones != null) {
                    tvTotalCotizaciones.setText("0");
                }
            }
        });
    }
    
    private void cargarVentasHoy() {
        // Obtener fecha de hoy en formato dd/MM/yyyy
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaHoy = sdf.format(new Date());
        
        System.out.println("DEBUG cargarVentasHoy: Buscando ventas con fecha: " + fechaHoy);
        
        ventasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double totalVentas = 0.0;
                int ventasEncontradas = 0;
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Venta venta = ds.getValue(Venta.class);
                    if (venta != null) {
                        String fechaVenta = venta.getFechaVenta();
                        
                        // Normalizar fecha de venta (puede venir con espacios o en diferentes formatos)
                        if (fechaVenta != null) {
                            fechaVenta = fechaVenta.trim();
                            // Si tiene espacio, tomar solo la parte de la fecha
                            if (fechaVenta.contains(" ")) {
                                fechaVenta = fechaVenta.split(" ")[0];
                            }
                            // Si tiene coma, tomar solo la parte de la fecha
                            if (fechaVenta.contains(",")) {
                                fechaVenta = fechaVenta.split(",")[0];
                            }
                        }
                        
                        System.out.println("DEBUG cargarVentasHoy: Venta ID=" + ds.getKey() + 
                                         ", fechaVenta=" + venta.getFechaVenta() + 
                                         ", fechaNormalizada=" + fechaVenta + 
                                         ", fechaHoy=" + fechaHoy);
                        
                        // Comparar fechas normalizadas
                        if (fechaHoy.equals(fechaVenta)) {
                            // Obtener total en soles
                            double totalEnSoles = venta.obtenerTotalEnSoles();
                            System.out.println("DEBUG cargarVentasHoy: Venta encontrada - totalEnSoles=" + totalEnSoles);
                            totalVentas += totalEnSoles;
                            ventasEncontradas++;
                        }
                    }
                }
                
                System.out.println("DEBUG cargarVentasHoy: Total ventas encontradas: " + ventasEncontradas + 
                                 ", Total en soles: " + totalVentas);
                
                // Formatear el total
                DecimalFormat df = new DecimalFormat("#,##0.00");
                if (tvTotalVentasHoy != null) {
                    tvTotalVentasHoy.setText("S/ " + df.format(totalVentas));
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("DEBUG cargarVentasHoy: Error - " + error.getMessage());
                if (tvTotalVentasHoy != null) {
                    tvTotalVentasHoy.setText("S/ 0.00");
                }
            }
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
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
            startActivity(new Intent(this, ProveedoresActivity.class));
        });
        
        // Ventas
        findViewById(R.id.cardFacturacion).setOnClickListener(v -> {
            startActivity(new Intent(this, VentasActivity.class));
        });
        
        // Reportes
        findViewById(R.id.cardReportes).setOnClickListener(v -> {
            // startActivity(new Intent(this, ReportesActivity.class));
        });
        
        // Deudas
        android.view.View cardDeudas = findViewById(R.id.cardDeudas);
        if (cardDeudas != null) {
            cardDeudas.setOnClickListener(v -> {
                System.out.println("DEBUG: Card Deudas clickeado");
                Toast.makeText(this, "Abriendo Deudas...", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(this, SaldosActivity.class);
                    startActivity(intent);
                    System.out.println("DEBUG: SaldosActivity iniciada exitosamente");
                } catch (Exception e) {
                    System.out.println("DEBUG: Error al abrir SaldosActivity: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Error al abrir Deudas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            System.out.println("ERROR: cardDeudas es null");
            Toast.makeText(this, "Error: No se encontró el card de Deudas", Toast.LENGTH_SHORT).show();
        }
        
        // Configuración
        findViewById(R.id.cardConfiguracion).setOnClickListener(v -> {
            mostrarMenuConfiguracion();
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
            
        // VENTAS
        } else if (id == R.id.nav_cotizaciones) {
            startActivity(new Intent(this, CotizacionesActivity.class));
        } else if (id == R.id.nav_ver_ventas) {
            startActivity(new Intent(this, VerVentasActivity.class));
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
            startActivity(new Intent(this, ProveedoresActivity.class));
            
        // GESTIÓN
        } else if (id == R.id.nav_saldos) {
            startActivity(new Intent(this, SaldosActivity.class));
        } else if (id == R.id.nav_reportes) {
            startActivity(new Intent(this, ReportesActivity.class));
            
        // CONFIGURACIÓN
        } else if (id == R.id.nav_tipo_cambio) {
            startActivity(new Intent(this, TipoCambioActivity.class));
        } else if (id == R.id.nav_finanzas) {
            startActivity(new Intent(this, FinanzasActivity.class));
        } else if (id == R.id.nav_configuracion_empresa) {
            startActivity(new Intent(this, ConfiguracionEmpresaActivity.class));
        } else if (id == R.id.nav_logout) {
            mostrarDialogoCerrarSesion();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean onBottomNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_inicio) {
            // Ya estamos en inicio
            return true;
        } else if (id == R.id.nav_cotizaciones) {
            startActivity(new Intent(this, CotizacionesActivity.class));
            return true;
        } else if (id == R.id.nav_notificaciones) {
            startActivity(new Intent(this, NotificacionesActivity.class));
            ServiciosNotificationPreferences.markBadgeClearedForToday(this);
            ocultarBadge();
            return true;
        } else if (id == R.id.nav_perfil) {
            startActivity(new Intent(this, PerfilActivity.class));
            return true;
        }
        
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBadgeNotificaciones();
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
    
    // Método para mostrar menú de configuración
    private void mostrarMenuConfiguracion() {
        String[] opciones = {"Tipo de Cambio", "Finanzas", "Datos de Empresa", "Cerrar Sesión"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Configuración")
               .setItems(opciones, (dialog, which) -> {
                   switch (which) {
                       case 0: // Tipo de Cambio
                           startActivity(new Intent(this, TipoCambioActivity.class));
                           break;
                       case 1: // Finanzas
                           startActivity(new Intent(this, FinanzasActivity.class));
                           break;
                       case 2: // Datos de Empresa
                           startActivity(new Intent(this, ConfiguracionEmpresaActivity.class));
                           break;
                       case 3: // Cerrar Sesión
                           mostrarDialogoCerrarSesion();
                           break;
                   }
               })
               .show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_POST_NOTIFICATIONS);
        }
    }

    private void setupNotificationBadge() {
        notificacionesBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_notificaciones);
        notificacionesBadge.setVisible(false);
        // Badge rojo para resaltar sobre la barra dorada
        notificacionesBadge.setBackgroundColor(ContextCompat.getColor(this, R.color.badge_red));
        notificacionesBadge.setBadgeTextColor(ContextCompat.getColor(this, android.R.color.white));
        actualizarBadgeNotificaciones();
    }

    private void actualizarBadgeNotificaciones() {
        consultarNotificaciones();
    }

    private void mostrarBadge(int count) {
        if (notificacionesBadge == null) return;
        notificacionesBadge.setVisible(true);
        notificacionesBadge.setNumber(count);
    }

    private void ocultarBadge() {
        if (notificacionesBadge == null) return;
        notificacionesBadge.clearNumber();
        notificacionesBadge.setVisible(false);
    }

    private void consultarNotificaciones() {
        NotificationCenter.queryNotifications(this).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                int pendientes = 0;
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    Notificacion n = child.getValue(Notificacion.class);
                    if (n != null && !n.isLeida()) {
                        pendientes++;
                    }
                }
                if (pendientes > 0) {
                    mostrarBadge(pendientes);
                } else {
                    ocultarBadge();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                // Ignorar
            }
        });
    }
    
    /**
     * Muestra un diálogo de confirmación para cerrar sesión
     */
    private void mostrarDialogoCerrarSesion() {
        // Inflar el layout del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cerrar_sesion, null);
        
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelarLogout);
        MaterialButton btnConfirmar = dialogView.findViewById(R.id.btnConfirmarLogout);
        
        // Crear el diálogo
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        // Configurar estilo del diálogo
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        
        // Botón Confirmar
        btnConfirmar.setOnClickListener(v -> {
            dialog.dismiss();
            cerrarSesion();
        });
        
        dialog.show();
    }
    
    /**
     * Ejecuta el proceso de cerrar sesión
     */
    private void cerrarSesion() {
        try {
            // Mostrar mensaje de progreso
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            
            // Cerrar sesión en Firebase Auth
            FirebaseAuth.getInstance().signOut();
            
            // Limpiar preferencias compartidas (si las hay)
            SharedPreferences prefs = getSharedPreferences("AppElitePrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            
            // Limpiar notificaciones locales (limpiar preferencias)
            SharedPreferences notifPrefs = getSharedPreferences("servicios_notification_prefs", MODE_PRIVATE);
            notifPrefs.edit().clear().apply();
            
            // Navegar a la pantalla de login
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
            Toast.makeText(this, "Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error al cerrar sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
