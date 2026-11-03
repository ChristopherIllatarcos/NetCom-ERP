package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.ItemCombo;
import modelo.LibroCompra;

public class LibroComprasDAO {

    // 1. Retorna la lista pura de proveedores para los combos (Sin tocar Swing)
    public List<ItemCombo> listarProveedoresCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM proveedores WHERE activo = TRUE ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar proveedores en LibroComprasDAO: " + e.getMessage());
        }
        return lista;
    }

    // 2. Consulta pura del libro de compras (Retorna los datos; los totales se calculan o procesan en la vista/controlador)
    public List<LibroCompra> consultarLibroCompras(Date fechaInicio, Date fechaFinal, int proveedorId) {
        List<LibroCompra> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.fecha_emision, c.numero_factura, p.nombre AS proveedor_social, "
                + "c.subtotal, c.iva, c.total "
                + "FROM compras c "
                + "LEFT JOIN proveedores p ON c.proveedor_id = p.id "
                + "WHERE 1=1"
        );

        if (fechaInicio != null) {
            sql.append(" AND c.fecha_emision >= ?");
        }
        if (fechaFinal != null) {
            sql.append(" AND c.fecha_emision <= ?");
        }
        if (proveedorId > 0) {
            sql.append(" AND c.proveedor_id = ?");
        }

        sql.append(" ORDER BY c.fecha_emision ASC, c.id ASC");

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;
            if (fechaInicio != null) {
                ps.setDate(index++, new java.sql.Date(fechaInicio.getTime()));
            }
            if (fechaFinal != null) {
                ps.setDate(index++, new java.sql.Date(fechaFinal.getTime()));
            }
            if (proveedorId > 0) {
                ps.setInt(index++, proveedorId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fecha = rs.getDate("fecha_emision");
                    String nFactura = rs.getString("numero_factura");
                    String proveedor = rs.getString("proveedor_social");
                    double neto = rs.getDouble("subtotal");
                    double iva = rs.getDouble("iva");
                    double total = rs.getDouble("total");

                    lista.add(new LibroCompra(fecha, nFactura, proveedor != null ? proveedor : "SIN PROVEEDOR", neto, iva, total));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar libro de compras: " + e.getMessage());
        }

        return lista;
    }
}
