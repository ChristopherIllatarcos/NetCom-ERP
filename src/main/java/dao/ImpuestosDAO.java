package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImpuestosDAO {

    public static class ItemCombo {

        private final int id;
        private final String nombre;
        private final double porcentaje;

        public ItemCombo(int id, String nombre, double porcentaje) {
            this.id = id;
            this.nombre = nombre;
            this.porcentaje = porcentaje;
        }

        public int getId() {
            return id;
        }

        public double getPorcentaje() {
            return porcentaje;
        }

        // --- ¡Agrega este método si te faltaba! ---
        public String getNombrePuro() {
            return nombre;
        }

        @Override
        public String toString() {
            if (porcentaje <= 0) {
                return nombre;
            }
            return nombre + " (" + porcentaje + "%)";
        }
    }

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

    // --- 2. Listar para Editar/Eliminar (excluyendo la opción por defecto si deseas) ---
    public List<ItemCombo> listarImpuestosComboParaEditar() {
        List<ItemCombo> lista = new ArrayList<>();
        // Excluimos el impuesto base con id 1 o porcentaje 0 si prefieres que no lo toquen, 
        // o los traemos todos para que puedan editarlos libremente. Aquí traemos todos los activos:
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
        // Puedes hacer un DELETE físico o un cambio de estado activo = FALSE
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
