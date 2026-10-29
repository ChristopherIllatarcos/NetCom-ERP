package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ItemCombo; 

public class ImpuestosDAO {

    // --- 1. Listar para el ComboBox principal ---
    public List<ItemCombo> listarImpuestosCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, codigo, nombre, porcentaje FROM impuestos_adicionales WHERE activo = TRUE ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("porcentaje")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar impuestos: " + e.getMessage());
        }

        return lista;
    }

    // --- 2. Listar para Editar/Eliminar ---
    public List<ItemCombo> listarImpuestosComboParaEditar() {
        List<ItemCombo> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, porcentaje FROM impuestos_adicionales WHERE activo = TRUE ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("porcentaje")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar impuestos para edición: " + e.getMessage());
        }

        return lista;
    }

    // --- 3. Guardar nuevo impuesto con código automático ---
    public boolean guardarImpuesto(String nombre, double porcentaje) {
        String codigoAuto = "IMP-" + System.currentTimeMillis() % 1000;
        String sql = "INSERT INTO impuestos_adicionales (codigo, nombre, porcentaje, activo) VALUES (?, ?, ?, TRUE)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoAuto);
            ps.setString(2, nombre);
            ps.setDouble(3, porcentaje);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar impuesto: " + e.getMessage());
            return false;
        }
    }

    // --- 4. Actualizar impuesto existente ---
    public boolean actualizarImpuesto(int id, String nuevoNombre, double nuevoPorcentaje) {
        String sql = "UPDATE impuestos_adicionales SET nombre = ?, porcentaje = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setDouble(2, nuevoPorcentaje);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar impuesto: " + e.getMessage());
            return false;
        }
    }

    // --- 5. Eliminar (o desactivar) impuesto ---
    public boolean eliminarImpuesto(int id) {
        String sql = "DELETE FROM impuestos_adicionales WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar impuesto (puede estar en uso): " + e.getMessage());
            return false;
        }
    }
}
