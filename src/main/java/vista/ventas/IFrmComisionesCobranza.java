package vista.ventas;

/**
 *
 * @author caill
 */
public class IFrmComisionesCobranza extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameComisionesCobranza
     */
    public IFrmComisionesCobranza() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        cargarVendedores();
        configuracionBotones();
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

    private void configuracionBotones() {
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnConsultar, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnExcel, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    private void colorBotones() {
        // Verde claro suave para Guardar / Aceptar
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        java.awt.Color colorExcel = new java.awt.Color(209, 250, 229);

        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnExcel, colorExcel);

    }

    private void configurarVentana() {
        this.setTitle("INFORME DE COMISIONES POR COBRANZA");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void cargarVendedores() {
        cmbVendedor.removeAllItems();
        cmbVendedor.addItem("Todos los Vendedores"); // Opción inicial para consultar global

        dao.VendedorDAO vendedorDAO = new dao.VendedorDAO();
        java.util.List<modelo.Vendedor> lista = vendedorDAO.listarVendedores();

        for (modelo.Vendedor v : lista) {
            cmbVendedor.addItem(v.getId() + " - " + v.getNombre());
        }
    }

    private void ejecutarConsulta() {
        // 1. Validar selección de fechas
        java.util.Date desde = jDateChooserDesde.getDate();
        java.util.Date hasta = jDateChooserHasta.getDate();

        if (desde == null || hasta == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccinar un rango de fecha válido.");
            return;
        }

        if (desde.after(hasta)) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        // 2. Obtener el ID del vendedor seleccionado en el JComboBox
        int idVendedor = 0;
        if (cmbVendedor.getSelectedIndex() > 0) {
            Object itemSeleccionado = cmbVendedor.getSelectedItem();

            if (itemSeleccionado instanceof modelo.Vendedor vendedor) {
                // Si agregaste objetos Vendedor directamente
                idVendedor = vendedor.getId();
            } else {
                // Si son cadenas String (ej: "1 - DAVID /VENDE")
                String item = itemSeleccionado.toString();
                if (item.contains(" - ")) {
                    try {
                        idVendedor = Integer.parseInt(item.split(" - ")[0].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear el ID del vendedor: " + e.getMessage());
                    }
                }
            }
        }

        // 3. Consultar a la base de datos
        dao.ConsultaDocumentosDAO daoConsulta = new dao.ConsultaDocumentosDAO();
        java.util.List<Object[]> lista = daoConsulta.obtenerComisionesPorCobranza(desde, hasta, idVendedor);

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        model.setRowCount(0); // Limpiar filas anteriores

        if (lista.isEmpty()) {
            utilidades.AlertaUtil.mostrarInformacion(this, "No se encontraron cobranzas en el periodo seleccionado.");
            // Resetear labels en $ 0,00
            lblTotalCobrado.setText("$ 0,00");
            lblTotalComisiones.setText("$ 0,00");
            return;
        }

        // 4. Formateador en Pesos Chilenos con decimales ($ #,##0.00)
        java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "CL"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0.00", simbolos);

        double sumTotalCobrado = 0.0;
        double sumTotalComisiones = 0.0;

        // 5. Llenar la tabla
        for (Object[] fila : lista) {
            double cobranzaTotal = (Double) fila[2];
            double pctComision = (Double) fila[3];
            double montoComision = (Double) fila[4];

            sumTotalCobrado += cobranzaTotal;
            sumTotalComisiones += montoComision;

            model.addRow(new Object[]{
                fila[0], // N° Recibo
                fila[1], // Nombre Vendedor
                df.format(cobranzaTotal), // Cobranza Total
                String.format("%.1f %%", pctComision), // % Comision
                df.format(montoComision) // $ Comision
            });
        }

        // 6. Actualizar JLabels de Totales
        lblTotalCobrado.setText(df.format(sumTotalCobrado));
        lblTotalComisiones.setText(df.format(sumTotalComisiones));
    }

    private void exportarExcel() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();

        if (model.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No hay datos en la tabla para exportar.",
                    "Atención", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Informe de Comisiones");
        fileChooser.setSelectedFile(new java.io.File("Informe_Comisiones.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(fileToSave),
                            java.nio.charset.StandardCharsets.ISO_8859_1))) {

                // 1. Escribir Encabezados
                for (int i = 0; i < model.getColumnCount(); i++) {
                    pw.print(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? ";" : ""));
                }
                pw.println();

                // 2. Escribir Filas de Datos
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        pw.print((val != null ? val.toString() : "") + (j < model.getColumnCount() - 1 ? ";" : ""));
                    }
                    pw.println();
                }

                // 3. Escribir Totales al final
                pw.println();
                pw.println("Totales Summary;;;");
                pw.println("Total Cobrado:;" + lblTotalCobrado.getText() + ";;");
                pw.println("Total Comisiones:;" + lblTotalComisiones.getText() + ";;");

                utilidades.AlertaUtil.mostrarExito(this, "Archivo exportado con éxito.");

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error al exportar archivo: " + e.getMessage(),
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jDateChooserDesde = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserHasta = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        cmbVendedor = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        lblTotalCobrado = new javax.swing.JLabel();
        lblTotalComisiones = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CRITERIOS DE BÚSQUEDA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Desde:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));
        jPanel1.add(jDateChooserDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, 300, -1));

        jLabel2.setText("Hasta:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 30, -1, -1));
        jPanel1.add(jDateChooserHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 30, 300, -1));

        jLabel3.setText("Vendedor:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        cmbVendedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos los Vendedores" }));
        cmbVendedor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(cmbVendedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, 520, -1));

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
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 70, -1, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 100, 100, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 30, 746, 117));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE COBRANZAS Y COMISIONES"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "N° Recibo", "Nombre Vendedor", "Cobranza Total", "% Com.", "$ Comisi."
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 27, 720, 240));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 164, 746, 280));

        jLabel4.setText("Total Cobrado:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, -1, -1));

        jLabel5.setText("Total Comisiones:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, -1, -1));

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
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 450, -1, -1));
        pnlPrincipal.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 480, 70, -1));

        lblTotalCobrado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCobrado.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalCobrado.setText("$ ");
        pnlPrincipal.add(lblTotalCobrado, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 460, -1, -1));

        lblTotalComisiones.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalComisiones.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalComisiones.setText("$ ");
        pnlPrincipal.add(lblTotalComisiones, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 460, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("INFORME DE COMISIONES POR COBRANZA");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        ejecutarConsulta();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcel();
    }//GEN-LAST:event_btnExcelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JComboBox<String> cmbVendedor;
    private com.toedter.calendar.JDateChooser jDateChooserDesde;
    private com.toedter.calendar.JDateChooser jDateChooserHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTotalCobrado;
    private javax.swing.JLabel lblTotalComisiones;
    private javax.swing.JPanel pnlPrincipal;
    // End of variables declaration//GEN-END:variables
}
