package vista.compras;

import dao.TipoDocumentoDAO;
import java.util.List;
import modelo.Producto;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class IFrmIngresoCompras extends javax.swing.JInternalFrame {

    private final dao.ProveedorDAO proveedorDAO = new dao.ProveedorDAO();
    private final dao.CompraDAO compraDAO = new dao.CompraDAO();
    private modelo.Proveedor proveedorSeleccionado = null;
    private final TipoDocumentoDAO tipoDocDAO = new TipoDocumentoDAO();
    private javax.swing.table.DefaultTableModel modeloTablaDetalle;
    private final dao.CondicionPagoDAO condicionPagoDAO = new dao.CondicionPagoDAO();
    private final dao.ProductosDAO productoDAO = new dao.ProductosDAO();

    private final DecimalFormatSymbols simbolos = new DecimalFormatSymbols() {
        {
            setGroupingSeparator('.');
        }
    };
    private final DecimalFormat formatoMoneda = new DecimalFormat("$ #,##0", simbolos);

    public IFrmIngresoCompras() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        quitarProductoSeleccionado();
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

    private void btnAgregar() {
        String codigo = txtCodArt.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String cantTexto = txtCant.getText().trim();
        String costoTexto = txtCostoUnit.getText().trim();

        if (codigo.isEmpty() || cantTexto.isEmpty() || costoTexto.isEmpty()) {
            utilidades.AlertaUtil.mostrarError(this, "Debe ingresar Código, Cantidad y Costo Unitario.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantTexto);
            double costoUnitario = Double.parseDouble(costoTexto);

            if (cantidad <= 0 || costoUnitario < 0) {
                utilidades.AlertaUtil.mostrarError(this, "La cantidad y el costo deben ser valores válidos.");
                return;
            }

            double subtotal = cantidad * costoUnitario;

            // Agregar la fila a la tabla
            modeloTablaDetalle.addRow(new Object[]{
                codigo,
                descripcion,
                cantidad,
                String.format("%.2f", costoUnitario),
                String.format("%.2f", subtotal)
            });

            // Limpiar campos del ítem
            txtCodArt.setText("");
            txtDescripcion.setText("");
            txtCant.setText("");
            txtCostoUnit.setText("");
            txtCodArt.requestFocus();

            // Recalcular montos de la compra
            calcularTotales();

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Cantidad y Costo deben ser valores numéricos válidos.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnAgregarTipoDoc() {
        String[] opciones = {"Nuevo", "Editar", "Eliminar", "Cancelar"};

        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Tipos de Documento",
                "¿Qué acción deseas realizar sobre los Tipos de Documento?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // AGREGAR 
                String nuevoTipo = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Agregar Tipo de Documento",
                        "Ingrese el nombre del nuevo Tipo de Documento (Ej: Factura Electrónica):"
                );

                if (nuevoTipo != null && !nuevoTipo.trim().isEmpty()) {
                    String codigoGenerado = nuevoTipo.trim().substring(0, Math.min(3, nuevoTipo.trim().length())).toUpperCase();

                    boolean exito = tipoDocDAO.agregarTipoDocumento(codigoGenerado, nuevoTipo.trim());
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Tipo de documento agregado con éxito.");
                        cargarComboTipoDoc();
                        cmbTipoDoc.setSelectedItem(nuevoTipo.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo guardar el tipo de documento.");
                    }
                }
            }

            case 1 -> {
                // EDITAR 
                String seleccionadoEdit = (String) cmbTipoDoc.getSelectedItem();
                if (seleccionadoEdit == null || seleccionadoEdit.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia("Advertencia", "Seleccione un tipo de documento válido para editar.");
                    return;
                }

                String nuevoNombre = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Modificar Tipo de Documento",
                        "Modifique el nombre del tipo de documento:",
                        seleccionadoEdit
                );

                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && !nuevoNombre.trim().equals(seleccionadoEdit)) {
                    boolean exito = tipoDocDAO.actualizarTipoDocumento(seleccionadoEdit, nuevoNombre.trim());
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Tipo de documento actualizado con éxito.");
                        cargarComboTipoDoc();
                        cmbTipoDoc.setSelectedItem(nuevoNombre.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo actualizar el tipo de documento.");
                    }
                }
            }

            case 2 -> {
                // ELIMINAR (Desactivar)
                String seleccionadoElim = (String) cmbTipoDoc.getSelectedItem();
                if (seleccionadoElim == null || seleccionadoElim.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia("Advertencia", "Seleccione un tipo de documento válido para eliminar.");
                    return;
                }

                boolean confirma = utilidades.AlertaUtil.mostrarConfirmacion(
                        this,
                        "Confirmar Eliminación",
                        "¿Está seguro de eliminar/desactivar el tipo de documento '" + seleccionadoElim + "'?"
                );

                if (confirma) {
                    boolean exito = tipoDocDAO.eliminarTipoDocumento(seleccionadoElim);
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Tipo de documento eliminado con éxito.");
                        cargarComboTipoDoc();
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar el tipo de documento.");
                    }
                }
            }

            default -> {
                // Si selecciona "Cancelar" (-1 o 3), no hace nada
            }
        }
    }

    private void btnAgregarCondPago() {
        String[] opciones = {"Nueva", "Editar", "Eliminar", "Cancelar"};

        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Condiciones de Pago - Compras",
                "¿Qué acción deseas realizar sobre las Condiciones de Pago?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // AGREGAR 
                String nuevaCondicion = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Agregar Condición de Pago",
                        "Ingrese el nombre de la nueva Condición de Pago (Ej: Crédito 45 días):"
                );

                if (nuevaCondicion != null && !nuevaCondicion.trim().isEmpty()) {
                    boolean exito = condicionPagoDAO.agregarCondicionPago(nuevaCondicion.trim(), 0);
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Condición de pago agregada con éxito.");
                        cargarComboCondPago(); // Recarga el combo cmbCondPago de Compras
                        cmbCondPago.setSelectedItem(nuevaCondicion.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo guardar la condición de pago.");
                    }
                }
            }

            case 1 -> {
                // EDITAR
                String seleccionadaEdit = (String) cmbCondPago.getSelectedItem();
                if (seleccionadaEdit == null || seleccionadaEdit.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia("Advertencia", "Seleccione una condición válida para editar.");
                    return;
                }

                String nuevoNombre = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Modificar Condición de Pago",
                        "Modifique el nombre de la condición:",
                        seleccionadaEdit
                );

                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && !nuevoNombre.trim().equals(seleccionadaEdit)) {
                    boolean exito = condicionPagoDAO.actualizarCondicionPago(seleccionadaEdit, nuevoNombre.trim());
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Condición actualizada con éxito.");
                        cargarComboCondPago();
                        cmbCondPago.setSelectedItem(nuevoNombre.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo actualizar la condición.");
                    }
                }
            }

            case 2 -> {
                // ELIMINAR (Borrado Lógico)
                String seleccionadaElim = (String) cmbCondPago.getSelectedItem();
                if (seleccionadaElim == null || seleccionadaElim.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia("Advertencia", "Seleccione una condición válida para eliminar.");
                    return;
                }

                boolean confirma = utilidades.AlertaUtil.mostrarConfirmacion(
                        this,
                        "Confirmar Eliminación",
                        "¿Está seguro de eliminar/desactivar la condición '" + seleccionadaElim + "'?"
                );

                if (confirma) {
                    boolean exito = condicionPagoDAO.eliminarCondicionPago(seleccionadaElim);
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito("Éxito", "Condición eliminada con éxito.");
                        cargarComboCondPago();
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar la condición.");
                    }
                }
            }

            default -> {
                // Si selecciona Cancelar o cierra la ventana, no hace nada
            }
        }
    }

    private void btnBuscarRut() {
        String rut = txtRutProv.getText().trim();
        if (rut.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un RUT para buscar.");
            txtRutProv.requestFocus();
            return;
        }

        java.util.List<modelo.Proveedor> resultado = proveedorDAO.buscarProveedores("rut", rut);

        if (!resultado.isEmpty()) {
            this.proveedorSeleccionado = resultado.get(0);
            txtNombre.setText(this.proveedorSeleccionado.getNombre());
        } else {
            this.proveedorSeleccionado = null;
            txtNombre.setText("");
            utilidades.AlertaUtil.mostrarError(this, "Proveedor no encontrado con el RUT ingresado.");
        }
    }

    private void btnCancelar() {
        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                this,
                "¿Desea cancelar el ingreso actual? Se perderán los datos ingresados"
        );

        if (confirmar) {
            limpiarFormulario();
            this.dispose();
        }
    }

    private void btnRegistrar() {
        // 1. Validaciones del encabezado
        if (proveedorSeleccionado == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe buscar y seleccionar un proveedor válido por su RUT");
            txtRutProv.requestFocus();
            return;
        }

        String folio = txtFolioDoc.getText().trim();
        if (folio.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese el número / folio del documento.");
            txtFolioDoc.requestFocus();
            return;
        }

        if (jDateChooser_fechaDoc.getDate() == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione la fecha de emisión del documento.");
            jDateChooser_fechaDoc.requestFocus();
            return;
        }

        // 2. Validación del detalle (mínimo un producto en la tabla)
        if (modeloTablaDetalle.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrarError(this, "Debe agregar al menos un producto al detalle de la compra.");
            txtCodArt.requestFocus();
            return;
        }

        // 3. Armar la entidad Compra (Cabecera)
        modelo.Compra compra = new modelo.Compra();
        compra.setNumeroFactura(folio);
        compra.setTipo(cmbTipoDoc.getSelectedItem().toString());
        compra.setFechaEmision(jDateChooser_fechaDoc.getDate());
        compra.setFechaVencimiento(jDateChooser_fechaDoc.getDate());
        compra.setProveedorId(proveedorSeleccionado.getId());
        compra.setCondicionPago(cmbCondPago.getSelectedItem() != null ? cmbCondPago.getSelectedItem().toString() : "Contado");
        compra.setFormaPago("Efectivo/Transferencia");
        compra.setUsuarioId(1);

        // Asignamos textos por defecto para evitar nulos en la base de datos
        compra.setLugarEntrega("Casa Matriz");
        compra.setObservaciones("Ninguna");

        // Calcular subtotal, IVA y total acumulados de la grilla
        double neto = 0;
        for (int i = 0; i < modeloTablaDetalle.getRowCount(); i++) {
            String subtotalStr = modeloTablaDetalle.getValueAt(i, 4).toString().replace(",", ".");
            neto += Double.parseDouble(subtotalStr);
        }

        double iva = neto * 0.19;
        double total = neto + iva;

        compra.setSubtotal(neto);
        compra.setDescuento(0.0);
        compra.setIva(iva);
        compra.setTotal(total);

        // 4. Armar la lista de DetalleCompra desde las filas de la JTable
        for (int i = 0; i < modeloTablaDetalle.getRowCount(); i++) {
            String codigo = modeloTablaDetalle.getValueAt(i, 0).toString();
            String descripcion = modeloTablaDetalle.getValueAt(i, 1).toString();
            int cantidad = Integer.parseInt(modeloTablaDetalle.getValueAt(i, 2).toString());
            double costoUnit = Double.parseDouble(modeloTablaDetalle.getValueAt(i, 3).toString().replace(",", "."));

            modelo.DetalleCompra detalle = new modelo.DetalleCompra(codigo, descripcion, cantidad, costoUnit);
            compra.addDetalle(detalle);
        }

        // 5. Guardar en Base de Datos vía DAO con Transacción
        boolean exito = compraDAO.registrarCompra(compra);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Compra registrada con éxito en el sistema!");
            limpiarFormulario();
        } else {
            utilidades.AlertaUtil.mostrarError(this, "¡Error al  registrar la compra.");
        }
    }

    private void buscarProductoPorCodigo() {
        String codigo = txtCodArt.getText().trim();

        if (codigo.isEmpty()) {
            return;
        }

        // Consultamos al ProductoDAO
        Producto producto = productoDAO.obtenerProductoPorCodigo(codigo);

        if (producto != null) {

            txtDescripcion.setText(producto.getDescripcion());
            txtCostoUnit.setText(String.valueOf(producto.getPrecioUnitario()));

            // Pasa el foco al campo de cantidad
            txtCant.requestFocus();
        } else {

            utilidades.AlertaUtil.mostrarAdvertencia(
                    this,
                    "Producto no encontrado con el código:" + codigo
            );

            txtDescripcion.setText("");
            txtCostoUnit.setText("");
            txtCodArt.selectAll();
            txtCodArt.requestFocus();
        }
    }

    private void btnHistorial() {
        // 1. Instanciamos la ventana de historial
        IFrmListadoCompras ventanaHistorial = new IFrmListadoCompras();

        // 2. Obtenemos el contenedor principal (JDesktopPane) desde la ventana actual
        javax.swing.JDesktopPane escritorio = this.getDesktopPane();

        if (escritorio != null) {
            // 3. Agregamos la ventana al escritorio y la hacemos visible
            escritorio.add(ventanaHistorial);
            ventanaHistorial.setVisible(true);

            try {
                // Opcional: la mandamos al frente y seleccionamos
                ventanaHistorial.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
                System.err.println("Error al enfocar la ventana de historial: " + e.getMessage());
            }
        } else {
            utilidades.AlertaUtil.mostrarError(
                    this,
                    "No se pudo encontrar el contendor principal del sistema."
            );

        }
    }

    private void calcularTotales() {
        double neto = 0;

        for (int i = 0; i < modeloTablaDetalle.getRowCount(); i++) {
            String subtotalStr = modeloTablaDetalle.getValueAt(i, 4).toString().replace(",", ".");
            neto += Double.parseDouble(subtotalStr);
        }

        // Redondeamos los valores a enteros 
        long netoEntero = Math.round(neto);
        long ivaEntero = Math.round(netoEntero * 0.19); // IVA Chile (19%)
        long totalEntero = netoEntero + ivaEntero;

        // Seteamos las etiquetas con el formato en pesos chilenos ($ 1.200.000)
        lblNeto.setText(formatoMoneda.format(netoEntero));
        lblIva.setText(formatoMoneda.format(ivaEntero));
        lblTotal.setText(formatoMoneda.format(totalEntero));
    }

    private void cargarComboTipoDoc() {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement("SELECCIONE...");

        java.util.List<String> tipos = tipoDocDAO.listarTiposDocumentoActivos();
        for (String tipo : tipos) {
            model.addElement(tipo);
        }

        cmbTipoDoc.setModel(model);
    }

    private void cargarComboCondPago() {
        cmbCondPago.removeAllItems();

        List<String> condiciones = condicionPagoDAO.listarCondicionesActivas();

        for (String condicion : condiciones) {
            cmbCondPago.addItem(condicion);
        }
    }

    private void configuracionVentana() {
        this.setTitle("INGRESO DE COMPRAS Y MERCADERÍA");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void configurarTabla() {
        modeloTablaDetalle = new javax.swing.table.DefaultTableModel(
                new String[]{"Código", "Descripción", "Cantidad", "Costo Unit.", "Subtotal"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Filas de solo lectura desde la tabla
            }
        };
        jTable1.setModel(modeloTablaDetalle);
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
//        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnAgregar, azulSuave);
        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);
        aplicarEfectoHoverPlano(btnRegsitrarCompra, verdeSuave);
        aplicarEfectoHoverPlano(btnHistorial, naranjaSuave);
    }

    private void limpiarFormulario() {
        txtFolioDoc.setText("");
        txtRutProv.setText("");
        txtNombre.setText("");
        proveedorSeleccionado = null;
        jDateChooser_fechaDoc.setDate(null);

        txtCodArt.setText("");
        txtDescripcion.setText("");
        txtCant.setText("");
        txtCostoUnit.setText("");

        // Vaciar filas de la JTable
        modeloTablaDetalle.setRowCount(0);

        // Resetear Labels de totales
        lblNeto.setText("$ 0.00");
        lblIva.setText("$ 0.00");
        lblTotal.setText("$ 0.00");
    }

    private void quitarProductoSeleccionado() {
        // 1. Crear el menú contextual y la opción de eliminar
        javax.swing.JPopupMenu popupMenu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem menuEliminar = new javax.swing.JMenuItem("Quitar producto seleccionado");

// 2. Acción al hacer clic en "Quitar producto"
        menuEliminar.addActionListener((java.awt.event.ActionEvent evt) -> {
            quitarFilaSeleccionada();
        });

        popupMenu.add(menuEliminar);

// 3. Asignar el menú contextual al componente de la tabla
        jTable1.setComponentPopupMenu(popupMenu);

        configurarTabla();
        configuracionVentana();
        cargarComboCondPago();
        cargarComboTipoDoc();

        // Evento al presionar ENTER en la caja del código
        txtCodArt.addActionListener((java.awt.event.ActionEvent evt) -> {
            buscarProductoPorCodigo();
        });

// Evento al hacer clic en la lupa de búsqueda
        btnCodArt.addActionListener((java.awt.event.ActionEvent evt) -> {
            buscarProductoPorCodigo();
        });

        // El nombre del proveedor se autocompleta con la búsqueda por RUT
        txtNombre.setEditable(false);
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void quitarFilaSeleccionada() {
        int filaSeleccionada = jTable1.getSelectedRow();

        if (filaSeleccionada >= 0) {
            // Removemos la fila del modelo de la tabla
            modeloTablaDetalle.removeRow(filaSeleccionada);

            // Recalculamos inmediatamente los totales (Neto, IVA, Total)
            calcularTotales();
        } else {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Haz clic derecho sobre la fila que deseas quitar.",
                    "Aviso",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void txtCodArt() {

        txtCodArt.addActionListener((java.awt.event.ActionEvent evt1) -> {
            buscarProductoPorCodigo();
        });
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
        cmbTipoDoc = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFolioDoc = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtRutProv = new javax.swing.JTextField();
        btnBuscarRut = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jDateChooser_fechaDoc = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        cmbCondPago = new javax.swing.JComboBox<>();
        btnAgregarTipoDoc = new javax.swing.JButton();
        btnAgregarCondPago = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtCodArt = new javax.swing.JTextField();
        btnCodArt = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txtCant = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtCostoUnit = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        btnRegsitrarCompra = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        lblNeto = new javax.swing.JLabel();
        lblIva = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnHistorial = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS DEL PROVEEDOR Y FACTURA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Tipo Doc:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 27, -1, -1));

        cmbTipoDoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(82, 24, 260, -1));

        jLabel2.setText("N° Folio/Doc:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(372, 30, -1, -1));
        jPanel1.add(txtFolioDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(453, 24, 233, -1));

        jLabel3.setText("R.U.T. Prov:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 61, -1, -1));
        jPanel1.add(txtRutProv, new org.netbeans.lib.awtextra.AbsoluteConstraints(78, 58, 262, -1));

        btnBuscarRut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscarRut.setBorderPainted(false);
        btnBuscarRut.setContentAreaFilled(false);
        btnBuscarRut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarRut.setFocusPainted(false);
        btnBuscarRut.addActionListener(this::btnBuscarRutActionPerformed);
        jPanel1.add(btnBuscarRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(344, 55, -1, -1));

        jLabel4.setText("Nombre:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(396, 61, -1, -1));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(453, 58, 233, -1));

        jLabel5.setText("Fecha Doc:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 101, -1, -1));
        jPanel1.add(jDateChooser_fechaDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(78, 95, 262, -1));

        jLabel6.setText("Cond. Pago:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(379, 98, -1, -1));

        cmbCondPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbCondPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(453, 95, 210, -1));

        btnAgregarTipoDoc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnAgregarTipoDoc.setToolTipText("");
        btnAgregarTipoDoc.setBorderPainted(false);
        btnAgregarTipoDoc.setContentAreaFilled(false);
        btnAgregarTipoDoc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarTipoDoc.setFocusPainted(false);
        btnAgregarTipoDoc.addActionListener(this::btnAgregarTipoDocActionPerformed);
        jPanel1.add(btnAgregarTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 22, -1, -1));

        btnAgregarCondPago.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnAgregarCondPago.setBorderPainted(false);
        btnAgregarCondPago.setContentAreaFilled(false);
        btnAgregarCondPago.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarCondPago.setFocusPainted(false);
        btnAgregarCondPago.addActionListener(this::btnAgregarCondPagoActionPerformed);
        jPanel1.add(btnAgregarCondPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(652, 92, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 706, 140));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("AGREGAR PRODUCTO AL INVENTARIO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setText("Cód. Art:");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 27, -1, -1));

        txtCodArt.addActionListener(this::txtCodArtActionPerformed);
        jPanel2.add(txtCodArt, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 24, 145, -1));

        btnCodArt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnCodArt.setBorderPainted(false);
        btnCodArt.setContentAreaFilled(false);
        btnCodArt.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCodArt.setFocusPainted(false);
        btnCodArt.addActionListener(this::btnCodArtActionPerformed);
        jPanel2.add(btnCodArt, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, 20, -1, -1));

        jLabel8.setText("Cant:");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(263, 27, -1, -1));
        jPanel2.add(txtCant, new org.netbeans.lib.awtextra.AbsoluteConstraints(303, 24, 145, -1));

        jLabel9.setText("Costo Unit. ($):");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(454, 27, -1, -1));
        jPanel2.add(txtCostoUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(539, 24, 144, -1));

        jLabel10.setText("Descripción:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 68, -1, -1));
        jPanel2.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(94, 65, 451, -1));

        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnAgregar.setText("AGREGAR");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setContentAreaFilled(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        jPanel2.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(557, 61, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 165, 706, 110));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE LA COMPRA"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Cant", "C. Unit.", "Total Linea ($)"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, 680, 110));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 285, 706, 145));

        jLabel11.setText("Monto Neto:");
        pnlPrincipal.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 455, -1, -1));

        jLabel12.setText("I.V.A (19%):");
        pnlPrincipal.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 485, -1, -1));

        jLabel13.setText("TOTAL FACTURA COMPRA:");
        pnlPrincipal.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 455, -1, -1));

        btnRegsitrarCompra.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegsitrarCompra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnRegsitrarCompra.setText("REGISTRAR COMPRA");
        btnRegsitrarCompra.setBorderPainted(false);
        btnRegsitrarCompra.setContentAreaFilled(false);
        btnRegsitrarCompra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRegsitrarCompra.setFocusPainted(false);
        btnRegsitrarCompra.addActionListener(this::btnRegsitrarCompraActionPerformed);
        pnlPrincipal.add(btnRegsitrarCompra, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 445, 180, -1));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 445, -1, -1));

        lblNeto.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNeto.setForeground(new java.awt.Color(0, 102, 204));
        lblNeto.setText("0");
        pnlPrincipal.add(lblNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 455, -1, -1));

        lblIva.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblIva.setForeground(new java.awt.Color(0, 102, 204));
        lblIva.setText("0");
        pnlPrincipal.add(lblIva, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 485, -1, -1));

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(0, 102, 204));
        lblTotal.setText("0");
        pnlPrincipal.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 485, -1, -1));

        btnHistorial.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ficha-tecnica.png"))); // NOI18N
        btnHistorial.setText("VER HISTORIAL");
        btnHistorial.setBorderPainted(false);
        btnHistorial.setContentAreaFilled(false);
        btnHistorial.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnHistorial.setFocusPainted(false);
        btnHistorial.addActionListener(this::btnHistorialActionPerformed);
        pnlPrincipal.add(btnHistorial, new org.netbeans.lib.awtextra.AbsoluteConstraints(453, 480, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("INGRESO DE COMPRAS Y MERCADERÍA");
        pnlPrincipal.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 5, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarRutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarRutActionPerformed
        btnBuscarRut();
    }//GEN-LAST:event_btnBuscarRutActionPerformed

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        btnAgregar();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnRegsitrarCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegsitrarCompraActionPerformed
        btnRegistrar();
    }//GEN-LAST:event_btnRegsitrarCompraActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        btnCancelar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnAgregarTipoDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoDocActionPerformed
        btnAgregarTipoDoc();
    }//GEN-LAST:event_btnAgregarTipoDocActionPerformed

    private void btnAgregarCondPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarCondPagoActionPerformed
        btnAgregarCondPago();
    }//GEN-LAST:event_btnAgregarCondPagoActionPerformed

    private void txtCodArtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodArtActionPerformed
        txtCodArt();
    }//GEN-LAST:event_txtCodArtActionPerformed

    private void btnHistorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistorialActionPerformed
        btnHistorial();
    }//GEN-LAST:event_btnHistorialActionPerformed

    private void btnCodArtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCodArtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCodArtActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAgregarCondPago;
    private javax.swing.JButton btnAgregarTipoDoc;
    private javax.swing.JButton btnBuscarRut;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnCodArt;
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnRegsitrarCompra;
    private javax.swing.JComboBox<String> cmbCondPago;
    private javax.swing.JComboBox<String> cmbTipoDoc;
    private com.toedter.calendar.JDateChooser jDateChooser_fechaDoc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblIva;
    private javax.swing.JLabel lblNeto;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtCant;
    private javax.swing.JTextField txtCodArt;
    private javax.swing.JTextField txtCostoUnit;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtFolioDoc;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtRutProv;
    // End of variables declaration//GEN-END:variables
}
