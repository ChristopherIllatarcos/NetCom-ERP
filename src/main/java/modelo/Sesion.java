package modelo;

public class Sesion {

    private static int idUsuarioLogueado;
    private static String usernameLogueado;
    private static String rolLogueado;

    // Getters y Setters Estáticos para poder consultarlos desde cualquier ventana
    public static int getIdUsuarioLogueado() {
        return idUsuarioLogueado;
    }

    public static void setIdUsuarioLogueado(int id) {
        idUsuarioLogueado = id;
    }

    public static String getUsernameLogueado() {
        return usernameLogueado;
    }

    public static void setUsernameLogueado(String username) {
        usernameLogueado = username;
    }

    public static String getRolLogueado() {
        return rolLogueado;
    }

    public static void setRolLogueado(String rol) {
        rolLogueado = rol;
    }
}
