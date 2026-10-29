package dao;

import conexion.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import modelo.AccesoFuncion;


public class AccesoFuncionDAO {

    // 1. Obtener Roles únicos desde la tabla usuarios
    public List<String> obtenerRoles() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT DISTINCT rol FROM usuarios WHERE rol IS NOT NULL AND TRIM(rol) != '' ORDER BY rol";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(rs.getString("rol"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener roles: " + e.getMessage());
        }
        return roles;
    }

    // 2. Obtener Usuarios (Username) desde la tabla usuarios
    public List<String> obtenerUsuarios() {
        List<String> usuarios = new ArrayList<>();
        String sql = "SELECT username FROM usuarios ORDER BY username";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    // 3. Cargar lista de permisos por tipo ("ROL" o "USUARIO") y su valor ("Administrador", "j.cierra", etc.)
    public List<AccesoFuncion> listarPermisos(String tipoAsignacion, String referencia) {
        List<AccesoFuncion> lista = new ArrayList<>();
        String sql = "SELECT id, tipo_asignacion, referencia, modulo, codigo_funcion, nombre_funcion, permitido "
                + "FROM accesos_funciones WHERE tipo_asignacion = ? AND referencia = ? ORDER BY modulo, nombre_funcion";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipoAsignacion);
            ps.setString(2, referencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AccesoFuncion af = new AccesoFuncion(
                            rs.getInt("id"),
                            rs.getString("tipo_asignacion"),
                            rs.getString("referencia"),
                            rs.getString("modulo"),
                            rs.getString("codigo_funcion"),
                            rs.getString("nombre_funcion"),
                            rs.getBoolean("permitido")
                    );
                    lista.add(af);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar permisos: " + e.getMessage());
        }
        return lista;
    }

    // 4. Guardar / Actualizar lista masiva de permisos
    public boolean guardarPermisos(List<AccesoFuncion> listaPermisos) {
        String sql = "INSERT INTO accesos_funciones (tipo_asignacion, referencia, modulo, codigo_funcion, nombre_funcion, permitido) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (tipo_asignacion, referencia, codigo_funcion) "
                + "DO UPDATE SET permitido = EXCLUDED.permitido";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            con.setAutoCommit(false); // Transacción para mayor rapidez y seguridad

            for (AccesoFuncion af : listaPermisos) {
                ps.setString(1, af.getTipoAsignacion());
                ps.setString(2, af.getReferencia());
                ps.setString(3, af.getModulo());
                ps.setString(4, af.getCodigoFuncion());
                ps.setString(5, af.getNombreFuncion());
                ps.setBoolean(6, af.isPermitido());
                ps.addBatch();
            }

            ps.executeBatch();
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar permisos: " + e.getMessage());
            return false;
        }
    }
}
