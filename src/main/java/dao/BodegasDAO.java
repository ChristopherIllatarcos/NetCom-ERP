package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import modelo.ItemCombo;

public class BodegasDAO {

    public List<ItemCombo> listarBodegasCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        lista.add(new ItemCombo(0, "-- SELECCIONE BODEGA / SECTOR --"));

        String sql = "SELECT id, nombre FROM bodegas WHERE activo = TRUE AND nombre IS NOT NULL AND TRIM(nombre) != '' ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar bodegas para combo: " + e.getMessage());
        }

        return lista;
    }

    public boolean guardarBodega(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("Error: El nombre de la bodega no puede estar vacío.");
            return false;
        }

        // Usar timestamp completo para garantizar unicidad absoluta sin colisiones
        String codigoAuto = "SEC-" + System.currentTimeMillis();

        String sql = "INSERT INTO bodegas (codigo, nombre, activo) VALUES (?, ?, TRUE)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoAuto);
            ps.setString(2, nombre.trim());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar bodega: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarBodega(int id, String nuevoNombre) {
        if (id <= 0 || nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            System.err.println("Error: El ID debe ser válido y el nuevo nombre no puede estar vacío.");
            return false;
        }

        String sql = "UPDATE bodegas SET nombre = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar bodega: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarBodega(int id) {
        if (id <= 0) {
            System.err.println("Error: ID de bodega inválido.");
            return false;
        }

        // Borrado lógico (Soft Delete): Desactiva la bodega en lugar de borrarla físicamente
        // para preservar la trazabilidad histórica de inventarios y movimientos.
        String sql = "UPDATE bodegas SET activo = FALSE WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar la bodega: " + e.getMessage());
            return false;
        }
    }

    public List<ItemCombo> listarBodegasComboParaEditar() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM bodegas WHERE activo = TRUE AND nombre IS NOT NULL AND TRIM(nombre) != '' ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar bodegas para edición: " + e.getMessage());
        }

        return lista;
    }

    public boolean registrarIngresoMercaderia(
            String folio,
            int tipoMotivoId,
            java.util.Date fechaIngreso,
            String observacion,
            java.util.List<modelo.DetalleIngresoDTO> listaDetalle) {

        if (listaDetalle == null || listaDetalle.isEmpty()) {
            return false;
        }

        String sqlCompra = "INSERT INTO compras (numero_factura, tipo, fecha_emision, proveedor_id, subtotal, iva, total, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_compras (compra_id, producto_id, cantidad, precio_unitario, subtotal, codigo_producto, descripcion, costo_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?";
        String sqlMovimiento = "INSERT INTO movimientos_stock (producto_id, tipo_movimiento, origen_id, cantidad, saldo_anterior, saldo_nuevo, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false); // Inicio de transacción segura

            double totalGral = 0.0;
            for (modelo.DetalleIngresoDTO item : listaDetalle) {
                totalGral += item.getSubtotal();
            }

            long compraIdGenerado = 0;

            // 1. Insertar Cabecera de Compra
            try (PreparedStatement psCompra = con.prepareStatement(sqlCompra, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psCompra.setString(1, folio);
                psCompra.setString(2, "INGRESO BODEGA");
                psCompra.setDate(3, new java.sql.Date(fechaIngreso.getTime()));
                psCompra.setInt(4, 1); // Proveedor por defecto o configurable
                psCompra.setDouble(5, totalGral);
                psCompra.setDouble(6, 0.0);
                psCompra.setDouble(7, totalGral);
                psCompra.setString(8, observacion);
                psCompra.executeUpdate();

                try (ResultSet rsKeys = psCompra.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        compraIdGenerado = rsKeys.getLong(1);
                    }
                }
            }

            ProductosDAO prodDao = new ProductosDAO();

            // 2. Preparar operaciones por lotes para el detalle, stock y movimientos
            try (PreparedStatement psDet = con.prepareStatement(sqlDetalle); PreparedStatement psStk = con.prepareStatement(sqlStock); PreparedStatement psMov = con.prepareStatement(sqlMovimiento)) {

                for (modelo.DetalleIngresoDTO item : listaDetalle) {
                    // Consultas de apoyo usando la misma lógica de negocio
                    int productoId = prodDao.obtenerIdPorCodigo(item.getCodigo());
                    modelo.Producto prodObj = prodDao.buscarProductoBodega(item.getCodigo());
                    double stockAnterior = (prodObj != null) ? prodObj.getStockActual() : 0.0;
                    double stockNuevo = stockAnterior + item.getCantidad();

                    // Parametrizar Detalle
                    psDet.setLong(1, compraIdGenerado);
                    psDet.setInt(2, productoId);
                    psDet.setInt(3, item.getCantidad());
                    psDet.setDouble(4, item.getCostoUnitario());
                    psDet.setDouble(5, item.getSubtotal());
                    psDet.setString(6, item.getCodigo());
                    psDet.setString(7, item.getDescripcion());
                    psDet.setDouble(8, item.getCostoUnitario());
                    psDet.addBatch();

                    // Parametrizar Stock
                    psStk.setInt(1, item.getCantidad());
                    psStk.setInt(2, productoId);
                    psStk.addBatch();

                    // Parametrizar Movimiento de Kardex
                    psMov.setInt(1, productoId);
                    psMov.setString(2, "ENTRADA");
                    psMov.setInt(3, (int) compraIdGenerado);
                    psMov.setInt(4, item.getCantidad());
                    psMov.setDouble(5, stockAnterior);
                    psMov.setDouble(6, stockNuevo);
                    psMov.setString(7, "Ingreso de mercadería - Folio: " + folio);
                    psMov.addBatch();
                }

                // Ejecutar lotes masivos
                psDet.executeBatch();
                psStk.executeBatch();
                psMov.executeBatch();
            }

            con.commit(); // Confirmar transacción completa
            return true;

        } catch (SQLException e) {
            System.err.println("Error crítico en transacción de ingreso de bodega: " + e.getMessage());
            // TODO: Agregar con.rollback() seguro si la conexión sigue abierta
            return false;
        }
    }

    public java.util.List<modelo.HistorialIngresoDTO> listarIngresosParaHistorial() {
        java.util.List<modelo.HistorialIngresoDTO> lista = new java.util.ArrayList<>();
        String sql = "SELECT c.id, c.numero_factura, c.fecha_emision, "
                + "STRING_AGG(d.descripcion, ', ') AS descripciones, "
                + "SUM(d.cantidad) AS cantidad_total, "
                + "c.subtotal, c.observaciones "
                + "FROM compras c "
                + "LEFT JOIN detalle_compras d ON c.id = d.compra_id "
                + "GROUP BY c.id, c.numero_factura, c.fecha_emision, c.subtotal, c.observaciones "
                + "ORDER BY c.id DESC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.HistorialIngresoDTO dto = new modelo.HistorialIngresoDTO(
                        rs.getInt("id"),
                        rs.getString("numero_factura"),
                        rs.getDate("fecha_emision"),
                        rs.getString("descripciones"),
                        rs.getInt("cantidad_total"),
                        rs.getDouble("subtotal"),
                        rs.getString("observaciones")
                );
                lista.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historial de ingresos: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizarIngresoCabecera(int idCompra, String nuevoFolio, java.util.Date nuevaFecha) {
        if (nuevaFecha == null) {
            System.err.println("Error: La fecha de emisión no puede ser nula.");
            return false;
        }

        String sql = "UPDATE compras SET numero_factura = ?, fecha_emision = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoFolio);
            ps.setDate(2, new java.sql.Date(nuevaFecha.getTime()));
            ps.setInt(3, idCompra);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar la cabecera del ingreso: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarDetalleCompra(int detalleId, int nuevaCantidad, double nuevoCosto, String nuevaDescripcion) {
        double nuevoSubtotal = nuevaCantidad * nuevoCosto;
        String sqlReal = "UPDATE detalle_compras SET cantidad = ?, costo_unitario = ?, precio_unitario = ?, subtotal = ?, descripcion = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sqlReal)) {
            ps.setInt(1, nuevaCantidad);
            ps.setDouble(2, nuevoCosto);
            ps.setDouble(3, nuevoCosto);
            ps.setDouble(4, nuevoSubtotal);
            ps.setString(5, nuevaDescripcion);
            ps.setInt(6, detalleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el detalle de compra: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarIngresoCompleto(int idCompra, String nuevoFolio, java.util.Date nuevaFecha, String nuevaObservacion, String nuevaDescripcion, int nuevaCantidad, double nuevoCosto) {
        // Blindaje defensivo contra nulos
        if (nuevaFecha == null) {
            System.err.println("Error de validación: La fecha de emisión no puede ser nula.");
            return false;
        }

        String sqlCompra = "UPDATE compras SET numero_factura = ?, fecha_emision = ?, observaciones = ? WHERE id = ?";
        String sqlSubtotalDetalle = "UPDATE detalle_compras SET descripcion = ?, cantidad = ?, costo_unitario = ?, precio_unitario = ?, subtotal = ? WHERE compra_id = ?";
        double nuevoSubtotal = nuevaCantidad * nuevoCosto;

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false); // Iniciamos la transacción manual (ACID)

            // 1. Actualizar la cabecera de la compra
            try (PreparedStatement psComp = con.prepareStatement(sqlCompra)) {
                psComp.setString(1, nuevoFolio);
                psComp.setDate(2, new java.sql.Date(nuevaFecha.getTime()));
                psComp.setString(3, nuevaObservacion);
                psComp.setInt(4, idCompra);
                psComp.executeUpdate();
            }

            // 2. Actualizar el detalle de la compra
            try (PreparedStatement psDet = con.prepareStatement(sqlSubtotalDetalle)) {
                psDet.setString(1, nuevaDescripcion);
                psDet.setInt(2, nuevaCantidad);
                psDet.setDouble(3, nuevoCosto);
                psDet.setDouble(4, nuevoCosto);
                psDet.setDouble(5, nuevoSubtotal);
                psDet.setInt(6, idCompra);
                psDet.executeUpdate();
            }

            con.commit(); // Si todo salió bien, confirmamos los cambios
            return true;

        } catch (SQLException e) {
            // Si ocurre cualquier error, revertimos la transacción para mantener la consistencia
            if (con != null) {
                try {
                    con.rollback();
                    System.err.println("Transacción revertida exitosamente (Rollback aplicado).");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error crítico al intentar hacer rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error al actualizar ingreso completo: " + e.getMessage());
            return false;
        } finally {
            // Nos aseguramos de cerrar la conexión de forma segura en el bloque finally
            if (con != null) {
                try {
                    con.setAutoCommit(true); // Restauramos el estado por defecto
                    con.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error al cerrar la conexión: " + closeEx.getMessage());
                }
            }
        }
    }

    public java.util.List<Object[]> listarDetallesPorCompra(int idCompra) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
        String sql = "SELECT codigo_producto, descripcion, cantidad, costo_unitario, subtotal FROM detalle_compras WHERE compra_id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[5];
                    fila[0] = rs.getString("codigo_producto");
                    fila[1] = rs.getString("descripcion");
                    fila[2] = rs.getInt("cantidad");
                    fila[3] = rs.getDouble("costo_unitario");
                    fila[4] = rs.getDouble("subtotal");
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar los detalles de la compra: " + e.getMessage());
        }
        return lista;
    }

    public Object[] obtenerPrimerDetalleCompra(int idCompra) {
        Object[] datos = new Object[]{1, 0.0};
        String sql = "SELECT cantidad, costo_unitario FROM detalle_compras WHERE compra_id = ? LIMIT 1";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    datos[0] = rs.getInt("cantidad");
                    datos[1] = rs.getDouble("costo_unitario");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener detalle para editar: " + e.getMessage());
        }
        return datos;
    }

    public java.util.List<Object[]> listarIngresosFiltrados(String folio, java.util.Date fecha) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.numero_factura, c.fecha_emision, "
                + "STRING_AGG(d.descripcion, ', ') AS descripciones, "
                + "SUM(d.cantidad) AS cantidad_total, "
                + "c.subtotal, c.observaciones "
                + "FROM compras c "
                + "LEFT JOIN detalle_compras d ON c.id = d.compra_id "
                + "WHERE 1=1"
        );

        boolean filtrarFolio = (folio != null && !folio.isEmpty());
        boolean filtrarFecha = (fecha != null);

        if (filtrarFolio) {
            sql.append(" AND c.numero_factura ILIKE ?");
        }
        if (filtrarFecha) {
            sql.append(" AND c.fecha_emision = ?");
        }

        sql.append(" GROUP BY c.id, c.numero_factura, c.fecha_emision, c.subtotal, c.observaciones ")
                .append(" ORDER BY c.id DESC");

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (filtrarFolio) {
                ps.setString(index++, "%" + folio + "%");
            }
            if (filtrarFecha) {
                ps.setDate(index++, new java.sql.Date(fecha.getTime()));
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[7];
                    fila[0] = rs.getInt("id");
                    fila[1] = rs.getString("numero_factura");
                    fila[2] = rs.getDate("fecha_emision");
                    fila[3] = rs.getString("descripciones");
                    fila[4] = rs.getInt("cantidad_total");
                    fila[5] = rs.getDouble("subtotal");
                    fila[6] = rs.getString("observaciones");
                    lista.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al listar ingresos filtrados: " + e.getMessage());
        }
        return lista;
    }

    public java.util.List<Object[]> obtenerHistorialKardex(String codProducto, java.util.Date fechaInicio, java.util.Date fechaFin) {
        java.util.List<Object[]> listaMovimientos = new java.util.ArrayList<>();

        String sql = "SELECT m.created_at AS fecha, "
                + "       m.tipo_movimiento, "
                + "       m.origen_id AS nro_doc, "
                + "       CASE WHEN m.tipo_movimiento ILIKE '%ENTRADA%' THEN m.cantidad ELSE 0 END AS entrada, "
                + "       CASE WHEN m.tipo_movimiento ILIKE '%SALIDA%' THEN m.cantidad ELSE 0 END AS salida, "
                + "       m.saldo_nuevo AS saldo "
                + "FROM movimientos_stock m "
                + "JOIN productos p ON m.producto_id = p.id "
                + "WHERE p.codigo = ? "
                + "  AND m.created_at >= ? AND m.created_at <= ? "
                + "ORDER BY m.created_at ASC";

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codProducto);

            java.sql.Timestamp tsInicio = fechaInicio != null ? new java.sql.Timestamp(fechaInicio.getTime()) : java.sql.Timestamp.valueOf("2000-01-01 00:00:00");
            java.sql.Timestamp tsFin = fechaFin != null ? new java.sql.Timestamp(fechaFin.getTime() + (24L * 60 * 60 * 1000 - 1)) : new java.sql.Timestamp(System.currentTimeMillis());

            ps.setTimestamp(2, tsInicio);
            ps.setTimestamp(3, tsFin);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[6];
                    fila[0] = rs.getTimestamp("fecha");
                    fila[1] = rs.getString("tipo_movimiento");
                    fila[2] = rs.getObject("nro_doc");
                    fila[3] = rs.getInt("entrada");
                    fila[4] = rs.getInt("salida");
                    fila[5] = rs.getInt("saldo");

                    listaMovimientos.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar el Kardex: " + e.getMessage());
        }

        return listaMovimientos;
    }

    public java.util.List<Object[]> obtenerInventario(java.util.Date fechaAlCorte, String familiaSeleccionada, boolean verConCero) {
        java.util.List<Object[]> listaInventario = new java.util.ArrayList<>();

        // Blindaje defensivo del parámetro de familia
        String familiaFiltro = (familiaSeleccionada == null || familiaSeleccionada.trim().isEmpty()) ? "TODAS LAS FAMILIAS" : familiaSeleccionada.trim();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.codigo, p.nombre AS descripcion, f.nombre AS familia, p.stock_actual AS stock, p.precio_compra AS costo ");
        sql.append("FROM productos p ");
        sql.append("LEFT JOIN familias f ON p.familia_id = f.id ");
        sql.append("WHERE p.activo = true ");

        boolean filtrarFamilia = !familiaFiltro.equals("TODAS LAS FAMILIAS");
        if (filtrarFamilia) {
            sql.append("AND f.nombre = ? ");
        }

        if (!verConCero) {
            sql.append("AND p.stock_actual > 0 ");
        }

        sql.append("ORDER BY p.nombre ASC");

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (filtrarFamilia) {
                ps.setString(1, familiaFiltro);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String descripcion = rs.getString("descripcion");

                    // Manejo seguro de nulos para la familia
                    String familia = rs.getString("familia");
                    if (familia == null || familia.trim().isEmpty()) {
                        familia = "GENERAL";
                    }

                    int stock = rs.getInt("stock");
                    double costo = rs.getDouble("costo");
                    double totalValorizado = stock * costo;

                    Object[] fila = new Object[6];
                    fila[0] = (codigo != null) ? codigo : "S/C";
                    fila[1] = (descripcion != null) ? descripcion : "Sin descripción";
                    fila[2] = familia;
                    fila[3] = stock;
                    fila[4] = costo;
                    fila[5] = totalValorizado;

                    listaInventario.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error crítico al consultar el inventario: " + e.getMessage());
        }

        return listaInventario;
    }

    public java.util.List<Object[]> obtenerInventarioCritico(String familiaSeleccionada, boolean soloStockCero) {
        java.util.List<Object[]> listaCriticos = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.codigo, p.nombre AS descripcion, p.stock_actual AS stock, p.stock_minimo AS stock_min, f.nombre AS familia ");
        sql.append("FROM productos p ");
        sql.append("LEFT JOIN familias f ON p.familia_id = f.id ");
        sql.append("WHERE p.activo = true ");

        if (soloStockCero) {
            sql.append("AND p.stock_actual = 0 ");
        } else {
            sql.append("AND p.stock_actual <= p.stock_minimo ");
        }

        if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
            sql.append("AND f.nombre = ? ");
        }

        sql.append("ORDER BY p.stock_actual ASC, p.nombre ASC");

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                ps.setString(index++, familiaSeleccionada);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String descripcion = rs.getString("descripcion");
                    int stock = rs.getInt("stock");
                    int stockMin = rs.getInt("stock_min");

                    int faltante = stockMin - stock;
                    if (faltante < 0) {
                        faltante = 0;
                    }

                    Object[] fila = new Object[5];
                    fila[0] = codigo;
                    fila[1] = descripcion;
                    fila[2] = stock;
                    fila[3] = stockMin;
                    fila[4] = faltante;

                    listaCriticos.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar inventario crítico: " + e.getMessage());
        }

        return listaCriticos;
    }

    public java.util.List<Object[]> obtenerRotacionVentas(
            boolean agruparPorProducto, String familiaSeleccionada,
            java.util.Date fechaInicio, java.util.Date fechaFin) {

        java.util.List<Object[]> listaRotacion = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder();

        if (agruparPorProducto) {
            sql.append("SELECT p.codigo, p.nombre AS descripcion, ");
            sql.append("SUM(m.cantidad) AS cant_vend, ");
            sql.append("SUM(m.cantidad * COALESCE(m.precio_promedio, p.precio_venta, 0)) AS total_vta ");
            sql.append("FROM movimientos_stock m ");
            sql.append("JOIN productos p ON m.producto_id = p.id ");
            sql.append("LEFT JOIN familias f ON p.familia_id = f.id ");
            sql.append("WHERE UPPER(m.tipo_movimiento) = 'SALIDA' ");

            if (fechaInicio != null && fechaFin != null) {
                sql.append("AND m.created_at BETWEEN ? AND ? ");
            }
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                sql.append("AND f.nombre = ? ");
            }
            sql.append("GROUP BY p.codigo, p.nombre ORDER BY total_vta DESC");

        } else {
            sql.append("SELECT 'N/A' AS codigo, f.nombre AS descripcion, ");
            sql.append("SUM(m.cantidad) AS cant_vend, ");
            sql.append("SUM(m.cantidad * COALESCE(m.precio_promedio, p.precio_venta, 0)) AS total_vta ");
            sql.append("FROM movimientos_stock m ");
            sql.append("JOIN productos p ON m.producto_id = p.id ");
            sql.append("JOIN familias f ON p.familia_id = f.id ");
            sql.append("WHERE UPPER(m.tipo_movimiento) = 'SALIDA' ");

            if (fechaInicio != null && fechaFin != null) {
                sql.append("AND m.created_at BETWEEN ? AND ? ");
            }
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                sql.append("AND f.nombre = ? ");
            }
            sql.append("GROUP BY f.nombre ORDER BY total_vta DESC");
        }

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (fechaInicio != null && fechaFin != null) {
                ps.setTimestamp(index++, new java.sql.Timestamp(fechaInicio.getTime()));
                ps.setTimestamp(index++, new java.sql.Timestamp(fechaFin.getTime() + 86399000L));
            }
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                ps.setString(index++, familiaSeleccionada);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                class RegistroTemp {

                    String codigo, desc;
                    double cant, totalVta;
                }
                java.util.List<RegistroTemp> temporales = new java.util.ArrayList<>();
                double ventaTotalGeneral = 0.0;

                while (rs.next()) {
                    RegistroTemp reg = new RegistroTemp();
                    reg.codigo = rs.getString("codigo");
                    reg.desc = rs.getString("descripcion");
                    reg.cant = rs.getDouble("cant_vend");
                    reg.totalVta = rs.getDouble("total_vta");

                    ventaTotalGeneral += reg.totalVta;
                    temporales.add(reg);
                }

                for (RegistroTemp t : temporales) {
                    double porcentaje = (ventaTotalGeneral > 0) ? (t.totalVta / ventaTotalGeneral) * 100 : 0.0;

                    Object[] fila = new Object[5];
                    fila[0] = t.codigo;
                    fila[1] = t.desc;
                    fila[2] = t.cant;
                    fila[3] = t.totalVta;
                    fila[4] = String.format("%.2f%%", porcentaje);

                    listaRotacion.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar rotación de ventas: " + e.getMessage());
        }

        return listaRotacion;
    }

    public java.util.List<Object[]> obtenerVentasPorFamilia(
            String familiaSeleccionada,
            java.util.Date fechaDesde, java.util.Date fechaHasta) {

        java.util.List<Object[]> listaVentas = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT f.id AS cod_fam, f.nombre AS nombre_familia, ");
        sql.append("SUM(m.cantidad) AS cant_vendida, ");
        sql.append("SUM(m.cantidad * COALESCE(m.precio_promedio, p.precio_venta, 0)) AS monto_total ");
        sql.append("FROM movimientos_stock m ");
        sql.append("JOIN productos p ON m.producto_id = p.id ");
        sql.append("JOIN familias f ON p.familia_id = f.id ");
        sql.append("WHERE UPPER(m.tipo_movimiento) = 'SALIDA' ");

        if (fechaDesde != null && fechaHasta != null) {
            sql.append("AND m.created_at BETWEEN ? AND ? ");
        }
        if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
            sql.append("AND f.nombre = ? ");
        }

        sql.append("GROUP BY f.id, f.nombre ORDER BY monto_total DESC");

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (fechaDesde != null && fechaHasta != null) {
                ps.setTimestamp(index++, new java.sql.Timestamp(fechaDesde.getTime()));
                ps.setTimestamp(index++, new java.sql.Timestamp(fechaHasta.getTime() + 86399000L));
            }
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                ps.setString(index++, familiaSeleccionada);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[4];
                    fila[0] = rs.getInt("cod_fam");
                    fila[1] = rs.getString("nombre_familia");
                    fila[2] = rs.getDouble("cant_vendida");
                    fila[3] = rs.getDouble("monto_total");

                    listaVentas.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar ventas por familia: " + e.getMessage());
        }

        return listaVentas;
    }
}
