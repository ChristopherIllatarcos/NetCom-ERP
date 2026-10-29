package utilidades;

import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.Dimension;

public class DisenoEstilos {

    /**
     * Deja el botón plano y configura su línea separadora personalizada
     * (grosor, color e invisible al inicio)
     *
     * * @param boton El JButton a estilizar
     * @param boton
     * @param separador El JSeparator que va debajo
     * @param ancho El largo horizontal de la línea (ej: 90)
     * @param grosor El alto o grosor de la línea (ej: 4)
     * @param color El color que tendrá la línea (ej: Color.WHITE o
     * Color.LIGHT_GRAY)
     */
    public static void aplicarEstiloBotonConLinea(JButton boton, JSeparator separador, int ancho, int grosor, Color color) {
        // 1. Estilo plano para el botón
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setRolloverEnabled(true);
        boton.putClientProperty("FlatLaf.style", ""); // Limpiamos estilos automáticos de FlatLaf

        // 2. Configuración estética del separador
        separador.setPreferredSize(new Dimension(ancho, grosor));
        separador.setOpaque(true);
        separador.setBackground(color);
        separador.setForeground(color);

        // 3. Inicia oculto por defecto
        separador.setVisible(false);
    }


}
