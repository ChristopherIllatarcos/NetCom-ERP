package vista.bodegas;

import dao.FamiliasDAO;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.FamiliaDTO;

/**
 *
 * @author caill
 */
public class IFrmRegistroFamilias extends javax.swing.JInternalFrame {

    private final FamiliasDAO familiasDAO;
    private int idFamiliaSeleccionada = -1;

    public IFrmRegistroFamilias() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        familiasDAO = new FamiliasDAO();
        cargarTablaFamilias();
        prepararNuevo();
        configuracionVentana();
        eventoSeleccionTabla();

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

    private void botonCerrar() {
        this.dispose();
    }

    private void botonEliminar() {
        if (idFamiliaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione una familia de la tabla para eliminar");
            return;
        }

        boolean confirm = utilidades.AlertaUtil.mostrarConfirmacion(
                this,
                "Confirmar Eliminación",
                "¿Está seguro de eliminar esta familia?"
        );

        if (confirm) {
            if (familiasDAO.eliminarFamilia(idFamiliaSeleccionada)) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Familia eliminada correctamente!");
                cargarTablaFamilias();
                prepararNuevo();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "No se pudo eliminar (puede estar asociada a productos).");
            }
        }
    }

    private void botonGuardar() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "El nombre de la familia es obligatorio.");
            txtNombre.requestFocus();
            return;
        }

        if (idFamiliaSeleccionada == -1) {
            // Guardar nuevo registro
            if (familiasDAO.guardarFamilia(codigo, nombre, descripcion)) {
                utilidades.AlertaUtil.mostrarExito(this, "¡Familia guardada con éxito!");
                cargarTablaFamilias();
                prepararNuevo();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al guardar en la base de datos.");
            }
        } else {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Para modificar un registro existente, utilice el botón MODIFICAR.");
        }
    }

    private void botonModificar() {
        if (idFamiliaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Seleccione una familia de la tabla para modificar.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();

        if (nombre.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "El nombre no puede estar vacío.");
            return;
        }

        if (familiasDAO.actualizarFamilia(idFamiliaSeleccionada, nombre, descripcion)) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Familia actualizada con éxito!");
            cargarTablaFamilias();
            prepararNuevo();
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al actualizar la familia.");
        }
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnNuevo, grisSuave);
        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnEliminar, rojoSuave);
        aplicarEfectoHoverPlano(btnModificar, azulSuave);
        aplicarEfectoHoverPlano(btnCerrar, oscuroSuave);

    }

    private void configuracionVentana() {
        this.setTitle("REGISTRO / MANTENEDOR DE FAMILIAS DE PRODUCTOS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarTablaFamilias() {
        // 1. Definimos las 3 columnas exactas que tienes en tu diseño visual
        DefaultTableModel modelo = new DefaultTableModel(new Object[]{"CODIGO", "NOMBRE", "DESCRIPCION"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };

        List<modelo.FamiliaDTO> lista = familiasDAO.listarFamilias();

        for (modelo.FamiliaDTO f : lista) {
            // Pasamos un objeto que lleva el ID oculto en el toString o usamos un truco de filas
            modelo.addRow(new Object[]{
                f.getCodigo(),
                f.getNombre(),
                f.getDescripcion()
            });
        }

        tblFamiliaRegistrada.setModel(modelo);
        lblTotalFamilia.setText(String.valueOf(lista.size()));
    }

    private void eventoSeleccionTabla() {
        tblFamiliaRegistrada.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFamiliaRegistradaMouseClicked(evt);
            }
        });
    }

    private void prepararNuevo() {
        idFamiliaSeleccionada = -1;
        lblId.setText("Nuevo");
        txtCodigo.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtCodigo.setEditable(true);
        txtCodigo.requestFocus();
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void tablaFamilia() {
        int fila = tblFamiliaRegistrada.getSelectedRow();
        if (fila >= 0) {
            // Obtenemos el código seleccionado en la tabla
            String codigoSeleccionado = tblFamiliaRegistrada.getValueAt(fila, 0).toString();

            // Usamos modelo.FamiliaDTO gracias a tu nueva estructura limpia
            List<modelo.FamiliaDTO> lista = familiasDAO.listarFamilias();
            for (modelo.FamiliaDTO f : lista) {
                if (f.getCodigo().equals(codigoSeleccionado)) {
                    idFamiliaSeleccionada = f.getId(); // ¡Recuperamos el ID real de la BD!
                    lblId.setText(String.valueOf(idFamiliaSeleccionada));
                    txtCodigo.setText(f.getCodigo());
                    txtNombre.setText(f.getNombre());
                    txtDescripcion.setText(f.getDescripcion());
                    break;
                }
            }

            txtCodigo.setEditable(false);
        }
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
        txtCodigo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        btnNuevo = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        lblId = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFamiliaRegistrada = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        lblTotalFamilia = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setPreferredSize(new java.awt.Dimension(770, 540));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS DE LA FAMILIA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Código:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 30, -1, -1));
        jPanel1.add(txtCodigo, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 30, 200, -1));

        jLabel2.setText("(Autogenerado o Manual)");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 30, -1, -1));

        jLabel3.setText("Descripción:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 70, -1, -1));

        txtDescripcion.addActionListener(this::txtDescripcionActionPerformed);
        jPanel1.add(txtDescripcion, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 70, 350, -1));

        btnNuevo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setBorderPainted(false);
        btnNuevo.setContentAreaFilled(false);
        btnNuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(this::btnNuevoActionPerformed);
        jPanel1.add(btnNuevo, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 120, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        jPanel1.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 120, -1, -1));

        btnModificar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnModificar.setText("MODIFICAR");
        btnModificar.setBorderPainted(false);
        btnModificar.setContentAreaFilled(false);
        btnModificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnModificar.setFocusPainted(false);
        btnModificar.addActionListener(this::btnModificarActionPerformed);
        jPanel1.add(btnModificar, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 120, -1, -1));

        btnEliminar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setBorderPainted(false);
        btnEliminar.setContentAreaFilled(false);
        btnEliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(this::btnEliminarActionPerformed);
        jPanel1.add(btnEliminar, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 120, -1, -1));

        jLabel7.setText("ID:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 30, -1, -1));

        lblId.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblId.setText("jLabel8");
        jPanel1.add(lblId, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 30, -1, -1));

        jLabel9.setText("Nombre:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));
        jPanel1.add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 70, 190, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 740, 180));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("FAMILIAS REGISTRADAS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblFamiliaRegistrada.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "NOMBRE", "CÓDIGO", "DESCRIPCIÓN"
            }
        ));
        tblFamiliaRegistrada.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblFamiliaRegistrada.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFamiliaRegistradaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblFamiliaRegistrada);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 25, 705, 170));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 225, 740, 210));

        jLabel6.setText("Total Familias:");
        pnlPrincipal.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 450, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 450, -1, -1));

        lblTotalFamilia.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalFamilia.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalFamilia.setText("jLabel7");
        pnlPrincipal.add(lblTotalFamilia, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 450, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("REGISTRO / MANTENEDOR DE FAMILIAS DE PRODUCTOS");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 758, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        prepararNuevo();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        botonModificar();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        botonEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void tblFamiliaRegistradaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFamiliaRegistradaMouseClicked
        tablaFamilia();
    }//GEN-LAST:event_tblFamiliaRegistradaMouseClicked

    private void txtDescripcionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescripcionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescripcionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblTotalFamilia;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblFamiliaRegistrada;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
