package vista.bodegas;

import java.awt.event.ActionEvent;
import java.util.List;

public class IFrmIngresoMercaderia extends javax.swing.JInternalFrame {

    private javax.swing.table.DefaultTableModel modeloTablaDetalle;

    public IFrmIngresoMercaderia() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        cargarComboTiposDocumento();
        configuracionVentana();
        inicializarTablaDetalle();
        asignarEventos();
        asignarEventoBtnAgregar();

    }

    private void asignarEventoBtnAgregar() {
        java.awt.event.ActionListener accionEnter = (ActionEvent e) -> {

            btnAgregar.doClick();
        };

        txtCantidad.addActionListener(accionEnter);
        txtDescripcion.addActionListener(accionEnter);
        btnGuardar.addActionListener(accionEnter);
    }

    private void asignarEventos() {
        txtCodProducto.addActionListener((ActionEvent e) -> {
            btnBuscarProducto.doClick();
        });
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

    private void botonAgregar() {
        String codigo = txtCodProducto.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String cantStr = txtCantidad.getText().trim();
        String costoStr = txtCostoUnit.getText().trim().replace(",", ".");

        if (codigo.isEmpty() || descripcion.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe buscar y seleccionar un producto válido.");
            txtCodProducto.requestFocus();
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantStr);
            double costoUnit = Double.parseDouble(costoStr);

            if (cantidad <= 0) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "La cantidad debe ser mayor a cero.");
                txtCantidad.requestFocus();
                return;
            }

            if (costoUnit < 0) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "El costo unitario no puede ser negativo.");
                txtCostoUnit.requestFocus();
                return;
            }

            // Cálculo del subtotal para esta línea
            double subtotal = cantidad * costoUnit;

            // Agregamos la fila a la tabla visual
            modeloTablaDetalle.addRow(new Object[]{
                codigo,
                descripcion,
                cantidad,
                String.format("%.2f", costoUnit),
                String.format("%.2f", subtotal)
            });

            // Actualizamos los acumuladores de abajo
            calcularTotalesGenerales();

            // Limpiamos los campos del producto para dejar listo el ingreso del siguiente item
            txtCodProducto.setText("");
            txtDescripcion.setText("");
            txtCantidad.setText("1");
            txtCostoUnit.setText("0.00");
            txtCodProducto.requestFocus();

        } catch (NumberFormatException e) {
            utilidades.AlertaUtil.mostrarError(this, "Verifique que la cantidad y el costo sean números válidos.");
        }
    }

    private void botonGuardar() {
        if (modeloTablaDetalle.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe agregar al menos un producto al detalle del ingreso.");
            return;
        }

        if (jDateChooserFechaIngreso.getDate() == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe agregar al menos un producto al detalle del ingreso.");
            return;
        }

        String folio = txtFolioMov.getText().trim();
        String observacion = txtObservacion.getText().trim();
        java.util.Date fecha = jDateChooserFechaIngreso.getDate();

        // Obtenemos el item seleccionado del combo de motivos
        dao.TiposDocumentoDAO.ItemCombo motivoSeleccionado
                = (dao.TiposDocumentoDAO.ItemCombo) cmbTipoMotivo.getSelectedItem();
        int tipoMotivoId = (motivoSeleccionado != null) ? motivoSeleccionado.getId() : 1;

        // Invocamos al DAO de bodegas de manera limpia y profesional
        dao.BodegasDAO bodegasDao = new dao.BodegasDAO();
        boolean exito = bodegasDao.registrarIngresoMercaderia(folio, tipoMotivoId, fecha, observacion, modeloTablaDetalle);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Ingreso de mercadería registrado y stock actualizado con éxito!");

        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al registrar el ingreso en la base de datos.");
        }
    }

    private void botonCancelar() {
        this.dispose();
    }

    private void botonDetalle() {
        // Instanciamos el JInternalFrame del detalle/historial
        vista.bodegas.IFrmDetalleProductos ventanaDetalle
                = new vista.bodegas.IFrmDetalleProductos();

        javax.swing.JDesktopPane escritorio = this.getDesktopPane();
        if (escritorio != null) {
            escritorio.add(ventanaDetalle);
            ventanaDetalle.setVisible(true);
            try {
                ventanaDetalle.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                System.err.println("Error al enfocar ventana: " + e.getMessage());
            }
        } else {

            ventanaDetalle.setVisible(true);
        }
    }

    private void buscarProductoPorCodigo() {
        String codigo = txtCodProducto.getText().trim();
        if (codigo.isEmpty()) {
            return;
        }

        // Instanciamos tu DAO existente
        dao.ProductosDAO productosDao = new dao.ProductosDAO();
        modelo.Producto prod = productosDao.buscarProductoBodega(codigo);

        if (prod != null && prod.isActivo()) {
            // Rellenamos los campos con los datos que nos devolvió tu método seguro
            txtDescripcion.setText(prod.getDescripcion());
            txtCostoUnit.setText(String.valueOf(prod.getCostoNeto()));
            txtCantidad.requestFocus(); // Saltamos directo a la cantidad
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Producto no encontrado o inactivo.");
            txtDescripcion.setText("");
            txtCostoUnit.setText("0.00");
            txtCodProducto.requestFocus();
        }
    }

    private void calcularTotalesGenerales() {
        int sumaCantidad = 0;
        double sumaCostoUnitarios = 0.0;
        double sumaTotalIngreso = 0.0;

        int filas = modeloTablaDetalle.getRowCount();
        for (int i = 0; i < filas; i++) {
            // Extraemos los valores de la tabla
            int cant = Integer.parseInt(modeloTablaDetalle.getValueAt(i, 2).toString());
            double costo = Double.parseDouble(modeloTablaDetalle.getValueAt(i, 3).toString().replace(",", "."));
            double sub = Double.parseDouble(modeloTablaDetalle.getValueAt(i, 4).toString().replace(",", "."));

            sumaCantidad += cant;
            sumaCostoUnitarios += costo;
            sumaTotalIngreso += sub;
        }

        // Asignamos los resultados a tus labels
        lblCantidad.setText(String.valueOf(sumaCantidad));
        lblTotalUni.setText(String.format("%.2f", sumaCostoUnitarios));
        lblTotalIngreso.setText(String.format("%.2f", sumaTotalIngreso));
    }

    private void cargarComboTiposDocumento() {
        javax.swing.JComboBox comboGenerico = (javax.swing.JComboBox) cmbTipoMotivo;
        comboGenerico.removeAllItems();

        dao.TiposDocumentoDAO daoDoc = new dao.TiposDocumentoDAO();
        List<dao.TiposDocumentoDAO.ItemCombo> lista = daoDoc.listarTiposDocumentoCombo();

        for (dao.TiposDocumentoDAO.ItemCombo item : lista) {
            comboGenerico.addItem(item);
        }
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);

        aplicarEfectoHoverPlano(btnDetalle, naranjaSuave);
        aplicarEfectoHoverPlano(btnAgregar, azulSuave);
        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
    }

    private void configuracionVentana() {
        this.setTitle("INGRESO DE MERCADERÍA A BODEGA");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void inicializarTablaDetalle() {
        modeloTablaDetalle = new javax.swing.table.DefaultTableModel(
                new Object[]{"CODIGO", "DESCRIPCION", "CANTIDAD", "COSTO UNIT.", "SUBTOTAL"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // La tabla de detalle es de solo lectura (se gestiona con botones)
            }
        };
        tblDetalle.setModel(modeloTablaDetalle);
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
        txtFolioMov = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserFechaIngreso = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        cmbTipoMotivo = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtObservacion = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtCodProducto = new javax.swing.JTextField();
        btnBuscarProducto = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCostoUnit = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDetalle = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        lblCantidad = new javax.swing.JLabel();
        lblTotalUni = new javax.swing.JLabel();
        lblTotalIngreso = new javax.swing.JLabel();
        btnDetalle = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("CABECERA DEL INGRESO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("N° Folio / Mov:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 30, -1, -1));
        jPanel2.add(txtFolioMov, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 260, -1));

        jLabel2.setText("Fecha Ingreso:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(375, 30, -1, -1));
        jPanel2.add(jDateChooserFechaIngreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 30, 260, -1));

        jLabel3.setText("Tipo Motivo:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 62, -1, -1));

        cmbTipoMotivo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoMotivo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel2.add(cmbTipoMotivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 620, -1));

        jLabel4.setText("Observación:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 90, -1, -1));
        jPanel2.add(txtObservacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 90, 620, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 745, 130));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("AGREGAR PRODUCTO AL INGRESO"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setText("Cód. Producto:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 30, -1, -1));

        txtCodProducto.addActionListener(this::txtCodProductoActionPerformed);
        jPanel1.add(txtCodProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 140, -1));

        btnBuscarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscarProducto.setBorderPainted(false);
        btnBuscarProducto.setContentAreaFilled(false);
        btnBuscarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarProducto.setFocusPainted(false);
        btnBuscarProducto.addActionListener(this::btnBuscarProductoActionPerformed);
        jPanel1.add(btnBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 25, -1, -1));

        jLabel6.setText("Cantidad:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, -1, -1));
        jPanel1.add(txtCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, 140, -1));

        jLabel7.setText("Costo Unit:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 30, -1, -1));
        jPanel1.add(txtCostoUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(575, 30, 140, -1));

        jLabel8.setText("Descripción:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 65, -1, -1));
        jPanel1.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 480, -1));

        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnAgregar.setText("AGREGAR");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setContentAreaFilled(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        jPanel1.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 57, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 162, 745, 105));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE PRODUCTOS A INGRESAR"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Cantidad", "Costo U.", "Total"
            }
        ));
        tblDetalle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblDetalle);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 25, 720, 140));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 270, 745, 180));

        jLabel9.setText("Cantidad Items:");
        pnlPrincipal.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 460, -1, -1));

        jLabel10.setText("Total Unidades:");
        pnlPrincipal.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 460, -1, -1));

        jLabel11.setText("TOTAL INGRESO:");
        pnlPrincipal.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 490, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlPrincipal.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 480, -1, -1));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 480, -1, -1));

        lblCantidad.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCantidad.setForeground(new java.awt.Color(0, 102, 204));
        lblCantidad.setText("0");
        pnlPrincipal.add(lblCantidad, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 460, -1, -1));

        lblTotalUni.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalUni.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalUni.setText("0");
        pnlPrincipal.add(lblTotalUni, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, -1, -1));

        lblTotalIngreso.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalIngreso.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalIngreso.setText("$");
        pnlPrincipal.add(lblTotalIngreso, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 490, -1, -1));

        btnDetalle.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDetalle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ficha-tecnica.png"))); // NOI18N
        btnDetalle.setText("VER REGISTROS");
        btnDetalle.setBorderPainted(false);
        btnDetalle.setContentAreaFilled(false);
        btnDetalle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDetalle.setFocusPainted(false);
        btnDetalle.addActionListener(this::btnDetalleActionPerformed);
        pnlPrincipal.add(btnDetalle, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 480, -1, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("INGRESO DE MERCADERÍA A BODEGA");
        pnlPrincipal.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(-4, 0, 780, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCodProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodProductoActionPerformed
        buscarProductoPorCodigo();
    }//GEN-LAST:event_txtCodProductoActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        botonAgregar();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
        buscarProductoPorCodigo();
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        botonCancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnDetalleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetalleActionPerformed
        botonDetalle();
    }//GEN-LAST:event_btnDetalleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnDetalle;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<String> cmbTipoMotivo;
    private com.toedter.calendar.JDateChooser jDateChooserFechaIngreso;
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
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblTotalIngreso;
    private javax.swing.JLabel lblTotalUni;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDetalle;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCodProducto;
    private javax.swing.JTextField txtCostoUnit;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtFolioMov;
    private javax.swing.JTextField txtObservacion;
    // End of variables declaration//GEN-END:variables
}
