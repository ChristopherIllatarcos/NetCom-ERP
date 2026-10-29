package vista.bodegas;

public class IFrmKardexMercaderia extends javax.swing.JInternalFrame {

    public IFrmKardexMercaderia() {
        initComponents();
        colorBotones();
        inicializarTablaKardex();
        ConfiguracionVentana();
        quitarBordeBarra();
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

    private void buscarProductoPorCodigo() {
        String codigo = txtCodProducto.getText().trim();
        if (codigo.isEmpty()) {
            return;
        }

        dao.ProductosDAO daoProd = new dao.ProductosDAO();
        String[] datos = daoProd.buscarDatosPorCodigo(codigo);

        if (datos != null) {
            String nombre = datos[0];
            String desc = datos[1];
            txtDescripcion.setText(nombre + (desc != null && !desc.isEmpty() ? " - " + desc : ""));
        } else {
            txtDescripcion.setText("");
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Producto no encontrado.");
        }
    }

    private void ConfiguracionVentana() {
        this.setTitle("KARDEX / MOVIMIENTOS DE MERCADERÍA");
        this.setSize(770, 540);
        this.setResizable(false);
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

    private void consultarKardex() {
        String codProducto = txtCodProducto.getText().trim();
        if (codProducto.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar o buscar un código de producto.");
            return;
        }

        // Obtenemos las fechas de los JDateChooser
        java.util.Date fechaInicio = jdcFechaInicial.getDate();
        java.util.Date fechaFin = jdcFechaFinal.getDate();

        dao.BodegasDAO tipoDao = new dao.BodegasDAO();

        // Llamamos al método que trae los movimientos de la BD
        java.util.List<Object[]> movimientos = tipoDao.obtenerHistorialKardex(codProducto, fechaInicio, fechaFin);

        // Limpiamos y poblamos la tabla
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblHistorial.getModel();
        modelo.setRowCount(0);

        int totalEntradas = 0;
        int totalSalidas = 0;
        int ultimoSaldo = 0;

        // Recorremos los movimientos obtenidos de PostgreSQL y los agregamos a la tabla
        for (Object[] fila : movimientos) {
            modelo.addRow(fila);

            // Sumamos entradas y salidas de forma segura
            if (fila[3] != null) {
                totalEntradas += ((Number) fila[3]).intValue();
            }
            if (fila[4] != null) {
                totalSalidas += ((Number) fila[4]).intValue();
            }

            // Guardamos el último saldo registrado
            if (fila[5] != null) {
                ultimoSaldo = ((Number) fila[5]).intValue();
            }
        }

        // Actualizamos los labels inferiores
        lblEntradas.setText(String.valueOf(totalEntradas));
        lblSalidas.setText(String.valueOf(totalSalidas));
        lblSaldo.setText(String.valueOf(ultimoSaldo));

        if (movimientos.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se encontraron movimientos para el producto en el rango de fechas seleccionado.", "Sin registros", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportarExcel() {
        javax.swing.table.TableModel modelo = tblHistorial.getModel();
        if (modelo.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay datos para exportar en la tabla");
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar como Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("Kardex_" + txtCodProducto.getText().trim() + ".csv"));

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

                utilidades.AlertaUtil.mostrarExito(this, "¡Archivo exportado con éxito a Excel!");
            } catch (Exception e) {
                utilidades.AlertaUtil.mostrarError(this, "Error al exportar el archivo.");
            }
        }
    }

    private void inicializarTablaKardex() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Fecha", "Tipo Movimiento", "N° Doc", "Entrada", "Salida", "Saldo"
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

        tblHistorial.setModel(modelo);

        // Opcional: Centrar algunas columnas o ajustar anchos si lo deseas
        tblHistorial.getColumnModel().getColumn(0).setPreferredWidth(100); // Fecha
        tblHistorial.getColumnModel().getColumn(1).setPreferredWidth(130); // Tipo Movimiento
        tblHistorial.getColumnModel().getColumn(2).setPreferredWidth(90);  // N° Doc
        tblHistorial.getColumnModel().getColumn(3).setPreferredWidth(80);  // Entrada
        tblHistorial.getColumnModel().getColumn(4).setPreferredWidth(80);  // Salida
        tblHistorial.getColumnModel().getColumn(5).setPreferredWidth(80);  // Saldo
    }

    private void imprimirKardex() {
        utilidades.ReporteUtil.generarPDFKardex(
                tblHistorial,
                jdcFechaInicial,
                jdcFechaFinal,
                txtCodProducto.getText().trim(),
                txtDescripcion.getText().trim(),
                lblEntradas,
                lblSalidas,
                lblSaldo
        );
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void txtCodProducto() {
        txtCodProducto.addActionListener(e -> buscarProductoPorCodigo());
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
        txtCodProducto = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jdcFechaInicial = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        jdcFechaFinal = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistorial = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblEntradas = new javax.swing.JLabel();
        lblSalidas = new javax.swing.JLabel();
        lblSaldo = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE CONSULTA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Cód. Producto:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, -1, -1));

        txtCodProducto.addActionListener(this::txtCodProductoActionPerformed);
        jPanel1.add(txtCodProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 250, -1));

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel1.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 25, -1, -1));

        jLabel2.setText("Descripción:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 30, -1, -1));
        jPanel1.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 30, 240, -1));

        jLabel3.setText("Fecha Inicial:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));
        jPanel1.add(jdcFechaInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 250, -1));

        jLabel4.setText("Fecha Final:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 60, -1, -1));
        jPanel1.add(jdcFechaFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 60, 240, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(605, 100, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 35, 740, 150));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("HISTORIAL DE MOVIMIENTOS (KARDEX)"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblHistorial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Fecha", "Tipo Movimiento", "N° Doc", "Entrada", "Salida", "Saldo"
            }
        ));
        tblHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblHistorial);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 25, 710, 200));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 200, 740, 240));

        jLabel5.setText("Entradas:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, -1, -1));

        jLabel6.setText("Salidas:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 480, -1, -1));

        jLabel7.setText("Saldo Final en Stock:");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 450, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 480, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 480, -1, -1));

        lblEntradas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblEntradas.setText("0");
        pnlPrincipal.add(lblEntradas, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 450, -1, -1));

        lblSalidas.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSalidas.setText("0");
        pnlPrincipal.add(lblSalidas, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 480, -1, -1));

        lblSaldo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSaldo.setText("0");
        pnlPrincipal.add(lblSaldo, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 480, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("KARDEX / MOVIMIENTOS DE MERCADERÍA");
        pnlPrincipal.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 10, -1, -1));

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

    private void txtCodProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodProductoActionPerformed
        txtCodProducto();
    }//GEN-LAST:event_txtCodProductoActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarProductoPorCodigo();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        consultarKardex();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        exportarExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        imprimirKardex();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
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
    private com.toedter.calendar.JDateChooser jdcFechaFinal;
    private com.toedter.calendar.JDateChooser jdcFechaInicial;
    private javax.swing.JLabel lblEntradas;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblSalidas;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblHistorial;
    private javax.swing.JTextField txtCodProducto;
    private javax.swing.JTextField txtDescripcion;
    // End of variables declaration//GEN-END:variables
}
