package vista.menu;

import java.beans.PropertyVetoException;
import vista.FrmMenuPrincipal;
import vista.archivos.DfrmCerrarSesion;
import utilidades.SesionUsuario;

/**
 *
 * @author caill
 */
public class IFrmArchivo extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameArchivo
     */
    public IFrmArchivo() {
        initComponents();
        utilidades();
        configurarVentana();

    }

    private void botonUsuariosSistema() {
        // 1. Validamos si el usuario actual tiene privilegios de Administrador
        if (SesionUsuario.rolActual != null && SesionUsuario.rolActual.equalsIgnoreCase("Administrador")) {

            
            try {
                vista.archivos.IFrmUsuarios frame = new vista.archivos.IFrmUsuarios();
                FrmMenuPrincipal.desktopPane.add(frame);
                frame.setVisible(true);
                frame.setSelected(true);
                this.dispose(); // Cierra el menú desplegable de iconos
            } catch (PropertyVetoException e) {
                System.out.println("Error al abrir Usuarios del Sistemas: " + e.getMessage());
            }

        } else {
            //NO TIENE PRIVILEGIOS: Lanzamos la alerta estilo SweetAlert ---
            mostrarAlertaSinPrivilegios();
        }
    }

    private void mostrarAlertaSinPrivilegios() {
       
        javax.swing.JDialog dialogo = new javax.swing.JDialog((javax.swing.JFrame) null, "Acceso Denegado", true);
        dialogo.setUndecorated(true); // Le quitamos los bordes estándar de la ventana
        dialogo.setSize(420, 190);
        dialogo.setLocationRelativeTo(this); // Se centra respecto a tu menú principal

        // Panel principal con fondo blanco y borde sutil rojo
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(220, 53, 69), 2));
        panel.setLayout(null);

        // Etiqueta de Título
        javax.swing.JLabel lblTitulo = new javax.swing.JLabel("¡Acceso Restringido!");
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        lblTitulo.setForeground(new java.awt.Color(220, 53, 69));
        lblTitulo.setBounds(30, 25, 360, 25);
        panel.add(lblTitulo);

        // Etiqueta de Mensaje descriptivo
        javax.swing.JLabel lblMensaje = new javax.swing.JLabel("<html>Usted no tiene privilegios de Administrador para entrar al módulo de Usuarios del Sistema.</html>");
        lblMensaje.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        lblMensaje.setForeground(new java.awt.Color(100, 100, 100));
        lblMensaje.setBounds(30, 60, 360, 45);
        panel.add(lblMensaje);

        // Botón Aceptar con estilo moderno
        javax.swing.JButton btnAceptar = new javax.swing.JButton("Entendido");
        btnAceptar.setBounds(150, 125, 120, 35);
        btnAceptar.setBackground(new java.awt.Color(220, 53, 69));
        btnAceptar.setForeground(java.awt.Color.WHITE);
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorder(null);

        // Acción del botón para cerrar la alerta al hacer clic
        btnAceptar.addActionListener(e -> dialogo.dispose());
        panel.add(btnAceptar);

        dialogo.add(panel);
        dialogo.setVisible(true);
    }

    private void botonIndexar() {
        try {
            vista.archivos.IFrmIndexacion frame = new vista.archivos.IFrmIndexacion();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Indexación a la base de datos: " + e.getMessage());
        }
    }

    private void botonFinalizar() {
        // 1. Obtener la referencia de la ventana principal 
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        String usuarioActual = "Administrador"; 
        String horaInicio = "23/10/2026 19:30";

        // 3. Cerrar/ocultar la ventana actual del Menú Archivo
        this.dispose();

        // 4. Crear el JDialog pasándole los 4 parámetros correctos
        DfrmCerrarSesion dialog = new DfrmCerrarSesion(parentFrame, true, usuarioActual, horaInicio);

        // 5. Mostrar el diálogo modal centrado
        dialog.setVisible(true);
    }

    private void botonFoliador() {
        try {
            vista.archivos.IFrmFoliadorHojas frame = new vista.archivos.IFrmFoliadorHojas();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Foliador de Hojas: " + e.getMessage());
        }
    }

    private void botonCamara() {
        try {
            vista.archivos.IFrmCamaraKay frame = new vista.archivos.IFrmCamaraKay();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Camara Key: " + e.getMessage());
        }
    }

    private void botonParametros() {
        try {
            vista.archivos.IFrmParametrosCorrelativos frame = new vista.archivos.IFrmParametrosCorrelativos();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Parametros y Correlativos: " + e.getMessage());
        }
    }

    private void botonLicencia() {
        try {
            vista.archivos.IFrmLicenciaUso frame = new vista.archivos.IFrmLicenciaUso();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Licencia de Uso: " + e.getMessage());
        }
    }

    private void botonAccesoFunciones() {
        try {
            vista.archivos.IFrmAccesosFunciones frame = new vista.archivos.IFrmAccesosFunciones();
            FrmMenuPrincipal.desktopPane.add(frame);
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose(); // Cierra el menú desplegable de iconos
        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Licencia de Uso: " + e.getMessage());
        }
    }

    private void configurarVentana() {
        this.setTitle("Menú Archivo");
        this.setSize(748, 500);
        this.setResizable(false);

    }

    private void utilidades() {
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnFinalizarSesion, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnUsuario, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnAccesoFunciones, jSeparator3, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnCamara, jSeparator4, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnFoliador, jSeparator5, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnParametros, jSeparator6, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnIndexar, jSeparator7, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnLicencia, jSeparator8, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlArchivo = new javax.swing.JPanel();
        btnFinalizarSesion = new javax.swing.JButton();
        btnUsuario = new javax.swing.JButton();
        btnAccesoFunciones = new javax.swing.JButton();
        btnCamara = new javax.swing.JButton();
        btnFoliador = new javax.swing.JButton();
        btnParametros = new javax.swing.JButton();
        btnIndexar = new javax.swing.JButton();
        btnLicencia = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();

        pnlArchivo.setBackground(new java.awt.Color(255, 255, 255));
        pnlArchivo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnFinalizarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/finalizar-la-sesion.png"))); // NOI18N
        btnFinalizarSesion.setText("Finalizar Sesión");
        btnFinalizarSesion.setBorderPainted(false);
        btnFinalizarSesion.setContentAreaFilled(false);
        btnFinalizarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFinalizarSesion.setFocusPainted(false);
        btnFinalizarSesion.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFinalizarSesion.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFinalizarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFinalizarSesionMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFinalizarSesionMouseExited(evt);
            }
        });
        btnFinalizarSesion.addActionListener(this::btnFinalizarSesionActionPerformed);
        pnlArchivo.add(btnFinalizarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 80, 155, 120));

        btnUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/usuario.png"))); // NOI18N
        btnUsuario.setText("Usuarios del Sistema");
        btnUsuario.setBorderPainted(false);
        btnUsuario.setContentAreaFilled(false);
        btnUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnUsuario.setFocusPainted(false);
        btnUsuario.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUsuario.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnUsuarioMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnUsuarioMouseExited(evt);
            }
        });
        btnUsuario.addActionListener(this::btnUsuarioActionPerformed);
        pnlArchivo.add(btnUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 80, 155, 120));

        btnAccesoFunciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/facil-acceso.png"))); // NOI18N
        btnAccesoFunciones.setText("Acceso a Funciones");
        btnAccesoFunciones.setBorderPainted(false);
        btnAccesoFunciones.setContentAreaFilled(false);
        btnAccesoFunciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAccesoFunciones.setFocusPainted(false);
        btnAccesoFunciones.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAccesoFunciones.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAccesoFunciones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAccesoFuncionesMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAccesoFuncionesMouseExited(evt);
            }
        });
        btnAccesoFunciones.addActionListener(this::btnAccesoFuncionesActionPerformed);
        pnlArchivo.add(btnAccesoFunciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 80, 155, 120));

        btnCamara.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/camara-web.png"))); // NOI18N
        btnCamara.setText("Cámara Key");
        btnCamara.setBorderPainted(false);
        btnCamara.setContentAreaFilled(false);
        btnCamara.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCamara.setFocusPainted(false);
        btnCamara.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCamara.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCamara.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCamaraMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCamaraMouseExited(evt);
            }
        });
        btnCamara.addActionListener(this::btnCamaraActionPerformed);
        pnlArchivo.add(btnCamara, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 80, 155, 120));

        btnFoliador.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/papel.png"))); // NOI18N
        btnFoliador.setText("Foliador de Hojas");
        btnFoliador.setBorderPainted(false);
        btnFoliador.setContentAreaFilled(false);
        btnFoliador.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFoliador.setFocusPainted(false);
        btnFoliador.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFoliador.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFoliador.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFoliadorMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFoliadorMouseExited(evt);
            }
        });
        btnFoliador.addActionListener(this::btnFoliadorActionPerformed);
        pnlArchivo.add(btnFoliador, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 250, 155, 120));

        btnParametros.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/parametro.png"))); // NOI18N
        btnParametros.setText("Parametros y Correlativos");
        btnParametros.setBorderPainted(false);
        btnParametros.setContentAreaFilled(false);
        btnParametros.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnParametros.setFocusPainted(false);
        btnParametros.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnParametros.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnParametros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnParametrosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnParametrosMouseExited(evt);
            }
        });
        btnParametros.addActionListener(this::btnParametrosActionPerformed);
        pnlArchivo.add(btnParametros, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 250, 170, 120));

        btnIndexar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/servidor-de-datos.png"))); // NOI18N
        btnIndexar.setText("Indexar BD");
        btnIndexar.setBorderPainted(false);
        btnIndexar.setContentAreaFilled(false);
        btnIndexar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIndexar.setFocusPainted(false);
        btnIndexar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnIndexar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnIndexar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnIndexarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnIndexarMouseExited(evt);
            }
        });
        btnIndexar.addActionListener(this::btnIndexarActionPerformed);
        pnlArchivo.add(btnIndexar, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 250, 155, 120));

        btnLicencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/licencia-de-software.png"))); // NOI18N
        btnLicencia.setText("Licencia de Uso");
        btnLicencia.setBorderPainted(false);
        btnLicencia.setContentAreaFilled(false);
        btnLicencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLicencia.setFocusPainted(false);
        btnLicencia.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLicencia.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLicencia.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLicenciaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLicenciaMouseExited(evt);
            }
        });
        btnLicencia.addActionListener(this::btnLicenciaActionPerformed);
        pnlArchivo.add(btnLicencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 250, 155, 120));
        pnlArchivo.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 190, 100, -1));
        pnlArchivo.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 190, 120, -1));
        pnlArchivo.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 190, 110, -1));
        pnlArchivo.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 190, 80, -1));
        pnlArchivo.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 360, 100, -1));
        pnlArchivo.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 360, 140, -1));
        pnlArchivo.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 360, 100, -1));
        pnlArchivo.add(jSeparator8, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 360, 100, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlArchivo, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlArchivo, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFinalizarSesionMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFinalizarSesionMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnFinalizarSesionMouseEntered

    private void btnFinalizarSesionMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFinalizarSesionMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnFinalizarSesionMouseExited

    private void btnUsuarioMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUsuarioMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnUsuarioMouseEntered

    private void btnUsuarioMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnUsuarioMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnUsuarioMouseExited

    private void btnAccesoFuncionesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAccesoFuncionesMouseEntered
        jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnAccesoFuncionesMouseEntered

    private void btnAccesoFuncionesMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAccesoFuncionesMouseExited
        jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnAccesoFuncionesMouseExited

    private void btnCamaraMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCamaraMouseEntered
        jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnCamaraMouseEntered

    private void btnCamaraMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCamaraMouseExited
        jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnCamaraMouseExited

    private void btnFoliadorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFoliadorMouseEntered
        jSeparator5.setVisible(true);
    }//GEN-LAST:event_btnFoliadorMouseEntered

    private void btnFoliadorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFoliadorMouseExited
        jSeparator5.setVisible(false);
    }//GEN-LAST:event_btnFoliadorMouseExited

    private void btnParametrosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnParametrosMouseEntered
        jSeparator6.setVisible(true);
    }//GEN-LAST:event_btnParametrosMouseEntered

    private void btnParametrosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnParametrosMouseExited
        jSeparator6.setVisible(false);
    }//GEN-LAST:event_btnParametrosMouseExited

    private void btnIndexarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnIndexarMouseEntered
        jSeparator7.setVisible(true);
    }//GEN-LAST:event_btnIndexarMouseEntered

    private void btnIndexarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnIndexarMouseExited
        jSeparator7.setVisible(false);
    }//GEN-LAST:event_btnIndexarMouseExited

    private void btnLicenciaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLicenciaMouseEntered
        jSeparator8.setVisible(true);
    }//GEN-LAST:event_btnLicenciaMouseEntered

    private void btnLicenciaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLicenciaMouseExited
        jSeparator8.setVisible(false);
    }//GEN-LAST:event_btnLicenciaMouseExited

    private void btnIndexarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIndexarActionPerformed
        botonIndexar();
    }//GEN-LAST:event_btnIndexarActionPerformed

    private void btnFinalizarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarSesionActionPerformed
        botonFinalizar();
    }//GEN-LAST:event_btnFinalizarSesionActionPerformed

    private void btnUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuarioActionPerformed
        botonUsuariosSistema();
    }//GEN-LAST:event_btnUsuarioActionPerformed

    private void btnAccesoFuncionesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAccesoFuncionesActionPerformed
        botonAccesoFunciones();
    }//GEN-LAST:event_btnAccesoFuncionesActionPerformed

    private void btnFoliadorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFoliadorActionPerformed
        botonFoliador();
    }//GEN-LAST:event_btnFoliadorActionPerformed

    private void btnCamaraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCamaraActionPerformed
        botonCamara();
    }//GEN-LAST:event_btnCamaraActionPerformed

    private void btnParametrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParametrosActionPerformed
        botonParametros();
    }//GEN-LAST:event_btnParametrosActionPerformed

    private void btnLicenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLicenciaActionPerformed
        botonLicencia();
    }//GEN-LAST:event_btnLicenciaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAccesoFunciones;
    private javax.swing.JButton btnCamara;
    private javax.swing.JButton btnFinalizarSesion;
    private javax.swing.JButton btnFoliador;
    private javax.swing.JButton btnIndexar;
    private javax.swing.JButton btnLicencia;
    private javax.swing.JButton btnParametros;
    private javax.swing.JButton btnUsuario;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JPanel pnlArchivo;
    // End of variables declaration//GEN-END:variables
}
