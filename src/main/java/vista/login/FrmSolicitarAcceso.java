package vista.login;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author caill
 */
public class FrmSolicitarAcceso extends javax.swing.JFrame {

    private boolean mostrarPass1 = false;
    private boolean mostrarPass2 = false;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmSolicitarAcceso.class.getName());

    public FrmSolicitarAcceso() {
        initComponents();
        aplicarColores();
        configuracionVentana();
        inicializarEventosOjos();
        configurarEventosEnter();

    }

    private void aplicarColores() {
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);
        aplicarEfectoHoverPlano(btnEnviar, azulSuave);
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

    private void botonCancelar() {
        vista.login.FrmLogin login = new vista.login.FrmLogin();

        login.setLocationRelativeTo(this);
        login.setVisible(true);

        this.dispose();
    }

    private void botonEnviar() {
        // 1. Obtener los valores de los campos
        String nombre = txtNombre.getText().trim();
        String username = txtUsuario.getText().trim();
        String correo = txtCorreo.getText().trim();
        String pass1 = new String(txtContrasena.getPassword()).trim();
        String pass2 = new String(txtConContrasena.getPassword()).trim();

        // 2. Validaciones de campos vacíos y contraseñas
        if (nombre.isEmpty() || username.isEmpty() || correo.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
            utilidades.AlertaUtil.mostrar("Campos Incompletos", "Debe rellenar todos los campos para continuar.", "advertencia");
            return;
        }

        if (!pass1.equals(pass2)) {
            utilidades.AlertaUtil.mostrar("Contraseñas no coinciden", "Las contraseñas ingresadas no coinciden, vuelva a intentar.", "advertencia");
            txtConContrasena.setText("");
            txtConContrasena.requestFocus();
            return;
        }

        // 3. Desactivar el botón para evitar clics dobles
        btnEnviar.setEnabled(false);

        // 4. Instanciar FrmCargandoMenu con texto personalizado
        vista.login.FrmCargandoMenu frmCargandoMenu = new vista.login.FrmCargandoMenu(null, true);
        frmCargandoMenu.setTextoMensaje("Enviando solicitud...");

        // 5. SwingWorker para procesar el registro en segundo plano
        javax.swing.SwingWorker<Integer, Void> worker = new javax.swing.SwingWorker<Integer, Void>() {

            @Override
            protected Integer doInBackground() throws Exception {
                dao.UsuarioDAO dao = new dao.UsuarioDAO();

                // Verificar si el usuario ya existe
                if (dao.obtenerUsuarioPorUsername(username) != null) {
                    return 1; // Usuario duplicado
                }

                // Crear objeto modelo
                modelo.Usuario nuevoUsuario = new modelo.Usuario();
                nuevoUsuario.setNombreCompleto(nombre);
                nuevoUsuario.setUsername(username);
                nuevoUsuario.setEmail(correo);
                nuevoUsuario.setPasswordHash(encriptarSHA256(pass1));
                nuevoUsuario.setActivo(false); // Inactivo por defecto (requiere aprobación del admin)

                // Registrar en BD
                boolean guardado = dao.registrarUsuario(nuevoUsuario);

                // Pequeña pausa de 1.5 seg para que se aprecie la animación del GIF
                Thread.sleep(1500);

                return guardado ? 0 : 2; // 0: Éxito, 2: Error
            }

            @Override
            protected void done() {
                // Cerramos el cargando y reactivamos botón
                frmCargandoMenu.dispose();
                btnEnviar.setEnabled(true);

                try {
                    int resultado = get();
                    switch (resultado) {
                        case 0 -> { // Éxito
                            utilidades.AlertaUtil.mostrar("Solicitud Enviada", "Su solicitud fue enviada con éxito al administrador.", "exito");
                            vista.login.FrmLogin login = new vista.login.FrmLogin();
                            login.setLocationRelativeTo(null);
                            login.setVisible(true);

                            FrmSolicitarAcceso.this.dispose();
                        }
                        case 1 -> { // Usuario duplicado
                            utilidades.AlertaUtil.mostrar("Usuario No Disponible", "El nombre de usuario '" + username + "' ya se encuentra registrado.", "advertencia");
                            txtUsuario.requestFocus();
                        }
                        case 2 -> { // Error al guardar
                            utilidades.AlertaUtil.mostrar("Error", "No se pudo registrar la solicitud. Intente nuevamente.", "error");
                        }
                    }
                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    utilidades.AlertaUtil.mostrar("Error de Sistema", "Ocurrió un error inesperado al enviar la solicitud.", "error");
                }
            }
        };

        // 6. Lanzar el cargando y ejecutar worker
        java.awt.EventQueue.invokeLater(() -> {
            frmCargandoMenu.setLocationRelativeTo(FrmSolicitarAcceso.this);
            worker.execute();
            frmCargandoMenu.setVisible(true); 
        });
    }

    private void configurarEventosEnter() {

        // 1. Del Nombre salta al Usuario
        txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtUsuario.requestFocus();
                }
            }
        });

        // 2. Del Usuario salta al Correo
        txtUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtCorreo.requestFocus();
                }
            }
        });

        // 3. Del Correo salta a la Contraseña
        txtCorreo.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtContrasena.requestFocus();
                }
            }
        });

        // 4. De la Contraseña salta a la Confirmación de Contraseña
        txtContrasena.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    txtConContrasena.requestFocus();
                }
            }
        });

        // 5. De la Confirmación de Contraseña, al presionar Enter ¡ejecuta el botón Enviar!
        txtConContrasena.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    btnEnviar.doClick(); // Simula el clic en el botón de registro
                }
            }
        });
    }

    private void configuracionVentana() {
        this.setTitle("SOLICITAR ACCESO DE USUARIO");
        this.setSize(450, 520);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }

    private void inicializarEventosOjos() {
        // 1. Cursor de manito para ambos ojitos
        lblOjoCerrado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblOjoCerrado2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // --- 1. OJITO PRINCIPAL (Contraseña) ---
        lblOjoCerrado.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarPass1 = !mostrarPass1;
                if (mostrarPass1) {
                    txtContrasena.setEchoChar((char) 0); // Muestra el texto
                    lblOjoCerrado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo-abierto.png")));
                } else {
                    txtContrasena.setEchoChar('•'); // Oculta el texto
                    lblOjoCerrado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png")));
                }
            }
        });

        // --- 2. OJITO SECUNDARIO (Confirmación) ---
        lblOjoCerrado2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mostrarPass2 = !mostrarPass2;
                if (mostrarPass2) {
                    txtConContrasena.setEchoChar((char) 0); // Muestra el texto
                    lblOjoCerrado2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo-abierto.png")));
                } else {
                    txtConContrasena.setEchoChar('•'); // Oculta el texto
                    lblOjoCerrado2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png")));
                }
            }
        });
    }

    private String encriptarSHA256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Método auxiliar para mostrar alertas personalizadas usando
     * FrmAlertaCustom
     *
     * @param titulo El título de la alerta
     * @param mensaje El texto con el detalle de la alerta
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        vista.login.FrmAlertaCustom alerta = new vista.login.FrmAlertaCustom(null, true);
        alerta.setTitulo(titulo);
        alerta.setMensaje(mensaje);
        alerta.setLocationRelativeTo(this); // Centra la alerta respecto al formulario actual
        alerta.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        lblOjoCerrado = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        jLabel7 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        lblOjoCerrado2 = new javax.swing.JLabel();
        txtConContrasena = new javax.swing.JPasswordField();
        btnCancelar = new javax.swing.JButton();
        btnEnviar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("SOLICITUD DE NUEVA CUENTA");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Por favor, complete sus datos para registrarse.");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 60, -1, -1));

        jLabel3.setText("Nombre Completo:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, -1, -1));

        txtNombre.setText(" ");
        txtNombre.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 360, 30));

        jLabel4.setText("Usuario:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 170, -1, -1));

        txtUsuario.setText(" ");
        txtUsuario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 190, 360, 30));

        jLabel5.setText("Correo Electrónico:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, -1, -1));

        txtCorreo.setText(" ");
        txtCorreo.setToolTipText("");
        txtCorreo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(txtCorreo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 360, 30));

        jLabel6.setText("Contraseña:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 290, -1, -1));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(83, 347, 283, -1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblOjoCerrado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png"))); // NOI18N
        lblOjoCerrado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel3.add(lblOjoCerrado, new org.netbeans.lib.awtextra.AbsoluteConstraints(327, 3, 30, -1));

        txtContrasena.setBorder(null);
        jPanel3.add(txtContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 2, 320, 25));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 312, 360, 30));

        jLabel7.setText("Confirmar Contraseña:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 350, -1, -1));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblOjoCerrado2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png"))); // NOI18N
        lblOjoCerrado2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel4.add(lblOjoCerrado2, new org.netbeans.lib.awtextra.AbsoluteConstraints(327, 3, 30, -1));

        txtConContrasena.setBorder(null);
        jPanel4.add(txtConContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 2, 320, 25));

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 360, 30));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        jPanel1.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 425, 130, 35));

        btnEnviar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEnviar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/nueva-cuenta.png"))); // NOI18N
        btnEnviar.setText("ENVIAR ");
        btnEnviar.setBorderPainted(false);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEnviar.setFocusPainted(false);
        btnEnviar.addActionListener(this::btnEnviarActionPerformed);
        jPanel1.add(btnEnviar, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 425, 130, 35));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 450, 520));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        botonCancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        botonEnviar();
    }//GEN-LAST:event_btnEnviarActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new FrmSolicitarAcceso().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblOjoCerrado;
    private javax.swing.JLabel lblOjoCerrado2;
    private javax.swing.JPasswordField txtConContrasena;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
