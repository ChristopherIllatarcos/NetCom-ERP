package vista.compras;

import dao.CompraDAO;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author caill
 */
public class IFrmListadoCompras extends javax.swing.JInternalFrame {

    private DefaultTableModel modeloTablaListado;
    private final CompraDAO compraDAO = new CompraDAO();

// Formateador para pesos chilenos ($ 1.428.000)
    private final DecimalFormatSymbols simbolos = new DecimalFormatSymbols() {
        {
            setGroupingSeparator('.');
        }
    };
    private final DecimalFormat formatoMoneda = new DecimalFormat("$ #,##0", simbolos);

    public IFrmListadoCompras() {
        initComponents();
        colorBontones();
        quitarBordeBarra();
        modeloPersonalizado();
        ConfiguracionVentana();
    }

    private void modeloPersonalizado() {
        // 1. Inicializamos tu modelo personalizado primero
        modeloTablaListado = new DefaultTableModel(
                new Object[]{"N° Factura", "RUT Prov.", "Nombre Proveedor", "Tipo Doc.", "Fecha", "Total"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // 2. Se lo asignamos a la tabla
        jTable1.setModel(modeloTablaListado);

        // 3. Limpiamos las fechas para que no filtren por defecto al abrir
        jDateChooserFechaDesde.setDate(null);
        jDateChooserHasta.setDate(null);

        // 4. Cargamos todo el listado inicial usando el modelo correcto
        CompraDAO dao = new CompraDAO();
        dao.listarComprasFiltradas(modeloTablaListado, "", null, null);

        // 5. Ejecutamos la verificación automática de los 100 registros usando 'JInternalFrameListadoCompras.this'
        boolean limpiezaRealizada = dao.verificarYLimpiarHistorialCompras(this, modeloTablaListado);

        if (limpiezaRealizada) {
            System.out.println("El historial superó los 100 registros, se exportó y se vació correctamente.");
        }
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior

    }

    private void colorBontones() {
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);
        aplicarEfectoHoverPlano(btnFiltrarCompra, azulSuave);

    }

    private void ConfiguracionVentana() {
        this.setTitle("HISTORIAL Y CONSULTA DE COMPRAS");
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

    private void botonCancelar() {
        this.dispose();
    }

    private void botonFiltrarCompra() {
        String texto = txtFolioRut.getText().trim();
        java.util.Date desde = jDateChooserFechaDesde.getDate();
        java.util.Date hasta = jDateChooserHasta.getDate();

        compraDAO.listarComprasFiltradas(modeloTablaListado, texto, desde, hasta);
    }

    private void botonExcel() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte como Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("ListadoCompras.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoToSave = fileChooser.getSelectedFile();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(archivoToSave, "ISO-8859-1")) {
                javax.swing.table.TableModel modelo = jTable1.getModel();

                // 1. Escribir las cabeceras de las columnas
                for (int i = 0; i < modelo.getColumnCount(); i++) {
                    pw.print(modelo.getColumnName(i) + (i == modelo.getColumnCount() - 1 ? "" : ";"));
                }
                pw.println();

                // 2. Escribir todas las filas de la tabla
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    for (int j = 0; j < modelo.getColumnCount(); j++) {
                        Object valor = modelo.getValueAt(i, j);
                        pw.print((valor != null ? valor.toString() : "") + (j == modelo.getColumnCount() - 1 ? "" : ";"));
                    }
                    pw.println();
                }

                utilidades.AlertaUtil.mostrarExito(this, "¡Archivo exportado con éxito!\nPuedes abrirlo directamente con Excel.");

            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error al exportar el archivo: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
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
        txtFolioRut = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserFechaDesde = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jDateChooserHasta = new com.toedter.calendar.JDateChooser();
        btnFiltrarCompra = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnExcel = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Buscar por Folio o RUT:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(24, 36, 150, -1));
        jPanel1.add(txtFolioRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 33, 530, -1));

        jLabel2.setText("Fecha Desde:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(78, 73, -1, -1));
        jPanel1.add(jDateChooserFechaDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 73, 530, -1));

        jLabel3.setText("Hasta");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(117, 113, -1, -1));
        jPanel1.add(jDateChooserHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 113, 530, -1));

        btnFiltrarCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnFiltrarCompra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnFiltrarCompra.setText("FILTRAR COMPRA");
        btnFiltrarCompra.setBorderPainted(false);
        btnFiltrarCompra.setContentAreaFilled(false);
        btnFiltrarCompra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFiltrarCompra.setFocusPainted(false);
        btnFiltrarCompra.addActionListener(this::btnFiltrarCompraActionPerformed);
        jPanel1.add(btnFiltrarCompra, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 145, -1, 30));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 730, 190));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE COMPRAS REGISTRADAS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 18, 705, 192));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 230, 730, 220));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText(" EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 460, -1, -1));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 460, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("HISTORIAL Y CONSULTA DE COMPRAS");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 12, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        botonCancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnFiltrarCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarCompraActionPerformed
        botonFiltrarCompra();
    }//GEN-LAST:event_btnFiltrarCompraActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnFiltrarCompra;
    private com.toedter.calendar.JDateChooser jDateChooserFechaDesde;
    private com.toedter.calendar.JDateChooser jDateChooserHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtFolioRut;
    // End of variables declaration//GEN-END:variables
}
