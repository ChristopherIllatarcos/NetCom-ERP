package vista.bodegas;


/**
 *
 * @author caill
 */
public class IFrmInventario extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameInventario
     */
    public IFrmInventario() {
        initComponents();
        colorBotones();
        inicializarTablaInventario();
        cargarComboFamilias();
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
        //        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
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
        this.setTitle("INVENTARIO DE PRODUCTOS (VALORIZADO Y STOCK)");
        this.setSize(785, 540);
        this.setResizable(false);

    }

    private void cargarComboFamilias() {
        cmbFamilia.removeAllItems();
        cmbFamilia.addItem("TODAS LAS FAMILIAS"); 

        dao.FamiliasDAO daoFamilias = new dao.FamiliasDAO();
        java.util.List<modelo.FamiliaDTO> listaFamilias = daoFamilias.listarFamilias();

        for (modelo.FamiliaDTO familia : listaFamilias) {
            cmbFamilia.addItem(familia.getNombre()); 
        }
    }

    private void consultarInventario() {
        // Obtenemos los valores de los filtros de la interfaz
        java.util.Date fechaCorte = jdcAlCorte.getDate();
        String familia = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS LAS FAMILIAS";
        boolean verCeros = chkVerCeros.isSelected();

        // Instanciamos tu DAO (ajusta el nombre si usas BodegaDAO)
        dao.BodegasDAO bodegaDao = new dao.BodegasDAO();
        java.util.List<Object[]> datos = bodegaDao.obtenerInventario(fechaCorte, familia, verCeros);

        // Limpiamos y poblamos la tabla
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDatos.getModel();
        modelo.setRowCount(0);

        int totalItems = 0;
        int totalUnidades = 0;

        for (Object[] fila : datos) {
            modelo.addRow(fila);
            totalItems++;
            if (fila[3] != null) {
                totalUnidades += ((Number) fila[3]).intValue();
            }
        }

        // Actualizamos los labels inferiores (lblItems y lblTotalUni)
        lblItems.setText(String.valueOf(totalItems));
        lblTotalUni.setText(String.valueOf(totalUnidades));

        if (datos.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No se encontraron productos con los filtros seleccionados.");
        }
    }

    private void exportarExcelInventario() {
        javax.swing.table.TableModel modelo = tblDatos.getModel();
        if (modelo.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No hay datos para exportar en la tabla.", "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Inventario como Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("Inventario_" + System.currentTimeMillis() + ".csv"));

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

                utilidades.AlertaUtil.mostrarExito(this, "¡Inventario exportado con éxito a Excel!");
            } catch (Exception e) {
                utilidades.AlertaUtil.mostrarError(this, "Error al exportar.");
            }
        }
    }

    private void inicializarTablaInventario() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Código", "Descripción", "Familia", "Stock", "Precio Costo / Unit...", "Total Valorizado"
                }
        ) {
            boolean[] canEdit = new boolean[]{
                false, false, false, false, false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        tblDatos.setModel(modelo);

        // Ajustar anchos de columnas opcionalmente
        tblDatos.getColumnModel().getColumn(0).setPreferredWidth(90);  // Código
        tblDatos.getColumnModel().getColumn(1).setPreferredWidth(180); // Descripción
        tblDatos.getColumnModel().getColumn(2).setPreferredWidth(100); // Familia
        tblDatos.getColumnModel().getColumn(3).setPreferredWidth(70);  // Stock
        tblDatos.getColumnModel().getColumn(4).setPreferredWidth(110); // Precio Costo
        tblDatos.getColumnModel().getColumn(5).setPreferredWidth(110); // Total Valorizado
    }

    private void imprimirInventarioPdf() {
        String familiaSeleccionada = cmbFamilia.getSelectedItem() != null ? cmbFamilia.getSelectedItem().toString() : "TODAS";
        utilidades.ReporteUtil.generarPDFInventario(
                tblDatos,
                jdcAlCorte,
                familiaSeleccionada,
                lblItems,
                lblTotalUni
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

        pnlPrincipal = new javax.swing.JPanel();
        pnlFiltroInventario = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jdcAlCorte = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        cmbFamilia = new javax.swing.JComboBox<>();
        chkVerCeros = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        btnConsultar = new javax.swing.JButton();
        pnlDatosGenerales = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDatos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblItems = new javax.swing.JLabel();
        lblTotalUni = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFiltroInventario.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltroInventario.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE INVENTARIO"));
        pnlFiltroInventario.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Al corte:");
        pnlFiltroInventario.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 32, -1, -1));
        pnlFiltroInventario.add(jdcAlCorte, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 30, 270, -1));

        jLabel2.setText("Familia:");
        pnlFiltroInventario.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 30, -1, -1));

        cmbFamilia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", "categoría", "familia de productos" }));
        cmbFamilia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlFiltroInventario.add(cmbFamilia, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 30, 270, -1));

        chkVerCeros.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnlFiltroInventario.add(chkVerCeros, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        jLabel3.setText("Incluir productos sin stock (Saldos Cero)");
        pnlFiltroInventario.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, -1, -1));

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
        pnlFiltroInventario.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 70, -1, -1));

        pnlPrincipal.add(pnlFiltroInventario, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 740, 120));

        pnlDatosGenerales.setBackground(new java.awt.Color(255, 255, 255));
        pnlDatosGenerales.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS GENERALES"));
        pnlDatosGenerales.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Familia", "Stock", "Precio Costo / Unitario", "Total Valorizado"
            }
        ));
        tblDatos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDatos);

        pnlDatosGenerales.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 23, 710, 250));

        pnlPrincipal.add(pnlDatosGenerales, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 740, 290));

        jLabel4.setText("Total Ítems:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 460, -1, -1));

        jLabel5.setText("Total Unidades:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 460, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setToolTipText("");
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
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 457, -1, -1));

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
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 457, 120, -1));

        lblItems.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblItems.setText("0");
        pnlPrincipal.add(lblItems, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 460, -1, -1));

        lblTotalUni.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalUni.setText("0");
        pnlPrincipal.add(lblTotalUni, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 460, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("INVENTARIO DE PRODUCTOS (VALORIZADO Y STOCK)");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 5, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 785, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
        // jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
        // jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        consultarInventario();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered
        // jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
        // jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnImprimirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseEntered
        //  jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnImprimirMouseEntered

    private void btnImprimirMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseExited
        // jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnImprimirMouseExited

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcelInventario();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirInventarioPdf();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JCheckBox chkVerCeros;
    private javax.swing.JComboBox<String> cmbFamilia;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jdcAlCorte;
    private javax.swing.JLabel lblItems;
    private javax.swing.JLabel lblTotalUni;
    private javax.swing.JPanel pnlDatosGenerales;
    private javax.swing.JPanel pnlFiltroInventario;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDatos;
    // End of variables declaration//GEN-END:variables
}
