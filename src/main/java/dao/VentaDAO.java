package dao;

import conexion.ConexionDB;
import modelo.Producto;
import modelo.Venta;
import modelo.DetalleVenta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.ClienteCombo;
import modelo.DetalleVenta;
import modelo.Producto;
import modelo.Venta;

public class VentaDAO {

    // Buscar producto por código de barras
    public Producto buscarProductoPorCodigo(String codigo) {
        String sql = "SELECT codigo, nombre, precio_venta FROM productos WHERE codigo = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getDouble("precio_venta")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }
        return null;
    }

    // Registrar Venta + Detalle de Venta
    public boolean guardarVenta(Venta venta) {
        // INSERT alineado exactamente con las columnas de tu tabla 'ventas'
        String sqlVenta = "INSERT INTO ventas ("
                + "tipo_documento, folio, cliente_id, vendedor_id, fecha_emision, "
                + "forma_pago, subtotal, descuento, iva, total, estado"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        // INSERT alineado con la tabla 'detalle_ventas'
        String sqlDetalle = "INSERT INTO detalle_ventas ("
                + "venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal"
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false); // Inicio de transacción

            int ventaId = -1;

            // 1. Guardar la cabecera de la Venta
            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta)) {
                psVenta.setString(1, venta.getTipoDoc());
                psVenta.setInt(2, venta.getFolio());

                // cliente_id dinámico
                if (venta.getClienteId() > 0) {
                    psVenta.setInt(3, venta.getClienteId());
                } else {
                    psVenta.setNull(3, java.sql.Types.INTEGER);
                }

                // vendedor_id dinámico
                if (venta.getVendedorId() > 0) {
                    psVenta.setInt(4, venta.getVendedorId());
                } else {
                    psVenta.setNull(4, java.sql.Types.INTEGER);
                }

                psVenta.setDate(5, new java.sql.Date(venta.getFecha().getTime()));
                psVenta.setString(6, venta.getCondicionPago());
                psVenta.setDouble(7, venta.getNeto());
                psVenta.setDouble(8, venta.getDescuento());
                psVenta.setDouble(9, venta.getIva());
                psVenta.setDouble(10, venta.getTotal());
                psVenta.setString(11, venta.getEstado() != null ? venta.getEstado() : "EMITIDA");

                try (ResultSet rs = psVenta.executeQuery()) {
                    if (rs.next()) {
                        ventaId = rs.getInt(1);
                    }
                }
            }

            // 2. Guardar cada ítem del Detalle
            if (ventaId != -1) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta det : venta.getDetalles()) {
                        psDetalle.setInt(1, ventaId);
                        psDetalle.setInt(2, det.getProductoId()); // ID del producto de la BD
                        psDetalle.setInt(3, det.getCantidad());
                        psDetalle.setDouble(4, det.getPrecioUnitario());
                        psDetalle.setDouble(5, det.getDescuento());
                        psDetalle.setDouble(6, det.getTotalLinea());
                        psDetalle.addBatch();
                    }
                    psDetalle.executeBatch();
                }
            }

            con.commit(); // Confirmar cambios en la BD
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); // Cancelar cambios ante cualquier falla
                } catch (SQLException ex) {
                }
            }
            System.err.println("Error al guardar la venta: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

// 1. OBTENER SIGUIENTE FOLIO (Limpia prefijos como "BOL-", "FAC-" para encontrar el número más alto)
    public int obtenerSiguienteFolio(String tipoDocumento) {
        int siguienteFolio = 1;
        // Extrae solo los dígitos numéricos de numero_documento usando REGEXP_REPLACE
        String sql = "SELECT COALESCE(MAX(NULLIF(REGEXP_REPLACE(numero_documento, '\\D', '', 'g'), '')::integer), 0) + 1 AS siguiente "
                + "FROM ventas WHERE tipo_documento = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    siguienteFolio = rs.getInt("siguiente");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el siguiente folio: " + e.getMessage());
        }
        return siguienteFolio;
    }

    // 2. REGISTRAR VENTA (Guardar tanto el folio numérico como el número formateado)
    public boolean registrarVenta(Venta venta, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO ventas ("
                + "tipo_documento, folio, numero_documento, cliente_id, vendedor_id, fecha_emision, "
                + "forma_pago, subtotal, descuento, iva, total, estado"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        String sqlDetalle = "INSERT INTO detalle_ventas ("
                + "venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal, observaciones"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            int ventaId = -1;

            try (PreparedStatement psVenta = con.prepareStatement(sqlVenta)) {
                psVenta.setString(1, venta.getTipoDoc());
                psVenta.setInt(2, venta.getFolio());

                // Si numero_documento en el modelo Venta no viene formateado, le asignamos el string del folio
                String numDoc = (venta.getNumeroDocumento() != null && !venta.getNumeroDocumento().isEmpty())
                        ? venta.getNumeroDocumento()
                        : String.valueOf(venta.getFolio());

                psVenta.setString(3, numDoc);

                if (venta.getClienteId() > 0) {
                    psVenta.setInt(4, venta.getClienteId());
                } else {
                    psVenta.setNull(4, java.sql.Types.INTEGER);
                }

                if (venta.getVendedorId() > 0) {
                    psVenta.setInt(5, venta.getVendedorId());
                } else {
                    psVenta.setNull(5, java.sql.Types.INTEGER);
                }

                psVenta.setDate(6, new java.sql.Date(venta.getFecha().getTime()));
                psVenta.setString(7, venta.getCondicionPago());
                psVenta.setDouble(8, venta.getNeto());
                psVenta.setDouble(9, venta.getDescuento());
                psVenta.setDouble(10, venta.getIva());
                psVenta.setDouble(11, venta.getTotal());
                psVenta.setString(12, venta.getEstado() != null ? venta.getEstado() : "EMITIDA");

                try (ResultSet rs = psVenta.executeQuery()) {
                    if (rs.next()) {
                        ventaId = rs.getInt(1);
                    }
                }
            }

            if (ventaId != -1) {
                try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle)) {
                    for (DetalleVenta det : detalles) {
                        psDetalle.setInt(1, ventaId);
                        psDetalle.setInt(2, det.getProductoId());
                        psDetalle.setInt(3, det.getCantidad());
                        psDetalle.setDouble(4, det.getPrecioUnitario());
                        psDetalle.setDouble(5, det.getDescuento());
                        psDetalle.setDouble(6, det.getTotalLinea());
                        psDetalle.setString(7, det.getObservaciones() != null ? det.getObservaciones() : "");

                        psDetalle.addBatch();
                    }
                    psDetalle.executeBatch();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                }
            }
            System.err.println("Error al registrar venta: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 3. LISTAR VENTAS (Incluye el nombre del Vendedor vía LEFT JOIN)
    public List<Venta> listarVentas() {
        List<Venta> lista = new ArrayList<>();
        // Incluimos v.folio en la consulta SQL
        String sql = "SELECT v.id, v.folio, v.tipo_documento, v.numero_documento, v.fecha_emision, "
                + "       v.forma_pago, v.total, v.estado, COALESCE(ven.nombre, 'SIN VENDEDOR') AS vendedor_nombre "
                + "FROM ventas v "
                + "LEFT JOIN vendedores ven ON v.vendedor_id = ven.id "
                + "ORDER BY v.id DESC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Venta v = new Venta();
                v.setId(rs.getInt("id"));
                v.setFolio(rs.getInt("folio")); // <--- ASIGNAR EL FOLIO DE LA BD
                v.setTipoDocumento(rs.getString("tipo_documento"));
                v.setNumeroDocumento(rs.getString("numero_documento"));
                v.setFechaEmision(rs.getDate("fecha_emision"));
                v.setFormaPago(rs.getString("forma_pago"));
                v.setEstado(rs.getString("estado"));
                v.setTotal(rs.getDouble("total"));
                v.setNombreVendedor(rs.getString("vendedor_nombre"));

                lista.add(v);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }

    public Venta obtenerVentaPorId(int id) {
        Venta v = null;
        String sql = "SELECT id, tipo_documento, folio, numero_documento, cliente_id, vendedor_id, "
                + "fecha_emision, forma_pago, subtotal, descuento, iva, total, estado "
                + "FROM ventas WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setTipoDocumento(rs.getString("tipo_documento"));
                    v.setFolio(rs.getInt("folio"));
                    v.setNumeroDocumento(rs.getString("numero_documento"));
                    v.setClienteId(rs.getInt("cliente_id"));
                    v.setVendedorId(rs.getInt("vendedor_id"));
                    v.setFechaEmision(rs.getDate("fecha_emision"));
                    v.setFormaPago(rs.getString("forma_pago"));
                    v.setNeto(rs.getDouble("subtotal"));
                    v.setDescuento(rs.getDouble("descuento"));
                    v.setIva(rs.getDouble("iva"));
                    v.setTotal(rs.getDouble("total"));
                    v.setEstado(rs.getString("estado"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener venta por ID: " + e.getMessage());
        }
        return v;
    }

    /**
     * Obtiene el número de folio máximo registrado en la base de datos y le
     * suma 1. Si no hay ventas registradas, retorna 1.
     */
    // 4. SOBRECARGA OBTENER SIGUIENTE FOLIO SIN PARÁMETROS
    public int obtenerSiguienteFolio() {
        return obtenerSiguienteFolio("BOLETA ELECTRONICA");
    }

    /**
     * Obtiene el consolidado de ventas por tipo de documento en un rango de
     * fechas.
     *
     * @param fechaInicial
     * @param fechaFinal
     * @return
     */
    public List<Object[]> obtenerResumenVentasPorPeriodo(Date fechaInicial, Date fechaFinal) {
        List<Object[]> lista = new ArrayList<>();

        // Ajustar fechas para cubrir desde las 00:00:00 del primer día hasta las 23:59:59 del último día
        java.sql.Date sqlInicio = new java.sql.Date(fechaInicioSinHora(fechaInicial).getTime());
        java.sql.Date sqlFin = new java.sql.Date(fechaFinDelDia(fechaFinal).getTime());

        // Consulta SQL ajustada exactamente a la estructura de la tabla 'ventas'
        String sql = "SELECT tipo_documento, "
                + "       COUNT(id) AS cant_emitida, "
                + "       SUM(subtotal) AS total_neto, "
                + "       SUM(total) AS total_venta "
                + "FROM ventas "
                + "WHERE fecha_emision BETWEEN ? AND ? "
                + "  AND estado != 'ANULADA' "
                + "GROUP BY tipo_documento "
                + "ORDER BY tipo_documento ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, sqlInicio);
            ps.setDate(2, sqlFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("tipo_documento"),
                        rs.getInt("cant_emitida"),
                        rs.getDouble("total_neto"),
                        rs.getDouble("total_venta")
                    });
                }
            }

        } catch (Exception e) {
            System.err.println("Error al obtener resumen de ventas: " + e.getMessage());
        }

        return lista;
    }

    private Date fechaInicioSinHora(Date fecha) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTime();
    }

    private Date fechaFinDelDia(Date fecha) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        return cal.getTime();
    }

    // 1. Cargar clientes para el JComboBox
    public List<ClienteCombo> cargarClientes() {
        List<ClienteCombo> lista = new ArrayList<>();
        lista.add(new ClienteCombo(0, "", "Todos los Clientes"));

        String sql = "SELECT id, rut, nombre FROM clientes WHERE activo = true ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ClienteCombo(
                        rs.getInt("id"),
                        rs.getString("rut"),
                        rs.getString("nombre")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
        }
        return lista;
    }

// 2. Obtener ventas filtradas por rango de fechas y cliente opcional
    public List<Object[]> obtenerVentasPorCliente(Date fechaInicial, Date fechaFinal, int clienteId) {
        List<Object[]> lista = new ArrayList<>();

        java.sql.Date sqlInicio = new java.sql.Date(fechaInicioSinHora(fechaInicial).getTime());
        java.sql.Date sqlFin = new java.sql.Date(fechaFinDelDia(fechaFinal).getTime());

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.rut, c.nombre, v.numero_documento, v.fecha_emision, v.total ")
                .append("FROM ventas v ")
                .append("INNER JOIN clientes c ON v.cliente_id = c.id ")
                .append("WHERE v.fecha_emision BETWEEN ? AND ? ")
                .append("  AND v.estado != 'ANULADA' ");

        if (clienteId > 0) {
            sql.append("  AND v.cliente_id = ? ");
        }

        sql.append("ORDER BY v.fecha_emision DESC, v.numero_documento DESC");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setDate(1, sqlInicio);
            ps.setDate(2, sqlFin);

            if (clienteId > 0) {
                ps.setInt(3, clienteId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getString("rut"),
                        rs.getString("nombre"),
                        rs.getString("numero_documento"),
                        rs.getDate("fecha_emision"),
                        rs.getDouble("total")
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener ventas por cliente: " + e.getMessage());
        }

        return lista;
    }

    public List<DetalleVenta> obtenerDetallesPorVentaId(int ventaId) {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT d.id, d.venta_id, d.producto_id, p.nombre AS producto_nombre, "
                + "d.cantidad, d.precio_unitario, d.descuento, d.subtotal "
                + "FROM detalle_ventas d "
                + "LEFT JOIN productos p ON d.producto_id = p.id "
                + "WHERE d.venta_id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ventaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleVenta det = new DetalleVenta();
                    det.setId(rs.getInt("id"));
                    det.setVentaId(rs.getInt("venta_id"));
                    det.setProductoId(rs.getInt("producto_id"));

                    // Asignar el nombre del producto si el modelo DetalleVenta lo soporta,
                    // o usar observaciones para mostrar la descripción
                    det.setObservaciones(rs.getString("producto_nombre"));
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setPrecioUnitario(rs.getDouble("precio_unitario"));
                    det.setDescuento(rs.getDouble("descuento"));
                    det.setTotalLinea(rs.getDouble("subtotal"));

                    lista.add(det);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el detalle de venta: " + e.getMessage());
        }

        return lista;
    }

}
