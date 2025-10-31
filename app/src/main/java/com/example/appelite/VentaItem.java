package com.example.appelite;

public class VentaItem {
    private String id;
    private String productoId;
    private String nombre;
    private double cantidad;
    private double precio;
    private double precioUnitario; // Para compatibilidad
    private String moneda; // Para compatibilidad
    private double subtotal;

    // Constructor vacío para Firebase
    public VentaItem() {}

    // Constructor completo
    public VentaItem(String id, String productoId, String nombre, double cantidad, double precio, double subtotal) {
        this.id = id;
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.precioUnitario = precio; // Mismo valor que precio
        this.subtotal = subtotal;
    }

    // Constructor para compatibilidad con VentaProductosAdapter
    public VentaItem(String id, String nombre, int cantidad, double precio, String moneda) {
        this.id = id;
        this.productoId = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
        this.precioUnitario = precio;
        this.moneda = moneda;
        this.subtotal = precio * cantidad;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}