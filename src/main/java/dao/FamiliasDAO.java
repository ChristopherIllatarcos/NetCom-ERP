package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.FamiliaDTO;

public class FamiliasDAO {


    public List<FamiliaDTO> listarFamilias() {
        List<FamiliaDTO> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, descripcion FROM familias WHERE activo = TRUE ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new FamiliaDTO(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar familias: " + e.getMessage());
        }
        return lista;
    }

    public boolean guardarFamilia(String codigo, String nombre, String descripcion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = "FAM-" + System.currentTimeMillis() % 10000;
        }

        String sql = "INSERT INTO familias (codigo, nombre, descripcion, activo) VALUES (?, ?, ?, TRUE)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            ps.setString(2, nombre.trim());
            ps.setString(3, descripcion != null ? descripcion.trim() : "");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar familia: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarFamilia(int id, String nombre, String descripcion) {
        String sql = "UPDATE familias SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, descripcion != null ? descripcion.trim() : "");
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar familia: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarFamilia(int id) {
        String sql = "DELETE FROM familias WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar familia: " + e.getMessage());
            return false;
        }
    }
}
