package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import conexion.ConexionDB;
import java.sql.SQLException;

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

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Error de BD: El código del tipo de documento ya se encuentra registrado.");
            } else {
                System.err.println("Error al agregar tipo de documento: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean actualizarTipoDocumento(int id, String nuevoNombre) {
        String sql = "UPDATE tipos_documento SET nombre = ? WHERE id = ?";
        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoNombre);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (java.sql.SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Aviso: El nombre del tipo de documento ya está en uso.");
            } else {
                System.err.println("Error al actualizar tipo de documento: " + e.getMessage());
            }
            return false;
        }
    }

// Método para desactivar el tipo de documento recibiendo su nombre (String)
    public boolean eliminarTipoDocumento(String nombre) {
        String sql = "UPDATE tipos_documento SET activo = false WHERE nombre = ?";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);

            return ps.executeUpdate() > 0;

        } catch (java.sql.SQLException e) {
            System.err.println("Error al desactivar tipo de documento: " + e.getMessage());
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
        // Generamos un código breve basado en el nombre o un genérico
        String codigo = nombre.length() >= 3 ? nombre.substring(0, 3).toUpperCase() : "DOC";

        String sql = "INSERT INTO tipos_documento (codigo, nombre, activo) VALUES (?, ?, true)";

        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigo);
            ps.setString(2, nombre);

            return ps.executeUpdate() > 0;

        } catch (java.sql.SQLException e) {
            // Validamos si es error de duplicidad en PostgreSQL (SQLState 23505)
            if ("23505".equals(e.getSQLState())) {
                System.err.println("Aviso: El tipo de documento '" + nombre + "' ya existe en la base de datos.");
                // Opcional: Puedes lanzar una advertencia más descriptiva si lo manejas arriba
            } else {
                System.err.println("Error al insertar tipo de documento: " + e.getMessage());
            }
            return false;
        }
    }

    public int obtenerIdPorNombre(String nombre) {
        String sql = "SELECT id FROM tipos_documento WHERE nombre = ?";
        try (java.sql.Connection con = ConexionDB.getConnection(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error al obtener ID por nombre: " + e.getMessage());
        }
        return -1; // Retorna -1 si ocurre un error
    }
}
