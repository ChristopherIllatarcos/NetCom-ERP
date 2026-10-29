package vista.login;

import java.util.concurrent.ExecutionException;

/**
 *
 * @author caill
 */
public class FrmRecuperarContrasena extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmRecuperarContrasena.class.getName());

    /**
     * Creates new form FrmRecuperarContrasena
     *
     * @param parent
     * @param modal
     */
    public FrmRecuperarContrasena(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        aplicarColores();
        this.getRootPane().setDefaultButton(btnEnviar);
        getRootPane().registerKeyboardAction(e -> this.dispose(),
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.setSize(431, 281);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    private void aplicarColores() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);

        aplicarEfectoHoverPlano(btnEnviar, azulSuave);
        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);

    }

    private void aplicarEfectoHoverPlano(javax.swing.JButton boton, java.awt.Color colorHover) {
        // 1. Cambiar el cursor a una manito
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Configuración base plana / sin bordes
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);

        // Eventos de entrada y salida del mouse
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setContentAreaFilled(true); // Activa el fondo al pasar el cursor
                boton.setBackground(colorHover);
                boton.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setContentAreaFilled(false); // Vuelve a ser transparente
                boton.repaint();
            }
        });
    }

    private void botonEnviar() {
        // 1. Capturar el texto ingresado
        String datoIngresado = txtUsuarioCorreo.getText().trim();

        // 2. Validar campo vacío usando AlertaUtil
        if (datoIngresado.isEmpty()) {
            utilidades.AlertaUtil.mostrar("Campo vacío", "Por favor, ingrese su usuario o correo electrónico.", "advertencia");
            txtUsuarioCorreo.requestFocus();
            return;
        }

        // 3. Desactivar el botón para evitar doble clic
        btnEnviar.setEnabled(false);

        // 4. Crear el SwingWorker para procesar en segundo plano
        javax.swing.SwingWorker<Boolean, Void> worker = new javax.swing.SwingWorker<Boolean, Void>() {

            // Ventana de carga con tu GIF animado
            javax.swing.JDialog frmCargando = utilidades.AlertaUtil.mostrarCarga("Buscando información...");
            private boolean usuarioEncontrado = false;

            @Override
            protected Boolean doInBackground() throws Exception {
                // Mostrar la carga en el hilo de eventos
                java.awt.EventQueue.invokeLater(() -> {
                    frmCargando.setLocationRelativeTo(FrmRecuperarContrasena.this);
                    frmCargando.setVisible(true);
                });
                Thread.sleep(3000);

                dao.UsuarioDAO dao = new dao.UsuarioDAO();
                usuarioEncontrado = dao.verificarExistencia(datoIngresado);
                usuarioEncontrado = true;

                return usuarioEncontrado;
            }

            @Override
            protected void done() {
                // Cerrar la ventana de carga
                frmCargando.dispose();
                btnEnviar.setEnabled(true);

                try {
                    boolean exito = get();
                    if (exito) {
                        // Alerta de éxito moderna
                        utilidades.AlertaUtil.mostrar(
                                "Solicitud Exitosa",
                                "Si el usuario o correo existe en el sistema,\nse han enviado las instrucciones de recuperación.",
                                "exito"
                        );

                        // Cerramos la ventana de recuperación y volvemos al login
                        FrmRecuperarContrasena.this.dispose();
                    } else {
                        // Alerta de error si no existe
                        utilidades.AlertaUtil.mostrar(
                                "Usuario No Encontrado",
                                "No se encontró ningún registro con los datos ingresados.",
                                "error"
                        );
                        txtUsuarioCorreo.requestFocus();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    utilidades.AlertaUtil.mostrar("Error", "Ocurrió un error al procesar la solicitud.", "error");
                }
            }
        };

        worker.execute();
    }

    private void botonCancelar() {
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlPrincipal = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtUsuarioCorreo = new javax.swing.JTextField();
        btnEnviar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("RECUPERACIÓN DE CONTRASEÑA");
        pnlPrincipal.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, -1, -1));

        jLabel2.setText("Ingrese su usuario o correo registrado para enviarle");
        pnlPrincipal.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 70, -1, -1));

        jLabel3.setText("las instrucciones de restablecimiento: ");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 85, -1, -1));

        jLabel4.setText("Usuario / Correo:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 120, -1, -1));
        pnlPrincipal.add(txtUsuarioCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 140, 300, 30));

        btnEnviar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEnviar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/nueva-cuenta.png"))); // NOI18N
        btnEnviar.setText("ENVIAR");
        btnEnviar.setBorderPainted(false);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEnviar.setFocusPainted(false);
        btnEnviar.addActionListener(this::btnEnviarActionPerformed);
        pnlPrincipal.add(btnEnviar, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 190, -1, -1));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 190, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        botonEnviar();
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        botonCancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            FrmRecuperarContrasena dialog = new FrmRecuperarContrasena(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtUsuarioCorreo;
    // End of variables declaration//GEN-END:variables
}
