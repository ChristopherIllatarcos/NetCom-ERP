package vista.bodegas;

public class IFrmRotacionVentas extends javax.swing.JInternalFrame {

    public IFrmRotacionVentas() {
        initComponents();
        colorBotones();
        inicializarTablaRotacion();
        inicializarFiltrosRadioRotacion();
        cargarComboFamiliasRotacion();
        ConfiguracionVentana();
        quitarBordeBarra();
        fechaPorDefecto();

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
//        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
//        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnGenerar, naranjaSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);

    }

    private void ConfiguracionVentana() {
        this.setTitle("INFORME / ROTACIÓN DE VENTAS Y PRODUCTOS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarComboFamiliasRotacion() {
        cmbFamilia.removeAllItems();
        cmbFamilia.addItem("TODAS LAS FAMILIAS");

        dao.FamiliasDAO daoFamilias = new dao.FamiliasDAO();
        java.util.List<modelo.FamiliaDTO> listaFamilias = daoFamilias.listarFamilias();

        for (modelo.FamiliaDTO familia : listaFamilias) {
            cmbFamilia.addItem(familia.getNombre());
        }
    }

    private void exportarExcelRotacion() {
        javax.swing.table.TableModel modelo = tblDetalle.getModel();
        if (modelo.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay datos en la tabla para exportar.");
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Rotación de Ventas como Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("Rotacion_Ventas_" + System.currentTimeMillis() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoToSave = fileChooser.getSelectedFile();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivoToSave), "UTF-8"))) {
                // Escribir cabeceras
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    pw.print(modelo.getColumnName(i) + (i == modelo.getColumnCount() - 1 ? "" : ";"));
                }
                pw.println();

                // Escribir filas
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        Object valor = modelo.getValueAt(i, j);
                        pw.print((valor != null ? valor.toString() : "") + (j == modelo.getColumnCount() - 1 ? "" : ";"));
                    }
                    pw.println();
                }

                utilidades.AlertaUtil.mostrarExito(this, "¡Informe de rotación exportado con éxito a Excel!");
            } catch (Exception e) {
                utilidades.AlertaUtil.mostrarError(this, "Error al exportar el archivo:");
            }
        }
    }

    private void fechaPorDefecto() {
        // Colocar fechas por defecto (ej. mes actual o fecha actual)
        if (jdcFechaIncial != null) {
            jdcFechaIncial.setDate(new java.util.Date());
        }
        if (jdcFechaFinal != null) {
            jdcFechaFinal.setDate(new java.util.Date());
        }
    }

    private void generarRotacionVentas() {
        boolean agruparProducto = rdbProducto.isSelected();
        String familia = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS LAS FAMILIAS";
        java.util.Date fechaInicio = jdcFechaIncial.getDate();
        java.util.Date fechaFin = jdcFechaFinal.getDate();

        // Instanciamos el DAO (ajusta el nombre si tu clase DAO se llama distinto)
        dao.BodegasDAO bodegaDao = new dao.BodegasDAO();
        java.util.List<Object[]> datos = bodegaDao.obtenerRotacionVentas(agruparProducto, familia, fechaInicio, fechaFin);

        // Limpiamos y poblamos la tabla tblDetalle
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0);

        double totalUnidadesVEND = 0.0;
        double totalVentasDinero = 0.0;

        for (Object[] fila : datos) {
            modelo.addRow(fila);
            if (fila[2] != null) {
                totalUnidadesVEND += Double.parseDouble(fila[2].toString());
            }
            if (fila[3] != null) {
                totalVentasDinero += Double.parseDouble(fila[3].toString());
            }
        }

        // Actualizamos los labels inferiores (lblTotalUni y lblTotalVentas)
        if (lblTotalUni != null) {
            lblTotalUni.setText(String.format("%.0f", totalUnidadesVEND));
        }
        if (lblTotalVentas != null) {
            lblTotalVentas.setText(String.format("$%.2f", totalVentasDinero));
        }

        if (datos.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No se encontraron registros de ventas para los filtros seleccionados.");
        }
    }

    private void inicializarTablaRotacion() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Código", "Descripción", "Cant.Vend", "Total Vta", "% Particip"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        tblDetalle.setModel(modelo);

        // Ajustar anchos recomendados para las columnas
        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(90);  // Código
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(220); // Descripción
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(80);  // Cant.Vend
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(90);  // Total Vta
        tblDetalle.getColumnModel().getColumn(4).setPreferredWidth(80);  // % Particip
    }

    private void inicializarFiltrosRadioRotacion() {
        javax.swing.ButtonGroup grupoAgrupar = new javax.swing.ButtonGroup();
        grupoAgrupar.add(rdbProducto);
        grupoAgrupar.add(rdbFamilia);

        // Seleccionamos "Por Producto" por defecto al abrir
        rdbProducto.setSelected(true);
    }

    private void imprimirPdfRotacion() {
        String familia = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS";
        String tipoAgrupacion = rdbProducto.isSelected() ? "Por Producto" : "Por Familia";

        utilidades.ReporteUtil.generarPDFRotacionVentas(
                tblDetalle,
                familia,
                tipoAgrupacion,
                jdcFechaIncial,
                jdcFechaFinal,
                lblTotalUni,
                lblTotalVentas
        );
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rdbProducto = new javax.swing.JRadioButton();
        rdbFamilia = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        cmbFamilia = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jdcFechaIncial = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        jdcFechaFinal = new com.toedter.calendar.JDateChooser();
        btnGenerar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        lblTotalUni = new javax.swing.JLabel();
        lblTotalVentas = new javax.swing.JLabel();

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE CONSULTA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Agrupar por:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        buttonGroup1.add(rdbProducto);
        rdbProducto.setText("Por Producto");
        jPanel1.add(rdbProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, -1, -1));

        buttonGroup1.add(rdbFamilia);
        rdbFamilia.setText("Por Familia");
        jPanel1.add(rdbFamilia, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 30, -1, -1));

        jLabel2.setText("Familia:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        cmbFamilia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbFamilia, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 610, -1));

        jLabel3.setText("Fecha Inicial:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));
        jPanel1.add(jdcFechaIncial, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 90, 260, -1));

        jLabel4.setText("Fecha Final:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 95, -1, -1));
        jPanel1.add(jdcFechaFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 90, 260, -1));

        btnGenerar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGenerar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/informacion.png"))); // NOI18N
        btnGenerar.setText("GENERAR");
        btnGenerar.setBorderPainted(false);
        btnGenerar.setContentAreaFilled(false);
        btnGenerar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGenerar.setFocusPainted(false);
        btnGenerar.addActionListener(this::btnGenerarActionPerformed);
        jPanel1.add(btnGenerar, new org.netbeans.lib.awtextra.AbsoluteConstraints(595, 120, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 25, 740, 165));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE ROTACIÓN DE VENTAS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Cant.Vend", "Total Vta", "% Particip"
            }
        ));
        tblDetalle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDetalle);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 28, 710, 200));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 190, 740, 245));

        jLabel5.setText("Total Unidades Vendidas:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        jLabel6.setText("TOTAL VENTAS:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 450, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(525, 485, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(625, 485, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("INFORME / ROTACIÓN DE VENTAS Y PRODUCTOS");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

        lblTotalUni.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalUni.setText("0");
        pnlPrincipal.add(lblTotalUni, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 450, -1, -1));

        lblTotalVentas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalVentas.setText("0");
        pnlPrincipal.add(lblTotalVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 450, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 770, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarActionPerformed
        generarRotacionVentas();
    }//GEN-LAST:event_btnGenerarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcelRotacion();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirPdfRotacion();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnGenerar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbFamilia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jdcFechaFinal;
    private com.toedter.calendar.JDateChooser jdcFechaIncial;
    private javax.swing.JLabel lblTotalUni;
    private javax.swing.JLabel lblTotalVentas;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rdbFamilia;
    private javax.swing.JRadioButton rdbProducto;
    private javax.swing.JTable tblDetalle;
    // End of variables declaration//GEN-END:variables
}
