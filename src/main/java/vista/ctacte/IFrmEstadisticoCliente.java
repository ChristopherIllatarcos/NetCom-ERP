package vista.ctacte;

public class IFrmEstadisticoCliente extends javax.swing.JInternalFrame {

    public IFrmEstadisticoCliente() {
        initComponents();
        colorBotones();
        configuracionComboBox();
        quitarBordeBarra();
        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        daoCc.cargarClientesCombo(cmbCliente);
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

    private void botonImprimir() {
        Object seleccion = ((javax.swing.JComboBox) cmbCliente).getSelectedItem();
        String nombreCliente = (seleccion != null) ? seleccion.toString() : "";

        utilidades.ReporteUtil.generarPDFEstadisticoCliente(
                this, nombreCliente, lblSaldo,
                lblComprados, lblTotalCom, lblPromCom, lblUltimaCom, lblMaxCom,
                lblPagado, lblTotalPago, lblPromPago, lblUltimoPago, lblMaxPago
        );
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void configuracionComboBox() {
        // 1. Hacer el combo editable para que funcione como buscador
        cmbCliente.setEditable(true);

// 1. Hacer el combo editable para permitir escritura
        cmbCliente.setEditable(true);

// 2. Obtener el componente de texto del editor del combo
        javax.swing.text.JTextComponent editorText = (javax.swing.text.JTextComponent) cmbCliente.getEditor().getEditorComponent();

// 3. Listener para buscar mientras se escribe (nombre o RUT)
        editorText.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Ignorar teclas de navegación (Enter, Arriba, Abajo)
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER
                        || evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP
                        || evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    return;
                }

                String textoBusqueda = editorText.getText().trim().toLowerCase();
                if (textoBusqueda.isEmpty()) {
                    return;
                }

                javax.swing.ComboBoxModel modeloCombo = cmbCliente.getModel();

                for (int i = 0; i < modeloCombo.getSize(); i++) {
                    Object elemento = modeloCombo.getElementAt(i);
                    if (elemento != null) {
                        String textoElemento = elemento.toString().toLowerCase();

                        // Comparamos si el elemento contiene lo que escribiste
                        if (textoElemento.contains(textoBusqueda)) {
                            cmbCliente.setSelectedIndex(i);

                            // Mantener el texto tipeado en la vista sin romper el cursor
                            editorText.setText(textoBusqueda);
                            editorText.setCaretPosition(textoBusqueda.length());
                            break;
                        }
                    }
                }
            }
        });
    }

    private void colorBotones() {
        //        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
//        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configuracionVentana() {
        this.setTitle("ESTADÍSTICO Y COMPORTAMIENTO FINANCIERO DE CLIENTE");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarEstadisticasClienteSeleccionado() {

        Object seleccion = ((javax.swing.JComboBox) cmbCliente).getSelectedItem();

        if (seleccion instanceof modelo.ItemCombo item) {
            if (item != null && item.getId() > 0) {
                int clienteId = item.getId();

                // Consultar estadísticas al DAO
                dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
                java.util.Map<String, Object> stats = daoCc.obtenerEstadisticasCliente(clienteId);

                // Rellenar etiquetas de Compras / Ventas
                lblSaldo.setText(String.format("$ %,.0f", (Double) stats.get("saldo_pendiente")));
                lblComprados.setText(String.valueOf(stats.get("cant_compras")));
                lblTotalCom.setText(String.format("$ %,.0f", (Double) stats.get("total_compras")));
                lblPromCom.setText(String.format("$ %,.0f", (Double) stats.get("prom_compras")));
                lblUltimaCom.setText(String.valueOf(stats.get("ultima_compra")));
                lblMaxCom.setText(String.format("$ %,.0f", (Double) stats.get("max_compra")));

                // Rellenar etiquetas de Pagos
                lblPagado.setText(String.valueOf(stats.get("cant_pagos")));
                lblTotalPago.setText(String.format("$ %,.0f", (Double) stats.get("total_pagos")));
                lblPromPago.setText(String.format("$ %,.0f", (Double) stats.get("prom_pagos")));
                lblUltimoPago.setText(String.valueOf(stats.get("ultimo_pago")));
                lblMaxPago.setText(String.format("$ %,.0f", (Double) stats.get("max_pago")));
            }
        } else {

            limpiarEtiquetas();
        }
    }

    private void limpiarEtiquetas() {
        lblSaldo.setText("$ 0");
        lblComprados.setText("0");
        lblTotalCom.setText("$ 0");
        lblPromCom.setText("$ 0");
        lblUltimaCom.setText("-");
        lblMaxCom.setText("$ 0");

        lblPagado.setText("0");
        lblTotalPago.setText("$ 0");
        lblPromPago.setText("$ 0");
        lblUltimoPago.setText("-");
        lblMaxPago.setText("$ 0");
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

        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbCliente = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblSaldo = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblMaxCom = new javax.swing.JLabel();
        lblComprados = new javax.swing.JLabel();
        lblTotalCom = new javax.swing.JLabel();
        lblPromCom = new javax.swing.JLabel();
        lblUltimaCom = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblMaxPago = new javax.swing.JLabel();
        lblPagado = new javax.swing.JLabel();
        lblTotalPago = new javax.swing.JLabel();
        lblPromPago = new javax.swing.JLabel();
        lblUltimoPago = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BÚSQUEDA DE CLIENTE"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Cliente:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 45, -1, -1));

        cmbCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        cmbCliente.addItemListener(this::cmbClienteItemStateChanged);
        jPanel1.add(cmbCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 45, 570, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 120));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("ESTADO DE CUENTA ACTUAL"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setText("SALDO ACTUAL PENDIENTE:");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 35, -1, -1));

        lblSaldo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblSaldo.setForeground(new java.awt.Color(0, 102, 204));
        lblSaldo.setText("$");
        jPanel2.add(lblSaldo, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 35, -1, -1));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 160, 740, 80));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("HISTORIAL DE COMPRAS"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText("Veces que ha comprado:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        jLabel5.setText("Total de compras:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(55, 60, -1, -1));

        jLabel6.setText("Promedio de compra:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 90, -1, -1));

        jLabel7.setText("Última compra:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 120, -1, -1));

        jLabel8.setText("Máximo comprado:");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(48, 150, -1, -1));

        lblMaxCom.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMaxCom.setForeground(new java.awt.Color(0, 102, 204));
        lblMaxCom.setText("0");
        jPanel3.add(lblMaxCom, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 150, -1, -1));

        lblComprados.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblComprados.setForeground(new java.awt.Color(0, 102, 204));
        lblComprados.setText("0");
        jPanel3.add(lblComprados, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 30, -1, -1));

        lblTotalCom.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCom.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalCom.setText("0");
        jPanel3.add(lblTotalCom, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, -1, -1));

        lblPromCom.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPromCom.setForeground(new java.awt.Color(0, 102, 204));
        lblPromCom.setText("0");
        jPanel3.add(lblPromCom, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, -1, -1));

        lblUltimaCom.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUltimaCom.setForeground(new java.awt.Color(0, 102, 204));
        lblUltimaCom.setText("/  /");
        jPanel3.add(lblUltimaCom, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 120, -1, -1));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 250, 355, 200));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("HISTORIAL DE PAGOS"));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel9.setText("Veces que ha pagado:");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 30, -1, -1));

        jLabel10.setText("Total de pagos:");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(55, 60, -1, -1));

        jLabel11.setText("Promedio de pago:");
        jPanel4.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 90, -1, -1));

        jLabel12.setText("Último pago:");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(68, 120, -1, -1));

        jLabel13.setText("Máximo pagado:");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(48, 150, -1, -1));

        lblMaxPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMaxPago.setForeground(new java.awt.Color(0, 102, 204));
        lblMaxPago.setText("0");
        jPanel4.add(lblMaxPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 150, -1, -1));

        lblPagado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPagado.setForeground(new java.awt.Color(0, 102, 204));
        lblPagado.setText("0");
        jPanel4.add(lblPagado, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, -1, -1));

        lblTotalPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalPago.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalPago.setText("0");
        jPanel4.add(lblTotalPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 60, -1, -1));

        lblPromPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblPromPago.setForeground(new java.awt.Color(0, 102, 204));
        lblPromPago.setText("0");
        jPanel4.add(lblPromPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 90, -1, -1));

        lblUltimoPago.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUltimoPago.setForeground(new java.awt.Color(0, 102, 204));
        lblUltimoPago.setText("/ /");
        jPanel4.add(lblUltimoPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 120, -1, -1));

        pnlPrincipal.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 250, 355, 200));

        btnImprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnImprimir.setText("IMPRIMIR");
        btnImprimir.setBorderPainted(false);
        btnImprimir.setContentAreaFilled(false);
        btnImprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnImprimir.setFocusPainted(false);
        btnImprimir.addActionListener(this::btnImprimirActionPerformed);
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 460, -1, -1));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 460, -1, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("ESTADÍSTICO Y COMPORTAMIENTO FINANCIERO DE CLIENTE");
        pnlPrincipal.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbClienteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbClienteItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            cargarEstadisticasClienteSeleccionado();
        }
    }//GEN-LAST:event_cmbClienteItemStateChanged

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JComboBox<String> cmbCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel lblComprados;
    private javax.swing.JLabel lblMaxCom;
    private javax.swing.JLabel lblMaxPago;
    private javax.swing.JLabel lblPagado;
    private javax.swing.JLabel lblPromCom;
    private javax.swing.JLabel lblPromPago;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblTotalCom;
    private javax.swing.JLabel lblTotalPago;
    private javax.swing.JLabel lblUltimaCom;
    private javax.swing.JLabel lblUltimoPago;
    private javax.swing.JPanel pnlPrincipal;
    // End of variables declaration//GEN-END:variables
}
