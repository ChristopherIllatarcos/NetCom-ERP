package vista.bodegas;

import java.util.List;

/**
 *
 * @author caill
 */
public class IFrmRegistroProductos extends javax.swing.JInternalFrame {

    private int idProductoActual = 0;

    public IFrmRegistroProductos() {
        initComponents();
        colorBotones();
        cargarComboBodegas();
        cargarComboImpuestos();
        quitarBordeBarra();
        dao.ProductosDAO daoProd = new dao.ProductosDAO();
        daoProd.cargarCategorias((javax.swing.JComboBox) cmbFamiliaCategoria);
        limpiarFormulario();
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

    private void botonNuevo() {
        limpiarFormulario();
        txtCodigoInterno.requestFocus();
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void botonGuardar() {
        // 1. Validación de la familia
        dao.ProductosDAO.ItemCombo familiaSel = (dao.ProductosDAO.ItemCombo) cmbFamiliaCategoria.getSelectedItem();
        if (familiaSel == null || familiaSel.getId() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar una familia válida.");
            cmbFamiliaCategoria.requestFocus();
            return;
        }

        try {
            modelo.Producto p = new modelo.Producto();
            p.setCodigoInterno(txtCodigoInterno.getText().trim());
            p.setCodigoBarra(txtCodigoBarra.getText().trim());
            p.setDescripcion(txtDescripcionComercial.getText().trim());

            // Obtener ID seleccionado del ComboBox de Categoría
            dao.ProductosDAO.ItemCombo cat = (dao.ProductosDAO.ItemCombo) cmbFamiliaCategoria.getSelectedItem();
            int categoriaId = (cat != null) ? cat.getId() : 0;

            // Asignar el ID al producto
            p.setCategoriaId(categoriaId);

            p.setActivo(rbdActivo.isSelected());

            // Costos y Precios
            p.setCostoNeto(txtCostoNeto.getText().isEmpty() ? 0.0 : Double.parseDouble(txtCostoNeto.getText().trim()));
            p.setMargenGanancia(txtMargenGanancia.getText().isEmpty() ? 0.0 : Double.parseDouble(txtMargenGanancia.getText().trim()));
            p.setPrecioUnitario(txtPrecioVentaPublico.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPrecioVentaPublico.getText().trim()));

            // CORREGIDO: Casting seguro a ImpuestosDAO.ItemCombo
            dao.ImpuestosDAO.ItemCombo impuesto = (dao.ImpuestosDAO.ItemCombo) cmbImpuestoAdicional.getSelectedItem();
            p.setImpuestoAdicionalId(impuesto != null ? impuesto.getId() : 0);

            // Stock y Ubicación
            p.setStockActual(txtStockActual.getText().isEmpty() ? 0.0 : Double.parseDouble(txtStockActual.getText().trim()));
            p.setStockMinimo(txtStokcMinimo.getText().isEmpty() ? 0.0 : Double.parseDouble(txtStokcMinimo.getText().trim()));

            // CORREGIDO: Casting seguro a BodegasDAO.ItemCombo
            dao.BodegasDAO.ItemCombo ubicacion = (dao.BodegasDAO.ItemCombo) cmbUbicacionBodega.getSelectedItem();
            p.setUbicacionBodegaId(ubicacion != null ? ubicacion.getId() : 0);

            dao.ProductosDAO dao = new dao.ProductosDAO();
            if (dao.guardar(p)) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Producto guardado exitosamente!");
                limpiarFormulario();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al guardar el producto en la base de datos.");
            }

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, revise que los campos numéricos (costos, precios, stock) sean válidos.", "Error de Formato", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void botonBuscar() {
        String codigoBusqueda = txtCodProductoBarra.getText().trim();
        if (codigoBusqueda.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un código de barra o interno para buscar.");
            return;
        }

        dao.ProductosDAO dao = new dao.ProductosDAO();
        modelo.Producto p = dao.buscarProductoBodega(codigoBusqueda);

        if (p != null) {
            idProductoActual = p.getId();

            // Rellenar campos en la interfaz (CORREGIDO: Código de barras correcto)
            txtCodigoInterno.setText(p.getCodigoInterno());
            txtCodigoBarra.setText(p.getCodigoBarra());
            txtDescripcionComercial.setText(p.getDescripcion());

            // Seleccionar la categoría correcta (ProductosDAO.ItemCombo)
            for (int i = 0; i < cmbFamiliaCategoria.getItemCount(); i++) {
                Object obj = cmbFamiliaCategoria.getItemAt(i);
                if (obj instanceof dao.ProductosDAO.ItemCombo item) {
                    if (item.getId() == p.getCategoriaId()) {
                        cmbFamiliaCategoria.setSelectedIndex(i);
                        break;
                    }
                }
            }

            rbdActivo.setSelected(p.isActivo());
            rbdInactivo.setSelected(!p.isActivo());

            txtCostoNeto.setText(String.valueOf(p.getCostoNeto()));
            txtMargenGanancia.setText(String.valueOf(p.getMargenGanancia()));
            txtPrecioVentaPublico.setText(String.valueOf(p.getPrecioUnitario()));

            for (int i = 0; i < cmbImpuestoAdicional.getItemCount(); i++) {
                Object obj = cmbImpuestoAdicional.getItemAt(i);
                if (obj instanceof dao.ImpuestosDAO.ItemCombo item) {
                    if (item.getId() == p.getImpuestoAdicionalId()) {
                        cmbImpuestoAdicional.setSelectedIndex(i);
                        break;
                    }
                }
            }

            txtStockActual.setText(String.valueOf(p.getStockActual()));
            txtStokcMinimo.setText(String.valueOf(p.getStockMinimo()));

            for (int i = 0; i < cmbUbicacionBodega.getItemCount(); i++) {
                Object obj = cmbUbicacionBodega.getItemAt(i);
                if (obj instanceof dao.BodegasDAO.ItemCombo item) {
                    if (item.getId() == p.getUbicacionBodegaId()) {
                        cmbUbicacionBodega.setSelectedIndex(i);
                        break;
                    }
                }
            }

            utilidades.AlertaUtil.mostrarExito(this, "¡Producto encontrado!");
        } else {
            utilidades.AlertaUtil.mostrarError(this, "No se encontró ningún producto con ese código.");
        }
    }

    private void botonEditar() {
        // 1. Validar que tengamos un producto cargado para editar (usando el ID actual)
        if (idProductoActual <= 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Primero debe buscar y seleccionar un producto para editar.");
            return;
        }

        // 2. Validación de la familia
        dao.ProductosDAO.ItemCombo familiaSel = (dao.ProductosDAO.ItemCombo) cmbFamiliaCategoria.getSelectedItem();
        if (familiaSel == null || familiaSel.getId() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar una familia válida.");
            cmbFamiliaCategoria.requestFocus();
            return;
        }

        try {
            modelo.Producto p = new modelo.Producto();
            p.setId(idProductoActual); // ¡Fundamental para que el UPDATE sepa a qué registro apuntar!
            p.setCodigoInterno(txtCodigoInterno.getText().trim());
            p.setCodigoBarra(txtCodigoBarra.getText().trim());
            p.setDescripcion(txtDescripcionComercial.getText().trim());

            // Obtener ID seleccionado del ComboBox de Categoría
            dao.ProductosDAO.ItemCombo cat = (dao.ProductosDAO.ItemCombo) cmbFamiliaCategoria.getSelectedItem();
            p.setCategoriaId(cat != null ? cat.getId() : 0);

            p.setActivo(rbdActivo.isSelected());

            // Costos y Precios (Cuidado de no usar cast a char aquí tampoco)
            p.setCostoNeto(txtCostoNeto.getText().isEmpty() ? 0.0 : Double.parseDouble(txtCostoNeto.getText().trim()));
            p.setMargenGanancia(txtMargenGanancia.getText().isEmpty() ? 0.0 : Double.parseDouble(txtMargenGanancia.getText().trim()));
            p.setPrecioUnitario(txtPrecioVentaPublico.getText().isEmpty() ? 0.0 : Double.parseDouble(txtPrecioVentaPublico.getText().trim()));

            // Impuesto Adicional (Casting seguro a ImpuestosDAO)
            dao.ImpuestosDAO.ItemCombo impuesto = (dao.ImpuestosDAO.ItemCombo) cmbImpuestoAdicional.getSelectedItem();
            p.setImpuestoAdicionalId(impuesto != null ? impuesto.getId() : 0);

            // Stock y Ubicación
            p.setStockActual(txtStockActual.getText().isEmpty() ? 0.0 : Double.parseDouble(txtStockActual.getText().trim()));
            p.setStockMinimo(txtStokcMinimo.getText().isEmpty() ? 0.0 : Double.parseDouble(txtStokcMinimo.getText().trim()));

            // Ubicación Bodega (Casting seguro a BodegasDAO)
            dao.BodegasDAO.ItemCombo ubicacion = (dao.BodegasDAO.ItemCombo) cmbUbicacionBodega.getSelectedItem();
            p.setUbicacionBodegaId(ubicacion != null ? ubicacion.getId() : 0);

            dao.ProductosDAO dao = new dao.ProductosDAO();

            // Llamamos al método actualizar del DAO (asegúrate de tenerlo implementado)
            if (dao.actualizar(p)) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Producto actualizado exitosamente!");
                limpiarFormulario();
                idProductoActual = 0; // Reseteamos el ID actual tras editar
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al actualizar el producto en la base de datos.");
            }

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Por favor, revise que los campos numéricos (costos, precios, stock) sean válidos.", "Error de Formato", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void botonEliminar() {
        if (idProductoActual == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Primero debe buscar un producto para poder eliminarlo.");
            return;
        }

        boolean confirmacion = utilidades.AlertaUtil.mostrarConfirmacion(
                this,
                "Confirmar Eliminación",
                "¿Está seguro de que desea eliminar este producto?"
        );

        if (confirmacion) {
            dao.ProductosDAO dao = new dao.ProductosDAO();
            if (dao.eliminar(idProductoActual)) {

                utilidades.AlertaUtil.mostrarExito(
                        this,
                        "¡Producto eliminado correctamente!"
                );
                limpiarFormulario();
            } else {

                utilidades.AlertaUtil.mostrarError(
                        this,
                        "Error al intentar eliminar el producto."
                );
            }
        }
    }

    private void botonGestionar() {
        dao.BodegasDAO daoBodega = new dao.BodegasDAO();

        // 🌟 Cambiado a String[] para que coincida perfectamente con AlertaUtil.mostrarOpciones
        String[] opciones = {"Nuevo", "Editar", "Eliminar / Desactivar", "Cancelar"};

        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Bodegas y Sectores",
                "¿Qué acción desea realizar con las bodegas o sectores?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // --- CREAR NUEVO ---
                String nuevoNombre = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Nueva Bodega",
                        "Ingrese el nombre del nuevo sector o bodega:"
                );

                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    if (daoBodega.guardarBodega(nuevoNombre.trim())) {
                        cargarComboBodegas();
                        utilidades.AlertaUtil.mostrarExito(this, "¡Bodega creada con éxito!");
                    } else {
                        utilidades.AlertaUtil.mostrarError(this, "Error al crear la bodega.");
                    }
                }
            }

            case 1 -> {
                // --- EDITAR EXISTENTE ---
                List<dao.BodegasDAO.ItemCombo> listaEditar = daoBodega.listarBodegasComboParaEditar(); // Excluye la opción por defecto "-- SELECCIONE --"
                if (listaEditar.isEmpty()) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay bodegas registradas para editar.");
                    return;
                }

                dao.BodegasDAO.ItemCombo bodegaSeleccionada = (dao.BodegasDAO.ItemCombo) javax.swing.JOptionPane.showInputDialog(
                        this,
                        "Seleccione la bodega que desea corregir/editar:",
                        "Editar Bodega",
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null,
                        listaEditar.toArray(),
                        listaEditar.get(0)
                );

                if (bodegaSeleccionada != null) {
                    String nombreEditado = utilidades.AlertaUtil.mostrarInput(
                            this,
                            "Editar Nombre",
                            "Modifique el nombre del sector:",
                            bodegaSeleccionada.getNombre()
                    );

                    if (nombreEditado != null && !nombreEditado.trim().isEmpty()) {
                        if (daoBodega.actualizarBodega(bodegaSeleccionada.getId(), nombreEditado.trim())) {
                            cargarComboBodegas();
                            utilidades.AlertaUtil.mostrarExito(this, "¡Bodega actualizada con éxito!");
                        } else {
                            utilidades.AlertaUtil.mostrarError(this, "Error al actualizar la bodega.");
                        }
                    }
                }
            }

            case 2 -> {
                // --- ELIMINAR / DESACTIVAR ---
                List<dao.BodegasDAO.ItemCombo> listaEliminar = daoBodega.listarBodegasComboParaEditar();
                if (listaEliminar.isEmpty()) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay bodegas registradas para eliminar.");
                    return;
                }

                dao.BodegasDAO.ItemCombo bodegaAEliminar = (dao.BodegasDAO.ItemCombo) javax.swing.JOptionPane.showInputDialog(
                        this,
                        "Seleccione la bodega que desea eliminar:",
                        "Eliminar Bodega",
                        javax.swing.JOptionPane.QUESTION_MESSAGE,
                        null,
                        listaEliminar.toArray(),
                        listaEliminar.get(0)
                );

                if (bodegaAEliminar != null) {
                    boolean confirmacion = utilidades.AlertaUtil.mostrarConfirmacion(
                            this,
                            "Confirmar Eliminación",
                            "¿Está seguro de eliminar el sector: " + bodegaAEliminar.getNombre() + "?"
                    );

                    if (confirmacion) {
                        if (daoBodega.eliminarBodega(bodegaAEliminar.getId())) {
                            cargarComboBodegas();
                            utilidades.AlertaUtil.mostrarExito(this, "¡Bodega eliminada correctamente!");
                        } else {
                            utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar (puede estar asociada a un producto).");
                        }
                    }
                }
            }

            default -> {
                // Si cancela o cierra la ventana
            }
        }
    }

    private void botonImpuesto() {
        dao.ImpuestosDAO daoImp = new dao.ImpuestosDAO();

        String[] opciones = {"Nuevo", "Editar", "Eliminar / Desactivar", "Cancelar"};

        int seleccion = utilidades.AlertaUtil.mostrarOpciones(
                this,
                "Gestión de Impuestos Adicionales",
                "¿Qué acción desea realizar con los impuestos adicionales?",
                opciones
        );

        switch (seleccion) {
            case 0 -> {
                // --- CREAR NUEVO (Usamos AlertaUtil.mostrarInput en dos pasos o un panel seguro)
                String nombre = utilidades.AlertaUtil.mostrarInput(
                        this,
                        "Nuevo Impuesto Adicional",
                        "Ingrese el nombre del impuesto (ej. Licores):"
                );

                if (nombre != null && !nombre.trim().isEmpty()) {
                    String porcStr = utilidades.AlertaUtil.mostrarInput(
                            this,
                            "Nuevo Impuesto Adicional",
                            "Ingrese el porcentaje (ej. 20.5 para 20.5%):"
                    );

                    if (porcStr != null && !porcStr.trim().isEmpty()) {
                        try {
                            double porcentaje = Double.parseDouble(porcStr.trim().replace(",", "."));
                            if (daoImp.guardarImpuesto(nombre.trim(), porcentaje)) {
                                cargarComboImpuestos();
                                utilidades.AlertaUtil.mostrarExito(this, "¡Impuesto creado con éxito!");
                            } else {
                                utilidades.AlertaUtil.mostrarError(this, "Error al guardar en la base de datos.");
                            }
                        } catch (NumberFormatException e) {
                            utilidades.AlertaUtil.mostrarAdvertencia(this, "El porcentaje debe ser un número válido.");
                        }
                    }
                }
            }

            case 1 -> {
                // --- EDITAR EXISTENTE ---
                List<dao.ImpuestosDAO.ItemCombo> listaEditar = daoImp.listarImpuestosComboParaEditar();
                if (listaEditar.isEmpty()) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay impuestos registrados para editar.");
                    return;
                }

                dao.ImpuestosDAO.ItemCombo impSeleccionado = (dao.ImpuestosDAO.ItemCombo) javax.swing.JOptionPane.showInputDialog(
                        this, "Seleccione el impuesto a modificar:", "Editar Impuesto",
                        javax.swing.JOptionPane.QUESTION_MESSAGE, null, listaEditar.toArray(), listaEditar.get(0)
                );

                if (impSeleccionado != null) {
                    String nuevoNom = utilidades.AlertaUtil.mostrarInput(
                            this,
                            "Editar Nombre",
                            "Modifique el nombre del impuesto:",
                            impSeleccionado.getNombrePuro()
                    );

                    if (nuevoNom != null && !nuevoNom.trim().isEmpty()) {
                        String nuevoPorcStr = utilidades.AlertaUtil.mostrarInput(
                                this,
                                "Editar Porcentaje",
                                "Modifique el porcentaje:",
                                String.valueOf(impSeleccionado.getPorcentaje())
                        );

                        if (nuevoPorcStr != null && !nuevoPorcStr.trim().isEmpty()) {
                            try {
                                double nuevoPorc = Double.parseDouble(nuevoPorcStr.trim().replace(",", "."));

                                if (daoImp.actualizarImpuesto(impSeleccionado.getId(), nuevoNom.trim(), nuevoPorc)) {
                                    cargarComboImpuestos();
                                    utilidades.AlertaUtil.mostrarExito(this, "¡Impuesto actualizado con éxito!");
                                }
                            } catch (NumberFormatException e) {
                                utilidades.AlertaUtil.mostrarError(this, "Porcentaje inválido.");
                            }
                        }
                    }
                }
            }

            case 2 -> {
                // --- ELIMINAR ---
                List<dao.ImpuestosDAO.ItemCombo> listaEliminar = daoImp.listarImpuestosComboParaEditar();
                if (listaEliminar.isEmpty()) {
                    utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay impuestos para eliminar.");
                    return;
                }

                dao.ImpuestosDAO.ItemCombo impEliminar = (dao.ImpuestosDAO.ItemCombo) javax.swing.JOptionPane.showInputDialog(
                        this, "Seleccione el impuesto a eliminar:", "Eliminar Impuesto",
                        javax.swing.JOptionPane.QUESTION_MESSAGE, null, listaEliminar.toArray(), listaEliminar.get(0)
                );

                if (impEliminar != null) {
                    boolean conf = utilidades.AlertaUtil.mostrarConfirmacion(
                            this,
                            "Confirmar",
                            "¿Eliminar " + impEliminar.toString() + "?"
                    );

                    if (conf) {
                        if (daoImp.eliminarImpuesto(impEliminar.getId())) {
                            cargarComboImpuestos();
                            utilidades.AlertaUtil.mostrarExito(this, "¡Eliminado correctamente!");
                        } else {
                            utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar (puede estar en uso por un producto).");
                        }
                    }
                }
            }

            default -> {
                // Si cancela o cierra
            }
        }
    }

    private void cargarComboBodegas() {

        javax.swing.JComboBox comboGenerico = (javax.swing.JComboBox) cmbUbicacionBodega;
        comboGenerico.removeAllItems();

        dao.BodegasDAO dao = new dao.BodegasDAO();
        List<dao.BodegasDAO.ItemCombo> bodegas = dao.listarBodegasCombo();

        for (dao.BodegasDAO.ItemCombo b : bodegas) {
            comboGenerico.addItem(b);
        }
    }

    private void cargarComboImpuestos() {
        javax.swing.JComboBox comboGenerico = cmbImpuestoAdicional;
        comboGenerico.removeAllItems();

        dao.ImpuestosDAO daoImp = new dao.ImpuestosDAO();
        java.util.List<dao.ImpuestosDAO.ItemCombo> lista = daoImp.listarImpuestosCombo();

        for (dao.ImpuestosDAO.ItemCombo imp : lista) {
            comboGenerico.addItem(imp);
        }
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnCerrar, oscuroSuave);

    }

    private void configuracionVentana() {
        this.setTitle("MAESTRO / REGISTRO DE PRODUCTOS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void limpiarFormulario() {
        idProductoActual = 0;
        txtCodigoInterno.setText("");
        txtCodigoBarra.setText("");
        txtDescripcionComercial.setText("");
        cmbFamiliaCategoria.setSelectedIndex(0);
        rbdActivo.setSelected(true); // Por defecto activo

        txtCostoNeto.setText("");
        txtMargenGanancia.setText("");
        txtPrecioVentaPublico.setText("");
        cmbImpuestoAdicional.setSelectedIndex(0);

        txtStockActual.setText("");
        txtStokcMinimo.setText("");
        cmbUbicacionBodega.setSelectedIndex(0);

        txtCodProductoBarra.setText("");
        txtDescripcion.setText("");

        // Actualizar contador en el JLabel
        dao.ProductosDAO daoProd = new dao.ProductosDAO();
        lblTotalProductos.setText(String.valueOf(daoProd.contarProductos()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtCodProductoBarra = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtCodigoInterno = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtCodigoBarra = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtDescripcionComercial = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cmbFamiliaCategoria = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        rbdActivo = new javax.swing.JRadioButton();
        rbdInactivo = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtCostoNeto = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtMargenGanancia = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtPrecioVentaPublico = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        cmbImpuestoAdicional = new javax.swing.JComboBox<>();
        btnImpuesto = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        txtStockActual = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtStokcMinimo = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        cmbUbicacionBodega = new javax.swing.JComboBox<>();
        btnGestionarBodegas = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        lblTotalProductos = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA Y ACCIONES PRINCIPALES"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Cód. Producto / Barra:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));
        jPanel1.add(txtCodProductoBarra, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 30, 220, -1));

        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel1.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 25, -1, -1));

        jLabel2.setText("Descripción:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 30, -1, -1));
        jPanel1.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 210, -1));

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setContentAreaFilled(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        jPanel1.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 70, -1, -1));

        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnEditar.setText("EDITAR");
        btnEditar.setBorderPainted(false);
        btnEditar.setContentAreaFilled(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 70, -1, -1));

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 70, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 40, 740, 120));

        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("Código Interno:");
        jLabel3.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jLabel3AncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));
        jPanel3.add(txtCodigoInterno, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 250, -1));

        jLabel4.setText("Código de Barras:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, -1, -1));
        jPanel3.add(txtCodigoBarra, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, 250, -1));

        jLabel5.setText("Descripción Comercial:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));
        jPanel3.add(txtDescripcionComercial, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 250, -1));

        jLabel6.setText("Familia / Categoría y Unidad de Medida:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 30, -1, -1));

        cmbFamiliaCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel3.add(cmbFamiliaCategoria, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 50, 390, -1));

        jLabel7.setText("Estado:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 90, -1, -1));

        buttonGroup1.add(rbdActivo);
        rbdActivo.setText("Activo");
        jPanel3.add(rbdActivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 110, -1, -1));

        buttonGroup1.add(rbdInactivo);
        rbdInactivo.setText("Inactivo");
        jPanel3.add(rbdInactivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 110, -1, -1));

        jTabbedPane1.addTab("Datos Básicos", jPanel3);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setText("Costo Neto ($):");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));
        jPanel2.add(txtCostoNeto, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 250, -1));

        jLabel9.setText("Margen de Ganancia (%):");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));
        jPanel2.add(txtMargenGanancia, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 250, -1));

        jLabel10.setText("Precio Venta Público ($):");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 50, -1, -1));
        jPanel2.add(txtPrecioVentaPublico, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 70, 250, -1));

        jLabel11.setText("tipo de Impuesto Adicional:");
        jPanel2.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 110, -1, -1));

        cmbImpuestoAdicional.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cmbImpuestoAdicional, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 130, 250, -1));

        btnImpuesto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnImpuesto.setBorderPainted(false);
        btnImpuesto.setContentAreaFilled(false);
        btnImpuesto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImpuesto.setFocusPainted(false);
        btnImpuesto.addActionListener(this::btnImpuestoActionPerformed);
        jPanel2.add(btnImpuesto, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 130, 30, -1));

        jTabbedPane1.addTab("Precios y Costos", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel12.setText("Stock Actual:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, -1, -1));
        jPanel4.add(txtStockActual, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 250, -1));

        jLabel13.setText("Stock Mínimo (Alerta):");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 110, -1, -1));
        jPanel4.add(txtStokcMinimo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 250, -1));

        jLabel14.setText("Ubicación en Bodega:");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 50, -1, -1));

        cmbUbicacionBodega.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbUbicacionBodega.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel4.add(cmbUbicacionBodega, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 70, 250, -1));

        btnGestionarBodegas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnGestionarBodegas.setBorderPainted(false);
        btnGestionarBodegas.setContentAreaFilled(false);
        btnGestionarBodegas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGestionarBodegas.setFocusPainted(false);
        btnGestionarBodegas.addActionListener(this::btnGestionarBodegasActionPerformed);
        jPanel4.add(btnGestionarBodegas, new org.netbeans.lib.awtextra.AbsoluteConstraints(567, 68, -1, -1));

        jTabbedPane1.addTab("Stock y Control", jPanel4);

        pnlPrincipal.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 170, 740, 260));

        jLabel15.setText("Total de Productos Registrados: ");
        pnlPrincipal.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 450, -1, -1));

        lblTotalProductos.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalProductos.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalProductos.setText("jLabel16");
        pnlPrincipal.add(lblTotalProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 450, -1, -1));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("MAESTRO / REGISTRO DE PRODUCTOS");
        pnlPrincipal.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel3AncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jLabel3AncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel3AncestorAdded

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        botonNuevo();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGestionarBodegasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGestionarBodegasActionPerformed
        botonGestionar();
    }//GEN-LAST:event_btnGestionarBodegasActionPerformed

    private void btnImpuestoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImpuestoActionPerformed
        botonImpuesto();
    }//GEN-LAST:event_btnImpuestoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGestionarBodegas;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnImpuesto;
    private javax.swing.JButton btnNuevo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbFamiliaCategoria;
    private javax.swing.JComboBox<String> cmbImpuestoAdicional;
    private javax.swing.JComboBox<String> cmbUbicacionBodega;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblTotalProductos;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rbdActivo;
    private javax.swing.JRadioButton rbdInactivo;
    private javax.swing.JTextField txtCodProductoBarra;
    private javax.swing.JTextField txtCodigoBarra;
    private javax.swing.JTextField txtCodigoInterno;
    private javax.swing.JTextField txtCostoNeto;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtDescripcionComercial;
    private javax.swing.JTextField txtMargenGanancia;
    private javax.swing.JTextField txtPrecioVentaPublico;
    private javax.swing.JTextField txtStockActual;
    private javax.swing.JTextField txtStokcMinimo;
    // End of variables declaration//GEN-END:variables
}
