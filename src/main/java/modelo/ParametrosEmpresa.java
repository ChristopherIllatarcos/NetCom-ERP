package modelo;

public class ParametrosEmpresa {

    private String razonSocial;
    private String rutEmpresa;
    private String direccionMatriz;
    private String telefono;
    private String cajaDefecto;
    private String giro;
    private String comuna;
    private String email;
    private String bodega;

    public ParametrosEmpresa() {
    }

    public ParametrosEmpresa(String razonSocial, String rutEmpresa, String direccionMatriz,
            String telefono, String cajaDefecto, String giro,
            String comuna, String email, String bodega) {
        this.razonSocial = razonSocial;
        this.rutEmpresa = rutEmpresa;
        this.direccionMatriz = direccionMatriz;
        this.telefono = telefono;
        this.cajaDefecto = cajaDefecto;
        this.giro = giro;
        this.comuna = comuna;
        this.email = email;
        this.bodega = bodega;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRutEmpresa() {
        return rutEmpresa;
    }

    public void setRutEmpresa(String rutEmpresa) {
        this.rutEmpresa = rutEmpresa;
    }

    public String getDireccionMatriz() {
        return direccionMatriz;
    }

    public void setDireccionMatriz(String direccionMatriz) {
        this.direccionMatriz = direccionMatriz;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCajaDefecto() {
        return cajaDefecto;
    }

    public void setCajaDefecto(String cajaDefecto) {
        this.cajaDefecto = cajaDefecto;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBodega() {
        return bodega;
    }

    public void setBodega(String bodega) {
        this.bodega = bodega;
    }
}
