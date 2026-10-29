package modelo;

public class ClienteCombo {

    private int id;
    private String rut;
    private String razonSocial;

    public ClienteCombo(int id, String rut, String razonSocial) {
        this.id = id;
        this.rut = rut;
        this.razonSocial = razonSocial;
    }

    public int getId() {
        return id;
    }

    public String getRut() {
        return rut;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    @Override
    public String toString() {
        if (id == 0) {
            return "Todos los Clientes";
        }
        return rut + " - " + razonSocial;
    }
}
