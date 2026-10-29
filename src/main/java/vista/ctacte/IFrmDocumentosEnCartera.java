package vista.ctacte;

import dao.CuentaCorrienteDAO;

public class IFrmDocumentosEnCartera extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameDocumentosEnCartera
     */
    public IFrmDocumentosEnCartera() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configuracionVentana();
        cargarCombosIniciales();
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
        // 1. Obtener el ID del cliente seleccionado usando ItemCombo
        modelo.ItemCombo itemCliente = (modelo.ItemCombo) cmbCliente.getSelectedItem();
        int clienteId = (itemCliente != null) ? itemCliente.getId() : 0;

        // 2. Obtener el estado seleccionado
        String estado = cmbEstadoDoc.getSelectedItem() != null ? cmbEstadoDoc.getSelectedItem().toString() : "TODOS";

        // 3. Obtener fechas de los JDateChooser
        java.util.Date fechaIni = jdcFechaInicial.getDate();
        java.util.Date fechaFin = jdcFechaFinal.getDate();

        // 4. Consultar al DAO
        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        java.util.List<Object[]> resultados = daoCc.consultarDocumentosCartera(clienteId, estado, fechaIni, fechaFin);

        // 5. Cargar datos en la tabla (tblDoctos) y calcular totales
        javax.swing.table.DefaultTableModel modeloTabla = (javax.swing.table.DefaultTableModel) tblDoctos.getModel();
        modeloTabla.setRowCount(0); // Limpiar tabla

        double sumaTotal = 0.0;
        int cantidadDocs = resultados.size();

        for (Object[] fila : resultados) {
            modeloTabla.addRow(fila);
            // El total (monto) está en el índice 7
            if (fila[7] != null) {
                sumaTotal += ((java.math.BigDecimal) fila[7]).doubleValue();
            }
        }

        // 6. Actualizar las etiquetas de totales (lblDoc y lblTotal)
        lblDoc.setText(String.valueOf(cantidadDocs));
        lblTotal.setText(String.format("$ %,.0f", sumaTotal));
    }

    private void botonExcel() {
        utilidades.ExcelUtil.exportarTablaAExcel(tblDoctos, "Documentos_En_Cartera");
    }

    private void botonImprimir() {
        utilidades.ReporteUtil.generarPDFDocumentosCartera(tblDoctos, lblDoc, lblTotal);
    }

    private void colorBotones() {
        // java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configuracionVentana() {
        this.setTitle("CTACTE / CARTERA DE CHEQUES Y DOCUMENTOS POR COBRAR");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarCombosIniciales() {
        // 1. Cargar clientes usando la clase importada directamente
        CuentaCorrienteDAO daoCc = new CuentaCorrienteDAO();
        daoCc.cargarClientesCombo(cmbCliente);

        // 2. Cargar estados de documentos de cartera
        cmbEstadoDoc.removeAllItems();
        cmbEstadoDoc.addItem("TODOS");
        cmbEstadoDoc.addItem("PENDIENTE");
        cmbEstadoDoc.addItem("VENCIDO");
        cmbEstadoDoc.addItem("PAGADO");
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
        cmbCliente = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jdcFechaInicial = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jdcFechaFinal = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        cmbEstadoDoc = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDoctos = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblDoc = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE CONSULTA"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Cliente:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 30, -1, -1));

        cmbCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cmbCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 610, -1));

        jLabel2.setText("Fecha Inicial:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));
        jPanel2.add(jdcFechaInicial, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 250, -1));

        jLabel3.setText("Fecha Final:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(365, 70, -1, -1));
        jPanel2.add(jdcFechaFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 70, 270, -1));

        jLabel4.setText("Estado Doc:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 110, -1, -1));

        cmbEstadoDoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cmbEstadoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 610, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        jPanel2.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 135, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 185));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DOCUMENTOS Y CHEQUES EN CARTERA"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDoctos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "RUT", "Nombre Cliente", "Tipo Doc", "Folio", "Fecha Emisión", "Fecha Vencimiento", "Total Monto", "Estado"
            }
        ));
        tblDoctos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDoctos);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 22, 720, 180));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 220, 740, 210));

        jLabel5.setText("Documentos:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 450, -1, -1));

        jLabel6.setText("TOTAL CARTERA:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 450, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 440, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 440, -1, -1));

        lblDoc.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblDoc.setForeground(new java.awt.Color(0, 102, 204));
        lblDoc.setText("0");
        pnlPrincipal.add(lblDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 450, -1, -1));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(0, 102, 204));
        lblTotal.setText("0");
        pnlPrincipal.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 450, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("CTACTE / CARTERA DE CHEQUES Y DOCUMENTOS POR COBRAR");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JComboBox<String> cmbEstadoDoc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jdcFechaFinal;
    private com.toedter.calendar.JDateChooser jdcFechaInicial;
    private javax.swing.JLabel lblDoc;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDoctos;
    // End of variables declaration//GEN-END:variables
}
