package com.example.appelite;

import java.io.Serializable;

public class Producto implements Serializable {
    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private double precioCosto;
    private int stock;
    private int stockMinimo;
    private String categoria;
    private String proveedor;
    private String unidadMedida;
    private boolean activo;
    private String fechaCreacion;
    private String fechaActualizacion;
    private String moneda; // Para compatibilidad con código existente

    // Constructor vacío necesario para Firebase
    public Producto() {}

    // Constructor original para compatibilidad
    public Producto(String id, String nombre, String descripcion, double precio, int stock, String moneda) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.moneda = moneda;
        this.activo = true;
        this.codigo = id; // Por defecto usar id como código
    }

    // Constructor completo
    public Producto(String id, String codigo, String nombre, String descripcion, 
                   double precio, double precioCosto, int stock, int stockMinimo,
                   String categoria, String proveedor, String unidadMedida, 
                   boolean activo, String fechaCreacion, String fechaActualizacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.precioCosto = precioCosto;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.categoria = categoria;
        this.proveedor = proveedor;
        this.unidadMedida = unidadMedida;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    // Constructor básico
    public Producto(String codigo, String nombre, double precio, int stock) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.activo = true;
        this.moneda = "PEN"; // Por defecto
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public double getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
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

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    // Métodos utilitarios
    public boolean tieneStockBajo() {
        return stock <= stockMinimo;
    }

    public double getMargenGanancia() {
        if (precioCosto > 0) {
            return ((precio - precioCosto) / precioCosto) * 100;
        }
        return 0;
    }

    // Método de utilidad para mostrar precio formateado (compatibilidad)
    public String getPrecioFormateado() {
        String simbolo = (moneda != null && moneda.equals("USD")) ? "$" : "S/";
        return simbolo + String.format("%.2f", precio);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                '}';
    }
}