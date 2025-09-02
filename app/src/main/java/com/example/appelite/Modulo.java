package com.example.appelite;

public class Modulo {
    private String titulo;
    private String descripcion;
    private int icono;
    private Class<?> activityClass;

    public Modulo(String titulo, String descripcion, int icono, Class<?> activityClass) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.icono = icono;
        this.activityClass = activityClass;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public int getIcono() { return icono; }
    public Class<?> getActivityClass() { return activityClass; }
}
