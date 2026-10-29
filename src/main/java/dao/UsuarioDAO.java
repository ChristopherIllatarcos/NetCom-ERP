package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Usuario;

public class UsuarioDAO {

    // 1. OBTENER USUARIO POR ID
    public Usuario obtenerUsuarioPorId(int id) {
        String sql = "SELECT id, username, password_hash, nombre_completo, email, rol, activo, permisos, ultimo_acceso, created_at, updated_at "
                + "FROM usuarios WHERE id = ?";
        Usuario usuario = null;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }

        return usuario;
    }

    // 2. OBTENER USUARIO POR USERNAME
    public Usuario obtenerUsuarioPorUsername(String username) {
        String sql = "SELECT id, username, password_hash, nombre_completo, email, rol, activo, permisos, ultimo_acceso, created_at, updated_at "
                + "FROM usuarios WHERE username = ?";
        Usuario usuario = null;

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por Username: " + e.getMessage());
        }

        return usuario;
    }

    // 3. LISTAR TODOS LOS USUARIOS
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, nombre_completo, email, rol, activo, permisos, ultimo_acceso, created_at, updated_at "
                + "FROM usuarios ORDER BY id ASC";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    // 4. GUARDAR (INSERTAR NUEVO USUARIO)
    public boolean guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre_completo, email, rol, activo, permisos, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getNombreCompleto());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getRol());
            ps.setBoolean(6, usuario.isActivo());

            // Si permisos es null o vacío, guardamos un jsonb vacío por defecto '{}'
            String perm = (usuario.getPermisos() != null && !usuario.getPermisos().trim().isEmpty()) ? usuario.getPermisos() : "{}";
            ps.setString(7, perm);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            return false;
        }
    }

    // 5. ACTUALIZAR (EDITAR USUARIO EXISTENTE)
    public boolean actualizar(Usuario usuario, boolean cambiarPassword) {
        String sql;
        if (cambiarPassword) {
            sql = "UPDATE usuarios SET username = ?, password_hash = ?, nombre_completo = ?, email = ?, rol = ?, activo = ?, permisos = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        } else {
            sql = "UPDATE usuarios SET username = ?, nombre_completo = ?, email = ?, rol = ?, activo = ?, permisos = ?::jsonb, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        }

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, usuario.getUsername());
            if (cambiarPassword) {
                ps.setString(idx++, usuario.getPasswordHash());
            }
            ps.setString(idx++, usuario.getNombreCompleto());
            ps.setString(idx++, usuario.getEmail());
            ps.setString(idx++, usuario.getRol());
            ps.setBoolean(idx++, usuario.isActivo());

            String perm = (usuario.getPermisos() != null && !usuario.getPermisos().trim().isEmpty()) ? usuario.getPermisos() : "{}";
            ps.setString(idx++, perm);

            ps.setInt(idx, usuario.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // 6. ELIMINAR USUARIO POR ID
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    // --- HELPER PARA MAPEAR RESULTSET ---
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setEmail(rs.getString("email"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        usuario.setPermisos(rs.getString("permisos"));
        usuario.setUltimoAcceso(rs.getTimestamp("ultimo_acceso"));
        usuario.setCreatedAt(rs.getTimestamp("created_at"));
        usuario.setUpdatedAt(rs.getTimestamp("updated_at"));
        return usuario;
    }

    /**
     * Registra un nuevo usuario en la base de datos (PostgreSQL)
     *
     * @param nuevoUsuario Objeto con los datos capturados en el formulario
     * @return true si se guardó con éxito, false si ocurrió un error
     */
    public boolean registrarUsuario(Usuario nuevoUsuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, nombre_completo, email, rol, activo, permisos) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoUsuario.getUsername());
            ps.setString(2, nuevoUsuario.getPasswordHash());
            ps.setString(3, nuevoUsuario.getNombreCompleto());
            ps.setString(4, nuevoUsuario.getEmail());
            ps.setString(5, nuevoUsuario.getRol() != null ? nuevoUsuario.getRol() : "USUARIO");
            ps.setBoolean(6, nuevoUsuario.isActivo()); // Queda false para que el admin lo apruebe
            ps.setString(7, nuevoUsuario.getPermisos() != null ? nuevoUsuario.getPermisos() : "{}");

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar el usuario: " + e.getMessage());
            return false;
        }
    }

    // Verificar existencia de usuario o email para recuperación de contraseña
    public boolean verificarExistencia(String datoIngresado) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? OR email = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, datoIngresado.trim());
            ps.setString(2, datoIngresado.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de usuario/email: " + e.getMessage());
        }
        return false;
    }

    // Método para que el Administrador reactive y blanquee la contraseña del usuario
    public boolean desbloquearYActualizarPassword(int idUsuario, String nuevaPasswordHash) {
        String sql = "UPDATE usuarios SET password_hash = ?, activo = true, intentos_fallidos = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaPasswordHash);
            ps.setInt(2, idUsuario);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al desbloquear usuario: " + e.getMessage());
            return false;
        }
    }

    // Incrementar en 1 los intentos fallidos y bloquear si llega a 3
    public void incrementarIntentoFallido(int idUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = COALESCE(intentos_fallidos, 0) + 1, "
                + "activo = CASE WHEN COALESCE(intentos_fallidos, 0) + 1 >= 3 THEN false ELSE activo END "
                + "WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al incrementar intentos fallidos: " + e.getMessage());
        }
    }

    // Reiniciar los intentos a 0 cuando el usuario entra exitosamente
    public void reiniciarIntentosFallidos(int idUsuario) {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0 WHERE id = ?";
        try (Connection con = ConexionDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al reiniciar intentos fallidos: " + e.getMessage());
        }
    }

}
