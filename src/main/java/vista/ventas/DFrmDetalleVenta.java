package vista.ventas;

import dao.VentaDAO;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleVenta;
import modelo.Venta;

public class DFrmDetalleVenta extends javax.swing.JDialog {

    private static final java.util.logging.Logger logger
            = java.util.logging.Logger.getLogger(DFrmDetalleVenta.class.getName());
    private DecimalFormat df = new DecimalFormat("$ #,##0.00");
    private Venta ventaActual;

    // Constructor que recibe la ventana padre y el objeto Venta
    public DFrmDetalleVenta(Window owner, Venta venta) {
        super(owner,
                "Detalle de Venta - " + venta.getTipoDocumento() + " N° "
                + (venta.getNumeroDocumento() != null ? venta.getNumeroDocumento() : venta.getFolio()),
                ModalityType.APPLICATION_MODAL);
        initComponents();
        
        

        java.awt.Color grisSuave = new java.awt.Color(240, 240, 240);

        aplicarEfectoHoverPlano(btnCerrar, grisSuave);

        this.ventaActual = venta;

        setSize(720, 560);
        setLocationRelativeTo(owner);

        btnCerrar.addActionListener(e -> dispose());

        cargarDatosVenta();
        cargarTablaDetalle();
    }

    private void cargarDatosVenta() {
        if (ventaActual != null) {
            lblTipoDoc.setText(ventaActual.getTipoDocumento() != null ? ventaActual.getTipoDocumento() : "-");
            lblFolio.setText(ventaActual.getNumeroDocumento() != null
                    ? ventaActual.getNumeroDocumento()
                    : String.valueOf(ventaActual.getFolio()));
            lblFecha.setText(ventaActual.getFechaEmision() != null ? ventaActual.getFechaEmision().toString() : "-");
            lblFormaPago.setText(ventaActual.getFormaPago() != null ? ventaActual.getFormaPago() : "-");

            // --- CAMBIO DE COLOR SEGÚN ESTADO ---
            String estado = ventaActual.getEstado() != null ? ventaActual.getEstado() : "-";
            lblEstado.setText(estado.toUpperCase());
            lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD)); // Negrita para mayor legibilidad

            if (estado.equalsIgnoreCase("EMITIDA")) {
                lblEstado.setForeground(new Color(40, 167, 69)); // Verde
            } else if (estado.equalsIgnoreCase("ANULADA")) {
                lblEstado.setForeground(new Color(220, 53, 69)); // Rojo
            } else {
                lblEstado.setForeground(Color.BLACK); // Color neutro para otros estados
            }

            // Formato para el Total
            lblTotalVenta.setFont(lblTotalVenta.getFont().deriveFont(Font.BOLD, 13f));
            lblTotalVenta.setForeground(new Color(0, 102, 204));
            lblTotalVenta.setText(df.format(ventaActual.getTotal()));
        }
    }

    private void cargarTablaDetalle() {
        if (ventaActual == null) {
            return;
        }

        System.out.println("DEBUG ventaActual.getId() = " + ventaActual.getId());
        System.out.println("DEBUG ventaActual.getNumeroDocumento() = " + ventaActual.getNumeroDocumento());

        VentaDAO dao = new VentaDAO();
        List<DetalleVenta> detalles = dao.obtenerDetallesPorVentaId(ventaActual.getId());
        System.out.println("DEBUG detalles.size() = " + detalles.size());  

        String[] columnas = {"Ítem / Producto", "Cantidad", "P. Unitario", "Descuento", "Subtotal"};
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (DetalleVenta d : detalles) {
            String producto = (d.getObservaciones() != null && !d.getObservaciones().isEmpty())
                    ? d.getObservaciones()
                    : "Producto ID: " + d.getProductoId();

            modeloTabla.addRow(new Object[]{
                producto,
                d.getCantidad(),
                df.format(d.getPrecioUnitario()),
                df.format(d.getDescuento()),
                df.format(d.getTotalLinea())
            });
        }

        tblDeltalle.setModel(modeloTabla);
        tblDeltalle.setRowHeight(25);
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

        jPanel1 = new javax.swing.JPanel();
        panelCabecera = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblTipoDoc = new javax.swing.JLabel();
        lblFecha = new javax.swing.JLabel();
        lblEstado = new javax.swing.JLabel();
        lblFolio = new javax.swing.JLabel();
        lblFormaPago = new javax.swing.JLabel();
        lblTotalVenta = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDeltalle = new javax.swing.JTable();
        btnCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelCabecera.setBackground(new java.awt.Color(255, 255, 255));
        panelCabecera.setBorder(javax.swing.BorderFactory.createTitledBorder("Información de la Venta"));
        panelCabecera.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Tipo Doc:");
        panelCabecera.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 40, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Fecha:");
        panelCabecera.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 68, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Estado:");
        panelCabecera.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 100, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("N° Folio:");
        panelCabecera.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(365, 40, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Forma Pago:");
        panelCabecera.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 70, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Total Venta:");
        panelCabecera.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 98, -1, -1));

        lblTipoDoc.setText("jLabel7");
        panelCabecera.add(lblTipoDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, -1, -1));

        lblFecha.setText("jLabel8");
        panelCabecera.add(lblFecha, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, -1, -1));

        lblEstado.setText("jLabel9");
        panelCabecera.add(lblEstado, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, -1, -1));

        lblFolio.setText("jLabel10");
        panelCabecera.add(lblFolio, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 40, -1, -1));

        lblFormaPago.setText("jLabel11");
        panelCabecera.add(lblFormaPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 70, -1, -1));

        lblTotalVenta.setText("jLabel12");
        panelCabecera.add(lblTotalVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 100, -1, -1));

        jPanel1.add(panelCabecera, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 680, 170));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Ítems / Productos Comprados"));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblDeltalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Ítem / Producto", "Cantidad", "P. Unitario", "Descuento", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(tblDeltalle);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 660, 190));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 680, 220));

        btnCerrar.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cerrar-ventana.png"))); // NOI18N
        btnCerrar.setText("CERRAR");
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrar.setFocusPainted(false);
        jPanel1.add(btnCerrar, new org.netbeans.lib.awtextra.AbsoluteConstraints(585, 450, -1, -1));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 720, 560));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            Venta ventaPrueba = new Venta();
            ventaPrueba.setId(1);
            ventaPrueba.setTipoDocumento("BOLETA");
            ventaPrueba.setNumeroDocumento("B001-000123");
            ventaPrueba.setFechaEmision(new java.util.Date());
            ventaPrueba.setFormaPago("EFECTIVO");
            ventaPrueba.setEstado("EMITIDA");
            ventaPrueba.setTotal(150.00);
            
            DFrmDetalleVenta dialog = new DFrmDetalleVenta((Window) null, ventaPrueba);
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCerrar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblEstado;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblFolio;
    private javax.swing.JLabel lblFormaPago;
    private javax.swing.JLabel lblTipoDoc;
    private javax.swing.JLabel lblTotalVenta;
    private javax.swing.JPanel panelCabecera;
    private javax.swing.JTable tblDeltalle;
    // End of variables declaration//GEN-END:variables
}
