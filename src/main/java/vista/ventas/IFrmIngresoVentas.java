package vista.ventas;

import dao.ClienteDAO;
import dao.VentaDAO;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import javax.swing.DefaultComboBoxModel;
import dao.TipoDocumentoDAO;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 *
 * @author caill
 */
public class IFrmIngresoVentas extends javax.swing.JInternalFrame {

    private javax.swing.table.DefaultTableModel modeloTabla;
    private int idClienteSeleccionado = 1;
    private final TipoDocumentoDAO tipoDocDAO = new TipoDocumentoDAO();
    private final dao.CondicionPagoDAO condicionPagoDAO = new dao.CondicionPagoDAO();

    
    public IFrmIngresoVentas() {
        initComponents();
        aplicarEfectoHover(btnGuardar);
        quitarBordesBarra();
        colorBonton();
        cargarSiguienteFolio();
        cargarComboCondPagoVentas();
        inicializarTabla();
        cargarCombos();
        cargarVendedoresCombo();
        cmbTipoDoc.addActionListener(this::cmbTipoDocActionPerformed);
        cargarFechaActual();
        calcularTotales();
        configuracionBontones();
        configurarVentana();
        asignarEventos();
        asignarEventoBtnAgregar();

    }

    private void asignarEventoBtnAgregar() {
        java.awt.event.ActionListener accionEnter = (ActionEvent e) -> {

            btnAgregar.doClick();
        };
        
        txtCant.addActionListener(accionEnter);
        txtDescripcion.addActionListener(accionEnter);
    }

    private void asignarEventos() {
        txtRutCliente.addActionListener((ActionEvent e) -> {
            btnBuscarCliente.doClick();
        });
    }

    private void aplicarEfectoHover(javax.swing.JButton boton) {
        // 1. Cambiar el cursor a una mano al pasar por encima
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // FORZAR a que el botón permita cambiar de color de fondo
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);

        // Guardar el color original del botón
        java.awt.Color colorOriginal = boton.getBackground();

        // Definir un color llamativo para cuando pase el mouse (ej: un verde bonito)
        java.awt.Color colorHover = new java.awt.Color(40, 167, 69);

        // 2. Agregar los eventos del mouse
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorHover);
                boton.repaint(); // Fuerza a redibujar el botón
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(colorOriginal);
                boton.repaint(); // Fuerza a redibujar el botón
            }
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

    private void btnAgregarTipoDoc() {
        String[] opciones = {"Agregar Nuevo", "Editar", "Eliminar", "Cancelar"};

        // 1. Menú de opciones moderno
        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Tipos de Documento",
                "¿Qué acción deseas realizar sobre los Tipos de Documento?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // AGREGAR (Usamos el método limpio de 3 parámetros para que no cargue texto vacío)
                String nuevoTipo = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Agregar Tipo de Documento",
                        "Ingrese el nombre del nuevo Tipo de Documento (Ej: Factura Electrónica):"
                );

                if (nuevoTipo != null && !nuevoTipo.trim().isEmpty()) {
                    String codigoGenerado = nuevoTipo.trim().substring(0, Math.min(3, nuevoTipo.trim().length())).toUpperCase();

                    boolean exito = tipoDocDAO.agregarTipoDocumento(codigoGenerado, nuevoTipo.trim());
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito(this, "Tipo de documento agregado con éxito.");
                        cargarComboTipoDoc();
                        cmbTipoDoc.setSelectedItem(nuevoTipo.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo guardar el tipo de documento.");
                    }
                }
            }

            case 1 -> {
                // EDITAR (Aquí sí usamos el de 4 parámetros pasándole el elemento seleccionado para que aparezca escrito)
                String seleccionadoEdit = (String) cmbTipoDoc.getSelectedItem();
                if (seleccionadoEdit == null || seleccionadoEdit.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un tipo de documento válido para editar.");
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
                        utilidades.AlertaUtil.mostrarExito(this, "Tipo de documento actualizado con éxito.");
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
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un tipo de documento válido para eliminar.");
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
                        utilidades.AlertaUtil.mostrarExito(this, "Tipo de documento eliminado con éxito.");
                        cargarComboTipoDoc();
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar el tipo de documento.");
                    }
                }
            }

            default -> {
                // Si cancela o cierra la ventana con la X
            }
        }
    }

    private void botonAgregarConPago() {
        String[] opciones = {"Agregar Nueva", "Editar", "Eliminar", "Cancelar"};

        // 1. Menú de opciones moderno
        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Condiciones de Pago",
                "¿Qué acción deseas realizar sobre las Condiciones de Pago?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // AGREGAR (Usamos el método limpio de 3 parámetros)
                String nuevaCondicion = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Agregar Condición de Pago",
                        "Ingrese el nombre de la nueva Condición de Pago:"
                );

                if (nuevaCondicion != null && !nuevaCondicion.trim().isEmpty()) {
                    boolean exito = condicionPagoDAO.agregarCondicionPago(nuevaCondicion.trim(), 0);
                    if (exito) {
                        utilidades.AlertaUtil.mostrarExito(this, "Condición de pago agregada con éxito.");
                        cargarComboCondPagoVentas();
                        cmbCondicionPago.setSelectedItem(nuevaCondicion.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo guardar la condición de pago.");
                    }
                }
            }

            case 1 -> {
                // EDITAR
                String seleccionadaEdit = (String) cmbCondicionPago.getSelectedItem();
                if (seleccionadaEdit == null || seleccionadaEdit.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione una condición válida para editar.");
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
                        utilidades.AlertaUtil.mostrarExito(this, "Condición actualizada con éxito.");
                        cargarComboCondPagoVentas();
                        cmbCondicionPago.setSelectedItem(nuevoNombre.trim());
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo actualizar la condición.");
                    }
                }
            }

            case 2 -> {
                // ELIMINAR (Desactivar)
                String seleccionadaElim = (String) cmbCondicionPago.getSelectedItem();
                if (seleccionadaElim == null || seleccionadaElim.equals("SELECCIONE...")) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione una condición válida para eliminar.");
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
                        utilidades.AlertaUtil.mostrarExito(this, "Condición eliminada con éxito.");
                        cargarComboCondPagoVentas();
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar la condición.");
                    }
                }
            }

            default -> {
                // Si cancela o cierra la ventana con la X
            }
        }
    }

    private void botonAgregar() {
        try {
            String codigo = txtCodBarra.getText().trim();
            String descripcion = txtDescripcion.getText().trim();

            // Validar que los campos de texto no estén vacíos antes de parsear
            if (codigo.isEmpty() || descripcion.isEmpty() || txtCant.getText().trim().isEmpty() || txtPUnit.getText().trim().isEmpty()) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe completar todos los campos del producto.");
                return;
            }

            int cant = Integer.parseInt(txtCant.getText().trim());
            double pUnit = Double.parseDouble(txtPUnit.getText().trim());

            if (cant <= 0 || pUnit <= 0) {
                utilidades.AlertaUtil.mostrar("Datos Inválidos", "La cantidad y el precio unitario deben ser mayores a cero.", "advertencia");
                return;
            }

            double totalLinea = cant * pUnit;

            // Agregar la fila directamente
            modeloTabla.addRow(new Object[]{codigo, descripcion, cant, pUnit, totalLinea});

            // Recalcular montos
            calcularTotales();

            // Limpiar campos de ingreso
            txtCodBarra.setText("");
            txtDescripcion.setText("");
            txtCant.setText("");
            txtPUnit.setText("");
            txtCodBarra.requestFocus();

        } catch (NumberFormatException e) {
            utilidades.AlertaUtil.mostrar("Error de Formato", "Ingrese datos numéricos válidos en Cantidad y Precio.", "advertencia");
        }
    }

    private void botonBuscar() {
        String rut = txtRutCliente.getText().trim();
        if (rut.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un RUT para buscar.");
            return;
        }

        // Llamada al DAO de Clientes
        ClienteDAO clienteDAO = new ClienteDAO();
        // En lugar de obtener solo el nombre, obtenemos el objeto Cliente entero
        Cliente cliente = clienteDAO.obtenerClientePorRut(rut);

        if (cliente != null) {
            txtNombre.setText(cliente.getNombre());
            this.idClienteSeleccionado = cliente.getId();
        } else {
            this.idClienteSeleccionado = 0;
            txtNombre.setText("");
            JOptionPane.showMessageDialog(this, "Cliente no encontrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void botonBuscarCod() {
        String codigo = txtCodBarra.getText().trim();

        if (codigo.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un código de producto para buscar.");
            return;
        }

        dao.VentaDAO ventaDAO = new dao.VentaDAO();
        modelo.Producto producto = ventaDAO.buscarProductoPorCodigo(codigo);

        if (producto != null) {
            txtDescripcion.setText(producto.getDescripcion());
            txtPUnit.setText(String.valueOf(producto.getPrecioUnitario()));
            txtCant.requestFocus(); // Mueve el foco a la cantidad para agilizar el ingreso
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Producto no encontrado en la base de datos.",
                    "Aviso",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            txtDescripcion.setText("");
            txtPUnit.setText("");
        }
    }

    private void colorBonton() {
        // Verde claro suave para Guardar / Aceptar
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);

        // Rojo claro suave para Cancelar / Cerrar / Eliminar
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);

        java.awt.Color azulSuave = new java.awt.Color(224, 242, 254);

        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnCancelar, rojoSuave);
        aplicarEfectoHoverPlano(btnAgregar, azulSuave);
    }

    private void configurarVentana() {
        this.setTitle("INGRESO DE VENTAS / POS");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void configuracionBontones() {
        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnBuscarCliente, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnBuscarCod, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnAgregar, jSeparator3, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnGuardar, jSeparator4, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnCancelar, jSeparator5, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    private void cargarCombos() {
        // 1. Cargar Tipos de Documento desde la BD
        cargarComboTipoDoc();

        // 2. Cargar Condiciones de Pago dinámicamente desde la BD
        cargarComboCondPagoVentas();
    }

    private void cargarVendedoresCombo() {
        cmbVendedor.removeAllItems();
        cmbVendedor.addItem("SELECCIONE...");

        dao.VendedorDAO vendedorDAO = new dao.VendedorDAO();
        java.util.List<modelo.Vendedor> lista = vendedorDAO.listarVendedores();

        for (modelo.Vendedor v : lista) {
            // Guardamos en formato "ID - Nombre"
            cmbVendedor.addItem(v.getId() + " - " + v.getNombre());
        }
    }

    private void cargarFechaActual() {
        jDateChooserFecha.setDate(new java.util.Date());
    }

    private void cargarSiguienteFolio() {
        if (cmbTipoDoc.getSelectedIndex() > 0) {
            String tipoDoc = cmbTipoDoc.getSelectedItem().toString();
            VentaDAO ventaDAO = new VentaDAO();
            int siguienteFolio = ventaDAO.obtenerSiguienteFolio(tipoDoc);

            txtFolio.setText(String.valueOf(siguienteFolio));
        }
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

    private void cargarComboCondPagoVentas() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        java.util.List<String> condiciones = condicionPagoDAO.listarCondicionesActivas();

        for (String condicion : condiciones) {
            model.addElement(condicion);
        }

        cmbCondicionPago.setModel(model);
    }

    private void codigoBarra() {
        String codigo = txtCodBarra.getText().trim();
        if (!codigo.isEmpty()) {
            dao.VentaDAO ventaDAO = new dao.VentaDAO();
            modelo.Producto p = ventaDAO.buscarProductoPorCodigo(codigo);

            if (p != null) {
                txtDescripcion.setText(p.getDescripcion());
                txtPUnit.setText(String.valueOf(p.getPrecioUnitario()));
                txtCant.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Producto no encontrado.");
            }
        }
    }

    private void calcularTotales() {
        double totalPagar = 0.0;

        // Recorrer filas validando que la celda no sea nula ni vacía
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Object val = modeloTabla.getValueAt(i, 4);
            if (val != null) {
                try {
                    // Limpia posibles caracteres no numéricos por si acaso
                    String strVal = val.toString().replaceAll("[^0-9.]", "");
                    if (!strVal.isEmpty()) {
                        totalPagar += Double.parseDouble(strVal);
                    }
                } catch (NumberFormatException e) {
                    // Ignora filas mal compuestas para evitar caídas
                }
            }
        }

        // Cálculo IVA 19% 
        double neto = Math.round(totalPagar / 1.19);
        double iva = totalPagar - neto;

        // Configuración del formateador de miles
        java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "CL"));
        simbolos.setGroupingSeparator('.');
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0", simbolos);

        // Asignación limpia a los JLabel
        lblNeto1.setText(df.format(neto));
        lblIva2.setText(df.format(iva));
        lblTotalPagar.setText(df.format(totalPagar));
    }

    private void historialVentas() {
        // 1. Instanciar la nueva ventana del historial
        IFrmHistorialVentas frameHistorial = new IFrmHistorialVentas();

        // 2. Obtener el contenedor principal (JDesktopPane) desde la ventana actual
        javax.swing.JDesktopPane desktopPane = getDesktopPane();

        if (desktopPane != null) {
            // Agregarlo al panel principal del sistema
            desktopPane.add(frameHistorial);
            frameHistorial.setVisible(true);
            try {
                frameHistorial.setSelected(true);
            } catch (java.beans.PropertyVetoException e) {
            }
        } else {
            // En caso de que no estés usando JDesktopPane, se muestra centrado sobre el frame actual
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No se encontró el contenedor principal.");
        }
    }

    private void inicializarTabla() {
        modeloTabla = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Código", "Descripción", "Cant", "P. Unit.", "Total Línea ($)"}
        );
        jTable1.setModel(modeloTabla);
    }

    private void limpiarFormulario() {
        txtFolio.setText("");
        txtRutCliente.setText("");
        txtNombre.setText("");
        txtCodBarra.setText("");
        txtCant.setText("");
        txtPUnit.setText("");
        txtDescripcion.setText("");
        lblNeto1.setText("");
        lblIva2.setText("");
        lblTotalPagar.setText("");
        modeloTabla.setRowCount(0);
        cmbVendedor.setSelectedIndex(0);
    }

    private void procesarGuardarVenta() {
        // 1. Validaciones
        if (cmbTipoDoc.getSelectedIndex() <= 0) {
            utilidades.AlertaUtil.mostrar("Documento Requerido", "Debe seleccionar un Tipo de Documento.", "advertencia");
            cmbTipoDoc.requestFocus();
            return;
        }

        // VALIDACIÓN DEL NUEVO COMBO VENDEDOR
        if (cmbVendedor.getSelectedIndex() <= 0) {
            utilidades.AlertaUtil.mostrar("Vendedor Requerido", "Debe seleccionar un Vendedor.", "advertencia");
            cmbVendedor.requestFocus();
            return;
        }

        if (txtRutCliente.getText().trim().isEmpty() || this.idClienteSeleccionado <= 0) {
            utilidades.AlertaUtil.mostrar("Cliente Requerido", "Debe seleccionar un cliente válido.", "advertencia");
            txtRutCliente.requestFocus();
            return;
        }

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        if (model.getRowCount() == 0) {
            utilidades.AlertaUtil.mostrar("Sin Productos", "Debe agregar al menos un producto a la venta.", "advertencia");
            return;
        }

        if (cmbCondicionPago.getSelectedIndex() <= 0) {
            utilidades.AlertaUtil.mostrar("Condición de Pago", "Debe seleccionar la Condición de Pago.", "advertencia");
            cmbCondicionPago.requestFocus();
            return;
        }

        // 2. Confirmación con tu AlertaUtil moderna (Sí / No)
        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                "Casa Victoria - Confirmar Venta",
                "¿Está seguro de emitir la venta por un total de <b>" + lblTotalPagar.getText() + "</b>?"
        );

        if (!confirmar) {
            return;
        }

        try {
            // EXTRAER EL ID DEL VENDEDOR DESDE EL COMBO ("ID - Nombre")
            String itemVendedor = cmbVendedor.getSelectedItem().toString();
            int idVendedor = Integer.parseInt(itemVendedor.split(" - ")[0].trim());

            // 3. Cabecera de Venta
            modelo.Venta venta = new modelo.Venta();
            venta.setTipoDoc(cmbTipoDoc.getSelectedItem().toString());
            venta.setFolio(Integer.parseInt(txtFolio.getText().trim()));
            venta.setFecha(new java.util.Date());
            venta.setClienteId(this.idClienteSeleccionado);

            // ASIGNAMOS EL ID DINÁMICO
            venta.setVendedorId(idVendedor);

            venta.setRutCliente(txtRutCliente.getText().trim());
            venta.setNombreCliente(txtNombre.getText().trim());
            venta.setCondicionPago(cmbCondicionPago.getSelectedItem().toString());

            // CONVERSIÓN SEGURA DESDE LOS JLABEL FORMATEADOS
            double neto = Double.parseDouble(lblNeto1.getText().replaceAll("[^0-9]", ""));
            double iva = Double.parseDouble(lblIva2.getText().replaceAll("[^0-9]", ""));
            double total = Double.parseDouble(lblTotalPagar.getText().replaceAll("[^0-9]", ""));

            venta.setNeto(neto);
            venta.setIva(iva);
            venta.setTotal(total);

            // 4. Detalle de Venta con ID de Producto dinámico
            List<modelo.DetalleVenta> detalles = new ArrayList<>();
            dao.ProductosDAO productoDAO = new dao.ProductosDAO();

            for (int i = 0; i < model.getRowCount(); i++) {
                String codigo = model.getValueAt(i, 0).toString();
                String descripcion = model.getValueAt(i, 1).toString();
                int cantidad = Integer.parseInt(model.getValueAt(i, 2).toString());
                double precioUnitario = Double.parseDouble(model.getValueAt(i, 3).toString());

                modelo.DetalleVenta det = new modelo.DetalleVenta(codigo, descripcion, cantidad, precioUnitario);

                // Obtener producto_id real desde la BD
                int idProd = productoDAO.obtenerIdPorCodigo(codigo);
                det.setProductoId(idProd);

                detalles.add(det);
            }

            // 5. Registrar Venta
            dao.VentaDAO ventaDAO = new dao.VentaDAO();
            if (ventaDAO.registrarVenta(venta, detalles)) {
                utilidades.AlertaUtil.mostrar("Venta Exitosa", "La venta se registró con éxito en el sistema.", "exito");
                limpiarFormulario();
                cargarSiguienteFolio();
            } else {
                utilidades.AlertaUtil.mostrar("Error de Base de Datos", "No se pudo guardar la venta en la base de datos.", "error");
            }

        } catch (java.awt.HeadlessException | NumberFormatException e) {
            utilidades.AlertaUtil.mostrar("Error de Proceso", "Ocurrió un error al procesar la venta: " + e.getMessage(), "error");
        }
    }

    private void quitarBordesBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void tipoDoc() {
        String tipoSeleccionado = cmbTipoDoc.getSelectedItem() != null ? cmbTipoDoc.getSelectedItem().toString() : "";

        if (!tipoSeleccionado.equals("SELECCIONE...") && !tipoSeleccionado.isEmpty()) {
            dao.VentaDAO ventaDAO = new dao.VentaDAO();
            int folio = ventaDAO.obtenerSiguienteFolio(tipoSeleccionado);
            txtFolio.setText(String.valueOf(folio));
            txtFolio.setEditable(false); // Bloqueamos el campo para evitar cambios manuales accidentales
        } else {
            txtFolio.setText("");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbTipoDoc = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFolio = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtRutCliente = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        txtNombre = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jDateChooserFecha = new com.toedter.calendar.JDateChooser();
        jLabel8 = new javax.swing.JLabel();
        cmbCondicionPago = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        cmbVendedor = new javax.swing.JComboBox<>();
        btnAgregarTipoDoc = new javax.swing.JButton();
        btnAgregarCondPago = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtCodBarra = new javax.swing.JTextField();
        btnBuscarCod = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtCant = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtPUnit = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnAgregar = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        lblNeto = new javax.swing.JLabel();
        lblIva = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        txtHistorialVentas = new javax.swing.JLabel();
        lblNeto1 = new javax.swing.JLabel();
        lblIva2 = new javax.swing.JLabel();
        lblTotalPagar = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CABECERA DE VENTA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Tipo Doc:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(33, 30, -1, -1));

        cmbTipoDoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "...", "Factura", "Boleta" }));
        cmbTipoDoc.addItemListener(this::cmbTipoDocItemStateChanged);
        cmbTipoDoc.addActionListener(this::cmbTipoDocActionPerformed);
        jPanel1.add(cmbTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, 180, -1));

        jLabel2.setText("N° Folio:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 30, -1, -1));
        jPanel1.add(txtFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 30, 80, -1));

        jLabel3.setText("R.U.T. Cliente:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(13, 70, -1, -1));
        jPanel1.add(txtRutCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 180, -1));

        btnBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscarCliente.setBorderPainted(false);
        btnBuscarCliente.setContentAreaFilled(false);
        btnBuscarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarCliente.setFocusPainted(false);
        btnBuscarCliente.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBuscarCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBuscarClienteMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnBuscarClienteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnBuscarClienteMouseExited(evt);
            }
        });
        btnBuscarCliente.addActionListener(this::btnBuscarClienteActionPerformed);
        jPanel1.add(btnBuscarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 65, -1, -1));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 70, 330, -1));

        jLabel6.setText("Nombre:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(331, 70, -1, -1));

        jLabel7.setText("Fecha:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(53, 110, -1, -1));
        jPanel1.add(jDateChooserFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 180, -1));

        jLabel8.setText("Condición Pago:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 110, -1, -1));

        cmbCondicionPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbCondicionPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(388, 110, 320, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 95, 50, -1));

        jLabel11.setText("Vendedor:");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 30, -1, -1));

        cmbVendedor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbVendedor, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 30, 180, -1));

        btnAgregarTipoDoc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnAgregarTipoDoc.setBorderPainted(false);
        btnAgregarTipoDoc.setContentAreaFilled(false);
        btnAgregarTipoDoc.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarTipoDoc.setFocusPainted(false);
        btnAgregarTipoDoc.addActionListener(this::btnAgregarTipoDocActionPerformed);
        jPanel1.add(btnAgregarTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 30, -1, -1));

        btnAgregarCondPago.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnAgregarCondPago.setBorderPainted(false);
        btnAgregarCondPago.setContentAreaFilled(false);
        btnAgregarCondPago.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarCondPago.setFocusPainted(false);
        btnAgregarCondPago.addActionListener(this::btnAgregarCondPagoActionPerformed);
        jPanel1.add(btnAgregarCondPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(695, 105, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 20, 740, 150));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("AGREGAR PRODUCTO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText("Cód. Barra/Art:");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 27, -1, -1));

        txtCodBarra.addActionListener(this::txtCodBarraActionPerformed);
        jPanel2.add(txtCodBarra, new org.netbeans.lib.awtextra.AbsoluteConstraints(102, 24, 160, -1));

        btnBuscarCod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscarCod.setBorderPainted(false);
        btnBuscarCod.setContentAreaFilled(false);
        btnBuscarCod.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarCod.setFocusPainted(false);
        btnBuscarCod.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnBuscarCod.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnBuscarCodMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnBuscarCodMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnBuscarCodMouseExited(evt);
            }
        });
        btnBuscarCod.addActionListener(this::btnBuscarCodActionPerformed);
        jPanel2.add(btnBuscarCod, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 20, -1, -1));

        jLabel5.setText("Cant:");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 30, -1, -1));
        jPanel2.add(txtCant, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 25, 160, -1));

        jLabel9.setText("P.Unit:");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 30, -1, -1));

        txtPUnit.addActionListener(this::txtPUnitActionPerformed);
        jPanel2.add(txtPUnit, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 25, 130, -1));

        jLabel10.setText("Descripción:");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 70, -1, -1));
        jPanel2.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 430, -1));

        btnAgregar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnAgregar.setText("AGREGAR");
        btnAgregar.setBorderPainted(false);
        btnAgregar.setContentAreaFilled(false);
        btnAgregar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregar.setFocusPainted(false);
        btnAgregar.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnAgregar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAgregarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAgregarMouseExited(evt);
            }
        });
        btnAgregar.addActionListener(this::btnAgregarActionPerformed);
        jPanel2.add(btnAgregar, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 65, -1, -1));
        jPanel2.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 50, 40, -1));
        jPanel2.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 95, 90, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 180, 740, 110));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE VENTA"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Descripción", "Cant", "P. Unit.", "Total Linea ($)"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 100));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 300, 740, 130));

        lblNeto.setText("Neto:");
        pnlPrincipal.add(lblNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        lblIva.setText("I.V.A:");
        pnlPrincipal.add(lblIva, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, -1, -1));

        lblTotal.setText("TOTAL A PAGAR:");
        pnlPrincipal.add(lblTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 450, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR / EMITIR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnGuardarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnGuardarMouseExited(evt);
            }
        });
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlPrincipal.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 465, -1, -1));

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR VENTA");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCancelarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCancelarMouseExited(evt);
            }
        });
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);
        pnlPrincipal.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 465, -1, -1));
        pnlPrincipal.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 495, 150, -1));
        pnlPrincipal.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 495, 130, -1));

        txtHistorialVentas.setBackground(new java.awt.Color(0, 102, 204));
        txtHistorialVentas.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        txtHistorialVentas.setForeground(new java.awt.Color(0, 102, 204));
        txtHistorialVentas.setText("Historial de Ventas");
        txtHistorialVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        txtHistorialVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtHistorialVentasMouseClicked(evt);
            }
        });
        pnlPrincipal.add(txtHistorialVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 430, -1, -1));

        lblNeto1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblNeto1.setForeground(new java.awt.Color(0, 102, 204));
        lblNeto1.setText("$");
        pnlPrincipal.add(lblNeto1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 450, -1, -1));

        lblIva2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblIva2.setForeground(new java.awt.Color(0, 102, 204));
        lblIva2.setText("$");
        pnlPrincipal.add(lblIva2, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 480, -1, -1));

        lblTotalPagar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalPagar.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalPagar.setText("$");
        pnlPrincipal.add(lblTotalPagar, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 480, -1, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("INGRESO DE VENTAS / POS");
        pnlPrincipal.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 5, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtPUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPUnitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPUnitActionPerformed

    private void btnBuscarClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarClienteMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBuscarClienteMouseClicked

    private void btnBuscarClienteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarClienteMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnBuscarClienteMouseEntered

    private void btnBuscarClienteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarClienteMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnBuscarClienteMouseExited

    private void btnBuscarCodMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarCodMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBuscarCodMouseClicked

    private void btnBuscarCodMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarCodMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnBuscarCodMouseEntered

    private void btnBuscarCodMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuscarCodMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnBuscarCodMouseExited

    private void btnAgregarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAgregarMouseEntered
        jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnAgregarMouseEntered

    private void btnAgregarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAgregarMouseExited
        jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnAgregarMouseExited

    private void btnGuardarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseEntered
        jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnGuardarMouseEntered

    private void btnGuardarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseExited
        jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnGuardarMouseExited

    private void btnCancelarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelarMouseEntered
        jSeparator5.setVisible(true);
    }//GEN-LAST:event_btnCancelarMouseEntered

    private void btnCancelarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelarMouseExited
        jSeparator5.setVisible(false);
    }//GEN-LAST:event_btnCancelarMouseExited

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        botonAgregar();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarClienteActionPerformed

    private void btnBuscarCodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarCodActionPerformed
        botonBuscarCod();
    }//GEN-LAST:event_btnBuscarCodActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void txtCodBarraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodBarraActionPerformed
        codigoBarra();
    }//GEN-LAST:event_txtCodBarraActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        procesarGuardarVenta();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void cmbTipoDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoDocActionPerformed
        tipoDoc();
    }//GEN-LAST:event_cmbTipoDocActionPerformed

    private void txtHistorialVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtHistorialVentasMouseClicked
        historialVentas();
    }//GEN-LAST:event_txtHistorialVentasMouseClicked

    private void cmbTipoDocItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbTipoDocItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            cargarSiguienteFolio();
        }
    }//GEN-LAST:event_cmbTipoDocItemStateChanged

    private void btnAgregarTipoDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTipoDocActionPerformed
        btnAgregarTipoDoc();
    }//GEN-LAST:event_btnAgregarTipoDocActionPerformed

    private void btnAgregarCondPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarCondPagoActionPerformed
        botonAgregarConPago();
    }//GEN-LAST:event_btnAgregarCondPagoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAgregarCondPago;
    private javax.swing.JButton btnAgregarTipoDoc;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnBuscarCod;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JComboBox<String> cmbCondicionPago;
    private javax.swing.JComboBox<String> cmbTipoDoc;
    private javax.swing.JComboBox<String> cmbVendedor;
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
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblIva;
    private javax.swing.JLabel lblIva2;
    private javax.swing.JLabel lblNeto;
    private javax.swing.JLabel lblNeto1;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalPagar;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtCant;
    private javax.swing.JTextField txtCodBarra;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtFolio;
    private javax.swing.JLabel txtHistorialVentas;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtPUnit;
    private javax.swing.JTextField txtRutCliente;
    // End of variables declaration//GEN-END:variables

}
