package modelo;

public class VentaRuta {

    private int folio;
    private String tipoDocumento;
    private String cliente;
    private String direccion;
    private double monto;
    private int bultos;
    private String estado;
    private int ventaId;
    private int clienteId;

    public VentaRuta(int folio, String tipoDocumento, String cliente, String direccion, double monto, int bultos, String estado, int ventaId, int clienteId) {
        this.folio = folio;
        this.tipoDocumento = tipoDocumento;
        this.cliente = cliente;
        this.direccion = direccion;
        this.monto = monto;
        this.bultos = bultos;
        this.estado = estado;
        this.ventaId = ventaId;
        this.clienteId = clienteId;
    }

    public int getFolio() {
        return folio;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getMonto() {
        return monto;
    }

    public int getBultos() {
        return bultos;
    }

    public String getEstado() {
        return estado;
    }

    public int getVentaId() {
        return ventaId;
    }

    public int getClienteId() {
        return clienteId;
    }
}
