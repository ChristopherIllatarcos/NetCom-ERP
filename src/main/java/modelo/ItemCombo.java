package modelo;

public class ItemCombo {

    private int id;
    private String descripcion;
    private double porcentaje; // Atributo opcional para impuestos

    // Constructor estándar para Bodegas, Familias, etc.
    public ItemCombo(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
        this.porcentaje = 0.0;
    }

    // Constructor extendido para Impuestos que manejan porcentaje
    public ItemCombo(int id, String descripcion, double porcentaje) {
        this.id = id;
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getNombre() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getNombrePuro() {
        return descripcion;
    }

    @Override
    public String toString() {
        // Si tiene porcentaje mayor a 0, lo muestra formateado en el ComboBox
        if (porcentaje <= 0) {
            return descripcion;
        }
        return descripcion + " (" + porcentaje + "%)";
    }
}
