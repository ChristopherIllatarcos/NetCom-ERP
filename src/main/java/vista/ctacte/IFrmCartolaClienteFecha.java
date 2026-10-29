package vista.ctacte;

/**
 *
 * @author caill
 */
public class IFrmCartolaClienteFecha extends javax.swing.JInternalFrame {

    private int clienteIdSeleccionado = 0;

    public IFrmCartolaClienteFecha() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configurarTablaMovimientos();
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

    private void buscarClientePorRut() {
        String rutBuscado = txtRut.getText().trim();
        if (rutBuscado.isEmpty()) {
            return;
        }

        // Llamamos al DAO que ya creamos anteriormente
        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        Object[] datosCliente = daoCc.buscarClientePorRut(rutBuscado);

        if (datosCliente != null) {
            this.clienteIdSeleccionado = (Integer) datosCliente[0];
            txtNombre.setText((String) datosCliente[1]);
            jdcDesde.requestFocus(); // Salta el cursor directo a la fecha inicial para comodidad
        } else {
            this.clienteIdSeleccionado = 0;
            txtNombre.setText("");
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Cliente no encontrado con ese RUT.");
            txtRut.requestFocus();
        }
    }

    private void botonConsultar() {
        // 1. Validar cliente seleccionado
        if (this.clienteIdSeleccionado <= 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar un cliente válido ingresando su RUT.");
            txtRut.requestFocus();
            return;
        }

        java.util.Date fechaDesde = jdcDesde.getDate();
        java.util.Date fechaHasta = jdcHasta.getDate();

        // 2. Consultar al DAO (reutilizamos el método de cartola que ya tenemos)
        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        java.util.List<Object[]> listaMovimientos = daoCc.obtenerCartolaClientePorVentas(this.clienteIdSeleccionado, fechaDesde, fechaHasta);

        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblMovimientos.getModel();
        modelo.setRowCount(0); // Limpiar la tabla antes de cargar

        double totalCargo = 0.0;
        double totalAbono = 0.0;
        double saldoAcumulado = 0.0;

        // 3. Recorrer movimientos y calcular saldo acumulado
        for (Object[] filaDao : listaMovimientos) {
            java.util.Date fecha = (java.util.Date) filaDao[0];
            String documento = (String) filaDao[1];
            String nDoc = (String) filaDao[2];
            double cargo = (Double) filaDao[3];
            double abono = (Double) filaDao[4];

            saldoAcumulado = saldoAcumulado + cargo - abono;
            totalCargo += cargo;
            totalAbono += abono;

            modelo.addRow(new Object[]{
                new java.text.SimpleDateFormat("dd/MM/yyyy").format(fecha),
                documento,
                nDoc,
                String.format("$ %,.0f", cargo),
                String.format("$ %,.0f", abono),
                String.format("$ %,.0f", saldoAcumulado)
            });
        }

        // 4. Actualizar las etiquetas de totales de la interfaz
        lblTotCargo.setText(String.format("$ %,.0f", totalCargo));
        lblTotAbonos.setText(String.format("$ %,.0f", totalAbono));
        lblSaldoFin.setText(String.format("$ %,.0f", saldoAcumulado));

        // 5. CONTROL DE MEMORIA Y EXPORTACIÓN AUTOMÁTICA (> 100 registros)
        if (tblMovimientos.getRowCount() >= 100) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "La cartola ha superado los 100 registros (" + tblMovimientos.getRowCount() + ").\n"
                    + "Se exportará a Excel automáticamente y se liberará la memoria de la vista.",
                    "Aviso de Seguridad Masiva",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

            String nombreSugerido = "Cartola_Fecha_" + txtRut.getText().trim() + "_" + System.currentTimeMillis();
            utilidades.ExcelUtil.exportarTablaAExcel(tblMovimientos, nombreSugerido);

            // Limpiar para liberar memoria
            modelo.setRowCount(0);
            lblTotCargo.setText("$ 0");
            lblTotAbonos.setText("$ 0");
            lblSaldoFin.setText("$ 0");
        }
    }

    private void botonExcel() {
        utilidades.ExcelUtil.exportarTablaAExcel(tblMovimientos, "Cartola_Fecha_" + txtRut.getText().trim());
    }

    private void botonImprimir() {
        utilidades.ReporteUtil.generarPDFCartolaClientePorVentas(
                tblMovimientos,
                txtNombre.getText().trim(),
                txtRut.getText().trim(),
                jdcDesde,
                jdcHasta,
                lblTotCargo,
                lblTotAbonos,
                lblSaldoFin
        );
    }

    private void colorBotones() {
        //java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configurarVentana() {
        this.setTitle("CARTOLA INTERNA CTA. CTE. CLIENTE (POR FECHA)");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void configurarTablaMovimientos() {
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(
                new Object[]{"Fecha", "Documento", "N° Doc", "Cargo ($)", "Abono ($)", "Saldo ($)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Bloquea edición directa
            }
        };

        tblMovimientos.setModel(modeloTabla);

        tblMovimientos.getColumnModel().getColumn(0).setPreferredWidth(95);  // Fecha
        tblMovimientos.getColumnModel().getColumn(1).setPreferredWidth(130); // Documento
        tblMovimientos.getColumnModel().getColumn(2).setPreferredWidth(100); // N° Doc
        tblMovimientos.getColumnModel().getColumn(3).setPreferredWidth(100); // Cargo
        tblMovimientos.getColumnModel().getColumn(4).setPreferredWidth(100); // Abono
        tblMovimientos.getColumnModel().getColumn(5).setPreferredWidth(100); // Saldo
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
        txtRut = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jdcDesde = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        jdcHasta = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMovimientos = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblTotCargo = new javax.swing.JLabel();
        lblTotAbonos = new javax.swing.JLabel();
        lblSaldoFin = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("SELECCIÓN DE CLIENTE Y FECHAS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("R.U.T. Cliente:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        txtRut.setForeground(new java.awt.Color(150, 150, 150));
        txtRut.addActionListener(this::txtRutActionPerformed);
        txtRut.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRutKeyPressed(evt);
            }
        });
        jPanel1.add(txtRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 160, -1));

        jLabel2.setText("Nombre:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 30, -1, -1));

        txtNombre.setForeground(new java.awt.Color(150, 150, 150));
        txtNombre.setName(""); // NOI18N
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 30, 350, -1));

        jLabel3.setText("Desde:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, -1, -1));
        jPanel1.add(jdcDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 160, -1));

        jLabel4.setText("Hasta:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 60, -1, -1));
        jPanel1.add(jdcHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 60, 160, -1));

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
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 65, -1, -1));

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel1.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 25, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 35, 732, 110));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("MOVIMIENTOS Y SALDOS DE LA CUENTA"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblMovimientos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblMovimientos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblMovimientos);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, 710, 240));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 155, 730, 270));

        jLabel5.setText("Tot. Cargos:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 440, -1, -1));

        jLabel6.setText("Tot. Abonos:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 440, -1, -1));

        jLabel7.setText("Saldo Final:");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 440, -1, -1));

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
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 468, -1, -1));

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
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 468, -1, -1));

        lblTotCargo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotCargo.setForeground(new java.awt.Color(0, 102, 204));
        lblTotCargo.setText("0");
        pnlPrincipal.add(lblTotCargo, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 440, -1, -1));

        lblTotAbonos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotAbonos.setForeground(new java.awt.Color(0, 102, 204));
        lblTotAbonos.setText("0");
        pnlPrincipal.add(lblTotAbonos, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 440, -1, -1));

        lblSaldoFin.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSaldoFin.setForeground(new java.awt.Color(0, 102, 204));
        lblSaldoFin.setText("0");
        pnlPrincipal.add(lblSaldoFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 440, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("CARTOLA INTERNA CTA. CTE. CLIENTE (POR FECHA)");
        pnlPrincipal.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered

    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnImprimirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseEntered
    }//GEN-LAST:event_btnImprimirMouseEntered

    private void btnImprimirMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseExited

    }//GEN-LAST:event_btnImprimirMouseExited

    private void txtRutKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRutKeyPressed

    }//GEN-LAST:event_txtRutKeyPressed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarClientePorRut();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtRutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRutActionPerformed


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
    private com.toedter.calendar.JDateChooser jdcDesde;
    private com.toedter.calendar.JDateChooser jdcHasta;
    private javax.swing.JLabel lblSaldoFin;
    private javax.swing.JLabel lblTotAbonos;
    private javax.swing.JLabel lblTotCargo;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblMovimientos;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRut;
    // End of variables declaration//GEN-END:variables

}
