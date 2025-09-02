package com.example.appelite;

import java.util.List;

public class Venta {
    private String id;
    private String clienteId;
    private String clienteNombre;
    private String clienteDocumento;
    private List<VentaItem> items;
    private double total;
    private String moneda; // USD o PEN
    private long fecha;

    public Venta() {}

    public Venta(String id, String clienteId, String clienteNombre, String clienteDocumento, List<VentaItem> items, double total, String moneda, long fecha) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteDocumento = clienteDocumento;
        this.items = items;
        this.total = total;
        this.moneda = moneda;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public String getClienteId() { return clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteDocumento() { return clienteDocumento; }
    public List<VentaItem> getItems() { return items; }
    public double getTotal() { return total; }
    public String getMoneda() { return moneda; }
    public long getFecha() { return fecha; }

    public void setId(String id) { this.id = id; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    public void setItems(List<VentaItem> items) { this.items = items; }
    public void setTotal(double total) { this.total = total; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public void setFecha(long fecha) { this.fecha = fecha; }
}
