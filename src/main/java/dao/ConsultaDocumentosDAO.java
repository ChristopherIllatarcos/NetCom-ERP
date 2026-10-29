package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author caill
 */
public class ConsultaDocumentosDAO {

    // 1. Obtener la cabecera del documento de venta
    public modelo.Venta buscarVentaPorDocumento(String tipoDoc, int folio, String rutCliente) {
        modelo.Venta venta = null;

        // Consulta SQL inmune a tildes y mayúsculas
        StringBuilder sql = new StringBuilder(
                "SELECT v.id, v.tipo_documento, v.folio, v.fecha_emision, v.cliente_id, "
                + "c.rut AS rut_cliente, c.nombre AS nombre_cliente, v.forma_pago, "
                + "v.subtotal, v.iva, v.total "
                + "FROM ventas v "
                + "LEFT JOIN clientes c ON v.cliente_id = c.id "
                + "WHERE 1=1 "
        );

        // Si hay folio, priorizamos el folio (es la búsqueda más precisa)
        if (folio > 0) {
            sql.append(" AND v.folio = ? ");
        } else {
            // Filtrar tipo de documento ignorando tildes y mayúsculas/minúsculas
            if (tipoDoc != null && !tipoDoc.isEmpty() && !tipoDoc.equals("...")) {
                sql.append(" AND LOWER(TRANSLATE(v.tipo_documento, 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou')) ");
                sql.append("   LIKE LOWER(TRANSLATE(?, 'ÁÉÍÓÚáéíóú', 'AEIOUaeiou')) ");
            }

            // Filtrar RUT ignorando puntos y guiones
            if (rutCliente != null && !rutCliente.trim().isEmpty()) {
                sql.append(" AND REPLACE(REPLACE(c.rut, '-', ''), '.', '') ");
                sql.append("   ILIKE REPLACE(REPLACE(?, '-', ''), '.', '') ");
            }
        }

        sql.append(" ORDER BY v.id DESC LIMIT 1");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (folio > 0) {
                ps.setInt(paramIndex++, folio);
            } else {
                if (tipoDoc != null && !tipoDoc.isEmpty() && !tipoDoc.equals("...")) {
                    ps.setString(paramIndex++, "%" + tipoDoc.trim() + "%");
                }
                if (rutCliente != null && !rutCliente.trim().isEmpty()) {
                    ps.setString(paramIndex++, "%" + rutCliente.trim() + "%");
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    venta = new modelo.Venta();
                    venta.setId(rs.getInt("id"));
                    venta.setTipoDoc(rs.getString("tipo_documento"));
                    venta.setFolio(rs.getInt("folio"));
                    venta.setFecha(rs.getDate("fecha_emision"));
                    venta.setRutCliente(rs.getString("rut_cliente"));
                    venta.setNombreCliente(rs.getString("nombre_cliente"));
                    venta.setCondicionPago(rs.getString("forma_pago"));
                    venta.setNeto(rs.getDouble("subtotal"));
                    venta.setIva(rs.getDouble("iva"));
                    venta.setTotal(rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el documento: " + e.getMessage());
        }
        return venta;
    }

    // 2. Obtener el detalle de la venta
    public List<modelo.DetalleVenta> obtenerDetalleVenta(int idVenta) {
        List<modelo.DetalleVenta> lista = new ArrayList<>();

        String sql = "SELECT p.codigo AS codigo_prod, p.nombre AS descripcion_prod, "
                + "d.cantidad, d.precio_unitario "
                + "FROM detalle_ventas d "
                + "LEFT JOIN productos p ON d.producto_id = p.id "
                + "WHERE d.venta_id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.DetalleVenta det = new modelo.DetalleVenta();
                    det.setCodigo(rs.getString("codigo_prod"));
                    det.setDescripcion(rs.getString("descripcion_prod"));
                    det.setCantidad(rs.getInt("cantidad"));
                    det.setPrecioUnitario(rs.getDouble("precio_unitario"));

                    lista.add(det);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar el detalle: " + e.getMessage());
        }
        return lista;
    }

    // Método para consultar cobranzas y comisiones por período y vendedor
    public List<Object[]> obtenerComisionesPorCobranza(java.util.Date desde, java.util.Date hasta, int idVendedor) {
        List<Object[]> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT v.folio, ven.nombre, v.total, COALESCE(ven.comision_cobranza, 0.0) AS pct, ")
                .append("       (v.total * COALESCE(ven.comision_cobranza, 0.0) / 100.0) AS comision ")
                .append("FROM ventas v ")
                .append("INNER JOIN vendedores ven ON v.vendedor_id = ven.id ")
                .append("WHERE v.fecha_emision BETWEEN ? AND ? "); // Usa fecha_emision real

        if (idVendedor > 0) {
            sql.append("AND v.vendedor_id = ? ");
        }

        sql.append("ORDER BY v.folio ASC");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            if (idVendedor > 0) {
                ps.setInt(3, idVendedor);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("folio"), // N° Recibo / Folio
                        rs.getString("nombre"), // Nombre Vendedor
                        rs.getDouble("total"), // Cobranza Total
                        rs.getDouble("pct"), // % Com.
                        rs.getDouble("comision") // $ Comision
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar comisiones: " + e.getMessage());
        }

        return lista;
    }
}
