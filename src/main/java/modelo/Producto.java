package modelo;

public class Producto {

    private int id;
    private String codigoInterno;
    private String codigoBarra;
    private String descripcion; // (Equivale a descripcionComercial de la interfaz)
    private int categoriaId;    // cmbFamiliaCategoria
    private boolean activo;     // rbdActivo / rbdInactivo

    // Precios y Costos
    private double costoNeto;
    private double margenGanancia;
    private double precioUnitario; // (Equivale a Precio Venta Público)
    private int impuestoAdicionalId; // cmbImpuestoAdicional

    // Stock y Control
    private double stockActual;
    private double stockMinimo;
    private int ubicacionBodegaId; // cmbUbicacionBodega

    // Constructores
    public Producto() {
    }

    public Producto(String codigoBarra, String descripcion, double precioUnitario) {
        this.codigoBarra = codigoBarra;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters completos
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public double getCostoNeto() {
        return costoNeto;
    }

    public void setCostoNeto(double costoNeto) {
        this.costoNeto = costoNeto;
    }

    public double getMargenGanancia() {
        return margenGanancia;
    }

    public void setMargenGanancia(double margenGanancia) {
        this.margenGanancia = margenGanancia;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getImpuestoAdicionalId() {
        return impuestoAdicionalId;
    }

    public void setImpuestoAdicionalId(int impuestoAdicionalId) {
        this.impuestoAdicionalId = impuestoAdicionalId;
    }

    public double getStockActual() {
        return stockActual;
    }

    public void setStockActual(double stockActual) {
        this.stockActual = stockActual;
    }

    public double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getUbicacionBodegaId() {
        return ubicacionBodegaId;
    }

    public void setUbicacionBodegaId(int ubicacionBodegaId) {
        this.ubicacionBodegaId = ubicacionBodegaId;
    }
}
