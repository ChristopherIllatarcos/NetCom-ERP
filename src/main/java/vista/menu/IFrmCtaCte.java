package vista.menu;

import java.awt.Component;
import java.beans.PropertyVetoException;
import vista.FrmMenuPrincipal;

/**
 *
 * @author caill
 */
public class IFrmCtaCte extends javax.swing.JInternalFrame {

    /**
     * Creates new form JInternalFrameCtaCte
     */
    public IFrmCtaCte() {
        initComponents();
        utilidades();
        configurarVentana();
    }

    private void botonFecha() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmCartolaClienteFecha framectacte = new vista.ctacte.IFrmCartolaClienteFecha();

            Component add = FrmMenuPrincipal.desktopPane.add(framectacte);

            // 3. La hacemos visible y la mandamos al frente
            framectacte.setVisible(true);
            framectacte.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir cartola por cliente por fecha: " + e.getMessage());
        }
    }

    private void botonCobros() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmCuentasXCobrar framectacte = new vista.ctacte.IFrmCuentasXCobrar();

            Component add = FrmMenuPrincipal.desktopPane.add(framectacte);

            // 3. La hacemos visible y la mandamos al frente
            framectacte.setVisible(true);
            framectacte.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir cuentas por cobrar: " + e.getMessage());
        }
    }

    private void botonEliminarPago() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmEliminarPagoCliente frame = new vista.ctacte.IFrmEliminarPagoCliente();

            Component add = FrmMenuPrincipal.desktopPane.add(frame);

            // 3. La hacemos visible y la mandamos al frente
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Eliminar pago: " + e.getMessage());
        }
    }

    private void botonListaDoc() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmDocumentosEnCartera frame = new vista.ctacte.IFrmDocumentosEnCartera();

            Component add = FrmMenuPrincipal.desktopPane.add(frame);

            // 3. La hacemos visible y la mandamos al frente
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Listado de Documentos: " + e.getMessage());
        }
    }

    private void botonEstadistico() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmEstadisticoCliente frame = new vista.ctacte.IFrmEstadisticoCliente();

            Component add = FrmMenuPrincipal.desktopPane.add(frame);

            // 3. La hacemos visible y la mandamos al frente
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Estadístico de Clientes: " + e.getMessage());
        }
    }

    private void botonCartola() {
        try {
            // 1. Instanciamos la nueva ventana cartola cliente por fecha
            vista.ctacte.IFrmCartolaClientePorVentas frame = new vista.ctacte.IFrmCartolaClientePorVentas();

            Component add = FrmMenuPrincipal.desktopPane.add(frame);

            // 3. La hacemos visible y la mandamos al frente
            frame.setVisible(true);
            frame.setSelected(true);
            this.dispose();

        } catch (PropertyVetoException e) {
            System.out.println("Error al abrir Cartola por Ventas: " + e.getMessage());
        }
    }

    private void configurarVentana() {
        this.setTitle("CTACTE CLIENTE");
        this.setSize(560, 394);
        this.setResizable(false);

    }

    private void utilidades() {
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnEliminarPago, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnListaDocumentos, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnEstadisticoCliente, jSeparator3, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnCartolaVentas, jSeparator4, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnFecha, jSeparator5, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnCobros, jSeparator6, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlCtaCte = new javax.swing.JPanel();
        btnEliminarPago = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnListaDocumentos = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        btnEstadisticoCliente = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        btnCartolaVentas = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        btnFecha = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        btnCobros = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JSeparator();

        pnlCtaCte.setBackground(new java.awt.Color(255, 255, 255));
        pnlCtaCte.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEliminarPago.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/quitar-pago-con-tarjeta.png"))); // NOI18N
        btnEliminarPago.setText("Eliminar Pago");
        btnEliminarPago.setBorderPainted(false);
        btnEliminarPago.setContentAreaFilled(false);
        btnEliminarPago.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarPago.setFocusPainted(false);
        btnEliminarPago.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEliminarPago.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEliminarPago.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEliminarPagoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEliminarPagoMouseExited(evt);
            }
        });
        btnEliminarPago.addActionListener(this::btnEliminarPagoActionPerformed);
        pnlCtaCte.add(btnEliminarPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 155, 120));
        pnlCtaCte.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(55, 150, 100, -1));

        btnListaDocumentos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lista-de-deseos.png"))); // NOI18N
        btnListaDocumentos.setText("Listado de Documentos");
        btnListaDocumentos.setBorderPainted(false);
        btnListaDocumentos.setContentAreaFilled(false);
        btnListaDocumentos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnListaDocumentos.setFocusPainted(false);
        btnListaDocumentos.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnListaDocumentos.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnListaDocumentos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnListaDocumentosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnListaDocumentosMouseExited(evt);
            }
        });
        btnListaDocumentos.addActionListener(this::btnListaDocumentosActionPerformed);
        pnlCtaCte.add(btnListaDocumentos, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 40, 160, 120));
        pnlCtaCte.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 150, 140, -1));

        btnEstadisticoCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/estadistica-inferencial.png"))); // NOI18N
        btnEstadisticoCliente.setText("Estadístico de Clientes");
        btnEstadisticoCliente.setBorderPainted(false);
        btnEstadisticoCliente.setContentAreaFilled(false);
        btnEstadisticoCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEstadisticoCliente.setFocusPainted(false);
        btnEstadisticoCliente.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEstadisticoCliente.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEstadisticoCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEstadisticoClienteMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEstadisticoClienteMouseExited(evt);
            }
        });
        btnEstadisticoCliente.addActionListener(this::btnEstadisticoClienteActionPerformed);
        pnlCtaCte.add(btnEstadisticoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 40, 155, 120));
        pnlCtaCte.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 150, 120, -1));

        btnCartolaVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/analitica.png"))); // NOI18N
        btnCartolaVentas.setText("Cartola por Ventas");
        btnCartolaVentas.setBorderPainted(false);
        btnCartolaVentas.setContentAreaFilled(false);
        btnCartolaVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCartolaVentas.setFocusPainted(false);
        btnCartolaVentas.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCartolaVentas.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCartolaVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCartolaVentasMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCartolaVentasMouseExited(evt);
            }
        });
        btnCartolaVentas.addActionListener(this::btnCartolaVentasActionPerformed);
        pnlCtaCte.add(btnCartolaVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 190, 155, 120));
        pnlCtaCte.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 110, -1));

        btnFecha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/detalle.png"))); // NOI18N
        btnFecha.setText("Cartola por Fecha");
        btnFecha.setBorderPainted(false);
        btnFecha.setContentAreaFilled(false);
        btnFecha.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnFecha.setFocusPainted(false);
        btnFecha.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFecha.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFecha.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnFechaMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnFechaMouseExited(evt);
            }
        });
        btnFecha.addActionListener(this::btnFechaActionPerformed);
        pnlCtaCte.add(btnFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 190, 155, 120));
        pnlCtaCte.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 300, 100, -1));

        btnCobros.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/contabilidad.png"))); // NOI18N
        btnCobros.setText("Cobros de la A a la Z");
        btnCobros.setBorderPainted(false);
        btnCobros.setContentAreaFilled(false);
        btnCobros.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCobros.setFocusPainted(false);
        btnCobros.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCobros.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCobros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCobrosMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCobrosMouseExited(evt);
            }
        });
        btnCobros.addActionListener(this::btnCobrosActionPerformed);
        pnlCtaCte.add(btnCobros, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 190, 155, 120));
        pnlCtaCte.add(jSeparator6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 300, 130, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlCtaCte, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlCtaCte, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEliminarPagoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarPagoMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnEliminarPagoMouseEntered

    private void btnEliminarPagoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEliminarPagoMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnEliminarPagoMouseExited

    private void btnListaDocumentosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnListaDocumentosMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnListaDocumentosMouseEntered

    private void btnListaDocumentosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnListaDocumentosMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnListaDocumentosMouseExited

    private void btnEstadisticoClienteMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEstadisticoClienteMouseEntered
        jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnEstadisticoClienteMouseEntered

    private void btnEstadisticoClienteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEstadisticoClienteMouseExited
        jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnEstadisticoClienteMouseExited

    private void btnCartolaVentasMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCartolaVentasMouseEntered
        jSeparator4.setVisible(true);
    }//GEN-LAST:event_btnCartolaVentasMouseEntered

    private void btnCartolaVentasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCartolaVentasMouseExited
        jSeparator4.setVisible(false);
    }//GEN-LAST:event_btnCartolaVentasMouseExited

    private void btnFechaMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFechaMouseEntered
        jSeparator5.setVisible(true);
    }//GEN-LAST:event_btnFechaMouseEntered

    private void btnFechaMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnFechaMouseExited
        jSeparator5.setVisible(false);
    }//GEN-LAST:event_btnFechaMouseExited

    private void btnCobrosMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCobrosMouseEntered
        jSeparator6.setVisible(true);
    }//GEN-LAST:event_btnCobrosMouseEntered

    private void btnCobrosMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCobrosMouseExited
        jSeparator6.setVisible(false);
    }//GEN-LAST:event_btnCobrosMouseExited

    private void btnFechaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFechaActionPerformed
        botonFecha();
    }//GEN-LAST:event_btnFechaActionPerformed

    private void btnCobrosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCobrosActionPerformed
        botonCobros();
    }//GEN-LAST:event_btnCobrosActionPerformed

    private void btnEliminarPagoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarPagoActionPerformed
        botonEliminarPago();
    }//GEN-LAST:event_btnEliminarPagoActionPerformed

    private void btnListaDocumentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnListaDocumentosActionPerformed
        botonListaDoc();
    }//GEN-LAST:event_btnListaDocumentosActionPerformed

    private void btnEstadisticoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEstadisticoClienteActionPerformed
        botonEstadistico();
    }//GEN-LAST:event_btnEstadisticoClienteActionPerformed

    private void btnCartolaVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCartolaVentasActionPerformed
        botonCartola();
    }//GEN-LAST:event_btnCartolaVentasActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCartolaVentas;
    private javax.swing.JButton btnCobros;
    private javax.swing.JButton btnEliminarPago;
    private javax.swing.JButton btnEstadisticoCliente;
    private javax.swing.JButton btnFecha;
    private javax.swing.JButton btnListaDocumentos;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPanel pnlCtaCte;
    // End of variables declaration//GEN-END:variables
}
