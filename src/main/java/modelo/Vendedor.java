package modelo;

public class Vendedor {

    private int id;
    private String codigo;
    private String nombre;
    private String tipo;
    private double comisionVenta;
    private double comisionCobranza;
    private boolean activo;
    private int usuarioId;

    public Vendedor() {
    }

    // Constructor completo
    public Vendedor(int id, String codigo, String nombre, String tipo, double comisionVenta, double comisionCobranza, boolean activo, int usuarioId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.comisionVenta = comisionVenta;
        this.comisionCobranza = comisionCobranza;
        this.activo = activo;
        this.usuarioId = usuarioId;
    }

    // Constructor básico para Combos
    public Vendedor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // --- GETTERS Y SETTERS ---
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getComisionVenta() {
        return comisionVenta;
    }

    public void setComisionVenta(double comisionVenta) {
        this.comisionVenta = comisionVenta;
    }

    public double getComisionCobranza() {
        return comisionCobranza;
    }

    public void setComisionCobranza(double comisionCobranza) {
        this.comisionCobranza = comisionCobranza;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return nombre; // Permite mostrar directamente el nombre en el JComboBox si guardas objetos Vendedor
    }
}
