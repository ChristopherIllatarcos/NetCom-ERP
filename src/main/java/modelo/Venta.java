package modelo;

import java.util.Date;
import java.util.List;

public class Venta {

    private String tipoDoc;
    private int folio; 
    private String numeroDocumento;
    private int clienteId;         // Para calce con 'cliente_id (int4)' de la BD
    private int vendedorId;        // Para calce con 'vendedor_id (int4)' de la BD
    private String rutCliente;
    private String nombreCliente;
    private Date fecha;
    private String condicionPago;
    private double neto;           // Actúa como subtotal
    private double descuento;
    private double iva;
    private double total;
    private List<DetalleVenta> detalles;
    private int id;
    private String estado;
    private String nombreVendedor;

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }
    

    public Venta() {
    }

    // Getters y Setters
    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    // Alias para compatibilidad con VentaDAO
    public String getTipoDocumento() {
        return tipoDoc;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDoc = tipoDocumento;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(int vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getRutCliente() {
        return rutCliente;
    }

    public void setRutCliente(String rutCliente) {
        this.rutCliente = rutCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    // Alias para compatibilidad de fecha
    public Date getFechaEmision() {
        return fecha;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fecha = fechaEmision;
    }

    public String getCondicionPago() {
        return condicionPago;
    }

    public void setCondicionPago(String condicionPago) {
        this.condicionPago = condicionPago;
    }

    // Alias para forma de pago
    public String getFormaPago() {
        return condicionPago;
    }

    public void setFormaPago(String formaPago) {
        this.condicionPago = formaPago;
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(double neto) {
        this.neto = neto;
    }

    // Alias para subtotal
    public double getSubtotal() {
        return neto;
    }

    public void setSubtotal(double subtotal) {
        this.neto = subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
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

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

// Si usas el alias setVentaId/getVentaId para compatibilidad:
    public int getVentaId() {
        return id;
    }

    public void setVentaId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    
  
}
