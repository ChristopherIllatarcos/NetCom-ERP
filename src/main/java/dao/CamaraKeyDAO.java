package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CamaraKeyDAO {

    /**
     * Obtiene las cajas o terminales disponibles.
     *
     * @return
     */
    public List<String> obtenerCajasTerminales() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_terminal FROM terminales_pos WHERE activo = true ORDER BY nombre_terminal";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre_terminal"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener terminales POS: " + e.getMessage());
            // Fallback por si acaso
            lista.add("Caja Principal 01");
        }

        return lista;
    }

    /**
     * Obtiene el mapeo de teclas según la caja y el perfil seleccionado.
     *
     * @param terminal
     * @param perfil
     * @return
     */
    public List<Object[]> obtenerMapeoTeclas(String terminal, String perfil) {
        List<Object[]> lista = new ArrayList<>();
        // Simulación o consulta a tu tabla de mapeo de teclas
        String sql = "SELECT tecla, accion, producto, estado FROM mapeo_teclas WHERE terminal = ? AND perfil = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, terminal);
            ps.setString(2, perfil);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[4];
                    fila[0] = rs.getString("tecla");
                    fila[1] = rs.getString("accion");
                    fila[2] = rs.getString("producto");
                    fila[3] = rs.getBoolean("estado") ? "Activo" : "Inactivo";
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener mapeo de teclas: " + e.getMessage());
        }

        return lista;
    }

    public boolean guardarMapeoTeclas(String terminal, String perfil, javax.swing.table.DefaultTableModel modelo) {
        // Primero podemos limpiar los registros anteriores de este terminal/perfil para reemplazarlos con los actuales
        String sqlDelete = "DELETE FROM mapeo_teclas WHERE terminal = ? AND perfil = ?";
        String sqlInsert = "INSERT INTO mapeo_teclas (terminal, perfil, tecla, accion, producto, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false); // Transacción segura

            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setString(1, terminal);
                psDel.setString(2, perfil);
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                for (int i = 0; i < modelo.getRowCount(); i++) {
                    String tecla = modelo.getValueAt(i, 0).toString();
                    String accion = modelo.getValueAt(i, 1).toString();
                    String producto = modelo.getValueAt(i, 2).toString();
                    boolean estado = modelo.getValueAt(i, 3).toString().equalsIgnoreCase("Activo");

                    psIns.setString(1, terminal);
                    psIns.setString(2, perfil);
                    psIns.setString(3, tecla);
                    psIns.setString(4, accion);
                    psIns.setString(5, producto);
                    psIns.setBoolean(6, estado);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar mapeo: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarNuevaCaja(String nombreTerminal) {
        String sql = "INSERT INTO terminales_pos (codigo, nombre_terminal, activo) VALUES (?, ?, true)";

        // Generamos un código automático simple basado en el nombre o la fecha
        String codigoAuto = "POS-" + System.currentTimeMillis() % 1000;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoAuto);
            ps.setString(2, nombreTerminal);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar nueva caja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los perfiles de teclado activos.
     * @return 
     */
    public List<String> obtenerPerfilesTeclado() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_perfil FROM perfiles_teclado WHERE activo = true ORDER BY nombre_perfil";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre_perfil"));
            }
        } catch (SQLException e) {
            System.err.println("Aviso perfiles: " + e.getMessage());
            // Fallback por si la tabla recién se está creando
            lista.add("Perfil Estándar POS");
            lista.add("Perfil Rápido - Almacén");
        }

        return lista;
    }

    /**
     * Registra un nuevo perfil de teclado en la base de datos.
     * @param nombrePerfil
     * @return 
     */
    public boolean registrarNuevoPerfil(String nombrePerfil) {
        String sql = "INSERT INTO perfiles_teclado (nombre_perfil, activo) VALUES (?, true)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombrePerfil);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar nuevo perfil: " + e.getMessage());
            return false;
        }
    }

}
