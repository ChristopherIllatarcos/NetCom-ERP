package modelo;

import java.util.Date;

public class HistorialIngresoDTO {

    private int id;
    private String numeroFactura;
    private Date fechaEmision;
    private String descripciones;
    private int cantidadTotal;
    private double subtotal;
    private String observaciones;

    public HistorialIngresoDTO() {
    }

    public HistorialIngresoDTO(int id, String numeroFactura, Date fechaEmision, String descripciones, int cantidadTotal, double subtotal, String observaciones) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.descripciones = descripciones;
        this.cantidadTotal = cantidadTotal;
        this.subtotal = subtotal;
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getDescripciones() {
        return descripciones;
    }

    public void setDescripciones(String descripciones) {
        this.descripciones = descripciones;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
