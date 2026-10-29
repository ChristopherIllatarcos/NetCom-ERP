package vista.compras;

import javax.swing.table.DefaultTableModel;
import dao.LibroComprasDAO;

/**
 *
 * @author caill
 */
public class IFrmLibroCompras extends javax.swing.JInternalFrame {

    public IFrmLibroCompras() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configuracionTabla();
        configuracionVentana();
    }

    private void configuracionTabla() {
        // 1. Configurar el modelo de la tabla para que no sea editable
        DefaultTableModel modeloTabla = new DefaultTableModel(
                new Object[]{"Fecha", "N° Fact.", "Proveedor / R. Social", "Neto ($)", "Iva ($)", "Total ($)"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblDetalleCompras.setModel(modeloTabla);

        // 2. Cargar los proveedores en el ComboBox al iniciar
        LibroComprasDAO dao = new LibroComprasDAO();
        dao.cargarProveedores((javax.swing.JComboBox) cmbProveedor);

        // 3. Limpiar selectores de fecha por defecto
        jDateChooserFechaInicio.setDate(null);
        jDateChooserFechaFinal.setDate(null);
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void colorBotones() {
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configuracionVentana() {
        this.setTitle("LIBRO / LISTADO DE COMPRAS");
        this.setSize(770, 540);
        this.setResizable(false);
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
        java.util.Date fechaInicio = jDateChooserFechaInicio.getDate();
        java.util.Date fechaFinal = jDateChooserFechaFinal.getDate();

        // Obtenemos el item seleccionado del ComboBox de proveedores
        LibroComprasDAO.ItemCombo itemProveedor = (LibroComprasDAO.ItemCombo) cmbProveedor.getSelectedItem();
        int proveedorId = (itemProveedor != null) ? itemProveedor.getId() : 0;

        DefaultTableModel modelo = (DefaultTableModel) tblDetalleCompras.getModel();
        modelo.setRowCount(0); // Limpiar tabla

        LibroComprasDAO dao = new LibroComprasDAO();

        // Llamamos al método que trae los datos y actualiza los labels de totales
        java.util.List<modelo.LibroCompra> lista = dao.consultarLibroCompras(
                fechaInicio, fechaFinal, proveedorId, lblTotalDoctos, lblTotalNeto, lblTtotalCompras
        );

        // Formato de moneda para la tabla
        java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        java.text.DecimalFormat formatoMoneda = new java.text.DecimalFormat("$ #,##0", simbolos);
        java.text.SimpleDateFormat formatoFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

        // Llenar la tabla con los resultados
        for (modelo.LibroCompra lc : lista) {
            Object[] fila = {
                lc.getFecha() != null ? formatoFecha.format(lc.getFecha()) : "",
                lc.getNumeroFactura(),
                lc.getProveedorRazonSocial(),
                formatoMoneda.format(lc.getNeto()),
                formatoMoneda.format(lc.getIva()),
                formatoMoneda.format(lc.getTotal())
            };
            modelo.addRow(fila);
        }
    }

    private void botonExcel() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Exportar Libro de Compras a Excel");
        fileChooser.setSelectedFile(new java.io.File("LibroDeCompras.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoToSave = fileChooser.getSelectedFile();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(archivoToSave, "ISO-8859-1")) {
                javax.swing.table.TableModel modelo = tblDetalleCompras.getModel();

                // Cabeceras
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    pw.print(modelo.getColumnName(i) + (i == modelo.getColumnCount() - 1 ? "" : ";"));
                }
                pw.println();

                // Filas
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        Object valor = modelo.getValueAt(i, j);
                        pw.print((valor != null ? valor.toString() : "") + (j == modelo.getColumnCount() - 1 ? "" : ";"));
                    }
                    pw.println();
                }

                javax.swing.JOptionPane.showMessageDialog(this,
                        "¡Libro de Compras exportado con éxito!",
                        "Exportación Exitosa",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error al exportar el archivo: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void botonImprimir() {
        utilidades.ReporteUtil.generarPDFLibroCompras(
                tblDetalleCompras,
                jDateChooserFechaInicio,
                jDateChooserFechaFinal,
                lblTotalDoctos,
                lblTotalNeto,
                lblTtotalCompras
        );
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
        jDateChooserFechaInicio = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserFechaFinal = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        cmbProveedor = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalleCompras = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        lblTotalDoctos = new javax.swing.JLabel();
        lblTotalNeto = new javax.swing.JLabel();
        lblTtotalCompras = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Fecha Inicial:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));
        jPanel1.add(jDateChooserFechaInicio, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, 250, -1));

        jLabel2.setText("Fecha Final:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(372, 30, -1, -1));
        jPanel1.add(jDateChooserFechaFinal, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 30, 250, -1));

        jLabel3.setText("Proveedor:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        cmbProveedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbProveedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 460, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 68, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 40, 735, 120));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DEL REGISTRO DE COMPRAS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDetalleCompras.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Fecha", "N° Fact.", "Proveedor / R. Social", "Neto ($)", "Total ($)"
            }
        ));
        tblDetalleCompras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDetalleCompras);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, 710, 220));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 170, 735, 250));

        jLabel4.setText("Total Doctos:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 440, -1, -1));

        jLabel5.setText("Total Neto:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(255, 440, -1, -1));

        jLabel6.setText("TOTAL COMPRAS:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 440, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 470, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 470, -1, -1));

        lblTotalDoctos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalDoctos.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalDoctos.setText("0");
        pnlPrincipal.add(lblTotalDoctos, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 440, -1, -1));

        lblTotalNeto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalNeto.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalNeto.setText("$");
        pnlPrincipal.add(lblTotalNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 440, -1, -1));

        lblTtotalCompras.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTtotalCompras.setForeground(new java.awt.Color(0, 102, 204));
        lblTtotalCompras.setText("$");
        pnlPrincipal.add(lblTtotalCompras, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 440, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("LIBRO / LISTADO DE COMPRAS");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 20, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

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
    private javax.swing.JComboBox<String> cmbProveedor;
    private com.toedter.calendar.JDateChooser jDateChooserFechaFinal;
    private com.toedter.calendar.JDateChooser jDateChooserFechaInicio;
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
    private javax.swing.JLabel lblTotalDoctos;
    private javax.swing.JLabel lblTotalNeto;
    private javax.swing.JLabel lblTtotalCompras;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDetalleCompras;
    // End of variables declaration//GEN-END:variables
}
