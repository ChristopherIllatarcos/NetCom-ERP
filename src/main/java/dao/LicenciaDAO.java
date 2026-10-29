package dao;

import conexion.ConexionDB;
import modelo.Licencia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LicenciaDAO {

    /**
     * Consulta la BD y retorna un objeto Licencia con TODOS los campos de la
     * tabla.
     * @return 
     */
    public Licencia obtenerLicenciaActiva() {
        Licencia lic = null;
        String sql = "SELECT id, rut_licenciatario, razon_social, estado, tipo_licencia, "
                + "cajas_nodos_permitidos, en_uso, fecha_activacion, fecha_vencimiento, serial_key "
                + "FROM licencia LIMIT 1;";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                lic = new Licencia();
                lic.setId(rs.getInt("id"));
                lic.setRutLicenciatario(rs.getString("rut_licenciatario"));
                lic.setRazonSocial(rs.getString("razon_social"));
                lic.setEstado(rs.getString("estado"));
                lic.setTipoLicencia(rs.getString("tipo_licencia"));
                lic.setCajasNodosPermitidos(rs.getInt("cajas_nodos_permitidos"));
                lic.setEnUso(rs.getInt("en_uso"));
                lic.setFechaActivacion(rs.getDate("fecha_activacion"));
                lic.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
                lic.setSerialKey(rs.getString("serial_key"));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar la licencia: " + e.getMessage());
        }
        return lic;
    }

    /**
     * Actualiza la clave de licencia (serial_key) en la base de datos.
     * @param nuevaClave
     * @return 
     */
    public boolean actualizarClaveLicencia(String nuevaClave) {
        String sql = "UPDATE licencia SET serial_key = ? WHERE id = (SELECT id FROM licencia LIMIT 1);";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaClave);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar la clave de licencia: " + e.getMessage());
            return false;
        }
    }
}
