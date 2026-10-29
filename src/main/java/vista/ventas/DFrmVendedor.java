package vista.ventas;

import dao.VendedorDAO;
import modelo.Vendedor;

public class DFrmVendedor extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DFrmVendedor.class.getName());
    private VendedorDAO vendedorDAO = new VendedorDAO();
    private Vendedor vendedorEditar = null;

    /**
     * Creates new form JDialogVendedor
     *
     * @param parent
     * @param modal
     */
    // CONSTRUCTOR 1: Para Crear Nuevo
    public DFrmVendedor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        colorBotones();
        this.setLocationRelativeTo(parent);
        this.setTitle("Nuevo Vendedor");
        chkActivo.setSelected(true);
    }

    // CONSTRUCTOR 2: Para Editar / Ver Ficha
    public DFrmVendedor(java.awt.Frame parent, boolean modal, Vendedor v) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setTitle("Editar Vendedor");
        this.vendedorEditar = v;
        cargarDatosEdicion();
    }

    private void colorBotones() {
        java.awt.Color rojoSave = new java.awt.Color(255, 218, 218);

        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);

        aplicarEfectoHoverPlano(btnCancelar, rojoSave);
        aplicarEfectoHoverPlano(btnGuardar, azulSuave);

    }

    private void cargarDatosEdicion() {
        if (vendedorEditar != null) {
            txtCodigo.setText(vendedorEditar.getCodigo() != null ? vendedorEditar.getCodigo() : "");
            txtNombre.setText(vendedorEditar.getNombre() != null ? vendedorEditar.getNombre() : "");
            txtComisionVenta.setText(String.valueOf(vendedorEditar.getComisionVenta()));
            txtComisionCobranza.setText(String.valueOf(vendedorEditar.getComisionCobranza()));
            chkActivo.setSelected(vendedorEditar.isActivo());
        }
    }

    private void guardarVendedor() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String strComVenta = txtComisionVenta.getText().trim();
        String strComCobranza = txtComisionCobranza.getText().trim();

        // Validar campo obligatorio
        if (nombre.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "El nombre del vendedor es obligatorio.",
                    "Atención",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            txtNombre.requestFocus();
            return;
        }

        try {
            // Convertir comisiones reemplazando coma por punto si fuera necesario
            double comVenta = strComVenta.isEmpty() ? 0.0 : Double.parseDouble(strComVenta.replace(",", "."));
            double comCobranza = strComCobranza.isEmpty() ? 0.0 : Double.parseDouble(strComCobranza.replace(",", "."));
            boolean activo = chkActivo.isSelected();

            if (vendedorEditar == null) {
                // --- NUEVO REGISTRO ---
                modelo.Vendedor v = new modelo.Vendedor();
                v.setCodigo(codigo);
                v.setNombre(nombre);
                v.setComisionVenta(comVenta);
                v.setComisionCobranza(comCobranza);
                v.setActivo(activo);

                if (vendedorDAO.guardarVendedor(v)) {
                   utilidades.AlertaUtil.mostrarExito(this, "Vendedor guardado correctamente.");
                    this.dispose(); // Cierra el JDialog
                } else {
                    utilidades.AlertaUtil.mostrarError(this, "Error al guardar el vendedor en la base de datos.");
                }
            } else {
                // --- EDICIÓN DE EXISTENTE ---
                vendedorEditar.setCodigo(codigo);
                vendedorEditar.setNombre(nombre);
                vendedorEditar.setComisionVenta(comVenta);
                vendedorEditar.setComisionCobranza(comCobranza);
                vendedorEditar.setActivo(activo);

                if (vendedorDAO.actualizarVendedor(vendedorEditar)) {
                   utilidades.AlertaUtil.mostrarExito(this, "Vendedor actualizado correctamente");
                    this.dispose(); // Cierra el JDialog
                } else {
                  utilidades.AlertaUtil.mostrarError(this, "Error al actualizar el vendedor.");
                }
            }

        } catch (NumberFormatException e) {
           utilidades.AlertaUtil.mostrarError(this, "Las comisiones deben ser valores numéricos válidos (ej: 2.5 o 0).");
        }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlPrincipal = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        txtComisionCobranza = new javax.swing.JTextField();
        txtNombre = new javax.swing.JTextField();
        txtComisionVenta = new javax.swing.JTextField();
        chkActivo = new javax.swing.JCheckBox();
        btnCancelar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Código:");

        jLabel2.setText("Nombre Vendedor:");

        jLabel3.setText("Comisión Venta (%):");

        jLabel4.setText("Comisión Cobranza (%):");

        jLabel5.setText("Estado:");

        chkActivo.setText("Activo en sistema");

        btnCancelar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnCancelar.setText("CANCELAR");
        btnCancelar.setBorderPainted(false);
        btnCancelar.setContentAreaFilled(false);
        btnCancelar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(this::btnCancelarActionPerformed);

        btnGuardar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnGuardar.setText("GUARDAR");
        btnGuardar.setBorderPainted(false);
        btnGuardar.setContentAreaFilled(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(this::btnGuardarActionPerformed);

        javax.swing.GroupLayout pnlPrincipalLayout = new javax.swing.GroupLayout(pnlPrincipal);
        pnlPrincipal.setLayout(pnlPrincipalLayout);
        pnlPrincipalLayout.setHorizontalGroup(
            pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrincipalLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlPrincipalLayout.createSequentialGroup()
                        .addComponent(btnGuardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCancelar))
                    .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtComisionCobranza, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtComisionVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(chkActivo)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        pnlPrincipalLayout.setVerticalGroup(
            pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrincipalLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtComisionVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtComisionCobranza, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(chkActivo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(pnlPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnGuardar))
                .addGap(58, 58, 58))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarVendedor();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnCancelarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DFrmVendedor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            // Pasamos 'null' como tercer parámetro para probar el modo "Nuevo Vendedor"
            DFrmVendedor dialog = new DFrmVendedor(new javax.swing.JFrame(), true, null);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JCheckBox chkActivo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtComisionCobranza;
    private javax.swing.JTextField txtComisionVenta;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
