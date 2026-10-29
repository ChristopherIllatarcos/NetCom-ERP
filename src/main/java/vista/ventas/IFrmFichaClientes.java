package vista.ventas;

import dao.ClienteDAO;
import javax.swing.ButtonGroup;
import modelo.Cliente;

/**
 *
 * @author caill
 */
public class IFrmFichaClientes extends javax.swing.JInternalFrame {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private Cliente clienteActual = null; // Almacena el cliente consultado/en edición
    private final ButtonGroup bgEstado = new ButtonGroup();

    public IFrmFichaClientes() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        initCustomComponents();
        configurarVentana();
        cargarCombos();
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

    private void buscarPorRut() {
        String rut = txtRut.getText().trim();
        if (rut.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un RUT para buscar.");
            return;
        }

        Cliente c = clienteDAO.obtenerClientePorRut(rut);
        if (c != null) {
            cargarDatosEnFormulario(c);
        } else {
            utilidades.AlertaUtil.mostrarError(this, "No se encontró ningún cliente con el RUT ingresado.");
        }
    }

    private void buscarPorNombre() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un Nombre para buscar.");
            return;
        }

        Cliente c = clienteDAO.obtenerClientePorNombre(nombre);
        if (c != null) {
            cargarDatosEnFormulario(c);
        } else {
            utilidades.AlertaUtil.mostrarError(this, "No se encontraron coincidencias para el Nombre ingresado");
        }
    }

    private void colorBotones() {
        // Verde claro suave para Guardar / Aceptar
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        // Rojo claro suave para Cancelar / Cerrar / Eliminar
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);

        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);

        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
    }

    private void configurarVentana() {
        this.setTitle("FICHA DE CLIENTES");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarCombos() {
        // 1. Cargar Vendedores
        if (cmbVendedorAsignado != null) {
            cmbVendedorAsignado.removeAllItems(); // Elimina los "Item 1, Item 2..." por defecto
            cmbVendedorAsignado.addItem("-- Seleccione Vendedor --");
            for (Object[] v : clienteDAO.obtenerVendedoresCombo()) {
                cmbVendedorAsignado.addItem(v[0] + " - " + v[1]);
            }
        }

        // 2. Cargar Listas de Precios
        if (cmbListaPrecios != null) {
            cmbListaPrecios.removeAllItems(); // Elimina los "Item 1, Item 2..." por defecto
            cmbListaPrecios.addItem("-- Seleccione Lista --");
            for (Object[] lp : clienteDAO.obtenerListasPreciosCombo()) {
                cmbListaPrecios.addItem(lp[0] + " - " + lp[1]);
            }
        }
    }

    private void cargarDatosEnFormulario(Cliente cliente) {
        if (cliente == null) {
            return;
        }

        this.clienteActual = cliente;

        // Cargar campos de texto
        txtRutDni.setText(cliente.getRut());
        txtNombreDatosGenerales.setText(cliente.getNombre());
        txtRazonSocial.setText(cliente.getRazonSocial());
        txtGiroRubro.setText(cliente.getGiroRubro());
        txtDireccion.setText(cliente.getDireccion());
        txtComuna.setText(cliente.getComuna());
        txtCiudad.setText(cliente.getCiudad());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());
        txtCupoLimiteCredito.setText(String.valueOf(cliente.getCupoLimiteCredito()));
        txtDiasCredito.setText(String.valueOf(cliente.getDiasCredito()));

        // Estado Activo/Inactivo
        if (cliente.isActivo()) {
            rdbActivo.setSelected(true);
        } else {
            rdbInactivo.setSelected(true);
        }

        seleccionarComboPorId(cmbVendedorAsignado, cliente.getVendedorId());
        seleccionarComboPorId(cmbListaPrecios, cliente.getListaPrecioId());
    }

    private Cliente extraerDatosFormulario() {
        Cliente c = (clienteActual != null) ? clienteActual : new Cliente();

        c.setRut(txtRutDni.getText().trim());
        c.setNombre(txtNombreDatosGenerales.getText().trim());
        c.setRazonSocial(txtRazonSocial.getText().trim());
        c.setGiroRubro(txtGiroRubro.getText().trim());
        c.setDireccion(txtDireccion.getText().trim());
        c.setComuna(txtComuna.getText().trim());
        c.setCiudad(txtCiudad.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setActivo(rdbActivo.isSelected());

        // 1. Cupo Límite de Crédito
        try {
            c.setCupoLimiteCredito(Double.parseDouble(txtCupoLimiteCredito.getText().trim()));
        } catch (NumberFormatException e) {
            c.setCupoLimiteCredito(0.0);
        }

        try {
            c.setDiasCredito(Integer.parseInt(txtDiasCredito.getText().trim()));
        } catch (NumberFormatException e) {
            c.setDiasCredito(0);
        }

        // 3. Extraer IDs elegidos en los Combos
        c.setVendedorId(obtenerIdSeleccionadoCombo(cmbVendedorAsignado));
        c.setListaPrecioId(obtenerIdSeleccionadoCombo(cmbListaPrecios));

        return c;
    }

    private void editarCliente() {
        if (clienteActual == null || clienteActual.getId() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe buscar un cliente antes de modificar.");
            return;
        }

        Cliente c = extraerDatosFormulario();
        boolean exito = clienteDAO.actualizar(c);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "Cliente actualizado exitosamente.");
            limpiarFormulario();
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al actualizar cliente.");
        }
    }

    private void eliminarCliente() {
        if (clienteActual == null || clienteActual.getId() == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe buscar y seleccionar un cliente para eliminar.");
            return;
        }

        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                "Confirmar Eliminación",
                "¿Está seguro de que desea eliminar el cliente " + clienteActual.getNombre() + "?"
        );

        if (confirmar) {
            boolean exito = clienteDAO.eliminar(clienteActual.getId());
            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Cliente eliminado correctamente.");
                limpiarFormulario();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al eliminar el cliente. Verifique que no tenga ventas asociadas.");
            }
        }
    }

    private void guardarCliente() {
        if (txtRutDni.getText().trim().isEmpty() || txtNombreDatosGenerales.getText().trim().isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar al menos el RUT y el Nombre del cliente.");
            return;
        }

        Cliente c = extraerDatosFormulario();
        boolean exito = clienteDAO.guardar(c);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "Cliente guardado exitosamente.");
            limpiarFormulario();
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al guardar el cliente.");
        }
    }

    private void initCustomComponents() {
        // Agrupar los RadioButtons de estado para que sean excluyentes
        bgEstado.add(rdbActivo);
        bgEstado.add(rdbInactivo);
        rdbActivo.setSelected(true);

        // Cargar combos si aplica
        cargarCombos();

        // Asignar eventos de búsqueda (al presionar ENTER en las cajas de arriba)
        txtRut.addActionListener(e -> buscarPorRut());
        txtNombre.addActionListener(e -> buscarPorNombre());

        // Asignar eventos a botones principales
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarCliente());
        btnEditar.addActionListener(e -> editarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnCerrar.addActionListener(e -> this.dispose());
    }

    private void limpiarFormulario() {
        clienteActual = null;

        txtRut.setText("");
        txtNombre.setText("");
        txtRutDni.setText("");
        txtRazonSocial.setText("");
        txtNombreDatosGenerales.setText("");
        txtGiroRubro.setText("");
        rdbActivo.setSelected(true);

        txtDireccion.setText("");
        txtComuna.setText("");
        txtCiudad.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");

        txtCupoLimiteCredito.setText("0");
        txtDiasCredito.setText("0");

        if (cmbVendedorAsignado.getItemCount() > 0) {
            cmbVendedorAsignado.setSelectedIndex(0);
        }
        if (cmbListaPrecios.getItemCount() > 0) {
            cmbListaPrecios.setSelectedIndex(0);
        }

        txtRegistros.setText("Nuevo Registro");
        txtRutDni.requestFocus();
    }

    private int obtenerIdSeleccionadoCombo(javax.swing.JComboBox<String> combo) {
        if (combo == null || combo.getSelectedIndex() <= 0) {
            return 0;
        }
        String item = combo.getSelectedItem().toString();
        try {
            return Integer.parseInt(item.split(" -")[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void seleccionarComboPorId(javax.swing.JComboBox<String> combo, int id) {
        if (id <= 0) {
            combo.setSelectedIndex(0); // Queda en "-- Seleccione --"
            return;
        }
        for (int i = 1; i < combo.getItemCount(); i++) {
            String item = combo.getItemAt(i);
            if (item.startsWith(id + " -")) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(0);
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
        txtRut = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtRutDni = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtRazonSocial = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtNombreDatosGenerales = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtGiroRubro = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        rdbActivo = new javax.swing.JRadioButton();
        rdbInactivo = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtDireccion = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtComuna = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtCiudad = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        txtCupoLimiteCredito = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtDiasCredito = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        cmbVendedorAsignado = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        cmbListaPrecios = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        txtRegistros = new javax.swing.JTextField();
        btnCerrar = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA Y ACCIONES PRINCIPALES"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("R.U.T.:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

        txtRut.addActionListener(this::txtRutActionPerformed);
        jPanel1.add(txtRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 40, 290, -1));

        jLabel2.setText("Nombre:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 40, -1, -1));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 40, 290, -1));

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setContentAreaFilled(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnNuevoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNuevoMouseExited(evt);
            }
        });
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        jPanel1.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 80, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
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
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, -1, -1));

        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnEditar.setText("EDITAR");
        btnEditar.setBorderPainted(false);
        btnEditar.setContentAreaFilled(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEditarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEditarMouseExited(evt);
            }
        });
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 80, -1, -1));

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEliminarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEliminarMouseExited(evt);
            }
        });
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 80, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 25, 746, 140));

        jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("RUT / DNI:");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 51, -1, -1));
        jPanel3.add(txtRutDni, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 48, 430, -1));

        jLabel4.setText("Razón Social:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 91, -1, -1));
        jPanel3.add(txtRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 88, 430, -1));

        jLabel5.setText("Nombre:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 131, -1, -1));
        jPanel3.add(txtNombreDatosGenerales, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 128, 430, -1));

        jLabel6.setText("Giro / Rubro:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 171, -1, -1));
        jPanel3.add(txtGiroRubro, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 170, 430, -1));

        jLabel7.setText("Estado:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 210, -1, -1));

        buttonGroup1.add(rdbActivo);
        rdbActivo.setText("Activo");
        jPanel3.add(rdbActivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 208, -1, -1));

        buttonGroup1.add(rdbInactivo);
        rdbInactivo.setText("Inactivo");
        jPanel3.add(rdbInactivo, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 208, -1, -1));

        jTabbedPane1.addTab("Datos Generales", jPanel3);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel8.setText("Dirección");

        jLabel9.setText("Comuna:");

        jLabel10.setText("Ciudad:");

        jLabel11.setText("Teléfono:");

        jLabel12.setText("Email:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel12))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtTelefono)
                                    .addComponent(txtEmail)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(18, 18, 18)
                                .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(144, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtComuna, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                            .addComponent(txtCiudad))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtComuna, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(txtCiudad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Ubicación y Contacto", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel13.setText("Cupo / Límite de Crédito: ");

        jLabel14.setText("Días de Crédito: ");

        jLabel15.setText("Vendedor Asignado:");

        cmbVendedorAsignado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel16.setText("Lista de Precios:");

        cmbListaPrecios.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDiasCredito)
                    .addComponent(txtCupoLimiteCredito)
                    .addComponent(cmbVendedorAsignado, 0, 460, Short.MAX_VALUE)
                    .addComponent(cmbListaPrecios, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(135, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(txtCupoLimiteCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(txtDiasCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(cmbVendedorAsignado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cmbListaPrecios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(87, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Crédito y Ventas", jPanel4);

        pnlPrincipal.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 180, -1, 290));

        jLabel17.setText("Registros:");
        pnlPrincipal.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 480, -1, -1));

        txtRegistros.setEnabled(false);
        pnlPrincipal.add(txtRegistros, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 480, 446, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCerrarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrarMouseExited(evt);
            }
        });
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 480, -1, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("FICHA DE CLIENTES");
        pnlPrincipal.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseEntered
        //  jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnNuevoMouseEntered

    private void btnNuevoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseExited
        // jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnNuevoMouseExited

    private void btnGuardarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseEntered
        // jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnGuardarMouseEntered

    private void btnGuardarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnGuardarMouseExited
        // jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnGuardarMouseExited

    private void btnEditarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseEntered
        // jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnEditarMouseEntered

    private void btnEditarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseExited
        //  jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnEditarMouseExited

    private void btnEliminarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseEntered
        // jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnEliminarMouseEntered

    private void btnEliminarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseExited
        //jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnEliminarMouseExited

    private void btnCerrarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseEntered
        //  jSeparator5.setVisible(true);
    }//GEN-LAST:event_btnCerrarMouseEntered

    private void btnCerrarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseExited
        // jSeparator5.setVisible(false);
    }//GEN-LAST:event_btnCerrarMouseExited

    private void txtRutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRutActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarCliente();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed

    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        editarCliente();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarCliente();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCerrarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbListaPrecios;
    private javax.swing.JComboBox<String> cmbVendedorAsignado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
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
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rdbActivo;
    private javax.swing.JRadioButton rdbInactivo;
    private javax.swing.JTextField txtCiudad;
    private javax.swing.JTextField txtComuna;
    private javax.swing.JTextField txtCupoLimiteCredito;
    private javax.swing.JTextField txtDiasCredito;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtGiroRubro;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreDatosGenerales;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtRegistros;
    private javax.swing.JTextField txtRut;
    private javax.swing.JTextField txtRutDni;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
