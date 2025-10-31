package com.example.appelite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private TextView tvTitle, tvEmpty, tvContadorFiltro;
    private LinearLayout btnTodas, btnPendientes, btnAceptadas, btnVencidas;
    private RecyclerView recyclerCotizaciones;
    private FloatingActionButton fabNuevaCotizacion;
    private ImageButton btnRecargar;
    
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
        tvContadorFiltro = findViewById(R.id.tvContadorFiltro);
        
        // Botones de filtro
        btnTodas = findViewById(R.id.btnTodas);
        btnPendientes = findViewById(R.id.btnPendientes);
        btnAceptadas = findViewById(R.id.btnAceptadas);
        btnVencidas = findViewById(R.id.btnVencidas);
        
        recyclerCotizaciones = findViewById(R.id.recyclerCotizaciones);
        fabNuevaCotizacion = findViewById(R.id.fabNuevaCotizacion);
        btnRecargar = findViewById(R.id.btnRecargar);
        
        // Configurar título
        if (tvTitle != null) {
            tvTitle.setText("Cotizaciones");
        }
    }
    
    private void initFirebase() {
        System.out.println("DEBUG: Inicializando Firebase...");
        cotizacionesRef = FirebaseDatabase.getInstance().getReference("cotizaciones");
        auth = FirebaseAuth.getInstance();
        cotizaciones = new ArrayList<>();
        cotizacionesFiltradas = new ArrayList<>();
        
        System.out.println("DEBUG: Firebase Database conectado: " + FirebaseDatabase.getInstance().getApp().getName());
        System.out.println("DEBUG: Referencia cotizaciones: " + cotizacionesRef.toString());
        
        // Verificar conexión a Firebase
        cotizacionesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("DEBUG: Conexión a Firebase exitosa");
            } else {
                System.out.println("DEBUG: Error de conexión a Firebase: " + task.getException());
            }
        });
    }
    
    private void setupRecyclerView() {
        System.out.println("DEBUG: Configurando RecyclerView...");
        adapter = new CotizacionesAdapter(cotizacionesFiltradas, new CotizacionesAdapter.OnCotizacionClickListener() {
            @Override
            public void onEditarCotizacion(Cotizacion cotizacion) {
                // TODO: Implementar edición
                Toast.makeText(CotizacionesActivity.this, "Editar: " + cotizacion.getCorrelativo(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onEliminarCotizacion(Cotizacion cotizacion) {
                mostrarConfirmacionEliminar(cotizacion);
            }
            
            @Override
            public void onDescargarPDF(Cotizacion cotizacion) {
                descargarPDF(cotizacion);
            }
            
            @Override
            public void onVerDetalles(Cotizacion cotizacion) {
                // TODO: Implementar ver detalles
                Toast.makeText(CotizacionesActivity.this, "Ver detalles: " + cotizacion.getCorrelativo(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onAceptarCotizacion(Cotizacion cotizacion) {
                aceptarCotizacion(cotizacion);
            }
            
            @Override
            public void onSwipeActionSelected(Cotizacion cotizacion, int position) {
                // Mostrar diálogo cuando se hace swipe
                mostrarOpcionesSwipe(cotizacion, position);
            }
        });
        
        recyclerCotizaciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerCotizaciones.setAdapter(adapter);
        
        // Configurar swipe actions
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CotizacionesAdapter.SwipeToDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerCotizaciones);
        
        System.out.println("DEBUG: RecyclerView configurado correctamente");
    }
    
    private void mostrarConfirmacionEliminar(Cotizacion cotizacion) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Eliminar Cotización")
               .setMessage("¿Estás seguro de que quieres eliminar la cotización " + cotizacion.getCorrelativo() + "?")
               .setPositiveButton("Eliminar", (dialog, which) -> {
                   eliminarCotizacion(cotizacion);
               })
               .setNegativeButton("Cancelar", null)
               .show();
    }
    
    private void eliminarCotizacion(Cotizacion cotizacion) {
        cotizacionesRef.child(cotizacion.getId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cotización eliminada", Toast.LENGTH_SHORT).show();
                loadCotizaciones(); // Recargar lista
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al eliminar cotización", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void aceptarCotizacion(Cotizacion cotizacion) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Aceptar Cotización")
               .setMessage("¿Estás seguro de que quieres aceptar la cotización " + cotizacion.getCorrelativo() + "?\n\nLa cotización se agregará al historial de ventas.")
               .setPositiveButton("Aceptar", (dialog, which) -> {
                   // Mostrar diálogo de método de pago
                   mostrarDialogoMetodoPago(cotizacion);
               })
               .setNegativeButton("Cancelar", null)
               .show();
    }
    
    private void mostrarDialogoMetodoPago(Cotizacion cotizacion) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Método de Pago")
               .setItems(new String[]{"Contado", "Crédito / Deuda"}, (dialog, which) -> {
                   String metodoPago = (which == 0) ? "Contado" : "Crédito";
                   // Marcar cotización como aceptada
                   cotizacion.setEstado("ACEPTADA");
                   cotizacionesRef.child(cotizacion.getId()).setValue(cotizacion)
                       .addOnSuccessListener(aVoid -> {
                           // Crear venta en el historial
                           crearVentaDesdeCotizacion(cotizacion, metodoPago, which == 1);
                           Toast.makeText(this, "Cotización aceptada y agregada al historial de ventas", Toast.LENGTH_SHORT).show();
                           loadCotizaciones(); // Recargar lista
                       })
                       .addOnFailureListener(e -> {
                           Toast.makeText(this, "Error al actualizar cotización", Toast.LENGTH_SHORT).show();
                       });
               })
               .setNegativeButton("Cancelar", null)
               .show();
    }
    
    private void crearVentaDesdeCotizacion(Cotizacion cotizacion, String metodoPago, boolean esCredito) {
        // Obtener fecha y hora actual
        java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.util.Date ahora = new java.util.Date();
        String fechaVenta = sdfFecha.format(ahora);
        String horaVenta = sdfHora.format(ahora);
        
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
        
        // Crear objeto Venta
        Venta venta = new Venta();
        venta.setClienteId(cotizacion.getClienteId() != null ? cotizacion.getClienteId() : "");
        venta.setClienteNombre(cotizacion.getClienteNombre());
        venta.setClienteDocumento(""); // Se puede obtener del cliente si es necesario
        venta.setFechaVenta(fechaVenta);
        venta.setHoraVenta(horaVenta);
        venta.setSubtotal(cotizacion.getSubtotal());
        venta.setDescuento(cotizacion.getDescuentoGeneral());
        venta.setIgv(cotizacion.getIgv());
        venta.setTotal(cotizacion.getTotal());
        venta.setMoneda(cotizacion.getMoneda().contains("SOLES") ? "PEN" : "USD");
        venta.setMetodoPago(metodoPago);
        venta.setEstado(esCredito ? "Pendiente" : "Completada");
        venta.setItems(ventaItems);
        venta.setCotizacionId(cotizacion.getId()); // Referencia a la cotización original
        
        // Guardar en Firebase en el nodo "ventas" (sin userId para que aparezca en VerVentasActivity)
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        String ventaId = ventasRef.push().getKey();
        venta.setId(ventaId);
        
        ventasRef.child(ventaId).setValue(venta)
            .addOnSuccessListener(aVoid -> {
                System.out.println("DEBUG: Venta creada exitosamente desde cotización: " + cotizacion.getCorrelativo());
                // Si es crédito, navegar a la pantalla de saldos
                if (esCredito) {
                    Intent intent = new Intent(CotizacionesActivity.this, SaldosActivity.class);
                    intent.putExtra("ventaId", ventaId);
                    intent.putExtra("clienteNombre", venta.getClienteNombre());
                    intent.putExtra("total", venta.getTotal());
                    startActivity(intent);
                }
            })
            .addOnFailureListener(e -> {
                System.out.println("ERROR: No se pudo crear la venta: " + e.getMessage());
                Toast.makeText(this, "Cotización aceptada, pero error al agregar al historial de ventas", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void mostrarOpcionesSwipe(Cotizacion cotizacion, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Acción para " + cotizacion.getCorrelativo())
               .setMessage("¿Qué acción deseas realizar?")
               .setPositiveButton("✅ Aceptar", (dialog, which) -> {
                   aceptarCotizacion(cotizacion);
               })
               .setNegativeButton("🗑️ Eliminar", (dialog, which) -> {
                   mostrarConfirmacionEliminar(cotizacion);
               })
               .setNeutralButton("Cancelar", (dialog, which) -> {
                   // Recargar la lista para que regrese a la posición normal
                   loadCotizaciones();
               })
               .setOnCancelListener(dialog -> {
                   // Si cancela, también recargar
                   loadCotizaciones();
               })
               .show();
    }
    
    private void descargarPDF(Cotizacion cotizacion) {
        Toast.makeText(this, "Generando PDF para: " + cotizacion.getCorrelativo(), Toast.LENGTH_SHORT).show();
        
        // Generar PDF en hilo secundario
        new Thread(() -> {
            String rutaArchivo = PDFGenerator.generarPDFCotizacion(this, cotizacion);
            
            runOnUiThread(() -> {
                if (rutaArchivo != null) {
                    mostrarOpcionesPDF(rutaArchivo, cotizacion);
                } else {
                    Toast.makeText(this, "Error al generar PDF", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void mostrarOpcionesPDF(String rutaArchivo, Cotizacion cotizacion) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("PDF Generado")
               .setMessage("¿Qué deseas hacer con el PDF de la cotización " + cotizacion.getCorrelativo() + "?")
               .setPositiveButton("Ver PDF", (dialog, which) -> {
                   verPDF(rutaArchivo);
               })
               .setNeutralButton("Compartir WhatsApp", (dialog, which) -> {
                   compartirPorWhatsApp(rutaArchivo, cotizacion);
               })
               .setNegativeButton("Solo Guardar", (dialog, which) -> {
                   Toast.makeText(this, "PDF guardado en Descargas/Cotizaciones/", Toast.LENGTH_SHORT).show();
               })
               .show();
    }
    
    private void verPDF(String rutaArchivo) {
        try {
            java.io.File archivo = new java.io.File(rutaArchivo);
            Uri uri = FileProvider.getUriForFile(this, 
                "com.example.appelite.fileprovider", archivo);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void compartirPorWhatsApp(String rutaArchivo, Cotizacion cotizacion) {
        try {
            java.io.File archivo = new java.io.File(rutaArchivo);
            Uri uri = FileProvider.getUriForFile(this, 
                "com.example.appelite.fileprovider", archivo);
            
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cotización " + cotizacion.getCorrelativo());
            intent.putExtra(Intent.EXTRA_TEXT, "Hola, te envío la cotización " + cotizacion.getCorrelativo() + 
                         " por un total de " + cotizacion.getTotal() + " " + cotizacion.getMoneda() + 
                         ". ¡Gracias por tu preferencia!");
            
            // Intentar abrir específicamente WhatsApp
            intent.setPackage("com.whatsapp");
            
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Si WhatsApp no está disponible, mostrar selector de aplicaciones
                intent.setPackage(null);
                startActivity(Intent.createChooser(intent, "Compartir cotización por:"));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al compartir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
            
            btnRecargar.setOnClickListener(v -> {
                Toast.makeText(this, "Recargando desde Firebase...", Toast.LENGTH_SHORT).show();
                recargarDesdeFirebase();
            });
            
            // Long click para crear cotización de prueba
            btnRecargar.setOnLongClickListener(v -> {
                crearCotizacionPruebaEnFirebase();
                return true;
            });
        }
    }
    
    private void loadCotizaciones() {
        System.out.println("DEBUG: Cargando cotizaciones desde Firebase");
        System.out.println("DEBUG: Referencia Firebase: " + cotizacionesRef.toString());
        
        cotizacionesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("DEBUG: Datos recibidos de Firebase - Total snapshots: " + dataSnapshot.getChildrenCount());
                System.out.println("DEBUG: Ruta Firebase: " + dataSnapshot.getRef().toString());
                cotizaciones.clear();
                
                if (dataSnapshot.getChildrenCount() == 0) {
                    System.out.println("DEBUG: No hay cotizaciones en Firebase, cargando datos de prueba");
                    cargarDatosPrueba();
                    return;
                }
                
                System.out.println("DEBUG: Procesando " + dataSnapshot.getChildrenCount() + " cotizaciones desde Firebase...");
                
                int contador = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        System.out.println("DEBUG: Procesando snapshot: " + snapshot.getKey());
                    Cotizacion cotizacion = snapshot.getValue(Cotizacion.class);
                    if (cotizacion != null) {
                        cotizacion.setId(snapshot.getKey());
                        
                        // Actualizar estado automáticamente si está vencida
                        if (cotizacion.estaVencida() && !"ACEPTADA".equals(cotizacion.getEstado()) && !"VENCIDA".equals(cotizacion.getEstado())) {
                            cotizacion.setEstado("VENCIDA");
                            // Actualizar en Firebase
                            cotizacionesRef.child(cotizacion.getId()).child("estado").setValue("VENCIDA");
                            System.out.println("DEBUG: Cotización " + cotizacion.getCorrelativo() + " marcada como VENCIDA automáticamente");
                        }
                        
                        cotizaciones.add(cotizacion);
                            contador++;
                            System.out.println("DEBUG: Cotización cargada - ID: " + snapshot.getKey() + 
                                             ", Cliente: " + cotizacion.getClienteNombre() + 
                                             ", Estado: " + cotizacion.getEstado() + 
                                             ", Total: " + cotizacion.getTotal());
                        } else {
                            System.out.println("DEBUG: Cotización es null para snapshot: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR procesando cotización: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("DEBUG: Total cotizaciones cargadas: " + contador);
                
                // CRÍTICO: Aplicar filtro inmediatamente después de cargar
                System.out.println("DEBUG: Aplicando filtro inmediatamente después de cargar datos");
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
        cot1.setClienteNombre("Andrea Agurto");
        cot1.setEstado("PENDIENTE");
        cot1.setCorrelativo("C001-00000001");
        cot1.setFechaEmision("15/01/2025");
        cot1.setTotal(215.50);
        cot1.setMoneda("SOLES (S/)");
        cotizaciones.add(cot1);
        
        Cotizacion cot2 = new Cotizacion();
        cot2.setId("2");
        cot2.setClienteNombre("Ana López");
        cot2.setEstado("ACEPTADA");
        cot2.setCorrelativo("C001-00000002");
        cot2.setFechaEmision("14/01/2025");
        cot2.setTotal(550.00);
        cot2.setMoneda("DÓLARES (USD)");
        cotizaciones.add(cot2);
        
        Cotizacion cot3 = new Cotizacion();
        cot3.setId("3");
        cot3.setClienteNombre("Carlos Mendoza");
        cot3.setEstado("VENCIDA");
        cot3.setCorrelativo("C001-00000003");
        cot3.setFechaEmision("10/01/2025");
        cot3.setTotal(120.75);
        cot3.setMoneda("SOLES (S/)");
        cotizaciones.add(cot3);
        
        Cotizacion cot4 = new Cotizacion();
        cot4.setId("4");
        cot4.setClienteNombre("María García");
        cot4.setEstado("PENDIENTE");
        cot4.setCorrelativo("C001-00000004");
        cot4.setFechaEmision("16/01/2025");
        cot4.setTotal(890.25);
        cot4.setMoneda("DÓLARES (USD)");
        cotizaciones.add(cot4);
        
        System.out.println("DEBUG: Datos de prueba cargados: " + cotizaciones.size() + " cotizaciones");
        aplicarFiltro(filtroActual);
    }
    
    // Método para forzar recarga desde Firebase
    private void recargarDesdeFirebase() {
        System.out.println("DEBUG: Forzando recarga desde Firebase...");
        
        // Primero, verificar qué hay en Firebase directamente
        cotizacionesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                System.out.println("DEBUG: Datos directos de Firebase: " + snapshot.getChildrenCount() + " cotizaciones");
                
                if (snapshot.getChildrenCount() > 0) {
                    System.out.println("DEBUG: Hay " + snapshot.getChildrenCount() + " cotizaciones en Firebase");
                    for (DataSnapshot child : snapshot.getChildren()) {
                        System.out.println("DEBUG: Cotización encontrada - ID: " + child.getKey());
                        System.out.println("DEBUG: Datos: " + child.getValue());
                    }
                } else {
                    System.out.println("DEBUG: No hay cotizaciones en Firebase");
                }
                
                // Ahora recargar con el listener
                loadCotizaciones();
            } else {
                System.out.println("DEBUG: Error al acceder a Firebase: " + task.getException());
            }
        });
    }
    
    private void crearCotizacionPruebaEnFirebase() {
        System.out.println("DEBUG: Creando cotización de prueba en Firebase...");
        
        Cotizacion cotizacionPrueba = new Cotizacion();
        cotizacionPrueba.setCorrelativo("C001-00000999");
        cotizacionPrueba.setClienteNombre("CLIENTE PRUEBA FIREBASE");
        cotizacionPrueba.setEstado("PENDIENTE");
        cotizacionPrueba.setFechaEmision("15/01/2025");
        cotizacionPrueba.setTotal(999.99);
        cotizacionPrueba.setMoneda("SOLES (S/)");
        cotizacionPrueba.setTipoDocumento("COTIZACIÓN");
        
        String pruebaId = cotizacionesRef.push().getKey();
        cotizacionPrueba.setId(pruebaId);
        
        cotizacionesRef.child(pruebaId).setValue(cotizacionPrueba)
            .addOnSuccessListener(aVoid -> {
                System.out.println("DEBUG: Cotización de prueba creada exitosamente con ID: " + pruebaId);
                Toast.makeText(this, "Cotización de prueba creada!", Toast.LENGTH_SHORT).show();
                recargarDesdeFirebase();
            })
            .addOnFailureListener(e -> {
                System.out.println("DEBUG: Error creando cotización de prueba: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
                    // Mostrar solo pendientes que NO estén vencidas
                    if ("Pendiente".equalsIgnoreCase(c.getEstado()) && !c.estaVencida()) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                    }
                }
                break;
            case "Aceptadas":
                for (Cotizacion c : cotizaciones) {
                    if ("Aceptada".equalsIgnoreCase(c.getEstado())) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                    }
                }
                break;
            case "Vencidas":
                for (Cotizacion c : cotizaciones) {
                    // Mostrar todas las vencidas (independientemente de su estado anterior)
                    if ("Vencida".equalsIgnoreCase(c.getEstado()) || c.estaVencida()) {
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
        System.out.println("DEBUG: cotizacionesFiltradas.size() = " + cotizacionesFiltradas.size());
        
        if (adapter != null) {
        adapter.notifyDataSetChanged();
            System.out.println("DEBUG: Adapter notificado de cambios - ItemCount: " + adapter.getItemCount());
        } else {
            System.out.println("ERROR: Adapter es null en aplicarFiltro!");
        }
        
        actualizarBotonesEstado(filtro);
        
        // Actualizar contador de cotizaciones
        if (tvContadorFiltro != null) {
            String textoContador = contadorFiltrado + (contadorFiltrado == 1 ? " cotización" : " cotizaciones");
            tvContadorFiltro.setText(textoContador);
        }
        
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
        // Resetear todos los botones a inactivos
        btnTodas.setBackgroundResource(R.drawable.filter_button_inactive);
        btnPendientes.setBackgroundResource(R.drawable.filter_button_inactive);
        btnAceptadas.setBackgroundResource(R.drawable.filter_button_inactive);
        btnVencidas.setBackgroundResource(R.drawable.filter_button_inactive);
        
        // Cambiar colores de texto a gris
        ((TextView) btnTodas.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_primary));
        ((TextView) btnPendientes.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_primary));
        ((TextView) btnAceptadas.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_primary));
        ((TextView) btnVencidas.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_primary));
        
        // Activar el botón seleccionado
        switch (filtroActivo) {
            case "Todas":
                btnTodas.setBackgroundResource(R.drawable.filter_button_active);
                ((TextView) btnTodas.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                break;
            case "Pendientes":
                btnPendientes.setBackgroundResource(R.drawable.filter_button_active);
                ((TextView) btnPendientes.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                break;
            case "Aceptadas":
                btnAceptadas.setBackgroundResource(R.drawable.filter_button_active);
                ((TextView) btnAceptadas.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                break;
            case "Vencidas":
                btnVencidas.setBackgroundResource(R.drawable.filter_button_active);
                ((TextView) btnVencidas.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
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
