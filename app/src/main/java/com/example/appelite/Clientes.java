package com.example.appelite;

public class Clientes {
    private String nombreCompleto;
    private String direccion;
    private String correo;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String id;

    public Clientes() {
        // Constructor vacío requerido por Firebase
    }

    public Clientes(String nombreCompleto, String direccion, String correo, String tipoDocumento,
                    String numeroDocumento, String telefono, String id) {
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.correo = correo;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.id = id;
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public String getDireccion() { return direccion; }
    public String getCorreo() { return correo; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getTelefono() { return telefono; }
    public String getId() { return id; }

    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setId(String id) { this.id = id; }
}
