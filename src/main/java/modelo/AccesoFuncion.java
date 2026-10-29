package modelo;

public class AccesoFuncion {

    private int id;
    private String tipoAsignacion; // "ROL" o "USUARIO"
    private String referencia;     // Nombre del rol o del usuario
    private String modulo;
    private String codigoFuncion;
    private String nombreFuncion;
    private boolean permitido;

    public AccesoFuncion() {
    }

    public AccesoFuncion(int id, String tipoAsignacion, String referencia, String modulo, String codigoFuncion, String nombreFuncion, boolean permitido) {
        this.id = id;
        this.tipoAsignacion = tipoAsignacion;
        this.referencia = referencia;
        this.modulo = modulo;
        this.codigoFuncion = codigoFuncion;
        this.nombreFuncion = nombreFuncion;
        this.permitido = permitido;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoAsignacion() {
        return tipoAsignacion;
    }

    public void setTipoAsignacion(String tipoAsignacion) {
        this.tipoAsignacion = tipoAsignacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getCodigoFuncion() {
        return codigoFuncion;
    }

    public void setCodigoFuncion(String codigoFuncion) {
        this.codigoFuncion = codigoFuncion;
    }

    public String getNombreFuncion() {
        return nombreFuncion;
    }

    public void setNombreFuncion(String nombreFuncion) {
        this.nombreFuncion = nombreFuncion;
    }

    public boolean isPermitido() {
        return permitido;
    }

    public void setPermitido(boolean permitido) {
        this.permitido = permitido;
    }
}
