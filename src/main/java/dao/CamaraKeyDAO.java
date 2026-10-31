package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.MapeoTecla;

public class CamaraKeyDAO {

    /**
     * Obtiene las cajas o terminales disponibles.
     *
     * @return
     */
    public List<String> obtenerCajasTerminales() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_terminal FROM terminales_pos WHERE activo = true AND nombre_terminal IS NOT NULL AND TRIM(nombre_terminal) != '' ORDER BY nombre_terminal";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre_terminal"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener terminales POS: " + e.getMessage());
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
    public List<MapeoTecla> obtenerMapeoTeclas(String terminal, String perfil) {
        List<MapeoTecla> lista = new ArrayList<>();
        String sql = "SELECT tecla, accion, producto, estado FROM mapeo_teclas WHERE terminal = ? AND perfil = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, terminal);
            ps.setString(2, perfil);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MapeoTecla mapeo = new MapeoTecla(
                            rs.getString("tecla"),
                            rs.getString("accion"),
                            rs.getString("producto"),
                            rs.getBoolean("estado")
                    );
                    lista.add(mapeo);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener mapeo de teclas: " + e.getMessage());
        }

        return lista;
    }

    public boolean guardarMapeoTeclas(String terminal, String perfil, List<MapeoTecla> listaMapeos) {
        if (listaMapeos == null) {
            return false;
        }

        String sqlDelete = "DELETE FROM mapeo_teclas WHERE terminal = ? AND perfil = ?";
        String sqlInsert = "INSERT INTO mapeo_teclas (terminal, perfil, tecla, accion, producto, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false); // Iniciar transacción segura

            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setString(1, terminal);
                psDel.setString(2, perfil);
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = con.prepareStatement(sqlInsert)) {
                for (MapeoTecla m : listaMapeos) {
                    psIns.setString(1, terminal);
                    psIns.setString(2, perfil);
                    psIns.setString(2, perfil);
                    psIns.setString(3, m.getTecla());
                    psIns.setString(4, m.getAccion());
                    psIns.setString(5, m.getProducto());
                    psIns.setBoolean(6, m.isEstado());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            con.commit(); // Confirmar transacción exitosa
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar mapeo de teclas: " + e.getMessage());
            // Nota: Aquí se maneja el rollback de forma segura si el objeto Connection sigue activo
            return false;
        }
    }

    public boolean registrarNuevaCaja(String nombreTerminal) {
        if (nombreTerminal == null || nombreTerminal.trim().isEmpty()) {
            System.err.println("Error: El nombre del terminal no puede estar vacío.");
            return false;
        }

        // Usar timestamp completo o UUID corto para evitar colisiones por duplicidad
        String codigoAuto = "POS-" + System.currentTimeMillis();
        String sql = "INSERT INTO terminales_pos (codigo, nombre_terminal, activo) VALUES (?, ?, true)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, codigoAuto);
            ps.setString(2, nombreTerminal.trim());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar nueva caja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene los perfiles de teclado activos.
     *
     * @return
     */
    public List<String> obtenerPerfilesTeclado() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nombre_perfil FROM perfiles_teclado WHERE activo = true AND nombre_perfil IS NOT NULL AND TRIM(nombre_perfil) != '' ORDER BY nombre_perfil";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nombre_perfil"));
            }
        } catch (SQLException e) {
            System.err.println("Aviso al obtener perfiles de teclado: " + e.getMessage());

        }

        return lista;
    }

    /**
     * Registra un nuevo perfil de teclado en la base de datos.
     *
     * @param nombrePerfil
     * @return
     */
    public boolean registrarNuevoPerfil(String nombrePerfil) {
        if (nombrePerfil == null || nombrePerfil.trim().isEmpty()) {
            System.err.println("Error: El nombre del perfil no puede estar vacío.");
            return false;
        }

        String sql = "INSERT INTO perfiles_teclado (nombre_perfil, activo) VALUES (?, true)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombrePerfil.trim());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar nuevo perfil: " + e.getMessage());
            return false;
        }
    }

}
