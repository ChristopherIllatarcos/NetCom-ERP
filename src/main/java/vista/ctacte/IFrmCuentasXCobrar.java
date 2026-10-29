package vista.ctacte;

/**
 *
 * @author caill
 */
public class IFrmCuentasXCobrar extends javax.swing.JInternalFrame {


    public IFrmCuentasXCobrar() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        cargarSectoresCombo();
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

    private void botonConsultar() {
        String sectorSeleccionado = cmbSectorZona.getSelectedItem() != null ? cmbSectorZona.getSelectedItem().toString() : "TODOS";
        boolean esResumen = rdbResumen.isSelected();

        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        java.util.List<Object[]> resultados = daoCc.obtenerCuentasPorCobrar(sectorSeleccionado);

        javax.swing.table.DefaultTableModel modelo;

        if (esResumen) {
            // Modelo para Resumen (ej: Cliente, Total Deuda)
            modelo = new javax.swing.table.DefaultTableModel(new Object[]{"R.U.T.", "Cliente", "Total Deuda ($)"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
        } else {
            // Modelo para Informe Detallado
            modelo = new javax.swing.table.DefaultTableModel(new Object[]{"R.U.T.", "Cliente", "Sector/Zona", "Saldo Deuda ($)"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
        }

        tblListado.setModel(modelo);
        modelo.setRowCount(0);

        int totalClientes = 0;
        double totalCobrarGeneral = 0.0;

        // Conjunto para llevar el conteo único de clientes con deuda
        java.util.Set<String> rutsUnicos = new java.util.HashSet<>();

        for (Object[] fila : resultados) {
            String rut = (String) fila[0];
            String nombre = (String) fila[1];
            String sector = (String) fila[2];
            double saldo = (Double) fila[3];

            rutsUnicos.add(rut);
            totalCobrarGeneral += saldo;

            if (esResumen) {
                modelo.addRow(new Object[]{rut, nombre, String.format("$ %,.0f", saldo)});
            } else {
                modelo.addRow(new Object[]{rut, nombre, sector, String.format("$ %,.0f", saldo)});
            }
        }

        totalClientes = rutsUnicos.size();

        // Actualizar etiquetas inferiores
        lblTotalCli.setText(String.valueOf(totalClientes));
        lblTotalCobrar.setText(String.format("$ %,.0f", totalCobrarGeneral));
    }

    private void botonExcel() {
        String tipo = rdbResumen.isSelected() ? "Resumen_Cuentas_Por_Cobrar" : "Informe_Cuentas_Por_Cobrar";
        String sector = cmbSectorZona.getSelectedItem() != null ? cmbSectorZona.getSelectedItem().toString() : "TODOS";
        String nombreArchivo = tipo + "_" + sector.replace(" ", "_");

        // Llamada limpia a tu utilidad de Excel
        utilidades.ExcelUtil.exportarTablaAExcel(tblListado, nombreArchivo);
    }

    private void botonImprimir() {
        String titulo = rdbResumen.isSelected() ? "RESUMEN DE CUENTAS POR COBRAR" : "INFORME DE CUENTAS POR COBRAR";
        String sector = cmbSectorZona.getSelectedItem() != null ? cmbSectorZona.getSelectedItem().toString() : "TODOS";

        // Llamada limpia a tu utilidad de Reportes (PDF)
        utilidades.ReporteUtil.generarPDFCuentasPorCobrar(
                tblListado,
                titulo,
                sector,
                lblTotalCli,
                lblTotalCobrar
        );
    }

    private void colorBotones() {
        // java.awt.Color azulSuave = new java.awt.Color(219, 234, 254);
        java.awt.Color verdeSuave = new java.awt.Color(210, 245, 215);
        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);
//        java.awt.Color rojoSuave = new java.awt.Color(255, 218, 218);
//        java.awt.Color naranjaSuave = new java.awt.Color(255, 237, 213);
        java.awt.Color oscuroSuave = new java.awt.Color(51, 65, 85);

        aplicarEfectoHoverPlano(btnExcel, verdeSuave);
        aplicarEfectoHoverPlano(btnConsultar, grisSuave);
        aplicarEfectoHoverPlano(btnImprimir, oscuroSuave);
    }

    private void configurarVentana() {
        this.setTitle("CUENTAS POR COBRAR (ALFABÉTICO)");
        this.setSize(765, 540);
        this.setResizable(false);

    }

    private void cargarSectoresCombo() {
        cmbSectorZona.removeAllItems();
        cmbSectorZona.addItem("TODOS");

        dao.CuentaCorrienteDAO daoCc = new dao.CuentaCorrienteDAO();
        java.util.List<String> sectores = daoCc.obtenerSectoresZonas();

        for (String sector : sectores) {
            cmbSectorZona.addItem(sector);
        }
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
        jLabel6 = new javax.swing.JLabel();
        pnlPrincipal = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rdbInforme = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        rdbResumen = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        cmbSectorZona = new javax.swing.JComboBox<>();
        btnConsultar = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblListado = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnExcel = new javax.swing.JButton();
        lblTotalCli = new javax.swing.JLabel();
        lblTotalCobrar = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnImprimir = new javax.swing.JButton();

        jLabel6.setText("jLabel6");

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("FILTROS DE CONSULTA"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Tipo Informe:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, -1, -1));

        buttonGroup1.add(rdbInforme);
        rdbInforme.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(rdbInforme, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 30, -1, -1));

        jLabel2.setText("Resumen:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 30, -1, -1));

        buttonGroup1.add(rdbResumen);
        rdbResumen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(rdbResumen, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 30, -1, -1));

        jLabel3.setText("Sector / Zona:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        cmbSectorZona.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione", "Sector 0" }));
        cmbSectorZona.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jPanel1.add(cmbSectorZona, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 60, 450, -1));

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
        jPanel1.add(btnConsultar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 50, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 30, 730, 100));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("LISTADO DE CUENTAS POR COBRAR"));
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

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 24, 708, 260));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 150, 730, 300));

        jLabel4.setText("Total Clientes con Deuda:");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 470, -1, -1));

        jLabel5.setText("Total x Cobrar:");
        pnlPrincipal.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 470, -1, -1));

        btnExcel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/excel.png"))); // NOI18N
        btnExcel.setText("EXCEL");
        btnExcel.setBorderPainted(false);
        btnExcel.setContentAreaFilled(false);
        btnExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExcel.setFocusPainted(false);
        btnExcel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnExcelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnExcelMouseExited(evt);
            }
        });
        btnExcel.addActionListener(this::btnExcelActionPerformed);
        pnlPrincipal.add(btnExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 460, -1, -1));

        lblTotalCli.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCli.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalCli.setText("0");
        pnlPrincipal.add(lblTotalCli, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 470, -1, -1));

        lblTotalCobrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalCobrar.setForeground(new java.awt.Color(0, 102, 204));
        lblTotalCobrar.setText("0");
        pnlPrincipal.add(lblTotalCobrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 470, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("CUENTAS POR COBRAR (ALFABÉTICO)");
        pnlPrincipal.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 15, -1, -1));

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
        pnlPrincipal.add(btnImprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 460, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConsultarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseEntered
    }//GEN-LAST:event_btnConsultarMouseEntered

    private void btnConsultarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConsultarMouseExited

    }//GEN-LAST:event_btnConsultarMouseExited

    private void btnExcelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseEntered

    }//GEN-LAST:event_btnExcelMouseEntered

    private void btnExcelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExcelMouseExited

    }//GEN-LAST:event_btnExcelMouseExited

    private void btnImprimirMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseEntered

    }//GEN-LAST:event_btnImprimirMouseEntered

    private void btnImprimirMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnImprimirMouseExited

    }//GEN-LAST:event_btnImprimirMouseExited

    private void btnConsultarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsultarActionPerformed
        botonConsultar();
    }//GEN-LAST:event_btnConsultarActionPerformed

    private void btnExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelActionPerformed
        botonExcel();
    }//GEN-LAST:event_btnExcelActionPerformed

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnImprimirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConsultar;
    private javax.swing.JButton btnExcel;
    private javax.swing.JButton btnImprimir;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbSectorZona;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTotalCli;
    private javax.swing.JLabel lblTotalCobrar;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JRadioButton rdbInforme;
    private javax.swing.JRadioButton rdbResumen;
    private javax.swing.JTable tblListado;
    // End of variables declaration//GEN-END:variables
}
