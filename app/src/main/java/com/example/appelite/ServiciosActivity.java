package com.example.appelite;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ServiciosActivity extends AppCompatActivity implements ServiciosAdapter.OnServicioClickListener {
    
    private TextView tvFechaHoy, tvServiciosHoy;
    private RecyclerView recyclerViewServicios;
    private LinearLayout layoutEmptyState;
    private Button btnProgramarServicio;
    private ImageButton btnBack, btnCalendario;
    
    private List<Servicio> serviciosHoy;
    private ServiciosAdapter adapter;
    private DatabaseReference serviciosRef;
    private String fechaHoy;
    private ValueEventListener serviciosListener;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("DEBUG: 🚀 ServiciosActivity onCreate iniciado");
        
        EdgeToEdge.enable(this);

        try {
        setContentView(R.layout.activity_servicios);
            System.out.println("DEBUG: ✅ Layout cargado exitosamente");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al cargar layout: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        aplicarWindowInsets();

        try {
        initializeViews();
            System.out.println("DEBUG: ✅ Views inicializados");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al inicializar views: " + e.getMessage());
            e.printStackTrace();
            return;
        }


        try {
        setupFirebase();
            System.out.println("DEBUG: ✅ Firebase configurado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar Firebase: " + e.getMessage());
            e.printStackTrace();
        }

        try {
        setupRecyclerView();
            System.out.println("DEBUG: ✅ RecyclerView configurado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar RecyclerView: " + e.getMessage());
            e.printStackTrace();
        }

        try {
        setupClickListeners();
            System.out.println("DEBUG: ✅ Click listeners configurados");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar click listeners: " + e.getMessage());
            e.printStackTrace();
        }

        try {
        configurarFechaHoy();
            System.out.println("DEBUG: ✅ Fecha configurada");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar fecha: " + e.getMessage());
            e.printStackTrace();
        }

        try {
        cargarServiciosHoy();
            System.out.println("DEBUG: ✅ Carga de servicios iniciada");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al cargar servicios: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("DEBUG: ✅ ServiciosActivity onCreate completado");
    }

    private void initializeViews() {
        System.out.println("DEBUG: 🔍 Iniciando inicialización de views...");
        
        try {
            tvFechaHoy = findViewById(R.id.tvFechaHoy);
            System.out.println("DEBUG: ✅ tvFechaHoy encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con tvFechaHoy: " + e.getMessage());
        }
        
        try {
            tvServiciosHoy = findViewById(R.id.tvServiciosHoy);
            System.out.println("DEBUG: ✅ tvServiciosHoy encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con tvServiciosHoy: " + e.getMessage());
        }
        if (tvServiciosHoy != null) {
            tvServiciosHoy.setText("0 servicios programados");
        }
        
        try {
        recyclerViewServicios = findViewById(R.id.recyclerViewServicios);
            System.out.println("DEBUG: ✅ recyclerViewServicios encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con recyclerViewServicios: " + e.getMessage());
        }
        
        try {
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
            System.out.println("DEBUG: ✅ layoutEmptyState encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con layoutEmptyState: " + e.getMessage());
        }
        
        try {
        btnProgramarServicio = findViewById(R.id.btnProgramarServicio);
            System.out.println("DEBUG: ✅ btnProgramarServicio encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con btnProgramarServicio: " + e.getMessage());
        }
        
        
        try {
        btnBack = findViewById(R.id.btnBack);
            System.out.println("DEBUG: ✅ btnBack encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con btnBack: " + e.getMessage());
        }
        
        try {
        btnCalendario = findViewById(R.id.btnCalendario);
            System.out.println("DEBUG: ✅ btnCalendario encontrado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error con btnCalendario: " + e.getMessage());
        }

        serviciosHoy = new ArrayList<>();
        System.out.println("DEBUG: ✅ Todos los views inicializados correctamente");
    }

    private void aplicarWindowInsets() {
        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void setupFirebase() {
        serviciosRef = FirebaseDatabase.getInstance().getReference("servicios");
    }

    private void setupRecyclerView() {
        System.out.println("DEBUG: 🔍 Configurando RecyclerView...");
        
        try {
        recyclerViewServicios.setLayoutManager(new LinearLayoutManager(this));
            System.out.println("DEBUG: ✅ LayoutManager configurado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar LayoutManager: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        try {
        adapter = new ServiciosAdapter(this, serviciosHoy);
            System.out.println("DEBUG: ✅ ServiciosAdapter creado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al crear ServiciosAdapter: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        try {
        adapter.setOnServicioClickListener(this);
            System.out.println("DEBUG: ✅ Click listener configurado");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar click listener: " + e.getMessage());
            e.printStackTrace();
        }
        
        try {
        recyclerViewServicios.setAdapter(adapter);
            System.out.println("DEBUG: ✅ Adapter asignado al RecyclerView");
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al asignar adapter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarioServiciosActivity.class);
            startActivity(intent);
        });
        
        // Configurar botón con funcionalidades múltiples
        btnProgramarServicio.setOnClickListener(v -> {
            // Detectar doble tap para limpiar datos corruptos
            if (System.currentTimeMillis() - lastClickTime < 500) {
                limpiarDatosCorruptos();
                return;
            }
            lastClickTime = System.currentTimeMillis();
            
            // Acción normal - ir a nuevo servicio
            Intent intent = new Intent(this, NuevoServicioActivity.class);
            intent.putExtra("fecha_seleccionada", fechaHoy);
            startActivity(intent);
        });
        
        // Mantener presionado para crear servicio de prueba
        btnProgramarServicio.setOnLongClickListener(v -> {
            crearServicioPrueba();
            return true;
        });
        
    }

    private void configurarFechaHoy() {
        Calendar calendar = Calendar.getInstance();
        
        // Fecha para mostrar
        SimpleDateFormat displayFormat = new SimpleDateFormat("d 'de' MMMM, yyyy", new Locale("es", "ES"));
        if (tvFechaHoy != null) {
            tvFechaHoy.setText(displayFormat.format(calendar.getTime()));
        }
        
        // Fecha para filtrar (formato yyyy-MM-dd)
        SimpleDateFormat filterFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fechaHoy = filterFormat.format(calendar.getTime());
    }

    private void cargarServiciosHoy() {
        System.out.println("DEBUG: 🔍 Iniciando carga de servicios para hoy: " + fechaHoy);
        System.out.println("DEBUG: 📍 Ruta de Firebase: " + serviciosRef.toString());
        
        // Verificar si hay datos en Firebase primero
        serviciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("DEBUG: 🔍 VERIFICACIÓN INICIAL - Total de servicios en Firebase: " + dataSnapshot.getChildrenCount());
                System.out.println("DEBUG: 📅 Fecha de filtro actual: " + fechaHoy);
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    System.out.println("DEBUG: 📋 Servicio encontrado: " + snapshot.getKey());
                    
                    // Verificar cada campo del servicio
                    String cliente = snapshot.child("cliente").getValue(String.class);
                    String fecha = snapshot.child("fecha").getValue(String.class);
                    String estado = snapshot.child("estado").getValue(String.class);
                    
                    System.out.println("DEBUG: 📋 - Cliente: " + cliente);
                    System.out.println("DEBUG: 📋 - Fecha: " + fecha);
                    System.out.println("DEBUG: 📋 - Estado: " + estado);
                    System.out.println("DEBUG: 📋 - ¿Coincide fecha?: " + fechaHoy.equals(fecha));
                    
                    if (fechaHoy.equals(fecha)) {
                        System.out.println("DEBUG: ✅ ESTE SERVICIO DEBERÍA APARECER EN LA LISTA");
                    } else {
                        System.out.println("DEBUG: ⏭️ Este servicio NO es de hoy");
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: ❌ Error en verificación inicial: " + databaseError.getMessage());
            }
        });
        
        try {
            serviciosListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("DEBUG: 📊 Datos recibidos de Firebase");
                    
                    try {
                serviciosHoy.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                System.out.println("DEBUG: 🔍 Procesando snapshot: " + snapshot.getKey());
                                
                                // Verificar si el snapshot contiene datos válidos
                                if (snapshot.exists() && snapshot.getValue() != null) {
                                    Object value = snapshot.getValue();
                                    System.out.println("DEBUG: 📋 Tipo de dato: " + value.getClass().getSimpleName());
                                    
                                    // Solo procesar si es un objeto, no un string
                                    if (!(value instanceof String)) {
                                        Servicio servicio = snapshot.getValue(Servicio.class);
                                        if (servicio != null) {
                                            servicio.setId(snapshot.getKey());
                                            String fechaServicio = normalizarFecha(servicio.getFecha());
                                            System.out.println("DEBUG: ✅ Servicio creado: " + servicio.getCliente() + " - Fecha: " + fechaServicio);
                                            
                                            if (fechaServicio != null && fechaServicio.equals(fechaHoy)) {
                                                serviciosHoy.add(servicio);
                                                System.out.println("DEBUG: ✅ Servicio agregado para hoy: " + servicio.getCliente());
                                            } else {
                                                System.out.println("DEBUG: ⏭️ Servicio descartado (fecha distinta): " + servicio.getFecha());
                                            }
                                        } else {
                                            System.out.println("DEBUG: ❌ Servicio es null");
                                        }
                                    } else {
                                        System.out.println("DEBUG: ❌ Datos corruptos (String) encontrados en: " + snapshot.getKey());
                                        System.out.println("DEBUG: 🗑️ Eliminando datos corruptos...");
                                        // Eliminar datos corruptos
                                        snapshot.getRef().removeValue();
                                    }
                                } else {
                                    System.out.println("DEBUG: ❌ Snapshot vacío o null");
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: ❌ Error al procesar servicio " + snapshot.getKey() + ": " + e.getMessage());
                                e.printStackTrace();
                                
                                // Eliminar datos corruptos que causan el error
                                System.out.println("DEBUG: 🗑️ Eliminando datos corruptos que causan error...");
                                snapshot.getRef().removeValue();
                            }
                        }
                        
                        System.out.println("DEBUG: 📋 Total servicios de hoy: " + serviciosHoy.size());
                
                actualizarUI();
                adapter.notifyDataSetChanged();
                        
                    } catch (Exception e) {
                        System.out.println("DEBUG: ❌ Error en onDataChange: " + e.getMessage());
                        e.printStackTrace();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    System.out.println("DEBUG: ❌ Error de Firebase: " + databaseError.getMessage());
                }
            };
            
            serviciosRef.addValueEventListener(serviciosListener);
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al configurar listener: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarUI() {
        System.out.println("DEBUG: 🔄 Actualizando UI...");
        
        try {
        int count = serviciosHoy.size();
        if (tvServiciosHoy != null) {
            tvServiciosHoy.setText(count + (count == 1 ? " servicio programado" : " servicios programados"));
        }
            System.out.println("DEBUG: ✅ Contador actualizado: " + count + " servicios");
        
        if (serviciosHoy.isEmpty()) {
            layoutEmptyState.setVisibility(android.view.View.VISIBLE);
            recyclerViewServicios.setVisibility(android.view.View.GONE);
                System.out.println("DEBUG: ✅ Mostrando estado vacío");
        } else {
            layoutEmptyState.setVisibility(android.view.View.GONE);
            recyclerViewServicios.setVisibility(android.view.View.VISIBLE);
                System.out.println("DEBUG: ✅ Mostrando lista de servicios");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: ❌ Error al actualizar UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onEditarServicio(Servicio servicio) {
        if (servicio == null) {
            Toast.makeText(this, "Servicio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("ServiciosActivity", "Solicitando editar servicio: " + servicio.getId());
        mostrarDialogoConfirmacionEditar(servicio);
    }

    @Override
    public void onEliminarServicio(Servicio servicio) {
        // Mostrar diálogo de confirmación para eliminar
        if (servicio == null) {
            Toast.makeText(this, "Servicio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("ServiciosActivity", "Solicitando eliminar servicio: " + servicio.getId());
        mostrarDialogoConfirmacionEliminar(servicio);
    }

    @Override
    public void onEstadoServicio(Servicio servicio) {
        if (servicio == null) {
            Toast.makeText(this, "Servicio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("ServiciosActivity", "Solicitando cambio de estado: " + servicio.getId() + " estado actual: " + servicio.getEstado());
        mostrarDialogoConfirmacionEstado(servicio);
    }

    private void mostrarDialogoConfirmacionEditar(Servicio servicio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar servicio");
        builder.setMessage("¿Deseas editar el servicio programado para " + servicio.getCliente() + " el " +
                (servicio.getFecha() != null ? servicio.getFecha() : "-") + " a las " +
                (servicio.getHora() != null ? servicio.getHora() : "-") + "?");
        builder.setPositiveButton("Editar", (dialog, which) -> {
            Log.d("ServiciosActivity", "Confirmada edición de servicio: " + servicio.getId());
            Intent intent = new Intent(this, NuevoServicioActivity.class);
            intent.putExtra("servicio_id", servicio.getId());
            intent.putExtra("modo_edicion", true);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarDialogoConfirmacionEstado(Servicio servicio) {
        String estadoActual = servicio.getEstado() != null ? servicio.getEstado() : "Pendiente";
        boolean esCompletado = "Completado".equalsIgnoreCase(estadoActual);
        String nuevoEstado = esCompletado ? "Pendiente" : "Completado";
        String titulo = esCompletado ? "Marcar como pendiente" : "Marcar como completado";
        String mensaje = esCompletado
                ? "¿Deseas marcar nuevamente como pendiente el servicio para " + servicio.getCliente() + "?"
                : "¿Confirmas que el servicio para " + servicio.getCliente() + " ya fue completado?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton(esCompletado ? "Sí, marcar pendiente" : "Sí, marcar completado",
                (dialog, which) -> actualizarEstadoServicio(servicio, nuevoEstado));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void actualizarEstadoServicio(Servicio servicio, String nuevoEstado) {
        Log.d("ServiciosActivity", "Actualizando estado servicio: " + servicio.getId() + " -> " + nuevoEstado);
        serviciosRef.child(servicio.getId()).child("estado").setValue(nuevoEstado)
            .addOnSuccessListener(aVoid -> {
                servicio.setEstado(nuevoEstado);
                adapter.notifyDataSetChanged();
                String mensaje = "Completado".equalsIgnoreCase(nuevoEstado)
                        ? "Servicio marcado como completado"
                        : "Servicio marcado como pendiente";
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                Log.d("ServiciosActivity", "Estado actualizado correctamente para servicio: " + servicio.getId());
                sincronizarCentroNotificaciones();
            })
            .addOnFailureListener(e -> {
                Log.e("ServiciosActivity", "Error actualizando estado: " + e.getMessage());
                Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
            });
    }

    private void mostrarDialogoConfirmacionEliminar(Servicio servicio) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Eliminación");
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Servicio: ").append(servicio.getCliente() != null ? servicio.getCliente() : "-").append("\n");
        mensaje.append("Fecha: ").append(servicio.getFecha() != null ? servicio.getFecha() : "-").append("\n");
        mensaje.append("Hora: ").append(servicio.getHora() != null ? servicio.getHora() : "-").append("\n\n");
        mensaje.append("Estas seguro de que deseas eliminar este servicio?\nEsta accion no se puede deshacer.");
        builder.setMessage(mensaje.toString());
        
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            Log.d("ServiciosActivity", "Confirmada eliminación de servicio: " + servicio.getId());
            // Eliminar servicio de Firebase
            serviciosRef.child(servicio.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Servicio eliminado correctamente", Toast.LENGTH_SHORT).show();
                    Log.d("ServiciosActivity", "Servicio eliminado correctamente: " + servicio.getId());
                    sincronizarCentroNotificaciones();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar el servicio", Toast.LENGTH_SHORT).show();
                    Log.e("ServiciosActivity", "Error al eliminar servicio: " + e.getMessage());
                });
        });
        
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("DEBUG: 🔄 ServiciosActivity onResume()");
        // Recargar datos cuando volvemos a la actividad
        // cargarServiciosHoy(); // Comentado temporalmente para evitar bucles
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("DEBUG: 🧹 ServiciosActivity onDestroy()");
        
        // Remover listeners de Firebase para evitar memory leaks
        if (serviciosRef != null && serviciosListener != null) {
            serviciosRef.removeEventListener(serviciosListener);
            System.out.println("DEBUG: ✅ Listener de servicios removido");
        }
        
        // Limpiar referencias
        if (serviciosListener != null) {
            serviciosListener = null;
        }
        if (serviciosRef != null) {
            serviciosRef = null;
        }
        if (adapter != null) {
            adapter = null;
        }
        if (serviciosHoy != null) {
            serviciosHoy.clear();
            serviciosHoy = null;
        }
        if (recyclerViewServicios != null) {
            recyclerViewServicios.setAdapter(null);
            recyclerViewServicios = null;
        }
        
        // Forzar garbage collection
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("DEBUG: ⏸️ ServiciosActivity onPause()");
    }

    private void crearServicioPrueba() {
        System.out.println("DEBUG: 🧪 CREANDO SERVICIO DE PRUEBA");
        System.out.println("DEBUG: 📅 Fecha actual: " + fechaHoy);
        
        Servicio servicioPrueba = new Servicio();
        servicioPrueba.setCliente("Cliente Prueba");
        servicioPrueba.setMina("Mina Prueba");
        servicioPrueba.setTelefono("999999999");
        servicioPrueba.setEmail("prueba@test.com");
        servicioPrueba.setDescripcionProblema("Servicio de prueba automático");
        servicioPrueba.setCostoEstimado(100.0);
        servicioPrueba.setMoneda("PEN");
        servicioPrueba.setFecha(fechaHoy); // Usar la fecha de hoy
        servicioPrueba.setHora("10:00");
        servicioPrueba.setEstado("Pendiente");
        
        String key = serviciosRef.push().getKey();
        if (key != null) {
            servicioPrueba.setId(key);
            System.out.println("DEBUG: 🆔 Key del servicio de prueba: " + key);
            
            serviciosRef.child(key).setValue(servicioPrueba)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("DEBUG: ✅ SERVICIO DE PRUEBA CREADO EXITOSAMENTE");
                    System.out.println("DEBUG: 📅 Fecha guardada: " + fechaHoy);
                    Toast.makeText(this, "Servicio de prueba creado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    System.out.println("DEBUG: ❌ ERROR AL CREAR SERVICIO DE PRUEBA: " + e.getMessage());
                    Toast.makeText(this, "Error al crear servicio de prueba", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void sincronizarCentroNotificaciones() {
        NotificationGenerator.syncAll(this, nuevas -> {
            if (nuevas == null || nuevas.isEmpty()) {
                return;
            }
            runOnUiThread(() -> {
                NotificationHelper.createNotificationChannel(this);
                for (Notificacion notificacion : nuevas) {
                    NotificationHelper.showNotification(this, notificacion);
                }
            });
        });
    }

    private void limpiarDatosCorruptos() {
        System.out.println("DEBUG: 🧹 INICIANDO LIMPIEZA DE DATOS CORRUPTOS");
        
        serviciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int corruptosEncontrados = 0;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Object value = snapshot.getValue();
                        if (value instanceof String) {
                            System.out.println("DEBUG: 🗑️ Eliminando dato corrupto: " + snapshot.getKey());
                            snapshot.getRef().removeValue();
                            corruptosEncontrados++;
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: 🗑️ Eliminando dato corrupto (error): " + snapshot.getKey());
                        snapshot.getRef().removeValue();
                        corruptosEncontrados++;
                    }
                }
                
                System.out.println("DEBUG: ✅ Limpieza completada. Datos corruptos eliminados: " + corruptosEncontrados);
                Toast.makeText(ServiciosActivity.this, "Limpieza completada: " + corruptosEncontrados + " datos eliminados", Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("DEBUG: ❌ Error en limpieza: " + databaseError.getMessage());
            }
        });
    }

    private String normalizarFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) {
            return null;
        }
        String limpia = fecha.trim();
        try {
            SimpleDateFormat formatoIso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return formatoIso.format(formatoIso.parse(limpia));
        } catch (Exception e) {
            try {
                SimpleDateFormat formatoDia = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat formatoIso = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return formatoIso.format(formatoDia.parse(limpia));
            } catch (Exception ex) {
                System.out.println("DEBUG: ❌ No se pudo normalizar la fecha: " + fecha);
                return null;
            }
        }
    }
}
