package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import conexion.ConexionDB;

public class TipoDocumentoDAO {

    public List<String> listarTiposDocumentoActivos() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documento WHERE activo = TRUE ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        } catch (Exception e) {
            System.err.println("Error al listar tipos de documento: " + e.getMessage());
        }
        return lista;
    }

    public boolean agregarTipoDocumento(String codigo, String nombre) {
        String sql = "INSERT INTO tipos_documento (codigo, nombre, activo) VALUES (?, ?, TRUE)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al agregar tipo de documento: " + e.getMessage());
            return false;
        }
    }

    // Modificar el nombre de un tipo de documento existente
    public boolean actualizarTipoDocumento(String nombreAntiguo, String nuevoNombre) {
        String sql = "UPDATE tipos_documento SET nombre = ? WHERE nombre = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, nombreAntiguo);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar tipo de documento: " + e.getMessage());
            return false;
        }
    }

    // Borrado lógico (Desactivar para preservar integridad referencial)
    public boolean eliminarTipoDocumento(String nombre) {
        String sql = "UPDATE tipos_documento SET activo = FALSE WHERE nombre = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar tipo de documento: " + e.getMessage());
            return false;
        }
    }

    // 1. Método para listar los nombres en el ComboBox
    public java.util.List<String> listarNombresTiposDocumento() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        String sql = "SELECT nombre FROM tipos_documento WHERE activo = true ORDER BY nombre ASC";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al listar nombres: " + e.getMessage());
        }
        return lista;
    }

    // 2. Método para insertar un nuevo tipo de documento (con código automático y activo en true)
    public boolean insertarTipoDocumento(String nombre) {
        // Generamos un código breve o se lo asignamos basado en el nombre (por ejemplo, las primeras letras o un genérico)
        String codigo = nombre.length() >= 3 ? nombre.substring(0, 3).toUpperCase() : "DOC";

        String sql = "INSERT INTO tipos_documento (codigo, nombre, activo) VALUES (?, ?, true)";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLException e) {
            System.err.println("Error al insertar tipo de documento: " + e.getMessage());
            return false;
        }
    }
}
