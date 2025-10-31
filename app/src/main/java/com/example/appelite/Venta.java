package com.example.appelite;

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
    private List<Producto> productos;
    private List<VentaItem> items; // Para compatibilidad con clases existentes
    private String cotizacionId; // Referencia a la cotización original

    // Constructor vacío para Firebase
    public Venta() {}

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
    public void setMoneda(String moneda) { this.moneda = moneda; }

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

    // Método para obtener la descripción de productos
    public String getDescripcionProductos() {
        if (productos == null || productos.isEmpty()) {
            return "Sin productos";
        }
        
        StringBuilder descripcion = new StringBuilder();
        for (int i = 0; i < productos.size() && i < 2; i++) {
            if (i > 0) descripcion.append(" + ");
            descripcion.append(productos.get(i).getNombre());
        }
        
        if (productos.size() > 2) {
            descripcion.append(" + ").append(productos.size() - 2).append(" más");
        }
        
        return descripcion.toString();
    }

    // Método para obtener la fecha formateada
    public String getFechaFormateada() {
        if (fechaVenta == null) return "";
        // Formato: DD-MMM, HH:MM
        return fechaVenta + ", " + (horaVenta != null ? horaVenta : "");
    }

    // Método para obtener el monto formateado
    public String getMontoFormateado() {
        String simbolo = "S/ ";
        if ("USD".equals(moneda)) {
            simbolo = "$ ";
        }
        return simbolo + String.format("%.2f", total);
    }
}