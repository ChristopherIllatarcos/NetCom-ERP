package dao;

import conexion.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HojaRutaDAO {

    public modelo.VentaRuta buscarVentaPorFolioYTipo(int folio, String tipoDoc) {
        String sql = "SELECT v.id AS venta_id, v.folio, v.tipo_documento, c.id AS cliente_id, c.nombre, "
                + "COALESCE(c.direccion, 'Sin Dirección') AS direccion, v.total, v.estado "
                + "FROM ventas v "
                + "LEFT JOIN clientes c ON v.cliente_id = c.id "
                + "WHERE v.folio = ? AND UPPER(v.tipo_documento) = UPPER(?) LIMIT 1";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, folio);
            ps.setString(2, tipoDoc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new modelo.VentaRuta(
                            rs.getInt("folio"),
                            rs.getString("tipo_documento"),
                            rs.getString("nombre") != null ? rs.getString("nombre") : "CLIENTE GENERAL",
                            rs.getString("direccion"),
                            rs.getDouble("total"),
                            1, // Bultos por defecto
                            rs.getString("estado") != null ? rs.getString("estado") : "EMITIDA",
                            rs.getInt("venta_id"),
                            rs.getInt("cliente_id")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar venta por folio y tipo: " + e.getMessage());
        }
        return null;
    }

    // MODIFICADO: Ahora recibe el idUsuario real para no dejarlo quemado en 1
    public boolean guardarHojaRuta(List<modelo.VentaRuta> listaVentas, int usuarioId) {
        // CORREGIDO: Usamos el usuarioId en 'creado_por' y un valor por defecto (o 1) en 'vendedor_id' 
        // para que no falle la llave foránea si el usuario no es un vendedor de la otra tabla.
        String sqlRuta = "INSERT INTO hojas_ruta (numero, fecha, vendedor_id, estado, creado_por) "
                + "VALUES ((SELECT COALESCE(MAX(numero), 0) + 1 FROM hojas_ruta), CURRENT_DATE, 1, 'EMITIDA', ?) RETURNING id";

        String sqlDetalle = "INSERT INTO detalle_ruta (hoja_ruta_id, cliente_id, orden_visita, venta_id, estado_visita) "
                + "VALUES (?, ?, ?, ?, 'PENDIENTE')";

        Connection con = null;
        try {
            con = ConexionDB.getConnection();
            con.setAutoCommit(false);

            int hojaRutaId = -1;

            try (PreparedStatement psRuta = con.prepareStatement(sqlRuta)) {
                psRuta.setInt(1, usuarioId); // Aquí se va el ID del usuario logueado en 'creado_por'
                try (ResultSet rs = psRuta.executeQuery()) {
                    if (rs.next()) {
                        hojaRutaId = rs.getInt(1);
                    }
                }
            }

            if (hojaRutaId != -1) {
                try (PreparedStatement psDet = con.prepareStatement(sqlDetalle)) {
                    for (int i = 0; i < listaVentas.size(); i++) {
                        modelo.VentaRuta item = listaVentas.get(i);

                        psDet.setInt(1, hojaRutaId);
                        psDet.setInt(2, item.getClienteId());
                        psDet.setInt(3, i + 1); // Orden de visita
                        psDet.setInt(4, item.getVentaId());
                        psDet.addBatch();
                    }
                    psDet.executeBatch();
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
            System.err.println("Error al guardar Hoja de Ruta: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public List<Object[]> buscarHojasRuta(java.util.Date desde, java.util.Date hasta, String numRuta, String filtroObs) {
        List<Object[]> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hr.id, hr.numero, hr.fecha, ")
                .append("COALESCE(u.nombre_completo, u.username, 'Usuario ' || hr.creado_por) AS nombre_usuario, ")
                .append("COUNT(dr.id) AS total_docs, hr.estado, COALESCE(hr.observaciones, '') AS observaciones ")
                .append("FROM hojas_ruta hr ")
                .append("LEFT JOIN usuarios u ON hr.creado_por = u.id ")
                .append("LEFT JOIN detalle_ruta dr ON hr.id = dr.hoja_ruta_id ")
                .append("WHERE 1=1 ");

        if (numRuta != null && !numRuta.trim().isEmpty()) {
            sql.append("AND hr.numero = ? ");
        } else {
            if (desde != null) {
                sql.append("AND hr.fecha >= ? ");
            }
            if (hasta != null) {
                sql.append("AND hr.fecha <= ? ");
            }
        }

        // 🔍 Filtro adicional por la observación (busca coincidencias con ILIKE)
        if (filtroObs != null && !filtroObs.trim().isEmpty()) {
            sql.append("AND hr.observaciones ILIKE ? ");
        }

        sql.append("GROUP BY hr.id, hr.numero, hr.fecha, u.nombre_completo, u.username, hr.creado_por, hr.estado, hr.observaciones ")
                .append("ORDER BY hr.id DESC");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (numRuta != null && !numRuta.trim().isEmpty()) {
                ps.setInt(paramIndex++, Integer.parseInt(numRuta.trim()));
            } else {
                if (desde != null) {
                    ps.setDate(paramIndex++, new java.sql.Date(desde.getTime()));
                }
                if (hasta != null) {
                    ps.setDate(paramIndex++, new java.sql.Date(hasta.getTime()));
                }
            }

            // Asignamos el parámetro del texto de la observación si viene con contenido
            if (filtroObs != null && !filtroObs.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + filtroObs.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("numero"),
                        rs.getDate("fecha"),
                        rs.getString("nombre_usuario"),
                        rs.getInt("total_docs"),
                        rs.getString("estado"),
                        rs.getString("observaciones")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar hojas de ruta: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("El número de ruta ingresado debe ser numérico: " + e.getMessage());
        }
        return lista;
    }

    // 2. Método para obtener el detalle de folios/documentos de una hoja de ruta seleccionada
    public List<Object[]> obtenerDetalleRuta(int hojaRutaId) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT v.folio, v.tipo_documento, "
                + "COALESCE(c.nombre, 'CLIENTE GENERAL') AS cliente, "
                + "COALESCE(c.direccion, 'Sin Dirección') AS direccion, "
                + "v.total AS monto, 1 AS bulto "
                + "FROM detalle_ruta dr "
                + "INNER JOIN ventas v ON dr.venta_id = v.id "
                + "LEFT JOIN clientes c ON v.cliente_id = c.id "
                + "WHERE dr.hoja_ruta_id = ? "
                + "ORDER BY dr.id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, hojaRutaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("folio"),
                        rs.getString("tipo_documento"),
                        rs.getString("cliente"),
                        rs.getString("direccion"),
                        rs.getDouble("monto"),
                        rs.getInt("bulto")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalle de ruta: " + e.getMessage());
        }
        return lista;
    }

    // 3. Método para anular una hoja de ruta
    public boolean anularHojaRuta(int hojaRutaId) {
        String sql = "UPDATE hojas_ruta SET estado = 'ANULADO' WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, hojaRutaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al anular hoja de ruta: " + e.getMessage());
            return false;
        }
    }

    // Método para actualizar la observación de una Hoja de Ruta por su número
    public boolean actualizarObservacion(int numeroRuta, String nuevaObservacion) {
        String sql = "UPDATE hojas_ruta SET observaciones = ? WHERE numero = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevaObservacion != null ? nuevaObservacion.trim() : "");
            ps.setInt(2, numeroRuta);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar la observación: " + e.getMessage());
            return false;
        }
    }

}
