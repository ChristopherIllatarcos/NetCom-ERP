package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FoliadorDAO {

    // Obtener Tipos de Documento para el combo
    public List<String> obtenerTiposDocumento() {
        List<String> lista = new ArrayList<>();
        lista.add("Factura Electrónica");
        lista.add("Boleta Electrónica");
        lista.add("Nota de Crédito");
        lista.add("Guía de Despacho");
        return lista;
    }

    // Obtener Estados para el combo
    public List<String> obtenerEstadosFolio() {
        List<String> lista = new ArrayList<>();
        lista.add("Activo");
        lista.add("Agotado");
        lista.add("Anulado");
        return lista;
    }

    // Listar todos los rangos registrados para la tabla tblRango
    public List<Object[]> obtenerRangosFolios() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT tipo_documento, serie_imprenta, folio_inicial, folio_final, folio_actual, estado FROM foliador_rangos ORDER BY id DESC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[6];
                fila[0] = rs.getString("tipo_documento");
                fila[1] = rs.getString("serie_imprenta");
                fila[2] = rs.getInt("folio_inicial");
                fila[3] = rs.getInt("folio_final");
                fila[4] = rs.getInt("folio_actual");
                fila[5] = rs.getString("estado");
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener rangos de folios: " + e.getMessage());
        }

        return lista;
    }

    // Guardar un nuevo rango de folios
    public boolean guardarRango(String tipoDoc, String serie, int ini, int fin, int actual, String estado) {
        String sql = "INSERT INTO foliador_rangos (tipo_documento, serie_imprenta, folio_inicial, folio_final, folio_actual, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipoDoc);
            ps.setString(2, serie);
            ps.setInt(3, ini);
            ps.setInt(4, fin);
            ps.setInt(5, actual);
            ps.setString(6, estado);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar rango: " + e.getMessage());
            return false;
        }
    }

    // Anular o cambiar el estado de un rango en la base de datos
    public boolean actualizarEstadoRango(String tipoDoc, String serie, String nuevoEstado) {
        String sql = "UPDATE foliador_rangos SET estado = ? WHERE tipo_documento = ? AND serie_imprenta = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setString(2, tipoDoc);
            ps.setString(3, serie);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del rango: " + e.getMessage());
            return false;
        }
    }

    // Modificar un rango existente en la base de datos
    public boolean modificarRango(String tipoDoc, String serie, int ini, int fin, int actual, String estado, String serieOriginal) {
        String sql = "UPDATE foliador_rangos SET tipo_documento = ?, serie_imprenta = ?, folio_inicial = ?, folio_final = ?, folio_actual = ?, estado = ? WHERE serie_imprenta = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tipoDoc);
            ps.setString(2, serie);
            ps.setInt(3, ini);
            ps.setInt(4, fin);
            ps.setInt(5, actual);
            ps.setString(6, estado);
            ps.setString(7, serieOriginal); // Filtro para actualizar la fila correcta

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al modificar el rango: " + e.getMessage());
            return false;
        }
    }

}
