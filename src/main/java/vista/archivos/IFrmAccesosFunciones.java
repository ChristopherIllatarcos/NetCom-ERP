package vista.archivos;

import dao.AccesoFuncionDAO;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.AccesoFuncion;
import utilidades.AlertaUtil;
import utilidades.UtilidadesPermisos;

public class IFrmAccesosFunciones extends javax.swing.JInternalFrame {

    private final AccesoFuncionDAO dao;
    private DefaultTableModel modeloTabla;
    private List<AccesoFuncion> listaPermisosActual;

    public IFrmAccesosFunciones() {
        initComponents();
        dao = new AccesoFuncionDAO();
        configurarTabla();
        cargarCombos();
        configurarEventosExtra();
        colorBotones();
        quitarBordesBarraTitulo();
        configuracionVentana();
        asignarEventoBtnAgregar();
    }
    
    private void asignarEventoBtnAgregar() {
        java.awt.event.ActionListener accionEnter = (ActionEvent e) -> {

            btnBuscar.doClick();
        };
        
        cmbPerfilRol.addActionListener(accionEnter);
        cmbPorUsuario.addActionListener(accionEnter);
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnGuardar, verdeSuave);
        aplicarEfectoHoverPlano(btnBuscar, azulSuave);
        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
    }

    private void quitarBordesBarraTitulo() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void configuracionVentana() {
        this.setTitle("CONTROL Y MATRIZ DE ACCESOS A FUNCIONES DEL SISTEMA");
        this.setSize(770, 540);
        this.setResizable(false);
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

    private void configurarTabla() {
        String[] columnas = {"Módulo", "Código", "Nombre de Función", "Permitido"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // La columna 4 (índice 3) es booleana para que se vea como un CheckBox en la JTable
                if (columnIndex == 3) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo la columna "Permitido" se puede editar directamente en la tabla
                return column == 3;
            }
        };
        tblPermisos.setModel(modeloTabla);
    }

    private void cargarCombos() {
        cmbPerfilRol.removeAllItems();
        cmbPerfilRol.addItem("Seleccione Rol...");
        for (String rol : dao.obtenerRoles()) {
            cmbPerfilRol.addItem(rol);
        }

        cmbPorUsuario.removeAllItems();
        cmbPorUsuario.addItem("Seleccione Usuario...");
        for (String user : dao.obtenerUsuarios()) {
            cmbPorUsuario.addItem(user);
        }
    }

    private void configurarEventosExtra() {
        cmbPerfilRol.addActionListener(e -> {
            if (cmbPerfilRol.getSelectedIndex() > 0) {
                cmbPorUsuario.setSelectedIndex(0);
            }
        });

        cmbPorUsuario.addActionListener(e -> {
            if (cmbPorUsuario.getSelectedIndex() > 0) {
                cmbPerfilRol.setSelectedIndex(0);
            }
        });
    }

    private void botonBuscar() {
        String rolSeleccionado = (cmbPerfilRol.getSelectedIndex() > 0) ? cmbPerfilRol.getSelectedItem().toString() : null;
        String usuarioSeleccionado = (cmbPorUsuario.getSelectedIndex() > 0) ? cmbPorUsuario.getSelectedItem().toString() : null;

        if (rolSeleccionado == null && usuarioSeleccionado == null) {
            AlertaUtil.mostrar("Selección requerida", "Debe seleccionar un Rol o un Usuario para buscar.", "advertencia");
            return;
        }

        String tipo = (rolSeleccionado != null) ? "ROL" : "USUARIO";
        String referencia = (rolSeleccionado != null) ? rolSeleccionado : usuarioSeleccionado;

        // Consultamos en la BD
        listaPermisosActual = dao.listarPermisos(tipo, referencia);

        // Si aún no tiene registros guardados en la BD, cargamos la plantilla base por defecto
        if (listaPermisosActual.isEmpty()) {
            listaPermisosActual = UtilidadesPermisos.obtenerFuncionesBase(tipo, referencia);
        }

        actualizarTablaUI();
    }

    private void actualizarTablaUI() {
        modeloTabla.setRowCount(0);
        for (AccesoFuncion af : listaPermisosActual) {
            Object[] fila = {
                af.getModulo(),
                af.getCodigoFuncion(),
                af.getNombreFuncion(),
                af.isPermitido()
            };
            modeloTabla.addRow(fila);
        }
    }

    private void botonGuardar() {
        if (listaPermisosActual == null || listaPermisosActual.isEmpty()) {
            AlertaUtil.mostrar("Sin datos", "No hay permisos cargados para guardar.", "advertencia");
            return;
        }

        // Sincronizamos los valores del modelo de la tabla con la lista de objetos
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            boolean permitidoTab = (boolean) modeloTabla.getValueAt(i, 3);
            listaPermisosActual.get(i).setPermitido(permitidoTab);
        }

        boolean exito = dao.guardarPermisos(listaPermisosActual);
        if (exito) {
            AlertaUtil.mostrar("Éxito", "Los permisos se han guardado correctamente.", "exito");
        } else {
            AlertaUtil.mostrar("Error", "Ocurrió un error al guardar los permisos en la base de datos.", "error");
        }
    }

    private void seleccionarTodo() {
        // Configuración para el CheckBox "Seleccionar Todo"
        cboSeleccionarTodo.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if (tblPermisos.isEditing()) {
                    tblPermisos.getCellEditor().stopCellEditing();
                }
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    modeloTabla.setValueAt(true, i, 3);
                }
                javax.swing.SwingUtilities.invokeLater(() -> cboSeleccionarTodo.setSelected(false));
            }
        });
    }

    private void desmarcarTodo() {
        cboDesmarcarTodos.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                if (tblPermisos.isEditing()) {
                    tblPermisos.getCellEditor().stopCellEditing();
                }
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    modeloTabla.setValueAt(false, i, 3);
                }
                // Regresa el checkbox a estado normal sin trabarse
                javax.swing.SwingUtilities.invokeLater(() -> cboDesmarcarTodos.setSelected(false));
            }
        });
    }

    private void botonCerrar() {
        this.dispose();
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbPerfilRol = new javax.swing.JComboBox<>();
        cmbPorUsuario = new javax.swing.JComboBox<>();
        btnBuscar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPermisos = new javax.swing.JTable();
        cboSeleccionarTodo = new javax.swing.JCheckBox();
        cboDesmarcarTodos = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("SELECCIÓN DE PERFIL / USUARIO"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Perfil / Rol:");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 30, -1, -1));

        jLabel2.setText("O por Usuario:");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        cmbPerfilRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cmbPerfilRol, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 510, -1));

        cmbPorUsuario.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel2.add(cmbPorUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 60, 510, -1));

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setText("BUSCAR");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel2.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 55, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 40, 740, 120));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("PERMISOS Y FUNCIONES ASIGNADAS"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblPermisos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblPermisos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblPermisos);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 230));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(12, 170, 740, 260));

        buttonGroup1.add(cboSeleccionarTodo);
        cboSeleccionarTodo.setText("Seleccionar Todo");
        cboSeleccionarTodo.addActionListener(this::cboSeleccionarTodoActionPerformed);
        pnlPrincipal.add(cboSeleccionarTodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, -1, -1));

        buttonGroup1.add(cboDesmarcarTodos);
        cboDesmarcarTodos.setText("Desmarcar Todo");
        cboDesmarcarTodos.addActionListener(this::cboDesmarcarTodosActionPerformed);
        pnlPrincipal.add(cboDesmarcarTodos, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 470, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlPrincipal.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 470, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 470, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("CONTROL Y MATRIZ DE ACCESOS A FUNCIONES DEL SISTEMA");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void cboSeleccionarTodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboSeleccionarTodoActionPerformed
        seleccionarTodo();
    }//GEN-LAST:event_cboSeleccionarTodoActionPerformed

    private void cboDesmarcarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboDesmarcarTodosActionPerformed
        desmarcarTodo();
    }//GEN-LAST:event_cboDesmarcarTodosActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox cboDesmarcarTodos;
    private javax.swing.JCheckBox cboSeleccionarTodo;
    private javax.swing.JComboBox<String> cmbPerfilRol;
    private javax.swing.JComboBox<String> cmbPorUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblPermisos;
    // End of variables declaration//GEN-END:variables

}
