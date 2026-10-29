package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.PagoCliente;

public class CuentaCorrienteDAO {

    // Método para obtener la lista de pagos del cliente y mostrarlos en la tabla
    public List<PagoCliente> obtenerPagosPorCliente(int clienteId) {
        List<PagoCliente> lista = new ArrayList<>();
        // Añadimos estado y motivo a la consulta
        String sql = "SELECT numero_recibo, fecha, medio_pago, documento_asociado, monto_pagado, estado, motivo "
                + "FROM pagos_clientes WHERE cliente_id = ? ORDER BY fecha DESC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PagoCliente p = new PagoCliente();
                    p.setNumeroRecibo(rs.getString("numero_recibo"));
                    p.setFecha(rs.getString("fecha"));
                    p.setMedioPago(rs.getString("medio_pago"));
                    p.setDocumentoAsociado(rs.getString("documento_asociado"));
                    p.setMontoPagado(rs.getDouble("monto_pagado"));
                    p.setEstado(rs.getString("estado"));
                    p.setMotivo(rs.getString("motivo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos del cliente: " + e.getMessage());
        }

        return lista;
    }

    // Método para anular el pago seleccionado con su respectivo motivo
    public boolean anularPago(String numeroRecibo, String motivo) {
        String sql = "UPDATE pagos_clientes SET estado = 'ANULADO', motivo = ? WHERE numero_recibo = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setString(2, numeroRecibo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al anular pago: " + e.getMessage());
            return false;
        }
    }

    // Para cargar el combo de clientes (ej. ID y Nombre/RUT)
    public void cargarClientesCombo(javax.swing.JComboBox cmb) {
        cmb.removeAllItems();
        cmb.addItem(new modelo.ItemCombo(0, "-- Todos los Clientes --"));

        String sql = "SELECT id, rut, nombre FROM clientes WHERE activo = true ORDER BY nombre ASC";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String texto = rs.getString("rut") + " - " + rs.getString("nombre");
                cmb.addItem(new modelo.ItemCombo(id, texto));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al cargar clientes en combo: " + e.getMessage());
        }
    }

    public java.util.List<Object[]> consultarDocumentosCartera(int clienteId, String estado, java.util.Date fechaIni, java.util.Date fechaFin) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT v.id, c.rut, c.nombre, v.tipo_documento, v.folio, v.fecha_emision, v.fecha_vencimiento, v.total, v.estado ");
        sql.append("FROM ventas v INNER JOIN clientes c ON v.cliente_id = c.id WHERE 1=1 ");

        java.util.List<Object> parametros = new java.util.ArrayList<>();

        // Filtro por Cliente (si es 0, trae todos)
        if (clienteId > 0) {
            sql.append("AND v.cliente_id = ? ");
            parametros.add(clienteId);
        }

        // Filtro por Estado (si no es "TODOS")
        if (estado != null && !"TODOS".equalsIgnoreCase(estado)) {
            sql.append("AND v.estado = ? ");
            parametros.add(estado);
        }

        // Filtro por Rango de Fechas de emisión
        if (fechaIni != null) {
            sql.append("AND v.fecha_emision >= ? ");
            parametros.add(new java.sql.Date(fechaIni.getTime()));
        }
        if (fechaFin != null) {
            sql.append("AND v.fecha_emision <= ? ");
            parametros.add(new java.sql.Date(fechaFin.getTime()));
        }

        sql.append("ORDER BY v.fecha_emision DESC");

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            // Asignar parámetros dinámicamente
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[]{
                        rs.getInt("id"),
                        rs.getString("rut"),
                        rs.getString("nombre"),
                        rs.getString("tipo_documento"),
                        rs.getInt("folio"),
                        rs.getDate("fecha_emision"),
                        rs.getDate("fecha_vencimiento"),
                        rs.getBigDecimal("total"),
                        rs.getString("estado")
                    };
                    lista.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar documentos en cartera: " + e.getMessage());
        }

        return lista;
    }

    public java.util.Map<String, Object> obtenerEstadisticasCliente(int clienteId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // Valores por defecto en caso de que no tenga registros
        stats.put("saldo_pendiente", 0.0);
        stats.put("cant_compras", 0);
        stats.put("total_compras", 0.0);
        stats.put("prom_compras", 0.0);
        stats.put("ultima_compra", "-");
        stats.put("max_compra", 0.0);

        stats.put("cant_pagos", 0);
        stats.put("total_pagos", 0.0);
        stats.put("prom_pagos", 0.0);
        stats.put("ultimo_pago", "-");
        stats.put("max_pago", 0.0);

        // 1. Obtener saldo pendiente del cliente
        String sqlCliente = "SELECT saldo_pendiente FROM clientes WHERE id = ?";
        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sqlCliente)) {
            ps.setInt(1, clienteId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("saldo_pendiente", rs.getBigDecimal("saldo_pendiente") != null ? rs.getBigDecimal("saldo_pendiente").doubleValue() : 0.0);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener saldo del cliente: " + e.getMessage());
        }

        // 2. Obtener estadísticas de Ventas (Compras del cliente)
        String sqlVentas = "SELECT COUNT(*), SUM(total), AVG(total), MAX(fecha_emision), MAX(total) FROM ventas WHERE cliente_id = ?";
        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sqlVentas)) {
            ps.setInt(1, clienteId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    stats.put("cant_compras", rs.getInt(1));
                    stats.put("total_compras", rs.getBigDecimal(2) != null ? rs.getBigDecimal(2).doubleValue() : 0.0);
                    stats.put("prom_compras", rs.getBigDecimal(3) != null ? rs.getBigDecimal(3).doubleValue() : 0.0);
                    java.sql.Date ultimaFecha = rs.getDate(4);
                    if (ultimaFecha != null) {
                        stats.put("ultima_compra", new java.text.SimpleDateFormat("dd/MM/yyyy").format(ultimaFecha));
                    }
                    stats.put("max_compra", rs.getBigDecimal(5) != null ? rs.getBigDecimal(5).doubleValue() : 0.0);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener estadísticas de ventas: " + e.getMessage());
        }

        // 3. Obtener estadísticas de Pagos de Clientes
        String sqlPagos = "SELECT COUNT(*), SUM(monto_pagado), AVG(monto_pagado), MAX(monto_pagado) FROM pagos_clientes WHERE cliente_id = ?";
        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sqlPagos)) {
            ps.setInt(1, clienteId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    stats.put("cant_pagos", rs.getInt(1));
                    stats.put("total_pagos", rs.getBigDecimal(2) != null ? rs.getBigDecimal(2).doubleValue() : 0.0);
                    stats.put("prom_pagos", rs.getBigDecimal(3) != null ? rs.getBigDecimal(3).doubleValue() : 0.0);
                    stats.put("max_pago", rs.getBigDecimal(4) != null ? rs.getBigDecimal(4).doubleValue() : 0.0);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener estadísticas de pagos: " + e.getMessage());
        }

        return stats;
    }

    public String obtenerRutCliente(int clienteId) {
        String rut = "";
        String sql = "SELECT rut FROM clientes WHERE id = ?";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rut = rs.getString("rut");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener el RUT del cliente: " + e.getMessage());
        }

        return rut;
    }

    /**
     * Obtiene el detalle de la cartola de ventas y pagos de un cliente por
     * rango de fechas.
     *
     * @param clienteId
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    public java.util.List<Object[]> obtenerCartolaClientePorVentas(int clienteId, java.util.Date fechaInicio, java.util.Date fechaFin) {
        java.util.List<Object[]> listaDetalle = new java.util.ArrayList<>();

        String sqlVentas = "SELECT fecha_emision AS fecha, tipo_documento AS documento, numero_documento AS n_doc, "
                + "total AS cargo, 0.0 AS abono FROM ventas WHERE cliente_id = ? ";

        String sqlPagos = "SELECT TO_DATE(fecha, 'YYYY-MM-DD') AS fecha, 'RECIBO PAGO' AS documento, numero_recibo AS n_doc, "
                + "0.0 AS cargo, monto_pagado AS abono FROM pagos_clientes WHERE cliente_id = ? ";

        String sqlFinal = "SELECT * FROM (" + sqlVentas + " UNION ALL " + sqlPagos + ") sub ORDER BY fecha ASC";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sqlFinal)) {

            ps.setInt(1, clienteId);
            ps.setInt(2, clienteId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[5];
                    fila[0] = rs.getDate("fecha");          // Fecha
                    fila[1] = rs.getString("documento");      // Tipo Documento / Recibo
                    fila[2] = rs.getString("n_doc");          // N° Documento
                    fila[3] = rs.getDouble("cargo");          // Facturado / Cargo
                    fila[4] = rs.getDouble("abono");          // Pagado / Abono
                    listaDetalle.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener cartola: " + e.getMessage());
        }

        return listaDetalle;
    }

    /**
     * Busca los datos de un cliente por su RUT. Retorna un arreglo con {id,
     * nombre} o null si no existe.
     *
     * @param rut
     * @return
     */
    public Object[] buscarClientePorRut(String rut) {
        Object[] cliente = null;
        String sql = "SELECT id, nombre FROM clientes WHERE rut = ?";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rut);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente = new Object[2];
                    cliente[0] = rs.getInt("id");
                    cliente[1] = rs.getString("nombre");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al buscar cliente por RUT: " + e.getMessage());
        }

        return cliente;
    }

    /**
     * Obtiene la lista de sectores o zonas desde la base de datos para el
     * ComboBox.
     *
     * @return
     */
    public java.util.List<String> obtenerSectoresZonas() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        // Usamos 'comuna' que es la columna real que existe en tu tabla clientes
        String sql = "SELECT DISTINCT comuna FROM clientes WHERE comuna IS NOT NULL AND comuna != '' ORDER BY comuna";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String val = rs.getString("comuna");
                if (val != null && !val.trim().isEmpty()) {
                    lista.add(val.trim());
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener comunas para el combo: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Obtiene las cuentas por cobrar filtradas por sector/zona. Retorna una
     * lista de arreglos con los datos necesarios.
     *
     * @param sectorZona
     * @return
     */
    public java.util.List<Object[]> obtenerCuentasPorCobrar(String sectorZona) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();

        // Usamos directamente el saldo pendiente que ya trae la tabla clientes, 
        // o calculamos los saldos de las ventas de forma segura.
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.rut, c.nombre, c.comuna, c.saldo_pendiente ");
        sql.append("FROM clientes c ");

        boolean filtrarZona = (sectorZona != null && !sectorZona.equals("TODOS"));
        if (filtrarZona) {
            sql.append("WHERE c.comuna = ? ");
        }

        sql.append("AND c.saldo_pendiente > 0 ");
        sql.append("ORDER BY c.nombre ASC");

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {

            if (filtrarZona) {
                ps.setString(1, sectorZona);
            }

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[4];
                    fila[0] = rs.getString("rut");
                    fila[1] = rs.getString("nombre");
                    fila[2] = rs.getString("comuna");
                    fila[3] = rs.getDouble("saldo_pendiente"); // Usamos el saldo directo de clientes
                    lista.add(fila);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al consultar cuentas por cobrar: " + e.getMessage());
        }

        return lista;
    }

}
