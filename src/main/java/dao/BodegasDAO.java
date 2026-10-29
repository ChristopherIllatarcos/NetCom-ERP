package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

public class BodegasDAO {

    public static class ItemCombo {

        private final int id;
        private final String nombre;

        public ItemCombo(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre; // Esto es lo que se muestra visualmente en el ComboBox
        }
    }

    public List<ItemCombo> listarBodegasCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        // Agregamos una opción por defecto arriba del todo
        lista.add(new ItemCombo(0, "-- SELECCIONE BODEGA / SECTOR --"));

        String sql = "SELECT id, nombre FROM bodegas WHERE activo = TRUE ORDER BY nombre ASC";

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
        // Generamos un código automático basado en el nombre o un prefijo
        String codigoAuto = "SEC-" + System.currentTimeMillis() % 1000; // Ejemplo simple: SEC-789

        String sql = "INSERT INTO bodegas (codigo, nombre, activo) VALUES (?, ?, TRUE)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoAuto);
            ps.setString(2, nombre);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar bodega: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarBodega(int id, String nuevoNombre) {
        String sql = "UPDATE bodegas SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar bodega: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarBodega(int id) {
        // Puedes hacer un DELETE físico o un SOFT DELETE (activo = FALSE)
        String sql = "DELETE FROM bodegas WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar bodega (llave foránea en uso): " + e.getMessage());
            return false;
        }
    }

    public List<ItemCombo> listarBodegasComboParaEditar() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM bodegas WHERE activo = TRUE ORDER BY nombre ASC";

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
            javax.swing.table.DefaultTableModel modeloDetalle) {

        String sqlCompra = "INSERT INTO compras (numero_factura, tipo, fecha_emision, proveedor_id, subtotal, iva, total, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_compras (compra_id, producto_id, cantidad, precio_unitario, subtotal, codigo_producto, descripcion, costo_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE productos SET stock_actual = stock_actual + ? WHERE id = ?";
        String sqlMovimiento = "INSERT INTO movimientos_stock (producto_id, tipo_movimiento, origen_id, cantidad, saldo_anterior, saldo_nuevo, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false); // Transacción segura

            // 1. Calcular total general desde la tabla
            double totalGral = 0.0;
            int filas = modeloDetalle.getRowCount();
            for (int i = 0; i < filas; i++) {
                totalGral += Double.parseDouble(modeloDetalle.getValueAt(i, 4).toString().replace(",", "."));
            }

            long compraIdGenerado = 0;
            // 2. Insertar cabecera (compras)
            try (PreparedStatement psCompra = con.prepareStatement(sqlCompra, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psCompra.setString(1, folio);
                psCompra.setString(2, "INGRESO BODEGA");
                psCompra.setDate(3, new java.sql.Date(fechaIngreso.getTime()));
                psCompra.setInt(4, 1); // Proveedor por defecto o genérico
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

            // 3. Iterar detalle, insertar, actualizar stock y registrar movimientos
            ProductosDAO prodDao = new ProductosDAO();
            for (int i = 0; i < filas; i++) {
                String codigo = modeloDetalle.getValueAt(i, 0).toString();
                String descripcion = modeloDetalle.getValueAt(i, 1).toString();
                int cantidad = Integer.parseInt(modeloDetalle.getValueAt(i, 2).toString());
                double costoUnit = Double.parseDouble(modeloDetalle.getValueAt(i, 3).toString().replace(",", "."));
                double subtotal = Double.parseDouble(modeloDetalle.getValueAt(i, 4).toString().replace(",", "."));

                int productoId = prodDao.obtenerIdPorCodigo(codigo);
                modelo.Producto prodObj = prodDao.buscarProductoBodega(codigo);
                double stockAnterior = (prodObj != null) ? prodObj.getStockActual() : 0.0;
                double stockNuevo = stockAnterior + cantidad;

                // Insertar detalle de compra
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                    psDet.setLong(1, compraIdGenerado);
                    psDet.setInt(2, productoId);
                    psDet.setInt(3, cantidad);
                    psDet.setDouble(4, costoUnit);
                    psDet.setDouble(5, subtotal);
                    psDet.setString(6, codigo);
                    psDet.setString(7, descripcion);
                    psDet.setDouble(8, costoUnit);
                    psDet.executeUpdate();
                }

                // Actualizar stock en productos
                try (PreparedStatement psStk = con.prepareStatement(sqlStock)) {
                    psStk.setInt(1, cantidad);
                    psStk.setInt(2, productoId);
                    psStk.executeUpdate();
                }

                // Registrar movimiento de stock
                try (PreparedStatement psMov = con.prepareStatement(sqlMovimiento)) {
                    psMov.setInt(1, productoId);
                    psMov.setString(2, "ENTRADA");
                    psMov.setInt(3, (int) compraIdGenerado);
                    psMov.setInt(4, cantidad);
                    psMov.setDouble(5, stockAnterior);
                    psMov.setDouble(6, stockNuevo);
                    psMov.setString(7, "Ingreso de mercadería - Folio: " + folio);
                    psMov.executeUpdate();
                }
            }

            con.commit(); // Todo correcto, guardado permanente
            return true;

        } catch (SQLException e) {
            System.err.println("Error crítico en transacción de ingreso de bodega: " + e.getMessage());
            return false;
        }
    }

    // Método para listar todos los ingresos de mercadería registrados en la cabecera
    public java.util.List<Object[]> listarIngresosParaHistorial() {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
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
                Object[] fila = new Object[7]; // Ahora son 7 columnas
                fila[0] = rs.getInt("id");
                fila[1] = rs.getString("numero_factura");
                fila[2] = rs.getDate("fecha_emision");
                fila[3] = rs.getString("descripciones");
                fila[4] = rs.getInt("cantidad_total"); // <--- Nueva columna cantidad
                fila[5] = rs.getDouble("subtotal");
                fila[6] = rs.getString("observaciones");
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historial: " + e.getMessage());
        }
        return lista;
    }

    // Método para actualizar la cabecera del ingreso (Folio y Fecha)
    public boolean actualizarIngresoCabecera(int idCompra, String nuevoFolio, java.util.Date nuevaFecha) {
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

    // Método para actualizar un ítem del detalle de compra
    public boolean actualizarDetalleCompra(int detalleId, int nuevaCantidad, double nuevoCosto, String nuevaDescripcion) {
        String sql = "UPDATE detalle_compras SET cantidad = ?, costo_unitario = ?, precio_unitario = ?, subtotal = (?, ?), descripcion = ? WHERE id = ?";
        // Nota: calculamos el subtotal multiplicando cantidad * costo
        double nuevoSubtotal = nuevaCantidad * nuevoCosto;

        String sqlReal = "UPDATE detalle_compras SET cantidad = ?, costo_unitario = ?, precio_unitario = ?, subtotal = ?, descripcion = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sqlReal)) {
            ps.setInt(1, nuevaCantidad);
            ps.setDouble(2, nuevoCosto);
            ps.setDouble(3, nuevoCosto); // precio_unitario suele ser igual al costo en ingresos
            ps.setDouble(4, nuevoSubtotal);
            ps.setString(5, nuevaDescripcion);
            ps.setInt(6, detalleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar el detalle de compra: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar de forma integral la cabecera y el detalle de un ingreso
    public boolean actualizarIngresoCompleto(int idCompra, String nuevoFolio, java.util.Date nuevaFecha, String nuevaObservacion, String nuevaDescripcion, int nuevaCantidad, double nuevoCosto) {
        String sqlCompra = "UPDATE compras SET numero_factura = ?, fecha_emision = ?, observaciones = ? WHERE id = ?";
        // Como el detalle puede tener varios productos, aquí actualizamos por el idCompra (o si prefieres por el id del detalle)
        String sqlDetalle = "UPDATE detalle_compras SET descripcion = ?, cantidad = ?, subtotal = (cantidad * costo_unitario) WHERE compra_id = ?";
        // Nota: para simplificar si es un registro por línea o actualizar el primer ítem asociado
        String sqlSubtotalDetalle = "UPDATE detalle_compras SET descripcion = ?, cantidad = ?, costo_unitario = ?, precio_unitario = ?, subtotal = ? WHERE compra_id = ?";
        double nuevoSubtotal = nuevaCantidad * nuevoCosto;

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false); // Transacción segura

            // 1. Actualizar Cabecera (compras)
            try (PreparedStatement psComp = con.prepareStatement(sqlCompra)) {
                psComp.setString(1, nuevoFolio);
                psComp.setDate(2, new java.sql.Date(nuevaFecha.getTime()));
                psComp.setString(3, nuevaObservacion);
                psComp.setInt(4, idCompra);
                psComp.executeUpdate();
            }

            // 2. Actualizar Detalle (detalle_compras)
            try (PreparedStatement psDet = con.prepareStatement(sqlSubtotalDetalle)) {
                psDet.setString(1, nuevaDescripcion);
                psDet.setInt(2, nuevaCantidad);
                psDet.setDouble(3, nuevoCosto);
                psDet.setDouble(4, nuevoCosto);
                psDet.setDouble(5, nuevoSubtotal);
                psDet.setInt(6, idCompra);
                psDet.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar ingreso completo: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener los productos del detalle de una compra específica
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

    // Método para obtener los datos del primer detalle de una compra para la edición
    public Object[] obtenerPrimerDetalleCompra(int idCompra) {
        Object[] datos = new Object[]{1, 0.0}; // Valores por defecto por seguridad: cantidad 1, costo 0.0
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

    // Método para listar ingresos filtrados por Folio y/o Fecha de emisión
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

        // Agregamos condiciones dinámicas según lo que el usuario haya ingresado
        boolean filtrarFolio = (folio != null && !folio.isEmpty());
        boolean filtrarFecha = (fecha != null);

        if (filtrarFolio) {
            sql.append(" AND c.numero_factura ILIKE ?"); // ILIKE no distingue mayúsculas/minúsculas en PostgreSQL
        }
        if (filtrarFecha) {
            sql.append(" AND c.fecha_emision = ?");
        }

        sql.append(" GROUP BY c.id, c.numero_factura, c.fecha_emision, c.subtotal, c.observaciones ")
                .append(" ORDER BY c.id DESC");

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (filtrarFolio) {
                ps.setString(index++, "%" + folio + "%"); // Permite buscar coincidencias parciales
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

    // Método para obtener el historial del Kardex por producto y fechas
    public java.util.List<Object[]> obtenerHistorialKardex(String codProducto, java.util.Date fechaInicio, java.util.Date fechaFin) {
        java.util.List<Object[]> listaMovimientos = new java.util.ArrayList<>();

        // SQL robusto con rango de fechas seguro
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

            // Si la fecha inicial es nula, ponemos una fecha muy antigua por defecto
            java.sql.Timestamp tsInicio = fechaInicio != null ? new java.sql.Timestamp(fechaInicio.getTime()) : java.sql.Timestamp.valueOf("2000-01-01 00:00:00");

            // Si la fecha final es nula, ponemos la hora actual o fin del día actual
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

    // Método para consultar el inventario de productos con filtros de familia, fecha al corte y stock cero
    public java.util.List<Object[]> obtenerInventario(java.util.Date fechaAlCorte, String familiaSeleccionada, boolean verConCero) {
        java.util.List<Object[]> listaInventario = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.codigo, p.nombre AS descripcion, f.nombre AS familia, p.stock_actual AS stock, p.precio_compra AS costo ");
        sql.append("FROM productos p ");
        sql.append("LEFT JOIN familias f ON p.familia_id = f.id ");
        sql.append("WHERE p.activo = true ");

        // Filtro por familia si no es "TODAS LAS FAMILIAS"
        if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
            sql.append("AND f.nombre = ? ");
        }

        // Filtro por stock cero
        if (!verConCero) {
            sql.append("AND p.stock_actual > 0 ");
        }

        sql.append("ORDER BY p.nombre ASC");

        try (java.sql.Connection con = conexion.ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (familiaSeleccionada != null && !familiaSeleccionada.equals("TODAS LAS FAMILIAS")) {
                ps.setString(index++, familiaSeleccionada);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    String descripcion = rs.getString("descripcion");
                    String familia = rs.getString("familia");
                    if (familia == null) {
                        familia = "GENERAL";
                    }

                    int stock = rs.getInt("stock");
                    double costo = rs.getDouble("costo");
                    double totalValorizado = stock * costo;

                    Object[] fila = new Object[6];
                    fila[0] = codigo;
                    fila[1] = descripcion;
                    fila[2] = familia;
                    fila[3] = stock;
                    fila[4] = costo;
                    fila[5] = totalValorizado;

                    listaInventario.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar el inventario: " + e.getMessage());
        }

        return listaInventario;
    }

    // Método para obtener productos con stock crítico o agotados según los filtros
    public java.util.List<Object[]> obtenerInventarioCritico(String familiaSeleccionada, boolean soloStockCero) {
        java.util.List<Object[]> listaCriticos = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.codigo, p.nombre AS descripcion, p.stock_actual AS stock, p.stock_minimo AS stock_min, f.nombre AS familia ");
        sql.append("FROM productos p ");
        sql.append("LEFT JOIN familias f ON p.familia_id = f.id ");
        sql.append("WHERE p.activo = true ");

        // Si se marca "Stock Cero (Agotados)", filtramos stock == 0. 
        // Si es "Todos Críticos", filtramos stock <= stock_minimo (o puedes ajustar la condición de crítico si manejas otra lógica).
        if (soloStockCero) {
            sql.append("AND p.stock_actual = 0 ");
        } else {
            sql.append("AND p.stock_actual <= p.stock_minimo ");
        }

        // Filtro por familia si no es "TODAS LAS FAMILIAS"
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

                    // Calculamos el faltante (lo que falta para alcanzar el stock mínimo)
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

    // Método para consultar la rotación de ventas (por producto o por familia) 
    public java.util.List<Object[]> obtenerRotacionVentas(
            boolean agruparPorProducto, String familiaSeleccionada,
            java.util.Date fechaInicio, java.util.Date fechaFin) {

        java.util.List<Object[]> listaRotacion = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder();

        if (agruparPorProducto) {
            // Consulta agrupada por Producto usando precio_promedio de movimientos_stock y precio_venta de respaldo
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
            // Consulta agrupada por Familia
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
                // Fin del día (23:59:59)
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

    // Método para consultar ventas por familia / categoría
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
                // Fin del día (23:59:59)
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
