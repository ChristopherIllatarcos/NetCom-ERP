package vista.ventas;

import dao.VentaDAO;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import utilidades.TablaUtil;
import utilidades.VentaUtil;

public class IFrmResumenVentas extends javax.swing.JInternalFrame {

    private final VentaDAO ventaDAO = new VentaDAO();

    public IFrmResumenVentas() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configuracionFechaEventoEnter();
        configurarVentana();
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
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnConsultar, azulSuave);
        aplicarEfectoHoverPlano(btnImprimir, grisSuave);
    }

    private void configurarVentana() {
        this.setTitle("RESUMEN DE VENTAS POR PERÍODO");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void configuracionFechaEventoEnter() {
        // 1. Cargar fechas por defecto (hoy)
        jDateChooserInicial.setDate(new Date());
        jDateChooserFinal.setDate(new Date());

        // 2. Delegar eventos ENTER al botón de consulta
        TablaUtil.configurarEventosEnterBusqueda(
                this.getRootPane(),
                btnConsultar,
                jDateChooserInicial,
                jDateChooserFinal,
                e -> ejecutarConsulta()
        );
    }

    public void ejecutarConsulta() {
        Date fechaInicio = jDateChooserInicial.getDate();
        Date fechaFin = jDateChooserFinal.getDate();

        // 1. Validar que las fechas no estén vacías
        if (fechaInicio == null || fechaFin == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar un rango de fechas válido.");
            return;
        }

        // 2. Validar coherencia de fechas
        if (fechaInicio.after(fechaFin)) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        // 3. Consultar a la BD
        List<Object[]> resumen = ventaDAO.obtenerResumenVentasPorPeriodo(fechaInicio, fechaFin);
        DefaultTableModel model = (DefaultTableModel) tblConsolidado.getModel();
        model.setRowCount(0);

        // 4. Validar si hay resultados
        if (resumen.isEmpty()) {
            utilidades.AlertaUtil.mostrarInformacion(this, "No existen ventas registradas en el período seleccionado.");
            VentaUtil.actualizarTotales(model, lblTotalDoc, lblTotalNeto, lblTotalGeneral);
            return;
        }

        // 5. Llenar tabla formateando valores
        for (Object[] fila : resumen) {
            model.addRow(new Object[]{
                fila[0], // Tipo Documento
                fila[1], // Cantidad
                VentaUtil.formatearMoneda((Double) fila[2]), // Neto
                VentaUtil.formatearMoneda((Double) fila[3]) // Total
            });
        }

        // 6. Actualizar automáticamente los Totales del pie de página
        VentaUtil.actualizarTotales(model, lblTotalDoc, lblTotalNeto, lblTotalGeneral);
    }

    public void exportarAExcel() {
        utilidades.ExcelUtil.exportarTablaAExcel(tblConsolidado, "Resumen_Ventas_Periodo");
    }

    public void exportarImprimir() {
        utilidades.ReporteUtil.generarPDFResumenVentas(tblConsolidado, jDateChooserInicial, jDateChooserFinal, lblTotalDoc, lblTotalNeto, lblTotalGeneral);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jDateChooserInicial = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserFinal = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblConsolidado = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblTotalGeneral = new javax.swing.JLabel();
        lblTotalDoc = new javax.swing.JLabel();
        lblTotalNeto = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("RANGO DE FECHAS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Fecha Inicial:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));
        jPanel1.add(jDateChooserInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 240, -1));

        jLabel2.setText("Fecha Final:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(395, 40, -1, -1));
        jPanel1.add(jDateChooserFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 40, 240, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnConsultarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnConsultarMouseExited(evt);
            }
        });
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 70, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 730, 120));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("CONSOLIDADO POR TIPO DE DOCUMENTO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblConsolidado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Tipo Documento", "Cant. Emitida", "Monto Neto ($)", "Total Venta ($"
            }
        ));
        tblConsolidado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblConsolidado);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 17, 706, 230));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 728, 259));

        jLabel3.setText("Total Doctos:");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 440, -1, -1));

        jLabel4.setText("Total Neto:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 440, -1, -1));

        jLabel5.setText("TOTAL GENERAL:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 440, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExcelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExcelMouseExited(evt);
            }
        });
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 470, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnImprimirMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnImprimirMouseExited(evt);
            }
        });
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 470, -1, -1));

        lblTotalGeneral.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalGeneral.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalGeneral.setText("$");
        pnlPrincipal.add(lblTotalGeneral, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 440, -1, -1));

        lblTotalDoc.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalDoc.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalDoc.setText("$");
        pnlPrincipal.add(lblTotalDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 440, -1, -1));

        lblTotalNeto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalNeto.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalNeto.setText("$");
        pnlPrincipal.add(lblTotalNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 440, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("RESUMEN DE VENTAS POR PERÍODO");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
        // jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
        //jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered
        // jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
        //  jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnImprimirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseEntered
        // jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnImprimirMouseEntered

    private void btnImprimirMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseExited
        //   jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnImprimirMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        ejecutarConsulta();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarAExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        exportarImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private com.toedter.calendar.JDateChooser jDateChooserFinal;
    private com.toedter.calendar.JDateChooser jDateChooserInicial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalDoc;
    private javax.swing.JLabel lblTotalGeneral;
    private javax.swing.JLabel lblTotalNeto;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblConsolidado;
    // End of variables declaration//GEN-END:variables

}
