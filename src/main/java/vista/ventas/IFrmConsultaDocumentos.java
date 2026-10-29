package vista.ventas;

/**
 *
 * @author caill
 */
public class IFrmConsultaDocumentos extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameConsultaDocumentos
     */
    public IFrmConsultaDocumentos() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        cargarComboTiposDocumento();
        cmbTipo.setSelectedIndex(0);
        utilidades();
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

    private void buscarDocumento() {
        String tipoDoc = cmbTipo.getSelectedItem() != null ? cmbTipo.getSelectedItem().toString() : "";
        String folioTexto = txtFolio.getText().trim();
        String rutCliente = txtRutCliente.getText().trim();

        // Validar que se ingrese al menos un criterio de búsqueda
        if ((cmbTipo.getSelectedIndex() <= 0) && folioTexto.isEmpty() && rutCliente.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese al menos un criterio de búsqueda (Tipo Doc, Folio o RUT).");
            return;
        }

        int folio = 0;
        if (!folioTexto.isEmpty()) {
            try {
                folio = Integer.parseInt(folioTexto);
            } catch (NumberFormatException e) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "El N° de Folio debe ser un valor numérico.");
                return;
            }
        }

        // Buscar en la base de datos usando la nueva clase ConsultaDocumentosDAO
        dao.ConsultaDocumentosDAO daoConsulta = new dao.ConsultaDocumentosDAO();
        modelo.Venta venta = daoConsulta.buscarVentaPorDocumento(tipoDoc, folio, rutCliente);

        if (venta != null) {
            // 1. Asignar datos a la Cabecera del Documento
            txtNroFolio.setText(String.valueOf(venta.getFolio()));
            jDateChooserFecha.setDate(venta.getFecha());
            txtCondicion.setText(venta.getCondicionPago());
            txtCliente.setText(venta.getNombreCliente());
            txtRut.setText(venta.getRutCliente());

            // 2. Cargar Detalle de Productos usando venta.getId()
            java.util.List<modelo.DetalleVenta> detalles = daoConsulta.obtenerDetalleVenta(venta.getId());
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
            model.setRowCount(0); // Limpiar filas anteriores

            // Configuración del formateador de miles con signo $
            java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "CL"));
            simbolos.setGroupingSeparator('.');
            java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0", simbolos);

            for (modelo.DetalleVenta d : detalles) {
                double totalLinea = d.getCantidad() * d.getPrecioUnitario();
                model.addRow(new Object[]{
                    d.getCodigo(),
                    d.getDescripcion(),
                    d.getCantidad(),
                    df.format(d.getPrecioUnitario()), // Formatear precio unitario
                    df.format(totalLinea) // Formatear total de la línea
                });
            }

            // 3. Asignar Totales usando el formateador 'df'
            lblNeto.setText(df.format(venta.getNeto()));
            lblIva.setText(df.format(venta.getIva()));
            lblTotal.setText(df.format(venta.getTotal()));
        }

    }

    private void botonAgregar() {
        // Creamos un diálogo emergente o ventana de gestión rápida   
        String[] opciones = {"Nuevo", "Editar", "Eliminar", "Cancelar"};
        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Tipos de Documento",
                "Seleccione qué acción desea realizar con los Tipos de Documento:",
                opciones
        );

        dao.TipoDocumentoDAO tipoDao = new dao.TipoDocumentoDAO();

        switch (seleccion) {
            case 0 -> {
                
                String nuevoTipo = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Nuevo Tipo de Documento",
                        "Ingrese el nombre del nuevo Tipo de Documento:",
                        ""
                );

                if (nuevoTipo != null && !nuevoTipo.trim().isEmpty()) {
                    nuevoTipo = nuevoTipo.trim().toUpperCase();
                    String codigo = nuevoTipo.length() >= 3 ? nuevoTipo.substring(0, 3).toUpperCase() : "DOC";

                    if (tipoDao.agregarTipoDocumento(codigo, nuevoTipo)) {
                        utilidades.AlertaUtil.mostrarExito(this, "¡Registrado con éxito!");
                        cargarComboTiposDocumento();
                        cmbTipo.setSelectedItem(nuevoTipo);
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "Error al registrar el tipo de documento.");
                    }
                }
            }

            case 1 -> {
                // EDITAR
                // Obtenemos los tipos actuales para que el usuario elija cuál editar
                java.util.List<String> tiposActuales = tipoDao.listarTiposDocumentoActivos();
                if (tiposActuales.isEmpty()) {
                    utilidades.AlertaUtil.mostrarError(this, "No hay tipos de documento para editar.");
                    return;
                }
                String aEditar = utilidades.AlertaUtil.mostrarSelector(
                        this,
                        "Editar Tipo",
                        "Seleccione el tipo a editar:",
                        tiposActuales.toArray(),
                        tiposActuales.get(0)
                );

                if (aEditar != null) {
                    String nuevoNombre = utilidades.AlertaUtil.mostrarInput(
                            this,
                            "Modificar Nombre",
                            "Ingrese el nuevo nombre:",
                            aEditar
                    );
                    if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                        nuevoNombre = nuevoNombre.trim().toUpperCase();
                        // Usamos tu método existente en el DAO para actualizar
                        if (tipoDao.actualizarTipoDocumento(aEditar, nuevoNombre)) {
                            utilidades.AlertaUtil.mostrarExito(this, "¡Actualizado con éxito!");
                            cargarComboTiposDocumento();
                        } else {
                            utilidades.AlertaUtil.mostrarError(this, "Error al actualizar.");
                        }
                    }
                }
            }

            case 2 -> {
                // ELIMINAR (Desactivar)
                java.util.List<String> tiposParaBorrar = tipoDao.listarTiposDocumentoActivos();
                if (tiposParaBorrar.isEmpty()) {
                    utilidades.AlertaUtil.mostrarError(this, "No hay tipos de documento disponibles.");
                    return;
                }
                String aBorrar = utilidades.AlertaUtil.mostrarSelector(
                        this,
                        "Eliminar Tipo",
                        "Seleccione el tipo a eliminar:",
                        tiposParaBorrar.toArray(),
                        tiposParaBorrar.get(0)
                );

                if (aBorrar != null) {
                    // 1. Usamos tu método de confirmación moderno (con título y mensaje)
                    boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                            "Confirmar Eliminación",
                            "¿Está seguro de eliminar el tipo: " + aBorrar + "?"
                    );

                    if (confirmar) {
                        // Usamos tu método existente en el DAO para borrado lógico
                        if (tipoDao.eliminarTipoDocumento(aBorrar)) {
                            // 2. Éxito moderno con borde verde
                            utilidades.AlertaUtil.mostrarExito(this, "¡Eliminado con éxito!");
                            cargarComboTiposDocumento();
                        } else {
                            // 3. Error moderno con borde rojo
                            utilidades.AlertaUtil.mostrarError(this, "Error al eliminar el tipo de documento.");
                        }
                    }
                }
            }

            default -> {
            }
        }
        // Cancelar 
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(224, 242, 254);

        java.awt.Color colorExcel = new java.awt.Color(209, 250, 229);

        aplicarEfectoHoverPlano(btnBuscar, azulSuave);

        aplicarEfectoHoverPlano(btnExcel, colorExcel);
    }

    private void configurarVentana() {
        this.setTitle("CONSULTA DE DOCUMENTOS DE VENTA");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void cargarComboTiposDocumento() {
        cmbTipo.removeAllItems();
        cmbTipo.addItem("SELECCIONE...");

        dao.TipoDocumentoDAO tipoDao = new dao.TipoDocumentoDAO();
        java.util.List<String> listaTipos = tipoDao.listarTiposDocumentoActivos();

        for (String tipo : listaTipos) {
            cmbTipo.addItem(tipo);
        }
    }

    private void limpiarPantalla() {
        txtNroFolio.setText("");
        jDateChooserFecha.setDate(null);
        txtCondicion.setText("");
        txtCliente.setText("");
        txtRut.setText("");
        lblNeto.setText("");
        lblIva.setText("");
        lblTotal.setText("");

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
    }

    private void procesarExcel() {
        if (jTable1.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay ningún documento cargado en pantalla para exportar.");
            return;
        }

        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Exportar Documento de Venta");
        fileChooser.setSelectedFile(new java.io.File("Documento_Folio_" + txtNroFolio.getText() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                fileToSave = new java.io.File(filePath + ".csv");
            }

            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(new java.io.FileOutputStream(fileToSave), "ISO-8859-1"))) {

                // Encabezado del documento
                bw.write("DOCUMENTO DE VENTA;FOLIO: " + txtNroFolio.getText());
                bw.newLine();
                bw.write("CLIENTE:;" + txtCliente.getText() + ";RUT:;" + txtRut.getText());
                bw.newLine();
                bw.write("CONDICION PAGO:;" + txtCondicion.getText());
                bw.newLine();
                bw.newLine();

                // Encabezados de la Tabla
                javax.swing.table.TableModel model = jTable1.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    bw.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        bw.write(";");
                    }
                }
                bw.newLine();

                // Filas del Detalle
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object val = model.getValueAt(i, j);
                        bw.write(val != null ? val.toString().replace(";", ",") : "");
                        if (j < model.getColumnCount() - 1) {
                            bw.write(";");
                        }
                    }
                    bw.newLine();
                }

                // Pie con Totales
                bw.newLine();
                bw.write("NETO:;" + lblNeto.getText());
                bw.newLine();
                bw.write("I.V.A. (19%):;" + lblIva.getText());
                bw.newLine();
                bw.write("TOTAL:;" + lblTotal.getText());
                bw.newLine();

                int respuesta = javax.swing.JOptionPane.showConfirmDialog(this,
                        "¡Documento exportado con éxito!\n¿Desea abrir el archivo en Excel?",
                        "Éxito", javax.swing.JOptionPane.YES_NO_OPTION);

                if (respuesta == javax.swing.JOptionPane.YES_OPTION) {
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

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior  
    }

    private void utilidades() {
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnBuscar, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnExcel, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
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
        cmbTipo = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFolio = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtRutCliente = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnAgregarTipo = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtNroFolio = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jDateChooserFecha = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        txtCondicion = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCliente = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtRut = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        lblTotal = new javax.swing.JLabel();
        lblNeto = new javax.swing.JLabel();
        lblIva = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Tipo Doc:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(36, 30, -1, -1));

        cmbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", " " }));
        cmbTipo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(cmbTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(104, 24, 280, -1));

        jLabel2.setText("Folio:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(442, 27, -1, -1));

        txtFolio.addActionListener(this::txtFolioActionPerformed);
        jPanel1.add(txtFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(489, 24, 200, -1));

        jLabel3.setText("R.U.T. Cliente:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 65, -1, -1));

        txtRutCliente.addActionListener(this::txtRutClienteActionPerformed);
        jPanel1.add(txtRutCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(103, 62, 280, -1));

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setText("BUSCAR DOCUMENTO");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnBuscarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnBuscarMouseExited(evt);
            }
        });
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel1.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 60, -1, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 90, 150, 3));

        btnAgregarTipo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnAgregarTipo.setBorderPainted(false);
        btnAgregarTipo.setContentAreaFilled(false);
        btnAgregarTipo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarTipo.setFocusPainted(false);
        btnAgregarTipo.addActionListener(this::btnAgregarTipoActionPerformed);
        jPanel1.add(btnAgregarTipo, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 20, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 730, 110));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("CABECERA DEL DOCUMENTO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText("N° Folio:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 27, -1, -1));
        jPanel2.add(txtNroFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 24, 160, -1));

        jLabel5.setText("Fecha:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 27, -1, -1));
        jPanel2.add(jDateChooserFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 24, 160, -1));

        jLabel6.setText("Condición:");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(471, 27, -1, -1));
        jPanel2.add(txtCondicion, new org.netbeans.lib.awtextra.AbsoluteConstraints(541, 24, 150, -1));

        jLabel7.setText("Cliente:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 61, -1, -1));
        jPanel2.add(txtCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 58, 380, -1));

        jLabel8.setText("RUT:");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 60, -1, -1));
        jPanel2.add(txtRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 60, 150, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, 730, 100));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DEL DOCUMENTO"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Cód. Prod.", "Descripción", "Cantidad", "P. Unitario", "Total ($)"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 27, 705, 130));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 270, 730, 170));

        jLabel9.setText("Netos:");
        pnlPrincipal.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 450, -1, -1));

        jLabel10.setText("I.V.A. (19%):");
        pnlPrincipal.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 450, -1, -1));

        jLabel11.setText("Total:");
        pnlPrincipal.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 450, -1, -1));

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
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 442, -1, -1));
        pnlPrincipal.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 470, 70, -1));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(0, 102, 204));
        lblTotal.setText("$");
        pnlPrincipal.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 450, -1, -1));

        lblNeto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNeto.setForeground(new java.awt.Color(0, 102, 204));
        lblNeto.setText("$");
        pnlPrincipal.add(lblNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 450, -1, -1));

        lblIva.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblIva.setForeground(new java.awt.Color(0, 102, 204));
        lblIva.setText("$");
        pnlPrincipal.add(lblIva, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 450, -1, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("CONSULTA DE DOCUMENTOS DE VENTA");
        pnlPrincipal.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 12, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnBuscarMouseEntered

    private void btnBuscarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnBuscarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnExcelMouseExited

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        buscarDocumento();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        procesarExcel();
        limpiarPantalla();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void txtRutClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRutClienteActionPerformed
        buscarDocumento();
    }//GEN-LAST:event_txtRutClienteActionPerformed

    private void txtFolioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFolioActionPerformed
        buscarDocumento();
    }//GEN-LAST:event_txtFolioActionPerformed

    private void btnAgregarTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoActionPerformed
        botonAgregar();
    }//GEN-LAST:event_btnAgregarTipoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarTipo;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JComboBox<String> cmbTipo;
    private com.toedter.calendar.JDateChooser jDateChooserFecha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblIva;
    private javax.swing.JLabel lblNeto;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtCliente;
    private javax.swing.JTextField txtCondicion;
    private javax.swing.JTextField txtFolio;
    private javax.swing.JTextField txtNroFolio;
    private javax.swing.JTextField txtRut;
    private javax.swing.JTextField txtRutCliente;
    // End of variables declaration//GEN-END:variables

}
