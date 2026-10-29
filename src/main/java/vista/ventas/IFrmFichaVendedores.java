package vista.ventas;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class IFrmFichaVendedores extends javax.swing.JInternalFrame {

    private dao.VendedorDAO vendedorDAO = new dao.VendedorDAO();
    private List<modelo.Vendedor> listaVendedores = new ArrayList<>();
    private javax.swing.ButtonGroup bgFiltro = new javax.swing.ButtonGroup();

    public IFrmFichaVendedores() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        aplicarRenderizador();
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

    private void aplicarRenderizador() {
        // Aplicar renderizador de colores a todas las columnas de la tabla
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // Convertimos el índice por si la tabla está ordenada
                int rowModel = table.convertRowIndexToModel(row);

                // La columna 'Activo' es la última en el modelo (índice 4)
                String estadoActivo = table.getModel().getValueAt(rowModel, 4).toString();

                if (isSelected) {
                    // Color cuando la fila está seleccionada por el usuario
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    // Si la fila no está seleccionada, aplicamos los colores suave
                    if ("SÍ".equals(estadoActivo)) {
                        c.setBackground(new java.awt.Color(220, 247, 220)); // Verde claro
                        c.setForeground(new java.awt.Color(0, 100, 0));      // Texto verde oscuro
                    } else {
                        c.setBackground(new java.awt.Color(255, 220, 220)); // Rojo / Rosado claro
                        c.setForeground(new java.awt.Color(150, 0, 0));      // Texto rojo oscuro
                    }
                }

                return c;
            }
        });

        // 1. Agrupar Radio Buttons
        bgFiltro.add(rdbNombre);
        bgFiltro.add(rdbCodigo);
        rdbNombre.setSelected(true); // Seleccionado por defecto

        // 2. Cargar datos iniciales
        cargarTablaVendedores();

        // 3. Búsqueda en tiempo real al escribir en txtBuscar
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarTabla();
            }
        });

    }

    private void botonNuevo() {
        // Abre el diálogo o frame para registrar un nuevo vendedor
        DFrmVendedor dialog = new DFrmVendedor(null, true);
        dialog.setVisible(true);
        cargarTablaVendedores(); // Refresca al cerrar el formulario  
    }

    private void botonEditar() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un vendedor de la lista para editar.");
            return;
        }

        // Convertir el índice de la fila en caso de que la tabla esté ordenada/filtrada
        int filaModelo = jTable1.convertRowIndexToModel(filaVista);

        // Obtener el ID de la columna 0 del modelo
        int idVendedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

        // Buscar el objeto Vendedor correspondiente en la lista
        modelo.Vendedor vendedorAEditar = null;
        for (modelo.Vendedor v : listaVendedores) {
            if (v.getId() == idVendedor) {
                vendedorAEditar = v;
                break;
            }
        }

        // Instanciar pasando el objeto Vendedor
        if (vendedorAEditar != null) {
            vista.ventas.DFrmVendedor dialog
                    = new vista.ventas.DFrmVendedor((java.awt.Frame) null, true, vendedorAEditar);
            dialog.setVisible(true);
            cargarTablaVendedores(); // Refresca la tabla después de guardar cambios
        }
    }

    private void botonEliminar() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un vendedor de la lista para eliminar.");
            return;
        }

        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                this,
                "¿Está seguro de desactivar al vendedor seleccionado?"
        );

        if (confirmar) {
            // Obtenemos el ID de la fila seleccionada
            int filaModelo = jTable1.convertRowIndexToModel(filaVista);
            int idVendedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

            // Ejecutamos la baja lógica
            boolean exito = vendedorDAO.eliminarVendedor(idVendedor);

            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Vendedor desactivado correctamente.");
                // REFRESCAR LA TABLA (Clave para actualizar la vista)
                cargarTablaVendedores();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "No se pudo deshabilitar el vendedor, contacte a soporte.");
            }
        }
    }

    private void botonVerFicha() {
        int filaVista = jTable1.getSelectedRow();
        if (filaVista == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione un vendedor para ver su ficha.");
            return;
        }

        int filaModelo = jTable1.convertRowIndexToModel(filaVista);
        int idVendedor = Integer.parseInt(jTable1.getModel().getValueAt(filaModelo, 0).toString());

        modelo.Vendedor vendedorSeleccionado = null;
        for (modelo.Vendedor v : listaVendedores) {
            if (v.getId() == idVendedor) {
                vendedorSeleccionado = v;
                break;
            }
        }

        if (vendedorSeleccionado != null) {
            vista.ventas.DFrmVendedor dialog
                    = new vista.ventas.DFrmVendedor((java.awt.Frame) null, true, vendedorSeleccionado);
            dialog.setVisible(true);
        }
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void colorBotones() {
        // Verde claro suave para Guardar / Aceptar
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        // Rojo claro suave para Cancelar / Cerrar / Eliminar
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);

        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);

        // java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnEditar, azulSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnVerFicha, verdeSuave);
        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
    }

    private void configurarVentana() {
        this.setTitle("FICHA DE VENDEDORES");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    public void cargarTablaVendedores() {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Código", "Nombre del Vendedor", "Comisión Cobranza (%)", "Activo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        // Consultamos la BD usando el DAO
        this.listaVendedores = vendedorDAO.listarVendedores();

        for (modelo.Vendedor v : listaVendedores) {
            modelo.addRow(new Object[]{
                v.getId(),
                v.getCodigo() != null ? v.getCodigo() : "S/C",
                v.getNombre(),
                String.format("%.2f %%", v.getComisionCobranza()), // Usa comision_cobranza
                v.isActivo() ? "SÍ" : "NO"
            });
        }

        jTable1.setModel(modelo);

        // Ocultar columna ID si no deseas mostrarla al usuario
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);

        // Actualizar total de registros
        lblTotalRegistros.setText("Total Registros: " + listaVendedores.size());
    }

    private void filtrarTabla() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0); // Limpiar filas actuales

        int contador = 0;

        for (modelo.Vendedor v : listaVendedores) {
            boolean coincide = false;

            if (rdbNombre.isSelected()) {
                coincide = v.getNombre() != null && v.getNombre().toLowerCase().contains(texto);
            } else if (rdbCodigo.isSelected()) {
                coincide = v.getCodigo() != null && v.getCodigo().toLowerCase().contains(texto);
            }

            if (coincide) {
                modelo.addRow(new Object[]{
                    v.getId(),
                    v.getCodigo() != null ? v.getCodigo() : "S/C",
                    v.getNombre(),
                    String.format("%.2f %%", v.getComisionCobranza()),
                    v.isActivo() ? "SÍ" : "NO"
                });
                contador++;
            }
        }

        lblTotalRegistros.setText("Total Registros: " + contador);
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        rdbNombre = new javax.swing.JRadioButton();
        rdbCodigo = new javax.swing.JRadioButton();
        btnNuevo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnVerFicha = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnCerrar = new javax.swing.JButton();
        lblTotalRegistros = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BUSCADOR Y ACCIONES"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Buscar:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 32, -1, -1));
        jPanel1.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(67, 29, 354, -1));

        jLabel2.setText("Filtrar por:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(439, 32, -1, -1));

        buttonGroup1.add(rdbNombre);
        rdbNombre.setText("Nombre");
        rdbNombre.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(rdbNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(511, 30, -1, -1));

        buttonGroup1.add(rdbCodigo);
        rdbCodigo.setText("Código");
        rdbCodigo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(rdbCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(596, 30, -1, -1));

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
        jPanel1.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(67, 69, -1, -1));

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
        jPanel1.add(btnEditar, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 69, -1, -1));

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
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(302, 69, -1, -1));

        btnVerFicha.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnVerFicha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/ojo.png"))); // NOI18N
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
        jPanel1.add(btnVerFicha, new org.netbeans.lib.awtextra.AbsoluteConstraints(434, 69, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 35, 740, 130));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE VENDEDORES"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Código", "Nombre del Vendedor", "Comisión (%)"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 18, 718, 220));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 190, 740, 250));

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
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, -1, -1));

        lblTotalRegistros.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalRegistros.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalRegistros.setText("0");
        pnlPrincipal.add(lblTotalRegistros, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 470, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("FICHA DE VENDEDORES");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 12, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseEntered
        //  jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnNuevoMouseEntered

    private void btnNuevoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNuevoMouseExited
        // jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnNuevoMouseExited

    private void btnEditarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseEntered
        //  jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnEditarMouseEntered

    private void btnEditarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditarMouseExited
        // jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnEditarMouseExited

    private void btnEliminarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseEntered
        // jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnEliminarMouseEntered

    private void btnEliminarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarMouseExited
        // jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnEliminarMouseExited

    private void btnVerFichaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerFichaMouseEntered
        //  jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnVerFichaMouseEntered

    private void btnVerFichaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVerFichaMouseExited
        // jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnVerFichaMouseExited

    private void btnCerrarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseEntered
        //jSeparator5.setVisible(true);
    }//GEN-LAST:event_btnCerrarMouseEntered

    private void btnCerrarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseExited
        // jSeparator5.setVisible(false);
    }//GEN-LAST:event_btnCerrarMouseExited

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        botonNuevo();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        botonEditar();
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnVerFichaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerFichaActionPerformed
        botonVerFicha();
    }//GEN-LAST:event_btnVerFichaActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnVerFicha;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rdbCodigo;
    private javax.swing.JRadioButton rdbNombre;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables
}
