package vista.ventas;

import dao.HojaRutaDAO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class IFrmConsultaHojasRuta extends javax.swing.JInternalFrame {

    private final HojaRutaDAO dao = new HojaRutaDAO();
    private int idHojaRutaSeleccionada = -1;
    private boolean seleccionarTodo = false;

    public IFrmConsultaHojasRuta() {
        initComponents();
        colorBotones();
        quitarBordeBarra();
        utilidades();
        configurarTabla();
        configuracionVentana();
        cargarHojasRutaEnTabla();
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

    private void botonBuscar() {
        Date desde = jDateChooserDesde.getDate();
        Date hasta = jDateChooserHasta.getDate();
        String numRuta = txtRuta.getText().trim();
        String filtroObs = "";

        // Reiniciar la variable y la cabecera cuando se hace una nueva búsqueda
        seleccionarTodo = false;
        tblHojaRutaEncontradas.getColumnModel().getColumn(0).setHeaderValue("Sel.");
        tblHojaRutaEncontradas.getTableHeader().repaint();

        List<Object[]> resultados = dao.buscarHojasRuta(desde, hasta, numRuta, filtroObs);

        DefaultTableModel model = (DefaultTableModel) tblHojaRutaEncontradas.getModel();
        model.setRowCount(0); // Limpiar tabla

        // Limpiar la tabla de detalle
        ((DefaultTableModel) tblDocRutaSelec.getModel()).setRowCount(0);
        idHojaRutaSeleccionada = -1;

        for (Object[] fila : resultados) {
            model.addRow(new Object[]{
                false, // Columna 0: CheckBox (Sel.)
                fila[1], // Columna 1: N° Ruta
                fila[2], // Columna 2: Fecha
                fila[3], // Columna 3: Vendedor/User
                fila[4], // Columna 4: N° Doc's
                fila[5], // Columna 5: Estado
                fila[6] // Columna 6: Observaciones
            });
        }

        if (resultados.isEmpty()) {
            utilidades.AlertaUtil.mostrarError(this, "No se encontraron hojas de ruta con los criterios ingresados.");
        }
    }

    private void botonAnularRuta() {
        if (idHojaRutaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Debe seleccionar una Hoja de Ruta de la tabla superior.");
            return;
        }

        boolean confirmar = utilidades.AlertaUtil.mostrarConfirmacion(
                "Confirmar Anulación",
                "¿Está seguro de que desea anular la Hoja de Ruta seleccionada?"
        );

        if (confirmar) {
            if (dao.anularHojaRuta(idHojaRutaSeleccionada)) {
                utilidades.AlertaUtil.mostrarExito(this, "Hoja de Ruta anulada con éxito.");
                btnBuscarActionPerformed(null); // Refrescar la búsqueda
            } else {
                utilidades.AlertaUtil.mostrarError(this, "Error al anular la Hoja de Ruta.");
            }
        }
    }

    private void botonImprimir() {
        List<Integer> idsSeleccionados = new ArrayList<>();
        List<String> numerosRuta = new ArrayList<>();

        Date desde = jDateChooserDesde.getDate();
        Date hasta = jDateChooserHasta.getDate();

        String filtroObs = "";

        // Recorremos las casillas seleccionadas
        for (int i = 0; i < tblHojaRutaEncontradas.getRowCount(); i++) {
            Boolean seleccionado = (Boolean) tblHojaRutaEncontradas.getValueAt(i, 0);

            if (seleccionado != null && seleccionado) {
                String numRuta = tblHojaRutaEncontradas.getValueAt(i, 1).toString();
                List<Object[]> res = dao.buscarHojasRuta(desde, hasta, numRuta, filtroObs);
                if (!res.isEmpty()) {
                    int idRuta = (int) res.get(0)[0];
                    idsSeleccionados.add(idRuta);
                    numerosRuta.add(numRuta);
                }
            }
        }

        // Si no hay casillas, tomar la fila activa
        if (idsSeleccionados.isEmpty() && idHojaRutaSeleccionada != -1) {
            int filaSelec = tblHojaRutaEncontradas.getSelectedRow();
            if (filaSelec != -1) {
                String numRuta = tblHojaRutaEncontradas.getValueAt(filaSelec, 1).toString();
                idsSeleccionados.add(idHojaRutaSeleccionada);
                numerosRuta.add(numRuta);
            }
        }

        if (idsSeleccionados.isEmpty()) {
            utilidades.AlertaUtil.mostrarError(this, "Debe seleccionar al menos una Hoja de Ruta marcando la casilla (Sel.).");
            return;
        }

        // LLAMADA ÚNICA (Sin bucle for)
        utilidades.ReporteUtil.generarPdfRutasConsolidadas(this, idsSeleccionados, numerosRuta);
    }

    private void botonCerrar() {
        this.dispose();
    }

    private void botonGuardarNota() {
        // 1. Verificamos que haya una hoja de ruta seleccionada en la tabla de abajo
        int filaSeleccionada = tblHojaRutaEncontradas.getSelectedRow();
        if (filaSeleccionada == -1) {
            utilidades.AlertaUtil.mostrarAdvertencia(this, "Por favor, selecciona una hoja de ruta de la tabla primero.");
            return;
        }

        // 2. Obtenemos el número de ruta de la fila (columna 1)
        Object numRutaObj = tblHojaRutaEncontradas.getValueAt(filaSeleccionada, 1);
        int numeroRuta = Integer.parseInt(numRutaObj.toString());

        // 3. Obtenemos el texto que escribieron en el cajón de observaciones de arriba
        String nuevaObservacion = txtObservaciones.getText().trim(); // (Ajusta 'txtObsFiltro' si le pusiste otro nombre en NetBeans)

        // 4. Actualizamos en la base de datos usando el DAO
        boolean actualizado = dao.actualizarObservacion(numeroRuta, nuevaObservacion);

        if (actualizado) {
            utilidades.AlertaUtil.mostrarExito(this, "¡Observación guardada con éxito para la ruta N° " + numeroRuta + "!");
            cargarHojasRutaEnTabla(); // Recargamos la tabla para ver el cambio reflejado al tiro
        } else {
            utilidades.AlertaUtil.mostrarError(this, "No se pudo guardar la observación.");
        }
    }

    private void colorBotones() {
        java.awt.Color colorBuscar = new java.awt.Color(224, 242, 254);
        java.awt.Color colorImprimir = new java.awt.Color(51, 65, 85);
        java.awt.Color colorAnular = new java.awt.Color(255, 218, 218);
        java.awt.Color colorCerrar = new java.awt.Color(235, 235, 235);

        aplicarEfectoHoverPlano(btnAnularRuta, colorAnular);
        aplicarEfectoHoverPlano(btnBuscar, colorBuscar);
        aplicarEfectoHoverPlano(btnCerrar, colorCerrar);
        aplicarEfectoHoverPlano(btnReimprimir, colorImprimir);

    }

    private void configuracionVentana() {
        this.setTitle("CONSULTA E HISTORIAL DE HOJAS DE RUTA");
        this.setSize(770, 540);
        this.setResizable(false);
    }

    private void cargarDetalle(int hojaRutaId) {
        List<Object[]> detalle = dao.obtenerDetalleRuta(hojaRutaId);
        DefaultTableModel modelDetalle = (DefaultTableModel) tblDocRutaSelec.getModel();
        modelDetalle.setRowCount(0);

        for (Object[] fila : detalle) {
            modelDetalle.addRow(fila);
        }
    }

    private void configurarTabla() {
        // 1. Definimos los encabezados agregando "Sel." en la primera posición
        String[] titulos = {"Sel.", "N° Ruta", "Fecha", "Vendedor/User", "N° Doc's", "Estado", "Observaciones"};

        // 2. Creamos el modelo personalizado
        DefaultTableModel model = new DefaultTableModel(null, titulos) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class; // Hace que la Columna 0 sea un CheckBox
                }
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Solo la casilla es editable
            }
        };

        // 3. Asignamos el modelo a la tabla visual
        tblHojaRutaEncontradas.setModel(model);

        // Ajustamos el ancho de la columna del CheckBox
        tblHojaRutaEncontradas.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblHojaRutaEncontradas.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void cargarHojasRutaEnTabla() {
        DefaultTableModel model = (DefaultTableModel) tblHojaRutaEncontradas.getModel();
        model.setRowCount(0);

        Date desde = jDateChooserDesde.getDate();
        Date hasta = jDateChooserHasta.getDate();
        String numRuta = txtRuta.getText().trim(); 
        String filtroObs = txtObservaciones.getText().trim(); 

        // Llamamos al DAO pasando las fechas, el número de ruta y el filtro de la observación
        List<Object[]> listaRutas = dao.buscarHojasRuta(desde, hasta, numRuta, filtroObs);

        for (Object[] fila : listaRutas) {
            model.addRow(new Object[]{
                false, // Columna 0: CheckBox
                fila[1], // Columna 1: N° Ruta
                fila[2], // Columna 2: Fecha
                fila[3], // Columna 3: Vendedor
                fila[4], // Columna 4: N° Documentos
                fila[5], // Columna 5: Estado
                fila[6] // Columna 6: Observaciones ("la punta del cerro")
            });
        }
    }

    private void quitarBordeBarra() {
        // --- QUITAR EL BORDE Y LA BARRA DE TÍTULO RETRO ---
        ((javax.swing.plaf.basic.BasicInternalFrameUI) this.getUI()).setNorthPane(null); // Elimina la barra superior de título
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Elimina el borde gris exterior
    }

    private void tablaHojaRuta() {
        int filaSelec = tblHojaRutaEncontradas.getSelectedRow();
        int columnaSelec = tblHojaRutaEncontradas.getSelectedColumn();
        String filtroObs = "";

        if (filaSelec != -1) {
            // Si hizo clic en la columna del CheckBox (Columna 0), no recargamos el detalle para que sea fluido
            if (columnaSelec == 0) {
                return;
            }

            // AHORA EL N° DE RUTA ESTÁ EN LA COLUMNA 1 (ya que la 0 es el CheckBox)
            String numRuta = tblHojaRutaEncontradas.getValueAt(filaSelec, 1).toString();

            // Buscamos el ID real mediante el número de ruta o la lista cargada
            Date desde = jDateChooserDesde.getDate();
            Date hasta = jDateChooserHasta.getDate();
            List<Object[]> resultados = dao.buscarHojasRuta(desde, hasta, numRuta, filtroObs);

            if (!resultados.isEmpty()) {
                idHojaRutaSeleccionada = (int) resultados.get(0)[0];
                cargarDetalle(idHojaRutaSeleccionada);
            }
        }
    }

    private void utilidades() {
        // 1. Delegar eventos ENTER y botón por defecto a TablaUtil
        utilidades.TablaUtil.configurarEventosEnterBusqueda(
                this.getRootPane(),
                btnBuscar,
                jDateChooserDesde,
                jDateChooserHasta,
                e -> btnBuscarActionPerformed(e)
        );

        // 2. Delegar la selección masiva de la cabecera a TablaUtil
        utilidades.TablaUtil.configurarSeleccionarTodoHeader(tblHojaRutaEncontradas);

        utilidades.TablaUtil.aplicarColorPorEstado(tblHojaRutaEncontradas, 5);

        // 3. Inicialización de datos de la ventana
        jDateChooserDesde.setDate(new Date());
        jDateChooserHasta.setDate(new Date());
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
        jDateChooserDesde = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jDateChooserHasta = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        txtRuta = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        txtObservaciones = new javax.swing.JTextField();
        btnGuardarNota = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHojaRutaEncontradas = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblDocRutaSelec = new javax.swing.JTable();
        btnCerrar = new javax.swing.JButton();
        btnReimprimir = new javax.swing.JButton();
        btnAnularRuta = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlPrincipal.setBackground(new java.awt.Color(255, 255, 255));
        pnlPrincipal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("BUSQUEDA Y FILTROS"));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setText("Desde:");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));
        jPanel1.add(jDateChooserDesde, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 160, -1));

        jLabel2.setText("Hasta:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 40, -1, -1));
        jPanel1.add(jDateChooserHasta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 40, 160, -1));

        jLabel3.setText("N° Ruta:");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 40, -1, -1));
        jPanel1.add(txtRuta, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 40, 160, -1));

        btnBuscar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnBuscar.setText("BUSCAR");
        btnBuscar.setBorderPainted(false);
        btnBuscar.setContentAreaFilled(false);
        btnBuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscar.setFocusPainted(false);
        btnBuscar.addActionListener(this::btnBuscarActionPerformed);
        jPanel1.add(btnBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 70, -1, -1));

        jLabel5.setText("Obs:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));
        jPanel1.add(txtObservaciones, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 75, 460, -1));

        btnGuardarNota.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/boton-agregar.png"))); // NOI18N
        btnGuardarNota.setBorderPainted(false);
        btnGuardarNota.setContentAreaFilled(false);
        btnGuardarNota.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGuardarNota.setFocusPainted(false);
        btnGuardarNota.addActionListener(this::btnGuardarNotaActionPerformed);
        jPanel1.add(btnGuardarNota, new org.netbeans.lib.awtextra.AbsoluteConstraints(525, 70, -1, -1));

        pnlPrincipal.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 20, 740, 115));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("HOJAS DE RUTA ENCONTRADAS (Cabeceras)"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblHojaRutaEncontradas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "N° Ruta", "Fecha", "Vendedor/User", "N° Doc's", "Estado", "Observaciones"
            }
        ));
        tblHojaRutaEncontradas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tblHojaRutaEncontradas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblHojaRutaEncontradasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblHojaRutaEncontradas);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 110));

        pnlPrincipal.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 140, 740, 140));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("DETALLE DE DOCUMENTOS EN LA RUTA SELECCIONADA"));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDocRutaSelec.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "N° Folio", "Tipo Documento", "Cliente", "Dirección", "Monto", "Bulto"
            }
        ));
        tblDocRutaSelec.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jScrollPane2.setViewportView(tblDocRutaSelec);

        jPanel3.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 720, 140));

        pnlPrincipal.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 290, 740, 170));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(this::btnCerrarActionPerformed);
        pnlPrincipal.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 470, -1, -1));

        btnReimprimir.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnReimprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/imprimir.png"))); // NOI18N
        btnReimprimir.setText("REIMPRIMIR HOJA");
        btnReimprimir.setBorderPainted(false);
        btnReimprimir.setContentAreaFilled(false);
        btnReimprimir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReimprimir.setFocusPainted(false);
        btnReimprimir.addActionListener(this::btnReimprimirActionPerformed);
        pnlPrincipal.add(btnReimprimir, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 470, -1, -1));

        btnAnularRuta.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAnularRuta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/eliminar.png"))); // NOI18N
        btnAnularRuta.setText("ANULAR RUTA");
        btnAnularRuta.setBorderPainted(false);
        btnAnularRuta.setContentAreaFilled(false);
        btnAnularRuta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAnularRuta.setFocusPainted(false);
        btnAnularRuta.addActionListener(this::btnAnularRutaActionPerformed);
        pnlPrincipal.add(btnAnularRuta, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 470, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("CONSULTA  HISTORIAL DE HOJAS DE RUTA");
        pnlPrincipal.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 5, -1, -1));

        getContentPane().add(pnlPrincipal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 770, 540));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        botonBuscar();
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void tblHojaRutaEncontradasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblHojaRutaEncontradasMouseClicked
        tablaHojaRuta();
    }//GEN-LAST:event_tblHojaRutaEncontradasMouseClicked

    private void btnAnularRutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnularRutaActionPerformed
        botonAnularRuta();
    }//GEN-LAST:event_btnAnularRutaActionPerformed

    private void btnReimprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReimprimirActionPerformed
        botonImprimir();
    }//GEN-LAST:event_btnReimprimirActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        botonCerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnGuardarNotaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarNotaActionPerformed
        botonGuardarNota();
    }//GEN-LAST:event_btnGuardarNotaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnularRuta;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnGuardarNota;
    private javax.swing.JButton btnReimprimir;
    private com.toedter.calendar.JDateChooser jDateChooserDesde;
    private com.toedter.calendar.JDateChooser jDateChooserHasta;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlPrincipal;
    private javax.swing.JTable tblDocRutaSelec;
    private javax.swing.JTable tblHojaRutaEncontradas;
    private javax.swing.JTextField txtObservaciones;
    private javax.swing.JTextField txtRuta;
    // End of variables declaration//GEN-END:variables
}
