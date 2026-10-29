package vista.ventas;

import dao.ComisionDAO;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.ComisionVendedor;
import utilidades.ReporteUtil;

public class IFrmComisiones extends javax.swing.JInternalFrame {

    // 1. Atributos para mantener limpia la vista
    private final ComisionDAO comisionDAO = new ComisionDAO();
    private List<ComisionVendedor> listaComisiones;
    private final DecimalFormat df = new DecimalFormat("$ #,##0");

    public IFrmComisiones() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
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

    private void botonImprimir() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte de Comisiones");
        fileChooser.setSelectedFile(new java.io.File("Reporte_Comisiones.pdf"));

        int seleccion = fileChooser.showSaveDialog(this);
        if (seleccion == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fileChooser.getSelectedFile();
            
            boolean exito = ReporteUtil.generarPDFComisiones(jTable1, "Reporte de Comisiones y Cobranzas", lblVentaTotal, archivo);
            
            if (exito) {
                utilidades.AlertaUtil.mostrarExito(this, "Reporte generado con éxito.");
            }
        }
    }

    private void colorBotones() {
        // Verde claro suave para Guardar / Aceptar
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        // Rojo claro suave para Cancelar / Cerrar / Eliminar
        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);

        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        // Aplicar botones con un solo método:
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnDetalle, naranjaSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configurarVentana() {
        this.setTitle("COMISIONES Y COBRANZAS DE VENTAS");
        this.setSize(748, 540);
        this.setResizable(false);

    }

    private void consultarComisiones() {
        if (jDateChooserDesde.getDate() == null || jDateChooserHasta.getDate() == null) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, selecciona un rango de fechas (Desde y Hasta)");
            return;
        }

        java.util.Date desde = jDateChooserDesde.getDate();
        java.util.Date hasta = jDateChooserHasta.getDate();

        if (desde.after(hasta)) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }

        this.listaComisiones = comisionDAO.obtenerComisionesPorFecha(desde, hasta);

        String[] columnas = {"N° Cobranza", "Nombre Vendedor", "Cobranza Total", "Comisión"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        double granTotalCobranza = 0;

        for (ComisionVendedor c : listaComisiones) {
            modelo.addRow(new Object[]{
                c.getNumeroCobranza(),
                c.getNombreVendedor(),
                df.format(c.getCobranzaTotal()),
                df.format(c.getComision())
            });
            granTotalCobranza += c.getCobranzaTotal();
        }

        jTable1.setModel(modelo);
        jTable1.setRowHeight(25);

        lblVentaTotal.setText(df.format(granTotalCobranza));
        lblVentaTotal.setForeground(new java.awt.Color(51, 102, 153));
    }

    private void configuracionBotones() {
        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnConsultar, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnDetalle, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        // Configuración para el primer botón
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnImprimir, jSeparator3, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior 
    }

    private void verDetalleVendedor() {
        int fila = jTable1.getSelectedRow();
        if (fila < 0) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, selecciona un vendedor de la tabla para ver su detalle.");
            return;
        }

        if (listaComisiones == null || listaComisiones.isEmpty()) {
            return;
        }

        ComisionVendedor comision = listaComisiones.get(fila);
        Date desde = jDateChooserDesde.getDate();
        Date hasta = jDateChooserHasta.getDate();

        java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);

        DFrmDetalleComision dialog = new DFrmDetalleComision(
                parentWindow,
                comision.getVendedorId(),
                comision.getNombreVendedor(),
                desde,
                hasta
        );
        dialog.setVisible(true);
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
        pnlFiltroBusqueda = new javax.swing.JPanel();
        jDateChooserDesde = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserHasta = new com.toedter.calendar.JDateChooser();
        btnConsultar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        pnlDetalleVendedores = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        btnDetalle = new javax.swing.JButton();
        btnImprimir = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        lblVentaTotal = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFiltroBusqueda.setBackground(new java.awt.Color(255, 255, 255));
        pnlFiltroBusqueda.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE BÚSQUEDA"));
        pnlFiltroBusqueda.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlFiltroBusqueda.add(jDateChooserDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(79, 35, 200, -1));

        jLabel1.setText("Desde:");
        pnlFiltroBusqueda.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 41, -1, -1));

        jLabel2.setText("Hasta:");
        pnlFiltroBusqueda.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, -1, -1));
        pnlFiltroBusqueda.add(jDateChooserHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 35, 200, -1));

        btnConsultar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnConsultar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/consultar.png"))); // NOI18N
        btnConsultar.setText("CONSULTAR");
        btnConsultar.setBorderPainted(false);
        btnConsultar.setContentAreaFilled(false);
        btnConsultar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConsultar.setFocusPainted(false);
        btnConsultar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnConsultarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnConsultarMouseExited(evt);
            }
        });
        btnConsultar.addActionListener(this::btnConsultarActionPerformed);
        pnlFiltroBusqueda.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 30, -1, -1));
        pnlFiltroBusqueda.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 60, 100, -1));

        pnlPrincipal.add(pnlFiltroBusqueda, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 710, 90));

        pnlDetalleVendedores.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetalleVendedores.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE VENDEDORES"));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "N° Cobranza", "Nombre Vendedor", "Cobranza Total", "Comisión"
            }
        ));
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout pnlDetalleVendedoresLayout = new javax.swing.GroupLayout(pnlDetalleVendedores);
        pnlDetalleVendedores.setLayout(pnlDetalleVendedoresLayout);
        pnlDetalleVendedoresLayout.setHorizontalGroup(
            pnlDetalleVendedoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleVendedoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlDetalleVendedoresLayout.setVerticalGroup(
            pnlDetalleVendedoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetalleVendedoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlPrincipal.add(pnlDetalleVendedores, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 170, 710, 300));

        jLabel3.setText("VENTA TOTAL:");
        pnlPrincipal.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 490, -1, -1));

        btnDetalle.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDetalle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/informacion.png"))); // NOI18N
        btnDetalle.setText("VER DETALLE");
        btnDetalle.setBorderPainted(false);
        btnDetalle.setContentAreaFilled(false);
        btnDetalle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDetalle.setFocusPainted(false);
        btnDetalle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDetalleMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDetalleMouseExited(evt);
            }
        });
        btnDetalle.addActionListener(this::btnDetalleActionPerformed);
        pnlPrincipal.add(btnDetalle, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 480, -1, -1));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnImprimirMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnImprimirMouseExited(evt);
            }
        });
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 480, -1, -1));
        pnlPrincipal.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 510, 100, -1));
        pnlPrincipal.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 510, 100, -1));

        lblVentaTotal.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblVentaTotal.setForeground(new java.awt.Color(0, 102, 204));
        lblVentaTotal.setText("$");
        pnlPrincipal.add(lblVentaTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 490, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("COMISIONES Y COBRANZAS DE VENTAS");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 45, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -30, 770, 570));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
        jSeparator1.setVisible(true);
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited
        jSeparator1.setVisible(false);
    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnDetalleMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDetalleMouseEntered
        jSeparator2.setVisible(true);
    }//GEN-LAST:event_btnDetalleMouseEntered

    private void btnDetalleMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDetalleMouseExited
        jSeparator2.setVisible(false);
    }//GEN-LAST:event_btnDetalleMouseExited

    private void btnImprimirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseEntered
        jSeparator3.setVisible(true);
    }//GEN-LAST:event_btnImprimirMouseEntered

    private void btnImprimirMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseExited
        jSeparator3.setVisible(false);
    }//GEN-LAST:event_btnImprimirMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        consultarComisiones();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnDetalleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetalleActionPerformed
        verDetalleVendedor();
    }//GEN-LAST:event_btnDetalleActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnDetalle;
    private javax.swing.JButton btnImprimir;
    private com.toedter.calendar.JDateChooser jDateChooserDesde;
    private com.toedter.calendar.JDateChooser jDateChooserHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblVentaTotal;
    private javax.swing.JPanel pnlDetalleVendedores;
    private javax.swing.JPanel pnlFiltroBusqueda;
    private javax.swing.JPanel pnlPrincipal;
    // End of variables declaration//GEN-END:variables
}
