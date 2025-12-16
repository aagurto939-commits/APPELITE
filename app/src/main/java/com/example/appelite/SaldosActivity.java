package com.example.appelite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SaldosActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTotalPorCobrar, tvPorVencer, tvVencido;
    private ChipGroup chipGroup;
    private Chip chipTodos, chipPorVencer, chipVencidos, chipPagados;
    private RecyclerView rvDeudas;
    private FloatingActionButton fabAbono;

    private DeudasAdapter deudasAdapter;
    private List<Deuda> listaDeudasOriginal;
    private List<Deuda> listaDeudasFiltrada;
    private DatabaseReference db;
    private String filtroActual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saldos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        setupToolbar();
        setupChips();
        setupRecyclerView();
        setupFAB();
        cargarDeudas();
        } catch (Exception e) {
            android.util.Log.e("SaldosActivity", "Error en onCreate: " + e.getMessage());
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error al cargar la pantalla: " + e.getMessage(), 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void initFirebase() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    private void initViews() {
        try {
            btnBack = findViewById(R.id.btnBack);
            tvTotalPorCobrar = findViewById(R.id.tvTotalPorCobrar);
            tvPorVencer = findViewById(R.id.tvPorVencer);
            tvVencido = findViewById(R.id.tvVencido);
            chipGroup = findViewById(R.id.chipGroup);
            chipTodos = findViewById(R.id.chipTodos);
            chipPorVencer = findViewById(R.id.chipPorVencer);
            chipVencidos = findViewById(R.id.chipVencidos);
            chipPagados = findViewById(R.id.chipPagados);
            rvDeudas = findViewById(R.id.rvDeudas);
            fabAbono = findViewById(R.id.fabAbono);
            
            if (btnBack == null) {
                android.util.Log.e("SaldosActivity", "btnBack es null");
            }
            if (chipGroup == null) {
                android.util.Log.e("SaldosActivity", "chipGroup es null");
            }
            if (rvDeudas == null) {
                android.util.Log.e("SaldosActivity", "rvDeudas es null");
            }
        } catch (Exception e) {
            android.util.Log.e("SaldosActivity", "Error en initViews: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupChips() {
        // Seleccionar "Todos" por defecto
        chipTodos.setChecked(true);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            Chip selectedChip = group.findViewById(checkedIds.get(0));
            if (selectedChip != null) {
                String filtro = selectedChip.getText().toString();
                aplicarFiltro(filtro);
            }
        });
    }

    private void setupRecyclerView() {
        rvDeudas.setLayoutManager(new LinearLayoutManager(this));
        
        // Inicializar listas
        listaDeudasOriginal = new ArrayList<>();
        listaDeudasFiltrada = new ArrayList<>();
        deudasAdapter = new DeudasAdapter(listaDeudasFiltrada);
        
        deudasAdapter.setOnDeudaClickListener(new DeudasAdapter.OnDeudaClickListener() {
            @Override
            public void onRegistrarAbono(Deuda deuda) {
                mostrarBottomSheetAbono(deuda);
            }

            @Override
            public void onDeudaClick(Deuda deuda) {
                mostrarDetallesDeuda(deuda);
            }

            @Override
            public void onVerCronograma(Deuda deuda) {
                mostrarCronograma(deuda);
            }

            @Override
            public void onEditarVencimiento(Deuda deuda) {
                editarVencimiento(deuda);
            }

            @Override
            public void onVerDetalleVenta(Deuda deuda) {
                verDetalleVenta(deuda);
            }
        });
        
        rvDeudas.setAdapter(deudasAdapter);
    }
    
    private ValueEventListener deudasListener;
    
    private void cargarDeudas() {
        // Remover listener anterior si existe
        if (deudasListener != null) {
            db.child("deudas").removeEventListener(deudasListener);
        }
        
        deudasListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listaDeudasOriginal.clear();
                
                System.out.println("DEBUG: 📊 Cargando deudas desde Firebase. Total de deudas encontradas: " + snapshot.getChildrenCount());
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Deuda deuda = ds.getValue(Deuda.class);
                    if (deuda != null) {
                        deuda.setId(ds.getKey());
                        
                        // Verificar que los campos de conversión estén presentes
                        // Si no están, intentar calcularlos desde los datos disponibles
                        String monedaDeuda = deuda.getMoneda();
                        String monedaNormalizada = deuda.getMoneda();
                        double totalDeuda = deuda.getTotal();
                        
                        // Si la deuda no tiene campos de conversión guardados, calcularlos
                        if (deuda.getTotalEnSoles() <= 0 || deuda.getTipoCambioUsado() <= 0) {
                            System.out.println("DEBUG: ⚠️ Deuda " + deuda.getId() + " no tiene campos de conversión completos, recalculando...");
                            
                            // Si no tiene monedaOriginal, usar la moneda actual
                            if (deuda.getMonedaOriginal() == null || deuda.getMonedaOriginal().isEmpty()) {
                                deuda.setMonedaOriginal(monedaNormalizada);
                            }
                            
                            // Si no tiene totalOriginal, usar el total actual
                            if (deuda.getTotalOriginal() <= 0) {
                                deuda.setTotalOriginal(totalDeuda);
                            }
                            
                            // Si no tiene tipoCambioUsado, intentar obtenerlo de la venta o cotización
                            if (deuda.getTipoCambioUsado() <= 0) {
                                // Intentar obtener desde la venta asociada
                                if (deuda.getVentaId() != null && !deuda.getVentaId().isEmpty()) {
                                    DatabaseReference ventaRef = FirebaseDatabase.getInstance().getReference("ventas").child(deuda.getVentaId());
                                    ventaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot ventaSnapshot) {
                                            if (ventaSnapshot.exists()) {
                                                Venta venta = ventaSnapshot.getValue(Venta.class);
                                                if (venta != null && venta.getTipoCambioUsado() > 0) {
                                                    deuda.setTipoCambioUsado(venta.getTipoCambioUsado());
                                                    deuda.setMonedaOriginal(venta.getMonedaOriginal());
                                                    deuda.setTotalOriginal(venta.getTotalOriginal());
                                                    calcularYGuardarConversionesDeuda(deuda, ds.getRef());
                                                }
                                            }
                                        }
                                        
                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            System.out.println("DEBUG: ⚠️ Error al obtener tipo de cambio de venta: " + error.getMessage());
                                        }
                                    });
                                } else if (deuda.getCotizacionId() != null && !deuda.getCotizacionId().isEmpty()) {
                                    // Intentar obtener desde la cotización
                                    DatabaseReference cotizacionRef = FirebaseDatabase.getInstance().getReference("cotizaciones").child(deuda.getCotizacionId());
                                    cotizacionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot cotizacionSnapshot) {
                                            if (cotizacionSnapshot.exists()) {
                                                Cotizacion cotizacion = cotizacionSnapshot.getValue(Cotizacion.class);
                                                if (cotizacion != null && cotizacion.getTipoCambio() > 0) {
                                                    deuda.setTipoCambioUsado(cotizacion.getTipoCambio());
                                                    String monedaCotizacion = cotizacion.getMoneda();
                                                    String monedaNormalizada = monedaCotizacion != null && monedaCotizacion.contains("SOLES") ? "PEN" : "USD";
                                                    deuda.setMonedaOriginal(monedaNormalizada);
                                                    deuda.setTotalOriginal(cotizacion.getTotal());
                                                    calcularYGuardarConversionesDeuda(deuda, ds.getRef());
                                                }
                                            }
                                        }
                                        
                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            System.out.println("DEBUG: ⚠️ Error al obtener tipo de cambio de cotización: " + error.getMessage());
                                        }
                                    });
                                }
                            } else {
                                // Ya tiene tipo de cambio, calcular conversiones
                                calcularYGuardarConversionesDeuda(deuda, ds.getRef());
                            }
                        }
                        
                        System.out.println("DEBUG: 📦 Deuda cargada desde Firebase - ID: " + deuda.getId() + 
                            ", Moneda: " + deuda.getMoneda() + 
                            ", Moneda original: " + deuda.getMonedaOriginal() + 
                            ", Total original: " + deuda.getTotalOriginal() + 
                            ", Tipo cambio: " + deuda.getTipoCambioUsado() + 
                            ", Total en soles: " + deuda.getTotalEnSoles());
                        
                        // Recalcular estado en caso de cambios (verifica fecha de vencimiento)
                        String estadoRecalculado = deuda.calcularEstado();
                        if (!estadoRecalculado.equals(deuda.getEstado())) {
                            // Si el estado cambió, actualizar en Firebase
                            db.child("deudas").child(deuda.getId()).child("estado").setValue(estadoRecalculado);
                            deuda.setEstado(estadoRecalculado);
                        }
                        listaDeudasOriginal.add(deuda);
                    }
                }
                
                System.out.println("DEBUG: 📋 Total de deudas cargadas: " + listaDeudasOriginal.size());
                
                completarMonedasDeudas(listaDeudasOriginal, () -> {
                    actualizarMetricas();
                    aplicarFiltro(filtroActual);
                });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("SaldosActivity", "Error cargando deudas: " + error.getMessage());
                Toast.makeText(SaldosActivity.this, "Error al cargar deudas", Toast.LENGTH_SHORT).show();
            }
        };
        
        db.child("deudas").addValueEventListener(deudasListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remover listener al destruir la actividad
        if (deudasListener != null && db != null) {
            db.child("deudas").removeEventListener(deudasListener);
        }
    }

    private void setupFAB() {
        fabAbono.setOnClickListener(v -> {
            // Si hay deudas, mostrar selector; si no, mostrar mensaje
            if (listaDeudasOriginal.isEmpty()) {
                Toast.makeText(this, "No hay deudas para registrar abonos", Toast.LENGTH_SHORT).show();
                return;
            }
            // Mostrar selector de deuda o abrir para la primera deuda pendiente
            mostrarSelectorDeudaParaAbono();
        });
    }

    private void mostrarSelectorDeudaParaAbono() {
        // Buscar la primera deuda pendiente
        Deuda primeraDeudaPendiente = null;
        for (Deuda deuda : listaDeudasOriginal) {
            if (!deuda.estaPagada()) {
                primeraDeudaPendiente = deuda;
                break;
            }
        }
        
        if (primeraDeudaPendiente != null) {
            mostrarBottomSheetAbono(primeraDeudaPendiente);
        } else {
            Toast.makeText(this, "No hay deudas pendientes para registrar abonos", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Calcula y guarda las conversiones de moneda para una deuda
     */
    private void calcularYGuardarConversionesDeuda(Deuda deuda, DatabaseReference deudaRef) {
        String monedaOriginal = deuda.getMonedaOriginal();
        double totalOriginal = deuda.getTotalOriginal();
        double tipoCambioUsado = deuda.getTipoCambioUsado();
        
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
        
        deuda.setTotalEnSoles(totalEnSoles);
        deuda.setTotalEnDolares(totalEnDolares);
        
        // Guardar en Firebase
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("monedaOriginal", monedaOriginal);
        updates.put("totalOriginal", totalOriginal);
        updates.put("tipoCambioUsado", tipoCambioUsado);
        updates.put("totalEnSoles", totalEnSoles);
        updates.put("totalEnDolares", totalEnDolares);
        
        deudaRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                System.out.println("DEBUG: ✅ Campos de conversión guardados para deuda " + deuda.getId());
            })
            .addOnFailureListener(e -> {
                System.out.println("DEBUG: ⚠️ Error al guardar campos de conversión: " + e.getMessage());
            });
    }
    
    private void actualizarMetricas() {
        double totalPorCobrar = 0.0;
        double porVencer = 0.0;
        double vencido = 0.0;
        
        for (Deuda deuda : listaDeudasOriginal) {
            if (deuda == null) continue;
            
            // Usar el saldo pendiente en soles guardado (no volver a convertir)
            // Calcular proporción del saldo pendiente respecto al total
            double saldoPendiente = deuda.getSaldoPendiente();
            double total = deuda.getTotal();
            double saldoEnSoles = 0.0;
            
            // IMPORTANTE: Siempre usar totalEnSoles guardado, que fue calculado con el tipo de cambio de la cotización
            if (total > 0 && deuda.getTotalEnSoles() > 0) {
                // Calcular proporción del saldo pendiente en soles
                double proporcion = saldoPendiente / total;
                saldoEnSoles = deuda.getTotalEnSoles() * proporcion;
            } else {
                // Fallback: usar método de la deuda
                saldoEnSoles = deuda.obtenerSaldoPendienteEnSoles();
            }
            
            System.out.println("DEBUG: 💰 Deuda ID: " + deuda.getId() + 
                ", Moneda original: " + deuda.getMonedaOriginal() + 
                ", Total original: " + deuda.getTotalOriginal() + 
                ", Tipo cambio usado: " + deuda.getTipoCambioUsado() + 
                ", Total en soles: " + deuda.getTotalEnSoles() + 
                ", Saldo pendiente: " + saldoPendiente + 
                ", Saldo en soles: " + saldoEnSoles);
            
            totalPorCobrar += saldoEnSoles;
            
            String estado = deuda.getEstado();
            if ("VENCIDO".equals(estado)) {
                vencido += saldoEnSoles;
            } else if ("POR_VENCER".equals(estado)) {
                porVencer += saldoEnSoles;
            }
        }
        
        System.out.println("DEBUG: 📊 Métricas de deudas actualizadas - Total por cobrar: S/ " + totalPorCobrar + 
            ", Por vencer: S/ " + porVencer + ", Vencido: S/ " + vencido);
        
        tvTotalPorCobrar.setText(String.format("S/ %.2f", totalPorCobrar));
        tvPorVencer.setText(String.format("S/ %.2f", porVencer));
        tvVencido.setText(String.format("S/ %.2f", vencido));
    }

    private void completarMonedasDeudas(List<Deuda> deudas, Runnable onComplete) {
        List<Deuda> sinMoneda = new ArrayList<>();
        Map<String, List<Deuda>> deudasPorVenta = new HashMap<>();
        Set<String> ventaIds = new HashSet<>();

        for (Deuda deuda : deudas) {
            if (deuda == null) continue;
            if (!deuda.tieneMonedaDefinida()) {
                sinMoneda.add(deuda);
                String ventaId = deuda.getVentaId();
                if (ventaId != null && !ventaId.trim().isEmpty()) {
                    deudasPorVenta.computeIfAbsent(ventaId, k -> new ArrayList<>()).add(deuda);
                    ventaIds.add(ventaId);
                }
            }
        }

        if (sinMoneda.isEmpty() || ventaIds.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.child("ventas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                DatabaseReference deudasRef = db.child("deudas");

                for (DataSnapshot ventaSnapshot : snapshot.getChildren()) {
                    String ventaId = ventaSnapshot.getKey();
                    if (ventaId == null || !ventaIds.contains(ventaId)) continue;

                    Venta venta = ventaSnapshot.getValue(Venta.class);
                    if (venta == null) continue;

                    List<Deuda> relacionados = deudasPorVenta.get(ventaId);
                    if (relacionados == null) continue;

                    String monedaVenta = venta.getMoneda();
                    String fechaVenta = venta.getFechaVenta();

                    for (Deuda deuda : relacionados) {
                        if (monedaVenta != null && !monedaVenta.trim().isEmpty()) {
                            deuda.setMoneda(monedaVenta);
                            deudasRef.child(deuda.getId()).child("moneda").setValue(deuda.getMoneda());
                        }

                        if ((deuda.getFechaCreacion() == null || deuda.getFechaCreacion().trim().isEmpty())
                                && fechaVenta != null && !fechaVenta.trim().isEmpty()) {
                            deuda.setFechaCreacion(fechaVenta);
                            deudasRef.child(deuda.getId()).child("fechaCreacion").setValue(fechaVenta);
                        }
                        
                        // Completar campos de conversión desde la venta si faltan
                        if (deuda.getTotalEnSoles() <= 0 || deuda.getTipoCambioUsado() <= 0) {
                            if (venta.getTotalEnSoles() > 0 && venta.getTipoCambioUsado() > 0) {
                                deuda.setMonedaOriginal(venta.getMonedaOriginal());
                                deuda.setTotalOriginal(venta.getTotalOriginal());
                                deuda.setTipoCambioUsado(venta.getTipoCambioUsado());
                                deuda.setTotalEnSoles(venta.getTotalEnSoles());
                                deuda.setTotalEnDolares(venta.getTotalEnDolares());
                                
                                // Actualizar en Firebase
                                DatabaseReference deudaRef = deudasRef.child(deuda.getId());
                                deudaRef.child("monedaOriginal").setValue(deuda.getMonedaOriginal());
                                deudaRef.child("totalOriginal").setValue(deuda.getTotalOriginal());
                                deudaRef.child("tipoCambioUsado").setValue(deuda.getTipoCambioUsado());
                                deudaRef.child("totalEnSoles").setValue(deuda.getTotalEnSoles());
                                deudaRef.child("totalEnDolares").setValue(deuda.getTotalEnDolares());
                                
                                System.out.println("DEBUG: ✅ Campos de conversión completados para deuda " + deuda.getId() + 
                                    " desde venta " + ventaId);
                            }
                        }
                    }
                }

                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void aplicarFiltro(String filtro) {
        filtroActual = filtro;
        listaDeudasFiltrada.clear();
        
        for (Deuda deuda : listaDeudasOriginal) {
            String estado = deuda.getEstado();
            boolean incluir = false;
            
            switch (filtro) {
                case "Todos":
                    incluir = true;
                    break;
                case "Por vencer":
                    incluir = "POR_VENCER".equals(estado);
                    break;
                case "Vencidos":
                    incluir = "VENCIDO".equals(estado);
                    break;
                case "Pagados":
                    incluir = "PAGADO".equals(estado);
                    break;
            }
            
            if (incluir) {
                listaDeudasFiltrada.add(deuda);
            }
        }
        
        deudasAdapter.actualizarLista(listaDeudasFiltrada);
    }

    private void mostrarBottomSheetAbono(Deuda deuda) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottomsheet_registrar_abono, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        
        // Referencias
        TextView tvDeudaInfo = bottomSheetView.findViewById(R.id.tvDeudaInfo);
        TextInputEditText etMonto = bottomSheetView.findViewById(R.id.etMonto);
        TextInputEditText etMoneda = bottomSheetView.findViewById(R.id.etMoneda);
        TextInputEditText etMetodoPago = bottomSheetView.findViewById(R.id.etMetodoPago);
        TextInputEditText etFecha = bottomSheetView.findViewById(R.id.etFecha);
        TextInputEditText etNota = bottomSheetView.findViewById(R.id.etNota);
        TextView tvSaldoActual = bottomSheetView.findViewById(R.id.tvSaldoActual);
        com.google.android.material.button.MaterialButton btnCancelar = bottomSheetView.findViewById(R.id.btnCancelarAbono);
        com.google.android.material.button.MaterialButton btnGuardar = bottomSheetView.findViewById(R.id.btnGuardarAbono);
        
        // Configurar datos
        tvDeudaInfo.setText("Cliente: " + (deuda.getClienteNombre() != null ? deuda.getClienteNombre().toUpperCase() : "N/A"));
        tvSaldoActual.setText(deuda.getSaldoFormateado());
        
        // Configurar fecha por defecto (hoy)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaHoy = sdf.format(Calendar.getInstance().getTime());
        etFecha.setText(fechaHoy);
        
        // Click en fecha
        etFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etFecha.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Click en moneda
        etMoneda.setOnClickListener(v -> {
            String[] monedas = {"DÓLARES (USD)", "SOLES (PEN)"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Seleccionar moneda del abono");
            builder.setItems(monedas, (dialog, which) -> {
                if (which == 0) {
                    etMoneda.setText("USD");
                } else {
                    etMoneda.setText("PEN");
                }
            });
            builder.show();
        });
        
        // Establecer moneda por defecto según la deuda
        String monedaDeuda = deuda.getMoneda() != null ? deuda.getMoneda() : "USD";
        if (monedaDeuda.contains("PEN") || monedaDeuda.contains("SOLES")) {
            etMoneda.setText("PEN");
        } else {
            etMoneda.setText("USD");
        }
        
        // Click en método de pago
        etMetodoPago.setOnClickListener(v -> {
            String[] metodos = {"EFECTIVO", "TRANSFERENCIA", "YAPE", "POS"};
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Seleccionar método de pago");
            builder.setItems(metodos, (dialog, which) -> {
                etMetodoPago.setText(metodos[which]);
            });
            builder.show();
        });
        
        // Botón guardar
        btnGuardar.setOnClickListener(v -> {
            String montoStr = etMonto.getText().toString().trim();
            String monedaAbono = etMoneda.getText().toString().trim();
            String metodoPago = etMetodoPago.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();
            String nota = etNota.getText().toString().trim();
            
            // Validaciones
            if (montoStr.isEmpty()) {
                etMonto.setError("Ingresa el monto del abono");
                return;
            }
            
            double monto;
            try {
                monto = Double.parseDouble(montoStr);
            } catch (NumberFormatException e) {
                etMonto.setError("Monto inválido");
                return;
            }
            
            if (monto <= 0) {
                etMonto.setError("El monto debe ser mayor a cero");
                return;
            }
            
            if (monedaAbono.isEmpty()) {
                etMoneda.setError("Selecciona la moneda del abono");
                return;
            }
            
            if (metodoPago.isEmpty()) {
                etMetodoPago.setError("Selecciona un método de pago");
                return;
            }
            
            if (fecha.isEmpty()) {
                etFecha.setError("Selecciona una fecha");
                return;
            }
            
            // Validar que el monto no exceda el saldo pendiente
            // Si el abono es en soles y la deuda en dólares, convertir primero para validar
            double montoEnMonedaDeuda = monto;
            if ("PEN".equals(monedaAbono) && deuda.getMoneda() != null && deuda.getMoneda().contains("USD")) {
                // Si el abono es en soles y la deuda en dólares, convertir
                if (deuda.getTipoCambioUsado() > 0) {
                    montoEnMonedaDeuda = monto / deuda.getTipoCambioUsado();
                } else {
                    etMonto.setError("No se puede convertir: falta tipo de cambio");
                    return;
                }
            } else if ("USD".equals(monedaAbono) && deuda.getMoneda() != null && deuda.getMoneda().contains("PEN")) {
                // Si el abono es en dólares y la deuda en soles, convertir
                if (deuda.getTipoCambioUsado() > 0) {
                    montoEnMonedaDeuda = monto * deuda.getTipoCambioUsado();
                } else {
                    etMonto.setError("No se puede convertir: falta tipo de cambio");
                    return;
                }
            }
            
            if (montoEnMonedaDeuda > deuda.getSaldoPendiente()) {
                etMonto.setError("El monto no puede ser mayor al saldo pendiente");
                return;
            }
            
            // Guardar abono
            guardarAbono(deuda, monto, monedaAbono, metodoPago, fecha, nota);
            bottomSheetDialog.dismiss();
        });
        
        btnCancelar.setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        bottomSheetDialog.show();
    }

    private void guardarAbono(Deuda deuda, double monto, String monedaAbono, String metodoPago, String fecha, String nota) {
        // Convertir monto a la moneda de la deuda si es necesario
        double montoCalculado = monto;
        
        // Si el abono es en soles y la deuda en dólares, convertir a dólares
        if ("PEN".equals(monedaAbono) && deuda.getMoneda() != null && deuda.getMoneda().contains("USD")) {
            if (deuda.getTipoCambioUsado() > 0) {
                montoCalculado = monto / deuda.getTipoCambioUsado();
            } else {
                Toast.makeText(this, "Error: No se puede convertir, falta tipo de cambio", Toast.LENGTH_LONG).show();
                return;
            }
        }
        // Si el abono es en dólares y la deuda en soles, convertir a soles
        else if ("USD".equals(monedaAbono) && deuda.getMoneda() != null && deuda.getMoneda().contains("PEN")) {
            if (deuda.getTipoCambioUsado() > 0) {
                montoCalculado = monto * deuda.getTipoCambioUsado();
            } else {
                Toast.makeText(this, "Error: No se puede convertir, falta tipo de cambio", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // Hacer la variable final para usarla en la lambda
        final double montoEnMonedaDeuda = montoCalculado;
        
        // Crear objeto Abono (guardar el monto original y su moneda)
        Abono abono = new Abono(deuda.getId(), monto, metodoPago, monedaAbono, fecha, nota);
        String abonoId = db.child("abonos").push().getKey();
        abono.setId(abonoId);
        
        // Guardar abono en Firebase
        db.child("abonos").child(abonoId).setValue(abono)
            .addOnSuccessListener(aVoid -> {
                // Actualizar saldo de la deuda (usar el monto convertido)
                double nuevoSaldo = deuda.getSaldoPendiente() - montoEnMonedaDeuda;
                double nuevoTotalAbonado = deuda.getTotalAbonado() + montoEnMonedaDeuda;
                
                DatabaseReference deudaRef = db.child("deudas").child(deuda.getId());
                deudaRef.child("saldoPendiente").setValue(nuevoSaldo);
                deudaRef.child("totalAbonado").setValue(nuevoTotalAbonado);
                
                // Actualizar estado en base al nuevo saldo
                deuda.setSaldoPendiente(nuevoSaldo);
                deuda.setTotalAbonado(nuevoTotalAbonado);
                String nuevoEstado = deuda.getEstado();
                
                deudaRef.child("estado").setValue(nuevoEstado)
                    .addOnSuccessListener(aVoid2 -> {
                        Toast.makeText(this, "Abono registrado exitosamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error al guardar abono: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void mostrarDetallesDeuda(Deuda deuda) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_detalle_deuda, null);
        builder.setView(dialogView);

        // Obtener referencias de las vistas
        TextView tvTituloDetalleDeuda = dialogView.findViewById(R.id.tvTituloDetalleDeuda);
        TextView tvClienteDetalle = dialogView.findViewById(R.id.tvClienteDetalle);
        TextView tvFechaDetalle = dialogView.findViewById(R.id.tvFechaDetalle);
        TextView tvTotalDetalle = dialogView.findViewById(R.id.tvTotalDetalle);
        TextView tvMetodoPagoDetalle = dialogView.findViewById(R.id.tvMetodoPagoDetalle);
        TextView tvEstadoDetalle = dialogView.findViewById(R.id.tvEstadoDetalle);
        TextView tvTotalOriginalDetalle = dialogView.findViewById(R.id.tvTotalOriginalDetalle);
        TextView tvTipoCambioDetalle = dialogView.findViewById(R.id.tvTipoCambioDetalle);
        TextView tvMontoConvertidoDetalle = dialogView.findViewById(R.id.tvMontoConvertidoDetalle);
        com.google.android.material.button.MaterialButton btnCerrarDetalleDeuda = dialogView.findViewById(R.id.btnCerrarDetalleDeuda);

        // Configurar datos
        tvTituloDetalleDeuda.setText("Detalle de Deuda");
        tvClienteDetalle.setText(deuda.getClienteNombre() != null ? deuda.getClienteNombre().toUpperCase() : "Sin cliente");
        
        // Fecha - usar fecha de creación o vencimiento
        String fecha = deuda.getFechaCreacion() != null ? deuda.getFechaCreacion() : deuda.getFechaVencimiento();
        if (fecha == null || fecha.isEmpty()) {
            fecha = "N/A";
        }
        tvFechaDetalle.setText(fecha);
        
        // Total
        tvTotalDetalle.setText(deuda.getTotalFormateado());
        
        // Método de pago
        String metodoPago = deuda.getMetodoPago();
        if (metodoPago != null) {
            if (metodoPago.equalsIgnoreCase("CREDITO") || metodoPago.equalsIgnoreCase("CRÉDITO")) {
                tvMetodoPagoDetalle.setText("Crédito");
            } else if (metodoPago.equalsIgnoreCase("CONTADO")) {
                tvMetodoPagoDetalle.setText("Contado");
            } else {
                tvMetodoPagoDetalle.setText(metodoPago);
            }
        } else {
            tvMetodoPagoDetalle.setText("N/A");
        }
        
        // Estado
        String estado = deuda.getEstadoDisplay();
        tvEstadoDetalle.setText(estado != null ? estado.toUpperCase() : "N/A");
        
        // Información de conversión
        String monedaOriginal = deuda.getMonedaOriginal();
        double totalOriginal = deuda.getTotalOriginal();
        double tipoCambio = deuda.getTipoCambioUsado();
        double totalConvertido = deuda.getTotalEnSoles();
        
        if (tipoCambio > 0 && totalOriginal > 0) {
            // Mostrar información de conversión
            String simboloOriginal = "USD".equals(monedaOriginal) ? "US$ " : "S/ ";
            tvTotalOriginalDetalle.setText(simboloOriginal + String.format(Locale.getDefault(), "%.2f", totalOriginal));
            tvTipoCambioDetalle.setText(String.format(Locale.getDefault(), "%.2f", tipoCambio));
            tvMontoConvertidoDetalle.setText("S/ " + String.format(Locale.getDefault(), "%.2f", totalConvertido));
        } else {
            // Ocultar o mostrar valores por defecto
            tvTotalOriginalDetalle.setText(deuda.getTotalFormateado());
            tvTipoCambioDetalle.setText("N/A");
            tvMontoConvertidoDetalle.setText("N/A");
        }

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCerrarDetalleDeuda.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarCronograma(Deuda deuda) {
        // Cargar abonos de esta deuda
        db.child("abonos").orderByChild("deudaId").equalTo(deuda.getId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Abono> abonos = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Abono abono = ds.getValue(Abono.class);
                        if (abono != null) {
                            abono.setId(ds.getKey());
                            abonos.add(abono);
                        }
                    }
                    mostrarDialogoCronograma(deuda, abonos);
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SaldosActivity.this, "Error al cargar cronograma", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void mostrarDialogoCronograma(Deuda deuda, List<Abono> abonos) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_cronograma_pagos, null);
        builder.setView(dialogView);

        // Obtener referencias de las vistas
        TextView tvTituloCronograma = dialogView.findViewById(R.id.tvTituloCronograma);
        TextView tvClienteCronograma = dialogView.findViewById(R.id.tvClienteCronograma);
        TextView tvTotalCronograma = dialogView.findViewById(R.id.tvTotalCronograma);
        TextView tvSaldoPendiente = dialogView.findViewById(R.id.tvSaldoPendiente);
        RecyclerView rvAbonos = dialogView.findViewById(R.id.rvAbonos);
        LinearLayout layoutSinAbonos = dialogView.findViewById(R.id.layoutSinAbonos);
        com.google.android.material.button.MaterialButton btnCerrarCronograma = dialogView.findViewById(R.id.btnCerrarCronograma);

        // Configurar datos
        tvTituloCronograma.setText("Cronograma de Pagos");
        tvClienteCronograma.setText("Cliente: " + (deuda.getClienteNombre() != null ? deuda.getClienteNombre().toUpperCase() : "Sin cliente"));
        tvTotalCronograma.setText(deuda.getTotalFormateado());
        tvSaldoPendiente.setText(deuda.getSaldoFormateado());

        // Configurar RecyclerView de abonos
        rvAbonos.setLayoutManager(new LinearLayoutManager(this));
        AbonosCronogramaAdapter abonosAdapter = new AbonosCronogramaAdapter(abonos);
        rvAbonos.setAdapter(abonosAdapter);

        // Mostrar/ocultar mensaje cuando no hay abonos
        if (abonos.isEmpty()) {
            layoutSinAbonos.setVisibility(View.VISIBLE);
            rvAbonos.setVisibility(View.GONE);
        } else {
            layoutSinAbonos.setVisibility(View.GONE);
            rvAbonos.setVisibility(View.VISIBLE);
        }

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCerrarCronograma.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void editarVencimiento(Deuda deuda) {
        Calendar calendar = Calendar.getInstance();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            java.util.Date fechaVenc = sdf.parse(deuda.getFechaVencimiento());
            calendar.setTime(fechaVenc);
        } catch (Exception e) {
            // Si hay error, usar fecha actual
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String nuevaFecha = sdf.format(calendar.getTime());
                
                // Actualizar en Firebase
                db.child("deudas").child(deuda.getId()).child("fechaVencimiento").setValue(nuevaFecha)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Fecha de vencimiento actualizada", Toast.LENGTH_SHORT).show();
                        cargarDeudas();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al actualizar fecha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void verDetalleVenta(Deuda deuda) {
        if (deuda.getVentaId() == null || deuda.getVentaId().isEmpty()) {
            Toast.makeText(this, "No hay información de venta disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        
        db.child("ventas").child(deuda.getVentaId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Venta venta = snapshot.getValue(Venta.class);
                    if (venta != null) {
                        mostrarDetalleVentaDialog(venta);
                    } else {
                        Toast.makeText(SaldosActivity.this, "Error al cargar datos de venta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SaldosActivity.this, "Venta no encontrada", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SaldosActivity.this, "Error al cargar venta", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDetalleVentaDialog(Venta venta) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_detalle_venta, null);
        builder.setView(dialogView);

        // Obtener referencias de las vistas
        TextView tvTituloDetalleVenta = dialogView.findViewById(R.id.tvTituloDetalleVenta);
        TextView tvClienteVenta = dialogView.findViewById(R.id.tvClienteVenta);
        TextView tvFechaVenta = dialogView.findViewById(R.id.tvFechaVenta);
        TextView tvTotalVenta = dialogView.findViewById(R.id.tvTotalVenta);
        TextView tvMetodoPagoVenta = dialogView.findViewById(R.id.tvMetodoPagoVenta);
        TextView tvEstadoVenta = dialogView.findViewById(R.id.tvEstadoVenta);
        TextView tvTotalOriginalVenta = dialogView.findViewById(R.id.tvTotalOriginalVenta);
        TextView tvTipoCambioVenta = dialogView.findViewById(R.id.tvTipoCambioVenta);
        TextView tvMontoConvertidoVenta = dialogView.findViewById(R.id.tvMontoConvertidoVenta);
        com.google.android.material.button.MaterialButton btnCerrarDetalleVenta = dialogView.findViewById(R.id.btnCerrarDetalleVenta);

        // Configurar datos
        tvTituloDetalleVenta.setText("Detalle de Venta");
        tvClienteVenta.setText(venta.getClienteNombre() != null ? venta.getClienteNombre().toUpperCase() : "Sin cliente");
        
        // Fecha y hora
        String fechaHora = "";
        if (venta.getFechaVenta() != null) {
            fechaHora = venta.getFechaVenta();
        }
        if (venta.getHoraVenta() != null && !venta.getHoraVenta().isEmpty()) {
            fechaHora += " " + venta.getHoraVenta();
        }
        if (fechaHora.isEmpty()) {
            fechaHora = "N/A";
        }
        tvFechaVenta.setText(fechaHora);
        
        // Total
        tvTotalVenta.setText(venta.getMontoFormateado());
        
        // Método de pago
        String metodoPago = venta.getMetodoPago();
        if (metodoPago != null) {
            if (metodoPago.equalsIgnoreCase("CREDITO") || metodoPago.equalsIgnoreCase("CRÉDITO")) {
                tvMetodoPagoVenta.setText("Crédito");
            } else if (metodoPago.equalsIgnoreCase("CONTADO")) {
                tvMetodoPagoVenta.setText("Contado");
            } else {
                tvMetodoPagoVenta.setText(metodoPago);
            }
        } else {
            tvMetodoPagoVenta.setText("N/A");
        }
        
        // Estado
        String estadoPago = venta.getEstadoPago();
        if (estadoPago != null) {
            if (venta.esCredito()) {
                tvEstadoVenta.setText("CREDITO");
            } else if (venta.esContado()) {
                tvEstadoVenta.setText("CONTADO");
            } else {
                tvEstadoVenta.setText(estadoPago.toUpperCase());
            }
        } else {
            tvEstadoVenta.setText("PENDIENTE");
        }
        
        // Información de conversión
        String monedaOriginal = venta.getMonedaOriginal();
        double totalOriginal = venta.getTotalOriginal();
        double tipoCambio = venta.getTipoCambioUsado();
        
        if (tipoCambio > 0 && totalOriginal > 0) {
            // Mostrar información de conversión
            String simboloOriginal = "USD".equals(monedaOriginal) ? "US$ " : "S/ ";
            tvTotalOriginalVenta.setText(simboloOriginal + String.format(Locale.getDefault(), "%.2f", totalOriginal));
            tvTipoCambioVenta.setText(String.format(Locale.getDefault(), "%.2f", tipoCambio));
            
            double montoConvertido = "USD".equals(monedaOriginal) ? venta.getTotalEnSoles() : venta.getTotalEnDolares();
            String simboloConvertido = "USD".equals(monedaOriginal) ? "S/ " : "US$ ";
            tvMontoConvertidoVenta.setText(simboloConvertido + String.format(Locale.getDefault(), "%.2f", montoConvertido));
        } else {
            // Ocultar o mostrar valores por defecto
            tvTotalOriginalVenta.setText(venta.getMontoFormateado());
            tvTipoCambioVenta.setText("N/A");
            tvMontoConvertidoVenta.setText("N/A");
        }

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnCerrarDetalleVenta.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // El ValueEventListener se ejecuta automáticamente cuando hay cambios
        // Solo refrescar si no hay listener activo
        if (db != null && deudasListener == null) {
            cargarDeudas();
        }
    }
}