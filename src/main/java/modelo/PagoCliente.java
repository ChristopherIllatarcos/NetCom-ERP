package modelo;

public class PagoCliente {

    private int id;
    private int clienteId;
    private String numeroRecibo;
    private String fecha;
    private String medioPago;
    private String documentoAsociado;
    private double montoPagado;
    private String estado;
    private String motivo;

    // Constructor vacío
    public PagoCliente() {
    }

    // Constructor completo
    public PagoCliente(int id, int clienteId, String numeroRecibo, String fecha, String medioPago, String documentoAsociado, double montoPagado, String estado, String motivo) {
        this.id = id;
        this.clienteId = clienteId;
        this.numeroRecibo = numeroRecibo;
        this.fecha = fecha;
        this.medioPago = medioPago;
        this.documentoAsociado = documentoAsociado;
        this.montoPagado = montoPagado;
        this.estado = estado;
        this.motivo = motivo;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getNumeroRecibo() {
        return numeroRecibo;
    }

    public void setNumeroRecibo(String numeroRecibo) {
        this.numeroRecibo = numeroRecibo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public String getDocumentoAsociado() {
        return documentoAsociado;
    }

    public void setDocumentoAsociado(String documentoAsociado) {
        this.documentoAsociado = documentoAsociado;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
