package com.example.appelite;

import com.google.firebase.database.Exclude;
import java.util.List;
import java.util.ArrayList;

public class Venta {
    private String id;
    private String clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private String fechaVenta;
    private String horaVenta;
    private double subtotal;
    private double descuento;
    private double igv;
    private double total;
    private String metodoPago;
    private String moneda;
    private String estado;
    private String estadoPago; // PENDIENTE, CONTADO, CREDITO
    private List<Producto> productos;
    private List<VentaItem> items; // Para compatibilidad con clases existentes
    private String cotizacionId; // Referencia a la cotización original
    private String cotizacionCorrelativo; // Correlativo de la cotización original
    
    // Campos para conversión de moneda (guardados desde la cotización)
    private String monedaOriginal; // Moneda original de la cotización
    private double totalOriginal; // Total original de la cotización
    private double tipoCambioUsado; // Tipo de cambio usado en la cotización
    private double totalEnSoles; // Total convertido a soles
    private double totalEnDolares; // Total convertido a dólares

    // Constructor vacío para Firebase
    public Venta() {
        this.estadoPago = "PENDIENTE"; // Por defecto pendiente
    }

    // Constructor completo
    public Venta(String id, String clienteId, String clienteNombre, String clienteDocumento, 
                 String fechaVenta, String horaVenta, double subtotal, double descuento, 
                 double igv, double total, String metodoPago, String moneda, String estado, 
                 List<Producto> productos, String cotizacionId) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteDocumento = clienteDocumento;
        this.fechaVenta = fechaVenta;
        this.horaVenta = horaVenta;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.igv = igv;
        this.total = total;
        this.metodoPago = metodoPago;
        this.moneda = moneda;
        this.estado = estado;
        this.productos = productos;
        this.cotizacionId = cotizacionId;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(String fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getHoraVenta() { return horaVenta; }
    public void setHoraVenta(String horaVenta) { this.horaVenta = horaVenta; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }

    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda != null ? moneda : "PEN"; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public List<VentaItem> getItems() { 
        if (items == null) {
            items = new ArrayList<>();
        }
        return items; 
    }
    public void setItems(List<VentaItem> items) { this.items = items; }

    public String getCotizacionId() { return cotizacionId; }
    public void setCotizacionId(String cotizacionId) { this.cotizacionId = cotizacionId; }
    
    public String getCotizacionCorrelativo() { return cotizacionCorrelativo; }
    public void setCotizacionCorrelativo(String cotizacionCorrelativo) { this.cotizacionCorrelativo = cotizacionCorrelativo; }
    
    public String getEstadoPago() { 
        return estadoPago != null ? estadoPago : "PENDIENTE"; 
    }
    public void setEstadoPago(String estadoPago) { 
        this.estadoPago = estadoPago != null ? estadoPago.toUpperCase() : "PENDIENTE"; 
    }
    
    // Métodos utilitarios para estadoPago
    public boolean esPendiente() {
        return "PENDIENTE".equals(getEstadoPago());
    }
    
    public boolean esContado() {
        return "CONTADO".equals(getEstadoPago());
    }
    
    public boolean esCredito() {
        return "CREDITO".equals(getEstadoPago()) || "CRÉDITO".equals(getEstadoPago());
    }
    
    public boolean estaProcesada() {
        return !esPendiente();
    }

    // Método para obtener la descripción de productos
    @Exclude
    public String getDescripcionProductos() {
        // Primero intentar con items (VentaItem)
        if (items != null && !items.isEmpty()) {
            StringBuilder descripcion = new StringBuilder();
            int maxItems = Math.min(items.size(), 2);
            for (int i = 0; i < maxItems; i++) {
                if (i > 0) descripcion.append(" + ");
                descripcion.append(items.get(i).getNombre());
            }
            if (items.size() > 2) {
                descripcion.append(" + ").append(items.size() - 2).append(" más");
            }
            return descripcion.toString();
        }
        
        // Si no hay items, intentar con productos
        if (productos != null && !productos.isEmpty()) {
            StringBuilder descripcion = new StringBuilder();
            int maxItems = Math.min(productos.size(), 2);
            for (int i = 0; i < maxItems; i++) {
                if (i > 0) descripcion.append(" + ");
                descripcion.append(productos.get(i).getNombre());
            }
            if (productos.size() > 2) {
                descripcion.append(" + ").append(productos.size() - 2).append(" más");
            }
            return descripcion.toString();
        }
        
        return "Sin productos";
    }

    // Método para obtener la fecha formateada
    @Exclude
    public String getFechaFormateada() {
        if (fechaVenta == null) return "";
        // Formato: DD-MMM, HH:MM
        return fechaVenta + ", " + (horaVenta != null ? horaVenta : "");
    }

    // Método para obtener el monto formateado
    @Exclude
    public String getMontoFormateado() {
        return getSimboloMoneda() + String.format(java.util.Locale.getDefault(), "%.2f", total);
    }

    public String getSimboloMoneda() {
        return "USD".equalsIgnoreCase(getMonedaNormalizada()) ? "US$ " : "S/ ";
    }

    public String getMonedaNormalizada() {
        if (moneda == null) return "PEN";
        String upper = moneda.trim().toUpperCase(java.util.Locale.getDefault());
        if (upper.contains("USD") || upper.contains("DOLAR") || upper.contains("DÓLAR")) {
            return "USD";
        }
        if (upper.contains("PEN") || upper.contains("SOL")) {
            return "PEN";
        }
        return upper;
    }
    
    // Getters y Setters para campos de conversión
    public String getMonedaOriginal() {
        return monedaOriginal != null ? monedaOriginal : getMonedaNormalizada();
    }
    
    public void setMonedaOriginal(String monedaOriginal) {
        this.monedaOriginal = monedaOriginal;
    }
    
    public double getTotalOriginal() {
        return totalOriginal > 0 ? totalOriginal : total;
    }
    
    public void setTotalOriginal(double totalOriginal) {
        this.totalOriginal = totalOriginal;
    }
    
    public double getTipoCambioUsado() {
        return tipoCambioUsado;
    }
    
    public void setTipoCambioUsado(double tipoCambioUsado) {
        this.tipoCambioUsado = tipoCambioUsado;
    }
    
    public double getTotalEnSoles() {
        return totalEnSoles;
    }
    
    public void setTotalEnSoles(double totalEnSoles) {
        this.totalEnSoles = totalEnSoles;
    }
    
    public double getTotalEnDolares() {
        return totalEnDolares;
    }
    
    public void setTotalEnDolares(double totalEnDolares) {
        this.totalEnDolares = totalEnDolares;
    }
    
    // Método para obtener el total en soles (usar el guardado o calcular)
    public double obtenerTotalEnSoles() {
        // Si ya está guardado y es mayor que 0, usarlo
        if (totalEnSoles > 0) {
            return totalEnSoles;
        }
        
        // Si no está guardado, calcular basándose en la moneda original y el tipo de cambio
        String monedaOrig = getMonedaOriginal();
        double totalOrig = getTotalOriginal();
        double tipoCambio = getTipoCambioUsado();
        
        // Si la moneda original es USD y hay tipo de cambio, multiplicar
        if ("USD".equals(monedaOrig) && tipoCambio > 0) {
            double calculado = totalOrig * tipoCambio;
            // Guardar el valor calculado para futuras consultas
            if (totalEnSoles <= 0) {
                totalEnSoles = calculado;
            }
            return calculado;
        } 
        // Si la moneda original es PEN, el total ya está en soles
        else if ("PEN".equals(monedaOrig)) {
            if (totalEnSoles <= 0) {
                totalEnSoles = totalOrig > 0 ? totalOrig : total;
            }
            return totalOrig > 0 ? totalOrig : total;
        }
        
        // Fallback: usar el total actual (asumiendo que está en soles)
        return total;
    }
    
    // Método para obtener el total en dólares (usar el guardado o calcular)
    public double obtenerTotalEnDolares() {
        if (totalEnDolares > 0) {
            return totalEnDolares;
        }
        // Si no está guardado, calcular basándose en la moneda original
        String monedaOrig = getMonedaOriginal();
        if ("PEN".equals(monedaOrig) && tipoCambioUsado > 0) {
            return totalOriginal / tipoCambioUsado;
        } else if ("USD".equals(monedaOrig)) {
            return totalOriginal;
        }
        return total;
    }
}