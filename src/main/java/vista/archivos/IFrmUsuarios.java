package vista.archivos;

import dao.UsuarioDAO;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;

public class IFrmUsuarios extends javax.swing.JInternalFrame {

    private boolean esEdicion = false;
    private DefaultTableModel modeloTabla;
    private final UsuarioDAO usuarioDAO;
    private int idUsuarioSeleccionado = -1; // Para saber qué ID editar o eliminar

    public IFrmUsuarios() {
        initComponents();

        verificarUsuariosBloqueados();
        usuarioDAO = new UsuarioDAO();
        configurarTabla();
        cargarCombos();
        estadoInicial();
        aplicarColores();
        quitarBordes();
        ConfiguracionVenta();
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

    private void aplicarColores() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnCerrar, grisSuave);

    }

    private void botonNuevo() {
        limpiarCampos();
        habilitarCampos(true);
        btnNuevo.setEnabled(true);
        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(true);
        btnEliminar.setEnabled(true);
        esEdicion = true;
        idUsuarioSeleccionado = -1;
        txtRut.requestFocus();
    }

    private void botonGuardar() {
        if (!validarCampos()) {
            return;
        }

        String username = txtRut.getText().trim();
        String nombreCompleto = txtNombreCompleto.getText().trim();
        String rol = cmbperfilRol.getSelectedItem().toString();
        boolean activo = cmbEstado.getSelectedItem().toString().equalsIgnoreCase("Activo");
        String password = new String(txtContrasena.getPassword()).trim();

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto(nombreCompleto);
        u.setRol(rol);
        u.setActivo(activo);
        u.setEmail(txtNombreUsuario.getText().trim());

        boolean exito;
        if (esEdicion) {
            u.setId(idUsuarioSeleccionado);

            boolean cambiarPass = !password.isEmpty();
            if (cambiarPass) {
                // Si decide cambiar la contraseña, la encriptamos con SHA-256 antes de enviarla
                u.setPasswordHash(encriptarSHA256(password));
            } else {
                u.setPasswordHash(""); // El DAO sabrá que no debe sobreescribirla si cambiarPass es false
            }

            exito = usuarioDAO.actualizar(u, cambiarPass);

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Usuario actualizado con éxito!");
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al actualizar el usuario.");
            }
        } else {
            // Al crear un usuario nuevo, la contraseña es obligatoria
            if (password.isEmpty()) {
                utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar una contraseña para el nuevo usuario.");
                txtContrasena.requestFocus();
                return;
            }

            // Encriptamos la contraseña con SHA-256 para que el Login pueda validarla después
            u.setPasswordHash(encriptarSHA256(password));

            exito = usuarioDAO.guardar(u);

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Usuario registrado con éxito!");
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al guardar el usuario. Verifique que no exista.");
            }
        }

        if (exito) {
            estadoInicial();
        }
    }

    private void botonEditar() {
        // 1. Validamos que realmente se haya seleccionado un usuario de la tabla
        if (idUsuarioSeleccionado == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un usuario de la tabla para editar.");
            return;
        }

        // 2. Deshabilitamos el RUT/Username para que no se altere la llave principal por seguridad
        habilitarCampos(true);
        txtRut.setEnabled(false);

        // 3. Gestionamos correctamente el estado de los botones
        btnNuevo.setEnabled(false);    // Apagamos nuevo mientras editamos
        btnGuardar.setEnabled(true);   // Encendemos guardar para aplicar los cambios
        btnEditar.setEnabled(false);   // Apagamos editar porque ya estamos en modo edición
        btnEliminar.setEnabled(false); // Opcional: apagamos eliminar para evitar accidentes mientras editas

        // 4. Activamos la bandera de edición
        esEdicion = true;

        // 5. Mandamos el foco a un campo editable (por ejemplo, el nombre completo)
        txtNombreCompleto.requestFocus();
    }

    private void botonEliminar() {
        if (idUsuarioSeleccionado == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un usuario de la tabla para eliminar.");
            return;
        }

        // Usamos el booleano de AlertaUtil para la confirmación de eliminación
        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(this, "¿Está seguro que desea eliminar este usuario del sistema?");

        if (confirmar) {
            boolean exito = usuarioDAO.eliminar(idUsuarioSeleccionado);
            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Usuario eliminado correctamente.");
                estadoInicial();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar el usuario.");
            }
        }
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void ConfiguracionVenta() {

        this.setTitle("MANTENEDOR DE USUARIOS Y PERFILES DE ACCESO");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void configurarTabla() {
        modeloTabla = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Usuario / RUT", "Nombre Completo", "Rol", "Estado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblListadoUsuario.setModel(modeloTabla);

        
        if (tblListadoUsuario.getColumnCount() > 0) {
            tblListadoUsuario.getColumnModel().getColumn(0).setMaxWidth(40);
        }
    }

    private void cargarCombos() {
        cmbperfilRol.removeAllItems();
        cmbperfilRol.addItem("Seleccione Rol...");
        cmbperfilRol.addItem("Administrador");
        cmbperfilRol.addItem("Supervisor");
        cmbperfilRol.addItem("Operador");
        cmbperfilRol.addItem("Vendedor");
        cmbperfilRol.addItem("Bodeguero");

        // Cargar Estados en cmbEstado
        cmbEstado.removeAllItems();
        cmbEstado.addItem("Activo");
        cmbEstado.addItem("Inactivo");
    }

    private void cargarTablaUsuarios() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        List<Usuario> lista = usuarioDAO.listarUsuarios();

        for (Usuario u : lista) {
            String estadoStr = u.isActivo() ? "Activo" : "Inactivo";
            modeloTabla.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getNombreCompleto(),
                u.getRol(),
                estadoStr
            });
        }

        // Actualizar el label de total de usuarios (jLabel9)
        lblTotalUsuario.setText(String.valueOf(lista.size()));
    }

    private void estadoInicial() {
        limpiarCampos();
        habilitarCampos(true);

        btnNuevo.setEnabled(true);
        btnGuardar.setEnabled(true);
        btnEditar.setEnabled(true);
        btnEliminar.setEnabled(true);

        esEdicion = false;
        idUsuarioSeleccionado = -1;
        cargarTablaUsuarios();
    }

    private String encriptarSHA256(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void habilitarCampos(boolean estado) {
        txtRut.setEnabled(estado);
        txtNombreCompleto.setEnabled(estado);
        txtNombreUsuario.setEnabled(estado);
        cmbperfilRol.setEnabled(estado);
        txtContrasena.setEnabled(estado);
        cmbEstado.setEnabled(estado);
    }

    private void limpiarCampos() {
        txtRut.setText("");
        txtNombreCompleto.setText("");
        txtNombreUsuario.setText("");
        txtContrasena.setText("");
        if (cmbperfilRol.getItemCount() > 0) {
            cmbperfilRol.setSelectedIndex(0);
        }
        if (cmbEstado.getItemCount() > 0) {
            cmbEstado.setSelectedIndex(0);
        }
    }

    private void quitarBordes() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior

    }

    private void tblListadoUsuario() {
        int filaSeleccionada = tblListadoUsuario.getSelectedRow();

        if (filaSeleccionada != -1) {
            // 1. Capturamos el ID del usuario seleccionado de la tabla
            idUsuarioSeleccionado = Integer.parseInt(tblListadoUsuario.getValueAt(filaSeleccionada, 0).toString());

            // 2. Rellenamos las cajas de texto con los datos de la fila
            txtRut.setText(tblListadoUsuario.getValueAt(filaSeleccionada, 1).toString());
            txtNombreCompleto.setText(tblListadoUsuario.getValueAt(filaSeleccionada, 2).toString());
            // (Agrega aquí el resto de tus campos si los tienes)

            // 3. Gestionamos el estado de los botones y la edición
            btnEditar.setEnabled(true);
            btnEliminar.setEnabled(true);
            btnGuardar.setEnabled(false);
            esEdicion = true;
        }
    }

    private void verificarUsuariosBloqueados() {
        try {
            dao.UsuarioDAO daoUsuario = new dao.UsuarioDAO();
            java.util.List<modelo.Usuario> listaUsuarios = daoUsuario.listarUsuarios();

            // Filtramos cuántos usuarios están inactivos y con intentos fallidos >= 3 (o simplemente inactivos)
            StringBuilder bloqueados = new StringBuilder();
            int contadorBloqueados = 0;

            for (modelo.Usuario u : listaUsuarios) {
                // Si está inactivo y no es el admin principal (opcional, por seguridad)
                if (!u.isActivo() && !u.getUsername().equals("admin")) {
                    bloqueados.append("• ").append(u.getNombreCompleto())
                            .append(" (Usuario: ").append(u.getUsername()).append(")\n");
                    contadorBloqueados++;
                }
            }

            // Si hay al menos uno bloqueado, lanzamos tu AlertaUtil personalizada
            if (contadorBloqueados > 0) {
                String mensaje = "Hay " + contadorBloqueados + " usuario(s) bloqueado(s) por seguridad:\n\n"
                        + bloqueados.toString()
                        + "\nSelecciónelo en la lista para reactivarlo y cambiar su contraseña.";

                utilidades.AlertaUtil.mostrar("Atención Administrador", mensaje, "advertencia");
            }

        } catch (Exception e) {
            System.err.println("Error al verificar usuarios bloqueados: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        if (txtRut.getText().trim().isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar el Usuario / Identificador.");
            txtRut.requestFocus();
            return false;
        }
        if (txtNombreCompleto.getText().trim().isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar el Nombre Completo.");
            txtNombreCompleto.requestFocus();
            return false;
        }
        if (cmbperfilRol.getSelectedIndex() <= 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar un Perfil / Rol válido.");
            cmbperfilRol.requestFocus();
            return false;
        }

        String pass = new String(txtContrasena.getPassword());
        if (!esEdicion && pass.trim().isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar una contraseña para el nuevo usuario.");
            txtContrasena.requestFocus();
            return false;
        }
        return true;
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNombreUsuario = new javax.swing.JTextField();
        txtRut = new javax.swing.JTextField();
        txtNombreCompleto = new javax.swing.JTextField();
        cmbperfilRol = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbEstado = new javax.swing.JComboBox<>();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtContrasena = new javax.swing.JPasswordField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblListadoUsuario = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        lblTotalUsuario = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS DEL USUARIO"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("R.U.T.:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, -1, -1));

        jLabel2.setText("Nombre Completo:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, -1));

        jLabel3.setText("Nombre Usuario:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(32, 100, -1, -1));

        jLabel4.setText("Perfil / Rol:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(65, 130, -1, -1));
        jPanel1.add(txtNombreUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 100, 240, -1));
        jPanel1.add(txtRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 40, 560, -1));
        jPanel1.add(txtNombreCompleto, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 70, 560, -1));

        cmbperfilRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbperfilRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 130, 240, -1));

        jLabel5.setText("Contraseña:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 100, -1, -1));

        jLabel6.setText("Estado:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(415, 130, -1, -1));

        cmbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "Inactivo" }));
        jPanel1.add(cmbEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 130, 240, -1));

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setContentAreaFilled(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        jPanel1.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 165, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 165, -1, -1));

        btnEditar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnEditar.setText("EDITAR");
        btnEditar.setBorderPainted(false);
        btnEditar.setContentAreaFilled(false);
        btnEditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditar.setFocusPainted(false);
        btnEditar.addActionListener(this::btnEditarActionPerformed);
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 165, -1, -1));

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(485, 165, -1, -1));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtContrasena.setText("jPasswordField1");
        txtContrasena.setBorder(null);
        jPanel3.add(txtContrasena, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 2, 200, 20));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 100, 240, 25));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 30, 740, 210));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE USUARIOS REGISTRADOS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblListadoUsuario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblListadoUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblListadoUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblListadoUsuarioMousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(tblListadoUsuario);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 190));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 245, 740, 220));

        jLabel7.setText("Total Usuarios:");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 475, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 475, -1, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("MANTENEDOR DE USUARIOS Y PERFILES DE ACCESO");
        pnlPrincipal.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 10, -1, -1));

        lblTotalUsuario.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalUsuario.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalUsuario.setText("jLabel9");
        pnlPrincipal.add(lblTotalUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 475, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void tblListadoUsuarioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblListadoUsuarioMousePressed
        tblListadoUsuario();
    }//GEN-LAST:event_tblListadoUsuarioMousePressed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        botonNuevo();
    }//GEN-LAST:event_btnNuevoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JComboBox<String> cmbEstado;
    private javax.swing.JComboBox<String> cmbperfilRol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalUsuario;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblListadoUsuario;
    private javax.swing.JPasswordField txtContrasena;
    private javax.swing.JTextField txtNombreCompleto;
    private javax.swing.JTextField txtNombreUsuario;
    private javax.swing.JTextField txtRut;
    // End of variables declaration//GEN-END:variables
}
