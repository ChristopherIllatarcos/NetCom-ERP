package vista;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.MatteBorder;

/**
 *
 * @author caill
 */
public class FrmMenuPrincipal extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmMenuPrincipal.class.getName());

    private modelo.Usuario usuarioLogueado;

    // 1. Constructor por defecto (llama al principal pasando null)
    public FrmMenuPrincipal() {
        this(null);
    }

    // 2. Constructor con sesión de usuario (Aquí se centraliza todo)
    public FrmMenuPrincipal(modelo.Usuario usuario) {
        initComponents();

        iconoMenuSuperior();
        this.usuarioLogueado = usuario;
        sincronizarUsuario();
        cargarDatosUsuario();
        reflejarNombreRol();
        aplicarEfectoHover();
        configurarVentana();
        cargarImagen();

    }

    private void aplicarEfectoHover() {
        // Aplica el efecto hover a los botones superiores
        estilizarBotonSuperior(btnVentas);
        estilizarBotonSuperior(btnCompras);
        estilizarBotonSuperior(btnBodegas);
        estilizarBotonSuperior(btnCtacte);
        estilizarBotonSuperior(btnArchivos);
    }

    private void botonVentas() {

        for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose(); // Cierra y libera la memoria de la ventana (como compras)
        }
        // =========================================================================

        vista.menu.IFrmVentas ventanaVentas = new vista.menu.IFrmVentas();
        desktopPane.add(ventanaVentas);
        ventanaVentas.setVisible(true);
    }

    private void botonCompras() {
        for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose(); // Cierra y libera la memoria de la ventana (como compras)
        }
        // =========================================================================

        vista.menu.IFrmCompras ventanaVentas = new vista.menu.IFrmCompras();
        desktopPane.add(ventanaVentas);
        ventanaVentas.setVisible(true);
    }

    private void botonBodegas() {
        for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose(); // Cierra y libera la memoria de la ventana (como compras)
        }
        // =========================================================================

        vista.menu.IFrmBodega ventanaVentas = new vista.menu.IFrmBodega();
        desktopPane.add(ventanaVentas);
        ventanaVentas.setVisible(true);
    }

    private void botonCtaCte() {
        for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose(); // Cierra y libera la memoria de la ventana (como compras)
        }
        // =========================================================================

        vista.menu.IFrmCtaCte ventanaVentas = new vista.menu.IFrmCtaCte();
        desktopPane.add(ventanaVentas);
        ventanaVentas.setVisible(true);
    }

    private void botonArchivos() {
        for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
            frame.dispose(); // Cierra y libera la memoria de la ventana (como compras)
        }
        // =========================================================================

        vista.menu.IFrmArchivo ventanaVentas = new vista.menu.IFrmArchivo();
        desktopPane.add(ventanaVentas);
        ventanaVentas.setVisible(true);
    }

    private void cargarImagen() {
        try {
            // Carga la imagen desde la carpeta resources
            java.awt.Image icono = new javax.swing.ImageIcon(getClass().getResource("/img/portada64.png")).getImage();
            this.setIconImage(icono);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el ícono de la aplicación: " + e.getMessage());
        }

    }

    private void configurarVentana() {
        this.setTitle("NetCom ERP");
        this.setSize(1000, 729);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        desktopPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                // Recorremos todas las ventanas internas del escritorio
                for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
                    frame.dispose(); // Las cerramos y liberamos memoria al hacer clic fuera
                }
            }
        });

    }

    private void cargarDatosUsuario() {
        if (usuarioLogueado != null) {

            lblUsuarioLogueado.setText("Usuario: " + usuarioLogueado.getNombreCompleto() + " | Rol: " + usuarioLogueado.getRol());
        }
    }

    private void estilizarBotonSuperior(JButton boton) {

        boton.setContentAreaFilled(false);
        boton.setBorderPainted(true);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // Cursor de manito

        // 2. Estado inicial: Borde transparente abajo (para que no salte el texto al aparecer la línea)
        boton.setBorder(new MatteBorder(0, 0, 4, 0, new Color(0, 0, 0, 0)));

        // 3. Programamos el efecto cuando el mouse pasa por encima (Hover)
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Cuando entra el cursor, pintamos una línea blanca (o celeste) de 4px abajo
                boton.setBorder(new MatteBorder(0, 0, 4, 0, Color.WHITE));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Cuando sale el cursor, volvemos la línea invisible
                boton.setBorder(new MatteBorder(0, 0, 4, 0, new Color(0, 0, 0, 0)));
            }
        });
    }

    private void iconoMenuSuperior() {
        // --- ICONOS PARA MENÚ SUPERIOR ---
        try {
            // 1. Icono para Ventas
            java.net.URL urlVentas = getClass().getResource("/iconos/carrito.png");
            if (urlVentas != null) {
                btnVentas.setIcon(new ImageIcon(urlVentas));
            }

            // 2. Icono para Compras
            java.net.URL urlCompras = getClass().getResource("/iconos/compras.png");
            if (urlCompras != null) {
                btnCompras.setIcon(new ImageIcon(urlCompras));
            }

            // 3. Icono para Bodegas
            java.net.URL urlBodegas = getClass().getResource("/iconos/bodega.png");
            if (urlBodegas != null) {
                btnBodegas.setIcon(new ImageIcon(urlBodegas));
            }

            // 4. Icono para Clientes
            java.net.URL urlClientes = getClass().getResource("/iconos/clientes.png");
            if (urlClientes != null) {
                btnCtacte.setIcon(new ImageIcon(urlClientes));
            }

            // 5. Icono para Archivos
            java.net.URL urlArchivos = getClass().getResource("/iconos/archivos.png");
            if (urlArchivos != null) {
                btnArchivos.setIcon(new ImageIcon(urlArchivos));
            }

        } catch (Exception e) {
            System.out.println("Error cargando los iconos superiores: " + e.getMessage());
        }
    }

    private void reflejarNombreRol() {
        // 3. Reflejamos el nombre y el rol en el lblUsuarioLogueado de forma impecable
        if (this.usuarioLogueado != null && utilidades.SesionUsuario.rolActual != null && !utilidades.SesionUsuario.rolActual.isEmpty()) {
            lblUsuarioLogueado.setText("Usuario: " + utilidades.SesionUsuario.nombreUsuarioActual + " (" + utilidades.SesionUsuario.rolActual + ")");
        } else if (this.usuarioLogueado != null) {
            lblUsuarioLogueado.setText("Usuario: " + this.usuarioLogueado.getNombreCompleto() + " (" + this.usuarioLogueado.getRol() + ")");
        } else {
            lblUsuarioLogueado.setText("Usuario: Invitado");
        }
    }

    private void sincronizarUsuario() {
        // 2. Sincronizamos con SesionUsuario por si acaso no se pasó por parámetro
        if (this.usuarioLogueado == null && !utilidades.SesionUsuario.nombreUsuarioActual.isEmpty()) {
            // Creamos un usuario temporal en memoria con los datos estáticos de la sesión
            this.usuarioLogueado = new modelo.Usuario();
            this.usuarioLogueado.setNombreCompleto(utilidades.SesionUsuario.nombreUsuarioActual);
            this.usuarioLogueado.setRol(utilidades.SesionUsuario.rolActual);
            this.usuarioLogueado.setId(utilidades.SesionUsuario.idUsuarioActual);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_menu_arriba = new javax.swing.JPanel();
        btnVentas = new javax.swing.JButton();
        btnCompras = new javax.swing.JButton();
        btnBodegas = new javax.swing.JButton();
        btnCtacte = new javax.swing.JButton();
        btnArchivos = new javax.swing.JButton();
        pnl_menu_abajo = new javax.swing.JPanel();
        lblUsuarioLogueado = new javax.swing.JLabel();
        desktopPane = new javax.swing.JDesktopPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1000, 729));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnl_menu_arriba.setBackground(new java.awt.Color(0, 51, 102));
        pnl_menu_arriba.setPreferredSize(new java.awt.Dimension(850, 100));
        pnl_menu_arriba.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnVentas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnVentas.setForeground(new java.awt.Color(255, 255, 255));
        btnVentas.setText("VENTAS");
        btnVentas.addActionListener(this::btnVentasActionPerformed);
        pnl_menu_arriba.add(btnVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 130, 55));

        btnCompras.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCompras.setForeground(new java.awt.Color(255, 255, 255));
        btnCompras.setText("COMPRAS");
        btnCompras.addActionListener(this::btnComprasActionPerformed);
        pnl_menu_arriba.add(btnCompras, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 30, 140, 55));

        btnBodegas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBodegas.setForeground(new java.awt.Color(255, 255, 255));
        btnBodegas.setText("BODEGAS");
        btnBodegas.addActionListener(this::btnBodegasActionPerformed);
        pnl_menu_arriba.add(btnBodegas, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, 150, 55));

        btnCtacte.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCtacte.setForeground(new java.awt.Color(255, 255, 255));
        btnCtacte.setText("CTACTE CLIENTES");
        btnCtacte.addActionListener(this::btnCtacteActionPerformed);
        pnl_menu_arriba.add(btnCtacte, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 30, -1, 55));

        btnArchivos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnArchivos.setForeground(new java.awt.Color(255, 255, 255));
        btnArchivos.setText("ARCHIVOS");
        btnArchivos.addActionListener(this::btnArchivosActionPerformed);
        pnl_menu_arriba.add(btnArchivos, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 30, -1, 55));

        getContentPane().add(pnl_menu_arriba, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1000, 100));

        pnl_menu_abajo.setBackground(new java.awt.Color(0, 51, 102));
        pnl_menu_abajo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblUsuarioLogueado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUsuarioLogueado.setForeground(new java.awt.Color(255, 255, 255));
        lblUsuarioLogueado.setText("lblUsuarioLogueado");
        pnl_menu_abajo.add(lblUsuarioLogueado, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, -1, -1));

        getContentPane().add(pnl_menu_abajo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 640, 1000, 90));

        jLabel2.setFont(new java.awt.Font("Bookman Old Style", 2, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("VICTORIA");
        desktopPane.add(jLabel2);
        jLabel2.setBounds(480, 130, 180, 40);

        jLabel3.setFont(new java.awt.Font("Bookman Old Style", 2, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 204, 0));
        jLabel3.setText("CASA");
        desktopPane.add(jLabel3);
        jLabel3.setBounds(360, 130, 100, 40);

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("casavictoriauno@gmail.com / 2827436 / 979670222");
        desktopPane.add(jLabel1);
        jLabel1.setBounds(330, 280, 350, 20);

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Closet / Comodas / M. cocina");
        desktopPane.add(jLabel4);
        jLabel4.setBounds(370, 190, 250, 25);

        jLabel5.setBackground(new java.awt.Color(255, 255, 255));
        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Colchones / Living / Toulette");
        desktopPane.add(jLabel5);
        jLabel5.setBounds(370, 220, 250, 25);

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Plumones / Sabanas / Cortinajes");
        desktopPane.add(jLabel6);
        jLabel6.setBounds(370, 250, 290, 25);

        getContentPane().add(desktopPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 1000, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVentasActionPerformed
        botonVentas();
    }//GEN-LAST:event_btnVentasActionPerformed

    private void btnComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnComprasActionPerformed
        botonCompras();
    }//GEN-LAST:event_btnComprasActionPerformed

    private void btnBodegasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBodegasActionPerformed
        botonBodegas();
    }//GEN-LAST:event_btnBodegasActionPerformed

    private void btnCtacteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCtacteActionPerformed
        botonCtaCte();
    }//GEN-LAST:event_btnCtacteActionPerformed

    private void btnArchivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArchivosActionPerformed
        botonArchivos();
    }//GEN-LAST:event_btnArchivosActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmMenuPrincipal().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnArchivos;
    private javax.swing.JButton btnBodegas;
    private javax.swing.JButton btnCompras;
    private javax.swing.JButton btnCtacte;
    private javax.swing.JButton btnVentas;
    public static javax.swing.JDesktopPane desktopPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblUsuarioLogueado;
    private javax.swing.JPanel pnl_menu_abajo;
    private javax.swing.JPanel pnl_menu_arriba;
    // End of variables declaration//GEN-END:variables

}
