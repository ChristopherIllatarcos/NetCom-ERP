package vista.ventas;

import java.awt.Component;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import vista.FrmMenuPrincipal;

/**
 *
 * @author caill
 */
public class IFrmHojaRuta extends javax.swing.JInternalFrame {

    private final List<modelo.VentaRuta> listaVentasRuta = new ArrayList<>();

    private void actualizarTotalFolios() {
        lblTotalFolio.setText(String.valueOf(listaVentasRuta.size()));
    }

    public IFrmHojaRuta() {
        initComponents();
        colorBoton();
        quitarBordeBarra();
        cargarCombos();
        configuracionBotones();
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

    private void agregarRuta() {
        if (cmbTipoDoc.getSelectedIndex() <= 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar un Tipo de Documento.");
            cmbTipoDoc.requestFocus();
            return;
        }

        String folioStr = txtFolio.getText().trim();
        if (folioStr.isEmpty()) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Ingrese un número de folio.");
            txtFolio.requestFocus();
            return;
        }

        try {
            int folio = Integer.parseInt(folioStr);
            String tipoDoc = cmbTipoDoc.getSelectedItem().toString();

            // Validar duplicados exactos: Mismo folio Y mismo tipo de documento
            for (modelo.VentaRuta v : listaVentasRuta) {
                if (v.getFolio() == folio && v.getTipoDocumento().equalsIgnoreCase(tipoDoc)) {
                    String mensaje = "El " + tipoDoc + " con folio " + folio + " ya está en la lista.";

                    utilidades.AlertaUtil.mostrarAdvertencia(this, mensaje);

                    txtFolio.setText("");
                    txtFolio.requestFocus();
                    return;
                }
            }

            dao.HojaRutaDAO dao = new dao.HojaRutaDAO();
            modelo.VentaRuta venta = dao.buscarVentaPorFolioYTipo(folio, tipoDoc);

            if (venta != null) {
                listaVentasRuta.add(venta);

                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.addRow(new Object[]{
                    venta.getFolio(),
                    venta.getTipoDocumento(),
                    venta.getCliente(),
                    venta.getDireccion(),
                    venta.getMonto(),
                    venta.getBultos(),
                    venta.getEstado()
                });

                actualizarTotalFolios();
                txtFolio.setText("");
                txtFolio.requestFocus();
            } else {
                utilidades.AlertaUtil.mostrarError(this, "No se encontró ningún documento " + tipoDoc + " con el Folio: " + folio);

            }

        } catch (NumberFormatException e) {
            utilidades.AlertaUtil.mostrarError(this, "El número de folio debe ser numérico.");
        }
    }

    private void colorBoton() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnConsultarHojaRuta, grisSuave);
        aplicarEfectoHoverPlano(btnQuitar, rojoSuave);
        aplicarEfectoHoverPlano(btnIngresar, azulSuave);
        aplicarEfectoHoverPlano(btnRegistrar, azulSuave);
    }

    private void configurarVentana() {
        this.setTitle("Hoja de Ruta");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void configuracionBotones() {
        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnIngresar, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnQuitar, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(btnRegistrar, jSeparator3, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnConsultarHojaRuta, jSeparator4, 90, 4, java.awt.Color.LIGHT_GRAY
        );

    }

    private void botonRegistrar() {
        if (listaVentasRuta.isEmpty()) {
            utilidades.AlertaUtil.mostrar("Sin Folios", "No hay folios en la Hoja de Ruta para guardar.", "advertencia");
            return;
        }

        // ID REAL DEL USUARIO QUE SE LOGUEÓ RECIÉN
        int idUsuarioActual = utilidades.SesionUsuario.idUsuarioActual;

        dao.HojaRutaDAO dao = new dao.HojaRutaDAO();

        // Le pasamos la lista de ventas y el ID del usuario real al DAO
        if (dao.guardarHojaRuta(listaVentasRuta, idUsuarioActual)) {
            utilidades.AlertaUtil.mostrar("¡Registro Exitoso!", "Hoja de Ruta registrada con éxito en el sistema.", "exito");

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            listaVentasRuta.clear();
            actualizarTotalFolios();
        } else {
            utilidades.AlertaUtil.mostrar("Error de Base de Datos", "Error al guardar la Hoja de Ruta en la base de datos.", "error");
        }
    }

    private void botonConsultar() {
        try {
            // 1. Instanciamos la nueva ventana de Hoja de Ruta
            vista.ventas.IFrmConsultaHojasRuta frameHojaRuta = new vista.ventas.IFrmConsultaHojasRuta();

            Component add = FrmMenuPrincipal.desktopPane.add(frameHojaRuta);

            // 3. La hacemos visible y la mandamos al frente
            frameHojaRuta.setVisible(true);
            frameHojaRuta.setSelected(true);

            // 4. (Opcional) Cerramos el submenú actual para mantener la pantalla limpia
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Hoja de Ruta: " + e.getMessage());
        }
    }

    private void cargarCombos() {
        // Limpiamos el combobox
        cmbTipoDoc.removeAllItems();
        cmbTipoDoc.addItem("SELECCIONE...");

        try {

            dao.TipoDocumentoDAO tipoDocDAO = new dao.TipoDocumentoDAO();
            java.util.List<String> listaTipos = tipoDocDAO.listarTiposDocumentoActivos();

            for (String tipo : listaTipos) {
                cmbTipoDoc.addItem(tipo);
            }

        } catch (Exception e) {
            // Si ocurre algún error, dejamos al menos las opciones por defecto para que no se caiga la ventana
            System.out.println("Error al cargar los tipos de documento desde la BD: " + e.getMessage());

             
            cmbTipoDoc.addItem("BOLETA ELECTRONICA");
            cmbTipoDoc.addItem("FACTURA ELECTRONICA");
            cmbTipoDoc.addItem("GUIA DE DESPACHO");
            cmbTipoDoc.addItem("TICKET / VALE INTERNO");
        }
    }

    private void quitarBordeBarra() {
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void quitar() {
        int filaSeleccionada = jTable1.getSelectedRow();
        if (filaSeleccionada >= 0) {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.removeRow(filaSeleccionada);
            listaVentasRuta.remove(filaSeleccionada);
            actualizarTotalFolios();
        } else {
            utilidades.AlertaUtil.mostrarError(this, "Seleccione un folio de la tabla para quitar.");
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
        txtFolio = new javax.swing.JTextField();
        btnIngresar = new javax.swing.JButton();
        btnQuitar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        cmbTipoDoc = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnRegistrar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        btnConsultarHojaRuta = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        lblTotalFolio = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BUSQUEDA Y CONTROL DE FOLIO"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("N° FOLIO:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 38, -1, -1));

        txtFolio.addActionListener(this::txtFolioActionPerformed);
        jPanel1.add(txtFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(97, 35, 340, -1));

        btnIngresar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnIngresar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnIngresar.setText("AGREGAR");
        btnIngresar.setBorderPainted(false);
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnIngresar.setFocusPainted(false);
        btnIngresar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnIngresarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnIngresarMouseExited(evt);
            }
        });
        btnIngresar.addActionListener(this::btnIngresarActionPerformed);
        jPanel1.add(btnIngresar, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 30, -1, -1));

        btnQuitar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnQuitar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/quitar-boton-circular.png"))); // NOI18N
        btnQuitar.setText("QUITAR");
        btnQuitar.setBorderPainted(false);
        btnQuitar.setContentAreaFilled(false);
        btnQuitar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnQuitar.setFocusPainted(false);
        btnQuitar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnQuitarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnQuitarMouseExited(evt);
            }
        });
        btnQuitar.addActionListener(this::btnQuitarActionPerformed);
        jPanel1.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 30, -1, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 60, 100, -1));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 60, 100, -1));

        jLabel3.setText("TIPO DOC:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 70, -1, -1));

        cmbTipoDoc.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbTipoDoc.addActionListener(this::cmbTipoDocActionPerformed);
        jPanel1.add(cmbTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(96, 70, 340, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 708, 140));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("DATOS GENERALES"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N° Folio", "Tipo Documento", "Cliente", "Dirección", "Monto", "Bultos", "Estado"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 680, 240));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, 705, 280));

        btnRegistrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegistrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete (1).png"))); // NOI18N
        btnRegistrar.setText("REGISTRAR");
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setContentAreaFilled(false);
        btnRegistrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnRegistrarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnRegistrarMouseExited(evt);
            }
        });
        btnRegistrar.addActionListener(this::btnRegistrarActionPerformed);
        pnlPrincipal.add(btnRegistrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 490, -1, -1));

        jLabel2.setText("Total Folios:");
        pnlPrincipal.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 490, -1, -1));
        pnlPrincipal.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 518, 100, -1));

        btnConsultarHojaRuta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultarHojaRuta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultarHojaRuta.setText("CONSULTAR HOJA DE RUTA");
        btnConsultarHojaRuta.setBorderPainted(false);
        btnConsultarHojaRuta.setContentAreaFilled(false);
        btnConsultarHojaRuta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultarHojaRuta.setFocusPainted(false);
        btnConsultarHojaRuta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnConsultarHojaRutaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnConsultarHojaRutaMouseExited(evt);
            }
        });
        btnConsultarHojaRuta.addActionListener(this::btnConsultarHojaRutaActionPerformed);
        pnlPrincipal.add(btnConsultarHojaRuta, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 490, -1, -1));
        pnlPrincipal.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 518, 185, -1));

        lblTotalFolio.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalFolio.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalFolio.setText("0");
        pnlPrincipal.add(lblTotalFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 490, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Hoja de Ruta");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 30, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -20, 770, 580));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnIngresarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnIngresarMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnIngresarMouseEntered

    private void btnIngresarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnIngresarMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnIngresarMouseExited

    private void btnQuitarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnQuitarMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnQuitarMouseEntered

    private void btnQuitarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnQuitarMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnQuitarMouseExited

    private void btnRegistrarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRegistrarMouseEntered
        jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnRegistrarMouseEntered

    private void btnRegistrarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRegistrarMouseExited
        jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnRegistrarMouseExited

    private void btnIngresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIngresarActionPerformed
        agregarRuta();
    }//GEN-LAST:event_btnIngresarActionPerformed

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        quitar();
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        botonRegistrar();
    }//GEN-LAST:event_btnRegistrarActionPerformed

    private void btnConsultarHojaRutaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarHojaRutaMouseEntered
        jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnConsultarHojaRutaMouseEntered

    private void btnConsultarHojaRutaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarHojaRutaMouseExited
        jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnConsultarHojaRutaMouseExited

    private void btnConsultarHojaRutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarHojaRutaActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarHojaRutaActionPerformed

    private void txtFolioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFolioActionPerformed

    }//GEN-LAST:event_txtFolioActionPerformed

    private void cmbTipoDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoDocActionPerformed

    }//GEN-LAST:event_cmbTipoDocActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultarHojaRuta;
    private javax.swing.JButton btnIngresar;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JComboBox<String> cmbTipoDoc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblTotalFolio;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTextField txtFolio;
    // End of variables declaration//GEN-END:variables
}
