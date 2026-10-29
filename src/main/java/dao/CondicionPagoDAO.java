package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import conexion.ConexionDB;

public class CondicionPagoDAO {

    // Listar las condiciones de pago activas para el combo
    public List<String> listarCondicionesActivas() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre FROM condiciones_pago WHERE activo = TRUE ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre"));
            }
        } catch (Exception e) {
            System.err.println("Error al listar condiciones de pago: " + e.getMessage());
        }
        return lista;
    }

    public boolean agregarCondicionPago(String nombre, int dias) {
        // Forzamos 'activo = TRUE' en el INSERT
        String sql = "INSERT INTO condiciones_pago (nombre, dias, activo) VALUES (?, ?, TRUE)";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, dias);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (Exception e) {
            System.err.println("Error al agregar condición de pago: " + e.getMessage());
            return false;
        }
    }

    // Modificar el nombre de una condición de pago existente
    public boolean actualizarCondicionPago(String nombreAntiguo, String nuevoNombre) {
        String sql = "UPDATE condiciones_pago SET nombre = ? WHERE nombre = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, nombreAntiguo);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar condición de pago: " + e.getMessage());
            return false;
        }
    }

// Borrado lógico (Desactivar para no romper el historial de ventas/compras)
    public boolean eliminarCondicionPago(String nombre) {
        String sql = "UPDATE condiciones_pago SET activo = FALSE WHERE nombre = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar condición de pago: " + e.getMessage());
            return false;
        }
    }
}
