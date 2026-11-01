package utilidades;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

public class FondoDesktop extends JDesktopPane {

    private Image fondo;

    public FondoDesktop() {
        java.net.URL url = getClass().getResource("/img/fondo.jpg");
        if (url != null) {
            fondo = new ImageIcon(url).getImage();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
        g2.dispose();
    }
}
