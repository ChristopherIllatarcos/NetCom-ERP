package vista.bodegas;

import dao.BodegasDAO;

public class IFrmDetalleProductos extends javax.swing.JInternalFrame {

    private javax.swing.table.DefaultTableModel modeloHistorial;

    public IFrmDetalleProductos() {
        initComponents();

        colorBotones();
        quitarBarraBordes();
        configuracionVentana();
        cargarTablaHistorial();
        detectarEnter();

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

    private void botonEditar() {
        int filaSeleccionada = tblDetalleHistorial.getSelectedRow();

        if (filaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, seleccione un registro de la tabla para editar.");
            return;
        }

        // 1. Extraemos los datos básicos de la tabla
        int idCompra = Integer.parseInt(modeloHistorial.getValueAt(filaSeleccionada, 0).toString());
        String folioActual = modeloHistorial.getValueAt(filaSeleccionada, 1).toString();
        java.util.Date fechaActual = (java.util.Date) modeloHistorial.getValueAt(filaSeleccionada, 2);

        Object valDesc = modeloHistorial.getValueAt(filaSeleccionada, 3);
        String descActual = (valDesc != null) ? valDesc.toString() : "";

        Object valObs = modeloHistorial.getValueAt(filaSeleccionada, 6);
        String obsActual = (valObs != null) ? valObs.toString() : "";

        // 2. Consultamos la cantidad y el costo reales desde el DAO
        dao.BodegasDAO bodegasDao = new dao.BodegasDAO();
        Object[] detalleDatos = bodegasDao.obtenerPrimerDetalleCompra(idCompra);
        int cantActual = (int) detalleDatos[0];
        double costoActual = (double) detalleDatos[1];

        // 3. Instanciamos nuestro JDialog personalizado (DfrmEditarCompra)
        java.awt.Frame parentFrame = javax.swing.JOptionPane.getFrameForComponent(this);
        vista.bodegas.DFrmEditarCompra dialogo = new vista.bodegas.DFrmEditarCompra(parentFrame, true);

        dialogo.cargarDatos(folioActual, fechaActual, descActual, cantActual, costoActual, obsActual);

        // Lo hacemos visible
        dialogo.setVisible(true);

        // 4. Si el usuario presionó el botón EDITAR / GUARDAR en el JDialog
        if (dialogo.isConfirmado()) {
            try {

                String nuevoFolio = dialogo.getFolio();
                java.util.Date nuevaFecha = dialogo.getFecha();
                String nuevaDesc = dialogo.getDescripcion();
                String nuevaObs = dialogo.getObservacion();

                int nuevaCant = Integer.parseInt(dialogo.getCantidadStr());
                double nuevoCosto = Double.parseDouble(dialogo.getSubTotalStr().replace(",", "."));

                if (nuevoFolio.isEmpty() || nuevaFecha == null) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "El folio y la fecha son obligatorios.");
                    return;
                }

                dao.BodegasDAO bDao = new dao.BodegasDAO();
                boolean actualizado = bDao.actualizarIngresoCompleto(idCompra, nuevoFolio, nuevaFecha, nuevaObs, nuevaDesc, nuevaCant, nuevoCosto);

                if (actualizado) {
                    utilidades.AlertaUtil.mostrarExito(this, "¡Registro y detalle actualizados con éxito!");
                    cargarTablaHistorial(); // Refrescamos la tabla al instante
                } else {
                    utilidades.AlertaUtil.mostrarError(this, "Error al actualizar en la base de datos.");
                }

            } catch (NumberFormatException e) {
                utilidades.AlertaUtil.mostrarError(this, "Ingrese números válidos para la cantidad y el subtotal/costo.");
            }
        }
    }

    private void botonExcel() {
        if (tblDetalleHistorial.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No hay registros en la tabla para exportar.",
                    "Tabla Vacía",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Llamamos al método exportador
        exportarTablaAExcel(tblDetalleHistorial);
    }

    private void colorBotones() {
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnExcel, verdeSuave);

    }

    private void configuracionVentana() {
        this.setTitle(" HISTORIAL DE INGRESOS DE MERCADERÍA ");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarTablaHistorial() {
        modeloHistorial = new javax.swing.table.DefaultTableModel(
                new Object[]{"ID", "N° FOLIO / MOV", "FECHA", "DESCRIPCIÓN", "CANTIDAD", "SUBTOTAL", "OBSERVACIONES"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura en la tabla principal
            }
        };
        tblDetalleHistorial.setModel(modeloHistorial);
        modeloHistorial.setRowCount(0);

        // Llamada limpia al DAO recibiendo la lista de DTOs fuertemente tipados
        dao.BodegasDAO bodegasDao = new dao.BodegasDAO();
        java.util.List<modelo.HistorialIngresoDTO> lista = bodegasDao.listarIngresosParaHistorial();

        for (modelo.HistorialIngresoDTO item : lista) {
            Object[] fila = new Object[]{
                item.getId(),
                item.getNumeroFactura(),
                item.getFechaEmision(),
                item.getDescripciones(),
                item.getCantidadTotal(),
                item.getSubtotal(),
                item.getObservaciones()
            };
            modeloHistorial.addRow(fila);
        }

        // Actualizamos el label con el total de registros encontrados
        lblTotalRegistros.setText(String.valueOf(modeloHistorial.getRowCount()));
    }

    private void detectarEnter() {
        // --- 1. DETECTAR ENTER EN EL TXTFOLIO ---
        txtFolio.addActionListener((java.awt.event.ActionEvent evt) -> {
            filtrarHistorial();
        });

        // --- 2. DETECTAR CAMBIO O SELECCIÓN EN EL DTCFECHA ---
        dtcFecha.getDateEditor().addPropertyChangeListener((java.beans.PropertyChangeEvent evt) -> {
            if ("date".equals(evt.getPropertyName())) {
                // Se ejecuta automáticamente al seleccionar o cambiar la fecha en el calendario
                filtrarHistorial();
            }
        });
    }

    private void exportarTablaAExcel(javax.swing.JTable tabla) {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Historial como Excel");
        fileChooser.setSelectedFile(new java.io.File("Historial_Ingresos_Mercaderia.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivoAguardar = fileChooser.getSelectedFile();

            // Si el usuario no ingresó la extensión, se la agregamos
            String filePath = archivoAguardar.getAbsolutePath();
            if (!filePath.endsWith(".csv") && !filePath.endsWith(".xls")) {
                archivoAguardar = new java.io.File(filePath + ".csv");
            }

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(archivoAguardar), "UTF-8"))) {

                javax.swing.table.TableModel model = tabla.getModel();

                // 1. Escribir los encabezados de las columnas
                for (int i = 0; i < model.getColumnCount(); i++) {
                    bw.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        bw.write(";"); // Separador compatible con Excel
                    }
                }
                bw.newLine();

                // 2. Escribir las filas de datos
                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        Object val = model.getValueAt(r, c);
                        bw.write(val != null ? val.toString().replace(";", ",") : "");
                        if (c < model.getColumnCount() - 1) {
                            bw.write(";");
                        }
                    }
                    bw.newLine();
                }

                javax.swing.JOptionPane.showMessageDialog(this,
                        "¡Historial exportado con éxito a!\n" + archivoAguardar.getAbsolutePath(),
                        "Exportación Exitosa",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } catch (java.io.IOException e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Error al exportar el archivo: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filtrarHistorial() {
        String folioBuscado = txtFolio.getText().trim();
        java.util.Date fechaBuscada = dtcFecha.getDate(); // Asumiendo que tu JDateChooser se llama dtcFecha

        // Llamas a tu DAO pasando estos filtros para refrescar la tabla
        dao.BodegasDAO bodegasDao = new dao.BodegasDAO();
        java.util.List<Object[]> lista = bodegasDao.listarIngresosFiltrados(folioBuscado, fechaBuscada);

        modeloHistorial.setRowCount(0);
        for (Object[] fila : lista) {
            modeloHistorial.addRow(fila);
        }
        lblTotalRegistros.setText(String.valueOf(modeloHistorial.getRowCount()));
    }

    private void quitarBarraBordes() {
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtFolio = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        dtcFecha = new com.toedter.calendar.JDateChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalleHistorial = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        lblTotalRegistros = new javax.swing.JLabel();
        btnEditar = new javax.swing.JButton();
        btnVolver = new javax.swing.JButton();
        btnExcel = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Filtro / Buscar pot Folio o Fecha:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));
        jPanel2.add(txtFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 40, 240, -1));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 35, -1, 30));
        jPanel2.add(dtcFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 40, 240, -1));

        tblDetalleHistorial.setModel(new javax.swing.table.DefaultTableModel(
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
        tblDetalleHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDetalleHistorial);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 720, 270));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 740, 370));

        jLabel2.setText("Total Registros:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, -1, -1));

        lblTotalRegistros.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalRegistros.setText("0");
        jPanel1.add(lblTotalRegistros, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 470, -1, -1));

        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnEditar.setText("EDITAR");
        btnEditar.setBorderPainted(false);
        btnEditar.setContentAreaFilled(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 470, -1, -1));

        btnVolver.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVolver.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/volver-flecha.png"))); // NOI18N
        btnVolver.setText("VOLVER");
        btnVolver.setBorderPainted(false);
        btnVolver.setContentAreaFilled(false);
        btnVolver.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVolver.setFocusPainted(false);
        btnVolver.addActionListener(this::btnVolverActionPerformed);
        jPanel1.add(btnVolver, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        jPanel1.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 470, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("HISTORIAL DE INGRESOS DE MERCADERÍA");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 50, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, -30, 800, 590));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnVolver;
    private com.toedter.calendar.JDateChooser dtcFecha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JTable tblDetalleHistorial;
    private javax.swing.JTextField txtFolio;
    // End of variables declaration//GEN-END:variables
}
