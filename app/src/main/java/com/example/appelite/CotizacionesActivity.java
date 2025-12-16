package com.example.appelite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cotizaciones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
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
                // Navegar a NuevaCotizacionActivity en modo edición
                Intent intent = new Intent(CotizacionesActivity.this, NuevaCotizacionActivity.class);
                intent.putExtra("cotizacion_editar", cotizacion);
                startActivity(intent);
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
        // Verificar si la cotización ya fue aceptada (ya existe una venta con este cotizacionId)
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        ventasRef.orderByChild("cotizacionId").equalTo(cotizacion.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    // Ya existe una venta para esta cotización
                    Toast.makeText(CotizacionesActivity.this, 
                        "Esta cotización ya fue aceptada anteriormente", 
                        Toast.LENGTH_LONG).show();
                    return;
                }
                
                // La cotización no ha sido aceptada, proceder con la aceptación
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CotizacionesActivity.this);
                builder.setTitle("Aceptar Cotización")
                       .setMessage("¿Estás seguro de que quieres aceptar la cotización " + cotizacion.getCorrelativo() + "?\n\nSelecciona el método de pago:")
                       .setPositiveButton("Contado", (dialog, which) -> {
                           // Marcar cotización como aceptada y crear venta como CONTADO
                           cotizacion.setEstado("ACEPTADA");
                           cotizacionesRef.child(cotizacion.getId()).setValue(cotizacion)
                               .addOnSuccessListener(aVoid -> {
                                   Log.d("CotizacionesActivity", "✅ Cotización aceptada: " + cotizacion.getCorrelativo() + " (ID: " + cotizacion.getId() + ")");
                                   crearVentaDesdeCotizacion(cotizacion, "CONTADO", false);
                                   Toast.makeText(CotizacionesActivity.this, "Cotización aceptada y agregada al historial de ventas", Toast.LENGTH_SHORT).show();
                                   loadCotizaciones(); // Recargar lista
                               })
                               .addOnFailureListener(e -> {
                                   Log.e("CotizacionesActivity", "❌ Error al actualizar cotización: " + e.getMessage());
                                   Toast.makeText(CotizacionesActivity.this, "Error al actualizar cotización", Toast.LENGTH_SHORT).show();
                               });
                       })
                       .setNeutralButton("Crédito", (dialog, which) -> {
                           // Marcar cotización como aceptada y crear venta como CRÉDITO
                           cotizacion.setEstado("ACEPTADA");
                           cotizacionesRef.child(cotizacion.getId()).setValue(cotizacion)
                               .addOnSuccessListener(aVoid -> {
                                   Log.d("CotizacionesActivity", "✅ Cotización aceptada: " + cotizacion.getCorrelativo() + " (ID: " + cotizacion.getId() + ") - CRÉDITO");
                                   crearVentaDesdeCotizacion(cotizacion, "CRÉDITO", true);
                                   Toast.makeText(CotizacionesActivity.this, "Cotización aceptada como crédito y agregada al historial", Toast.LENGTH_SHORT).show();
                                   loadCotizaciones(); // Recargar lista
                               })
                               .addOnFailureListener(e -> {
                                   Log.e("CotizacionesActivity", "❌ Error al actualizar cotización: " + e.getMessage());
                                   Toast.makeText(CotizacionesActivity.this, "Error al actualizar cotización", Toast.LENGTH_SHORT).show();
                               });
                       })
                       .setNegativeButton("Cancelar", null)
                       .show();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("CotizacionesActivity", "ERROR verificando si la cotización ya fue aceptada: " + error.getMessage());
                // En caso de error, proceder con la aceptación
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CotizacionesActivity.this);
                builder.setTitle("Aceptar Cotización")
                       .setMessage("¿Estás seguro de que quieres aceptar la cotización " + cotizacion.getCorrelativo() + "?\n\nSelecciona el método de pago:")
                       .setPositiveButton("Contado", (dialog, which) -> {
                           cotizacion.setEstado("ACEPTADA");
                           cotizacionesRef.child(cotizacion.getId()).setValue(cotizacion)
                               .addOnSuccessListener(aVoid -> {
                                   Log.d("CotizacionesActivity", "✅ Cotización aceptada: " + cotizacion.getCorrelativo() + " (ID: " + cotizacion.getId() + ")");
                                   crearVentaDesdeCotizacion(cotizacion, "CONTADO", false);
                                   Toast.makeText(CotizacionesActivity.this, "Cotización aceptada", Toast.LENGTH_SHORT).show();
                                   loadCotizaciones();
                               });
                       })
                       .setNeutralButton("Crédito", (dialog, which) -> {
                           cotizacion.setEstado("ACEPTADA");
                           cotizacionesRef.child(cotizacion.getId()).setValue(cotizacion)
                               .addOnSuccessListener(aVoid -> {
                                   Log.d("CotizacionesActivity", "✅ Cotización aceptada: " + cotizacion.getCorrelativo() + " (ID: " + cotizacion.getId() + ") - CRÉDITO");
                                   crearVentaDesdeCotizacion(cotizacion, "CRÉDITO", true);
                                   Toast.makeText(CotizacionesActivity.this, "Cotización aceptada como crédito", Toast.LENGTH_SHORT).show();
                                   loadCotizaciones();
                               });
                       })
                       .setNegativeButton("Cancelar", null)
                       .show();
            }
        });
    }
    
    private void crearVentaDesdeCotizacion(Cotizacion cotizacion, String metodoPago, boolean esCredito) {
        // Usar la fecha de emisión de la cotización como fecha de venta (para que aparezca al filtrar por esa fecha)
        java.text.SimpleDateFormat sdfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        
        String fechaVenta;
        if (cotizacion.getFechaEmision() != null && !cotizacion.getFechaEmision().isEmpty()) {
            // Normalizar la fecha de emisión al formato dd/MM/yyyy
            try {
                // Intentar parsear la fecha en diferentes formatos posibles
                String fechaEmision = cotizacion.getFechaEmision();
                // Si ya está en formato dd/MM/yyyy, usarla directamente
                if (fechaEmision.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    fechaVenta = fechaEmision;
                } else {
                    // Intentar parsear otros formatos
                    java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    java.util.Date fecha = sdfInput.parse(fechaEmision);
                    fechaVenta = sdfFecha.format(fecha);
                }
            } catch (Exception e) {
                Log.e("CotizacionesActivity", "Error parseando fecha de emisión: " + e.getMessage());
                // Si hay error, usar la fecha actual
                fechaVenta = sdfFecha.format(new java.util.Date());
            }
        } else {
            // Si no hay fecha de emisión, usar la fecha actual
            fechaVenta = sdfFecha.format(new java.util.Date());
        }
        
        String horaVenta = sdfHora.format(new java.util.Date());
        
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
        venta.setClienteNombre(cotizacion.getClienteNombre() != null ? cotizacion.getClienteNombre().toUpperCase() : "");
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
        
        // Configurar estadoPago según el método de pago seleccionado
        if (esCredito) {
            venta.setEstadoPago("CREDITO");
        } else {
            venta.setEstadoPago("CONTADO");
        }
        
        venta.setItems(ventaItems);
        venta.setCotizacionId(cotizacion.getId()); // Referencia a la cotización original
        venta.setCotizacionCorrelativo(cotizacion.getCorrelativo()); // Guardar el correlativo de la cotización
        
        // Guardar campos de conversión de moneda para calcular correctamente el total en soles
        String monedaOriginal = cotizacion.getMoneda().contains("SOLES") ? "PEN" : "USD";
        venta.setMonedaOriginal(monedaOriginal);
        venta.setTotalOriginal(cotizacion.getTotal());
        venta.setTipoCambioUsado(cotizacion.getTipoCambio());
        
        // Calcular y guardar total en soles
        double totalEnSoles;
        if ("USD".equals(monedaOriginal) && cotizacion.getTipoCambio() > 0) {
            totalEnSoles = cotizacion.getTotal() * cotizacion.getTipoCambio();
        } else {
            totalEnSoles = cotizacion.getTotal(); // Ya está en soles
        }
        venta.setTotalEnSoles(totalEnSoles);
        
        // Guardar en Firebase en el nodo "ventas" (sin userId para que aparezca en VerVentasActivity)
        DatabaseReference ventasRef = FirebaseDatabase.getInstance().getReference("ventas");
        String ventaId = ventasRef.push().getKey();
        venta.setId(ventaId);
        
        Log.d("CotizacionesActivity", "🔄 Creando venta desde cotización:");
        Log.d("CotizacionesActivity", "   - Cotización ID: " + cotizacion.getId());
        Log.d("CotizacionesActivity", "   - Cotización Correlativo: " + cotizacion.getCorrelativo());
        Log.d("CotizacionesActivity", "   - Fecha Emisión Cotización: " + cotizacion.getFechaEmision());
        Log.d("CotizacionesActivity", "   - Fecha Venta (usada): " + fechaVenta);
        Log.d("CotizacionesActivity", "   - Venta ID: " + ventaId);
        Log.d("CotizacionesActivity", "   - Método Pago: " + metodoPago);
        Log.d("CotizacionesActivity", "   - Estado Pago: " + venta.getEstadoPago());
        Log.d("CotizacionesActivity", "   - Total: " + venta.getTotal());
        Log.d("CotizacionesActivity", "   - Moneda: " + venta.getMoneda());
        
        ventasRef.child(ventaId).setValue(venta)
            .addOnSuccessListener(aVoid -> {
                Log.d("CotizacionesActivity", "✅ Venta creada exitosamente:");
                Log.d("CotizacionesActivity", "   - Venta ID: " + ventaId);
                Log.d("CotizacionesActivity", "   - Cotización ID: " + cotizacion.getId());
                Log.d("CotizacionesActivity", "   - Estado Pago: " + venta.getEstadoPago());
                
                // Si es crédito, crear deuda
                if (esCredito) {
                    crearDeudaDesdeVenta(venta, cotizacion);
                }
                
                // Descontar productos del inventario
                descontarProductosDelInventario(cotizacion);
            })
            .addOnFailureListener(e -> {
                Log.e("CotizacionesActivity", "❌ ERROR: No se pudo crear la venta:");
                Log.e("CotizacionesActivity", "   - Error: " + e.getMessage());
                Log.e("CotizacionesActivity", "   - Cotización ID: " + cotizacion.getId());
                Toast.makeText(this, "Cotización aceptada, pero error al agregar al historial de ventas", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void crearDeudaDesdeVenta(Venta venta, Cotizacion cotizacion) {
        // Crear deuda cuando la venta es a crédito
        DatabaseReference deudasRef = FirebaseDatabase.getInstance().getReference("deudas");
        String deudaId = deudasRef.push().getKey();
        
        // Calcular fecha de vencimiento (30 días desde hoy)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String fechaVencimiento = sdf.format(cal.getTime());
        
        Deuda deuda = new Deuda();
        deuda.setId(deudaId);
        deuda.setClienteId(venta.getClienteId());
        deuda.setClienteNombre(venta.getClienteNombre());
        deuda.setCotizacionId(cotizacion.getId());
        deuda.setVentaId(venta.getId());
        deuda.setVentaCorrelativo(venta.getCotizacionCorrelativo());
        deuda.setTotal(venta.getTotal());
        deuda.setSaldoPendiente(venta.getTotal());
        deuda.setTotalAbonado(0.0);
        deuda.setFechaVencimiento(fechaVencimiento);
        deuda.setFechaCreacion(venta.getFechaVenta());
        deuda.setMoneda(venta.getMoneda());
        
        deudasRef.child(deudaId).setValue(deuda)
            .addOnSuccessListener(aVoid -> {
                Log.d("CotizacionesActivity", "✅ Deuda creada exitosamente:");
                Log.d("CotizacionesActivity", "   - Deuda ID: " + deudaId);
                Log.d("CotizacionesActivity", "   - Venta ID: " + venta.getId());
                Log.d("CotizacionesActivity", "   - Monto: " + deuda.getTotal());
            })
            .addOnFailureListener(e -> {
                Log.e("CotizacionesActivity", "❌ ERROR al crear deuda: " + e.getMessage());
            });
    }
    
    /**
     * Descuenta las cantidades de productos del inventario cuando se acepta una cotización
     */
    private void descontarProductosDelInventario(Cotizacion cotizacion) {
        if (cotizacion.getProductos() == null || cotizacion.getProductos().isEmpty()) {
            System.out.println("DEBUG: No hay productos para descontar en la cotización " + cotizacion.getCorrelativo());
            return;
        }
        
        DatabaseReference productosRef = FirebaseDatabase.getInstance().getReference("productos");
        final int[] productosProcesados = {0};
        final int[] productosConError = {0};
        
        System.out.println("DEBUG: Descontando productos del inventario para cotización " + cotizacion.getCorrelativo());
        
        for (ProductoItem productoItem : cotizacion.getProductos()) {
            final String productoId = productoItem.getProductoId();
            final int cantidadVendida = productoItem.getCantidad();
            final String nombreProducto = productoItem.getNombre();
            
            if (productoId == null || productoId.isEmpty()) {
                System.out.println("WARNING: ProductoItem sin ID, saltando: " + nombreProducto);
                productosConError[0]++;
                continue;
            }
            
            // Obtener el producto actual desde Firebase
            productosRef.child(productoId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Producto producto = snapshot.getValue(Producto.class);
                        if (producto != null) {
                            final int stockActual = producto.getStock();
                            final String nombreProductoFinal = producto.getNombre();
                            int nuevoStock = stockActual - cantidadVendida;
                            
                            // Asegurar que el stock no sea negativo
                            if (nuevoStock < 0) {
                                System.out.println("WARNING: Stock insuficiente para producto " + nombreProductoFinal + 
                                                 ". Stock actual: " + stockActual + ", Cantidad vendida: " + cantidadVendida);
                                nuevoStock = 0; // No permitir stock negativo
                            }
                            
                            final int nuevoStockFinal = nuevoStock;
                            
                            // Actualizar el stock en Firebase
                            final DatabaseReference productoRef = productosRef.child(productoId);
                            productoRef.child("stock").setValue(nuevoStockFinal)
                                .addOnSuccessListener(aVoid -> {
                                    System.out.println("DEBUG: ✓ Stock actualizado para " + nombreProductoFinal + 
                                                     " - Stock anterior: " + stockActual + ", Nuevo stock: " + nuevoStockFinal);
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("ERROR: No se pudo actualizar stock para " + nombreProductoFinal + ": " + e.getMessage());
                                });
                        } else {
                            System.out.println("ERROR: Producto null al obtener desde Firebase: " + productoId);
                        }
                    } else {
                        System.out.println("WARNING: Producto no encontrado en Firebase: " + productoId + " (" + nombreProducto + ")");
                    }
                    productosProcesados[0]++;
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("ERROR: Error al obtener producto desde Firebase: " + error.getMessage());
                    productosConError[0]++;
                }
            });
        }
        
        System.out.println("DEBUG: Iniciado proceso de descuento para " + cotizacion.getProductos().size() + " productos del inventario");
    }
    
    private void mostrarOpcionesSwipe(Cotizacion cotizacion, int position) {
        // Verificar si la cotización está vencida
        String estadoNormalizado = cotizacion.getEstado() != null ? cotizacion.getEstado().toUpperCase() : "";
        boolean esVencida = "VENCIDA".equals(estadoNormalizado) || cotizacion.estaVencida();
        boolean esAceptada = "ACEPTADA".equals(estadoNormalizado);
        
        String mensaje = "¿Qué acción deseas realizar?";
        if (esVencida && !esAceptada) {
            mensaje = "Esta cotización está vencida. ¿Deseas aceptarla de todas formas?";
        }
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Acción para " + cotizacion.getCorrelativo())
               .setMessage(mensaje)
               .setPositiveButton("✅ Aceptar", (dialog, which) -> {
                   // Permitir aceptar incluso si está vencida (pero no si ya está aceptada)
                   if (!esAceptada) {
                       aceptarCotizacion(cotizacion);
                   } else {
                       Toast.makeText(this, "Esta cotización ya fue aceptada", Toast.LENGTH_SHORT).show();
                       loadCotizaciones();
                   }
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
        // Crear mensaje con correlativo y nombre completo del cliente
        String nombreCliente = cotizacion.getClienteNombre() != null ? cotizacion.getClienteNombre().toUpperCase() : "Sin nombre";
        String correlativo = cotizacion.getCorrelativo() != null ? cotizacion.getCorrelativo() : "N/A";
        String mensaje = "Cotización " + correlativo + "\n" +
                        "Cliente: " + nombreCliente + "\n" +
                        "Total: " + cotizacion.getTotal() + " " + cotizacion.getMoneda();
        
        try {
            java.io.File archivo = new java.io.File(rutaArchivo);
            Uri uri = FileProvider.getUriForFile(this, 
                "com.example.appelite.fileprovider", archivo);
            
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Cotización " + correlativo + " - " + nombreCliente);
            intent.putExtra(Intent.EXTRA_TEXT, mensaje + "\n\n¡Gracias por tu preferencia!");
            
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
                        
                        // Normalizar estado a valores estándar
                        String estadoActual = cotizacion.getEstado();
                        String estadoNormalizado = normalizarEstado(cotizacion);
                        
                        // Si el estado cambió al normalizar, actualizar en Firebase
                        if (estadoActual != null && !estadoNormalizado.equals(estadoActual.toUpperCase().trim())) {
                            cotizacion.setEstado(estadoNormalizado);
                            cotizacionesRef.child(cotizacion.getId()).child("estado").setValue(estadoNormalizado);
                            System.out.println("DEBUG: Cotización " + cotizacion.getCorrelativo() + 
                                             " estado normalizado: " + estadoActual + " → " + estadoNormalizado);
                        }
                        
                        // Actualizar estado automáticamente si está vencida
                        // IMPORTANTE: Solo marcar como VENCIDA si realmente está vencida (fecha anterior a hoy)
                        if (cotizacion.estaVencida() && !"ACEPTADA".equals(cotizacion.getEstado())) {
                            String estadoAnterior = cotizacion.getEstado();
                            cotizacion.setEstado("VENCIDA");
                            cotizacionesRef.child(cotizacion.getId()).child("estado").setValue("VENCIDA");
                            System.out.println("DEBUG: Cotización " + cotizacion.getCorrelativo() + 
                                             " marcada como VENCIDA automáticamente (estado anterior: " + estadoAnterior + 
                                             ", fecha vencimiento: " + cotizacion.getFechaVencimiento() + ")");
                        } else if (!cotizacion.estaVencida() && "VENCIDA".equals(cotizacion.getEstado()) && !"ACEPTADA".equals(cotizacion.getEstado())) {
                            // Si NO está vencida pero tiene estado VENCIDA, corregir a PENDIENTE
                            cotizacion.setEstado("PENDIENTE");
                            cotizacionesRef.child(cotizacion.getId()).child("estado").setValue("PENDIENTE");
                            System.out.println("DEBUG: Cotización " + cotizacion.getCorrelativo() + 
                                             " corregida de VENCIDA a PENDIENTE (fecha vencimiento: " + cotizacion.getFechaVencimiento() + ")");
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
        cot1.setEstado("PENDIENTE"); // Normalizado a mayúsculas
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
        cotizacionPrueba.setEstado("PENDIENTE"); // Normalizado a mayúsculas
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
    
    /**
     * Normaliza el estado de una cotización a valores estándar: PENDIENTE, ACEPTADA, VENCIDA
     */
    private String normalizarEstado(Cotizacion c) {
        String estado = c.getEstado();
        if (estado == null) {
            return "PENDIENTE";
        }
        
        // Normalizar a mayúsculas
        estado = estado.toUpperCase().trim();
        
        // Si está vencida y no es ACEPTADA, debe ser VENCIDA
        if (c.estaVencida() && !"ACEPTADA".equals(estado)) {
            return "VENCIDA";
        }
        
        // Mapear variaciones comunes a valores estándar
        if (estado.startsWith("PEND")) {
            return "PENDIENTE";
        } else if (estado.startsWith("ACEPT")) {
            return "ACEPTADA";
        } else if (estado.startsWith("VENC")) {
            return "VENCIDA";
        }
        
        // Si no coincide con ningún patrón conocido, devolver como está (en mayúsculas)
        return estado;
    }
    
    private void aplicarFiltro(String filtro) {
        System.out.println("DEBUG: ========================================");
        System.out.println("DEBUG: Aplicando filtro: " + filtro);
        filtroActual = filtro;
        cotizacionesFiltradas.clear();
        
        int contadorOriginal = cotizaciones.size();
        int contadorFiltrado = 0;
        
        // Contadores de diagnóstico por estado
        int contadorPendiente = 0;
        int contadorAceptada = 0;
        int contadorVencida = 0;
        int contadorOtros = 0;
        
        // Primero, contar estados actuales para diagnóstico
        for (Cotizacion c : cotizaciones) {
            String estadoNormalizado = normalizarEstado(c);
            switch (estadoNormalizado) {
                case "PENDIENTE":
                    contadorPendiente++;
                    break;
                case "ACEPTADA":
                    contadorAceptada++;
                    break;
                case "VENCIDA":
                    contadorVencida++;
                    break;
                default:
                    contadorOtros++;
                    System.out.println("DEBUG: Estado no reconocido: " + estadoNormalizado + " (original: " + c.getEstado() + ")");
                    break;
            }
        }
        
        System.out.println("DEBUG: Diagnóstico de estados:");
        System.out.println("DEBUG:   PENDIENTE: " + contadorPendiente);
        System.out.println("DEBUG:   ACEPTADA: " + contadorAceptada);
        System.out.println("DEBUG:   VENCIDA: " + contadorVencida);
        System.out.println("DEBUG:   OTROS: " + contadorOtros);
        
        // Aplicar filtro con igualdad exacta
        switch (filtro) {
            case "Pendientes":
                for (Cotizacion c : cotizaciones) {
                    String estadoNormalizado = normalizarEstado(c);
                    // Solo PENDIENTE que NO esté vencida
                    if ("PENDIENTE".equals(estadoNormalizado)) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                        System.out.println("DEBUG: ✓ Agregada PENDIENTE: " + c.getCorrelativo() + " (estado original: " + c.getEstado() + ")");
                    }
                }
                break;
            case "Aceptadas":
                for (Cotizacion c : cotizaciones) {
                    String estadoNormalizado = normalizarEstado(c);
                    // Solo ACEPTADA exactamente
                    if ("ACEPTADA".equals(estadoNormalizado)) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                        System.out.println("DEBUG: ✓ Agregada ACEPTADA: " + c.getCorrelativo() + " (estado original: " + c.getEstado() + ")");
                    }
                }
                break;
            case "Vencidas":
                for (Cotizacion c : cotizaciones) {
                    String estadoNormalizado = normalizarEstado(c);
                    // Solo VENCIDA exactamente
                    if ("VENCIDA".equals(estadoNormalizado)) {
                        cotizacionesFiltradas.add(c);
                        contadorFiltrado++;
                        System.out.println("DEBUG: ✓ Agregada VENCIDA: " + c.getCorrelativo() + " (estado original: " + c.getEstado() + ")");
                    }
                }
                break;
            default: // Todas
                cotizacionesFiltradas.addAll(cotizaciones);
                contadorFiltrado = contadorOriginal;
                break;
        }
        
        System.out.println("DEBUG: Resultado del filtro:");
        System.out.println("DEBUG:   Total original: " + contadorOriginal);
        System.out.println("DEBUG:   Total filtrado: " + contadorFiltrado);
        System.out.println("DEBUG:   Tamaño lista filtrada: " + cotizacionesFiltradas.size());
        System.out.println("DEBUG: ========================================");
        
        // Verificar que no haya elementos de otros estados en la lista filtrada
        for (Cotizacion c : cotizacionesFiltradas) {
            String estadoNormalizado = normalizarEstado(c);
            boolean estadoCorrecto = false;
            switch (filtro) {
                case "Pendientes":
                    estadoCorrecto = "PENDIENTE".equals(estadoNormalizado);
                    break;
                case "Aceptadas":
                    estadoCorrecto = "ACEPTADA".equals(estadoNormalizado);
                    break;
                case "Vencidas":
                    estadoCorrecto = "VENCIDA".equals(estadoNormalizado);
                    break;
                default:
                    estadoCorrecto = true;
                    break;
            }
            if (!estadoCorrecto) {
                System.out.println("ERROR: Estado incorrecto en lista filtrada - " + c.getCorrelativo() + 
                                 " tiene estado " + estadoNormalizado + " pero el filtro es " + filtro);
            }
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            System.out.println("DEBUG: Adapter notificado - ItemCount: " + adapter.getItemCount());
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
