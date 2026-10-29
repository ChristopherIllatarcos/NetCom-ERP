package modelo;

public class DetalleVenta {

    private String codigo;
    private String descripcion;
    private int cantidad;
    private double precioUnitario;
    private double totalLinea;
    private int productoId; // ID numérico de la tabla productos en BD
    private String observaciones;
    private int id;         // ID único del registro en la tabla detalle_ventas
    private int ventaId;    // ID de la cabecera de venta
    private double descuento; // Monto de descuento por ítem
    // Constructor sin parámetros
    public DetalleVenta() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    // Tu constructor actual
    public DetalleVenta(String codigo, String descripcion, int cantidad, double precioUnitario) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalLinea = cantidad * precioUnitario;
    }

    // Getters y Setters originales
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public double getTotalLinea() {
        return totalLinea;
    }

    public void setTotalLinea(double totalLinea) {
        this.totalLinea = totalLinea;
    }

    // Compatibilidad con Base de Datos (detalle_ventas)
    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

   public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }
    
    public double getSubtotal() {
        return totalLinea;
    } // Mapea totalLinea a la columna subtotal

    // Getter y Setter para observaciones
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
