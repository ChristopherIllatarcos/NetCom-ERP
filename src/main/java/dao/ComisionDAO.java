package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.ComisionVendedor;
import modelo.Venta;

public class ComisionDAO {

    public List<ComisionVendedor> obtenerComisionesPorFecha(Date desde, Date hasta) {
        List<ComisionVendedor> lista = new ArrayList<>();

        // Consulta agrupando por vendedor e integrando la tasa de comisión
        String sql = "SELECT "
                + "  v.id AS vendedor_id, "
                + "  v.nombre AS vendedor_nombre, "
                + "  COALESCE(SUM(ve.total), 0) AS cobranza_total, "
                + "  COALESCE(v.comision_cobranza, v.comision_venta, 0) AS porc_comision "
                + "FROM vendedores v "
                + "INNER JOIN ventas ve ON v.id = ve.vendedor_id "
                + "WHERE ve.fecha_emision >= ? AND ve.fecha_emision <= ? "
                + "  AND UPPER(ve.estado) != 'ANULADA' "
                + "GROUP BY v.id, v.nombre, v.comision_cobranza, v.comision_venta "
                + "ORDER BY v.nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(desde.getTime()));
            ps.setDate(2, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                int contador = 1;
                while (rs.next()) {
                    ComisionVendedor item = new ComisionVendedor();
                    item.setNumeroCobranza(contador++); // N° correlativo para la primera columna
                    item.setVendedorId(rs.getInt("vendedor_id"));
                    item.setNombreVendedor(rs.getString("vendedor_nombre"));

                    double totalCobranza = rs.getDouble("cobranza_total");
                    double porcComision = rs.getDouble("porc_comision");
                    double montoComision = totalCobranza * (porcComision / 100.0);

                    item.setCobranzaTotal(totalCobranza);
                    item.setComision(montoComision);

                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar comisiones: " + e.getMessage());
        }

        return lista;
    }
    
    
   // Método que consulta las ventas individuales de un vendedor para el JDialog
    public List<Venta> obtenerVentasPorVendedorYFecha(int vendedorId, Date desde, Date hasta) {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT id, numero_documento, tipo_documento, folio, fecha_emision, forma_pago, total, estado "
                   + "FROM ventas "
                   + "WHERE vendedor_id = ? "
                   + "  AND fecha_emision >= ? AND fecha_emision <= ? "
                   + "  AND UPPER(estado) != 'ANULADA' "
                   + "ORDER BY fecha_emision DESC";

        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, vendedorId);
            ps.setDate(2, new java.sql.Date(desde.getTime()));
            ps.setDate(3, new java.sql.Date(hasta.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Venta v = new Venta();
                    v.setId(rs.getInt("id"));
                    v.setNumeroDocumento(rs.getString("numero_documento"));
                    v.setTipoDocumento(rs.getString("tipo_documento"));
                    v.setFolio(rs.getInt("folio"));
                    v.setFechaEmision(rs.getDate("fecha_emision"));
                    v.setFormaPago(rs.getString("forma_pago"));
                    v.setTotal(rs.getDouble("total"));
                    v.setEstado(rs.getString("estado"));

                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por vendedor: " + e.getMessage());
        }

        return lista;
    }
}
