package vista.archivos;

import java.util.List;
import javax.swing.table.DefaultTableModel;
import dao.CamaraKeyDAO;

public class IFrmCamaraKay extends javax.swing.JInternalFrame {

    private final CamaraKeyDAO dao = new CamaraKeyDAO();

    public IFrmCamaraKay() {
        initComponents();

        colorBotones();
        quitarBordesBarra();
        cargarCombos();
        configurarTabla();
        cargarMapeoTabla();
        this.ConfiguracionVentana();
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

    private void botonAgregarTecla() {
        // 1. Pedir la Tecla usando InputUtil
        String tecla = utilidades.InputUtil.mostrarInput(this, "Nueva Tecla", "Ingrese la tecla (ej: F1, A, Ctrl+1):");
        if (tecla == null || tecla.trim().isEmpty()) {
            return; // Si cancela o deja vacío, no hace nada
        }

        // 2. Pedir la Acción o Función usando InputUtil
        String accion = utilidades.InputUtil.mostrarInput(this, "Acción", "Ingrese la Acción o Función (ej: Venta Rápida):");
        if (accion == null || accion.trim().isEmpty()) {
            return;
        }

        // 3. Pedir el Producto Asignado usando InputUtil
        String producto = utilidades.InputUtil.mostrarInput(this, "Producto", "Ingrese el Producto Asignado:");
        if (producto == null || producto.trim().isEmpty()) {
            producto = "Sin Producto";
        }

        // 4. Agregar directamente a la tabla visualmente
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblMapeo.getModel();
        modelo.addRow(new Object[]{tecla.trim().toUpperCase(), accion.trim(), producto.trim(), "Activo"});

        utilidades.AlertaUtil.mostrarInformacion(this, "¡Tecla agregada a la lista con éxito!");
    }

    private void botonModificar() {
        int filaSeleccionada = tblMapeo.getSelectedRow();

        // 1. Validar que realmente haya seleccionado una fila
        if (filaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, seleccione una tecla de la tabla para modificar.");
            return;
        }

        // 2. Obtener los valores actuales de la fila seleccionada
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblMapeo.getModel();
        String teclaActual = modelo.getValueAt(filaSeleccionada, 0).toString();
        String accionActual = modelo.getValueAt(filaSeleccionada, 1).toString();
        String productoActual = modelo.getValueAt(filaSeleccionada, 2).toString();
        String estadoActual = modelo.getValueAt(filaSeleccionada, 3).toString();

        // 3. Pedir nuevos datos usando InputUtil con los valores pre-cargados
        String nuevaTecla = utilidades.InputUtil.mostrarInput(this, "Modificar Tecla", "Modificar Tecla:", teclaActual);
        if (nuevaTecla == null) {
            return;
        }

        String nuevaAccion = utilidades.InputUtil.mostrarInput(this, "Modificar Acción", "Modificar Acción o Función:", accionActual);
        if (nuevaAccion == null) {
            return;
        }

        String nuevoProducto = utilidades.InputUtil.mostrarInput(this, "Modificar Producto", "Modificar Producto Asignado:", productoActual);
        if (nuevoProducto == null) {
            nuevoProducto = "Sin Producto";
        }

        // 4. Actualizar la fila en la tabla visual
        modelo.setValueAt(nuevaTecla.trim().toUpperCase(), filaSeleccionada, 0);
        modelo.setValueAt(nuevaAccion.trim(), filaSeleccionada, 1);
        modelo.setValueAt(nuevoProducto.trim(), filaSeleccionada, 2);
        modelo.setValueAt(estadoActual, filaSeleccionada, 3); // Mantiene el estado que tenía

        utilidades.AlertaUtil.mostrarInformacion(
                this,
                "¡Tecla modificada con éxito en la lista! (No olvide hacer clic en Guardar para aplicar cambios en la base de datos)."
        );
    }

    private void botonGuardar() {
        String terminal = cmbCajaTerminal.getSelectedItem() != null ? cmbCajaTerminal.getSelectedItem().toString() : "";
        String perfil = cmbPerfilTeclado.getSelectedItem() != null ? cmbPerfilTeclado.getSelectedItem().toString() : "";

        if (terminal.isEmpty() || perfil.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar una Caja y un Perfil de Teclado.");
            return;
        }

        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tblMapeo.getModel();
        int totalFilas = modelo.getRowCount();

        if (totalFilas == 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "No hay teclas en la tabla para guardar.");
            return;
        }

        // 1. Convertir el DefaultTableModel de la UI a una lista tipada de MapeoTecla
        java.util.List<modelo.MapeoTecla> listaMapeos = new java.util.ArrayList<>();

        for (int i = 0; i < totalFilas; i++) {
            Object valTecla = modelo.getValueAt(i, 0);
            Object valAccion = modelo.getValueAt(i, 1);
            Object valProducto = modelo.getValueAt(i, 2);
            Object valEstado = modelo.getValueAt(i, 3);

            String tecla = (valTecla != null) ? valTecla.toString() : "";
            String accion = (valAccion != null) ? valAccion.toString() : "";
            String producto = (valProducto != null) ? valProducto.toString() : "";
            boolean estado = (valEstado != null) && valEstado.toString().equalsIgnoreCase("Activo");

            listaMapeos.add(new modelo.MapeoTecla(tecla, accion, producto, estado));
        }

        // 2. Llamar al DAO pasándole la lista limpia y desacoplada
        boolean exito = dao.guardarMapeoTeclas(terminal, perfil, listaMapeos);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Configuración de teclas guardada correctamente!");
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al guardar las teclas en la base de datos.");
        }
    }

    private void botonNuevaCaja() {
        // 1. Pedir el nombre de la nueva caja usando nuestro InputUtil personalizado
        String nombreCaja = utilidades.InputUtil.mostrarInput(
                this,
                "Registrar Nueva Caja",
                "Ingrese el nombre de la nueva Caja / Terminal:"
        );

        if (nombreCaja == null || nombreCaja.trim().isEmpty()) {
            return;
        }

        nombreCaja = nombreCaja.trim();

        // 2. Guardarla automáticamente en la base de datos usando el DAO
        boolean exito = dao.registrarNuevaCaja(nombreCaja);

        if (exito) {

            utilidades.AlertaUtil.mostrarExito(this, "¡Caja registrada con éxito!");

            // 3. Recargar el ComboBox automáticamente para que aparezca la nueva caja seleccionada
            cargarCombos();
            cmbCajaTerminal.setSelectedItem(nombreCaja); // La selecciona al tiro
        } else {

            utilidades.AlertaUtil.mostrarError(this, "Error al registrar la caja en la base de datos.");
        }
    }

    private void botonNuevoPerfil() {
        // 1. Pedir el nombre del nuevo perfil usando InputUtil
        String nombrePerfil = utilidades.InputUtil.mostrarInput(
                this,
                "Registrar Nuevo Perfil",
                "Ingrese el nombre del nuevo Perfil de Teclado:"
        );

        if (nombrePerfil == null || nombrePerfil.trim().isEmpty()) {
            return;
        }

        nombrePerfil = nombrePerfil.trim();

        // 2. Guardarlo en la base de datos mediante el DAO
        boolean exito = dao.registrarNuevoPerfil(nombrePerfil);

        if (exito) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Perfil de teclado registrado con éxito!");

            // 3. Recargar el combo y dejar seleccionado el nuevo perfil al tiro
            cargarCombos();
            cmbPerfilTeclado.setSelectedItem(nombrePerfil);
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al registrar el perfil en la base de datos.");
        }
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnModificar, azulSuave);
        aplicarEfectoHoverPlano(btnAgregarTegla, grisSuave);

    }

    private void ConfiguracionVentana() {
        this.setTitle("CONFIGURACIÓN DE TECLAS RÁPIDAS Y CÁMARA KEY (POS)");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarCombos() {
        cmbCajaTerminal.removeAllItems();
        cmbPerfilTeclado.removeAllItems();

        // 1. Cargar Terminales desde la BD
        List<String> terminales = dao.obtenerCajasTerminales();
        for (String t : terminales) {
            cmbCajaTerminal.addItem(t);
        }

        // 2. Cargar Perfiles de Teclado desde la BD
        List<String> perfiles = dao.obtenerPerfilesTeclado();
        for (String p : perfiles) {
            cmbPerfilTeclado.addItem(p);
        }
    }

    private void configurarTabla() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Tecla", "Acción / Función", "Producto Asignado", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Celdas no editables directamente
            }
        };
        tblMapeo.setModel(modelo);
    }

    private void cargarMapeoTabla() {
        String terminal = cmbCajaTerminal.getSelectedItem() != null ? cmbCajaTerminal.getSelectedItem().toString() : "";
        String perfil = cmbPerfilTeclado.getSelectedItem() != null ? cmbPerfilTeclado.getSelectedItem().toString() : "";

        // 1. Obtenemos la lista de DTOs desde el DAO
        java.util.List<modelo.MapeoTecla> datos = dao.obtenerMapeoTeclas(terminal, perfil);
        DefaultTableModel modelo = (DefaultTableModel) tblMapeo.getModel();
        modelo.setRowCount(0);

        // 2. Recorremos los objetos y los agregamos al modelo de la JTable
        for (modelo.MapeoTecla m : datos) {
            Object[] fila = new Object[]{
                m.getTecla(),
                m.getAccion(),
                m.getProducto(),
                m.getEstadoStr() // Retorna "Activo" o "Inactivo"
            };
            modelo.addRow(fila);
        }
    }

    private void quitarBordesBarra() {
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
        jLabel2 = new javax.swing.JLabel();
        cmbCajaTerminal = new javax.swing.JComboBox<>();
        cmbPerfilTeclado = new javax.swing.JComboBox<>();
        btnNuevaCaja = new javax.swing.JButton();
        btnNuevoPerfil = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMapeo = new javax.swing.JTable();
        btnAgregarTegla = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("SELECCIÓN DE POS / CAJA"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Caja / Terminal:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel2.setText("Perfil Teclado:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(29, 60, -1, -1));

        cmbCajaTerminal.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCajaTerminal.addItemListener(this::cmbCajaTerminalItemStateChanged);
        cmbCajaTerminal.addActionListener(this::cmbCajaTerminalActionPerformed);
        jPanel2.add(cmbCajaTerminal, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 30, 540, -1));

        cmbPerfilTeclado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbPerfilTeclado.addItemListener(this::cmbPerfilTecladoItemStateChanged);
        jPanel2.add(cmbPerfilTeclado, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 540, -1));

        btnNuevaCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnNuevaCaja.setBorderPainted(false);
        btnNuevaCaja.setContentAreaFilled(false);
        btnNuevaCaja.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevaCaja.setFocusPainted(false);
        btnNuevaCaja.addActionListener(this::btnNuevaCajaActionPerformed);
        jPanel2.add(btnNuevaCaja, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 25, -1, -1));

        btnNuevoPerfil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar-archivo.png"))); // NOI18N
        btnNuevoPerfil.setBorderPainted(false);
        btnNuevoPerfil.setContentAreaFilled(false);
        btnNuevoPerfil.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevoPerfil.setFocusPainted(false);
        btnNuevoPerfil.addActionListener(this::btnNuevoPerfilActionPerformed);
        jPanel2.add(btnNuevoPerfil, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 55, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 110));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("MAPEADO DE TECLAS Y PRODUCTOS RÁPIDOS"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblMapeo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Tecla", "Acción / Función", "Producto Asignado", "Estado"
            }
        ));
        tblMapeo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblMapeo);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 260));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 150, 740, 290));

        btnAgregarTegla.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAgregarTegla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnAgregarTegla.setText("AGREGAR TECLA");
        btnAgregarTegla.setToolTipText("");
        btnAgregarTegla.setBorderPainted(false);
        btnAgregarTegla.setContentAreaFilled(false);
        btnAgregarTegla.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAgregarTegla.setFocusPainted(false);
        btnAgregarTegla.addActionListener(this::btnAgregarTeglaActionPerformed);
        pnlPrincipal.add(btnAgregarTegla, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 470, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 470, -1, -1));

        btnModificar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnModificar.setText("MODIFICAR");
        btnModificar.setBorderPainted(false);
        btnModificar.setContentAreaFilled(false);
        btnModificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnModificar.setFocusPainted(false);
        btnModificar.addActionListener(this::btnModificarActionPerformed);
        pnlPrincipal.add(btnModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 470, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlPrincipal.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 470, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("CONFIGURACIÓN DE TECLAS RÁPIDAS Y CÁMARA KEY (POS");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAgregarTeglaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarTeglaActionPerformed
        botonAgregarTecla();
    }//GEN-LAST:event_btnAgregarTeglaActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        botonModificar();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void cmbCajaTerminalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCajaTerminalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCajaTerminalActionPerformed

    private void cmbCajaTerminalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbCajaTerminalItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            cargarMapeoTabla();
        }
    }//GEN-LAST:event_cmbCajaTerminalItemStateChanged

    private void cmbPerfilTecladoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbPerfilTecladoItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            cargarMapeoTabla();
        }
    }//GEN-LAST:event_cmbPerfilTecladoItemStateChanged

    private void btnNuevaCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaCajaActionPerformed
        botonNuevaCaja();
    }//GEN-LAST:event_btnNuevaCajaActionPerformed

    private void btnNuevoPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoPerfilActionPerformed
        botonNuevoPerfil();
    }//GEN-LAST:event_btnNuevoPerfilActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregarTegla;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevaCaja;
    private javax.swing.JButton btnNuevoPerfil;
    private javax.swing.JComboBox<String> cmbCajaTerminal;
    private javax.swing.JComboBox<String> cmbPerfilTeclado;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblMapeo;
    // End of variables declaration//GEN-END:variables
}
