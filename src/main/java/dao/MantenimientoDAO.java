package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de ejecutar las rutinas de mantenimiento dinámicas y
 * reindexación general en PostgreSQL para Casa Victoria.
 */
public class MantenimientoDAO {

    /**
     * Obtiene la lista de todas las tablas del esquema público en PostgreSQL.
     *
     * @return Lista de nombres de tablas.
     */
    public List<String> obtenerTodasLasTablas() {
        List<String> tablas = new ArrayList<>();
        String sql = "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;";

        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                tablas.add(rs.getString("tablename"));
            }

        } catch (Exception e) {
            System.err.println("Error al obtener las tablas de la BD: " + e.getMessage());
        }

        return tablas;
    }

    /**
     * Ejecuta una sentencia SQL administrativa (Ej: REINDEX, VACUUM ANALYZE).
     *
     * @param sql Sentencia SQL a ejecutar
     * @return true si se ejecutó con éxito, false si hubo error.
     */
    public boolean ejecutarSentenciaAdmin(String sql) {
        try (Connection con = ConexionDB.getConnection(); Statement st = con.createStatement()) {

            con.setAutoCommit(true);
            st.execute(sql);
            return true;

        } catch (Exception e) {
            System.err.println("Error en MantenimientoDAO [" + sql + "]: " + e.getMessage());
            return false;
        }
    }
}
