
package modelo;

/**
 *
 * @author caill
 */
public class FamiliaDTO {

    private final int id;
    private final String codigo;
    private final String nombre;
    private final String descripcion;

    public FamiliaDTO(int id, String codigo, String nombre, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
