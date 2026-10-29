package vista.archivos;

import dao.LicenciaDAO;
import modelo.Licencia;
import java.text.SimpleDateFormat;

public class IFrmLicenciaUso extends javax.swing.JInternalFrame {

    private final LicenciaDAO licenciaDAO;
    private final SimpleDateFormat sdf;

    public IFrmLicenciaUso() {
        initComponents();
        colorBotones();
        licenciaDAO = new LicenciaDAO();
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        configurarCampos();
        cargarDatosLicencia();
        asignarEventos();
        quitarBordeBarra();
        configuracionVentana();
    }

    private void asignarEventos() {
        // Evento Botón Cerrar
        btnCerrar.addActionListener(e -> dispose());

        // Evento Botón Verificar: Valida la conexión y el estado de la licencia actual
        btnVerificar.addActionListener(e -> {
            Licencia lic = licenciaDAO.obtenerLicenciaActiva();
            if (lic != null) {
                String estado = lic.getEstado() != null ? lic.getEstado().toUpperCase() : "DESCONOCIDO";
                String razon = lic.getRazonSocial() != null ? lic.getRazonSocial() : "Sin Razón Social";

                utilidades.AlertaUtil.mostrarInformacion(this,
                        "Estado actual de la licencia: " + estado + "\n"
                        + "Cliente / Razón Social: " + razon + "\n"
                        + "¡La conexión con PostgreSQL y el registro son válidos!"
                );
            } else {
                utilidades.AlertaUtil.mostrarError(this,
                        "No se encontró ninguna licencia activa registrada en la base de datos."
                );
            }
        });

        // Evento Botón Ingresar Nueva Clave: Guarda la clave escrita en el campo Serial Key
        btnIngresarClave.addActionListener(e -> {
            String nuevaClave = txtSerieKey.getText().trim();

            if (nuevaClave.isEmpty()) {
                utilidades.AlertaUtil.mostrarAdvertencia(this,
                        "Por favor, ingrese un Serial Key válido en el campo correspondiente antes de guardar."
                );
                txtSerieKey.requestFocus();
                return;
            }

            boolean exito = licenciaDAO.actualizarClaveLicencia(nuevaClave);

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this,
                        "¡Clave de licencia / Renew Key actualizada correctamente en PostgreSQL!"
                );
                cargarDatosLicencia(); // Recarga la vista para refrescar los datos
            } else {
                utilidades.AlertaUtil.mostrarError(this,
                        "Ocurrió un error al intentar actualizar la clave en la base de datos."
                );
            }
        });
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

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
//        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
        aplicarEfectoHoverPlano(btnVerificar, azulSuave);
        aplicarEfectoHoverPlano(btnIngresarClave, naranjaSuave);
    }

    private void configuracionVentana() {
        this.setTitle("INFORMACIÓN DE LICENCIA Y REGISTRO DEL SISTEMA");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void configurarCampos() {
        txtClienteRazonSocial.setEditable(false);
        txtRutLicencia.setEditable(false);
        txtEstado.setEditable(false);
        txtTipoLicencia.setEditable(false);
        txtCajaNodosPermitidos.setEditable(false);
        txtEnUso.setEditable(false);
        txtFechaAct.setEditable(false);
        txtVence.setEditable(false);

        lblManSystem.setText("Management System v1.0");
    }

    private void cargarDatosLicencia() {
        Licencia lic = licenciaDAO.obtenerLicenciaActiva();

        if (lic != null) {
            txtClienteRazonSocial.setText(lic.getRazonSocial() != null ? lic.getRazonSocial() : "");
            txtRutLicencia.setText(lic.getRutLicenciatario() != null ? lic.getRutLicenciatario() : "");
            txtEstado.setText(lic.getEstado() != null ? lic.getEstado() : "");
            txtTipoLicencia.setText(lic.getTipoLicencia() != null ? lic.getTipoLicencia() : "");
            txtCajaNodosPermitidos.setText(String.valueOf(lic.getCajasNodosPermitidos()));
            txtEnUso.setText(String.valueOf(lic.getEnUso()));

            txtFechaAct.setText(lic.getFechaActivacion() != null ? sdf.format(lic.getFechaActivacion()) : "N/A");
            txtVence.setText(lic.getFechaVencimiento() != null ? sdf.format(lic.getFechaVencimiento()) : "N/A");

            // Lógica de enmascaramiento por seguridad para la clave de serie
            String serialReal = lic.getSerialKey();
            if (serialReal != null && !serialReal.trim().isEmpty()) {
                if (serialReal.length() > 4) {
                    // Muestra solo los últimos 4 caracteres y oculta el resto
                    String enmascarado = "****-****-****-" + serialReal.substring(serialReal.length() - 4);
                    txtSerieKey.setText(enmascarado);
                } else {
                    txtSerieKey.setText("****");
                }
            } else {
                txtSerieKey.setText("");
            }

        } else {
           utilidades.AlertaUtil.mostrarAdvertencia(this, "No se encontraron datos de licencia registrados en el sistema.");
        }
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtClienteRazonSocial = new javax.swing.JTextField();
        txtRutLicencia = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtEstado = new javax.swing.JTextField();
        txtTipoLicencia = new javax.swing.JTextField();
        txtCajaNodosPermitidos = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtEnUso = new javax.swing.JTextField();
        txtFechaAct = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtVence = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtSerieKey = new javax.swing.JTextField();
        btnIngresarClave = new javax.swing.JButton();
        btnVerificar = new javax.swing.JButton();
        lblManSystem = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS DE LA LICENCIA ACTIVADA"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Cliente / Razón Social:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        jLabel2.setText("R.U.T. Licenciatario:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(53, 70, -1, -1));

        jLabel3.setText("Tipo de Licencia:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 100, -1, -1));

        jLabel4.setText("Cajas / Nodos Permitidos:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, -1));

        jLabel5.setText("Fecha de Activación:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(48, 160, -1, -1));

        txtClienteRazonSocial.setEditable(false);
        jPanel2.add(txtClienteRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 38, 550, -1));

        txtRutLicencia.setEditable(false);
        jPanel2.add(txtRutLicencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, 250, -1));

        jLabel6.setText("Estado:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 70, -1, -1));

        txtEstado.setEditable(false);
        jPanel2.add(txtEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 70, 240, -1));

        txtTipoLicencia.setEditable(false);
        jPanel2.add(txtTipoLicencia, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 100, 550, -1));

        txtCajaNodosPermitidos.setEditable(false);
        jPanel2.add(txtCajaNodosPermitidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 130, 250, -1));

        jLabel7.setText("En Uso:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 130, -1, -1));

        txtEnUso.setEditable(false);
        jPanel2.add(txtEnUso, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 130, 240, -1));

        txtFechaAct.setEditable(false);
        jPanel2.add(txtFechaAct, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 160, 250, -1));

        jLabel8.setText("Vence:");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(435, 160, -1, -1));

        txtVence.setEditable(false);
        jPanel2.add(txtVence, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 160, 240, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 200));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("CLAVE DE ACTIVACIÓN / RENEW KEY"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setText("Serial Key:");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 35, -1, -1));
        jPanel3.add(txtSerieKey, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 630, -1));

        btnIngresarClave.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnIngresarClave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/llave.png"))); // NOI18N
        btnIngresarClave.setText("INGRESAR NUEVA CLAVE");
        btnIngresarClave.setBorderPainted(false);
        btnIngresarClave.setContentAreaFilled(false);
        btnIngresarClave.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIngresarClave.setFocusPainted(false);
        btnIngresarClave.addActionListener(this::btnIngresarClaveActionPerformed);
        jPanel3.add(btnIngresarClave, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 70, -1, -1));

        btnVerificar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVerificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/verificado.png"))); // NOI18N
        btnVerificar.setText("VERIFICAR");
        btnVerificar.setBorderPainted(false);
        btnVerificar.setContentAreaFilled(false);
        btnVerificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVerificar.setFocusPainted(false);
        btnVerificar.addActionListener(this::btnVerificarActionPerformed);
        jPanel3.add(btnVerificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 70, -1, -1));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 240, 740, 120));

        lblManSystem.setText(" Management System v1.0");
        pnlPrincipal.add(lblManSystem, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 450, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("INFORMACIÓN DE LICENCIA Y REGISTRO DEL SISTEMA");
        pnlPrincipal.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerificarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnVerificarActionPerformed

    private void btnIngresarClaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIngresarClaveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnIngresarClaveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnIngresarClave;
    private javax.swing.JButton btnVerificar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblManSystem;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtCajaNodosPermitidos;
    private javax.swing.JTextField txtClienteRazonSocial;
    private javax.swing.JTextField txtEnUso;
    private javax.swing.JTextField txtEstado;
    private javax.swing.JTextField txtFechaAct;
    private javax.swing.JTextField txtRutLicencia;
    private javax.swing.JTextField txtSerieKey;
    private javax.swing.JTextField txtTipoLicencia;
    private javax.swing.JTextField txtVence;
    // End of variables declaration//GEN-END:variables
}
