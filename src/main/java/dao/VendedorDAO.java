package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import conexion.ConexionDB;
import modelo.Vendedor;

public class VendedorDAO {

    // 1. Guardar
    public boolean guardarVendedor(Vendedor v) {
        String sql = "INSERT INTO vendedores (codigo, nombre, comision_venta, comision_cobranza, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getCodigo());
            ps.setString(2, v.getNombre());
            ps.setDouble(3, v.getComisionVenta());
            ps.setDouble(4, v.getComisionCobranza());
            ps.setBoolean(5, v.isActivo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar vendedor: " + e.getMessage());
            return false;
        }
    }

    // 2. Actualizar
    public boolean actualizarVendedor(Vendedor v) {
        String sql = "UPDATE vendedores SET codigo = ?, nombre = ?, comision_venta = ?, comision_cobranza = ?, activo = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getCodigo());
            ps.setString(2, v.getNombre());
            ps.setDouble(3, v.getComisionVenta());
            ps.setDouble(4, v.getComisionCobranza());
            ps.setBoolean(5, v.isActivo());
            ps.setInt(6, v.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar vendedor: " + e.getMessage());
            return false;
        }
    }

    public List<Vendedor> listarVendedores() {
        List<Vendedor> lista = new ArrayList<>();
        // Seleccionamos TODOS los campos necesarios de la tabla vendedores
        String sql = "SELECT id, codigo, nombre, tipo, comision_venta, comision_cobranza, activo FROM vendedores ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vendedor v = new Vendedor();
                v.setId(rs.getInt("id"));
                v.setCodigo(rs.getString("codigo"));
                v.setNombre(rs.getString("nombre"));
                v.setTipo(rs.getString("tipo"));
                v.setComisionVenta(rs.getDouble("comision_venta"));
                v.setComisionCobranza(rs.getDouble("comision_cobranza"));
                v.setActivo(rs.getBoolean("activo"));

                lista.add(v);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar vendedores: " + e.getMessage());
        }

        return lista;
    }

    // Eliminar/Desactivar lógicamente un vendedor por su ID
    public boolean eliminarVendedor(int id) {
        // Borrado lógico sin updated_at por si la columna no existe en tu esquema
        String sql = "UPDATE vendedores SET activo = false WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar/desactivar el vendedor: " + e.getMessage());
            return false;
        }
    }

}
