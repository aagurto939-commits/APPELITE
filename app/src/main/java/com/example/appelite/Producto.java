package com.example.appelite;

public class Producto {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String moneda; // "USD" o "PEN"

    // Constructor vacío requerido por Firebase
    public Producto() {}

    // Constructor completo
    public Producto(String id, String nombre, String descripcion, double precio, int stock, String moneda) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.moneda = moneda;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getMoneda() { return moneda; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    // Método de utilidad para mostrar precio formateado
    public String getPrecioFormateado() {
        String simbolo = moneda.equals("USD") ? "$" : "S/";
        return simbolo + String.format("%.2f", precio);
    }
}