package vista.archivos;

import dao.ParametrosCorrelativosDAO;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.ItemCombo;

public class IFrmParametrosCorrelativos extends javax.swing.JInternalFrame {

    private final ParametrosCorrelativosDAO dao;
    private DefaultTableModel modeloTabla;

    public IFrmParametrosCorrelativos() {
        initComponents();
        colorBotones();
        quitarBordesBarra();
        dao = new ParametrosCorrelativosDAO();
        inicializarTablaCorrelativos();
        modeloTabla = (DefaultTableModel) tblListado.getModel();
        cargarCombos();
        cargarDatosEmpresa();
        dao.cargarCorrelativosTabla(modeloTabla);
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

    private void botonModificarCor() {
        int filaSeleccionada = tblListado.getSelectedRow();
        if (filaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor seleccione un documento de la tabla para modificar su correlativo.");
            return;
        }

        int idCorrelativo = Integer.parseInt(tblListado.getValueAt(filaSeleccionada, 0).toString());
        String tipoDoc = tblListado.getValueAt(filaSeleccionada, 1).toString();
        String actualStr = tblListado.getValueAt(filaSeleccionada, 3).toString();

      
        String nuevoValorStr = utilidades.InputUtil.mostrarInput(
                this,
                "Modificar Correlativo",
                "Ingrese el nuevo correlativo actual para [" + tipoDoc + "]:",
                actualStr
        );

        if (nuevoValorStr != null && !nuevoValorStr.trim().isEmpty()) {
            try {
                int nuevoCorrelativo = Integer.parseInt(nuevoValorStr.trim());
                boolean actualizado = dao.actualizarCorrelativo(idCorrelativo, nuevoCorrelativo);

                if (actualizado) {
                   
                    utilidades.AlertaUtil.mostrarExito(this, "Correlativo actualizado con éxito.");
                    dao.cargarTablaCorrelativos(modeloTabla);
                } else {
                    
                    utilidades.AlertaUtil.mostrarError(this, "No se pudo actualizar el correlativo.");
                }
            } catch (NumberFormatException e) {
               
                utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe ingresar un número entero válido.");
            }
        }
    }

    private void botoncerrar() {
        this.dispose();
    }

    private void botonGuardar() {
        // 1. Obtener valores de la empresa desde la interfaz
        String razon = txtRazonSocial.getText().trim();
        String rut = txtRutEmpresa.getText().trim();
        String direccion = txtDireccionMatriz.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String giro = txtGiro.getText().trim();
        String comuna = txtComuna.getText().trim();
        String email = txtEmail.getText().trim();

        String caja = "";
        if (cmbCajaDefecto.getSelectedItem() instanceof modelo.ItemCombo itemCaja) {
            caja = itemCaja.getDescripcion();
        }

        String bodega = "";
        if (cmbBodega.getSelectedItem() instanceof modelo.ItemCombo itemBodega) {
            bodega = itemBodega.getDescripcion();
        }

        // 2. Guardar/Actualizar en la BD
        boolean exito = dao.guardarParametrosEmpresa(razon, rut, direccion, telefono, caja, giro, comuna, email, bodega);

        if (exito) {
           utilidades.AlertaUtil.mostrarExito(this, "Datos de la empresa guardados correctamente.");

            // 3. Sincronizar y refrescar la vista
            cargarDatosEmpresa();
            modeloTabla.setRowCount(0);
            dao.cargarCorrelativosTabla(modeloTabla);

        } else {
            utilidades.AlertaUtil.mostrarError(this, "Error al guardar los datos de la empresa en la base de datos.");
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
        aplicarEfectoHoverPlano(btnModificarCor, azulSuave);

    }

    private void configuracionVentana() {
        this.setTitle("PARÁMETROS DEL SISTEMA Y CONTROL DE CORRELATIVOS");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarCombos() {
        // 1. Cargar Bodegas en cmbBodega
        List<ItemCombo> bodegas = dao.listarBodegasCombo();
        DefaultComboBoxModel modelBodega = new DefaultComboBoxModel(); // Sin tipar estrictamente para evitar conflicto con Swing
        for (ItemCombo item : bodegas) {
            modelBodega.addElement(item);
        }
        cmbBodega.setModel(modelBodega);

        // 2. Cargar Cajas en cmbCajaDefecto
        List<ItemCombo> cajas = dao.listarCajasCombo();
        DefaultComboBoxModel modelCaja = new DefaultComboBoxModel(); // Sin tipar estrictamente
        for (ItemCombo item : cajas) {
            modelCaja.addElement(item);
        }
        cmbCajaDefecto.setModel(modelCaja);
    }

    private void cargarDatosEmpresa() {
        String[] datos = dao.obtenerParametrosEmpresa();
        if (datos[0] != null) {
            txtRazonSocial.setText(datos[0]);
            txtRutEmpresa.setText(datos[1]);
            txtDireccionMatriz.setText(datos[2]);
            txtTelefono.setText(datos[3]);

            if (datos[4] != null) {
                seleccionarItemCombo(cmbCajaDefecto, datos[4]);
            }

            txtGiro.setText(datos[5]);
            txtComuna.setText(datos[6]);
            txtEmail.setText(datos[7]);

            if (datos[8] != null) {
                seleccionarItemCombo(cmbBodega, datos[8]);
            }
        }
    }

    private void inicializarTablaCorrelativos() {
        String[] columnas = {"ID", "Tipo Documento", "Prefijo", "Correlativo Actual", "Estado"};

        // Inicializamos el modelo con las columnas y 0 filas iniciales
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hace que las celdas no sean editables directamente al hacerles clic
            }
        };

        // Asignamos el modelo a la JTable de la interfaz
        tblListado.setModel(modeloTabla);
    }

    private void quitarBordesBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void seleccionarItemCombo(javax.swing.JComboBox<?> combo, String valor) {
        if (valor == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof modelo.ItemCombo itemCombo) {
                if (itemCombo.getDescripcion().equalsIgnoreCase(valor)) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
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
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtRazonSocial = new javax.swing.JTextField();
        txtRutEmpresa = new javax.swing.JTextField();
        txtDireccionMatriz = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        cmbCajaDefecto = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtGiro = new javax.swing.JTextField();
        txtComuna = new javax.swing.JTextField();
        cmbBodega = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblListado = new javax.swing.JTable();
        btnCerrar = new javax.swing.JButton();
        btnModificarCor = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Pestaña 1: DATOS EMPRESA - Pestaña 2: CORRELATIVOS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Razón Social:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 40, -1, -1));

        jLabel2.setText("R.U.T. Empresa:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 70, -1, -1));

        jLabel3.setText("Dirección Matriz:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(22, 100, -1, -1));

        jLabel4.setText("Teléfono: ");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 130, -1, -1));

        jLabel5.setText("Caja Defecto:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(38, 160, -1, -1));
        jPanel1.add(txtRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 600, -1));
        jPanel1.add(txtRutEmpresa, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 70, 260, -1));
        jPanel1.add(txtDireccionMatriz, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 100, 260, -1));
        jPanel1.add(txtTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 130, 260, -1));

        cmbCajaDefecto.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbCajaDefecto, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 160, 260, -1));

        jLabel6.setText("Giro:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(422, 70, -1, -1));

        jLabel7.setText("Comuna:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(398, 100, -1, -1));
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 130, 260, -1));

        jLabel8.setText("Bodega:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 160, -1, -1));
        jPanel1.add(txtGiro, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 70, 260, -1));
        jPanel1.add(txtComuna, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 100, 260, -1));

        cmbBodega.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(cmbBodega, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 160, 260, -1));

        jLabel9.setText("Email:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(415, 130, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 50, 740, 200));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE CORRELATIVOS DE DOCUMENTOS"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblListado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tblListado.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(tblListado);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 170));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 250, 740, 200));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 460, -1, -1));

        btnModificarCor.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnModificarCor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/editar.png"))); // NOI18N
        btnModificarCor.setText("MODIFICAR CORRELATIVO");
        btnModificarCor.setBorderPainted(false);
        btnModificarCor.setContentAreaFilled(false);
        btnModificarCor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnModificarCor.setFocusPainted(false);
        btnModificarCor.addActionListener(this::btnModificarCorActionPerformed);
        pnlPrincipal.add(btnModificarCor, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 460, -1, -1));

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);
        pnlPrincipal.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 460, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("PARÁMETROS DEL SISTEMA Y CONTROL DE CORRELATIVOS");
        pnlPrincipal.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnModificarCorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarCorActionPerformed
        botonModificarCor();
    }//GEN-LAST:event_btnModificarCorActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botoncerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        botonGuardar();
    }//GEN-LAST:event_btnGuardarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnModificarCor;
    private javax.swing.JComboBox<String> cmbBodega;
    private javax.swing.JComboBox<String> cmbCajaDefecto;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblListado;
    private javax.swing.JTextField txtComuna;
    private javax.swing.JTextField txtDireccionMatriz;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtGiro;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtRutEmpresa;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
