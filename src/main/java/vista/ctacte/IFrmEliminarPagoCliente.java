package vista.ctacte;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author caill
 */
public class IFrmEliminarPagoCliente extends javax.swing.JInternalFrame {

    private int clienteIdActual = -1;

    public IFrmEliminarPagoCliente() {
        initComponents();
        colorBotones();
        configuracionTabla();
        quitarBordeBarra();
        configuracionVentana();
        asignarEventoBtnConsultar();
        asignarEventos();
    }

    private void asignarEventoBtnConsultar() {
        java.awt.event.ActionListener accionEnter = (ActionEvent e) -> {

            btnConsultarPago.doClick();
        };

        txtRutCliente.addActionListener(accionEnter);
        txtNombre.addActionListener(accionEnter);
    }

    private void asignarEventos() {
        txtRutCliente.addActionListener((ActionEvent e) -> {
            btnBuscar.doClick();
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

    private void botonBuscar() {
        String rut = txtRutCliente.getText().trim();
        if (rut.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, ingrese un R.U.T. para buscar.");
            return;
        }

        try {

            dao.ClienteDAO daoCliente = new dao.ClienteDAO();
            modelo.Cliente cliente = daoCliente.obtenerClientePorRut(rut);

            if (cliente != null) {
                clienteIdActual = cliente.getId();
                txtNombre.setText(cliente.getNombre());
            } else {
                clienteIdActual = -1;
                txtNombre.setText("");
                utilidades.AlertaUtil.mostrarError(this, "No se encontró ningún cliente con ese R.U.T.");
            }
        } catch (HeadlessException e) {
            System.err.println("Error al buscar cliente: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Ocurrió un error al buscar el cliente.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void botonConsultar() {
        if (clienteIdActual == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Primero debe buscar y seleccionar un cliente válido.");
            return;
        }

        cargarHistorialPagos(clienteIdActual);
    }

    private void botonEliminar() {
        int filaSeleccionada = tblHistorial.getSelectedRow();
        if (filaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar de la tabla el pago o abono que desea anular/eliminar.");
            return;
        }

        String motivo = txtMotivo.getText().trim();
        if (motivo.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar obligatoriamente un motivo para la anulación.");
            txtMotivo.requestFocus();
            return;
        }

        Object reciboObj = tblHistorial.getValueAt(filaSeleccionada, 0);
        String numeroRecibo = reciboObj != null ? reciboObj.toString() : "";

        int confirmacion = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de anular el recibo N° " + numeroRecibo + "?\nEsta acción revertirá los montos en la cuenta corriente.",
                "Confirmar Anulación",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == javax.swing.JOptionPane.YES_OPTION) {
            try {
                dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
                boolean exito = daoCc.anularPago(numeroRecibo, motivo);

                if (exito) {
                    utilidades.AlertaUtil.mostrarExito(this, "¡Pago anulado exitosamente!");
                    txtMotivo.setText("");
                    cargarHistorialPagos(clienteIdActual); // Refrescar la tabla
                } else {
                    utilidades.AlertaUtil.mostrarError(this, "No se pudo procesar la anulación del pago.");
                }

            } catch (java.awt.HeadlessException e) {
                System.err.println("Error al eliminar pago: " + e.getMessage());
                utilidades.AlertaUtil.mostrarError(this, "Error de conexión o base de datos al anular el pago.");
            }
        }
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void configuracionTabla() {

        tblHistorial.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Object estadoObj = table.getValueAt(row, 5); // Columna Estado
                String estado = estadoObj != null ? estadoObj.toString() : "";

                if (!isSelected) {
                    c.setBackground(table.getBackground()); // Fondo normal
                    if ("ANULADO".equalsIgnoreCase(estado)) {
                        c.setForeground(new java.awt.Color(200, 0, 0)); // Letras rojas
                    } else {
                        c.setForeground(new java.awt.Color(0, 120, 0)); // Letras verdes
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }

                return c;
            }
        });
    }

    private void colorBotones() {
        // java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
//        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnEliminarPago, rojoSuave);
        aplicarEfectoHoverPlano(btnCerrar, oscuroSuave);
        aplicarEfectoHoverPlano(btnConsultarPago, grisSuave);
    }

    private void configuracionVentana() {
        this.setTitle("ELIMINAR / ANULAR PAGO DE CLIENTE");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarHistorialPagos(int clienteId) {
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tblHistorial.getModel();
        modeloTabla.setRowCount(0); // Limpiar la tabla

        try {
            dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
            java.util.List<modelo.PagoCliente> listaPagos = daoCc.obtenerPagosPorCliente(clienteId);

            verificarYRespaldarLimite(listaPagos, clienteId);

            if (listaPagos.size() >= 100) {
                String mensaje = """
                                 \u26a0\ufe0f \u00a1Atenci\u00f3n! La tabla ha alcanzado o superado el l\u00edmite de 100 registros.
                                 Debe exportar los datos a Excel antes de limpiar y continuar.""";

                utilidades.AlertaUtil.mostrarAdvertencia(this, mensaje);

                return; // Detenemos la carga para forzar el respaldo
            }

            for (modelo.PagoCliente p : listaPagos) {
                Object[] fila = {
                    p.getNumeroRecibo(),
                    p.getFecha(),
                    p.getMedioPago(),
                    p.getDocumentoAsociado(),
                    p.getMontoPagado(),
                    p.getEstado(),
                    p.getMotivo() != null ? p.getMotivo() : ""
                };
                modeloTabla.addRow(fila);
            }

            lblRecibidos.setText("Recibos Encontrados: " + listaPagos.size());

            if (listaPagos.isEmpty()) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "El cliente no registra pagos o abonos activos.");
            }

            // ... (aquí mantienes abajo tu DefaultTableCellRenderer para los colores de rojo/verde) ...
        } catch (HeadlessException | FileNotFoundException | UnsupportedEncodingException e) {
            System.err.println("Error al consultar pagos: " + e.getMessage());
            javax.swing.JOptionPane.showMessageDialog(this, "Error al cargar el historial de pagos.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        // 1. Reiniciar la variable global del ID del cliente actual
        // (Asegúrate de tener declarada una variable tipo 'private int clienteIdActual = -1;' en tu clase)
        clienteIdActual = -1;

        // 2. Limpiar los campos de texto
        txtRutCliente.setText("");
        txtNombre.setText("");
        txtMotivo.setText("");

        // 3. Reiniciar etiquetas o contadores
        lblRecibidos.setText("Recibos Encontrados: 0");

        // 4. Vaciar por completo el modelo de la tabla (tblHistorial)
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tblHistorial.getModel();
        modeloTabla.setRowCount(0);

        // 5. Devolver el foco al campo RUT para agilizar la siguiente búsqueda
        txtRutCliente.requestFocus();
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void verificarYRespaldarLimite(java.util.List<modelo.PagoCliente> listaPagos, int clienteId) throws UnsupportedEncodingException, FileNotFoundException {
        if (listaPagos.size() >= 100) {
            String mensajeAviso = """
                                  La tabla ha alcanzado los 100 registros.
                                  El sistema realizar\u00e1 un respaldo autom\u00e1tico a Excel y limpiar\u00e1 la vista.""";

            utilidades.AlertaUtil.mostrarInformacion(this, mensajeAviso);

            try {

                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String rutaArchivo = System.getProperty("user.home") + "/Desktop/Respaldo_Pagos_" + timestamp + ".csv";

                java.io.File archivo = new java.io.File(rutaArchivo);
                try (java.io.PrintWriter pw = new java.io.PrintWriter(archivo, "UTF-8")) {
                    // Escribir cabeceras
                    pw.println("N° Recibo;Fecha;Medio Pago;Documento;Monto;Estado;Motivo");

                    // Escribir los registros
                    for (modelo.PagoCliente p : listaPagos) {
                        pw.println(p.getNumeroRecibo() + ";"
                                + p.getFecha() + ";"
                                + p.getMedioPago() + ";"
                                + p.getDocumentoAsociado() + ";"
                                + p.getMontoPagado() + ";"
                                + p.getEstado() + ";"
                                + (p.getMotivo() != null ? p.getMotivo() : ""));
                    }
                }

                String mensajeExito = "Respaldo automático guardado exitosamente en el Escritorio:\nRespaldo_Pagos_" + timestamp + ".csv";

                utilidades.AlertaUtil.mostrarInformacion(this, mensajeExito);

                // 2. Limpiar formulario o la grilla para reiniciar el ciclo
                limpiarFormulario();

            } catch (HeadlessException e) {
                System.err.println("Error al exportar automáticamente: " + e.getMessage());

                String mensajeError = "No se pudo generar el respaldo automático: " + e.getMessage();

                utilidades.AlertaUtil.mostrarError(this, mensajeError);
            }
        }
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
        txtRutCliente = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        btnConsultarPago = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtMotivo = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        btnEliminarPago = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        lblRecibidos = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA DE CLIENTE"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("R.U.T. Cliente:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, -1, -1));

        txtRutCliente.addActionListener(this::txtRutClienteActionPerformed);
        txtRutCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRutClienteKeyPressed(evt);
            }
        });
        jPanel2.add(txtRutCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 250, -1));

        jLabel2.setText("Nombre:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, -1, -1));
        jPanel2.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(458, 30, 250, -1));

        btnConsultarPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultarPago.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnConsultarPago.setText("CONSULTAR PAGOS");
        btnConsultarPago.setBorderPainted(false);
        btnConsultarPago.setContentAreaFilled(false);
        btnConsultarPago.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultarPago.setFocusPainted(false);
        btnConsultarPago.addActionListener(this::btnConsultarPagoActionPerformed);
        jPanel2.add(btnConsultarPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(535, 65, -1, -1));

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel2.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 25, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 730, 112));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("HISTORIAL DE PAGOS / ABONOS DEL CLIENTE"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "N° Recibo", "Fecha", "Medio Pago", "Docto", "Monto Pagado ($)", "Estado", "Motivo"
            }
        ));
        tblHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblHistorial);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 23, 710, 160));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 730, 195));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("MOTIVO DE LA ELIMINACIÓN / ANULACIÓN"));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("Motivo:");
        jPanel4.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));
        jPanel4.add(txtMotivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 30, 630, -1));

        pnlPrincipal.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 730, 80));

        jLabel4.setText("Recibos Encontrados:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 475, -1, -1));

        btnEliminarPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminarPago.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnEliminarPago.setText("ELIMINAR PAGO");
        btnEliminarPago.setBorderPainted(false);
        btnEliminarPago.setContentAreaFilled(false);
        btnEliminarPago.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarPago.setFocusPainted(false);
        btnEliminarPago.addActionListener(this::btnEliminarPagoActionPerformed);
        pnlPrincipal.add(btnEliminarPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 470, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(645, 470, -1, -1));

        lblRecibidos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRecibidos.setForeground(new java.awt.Color(0, 102, 204));
        lblRecibidos.setText("0");
        pnlPrincipal.add(lblRecibidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 475, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("ELIMINAR / ANULAR PAGO DE CLIENTE");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 25, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -20, 770, 580));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnConsultarPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarPagoActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarPagoActionPerformed

    private void btnEliminarPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPagoActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarPagoActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void txtRutClienteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRutClienteKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            btnBuscarActionPerformed(null);
        }
    }//GEN-LAST:event_txtRutClienteKeyPressed

    private void txtRutClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRutClienteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRutClienteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnConsultarPago;
    private javax.swing.JButton btnEliminarPago;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblRecibidos;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtMotivo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRutCliente;
    // End of variables declaration//GEN-END:variables
}
