package vista.ctacte;

public class IFrmCartolaClientePorVentas extends javax.swing.JInternalFrame {

    private int clienteIdSeleccionado = 0;

    public IFrmCartolaClientePorVentas() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configurarTablaDetalle();
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

    private void botonConsultar() {
        // 1. Validar que exista un cliente seleccionado (asumiendo que guardas el ID en alguna variable o campo oculto)
        // Si manejas el clienteId desde una búsqueda previa con txtRutCliente / txtNombre:
        int clienteId = obtenerClienteIdActual();

        if (clienteId <= 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar un cliente válido para consultar la cartola.");
            return;
        }

        java.util.Date fechaIni = jdcFechaIni.getDate();
        java.util.Date fechaFin = jdcFechaFin.getDate();

        // 2. Consultar al DAO
        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        java.util.List<Object[]> listaMovimientos = daoCc.obtenerCartolaClientePorVentas(clienteId, fechaIni, fechaFin);

        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0); // Limpiar la tabla antes de cargar

        double totalFac = 0.0;
        double totalAbonos = 0.0;
        double saldoAcumulado = 0.0;

        // 3. Recorrer los movimientos y calcular el saldo acumulado fila por fila
        for (Object[] filaDao : listaMovimientos) {
            java.util.Date fecha = (java.util.Date) filaDao[0];
            String documento = (String) filaDao[1];
            String nDoc = (String) filaDao[2];
            double cargo = (Double) filaDao[3];
            double abono = (Double) filaDao[4];

            // Calcular saldo: si es cargo suma, si es abono resta (ajusta según tu lógica contable)
            saldoAcumulado = saldoAcumulado + cargo - abono;

            totalFac += cargo;
            totalAbonos += abono;

            // Añadir a la tabla: Fecha, Documento, N° Doc, Cargo, Abono, Saldo
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
        lblTotalFac.setText(String.format("$ %,.0f", totalFac));
        lblTotalAbonos.setText(String.format("$ %,.0f", totalAbonos));
        lblSaldo.setText(String.format("$ %,.0f", saldoAcumulado));

        // 5. VALIDACIÓN AUTOMÁTICA: Si la tabla tiene 100 o más registros, exportar a Excel automáticamente
        if (tblDetalle.getRowCount() >= 100) {
            String mensaje = "La cartola ha superado los 100 registros (" + tblDetalle.getRowCount() + ").\n"
                    + "Se exportará a Excel automáticamente por seguridad.";

            utilidades.AlertaUtil.mostrarInformacion(this, "Se ha exportado los registro.");

            String nombreSugerido = "Cartola_Masiva_" + txtRutCliente.getText().trim() + "_" + System.currentTimeMillis();
            utilidades.ExcelUtil.exportarTablaAExcel(tblDetalle, nombreSugerido);
        }
    }

    private void botonBuscar() {
        String rutBuscado = txtRutCliente.getText().trim();
        if (rutBuscado.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un RUT para buscar.");
            return;
        }

        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        Object[] datosCliente = daoCc.buscarClientePorRut(rutBuscado);

        if (datosCliente != null) {
            this.clienteIdSeleccionado = (Integer) datosCliente[0];
            txtNombre.setText((String) datosCliente[1]);
        } else {
            this.clienteIdSeleccionado = 0;
            txtNombre.setText("");
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Cliente no encontrado.");
        }
    }

    private void botonImprimir() {
        utilidades.ReporteUtil.generarPDFCartolaClientePorVentas(
                tblDetalle,
                txtNombre.getText().trim(),
                txtRutCliente.getText().trim(),
                jdcFechaIni,
                jdcFechaFin,
                lblTotalFac,
                lblTotalAbonos,
                lblSaldo
        );
    }

    private void botonLimpiar() {
        // 1. Limpiar campos de texto y variable de ID
        txtRutCliente.setText("");
        txtNombre.setText("");
        this.clienteIdSeleccionado = 0;

        // 2. Limpiar selectores de fecha (puedes dejarlos en null)
        jdcFechaIni.setDate(null);
        jdcFechaFin.setDate(null);

        // 3. Limpiar la tabla de detalles
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0);

        // 4. Reiniciar etiquetas de totales
        lblTotalFac.setText("$ 0");
        lblTotalAbonos.setText("$ 0");
        lblSaldo.setText("$ 0");

        // 5. Enfocar el cursor nuevamente en el RUT para una nueva consulta rápida
        txtRutCliente.requestFocus();
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

    private void configuaracionVentana() {
        this.setTitle("CTACTE / CARTOLA DE CLIENTE POR VENTAS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void configurarTablaDetalle() {
        javax.swing.table.DefaultTableModel modeloTabla = new javax.swing.table.DefaultTableModel(
                new Object[]{"Fecha", "Documento", "N° Doc", "Cargo ($)", "Abono ($)", "Saldo ($)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Evita que editen las celdas directamente
            }
        };

        tblDetalle.setModel(modeloTabla);

        // Opcional: Ajustar ancho de columnas si lo deseas
        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(90);  // Fecha
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(120); // Documento
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(100); // N° Doc
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(100); // Cargo
        tblDetalle.getColumnModel().getColumn(4).setPreferredWidth(100); // Abono
        tblDetalle.getColumnModel().getColumn(5).setPreferredWidth(100); // Saldo
    }

    private int obtenerClienteIdActual() {
        return this.clienteIdSeleccionado;
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtRutCliente = new javax.swing.JTextField();
        jdcFechaIni = new com.toedter.calendar.JDateChooser();
        btnBuscar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jdcFechaFin = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblSaldo = new javax.swing.JLabel();
        lblTotalFac = new javax.swing.JLabel();
        lblTotalAbonos = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA DE CLIENTE"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("R.U.T. Cliente:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel2.setText("Fecha Inicial:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 60, -1, -1));

        txtRutCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtRutClienteKeyPressed(evt);
            }
        });
        jPanel2.add(txtRutCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(102, 30, 250, -1));
        jPanel2.add(jdcFechaIni, new org.netbeans.lib.awtextra.AbsoluteConstraints(102, 60, 250, -1));

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel2.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 25, -1, -1));

        jLabel3.setText("Nombre:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(415, 30, -1, -1));

        jLabel4.setText("Fecha Final:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(395, 60, -1, -1));
        jPanel2.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 30, 250, -1));
        jPanel2.add(jdcFechaFin, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 60, 250, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        jPanel2.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 100, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 40, 740, 160));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE VENTAS Y SALDOS"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tblDetalle);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(13, 20, 715, 190));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 220, 740, 220));

        jLabel5.setText("Total Facturado:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 460, -1, -1));

        jLabel6.setText("Total Abonos:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(188, 460, -1, -1));

        jLabel7.setText("SALDO DEUDA:");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 460, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 470, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 470, -1, -1));

        lblSaldo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSaldo.setForeground(new java.awt.Color(0, 102, 204));
        lblSaldo.setText("0");
        pnlPrincipal.add(lblSaldo, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 480, -1, -1));

        lblTotalFac.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalFac.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalFac.setText("0");
        pnlPrincipal.add(lblTotalFac, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 480, -1, -1));

        lblTotalAbonos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalAbonos.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalAbonos.setText("0");
        pnlPrincipal.add(lblTotalAbonos, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 480, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("CTACTE / CARTOLA DE CLIENTE POR VENTAS");
        pnlPrincipal.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 15, -1, -1));

        btnLimpiar.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        btnLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/borrar (1).png"))); // NOI18N
        btnLimpiar.setText("LIMPIAR");
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setContentAreaFilled(false);
        btnLimpiar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        pnlPrincipal.add(btnLimpiar, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 200, -1, 20));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        utilidades.ExcelUtil.exportarTablaAExcel(tblDetalle, "Cartola_Cliente" + txtRutCliente.getText().trim());
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void txtRutClienteKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtRutClienteKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            btnBuscarActionPerformed(null);
        }
    }//GEN-LAST:event_txtRutClienteKeyPressed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        botonLimpiar();
    }//GEN-LAST:event_btnLimpiarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jdcFechaFin;
    private com.toedter.calendar.JDateChooser jdcFechaIni;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblTotalAbonos;
    private javax.swing.JLabel lblTotalFac;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRutCliente;
    // End of variables declaration//GEN-END:variables
}
