package vista.archivos;

/**
 *
 * @author caill
 */
public class IFrmIndexacion extends javax.swing.JInternalFrame {

    private javax.swing.JTextArea txtLog;

    public IFrmIndexacion() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        disenoEstilo();
        iniciarLog();
        configurarVentana();
        getRootPane().setDefaultButton(btnMantenimiento); //para usar el enter.
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

    private void botonMantenimientoEntered() {
        jSeparator1.setVisible(true);
    }

    private void botonMantenimientoExited() {
        jSeparator1.setVisible(false);
    }

    private void botonCerrarEntered() {
        jSeparator2.setVisible(true);
    }

    private void botonCerrarExited() {
        jSeparator2.setVisible(false);
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void botonMantenimiento() {
        btnMantenimiento.setEnabled(false);
        txtLog.setText("");

        dao.MantenimientoDAO daoMant = new dao.MantenimientoDAO();

        javax.swing.SwingWorker<Boolean, String> worker = new javax.swing.SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                boolean todoOk = true;

                publish("5% - Conectando con PostgreSQL y analizando estructura...");
                setProgress(5);
                Thread.sleep(500);

                java.util.List<String> listaTablas = daoMant.obtenerTodasLasTablas();

                if (listaTablas.isEmpty()) {
                    publish("10% - [AVISO] No se encontraron tablas en el esquema.");
                    setProgress(10);
                } else {
                    int total = listaTablas.size();

                    for (int i = 0; i < total; i++) {
                        String tabla = listaTablas.get(i);

                        // Cálculo dinámico del porcentaje (repartido entre el 10% y el 85%)
                        int porcentaje = 10 + (int) (((double) (i + 1) / total) * 75);

                        publish(porcentaje + "% - Reindexando tabla: " + tabla.toUpperCase() + " (" + (i + 1) + "/" + total + ")");

                        daoMant.ejecutarSentenciaAdmin("REINDEX TABLE " + tabla + ";");

                        setProgress(porcentaje);
                        Thread.sleep(350);
                    }
                }

              
                publish("95% - Ejecutando VACUUM ANALYZE general en la base de datos...");
                if (!daoMant.ejecutarSentenciaAdmin("VACUUM ANALYZE;")) {
                    todoOk = false;
                }
                setProgress(95);
                Thread.sleep(600);

                
                publish("100% - ¡Mantenimiento y reindexación completados exitosamente!");
                setProgress(100);

                return todoOk;
            }

            @Override
            protected void process(java.util.List<String> chunksData) {
                for (String mensaje : chunksData) {
                    // Muestra el porcentaje y el texto en la etiqueta de proceso
                    lblPorcesando.setText(mensaje);
                    // Guarda el registro detallado en la consola inferior
                    txtLog.append("[LOG] " + mensaje + "\n");
                }
            }

            @Override

            protected void done() {
                btnMantenimiento.setEnabled(true);
                utilidades.AlertaUtil.mostrarInformacion(
                        javax.swing.SwingUtilities.getWindowAncestor(btnMantenimiento),
                        "¡Mantenimiento finalizado con éxito!"
                );
            }
        };

        worker.addPropertyChangeListener(evtProp -> {
            if ("progress".equals(evtProp.getPropertyName())) {
                jProgressBar1.setValue((Integer) evtProp.getNewValue());
            }
        });

        worker.execute();
    }

    private void colorBotones() {
        java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        aplicarEfectoHoverPlano(btnCerrar, grisSuave);
        aplicarEfectoHoverPlano(btnMantenimiento, azulSuave);
    }

    private void configurarVentana() {
        this.setTitle("MANTENIMIENTO Y REINDEXACIÓN DE BASE DE DATOS");
        this.setSize(770, 540);
        this.setResizable(false);

    }

    private void disenoEstilo() {
        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnMantenimiento, jSeparator1, 90, 4, java.awt.Color.LIGHT_GRAY
        );

        utilidades.DisenoEstilos.aplicarEstiloBotonConLinea(
                btnCerrar, jSeparator2, 90, 4, java.awt.Color.LIGHT_GRAY
        );
    }

    private void iniciarLog() {
        // --- Inicializar el Log dentro del jPanelLog existente ---
        jPanelLog.setLayout(new java.awt.BorderLayout());
        txtLog = new javax.swing.JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 11)); // Estilo consola
        txtLog.setBackground(new java.awt.Color(245, 245, 245));

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(txtLog);
        jPanelLog.add(scrollPane, java.awt.BorderLayout.CENTER);
        jPanelLog.revalidate();
        jPanelLog.repaint();

        jProgressBar1.setStringPainted(true);

        // Configuración inicial de componentes
        jProgressBar1.setValue(0);
        jProgressBar1.setStringPainted(true);

    }

    private void quitarBordeBarra() {
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null);
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnMantenimiento = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jProgressBar1 = new javax.swing.JProgressBar();
        lblPorcesando = new javax.swing.JLabel();
        jPanelLog = new javax.swing.JPanel();
        btnCerrar = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("ESTADO Y OPTIMIZACIÓN"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Este proceso optimiza las tablas, reorganiza índices y libera");
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 370, -1));

        jLabel2.setText("espacio en disco del sistema.");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 46, 220, -1));

        btnMantenimiento.setBackground(new java.awt.Color(0, 102, 204));
        btnMantenimiento.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnMantenimiento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/recuperacion-de-datos.png"))); // NOI18N
        btnMantenimiento.setText(" INICIAR MANTENIMIENTO");
        btnMantenimiento.setBorderPainted(false);
        btnMantenimiento.setContentAreaFilled(false);
        btnMantenimiento.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMantenimiento.setFocusPainted(false);
        btnMantenimiento.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnMantenimientoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnMantenimientoMouseExited(evt);
            }
        });
        btnMantenimiento.addActionListener(this::btnMantenimientoActionPerformed);
        jPanel2.add(btnMantenimiento, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 80, 308, -1));
        jPanel2.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 115, 300, 3));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 30, 740, 140));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("PROGRESO DEL PROCESO"));

        jProgressBar1.setBackground(new java.awt.Color(0, 102, 204));

        lblPorcesando.setText("Inicializando motor PostgreSQL y analizando catálogo de tablas...");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblPorcesando)
                        .addContainerGap(381, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(75, 75, 75))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPorcesando)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 177, 740, 110));

        jPanelLog.setBackground(new java.awt.Color(255, 255, 255));
        jPanelLog.setBorder(javax.swing.BorderFactory.createTitledBorder("LOG DE EJECUCIÓN"));

        javax.swing.GroupLayout jPanelLogLayout = new javax.swing.GroupLayout(jPanelLog);
        jPanelLog.setLayout(jPanelLogLayout);
        jPanelLogLayout.setHorizontalGroup(
            jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 730, Short.MAX_VALUE)
        );
        jPanelLogLayout.setVerticalGroup(
            jPanelLogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 137, Short.MAX_VALUE)
        );

        pnlPrincipal.add(jPanelLog, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 300, 740, 160));

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
        pnlPrincipal.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 490, 85, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("MANTENIMIENTO Y REINDEXACIÓN DE BASE DE DATOS");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 12, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnMantenimientoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMantenimientoMouseEntered
        botonMantenimientoEntered();
    }//GEN-LAST:event_btnMantenimientoMouseEntered

    private void btnMantenimientoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMantenimientoMouseExited
        botonMantenimientoExited();
    }//GEN-LAST:event_btnMantenimientoMouseExited

    private void btnCerrarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseEntered
        botonCerrarEntered();
    }//GEN-LAST:event_btnCerrarMouseEntered

    private void btnCerrarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCerrarMouseExited
        botonCerrarExited();
    }//GEN-LAST:event_btnCerrarMouseExited

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnMantenimientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMantenimientoActionPerformed
        botonMantenimiento();
    }//GEN-LAST:event_btnMantenimientoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnMantenimiento;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelLog;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblPorcesando;
    private javax.swing.JPanel pnlPrincipal;
    // End of variables declaration//GEN-END:variables

}
