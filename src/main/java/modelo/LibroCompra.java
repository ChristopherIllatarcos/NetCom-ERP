package modelo;

import java.util.Date;

public class LibroCompra {

    private Date fecha;
    private String numeroFactura;
    private String proveedorRazonSocial;
    private double neto;
    private double iva;
    private double total;

    // Constructor vacío
    public LibroCompra() {
    }

    // Constructor con parámetros
    public LibroCompra(Date fecha, String numeroFactura, String proveedorRazonSocial, double neto, double iva, double total) {
        this.fecha = fecha;
        this.numeroFactura = numeroFactura;
        this.proveedorRazonSocial = proveedorRazonSocial;
        this.neto = neto;
        this.iva = iva;
        this.total = total;
    }

    // Getters y Setters
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getProveedorRazonSocial() {
        return proveedorRazonSocial;
    }

    public void setProveedorRazonSocial(String proveedorRazonSocial) {
        this.proveedorRazonSocial = proveedorRazonSocial;
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(double neto) {
        this.neto = neto;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
