package modelo;

public class Cliente {

    private int id;
    private String rut;
    private String razonSocial;
    private String nombre;
    private String giroRubro;
    private boolean activo;
    private String direccion;
    private String comuna;
    private String ciudad;
    private String telefono;
    private String email;
    private double cupoLimiteCredito;
    private int diasCredito;
    private int vendedorId;
    private int listaPrecioId;
   

    // Constructor vacío
    public Cliente() {
        this.activo = true; // Por defecto nace activo
    }

    // Constructor con los campos principales
    public Cliente(int id, String rut, String nombre) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.activo = true;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGiroRubro() {
        return giroRubro;
    }

    public void setGiroRubro(String giroRubro) {
        this.giroRubro = giroRubro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getCupoLimiteCredito() {
        return cupoLimiteCredito;
    }

    public void setCupoLimiteCredito(double cupoLimiteCredito) {
        this.cupoLimiteCredito = cupoLimiteCredito;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public int getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(int vendedorId) {
        this.vendedorId = vendedorId;
    }

    public int getListaPrecioId() {
        return listaPrecioId;
    }

    public void setListaPrecioId(int listaPreciosId) {
        this.listaPrecioId = listaPreciosId;
    }

   
    
   
}
