package utilidades;

import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class PanelImagenFondo extends JPanel {

    private Image imagen;

    public PanelImagenFondo(String rutaImagen) {
        try {
            URL url = getClass().getResource(rutaImagen);
            if (url != null) {
                imagen = new ImageIcon(url).getImage();
            } else {
                System.err.println("No se encontró la imagen en la ruta: " + rutaImagen);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagen != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int imgWidth = imagen.getWidth(this);
            int imgHeight = imagen.getHeight(this);

            if (imgWidth > 0 && imgHeight > 0) {
                // Calcula la escala para rellenar todo el panel sin deformar bruscamente (estilo Cover)
                double scaleX = (double) panelWidth / imgWidth;
                double scaleY = (double) panelHeight / imgHeight;
                double scale = Math.max(scaleX, scaleY);

                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);

                // Centra la imagen por si se sale un poco por los lados
                int x = (panelWidth - newWidth) / 2;
                int y = (panelHeight - newHeight) / 2;

                g.drawImage(imagen, x, y, newWidth, newHeight, this);
            }
        }
    }
}
