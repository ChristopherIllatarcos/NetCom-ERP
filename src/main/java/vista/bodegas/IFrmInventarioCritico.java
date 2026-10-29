package vista.bodegas;

import dao.FamiliasDAO;
import java.util.List;

public class IFrmInventarioCritico extends javax.swing.JInternalFrame {

    private FamiliasDAO familiaDAO = new FamiliasDAO();

    public IFrmInventarioCritico() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        inicializarTablaStockCritico();
        inicializarFiltrosRadio();
        cargarComboFamiliasCriticas();
        ConfiguracionVentana();
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
        aplicarEfectoHoverPlano(btnFiltrar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void ConfiguracionVentana() {
        this.setTitle("REPORTE / INVENTARIO CRÍTICO (ALERTA DE REABASTECIMIENTO)");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarComboFamiliasCriticas() {
        cmbFamiliaCat.removeAllItems();
        cmbFamiliaCat.addItem("TODAS LAS FAMILIAS");

        dao.FamiliasDAO daoFamilias = new dao.FamiliasDAO();

        List<modelo.FamiliaDTO> listaFamilias = daoFamilias.listarFamilias();

        for (modelo.FamiliaDTO familia : listaFamilias) {
            cmbFamiliaCat.addItem(familia.getNombre());
        }
    }

    private void inicializarTablaStockCritico() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Código", "Descripción", "Stock", "Stock Min", "Faltante"
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

        tblProducStock.setModel(modelo);

        // Ajustar anchos recomendados para las columnas
        tblProducStock.getColumnModel().getColumn(0).setPreferredWidth(90);  // Código
        tblProducStock.getColumnModel().getColumn(1).setPreferredWidth(220); // Descripción
        tblProducStock.getColumnModel().getColumn(2).setPreferredWidth(70);  // Stock
        tblProducStock.getColumnModel().getColumn(3).setPreferredWidth(80);  // Stock Min
        tblProducStock.getColumnModel().getColumn(4).setPreferredWidth(80);  // Faltante
    }

    private void inicializarFiltrosRadio() {
        javax.swing.ButtonGroup grupoEstado = new javax.swing.ButtonGroup();
        grupoEstado.add(rdbTodoCritico);
        grupoEstado.add(rdbStockCero);

        rdbTodoCritico.setSelected(true);
    }

    private void imprimirPdfCritico() {
        String familia = cmbFamiliaCat.getSelectedItem() != null ? cmbFamiliaCat.getSelectedItem().toString() : "TODAS";
        String tipoFiltro = rdbStockCero.isSelected() ? "Stock Cero (Agotados)" : "Todos Críticos";

        utilidades.ReporteUtil.generarPDFInventarioCritico(
                tblProducStock,
                familia,
                tipoFiltro,
                lblProducto
        );
    }

    private void exportarExcelCritico() {
        javax.swing.table.TableModel modelo = tblProducStock.getModel();
        if (modelo.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarError(this, "No hay datos en la tabla para exportar.");
            return;
            
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Inventario Crítico como Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("Inventario_Critico_" + System.currentTimeMillis() + ".csv"));

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

                javax.swing.JOptionPane.showMessageDialog(this, "¡Reporte crítico exportado con éxito a Excel!");
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al exportar el archivo: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filtrarInventarioCritico() {
        // Obtenemos los filtros de la interfaz
        String familia = cmbFamiliaCat.getSelectedItem() != null ? cmbFamiliaCat.getSelectedItem().toString() : "TODAS LAS FAMILIAS";
        boolean soloCero = rdbStockCero.isSelected(); // Si está seleccionado el radio de stock cero

        // Instanciamos el DAO (ajusta el nombre si tu clase DAO se llama distinto)
        dao.BodegasDAO bodegaDao = new dao.BodegasDAO();
        java.util.List<Object[]> datos = bodegaDao.obtenerInventarioCritico(familia, soloCero);

        // Limpiamos y poblamos la tabla tblProducStock
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblProducStock.getModel();
        modelo.setRowCount(0);

        int totalAlertas = 0;

        for (Object[] fila : datos) {
            modelo.addRow(fila);
            totalAlertas++;
        }

        // Actualizamos el label inferior de productos en alerta (lblProducto u otro label de conteo)
        if (lblProducto != null) {
            lblProducto.setText(String.valueOf(totalAlertas));
        }

        if (datos.isEmpty()) {
            utilidades.AlertaUtil.mostrarError(this, "No se encontraron productos en estado crítico con los filtros seleccionados.");
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbFamiliaCat = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        rdbTodoCritico = new javax.swing.JRadioButton();
        rdbStockCero = new javax.swing.JRadioButton();
        btnFiltrar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProducStock = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblProducto = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Familia / Cat.:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        cmbFamiliaCat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbFamiliaCat, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 620, -1));

        jLabel2.setText("Estado Stock:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        buttonGroup1.add(rdbTodoCritico);
        rdbTodoCritico.setText("Todos Críticos");
        jPanel1.add(rdbTodoCritico, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, -1, -1));

        buttonGroup1.add(rdbStockCero);
        rdbStockCero.setText("Stock Cero (Agotados)");
        jPanel1.add(rdbStockCero, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 70, -1, -1));

        btnFiltrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFiltrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnFiltrar.setText("FILTRAR");
        btnFiltrar.setBorderPainted(false);
        btnFiltrar.setContentAreaFilled(false);
        btnFiltrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(this::btnFiltrarActionPerformed);
        jPanel1.add(btnFiltrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 65, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 740, 110));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("PRODUCTOS EN STOCK CRÍTICO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblProducStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Stock", "Stock Min", "Faltante"
            }
        ));
        tblProducStock.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblProducStock);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 27, 700, 230));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 160, 740, 280));

        jLabel3.setText("Productos en Alerta:");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 460, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 460, -1, -1));

        lblProducto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblProducto.setText("0");
        pnlPrincipal.add(lblProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 460, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("REPORTE / INVENTARIO CRÍTICO (ALERTA DE REABASTECIMIENTO)");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        filtrarInventarioCritico();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcelCritico();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirPdfCritico();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbFamiliaCat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblProducto;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rdbStockCero;
    private javax.swing.JRadioButton rdbTodoCritico;
    private javax.swing.JTable tblProducStock;
    // End of variables declaration//GEN-END:variables
}
