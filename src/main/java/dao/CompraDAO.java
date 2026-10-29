package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Compra;
import modelo.DetalleCompra;
import conexion.ConexionDB;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.swing.table.DefaultTableModel;

public class CompraDAO {

    public boolean registrarCompra(Compra compra) {
        String sqlCompra = "INSERT INTO compras (numero_factura, tipo, fecha_emision, fecha_vencimiento, "
                + "proveedor_id, subtotal, descuento, iva, total, forma_pago, condicion_pago, "
                + "lugar_entrega, observaciones, usuario_id, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        // Insertamos tanto precio_unitario como costo_unitario para evitar conflictos de NOT NULL
        String sqlDetalle = "INSERT INTO detalle_compras (compra_id, producto_id, codigo_producto, "
                + "descripcion, cantidad, precio_unitario, costo_unitario, subtotal) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement psCompra = null;
        PreparedStatement psDetalle = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            // 1. Insertar la Cabecera de la Compra
            psCompra = con.prepareStatement(sqlCompra, java.sql.Statement.RETURN_GENERATED_KEYS);
            psCompra.setString(1, compra.getNumeroFactura());
            psCompra.setString(2, compra.getTipo());

            psCompra.setDate(3, compra.getFechaEmision() != null ? new java.sql.Date(compra.getFechaEmision().getTime()) : null);
            psCompra.setDate(4, compra.getFechaVencimiento() != null ? new java.sql.Date(compra.getFechaVencimiento().getTime()) : null);

            psCompra.setInt(5, compra.getProveedorId());
            psCompra.setDouble(6, compra.getSubtotal());
            psCompra.setDouble(7, compra.getDescuento());
            psCompra.setDouble(8, compra.getIva());
            psCompra.setDouble(9, compra.getTotal());
            psCompra.setString(10, compra.getFormaPago());
            psCompra.setString(11, compra.getCondicionPago());
            psCompra.setString(12, compra.getLugarEntrega());
            psCompra.setString(13, compra.getObservaciones());
            psCompra.setInt(14, compra.getUsuarioId() > 0 ? compra.getUsuarioId() : 1);

            int filasAfectadas = psCompra.executeUpdate();

            if (filasAfectadas == 0) {
                con.rollback();
                return false;
            }

            rs = psCompra.getGeneratedKeys();
            int compraId = 0;
            if (rs.next()) {
                compraId = rs.getInt(1);
            } else {
                con.rollback();
                return false;
            }

            // 2. Insertar cada ítem del Detalle
            psDetalle = con.prepareStatement(sqlDetalle);
            for (DetalleCompra detalle : compra.getDetalles()) {
                psDetalle.setInt(1, compraId);

                if (detalle.getProductoId() > 0) {
                    psDetalle.setInt(2, detalle.getProductoId());
                } else {
                    psDetalle.setNull(2, java.sql.Types.INTEGER);
                }

                psDetalle.setString(3, detalle.getCodigoProducto());
                psDetalle.setString(4, detalle.getDescripcion());
                psDetalle.setInt(5, detalle.getCantidad());
                psDetalle.setDouble(6, detalle.getCostoUnitario()); // precio_unitario
                psDetalle.setDouble(7, detalle.getCostoUnitario()); // costo_unitario
                psDetalle.setDouble(8, detalle.getSubtotal());

                psDetalle.addBatch();
            }

            psDetalle.executeBatch();
            System.out.println(">>> ¡A PUNTO DE HACER COMMIT! ID Generado: " + compraId);
            con.commit();
            System.out.println(">>> ¡COMMIT EJECUTADO CON ÉXITO!");
            return true;

        } catch (SQLException e) {
            System.err.println("Error detallado al registrar la compra: " + e.getMessage());
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (psCompra != null) {
                    psCompra.close();
                }
                if (psDetalle != null) {
                    psDetalle.close();
                }
                if (con != null) {
                    con.setAutoCommit(true);
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar recursos: " + e.getMessage());
            }
        }
    }

    public void listarComprasFiltradas(DefaultTableModel modeloTabla, String textoBusqueda, java.util.Date fechaDesde, java.util.Date fechaHasta) {
        modeloTabla.setRowCount(0); // Limpiamos la tabla

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols() {
            {
                setGroupingSeparator('.');
            }
        };
        DecimalFormat formatoMoneda = new DecimalFormat("$ #,##0", simbolos);

        String textoLimpio = (textoBusqueda != null) ? textoBusqueda.trim() : "";

        StringBuilder sql = new StringBuilder(
                "SELECT c.numero_factura, p.rut AS rut_proveedor, p.nombre AS nombre_proveedor, "
                + "c.tipo, c.fecha_emision, c.total "
                + "FROM compras c "
                + "LEFT JOIN proveedores p ON c.proveedor_id = p.id "
                + "WHERE 1=1"
        );

        if (!textoLimpio.isEmpty()) {
            sql.append(" AND (c.numero_factura ILIKE ? OR p.rut ILIKE ?)");
        }
        if (fechaDesde != null) {
            sql.append(" AND c.fecha_emision >= ?");
        }
        if (fechaHasta != null) {
            sql.append(" AND c.fecha_emision <= ?");
        }

        sql.append(" ORDER BY c.fecha_emision DESC");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (!textoLimpio.isEmpty()) {
                String comodin = "%" + textoLimpio + "%";
                ps.setString(index++, comodin);
                ps.setString(index++, comodin);
            }
            if (fechaDesde != null) {
                ps.setDate(index++, new java.sql.Date(fechaDesde.getTime()));
            }
            if (fechaHasta != null) {
                ps.setDate(index++, new java.sql.Date(fechaHasta.getTime()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean hayDatos = false;
                while (rs.next()) {
                    hayDatos = true;
                    Object[] fila = {
                        rs.getString("numero_factura"),
                        rs.getString("rut_proveedor"),
                        rs.getString("nombre_proveedor"),
                        rs.getString("tipo"),
                        rs.getDate("fecha_emision"),
                        formatoMoneda.format(rs.getDouble("total"))
                    };
                    modeloTabla.addRow(fila);
                }
                if (!hayDatos) {
                    System.out.println(">>> La consulta no devolvió registros para los filtros ingresados.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar compras filtradas en DAO: " + e.getMessage());
        }
    }

    public void listarCompras(DefaultTableModel modeloTabla, String textoBusqueda) {
        listarComprasFiltradas(modeloTabla, textoBusqueda, null, null);
    }

    public void cargarDetallesEnTabla(DefaultTableModel modeloDetalle, String numeroFactura) {
        modeloDetalle.setRowCount(0); // Limpiar la tabla del detalle por si acaso

        String sql = "SELECT dc.codigo_producto, dc.descripcion, dc.cantidad, dc.precio_unitario, dc.subtotal "
                + "FROM detalle_compras dc "
                + "JOIN compras c ON dc.compra_id = c.id "
                + "WHERE c.numero_factura = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, numeroFactura);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = {
                        rs.getString("codigo_producto"),
                        rs.getString("descripcion"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario"),
                        rs.getDouble("subtotal")
                    };
                    modeloDetalle.addRow(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar los detalles de la compra: " + e.getMessage());
        }
    }

    public boolean verificarYLimpiarHistorialCompras(java.awt.Component ventanaPadre, DefaultTableModel modeloTabla) {
        String sqlContar = "SELECT COUNT(*) FROM compras";
        String sqlEliminarDetalle = "DELETE FROM detalle_compras";
        String sqlEliminarCompras = "DELETE FROM compras";

        try (Connection con = ConexionDB.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sqlContar); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalRegistros = rs.getInt(1);
                    if (totalRegistros < 100) {
                        return false;
                    }
                }
            }

            javax.swing.JOptionPane.showMessageDialog(ventanaPadre, """
                                                                    \u00a1Alerta de Mantenimiento!
                                                                    Se han acumulado 100 o m\u00e1s registros en el historial.
                                                                    El sistema proceder\u00e1 a exportar un respaldo a Excel y a limpiar la tabla autom\u00e1ticamente.""",
                    "Límite de Registros Alcanzado",
                    javax.swing.JOptionPane.WARNING_MESSAGE);

            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Guardar respaldo automático de compras");
            fileChooser.setSelectedFile(new java.io.File("RespaldoHistorialCompras_Automatico.csv"));

            int userSelection = fileChooser.showSaveDialog(ventanaPadre);
            if (userSelection != javax.swing.JFileChooser.APPROVE_OPTION) {
                javax.swing.JOptionPane.showMessageDialog(ventanaPadre, "Operación cancelada por el usuario. No se limpió la tabla.", "Cancelado", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return false;
            }

            java.io.File archivoToSave = fileChooser.getSelectedFile();

            try (java.io.PrintWriter pw = new java.io.PrintWriter(archivoToSave, "ISO-8859-1")) {
                for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
                    pw.print(modeloTabla.getColumnName(i) + (i == modeloTabla.getColumnCount() - 1 ? "" : ";"));
                }
                pw.println();

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                        Object valor = modeloTabla.getValueAt(i, j);
                        pw.print((valor != null ? valor.toString() : "") + (j == modeloTabla.getColumnCount() - 1 ? "" : ";"));
                    }
                    pw.println();
                }
            }

            con.setAutoCommit(false);
            try (PreparedStatement psDetalle = con.prepareStatement(sqlEliminarDetalle); PreparedStatement psCompras = con.prepareStatement(sqlEliminarCompras)) {

                psDetalle.executeUpdate();
                psCompras.executeUpdate();
                con.commit();

                modeloTabla.setRowCount(0);

                javax.swing.JOptionPane.showMessageDialog(ventanaPadre,
                        "Respaldo guardado con éxito y tabla limpiada correctamente.",
                        "Limpieza Exitosa",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                return true;

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }

        } catch (Exception e) {
            System.err.println("Error en el proceso de limpieza automática: " + e.getMessage());
            return false;
        }
    }

}
