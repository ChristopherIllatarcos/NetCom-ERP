package utilidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

public class BotonRedondeado extends JButton {

    private final int radioEsquinas = 50; // Puedes cambiar este valor para hacerlo más o menos redondo

    public BotonRedondeado(String text) {
        super(text);
        setContentAreaFilled(false); // Desactiva el fondo por defecto de Java
        setFocusPainted(false);      // Quita el borde de selección al hacer clic
        setBorderPainted(false);     // Quita el borde estándar
        setForeground(Color.WHITE);    // Color del texto (blanco)
        setBackground(new Color(30, 144, 255)); // Azul moderno (puedes ajustarlo a tu tono)
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // Cursor de manito
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Activar suavizado para que los bordes no se vean pixelados
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Si el botón está presionado, oscurecemos un poquito el color de fondo
        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            // Si pasas el mouse por encima, aclaramos un poco o mantenemos
            g2.setColor(getBackground().brighter());
        } else {
            g2.setColor(getBackground());
        }

        // Dibuja el rectángulo con esquinas redondeadas
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radioEsquinas, radioEsquinas);

        g2.dispose();

        // Dibuja el texto encima del botón
        super.paintComponent(g);
    }
}
