package dao;

import conexion.ConexionDB;
import modelo.ItemCombo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class ParametrosCorrelativosDAO {

    public modelo.ParametrosEmpresa obtenerParametrosEmpresa() {
        modelo.ParametrosEmpresa emp = null;
        String sql = "SELECT razon_social, rut_empresa, direccion_matriz, telefono, caja_defecto, giro, comuna, email, bodega FROM empresa_parametros LIMIT 1";

        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                emp = new modelo.ParametrosEmpresa();
                emp.setRazonSocial(rs.getString("razon_social"));
                emp.setRutEmpresa(rs.getString("rut_empresa"));
                emp.setDireccionMatriz(rs.getString("direccion_matriz"));
                emp.setTelefono(rs.getString("telefono"));
                emp.setCajaDefecto(rs.getString("caja_defecto"));
                emp.setGiro(rs.getString("giro"));
                emp.setComuna(rs.getString("comuna"));
                emp.setEmail(rs.getString("email"));
                emp.setBodega(rs.getString("bodega"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener parámetros de empresa: " + e.getMessage());
        }
        return emp;
    }

    public boolean guardarParametrosEmpresa(modelo.ParametrosEmpresa emp) {
        String sqlCheck = "SELECT COUNT(*) FROM empresa_parametros";
        String sqlUpdate = "UPDATE empresa_parametros SET razon_social=?, rut_empresa=?, direccion_matriz=?, telefono=?, caja_defecto=?, giro=?, comuna=?, email=?, bodega=?";
        String sqlInsert = "INSERT INTO empresa_parametros (razon_social, rut_empresa, direccion_matriz, telefono, caja_defecto, giro, comuna, email, bodega) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlCheck)) {

            rs.next();
            int count = rs.getInt(1);

            String sqlToExecute = (count > 0) ? sqlUpdate : sqlInsert;

            try (PreparedStatement ps = con.prepareStatement(sqlToExecute)) {
                ps.setString(1, emp.getRazonSocial());
                ps.setString(2, emp.getRutEmpresa());
                ps.setString(3, emp.getDireccionMatriz());
                ps.setString(4, emp.getTelefono());
                ps.setString(5, emp.getCajaDefecto());
                ps.setString(6, emp.getGiro());
                ps.setString(7, emp.getComuna());
                ps.setString(8, emp.getEmail());
                ps.setString(9, emp.getBodega());
                ps.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar parámetros de empresa: " + e.getMessage());
            return false;
        }
    }

    public void cargarCorrelativosTabla(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        String sql = "SELECT id, tipo_documento, prefijo, correlativo_actual, estado FROM correlativos_documentos ORDER BY id";

        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id"),
                    rs.getString("tipo_documento"),
                    rs.getString("prefijo"),
                    rs.getInt("correlativo_actual"),
                    rs.getString("estado")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar correlativos en tabla: " + e.getMessage());
        }
    }

    public boolean actualizarCorrelativo(int id, int nuevoCorrelativo) {
        String sql = "UPDATE correlativos_documentos SET correlativo_actual = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoCorrelativo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar correlativo: " + e.getMessage());
            return false;
        }
    }

    public List<ItemCombo> listarBodegasCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        lista.add(new ItemCombo(0, "-- SELECCIONE BODEGA --"));

        String sql = "SELECT id, nombre FROM bodegas ORDER BY nombre ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar bodegas para combo: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 6. Lista las cajas (terminales POS) desde la base de datos envueltas en
     * objetos ItemCombo.
     *
     * @return
     */
    public List<ItemCombo> listarCajasCombo() {
        List<ItemCombo> lista = new ArrayList<>();
        lista.add(new ItemCombo(0, "-- SELECCIONE CAJA --"));

        String sql = "SELECT id, nombre_terminal FROM terminales_pos WHERE activo = true ORDER BY nombre_terminal ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ItemCombo(rs.getInt("id"), rs.getString("nombre_terminal")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cajas para combo: " + e.getMessage());
        }
        return lista;
    }

    /**
     * 3. Carga el listado de correlativos de documentos en el DefaultTableModel
     * de la JTable.
     *
     * @param modelo
     */
    public void cargarTablaCorrelativos(DefaultTableModel modelo) {
        modelo.setRowCount(0);
        String sql = "SELECT id, tipo_documento, prefijo, correlativo_actual, estado FROM correlativos_documentos ORDER BY id";

        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id"),
                    rs.getString("tipo_documento"),
                    rs.getString("prefijo"),
                    rs.getInt("correlativo_actual"),
                    rs.getString("estado")
                };
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar correlativos en tabla: " + e.getMessage());
        }
    }

}
