package dao;

import conexion.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Proveedor;


public class ProveedorDAO {

    // 1. LISTAR TODOS LOS PROVEEDORES
    public List<Proveedor> listarProveedores() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, rut, nombre, direccion, ciudad, telefono, casilla, email, contacto, activo "
                + "FROM proveedores ORDER BY nombre ASC";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setId(rs.getInt("id"));
                p.setRut(rs.getString("rut"));
                p.setNombre(rs.getString("nombre"));
                p.setDireccion(rs.getString("direccion"));
                p.setCiudad(rs.getString("ciudad"));
                p.setTelefono(rs.getString("telefono"));
                p.setCasilla(rs.getString("casilla"));
                p.setEmail(rs.getString("email"));
                p.setContacto(rs.getString("contacto"));
                p.setActivo(rs.getBoolean("activo"));
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar proveedores: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }

        return lista;
    }

    // 2. BUSCAR PROVEEDORES POR NOMBRE O RUT
    public List<Proveedor> buscarProveedores(String criterio, String valor) {
        List<Proveedor> lista = new ArrayList<>();
        String columna = "rut".equalsIgnoreCase(criterio) ? "rut" : "nombre";
        String sql = "SELECT id, rut, nombre, direccion, ciudad, telefono, casilla, email, contacto, activo "
                + "FROM proveedores WHERE LOWER(" + columna + ") LIKE LOWER(?) ORDER BY nombre ASC";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + valor + "%");
            rs = ps.executeQuery();

            while (rs.next()) {
                Proveedor p = new Proveedor();
                p.setId(rs.getInt("id"));
                p.setRut(rs.getString("rut"));
                p.setNombre(rs.getString("nombre"));
                p.setDireccion(rs.getString("direccion"));
                p.setCiudad(rs.getString("ciudad"));
                p.setTelefono(rs.getString("telefono"));
                p.setCasilla(rs.getString("casilla"));
                p.setEmail(rs.getString("email"));
                p.setContacto(rs.getString("contacto"));
                p.setActivo(rs.getBoolean("activo"));
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar proveedores: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }

        return lista;
    }

    // 3. GUARDAR NUEVO PROVEEDOR
    public boolean guardarProveedor(Proveedor p) {
        String sql = "INSERT INTO proveedores (rut, nombre, direccion, ciudad, telefono, casilla, email, contacto, activo, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getRut());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getCiudad());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getCasilla());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getContacto());
            ps.setBoolean(9, p.isActivo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al guardar proveedor: " + e.getMessage());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    // 4. ACTUALIZAR PROVEEDOR
    public boolean actualizarProveedor(Proveedor p) {
        String sql = "UPDATE proveedores SET rut = ?, nombre = ?, direccion = ?, ciudad = ?, "
                + "telefono = ?, casilla = ?, email = ?, contacto = ?, activo = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);

            ps.setString(1, p.getRut());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getDireccion());
            ps.setString(4, p.getCiudad());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getCasilla());
            ps.setString(7, p.getEmail());
            ps.setString(8, p.getContacto());
            ps.setBoolean(9, p.isActivo());
            ps.setInt(10, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar proveedor: " + e.getMessage());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    // 5. DESACTIVAR (DESACTIVACIÓN LÓGICA)
    public boolean eliminarProveedor(int id) {
        String sql = "UPDATE proveedores SET activo = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = ConexionDB.getConnection();
            ps = con.prepareStatement(sql);

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al desactivar proveedor: " + e.getMessage());
            return false;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
