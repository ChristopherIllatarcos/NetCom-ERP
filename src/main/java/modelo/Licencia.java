package modelo;

import java.sql.Date;


public class Licencia {

    private int id;
    private String rutLicenciatario;
    private String razonSocial;
    private String estado;
    private String tipoLicencia;
    private int cajasNodosPermitidos;
    private int enUso;
    private Date fechaActivacion;
    private Date fechaVencimiento;
    private String serialKey;

    public Licencia() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRutLicenciatario() {
        return rutLicenciatario;
    }

    public void setRutLicenciatario(String rutLicenciatario) {
        this.rutLicenciatario = rutLicenciatario;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoLicencia() {
        return tipoLicencia;
    }

    public void setTipoLicencia(String tipoLicencia) {
        this.tipoLicencia = tipoLicencia;
    }

    public int getCajasNodosPermitidos() {
        return cajasNodosPermitidos;
    }

    public void setCajasNodosPermitidos(int cajasNodosPermitidos) {
        this.cajasNodosPermitidos = cajasNodosPermitidos;
    }

    public int getEnUso() {
        return enUso;
    }

    public void setEnUso(int enUso) {
        this.enUso = enUso;
    }

    public Date getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(Date fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getSerialKey() {
        return serialKey;
    }

    public void setSerialKey(String serialKey) {
        this.serialKey = serialKey;
    }
}
