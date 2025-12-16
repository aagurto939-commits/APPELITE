package com.example.appelite;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    private boolean processing = false;
    private boolean verificandoCotizaciones = false; // Flag para evitar verificaciones duplicadas
    private ValueEventListener ventasListener; // Listener de Firebase para poder removerlo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: VentasActivity onCreate iniciado");
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ventas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
                // Mostrar diálogo para cambiar método de pago
                mostrarDialogoMetodoPago(venta);
            }
            
            @Override
            public void onVerDetalle(Venta venta) {
                // Mostrar diálogo para cambiar método de pago
                mostrarDialogoMetodoPago(venta);
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
        
        if (listaVentasOriginal.isEmpty()) {
            ventasAdapter.notifyDataSetChanged();
            return;
        }
        
        // Si no hay filtro de fecha, mostrar todas las ventas
        if (fechaFiltro == null || fechaFiltro.trim().isEmpty()) {
            listaVentas.addAll(listaVentasOriginal);
            ventasAdapter.notifyDataSetChanged();
            return;
        }
        
        // Convertir fechaFiltro de formato "dd-MM-yyyy" a "dd/MM/yyyy" para comparar
        String fechaFiltroComparar = fechaFiltro.replace("-", "/");
        
        System.out.println("DEBUG: 🔍 Filtrando ventas por fecha: " + fechaFiltroComparar);
        System.out.println("DEBUG: 📊 Total de ventas a filtrar: " + listaVentasOriginal.size());
        
        for (Venta venta : listaVentasOriginal) {
            try {
                String fechaVenta = venta.getFechaVenta();
                if (fechaVenta == null || fechaVenta.trim().isEmpty()) {
                    continue;
                }
                
                // Normalizar fecha de venta (puede tener hora, solo tomar la parte de fecha)
                String fechaVentaNormalizada = normalizarFecha(fechaVenta);
                
                // Filtrar por la fecha seleccionada
                if (fechaVentaNormalizada != null && fechaVentaNormalizada.equals(fechaFiltroComparar)) {
                    listaVentas.add(venta);
                    System.out.println("DEBUG: ✅ Venta agregada al filtro - Fecha: " + fechaVenta + 
                        " (normalizada: " + fechaVentaNormalizada + ")" +
                        ", Cliente: " + venta.getClienteNombre());
                } else {
                    System.out.println("DEBUG: ⏭️ Venta omitida - Fecha venta: " + fechaVenta + 
                        " (normalizada: " + fechaVentaNormalizada + ")" +
                        ", Filtro: " + fechaFiltroComparar);
                }
            } catch (Exception e) {
                System.out.println("ERROR filtrando venta: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("DEBUG: 📋 Total de ventas después del filtro: " + listaVentas.size());
        
        ventasAdapter.notifyDataSetChanged();
        
        // Mostrar mensaje si no hay ventas para la fecha seleccionada
        if (listaVentas.isEmpty() && !listaVentasOriginal.isEmpty()) {
            Toast.makeText(this, "No hay ventas para la fecha seleccionada", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Verifica si hay cotizaciones aceptadas que no tienen venta asociada y las crea
     */
    private void verificarCotizacionesAceptadasSinVenta() {
        if (verificandoCotizaciones) {
            System.out.println("DEBUG: ⏭️ Verificación de cotizaciones ya en curso, omitiendo...");
            return;
        }
        
        verificandoCotizaciones = true;
        DatabaseReference cotizacionesRef = FirebaseDatabase.getInstance().getReference("cotizaciones");
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        
        // Primero, obtener todas las ventas existentes para verificar duplicados
        ventasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ventasSnapshot) {
                // Crear un Set con los cotizacionId de las ventas existentes (tanto en Firebase como en la lista local)
                java.util.Set<String> cotizacionIdsConVenta = new java.util.HashSet<>();
                
                // Agregar cotizacionIds de Firebase
                for (DataSnapshot ventaDs : ventasSnapshot.getChildren()) {
                    Venta venta = ventaDs.getValue(Venta.class);
                    if (venta != null && venta.getCotizacionId() != null && !venta.getCotizacionId().isEmpty()) {
                        cotizacionIdsConVenta.add(venta.getCotizacionId());
                    }
                }
                
                // También agregar cotizacionIds de la lista local (por si acaso hay alguna diferencia)
                if (listaVentasOriginal != null) {
                    for (Venta venta : listaVentasOriginal) {
                        if (venta != null && venta.getCotizacionId() != null && !venta.getCotizacionId().isEmpty()) {
                            cotizacionIdsConVenta.add(venta.getCotizacionId());
                        }
                    }
                }
                
                System.out.println("DEBUG: 🔍 Verificando cotizaciones aceptadas sin venta...");
                System.out.println("DEBUG: 📊 Ventas existentes con cotizacionId: " + cotizacionIdsConVenta.size());
                
                // Ahora verificar cotizaciones
                cotizacionesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot cotizacionesSnapshot) {
                        int cotizacionesProcesadas = 0;
                        int ventasCreadas = 0;
                        
                        for (DataSnapshot cotizacionDs : cotizacionesSnapshot.getChildren()) {
                            Cotizacion cotizacion = cotizacionDs.getValue(Cotizacion.class);
                            if (cotizacion == null) continue;
                            
                            String estado = cotizacion.getEstado();
                            if (estado == null || !estado.equalsIgnoreCase("ACEPTADA")) {
                                continue; // Solo procesar cotizaciones aceptadas
                            }
                            
                            cotizacionesProcesadas++;
                            String cotizacionId = cotizacion.getId();
                            
                            // Verificar si ya existe una venta para esta cotización
                            if (cotizacionId != null && !cotizacionId.isEmpty() && !cotizacionIdsConVenta.contains(cotizacionId)) {
                                // No hay venta asociada, crear una
                                System.out.println("DEBUG: ⚠️ Cotización aceptada sin venta encontrada:");
                                System.out.println("DEBUG:    - Correlativo: " + cotizacion.getCorrelativo());
                                System.out.println("DEBUG:    - Fecha: " + cotizacion.getFechaEmision());
                                System.out.println("DEBUG:    - Cliente: " + cotizacion.getClienteNombre());
                                System.out.println("DEBUG:    - Creando venta automáticamente...");
                                
                                // Crear venta como CONTADO por defecto (el usuario puede cambiarlo después)
                                crearVentaDesdeCotizacionAceptada(cotizacion);
                                ventasCreadas++;
                            } else {
                                System.out.println("DEBUG: ✅ Cotización " + cotizacion.getCorrelativo() + " ya tiene venta asociada");
                            }
                        }
                        
                        System.out.println("DEBUG: ✅ Verificación completada:");
                        System.out.println("DEBUG:    - Cotizaciones procesadas: " + cotizacionesProcesadas);
                        System.out.println("DEBUG:    - Ventas creadas: " + ventasCreadas);
                        verificandoCotizaciones = false;
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("ERROR cargando cotizaciones: " + error.getMessage());
                        verificandoCotizaciones = false;
                    }
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("ERROR cargando ventas para verificación: " + error.getMessage());
                verificandoCotizaciones = false;
            }
        });
    }
    
    /**
     * Crea una venta desde una cotización aceptada que no tenía venta asociada
     */
    private void crearVentaDesdeCotizacionAceptada(Cotizacion cotizacion) {
        // Usar la fecha de emisión de la cotización como fecha de venta (normalizada a dd/MM/yyyy)
        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaVentaTemp;
        
        if (cotizacion.getFechaEmision() != null && !cotizacion.getFechaEmision().isEmpty()) {
            try {
                String fechaEmision = cotizacion.getFechaEmision();
                // Si ya está en formato dd/MM/yyyy, usarla directamente
                if (fechaEmision.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    fechaVentaTemp = fechaEmision;
                } else {
                    // Intentar parsear otros formatos (yyyy-MM-dd, etc.)
                    SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date fecha = sdfInput.parse(fechaEmision);
                    fechaVentaTemp = sdfFecha.format(fecha);
                }
            } catch (Exception e) {
                System.out.println("ERROR parseando fecha de emisión: " + e.getMessage());
                fechaVentaTemp = sdfFecha.format(new Date());
            }
        } else {
            fechaVentaTemp = sdfFecha.format(new Date());
        }
        
        final String fechaVenta = fechaVentaTemp;
        String horaVenta = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        
        // Convertir ProductoItem a VentaItem
        List<VentaItem> ventaItems = new ArrayList<>();
        if (cotizacion.getProductos() != null) {
            for (ProductoItem productoItem : cotizacion.getProductos()) {
                VentaItem ventaItem = new VentaItem(
                    productoItem.getProductoId(),
                    productoItem.getProductoId(),
                    productoItem.getNombre(),
                    productoItem.getCantidad(),
                    productoItem.getPrecioUnitario(),
                    productoItem.getSubtotal()
                );
                ventaItems.add(ventaItem);
            }
        }
        
        // Obtener datos de la cotización
        String monedaCotizacion = cotizacion.getMoneda();
        String monedaNormalizada = monedaCotizacion != null && monedaCotizacion.contains("SOLES") ? "PEN" : "USD";
        double totalOriginal = cotizacion.getTotal();
        double tipoCambioUsado = cotizacion.getTipoCambio();
        
        // Calcular totales convertidos según las reglas del usuario
        double totalEnSoles = 0.0;
        double totalEnDolares = 0.0;
        
        if ("USD".equals(monedaNormalizada)) {
            // Si la cotización fue en dólares:
            // totalEnDolares = totalOriginal
            // totalEnSoles = totalOriginal * tipoCambioGuardado
            totalEnDolares = totalOriginal;
            if (tipoCambioUsado > 0) {
                totalEnSoles = totalOriginal * tipoCambioUsado;
            } else {
                totalEnSoles = totalOriginal; // Fallback si no hay tipo de cambio
            }
        } else {
            // Si la cotización fue en soles:
            // totalEnSoles = totalOriginal
            // totalEnDolares = totalOriginal / tipoCambioGuardado
            totalEnSoles = totalOriginal;
            if (tipoCambioUsado > 0) {
                totalEnDolares = totalOriginal / tipoCambioUsado;
            } else {
                totalEnDolares = totalOriginal; // Fallback si no hay tipo de cambio
            }
        }
        
        // Crear objeto Venta
        Venta venta = new Venta();
        venta.setClienteId(cotizacion.getClienteId() != null ? cotizacion.getClienteId() : "");
        venta.setClienteNombre(cotizacion.getClienteNombre() != null ? cotizacion.getClienteNombre().toUpperCase() : "");
        venta.setClienteDocumento("");
        venta.setFechaVenta(fechaVenta);
        venta.setHoraVenta(horaVenta);
        venta.setSubtotal(cotizacion.getSubtotal());
        venta.setDescuento(cotizacion.getDescuentoGeneral());
        venta.setIgv(cotizacion.getIgv());
        venta.setTotal(cotizacion.getTotal());
        venta.setMoneda(monedaNormalizada);
        venta.setMetodoPago("CONTADO");
        venta.setEstado("Completada");
        venta.setEstadoPago("PENDIENTE"); // Dejar como PENDIENTE para que el usuario lo defina después
        venta.setItems(ventaItems);
        venta.setCotizacionId(cotizacion.getId());
        venta.setCotizacionCorrelativo(cotizacion.getCorrelativo());
        
        // Guardar información de conversión
        venta.setMonedaOriginal(monedaNormalizada);
        venta.setTotalOriginal(totalOriginal);
        venta.setTipoCambioUsado(tipoCambioUsado);
        venta.setTotalEnSoles(totalEnSoles);
        venta.setTotalEnDolares(totalEnDolares);
        
        // Guardar en Firebase
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        String ventaId = ventasRef.push().getKey();
        venta.setId(ventaId);
        
        // Guardar explícitamente todos los campos, especialmente los de conversión
        DatabaseReference ventaRef = ventasRef.child(ventaId);
        java.util.Map<String, Object> ventaMap = new java.util.HashMap<>();
        ventaMap.put("id", venta.getId());
        ventaMap.put("clienteId", venta.getClienteId());
        ventaMap.put("clienteNombre", venta.getClienteNombre());
        ventaMap.put("clienteDocumento", venta.getClienteDocumento());
        ventaMap.put("fechaVenta", venta.getFechaVenta());
        ventaMap.put("horaVenta", venta.getHoraVenta());
        ventaMap.put("subtotal", venta.getSubtotal());
        ventaMap.put("descuento", venta.getDescuento());
        ventaMap.put("igv", venta.getIgv());
        ventaMap.put("total", venta.getTotal());
        ventaMap.put("metodoPago", venta.getMetodoPago());
        ventaMap.put("moneda", venta.getMoneda());
        ventaMap.put("estado", venta.getEstado());
        ventaMap.put("estadoPago", venta.getEstadoPago());
        ventaMap.put("items", venta.getItems());
        ventaMap.put("cotizacionId", venta.getCotizacionId());
        ventaMap.put("cotizacionCorrelativo", venta.getCotizacionCorrelativo());
        
        // Campos de conversión (CRÍTICOS)
        ventaMap.put("monedaOriginal", venta.getMonedaOriginal());
        ventaMap.put("totalOriginal", venta.getTotalOriginal());
        ventaMap.put("tipoCambioUsado", venta.getTipoCambioUsado());
        ventaMap.put("totalEnSoles", venta.getTotalEnSoles());
        ventaMap.put("totalEnDolares", venta.getTotalEnDolares());
        
        System.out.println("DEBUG: 💾 Guardando venta en Firebase:");
        System.out.println("DEBUG:    - Venta ID: " + ventaId);
        System.out.println("DEBUG:    - Moneda original: " + venta.getMonedaOriginal());
        System.out.println("DEBUG:    - Total original: " + venta.getTotalOriginal());
        System.out.println("DEBUG:    - Tipo cambio usado: " + venta.getTipoCambioUsado());
        System.out.println("DEBUG:    - Total en soles: " + venta.getTotalEnSoles());
        System.out.println("DEBUG:    - Total en dólares: " + venta.getTotalEnDolares());
        
        ventaRef.setValue(ventaMap)
            .addOnSuccessListener(aVoid -> {
                System.out.println("DEBUG: ✅ Venta creada automáticamente para cotización " + cotizacion.getCorrelativo());
                System.out.println("DEBUG:    - Venta ID: " + ventaId);
                System.out.println("DEBUG:    - Fecha: " + fechaVenta);
                Toast.makeText(VentasActivity.this, 
                    "Venta creada para cotización " + cotizacion.getCorrelativo(), 
                    Toast.LENGTH_SHORT).show();
                // No recargar ventas aquí - el listener lo hará automáticamente
            })
            .addOnFailureListener(e -> {
                System.out.println("ERROR creando venta para cotización " + cotizacion.getCorrelativo() + ": " + e.getMessage());
                e.printStackTrace();
            });
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
        actualizarKpisVentas();
    }
    
    /**
     * Calcula y guarda las conversiones de moneda para una venta
     */
    private void calcularYGuardarConversiones(Venta venta, DatabaseReference ventaRef) {
        String monedaOriginal = venta.getMonedaOriginal();
        double totalOriginal = venta.getTotalOriginal();
        double tipoCambioUsado = venta.getTipoCambioUsado();
        
        double totalEnSoles = 0.0;
        double totalEnDolares = 0.0;
        
        if ("USD".equals(monedaOriginal) && tipoCambioUsado > 0) {
            // Si la cotización fue en dólares: totalEnSoles = totalOriginal * tipoCambioUsado
            totalEnDolares = totalOriginal;
            totalEnSoles = totalOriginal * tipoCambioUsado;
        } else if ("PEN".equals(monedaOriginal) && tipoCambioUsado > 0) {
            // Si la cotización fue en soles: totalEnDolares = totalOriginal / tipoCambioUsado
            totalEnSoles = totalOriginal;
            totalEnDolares = totalOriginal / tipoCambioUsado;
        } else {
            // Sin conversión o sin tipo de cambio
            totalEnSoles = totalOriginal;
            totalEnDolares = totalOriginal;
        }
        
        venta.setTotalEnSoles(totalEnSoles);
        venta.setTotalEnDolares(totalEnDolares);
        
        // Guardar en Firebase
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("monedaOriginal", monedaOriginal);
        updates.put("totalOriginal", totalOriginal);
        updates.put("tipoCambioUsado", tipoCambioUsado);
        updates.put("totalEnSoles", totalEnSoles);
        updates.put("totalEnDolares", totalEnDolares);
        
        ventaRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                System.out.println("DEBUG: ✅ Campos de conversión guardados para venta " + venta.getId());
            })
            .addOnFailureListener(e -> {
                System.out.println("DEBUG: ⚠️ Error al guardar campos de conversión: " + e.getMessage());
            });
    }
    
    private void actualizarKpisVentas() {
        if (tvVentasHoyMonto == null || tvTotalVentasMonto == null) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaHoy = sdf.format(new Date());

        double totalHoyPen = 0.0;
        int numeroHoy = 0;
        double totalPen = 0.0;
        int numeroTotal = 0;

        if (listaVentasOriginal != null) {
            for (Venta venta : listaVentasOriginal) {
                if (venta == null) continue;
                numeroTotal++;

                // IMPORTANTE: Siempre usar totalEnSoles guardado o calcularlo con el tipo de cambio de la cotización
                // Si la venta es en dólares, debe multiplicarse por el tipo de cambio de la cotización
                double montoPen = venta.obtenerTotalEnSoles();
                
                System.out.println("DEBUG: 💰 Venta ID: " + venta.getId() + 
                    ", Moneda: " + venta.getMoneda() + 
                    ", Moneda original: " + venta.getMonedaOriginal() + 
                    ", Total original: " + venta.getTotalOriginal() + 
                    ", Tipo cambio usado: " + venta.getTipoCambioUsado() + 
                    ", Total en soles guardado: " + venta.getTotalEnSoles() + 
                    ", obtenerTotalEnSoles(): " + montoPen);
                
                totalPen += montoPen;

                String fechaVenta = venta.getFechaVenta();
                if (sonFechasIguales(fechaVenta, fechaHoy)) {
                    numeroHoy++;
                    totalHoyPen += montoPen;
                }
            }
        }

        System.out.println("DEBUG: 📊 KPIs actualizados - Total Ventas: S/ " + totalPen + 
            " (" + numeroTotal + " ventas), Ventas Hoy: S/ " + totalHoyPen + " (" + numeroHoy + " ventas)");

        tvVentasHoyNumero.setText(String.valueOf(numeroHoy));
        tvVentasHoyMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", totalHoyPen));
        tvTotalVentasNumero.setText(String.valueOf(numeroTotal));
        tvTotalVentasMonto.setText(String.format(Locale.getDefault(), "S/ %.2f", totalPen));
    }

    private boolean sonFechasIguales(String fecha1, String fecha2) {
        String normalizada1 = normalizarFecha(fecha1);
        String normalizada2 = normalizarFecha(fecha2);
        return normalizada1 != null && normalizada1.equals(normalizada2);
    }

    private String normalizarFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return null;
        }
        String limpia = fecha.trim();
        if (limpia.contains(" ")) {
            limpia = limpia.split(" ")[0];
        }
        return limpia.replace("-", "/");
    }
    
    private void loadVentas() {
        // Remover listener anterior si existe
        if (ventasListener != null && db != null) {
            db.child("ventas").removeEventListener(ventasListener);
        }
        
        // Cargar ventas directamente del nodo "ventas" (sin userId) para mostrar todas las ventas
        // Esto incluye las ventas creadas desde cotizaciones aceptadas
        ventasListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    listaVentasOriginal.clear();
                    
                    System.out.println("DEBUG: 📊 Cargando ventas desde Firebase. Total de ventas encontradas: " + snapshot.getChildrenCount());
                    
                    // Usar un Set para evitar duplicados por ID
                    java.util.Set<String> ventasIds = new java.util.HashSet<>();
                    // Usar un Map para evitar duplicados por cotizacionId (mantener solo la más reciente)
                    java.util.Map<String, Venta> ventasPorCotizacionId = new java.util.HashMap<>();
                    // Lista temporal para almacenar todas las ventas antes de filtrar duplicados
                    java.util.List<Venta> ventasTemporales = new java.util.ArrayList<>();
                    
                    // Primero, cargar todas las ventas en una lista temporal
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Venta venta = ds.getValue(Venta.class);
                        if (venta != null) {
                            String ventaId = ds.getKey();
                            venta.setId(ventaId);
                            
                            // Verificar duplicados por ID
                            if (ventasIds.contains(ventaId)) {
                                System.out.println("DEBUG: ⚠️ Venta duplicada detectada (ID: " + ventaId + "), omitiendo...");
                                continue;
                            }
                            
                            // Verificar que los campos de conversión estén presentes
                            // Si no están, intentar calcularlos desde los datos disponibles
                            String monedaVenta = venta.getMoneda();
                            String monedaNormalizada = venta.getMonedaNormalizada();
                            double totalVenta = venta.getTotal();
                            
                            // Si la venta no tiene campos de conversión guardados, calcularlos
                            if (venta.getTotalEnSoles() <= 0 || venta.getTipoCambioUsado() <= 0) {
                                System.out.println("DEBUG: ⚠️ Venta " + ventaId + " no tiene campos de conversión completos, recalculando...");
                                
                                // Si no tiene monedaOriginal, usar la moneda actual
                                if (venta.getMonedaOriginal() == null || venta.getMonedaOriginal().isEmpty()) {
                                    venta.setMonedaOriginal(monedaNormalizada);
                                }
                                
                                // Si no tiene totalOriginal, usar el total actual
                                if (venta.getTotalOriginal() <= 0) {
                                    venta.setTotalOriginal(totalVenta);
                                }
                                
                                // Si no tiene tipoCambioUsado, intentar obtenerlo de la cotización
                                if (venta.getTipoCambioUsado() <= 0 && venta.getCotizacionId() != null && !venta.getCotizacionId().isEmpty()) {
                                    // Buscar el tipo de cambio en la cotización
                                    DatabaseReference cotizacionRef = FirebaseDatabase.getInstance().getReference("cotizaciones").child(venta.getCotizacionId());
                                    cotizacionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot cotizacionSnapshot) {
                                            if (cotizacionSnapshot.exists()) {
                                                Cotizacion cotizacion = cotizacionSnapshot.getValue(Cotizacion.class);
                                                if (cotizacion != null && cotizacion.getTipoCambio() > 0) {
                                                    venta.setTipoCambioUsado(cotizacion.getTipoCambio());
                                                    calcularYGuardarConversiones(venta, ds.getRef());
                                                }
                                            }
                                        }
                                        
                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            System.out.println("DEBUG: ⚠️ Error al obtener tipo de cambio de cotización: " + error.getMessage());
                                        }
                                    });
                                } else if (venta.getTipoCambioUsado() > 0) {
                                    // Ya tiene tipo de cambio, calcular conversiones
                                    calcularYGuardarConversiones(venta, ds.getRef());
                                }
                            }
                            
                            System.out.println("DEBUG: 📦 Venta cargada desde Firebase - ID: " + ventaId + 
                                ", Moneda: " + venta.getMoneda() + 
                                ", Moneda original: " + venta.getMonedaOriginal() + 
                                ", Total original: " + venta.getTotalOriginal() + 
                                ", Tipo cambio: " + venta.getTipoCambioUsado() + 
                                ", Total en soles: " + venta.getTotalEnSoles());
                            
                            ventasIds.add(ventaId);
                            ventasTemporales.add(venta);
                        }
                    }
                    
                    // Ahora, procesar las ventas y eliminar duplicados por cotizacionId
                    for (Venta venta : ventasTemporales) {
                        String cotizacionId = venta.getCotizacionId();
                        boolean ventaAgregada = false;
                        
                        if (cotizacionId != null && !cotizacionId.isEmpty()) {
                            Venta ventaExistente = ventasPorCotizacionId.get(cotizacionId);
                            if (ventaExistente != null) {
                                // Ya existe una venta con este cotizacionId
                                // Mantener la más reciente (comparar por ID)
                                System.out.println("DEBUG: ⚠️ Venta duplicada por cotizacionId detectada:");
                                System.out.println("DEBUG:    - Venta existente ID: " + ventaExistente.getId() + ", Cotización: " + ventaExistente.getCotizacionCorrelativo());
                                System.out.println("DEBUG:    - Venta nueva ID: " + venta.getId() + ", Cotización: " + venta.getCotizacionCorrelativo());
                                
                                // Comparar por ID (el más reciente en Firebase tiene un ID más grande)
                                if (venta.getId().compareTo(ventaExistente.getId()) > 0) {
                                    // La nueva venta es más reciente, reemplazar
                                    System.out.println("DEBUG:    → Reemplazando venta existente con la más reciente");
                                    // Remover la venta anterior de la lista
                                    listaVentasOriginal.remove(ventaExistente);
                                    ventasPorCotizacionId.put(cotizacionId, venta);
                                    listaVentasOriginal.add(venta);
                                    ventaAgregada = true;
                                } else {
                                    // La venta existente es más reciente, omitir la nueva
                                    System.out.println("DEBUG:    → Omitiendo venta duplicada (manteniendo la más reciente)");
                                }
                            } else {
                                // Primera venta con este cotizacionId
                                ventasPorCotizacionId.put(cotizacionId, venta);
                                listaVentasOriginal.add(venta);
                                ventaAgregada = true;
                            }
                        } else {
                            // Venta sin cotizacionId, agregarla directamente
                            listaVentasOriginal.add(venta);
                            ventaAgregada = true;
                        }
                        
                        if (ventaAgregada) {
                            System.out.println("DEBUG: ✅ Venta cargada - ID: " + venta.getId() + 
                                ", Fecha: " + venta.getFechaVenta() + 
                                ", Cliente: " + venta.getClienteNombre() + 
                                ", Cotización: " + venta.getCotizacionCorrelativo() +
                                ", Estado Pago: " + venta.getEstadoPago());
                        }
                    }
                    
                    System.out.println("DEBUG: 📋 Total de ventas en listaVentasOriginal (sin duplicados): " + listaVentasOriginal.size());
                    
                    // Ordenar por fecha de venta (más recientes primero)
                    listaVentasOriginal.sort((v1, v2) -> {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            Date fecha1 = sdf.parse(v1.getFechaVenta() != null ? v1.getFechaVenta() : "01/01/2000");
                            Date fecha2 = sdf.parse(v2.getFechaVenta() != null ? v2.getFechaVenta() : "01/01/2000");
                            return fecha2.compareTo(fecha1); // Orden descendente
                        } catch (Exception e) {
                            return 0;
                        }
                    });
                    
                    // Verificar cotizaciones aceptadas sin venta asociada (solo una vez)
                    if (!verificandoCotizaciones) {
                        verificarCotizacionesAceptadasSinVenta();
                    }
                    
                    filtrarVentas();
                    actualizarKpisVentas();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("ERROR cargando ventas: " + error.getMessage());
                    cargarVentasPrueba();
                }
            };
        
        db.child("ventas").addValueEventListener(ventasListener);
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
        actualizarKpisVentas();
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
    
    private void mostrarDialogoMetodoPago(Venta venta) {
        // Verificar si está procesando
        if (processing) {
            Toast.makeText(this, "Procesando... Por favor espera", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verificar el estado de pago actual
        String estadoPagoActual = venta.getEstadoPago();
        boolean esPendiente = venta.esPendiente();
        boolean esContado = venta.esContado();
        boolean esCredito = venta.esCredito();
        
        // Inflar el layout personalizado
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_venta_opciones, null);
        
        // Obtener referencias de los elementos del diálogo
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvSubtitulo = dialogView.findViewById(R.id.tvSubtitulo);
        TextView tvCotizacion = dialogView.findViewById(R.id.tvCotizacion);
        TextView tvCliente = dialogView.findViewById(R.id.tvCliente);
        TextView tvFechaHora = dialogView.findViewById(R.id.tvFechaHora);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotal);
        com.google.android.material.button.MaterialButton btnContado = dialogView.findViewById(R.id.btnContado);
        com.google.android.material.button.MaterialButton btnCredito = dialogView.findViewById(R.id.btnCredito);
        com.google.android.material.button.MaterialButton btnEliminar = dialogView.findViewById(R.id.btnEliminar);
        com.google.android.material.button.MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        com.google.android.material.button.MaterialButton btnIrDeuda = dialogView.findViewById(R.id.btnIrDeuda);
        
        // Referencias para información de conversión
        android.view.View separadorConversion = dialogView.findViewById(R.id.separadorConversion);
        android.view.View layoutConversion = dialogView.findViewById(R.id.layoutConversion);
        TextView tvTotalOriginal = dialogView.findViewById(R.id.tvTotalOriginal);
        TextView tvTipoCambioUsado = dialogView.findViewById(R.id.tvTipoCambioUsado);
        TextView tvMontoConvertido = dialogView.findViewById(R.id.tvMontoConvertido);
        
        // Configurar datos de la venta
        if (tvTitulo != null) {
            tvTitulo.setText("Gestionar Venta");
        }
        
        String estadoTexto = "";
        if (esPendiente) {
            estadoTexto = "Resumen de la operación";
        } else if (esContado) {
            estadoTexto = "Venta marcada como CONTADO";
        } else if (esCredito) {
            estadoTexto = "Venta marcada como CRÉDITO";
        }
        
        if (tvSubtitulo != null) {
            tvSubtitulo.setText(estadoTexto);
        }
        
        String cotizacionStr = (venta.getCotizacionCorrelativo() != null && !venta.getCotizacionCorrelativo().isEmpty()) 
            ? venta.getCotizacionCorrelativo() 
            : "Venta directa";
        tvCotizacion.setText(cotizacionStr);
        tvCliente.setText(venta.getClienteNombre() != null ? venta.getClienteNombre().toUpperCase() : "N/A");
        tvTotal.setText(venta.getMontoFormateado());
        
        // Configurar fecha y hora
        String fechaHora = "";
        if (venta.getFechaVenta() != null) {
            fechaHora = venta.getFechaVenta();
        }
        if (venta.getHoraVenta() != null && !venta.getHoraVenta().isEmpty()) {
            fechaHora += " • " + venta.getHoraVenta();
        }
        if (fechaHora.isEmpty()) {
            fechaHora = "N/A";
        }
        tvFechaHora.setText(fechaHora);
        
        // Mostrar información de conversión si existe
        if (venta.getTipoCambioUsado() > 0 && layoutConversion != null) {
            String monedaOriginal = venta.getMonedaOriginal();
            double totalOriginal = venta.getTotalOriginal();
            double tipoCambioUsado = venta.getTipoCambioUsado();
            
            // Formatear total original
            String simboloOriginal = "USD".equals(monedaOriginal) ? "US$ " : "S/ ";
            if (tvTotalOriginal != null) {
                tvTotalOriginal.setText(String.format(Locale.getDefault(), "%s%.2f", simboloOriginal, totalOriginal));
            }
            
            // Mostrar tipo de cambio usado
            if (tvTipoCambioUsado != null) {
                tvTipoCambioUsado.setText(String.format(Locale.getDefault(), "%.2f", tipoCambioUsado));
            }
            
            // Mostrar monto convertido (en la moneda opuesta)
            if (tvMontoConvertido != null) {
                double montoConvertido = "USD".equals(monedaOriginal) ? venta.getTotalEnSoles() : venta.getTotalEnDolares();
                String simboloConvertido = "USD".equals(monedaOriginal) ? "S/ " : "US$ ";
                tvMontoConvertido.setText(String.format(Locale.getDefault(), "%s%.2f", simboloConvertido, montoConvertido));
            }
            
            // Mostrar sección de conversión
            if (separadorConversion != null) {
                separadorConversion.setVisibility(android.view.View.VISIBLE);
            }
            layoutConversion.setVisibility(android.view.View.VISIBLE);
        } else {
            // Ocultar sección de conversión si no hay tipo de cambio
            if (separadorConversion != null) {
                separadorConversion.setVisibility(android.view.View.GONE);
            }
            if (layoutConversion != null) {
                layoutConversion.setVisibility(android.view.View.GONE);
            }
        }
        
        // Crear el diálogo PRIMERO para poder usarlo en los listeners
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        
        // Configurar visibilidad de botones según el estado
        if (esPendiente) {
            // PENDIENTE: Mostrar botones Contado/Crédito
            btnContado.setVisibility(android.view.View.VISIBLE);
            btnCredito.setVisibility(android.view.View.VISIBLE);
            if (btnIrDeuda != null) {
                btnIrDeuda.setVisibility(android.view.View.GONE);
            }
        } else if (esContado) {
            // CONTADO: Solo mostrar "Ver detalle" (ocultar acciones)
            btnContado.setVisibility(android.view.View.GONE);
            btnCredito.setVisibility(android.view.View.GONE);
            if (btnIrDeuda != null) {
                btnIrDeuda.setVisibility(android.view.View.GONE);
            }
            Toast.makeText(this, "Esta venta ya fue procesada como CONTADO", Toast.LENGTH_SHORT).show();
        } else if (esCredito) {
            // CRÉDITO: Solo mostrar "Ir a Deuda"
            btnContado.setVisibility(android.view.View.GONE);
            btnCredito.setVisibility(android.view.View.GONE);
            // Si no existe btnIrDeuda en el layout, lo creamos dinámicamente o lo ocultamos
            if (btnIrDeuda != null) {
                btnIrDeuda.setVisibility(android.view.View.VISIBLE);
                btnIrDeuda.setText("Ir a Deudas");
                btnIrDeuda.setOnClickListener(v -> {
                    dialog.dismiss();
                    irADeudas(venta);
                });
            }
        }
        
        // Configurar botones solo si están visibles
        if (btnContado.getVisibility() == android.view.View.VISIBLE) {
            btnContado.setOnClickListener(v -> {
                dialog.dismiss();
                if (!processing) {
                    actualizarMetodoPago(venta, "Contado", false);
                }
            });
        }
        
        if (btnCredito.getVisibility() == android.view.View.VISIBLE) {
            btnCredito.setOnClickListener(v -> {
                dialog.dismiss();
                if (!processing) {
                    actualizarMetodoPago(venta, "Crédito", true);
                }
            });
        }
        
        btnEliminar.setOnClickListener(v -> {
            dialog.dismiss();
            mostrarConfirmacionEliminar(venta);
        });
        
        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        
        // Mostrar el diálogo
        dialog.show();
        
        // Ajustar el ancho del diálogo para que se vea mejor
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
    
    private void irADeudas(Venta venta) {
        Intent intent = new Intent(this, SaldosActivity.class);
        intent.putExtra("ventaId", venta.getId());
        startActivity(intent);
    }
    
    private void mostrarConfirmacionEliminar(Venta venta) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Venta")
            .setMessage("¿Estás seguro de que deseas eliminar esta venta?\n\n" +
                       "Cliente: " + (venta.getClienteNombre() != null ? venta.getClienteNombre() : "N/A") + "\n" +
                       "Total: " + venta.getMontoFormateado() + "\n\n" +
                       "Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> eliminarVenta(venta))
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void eliminarVenta(Venta venta) {
        if (venta.getId() == null || venta.getId().isEmpty()) {
            Toast.makeText(this, "Error: La venta no tiene ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        System.out.println("DEBUG: 🗑️ Eliminando venta:");
        System.out.println("DEBUG:    - Venta ID: " + venta.getId());
        System.out.println("DEBUG:    - Cliente: " + venta.getClienteNombre());
        System.out.println("DEBUG:    - Total: " + venta.getTotal());
        System.out.println("DEBUG:    - Moneda: " + venta.getMoneda());
        System.out.println("DEBUG:    - Estado Pago: " + venta.getEstadoPago());
        System.out.println("DEBUG:    - Cotización ID: " + venta.getCotizacionId());
        
        // Primero, eliminar la deuda asociada si existe (si la venta es CRÉDITO)
        eliminarDeudaAsociada(venta.getId(), () -> {
            // Luego, devolver productos al inventario
            devolverProductosAlInventario(venta, () -> {
                // Marcar la cotización como "CANCELADA" si tiene una cotización asociada
                // Esto evita que se vuelva a crear la venta automáticamente
                if (venta.getCotizacionId() != null && !venta.getCotizacionId().isEmpty()) {
                    DatabaseReference cotizacionesRef = FirebaseDatabase.getInstance().getReference("cotizaciones");
                    cotizacionesRef.child(venta.getCotizacionId()).child("estado").setValue("CANCELADA")
                        .addOnSuccessListener(aVoid -> {
                            System.out.println("DEBUG: ✅ Cotización marcada como CANCELADA para evitar recreación de venta");
                        })
                        .addOnFailureListener(e -> {
                            System.out.println("DEBUG: ⚠️ No se pudo marcar la cotización como CANCELADA: " + e.getMessage());
                        });
                }
                
                // Finalmente, eliminar la venta
                DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
                DatabaseReference ventaRef = ventasRef.child(venta.getId());
                
                ventaRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("DEBUG: ✅ Venta eliminada exitosamente de Firebase");
                        // El listener de Firebase actualizará automáticamente la lista y los KPIs
                        Toast.makeText(this, "Venta eliminada exitosamente. Productos devueltos al inventario.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        System.out.println("DEBUG: ❌ Error al eliminar venta: " + e.getMessage());
                        Toast.makeText(this, "Error al eliminar venta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            });
        });
    }
    
    /**
     * Elimina la deuda asociada a una venta (si existe)
     */
    private void eliminarDeudaAsociada(String ventaId, Runnable onComplete) {
        DatabaseReference deudasRef = FirebaseDatabase.getInstance().getReference("deudas");
        
        deudasRef.orderByChild("ventaId").equalTo(ventaId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        System.out.println("DEBUG: 🔍 Deuda asociada encontrada para venta " + ventaId);
                        
                        // Eliminar todas las deudas asociadas (normalmente debería ser solo una)
                        final int[] deudasEliminadas = {0};
                        final int totalDeudas = (int) snapshot.getChildrenCount();
                        
                        for (DataSnapshot deudaSnapshot : snapshot.getChildren()) {
                            String deudaId = deudaSnapshot.getKey();
                            System.out.println("DEBUG: 🗑️ Eliminando deuda ID: " + deudaId);
                            
                            deudasRef.child(deudaId).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    deudasEliminadas[0]++;
                                    System.out.println("DEBUG: ✅ Deuda " + deudaId + " eliminada exitosamente (" + deudasEliminadas[0] + "/" + totalDeudas + ")");
                                    
                                    if (deudasEliminadas[0] == totalDeudas) {
                                        System.out.println("DEBUG: ✅ Todas las deudas asociadas eliminadas");
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("DEBUG: ❌ Error al eliminar deuda " + deudaId + ": " + e.getMessage());
                                    deudasEliminadas[0]++;
                                    if (deudasEliminadas[0] == totalDeudas) {
                                        onComplete.run();
                                    }
                                });
                        }
                    } else {
                        System.out.println("DEBUG: ℹ️ No hay deuda asociada para venta " + ventaId);
                        onComplete.run();
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("DEBUG: ⚠️ Error al buscar deuda asociada: " + error.getMessage());
                    // Continuar con la eliminación de la venta aunque haya error al buscar la deuda
                    onComplete.run();
                }
            });
    }
    
    /**
     * Devuelve los productos de una venta al inventario (suma la cantidad vendida al stock actual)
     */
    private void devolverProductosAlInventario(Venta venta, Runnable onComplete) {
        DatabaseReference productosRef = FirebaseDatabase.getInstance().getReference("productos");
        
        // Obtener los items de la venta
        List<VentaItem> ventaItems = venta.getItems();
        
        if (ventaItems == null || ventaItems.isEmpty()) {
            System.out.println("DEBUG: ⚠️ La venta no tiene productos para devolver al inventario");
            onComplete.run();
            return;
        }
        
        System.out.println("DEBUG: 🔄 Devolviendo " + ventaItems.size() + " productos al inventario para venta " + venta.getId());
        
        final int[] productosProcesados = {0};
        final int[] productosConError = {0};
        final int totalProductos = ventaItems.size();
        
        for (VentaItem ventaItem : ventaItems) {
            String productoId = ventaItem.getProductoId();
            double cantidadVendida = ventaItem.getCantidad();
            String nombreProducto = ventaItem.getNombre();
            
            if (productoId == null || productoId.isEmpty()) {
                System.out.println("WARNING: VentaItem sin ID de producto, saltando: " + nombreProducto);
                productosConError[0]++;
                productosProcesados[0]++;
                if (productosProcesados[0] == totalProductos) {
                    onComplete.run();
                }
                continue;
            }
            
            // Obtener el producto actual desde Firebase
            productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Producto producto = snapshot.getValue(Producto.class);
                        if (producto != null) {
                            int stockActual = producto.getStock();
                            String nombreProductoFinal = producto.getNombre();
                            int nuevoStock = stockActual + (int) cantidadVendida; // Sumar la cantidad vendida
                            
                            // Actualizar el stock en Firebase
                            DatabaseReference productoRef = productosRef.child(productoId);
                            productoRef.child("stock").setValue(nuevoStock)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("DEBUG: ✓ Stock devuelto para " + nombreProductoFinal + 
                                                     " - Stock anterior: " + stockActual + ", Nuevo stock: " + nuevoStock + 
                                                     " (se devolvieron " + (int) cantidadVendida + " unidades)");
                                    productosProcesados[0]++;
                                    if (productosProcesados[0] == totalProductos) {
                                        System.out.println("DEBUG: ✅ Todos los productos devueltos al inventario");
                                        onComplete.run();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("ERROR: No se pudo devolver stock para " + nombreProductoFinal + ": " + e.getMessage());
                                    productosConError[0]++;
                                    productosProcesados[0]++;
                                    if (productosProcesados[0] == totalProductos) {
                                        System.out.println("DEBUG: ⚠️ Proceso completado con " + productosConError[0] + " errores");
                                        onComplete.run();
                                    }
                                });
                        } else {
                            System.out.println("ERROR: Producto null al obtener desde Firebase: " + productoId);
                            productosConError[0]++;
                            productosProcesados[0]++;
                            if (productosProcesados[0] == totalProductos) {
                                onComplete.run();
                            }
                        }
                    } else {
                        System.out.println("WARNING: Producto no encontrado en Firebase: " + productoId + " (" + nombreProducto + ")");
                        productosConError[0]++;
                        productosProcesados[0]++;
                        if (productosProcesados[0] == totalProductos) {
                            onComplete.run();
                        }
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("ERROR: Error al obtener producto desde Firebase: " + error.getMessage());
                    productosConError[0]++;
                    productosProcesados[0]++;
                    if (productosProcesados[0] == totalProductos) {
                        onComplete.run();
                    }
                }
            });
        }
        
        System.out.println("DEBUG: Iniciado proceso de devolución para " + totalProductos + " productos del inventario");
    }
    
    private void actualizarMetodoPago(Venta venta, String metodoPago, boolean esCredito) {
        // Verificar si ya está procesando
        if (processing) {
            Toast.makeText(this, "Ya se está procesando una operación. Por favor espera.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verificar si la venta ya fue procesada
        if (!venta.esPendiente()) {
            String mensaje = "La venta ya fue procesada como: " + venta.getEstadoPago();
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            return;
        }
        
        if (venta.getId() == null || venta.getId().isEmpty()) {
            Toast.makeText(this, "Error: La venta no tiene ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Activar flag de procesamiento
        processing = true;
        
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        DatabaseReference ventaRef = ventasRef.child(venta.getId());
        
        // Determinar el nuevo estadoPago
        String nuevoEstadoPago = esCredito ? "CREDITO" : "CONTADO";
        String nuevoEstado = esCredito ? "Pendiente" : "Completada";
        
        // Si es crédito, verificar si ya existe una deuda para esta venta
        if (esCredito) {
            DatabaseReference deudasRef = FirebaseDatabase.getInstance().getReference("deudas");
            deudasRef.orderByChild("ventaId").equalTo(venta.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                            processing = false;
                            Toast.makeText(VentasActivity.this, 
                                "Ya existe una deuda para esta venta", 
                                Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // No existe deuda, proceder con la actualización
                        actualizarVentaYCrearDeuda(venta, ventaRef, metodoPago, nuevoEstadoPago, nuevoEstado, esCredito);
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        processing = false;
                        Toast.makeText(VentasActivity.this, 
                            "Error al verificar deudas: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            // Es contado, actualizar directamente
            actualizarVentaYCrearDeuda(venta, ventaRef, metodoPago, nuevoEstadoPago, nuevoEstado, esCredito);
        }
    }
    
    private void actualizarVentaYCrearDeuda(Venta venta, DatabaseReference ventaRef, 
                                            String metodoPago, String nuevoEstadoPago, 
                                            String nuevoEstado, boolean esCredito) {
        // Actualizar método de pago
        ventaRef.child("metodoPago").setValue(metodoPago)
            .addOnSuccessListener(aVoid -> {
                // Actualizar estadoPago
                ventaRef.child("estadoPago").setValue(nuevoEstadoPago)
                    .addOnSuccessListener(aVoid2 -> {
                        // Actualizar estado
                        ventaRef.child("estado").setValue(nuevoEstado)
                            .addOnSuccessListener(aVoid3 -> {
                                // Actualizar objeto localmente
                                venta.setMetodoPago(metodoPago);
                                venta.setEstadoPago(nuevoEstadoPago);
                                venta.setEstado(nuevoEstado);
                                
                                // Si es crédito, crear la deuda
                                if (esCredito) {
                                    crearDeudaDesdeVenta(venta);
                                } else {
                                    processing = false;
                                    ventasAdapter.notifyDataSetChanged();
                                    Toast.makeText(this, 
                                        "Venta marcada como CONTADO exitosamente", 
                                        Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                processing = false;
                                Toast.makeText(this, 
                                    "Error al actualizar estado: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                    })
                    .addOnFailureListener(e -> {
                        processing = false;
                        Toast.makeText(this, 
                            "Error al actualizar estadoPago: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                processing = false;
                Toast.makeText(this, 
                    "Error al actualizar método de pago: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void crearDeudaDesdeVenta(Venta venta) {
        DatabaseReference deudasRef = FirebaseDatabase.getInstance().getReference("deudas");
        
        // Verificar nuevamente que no exista deuda para esta venta
        deudasRef.orderByChild("ventaId").equalTo(venta.getId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                        processing = false;
                        Toast.makeText(VentasActivity.this, 
                            "Ya existe una deuda para esta venta", 
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // Crear nueva deuda
                    Deuda nuevaDeuda = new Deuda();
                    nuevaDeuda.setClienteId(venta.getClienteId());
                    nuevaDeuda.setClienteNombre(venta.getClienteNombre());
                    nuevaDeuda.setCotizacionId(venta.getCotizacionId());
                    nuevaDeuda.setCotizacionCorrelativo(venta.getCotizacionCorrelativo());
                    nuevaDeuda.setVentaId(venta.getId());
                    nuevaDeuda.setVentaCorrelativo("V" + venta.getId()); // Generar correlativo de venta
                    nuevaDeuda.setTotal(venta.getTotal());
                    nuevaDeuda.setSaldoPendiente(venta.getTotal());
                    nuevaDeuda.setMetodoPago("CREDITO");
                    nuevaDeuda.setMoneda(venta.getMoneda());
                    
                    // Copiar información de conversión de la venta a la deuda
                    nuevaDeuda.setMonedaOriginal(venta.getMonedaOriginal());
                    nuevaDeuda.setTotalOriginal(venta.getTotalOriginal());
                    nuevaDeuda.setTipoCambioUsado(venta.getTipoCambioUsado());
                    nuevaDeuda.setTotalEnSoles(venta.getTotalEnSoles());
                    nuevaDeuda.setTotalEnDolares(venta.getTotalEnDolares());
                    String fechaCreacion = venta.getFechaVenta();
                    if (fechaCreacion == null || fechaCreacion.trim().isEmpty()) {
                        fechaCreacion = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                    }
                    nuevaDeuda.setFechaCreacion(fechaCreacion);
                    
                    // Calcular fecha de vencimiento (30 días después de la venta)
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        java.util.Date fechaVenta = sdf.parse(venta.getFechaVenta());
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(fechaVenta);
                        cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
                        nuevaDeuda.setFechaVencimiento(sdf.format(cal.getTime()));
                    } catch (Exception e) {
                        // Si hay error, usar fecha por defecto
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        nuevaDeuda.setFechaVencimiento(sdf.format(cal.getTime()));
                    }
                    
                    // Guardar deuda en Firebase
                    String deudaId = deudasRef.push().getKey();
                    nuevaDeuda.setId(deudaId);
                    
                    // Guardar explícitamente todos los campos, especialmente los de conversión
                    java.util.Map<String, Object> deudaMap = new java.util.HashMap<>();
                    deudaMap.put("id", nuevaDeuda.getId());
                    deudaMap.put("clienteId", nuevaDeuda.getClienteId());
                    deudaMap.put("clienteNombre", nuevaDeuda.getClienteNombre());
                    deudaMap.put("cotizacionId", nuevaDeuda.getCotizacionId());
                    deudaMap.put("cotizacionCorrelativo", nuevaDeuda.getCotizacionCorrelativo());
                    deudaMap.put("ventaId", nuevaDeuda.getVentaId());
                    deudaMap.put("ventaCorrelativo", nuevaDeuda.getVentaCorrelativo());
                    deudaMap.put("total", nuevaDeuda.getTotal());
                    deudaMap.put("saldoPendiente", nuevaDeuda.getSaldoPendiente());
                    deudaMap.put("totalAbonado", nuevaDeuda.getTotalAbonado());
                    deudaMap.put("fechaVencimiento", nuevaDeuda.getFechaVencimiento());
                    deudaMap.put("fechaCreacion", nuevaDeuda.getFechaCreacion());
                    deudaMap.put("estado", nuevaDeuda.getEstado());
                    deudaMap.put("metodoPago", nuevaDeuda.getMetodoPago());
                    deudaMap.put("moneda", nuevaDeuda.getMoneda());
                    
                    // Campos de conversión (CRÍTICOS)
                    deudaMap.put("monedaOriginal", nuevaDeuda.getMonedaOriginal());
                    deudaMap.put("totalOriginal", nuevaDeuda.getTotalOriginal());
                    deudaMap.put("tipoCambioUsado", nuevaDeuda.getTipoCambioUsado());
                    deudaMap.put("totalEnSoles", nuevaDeuda.getTotalEnSoles());
                    deudaMap.put("totalEnDolares", nuevaDeuda.getTotalEnDolares());
                    
                    System.out.println("DEBUG: 💾 Guardando deuda en Firebase:");
                    System.out.println("DEBUG:    - Deuda ID: " + deudaId);
                    System.out.println("DEBUG:    - Moneda original: " + nuevaDeuda.getMonedaOriginal());
                    System.out.println("DEBUG:    - Total original: " + nuevaDeuda.getTotalOriginal());
                    System.out.println("DEBUG:    - Tipo cambio usado: " + nuevaDeuda.getTipoCambioUsado());
                    System.out.println("DEBUG:    - Total en soles: " + nuevaDeuda.getTotalEnSoles());
                    System.out.println("DEBUG:    - Total en dólares: " + nuevaDeuda.getTotalEnDolares());
                    
                    deudasRef.child(deudaId).setValue(deudaMap)
                        .addOnSuccessListener(aVoid -> {
                            processing = false;
                            ventasAdapter.notifyDataSetChanged();
                            Toast.makeText(VentasActivity.this, 
                                "Venta marcada como CRÉDITO y deuda creada exitosamente", 
                                Toast.LENGTH_LONG).show();
                            
                            // Navegar a SaldosActivity
                            Intent intent = new Intent(VentasActivity.this, SaldosActivity.class);
                            intent.putExtra("ventaId", venta.getId());
                            intent.putExtra("clienteNombre", venta.getClienteNombre());
                            intent.putExtra("total", venta.getTotal());
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            processing = false;
                            Toast.makeText(VentasActivity.this, 
                                "Error al crear deuda: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        });
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    processing = false;
                    Toast.makeText(VentasActivity.this, 
                        "Error al verificar deudas: " + error.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar datos al volver de otra actividad
        loadDashboardData();
        if (db != null) {
            loadVentas(); // Recargar ventas para actualizar estados
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover listener de Firebase para evitar memory leaks
        if (ventasListener != null && db != null) {
            db.child("ventas").removeEventListener(ventasListener);
            ventasListener = null;
        }
    }
}
