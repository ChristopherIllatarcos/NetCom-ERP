package vista.compras;

import javax.swing.table.DefaultTableModel;

public class IFrmProveedores extends javax.swing.JInternalFrame {

    private final dao.ProveedorDAO proveedorDAO = new dao.ProveedorDAO();
    private java.util.List<modelo.Proveedor> listaProveedores = new java.util.ArrayList<>();

    public IFrmProveedores() {

        initComponents();
        colorBotones();
        quitarBordeBarra();
        javax.swing.ButtonGroup grupoBusqueda = new javax.swing.ButtonGroup();
        grupoBusqueda.add(rbdNombre);
        grupoBusqueda.add(rbdRut);
        rbdNombre.setSelected(true); // Selección por defecto: Nombre
        configurarEventoBusqueda();
        configurarVentana();
    }

    private void aplicarRenderizadorTabla() {
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                int rowModel = table.convertRowIndexToModel(row);
                // La columna 'Activo' es el índice 6 en nuestro modelo de tabla
                String estadoActivo = table.getModel().getValueAt(rowModel, 6).toString();

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    if ("SÍ".equals(estadoActivo)) {
                        c.setBackground(new java.awt.Color(220, 247, 220)); // Verde claro
                        c.setForeground(new java.awt.Color(0, 100, 0));      // Texto verde oscuro
                    } else {
                        c.setBackground(new java.awt.Color(255, 220, 220)); // Rojo claro
                        c.setForeground(new java.awt.Color(150, 0, 0));      // Texto rojo oscuro
                    }
                }
                return c;
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

    private void botonEditar() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(
                    this,
                    "Seleccione un proveedor de la lista editar."
            );
            return;
        }

        // Convertimos el índice de la fila en caso de ordenamiento
        int filaModelo = jTable1.convertRowIndexToModel(filaVista);
        int idProveedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

        // Buscamos el proveedor en la lista por su ID
        modelo.Proveedor proveedorAEditar = null;
        for (modelo.Proveedor p : listaProveedores) {
            if (p.getId() == idProveedor) {
                proveedorAEditar = p;
                break;
            }
        }

        if (proveedorAEditar != null) {
            // Obtenemos el Frame padre de forma segura
            java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);

            // Instanciamos corrigiendo el nombre del paquete (proveedores.jdialog)
            vista.compras.DFrmProveedor dialog
                    = new vista.compras.DFrmProveedor(parentFrame, true, proveedorAEditar);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // Refrescamos la tabla tras guardar los cambios
            cargarTablaProveedores();
        }
    }

    private void botonEliminar() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(
                    this,
                    "Seleccione un proveedor de la lista para desactivar/eliminar."
            );
            return;
        }

        boolean confirmacion = utilidades.AlertaUtil.mostrarConfirmacion(
                this,
                "Confirmar Desactivación",
                "¿Está seguro de desactivar al proveedor seleccionado?"
        );

        if (confirmacion) {
            int filaModelo = jTable1.convertRowIndexToModel(filaVista);
            int idProveedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

            boolean exito = proveedorDAO.eliminarProveedor(idProveedor);

            if (exito) {

                utilidades.AlertaUtil.mostrarExito(
                        this,
                        "Proveedor desactivado correctamente."
                );
                cargarTablaProveedores(); // Refresca la tabla y lo pinta en rojo
            } else {

                utilidades.AlertaUtil.mostrarError(
                        this,
                        "No se pudo desactivar el proveedor."
                );
            }
        }
    }

    private void botonNuevo() {
        java.awt.Frame parentFrame = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Llamado pasando solo 2 parámetros
        vista.compras.DFrmProveedor dialog
                = new vista.compras.DFrmProveedor(parentFrame, true);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        cargarTablaProveedores();
    }

    private void botonVerFicha() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(
                    this,
                    "Seleccione un proveedor para ver su ficha"
            );
            return;
        }

        int filaModelo = jTable1.convertRowIndexToModel(filaVista);
        int idProveedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

        modelo.Proveedor proveedorSeleccionado = null;
        for (modelo.Proveedor p : listaProveedores) {
            if (p.getId() == idProveedor) {
                proveedorSeleccionado = p;
                break;
            }
        }

        if (proveedorSeleccionado != null) {
            vista.compras.DFrmProveedor dialog
                    = new vista.compras.DFrmProveedor((java.awt.Frame) null, true, proveedorSeleccionado);
            dialog.setVisible(true);
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        String criterio = rbdRut.isSelected() ? "rut" : "nombre";

        if (texto.isEmpty()) {
            cargarTablaProveedores(); // Si la búsqueda está vacía, lista todos
            return;
        }

        // Buscar en la BD con filtro
        this.listaProveedores = proveedorDAO.buscarProveedores(criterio, texto);

        DefaultTableModel modeloTabla = (DefaultTableModel) jTable1.getModel();
        modeloTabla.setRowCount(0);

        for (modelo.Proveedor p : listaProveedores) {
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getRut() != null ? p.getRut() : "S/RUT",
                p.getNombre(),
                p.getCiudad() != null ? p.getCiudad() : "-",
                p.getTelefono() != null ? p.getTelefono() : "-",
                p.getContacto() != null ? p.getContacto() : "-",
                p.isActivo() ? "SÍ" : "NO"
            });
        }

        lblTotalProvedores.setText("Total Proveedores: " + listaProveedores.size());
    }

    public void cargarTablaProveedores() {
        // Definimos la estructura del modelo
        DefaultTableModel modeloTabla = new DefaultTableModel(
                new String[]{"ID", "RUT", "Nombre Proveedor", "Ciudad", "Teléfono", "Contacto", "Activo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla de solo lectura
            }
        };

        // Consultamos la BD usando el DAO
        this.listaProveedores = proveedorDAO.listarProveedores();

        // Poblar las filas
        for (modelo.Proveedor p : listaProveedores) {
            modeloTabla.addRow(new Object[]{
                p.getId(),
                p.getRut() != null ? p.getRut() : "S/RUT",
                p.getNombre(),
                p.getCiudad() != null ? p.getCiudad() : "-",
                p.getTelefono() != null ? p.getTelefono() : "-",
                p.getContacto() != null ? p.getContacto() : "-",
                p.isActivo() ? "SÍ" : "NO"
            });
        }

        jTable1.setModel(modeloTabla);

        // Ocultar la columna ID (Índice 0)
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);

        // Actualizar total de registros
        lblTotalProvedores.setText("Total Proveedores: " + listaProveedores.size());

        // Aplicar Renderizador para pintar de Verde (SÍ) / Rojo (NO)
        aplicarRenderizadorTabla();
    }

    private void configurarEventoBusqueda() {
        // Configurar el evento de búsqueda dinámica al escribir en txtBuscar
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                buscar();
            }
        });
    }

    private void configurarVentana() {
        this.setTitle("MAESTRO DE PROVEEDORES");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void colorBotones() {
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);

        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnVerFicha, naranjaSuave);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlPrincipal = new javax.swing.JPanel();
        pnlBusquedaControl = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        rbdNombre = new javax.swing.JRadioButton();
        rbdRut = new javax.swing.JRadioButton();
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlListado = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnVerFicha = new javax.swing.JButton();
        lblTotalProvedores = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlBusquedaControl.setBackground(new java.awt.Color(255, 255, 255));
        pnlBusquedaControl.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA Y CONTROL"));
        pnlBusquedaControl.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        jLabel1.setText("Buscar:");
        pnlBusquedaControl.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 33, -1, -1));
        pnlBusquedaControl.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(95, 34, 260, -1));

        jLabel2.setText("Filtrar por:");
        pnlBusquedaControl.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));

        buttonGroup1.add(rbdNombre);
        rbdNombre.setText("Nombre");
        pnlBusquedaControl.add(rbdNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 80, -1, -1));

        buttonGroup1.add(rbdRut);
        rbdRut.setText("RUT");
        pnlBusquedaControl.add(rbdRut, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 80, -1, -1));

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
        pnlBusquedaControl.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 30, -1, -1));

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
        pnlBusquedaControl.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(477, 30, -1, -1));

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
        pnlBusquedaControl.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(595, 30, -1, -1));

        pnlPrincipal.add(pnlBusquedaControl, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 730, 120));

        pnlListado.setBackground(new java.awt.Color(255, 255, 255));
        pnlListado.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE PROVEEDORES"));
        pnlListado.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "R.U.T.", "Nombre / Razón Social", "Ciudad", "Teléfono"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        pnlListado.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 715, 250));

        pnlPrincipal.add(pnlListado, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 735, 280));

        btnVerFicha.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVerFicha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ficha-tecnica.png"))); // NOI18N
        btnVerFicha.setText("VER FICHA");
        btnVerFicha.setBorderPainted(false);
        btnVerFicha.setContentAreaFilled(false);
        btnVerFicha.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVerFicha.setFocusPainted(false);
        btnVerFicha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnVerFichaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnVerFichaMouseExited(evt);
            }
        });
        btnVerFicha.addActionListener(this::btnVerFichaActionPerformed);
        pnlPrincipal.add(btnVerFicha, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 460, -1, -1));

        lblTotalProvedores.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalProvedores.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalProvedores.setText("Total Proveedores");
        pnlPrincipal.add(lblTotalProvedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 460, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("MAESTRO DE PROVEEDORES");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseEntered
        // jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnNuevoMouseEntered

    private void btnNuevoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseExited
        //jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnNuevoMouseExited

    private void btnEditarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseEntered
        //jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnEditarMouseEntered

    private void btnEditarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseExited
        //jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnEditarMouseExited

    private void btnEliminarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseEntered
        //jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnEliminarMouseEntered

    private void btnEliminarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseExited
        //jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnEliminarMouseExited

    private void btnVerFichaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerFichaMouseEntered
        //jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnVerFichaMouseEntered

    private void btnVerFichaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerFichaMouseExited
        //  jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnVerFichaMouseExited

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        botonNuevo();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnVerFichaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerFichaActionPerformed
        botonVerFicha();
    }//GEN-LAST:event_btnVerFichaActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVerFicha;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTotalProvedores;
    private javax.swing.JPanel pnlBusquedaControl;
    private javax.swing.JPanel pnlListado;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rbdNombre;
    private javax.swing.JRadioButton rbdRut;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
