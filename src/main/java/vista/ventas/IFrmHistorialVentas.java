package vista.ventas;

import dao.VentaDAO;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Venta;

/**
 *
 * @author caill
 */
public class IFrmHistorialVentas extends javax.swing.JInternalFrame {

    private final VentaDAO ventaDAO = new VentaDAO();
    private List<Venta> listaVentas;

    public IFrmHistorialVentas() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        configuracionVentana();
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

    private void botonVolver() {
        // 1. Cerrar (destruir) el marco interno del historial
        this.dispose();

        // 2. Si la ventana de Ingreso de Ventas ya está abierta en el fondo,
        // le devolvemos el foco y la traemos al frente:
        javax.swing.JDesktopPane desktopPane = getDesktopPane();
        if (desktopPane != null) {
            for (javax.swing.JInternalFrame frame : desktopPane.getAllFrames()) {
                if (frame instanceof IFrmIngresoVentas) {
                    try {
                        frame.setSelected(true);
                        frame.toFront();
                    } catch (java.beans.PropertyVetoException e) {
                    }
                    break;
                }
            }
        }
    }

    private void botonBuscar() {
        String textoBuscado = txtFolio.getText().trim();

        if (textoBuscado.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un N° de folio para buscar");
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0); // Limpiar filas

        List<Venta> lista = ventaDAO.listarVentas();
        boolean encontrado = false;

        for (Venta v : lista) {
            String folioStr = String.valueOf(v.getFolio());
            String numDocStr = v.getNumeroDocumento() != null ? v.getNumeroDocumento() : "";
            String idStr = String.valueOf(v.getId());

            // Compara por Folio numérico, Número de Documento o ID
            if (folioStr.equals(textoBuscado) || numDocStr.equalsIgnoreCase(textoBuscado)
                    || numDocStr.contains(textoBuscado) || idStr.equals(textoBuscado)) {

                Object folioAMostrar = (!numDocStr.isEmpty()) ? numDocStr : (v.getFolio() > 0 ? v.getFolio() : v.getId());

                modelo.addRow(new Object[]{
                    v.getId(),
                    folioAMostrar,
                    v.getTipoDocumento(),
                    v.getFechaEmision(),
                    v.getNombreVendedor(),
                    v.getFormaPago(),
                    String.format("%.0f", v.getTotal()),
                    v.getEstado()
                });
                encontrado = true;
            }
        }

        if (!encontrado) {
            utilidades.AlertaUtil.mostrarError(this, "No se encontró la venta con el Folio/Doc N°");
        }
    }

    private void botonMostrar() {
        txtFolio.setText("");
        cargarTabla();
    }

    private void botonVerDetalle() {
        int fila = jTable1.getSelectedRow();
        if (fila == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione una venta de la tabla.");
            return;
        }

        // Obtener el ID desde la fila seleccionada
        int idVenta = Integer.parseInt(jTable1.getValueAt(fila, 0).toString());

        // Buscar la Venta completa en el DAO
        VentaDAO dao = new VentaDAO();
        Venta ventaSeleccionada = dao.obtenerVentaPorId(idVenta);

        if (ventaSeleccionada == null) {
            utilidades.AlertaUtil.mostrarError(this, "No se encontró la venta con ID");
            return;
        }

        // Obtener la ventana padre real
        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);

        DFrmDetalleVenta dialog = new DFrmDetalleVenta(owner, ventaSeleccionada);
        dialog.setVisible(true);
    }

    private void botonExcel() {
        // 1. Validar que la tabla tenga datos
        if (jTable1.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarError(this, "No hay datos en la tabla para exportar.");
            return;
        }

        // 2. Configurar el guardado del archivo
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Historial de Ventas");
        fileChooser.setSelectedFile(new java.io.File("Historial_Ventas.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                fileToSave = new java.io.File(filePath + ".csv");
            }

            // 3. Generar CSV con codificación compatible con Excel en español
            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(fileToSave), "ISO-8859-1"))) {

                javax.swing.table.TableModel model = jTable1.getModel();

                // Escribir los encabezados de la tabla
                for (int i = 0; i < model.getColumnCount(); i++) {
                    bw.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        bw.write(";"); // Punto y coma para separar columnas en Excel
                    }
                }
                bw.newLine();

                // Escribir las filas de datos
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        String texto = (val != null) ? val.toString().replace(";", ",") : "";
                        bw.write(texto);
                        if (j < model.getColumnCount() - 1) {
                            bw.write(";");
                        }
                    }
                    bw.newLine();
                }

                // 1. Primero mostramos el mensaje de éxito y la pregunta usando un confirmador moderno
                boolean abrirExcel = utilidades.AlertaUtil.mostrarConfirmacion(
                        "Exportación Exitosa",
                        "¡Historial exportado con éxito!\n¿Desea abrir el archivo en Excel?"
                );

                // 2. Si el usuario acepta (clic en Sí), abrimos el archivo con Desktop
                if (abrirExcel) {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(fileToSave);
                    }
                }

            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error al guardar el archivo: " + e.getMessage(),
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void colorBotones() {
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);

//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);

        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);

        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnMostrarTodos, grisSuave);
        aplicarEfectoHoverPlano(btnBuscar, azulSuave);
        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnVerDetalleVenta, naranjaSuave);
    }

    private void configuracionVentana() {
        this.setTitle("HISTORIAL DE VENTAS REALIZADAS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    public void cargarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Folio", "Tipo Documento", "Fecha", "Vendedor", "Forma Pago", "Total ($)", "Est."}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        this.listaVentas = ventaDAO.listarVentas();

        for (Venta v : listaVentas) {
            // Validamos si viene número de documento o folio
            Object folioAMostrar = (v.getNumeroDocumento() != null && !v.getNumeroDocumento().isEmpty())
                    ? v.getNumeroDocumento()
                    : (v.getFolio() > 0 ? v.getFolio() : v.getId()); // Si todo es 0, muestra el ID por mientras

            modelo.addRow(new Object[]{
                v.getId(),
                folioAMostrar,
                v.getTipoDocumento(),
                v.getFechaEmision(),
                v.getNombreVendedor(),
                v.getFormaPago(),
                String.format("%.0f", v.getTotal()),
                v.getEstado()
            });
        }

        jTable1.setModel(modelo);
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtFolio = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        btnMostrarTodos = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnVerDetalleVenta = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnExcel = new javax.swing.JButton();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("FILTROS DE BÚSQUEDA");
        pnlPrincipal.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, -1, -1));

        jLabel2.setText("N° Folio:");
        pnlPrincipal.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 100, -1, -1));
        pnlPrincipal.add(txtFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 100, 300, -1));

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setText("BUSCAR");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        pnlPrincipal.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 90, -1, -1));

        btnMostrarTodos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMostrarTodos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/actualizar.png"))); // NOI18N
        btnMostrarTodos.setText("MOSTRAR TODOS");
        btnMostrarTodos.setBorderPainted(false);
        btnMostrarTodos.setContentAreaFilled(false);
        btnMostrarTodos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMostrarTodos.setFocusPainted(false);
        btnMostrarTodos.addActionListener(this::btnMostrarTodosActionPerformed);
        pnlPrincipal.add(btnMostrarTodos, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 85, -1, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Folio", "Tipo Documento", "Fecha", "Vendedor", "Forma Pago", "Total ($)", "Est."
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        pnlPrincipal.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 140, 720, 300));

        btnVerDetalleVenta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVerDetalleVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/informacion.png"))); // NOI18N
        btnVerDetalleVenta.setText("VER DETALLE DE VENTA");
        btnVerDetalleVenta.setBorderPainted(false);
        btnVerDetalleVenta.setContentAreaFilled(false);
        btnVerDetalleVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVerDetalleVenta.setFocusPainted(false);
        btnVerDetalleVenta.addActionListener(this::btnVerDetalleVentaActionPerformed);
        pnlPrincipal.add(btnVerDetalleVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 450, -1, -1));

        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVolver.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/volver-flecha.png"))); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(this::btnVolverActionPerformed);
        pnlPrincipal.add(btnVolver, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 450, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        botonVolver();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnMostrarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMostrarTodosActionPerformed
        botonMostrar();
    }//GEN-LAST:event_btnMostrarTodosActionPerformed

    private void btnVerDetalleVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerDetalleVentaActionPerformed
        botonVerDetalle();
    }//GEN-LAST:event_btnVerDetalleVentaActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnMostrarTodos;
    private javax.swing.JButton btnVerDetalleVenta;
    private javax.swing.JButton btnVolver;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtFolio;
    // End of variables declaration//GEN-END:variables

}
