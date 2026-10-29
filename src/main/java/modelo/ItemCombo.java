package modelo;

public class ItemCombo {

    private int id;
    private String descripcion;

    public ItemCombo(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
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

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        // Esto es clave: lo que devuelva aquí es lo que se muestra visualmente en el ComboBox
        return descripcion;
    }
}
