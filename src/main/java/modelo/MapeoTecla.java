package modelo;

public class MapeoTecla {

    private String tecla;
    private String accion;
    private String producto;
    private boolean estado;

    // Constructor, Getters y Setters
    public MapeoTecla(String tecla, String accion, String producto, boolean estado) {
        this.tecla = tecla;
        this.accion = accion;
        this.producto = producto;
        this.estado = estado;
    }

    public String getTecla() {
        return tecla;
    }

    public String getAccion() {
        return accion;
    }

    public String getProducto() {
        return producto;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getEstadoStr() {
        return estado ? "Activo" : "Inactivo";
    }
}
