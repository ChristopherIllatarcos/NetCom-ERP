package vista.bodegas;

/**
 *
 * @author caill
 */
public class IFrmVentasPorFamilia extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameVentasPorFamilia
     */
    public IFrmVentasPorFamilia() {
        initComponents();
        inicializarTablaVentasFamilia();
        cargarComboFamiliasVentas();
        colorBotones();
        quitarBordeBarra();
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
        //java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configurarVentana() {
        this.setTitle("INFORME DE VENTAS POR FAMILIA");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void cargarComboFamiliasVentas() {
        cmbFamilia.removeAllItems();
        cmbFamilia.addItem("TODAS LAS FAMILIAS");

        dao.FamiliasDAO daoFamilias = new dao.FamiliasDAO();
        java.util.List<modelo.FamiliaDTO> listaFamilias = daoFamilias.listarFamilias();

        for (modelo.FamiliaDTO familia : listaFamilias) {
            cmbFamilia.addItem(familia.getNombre());
        }
    }

    private void consultarVentasPorFamilia() {
        String familia = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS LAS FAMILIAS";
        java.util.Date fechaDesde = jdcDesde.getDate();
        java.util.Date fechaHasta = jdcHasta.getDate();

        // Instanciamos el DAO
        dao.BodegasDAO bodegaDao = new dao.BodegasDAO();
        java.util.List<Object[]> datos = bodegaDao.obtenerVentasPorFamilia(familia, fechaDesde, fechaHasta);

        // Limpiamos y poblamos la tabla tblDetalle
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0);

        int totalCategorias = datos.size();
        double montoTotalGeneral = 0.0;

        for (Object[] fila : datos) {
            modelo.addRow(fila);
            if (fila[3] != null) {
                montoTotalGeneral += Double.parseDouble(fila[3].toString());
            }
        }

        // Actualizamos los labels inferiores (lblTotalCate y lblMonto)
        if (lblTotalCate != null) {
            lblTotalCate.setText(String.valueOf(totalCategorias));
        }
        if (lblMonto != null) {
            lblMonto.setText(String.format("$%.2f", montoTotalGeneral));
        }

        if (datos.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No se encontraron registros de ventas por familia para el período seleccionado.");
        }
    }

    private void exportarExcelVentasPorFamilia() {
        // Llamamos directamente a tu clase utilidades.ExcelUtil existente
        utilidades.ExcelUtil.exportarTablaAExcel(tblDetalle, "Ventas_Por_Familia");
    }

    private void generarPdfVentasPorFamilia() {
        String familiaSeleccionada = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS LAS FAMILIAS";

        utilidades.ReporteUtil.generarPDFVentasPorFamilia(
                tblDetalle,
                jdcDesde,
                jdcHasta,
                familiaSeleccionada,
                lblTotalCate,
                lblMonto
        );
    }

    private void inicializarTablaVentasFamilia() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Cód. Fam.", "Nombre Familia", "Cant. Vendida", "Monto Total ($)"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        tblDetalle.setModel(modelo);

        // Ajustar anchos recomendados para las columnas
        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(90);  // Cód. Fam.
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(250); // Nombre Familia
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(100); // Cant. Vendida
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(120); // Monto Total ($)
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
        pnlFiltro = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jdcDesde = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jdcHasta = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        cmbFamilia = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        lblTotalCate = new javax.swing.JLabel();
        lblMonto = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFiltro.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltro.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        pnlFiltro.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Desde:");
        pnlFiltro.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, -1, -1));
        pnlFiltro.add(jdcDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(64, 24, 300, -1));

        jLabel2.setText("Hasta:");
        pnlFiltro.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 25, -1, -1));
        pnlFiltro.add(jdcHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 25, 300, -1));

        jLabel3.setText("Familia:");
        pnlFiltro.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 70, -1, -1));

        cmbFamilia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pnlFiltro.add(cmbFamilia, new org.netbeans.lib.awtextra.AbsoluteConstraints(64, 67, 530, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
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
        pnlFiltro.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 60, -1, -1));

        pnlPrincipal.add(pnlFiltro, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 110));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE VENTAS POR FAMILIA DE PRODUCTOS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Cód. Fam.", "Nombre Familia", "Cant. Vendida", "Mto. Total ($)"
            }
        ));
        jScrollPane1.setViewportView(tblDetalle);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, 720, 260));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 160, 740, 290));

        jLabel4.setText("Total Categorías:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, -1, -1));

        jLabel5.setText("Monto Total:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 470, -1, -1));

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
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 460, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("INFORME DE VENTAS POR FAMILIA");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 12, -1, -1));

        lblTotalCate.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCate.setText("0");
        pnlPrincipal.add(lblTotalCate, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 470, -1, -1));

        lblMonto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMonto.setText("0");
        pnlPrincipal.add(lblMonto, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 470, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 460, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
        //jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
        // jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered
        //jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
        // jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        consultarVentasPorFamilia();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcelVentasPorFamilia();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        generarPdfVentasPorFamilia();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JComboBox<String> cmbFamilia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jdcDesde;
    private com.toedter.calendar.JDateChooser jdcHasta;
    private javax.swing.JLabel lblMonto;
    private javax.swing.JLabel lblTotalCate;
    private javax.swing.JPanel pnlFiltro;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDetalle;
    // End of variables declaration//GEN-END:variables
}
