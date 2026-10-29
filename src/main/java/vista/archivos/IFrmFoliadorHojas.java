package vista.archivos;

import dao.FoliadorDAO;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class IFrmFoliadorHojas extends javax.swing.JInternalFrame {

    private final FoliadorDAO dao = new FoliadorDAO();
    private boolean esEdicion = false;
    private String serieOriginalEdicion = "";

    public IFrmFoliadorHojas() {
        initComponents();
        inicializarVista();
        colorBotones();
        quitarBordesBarra();
        configuaracionVentana();
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

    private void botonNuevo() {
        limpiarCampos();
        esEdicion = false;
        serieOriginalEdicion = "";
    }

    private void botonGuardar() {
        String tipoDoc = cmbTipoDocumento.getSelectedItem() != null ? cmbTipoDocumento.getSelectedItem().toString() : "";
        String serie = txtSerieImprenta.getText().trim();
        String iniStr = txtFolioInicial.getText().trim();
        String finStr = txtFolioFinal.getText().trim();
        String actStr = txtFolioActual.getText().trim();
        String estado = cmbEstado.getSelectedItem() != null ? cmbEstado.getSelectedItem().toString() : "Activo";

        if (serie.isEmpty() || iniStr.isEmpty() || finStr.isEmpty() || actStr.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor complete todos los campos obligatorios.");
            return;
        }

        try {
            int ini = Integer.parseInt(iniStr);
            int fin = Integer.parseInt(finStr);
            int act = Integer.parseInt(actStr);

            if (ini > fin) {
                utilidades.AlertaUtil.mostrarError(this, "El folio inicial no puede ser mayor al folio final.");
                return;
            }

            boolean exito;
            if (esEdicion) {
                // Si estamos editando, hacemos UPDATE
                exito = dao.modificarRango(tipoDoc, serie, ini, fin, act, estado, serieOriginalEdicion);
            } else {
                // Si es un registro nuevo, hacemos INSERT
                exito = dao.guardarRango(tipoDoc, serie, ini, fin, act, estado);
            }

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Rango modificado con éxito.");
                cargarTabla();
                limpiarCampos();
                esEdicion = false; // Reseteamos bandera
                serieOriginalEdicion = "";
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al guardar los cambios en la base de datos.");
            }

        } catch (NumberFormatException e) {
            utilidades.AlertaUtil.mostrarError(this, "Los folios deben ser valores numéricos enteros.");
        }
    }

    private void botonEditar() {
        int fila = tblRango.getSelectedRow();
        if (fila == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un rango de la tabla para editar.");
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) tblRango.getModel();
        cmbTipoDocumento.setSelectedItem(modelo.getValueAt(fila, 0).toString());
        txtSerieImprenta.setText(modelo.getValueAt(fila, 1).toString());
        txtFolioInicial.setText(modelo.getValueAt(fila, 2).toString());
        txtFolioFinal.setText(modelo.getValueAt(fila, 3).toString());
        txtFolioActual.setText(modelo.getValueAt(fila, 4).toString());
        cmbEstado.setSelectedItem(modelo.getValueAt(fila, 5).toString());

        // Activamos el modo edición
        esEdicion = true;
        serieOriginalEdicion = modelo.getValueAt(fila, 1).toString();
    }

    private void botonAnularRango() {
        int fila = tblRango.getSelectedRow();
        if (fila == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un rango de la tabla para anular.");
            return;
        }

        // Pedir confirmación usando el booleano de AlertaUtil
        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(this, "¿Está seguro de anular este rango de folios?");

        if (confirmar) {
            DefaultTableModel modelo = (DefaultTableModel) tblRango.getModel();

            // Extraemos el tipo de documento y la serie de la fila seleccionada
            String tipoDoc = modelo.getValueAt(fila, 0).toString();
            String serie = modelo.getValueAt(fila, 1).toString();

            // Llamamos al DAO para actualizar directamente en la BD
            boolean exito = dao.actualizarEstadoRango(tipoDoc, serie, "Anulado");

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Rango anulado correctamente en la base de datos.");
                cargarTabla(); // Recargamos la tabla desde la BD para que se refleje fresco
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al anular el rango en la base de datos.");
            }
        }
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void botonExcel() {
        utilidades.ExcelUtil.exportarTablaAExcel(tblRango, "Reorte_Foliador_hojas");
    }

    private void botonImprimir() {
        utilidades.ReporteUtil.generarPDFFoliadorHojas(tblRango, lblTotalRango);
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);
//        java.awt.Color verdeExcel = new java.awt.Color(51, 204, 0);

        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnAnularRango, rojoSuave);
        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
        aplicarEfectoHoverPlano(btnNuevoRango, grisSuave);

    }

    private void configuaracionVentana() {
        this.setTitle("CONTROL Y REGISTRO DE FOLIADOR DE HOJAS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void configurarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Tipo Documento", "Serie / Imprenta", "Folio Inicial", "Folio Final", "Folio Actual", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };
        tblRango.setModel(modelo);
    }

    private void cargarCombos() {
        cmbTipoDocumento.removeAll();
        cmbEstado.removeAllItems();

        List<String> tipos = dao.obtenerTiposDocumento();
        for (String t : tipos) {
            cmbTipoDocumento.addItem(t);
        }

        List<String> estados = dao.obtenerEstadosFolio();
        for (String e : estados) {
            cmbEstado.addItem(e);
        }
    }

    private void cargarTabla() {
        DefaultTableModel modelo = (DefaultTableModel) tblRango.getModel();
        modelo.setRowCount(0);

        List<Object[]> datos = dao.obtenerRangosFolios();
        for (Object[] fila : datos) {
            modelo.addRow(fila);
        }

        //Actualiza la etiqueta del contador total
        if (lblTotalRango != null) {
            lblTotalRango.setText(String.valueOf(modelo.getRowCount()));
        }
    }

    private void inicializarVista() {
        configurarTabla();
        cargarCombos();
        cargarTabla();
        limpiarCampos();
    }

    private void limpiarCampos() {
        if (cmbTipoDocumento.getItemCount() > 0) {
            cmbTipoDocumento.setSelectedIndex(0);
        }
        if (cmbEstado.getItemCount() > 0) {
            cmbEstado.setSelectedIndex(0);
        }
        txtSerieImprenta.setText("");
        txtFolioInicial.setText("");
        txtFolioFinal.setText("");
        txtFolioActual.setText("");
        txtSerieImprenta.requestFocus();
    }

    private void quitarBordesBarra() {
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cmbTipoDocumento = new javax.swing.JComboBox<>();
        txtSerieImprenta = new javax.swing.JTextField();
        txtFolioInicial = new javax.swing.JTextField();
        txtFolioActual = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtFolioFinal = new javax.swing.JTextField();
        cmbEstado = new javax.swing.JComboBox<>();
        btnNuevoRango = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnAnularRango = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRango = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        lblTotalRango = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CONFIGURACIÓN Y ASIGNACIÓN DE FOLIOS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Tipo Documento:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel2.setText("Serie / Imprenta:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 60, -1, -1));

        jLabel3.setText("Folio Inicial:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 90, -1, -1));

        jLabel4.setText("Folio Actual:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(48, 120, -1, -1));

        jPanel1.add(cmbTipoDocumento, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 30, 590, -1));
        jPanel1.add(txtSerieImprenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 60, 590, -1));
        jPanel1.add(txtFolioInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 90, 240, -1));
        jPanel1.add(txtFolioActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 120, 240, -1));

        jLabel5.setText("Folio Final:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 90, -1, -1));

        jLabel6.setText("Estado:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 120, -1, -1));
        jPanel1.add(txtFolioFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(472, 90, 240, -1));

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(472, 120, 240, -1));

        btnNuevoRango.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevoRango.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnNuevoRango.setText("NUEVO RANGO");
        btnNuevoRango.setBorderPainted(false);
        btnNuevoRango.setContentAreaFilled(false);
        btnNuevoRango.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevoRango.setFocusPainted(false);
        btnNuevoRango.addActionListener(this::btnNuevoRangoActionPerformed);
        jPanel1.add(btnNuevoRango, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 155, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 155, -1, -1));

        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnEditar.setText("EDITAR");
        btnEditar.setBorderPainted(false);
        btnEditar.setContentAreaFilled(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(395, 155, -1, -1));

        btnAnularRango.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAnularRango.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnAnularRango.setText("ANULAR RANGO");
        btnAnularRango.setBorderPainted(false);
        btnAnularRango.setContentAreaFilled(false);
        btnAnularRango.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAnularRango.setFocusPainted(false);
        btnAnularRango.addActionListener(this::btnAnularRangoActionPerformed);
        jPanel1.add(btnAnularRango, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 155, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 35, 740, 200));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("RANGOS DE FOLIOS AUTORIZADOS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblRango.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblRango.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblRango);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 190));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 240, 740, 220));

        jLabel7.setText("Total Rangos Registrados:");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 480, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 470, -1, -1));

        lblTotalRango.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalRango.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalRango.setText("0");
        pnlPrincipal.add(lblTotalRango, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 480, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("CONTROL Y REGISTRO DE FOLIADOR DE HOJAS");
        pnlPrincipal.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 8, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 470, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 470, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoRangoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoRangoActionPerformed
        botonNuevo();
    }//GEN-LAST:event_btnNuevoRangoActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnAnularRangoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnularRangoActionPerformed
        botonAnularRango();
    }//GEN-LAST:event_btnAnularRangoActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnularRango;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnNuevoRango;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbTipoDocumento;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalRango;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblRango;
    private javax.swing.JTextField txtFolioActual;
    private javax.swing.JTextField txtFolioFinal;
    private javax.swing.JTextField txtFolioInicial;
    private javax.swing.JTextField txtSerieImprenta;
    // End of variables declaration//GEN-END:variables
}
