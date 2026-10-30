package vista.login;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author caill
 */
public class FrmLogin extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FrmLogin.class.getName());

    // Cargas de los íconos (ajusta las rutas según la ubicación de tus imágenes en resources)
    private final javax.swing.ImageIcon iconoOjoAbierto = new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo-abierto.png"));
    private final javax.swing.ImageIcon iconoOjoCerrado = new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png"));
    // Estado para controlar si la contraseña está visible u oculta
    private boolean passVisible = false;

    public FrmLogin() {
        initComponents();
        cargarPreferenciasRecordar();
        cargarEstilosubrayado();
        saltoLinea();
        configuracionVentana();
        cargarImagen();
    }

    private void botonAcceder() {
        // 1. Capturar datos de los campos
        String usuarioIngresado = txtUsuario.getText().trim();
        String passwordIngresada = new String(txtContrasena.getPassword()).trim();

        // 2. Validar campos vacíos usando AlertaUtil
        if (usuarioIngresado.isEmpty() || passwordIngresada.isEmpty()) {
            utilidades.AlertaUtil.mostrar("Campos vacíos", "Por favor, ingrese el usuario y la contraseña.", "advertencia");
            return;
        }

        // --- GESTIÓN DE PREFERENCIAS ("RECUÉRDARME") ---
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(FrmLogin.class);
        if (cboRecordarme.isSelected()) {
            prefs.put("usuario_recuerdame", usuarioIngresado);
            prefs.putBoolean("check_recuerdame", true);
        } else {
            prefs.remove("usuario_recuerdame");
            prefs.putBoolean("check_recuerdame", false);
        }
        // ----------------------------------------------

        // 3. Desactivar el botón para evitar doble clic mientras procesa
        btnAcceder.setEnabled(false);

        // 4. Crear el SwingWorker para procesar la BD en segundo plano
        javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<Void, Void>() {

            // Instanciamos el diálogo de carga usando nuestro AlertaUtil
            javax.swing.JDialog frmCargando = utilidades.AlertaUtil.mostrarCarga("Iniciando sesión...");

            // Variables para manejar los resultados de la base de datos
            private modelo.Usuario usuario = null;
            private int estadoResultado = 0; // 0: Éxito, 1: Inactivo/Bloqueado, 2: Password Incorrecto, 3: No existe

            @Override
            protected Void doInBackground() throws Exception {
                // Mostrar la ventana de carga de forma segura en el hilo de eventos
                java.awt.EventQueue.invokeLater(() -> {
                    frmCargando.setLocationRelativeTo(FrmLogin.this);
                    frmCargando.setVisible(true);
                });

                // --- CONSULTA A LA BASE DE DATOS (En segundo plano) ---
                dao.UsuarioDAO dao = new dao.UsuarioDAO();
                usuario = dao.obtenerUsuarioPorUsername(usuarioIngresado);

                if (usuario != null) {
                    // Validamos si el usuario ya está inactivo o bloqueado
                    if (!usuario.isActivo()) {
                        estadoResultado = 1; // Inactivo / Bloqueado
                    } else {
                        // Encriptamos la contraseña ingresada para compararla con el Hash de la BD
                        String passwordHashIngresada = encriptarSHA256(passwordIngresada);

                        if (usuario.getPasswordHash().equals(passwordHashIngresada)) {
                            // Si acierta, reiniciamos los intentos fallidos a 0 y permitimos el acceso
                            dao.reiniciarIntentosFallidos(usuario.getId());
                            estadoResultado = 0; // Éxito
                        } else {
                            // Si falla la contraseña, sumamos 1 intento fallido en la BD (lo que puede bloquearlo si llega a 3)
                            dao.incrementarIntentoFallido(usuario.getId());
                            estadoResultado = 2; // Contraseña incorrecta
                        }
                    }
                } else {
                    estadoResultado = 3; // Usuario no existe
                }

                return null;
            }

            @Override
            protected void done() {
                // Cerramos la ventana de carga del login
                frmCargando.dispose();

                // Reactivamos el botón
                btnAcceder.setEnabled(true);

                // Evaluamos el resultado y mostramos las alertas con AlertaUtil
                switch (estadoResultado) {
                    case 0 -> { // Éxito
                        // 1. Guardamos los datos de sesión inmediatamente
                        utilidades.SesionUsuario.rolActual = usuario.getRol();
                        utilidades.SesionUsuario.nombreUsuarioActual = usuario.getNombreCompleto();
                        utilidades.SesionUsuario.idUsuarioActual = usuario.getId();
                        utilidades.SesionUsuario.horaInicioSesion = new java.util.Date();

                        // 2. Alerta de éxito moderna con AlertaUtil
                        utilidades.AlertaUtil.mostrar("¡Bienvenido!", "¡Hola, " + usuario.getNombreCompleto() + "! Acceso concedido.", "exito");

                        // 3. Ventana de carga "Espere..." mientras prepara el Menú Principal
                        javax.swing.JDialog cargandoMenu = utilidades.AlertaUtil.mostrarCarga("Abriendo sistema, espere...");

                        javax.swing.SwingWorker<Void, Void> workerMenu = new javax.swing.SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                Thread.sleep(3000); // Pausa elegante para que se luzca el gif de carga
                                return null;
                            }

                            @Override
                            protected void done() {
                                cargandoMenu.dispose();

                                // 4. Abrimos el menú principal y cerramos el login
                                vista.FrmMenuPrincipal menu = new vista.FrmMenuPrincipal();
                                menu.setVisible(true);
                                FrmLogin.this.dispose();
                            }
                        };

                        workerMenu.execute();
                        cargandoMenu.setLocationRelativeTo(FrmLogin.this);
                        cargandoMenu.setVisible(true);
                    }

                    case 1 -> { // Inactivo o Bloqueado
                        utilidades.AlertaUtil.mostrar("Cuenta Bloqueada", "Su cuenta se encuentra inactiva o bloqueada por seguridad.\nContacte al Administrador.", "error");
                    }

                    case 2 -> { // Contraseña incorrecta
                        utilidades.AlertaUtil.mostrar("Error de Autenticación", "La contraseña ingresada es incorrecta.\n(Si acumula 3 intentos fallidos, la cuenta será bloqueada)", "error");
                        txtContrasena.setText("");
                        txtContrasena.requestFocus();
                    }

                    case 3 -> { // No existe
                        utilidades.AlertaUtil.mostrar("Error de Autenticación", "El usuario ingresado no existe en la base de datos.", "error");
                        txtUsuario.requestFocus();
                    }
                }
            }
        };

        worker.execute();
    }

    private void botonAcceso() {
        vista.login.FrmSolicitarAcceso solicitud = new vista.login.FrmSolicitarAcceso();

        solicitud.setLocationRelativeTo(this);
        solicitud.setVisible(true);
        this.dispose();
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

    private void configuracionVentana() {
        //this.setTitle("INICIAR SESIÓN");
        this.setSize(720, 500);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

    }

    private void cargarPreferenciasRecordar() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(FrmLogin.class);

        // Leemos el usuario guardado (si no hay nada, devuelve una cadena vacía "")
        String usuarioGuardado = prefs.get("usuario_recuerdame", "");
        boolean recordarActivo = prefs.getBoolean("check_recuerdame", false);

        if (recordarActivo) {
            txtUsuario.setText(usuarioGuardado); // Rellena el campo de usuario
            cboRecordarme.setSelected(true);   // Marca el checkbox
            txtContrasena.requestFocus();       // Manda el cursor directo a la contraseña
        }
    }

    private void cargarEstilosubrayado() {
        // 1. Cambiar el cursor a manito y configurar el color inicial (ej. un gris oscuro o azul corporativo)
        lblOlvidemiContrasena.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        java.awt.Color colorOriginal = lblOlvidemiContrasena.getForeground();

// 2. Agregar el efecto visual cuando pasa y sale el mouse
        lblOlvidemiContrasena.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Azul más claro cuando pones el mouse encima (Ej: Celeste/Azul claro moderno)
                lblOlvidemiContrasena.setForeground(new java.awt.Color(51, 153, 255));

                lblOlvidemiContrasena.setText("<html><u>¿Olvidaste tu contraseña?</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Regresa a su color original cuando el mouse se aleja
                lblOlvidemiContrasena.setForeground(colorOriginal);

                // Regresa al texto normal sin subrayar
                lblOlvidemiContrasena.setText("¿Olvidaste tu contraseña?");
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

    private void lblOjoAbierto() {
        if (passVisible) {
            // Oculta la contraseña y cambia el ícono a cerrado
            txtContrasena.setEchoChar('•');
            lblOjoAbieto.setIcon(iconoOjoCerrado);
            passVisible = false;
        } else {
            // Muestra la contraseña en texto plano y cambia el ícono a abierto
            txtContrasena.setEchoChar((char) 0);
            lblOjoAbieto.setIcon(iconoOjoAbierto);
            passVisible = true;
        }
    }

    private void recuperarContrasena() {
        // Instanciamos y abrimos nuestro JDialog moderno de recuperación
        vista.login.FrmRecuperarContrasena frmRecuperar = new vista.login.FrmRecuperarContrasena(this, true);
        frmRecuperar.setLocationRelativeTo(this); // Centrado respecto al login
        frmRecuperar.setVisible(true);
    }

    private void saltoLinea() {
        // 1. Al presionar ENTER en el campo de Usuario, pasa el foco al campo de Contraseña
        txtUsuario.addActionListener((java.awt.event.ActionEvent evt) -> {
            txtContrasena.requestFocus(); // Pasa el cursor a la contraseña
        });

        // 2. Al presionar ENTER en el campo de Contraseña, simula clic en el botón Acceder
        txtContrasena.addActionListener((java.awt.event.ActionEvent evt) -> {
            btnAcceder.doClick(); // Ejecuta el evento del botón de inicio de sesión
        });

        // Inicia con la contraseña oculta y el ojo cerrado
        txtContrasena.setEchoChar('•');
        lblOjoAbieto.setIcon(iconoOjoCerrado);

        // Cambia el cursor a una manito al pasar sobre el ojo
        lblOjoAbieto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnllogin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlUser = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        pnlPass = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtContrasena = new javax.swing.JPasswordField();
        lblOjoAbieto = new javax.swing.JLabel();
        cboRecordarme = new javax.swing.JCheckBox();
        lblOlvidemiContrasena = new javax.swing.JLabel();
        btnAcceder = new utilidades.BotonRedondeado("ACCEDER");
        btnAcceso = new utilidades.BotonRedondeado("Solicitar acceso")
        ;
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblPortada = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnllogin.setBackground(new java.awt.Color(255, 255, 255));
        pnllogin.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/portada64.png"))); // NOI18N
        pnllogin.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(135, 30, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(102, 102, 102));
        jLabel2.setText("INICIAR SESIÓN");
        pnllogin.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(105, 110, -1, -1));

        pnlUser.setBackground(new java.awt.Color(255, 255, 255));
        pnlUser.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlUser.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/user.png"))); // NOI18N
        pnlUser.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, 20, 25));

        txtUsuario.setBorder(null);
        pnlUser.add(txtUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 5, 210, 25));

        pnllogin.add(pnlUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 150, 250, 35));

        pnlPass.setBackground(new java.awt.Color(255, 255, 255));
        pnlPass.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlPass.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/candado.png"))); // NOI18N
        pnlPass.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, -1, -1));

        txtContrasena.setBorder(null);
        txtContrasena.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtContrasenaMouseClicked(evt);
            }
        });
        pnlPass.add(txtContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 5, 183, 25));

        lblOjoAbieto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo_cerrado.png"))); // NOI18N
        lblOjoAbieto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOjoAbietoMouseClicked(evt);
            }
        });
        pnlPass.add(lblOjoAbieto, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 5, -1, -1));

        pnllogin.add(pnlPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 195, 250, 35));

        cboRecordarme.setText("Recordarme");
        pnllogin.add(cboRecordarme, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 240, -1, -1));

        lblOlvidemiContrasena.setForeground(new java.awt.Color(0, 102, 204));
        lblOlvidemiContrasena.setText("Olvidé mi contraseña");
        lblOlvidemiContrasena.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblOlvidemiContrasena.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblOlvidemiContrasenaMouseClicked(evt);
            }
        });
        pnllogin.add(lblOlvidemiContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 240, -1, -1));

        btnAcceder.setBackground(new java.awt.Color(0, 51, 153));
        btnAcceder.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAcceder.setForeground(new java.awt.Color(255, 255, 255));
        btnAcceder.setText("ACCEDER");
        btnAcceder.addActionListener(this::btnAccederActionPerformed);
        pnllogin.add(btnAcceder, new org.netbeans.lib.awtextra.AbsoluteConstraints(45, 280, 260, 40));

        btnAcceso.setBackground(new java.awt.Color(102, 102, 102));
        btnAcceso.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAcceso.setText("SOLICITAR ACCESO");
        btnAcceso.addActionListener(this::btnAccesoActionPerformed);
        pnllogin.add(btnAcceso, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 340, 200, 35));

        getContentPane().add(pnllogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 0, 360, 500));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("HOLA,");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 160, -1, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("¡BIENVENIDO!");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 200, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("NetCom ERP");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Comercial.");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 390, 90, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText(" Sistema de Control Logístico y Moblaje");
        jLabel5.setToolTipText("");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 280, 20));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/portada64.png"))); // NOI18N
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        lblPortada.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fondo8.jpg"))); // NOI18N
        getContentPane().add(lblPortada, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 360, 500));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtContrasenaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtContrasenaMouseClicked

    }//GEN-LAST:event_txtContrasenaMouseClicked

    private void lblOjoAbietoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOjoAbietoMouseClicked
        lblOjoAbierto();
    }//GEN-LAST:event_lblOjoAbietoMouseClicked

    private void btnAccederActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccederActionPerformed
        botonAcceder();
    }//GEN-LAST:event_btnAccederActionPerformed

    private void btnAccesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccesoActionPerformed
        botonAcceso();
    }//GEN-LAST:event_btnAccesoActionPerformed

    private void lblOlvidemiContrasenaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblOlvidemiContrasenaMouseClicked
        recuperarContrasena();
    }//GEN-LAST:event_lblOlvidemiContrasenaMouseClicked

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
        java.awt.EventQueue.invokeLater(() -> new FrmLogin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcceder;
    private javax.swing.JButton btnAcceso;
    private javax.swing.JCheckBox cboRecordarme;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel lblOjoAbieto;
    private javax.swing.JLabel lblOlvidemiContrasena;
    private javax.swing.JLabel lblPortada;
    private javax.swing.JPanel pnlPass;
    private javax.swing.JPanel pnlUser;
    private javax.swing.JPanel pnllogin;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
