package com.example.appelite;

public class VentaItem {
    private String productoId;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private String moneda;

    public VentaItem() {}

    public VentaItem(String productoId, String nombreProducto, int cantidad, double precioUnitario, String moneda) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.moneda = moneda;
    }

    public String getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public String getMoneda() { return moneda; }

    public void setProductoId(String productoId) { this.productoId = productoId; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
}
