
package utilidades;

import java.util.Date;

public class SesionUsuario {

    // Variable estática global para almacenar el rol del usuario actual (ej: "Administrador", "Vendedor")
    public static String rolActual = "";
    public static String nombreUsuarioActual = "";
    public static int idUsuarioActual = -1;
    public static Date horaInicioSesion = null;

    /**
     * Limpia la sesión actual (ideal para cuando el usuario presiona "Cerrar
     * Sesión").
     */
    public static void limpiarSesion() {
        rolActual = "";
        nombreUsuarioActual = "";
        idUsuarioActual = -1;
        horaInicioSesion = null;
    }
}
