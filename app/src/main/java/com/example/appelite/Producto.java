package com.example.appelite;

public class Producto {
    private String id;
    private String nombre;
    private String codigo;
    private String categoria;
    private String marca;
    private double precioCosto;
    private double precioVenta;
    private String moneda;
    private int stock;
    private String descripcion;
    private int stockMinimo;
    private boolean activo;
    private String fechaCreacion;
    
    // Constructor vacío requerido para Firebase
    public Producto() {}
    
    // Constructor con parámetros básicos
    public Producto(String nombre, String codigo, String categoria, double precioCosto, double precioVenta, String moneda, int stock, String descripcion) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.categoria = categoria;
        this.precioCosto = precioCosto;
        this.precioVenta = precioVenta;
        this.moneda = moneda;
        this.stock = stock;
        this.descripcion = descripcion;
        this.stockMinimo = 0;
        this.activo = true;
        this.fechaCreacion = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
    
    // Constructor para compatibilidad con código existente
    public Producto(String id, String nombre, String descripcion, double precio, int stock, String moneda) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioVenta = precio;
        this.stock = stock;
        this.moneda = moneda;
        this.stockMinimo = 0;
        this.activo = true;
        this.fechaCreacion = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getMarca() {
        return marca;
    }
    
    public void setMarca(String marca) {
        this.marca = marca;
    }
    
    public double getPrecioCosto() {
        return precioCosto;
    }
    
    public void setPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
    }
    
    public double getPrecioVenta() {
        return precioVenta;
    }
    
    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }
    
    public String getMoneda() {
        return moneda;
    }
    
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
    
    public int getStock() {
        return stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    // Getters y Setters adicionales para compatibilidad
    public int getStockMinimo() {
        return stockMinimo;
    }
    
    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public String getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    // Método para compatibilidad con código existente
    public double getPrecio() {
        return precioVenta;
    }
    
    public void setPrecio(double precio) {
        this.precioVenta = precio;
    }
    
    // Método para verificar stock bajo
    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }
}