package modelo;

public class ComisionVendedor {

    private int numeroCobranza;
    private int vendedorId;
    private String nombreVendedor;
    private double cobranzaTotal;
    private double comision;

    public ComisionVendedor() {
    }

    // Getters y Setters
    public int getNumeroCobranza() {
        return numeroCobranza;
    }

    public void setNumeroCobranza(int numeroCobranza) {
        this.numeroCobranza = numeroCobranza;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(int vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public double getCobranzaTotal() {
        return cobranzaTotal;
    }

    public void setCobranzaTotal(double cobranzaTotal) {
        this.cobranzaTotal = cobranzaTotal;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }
}
